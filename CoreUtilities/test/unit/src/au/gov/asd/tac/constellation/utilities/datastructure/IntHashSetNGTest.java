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
package au.gov.asd.tac.constellation.utilities.datastructure;

import java.io.Serializable;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * Vaguely tests the abstract class IntHashSet. The tests are "vague" because
 * the only available feedback given from interaction with the class is: -
 * {@link IntHashSet#getAverageQueue IntHashSet.getAverageQueue()}, the average
 * bucket size for each element - {@link IntHashSet#add IntHashSet.add(int)},
 * the id of the existing element if there is one Everything else is internal to
 * IntHashSet rendering it inaccessible to anything short of reflection - which
 * is slow and Constellation's test regime is already giganticly slow.
 *
 * Therefore the most practical way forward is exercise the public methods and
 * use {@link IntHashSet#getAverageQueue IntHashSet.getAverageQueue()} to get an
 * indication that the operation may have worked.
 *
 * Better than nothing.
 *
 * @author groombridge34a
 */
public class IntHashSetNGTest implements Serializable {

    /**
     * IntHashSet is abstract so this is a concrete implementation for testing.
     * Specifically, the hashing algorithm is fantastic.
     */
    private class IntHashSetImpl extends IntHashSet {

        public IntHashSetImpl(int capacity) {
            super(capacity);
        }

        public IntHashSetImpl(IntHashSet hs) {
            super(hs);
        }

        @Override
        protected int getHash(int element) {
            return 7 * element;
        }

        @Override
        protected boolean equals(int element1, int element2) {
            return element1 == element2;
        }
    }

    /**
     * An implementation of IntHashSet with a terrible hashing algorithm. This
     * means we can use
     * {@link IntHashSet#getAverageQueue IntHashSet.getAverageQueue()} to give
     * an indication of changes occurring to the internal state of the object.
     */
    private class IntHashSetImplBadHash extends IntHashSet {

        public IntHashSetImplBadHash(int capacity) {
            super(capacity);
        }

        public IntHashSetImplBadHash(IntHashSet hs) {
            super(hs);
        }

        @Override
        protected int getHash(int element) {
            return element % 2;
        }

        @Override
        protected boolean equals(int element1, int element2) {
            return element1 == element2;
        }
    }

    /**
     * Can clone an IntHashSet.
     */
    @Test
    public void testCopyConstructor() {
        IntHashSet i = new IntHashSetImplBadHash(55);

        i.add(0);
        i.add(3);
        i.add(54);
        i.add(1);
        i.add(14);
        i.add(12);
        i.add(2);
        i.add(4);
        i.add(5);
        i.add(6);

        IntHashSet i2 = new IntHashSetImplBadHash(i);
        // Check the average queue of i and i2 are the same, loosely indicating
        // the two hashsets contain the same values.
        assertEquals(i2.getAverageQueue(), i.getAverageQueue());
    }

    /**
     * Can add elements to an IntHashSet.
     */
    @Test
    public void testAdd() {
        IntHashSet i = new IntHashSetImplBadHash(55);

        // Return value of -1 from the add method indicates the element did not
        // exist, and return value of 1F from getAverageQueue indicates there
        // are not many elements in the IntHashSet.
        assertEquals(i.add(1), -1);
        assertEquals(i.getAverageQueue(), 1F);

        // Return value of 1 from the add method indicates the element did
        // exist in the IntHashSet.
        assertEquals(i.add(1), 1);
        assertEquals(i.getAverageQueue(), 1F);

        // Return value of 2.0F from getAverageQueue indicates there are more
        // elements in the HashSet.
        assertEquals(i.add(2), -1);
        assertEquals(i.add(3), -1);
        assertEquals(i.add(4), -1);
        assertEquals(i.getAverageQueue(), 2.0F);
    }

    /**
     * Can remove elements from an IntHashSet.
     */
    @Test
    public void testRemove() {
        IntHashSet i = new IntHashSetImplBadHash(55);

        // Removing an non-existant element is ignored. getAverageQueue
        // returning 0F means the IntHashSet is empty.
        i.remove(1);
        assertEquals(i.getAverageQueue(), 0F);

        // Add and remove the same element then check IntHashSet is empty.
        i.add(1);
        i.remove(1);
        assertEquals(i.getAverageQueue(), 0F);

        // Adding four elements then removing one shows a change in
        // getAverageQueue, indicating there are still multiple items.
        i.add(2);
        i.add(3);
        i.add(4);
        i.add(1);
        i.remove(3);
        assertEquals(i.getAverageQueue(), 1.6666666F);

        // getAverageQueue returning 1.0 in this case indicates that there is
        // probably only one element left in the IntHashSet.
        i.remove(2);
        i.remove(1);
        assertEquals(i.getAverageQueue(), 1.0F);

        // Confirm that removing one more element leaves the IntHashSet empty.
        i.remove(4);
        assertEquals(i.getAverageQueue(), 0F);
    }

    /**
     * Can get the average queue length of an IntHashSet with a spectacular
     * hashing algorithm.
     */
    @Test
    public void testGetAverageQueue() {
        IntHashSet i = new IntHashSetImpl(55);

        // Returns zero if there are no items.
        assertEquals(i.getAverageQueue(), 0F);

        i.add(0);
        i.add(0);
        i.add(3);
        i.add(54);
        i.add(1);
        i.add(14);
        i.add(3);
        i.add(12);
        i.add(3);
        i.add(2);
        i.add(4);
        i.add(5);
        i.add(6);

        // The hashing algorithm is awesome, so a good average queue length is
        // returned.
        assertEquals(i.getAverageQueue(), 1.0F);
    }

    /**
     * Can get the average queue length of an IntHashSet with a abysmal hashing
     * algorithm.
     */
    @Test
    public void testGetAverageQueueBadHash() {
        IntHashSet i = new IntHashSetImplBadHash(55);

        i.add(0);
        i.add(3);
        i.add(54);
        i.add(1);
        i.add(14);
        i.add(12);
        i.add(2);
        i.add(4);
        i.add(5);
        i.add(6);

        assertEquals(i.getAverageQueue(), 5.8F);
    }
}
