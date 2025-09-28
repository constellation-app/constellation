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
package au.gov.asd.tac.constellation.utilities.datastructure;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A hash set that holds primitive integers instead of Integer objects for
 * speed. The hash set has a capacity and it is assumed that all entries will be
 * in the range 0 &lt;= x &lt; capacity.
 *
 * @author sirius
 */
public abstract class IntHashSet implements Serializable {

    /**
     * Subclasses should override this method to provide hash codes for each
     * integer value in the hash set.
     *
     * @param element the id of the element.
     * @return the hashcode of the specified element.
     */
    protected abstract int getHash(int element);

    /**
     * Subclasses should override this method to define what equals means for
     * elements in this hash set.
     *
     * @param element1 the id of the first element to compare.
     * @param element2 the id of the second element to compare.
     * @return true if the 2 elements are considered equal for the purposes of
     * this set.
     */
    protected abstract boolean equals(final int element1, final int element2);
    
    private final int[] buckets;
    private final int[] next;
    private final int[] prev;
    private final int[] hashCache;

    protected IntHashSet(final int capacity) {
        buckets = new int[capacity];
        Arrays.fill(buckets, -1);
        next = new int[capacity];
        prev = new int[capacity];
        hashCache = new int[capacity];
    }

    protected IntHashSet(final IntHashSet original) {
        this.buckets = Arrays.copyOf(original.buckets, original.buckets.length);
        this.next = Arrays.copyOf(original.next, original.next.length);
        this.prev = Arrays.copyOf(original.prev, original.prev.length);
        this.hashCache = Arrays.copyOf(original.hashCache, original.hashCache.length);
    }

    /**
     * Returns the average bucket size for each element as a measure of the
     * quality of the hash function. This can be interpreted as the average
     * number of items that must be searched through to find an entry.
     *
     * @return the average bucket size for each element.
     */
    public float getAverageQueue() {
        float total = 0;
        float items = 0;
        for (int bucket = 0; bucket < buckets.length; bucket++) {
            int length = 0;
            int item = buckets[bucket];
            while (item >= 0) {
                length++;
                item = next[item];
            }
            items += length;
            total += length * length;
        }
        return items != 0 ? total / items : 0;
    }

    /**
     * Add a new element to the hash set. If there is no element in the hash set
     * that equals the new element then -1 is returned. If an equal element is
     * already in the hash set then the id of that element is returned.
     *
     * Note that equals is not determined on the equality of the element id, but
     * rather by the equality function defined by subclasses. Therefore,
     * elements that have different ids can be considered equal in the hash set.
     *
     * @param element the element to be added.
     * @return the id of the existing element or -1 if no equal element exists
     * in the set.
     */
    public int add(final int element) {
        final int hash = getHash(element);
        final int bucket = Math.abs(hash) % buckets.length;
        int existingElement = buckets[bucket];

        while (existingElement >= 0) {
            if (hashCache[existingElement] == hash && equals(existingElement, element)) {
                return existingElement;
            }
            existingElement = next[existingElement];
        }

        next[element] = buckets[bucket];
        prev[element] = bucket | 0x80000000;
        hashCache[element] = hash;

        if (buckets[bucket] >= 0) {
            prev[buckets[bucket]] = element;
        }
        buckets[bucket] = element;

        return -1;
    }

    /**
     * Remove the specified element from the hash set.
     *
     * @param element the id of the element to remove from the set.
     */
    public void remove(final int element) {
        if (prev[element] < 0) {
            final int bucket = prev[element] & 0x7FFFFFFF;
            buckets[bucket] = next[element];
        } else {
            next[prev[element]] = next[element];
        }

        if (next[element] >= 0) {
            prev[next[element]] = prev[element];
        }
    }
}
