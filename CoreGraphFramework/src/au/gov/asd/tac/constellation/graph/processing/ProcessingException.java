/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.processing;

/**
 * An {@link Exception} which should be used to describe an issue with
 * processing as performed by a {@link DatumProcessor}.
 *
 * @author twilight_sparkle
 */
public class ProcessingException extends Exception {

    /**
     * Construct a ProcessingException with no details.
     */
    public ProcessingException() {
        super();
    }

    /**
     * Construct a ProcessingException with a simple message.
     *
     * @param message A {@link String} representing a message associated with
     * this ProcessingException.
     */
    public ProcessingException(final String message) {
        super(message);
    }

    /**
     * Construct a ProcessingException with an underlying cause.
     *
     * @param cause A {@link Throwable} which is the underlying cause for this
     * ProcessingException.
     */
    public ProcessingException(final Throwable cause) {
        super(cause);
    }

    /**
     * Construct a ProcessingException with a simple message and an underlying
     * {@link Throwable}.
     *
     * @param message A {@link String} representing a message associated with
     * this ProcessingException.
     * @param cause A {@link Throwable} which is the underlying cause for this
     * ProcessingException.
     */
    public ProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
