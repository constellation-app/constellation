/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph;

/**
 * A DuplicateKeyException is thrown when two elements of the same type are
 * discovered in a graph that have identical values for their primary keys.
 * There are two situations when this condition is checked for allowing this
 * exception to be thrown. The most common situation occurs when a commit occurs
 * at which stage the graph automatically checks for duplicate keys and throws
 * an exception as appropriate. If an exception is thrown, all changes that are
 * part of the commit will be rolled back. The other situation where a primary
 * key check is performed is when a plugin explicitly requests it by calling {@link GraphWriteMethods#validateKey(au.gov.asd.tac.constellation.graph.GraphElementType, boolean)
 * }
 * or {@link GraphWriteMethods#validateKey(au.gov.asd.tac.constellation.graph.GraphElementType, int, boolean)
 * }. At all other times, such as during a graph transaction, duplicate keys are
 * allowed and no exception will be thrown.
 *
 * @author sirius
 */
public class DuplicateKeyException extends RuntimeException {

    private final GraphElementType elementType;
    private final int existingId;
    private final int newId;

    /**
     * Creates a new DuplicateKeyException.
     *
     * @param message the message describing the primary key clash.
     * @param elementType the type of element that caused the key clash.
     * @param existingId the original element that held the duplicate primary
     * key.
     * @param newId the new element that now also holds the duplicate primary
     * key.
     */
    public DuplicateKeyException(final String message, final GraphElementType elementType, final int existingId, final int newId) {
        super(message);
        this.elementType = elementType;
        this.existingId = existingId;
        this.newId = newId;
    }

    /**
     * Returns the element type where the duplicate primary key occurred.
     *
     * @return the element type where the duplicate primary key occurred.
     */
    public GraphElementType getElementType() {
        return elementType;
    }

    /**
     * Returns the original element that had the duplicate primary key.
     *
     * @return the original element that had the duplicate primary key.
     */
    public int getExistingId() {
        return existingId;
    }

    /**
     * Returns the new element that now also has the duplicate primary key.
     *
     * @return the new element that now also has the duplicate primary key.
     */
    public int getNewId() {
        return newId;
    }
}
