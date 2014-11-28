package kina.agent;

import kina.agent.transformer.MetadataTransformer;
import org.apache.log4j.Logger;

import java.lang.instrument.Instrumentation;

/**
 * JVM Agent used to inject new bytecode to com.datastax.driver.core.Metadata
 * kina-cassandra, needs this to dynamically get the list of replicas for a given node.
 *
 * Attach to the JVM using -javaagent:/path/to/kina-agent.jar
 *
 * Created by luca on 28/11/14.
 */
public class KinaAgent {
    private static final Logger LOG = Logger.getLogger(KinaAgent.class);

    /**
     * Entry point hook
     *
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void premain(String args, Instrumentation inst) throws Exception {
        LOG.info("KinaAgent loaded");
        inst.addTransformer(new MetadataTransformer());

        LOG.info("MetadataTransformer registered");

    }
}
