/*
 * Copyright 2010-2023 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.views.analyticview.utilities;

/**
 * An {@link Exception} which should be used to describe an issue with the
 * execution of an {@link AnalyticPlugin}.
 *
 * @author cygnus_x-1
 */
public class AnalyticException extends Exception {

    /**
     * Construct an AnalyticException with no details.
     */
    public AnalyticException() {
        super();
    }

    /**
     * Construct an AnalyticException with a simple message.
     *
     * @param message A {@link String} representing a message associated with
     * this AnalyticException.
     */
    public AnalyticException(final String message) {
        super(message);
    }

    /**
     * Construct an AnalyticException with an underlying cause.
     *
     * @param cause A {@link Throwable} which is the underlying cause for this
     * AnalyticException.
     */
    public AnalyticException(final Throwable cause) {
        super(cause);
    }

    /**
     * Construct an AnalyticException with a simple message and an underlying
     * {@link Throwable}.
     *
     * @param message A {@link String} representing a message associated with
     * this AnalyticException.
     * @param cause A {@link Throwable} which is the underlying cause for this
     * AnalyticException.
     */
    public AnalyticException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
