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
public class FloatArrayNGTest {
    
    private static final float F1 = 1F;
    private static final float F2 = 2F;
    private static final float F3 = 3F;
    private static final float F4 = 4F;
    private static final float F5 = 5F;
    
    /**
     * Can create a FloatArray. This sets the capacity of the array but the 
     * capacity which cannot be checked without increasing the value of the size
     * field.
     */
    @Test
    public void testConstructor() {
        // default constructor creates an array of size 10
        final FloatArray f1 = new FloatArray();
        f1.ensureSize(9, 0F);
        assertEquals(f1.toString(), "F[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0]");
        
        // parameter constructor
        final FloatArray f2 = new FloatArray(6);
        assertEquals(f2.size(), 0);
        f2.ensureSize(6, 0F);
        assertEquals(f2.toString(), "F[0.0,0.0,0.0,0.0,0.0,0.0]");
        
        final FloatArray f3 = new FloatArray();
        f3.add(F4, F3, F2, F1);
        final FloatArray fClone = new FloatArray(f3);
        assertEquals(f3.toString(), fClone.toString());
    }
    
    /**
     * Cannot create a FloatArray with a negative capacity.
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Illegal Capacity: -1")
    public void testConstructorNegativeCap() {
        new FloatArray(-1);
    }
    
    /**
     * Can trim a FloatArray to size.
     */
    @Test
    public void testTrimToSize() {
        // trim
        final FloatArray f = new FloatArray(6);
        f.add(F1);
        f.add(F2);
        f.trimToSize();
        final String expected = "F[" + F1 + "," + F2 + "]";
        assertEquals(f.toString(), expected);
        
        // attempting to trim a trimmed array does nothing
        f.trimToSize();
        assertEquals(f.toString(), expected);
    }
    
    /**
     * Can increase the capacity of a FloatArray.
     */
    @Test
    public void testEnsureCapacity() {
        // ensure capacity
        final FloatArray f = new FloatArray(0);
        f.add(F1);
        f.add(F2);
        f.ensureCapacity(4);
        f.ensureSize(4, 0F);
        final String expected = "F[" + F1 + "," + F2 + ",0.0,0.0]";
        assertEquals(f.toString(), expected);
        
        // ensuring capacity smaller than size does nothing
        f.ensureCapacity(f.size() - 1);
        assertEquals(f.toString(), expected);
    }
    
    /**
     * Can increase the size of a FloatArray.
     */
    @Test
    public void testEnsureSize() {
        // ensure size
        final FloatArray f = new FloatArray(2);
        f.ensureSize(4, F1);
        final String expected = "F[" + F1 + "," + F1 + "," + F1 + "," + F1 + "]";
        assertEquals(f.toString(), expected);
        
        // ensuring size smaller than current size does nothing
        f.ensureSize(f.size() - 1, F2);
        assertEquals(f.toString(), expected);
    }
    
    /**
     * Can get the size of a FloatArray.
     */
    @Test
    public void testSize() {
        final FloatArray f = new FloatArray();
        assertEquals(f.size(), 0);
        f.add(F1);
        f.add(F2);
        f.add(F3);
        assertEquals(f.size(), 3);
    }
    
    /**
     * Can check if a FloatArray is empty.
     */
    @Test
    public void testIsEmpty() {
        final FloatArray f = new FloatArray();
        assertTrue(f.isEmpty());
        f.add(F1);
        assertFalse(f.isEmpty());
    }
    
    /**
     * Can check if the FloatArray contains a specific float value.
     */
    @Test
    public void testContains() {
        final FloatArray f = new FloatArray();
        assertFalse(f.contains(F1));
        f.add(F1);
        assertTrue(f.contains(F1));
        f.add(F2, F3, F1);
        assertTrue(f.contains(F1));
    }
    
    /**
     * Can get the index of a specific float value in a FloatArray.
     */
    @Test
    public void testIndexOf() {
        final FloatArray f = new FloatArray();
        assertEquals(f.indexOf(F1), -1);
        f.add(F2, F3, F4, F1);
        assertEquals(f.indexOf(F1), 3);
    }
    
    /**
     * Can return the array of elements.
     */
    @Test
    public void testArray() {
        final FloatArray f = new FloatArray();
        f.add(F3, F1, F4, F2);
        final float[] expected = new float[] {F3, F1, F4, F2};
        
        // safe copy method
        final float[] fSafe = f.toArray();
        assertEquals(fSafe, expected);
        assertNotSame(fSafe, f.toArray());
        
        // raw array accessor
        final float[] fRaw = f.rawArray();
        assertEquals(fRaw, expected);
        assertSame(fRaw, f.rawArray());
    }
    
    /**
     * Can get an element.
     */
    @Test
    public void testGet() {
        final FloatArray f = new FloatArray();
        f.add(F3, F1, F4, F2);
        assertEquals(f.get(2), F4);
    }
    
    /**
     * An Exception is thrown when attempting to get an element outside of the
     * array range.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: 5, Size: 4")
    public void testGetException() {
        final FloatArray f = new FloatArray();
        f.add(F3, F1, F4, F2);
        f.get(5);
    }
    
    /**
     * Can set an element.
     */
    @Test
    public void testSet() {
        final FloatArray f = new FloatArray();
        f.add(F3, F1, F4, F2);
        assertEquals(f.set(1, F5), F1);
        assertEquals(f.get(1), F5);
    }
    
    /**
     * An Exception is thrown when attempting to get an element outside of the
     * array range.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: 7, Size: 3")
    public void testSetException() {
        final FloatArray f = new FloatArray();
        f.add(F3, F1, F2);
        f.set(7, F5);
    }
    
    /**
     * Can add elements.
     */
    @Test
    public void testAdd() {
        final FloatArray f = new FloatArray();
        
        // single add
        assertTrue(f.add(F5));
        assertTrue(f.add(F3));
        assertTrue(f.add(F1));
        assertEquals(f.toArray(), new float[] {F5, F3, F1});
        
        // triple add
        assertTrue(f.add(F2, F4, F2));
        assertEquals(f.toArray(), new float[] {F5, F3, F1, F2, F4, F2});
        
        // quadro-add
        assertTrue(f.add(F1, F3, F5, F4));
        assertEquals(f.toArray(), 
                new float[] {F5, F3, F1, F2, F4, F2, F1, F3, F5, F4});
        
        // the add that's more of an insert
        f.add(4, F1);
        assertEquals(f.toArray(), 
                new float[] {F5, F3, F1, F2, F1, F4, F2, F1, F3, F5, F4});
        
        // insertAdd at the end of the array
        f.add(11, F1);
        assertEquals(f.toArray(), 
                new float[] {F5, F3, F1, F2, F1, F4, F2, F1, F3, F5, F4, F1});
        
        // insertAdd into an empty array
        final FloatArray fEmpty = new FloatArray(0);
        fEmpty.add(0, F4);
        assertEquals(fEmpty.toArray(), new float[] {F4});
    }
    
    /**
     * An Exception is thrown when attempting to add an element at a negative
     * index.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: -12, Size: 3")
    public void testAddNegativeException() {
        final FloatArray f = new FloatArray();
        f.add(F3, F1, F2);
        f.add(-12, F5);
    }
    
    /**
     * An Exception is thrown when attempting to add an element at an index 
     * greater than the size of the array.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: 4, Size: 3")
    public void testAddGreaterThanSizeException() {
        final FloatArray f = new FloatArray();
        f.add(F3, F1, F2);
        f.add(4, F5);
    }
    
    /**
     * Can remove an element from a specific index.
     */
    @Test
    public void testRemoveAt() {
        // remove last element
        final FloatArray f = new FloatArray();
        f.add(F1);
        assertEquals(f.removeAt(0), F1);
        assertEquals(f.toArray(), new float[] {});
        
        // remove and leave other elements remaining
        f.add(F5, F4, F3, F1);
        assertEquals(f.removeAt(1), F4);
        assertEquals(f.toArray(), new float[] {F5, F3, F1});
    }
    
    /**
     * An Exception is thrown when attempting to remove an element outside of 
     * the array range.
     */
    @Test(expectedExceptions = {IndexOutOfBoundsException.class}, expectedExceptionsMessageRegExp = "Index: 5, Size: 3")
    public void testRemoveAtException() {
        final FloatArray f = new FloatArray();
        f.add(F3, F1, F2);
        f.removeAt(5);
    }
    
    /**
     * Can remove the first occurrence of an element.
     */
    @Test
    public void testRemove() {
        // remove last element
        final FloatArray f = new FloatArray();
        f.add(F1);
        assertTrue(f.remove(F1));
        assertEquals(f.toArray(), new float[] {});
        
        // remove and leave other elements remaining
        f.add(F5, F2, F3, F1);
        f.add(F3, F2, F3, F2);
        assertTrue(f.remove(F3));
        assertEquals(f.toArray(), new float[] {F5, F2, F1, F3, F2, F3, F2});
        
        // remove a non-existent element
        assertFalse(f.remove(F4));
    }
    
    /**
     * Can clear the array.
     */
    @Test
    public void testClear() {
        final FloatArray f = new FloatArray();
        f.clear();
        assertEquals(f.toArray(), new float[] {});
        assertEquals(f.size(), 0);
        f.add(F5, F2, F3, F1);
        f.clear();
        assertEquals(f.toArray(), new float[] {});
        assertEquals(f.size(), 0);
        f.clear();
        assertEquals(f.toArray(), new float[] {});
        assertEquals(f.size(), 0);
    }
    
    /**
     * Can get an Iterator of the FloatArray and iterate over it.
     */
    @Test
    public void testIterator() {
        final FloatArray f = new FloatArray();
        f.add(F5, F2, F3, F1);
        final FloatArray.Itr itr = f.iterator();
        for (int i = 0; i < f.size(); i++) {
            assertTrue(itr.hasNext());
            assertEquals(itr.next(), f.get(i));
        }
        assertFalse(itr.hasNext());
    }
    
    /**
     * An Exception is thrown when attempting to retrieve the next element from 
     * an iterator when there are no more elements.
     */
    @Test(expectedExceptions = {NoSuchElementException.class})
    public void testNextException() {
        new FloatArray().iterator().next();
    }
    
    /**
     * An Exception is thrown when attempting to remove an element from the
     * iterator.
     */
    @Test(expectedExceptions = {UnsupportedOperationException.class}, expectedExceptionsMessageRegExp = "Not supported.")
    public void testRemoveException() {
        new FloatArray().iterator().remove();
    }
    
    /**
     * Can get a String representation of a FloatArray.
     */
    @Test
    public void testToString() {
        final FloatArray f = new FloatArray();
        f.add(F5, F2, F3, F1);
        assertEquals(f.toString(), "F[" + F5 + "," + F2 + "," + F3 + "," + F1 + "]");
    }
}
