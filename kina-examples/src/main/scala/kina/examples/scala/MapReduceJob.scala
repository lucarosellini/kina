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

import kina.config.CassandraConfigFactory
import kina.context.CassandraKinaContext
import org.apache.spark.SparkContext._
import kina.config._
import kina.context._
import kina.rdd._

import org.apache.spark.rdd.RDD
import kina.testutils.ContextProperties
import kina.testentity.TweetEntity

/**
 * Author: Emmanuelle Raffenne
 * Date..: 12-feb-2014
 */

object MapReduceJob {

  def main(args: Array[String]) {

    val job = "scala:mapReduceJob"

    val keyspaceName = "test"
    val tableName = "tweets"

    // Creating the Kina Context where args are Spark Master and Job Name
    val p = new ContextProperties(args)
    val kinaContext = new CassandraKinaContext(p.getCluster, job, p.getSparkHome, p.getJars)

    // Creating a configuration for the RDD and initialize it
    val config = CassandraConfigFactory.create(classOf[TweetEntity])
      .host(p.getCassandraHost).cqlPort(p.getCassandraCqlPort).rpcPort(p.getCassandraThriftPort)
      .keyspace(keyspaceName).table(tableName)
      .initialize

    // Creating the RDD
    val rdd: RDD[TweetEntity] = kinaContext.cassandraRDD(config)

    // ------------------ MapReduce block
    // Map stage: Getting key-value pairs from the RDD
    val pairsRDD: RDD[(String, Int)] = rdd map {
      e: TweetEntity => (e.getAuthor, 1)
    }

    // Reduce stage: counting rows
    val counts: RDD[(String, Int)] = pairsRDD reduceByKey {
      _ + _
    }

    // Fetching the results
    val results: Array[(String, Int)] = counts.collect()

    results foreach {
      t: (String, Int) => println(t._1 + ": " + t._2.toString)
    }
  }
}
