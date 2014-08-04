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

package kina.rdd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import kina.config.CassandraConfigFactory;
import kina.config.CassandraKinaConfig;
import kina.embedded.CassandraServer;
import kina.exceptions.*;
import kina.functions.AbstractSerializableFunction;
import kina.testentity.TestEntity;
import kina.utils.Constants;
import org.apache.log4j.Logger;
import org.apache.spark.rdd.RDD;
import org.apache.spark.serializer.DeserializationStream;
import org.apache.spark.serializer.JavaSerializer;
import org.apache.spark.serializer.SerializationStream;
import org.apache.spark.serializer.SerializerInstance;
import org.testng.annotations.Test;
import scala.Function1;
import scala.collection.Iterator;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import static kina.utils.Utils.quote;
import static org.testng.Assert.*;

/**
 * Integration tests for entity RDDs.
 */
@Test(suiteName = "cassandraRddTests", groups = {"CassandraEntityRDDTest"})
public class CassandraEntityRDDTest extends CassandraRDDTest<TestEntity> {
    private Logger logger = Logger.getLogger(CassandraEntityRDDTest.class);

    private static class TestEntityAbstractSerializableFunction extends
            AbstractSerializableFunction<TestEntity, TestEntity> {

        private static final long serialVersionUID = -1555102599662015841L;

        @Override
        public TestEntity apply(TestEntity e) {
            return new TestEntity(e.getId(), e.getDomain(), e.getUrl(), e.getResponseTime() + 1, e.getResponseCode(),
                    e.getNotMappedField());
        }
    }

    @Override
    protected void checkComputedData(TestEntity[] entities) {
        boolean found = false;

        assertEquals(entities.length, entityTestDataSize);

        for (TestEntity e : entities) {
            if (e.getId().equals("e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7")) {
                assertEquals(e.getUrl(), "http://11870.com/k/es/de");
                assertEquals(e.getResponseTime(), new Integer(421));
                assertEquals(e.getDownloadTime(), new Long(1380802049275L));
                found = true;
                break;
            }
        }

        if (!found) {
            fail();
        }
    }

    protected void checkOutputTestData() {
        Cluster cluster = Cluster.builder().withPort(CassandraServer.CASSANDRA_CQL_PORT)
                .addContactPoint(Constants.DEFAULT_CASSANDRA_HOST).build();
        Session session = cluster.connect();

        String command = "select count(*) from " + quote(OUTPUT_KEYSPACE_NAME) + "." + quote(OUTPUT_COLUMN_FAMILY) + ";";

        ResultSet rs = session.execute(command);
        assertEquals(rs.one().getLong(0), entityTestDataSize);

        command = "SELECT * from " + quote(OUTPUT_KEYSPACE_NAME) + "." + quote(OUTPUT_COLUMN_FAMILY)
                + " WHERE \"id\" = 'e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7';";

        rs = session.execute(command);
        Row row = rs.one();

        assertEquals(row.getString("domain_name"), "11870.com");
        assertEquals(row.getString("url"), "http://11870.com/k/es/de");
        assertEquals(row.getInt("response_time"), 421 + 1);

        //cannot delete a column using CQL, forcing it to null converts it to 0!!! see CASSANDRA-5885 and CASSANDRA-6180
        assertEquals(row.getLong("download_time"), 0);
        session.close();
    }

    @Override
    protected void checkSimpleTestData() {
        Cluster cluster = Cluster.builder().withPort(CassandraServer.CASSANDRA_CQL_PORT)
                .addContactPoint(Constants.DEFAULT_CASSANDRA_HOST).build();
        Session session = cluster.connect();

        String command = "select count(*) from " + quote(OUTPUT_KEYSPACE_NAME) + "." + quote(OUTPUT_COLUMN_FAMILY) + ";";

        ResultSet rs = session.execute(command);
        assertEquals(rs.one().getLong(0), entityTestDataSize);

        command = "select * from " + quote(OUTPUT_KEYSPACE_NAME) + "." + quote(OUTPUT_COLUMN_FAMILY)
                + " WHERE \"id\" = 'e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7';";

        rs = session.execute(command);
        Row row = rs.one();

        assertEquals(row.getString("domain_name"), "11870.com");
        assertEquals(row.getInt("response_time"), 421);
        assertEquals(row.getLong("download_time"), 1380802049275L);
        assertEquals(row.getString("url"), "http://11870.com/k/es/de");
        session.close();
    }

    @Test
    public void testAdditionalFilters() {


        try {
            CassandraConfigFactory
                    .create(TestEntity.class)
                    .host(Constants.DEFAULT_CASSANDRA_HOST)
                    .rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
                    .cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
                    .keyspace(KEYSPACE_NAME)
                    .columnFamily(COLUMN_FAMILY)
                    .filterByField("notExistentField", "val")
                    .initialize();

            fail();
        } catch (kina.exceptions.NoSuchFieldException e) {
            // OK
        }

        try {
            CassandraConfigFactory
                    .create(TestEntity.class)
                    .host(Constants.DEFAULT_CASSANDRA_HOST)
                    .rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
                    .cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
                    .keyspace(KEYSPACE_NAME)
                    .columnFamily(COLUMN_FAMILY)
                    .filterByField("url", "val")
                    .initialize();

            fail();
        } catch (IndexNotFoundException e) {
            // OK
        }

        TestEntity[] entities = (TestEntity[]) rdd.collect();
        int allElements = entities.length;
        assertTrue(allElements > 2);

        CassandraKinaConfig<TestEntity> config = CassandraConfigFactory
                .create(TestEntity.class)
                .host(Constants.DEFAULT_CASSANDRA_HOST)
                .rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
                .cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
                .keyspace(KEYSPACE_NAME)
                .columnFamily(COLUMN_FAMILY)
                .filterByField("response_time", 371)
                .initialize();

        RDD<TestEntity> otherRDD = context.cassandraRDD(config);

        entities = (TestEntity[]) otherRDD.collect();
        assertEquals(entities.length, 2);

        /*
        config = DeepJobConfigFactory
                .create(TestEntity.class)
                .host(Constants.DEFAULT_CASSANDRA_HOST)
                .rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
                .cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
                .keyspace(KEYSPACE_NAME)
                .columnFamily(COLUMN_FAMILY)
                .filterByField("lucene", "{filter:{type:\"range\",field:\"response_time\",lower:160,upper:840," +
                        "include_lower:true,include_upper:true}}")
                .initialize();

        otherRDD = context.cassandraEntityRDD(config);

        entities = (TestEntity[]) otherRDD.collect();
        assertEquals(entities.length, 9);
        */
    }

    @Override
    protected CassandraRDD<TestEntity> initRDD() {
        assertNotNull(context);
        return (CassandraRDD)context.cassandraRDD(getReadConfig());
    }

    @Override
    protected CassandraKinaConfig<TestEntity> initReadConfig() {
        CassandraKinaConfig<TestEntity> config = CassandraConfigFactory.create(TestEntity.class)
                .host(Constants.DEFAULT_CASSANDRA_HOST).rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
                .cqlPort(CassandraServer.CASSANDRA_CQL_PORT).keyspace(KEYSPACE_NAME).columnFamily(COLUMN_FAMILY)
                .bisectFactor(testBisectFactor).pageSize(DEFAULT_PAGE_SIZE).initialize();

        return config;
    }

    @Override
    protected CassandraKinaConfig<TestEntity> initWriteConfig() {
        CassandraKinaConfig<TestEntity> writeConfig = CassandraConfigFactory.createWriteConfig(TestEntity.class)
                .host(Constants.DEFAULT_CASSANDRA_HOST)
                .rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
                .cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
                .keyspace(OUTPUT_KEYSPACE_NAME)
                .columnFamily(OUTPUT_COLUMN_FAMILY)
                .batchSize(2)
                .createTableOnWrite(Boolean.TRUE);
        return writeConfig.initialize();
    }

    @Test
    public void testCountWithInputColumns() {
        logger.info("testCountWithInputColumns()");

        CassandraKinaConfig<TestEntity> tmpConfig = CassandraConfigFactory.create(TestEntity.class)
                .host(Constants.DEFAULT_CASSANDRA_HOST)
                .rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
                .cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
                .keyspace(KEYSPACE_NAME)
                .columnFamily(COLUMN_FAMILY)
                .batchSize(2)
				.pageSize(DEFAULT_PAGE_SIZE)
                .inputColumns("domain_name", "response_time")
                .initialize();

        RDD<TestEntity> tmpRdd = context.cassandraRDD(tmpConfig);

        TestEntity[] cells = (TestEntity[]) tmpRdd.collect();

        assertEquals(cells.length, entityTestDataSize);

        for (TestEntity e : cells) {
            assertNotNull(e.getDomain());
            assertNotNull(e.getResponseTime());
            assertNotNull(e.getId());

            assertNull(e.getResponseCode());
            assertNull(e.getDownloadTime());
            assertNull(e.getNotMappedField());
            assertNull(e.getUrl());
        }
    }

    @Override
    public void testSaveToCassandra() {
        Function1<TestEntity, TestEntity> mappingFunc = new TestEntityAbstractSerializableFunction();

        RDD<TestEntity> mappedRDD = getRDD().map(mappingFunc, ClassTag$.MODULE$.<TestEntity>apply(TestEntity.class));

        try {
            executeCustomCQL("DROP TABLE " + quote(OUTPUT_KEYSPACE_NAME) + "." + quote(OUTPUT_COLUMN_FAMILY));
        } catch (Exception e) {
        }

        assertTrue(mappedRDD.count() > 0);

        CassandraKinaConfig<TestEntity> writeConfig = getWriteConfig();
        writeConfig.createTableOnWrite(Boolean.FALSE);

        try {
            CassandraRDD.saveRDDToCassandra(mappedRDD, writeConfig);

            fail();
        } catch (IOException e) {
            // ok
            writeConfig.createTableOnWrite(Boolean.TRUE);
        }

        CassandraRDD.saveRDDToCassandra(mappedRDD, writeConfig);

        checkOutputTestData();

    }

    @Override
    public void testSimpleSaveToCassandra() {
        CassandraKinaConfig<TestEntity> writeConfig = getWriteConfig();
        writeConfig.createTableOnWrite(Boolean.FALSE);

        try {
	        executeCustomCQL("DROP TABLE " + quote(OUTPUT_KEYSPACE_NAME) + "." + quote(OUTPUT_COLUMN_FAMILY));
        } catch (Exception e) {
        }

        try {
            CassandraRDD.saveRDDToCassandra(getRDD(), writeConfig);

            fail();
        } catch (Exception e) {
            // ok
            writeConfig.createTableOnWrite(Boolean.TRUE);
        }

        assertEquals(getRDD().count(), entityTestDataSize);
        CassandraRDD.saveRDDToCassandra(getRDD(), writeConfig);

        checkSimpleTestData();
    }

    @Override
    public void testCql3SaveToCassandra() {

        try {
	        executeCustomCQL("DROP TABLE " + quote(OUTPUT_KEYSPACE_NAME) + "." + quote(OUTPUT_COLUMN_FAMILY));
        } catch (Exception e) {
        }

        CassandraKinaConfig<TestEntity> writeConfig = getWriteConfig();

        CassandraRDD.cql3SaveRDDToCassandra(getRDD(), writeConfig);
        checkSimpleTestData();
    }


    @Test
    public void testJavaSerialization() {
        JavaSerializer ser = new JavaSerializer(context.getConf());

        SerializerInstance instance = ser.newInstance();
        ClassTag<CassandraRDD<TestEntity>> classTag = ClassTag$.MODULE$.<CassandraRDD<TestEntity>>apply(rdd.getClass());

        ByteBuffer serializedRDD = instance.serialize(rdd, classTag);

        CassandraRDD deserializedRDD = instance.deserialize(serializedRDD, classTag);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        SerializationStream serializationStream = instance.serializeStream(baos);
        serializationStream = serializationStream.writeObject(rdd, classTag);

        serializationStream.flush();
        serializationStream.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(serializedRDD.array());

        DeserializationStream deserializationStream = instance.deserializeStream(bais);
        Iterator<Object> iter = deserializationStream.asIterator();
        assertTrue(iter.hasNext());

        deserializedRDD = (CassandraRDD) iter.next();
        assertNotNull(deserializedRDD);
    }

}
