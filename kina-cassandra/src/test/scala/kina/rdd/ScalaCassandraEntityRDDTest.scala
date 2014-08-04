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

package kina.rdd

import com.datastax.driver.core.{Cluster, ResultSet, Row, Session}
import kina.testentity.KinaScalaPageEntity
import kina.config.{CassandraConfigFactory, CassandraKinaConfig}
import kina.context.AbstractKinaContextAwareTest
import kina.embedded.CassandraServer
import kina.utils.{Utils, Constants}
import org.apache.spark.Partition
import org.testng.Assert._
import org.testng.annotations.{BeforeClass, Test}

/**
 * Created by luca on 20/03/14.
 */
@Test(suiteName = "cassandraRddTests", dependsOnGroups = Array("CassandraJavaRDDTest"), groups = Array("ScalaCassandraEntityRDDTest"))
class ScalaCassandraEntityRDDTest extends AbstractKinaContextAwareTest {
  private var rdd: CassandraRDD[KinaScalaPageEntity] = _
  private var rddConfig: CassandraKinaConfig[KinaScalaPageEntity] = _
  private var writeConfig: CassandraKinaConfig[KinaScalaPageEntity] = _
  private val OUTPUT_COLUMN_FAMILY: String = "out_scalatest_page"

  @BeforeClass
  protected def initServerAndRDD {
    rddConfig = initReadConfig
    writeConfig = initWriteConfig
    rdd = initRDD
  }

  @Test(dependsOnMethods = Array("testGetPreferredLocations"))
  def testCompute {
    val obj: AnyRef = rdd.collect
    assertNotNull(obj)
    val entities: Array[KinaScalaPageEntity] = obj.asInstanceOf[Array[KinaScalaPageEntity]]
    checkComputedData(entities)
  }

  @Test(dependsOnMethods = Array("testRDDInstantiation")) def testGetPartitions {
    val partitions: Array[Partition] = rdd.partitions
    assertNotNull(partitions)
    assertEquals(partitions.length, 8 + 1)
  }

  @Test(dependsOnMethods = Array("testGetPartitions")) def testGetPreferredLocations {
    val partitions: Array[Partition] = rdd.partitions
    val locations: Seq[String] = rdd.getPreferredLocations(partitions(0))
    assertNotNull(locations)
  }

  @Test def testRDDInstantiation {
    assertNotNull(rdd)
  }

  @Test
  def testCql3SaveToCassandra(): Unit = {

  }

  @Test
  def testSimpleSaveToCassandra(): Unit = {

    try {
      AbstractKinaContextAwareTest.executeCustomCQL("DROP TABLE " +
        Utils.quote(AbstractKinaContextAwareTest.OUTPUT_KEYSPACE_NAME) + "." + Utils.quote(OUTPUT_COLUMN_FAMILY))
    }
    catch {
      case e: Exception =>

    };

    CassandraRDD.saveRDDToCassandra(rdd, writeConfig)
    checkSimpleTestData()
  }

  private def initWriteConfig(): CassandraKinaConfig[KinaScalaPageEntity] = {

    writeConfig =
      CassandraConfigFactory
        .createWriteConfig(classOf[KinaScalaPageEntity])
        .host(Constants.DEFAULT_CASSANDRA_HOST)
        .rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
        .cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
        .keyspace(AbstractKinaContextAwareTest.OUTPUT_KEYSPACE_NAME)
        .columnFamily(OUTPUT_COLUMN_FAMILY)
        .batchSize(2)
        .createTableOnWrite(true)

    writeConfig.initialize
  }

  private def initReadConfig(): CassandraKinaConfig[KinaScalaPageEntity] = {
    rddConfig =
      CassandraConfigFactory
        .create(classOf[KinaScalaPageEntity])
        .host(Constants.DEFAULT_CASSANDRA_HOST)
        .rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
        .cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
        .keyspace(AbstractKinaContextAwareTest.KEYSPACE_NAME)
        .columnFamily(AbstractKinaContextAwareTest.COLUMN_FAMILY)

    rddConfig.initialize()
  }

  private def initRDD(): CassandraRDD[KinaScalaPageEntity] = {

    super.getContext.cassandraRDD(rddConfig).asInstanceOf[CassandraRDD[KinaScalaPageEntity]]
  }

  private def checkSimpleTestData(): Unit = {
    val cluster: Cluster = Cluster.builder.withPort(CassandraServer.CASSANDRA_CQL_PORT).addContactPoint(Constants.DEFAULT_CASSANDRA_HOST).build
    val session: Session = cluster.connect
    var command: String = "select count(*) from " +
      Utils.quote(AbstractKinaContextAwareTest.OUTPUT_KEYSPACE_NAME) + "." +
      Utils.quote(AbstractKinaContextAwareTest.OUTPUT_COLUMN_FAMILY) + ";"
    var rs: ResultSet = session.execute(command)
    assertEquals(rs.one.getLong(0), AbstractKinaContextAwareTest.entityTestDataSize)
    command = "select * from " +
      Utils.quote(AbstractKinaContextAwareTest.OUTPUT_KEYSPACE_NAME) + "." +
        Utils.quote(AbstractKinaContextAwareTest.OUTPUT_COLUMN_FAMILY) + " WHERE \"id\" = 'e71aa3103bb4a63b9e7d3aa081c1dc5ddef85fa7';"
    rs = session.execute(command)
    val row: Row = rs.one
    assertEquals(row.getString("domain_name"), "11870.com")
    assertEquals(row.getInt("response_time"), 421)
    assertEquals(row.getLong("download_time"), 1380802049275L)
    assertEquals(row.getString("url"), "http://11870.com/k/es/de")
    session.close
  }

  private def checkComputedData(entities: Array[KinaScalaPageEntity]): Unit = {

  }
}
