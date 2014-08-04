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

import kina.config.CassandraConfigFactory;
import kina.config.CassandraKinaConfig;
import kina.context.CassandraKinaContext;
import kina.entity.Cells;
import kina.rdd.CassandraJavaRDD;
import kina.testutils.ContextProperties;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;

/**
 * Author: Emmanuelle Raffenne
 * Date..: 19-feb-2014
 */
public final class CreatingCellRDD {
    private static final Logger LOG = Logger.getLogger(CreatingCellRDD.class);

    private static Long counts;

    private CreatingCellRDD() {
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
        String job = "java:creatingCellRDD";

        String keyspaceName = "test";
        String tableName = "tweets";

        // Creating the Kina Context
        ContextProperties p = new ContextProperties(args);
        SparkConf sparkConf = new SparkConf()
                .setMaster(p.getCluster())
                .setAppName(job)
                .setJars(p.getJars())
                .setSparkHome(p.getSparkHome())
                .set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
                .set("spark.kryo.registrator","kina.serializer.KinaKryoRegistrator");

        SparkContext sc = new SparkContext(p.getCluster(), job, sparkConf);

        LOG.info("spark.serializer: " + System.getProperty("spark.serializer"));
        LOG.info("spark.kryo.registrator: " + System.getProperty("spark.kryo.registrator"));

	    CassandraKinaContext deepContext = new CassandraKinaContext(sc);

        // Configuration and initialization
        CassandraKinaConfig<Cells> config = CassandraConfigFactory.create()
                .host(p.getCassandraHost())
                .cqlPort(p.getCassandraCqlPort())
                .rpcPort(p.getCassandraThriftPort())
                .keyspace(keyspaceName)
                .table(tableName)
                .initialize();

        // Creating the RDD
        CassandraJavaRDD rdd = (CassandraJavaRDD) deepContext.cassandraJavaRDD(config);

        counts = rdd.count();

        LOG.info("Num of rows: " + counts);

        deepContext.stop();
    }

    public static Long getCounts() {
        return counts;
    }
}
