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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;

/**
 * A Bin represents a single bar in the histogram view. It holds various
 * information needed to display the histogram including how many elements are
 * currently in the bin, how many elements are currently selected, and a linked
 * list of the elements in the bin. If the histogram is required to display new
 * attribute types that are not provided for by the provided types, this class
 * should be extended to provide the needed functionality.
 *
 * @author sirius
 */
public abstract class Bin implements Comparable<Bin> {

    // The number of elements in this bin
    int elementCount = 0;

    // The number of elements in this bin that are currently selected
    int selectedCount = 0;

    // A place to hold a saved selection count value
    // This is used to hold the original selected count value when the user
    // selects this bin by dragging. The user can reduce the size
    // of the dragged area meaning that this bin is no longer selected.
    // If this happens then we need to be able to reinstate the original selected
    // count for this bin.
    int savedSelectedCount = 0;

    // A pointer to the first element in this bin
    int firstElement = -1;

    // Is the bin activated? A bin becomes activated when the user uses shift-click to
    // select a range of bins. The start of this range (selected in the previous click)
    // is the activated bin.
    boolean activated = false;

    // A place to hold a saved activated value
    // This is used to hold the original activated value when the user
    // performs a dragging gesture. In this case, the activation value
    // may need to be hidden but then restored if the user reduces the
    // size of the drag.
    boolean savedActivated = false;

    // The label for this bin that is displayed to the user.
    protected String label;

    // Do not create a bin if all the elements are null
    boolean allElementsAreNull = false;

    public boolean isAllElementsAreNull() {
        return allElementsAreNull;
    }

    public void setAllElementsAreNull(boolean allElementsNull) {
        this.allElementsAreNull = allElementsNull;
    }
    
    /**
     * Returns the label.
     *
     * @return the label.
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label == null ? "<null>" : label;
    }

    /**
     * Returns true if this bin represents a null value. Each collection of bins
     * should only have one null bin.
     *
     * @return true if this bin represents a null value.
     */
    public boolean isNull() {
        return false;
    }

    /**
     * Called by the histogram framework to allow this bin to perform any
     * initialisation. This is useful for calculations that only need to be
     * performed once in the creation of a histogram, rather than those that
     * need to be performed for each data point. This could include things like
     * calculation of a time zone for datetime attributes.
     *
     * @param graph the graph that is providing the data to the histogram.
     * @param attributeId the id of the attribute.
     */
    public void init(GraphReadMethods graph, final int attributeId) {
    }

    /**
     * Called by the histogram framework to allow the bin to update its key and
     * label from its native data type. This can often be a relatively expensive
     * operation so the framework will only call this method once when the bin
     * is created. In general, the bin should function based purely on its
     * native data values.
     */
    public abstract void prepareForPresentation();

    /**
     * Called by the histogram framework to allow the bin to sets its key from a
     * graph, given an attribute and an element id.
     *
     * @param graph the graph providing data to the histogram.
     * @param attribute the attribute being visualised.
     * @param element the element that is providing the data point.
     */
    public abstract void setKey(GraphReadMethods graph, int attribute, int element);

    /**
     * Creates a new histogram bin of the same type of this bin. This method is
     * used by the framework to create new bins as new bin values are
     * discovered.
     *
     * @return a new histogram bin of the same type of this bin.
     */
    public abstract Bin create();

    /**
     * Returns an object representation of this bin's key value. This is useful
     * as bins often store their values as primitives for performance reasons.
     *
     * @return an object representation of this bin's key value.
     */
    public abstract Object getKeyAsObject();
}
