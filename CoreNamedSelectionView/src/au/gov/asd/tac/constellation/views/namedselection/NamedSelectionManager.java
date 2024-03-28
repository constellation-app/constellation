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
package au.gov.asd.tac.constellation.views.namedselection;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.namedselection.panes.NamedSelectionAllAllocPanel;
import au.gov.asd.tac.constellation.views.namedselection.panes.NamedSelectionProtectedPanel;
import au.gov.asd.tac.constellation.views.namedselection.state.NamedSelectionState;
import au.gov.asd.tac.constellation.views.namedselection.state.NamedSelectionStatePlugin;
import au.gov.asd.tac.constellation.views.namedselection.utilities.NamedSelectionEditorPlugin;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * The <code>NamedSelectionManager</code> coordinates the named selection
 * functionality for CONSTELLATION.
 * <p>
 * The manager, firstly, acts as a listener for user interaction and graph
 * structure changes that impact on named selections, and pushes these changes
 * (if pertinent) to the named selection browser. Examples of what will be
 * listened for are moving to/from graphs, undo/redo-ing on graphs, and changes
 * to the <code>NamedSelectionState</code>.
 * <p>
 * The manager is also responsible for managing user driven requests for named
 * selection operations such as execution of unions and intersections on given
 * named selections, and recalling and creation / modification of
 * <code>NamedSelection</code>s.
 * <p>
 * The manager is implemented as a singleton to allow for application wide
 * knowledge of named selections, and ability to act with in independently of
 * <code>NamedSelectionTopComponent</code> and
 * <code>NamedSelectionShortcuts</code>.
 *
 * @see NamedSelectionState
 * @see NamedSelectionTopComponent
 * @see NamedSelectionShortcuts
 *
 * @author betelgeuse
 */
@Messages({
    "ProtectedSelection=Unable to Perform Modification",
    "AllAllocated=All Named Selection Slots Have Been Used"
})
public class NamedSelectionManager implements LookupListener, GraphChangeListener {
    
    private static final Logger LOGGER = Logger.getLogger(NamedSelectionManager.class.getName());

    private static final int NO_AVAILABLE = -1;
    private static final int CURRENT_SELECTION = NO_AVAILABLE;
    private static final int MAXIMUM_SELECTION_COUNT = 63;
    private static final int START_INDEX = 0;
    private NamedSelectionState state = new NamedSelectionState();
    private long valueModificationCounter;
    private GraphNode graphNode = null;
    private final Lookup.Result<GraphNode> result;

    /**
     * Gets the global <code>NamedSelectionManager</code>.
     * <p>
     * Accesses the singleton access. Uses the NetBeans 'getDefault' rather than
     * the more general 'getInstance' as used in singletons.
     *
     * @return The <code>NamedSelectionManager</code> singleton.
     */
    public static NamedSelectionManager getDefault() {
        return NSMSingletonHolder.INSTANCE;
    }

    /**
     * Constructs a new <code>NamedSelectionManager</code>.
     * <p>
     * Private constructor only for use in creation of singleton.
     */
    private NamedSelectionManager() {
        // Attach listener that determines the active graphnode:
        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        result.addLookupListener(this);

        // Manually trigger an event on creation to pick up the current graphnode initially.
        resultChanged(null);
    }

    /**
     * Private class that holds the instance of the
     * <code>NamedSelectionManager</code> for use in the singleton pattern.
     *
     * @see NamedSelectionManager
     */
    private static final class NSMSingletonHolder {

        static final NamedSelectionManager INSTANCE = new NamedSelectionManager();
    }

    /**
     * Either creates a new named selection that includes all currently selected
     * graph elements, and saves the corresponding hotkey to it, or overwrites a
     * currently existing named selection already assigned to the hotkey.
     * <p>
     * This method handles all underlying updates of the named selection state
     * as well.
     *
     * @param hotkey the hotkey that will activate the named selection.
     */
    public void createNamedSelectionFromHotkey(final String hotkey) {
        if (graphNode != null) {
            final Graph graph = graphNode.getGraph();

            if (graph != null) {
                // Check if the hotkey already exists:
                NamedSelection existing = null;
                Iterator<NamedSelection> itr = state.getNamedSelections().listIterator();

                while (itr.hasNext()) {
                    final NamedSelection selection = itr.next();

                    if (hotkey.equals(selection.getHotkey())) {
                        // Recall the selection from the graph elements:
                        existing = selection;
                        break;
                    }
                }

                // If it exists, overwrite it, otherwise check if there is space for a new named selection.
                if (existing != null) {
                    if (!existing.isLocked()) {
                        performSave(graph, existing.getID());
                    } else {
                        notifyProtected(existing.getName());
                    }
                } else {
                    if (getAvailableSelectionCount() > 0) {
                        final NamedSelection newSelection = new NamedSelection(getAvailableSelection(), hotkey);

                        state.addSelection(newSelection);

                        // Save the selection to the graph elements:
                        performSave(graph, newSelection.getID());

                        // Write this to the state
                        saveStateToGraph();
                    } else {
                        notifyAllAlloc();
                    }
                }

                updateState(true);
            }
        }
    }

    /**
     * Creates a new named selection that includes all currently selected graph
     * elements.
     * <p>
     * This method handles all underlying updates of the named selection state
     * as well.
     */
    public void createNamedSelection() {
        if (graphNode != null) {
            if (getAvailableSelectionCount() > 0) {
                NamedSelection newSelection = new NamedSelection(getAvailableSelection());

                state.addSelection(newSelection);

                // Save the selection to the graph elements:
                performSave(graphNode.getGraph(), newSelection.getID());

                // Write this to the state
                saveStateToGraph();

                updateState();
            } else {
                notifyAllAlloc();
            }
        }
    }

    /**
     * Creates a new custom named selection that includes the supplied graph
     * elements.
     * <p>
     * This method handles all underlying updates of the named selection state
     * as well.
     *
     * @param nodesToSave the nodes to save in the named selection.
     * @param transactionsToSave the transactions to save in the named
     * selection.
     *
     * @return if the save was successful.
     */
    public boolean createCustomNamedSelection(int[] nodesToSave, int[] transactionsToSave) {
        return createCustomNamedSelection(nodesToSave, transactionsToSave, true);
    }

    public boolean createCustomNamedSelection(int[] nodesToSave, int[] transactionsToSave, boolean notifyIfFail) {
        return createCustomNamedSelection(nodesToSave, transactionsToSave, notifyIfFail, null);
    }

    public boolean createCustomNamedSelection(int[] nodesToSave, int[] transactionsToSave, boolean notifyIfFail, String selectionName) {
        if (graphNode != null) {
            if (getAvailableSelectionCount() > 0) {
                NamedSelection newSelection = new NamedSelection(getAvailableSelection());
                if (selectionName != null) {
                    newSelection.setName(selectionName);
                }
                state.addSelection(newSelection);

                // Save the selection to the graph elements:
                performCustomSave(graphNode.getGraph(), nodesToSave, transactionsToSave, newSelection.getID());

                // Write this to the state
                saveStateToGraph();

                updateState();
                return true;
            } else if (notifyIfFail) {
                notifyAllAlloc();
            } else {
                // Do nothing
            }
        }
        return false;
    }

    /**
     * Overwrites an existing named selection with the currently selected graph
     * elements.
     *
     * @param current The <code>NamedSelection</code> to be overwritten.
     *
     * @see NamedSelection
     */
    public void overwriteNamedSelection(final NamedSelection current) {
        if (graphNode != null) {
            if (!current.isLocked()) {
                performSave(graphNode.getGraph(), current.getID());
            } else {
                notifyProtected(current.getName());
            }
        }
    }

    /**
     * Clones an existing named selection to a new named selection.
     * <p>
     * This method handles all underlying updates of the named selection state
     * as well.
     *
     * @param selection the named selection to be cloned.
     */
    public void cloneNamedSelection(final NamedSelection selection) {
        if (graphNode != null) {
            final Graph graph = graphNode.getGraph();

            if (getAvailableSelectionCount() > 0) {
                NamedSelection clonedSelection = new NamedSelection(getAvailableSelection());

                clonedSelection.setName(selection.getName() + " (Copy)");

                // We will need to recall the selection we are cloning so that we can save the elements:
                performRecall(graph, true, false, selection.getID());

                state.addSelection(clonedSelection);

                // Save the selection to the graph elements:
                performSave(graph, clonedSelection.getID());

                // Recall the newly saved selection with the dim / select states being honoured:
                performRecall(graph, state.isSelectResults(), state.isDimOthers(), clonedSelection.getID());

                // Write this to the state
                saveStateToGraph();

                updateState();
            } else {
                notifyAllAlloc();
            }
        }
    }

    /**
     * Recalls the named selection that correlates to the given hotkey.
     * <p>
     * This method honours the named selection display settings.
     *
     * @param hotkey The hotkey of the named selection to be recalled.
     */
    public void recallSelectionFromHotkey(final String hotkey) {
        if (graphNode != null) {
            Iterator<NamedSelection> itr = state.getNamedSelections().listIterator();

            while (itr.hasNext()) {
                final NamedSelection selection = itr.next();

                if (hotkey.equals(selection.getHotkey())) {
                    // Recall the selection from the graph elements:
                    performRecall(graphNode.getGraph(), state.isSelectResults(), state.isDimOthers(), selection.getID());
                    break;
                }
            }
        }
    }

    /**
     * Recalls the given named selection to the graph.
     * <p>
     * This method honours the named selection display settings.
     *
     * @param recall The named selection to be recalled to the graph.
     *
     * @see NamedSelection
     */
    public void recallSelection(final NamedSelection recall) {
        if (graphNode != null) {
            // Recall the selection from the graph elements:
            performRecall(graphNode.getGraph(), state.isSelectResults(), state.isDimOthers(), recall.getID());
        }
    }

    /**
     * Recalls a named selection, and automatically dims non-member graph
     * elements.
     * <p>
     * Member elements do not have their 'selected' attribute set, so the
     * elements retain their original non-selected colors.
     *
     * @param notDim The named selection to be recalled.
     *
     * @see NamedSelection
     */
    public void dimOtherThanSelection(final NamedSelection notDim) {
        if (graphNode != null) {
            // Recall the notDim selection, with options to not select, and dim others:
            performRecall(graphNode.getGraph(), false, true, notDim.getID());
        }
    }

    /**
     * Resets a graph, and automatically un-dims all member graph elements.
     * <p>
     * Member elements do not have their 'selected' attribute set, so all
     * elements retain their original non-selected colors.
     *
     * @param reset The named selection to be reset.
     *
     * @see NamedSelection
     */
    public void resetSelection(final NamedSelection reset) {
        if (graphNode != null) {
            // Recall the graph selection, with options to not select or dim others:
            performRecall(graphNode.getGraph(), false, false, reset.getID());
        }
    }

    /**
     * Requests a union on the given named selections.
     * <p>
     * Results of the union are reflected on the graph.
     *
     * @param selections <code>ArrayList&lt;NamedSelection&gt;</code> of
     * selections to be included in the union.
     *
     * @see ArrayList
     * @see NamedSelection
     */
    public void performUnion(final ArrayList<NamedSelection> selections) {
        int[] ids = new int[selections.size()];

        boolean useCurrent = false;
        int i = 0;
        for (final NamedSelection selection : selections) {
            if (selection.getID() == CURRENT_SELECTION) {
                useCurrent = true;
            }
            ids[i] = selection.getID();
            i++;
        }

        performUnion(graphNode.getGraph(), useCurrent, state.isSelectResults(), state.isDimOthers(), ids);
    }

    /**
     * Requests an intersection on the given named selections.
     * <p>
     * Results of the intersection are reflected on the graph.
     *
     * @param selections <code>ArrayList&lt;NamedSelection&gt;</code> of
     * selections to be checked for intersecting member elements.
     *
     * @see ArrayList
     * @see NamedSelection
     */
    public void performIntersection(final ArrayList<NamedSelection> selections) {
        int[] ids = new int[selections.size()];

        boolean useCurrent = false;
        int i = 0;
        for (final NamedSelection selection : selections) {
            if (selection.getID() == CURRENT_SELECTION) {
                useCurrent = true;
            }
            ids[i] = selection.getID();
            i++;
        }

        performIntersection(graphNode.getGraph(), useCurrent, state.isSelectResults(), state.isDimOthers(), ids);
    }

    /**
     * Updates the name of the given named selection.
     *
     * @param selection The selection being updated.
     * @param newName The name being set on the given named selection.
     */
    public void renameNamedSelection(final NamedSelection selection, final String newName) {
        if (!selection.isLocked()) {
            final int index = state.getNamedSelections().indexOf(selection);

            state.getNamedSelections().get(index).setName(newName);

            // Write this to the state
            saveStateToGraph();

            updateState();
        } else {
            notifyProtected(selection.getName());
        }
    }

    /**
     * Updates the description of the given named selection.
     *
     * @param selection The selection being updated.
     * @param newDescription The description being set on the given named
     * selection.
     */
    public void setDescriptionNamedSelection(final NamedSelection selection, final String newDescription) {
        if (!selection.isLocked()) {
            final int index = state.getNamedSelections().indexOf(selection);

            state.getNamedSelections().get(index).setDescription(newDescription);

            // Write this to the state
            saveStateToGraph();

            updateState();
        } else {
            notifyProtected(selection.getName());
        }
    }

    /**
     * Changes the locked state for a given named selection.
     *
     * @param selection The <code>NamedSelection</code> to have the locked state
     * changed on.
     *
     * @see NamedSelection
     */
    public void toggleLockedNamedSelection(final NamedSelection selection) {
        final int index = state.getNamedSelections().indexOf(selection);

        state.getNamedSelections().get(index).setLocked(!selection.isLocked());

        // Write this to the state:
        saveStateToGraph();

        updateState();
    }

    /**
     * Looks up a vacant named selection saved spot, and returns the ID for use
     * in the creation of new named selections.
     *
     * @return An ID corresponding to a vacant position in the named selection
     * underlying structure.
     */
    public int getAvailableSelection() {
        final int index = state.getCurrentlyAllocated().nextClearBit(START_INDEX);

        // Check if we actually have a clear bit, and return the first available:
        if (index >= 0) {
            return index;
        }

        // return -1 if none available.
        return NO_AVAILABLE;
    }

    /**
     * Returns the current total of available named selection 'slots'.
     *
     * @return The total number of available named selection IDs.
     */
    public int getAvailableSelectionCount() {
        return (MAXIMUM_SELECTION_COUNT + 1) - state.getCurrentlyAllocated().cardinality();
    }

    /**
     * Removes a single named selection from the named selection state.
     *
     * @param selection The named selection to be removed from the state.
     *
     * @see NamedSelection
     */
    public void clearSelection(final NamedSelection selection) {
        if (!selection.isLocked()) {
            resetSelection(selection);
            state.getCurrentlyAllocated().clear(selection.getID());
            state.getNamedSelections().remove(selection);

            updateState();
        } else {
            notifyProtected(selection.getName());
        }
    }

    /**
     * Removes the given named selections from the named selection state.
     *
     * @param removeSelections An <code>ArrayList&lt;NamedSelection&gt;</code>
     * containing the named selections to be removed from the named selection
     * state.
     *
     * @see ArrayList
     * @see NamedSelection
     */
    public void clearSelections(final ArrayList<NamedSelection> removeSelections) {
        for (final NamedSelection remove : removeSelections) {
            if (!remove.isLocked()) {
                resetSelection(remove);
                state.getCurrentlyAllocated().clear(remove.getID());
                state.getNamedSelections().remove(remove);
            } else {
                notifyProtected(remove.getName());
            }
        }

        updateState();
    }

    /**
     * Returns the maximum number of named selections that can be stored to a
     * single graph.
     *
     * @return The maximum number of named selections possible.
     */
    public static int getMaximumSelectionCount() {
        return MAXIMUM_SELECTION_COUNT;
    }

    /**
     * Updates the state to reflect the status of the 'dim others' control.
     *
     * @param isDimOthers The 'dim others' state.
     */
    public void updateDimOthersState(final boolean isDimOthers) {
        state.setDimOthers(isDimOthers);

        saveStateToGraph();
    }

    /**
     * Updates the state to reflect the status of the 'select results' control.
     *
     * @param isSelectResults The 'select results' state.
     */
    public void updateSelectResultsState(final boolean isSelectResults) {
        state.setSelectResults(isSelectResults);

        saveStateToGraph();
    }

    /**
     * Listen to attribute changes in the graph so we can reflect them in the
     * GUI.
     * <p>
     * The event may be null, since we call this manually from setNode() after a
     * graph change.
     *
     * @param evt PropertyChangeEvent.
     */
    @Override
    public void graphChanged(final GraphChangeEvent evt) {
        final Graph graph = graphNode.getGraph();
        // TODO: update this so that it listens specifically for changes to the state meta attribute.
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final long vmc = rg.getGlobalModificationCounter();
            if (vmc != valueModificationCounter) {
                // We may have undone something that is contained on the graph, so ensure that GUI is checked for consistency:
                readStateFromGraph(rg);

                updateState();
            }
            valueModificationCounter = vmc;
        } finally {
            rg.release();
        }
    }

    @Override
    public void resultChanged(final LookupEvent lev) {
        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        if (activatedNodes != null && activatedNodes.length == 1
                && activatedNodes[0] instanceof GraphNode) {
            final GraphNode gnode = ((GraphNode) activatedNodes[0]);

            if (gnode != graphNode) {
                setNode(gnode);
            }
        } else {
            setNode(null);
        }
    }

    /**
     * Requests a intersection operation to be performed for the named
     * selections that match an array of named selection IDs.
     * <p>
     * This private method takes into account the display settings as per the
     * current named selection state.
     * <p>
     * Member and non-member elements of 'intersected' named selections will be
     * shown as per the display settings (ie, if dim others has been set, then
     * non-member elements will be dimmed on the graph).
     *
     * @param graph The graph that is to have the intersection operation
     * performed upon.
     * @param useCurrentlySelected <code>true</code> to include the currently
     * selected graph elements in the intersection operation.
     * @param isSelectResults <code>true</code> to set member elements
     * 'selected' attribute.
     * @param isDimOthers <code>true</code> to dim non-member elements.
     * @param ids The array of ids of the named selections that are to be
     * 'intersected'.
     */
    private void performIntersection(final Graph graph, final boolean useCurrentlySelected,
            final boolean isSelectResults, final boolean isDimOthers, final int... ids) {
        final Plugin namedSelectionEdit = new NamedSelectionEditorPlugin(NamedSelectionEditorPlugin.Operation.INTERSECTION, useCurrentlySelected, isSelectResults, isDimOthers, ids);
        final Future<?> f = PluginExecution.withPlugin(namedSelectionEdit).executeLater(graph);
        try {
            f.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Named Selection Intersection was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Requests a union operation to be performed for the named selections that
     * match an array of named selection IDs.
     * <p>
     * This private method takes into account the display settings as per the
     * current named selection state.
     * <p>
     * Member and non-member elements of 'unioned' named selections will be
     * shown as per the display settings (ie, if dim others has been set, then
     * non-member elements will be dimmed on the graph).
     *
     * @param graph The graph that is to have the union operation performed
     * upon.
     * @param useCurrentlySelected <code>true</code> to include the currently
     * selected graph elements in the union operation.
     * @param isSelectResults <code>true</code> to set member elements
     * 'selected' attribute.
     * @param isDimOthers <code>true</code> to dim non-member elements.
     * @param ids The array of ids of the named selections that are to be
     * 'unioned'.
     */
    private void performUnion(final Graph graph, final boolean useCurrentlySelected,
            final boolean isSelectResults, final boolean isDimOthers, final int... ids) {
        final Plugin namedSelectionEdit = new NamedSelectionEditorPlugin(NamedSelectionEditorPlugin.Operation.UNION, useCurrentlySelected, isSelectResults, isDimOthers, ids);
        final Future<?> f = PluginExecution.withPlugin(namedSelectionEdit).executeLater(graph);
        try {
            f.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Named Selection Union was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

    }

    /**
     * Requests the recall of a named selection on the graph for the given ID
     * number.
     * <p>
     * This private method takes into account the display settings as per the
     * current named selection state.
     * <p>
     * Member and non-member elements of the named selection will be shown as
     * per the display settings (ie, if dim others has been set, then non-member
     * elements will be dimmed on the graph).
     *
     * @param graph The graph that is to have the named selection retrieved for.
     * @param isSelectResults <code>true</code> to set member elements
     * 'selected' attribute.
     * @param isDimOthers <code>true</code> to dim non-member elements.
     * @param id The id of the named selection that is to be retrieved.
     */
    private void performRecall(final Graph graph, final boolean isSelectResults,
            final boolean isDimOthers, final int id) {
        final Plugin namedSelectionEdit = new NamedSelectionEditorPlugin(isSelectResults, isDimOthers, id);
        final Future<?> f = PluginExecution.withPlugin(namedSelectionEdit).executeLater(graph);
        try {
            f.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Named Selections recall was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

    }

    /**
     * Requests a new named selection to be created on the graph for the given
     * ID number.
     * <p>
     * Selected graph elements will have their 'named selection' attribute
     * updated to reflect their membership in the new named selection.
     *
     * @param graph The graph that is having the named selection created on.
     * @param id The id of the named selection that is to be included on
     * currently selected graph elements.
     */
    private void performSave(final Graph graph, final int id) {
        final Plugin namedSelectionEdit = new NamedSelectionEditorPlugin(id);
        final Future<?> f = PluginExecution.withPlugin(namedSelectionEdit).executeLater(graph);
        try {
            f.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Named Selection save was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    private void performCustomSave(final Graph graph, final int[] nodesToSave, final int[] transactionsToSave, final int id) {
        final Plugin namedSelectionEdit = new NamedSelectionEditorPlugin(nodesToSave, transactionsToSave, id);
        final Future<?> f = PluginExecution.withPlugin(namedSelectionEdit).executeLater(graph);
        try {
            f.get();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Named Selection custom save was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Helper method that saves the current Named Selection state to the graph.
     *
     * @return The state that was saved to the graph.
     */
    @SuppressWarnings("unchecked")
    private NamedSelectionState saveStateToGraph() {
        final Graph graph = graphNode.getGraph();
        final NamedSelectionState newState;
        if (state != null) {
            newState = new NamedSelectionState(state);
        } else {
            newState = new NamedSelectionState();
        }

        // Only write graph if it exists.
        if (graph != null) {
            // Write what we have to the graph:
            final NamedSelectionStatePlugin nssp = new NamedSelectionStatePlugin(newState);
            final Future<?> f = PluginExecution.withPlugin(nssp).interactively(true).executeLater(graph);
            try {
                f.get();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Saving Named Selection state was interrupted", ex);
                Thread.currentThread().interrupt();
            } catch (final ExecutionException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }

        // Update the old state now:
        state = newState;

        return newState;
    }

    /**
     * Queries the given graph (via the PluginFramework) for a saved
     * <code>NamedSelectionState</code>.
     *
     * @param graph The graph to retrieve the <code>NamedSelectionState</code>
     * from.
     *
     * @see NamedSelectionState
     * @see NamedSelectionStatePlugin
     */
    @SuppressWarnings("unchecked")
    private void readStateFromGraph(final GraphReadMethods graph) {
        // Ensure that the state gets set correctly under all circumstances.
        this.state = null;

        final int attrID = graph.getAttribute(GraphElementType.META, NamedSelectionState.ATTRIBUTE_NAME);
        if (attrID != Graph.NOT_FOUND) {
            final Object possibleState = graph.getObjectValue(attrID, 0);
            if (possibleState instanceof NamedSelectionState namedSelectionState) {
                this.state = namedSelectionState;
            }
        }

        if (this.state == null) {
            this.state = new NamedSelectionState();
        }
    }

    /**
     * Helper method that firstly ensures that the named selection browser (
     * <code>NamedSelectionTopComponent</code>) has been opened, then manually
     * updates it with the latest information regarding the state of all known
     * named selections for this graph.
     * <p>
     * Update state is executed on the EDT to ensure that there are no
     * incidences where the top component cannot be found.
     *
     * @see NamedSelectionTopComponent
     */
    private void updateState() {
        updateState(false);
    }

    private void updateState(final boolean openTopComponent) {
        // Force execution onto the EDT using SwingUtilities.invokeLater and an anonymous class:
        SwingUtilities.invokeLater(() -> {
            final NamedSelectionTopComponent tc
                    = (NamedSelectionTopComponent) WindowManager.getDefault().findTopComponent(NamedSelectionTopComponent.class.getSimpleName());

            if (tc != null) {
                if (openTopComponent && !tc.isOpened()) {
                    tc.open();
                    tc.requestActive();
                }

                // If there is no graph, set logical defaults, then disable UI:
                if (graphNode == null) {
                    tc.updateState(null);
                    tc.updateDimOthers(false);
                    tc.updateSelectResults(true);
                } else { // Update the browser based on information from the current state:
                    tc.updateState(state.getNamedSelections());
                    tc.updateDimOthers(state.isDimOthers());
                    tc.updateSelectResults(state.isSelectResults());
                }
            }
        });
    }

    /**
     * Helper method that alerts the user that no remaining 'slots' for named
     * selections remain.
     */
    private void notifyAllAlloc() {
        final NamedSelectionAllAllocPanel panel = new NamedSelectionAllAllocPanel();
        final DialogDescriptor dd = new DialogDescriptor(panel, Bundle.AllAllocated());

        dd.setOptions(new Object[]{"OK"});
        DialogDisplayer.getDefault().notify(dd);
    }

    /**
     * Helper method that notifies the user that they are trying to perform
     * modifications on a locked / protected named selection.
     *
     * @param name The name of the selection that is being intercepted from
     * modification.
     */
    private void notifyProtected(final String name) {
        final NamedSelectionProtectedPanel panel = new NamedSelectionProtectedPanel(name);
        final DialogDescriptor dd = new DialogDescriptor(panel, Bundle.ProtectedSelection());

        dd.setOptions(new Object[]{"OK"});
        DialogDisplayer.getDefault().notify(dd);
    }

    /**
     * Make the graph in the specified node the source for the manager.
     * <p>
     * If another graph is attached to the model, it is detached first.
     *
     * @param node The GraphNode containing the graph to be displayed.
     */
    private void setNode(final GraphNode node) {
        // Check if we are moving graphs:
        if (graphNode != null) {
            final Graph graph = graphNode.getGraph();

            // disconnect the listener from this graph, as we are moving away from it.
            graph.removeGraphChangeListener(this);
        }

        // We are entering a new graph, so set up accordingly:
        if (node != null) {
            graphNode = node;
            final Graph graph = graphNode.getGraph();

            ReadableGraph rg = graph.getReadableGraph();
            try {
                // Get the current global modification counter as we are transitioning to a different graph:
                valueModificationCounter = rg.getGlobalModificationCounter();

                // attach this listener to the new graph:
                graph.addGraphChangeListener(this);

                readStateFromGraph(rg);

            } finally {
                rg.release();
            }

// No active graphs
        } else {
            graphNode = null;
        }

        updateState();
    }

    public boolean isManagingActiveGraph() {
        return graphNode != null;
    }
}
