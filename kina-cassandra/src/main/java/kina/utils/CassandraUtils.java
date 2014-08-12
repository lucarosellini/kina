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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import kina.annotations.ClusterKey;
import kina.annotations.Field;
import kina.annotations.Key;
import kina.annotations.PartitionKey;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.UTF8Type;

/**
 * Created by luca on 12/08/14.
 */
public class CassandraUtils {
	/**
	 * Returns the field name as known by the datastore. If the provided field object Field annotation
	 * specifies the fieldName property, the value of this property will be returned, otherwise the java field name
	 * will be returned.
	 *
	 * @param field the Field object associated to the property for which we want to resolve the name.
	 * @return the field name.
	 */
	public static String kinaFieldName(java.lang.reflect.Field field) {

		Field fieldAnnotation = field.getAnnotation(Field.class);
		Key genericKeyAnnotation = field.getAnnotation(Key.class);
		PartitionKey partitionKeyAnnotation = field.getAnnotation(PartitionKey.class);
		ClusterKey clusterKeyAnnotation = field.getAnnotation(ClusterKey.class);

		String fieldName = null;

		if (fieldAnnotation != null){
			fieldName = fieldAnnotation.fieldName();
		} else if (genericKeyAnnotation != null){
			fieldName = genericKeyAnnotation.fieldName();
		} else if (partitionKeyAnnotation != null){
			fieldName = partitionKeyAnnotation.fieldName();
		} else if (clusterKeyAnnotation != null){
			fieldName = clusterKeyAnnotation.fieldName();
		}

		if (StringUtils.isNotEmpty(fieldName)) {
			return fieldName;
		} else {
			return field.getName();
		}
	}

	/**
	 * Utility method that filters out all the fields _not_ annotated
	 * with the {@link kina.annotations.Field} annotation.
	 *
	 * @param clazz the Class object for which we want to resolve kina fields.
	 * @return an array of kina Field(s).
	 */
	public static java.lang.reflect.Field[] filterKinaFields(Class clazz) {
		java.lang.reflect.Field[] fields = Utils.getAllFields(clazz);
		List<java.lang.reflect.Field> filtered = new ArrayList<>();
		for (java.lang.reflect.Field f : fields) {
			if (f.isAnnotationPresent(Field.class) ||
				f.isAnnotationPresent(Key.class) ||
				f.isAnnotationPresent(PartitionKey.class) ||
				f.isAnnotationPresent(ClusterKey.class)) {

				filtered.add(f);
			}
		}
		return filtered.toArray(new java.lang.reflect.Field[filtered.size()]);
	}

	public static Class<? extends AbstractType> validationClass(java.lang.reflect.Field field){
		Field fieldAnnotation = field.getAnnotation(Field.class);
		Key genericKeyAnnotation = field.getAnnotation(Key.class);
		PartitionKey partitionKeyAnnotation = field.getAnnotation(PartitionKey.class);
		ClusterKey clusterKeyAnnotation = field.getAnnotation(ClusterKey.class);

		Class<? extends AbstractType> res = null;

		if (fieldAnnotation != null){
			res = fieldAnnotation.validationClass();
		} else if (genericKeyAnnotation != null){
			res = UTF8Type.class;
		} else if (partitionKeyAnnotation != null){
			res = partitionKeyAnnotation.validationClass();
		} else if (clusterKeyAnnotation != null){
			res = clusterKeyAnnotation.validationClass();
		}

		return res;
	}

	/**
	 * Returns true is given field is part of the table key.
	 *
	 * @param field the Field object we want to process.
	 * @return true if the field is part of the cluster key or the partition key, false otherwise.
	 */
	public static boolean isKey(java.lang.reflect.Field field){

		Key genericKeyAnnotation = field.getAnnotation(Key.class);
		PartitionKey partitionKeyAnnotation = field.getAnnotation(PartitionKey.class);
		ClusterKey clusterKeyAnnotation = field.getAnnotation(ClusterKey.class);

		return genericKeyAnnotation != null || partitionKeyAnnotation !=null || clusterKeyAnnotation != null;
	}

	/**
	 * Returns true is given field is part of the table key.
	 *
	 * @param field the Field object we want to process.
	 * @return true if the field is part of the cluster key or the partition key, false otherwise.
	 */
	public static boolean isClusterKey(java.lang.reflect.Field field){

		ClusterKey annotation = field.getAnnotation(ClusterKey.class);

		return annotation != null;
	}

	/**
	 * Returns true is given field is part of the table key.
	 *
	 * @param field the Field object we want to process.
	 * @return true if the field is part of the cluster key or the partition key, false otherwise.
	 */
	public static boolean isPartitionKey(java.lang.reflect.Field field){

		Key genericKeyAnnotation = field.getAnnotation(Key.class);
		PartitionKey partitionKeyAnnotation = field.getAnnotation(PartitionKey.class);

		return genericKeyAnnotation != null || partitionKeyAnnotation !=null;
	}

	/**
	 * Return a pair of Field[] whose left element is
	 * the array of keys fields.
	 * The right element contains the array of all other non-key fields.
	 *
	 * @param clazz the Class object
	 * @return a pair object whose first element contains key fields, and whose second element contains all other columns.
	 */
	public static Pair<java.lang.reflect.Field[], java.lang.reflect.Field[]> filterKeyFields(Class clazz) {
		java.lang.reflect.Field[] filtered = filterKinaFields(clazz);
		List<java.lang.reflect.Field> keys = new ArrayList<>();
		List<java.lang.reflect.Field> others = new ArrayList<>();

		for (java.lang.reflect.Field field : filtered) {
			if (isKey(field)) {
				keys.add(field);
			} else {
				others.add(field);
			}
		}

		return Pair.create(keys.toArray(new java.lang.reflect.Field[keys.size()]), others.toArray(new java.lang.reflect.Field[others.size()]));
	}
}
