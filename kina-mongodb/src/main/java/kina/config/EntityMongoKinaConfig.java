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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import kina.entity.KinaType;
import kina.utils.UtilMongoDB;

/**
 * Class containing the appropriate configuration for a MongoEntityRDD.
 * <p/>
 * Remember to call {@link #initialize()} after having configured all the
 * properties.
 *
 * @param <T>
 */
public final class EntityMongoKinaConfig<T extends KinaType> extends GenericMongoKinaConfig<T> {

    private static final long serialVersionUID = 123;

    /**
     *
     */
    private Map<String, String> mapDBNameToEntityName;

    /**
     * @param entityClass
     */
    public EntityMongoKinaConfig(Class<T> entityClass) {
        super();
        this.entityClass = entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericMongoKinaConfig<T> initialize() {
        super.initialize();

        Map<String, String> tmpMap = new HashMap<>();

        Field[] kinaFields = UtilMongoDB.filterKinaFields(entityClass);

        for (Field f : kinaFields) {
            String dbName = UtilMongoDB.kinaFieldName(f);
            String beanFieldName = f.getName();

            tmpMap.put(dbName, beanFieldName);
        }

        mapDBNameToEntityName = Collections.unmodifiableMap(tmpMap);

        return this;
    }


    public Map<String, String> getMapDBNameToEntityName() {
        return mapDBNameToEntityName;
    }

    public void setMapDBNameToEntityName(Map<String, String> mapDBNameToEntityName) {
        this.mapDBNameToEntityName = mapDBNameToEntityName;
    }
}
