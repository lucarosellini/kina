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
import scala.beans.BeanProperty
import org.apache.cassandra.db.marshal.Int32Type

/**
 * Author: Luca Rosellini
 * Date..: 21-mar-2014
 */

@Entity class ScalaOutputEntity extends KinaType {

  @BeanProperty
  @Field(isPartOfPartitionKey = true)
  var key: String = _

  @BeanProperty
  @Field(validationClass = classOf[Int32Type])
  var value: Integer = _
}

