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

package kina.functions;

import java.io.Serializable;

import scala.runtime.AbstractFunction1;

/**
 * Abstract base type to create serializable functions of tipe 1.
 *
 * @author Luca Rosellini <luca.rosellini@gmail.com>
 */
public abstract class AbstractSerializableFunction<T, U> extends AbstractFunction1<T, U> implements Serializable {

    private static final long serialVersionUID = 3528398361663219123L;
}
