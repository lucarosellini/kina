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
import kina.config.MongoKinaConfig;
import kina.entity.Cells;
import kina.exceptions.GenericException;
import kina.rdd.mongodb.MongoCellRDD;
import kina.rdd.mongodb.MongoEntityRDD;
import kina.rdd.mongodb.MongoJavaRDD;
import org.apache.log4j.Logger;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.rdd.DeepMongoRDD;

import java.util.Map;

/**
 * Created by luca on 11/07/14.
 */
public class MongoKinaContext extends KinaContext {
    private static final Logger LOG = Logger.getLogger(MongoKinaContext.class);

    /**
     * {@inheritDoc}
     */
    public MongoKinaContext(SparkContext sc) {
        super(sc);
    }

    /**
     * {@inheritDoc}
     */
    public MongoKinaContext(String master, String appName) {
        super(master, appName);
    }

    /**
     * {@inheritDoc}
     */
    public MongoKinaContext(String master, String appName, String sparkHome, String jarFile) {
        super(master, appName, sparkHome, jarFile);
    }

    /**
     * {@inheritDoc}
     */
    public MongoKinaContext(String master, String appName, String sparkHome, String[] jars) {
        super(master, appName, sparkHome, jars);
    }

    /**
     * {@inheritDoc}
     */
    public MongoKinaContext(String master, String appName, String sparkHome, String[] jars, Map<String, String> environment) {
        super(master, appName, sparkHome, jars, environment);
    }

    /**
     * Builds a new entity based MongoEntityRDD
     *
     * @param config
     * @param <T>
     * @return
     */
    public <T> JavaRDD<T> mongoJavaRDD(MongoKinaConfig<T> config) {
        return new MongoJavaRDD<T>(mongoRDD(config));
    }

    /**
     * Builds a new Mongo RDD.
     *
     * @param config
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> DeepMongoRDD<T> mongoRDD(MongoKinaConfig<T> config) {
        if (EntityDeepJobConfigMongoDB.class.isAssignableFrom(config.getClass())) {
            return new MongoEntityRDD(sc(), config);
        }

        if (CellDeepJobConfigMongoDB.class.isAssignableFrom(config.getClass())) {
            return (DeepMongoRDD<T>) new MongoCellRDD(sc(), (MongoKinaConfig<Cells>) config);
        }

        throw new GenericException("not recognized config type");

    }
}
