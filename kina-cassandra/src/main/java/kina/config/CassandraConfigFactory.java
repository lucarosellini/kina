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

package kina.config;

import java.io.*;
import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import javassist.*;
import kina.entity.Cells;
import kina.entity.KinaType;
import kina.exceptions.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Factory class for kina configuration objects.
 *
 * @author Luca Rosellini <luca.rosellini@gmail.com>
 */
public final class CassandraConfigFactory implements Serializable {
    private static final Logger LOG = Logger.getLogger(CassandraConfigFactory.class);

    private static final long serialVersionUID = -4559130919203819088L;


    /**
     * private constructor
     */
    private CassandraConfigFactory() {
    }

    /**
     * Creates a new cell-based job configuration object.
     *
     * @return a new cell-based job configuration object.
     */
    public static CassandraKinaConfig<Cells> create() {
        return new CellCassandraKinaConfig(false);
    }

    /**
     * Creates a new cell-based write suitable job configuration object.
     *
     * @return a new cell-based write suitable job configuration object.
     */
    public static CassandraKinaConfig<Cells> createWriteConfig() {
	    return new CellCassandraKinaConfig(true);
    }

    /**
     * Creates an entity-based configuration object.
     *
     * @param entityClass the class instance of the entity class that will be used to map db objects to Java objects.
     * @param <T> the generic type of the entity object implementing KinaType.
     * @return a new an entity-based configuration object.
     */
    public static <T extends KinaType> CassandraKinaConfig<T> create(Class<T> entityClass) {
        return new EntityCassandraKinaConfig<>(entityClass, false);
    }

    /**
     * Creates an entity-based write configuration object.
     *
     * @return an entity-based write configuration object.
     */
    public static <T extends KinaType> CassandraKinaConfig<T> createWriteConfig(Class<T> entityClass) {
	    return new EntityCassandraKinaConfig<>(entityClass, true);
    }



}
