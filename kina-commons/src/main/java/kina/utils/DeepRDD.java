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

package kina.utils;

/**
 * Created by rcrespo on 10/07/14.
 */
public enum DeepRDD {


    MONGODB_JAVA("kina.rdd.mongodb.MongoJavaRDD"),
    MONGODB_ENTITY("kina.rdd.mongodb.MongoEntityRDD"),
    MONGODB_CELL("kina.rdd.mongodb.MongoCellRDD"),
    CASSANDRA_JAVA("kina.rdd.CassandraJavaRDD"),
    CASSANDRA_ENTITY("kina.rdd.CassandraEntityRDD"),
    CASSANDRA_CELL("kina.rdd.CassandraCellRDD");

    private String className;

    private DeepRDD(String className) {
        this.className = className;
    }

    public String getClassName(){
        return className;
    }



}
