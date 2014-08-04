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

import com.google.common.collect.Lists;

import kina.config.CassandraConfigFactory;
import kina.config.CassandraKinaConfig;
import kina.context.CassandraKinaContext;
import kina.rdd.CassandraRDD;
import kina.testentity.DomainEntity;
import kina.testentity.PageEntity;
import kina.testutils.ContextProperties;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

/**
 * Author: Emmanuelle Raffenne
 * Date..: 13-feb-2014
 */
public final class WritingEntityToCassandra {
    private static final Logger LOG = Logger.getLogger(WritingEntityToCassandra.class);
    public static List<Tuple2<String, Integer>> results;

    private WritingEntityToCassandra() {
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
        String job = "java:writingEntityToCassandra";

        String keyspaceName = "crawler";
        String inputTableName = "Page";
        String outputTableName = "listdomains";

        // Creating the Kina Context where args are Spark Master and Job Name
        ContextProperties p = new ContextProperties(args);
	    CassandraKinaContext deepContext = new CassandraKinaContext(p.getCluster(), job, p.getSparkHome(), p.getJars());


        // --- INPUT RDD
        CassandraKinaConfig<PageEntity> inputConfig = CassandraConfigFactory.create(PageEntity.class)
                .host(p.getCassandraHost()).cqlPort(p.getCassandraCqlPort()).rpcPort(p.getCassandraThriftPort())
                .keyspace(keyspaceName).table(inputTableName)
                .initialize();

        JavaRDD<PageEntity> inputRDD = deepContext.cassandraJavaRDD(inputConfig);

        JavaPairRDD<String, PageEntity> pairRDD = inputRDD.mapToPair(new PairFunction<PageEntity, String,
                PageEntity>() {
            @Override
            public Tuple2<String, PageEntity> call(PageEntity e) {
                return new Tuple2<String, PageEntity>(e.getDomainName(), e);
            }
        });

        JavaPairRDD<String, Integer> numPerKey = pairRDD.groupByKey()
                .mapToPair(new PairFunction<Tuple2<String, Iterable<PageEntity>>, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(Tuple2<String, Iterable<PageEntity>> t) {
                        return new Tuple2<String, Integer>(t._1(), Lists.newArrayList(t._2()).size());
                    }
                });

        results = numPerKey.collect();

        for (Tuple2<String, Integer> result : results) {
            LOG.info(result);
        }

        // --- OUTPUT RDD
        CassandraKinaConfig<DomainEntity> outputConfig = CassandraConfigFactory.createWriteConfig(DomainEntity.class)
                .host(p.getCassandraHost()).cqlPort(p.getCassandraCqlPort()).rpcPort(p.getCassandraThriftPort())
                .keyspace(keyspaceName).table(outputTableName)
                .createTableOnWrite(true).initialize();

        JavaRDD<DomainEntity> outputRDD = numPerKey.map(new Function<Tuple2<String, Integer>, DomainEntity>() {
            @Override
            public DomainEntity call(Tuple2<String, Integer> t) throws Exception {
                DomainEntity e = new DomainEntity();
                e.setDomain(t._1());
                e.setNumPages(t._2());
                return e;
            }
        });

        CassandraRDD.saveRDDToCassandra(outputRDD, outputConfig);

        deepContext.stop();
    }
}
