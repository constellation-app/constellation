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
package au.gov.asd.tac.constellation.testing.construction;

/**
 *
 * @author canis_majoris
 */
public class GraphBuildException extends IllegalArgumentException {

    /**
     * Constructs a <code>NumberFormatException</code> with no detail message.
     */
    public GraphBuildException() {
        super();
    }

    /**
     * Constructs a <code>NumberFormatException</code> with the specified detail
     * message.
     *
     * @param s the detail message.
     */
    public GraphBuildException(String s) {
        super(s);
    }

    /**
     * Factory method for making a <code>NumberFormatException</code> given the
     * specified input which caused the error.
     *
     * @param s the input causing the error
     */
    static GraphBuildException forInputString(String s) {
        return new GraphBuildException("For input string: \"" + s + "\"");
    }
}
