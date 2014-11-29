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

import org.apache.spark.api.java.JavaRDD;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

/**
 * Commodity RDD implementation that should be used as a
 * Java Wrapper for {@link CassandraEntityRDD}.
 *
 * @param <W>
 * @author Luca Rosellini <luca@strat.io>
 */
public final class CassandraJavaRDD<W> extends JavaRDD<W> {
    private static final long serialVersionUID = -3208994171892747470L;

    /**
     * Default constructor. Constructs a new Java-friendly Cassandra RDD
     *
     * @param rdd
     */
    public CassandraJavaRDD(CassandraRDD<W> rdd) {
        super(rdd, ClassTag$.MODULE$.<W>apply(rdd.config.value().getEntityClass()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassTag<W> classTag() {
        return ClassTag$.MODULE$.<W>apply(((CassandraRDD<W>) this.rdd()).config.value().getEntityClass());
    }
}
