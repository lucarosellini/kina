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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.CharacterCodingException;

import kina.config.CassandraKinaConfig;
import kina.context.AbstractKinaContextAwareTest;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.spark.Partition;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import scala.collection.Seq;

import static kina.utils.Utils.quote;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Abstract class defining the common test structure that all concrete subclasses should respect.
 *
 * @param <W>
 */
public abstract class CassandraRDDTest<W> extends AbstractKinaContextAwareTest {
    private Logger logger = Logger.getLogger(getClass());

    protected CassandraRDD<W> rdd;
    private CassandraKinaConfig<W> rddConfig;
    private CassandraKinaConfig<W> writeConfig;

    protected int testBisectFactor = 8;

	protected static final int DEFAULT_PAGE_SIZE = 100;

    protected abstract void checkComputedData(W[] entities);

    protected abstract void checkSimpleTestData();

    protected CassandraRDD<W> getRDD() {
        return this.rdd;
    }

    protected CassandraKinaConfig<W> getReadConfig() {
        return rddConfig;
    }

    protected CassandraKinaConfig<W> getWriteConfig() {
        return writeConfig;
    }

    protected abstract CassandraRDD<W> initRDD();

    protected abstract CassandraKinaConfig<W> initReadConfig();

    protected abstract CassandraKinaConfig<W> initWriteConfig();

    @BeforeClass
    protected void initServerAndRDD() throws IOException, URISyntaxException, ConfigurationException,
            InterruptedException {

        logger.info("<<<<< DROPPING KEYSPACES >>>>>>");
        executeCustomCQL(
                "DROP KEYSPACE IF EXISTS " + quote(KEYSPACE_NAME),
                "DROP KEYSPACE IF EXISTS " + quote(OUTPUT_KEYSPACE_NAME));

        cassandraServer.initKeySpace();

        initConfigsAndRdd();
    }

    protected void initConfigsAndRdd(){
        rddConfig = initReadConfig();
        writeConfig = initWriteConfig();
        rdd = initRDD();
    }


    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = "testGetPreferredLocations")
    public void testCompute() throws CharacterCodingException {

        logger.info("testCompute()");
        Object obj = getRDD().collect();

        assertNotNull(obj);

        W[] entities = (W[]) obj;

        checkComputedData(entities);
    }

    @Test(dependsOnMethods = "testRDDInstantiation")
    public void testGetPartitions() {
        logger.info("testGetPartitions()");
        Partition[] partitions = getRDD().partitions();

        assertNotNull(partitions);
        assertEquals(partitions.length, getReadConfig().getBisectFactor() * (8 + 1));
    }

    @Test(dependsOnMethods = "testGetPartitions")
    public void testGetPreferredLocations() {
        logger.info("testGetPreferredLocations()");
        Partition[] partitions = getRDD().partitions();

        Seq<String> locations = getRDD().getPreferredLocations(partitions[0]);

        assertNotNull(locations);
    }

    @Test
    public void testRDDInstantiation() {
        logger.info("testRDDInstantiation()");
        assertNotNull(getRDD());


    }

    @Test(dependsOnMethods = "testSimpleSaveToCassandra")
    public abstract void testSaveToCassandra();

    @Test(dependsOnMethods = "testCompute")
    public abstract void testSimpleSaveToCassandra();

    protected static void truncateCf(String keyspace, String cf) {
        executeCustomCQL("TRUNCATE  " + quote(keyspace) + "." + cf);
    }

    @Test(dependsOnMethods = "testSaveToCassandra")
    public abstract void testCql3SaveToCassandra();
}
