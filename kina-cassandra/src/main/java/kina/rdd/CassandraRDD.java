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

package kina.rdd;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import kina.config.CassandraKinaConfig;
import kina.cql.CqlRecordReader;
import kina.cql.Range;
import kina.cql.RangeUtils;
import kina.entity.Cells;
import kina.entity.KinaType;
import kina.exceptions.IOException;
import kina.functions.CellList2TupleFunction;
import kina.functions.KinaType2TupleFunction;
import kina.partition.impl.KinaPartition;
import kina.utils.Pair;
import org.apache.spark.InterruptibleIterator;
import org.apache.spark.Partition;
import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.rdd.RDD;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.reflect.ClassTag$;
import scala.runtime.AbstractFunction0;
import scala.runtime.BoxedUnit;

import static scala.collection.JavaConversions.asScalaBuffer;
import static scala.collection.JavaConversions.asScalaIterator;

/**
 * Base class that abstracts the complexity of interacting with the Cassandra Datastore.<br/>
 * Implementors should only provide a way to convert an object of type T to a {@link kina.entity.Cells}
 * element.
 */
public abstract class CassandraRDD<T> extends RDD<T> {

    private static final long serialVersionUID = -7338324965474684418L;

    /**
     * RDD configuration. This config is broadcasted to all the Sparks machines.
     */
    protected final Broadcast<CassandraKinaConfig<T>> config;

    /**
     * Transform a row coming from the Cassandra's API to an element of
     * type <T>.
     *
     * @param elem the element to transform.
     * @return the transformed element.
     */
    protected abstract T transformElement(Pair<Map<String, ByteBuffer>, Map<String, ByteBuffer>> elem);

    /**
     * Helper callback class called by Spark when the current RDD is computed
     * successfully. This class simply closes the {@link org.apache.cassandra.hadoop.cql3.CqlPagingRecordReader}
     * passed as an argument.
     *
     * @param <R>
     * @author Luca Rosellini <luca@strat.io>
     */
    class OnComputedRDDCallback<R> extends AbstractFunction0<R> {
        private final CqlRecordReader recordReader;
        private final KinaPartition kinaPartition;

        public OnComputedRDDCallback(
                CqlRecordReader recordReader,
                KinaPartition dp) {
            super();
            this.recordReader = recordReader;
            this.kinaPartition = dp;
        }

        @Override
        public R apply() {
            recordReader.close();

            return null;
        }

    }

    /**
     * Persists the given RDD to the underlying Cassandra datastore using the java cql3 driver.<br/>
     * Beware: this method does not perform a distributed write as
     * {@link CassandraRDD#saveRDDToCassandra}
     * does, uses the Datastax Java Driver to perform a batch write to the Cassandra server.<br/>
     * This currently scans the partitions one by one, so it will be slow if a lot of partitions are required.
     *
     * @param rdd         the RDD to persist.
     * @param writeConfig the write configuration object.
     */
    @SuppressWarnings("unchecked")
    public static <W, T extends KinaType> void cql3SaveRDDToCassandra(RDD<W> rdd, CassandraKinaConfig<W> writeConfig) {
        if (KinaType.class.isAssignableFrom(writeConfig.getEntityClass())) {
            CassandraKinaConfig<T> c = (CassandraKinaConfig<T>) writeConfig;
            RDD<T> r = (RDD<T>) rdd;

            CassandraRDDUtils.doCql3SaveToCassandra(r, c, new KinaType2TupleFunction<T>());
        } else if (Cells.class.isAssignableFrom(writeConfig.getEntityClass())) {
            CassandraKinaConfig<Cells> c = (CassandraKinaConfig<Cells>) writeConfig;
            RDD<Cells> r = (RDD<Cells>) rdd;

            CassandraRDDUtils.doCql3SaveToCassandra(r, c, new CellList2TupleFunction());
        } else {
            throw new IllegalArgumentException("Provided RDD must be an RDD of Cells or an RDD of KinaType");
        }
    }

    /**
     * Persists the given RDD of Cells to the underlying Cassandra datastore, using configuration
     * options provided by <i>writeConfig</i>.
     *
     * @param rdd         the RDD to persist.
     * @param writeConfig the write configuration object.
     */
    @SuppressWarnings("unchecked")
    public static <W, T extends KinaType> void saveRDDToCassandra(RDD<W> rdd, CassandraKinaConfig<W> writeConfig) {
        if (KinaType.class.isAssignableFrom(writeConfig.getEntityClass())) {
            CassandraKinaConfig<T> c = (CassandraKinaConfig<T>) writeConfig;
            RDD<T> r = (RDD<T>) rdd;

            CassandraRDDUtils.doSaveToCassandra(r, c, new KinaType2TupleFunction<T>());
        } else if (Cells.class.isAssignableFrom(writeConfig.getEntityClass())) {
            CassandraKinaConfig<Cells> c = (CassandraKinaConfig<Cells>) writeConfig;
            RDD<Cells> r = (RDD<Cells>) rdd;

            CassandraRDDUtils.doSaveToCassandra(r, c, new CellList2TupleFunction());
        } else {
            throw new IllegalArgumentException("Provided RDD must be an RDD of Cells or an RDD of KinaType");
        }
    }

    /**
     * Persists the given JavaRDD to the underlying Cassandra datastore.
     *
     * @param rdd         the RDD to persist.
     * @param writeConfig the write configuration object.
     * @param <W>         the generic type associated to the provided configuration object.
     */
    public static <W> void saveRDDToCassandra(JavaRDD<W> rdd, CassandraKinaConfig<W> writeConfig) {
        saveRDDToCassandra(rdd.rdd(), writeConfig);
    }


    /**
     * Public constructor that builds a new Cassandra RDD given the context and the configuration file.
     *
     * @param sc     the spark context to which the RDD will be bound to.
     * @param config the kina configuration object.
     */
    @SuppressWarnings("unchecked")
    public CassandraRDD(SparkContext sc, CassandraKinaConfig<T> config) {
        super(sc, scala.collection.Seq$.MODULE$.empty(), ClassTag$.MODULE$.<T>apply(config.getEntityClass()));
        this.config = sc.broadcast(config, ClassTag$.MODULE$.<CassandraKinaConfig<T>>apply(config.getClass()));
    }

    /**
     * Computes the current RDD over the given data partition. Returns an
     * iterator of Scala tuples.
     */
    @Override
    public Iterator<T> compute(Partition split, TaskContext ctx) {

        KinaPartition kinaPartition = (KinaPartition) split;

        log().debug("Executing compute for split: " + kinaPartition);

        final CqlRecordReader recordReader = initRecordReader(ctx, kinaPartition);

        /**
         * Creates a new anonymous iterator inner class and returns it as a
         * scala iterator.
         */
        java.util.Iterator<T> recordReaderIterator = new java.util.Iterator<T>() {

            @Override
            public boolean hasNext() {
                return recordReader.hasNext();
            }

            @Override
            public T next() {
                return transformElement(recordReader.next());
            }

            @Override
            public void remove() {
                throw new IOException("Method not implemented (and won't be implemented anytime soon!!!)");
            }
        };

        return new InterruptibleIterator<T>(ctx, asScalaIterator(recordReaderIterator));
    }

    /**
     * Gets an instance of the callback that will be used on the completion of the computation of this RDD.
     *
     * @param recordReader the kina record reader.
     * @param dp           the spark kina partition.
     * @return an instance of the callback that will be used on the completion of the computation of this RDD.
     */
    protected AbstractFunction0<BoxedUnit> getComputeCallback(CqlRecordReader recordReader,
                                                              KinaPartition dp) {
        return new OnComputedRDDCallback<>(recordReader, dp);
    }

    /**
     * Returns the partitions on which this RDD depends on.
     * <p/>
     * Uses the underlying CqlPagingInputFormat in order to retrieve the splits.
     * <p/>
     * The number of splits, and hence the number of partitions equals to the
     * number of tokens configured in cassandra.yaml + 1.
     */
    @Override
    public Partition[] getPartitions() {

        List<Range> underlyingInputSplits = RangeUtils.getSplits(config.value());

        Partition[] partitions = new KinaPartition[underlyingInputSplits.size()];

        int i = 0;

        for (Range split : underlyingInputSplits) {
            partitions[i] = new KinaPartition(id(), i, split);

            log().debug("Detected partition: " + partitions[i]);
            ++i;
        }

        return partitions;
    }

    /**
     * Returns a list of hosts on which the given split resides.
     */
    @Override
    public Seq<String> getPreferredLocations(Partition split) {
        KinaPartition p = (KinaPartition) split;

        List<String> locations = p.splitWrapper().getReplicas();
        log().debug("getPreferredLocations: " + p);

        return asScalaBuffer(locations);
    }

    /**
     * Instantiates a new kina record reader object associated to the provided partition.
     *
     * @param ctx the spark task context.
     * @param dp  a spark kina partition
     * @return the kina record reader associated to the provided partition.
     */
    private CqlRecordReader initRecordReader(TaskContext ctx, final KinaPartition dp) {
        CqlRecordReader recordReader = new CqlRecordReader(config.value(), dp.splitWrapper());
        ctx.addOnCompleteCallback(getComputeCallback(recordReader, dp));
        return recordReader;

    }
}
