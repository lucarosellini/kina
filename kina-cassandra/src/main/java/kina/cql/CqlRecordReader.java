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

import java.nio.ByteBuffer;
import java.util.*;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import kina.config.GenericCassandraKinaConfig;
import kina.config.CassandraKinaConfig;
import kina.entity.CassandraCell;
import kina.exceptions.GenericException;
import kina.exceptions.IOException;
import kina.exceptions.IllegalAccessException;
import kina.partition.impl.KinaPartitionLocationComparator;
import kina.utils.Pair;
import kina.utils.Utils;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.dht.IPartitioner;

import static kina.cql.CassandraClientProvider.trySessionForLocation;
import static kina.utils.Utils.additionalFilterGenerator;

/**
 * Implements a cassandra record reader with pagination capabilities.
 * Does not rely on Cassandra's Hadoop CqlPagingRecordReader.
 *
 * Pagination is outsourced to Datastax Java Driver.
 *
 * @author Luca Rosellini <luca@strat.io>
 */
public class CqlRecordReader {
    private static final Logger LOG = LoggerFactory.getLogger(CqlRecordReader.class);

    private Range split;
    private RowIterator rowIterator;

    private String cfName;

    // partition keys -- key aliases
    private List<BoundColumn> partitionBoundColumns = new ArrayList<>();

    // cluster keys -- column aliases
    private List<BoundColumn> clusterColumns = new ArrayList<>();

    // cql query select columns
    private String columns;

    // the number of cql rows per page
    private final int pageSize;

    private IPartitioner partitioner;

    private AbstractType<?> keyValidator;

    private final CassandraKinaConfig config;

    private Session session;

    /**
     * public constructor. Takes a list of filters to pass to the underlying data stores.
     *
     * @param config the Kina configuration object.
     * @param split  the token range on which the new reader will be based.
     */
    public CqlRecordReader(CassandraKinaConfig config, Range split) {
        this.config = config;
        this.split = split;
	    this.pageSize = config.getPageSize();
        initialize();
    }

    /**
     * Initialized this object.
     * <p>Creates a new client and row iterator.</p>
     */
    private void initialize() {
        cfName = config.getTable();

        if (!ArrayUtils.isEmpty(config.getInputColumns())) {
            columns = StringUtils.join(config.getInputColumns(), ",");
        }

        partitioner = Utils.newTypeInstance(config.getPartitionerClassName(), IPartitioner.class);

        try {
            session = createConnection();

            retrieveKeys();
        } catch (Exception e) {
            throw new IOException(e);
        }

        rowIterator = new RowIterator();
    }

    /**
     * Creates a new connection. Reuses a cached connection if possible.
     *
     * @return the new session
     */
    private Session createConnection() {

        /* reorder locations */
        List<String> locations = Lists.newArrayList(split.getReplicas());
        Collections.sort(locations, new KinaPartitionLocationComparator());

        Exception lastException = null;

        LOG.debug("createConnection: " + locations);
        for (String location : locations) {

            try {
                return trySessionForLocation(location, config, false).left;
            } catch (Exception e) {
                LOG.error("Could not get connection for: {}, replicas: {}", location, locations);
                lastException = e;
            }
        }

        throw new IOException(lastException);
    }

    /**
     * Closes this input reader object.
     */
    public void close() {
        /* dummy close method, no need to close any resource here */
    }

    /**
     * Creates a new empty LinkedHashMap.
     *
     * @return the map of associations between row column names and their values.
     */
    public Map<String, ByteBuffer> createEmptyMap() {
        return new LinkedHashMap<String, ByteBuffer>();
    }

    /**
     * CQL row iterator
     */
    class RowIterator extends AbstractIterator<Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>>> {
        private Iterator<Row> rows;
        private String partitionKeyString;       // keys in <key1>, <key2>, <key3> string format
        private String partitionKeyMarkers;      // question marks in ? , ? , ? format which matches the number of keys

        /**
         * Default constructor.
         */
        public RowIterator() {
            // initial page
            executeQuery();
        }

        private boolean isColumnWanted(String columnName) {
            return ArrayUtils.isEmpty(config.getInputColumns()) ||
                    ArrayUtils.contains(config.getInputColumns(), columnName);
        }

        /**
         * {@inheritDoc}
         */
        protected Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>> computeNext() {
            if (rows == null || !rows.hasNext()) {
                return endOfData();
            }

	        Map<String, ByteBuffer> valueColumns = createEmptyMap();
	        Map<String, ByteBuffer> keyColumns = createEmptyMap();

	        initColumns(valueColumns, keyColumns);

	        return Pair.create(keyColumns, valueColumns);
        }

        private void initColumns(Map<String, ByteBuffer> valueColumns, Map<String, ByteBuffer> keyColumns) {
            Row row = rows.next();
            TableMetadata tableMetadata = ((GenericCassandraKinaConfig) config).fetchTableMetadata();

            List<ColumnMetadata> partitionKeys = tableMetadata.getPartitionKey();
            List<ColumnMetadata> clusteringKeys = tableMetadata.getClusteringColumns();
            List<ColumnMetadata> allColumns = tableMetadata.getColumns();

            for (ColumnMetadata key : partitionKeys) {
                String columnName = key.getName();
                ByteBuffer bb = row.getBytesUnsafe(columnName);
                keyColumns.put(columnName, bb);
            }
            for (ColumnMetadata key : clusteringKeys) {
                String columnName = key.getName();
                ByteBuffer bb = row.getBytesUnsafe(columnName);
                keyColumns.put(columnName, bb);
            }
            for (ColumnMetadata key : allColumns) {
                String columnName = key.getName();
                if (keyColumns.containsKey(columnName) || !isColumnWanted(columnName)) {
                    continue;
                }

                ByteBuffer bb = row.getBytesUnsafe(columnName);
                valueColumns.put(columnName, bb);
            }
        }

        /**
         * serialize the prepared query, pair.left is query id, pair.right is query
         */
        private String composeQuery(String cols) {
            String generatedColumns = cols;
            String clause = whereClause();
            if (generatedColumns == null) {
                generatedColumns = "*";
            } else {
                // add keys in the front in order
                String partitionKey = keyString(partitionBoundColumns);
                String clusterKey = keyString(clusterColumns);

                generatedColumns = withoutKeyColumns(generatedColumns);
	            generatedColumns = (generatedColumns != null ? "," + generatedColumns : "");

                generatedColumns = StringUtils.isEmpty(clusterKey)
                        ? partitionKey + generatedColumns
                        : partitionKey + "," + clusterKey + generatedColumns;
            }

            return String.format("SELECT %s FROM %s%s%s ALLOW FILTERING",
				            generatedColumns, quote(cfName), clause,
				            additionalFilterGenerator(config.getAdditionalFilters()));
        }

        /**
         * remove key columns from the column string
         */
        private String withoutKeyColumns(String columnString) {
            Set<String> keyNames = new HashSet<>();
            for (BoundColumn column : Iterables.concat(partitionBoundColumns, clusterColumns)) {
                keyNames.add(column.name);
            }

            String[] cols = columnString.split(",");
            String result = null;
            for (String column : cols) {
                String trimmed = column.trim();
                if (keyNames.contains(trimmed)) {
                    continue;
                }

                String quoted = quote(trimmed);
                result = result == null ? quoted : result + "," + quoted;
            }
            return result;
        }

        /**
         * serialize the where clause
         */
        private String whereClause() {
            if (partitionKeyString == null) {
                partitionKeyString = keyString(partitionBoundColumns);
            }

            if (partitionKeyMarkers == null) {
                partitionKeyMarkers = partitionKeyMarkers();
            }
            // initial
            // query token(k) >= start_token and token(k) <= end_token
	        return String.format(" WHERE token(%s) > ? AND token(%s) <= ?", partitionKeyString,
					        partitionKeyString);
        }

        /**
         * serialize the partition key string in format of <key1>, <key2>, <key3>
         */
        private String keyString(List<BoundColumn> columns) {
            String result = null;
            for (BoundColumn column : columns) {
                result = result == null ? quote(column.name) : result + "," + quote(column.name);
            }

            return result == null ? "" : result;
        }

        /**
         * serialize the question marks for partition key string in format of ?, ? , ?
         */
        private String partitionKeyMarkers() {
            String result = null;
	        for (BoundColumn partitionBoundColumn : partitionBoundColumns) {
		        result = result == null ? "?" : result + ",?";
	        }

            return result;
        }

        /**
         * serialize the query binding variables, pair.left is query id, pair.right is the binding variables
         */
        private List<Object> preparedQueryBindValues() {
            List<Object> values = new LinkedList<>();

            Object startToken = split.getStartToken();
            Object endToken = split.getEndToken();

	        values.add(startToken);
	        values.add(endToken);
	        return values;
        }

        /**
         * Quoting for working with uppercase
         */
        private String quote(String identifier) {
            return "\"" + identifier.replaceAll("\"", "\"\"") + "\"";
        }

        /**
         * execute the prepared query
         */
        private void executeQuery() {
            String query = composeQuery(columns);

            List<Object> bindValues = preparedQueryBindValues();
            assert bindValues != null;

	        rows = null;

            int retries = 0;

            Exception exception = null;
            // only try three times for TimedOutException and UnavailableException
            while (retries < 3) {
                try {
                    Object[] values = bindValues.toArray(new Object[bindValues.size()]);

	                LOG.debug("query: " + query + "; values: " + Arrays.toString(values));

	                Statement stmt = new SimpleStatement(query, values);
	                stmt.setFetchSize(pageSize);

                    ResultSet resultSet = session.execute(stmt);

                    if (resultSet != null) {
                        rows = resultSet.iterator();
                    }
                    return;
                } catch (NoHostAvailableException e) {
                    LOG.error("Could not connect to ");
                    exception = e;

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        LOG.error("sleep exception", e1);
                    }

                    ++retries;

                } catch (Exception e) {
                    throw new IOException(e);
                }
            }

            if (exception != null) {
                throw new IOException(exception);
            }
        }
    }

    /**
     * retrieve the partition keys and cluster keys from system.schema_columnfamilies table
     */
    private void retrieveKeys() {
        TableMetadata tableMetadata = ((GenericCassandraKinaConfig) config).fetchTableMetadata();

        List<ColumnMetadata> partitionKeys = tableMetadata.getPartitionKey();
        List<ColumnMetadata> clusteringKeys = tableMetadata.getClusteringColumns();

        List<AbstractType<?>> types = new ArrayList<>();

        for (ColumnMetadata key : partitionKeys) {
            String columnName = key.getName();
            BoundColumn boundColumn = new BoundColumn(columnName);
	        boundColumn.validator = CassandraCell.getValueType(key.getType()).getAbstractType();
            partitionBoundColumns.add(boundColumn);
            types.add(boundColumn.validator);
        }
        for (ColumnMetadata key : clusteringKeys) {
            String columnName = key.getName();
            BoundColumn boundColumn = new BoundColumn(columnName);
	        boundColumn.validator = CassandraCell.getValueType(key.getType()).getAbstractType();
            clusterColumns.add(boundColumn);
        }

        if (types.size() > 1) {
            keyValidator = CompositeType.getInstance(types);
        } else if (types.size() == 1) {
            keyValidator = types.get(0);
        } else {
            throw new GenericException("Cannot determine if keyvalidator is composed or not, " +
                    "partitionKeys: " + partitionKeys);
        }
    }

    /**
     * check whether current row is at the end of range
     */
    private boolean reachEndRange() {
        // current row key
        ByteBuffer rowKey;

        if (keyValidator instanceof CompositeType) {
            ByteBuffer[] keys = new ByteBuffer[partitionBoundColumns.size()];
            for (int i = 0; i < partitionBoundColumns.size(); i++) {
                keys[i] = partitionBoundColumns.get(i).value.duplicate();
            }

            rowKey = CompositeType.build(keys);
        } else {
            rowKey = partitionBoundColumns.get(0).value;
        }

        String endToken = String.valueOf(split.getEndToken());
        String currentToken = partitioner.getToken(rowKey).toString();

        return endToken.equals(currentToken);
    }

    private static class BoundColumn {
        private final String name;
        private ByteBuffer value;
        private AbstractType<?> validator;

        public BoundColumn(String name) {
            this.name = name;
        }
    }

    /**
     * Returns a boolean indicating if the underlying rowIterator has a new element or not.
     * DOES NOT advance the iterator to the next element.
     *
     * @return a boolean indicating if the underlying rowIterator has a new element or not.
     */
    public boolean hasNext() {
        return rowIterator.hasNext();
    }

    /**
     * Returns the next element in the underlying rowIterator.
     *
     * @return the next element in the underlying rowIterator.
     */
    public Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>> next() {
        if (!this.hasNext()) {
            throw new IllegalAccessException("CqlRecordReader exhausted");
        }
        return rowIterator.next();
    }
}
