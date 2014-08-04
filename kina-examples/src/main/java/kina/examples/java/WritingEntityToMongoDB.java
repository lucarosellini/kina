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
import kina.rdd.mongodb.MongoEntityRDD;
import kina.testentity.MessageEntity;
import kina.testutils.ContextProperties;
import org.apache.log4j.Logger;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;

/**
 * Example class to write an entity to mongoDB
 */
public final class WritingEntityToMongoDB {
    private static final Logger LOG = Logger.getLogger(kina.examples.java.WritingEntityToMongoDB.class);
    public static List<Tuple2<String, Integer>> results;

    private WritingEntityToMongoDB() {
    }

    /**
     * Application entry point.
     *
     * @param args the arguments passed to the application.
     */
    public static void main(String[] args) {
        doMain(args);
    }

    public static void doMain(String[] args) {
        String job = "java:writingEntityToMongoDB";

        String host = "localhost:27017";

        String database = "test";
        String inputCollection = "input";
        String outputCollection = "output";

        String readPreference = "nearest";

        // Creating the Kina Context where args are Spark Master and Job Name
        ContextProperties p = new ContextProperties(args);
	    MongoKinaContext kinaContext = new MongoKinaContext(p.getCluster(), job, p.getSparkHome(),
                p.getJars());


        MongoKinaConfig<MessageEntity> inputConfigEntity =
				        MongoConfigFactory.createMongoDB(MessageEntity.class).host(host).database(database).collection(inputCollection).readPreference(readPreference).initialize();

        RDD<MessageEntity> inputRDDEntity = kinaContext.mongoRDD(inputConfigEntity);


        MongoKinaConfig<MessageEntity> outputConfigEntityPruebaGuardado =
				        MongoConfigFactory.createMongoDB(MessageEntity.class).host(host).database(database).collection(outputCollection).readPreference(readPreference).initialize();


        MongoEntityRDD.saveEntity(inputRDDEntity, outputConfigEntityPruebaGuardado);


        kinaContext.stop();
    }


}
