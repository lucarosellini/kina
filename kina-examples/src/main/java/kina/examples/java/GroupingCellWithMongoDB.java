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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kina.config.MongoConfigFactory;
import kina.config.MongoKinaConfig;
import kina.context.MongoKinaContext;
import kina.entity.Cells;
import kina.entity.MongoCell;
import kina.rdd.mongodb.MongoCellRDD;
import kina.testutils.ContextProperties;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

/**
 * Created by rcrespo on 25/06/14.
 */
public final class GroupingCellWithMongoDB {


    private GroupingCellWithMongoDB() {
    }


    public static void main(String[] args) {
        doMain(args);
    }


    public static void doMain(String[] args) {
        String job = "scala:groupingCellWithMongoDB";

        String host = "localhost:27017";

        String database = "book";
        String inputCollection = "input";
        String outputCollection = "output";


        // Creating the Kina Context where args are Spark Master and Job Name
        ContextProperties p = new ContextProperties(args);
	    MongoKinaContext deepContext = new MongoKinaContext(p.getCluster(), job, p.getSparkHome(),
                p.getJars());


        MongoKinaConfig<Cells> inputConfigEntity =
				        MongoConfigFactory.createMongoDB().host(host).database(database).collection(inputCollection).initialize();

        JavaRDD<Cells> inputRDDEntity = deepContext.mongoJavaRDD(inputConfigEntity);


        JavaRDD<String> words =inputRDDEntity.flatMap(new FlatMapFunction<Cells, String>() {
            @Override
            public Iterable<String> call(Cells cells) throws Exception {

                List<String> words = new ArrayList<>();
                for (Cells canto : (List<Cells>)cells.getCellByName("cantos").getCellValue() ){
                    words.addAll(Arrays.asList( ((String)canto.getCellByName("text").getCellValue()).split(" ")));
                }
                return words;
            }
        });


        JavaPairRDD<String, Integer> wordCount = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String, Integer>(s,1);
            }
        });


        JavaPairRDD<String, Integer>  wordCountReduced = wordCount.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        });

        JavaRDD<Cells>  outputRDD =  wordCountReduced.map(new Function<Tuple2<String, Integer>, Cells>() {
            @Override
            public Cells call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return new Cells(MongoCell.create("word", stringIntegerTuple2._1()) , MongoCell.create("count", stringIntegerTuple2._2()));
            }
        });


        MongoKinaConfig<Cells> outputConfigEntity =
				        MongoConfigFactory.createMongoDB().host(host).database(database).collection(outputCollection).initialize();

        MongoCellRDD.saveCell(outputRDD.rdd(), outputConfigEntity);

        deepContext.stop();
    }
}
