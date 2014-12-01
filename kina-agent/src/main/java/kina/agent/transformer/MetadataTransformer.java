package kina.agent.transformer;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import javassist.*;
import org.apache.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;

/**
 * Class file transformer that injects 'getTokenReplicas' method to com.datastax.driver.core.Metadata
 *
 * Created by luca on 28/11/14.
 */
public class MetadataTransformer implements  ClassFileTransformer {
    private static final Logger LOG = Logger.getLogger(MetadataTransformer.class);

    private static final String COM_DATASTAX_DRIVER_CORE_METADATA_PATH = "com/datastax/driver/core/Metadata";

    private static final String COM_DATASTAX_DRIVER_CORE_METADATA$TOKEN_MAP_PATH =
            COM_DATASTAX_DRIVER_CORE_METADATA_PATH + "$TokenMap";

    private static final String COM_DATASTAX_DRIVER_CORE_METADATA_CLASS =
            COM_DATASTAX_DRIVER_CORE_METADATA_PATH.replaceAll("/",".");

    private static final String COM_DATASTAX_DRIVER_CORE_METADATA$TOKEN_MAP_CLASS =
            COM_DATASTAX_DRIVER_CORE_METADATA$TOKEN_MAP_PATH.replaceAll("/",".");

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {


        if (COM_DATASTAX_DRIVER_CORE_METADATA_PATH.equals(className)){
            LOG.debug(className + " is about to be instrumented");
            return instrumentMetadata();
        }

        if (COM_DATASTAX_DRIVER_CORE_METADATA$TOKEN_MAP_PATH.equals(className)){
            LOG.debug(className + " is about to be instrumented");
            return instrumentTokenMap();
        }

        return null;
    }

    /**
     * Modify the visibility of some fields/methods of the TokenMap class
     */
    private byte[] instrumentTokenMap(){
        try {
            CtClass clazz = ClassPool.getDefault().get(COM_DATASTAX_DRIVER_CORE_METADATA$TOKEN_MAP_CLASS);
            clazz.defrost();
            clazz.setModifiers(clazz.getModifiers() & Modifier.PUBLIC);
            clazz.getField("factory").setModifiers(clazz.getModifiers() & Modifier.PUBLIC);
            clazz.getDeclaredMethod("getReplicas").setModifiers(clazz.getModifiers() & Modifier.PUBLIC);

            byte[] bytecode = clazz.toBytecode();

            clazz.freeze();
            return bytecode;
        } catch (Exception e){
            LOG.error("",e);
            throw new kina.exceptions.InstantiationException(e);
        }
    }

    /**
     * Performs the actual code injection to the datastax metadata class
     */
    private byte[] instrumentMetadata() {
        try {
            URL url = Resources.getResource("getTokenReplicas.javassist");
            String code = Resources.toString(url, Charsets.UTF_8);

            CtClass dtstxMetadata = ClassPool.getDefault().get(COM_DATASTAX_DRIVER_CORE_METADATA_CLASS);

            CtClass[] declaredClasses = dtstxMetadata.getDeclaredClasses();
            for (CtClass clazz : declaredClasses) {
                if (clazz.getName().equals(COM_DATASTAX_DRIVER_CORE_METADATA$TOKEN_MAP_CLASS)){
                    instrumentTokenMap();
                }
            }

            try {
                dtstxMetadata.getDeclaredMethod("getTokenReplicas");
            } catch (NotFoundException e) {
                // inject the new method

                CtMethod getTokenReplicas = CtNewMethod.make(code, dtstxMetadata);
                dtstxMetadata.addMethod(getTokenReplicas);

                return dtstxMetadata.toBytecode();
            }
        } catch (Exception e) {
            LOG.error("",e);
            throw new kina.exceptions.InstantiationException(e);
        }

        return null;
    }
}
