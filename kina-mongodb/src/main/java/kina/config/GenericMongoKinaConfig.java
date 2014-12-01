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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.QueryBuilder;
import com.mongodb.hadoop.MongoInputFormat;
import com.mongodb.hadoop.util.MongoConfigUtil;

import kina.entity.Cell;

import kina.rdd.mongodb.KinaBSONFileInputFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;


/**
 * Base class for all mongo-specific configuration objects.
 *
 * @param <T>
 */
public class GenericMongoKinaConfig<T> implements MongoKinaConfig<T> {
    private static final long serialVersionUID = -7179376653643603038L;


    /**
     * configuration to be broadcasted to every spark node
     */
    private transient Configuration configHadoop;

    /**
     * A list of mongodb host to connect
     */
    private List<String> hostList = new ArrayList<>();

    /**
     * A local or HDFS path containing bson dump files to process
     */
    private String bsonFile;

    private Boolean recursiveBsonFileDiscovery;

    /**
     * MongoDB username
     */
    private String username;

    /**
     * MongoDB password
     */

    private String password;

    /**
     * Indicates the replica set's name
     */
    private String replicaSet;

    /**
     * Collection to get or insert data
     */
    private String collection;

    /**
     * Database to connect
     */
    private String database;

    /**
     * Read Preference
     * primaryPreferred is the recommended read preference. If the primary node go down, can still read from secundaries
     */
    private String readPreference;

    /**
     * Entity class to map BSONObject
     */
    protected Class<T> entityClass;

    /**
     * VIP, this MUST be transient!
     */
    private transient Map<String, Cell> columnDefinitionMap;

    private String[] inputColumns;

    private static String[] bsonFilesExcludePatterns;

    /**
     * OPTIONAL
     * filter query
     */
    private String query;

    /**
     * OPTIONAL
     * fields to be returned
     */
    private BSONObject fields;

    /**
     * OPTIONAL
     * sorting
     */
    private String sort;


    /**
     * Shard key
     */
    private String inputKey;


    private boolean createInputSplit = true;


    private boolean useShards = false;


    private boolean splitsUseChunks = true;

    private Class<? extends InputFormat<Object, BSONObject>> inputFormatClass;

    /**
     * Default constructor
     */
    public GenericMongoKinaConfig() {

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Cell> columnDefinitions() {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    // TODO
    @Override
    public MongoKinaConfig<T> pageSize(int pageSize) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHost() {
        return !hostList.isEmpty() ? hostList.get(0) : null;
    }

    public List<String> getHostList() {
        return hostList;
    }

    /**
     * {@inheritDoc}
     */
    // TODO
    @Override
    public String[] getInputColumns() {
        return fields.keySet().toArray(new String[fields.keySet().size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> host(String host) {
        this.hostList.add(host);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> host(List<String> host) {
        this.hostList.addAll(host);
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> filterQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> filterQuery(BSONObject query) {
        this.query = query.toString();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> filterQuery(QueryBuilder query) {
        this.query = query.get().toString();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> replicaSet(String replicaSet) {
        this.replicaSet = replicaSet;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> database(String database) {
        this.database = database;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> collection(String collection) {
        this.collection = collection;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> username(String username) {
        this.username = username;
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> fields(BSONObject fields) {
        this.fields = fields;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> sort(String sort) {
        this.sort = sort;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> sort(BSONObject sort) {
        this.sort = sort.toString();
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> createInputSplit(boolean createInputSplit) {
        this.createInputSplit = createInputSplit;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> useShards(boolean useShards) {
        this.useShards = useShards;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> splitsUseChunks(boolean splitsUseChunks) {
        this.splitsUseChunks = splitsUseChunks;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> inputKey(String inputKey) {
        this.inputKey = inputKey;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> bsonFile(String bsonFile, Boolean recursive){
        this.bsonFile = bsonFile;
        this.recursiveBsonFileDiscovery = recursive != null ? recursive : Boolean.FALSE;
        return this;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> bsonFilesExcludePatterns(String[] bsonFilesExcludePatterns){
        this.bsonFilesExcludePatterns = bsonFilesExcludePatterns;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    // TODO
    @Override
    public int getPageSize() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> password(String password) {
        this.password = password;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> readPreference(String readPreference) {
        this.readPreference = readPreference;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> ignoreIdField() {
        BSONObject bsonFields = fields != null ? fields : new BasicBSONObject();
        bsonFields.put("_id", 0);
        fields = bsonFields;
        return this;
    }

    private void initRealClusterConfig(){
        StringBuilder connection = new StringBuilder();

        connection.append("mongodb").append(":").append("//");

        boolean firstHost = true;
        for (String host : hostList) {
            if (!firstHost) {
                connection.append(",");
            }
            connection.append(host);
            firstHost = false;
        }


        if (username != null && password != null) {
            connection.append(username).append(":").append(password).append("@");
        }

        connection.append("/").append(database).append(".").append(collection);

        StringBuilder options = new StringBuilder();
        boolean asignado = false;


        if (readPreference != null) {
            asignado = true;
            options.append("?readPreference=").append(readPreference);
        }

        if (replicaSet != null) {
            if (asignado) {
                options.append("&");
            } else {
                options.append("?");
            }
            options.append("replicaSet=").append(replicaSet);
        }

        connection.append(options);

        configHadoop.set(MongoConfigUtil.INPUT_URI, connection.toString());
        configHadoop.set(MongoConfigUtil.OUTPUT_URI, connection.toString());
        if (username != null && password != null) {
            configHadoop.set(MongoConfigUtil.AUTH_URI, connection.toString());
        }

        if (inputKey != null) {
            configHadoop.set(MongoConfigUtil.INPUT_KEY, inputKey);
        }


        inputFormatClass = MongoInputFormat.class;
        configHadoop.set(MongoConfigUtil.SPLITS_USE_SHARDS, String.valueOf(useShards));
        configHadoop.set(MongoConfigUtil.CREATE_INPUT_SPLITS, String.valueOf(createInputSplit));
        configHadoop.set(MongoConfigUtil.SPLITS_USE_CHUNKS, String.valueOf(splitsUseChunks));

    }

    private void initBSONDumpConfig(){
        inputFormatClass = KinaBSONFileInputFormat.class;

        Path path = new Path(bsonFile);
        try {
            path = path.getFileSystem(configHadoop).makeQualified(path);
            if (!path.getFileSystem(configHadoop).exists(path)){
                throw new IOException(new FileNotFoundException(path.getName()));
            }

            String dirStr = org.apache.hadoop.util.StringUtils.escapeString(path.toString());
            String dirs = configHadoop.get(FileInputFormat.INPUT_DIR);
            configHadoop.set(FileInputFormat.INPUT_DIR, dirs == null ? dirStr : dirs + "," + dirStr);
            configHadoop.set(FileInputFormat.INPUT_DIR_RECURSIVE, recursiveBsonFileDiscovery.toString());

            configHadoop.setClass(FileInputFormat.PATHFILTER_CLASS, KinaMongoPathFilter.class,
                    PathFilter.class);

            if (bsonFilesExcludePatterns != null){
                configHadoop.setStrings(KinaMongoPathFilter.PATH_FILTER_CONF, bsonFilesExcludePatterns);
            }

        } catch (IOException e) {
            throw new kina.exceptions.IOException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericMongoKinaConfig<T> initialize() {
        validate();
        configHadoop = new Configuration();

        if (StringUtils.isEmpty(bsonFile)){
            initRealClusterConfig();
        } else {
            initBSONDumpConfig();
        }

        configHadoop.set(MongoConfigUtil.JOB_INPUT_FORMAT, inputFormatClass.getCanonicalName());

        if (query != null) {
            configHadoop.set(MongoConfigUtil.INPUT_QUERY, query);
        }

        if (fields != null) {
            configHadoop.set(MongoConfigUtil.INPUT_FIELDS, fields.toString());
        }


        if (sort != null) {
            configHadoop.set(MongoConfigUtil.INPUT_SORT, sort);
        }

        return this;
    }

    /**
     * validates connection parameters
     */
    private void validate() {

        if (StringUtils.isEmpty(bsonFile)) {
            if (hostList.isEmpty()) {
                throw new IllegalArgumentException("host cannot be null");
            }
            if (database == null) {
                throw new IllegalArgumentException("database cannot be null");
            }
            if (collection == null) {
                throw new IllegalArgumentException("collection cannot be null");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MongoKinaConfig<T> inputColumns(String... columns) {
        BSONObject bsonFields = fields != null ? fields : new BasicBSONObject();
        for (String column : columns) {
            bsonFields.put(column, 1);
        }
        fields = bsonFields;
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getHadoopConfiguration() {
        if (configHadoop == null) {
            initialize();
        }
        return configHadoop;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getBsonFile() {
        return bsonFile;
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends InputFormat<Object,BSONObject>> getInputFormatClass() {
        return inputFormatClass;
    }
}
