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

import java.lang.reflect.Field;

import kina.testentity.CommonsTestEntity;
import org.testng.annotations.Test;

import static kina.utils.Utils.getAllFields;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by luca on 12/08/14.
 */
public class CassandraUtilsTest {
	@Test
	public void testFilterKinaFields() {
		Field[] fields = getAllFields(CommonsTestEntity.class);

		assertTrue(fields.length > 6);

		fields = CassandraUtils.filterKinaFields(CommonsTestEntity.class);

		assertEquals(fields.length, 9);
	}

	@Test
	public void testFilterKeyFields() {
		Pair<Field[], Field[]> keyFields =
						CassandraUtils.filterKeyFields(CommonsTestEntity.class);

		assertNotNull(keyFields);
		assertNotNull(keyFields.left);
		assertNotNull(keyFields.right);
		assertTrue(keyFields.left.length == 1);
		assertTrue(keyFields.right.length == 8);

		assertTrue(keyFields.left[0].getName().equals("id"));
	}
}
