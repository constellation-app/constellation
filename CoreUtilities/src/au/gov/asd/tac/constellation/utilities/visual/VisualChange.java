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
package au.gov.asd.tac.constellation.utilities.visual;

import java.util.Arrays;

/**
 * Visual Change objects describe the change in a single visual property across
 * a data structure (typically a graph).
 * <p>
 * These objects are used by the {@link VisualManager} to relate information
 * about necessary visual updates to a {@link VisualProcessor}. This allows
 * significantly more efficient updating than requiring a
 * {@link VisualProcessor} to listen directly to the data it is visualising.
 * <p>
 * Clients that need to change visual properties of the graph and have a
 * {@link VisualProcessor} respond quickly to them should generate the relevant
 * VisualChange objects, wrap them in {@link VisualOperation} objects, and then
 * queue these operations with the {@link VisualManager}. Otherwise, changes to
 * visual properties will eventually picked up by the
 * {@link GraphChangeDetector}, but this is far too slow for some applications,
 * such as event handling. Creation of VisualChange objects is achieved via a
 * {@link VisualChangeBuilder} rather than through a constructor directly.
 * <p>
 * A VisualChange contains a single {@link VisualProperty} which has changed, an
 * id for de-duplication (when two changes stem from the one logical operation
 * and only one need be recognised), and either a size of the number of changed
 * items, or an explicit integer array of the items that have changed.
 * <p>
 * From the perspective of a {@link VisualProcessor} consuming VisualChange
 * objects, the typical pattern is to loop over the changed elements as follows:
 * <pre>
 * for (int i = 0; i &lt; change.getSize(); i++) {
 *     int changedIndex = change.getElement(i);
 *     // do something with changedIndex
 * }
 * </pre>
 *
 * @author twilight_sparkle
 */
public final class VisualChange implements Comparable<VisualChange> {

    private static int globalOrder = 0;

    private final int order;
    public final long id;
    public final VisualProperty property;
    private final int[] changeList;
    private final int changeListSize;

    /**
     * Construct a new VisualChange with the desired property and change
     * information
     *
     * @param property The {@link VisualProperty} that has changed.
     * @param changeList An array listing integer indexes for the items involved
     * in this change. If this is null, then the total number of changed items
     * should instead be specified in the next argument. In this case the
     * changed elements list consists of the numbers from <code>0</code> through
     * to <code>numChangedItems - 1</code>.
     * @param numChangedItems The number of items involved in this changed; if
     * changeList is non-null this argument is ignored.
     * @param id An ID relating this change to the process that generated it.
     */
    protected VisualChange(final VisualProperty property, final int[] changeList, final int numChangedItems, final long id) {
        this.property = property;
        this.changeList = changeList;
        this.changeListSize = changeList == null ? numChangedItems : changeList.length;
        this.id = id;
        order = globalOrder++;
    }

    /**
     * Get the number of elements that have been changed.
     *
     * @return The number of elements in this change set.
     */
    public int getSize() {
        return changeListSize;
    }
    
    protected int getOrder() {
        return order;
    }
    
    /**
     * Get the change list
     * 
     * @return The list of changes
     */
    protected int[] getChangeList() {
        return changeList != null ? changeList.clone() : null;
    }

    /**
     * Compares the changeList of two VisualChanges to see if they are the same.
     * If both changeLists are null then returns true. Otherwise both
     * changeLists must have the same elements in the same order to be
     * considered equal.
     *
     * @param other
     * @return boolean indicating whether changeLists of VisualChanges are
     * equal.
     */
    public boolean hasSameChangeList(final VisualChange other) {
        if (other == null) {
            return false;
        }
        if (changeList == null) {
            return other.changeList == null;
        }
        return Arrays.equals(changeList, other.changeList);
    }

    /**
     * Get the index of the element that has been changed at the specified
     * position in this change list.
     * 
     * Position will be returned as default when the position does not fall within
     * the array bounds. 
     *
     * @param position A position in this change list between <code>0</code> and
     * <code>getSize()-1</code>
     * @return The index of the changed element.
     */
    public int getElement(final int position) {
        return changeList == null || position < 0 || position > changeListSize -1 ? position : changeList[position];
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof VisualChange visualChangeObj && id == visualChangeObj.id;
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public int compareTo(final VisualChange o) {
        if (o == null) {
            return -1;
        }
        return id == o.id ? 0 : Integer.compare(order, o.order);
    }
}
