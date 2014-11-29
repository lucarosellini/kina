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

import kina.config._
import kina.context._
import kina.rdd._
import kina.config.CassandraConfigFactory
import kina.context.CassandraKinaContext

import org.apache.spark.rdd.RDD
import kina.testutils.ContextProperties
import kina.testentity.TweetEntity

/**
 * Author: Emmanuelle Raffenne
 * Date..: 10-feb-2014
 */

object GroupingByColumn {

  def main(args: Array[String]) {

    val job = "scala:groupingByColumn"
    val keyspaceName = "test"
    val tableName = "tweets"

    // Creating the Kina Context where args are Spark Master and Job Name
    val p = new ContextProperties(args)
    val kinaContext = new CassandraKinaContext(p.getCluster, job, p.getSparkHome, p.getJars)

    // Configure and initialize the RDD
    val config = CassandraConfigFactory.create(classOf[TweetEntity])
      .host(p.getCassandraHost).cqlPort(p.getCassandraCqlPort).rpcPort(p.getCassandraThriftPort)
      .keyspace(keyspaceName).table(tableName)
      .initialize

    // Create the RDD
    val rdd: RDD[TweetEntity] = kinaContext.cassandraRDD(config)

    // grouping
    val groups: RDD[(String, Iterable[TweetEntity])] = rdd groupBy {
      t: TweetEntity => t.getAuthor
    }

    // counting elements in groups
    val counts: RDD[(String, Int)] = groups map {
      t: (String, Iterable[TweetEntity]) => (t._1, t._2.size)
    }

    // fetching results
    val result: Array[(String, Int)] = counts.collect()

    // printing out the result
    println("GroupBy")
    result foreach {
      t: (String, Int) => println(t._1 + ": " + t._2.toString)
    }
  }

}
