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

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A ListStore manages a collection of element ids in a collection of lists.
 * With in each list, each element is allocated to a specific category.
 *
 * This is used in StoreGraph to associate elements of one type with elements of
 * another type, such as transactions to the links that hold them. In this
 * example, each list represents the transactions in a particular link, with the
 * categories representing the directions of the transaction.
 *
 * @author sirius
 */
public class ListStore implements Serializable {

    private static final int CATEGORY_SHIFT = 29;
    private static final int POSITION_MASK = 0x1FFFFFFF;
    private static final int[] EMPTY_ARRAY = new int[0];

    private final int categories;
    private final int countLength;
    private int listCapacity;
    private int elementCapacity;
    private int[] categoryCounts;
    private int[][] elements;
    private int[] elementLists;
    private int[] elementPositions;

    public ListStore(final int categories, final int listCapacity, final int elementCapacity) {
        this.categories = categories;
        this.countLength = categories + 1;
        this.listCapacity = listCapacity;
        this.elementCapacity = elementCapacity;

        this.categoryCounts = new int[(categories + 1) * listCapacity];

        this.elements = new int[listCapacity][];
        Arrays.fill(elements, EMPTY_ARRAY);

        this.elementLists = new int[elementCapacity];
        Arrays.fill(elementLists, -1);

        this.elementPositions = new int[elementCapacity];
    }

    public ListStore(final ListStore original) {
        this.categories = original.categories;
        this.countLength = original.countLength;
        this.listCapacity = original.listCapacity;
        this.elementCapacity = original.elementCapacity;

        this.categoryCounts = Arrays.copyOf(original.categoryCounts, original.categoryCounts.length);

        this.elements = new int[original.elements.length][];
        for (int i = 0; i < this.elements.length; i++) {
            if (original.elements[i] != null) {
                this.elements[i] = Arrays.copyOf(original.elements[i], original.elements[i].length);
            }
        }

        this.elementLists = Arrays.copyOf(original.elementLists, original.elementLists.length);
        this.elementPositions = Arrays.copyOf(original.elementPositions, original.elementPositions.length);
    }

    public void addElement(final int list, final int element, final int category) {
        int[] l = elements[list];
        final int base = countLength * list + category;
        int currentCategory = categories - category;

        // Create a space at the end of the list entire list
        int space = categoryCounts[currentCategory + base]++;

        // If the space is past the end of the array then expand the array
        if (space >= l.length) {
            l = elements[list] = Arrays.copyOf(l, l.length == 0 ? 1 : (l.length << 1));
        }

        while (--currentCategory > 0) {
            // Find the first element in the current category
            final int firstPosition = categoryCounts[currentCategory + base]++;

            if (firstPosition < space) {
                final int firstElement = l[firstPosition];

                // Move the first element into the space
                l[space] = firstElement;
                elementPositions[firstElement] = space | ((category + currentCategory) << CATEGORY_SHIFT);

                // The space has moved to the first position in the current category
                space = firstPosition;
            }
        }

        l[space] = element;
        elementPositions[element] = space | (category << CATEGORY_SHIFT);
        elementLists[element] = list;
    }

    public void removeElement(final int element) {
        final int list = elementLists[element];
        int category = (elementPositions[element] >>> CATEGORY_SHIFT) - 1;
        int space = elementPositions[element] & POSITION_MASK;

        final int base = countLength * list;

        final int[] l = elements[list];

        // Mark the element as removed from the list
        elementLists[element] = -1;

        while (++category < categories) {
            final int newSpace = --categoryCounts[category + base + 1];

            if (newSpace > space) {
                final int id = l[newSpace];
                l[space] = id;
                elementPositions[id] = space | (category << CATEGORY_SHIFT);

                space = newSpace;
            }
        }
    }

    public boolean elementExists(final int element) {
        return element >= 0 && element < elementCapacity && elementLists[element] >= 0;
    }

    public int getElementList(final int element) {
        return elementLists[element];
    }

    public int getElementCategory(final int element) {
        return elementPositions[element] >>> CATEGORY_SHIFT;
    }

    public void expandElementCapacity(final int capacity) {
        elementLists = Arrays.copyOf(elementLists, capacity);
        Arrays.fill(elementLists, elementCapacity, capacity, -1);

        elementPositions = Arrays.copyOf(elementPositions, capacity);

        elementCapacity = capacity;
    }

    public void expandListCapacity(final int capacity) {
        categoryCounts = Arrays.copyOf(categoryCounts, capacity * countLength);

        elements = Arrays.copyOf(elements, capacity);
        Arrays.fill(elements, listCapacity, capacity, EMPTY_ARRAY);

        listCapacity = capacity;
    }

    public int getElementCount(final int list) {
        return categoryCounts[list * countLength + categories];
    }

    public int getElementCount(final int list, final int category) {
        final int base = list * countLength;
        return categoryCounts[base + category + 1] - categoryCounts[base + category];
    }

    public int getElement(final int list, final int position) {
        return elements[list][position];
    }

    public int getElement(final int list, final int category, final int position) {
        return elements[list][categoryCounts[list * countLength + category] + position];
    }

    public String toString(final int list) {
        final StringBuilder out = new StringBuilder();

        for (int category = 0; category < categories; category++) {
            out.append(category);
            out.append('(');
            out.append(getElementCount(list, category));
            out.append("):");
            for (int position = 0; position < getElementCount(list, category); position++) {
                final int element = getElement(list, category, position);
                out.append(" ");
                out.append(element);
                out.append('(');
                out.append(getElementCategory(element));
                out.append(')');
            }
            out.append(SeparatorConstants.NEWLINE);
        }

        return out.toString();
    }
}
