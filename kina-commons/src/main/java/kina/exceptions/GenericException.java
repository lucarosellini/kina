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

package kina.exceptions;

/**
 * Generic Kina exception.
 *
 * @author Luca Rosellini <luca.rosellini@gmail.com>
 */
public class GenericException extends RuntimeException {

    private static final long serialVersionUID = -1912500294194290036L;

    /**
     * Default constructor.
     */
    public GenericException() {
        super();
    }

    /**
     * Public constructor.
     */
    public GenericException(String message) {
        super(message);
    }

    /**
     * Public constructor.
     */
    public GenericException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Public constructor.
     */
    public GenericException(Throwable cause) {
        super(cause);
    }

}
