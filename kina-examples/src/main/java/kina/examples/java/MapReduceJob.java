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

import kina.config.CassandraConfigFactory;
import kina.config.CassandraKinaConfig;
import kina.context.CassandraKinaContext;
import kina.testentity.TweetEntity;
import kina.testutils.ContextProperties;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

/**
 * Author: Emmanuelle Raffenne
 * Date..: 13-feb-2014
 */

public final class MapReduceJob {
    private static final Logger LOG = Logger.getLogger(MapReduceJob.class);
    public static List<Tuple2<String, Integer>> results;

    private MapReduceJob() {
    }

    /**
     * Application entry point.
     *
     * @param args the arguments passed to the application.
     */
    public static void main(String[] args) {
        doMain(args);
    }

    /**
     * This is the method called by both main and tests.
     *
     * @param args
     */
    public static void doMain(String[] args) {
        String job = "java:mapReduceJob";

        String keyspaceName = "test";
        String tableName = "tweets";

        // Creating the Kina Context where args are Spark Master and Job Name
        ContextProperties p = new ContextProperties(args);
	    CassandraKinaContext deepContext = new CassandraKinaContext(p.getCluster(), job, p.getSparkHome(), p.getJars());


        // Creating a configuration for the RDD and initialize it
        CassandraKinaConfig<TweetEntity> config = CassandraConfigFactory.create(TweetEntity.class)
                .host(p.getCassandraHost()).cqlPort(p.getCassandraCqlPort()).rpcPort(p.getCassandraThriftPort())
                .keyspace(keyspaceName).table(tableName)
                .initialize();

        // Creating the RDD
        JavaRDD<TweetEntity> rdd = deepContext.cassandraJavaRDD(config);

        // Map stage: Getting key-value pairs from the RDD
        JavaPairRDD<String, Integer> pairsRDD = rdd.mapToPair(new PairFunction<TweetEntity, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(TweetEntity t) {
                return new Tuple2<String, Integer>(t.getAuthor(), 1);
            }
        });

        // Reduce stage: counting rows
        JavaPairRDD<String, Integer> counts = pairsRDD.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer a, Integer b) {
                return a + b;
            }
        });

        // Fetching the results
        results = counts.collect();

        for (Tuple2<String, Integer> t : results) {
            LOG.info(t._1() + ": " + t._2().toString());
        }

        deepContext.stop();
    }
}
