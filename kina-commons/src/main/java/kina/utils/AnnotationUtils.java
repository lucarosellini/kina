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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.beanutils.PropertyUtils;

import kina.entity.KinaType;
import kina.exceptions.IOException;
import org.apache.cassandra.db.marshal.*;

/**
 * Common utility methods to manipulate beans and fields annotated with @Entity and @Field.
 */
public final class AnnotationUtils {
    /**
     * Static map of associations between Class objects and the equivalent
     * Cassandra marshaller.
     */
    public static final Map<Class, AbstractType<?>> MAP_JAVA_TYPE_TO_ABSTRACT_TYPE =
            ImmutableMap.<Class, AbstractType<?>>builder()
                    .put(String.class, UTF8Type.instance)
                    .put(Integer.class, Int32Type.instance)
                    .put(Boolean.class, BooleanType.instance)
                    .put(Date.class, TimestampType.instance)
                    .put(BigDecimal.class, DecimalType.instance)
                    .put(Long.class, LongType.instance)
                    .put(Double.class, DoubleType.instance)
                    .put(Float.class, FloatType.instance)
                    .put(InetAddress.class, InetAddressType.instance)
                    .put(Inet4Address.class, InetAddressType.instance)
                    .put(Inet6Address.class, InetAddressType.instance)
                    .put(BigInteger.class, IntegerType.instance)
                    .put(UUID.class, UUIDType.instance)
                    .put(ByteBuffer.class, BytesType.instance)
                    .build();

    /**
     * Static map of associations between a cassandra marshaller fully qualified class name and the corresponding
     * Java class.
     */
    public static final Map<String, Class> MAP_ABSTRACT_TYPE_CLASSNAME_TO_JAVA_TYPE =
            ImmutableMap.<String, Class>builder()
                    .put(UTF8Type.class.getCanonicalName(), String.class)
                    .put(Int32Type.class.getCanonicalName(), Integer.class)
                    .put(BooleanType.class.getCanonicalName(), Boolean.class)
                    .put(TimestampType.class.getCanonicalName(), Date.class)
                    .put(DateType.class.getCanonicalName(), Date.class)
                    .put(DecimalType.class.getCanonicalName(), BigDecimal.class)
                    .put(LongType.class.getCanonicalName(), Long.class)
                    .put(DoubleType.class.getCanonicalName(), Double.class)
                    .put(FloatType.class.getCanonicalName(), Float.class)
                    .put(InetAddressType.class.getCanonicalName(), InetAddress.class)
                    .put(IntegerType.class.getCanonicalName(), BigInteger.class)
                    .put(UUIDType.class.getCanonicalName(), UUID.class)
                    .put(TimeUUIDType.class.getCanonicalName(), UUID.class)
                    .put(SetType.class.getCanonicalName(), Set.class)
                    .put(ListType.class.getCanonicalName(), List.class)
                    .put(MapType.class.getCanonicalName(), Map.class)
                    .put(BytesType.class.getCanonicalName(), ByteBuffer.class)
                    .build();

    /**
     * Static map of associations between cassandra marshaller Class objects and their instance.
     */
    public static final Map<Class<?>, AbstractType<?>> MAP_ABSTRACT_TYPE_CLASS_TO_ABSTRACT_TYPE =
            ImmutableMap.<Class<?>, AbstractType<?>>builder()
                    .put(UTF8Type.class, UTF8Type.instance)
                    .put(Int32Type.class, Int32Type.instance)
                    .put(BooleanType.class, BooleanType.instance)
                    .put(TimestampType.class, TimestampType.instance)
                    .put(DateType.class, DateType.instance)
                    .put(DecimalType.class, DecimalType.instance)
                    .put(LongType.class, LongType.instance)
                    .put(DoubleType.class, DoubleType.instance)
                    .put(FloatType.class, FloatType.instance)
                    .put(InetAddressType.class, InetAddressType.instance)
                    .put(IntegerType.class, IntegerType.instance)
                    .put(UUIDType.class, UUIDType.instance)
                    .put(TimeUUIDType.class, TimeUUIDType.instance)
                    .put(BytesType.class, BytesType.instance)
                    .build();

    /**
     * Returns the value of the fields <i>kinaField</i> in the instance <i>entity</i> of type T.
     *
     * @param entity    the entity to process.
     * @param kinaField the Field to process belonging to <i>entity</i>
     * @return the property value.
     */
    public static Serializable getBeanFieldValue(KinaType entity, java.lang.reflect.Field kinaField) {
        try {
            return (Serializable) PropertyUtils.getProperty(entity, kinaField.getName());

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e1) {
            throw new IOException(e1);
        }

    }

    /**
     * Returns the list of generic types associated to the provided field (if any).
     *
     * @param field the field instance to process.
     * @return the list of generic types associated to the provided field (if any).
     */
    public static Class[] getGenericTypes(java.lang.reflect.Field field) {
        try {
            ParameterizedType type = (ParameterizedType) field.getGenericType();
            Type[] types = type.getActualTypeArguments();

            Class<?>[] res = new Class<?>[types.length];

            for (int i = 0; i < types.length; i++) {
                res[i] = (Class<?>) types[i];
            }

            return res;

        } catch (ClassCastException e) {
            return new Class<?>[]{(Class<?>) field.getGenericType()};
        }
    }

    /**
     * private constructor.
     */
    private AnnotationUtils() {
    }
}
