package com.stratio.deep.rdd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.stratio.deep.config.DeepJobConfigFactory;
import com.stratio.deep.config.IDeepJobConfig;
import com.stratio.deep.embedded.CassandraServer;
import com.stratio.deep.entity.TestEntity;
import com.stratio.deep.functions.AbstractSerializableFunction;
import com.stratio.deep.util.Constants;
import org.apache.spark.rdd.RDD;
import org.apache.spark.serializer.DeserializationStream;
import org.apache.spark.serializer.JavaSerializer;
import org.apache.spark.serializer.SerializationStream;
import org.apache.spark.serializer.SerializerInstance;
import org.testng.annotations.Test;
import scala.Function1;
import scala.collection.Iterator;
import scala.reflect.ClassTag$;

import static org.testng.Assert.*;

/**
 * Created by luca on 05/02/14.
 */
@Test(suiteName = "cassandraRddTests", groups = { "CassandraEntityRDDTest" })
public class CassandraEntityRDDTest extends CassandraRDDTest<TestEntity> {

    private static class TestEntityAbstractSerializableFunction extends
		    AbstractSerializableFunction<TestEntity, TestEntity> {

	/**
	 *
	 */
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

	String command = "select count(*) from " + OUTPUT_KEYSPACE_NAME + "." + OUTPUT_COLUMN_FAMILY + ";";

	ResultSet rs = session.execute(command);
	assertEquals(rs.one().getLong(0), entityTestDataSize);

	command = "SELECT * from " + OUTPUT_KEYSPACE_NAME + "." + OUTPUT_COLUMN_FAMILY
			+ " WHERE \"id\" = 'e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7';";

	rs = session.execute(command);
	Row row = rs.one();

	assertEquals(row.getString("domain_name"), "11870.com");
	assertEquals(row.getString("url"), "http://11870.com/k/es/de");
	assertEquals(row.getInt("response_time"), 421 + 1);

	//TODO: cannot delete a column using CQL, forcing it to null converts it to 0!!! see CASSANDRA-5885 and CASSANDRA-6180
	assertEquals(row.getLong("download_time"), 0);
	session.shutdown();
    }

    @Override
    protected void checkSimpleTestData() {
	Cluster cluster = Cluster.builder().withPort(CassandraServer.CASSANDRA_CQL_PORT)
			.addContactPoint(Constants.DEFAULT_CASSANDRA_HOST).build();
	Session session = cluster.connect();

	String command = "select count(*) from " + OUTPUT_KEYSPACE_NAME + "." + OUTPUT_COLUMN_FAMILY + ";";

	ResultSet rs = session.execute(command);
	assertEquals(rs.one().getLong(0), entityTestDataSize);

	command = "select * from " + OUTPUT_KEYSPACE_NAME + "." + OUTPUT_COLUMN_FAMILY
			+ " WHERE \"id\" = 'e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7';";

	rs = session.execute(command);
	Row row = rs.one();

	assertEquals(row.getString("domain_name"), "11870.com");
	assertEquals(row.getInt("response_time"), 421);
	assertEquals(row.getLong("download_time"), 1380802049275L);
	assertEquals(row.getString("url"), "http://11870.com/k/es/de");
	session.shutdown();
    }

    @Override
    protected CassandraRDD<TestEntity> initRDD() {
	assertNotNull(context);
	return context.cassandraEntityRDD(getReadConfig());
    }

    @Override
    protected IDeepJobConfig<TestEntity> initReadConfig() {
	IDeepJobConfig<TestEntity> config = DeepJobConfigFactory.create(TestEntity.class)
			.host(Constants.DEFAULT_CASSANDRA_HOST).rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
			.cqlPort(CassandraServer.CASSANDRA_CQL_PORT).keyspace(KEYSPACE_NAME).columnFamily(COLUMN_FAMILY);

	config.getConfiguration();

	return config;
    }

    @Override
    protected IDeepJobConfig<TestEntity> initWriteConfig() {
	IDeepJobConfig<TestEntity> writeConfig = DeepJobConfigFactory.create(TestEntity.class)
			.host(Constants.DEFAULT_CASSANDRA_HOST).rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
			.cqlPort(CassandraServer.CASSANDRA_CQL_PORT).keyspace(OUTPUT_KEYSPACE_NAME).columnFamily(OUTPUT_COLUMN_FAMILY);

	writeConfig.getConfiguration();
	return writeConfig;
    }

    @Override
    public void testSaveToCassandra() {
	Function1<TestEntity, TestEntity> mappingFunc = new TestEntityAbstractSerializableFunction();

	RDD<TestEntity> mappedRDD = getRDD().map(mappingFunc, ClassTag$.MODULE$.<TestEntity>apply(TestEntity.class));

	executeCustomCQL("TRUNCATE  " + OUTPUT_KEYSPACE_NAME + "." + OUTPUT_COLUMN_FAMILY);

	assertTrue(mappedRDD.count() > 0);

	CassandraRDD.saveRDDToCassandra(mappedRDD, getWriteConfig());

	checkOutputTestData();
    }

    @Override
    public void testSimpleSaveToCassandra() {
	CassandraRDD.saveRDDToCassandra(getRDD(), getWriteConfig());
	checkSimpleTestData();
    }

    @Test
    public void testJavaSerialization(){
	JavaSerializer ser = new JavaSerializer(context.getConf());

	SerializerInstance instance = ser.newInstance();

	ByteBuffer serializedRDD = instance.serialize(rdd);

	CassandraRDD deserializedRDD = instance.deserialize(serializedRDD);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	SerializationStream serializationStream = instance.serializeStream(baos);
	serializationStream = serializationStream.writeObject(rdd);

	serializationStream.flush();
	serializationStream.close();

	ByteArrayInputStream bais = new ByteArrayInputStream(serializedRDD.array());

	DeserializationStream deserializationStream = instance.deserializeStream(bais);
	Iterator<Object> iter = deserializationStream.asIterator();
	assertTrue(iter.hasNext());

	deserializedRDD = (CassandraRDD)iter.next();
	assertNotNull(deserializedRDD);
    }

}