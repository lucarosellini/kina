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

package kina.testentity

import kina.annotations.{Field, Entity}
import kina.entity.KinaType
import org.apache.cassandra.db.marshal.{Int32Type, LongType}

import scala.beans.BeanProperty

@Entity class KinaScalaPageEntity extends KinaType {

  @BeanProperty
  @Field(isPartOfPartitionKey = true)
  var id: String = null

  @BeanProperty
  @Field(fieldName = "domain_name")
  var domain: String = null

  @BeanProperty
  @Field
  var url: String = null

  @BeanProperty
  @Field(validationClass = classOf[Int32Type], fieldName = "response_time")
  var responseTime: Integer = _

  @BeanProperty
  @Field(fieldName = "response_code", validationClass = classOf[Int32Type])
  var responseCode: Integer = _

  @BeanProperty
  @Field(validationClass = classOf[LongType], fieldName = "download_time")
  var downloadTime: java.lang.Long = _
}
