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

package kina.examples.scala

import kina.context._
import kina.config._
import kina.rdd._
import kina.testutils.ContextProperties
import kina.config.{CassandraConfigFactory, CassandraKinaConfig}
import kina.context.CassandraKinaContext
import kina.entity.Cells
import org.apache.spark.rdd.RDD

/**
 * Author: Emmanuelle Raffenne
 * Date..: 19-feb-2014
 */

object CreatingCellRDD {

  def main(args: Array[String]) {

    val job = "scala:creatingCellRDD"
    val keyspaceName = "test"
    val tableName = "tweets"

    // Creating the Kina Context where args are Spark Master and Job Name
    val p = new ContextProperties(args)
    val kinaContext = new CassandraKinaContext(p.getCluster, job)

    // Configuration and initialization
    val config: CassandraKinaConfig[Cells] = CassandraConfigFactory.create()
      .host(p.getCassandraHost).cqlPort(p.getCassandraCqlPort).rpcPort(p.getCassandraThriftPort)
      .keyspace(keyspaceName).table(tableName)
      .initialize

    // Creating the RDD
    val rdd: RDD[Cells] = kinaContext.cassandraRDD(config)

    val counts = rdd.count()

    println("Num of rows: " + counts.toString)
  }
}
