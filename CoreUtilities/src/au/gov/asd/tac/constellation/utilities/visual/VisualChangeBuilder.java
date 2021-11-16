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
package au.gov.asd.tac.constellation.utilities.visual;

/**
 * This class acts as a builder which allows {@link VisualChange} objects to be
 * created. It should be used by components needing to inform a
 * {@link VisualProcessor} of changing visual information.
 *
 * @author twilight_sparkle
 */
public final class VisualChangeBuilder {

    private static long currentId = 0;

    private final VisualProperty property;
    private Long id = null;
    private int[] changeList = null;
    private int numChangedItems = -1;

    /**
     * Provides a thread safe way to generate unique, reusable IDs for
     * {@link VisualChange} objects that they wish to create.
     *
     * @return A unique ID that can be used to build {@link VisualChange}
     * objects.
     */
    public static final synchronized long generateNewId() {
        return currentId++;
    }
    
    /**
     * Create a new VisualChangeBuilder to build a {@link VisualChange} for the
     * specified {@link VisualProperty}.
     *
     * @param property The {@link VisualProperty} that has changed.
     */
    public VisualChangeBuilder(final VisualProperty property) {
        this.property = property;
    }

    /**
     * Set an ID for the visual change that will be built. The provided ID
     * should be one that was returned from
     * {@link #generateNewId generateNewId()}
     * <p>
     * This allows for de-duplication of changes that are being generated
     * rapidly from the same source. If multiple {@link VisualChange} objects
     * having the same ID are received by the {@link VisualManager} (in the same
     * update cycle), only the visual change which was created last will be
     * honoured.
     *
     * @param id An ID for the change being built.
     * @return
     */
    public VisualChangeBuilder withId(final long id) {
        this.id = id;
        return this;
    }
    
    /**
     * Get the Id of the currently built VisualChange. Added for testing
     * 
     * @return the current Id as a Long
     */
    protected long getId() {
        return this.id;
    }

    /**
     * Specify the total number of items that have changed. This will result in
     * the creation of a change with change-set consisting of items with indices
     * <code>0</code> through <code>numChangedItems - 1</code>.
     * <p>
     * This is most commonly used when all of the items pertaining to the
     * relevant {@link VisualPropety} have changed. Its effect is overridden by
     * the {@link #forItems(int[]) forItems(int[])} method.
     *
     * @param numChangedItems The total number of items that have changed.
     * @return
     */
    public VisualChangeBuilder forItems(final int numChangedItems) {
        this.numChangedItems = numChangedItems;
        return this;
    }
    
    /**
     * Get the numChangedItems of the currently built VisualChange. Added for testing
     * 
     * @return the current numChangedItems as an int
     */
    protected int getChangedItemsCount() {
        return this.numChangedItems;
    }

    /**
     * Specify the indices individually of the items that have changed.
     * <p>
     * This is most commonly used when some but not all items pertaining to the
     * relevant {@link VisualProperty} have changed. Its effect overrides that
     * of the {@link #forItems(int) forItems(int)} method.
     *
     * @param changeList The
     * @return
     */
    public VisualChangeBuilder forItems(final int[] changeList) {
        this.changeList = changeList;
        return this;
    }
    
    /**
     * Get the changeList of the currently built VisualChange. Added for testing
     * 
     * @return the current changeList int[]
     */
    protected int[] getChangedItems() {
        return this.changeList != null ? this.changeList.clone() : null;
    }

    /**
     * Build and return the VisualChange as specified by this builder.
     * <p>
     * If neither <code>forItems</code> method has been called on this builder,
     * the created {@link VisualChange} can not be used to iterate over changed
     * elements. This should only be used for {@link VisualProperty} constants
     * that represent changes that affect the data as a whole, i.e. those with
     * element types <code>GRAPH</code> or <code>NONE</code>.
     * <p>
     * If an ID has not been specified using {@link #withId withId(long)}, then
     * a new unique, one-time ID is generated for this change (which will never
     * be used again).
     *
     * @return A {@link VisualChange} object as specified by this builder.
     */
    public VisualChange build() {
        return new VisualChange(property, changeList, numChangedItems, id == null ? generateNewId() : id);
    }
}
