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

package kina.functions;

import kina.entity.Cells;
import kina.entity.KinaType;
import scala.Tuple2;

import static kina.rdd.CassandraRDDUtils.kinaType2tuple;

/**
 * Function that converts an KinaType to tuple of two Cells.<br/>
 * The first Cells element contains the list of Cell elements that represent the key (partition + cluster key). <br/>
 * The second Cells element contains all the other columns.
 */
public class KinaType2TupleFunction<T extends KinaType> extends AbstractSerializableFunction<T, Tuple2<Cells, Cells>> {

    private static final long serialVersionUID = 3701384431140105598L;

    /**
     * {@inheritDoc}
     */
    @Override
    public Tuple2<Cells, Cells> apply(T e) {
        return kinaType2tuple(e);
    }
}
