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

package kina.context;/*
 * Copyright 2014, Stratio.
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

import java.util.Map;

import javassist.*;
import kina.config.CassandraKinaConfig;
import kina.config.CellCassandraKinaConfig;
import kina.config.EntityCassandraKinaConfig;
import kina.entity.Cells;
import kina.exceptions.GenericException;
import kina.rdd.CassandraCellRDD;
import kina.rdd.CassandraEntityRDD;
import kina.rdd.CassandraJavaRDD;
import kina.rdd.CassandraRDD;
import kina.utils.Utils;
import org.apache.log4j.Logger;
import org.apache.spark.SparkContext;


/**
 * Cassandra-related kina spark context.
 *
 * Created by luca on 11/07/14.
 */
public class CassandraKinaContext extends KinaContext {

	/**
	 * {@inheritDoc}
	 */
	public CassandraKinaContext(SparkContext sc) {
		super(sc);


	}
	/**
	 * {@inheritDoc}
	 */
	public CassandraKinaContext(String master, String appName) {
		super(master, appName);
        //Utils.instrumentMetadata();
	}

	/**
	 * {@inheritDoc}
	 */
	public CassandraKinaContext(String master, String appName, String sparkHome, String jarFile) {
		super(master, appName, sparkHome, jarFile);
        //Utils.instrumentMetadata();
	}
	/**
	 * {@inheritDoc}
	 */
	public CassandraKinaContext(String master, String appName, String sparkHome, String[] jars) {
		super(master, appName, sparkHome, jars);
        //Utils.instrumentMetadata();
	}
	/**
	 * {@inheritDoc}
	 */
	public CassandraKinaContext(String master, String appName, String sparkHome, String[] jars, Map<String, String> environment) {
		super(master, appName, sparkHome, jars, environment);
        //Utils.instrumentMetadata();
	}

	/**
	 * Builds a new CassandraJavaRDD.
	 *
	 * @param config the Kina configuration object to use to create the new RDD.
	 * @return a new CassandraJavaRDD
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> CassandraJavaRDD<T> cassandraJavaRDD(CassandraKinaConfig<T> config) {
		return new CassandraJavaRDD<>(cassandraRDD(config));
	}

	/**
	 * Builds a new generic Cassandra RDD.
	 *
	 * @param config the Kina configuration object to use to create the new RDD.
	 * @return a new generic CassandraRDD.
	 */
	@SuppressWarnings("unchecked")
	public <T> CassandraRDD<T> cassandraRDD(CassandraKinaConfig<T> config) {
		if (config.getClass().isAssignableFrom(EntityCassandraKinaConfig.class)) {
			return new CassandraEntityRDD(sc(), config);
		}

		if (config.getClass().isAssignableFrom(CellCassandraKinaConfig.class)) {
			return (CassandraRDD<T>) new CassandraCellRDD(sc(), (CassandraKinaConfig<Cells>) config);
		}

		throw new GenericException("not recognized config type");

	}
}
