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

import kina.context.CassandraKinaContext;
import kina.context.KinaContext;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.testng.annotations.Test;

/**
 * Tests KinaContext instantiations.
 */
@Test(suiteName = "CassandraKinaContextTest")
public class CassandraKinaContextTest {

    public void testInstantiationBySparkContext() {
        KinaContext sc = new CassandraKinaContext(new SparkContext("local", "myapp1", new SparkConf()));

        sc.stop();
    }

    public void testInstantiationWithJar() {
        KinaContext sc = new CassandraKinaContext("local", "myapp1", "/tmp", "");
        sc.stop();
    }


    public void testInstantiationWithJars() {
        KinaContext sc = new CassandraKinaContext("local", "myapp1", "/tmp", new String[]{"", ""});
        sc.stop();
    }

    public void testInstantiationWithJarsAndEnv() {
        Map<String, String> env = new HashMap<>();
        KinaContext sc = new CassandraKinaContext("local", "myapp1", "/tmp", new String[]{"", ""}, env);
        sc.stop();
    }
}
