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
package au.gov.asd.tac.constellation.utilities.graphics;

import java.util.NoSuchElementException;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * @author groombridge34a
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class IntArrayNGTest extends ConstellationTest {
    
    private static final int I1 = 1;
    private static final int I2 = 2;
    private static final int I3 = 3;
    private static final int I4 = 4;
    private static final int I5 = 5;
    
    /**
     * Can create an IntArray. This sets the capacity of the array but the 
     * capacity which cannot be checked without increasing the value of the size
     * field.
     */
    @Test
    public void testConstructor() {
        // default constructor creates an array of size 10
        final IntArray i1 = new IntArray();
        i1.ensureSize(9, 0);
        assertEquals(i1.toString(), "I[0,0,0,0,0,0,0,0,0]");
        
        // constructor specifying capacity
        final IntArray i2 = new IntArray(6);
        assertEquals(i2.size(), 0);
        i2.ensureSize(6, 0);
        assertEquals(i2.toString(), "I[0,0,0,0,0,0]");
        
        // copy constructor
        final IntArray i3 = new IntArray();
        i3.add(I1, I2, I3, I4);
        final IntArray i4 = new IntArray(i3);
        assertEquals(i4.size(), i3.size());
        assertEquals(i4.toArray(), i3.toArray());
    }
    
    /**
     * Cannot create an IntArray with a negative capacity.
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Illegal Capacity: -1")
    public void testConstructorNegativeCap() {
        new IntArray(-1);
    }
    
    /**
     * Can trim a IntArray to size.
     */
    @Test
    public void testTrimToSize() {
        // trim
        final IntArray i = new IntArray(6);
        i.add(I1);
        i.add(I2);
        i.trimToSize();
        final String expected = "I[" + I1 + "," + I2 + "]";
        assertEquals(i.toString(), expected);
        
        // attempting to trim a trimmed array does nothing
        i.trimToSize();
        assertEquals(i.toString(), expected);
    }
    
    /**
     * Can increase the capacity of a IntArray.
     */
    @Test
    public void testEnsureCapacity() {
        // ensure capacity
        final IntArray i = new IntArray(0);
        i.add(I1);
        i.add(I2);
        i.ensureCapacity(4);
        i.ensureSize(4, 0);
        final String expected = "I[" + I1 + "," + I2 + ",0,0]";
        assertEquals(i.toString(), expected);
        
        // ensuring capacity smaller than size does nothing
        i.ensureCapacity(i.size() - 1);
        assertEquals(i.toString(), expected);
    }
    
    /**
     * Can increase the size of a IntArray.
     */
    @Test
    public void testEnsureSize() {
        // ensure size
        final IntArray i = new IntArray(2);
        i.ensureSize(4, I1);
        final String expected = "I[" + I1 + "," + I1 + "," + I1 + "," + I1 + "]";
        assertEquals(i.toString(), expected);
        
        // ensuring size smaller than current size does nothing
        i.ensureSize(i.size() - 1, I2);
        assertEquals(i.toString(), expected);
    }
    
    /**
     * Can get the size of a IntArray.
     */
    @Test
    public void testSize() {
        final IntArray i = new IntArray();
        assertEquals(i.size(), 0);
        i.add(I1);
        i.add(I2);
        i.add(I3);
        assertEquals(i.size(), 3);
    }
    
    /**
     * Can check if a IntArray is empty.
     */
    @Test
    public void testIsEmpty() {
        final IntArray i = new IntArray();
        assertTrue(i.isEmpty());
        i.add(I1);
        assertFalse(i.isEmpty());
    }
    
    /**
     * Can check if the IntArray contains a specific int value.
     */
    @Test
    public void testContains() {
        final IntArray i = new IntArray();
        assertFalse(i.contains(I1));
        i.add(I1);
        assertTrue(i.contains(I1));
        i.add(I2, I3, I1, I5);
        assertTrue(i.contains(I1));
    }
    
    /**
     * Can get the index of a specific int value in a IntArray.
     */
    @Test
    public void testIndexOf() {
        final IntArray i = new IntArray();
        assertEquals(i.indexOf(I1), -1);
        i.add(I2, I3, I4, I1);
        assertEquals(i.indexOf(I1), 3);
    }
    
    /**
     * Can clone a IntArray.
     */
    @Test
    public void IntArray() {
        final IntArray i = new IntArray();
        i.add(I4, I3, I2, I1);
        final IntArray iClone = i.clone();
        assertEquals(i.toString(), iClone.toString());
    }
    
    /**
     * Can return the array of elements.
     */
    @Test
    public void testArray() {
        final IntArray i = new IntArray();
        i.add(I3, I1, I4, I2);
        final int[] expected = new int[] {I3, I1, I4, I2};
        
        // safe copy method
        final int[] iSafe = i.toArray();
        assertEquals(iSafe, expected);
        assertNotSame(iSafe, i.toArray());
        
        // raw array accessor
        final int[] iRaw = i.rawArray();
        assertEquals(iRaw, expected);
        assertSame(iRaw, i.rawArray());
    }
    
    /**
     * Can get an element.
     */
    @Test
    public void testGet() {
        final IntArray i = new IntArray();
        i.add(I3, I1, I4, I2);
        assertEquals(i.get(2), I4);
    }
    
    /**
     * An Exception is thrown when attempting to get an element outside of the
     * array range.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: 5, Size: 4")
    public void testGetException() {
        final IntArray i = new IntArray();
        i.add(I3, I1, I4, I2);
        i.get(5);
    }
    
    /**
     * Can set an element.
     */
    @Test
    public void testSet() {
        final IntArray i = new IntArray();
        i.add(I3, I1, I4, I2);
        assertEquals(i.set(1, I5), I1);
        assertEquals(i.get(1), I5);
    }
    
    /**
     * An Exception is thrown when attempting to get an element outside of the
     * array range.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: 7, Size: 4")
    public void testSetException() {
        final IntArray i = new IntArray();
        i.add(I3, I1, I2, I3);
        i.set(7, I5);
    }
    
    /**
     * Can add elements.
     */
    @Test
    public void testAdd() {
        final IntArray i = new IntArray();
        
        // single add
        assertTrue(i.add(I5));
        assertTrue(i.add(I3));
        assertTrue(i.add(I1));
        assertEquals(i.toArray(), new int[] {I5, I3, I1});
        
        // quadro-add
        assertTrue(i.add(I2, I4, I2, I1));
        assertEquals(i.toArray(), 
                new int[] {I5, I3, I1, I2, I4, I2, I1});
        
        // the add that's more of an insert
        i.add(4, I1);
        assertEquals(i.toArray(), 
                new int[] {I5, I3, I1, I2, I1, I4, I2, I1});
        
        // insertAdd at the end of the array
        i.add(8, I1);
        assertEquals(i.toArray(), 
                new int[] {I5, I3, I1, I2, I1, I4, I2, I1, I1});
        
        // insertAdd into an empty array
        final IntArray i2 = new IntArray(0);
        i2.add(0, I4);
        assertEquals(i2.toArray(), new int[] {I4});
        
        // add everything from an empty array
        final IntArray i3 = new IntArray(0);
        assertFalse(i3.addAll(i3));
        assertEquals(i3.toArray(), new int[] {});
        
        // add everything from another array
        assertTrue(i3.addAll(i));
        assertEquals(i3.toArray(), 
                new int[] {I5, I3, I1, I2, I1, I4, I2, I1, I1});
        assertTrue(i3.addAll(i));
        assertEquals(i3.toArray(), 
                new int[] {I5, I3, I1, I2, I1, I4, I2, I1, I1, I5, I3, I1, I2, 
                    I1, I4, I2, I1, I1});
    }
    
    /**
     * An Exception is thrown when attempting to add an element at a negative
     * index.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: -12, Size: 4")
    public void testAddNegativeException() {
        final IntArray i = new IntArray();
        i.add(I3, I1, I2, I3);
        i.add(-12, I5);
    }
    
    /**
     * An Exception is thrown when attempting to add an element at an index 
     * greater than the size of the array.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: 5, Size: 4")
    public void testAddGreaterThanSizeException() {
        final IntArray i = new IntArray();
        i.add(I3, I1, I2, I3);
        i.add(5, I5);
    }
    
    /**
     * Can remove an element from a specific index.
     */
    @Test
    public void testRemoveAt() {
        // remove last element
        final IntArray i = new IntArray();
        i.add(I1);
        assertEquals(i.removeAt(0), I1);
        assertEquals(i.toArray(), new int[] {});
        
        // remove and leave other elements remaining
        i.add(I5, I4, I3, I1);
        assertEquals(i.removeAt(1), I4);
        assertEquals(i.toArray(), new int[] {I5, I3, I1});
    }
    
    /**
     * An Exception is thrown when attempting to remove an element outside of 
     * the array range.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: 6, Size: 4")
    public void testRemoveAtException() {
        final IntArray i = new IntArray();
        i.add(I3, I1, I2, I3);
        i.removeAt(6);
    }
    
    /**
     * Can remove the first occurrence of an element.
     */
    @Test
    public void testRemove() {
        // remove last element
        final IntArray i = new IntArray();
        i.add(I1);
        assertTrue(i.remove(I1));
        assertEquals(i.toArray(), new int[] {});
        
        // remove and leave other elements remaining
        i.add(I5, I2, I3, I1);
        i.add(I3, I2, I3, I2);
        assertTrue(i.remove(I3));
        assertEquals(i.toArray(), new int[] {I5, I2, I1, I3, I2, I3, I2});
        
        // remove a non-existent element
        assertFalse(i.remove(I4));
    }
    
    /**
     * Can clear the array.
     */
    @Test
    public void testClear() {
        final IntArray i = new IntArray();
        i.clear();
        assertEquals(i.toArray(), new int[] {});
        assertEquals(i.size(), 0);
        i.add(I5, I2, I3, I1);
        i.clear();
        assertEquals(i.toArray(), new int[] {});
        assertEquals(i.size(), 0);
        i.clear();
        assertEquals(i.toArray(), new int[] {});
        assertEquals(i.size(), 0);
    }
    
    /**
     * Can get an Iterator of the IntArray and iterate over it.
     */
    @Test
    public void testIterator() {
        final IntArray i = new IntArray();
        i.add(I5, I2, I3, I1);
        final IntArray.Itr itr = i.iterator();
        for (int j = 0; j < i.size(); j++) {
            assertTrue(itr.hasNext());
            assertEquals(itr.next().intValue(), i.get(j));
        }
        assertFalse(itr.hasNext());
    }
    
    /**
     * An Exception is thrown when attempting to retrieve the next element from 
     * an iterator when there are no more elements.
     */
    @Test(expectedExceptions = {NoSuchElementException.class})
    public void testNextException() {
        new IntArray().iterator().next();
    }
    
    /**
     * An Exception is thrown when attempting to remove an element from the
     * iterator.
     */
    @Test(expectedExceptions = {UnsupportedOperationException.class}, expectedExceptionsMessageRegExp = "Not supported.")
    public void testRemoveException() {
        new IntArray().iterator().remove();
    }
    
    /**
     * Can get a String representation of a IntArray.
     */
    @Test
    public void testToString() {
        final IntArray i = new IntArray();
        i.add(I5, I2, I3, I1);
        assertEquals(i.toString(), "I[" + I5 + "," + I2 + "," + I3 + "," + I1 + "]");
    }
}
