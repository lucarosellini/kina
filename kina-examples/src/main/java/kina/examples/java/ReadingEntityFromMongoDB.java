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

package kina.examples.java;

import java.util.List;

import kina.config.MongoKinaConfig;
import kina.config.MongoConfigFactory;
import kina.context.MongoKinaContext;
import kina.testentity.MessageEntity;
import kina.testutils.ContextProperties;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import scala.Tuple2;

/**
 * Example class to read an entity from mongoDB
 */
public final class ReadingEntityFromMongoDB {
    private static final Logger LOG = Logger.getLogger(kina.examples.java.ReadingEntityFromMongoDB.class);
    public static List<Tuple2<String, Integer>> results;

    private ReadingEntityFromMongoDB() {
    }


    public static void main(String[] args) {
        doMain(args);
    }


    public static void doMain(String[] args) {
        String job = "java:readingEntityFromMongoDB";

        String host = "localhost:27017";

        String database = "test";
        String inputCollection = "input";

        // Creating the Kina Context where args are Spark Master and Job Name
        ContextProperties p = new ContextProperties(args);
	    MongoKinaContext deepContext = new MongoKinaContext(p.getCluster(), job, p.getSparkHome(),
                p.getJars());


        MongoKinaConfig<MessageEntity> inputConfigEntity =
				        MongoConfigFactory.createMongoDB(MessageEntity.class).host(host).database(database).collection(inputCollection).initialize();

        JavaRDD<MessageEntity> inputRDDEntity = deepContext.mongoJavaRDD(inputConfigEntity);


	    LOG.info("count : " + inputRDDEntity.cache().count());


        deepContext.stop();
    }
}
