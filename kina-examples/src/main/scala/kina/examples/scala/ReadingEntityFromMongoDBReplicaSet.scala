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

import kina.config.{MongoConfigFactory, MongoKinaConfig}
import kina.context.MongoKinaContext
import kina.rdd.mongodb.MongoJavaRDD
import kina.testentity.MessageEntity
import kina.testutils.ContextProperties
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD

/**
 * Example class to read an entity from a mongoDB replica set
 */
final object ReadingEntityFromMongoDBReplicaSet {
  def main(args: Array[String]) {
    doMain(args)
  }

  def doMain(args: Array[String]) {
    val job: String = "scala:readingEntityFromMongoDBReplicaSet"
    val host1: String = "localhost:57017"
    val host2: String = "localhost:57018"
    val host3: String = "localhost:57019"
    val database: String = "test"
    val inputCollection: String = "input"
    val replicaSet: String = "s2"
    val readPreference: String = "primaryPreferred"

    val p: ContextProperties = new ContextProperties(args)

    val kinaContext = new MongoKinaContext(p.getCluster, job, p.getSparkHome, p.getJars)

    val inputConfigEntity: MongoKinaConfig[MessageEntity] = MongoConfigFactory.createMongoDB(classOf[MessageEntity]).host(host1).host(host2).host(host3).database(database).collection(inputCollection).replicaSet(replicaSet).readPreference(readPreference).initialize

    val inputRDDEntity: RDD[MessageEntity] = kinaContext.mongoJavaRDD(inputConfigEntity)

    System.out.println("count : " + inputRDDEntity.cache.count)
    kinaContext.stop
  }


}
