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

import kina.config.CellDeepJobConfigMongoDB;
import kina.config.EntityDeepJobConfigMongoDB;
import kina.config.GenericDeepJobConfigMongoDB;
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
import org.apache.spark.rdd.DeepMongoRDD;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

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

        DeepMongoRDD deepMongoRDD = sc.mongoRDD(new CellDeepJobConfigMongoDB());

        assertTrue(deepMongoRDD instanceof MongoCellRDD);

        assertFalse(deepMongoRDD instanceof MongoEntityRDD);


        deepMongoRDD = sc.mongoRDD(new EntityDeepJobConfigMongoDB(BookEntity.class));

        assertTrue(deepMongoRDD instanceof MongoEntityRDD);

        assertFalse(deepMongoRDD instanceof MongoCellRDD);


        JavaRDD<Cells> javaRDDCells = sc.mongoJavaRDD(new CellDeepJobConfigMongoDB());

        assertNotNull(javaRDDCells);

        assertTrue(javaRDDCells instanceof JavaRDD);

        assertTrue(javaRDDCells instanceof MongoJavaRDD);


        JavaRDD<BookEntity> javaRDDEntity = sc.mongoJavaRDD(new EntityDeepJobConfigMongoDB(BookEntity.class));

        assertNotNull(javaRDDEntity);

        assertTrue(javaRDDEntity instanceof JavaRDD);

        assertTrue(javaRDDEntity instanceof MongoJavaRDD);

        try {
            DeepMongoRDD failRDD = sc.mongoRDD(new GenericDeepJobConfigMongoDB());
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

        DeepMongoRDD deepMongoRDD = sc.mongoRDD(new CellDeepJobConfigMongoDB());

        assertTrue(deepMongoRDD instanceof MongoCellRDD);

        assertFalse(deepMongoRDD instanceof MongoEntityRDD);


        deepMongoRDD = sc.mongoRDD(new EntityDeepJobConfigMongoDB(BookEntity.class));

        assertTrue(deepMongoRDD instanceof MongoEntityRDD);

        assertFalse(deepMongoRDD instanceof MongoCellRDD);

        sc.stop();
    }

    @Test
    public void testInstantiationWithJar() {
        MongoKinaContext sc = new MongoKinaContext("local", "myapp1", "/tmp", "");
        assertNotNull(sc);

        DeepMongoRDD deepMongoRDD = sc.mongoRDD(new CellDeepJobConfigMongoDB());

        assertTrue(deepMongoRDD instanceof MongoCellRDD);

        assertFalse(deepMongoRDD instanceof MongoEntityRDD);


        deepMongoRDD = sc.mongoRDD(new EntityDeepJobConfigMongoDB(BookEntity.class));

        assertTrue(deepMongoRDD instanceof MongoEntityRDD);

        assertFalse(deepMongoRDD instanceof MongoCellRDD);

        sc.stop();
    }

    @Test
    public void testInstantiationWithJars() {
        MongoKinaContext sc = new MongoKinaContext("local", "myapp1", "/tmp", new String[]{"", ""});
        assertNotNull(sc);

        DeepMongoRDD deepMongoRDD = sc.mongoRDD(new CellDeepJobConfigMongoDB());

        assertTrue(deepMongoRDD instanceof MongoCellRDD);

        assertFalse(deepMongoRDD instanceof MongoEntityRDD);


        deepMongoRDD = sc.mongoRDD(new EntityDeepJobConfigMongoDB(BookEntity.class));

        assertTrue(deepMongoRDD instanceof MongoEntityRDD);

        assertFalse(deepMongoRDD instanceof MongoCellRDD);

        sc.stop();
    }

    @Test
    public void testInstantiationWithJarsAndEnv() {
        Map<String, String> env = new HashMap<>();

        MongoKinaContext sc = new MongoKinaContext("local", "myapp1", "/tmp", new String[]{"", ""}, env);

        assertNotNull(sc);
        DeepMongoRDD deepMongoRDD = sc.mongoRDD(new CellDeepJobConfigMongoDB());

        assertTrue(deepMongoRDD instanceof MongoCellRDD);

        assertFalse(deepMongoRDD instanceof MongoEntityRDD);


        deepMongoRDD = sc.mongoRDD(new EntityDeepJobConfigMongoDB(BookEntity.class));

        assertTrue(deepMongoRDD instanceof MongoEntityRDD);

        assertFalse(deepMongoRDD instanceof MongoCellRDD);


        sc.stop();
    }
}
