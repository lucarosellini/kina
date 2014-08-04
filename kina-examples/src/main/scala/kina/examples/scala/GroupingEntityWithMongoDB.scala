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
import kina.context.MongoKinaContext
import kina.rdd.mongodb.MongoEntityRDD
import kina.testentity.{BookEntity, WordCount}
import kina.testutils.ContextProperties
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._

import scala.collection.JavaConversions._

/**
 * Created by rcrespo on 25/06/14.
 */
final object GroupingEntityWithMongoDB {
  def main(args: Array[String]) {
    doMain(args)
  }

  def doMain(args: Array[String]) {
    val job: String = "java:readingEntityFromMongoDB"
    val host: String = "localhost:27017"
    val database: String = "book"
    val inputCollection: String = "input"
    val outputCollection: String = "output"

    val p: ContextProperties = new ContextProperties(args)

    val kinaContext = new MongoKinaContext(p.getCluster, job, p.getSparkHome, p.getJars)

    val inputConfigEntity: MongoKinaConfig[BookEntity] = MongoConfigFactory.createMongoDB(classOf[BookEntity]).host(host).database(database).collection(inputCollection).initialize

    val inputRDDEntity: RDD[BookEntity] = kinaContext.mongoRDD(inputConfigEntity)

    val words: RDD[String] = inputRDDEntity flatMap {
      e: BookEntity => (for (canto <- e.getCantoEntities) yield canto.getText.split(" ")).flatten
    }

    val wordCount : RDD[(String, Integer)] = words map { s:String => (s,1) }

    val wordCountReduced  = wordCount reduceByKey { (a,b) =>a + b }

    val outputRDD = wordCountReduced map { e:(String, Integer) => new WordCount(e._1, e._2)  }

    val outputConfigEntity: MongoKinaConfig[WordCount] =
      MongoConfigFactory.createMongoDB(classOf[WordCount]).host(host).database(database).collection(outputCollection).initialize

    MongoEntityRDD.saveEntity(outputRDD, outputConfigEntity)

    kinaContext.stop
  }

}
