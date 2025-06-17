/*
 * Copyright 2010-2025 Australian Signals Directorate
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
 * A GraphIndexResult represents the result of an index query on a graph
 * attribute.
 *
 * @author sirius
 */
public interface GraphIndexResult {

    /**
     * Return the number of elements in this index result.
     *
     * @return The number of elements in this index result.
     */
    public int getCount();

    /**
     * Return the next element in this index result.
     * <p>
     * It is valid to call getNextElement() for as many elements as there are in
     * this index result; see {@link GraphIndexResult#getCount() }. The result
     * of calling getNextElement() more often is undefined.
     *
     * @return The next element in this index result.
     */
    public int getNextElement();
}
