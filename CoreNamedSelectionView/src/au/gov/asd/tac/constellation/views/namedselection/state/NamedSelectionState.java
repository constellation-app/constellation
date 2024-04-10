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
package au.gov.asd.tac.constellation.views.namedselection.state;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.views.namedselection.NamedSelection;
import au.gov.asd.tac.constellation.views.namedselection.NamedSelectionManager;
import java.util.ArrayList;
import java.util.BitSet;
import org.openide.awt.UndoRedo;

/**
 * This class holds the state variables used by the NamedSelection services.
 * <p>
 * It also implements AbstractGraphIOProvider so it can read and write itself.
 * However, under the covers it passes these duties off to a helper class to
 * avoid the clutter here.
 *
 * @author betelgeuse
 */
public final class NamedSelectionState {

    /**
     * The name of the graph attribute used to hold the instance.
     */
    public static final String ATTRIBUTE_NAME = "named_selection_state";
    public static final String ATTR_DESC = "Named Selection State";
    private BitSet allocated;
    private boolean isDimOthers;
    private boolean isSelectResults;
    private final ArrayList<NamedSelection> namedSelections;

    /**
     * Construct a new NamedSelectionState.
     */
    public NamedSelectionState() {
        allocated = new BitSet(NamedSelectionManager.getMaximumSelectionCount());
        isDimOthers = false;
        isSelectResults = true;
        namedSelections = new ArrayList<>();
    }

    /**
     * Construct a new NamedSelectionState from an existing NamedSelectionState
     * using a deep copy.
     *
     * @param state The original NamedSelectionState.
     */
    public NamedSelectionState(final NamedSelectionState state) {
        allocated = new BitSet();
        allocated.or(state.allocated);
        isDimOthers = state.isDimOthers;
        isSelectResults = state.isSelectResults;
        namedSelections = new ArrayList<>();

        // Iterate over all namedSelections to deep copy all content:
        for (final NamedSelection selection : state.getNamedSelections()) {
            namedSelections.add(selection.clone());
        }
    }

    /**
     * Adds a <code>NamedSelection</code> to this NamedSelectionState.
     *
     * @param selection The <code>NamedSelection</code> to add.
     *
     * @see NamedSelection
     */
    public void addSelection(final NamedSelection selection) {
        namedSelections.add(selection);
        allocated.set(selection.getID());
    }

    /**
     * Adds a <code>NamedSelection</code> to this NamedSelectionState without
     * modifying the currently allocated selections.
     *
     * This is used for overwriting existing selections, such as renaming
     * selections, or adding descriptions to selections.
     *
     * @param selection The <code>NamedSelection</code> to add.
     *
     * @see NamedSelection
     */
    public void addSelectionFromGraph(final NamedSelection selection) {
        namedSelections.add(selection);
    }

    /**
     * Returns the total number of currently allocated
     * <code>NamedSelection</code>s.
     *
     * @return The number of currently in use selections.
     *
     * @see NamedSelection
     */
    public int getSelectionCount() {
        return allocated.cardinality();
    }

    /**
     * Returns a <code>BitSet</code> representing the current allocation of
     * <code>NamedSelection</code>s.
     *
     * Each set bit of the BitSet represents that the named selection slot is
     * currently in use.
     *
     * @return The current allocation of <code>NamedSelection</code>s.
     *
     * @see NamedSelection
     */
    public BitSet getCurrentlyAllocated() {
        return allocated;
    }

    /**
     * Sets a pre-allocated <code>BitSet</code> representing the known
     * allocation of selections.
     *
     * Would typically be used when opening a graph with a previous
     * NamedSelection state.
     *
     * @param allocated A prefilled <code>BitSet</code> representing the current
     * allocation of <code>NamedSelection</code>s.
     *
     * @see NamedSelection
     */
    public void setAllocatedFromGraph(final BitSet allocated) {
        this.allocated = allocated;
    }

    public ArrayList<NamedSelection> getNamedSelections() {
        return namedSelections;
    }

    /**
     * Returns the currently 'dim other' value, which is used to indicate
     * whether other graph items should be dimmed after performing a
     * NamedSelection operation.
     *
     * @return <code>true</code> indicating others should be dimmed.
     */
    public boolean isDimOthers() {
        return isDimOthers;
    }

    /**
     * Sets the 'dim other' value, which is used to indicate whether other graph
     * items should be dimmed after performing a NamedSelection operation.
     *
     * @param isDimOthers <code>true</code> to indicate that others should be
     * dimmed.
     */
    public void setDimOthers(final boolean isDimOthers) {
        this.isDimOthers = isDimOthers;
    }

    /**
     * Returns the currently 'select results' value, which is used to indicate
     * whether resulting elements should be selected after performing a
     * NamedSelection operation.
     *
     * @return <code>true</code> indicating results should be selected.
     */
    public boolean isSelectResults() {
        return isSelectResults;
    }

    /**
     * Sets the 'select results' value, which is used to indicate whether
     * resulting elements should be selected after performing a NamedSelection
     * operation.
     *
     * @param isSelectResults <code>true</code> to indicate that results should
     * be selected.
     */
    public void setSelectResults(final boolean isSelectResults) {
        this.isSelectResults = isSelectResults;
    }

    /**
     * Update the find state META attribute in an undoable way.
     * <p>
     * This calls the other update() with isSignificant=true.
     *
     * @param graph The graph to update.
     * @param undoRedoManager The UndoRedo.Manager for the graph.
     * @param stateAttr The attribute id of the find_state attribute.
     * @param name The name of the undoable edit.
     *
     * @throws java.lang.InterruptedException if the process was canceled during
     * execution.
     */
    public void update(final Graph graph, final UndoRedo.Manager undoRedoManager,
            final int stateAttr, final String name) throws InterruptedException {
        update(graph, undoRedoManager, stateAttr, name, false);
    }

    /**
     * Update a graph's find state meta-attribute with this instance in an
     * undoable way.
     * <p>
     * To make find state undoable, the graph must be updated within a
     * GraphUndoableEdit. This is a convenience method to do that.
     *
     * @param graph The graph to update.
     * @param undoRedoManager The UndoRedo.Manager for the graph.
     * @param stateAttr The attribute id of the find_state attribute.
     * @param name The name of the undoable edit.
     * @param isSignificant Is this a significant edit?
     *
     * @throws java.lang.InterruptedException if the process was canceled during
     * execution.
     */
    public void update(final Graph graph, final UndoRedo.Manager undoRedoManager, final int stateAttr,
            final String name, final boolean isSignificant) throws InterruptedException {
        WritableGraph wg = graph.getWritableGraph(name, isSignificant);
        try {
            wg.setObjectValue(stateAttr, 0, this);
        } finally {
            wg.commit();
        }
    }

    @Override
    public String toString() {
        return String.format("NamedSelectionState %s: contains %d NamedSelections", ((Object) this).hashCode(), namedSelections.size());
    }
}
