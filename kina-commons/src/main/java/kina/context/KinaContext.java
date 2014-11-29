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


import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * Entry point to the Cassandra-aware Spark context.
 *
 * @author Luca Rosellini <luca.rosellini@gmail.com>
 */
public abstract class KinaContext extends JavaSparkContext {

    private static final Logger LOG = Logger.getLogger(KinaContext.class);

    /**
     * Overridden superclass constructor.
     *
     * @param sc an already created spark context.
     */
    KinaContext(SparkContext sc) {
        super(sc);
    }

    /**
     * Overridden superclass constructor.
     *
     * @param master  the url of the master node.
     * @param appName the name of the application.
     */
    KinaContext(String master, String appName) {
        super(master, appName);
    }

    /**
     * Overridden superclass constructor.
     *
     * @param master    the url of the master node.
     * @param appName   the name of the application.
     * @param sparkHome the spark home folder.
     * @param jarFile   the jar file to serialize and send to all the cluster nodes.
     */
    KinaContext(String master, String appName, String sparkHome, String jarFile) {
        super(master, appName, sparkHome, jarFile);
    }

    /**
     * Overridden superclass constructor.
     *
     * @param master    the url of the master node.
     * @param appName   the name of the application.
     * @param sparkHome the spark home folder.
     * @param jars      the jar file(s) to serialize and send to all the cluster nodes.
     */
    KinaContext(String master, String appName, String sparkHome, String[] jars) {
        super(master, appName, sparkHome, jars);
    }

    /**
     * Overridden superclass constructor.
     *
     * @param master      the url of the master node.
     * @param appName     the name of the application.
     * @param sparkHome   the spark home folder.
     * @param jars        the jar file(s) to serialize and send to all the cluster nodes.
     * @param environment a map of environment variables.
     */
    KinaContext(String master, String appName, String sparkHome, String[] jars,
				    Map<String, String> environment) {
        super(master, appName, sparkHome, jars, environment);
    }

}
