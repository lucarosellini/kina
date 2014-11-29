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

import java.io.Serializable;
import java.util.Map;

import kina.entity.Cell;

/**
 * Defines the public methods that each Kina configuration object should implement.
 *
 * @param <T> the generic type associated to this configuration object.
 */
public interface KinaConfig<T, S extends KinaConfig> extends Serializable {

    /**
     * Returns the password needed to authenticate
     * to the remote datastore cluster.
     *
     * @return the password used to login to the remote cluster.
     */
    public abstract String getPassword();

    /**
     * Fetches table metadata from the underlying datastore and generates a Map<K, V> where the key is the column name, and the value
     * is the {@link kina.entity.Cell} containing column's metadata.
     *
     * @return the map of column names and the corresponding Cell object containing its metadata.
     */
    Map<String, Cell> columnDefinitions();

    /**
     * Sets the number of rows to retrieve for each page of data fetched from Cassandra.<br/>
     * Defaults to 1000 rows.
     *
     * @param pageSize the number of rows per page
     * @return this configuration object.
     */
    public abstract S pageSize(int pageSize);


    /* Getters */

    /**
     * Returns the underlying testentity class used to map the Cassandra
     * Column family.
     *
     * @return the entity class object associated to this configuration object.
     */
    public abstract Class<T> getEntityClass();

    /**
     * Returns the hostname of the cassandra server.
     *
     * @return the endpoint of the cassandra server.
     */
    public abstract String getHost();

    /**
     * Returns the list of column names that will
     * be fetched from the underlying datastore.
     *
     * @return the array of column names that will be retrieved from the data store.
     */
    public abstract String[] getInputColumns();

    /**
     * Returns the username used to authenticate to the cassandra server.
     * Defaults to the empty string.
     *
     * @return the username to use to login to the remote server.
     */
    public abstract String getUsername();

    /**
     * Sets the datastore hostname
     *
     * @param hostname the cassandra server endpoint.
     * @return this object.
     */
    public abstract S host(String hostname);

    /**
     * Initialized the current configuration object.
     *
     * @return this object.
     */
    public abstract S initialize();

    /**
     * Defines a projection over the CF columns. <br/>
     * Key columns will always be returned, even if not specified in the columns input array.
     *
     * @param columns list of columns we want to retrieve from the datastore.
     * @return this object.
     */
    public abstract S inputColumns(String... columns);

    /**
     * Sets the password to use to login to Cassandra. Leave empty if you do not need authentication.
     *
     * @return this object.
     */
    public abstract S password(String password);

    /**
     * /**
     * Sets the username to use to login to Cassandra. Leave empty if you do not need authentication.
     *
     * @return this object.
     */
    public abstract S username(String username);



    /**
     * Returns the maximum number of rows that will be retrieved when fetching data pages from Cassandra.
     *
     * @return the page size
     */
    public abstract int getPageSize();
}
