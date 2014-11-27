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

import java.lang.reflect.*;
import java.lang.reflect.Field;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import kina.annotations.*;
import kina.entity.Cell;
import kina.entity.Cells;
import kina.entity.KinaType;
import kina.entity.MongoCell;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;

/**
 * Several utilities to work used in the Spark <=> MongoDB integration.
 */
public final class UtilMongoDB {

    public static final String MONGO_DEFAULT_ID = "_id";

    /**
     * Private default constructor.
     */
    private UtilMongoDB() {
        throw new UnsupportedOperationException();
    }

    /**
     * converts from BsonObject to an entity class with kina's anotations
     *
     * @param classEntity the entity name.
     * @param bsonObject  the instance of the BSONObjet to convert.
     * @param <T>         return type.
     * @return the provided bsonObject converted to an instance of T.
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public static <T> T getObjectFromBson(Class<T> classEntity, BSONObject bsonObject) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T t = classEntity.newInstance();

        Field[] fields = filterKinaFields(classEntity);

        Object insert;

        for (Field field : fields) {
            Method method = Utils.findSetter(field.getName(), classEntity, field.getType());

            Class<?> classField = field.getType();

            Object currentBson = bsonObject.get(kinaFieldName(field));
            if (currentBson != null) {

                if (Iterable.class.isAssignableFrom(classField)) {
                    Type type = field.getGenericType();

                    insert = subDocumentListCase(type, (List) bsonObject.get(kinaFieldName(field)));


                } else if (KinaType.class.isAssignableFrom(classField)) {
                    insert = getObjectFromBson(classField, (BSONObject) bsonObject.get(kinaFieldName
				                    (field)));
                } else {
                    insert = currentBson;
                }

                method.invoke(t, insert);
            }
        }

        return t;
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
			if (f.isAnnotationPresent(kina.annotations.Field.class) ||
							f.isAnnotationPresent(Key.class)) {

				filtered.add(f);
			}
		}
		return filtered.toArray(new java.lang.reflect.Field[filtered.size()]);
	}


    private static <T> Object subDocumentListCase(Type type, List<T> bsonOject) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        ParameterizedType listType = (ParameterizedType) type;

        Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];

        List list = new ArrayList();
        for (T t : bsonOject) {

            if (BSONObject.class.isAssignableFrom(t.getClass())){
                list.add(getObjectFromBson(listClass, (BSONObject) t));
            } else {
                list.add(t);
            }
        }


        return list;
    }


    /**
     * converts from an entity class with kina's anotations to BsonObject.
     *
     * @param t   an instance of an object of type T to convert to BSONObject.
     * @param <T> the type of the object to convert.
     * @return the provided object converted to BSONObject.
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public static <T extends KinaType> BSONObject getBsonFromObject(T t) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Field[] fields = filterKinaFields(t.getClass());

        BSONObject bson = new BasicBSONObject();

        for (Field field : fields) {
            Method method = Utils.findGetter(field.getName(), t.getClass());
            Object object = method.invoke(t);
            if (object != null) {
                if (Collection.class.isAssignableFrom(field.getType())) {
                    Collection c = (Collection) object;
                    Iterator iterator = c.iterator();
                    List<BSONObject> innerBsonList = new ArrayList<>();

                    while (iterator.hasNext()) {
                        innerBsonList.add(getBsonFromObject((KinaType) iterator.next()));
                    }
                    bson.put(kinaFieldName(field), innerBsonList);
                } else if (KinaType.class.isAssignableFrom(field.getType())) {
                    bson.put(kinaFieldName(field), getBsonFromObject((KinaType) object));
                } else {
                    bson.put(kinaFieldName(field), object);
                }
            }
        }

        return bson;
    }

    /**
     * returns the id value annotated with @Field(fieldName = "_id")
     *
     * @param t   an instance of an object of type T to convert to BSONObject.
     * @param <T> the type of the object to convert.
     * @return the provided object converted to Object.
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public static <T extends KinaType> Object getId(T t) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Field[] fields = filterKinaFields(t.getClass());

        for (Field field : fields) {
            if (MONGO_DEFAULT_ID.equals(kinaFieldName(field))) {
                return Utils.findGetter(field.getName(), t.getClass()).invoke(t);
            }

        }

        return null;
    }


    /**
     * converts from BsonObject to cell class with kina's anotations
     *
     * @param bsonObject
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public static Cells getCellFromBson(BSONObject bsonObject) throws IllegalAccessException, InstantiationException, InvocationTargetException {

        Cells cells = new Cells();

        Map<String, Object> map = bsonObject.toMap();

        Set<Map.Entry<String, Object>> entryBson = map.entrySet();

        for (Map.Entry<String, Object> entry : entryBson) {

            if (entry.getValue() == null){
                cells.add(MongoCell.create(entry.getKey(), null));
            }else if (BasicBSONList.class.isAssignableFrom(entry.getValue().getClass())) {
                BasicBSONList basicBSONList = (BasicBSONList)entry.getValue();

                List<Cells> innerCell = new ArrayList<>();

                Set<String> keySet = basicBSONList.keySet();

                for (String key : keySet) {
                    Object obj = basicBSONList.get(key);
                    BSONObject innerBson = null;
                    if (BSONObject.class.isAssignableFrom(obj.getClass())){
                        innerBson = (BSONObject)obj;
                    } else {
                        innerBson = new BasicBSONObject(key,obj);
                    }
                    innerCell.add(getCellFromBson(innerBson));
                }

                cells.add(MongoCell.create(entry.getKey(), innerCell));
            } else if (List.class.isAssignableFrom(entry.getValue().getClass())) {
                List<Cells> innerCell = new ArrayList<>();
                for (BSONObject innerBson : (List<BSONObject>) entry.getValue()) {
                    innerCell.add(getCellFromBson(innerBson));
                }
                cells.add(MongoCell.create(entry.getKey(), innerCell));
            } else if (BSONObject.class.isAssignableFrom(entry.getValue().getClass())) {
                Cells innerCells = getCellFromBson((BSONObject) entry.getValue());
                cells.add(MongoCell.create(entry.getKey(), innerCells));
            } else {
                cells.add(MongoCell.create(entry.getKey(), entry.getValue()));
            }

        }
        return cells;
    }


    /**
     * converts from and entity class with kina's anotations to BsonObject
     *
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public static BSONObject getBsonFromCell(Cells cells) throws IllegalAccessException, InstantiationException, InvocationTargetException {

        BSONObject bson = new BasicBSONObject();
        for (Cell cell : cells) {
            if (List.class.isAssignableFrom(cell.getCellValue().getClass())) {
                Collection c = (Collection) cell.getCellValue();
                Iterator iterator = c.iterator();
                List<BSONObject> innerBsonList = new ArrayList<>();

                while (iterator.hasNext()) {
                    innerBsonList.add(getBsonFromCell((Cells) iterator.next()));
                }
                bson.put(cell.getCellName(), innerBsonList);
            } else if (Cells.class.isAssignableFrom(cell.getCellValue().getClass())) {
                bson.put(cell.getCellName(), getBsonFromCell((Cells) cell.getCellValue()));
            } else {
                bson.put(cell.getCellName(), cell.getCellValue());
            }


        }


        return bson;
    }

	/**
	 * Returns the field name as known by the datastore. If the provided field object Field annotation
	 * specifies the fieldName property, the value of this property will be returned, otherwise the java field name
	 * will be returned.
	 *
	 * @param field the Field object associated to the property for which we want to resolve the name.
	 * @return the field name.
	 */
	public static String kinaFieldName(java.lang.reflect.Field field) {

		kina.annotations.Field fieldAnnotation = field.getAnnotation(kina.annotations.Field.class);
		Key genericKeyAnnotation = field.getAnnotation(Key.class);
		String fieldName = null;

		if (fieldAnnotation != null){
			fieldName = fieldAnnotation.fieldName();
		} else if (genericKeyAnnotation != null){
			fieldName = genericKeyAnnotation.fieldName();
		}

		if (StringUtils.isNotEmpty(fieldName)) {
			return fieldName;
		} else {
			return field.getName();
		}
	}
}
