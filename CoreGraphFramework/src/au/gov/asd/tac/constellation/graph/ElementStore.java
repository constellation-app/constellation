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

import java.io.Serializable;
import java.util.Arrays;

/**
 * An ElementStore stores a ordered list of element ids as well as a list of
 * free ids that can be used for new elements. It is used in {@link StoreGraph}
 * to hold the master list of each element type in the graph.
 *
 * @author sirius
 */
public class ElementStore implements Serializable {

    private static final int HIGH_BIT = 0x80000000;
    private static final int LOW_BITS = 0x7FFFFFFF;
    private int capacity;
    private int count = 0;
    private int[] position2id;
    private int[] id2position;
    private long[] id2UID;

    public ElementStore(final int capacity) {
        this.capacity = capacity;
        this.position2id = new int[capacity];
        this.id2position = new int[capacity];
        this.id2UID = new long[capacity];

        for (int i = 0; i < capacity; i++) {
            position2id[i] = i;
            id2position[i] = i | HIGH_BIT;
        }
    }

    public ElementStore(final ElementStore original) {
        this.capacity = original.capacity;
        this.count = original.count;

        this.position2id = Arrays.copyOf(original.position2id, original.position2id.length);
        this.id2position = Arrays.copyOf(original.id2position, original.id2position.length);
        this.id2UID = Arrays.copyOf(original.id2UID, original.id2UID.length);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCount() {
        return count;
    }

    public boolean ensureCapacity() {
        if (count == capacity) {
            capacity <<= 1;
            position2id = Arrays.copyOf(position2id, capacity);
            id2position = Arrays.copyOf(id2position, capacity);
            id2UID = Arrays.copyOf(id2UID, capacity);
            for (int i = count; i < capacity; i++) {
                position2id[i] = i;
                id2position[i] = i | HIGH_BIT;
            }
            return true;
        }
        return false;
    }

    public boolean ensureCapacity(final int capacity) {
        if (this.capacity < capacity) {
            int c = this.capacity;
            while (c < capacity) {
                c <<= 1;
            }

            position2id = Arrays.copyOf(position2id, c);
            id2position = Arrays.copyOf(id2position, c);
            id2UID = Arrays.copyOf(id2UID, c);
            for (int i = this.capacity; i < c; i++) {
                position2id[i] = i;
                id2position[i] = i | HIGH_BIT;
            }
            this.capacity = c;
            return true;
        }
        return false;
    }

    public int add() {
        final int element = position2id[count];
        id2position[element] = count++;
        return element;
    }

    public void add(final int element) {
        // Get the position of the new element
        final int position = id2position[element] & LOW_BITS;

        // Move the next element to the positon of the element
        final int nextElement = position2id[count];
        id2position[nextElement] = position | HIGH_BIT;
        position2id[position] = nextElement;

        position2id[count] = element;
        id2position[element] = count++;
    }

    public boolean addIfRemoved(final int element) {
        // Get the position of the new element
        int position = id2position[element];

        if (position < 0) {
            position &= LOW_BITS;

            // Move the next element to the positon of the element
            final int nextElement = position2id[count];
            id2position[nextElement] = position | HIGH_BIT;
            position2id[position] = nextElement;

            position2id[count] = element;
            id2position[element] = count++;

            return true;
        }

        return false;
    }

    public void remove(final int element) {
        final int position = id2position[element];

        final int lastAddedElement = position2id[--count];
        position2id[position] = lastAddedElement;
        id2position[lastAddedElement] = position;

        position2id[count] = element;
        id2position[element] = count | HIGH_BIT;
    }

    public boolean removeIfAdded(final int element) {
        final int position = id2position[element];

        if (position >= 0) {
            final int lastAddedElement = position2id[--count];
            position2id[position] = lastAddedElement;
            id2position[lastAddedElement] = position;

            position2id[count] = element;
            id2position[element] = count | HIGH_BIT;

            return true;
        }

        return false;
    }

    public int getLast() {
        return count <= 0 ? -1 : position2id[count - 1];
    }

    public int removeLast() {
        final int element = position2id[--count];

        id2position[element] = count | HIGH_BIT;
        return element;
    }

    public boolean elementExists(final int element) {
        return element >= 0 && element < capacity && id2position[element] >= 0;
    }

    public int getElement(final int position) {
        return position2id[position];
    }

    public int getElementPosition(final int element) {
        return id2position[element] & LOW_BITS;
    }

    public long getUID(final int element) {
        return id2UID[element];
    }

    public void setUID(final int element, final long UID) {
        id2UID[element] = UID;
    }
}
