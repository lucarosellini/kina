/*
 * Copyright 2014, Luca Rosellini.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kina.rdd.mongodb;

import com.mongodb.DBObject;
import kina.config.MongoKinaConfig;
import kina.config.MongoConfigFactory;
import kina.config.RawMongoKinaConfig;
import kina.context.MongoKinaContext;
import kina.entity.Cells;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.rdd.RDD;
import org.bson.BSONObject;
import org.testng.annotations.Test;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.io.Serializable;

import static kina.rdd.mongodb.MongoJavaRDDTest.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

class MyTransformer extends AbstractFunction1<Cells, BoxedUnit> implements Serializable {

    @Override
    public BoxedUnit apply(Cells v1) {
        return BoxedUnit.UNIT;
    }
}
class MyRawTransformer extends AbstractFunction1<BSONObject, BoxedUnit> implements Serializable {

    @Override
    public BoxedUnit apply(BSONObject v1) {
        System.out.println(v1);
        return BoxedUnit.UNIT;
    }
}
/**
 * Created by rcrespo on 16/07/14.
 */
@Test(suiteName = "mongoRddTests", groups = {"MongoCellRDDTest"})
public class MongoCellRDDTest {

    private Logger log = Logger.getLogger(getClass());

    @Test
    public void testReadingRDD() {

        String hostConcat = MongoJavaRDDTest.HOST.concat(":").concat(MongoJavaRDDTest.PORT.toString());
        MongoKinaContext context = new MongoKinaContext("local", "kinaContextTest");

        MongoKinaConfig<Cells> inputConfigEntity = MongoConfigFactory.createMongoDB()
                .host(hostConcat).database(DATABASE).collection(COLLECTION_INPUT).initialize();

        JavaRDD<Cells> inputRDDEntity = context.mongoJavaRDD(inputConfigEntity);

        assertEquals(MongoJavaRDDTest.col.count(), inputRDDEntity.cache().count());
        assertEquals(MongoJavaRDDTest.col.findOne().get("message"), inputRDDEntity.first().getCellByName("message").getCellValue());

        context.stop();

    }

    @Test
    public void testWritingRDD() {


        String hostConcat = HOST.concat(":").concat(PORT.toString());

        MongoKinaContext context = new MongoKinaContext("local", "kinaContextTest");

        MongoKinaConfig<Cells> inputConfigEntity = MongoConfigFactory.createMongoDB()
                .host(hostConcat).database(DATABASE).collection(COLLECTION_INPUT).initialize();

        RDD<Cells> inputRDDEntity = context.mongoRDD(inputConfigEntity);

        MongoKinaConfig<Cells> outputConfigEntity = MongoConfigFactory.createMongoDB()
                .host(hostConcat).database(DATABASE).collection(COLLECTION_OUTPUT_CELL).initialize();


        //Save RDD in MongoDB
        MongoCellRDD.saveCell(inputRDDEntity, outputConfigEntity);

        RDD<Cells> outputRDDEntity = context.mongoRDD(outputConfigEntity);


        assertEquals(MongoJavaRDDTest.mongo.getDB(DATABASE).getCollection(COLLECTION_OUTPUT_CELL).findOne().get("message"),
                outputRDDEntity.first().getCellByName("message").getCellValue());


        context.stop();


    }

    @Test
    public void testBSONReadRDD(){
        String bsonFile = getClass().getClassLoader().getResource("dump/").getFile();

        MongoKinaContext context = new MongoKinaContext("local", "kinaContextTest");
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
        System.out.println(rdd.count());

        rdd.foreach(new MyTransformer());
    }

    @Test
    public void testRawBSONReadRDD(){
        String bsonFile = getClass().getClassLoader().getResource("dump/").getFile();

        MongoKinaContext context = new MongoKinaContext("local", "kinaContextTest");

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
        System.out.println(rdd.count());

        rdd.foreach(new MyRawTransformer());
    }

//    @Test
//    public void testTransform() {
//
//
//        String hostConcat = HOST.concat(":").concat(PORT.toString());
//
//        MongoKinaContext context = new MongoKinaContext("local", "kinaContextTest");
//
//        MongoKinaConfig<Cells> inputConfigEntity = MongoConfigFactory.createMongoDB()
//                .host(hostConcat).database(DATABASE).collection(COLLECTION_INPUT).initialize();
//
//        RDD<Cells> inputRDDEntity = context.mongoRDD(inputConfigEntity);
//
//        try{
//            inputRDDEntity.first();
//            fail();
//        }catch (TransformException e){
//            // OK
//            log.info("Correctly catched TransformException: " + e.getLocalizedMessage());
//        }
//
//        context.stop();
//
//    }

}
