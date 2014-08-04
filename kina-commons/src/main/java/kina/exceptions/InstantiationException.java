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
 * Runtime exception representing an instantiation exception.
 */
public class InstantiationException extends RuntimeException {


    private static final long serialVersionUID = -8281399377853619766L;

    /**
     * Public constructor.
     */
    public InstantiationException() {
        super();
    }

    /**
     * Public constructor.
     */
    public InstantiationException(String msg) {
        super(msg);
    }

    /**
     * Public constructor.
     */
    public InstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Public constructor.
     */
    public InstantiationException(String message, Throwable cause, boolean enableSuppression,
				    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Public constructor.
     */
    public InstantiationException(Throwable cause) {
        super(cause);
    }
}
