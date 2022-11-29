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

import java.util.Arrays;

/**
 * A data structure that holds an ordered list of element ids. The elements are
 * stored as a doubly linked list allowing efficient addition and removal from
 * both ends of the list.
 *
 * @author sirius
 */
public class ElementList {

    private int[] prev;
    private int[] next;

    private int capacity;
    private int size;
    private int first;
    private int last;

    /**
     * Creates a new ElementList with the specified capacity.
     *
     * @param capacity the capacity of the new ElementList.
     */
    public ElementList(final int capacity) {

        this.capacity = capacity;
        this.size = 0;
        this.first = Graph.NOT_FOUND;
        this.last = Graph.NOT_FOUND;

        this.prev = new int[capacity];
        this.next = new int[capacity];

        for (int i = 0; i < capacity; i++) {
            prev[i] = Graph.NOT_FOUND;
        }
    }

    /**
     * Creates a new ElementList that is identical to the specified other
     * ElementList.
     *
     * @param original the other ElementList.
     */
    public ElementList(final ElementList original) {
        this.capacity = original.capacity;
        this.size = original.size;
        this.first = original.first;
        this.last = original.last;
        this.prev = Arrays.copyOf(original.prev, original.prev.length);
        this.next = Arrays.copyOf(original.next, original.next.length);
    }

    /**
     * Creates a new ElementList with the same elements as the element store.
     * The capacity of the new ElementList will also be the same as that of the
     * ElementStore.
     *
     * @param elementStore the ElementStore.
     */
    public ElementList(final ElementStore elementStore) {
        this(elementStore.getCapacity());
        int elementCount = elementStore.getCount();
        for (int i = 0; i < elementCount; i++) {
            this.addToBack(elementStore.getElement(i));
        }
    }

    /**
     * Ensures that the list is capable of holding the specified number of
     * elements. The elements will be from 0 to capacity - 1 inclusive.
     *
     * @param newCapacity the new capacity.
     */
    public void ensureCapacity(final int newCapacity) {
        if (newCapacity > capacity) {
            this.prev = Arrays.copyOf(prev, newCapacity);
            this.next = Arrays.copyOf(next, newCapacity);

            for (int i = capacity; i < newCapacity; i++) {
                prev[i] = Graph.NOT_FOUND;
            }

            capacity = newCapacity;
        }
    }

    /**
     * Adds an element to the front of the list. If the element is already in
     * the list, it is removed from its current position before being added to
     * the front.
     *
     * @param element the element to be added.
     * @return true if the element was added (not already in the list)
     */
    public boolean addToFront(final int element) {

        // If the element is not currently in the list...
        if (prev[element] == Graph.NOT_FOUND) {
            if (first == Graph.NOT_FOUND) {
                first = last = element;
            } else {
                prev[first] = element;
                next[element] = first;
                first = element;
            }
            prev[element] = 0;
            size++;

            // Return true to indicate that we did add the element to the list
            return true;

            // If the element is currently in the list but not the last element...
        } else if (first != element) {
            if (last == element) {
                last = prev[element];
            } else {
                next[prev[element]] = next[element];
                prev[next[element]] = prev[element];
            }

            // Add the element to the back of the list
            prev[first] = element;
            next[element] = first;
            first = element;
        } else {
            // Do nothing
        }

        // Return false to indicate that we did not add the element to the list
        return false;
    }

    /**
     * Adds an element to the back of the list. If the element is already in the
     * list, it is removed from its current position before being added to the
     * back.
     *
     * @param element the element to be added.
     * @return true if the element was added (not already in the list)
     */
    public boolean addToBack(final int element) {

        // If the element is not currently in the list...
        if (prev[element] == Graph.NOT_FOUND) {
            if (last == Graph.NOT_FOUND) {
                first = last = element;
                prev[element] = 0;
            } else {
                next[last] = element;
                prev[element] = last;
                last = element;
            }
            size++;
            return true;

            // If the element is currently in the list but not the last element...
        } else if (last != element) {

            // If the element is currently the first element in the list
            if (first == element) {
                first = next[element];
            } else {
                next[prev[element]] = next[element];
                prev[next[element]] = prev[element];
            }

            next[last] = element;
            prev[element] = last;
            last = element;
        } else {
            // Do nothing
        }

        return false;
    }

    /**
     * Returns true if the element is currently in the list.
     *
     * @param element the element.
     * @return true if the element is currently in the list.
     */
    public boolean contains(final int element) {
        return prev[element] != Graph.NOT_FOUND;
    }

    /**
     * Returns the first element in the list or Graph.NOT_FOUND if the list is
     * empty.
     *
     * @return the first element in the list or Graph.NOT_FOUND if the list is
     * empty.
     */
    public int getFirst() {
        return first;
    }

    /**
     * Returns the last element in the list or Graph.NOT_FOUND if the list is
     * empty.
     *
     * @return the last element in the list or Graph.NOT_FOUND if the list is
     */
    public int getLast() {
        return last;
    }

    /**
     * Returns the number of elements in the list.
     *
     * @return the number of elements in the list.
     */
    public int getSize() {
        return size;
    }

    /**
     * Removes the specified element from the list if it is currently in the
     * list.
     *
     * @param element the element.
     * @return true if the element was removed (was previously in the list).
     */
    public boolean remove(final int element) {
        if (prev[element] == Graph.NOT_FOUND) {
            return false;
        }

        if (first == last) {
            first = last = Graph.NOT_FOUND;
        } else if (last == element) {
            last = prev[element];
        } else if (first == element) {
            first = next[element];
        } else {
            next[prev[element]] = next[element];
            prev[next[element]] = prev[element];
        }

        prev[element] = Graph.NOT_FOUND;
        size--;

        return true;
    }

    /**
     * Removes and returns the first element on the list, or Graph.NOT_FOUND if
     * the list is empty.
     *
     * @return the first element on the list, or Graph.NOT_FOUND if the list is
     * empty.
     */
    public int removeFirst() {
        final int element = first;

        // If the list is not empty...
        if (first != Graph.NOT_FOUND) {

            // If the list has exactly 1 element...
            if (first == last) {
                first = last = Graph.NOT_FOUND;

                // If the list has > 1 element...
            } else {
                first = next[element];
            }

            prev[element] = Graph.NOT_FOUND;
            size--;
        }

        return element;
    }

    /**
     * Removes and returns the last element on the list, or Graph.NOT_FOUND if
     * the list is empty.
     *
     * @return the last element on the list, or Graph.NOT_FOUND if the list is
     * empty.
     */
    public int removeLast() {
        final int element = last;

        // If the list is not empty...
        if (last != Graph.NOT_FOUND) {

            // If the list has exactly 1 element...
            if (first == last) {
                first = last = Graph.NOT_FOUND;

                // If the list has > 1 element...
            } else {
                last = prev[element];
            }

            prev[element] = Graph.NOT_FOUND;
            size--;
        }

        return element;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("ElementList[size=").append(size);
        out.append(",first=").append(first).append(",last=").append(last);
        out.append(",elements={");

        if (size > 0) {
            int element = first;
            out.append(element);

            while (element != last) {
                element = next[element];
                out.append(',').append(element);
            }
        }

        out.append("}]");

        return out.toString();
    }
}
