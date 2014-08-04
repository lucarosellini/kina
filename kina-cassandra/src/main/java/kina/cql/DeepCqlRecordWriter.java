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

package kina.cql;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import kina.config.CassandraKinaConfig;
import kina.entity.CassandraCell;
import kina.entity.Cells;
import kina.exceptions.*;
import kina.utils.Pair;
import kina.utils.Utils;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.TypeParser;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.SyntaxException;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.spark.TaskContext;

import static kina.rdd.CassandraRDDUtils.updateQueryGenerator;
import static kina.utils.Utils.quote;

/**
 * Handles the distributed write to cassandra in batch.
 */
public class DeepCqlRecordWriter implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeepCqlRecordWriter.class);

    // handles for clients for each range exhausted in the threadpool
    private final Map<Token, RangeClient> clients;
    private final Map<Token, RangeClient> removedClients;

    private AbstractType<?> keyValidator;
    private String[] partitionKeyColumns;

    private final CassandraKinaConfig writeConfig;
    private final IPartitioner partitioner;
    private final InetAddress localhost;

    /**
     * Con
     *
     * @param context
     * @param writeConfig
     */
    public DeepCqlRecordWriter(TaskContext context, CassandraKinaConfig writeConfig) {
        this.clients = new HashMap<>();
        this.removedClients = new HashMap<>();
        this.writeConfig = writeConfig;
        this.partitioner = RangeUtils.getPartitioner(writeConfig);
        try {
            this.localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new kina.exceptions.InstantiationException("Cannot resolve local hostname", e);
        }
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        LOG.debug("Closing all clients");
        // close all the clients before throwing anything
        for (RangeClient client : clients.values()) {
            try {
                IOUtils.closeQuietly(client);
            } catch (Exception e) {
                LOG.error("exception", e);
            }
        }

        for (RangeClient client : removedClients.values()) {
            try {
                IOUtils.closeQuietly(client);
            } catch (Exception e) {
                LOG.error("exception", e);
            }
        }
    }

    private ByteBuffer getPartitionKey(Cells cells) {
        ByteBuffer partitionKey;
        if (keyValidator instanceof CompositeType) {
            ByteBuffer[] keys = new ByteBuffer[partitionKeyColumns.length];

            for (int i = 0; i < cells.size(); i++) {
                CassandraCell c = (CassandraCell) cells.getCellByIdx(i);

                if (c.isPartitionKey()) {
                    keys[i] = c.getDecomposedCellValue();
                }
            }

            partitionKey = CompositeType.build(keys);
        } else {
            partitionKey = ((CassandraCell)cells.getCellByIdx(0)).getDecomposedCellValue();
        }
        return partitionKey;
    }

    private void init() {
        try {
            retrievePartitionKeyValidator();

        } catch (Exception e) {
            throw new GenericException(e);
        }
    }

    private AbstractType<?> parseType(String type) throws ConfigurationException {
        try {
            // always treat counters like longs, specifically CCT.serialize is not what we need
            if (type != null && "org.apache.cassandra.db.marshal.CounterColumnType".equals(type)) {
                return LongType.instance;
            }
            return TypeParser.parse(type);
        } catch (SyntaxException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    /**
     * retrieve the key validator from system.schema_columnfamilies table
     */
    protected void retrievePartitionKeyValidator() throws ConfigurationException {
        Pair<Session, String> sessionWithHost =
                CassandraClientProvider.trySessionForLocation(localhost.getHostAddress(), writeConfig, false);

        String keyspace = writeConfig.getKeyspace();
        String cfName = writeConfig.getColumnFamily();

        Row row = getRowMetadata(sessionWithHost, keyspace, cfName);

        if (row == null) {
            throw new IOException(String.format("cannot find metadata for %s.%s", keyspace, cfName));
        }

        String validator = row.getString("key_validator");
        keyValidator = parseType(validator);

        String keyString = row.getString("key_aliases");
        LOG.debug("partition keys: " + keyString);

        List<String> keys = FBUtilities.fromJsonList(keyString);
        partitionKeyColumns = new String[keys.size()];
        int i = 0;
        for (String key : keys) {
            partitionKeyColumns[i] = key;
            i++;
        }

        String clusterColumnString = row.getString("column_aliases");

        LOG.debug("cluster columns: " + clusterColumnString);
    }

    /**
     * Fetches row metadata for the given column family.
     *
     * @param sessionWithHost the connection to the DB.
     * @param keyspace        the keyspace name
     * @param cfName          the column family
     * @return the Row object
     */
    private static Row getRowMetadata(Pair<Session, String> sessionWithHost, String keyspace, String cfName) {
        String query =
                "SELECT key_validator,key_aliases,column_aliases " +
                        "FROM system.schema_columnfamilies " +
                        "WHERE keyspace_name='%s' and columnfamily_name='%s' ";
        String formatted = String.format(query, keyspace, cfName);
        ResultSet resultSet = sessionWithHost.left.execute(formatted);
        return resultSet.one();
    }

    /**
     * Adds the provided row to a batch. If the batch size reaches the threshold configured in KinaConfig.getBatchSize
     * the batch will be sent to the data store.
     *
     * @param keys   the Cells object containing the row keys.
     * @param values the Cells object containing all the other row  columns.
     */
    public void write(Cells keys, Cells values) {
        /* generate SQL */
        String localCql = updateQueryGenerator(keys, values, writeConfig.getKeyspace(),
                quote(writeConfig.getColumnFamily()));

        Token range = partitioner.getToken(getPartitionKey(keys));

        // add primary key columns to the bind variables
        List<Object> allValues = new ArrayList<>(values.getCellValues());
        allValues.addAll(keys.getCellValues());

        // get the client for the given range, or create a new one
        RangeClient client = clients.get(range);
        if (client == null) {
            // haven't seen keys for this range: create new client
            client = new RangeClient();
            clients.put(range, client);
        }

        if (client.put(localCql, allValues)) {
            removedClients.put(range, clients.remove(range));
        }

    }

    /**
     * A client that runs in a threadpool and connects to the list of endpoints for a particular
     * range. Bound variables for keys in that range are sent to this client via a queue.
     */
    private class RangeClient extends Thread implements Closeable {

        private final int batchSize = writeConfig.getBatchSize();
        private List<String> batchStatements = new ArrayList<>();
        private List<Object> bindVariables = new ArrayList<>();
        private UUID identity = UUID.randomUUID();
        private String cql;

        /**
         * Returns true if adding the current element triggers the batch execution.
         *
         * @param stmt   the query to add to the batch.
         * @param values the list of binding values.
         * @return a boolean indicating if the batch has been triggered or not.
         */
        public synchronized boolean put(String stmt, List<Object> values) {
            batchStatements.add(stmt);
            bindVariables.addAll(values);

            boolean res = batchStatements.size() >= batchSize;
            if (res) {
                triggerBatch();
            }
            return res;
        }

        /**
         * Triggers the execution of the batch
         */
        private void triggerBatch() {
            if (getState() != Thread.State.NEW) {
                return;
            }

            cql = Utils.batchQueryGenerator(batchStatements);
            this.start();
        }

        /**
         * Constructs an {@link RangeClient} for the given endpoints.
         */
        public RangeClient() {
            LOG.debug("Create client " + this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return identity.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws java.io.IOException {
            LOG.debug("[" + this + "] Called close on client, state: " + this.getState());
            triggerBatch();

            try {
                this.join();
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        }

        /**
         * Loops collecting cql binded variable values from the queue and sending to Cassandra
         */
        @Override
        public void run() {
            LOG.debug("[" + this + "] Initializing cassandra client");
            Pair<Session, String> sessionWithHost = CassandraClientProvider.trySessionForLocation(localhost
                    .getHostAddress(), writeConfig, false);
            sessionWithHost.left.execute(cql, bindVariables.toArray(new Object[bindVariables.size()]));
        }
    }
}
