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

import java.lang.annotation.AnnotationTypeMismatchException;

import kina.context.AbstractKinaContextAwareTest;
import kina.embedded.CassandraServer;
import kina.entity.Cells;
import kina.entity.KinaType;
import kina.exceptions.IllegalAccessException;
import kina.exceptions.NoSuchFieldException;
import kina.testentity.TestEntity;
import kina.testentity.WronglyMappedTestEntity;
import kina.utils.Constants;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;

@Test(suiteName = "cassandraRddTests", groups = {"GenericConfigFactoryTest"},
        dependsOnGroups = {"CassandraJavaRDDTest"})
public class GenericConfigFactoryTest extends AbstractKinaContextAwareTest {
    class NotAnnotatedTestEntity implements KinaType {
        private static final long serialVersionUID = -2603126590709315326L;
    }

    private Logger log = Logger.getLogger(getClass());

    @Test
    public void testWriteConfigValidation() {
        CassandraKinaConfig<TestEntity> djc = CassandraConfigFactory.createWriteConfig(TestEntity.class);

        djc.rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT).cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
                .columnFamily("inexistent_test_page").keyspace(KEYSPACE_NAME);

        try {
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
            djc.createTableOnWrite(true);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        djc.initialize();
    }

    @Test
    public void testInputColumnsExist() {
        CassandraKinaConfig<Cells> djc = CassandraConfigFactory.create();

        djc.rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT).cqlPort(CassandraServer.CASSANDRA_CQL_PORT)
                .columnFamily(COLUMN_FAMILY).keyspace(KEYSPACE_NAME).inputColumns("not_existent_col1",
                "not_existent_col2");

        try {
            djc.initialize();
            fail();
        } catch (NoSuchFieldException iae) {
            // OK
            log.info("Correctly catched NoSuchFieldException: " + iae.getLocalizedMessage());
            djc.inputColumns("domain_name", "response_time", "url");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        djc.initialize();
    }

    @Test
    public void testValidation() {

        CassandraKinaConfig<TestEntity> djc = CassandraConfigFactory.create(TestEntity.class);

        djc.host(null).rpcPort(null).pageSize(0).bisectFactor(3);

        try {
            djc.getKeyspace();
        } catch (IllegalAccessException e) {
            log.info("Correctly catched IllegalAccessException: " + e.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());

        }

        try {
            djc.getHost();
        } catch (IllegalAccessException e) {
            log.info("Correctly catched IllegalAccessException: " + e.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());

        }

        try {
            djc.getRpcPort();
        } catch (IllegalAccessException e) {
            log.info("Correctly catched IllegalAccessException: " + e.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());

        }

        try {
            djc.getUsername();
        } catch (IllegalAccessException e) {
            log.info("Correctly catched IllegalAccessException: " + e.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());

        }

        try {
            djc.getPassword();
        } catch (IllegalAccessException e) {
            log.info("Correctly catched IllegalAccessException: " + e.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());

        }

        try {
            djc.getColumnFamily();
        } catch (IllegalAccessException e) {
            log.info("Correctly catched IllegalAccessException: " + e.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());

        }

        try {
            djc.getPageSize();
        } catch (IllegalAccessException e) {
            log.info("Correctly catched IllegalAccessException: " + e.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            djc.getPageSize();
        } catch (IllegalAccessException e) {
            log.info("Correctly catched IllegalAccessException: " + e.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());

        }


        try {
            djc.getEntityClass();
        } catch (IllegalAccessException e) {
            log.info("Correctly catched IllegalAccessException: " + e.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        djc.host("localhost");

        try {
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        djc.rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
                .cqlPort(CassandraServer.CASSANDRA_CQL_PORT);

        try {
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        djc.keyspace(KEYSPACE_NAME);

        try {
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        djc.columnFamily("test_page");

        try {
            djc.readConsistencyLevel("not valid CL");
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            djc.pageSize(0);
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            djc.pageSize(1 + Constants.DEFAULT_MAX_PAGE_SIZE);
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());

            djc.pageSize(10);
        } catch (Exception e) {
            fail(e.getMessage());
        }


        djc.readConsistencyLevel(ConsistencyLevel.LOCAL_ONE.name());

        try {
            djc.writeConsistencyLevel("not valid CL");
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        djc.writeConsistencyLevel(ConsistencyLevel.LOCAL_ONE.name());

        try {
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
            djc.columnFamily(COLUMN_FAMILY);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            djc.initialize();
            fail();
        } catch (IllegalArgumentException iae) {
            // OK
            log.info("Correctly catched IllegalArgumentException: " + iae.getLocalizedMessage());
            djc.bisectFactor(4);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        djc.initialize();
    }

    @Test
    public void testWronglyMappedField() {

        CassandraKinaConfig<WronglyMappedTestEntity> djc = CassandraConfigFactory.create(WronglyMappedTestEntity.class).host
                (Constants.DEFAULT_CASSANDRA_HOST).rpcPort(CassandraServer.CASSANDRA_THRIFT_PORT)
                .cqlPort(CassandraServer.CASSANDRA_CQL_PORT).keyspace(KEYSPACE_NAME).columnFamily(COLUMN_FAMILY);

        try {
            djc.initialize();

            fail();
        } catch (kina.exceptions.NoSuchFieldException e) {
            // ok
            log.info("Correctly catched NoSuchFieldException: " + e.getLocalizedMessage());
        }
    }

    @Test
    public void testValidationNotAnnotadedTestEntity() {
        CassandraKinaConfig<NotAnnotatedTestEntity> djc = CassandraConfigFactory.create(NotAnnotatedTestEntity.class)
                .keyspace("a").columnFamily("cf");
        try {
            djc.initialize();

            fail();
        } catch (AnnotationTypeMismatchException iae) {
            log.info("Correctly catched AnnotationTypeMismatchException: " + iae.getLocalizedMessage());
        } catch (Exception e) {
            log.error("Error", e);
            fail(e.getMessage());
        }
    }
}
