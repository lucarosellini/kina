package kina.rdd.mongodb;

import kina.config.MongoConfigFactory;
import kina.config.MongoKinaConfig;
import kina.context.MongoKinaContext;
import kina.entity.Cells;
import kina.testentity.AccountEntity;
import kina.testentity.TransactionFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.rdd.RDD;
import org.bson.BSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by luca on 10/12/14.
 */
@Test(suiteName = "mongoRddTests", groups = {"BSONReadRDDTest"})
public class BSONReadRDDTest {
    private MongoKinaContext context;

    @BeforeMethod
    public void init(){
        context = new MongoKinaContext("local", "testRawBSONReadRDD");
    }

    @AfterMethod
    public void cleanup(){
        context.stop();
    }

    @Test
    public void testRawBSONReadRDD(){
        String bsonFile = getClass().getClassLoader().getResource("dump/").getFile();

        MongoKinaConfig<BSONObject> config =
                MongoConfigFactory.createRawMongoConfig()
                        .bsonFile(bsonFile, true)
                        .bsonFilesExcludePatterns(
                                new String[]{
                                        "[\\w\\W]*\\.metadata\\.json$",
                                        "[\\w\\W]*\\.indexes\\.bson$"})
                        .initialize();

        RDD<BSONObject> rdd = context.mongoRDD(config);

        assertTrue(rdd.count() > 0);

        rdd.foreach(new MyRawTransformer());
    }

    @Test(dependsOnMethods = "testRawBSONReadRDD")
    public void testEntityBSONReadRDD(){
        String bsonFile = getClass().getClassLoader().getResource("dump/business/account.bson").getFile();

        MongoKinaConfig<AccountEntity> inputConfigEntity =
                MongoConfigFactory
                        .createMongoDB(AccountEntity.class)
                        .bsonFile(bsonFile, false)
                        .initialize();

        RDD<AccountEntity> rdd = context.mongoRDD(inputConfigEntity);

        assertTrue(rdd.count() > 0);

        rdd.foreach(new MyEntityTransformer());
    }

    @Test(dependsOnMethods = "testEntityBSONReadRDD")
    public void testCellBSONReadRDD(){
        String bsonFile = getClass().getClassLoader().getResource("dump/").getFile();

        MongoKinaConfig<Cells> inputConfigEntity =
                MongoConfigFactory.createMongoDB()
                        .bsonFile(bsonFile, true)
                        .bsonFilesExcludePatterns(
                                new String[]{
                                        "[\\w\\W]*\\.metadata\\.json$",
                                        "[\\w\\W]*\\.indexes\\.bson$"})
                        .initialize();

        RDD<Cells> rdd = context.mongoRDD(inputConfigEntity);

        assertTrue(rdd.count() > 0);

        rdd.foreach(new MyCellTransformer());
    }

}

class MyEntityTransformer extends AbstractFunction1<AccountEntity, BoxedUnit> implements Serializable {

    @Override
    public BoxedUnit apply(AccountEntity v1) {
        assertNotNull(v1.getAccountAlarm());
        assertNotNull(v1.getAccountAlarm().getAlarmTxFilter());

        List<TransactionFilter> txsFilt = v1.getAccountAlarm().getAlarmTxFilter();

        for (TransactionFilter f : txsFilt) {
            assertNotNull(f.getCategories());
            assertTrue(f.getCategories().size() > 0);
            assertNotNull(f.getThreshold());
        }

        assertNotNull(v1.getAccountAlarm().getAlarmTxFilter().iterator());
        assertNotNull(v1.getAccountAlarm().getBalanceThreshold());

        assertNotNull(v1.getAccountAlarm().getEnabledByType());

        assertTrue(v1.getAccountAlarm().getEnabledByType().size() > 0);

        assertNotNull(v1.getAlias());

        assertTrue(StringUtils.isNotEmpty(v1.getCcc()));
        assertTrue(StringUtils.isEmpty(v1.getUserAlias()));

        assertNotNull(v1.getAccountId());
        assertTrue(StringUtils.isNotEmpty(v1.getAccountId().getBank()));
        assertTrue(StringUtils.isNotEmpty(v1.getAccountId().getBranch()));
        assertTrue(StringUtils.isNotEmpty(v1.getAccountId().getControlDigits()));
        assertTrue(StringUtils.isNotEmpty(v1.getAccountId().getNumber()));

        return BoxedUnit.UNIT;
    }
}

class MyCellTransformer extends AbstractFunction1<Cells, BoxedUnit> implements Serializable {

    @Override
    public BoxedUnit apply(Cells v1) {
        return BoxedUnit.UNIT;
    }
}
class MyRawTransformer extends AbstractFunction1<BSONObject, BoxedUnit> implements Serializable {

    @Override
    public BoxedUnit apply(BSONObject v1) {
        return BoxedUnit.UNIT;
    }
}