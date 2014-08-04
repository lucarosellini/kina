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

package kina.context;

import java.util.HashMap;
import java.util.Map;

import kina.config.CellMongoKinaConfig;
import kina.config.EntityMongoKinaConfig;
import kina.config.GenericMongoKinaConfig;
import kina.entity.Cells;
import kina.exceptions.GenericException;
import kina.rdd.mongodb.MongoCellRDD;
import kina.rdd.mongodb.MongoEntityRDD;
import kina.rdd.mongodb.MongoJavaRDD;
import kina.testentity.BookEntity;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.rdd.KinaMongoRDD;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


/**
 * Created by rcrespo on 16/07/14.
 */
@Test
public class MongoKinaContextTest {

    private Logger log = Logger.getLogger(getClass());

    @Test()
    public void mongoRDDTest() {
        MongoKinaContext sc = new MongoKinaContext("local", "MongoKinaContextTest");

        KinaMongoRDD rdd = sc.mongoRDD(new CellMongoKinaConfig());

        assertTrue(rdd instanceof MongoCellRDD);

        assertFalse(rdd instanceof MongoEntityRDD);


        rdd = sc.mongoRDD(new EntityMongoKinaConfig(BookEntity.class));

        assertTrue(rdd instanceof MongoEntityRDD);

        assertFalse(rdd instanceof MongoCellRDD);


        JavaRDD<Cells> javaRDDCells = sc.mongoJavaRDD(new CellMongoKinaConfig());

        assertNotNull(javaRDDCells);

        assertTrue(javaRDDCells instanceof JavaRDD);

        assertTrue(javaRDDCells instanceof MongoJavaRDD);


        JavaRDD<BookEntity> javaRDDEntity = sc.mongoJavaRDD(new EntityMongoKinaConfig(BookEntity.class));

        assertNotNull(javaRDDEntity);

        assertTrue(javaRDDEntity instanceof JavaRDD);

        assertTrue(javaRDDEntity instanceof MongoJavaRDD);

        try {
            KinaMongoRDD failRDD = sc.mongoRDD(new GenericMongoKinaConfig());
            fail();
        } catch (GenericException e) {
            log.info("Correctly catched GenericException: " + e.getLocalizedMessage());
        }


        sc.stop();


    }

    @Test
    public void testInstantiationBySparkContext() {
        MongoKinaContext sc = new MongoKinaContext(new SparkContext("local", "myapp1", new SparkConf()));

        assertNotNull(sc);

        KinaMongoRDD rdd = sc.mongoRDD(new CellMongoKinaConfig());

        assertTrue(rdd instanceof MongoCellRDD);

        assertFalse(rdd instanceof MongoEntityRDD);


        rdd = sc.mongoRDD(new EntityMongoKinaConfig(BookEntity.class));

        assertTrue(rdd instanceof MongoEntityRDD);

        assertFalse(rdd instanceof MongoCellRDD);

        sc.stop();
    }

    @Test
    public void testInstantiationWithJar() {
        MongoKinaContext sc = new MongoKinaContext("local", "myapp1", "/tmp", "");
        assertNotNull(sc);

        KinaMongoRDD rdd = sc.mongoRDD(new CellMongoKinaConfig());

        assertTrue(rdd instanceof MongoCellRDD);

        assertFalse(rdd instanceof MongoEntityRDD);


        rdd = sc.mongoRDD(new EntityMongoKinaConfig(BookEntity.class));

        assertTrue(rdd instanceof MongoEntityRDD);

        assertFalse(rdd instanceof MongoCellRDD);

        sc.stop();
    }

    @Test
    public void testInstantiationWithJars() {
        MongoKinaContext sc = new MongoKinaContext("local", "myapp1", "/tmp", new String[]{"", ""});
        assertNotNull(sc);

        KinaMongoRDD rdd = sc.mongoRDD(new CellMongoKinaConfig());

        assertTrue(rdd instanceof MongoCellRDD);

        assertFalse(rdd instanceof MongoEntityRDD);


        rdd = sc.mongoRDD(new EntityMongoKinaConfig(BookEntity.class));

        assertTrue(rdd instanceof MongoEntityRDD);

        assertFalse(rdd instanceof MongoCellRDD);

        sc.stop();
    }

    @Test
    public void testInstantiationWithJarsAndEnv() {
        Map<String, String> env = new HashMap<>();

        MongoKinaContext sc = new MongoKinaContext("local", "myapp1", "/tmp", new String[]{"", ""}, env);

        assertNotNull(sc);
        KinaMongoRDD rdd = sc.mongoRDD(new CellMongoKinaConfig());

        assertTrue(rdd instanceof MongoCellRDD);

        assertFalse(rdd instanceof MongoEntityRDD);


        rdd = sc.mongoRDD(new EntityMongoKinaConfig(BookEntity.class));

        assertTrue(rdd instanceof MongoEntityRDD);

        assertFalse(rdd instanceof MongoCellRDD);


        sc.stop();
    }
}
