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
package au.gov.asd.tac.constellation.utilities.graphics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author algol
 */
/**
 * Resizable-array implementation of int[]. This uses less storage than an
 * ArrayList&lt;Integer&gt;.
 *
 * The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, <tt>set</tt>,
 * <tt>iterator</tt>, and <tt>listIterator</tt> operations run in constant time.
 * The <tt>add</tt> operation runs in <i>amortized constant time</i>, that is,
 * adding n elements requires O(n) time. All of the other operations run in
 * linear time (roughly speaking). The constant factor is low compared to that
 * for the <tt>LinkedList</tt> implementation.<p>
 *
 * Each <tt>ArrayList</tt> instance has a <i>capacity</i>. The capacity is the
 * size of the array used to store the elements in the list. It is always at
 * least as large as the list size. As elements are added to an ArrayList, its
 * capacity grows automatically. The details of the growth policy are not
 * specified beyond the fact that adding an element has constant amortized time
 * cost.<p>
 *
 * An application can increase the capacity of an <tt>ArrayList</tt> instance
 * before adding a large number of elements using the <tt>ensureCapacity</tt>
 * operation. This may reduce the amount of incremental reallocation.
 *
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access an <tt>ArrayList</tt> instance concurrently, and
 * at least one of the threads modifies the list structurally, it
 * <i>must</i> be synchronized externally. (A structural modification is any
 * operation that adds or deletes one or more elements, or explicitly resizes
 * the backing array; merely setting the value of an element is not a structural
 * modification.) This is typically accomplished by synchronizing on some object
 * that naturally encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the
 * {@link Collections#synchronizedList Collections.synchronizedList} method.
 * This is best done at creation time, to prevent accidental unsynchronized
 * access to the list:
 * <pre>
 *   List list = Collections.synchronizedList(new ArrayList(...));</pre>
 *
 * <p>
 * The iterators returned by this class's <tt>iterator</tt> and
 * <tt>listIterator</tt> methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in any way
 * except through the iterator's own <tt>remove</tt> or <tt>add</tt> methods,
 * the iterator will throw a {@link ConcurrentModificationException}. Thus, in
 * the face of concurrent modification, the iterator fails quickly and cleanly,
 * rather than risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 *
 */
public final class IntArray implements Iterable<Integer>, Cloneable {

    private static final long serialVersionUID = 8683452581122332189L;
    /**
     * The array buffer into which the elements of the ArrayList are stored. The
     * capacity of the ArrayList is the length of this array buffer.
     */
    private int[] elementData;
    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * @serial
     */
    private int size;

    /**
     * The number of times this list has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the list, or
     * otherwise perturb it in such a fashion that iterations in progress may
     * yield incorrect results.
     *
     * <p>
     * This field is used by the iterator and list iterator implementation
     * returned by the {@code iterator} and {@code listIterator} methods. If the
     * value of this field changes unexpectedly, the iterator (or list iterator)
     * will throw a {@code ConcurrentModificationException} in response to the
     * {@code next}, {@code remove}, {@code previous}, {@code set} or
     * {@code add} operations. This provides
     * <i>fail-fast</i> behavior, rather than non-deterministic behavior in the
     * face of concurrent modification during iteration.
     *
     * <p>
     * <b>Use of this field by subclasses is optional.</b> If a subclass wishes
     * to provide fail-fast iterators (and list iterators), then it merely has
     * to increment this field in its {@code add(int, E)} and
     * {@code remove(int)} methods (and any other methods that it overrides that
     * result in structural modifications to the list). A single call to
     * {@code add(int, E)} or {@code remove(int)} must add no more than one to
     * this field, or the iterators (and list iterators) will throw bogus
     * {@code ConcurrentModificationExceptions}. If an implementation does not
     * wish to provide fail-fast iterators, this field may be ignored.
     */
    private int modCount = 0;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @exception IllegalArgumentException if the specified initial capacity is
     * negative
     */
    public IntArray(final int initialCapacity) {
        super();

        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }

        this.elementData = new int[initialCapacity];
        size = 0;
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public IntArray() {
        this(10);
    }

    public IntArray(final IntArray other) {
        size = other.size;
        elementData = Arrays.copyOf(other.elementData, size);
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the list's
     * current size. An application can use this operation to minimize the
     * storage of an <tt>ArrayList</tt> instance.
     */
    public void trimToSize() {
        modCount++;
        final int oldCapacity = elementData.length;
        if (size < oldCapacity) {
            elementData = Arrays.copyOf(elementData, size);
        }
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if necessary,
     * to ensure that it can hold at least the number of elements specified by
     * the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    public void ensureCapacity(final int minCapacity) {
        modCount++;
        final int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            // minCapacity is usually close to size, so this is a win:
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    /**
     * Increases the size of this array instance to the specified size. If the
     * current size is greater than or equal to the required size, nothing
     * happens.
     *
     * Any added array elements are set to the specified value.
     *
     * @param size The size
     * @param value The value to set any new elements to.
     */
    public void ensureSize(final int size, final int value) {
        if (this.size < size) {
            modCount++;
            final int currentSize = this.size;
            ensureCapacity(size);
            this.size = size;

            Arrays.fill(elementData, currentSize, size, value);
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element. More
     * formally, returns <tt>true</tt> if and only if this list contains at
     * least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    public boolean contains(final int o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element in
     * this list, or -1 if this list does not contain the element. More
     * formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o the element to search for.
     * @return the index of the first occurrence of the specified element in
     * this list, or -1 if this list does not contain the element.
     */
    public int indexOf(final int o) {
        for (int i = 0; i < size; i++) {
            if (elementData[i] == o) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance. (The elements
     * themselves are not copied.)
     *
     * @return a clone of this <tt>ArrayList</tt> instance
     */
    @Override
    public IntArray clone() {
        try {
            final IntArray v = (IntArray) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.size = size;
            v.modCount = 0;
            return v;
        } catch (final CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element).
     *
     * <p>
     * The returned array will be "safe" in that no references to it are
     * maintained by this list. (In other words, this method must allocate a new
     * array). The caller is thus free to modify the returned array.
     *
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     *
     * @return an array containing all of the elements in this list in proper
     * sequence
     */
    public int[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    /**
     * Return the array containing the data elements.
     * <p>
     * The returned array is the actual array used to store data. Therefore, any
     * modifications made by the caller are at the caller's risk.
     * <p>
     * This is faster than toArray() since a reference to the raw array is
     * returned, rather than a reference to a copy.
     * <p>
     * Due to pre-allocation by ensureCapacity(), the size of the returned array
     * may be greater than size().
     *
     * @return The raw array used to store data.
     */
    public int[] rawArray() {
        trimToSize();
        return elementData;
    }

    // Positional Access Operations
    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     */
    public int get(final int index) {
        rangeCheck(index);

        return elementData[index];
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     */
    public int set(final int index, final int element) {
        rangeCheck(index);

        final int oldValue = elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(final int e) {
        ensureCapacity(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    public void add(final int index, final int element) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        ensureCapacity(size + 1);  // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    /**
     * Appends the specified elements to the end of this list.
     *
     * @param i0 the first element.
     * @param i1 the second element.
     * @param i2 the third element.
     * @param i3 the fourth element.
     *
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(final int i0, final int i1, final int i2, final int i3) {
        ensureCapacity(size + 4);  // Increments modCount!!
        elementData[size++] = i0;
        elementData[size++] = i1;
        elementData[size++] = i2;
        elementData[size++] = i3;

        return true;
    }

    /**
     * Removes the element at the specified position in this list. Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     */
    public int removeAt(final int index) {
        rangeCheck(index);

        modCount++;
        final int oldValue = elementData[index];

        final int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        }
        --size;

        return oldValue;
    }

    /**
     * Removes the first occurrence of the specified element from this list, if
     * it is present. If the list does not contain the element, it is unchanged.
     * More formally, removes the element with the lowest index
     * <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists). Returns <tt>true</tt> if this list contained
     * the specified element (or equivalently, if this list changed as a result
     * of the call).
     *
     * @param o element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean remove(final int o) {
        for (int index = 0; index < size; index++) {
            if (o == elementData[index]) {
                fastRemove(index);
                return true;
            }
        }

        return false;
    }

    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
    private void fastRemove(final int index) {
        modCount++;
        final int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        }
        --size;
    }

    /**
     * Removes all of the elements from this list. The list will be empty after
     * this call returns.
     */
    public void clear() {
        modCount++;
        size = 0;
    }

    /**
     * Appends all of the elements in the specified IntArray to the end of this
     * list, in the order that they are returned by the specified IntArray's
     * Iterator. The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress. (This implies
     * that the behavior of this call is undefined if the specified IntArray is
     * this one, and this list is nonempty.)
     *
     * @param c IntArray containing elements to be added to this list
     * @return <tt>true</tt> if this array changed as a result of the call
     * @throws NullPointerException if the specified IntArray is null
     */
    public boolean addAll(final IntArray c) {
        final int numNew = c.size();
        ensureCapacity(size + numNew);
        System.arraycopy(c.elementData, 0, elementData, size, numNew);
        size += numNew;

        return numNew != 0;
    }

    /**
     * Checks if the given index is in range. If not, throws an appropriate
     * runtime exception. This method does *not* check if the index is negative:
     * It is always used immediately prior to an array access, which throws an
     * ArrayIndexOutOfBoundsException if index is negative.
     */
    private void rangeCheck(final int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public Itr iterator() {
        return new Itr();
    }

    /**
     * An iterator over the elements of an IntArray.
     */
    public class Itr implements Iterator<Integer> {

        final int size;
        int cursor;

        Itr() {
            cursor = 0;
            size = size();
        }

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return elementData[cursor++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (b.length() > 0) {
                b.append(',');
            }
            b.append(String.valueOf(elementData[i]));
        }

        return String.format("I[%s]", b.toString());
    }
}
