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
package au.gov.asd.tac.constellation.graph;

/**
 * A GraphIndex represents an index on a specific attribute in the graph.
 *
 * In general, this is an object created by a specific AttributeDescription and
 * has access to the internal data structures of the that AttributeDescription.
 *
 * @author sirius
 */
public interface GraphIndex {

    /**
     * Adds the specified element to the index.
     *
     * @param element the element id.
     */
    public void addElement(final int element);

    /**
     * Removes the specified element from the index.
     *
     * @param element the element id.
     */
    public void removeElement(final int element);

    /**
     * Updates the index to reflect a change in the attribute value for the
     * specified element.
     *
     * @param element the element id.
     */
    public void updateElement(final int element);

    /**
     * Returns a graph index result holding all elements that have attribute
     * values equal to the specified value, or null if this index is not capable
     * of performing this type of query.
     *
     * @param value the attribute value to compare against.
     * @return a graph index result holding all elements that have attribute
     * values equal to the specified value, or null if this index is not capable
     * of performing this type of query.
     */
    public GraphIndexResult getElementsWithAttributeValue(final Object value);

    /**
     * Returns a graph index result holding all elements that have attribute
     * values with in the specified range, or null if this index is not capable
     * of performing this type of query. This range is considered to be
     * inclusive of the start object but exclusive of the end value.
     *
     * @param start of the beginning of the range (inclusive).
     * @param end the end of the range (exclusive).
     *
     * @return a graph index result holding all elements that have attribute
     * values with in the specified range, or null if this index is not capable
     * of performing this type of query.
     */
    public GraphIndexResult getElementsWithAttributeValueRange(final Object start, final Object end);

    /**
     * Causes this index to expand its capacity to hold a greater number of
     * elements.
     *
     * @param newCapacity the new capacity of the index.
     */
    public void expandCapacity(final int newCapacity);

}
