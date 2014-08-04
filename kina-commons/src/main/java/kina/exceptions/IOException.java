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
 * Unchecked variant of {@link java.io.IOException}.
 *
 * @author Luca Rosellini <luca.rosellini@gmail.com>
 */
public class IOException extends RuntimeException {

    private static final long serialVersionUID = 6496798210905077525L;

    /**
     * Default constructor.
     */
    public IOException() {
        super();
    }

    /**
     * Public constructor.
     */
    public IOException(String msg) {
        super(msg);
    }

    /**
     * Public constructor.
     */
    public IOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Public constructor.
     */
    public IOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

    }

    /**
     * Public constructor.
     */
    public IOException(Throwable cause) {
        super(cause);
    }

}
