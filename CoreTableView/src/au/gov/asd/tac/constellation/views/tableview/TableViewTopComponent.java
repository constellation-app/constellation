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
package au.gov.asd.tac.constellation.views.tableview;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.monitor.AttributeValueMonitor;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.threadpool.ConstellationGlobalThreadPool;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.plugins.SelectionToGraphPlugin;
import au.gov.asd.tac.constellation.views.tableview.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewConcept;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import au.gov.asd.tac.constellation.views.tableview.tasks.TriggerDataUpdateTask;
import au.gov.asd.tac.constellation.views.tableview.tasks.TriggerSelectionUpdateTask;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Table View Top Component.
 *
 * @author elnath
 * @author cygnus_x-1
 */
@TopComponent.Description(
        preferredID = "TableViewTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/tableview/resources/table-view.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 1500),
    @ActionReference(path = "Shortcuts", name = "CS-Y")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_TableViewAction",
        preferredID = "TableViewTopComponent"
)
@Messages({
    "CTL_TableViewAction=Table View",
    "CTL_TableViewTopComponent=Table View",
    "HINT_TableViewTopComponent=Table View"
})
public final class TableViewTopComponent extends JavaFxTopComponent<TablePane> {

    public static final Object TABLE_LOCK = new Object();

    private final ExecutorService executorService = ConstellationGlobalThreadPool.getThreadPool().getFixedThreadPool();

    private final TablePane pane;
    private final Set<AttributeValueMonitor> columnAttributeMonitors;

    private TableViewState currentState;

    public TableViewTopComponent() {
        setName(Bundle.CTL_TableViewTopComponent());
        setToolTipText(Bundle.HINT_TableViewTopComponent());

        initComponents();

        this.currentState = null;
        this.pane = new TablePane(this);
        this.columnAttributeMonitors = new HashSet<>();

        // The table pane is initialized above is returned in the overridden
        // method getContent() below. That is how the initContent in the super
        // class can reference it and add the table to this pane.
        initContent();

        // If the graph structure changes then update the table rows
        addStructureChangeHandler(graph -> {
            // Only update if the table is visible
            if (!needsUpdate()) {
                return;
            }
            executorService.submit(new TriggerDataUpdateTask(pane, graph, getCurrentState()));
        });

        // If graph attributes are added or removed then update the table rows,
        // columns and selection
        addAttributeCountChangeHandler(graph -> {
            if (needsUpdate()) {
                pane.updateTable(graph, currentState);
            }
        });

        // If the selection attribute on a vertex is changed in the graph and the
        // table is in "Selected Only" mode then update the table rows as this
        // represents a change to the actual table data. Otherwise just update
        // the selection. The table also needs to have its element type set to VERTEX
        addAttributeValueChangeHandler(VisualConcept.VertexAttribute.SELECTED, graph -> {
            if (needsUpdate() && currentState != null
                    && currentState.getElementType() == GraphElementType.VERTEX) {
                if (currentState.isSelectedOnly()) {
                    executorService.submit(new TriggerDataUpdateTask(pane, graph, getCurrentState()));
                } else {
                    executorService.submit(new TriggerSelectionUpdateTask(pane, graph, getCurrentState()));
                }
            }
        });

        // If the selection attribute on a transaction is changed in the graph and the
        // table is in "Selected Only" mode then update the table rows as this
        // represents a change to the actual table data. Otherwise just update
        // the selection. The table also needs to have its element type set to TRANSACTION
        addAttributeValueChangeHandler(VisualConcept.TransactionAttribute.SELECTED, graph -> {
            if (needsUpdate() && currentState != null
                    && currentState.getElementType() == GraphElementType.TRANSACTION) {
                if (currentState.isSelectedOnly()) {
                    executorService.submit(new TriggerDataUpdateTask(pane, graph, getCurrentState()));
                } else {
                    executorService.submit(new TriggerSelectionUpdateTask(pane, graph, getCurrentState()));
                }
            }
        });

        // If the table state is updated in the graph then refresh the table
        addAttributeValueChangeHandler(TableViewConcept.MetaAttribute.TABLE_VIEW_STATE,
                graph -> handleNewGraph(graph));

        // This is a table plugin that sends the current table selection to the
        // graph and selects the corresponding elements. To avoid a never ending
        // loop of events, events triggered by this plugin are ignored.
        addIgnoredEvent(SelectionToGraphPlugin.SELECT_ON_GRAPH_PLUGIN);
    }

    /**
     * Copy's the existing table view state and sets the new state's element
     * type to the passed value. Also ensures the new state is in "Selected
     * Only" mode. The graph table view state attribute is updated with the new
     * state and then the table's selection is updated.
     *
     * @param elementType the element type to set to the new state
     * @param elementId can be anything, not used
     */
    public Future<?> showSelected(final GraphElementType elementType, final int elementId) {
        final TableViewState stateSnapshot = getCurrentState();
        final Future<?> stateLock;

        if (getCurrentState() != null && getCurrentState().getElementType() != elementType) {
            final TableViewState newState = new TableViewState(getCurrentState());
            newState.setElementType(elementType);
            newState.setSelectedOnly(true);

            stateLock = PluginExecution.withPlugin(
                    new UpdateStatePlugin(newState)
            ).executeLater(getCurrentGraph());
        } else {
            stateLock = null;
        }

        if (stateLock != null) {
            try {
                stateLock.get();
            } catch (final ExecutionException ex) {
                // DO NOTHING
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return getExecutorService().submit(() -> {
            while (stateLock != null && getCurrentState() == stateSnapshot) {
                try {
                    // TODO: remove sleep
                    // ...but there is an async issue which needs to be
                    // resolved first. When showSelected() is called, the
                    // order of operations is to update the Table View
                    // state (if required) and then to select the rows in
                    // the table based on the current graph selection. The
                    // issue is that the state is updated by writing a
                    // TableViewState object to the graph and letting a
                    // Table View listener respond to that. Unfortunately,
                    // there is no obvious way for this operation to know
                    // when the Table View listener has finished responding,
                    // so for now we just wait until the currentState object
                    // matches the state object we updated it to.
                    Thread.sleep(10);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            getTablePane().getTable().updateSelection(
                    getCurrentGraph(),
                    getCurrentState()
            );
        });
    }

    /**
     * Get the current table view state.
     *
     * @return the current table state
     */
    public TableViewState getCurrentState() {
        return currentState;
    }

    /**
     * Gets the table pane.
     *
     * @return the table pane
     */
    public TablePane getTablePane() {
        return pane;
    }

    /**
     * Gets the tables executor service for running various update tasks.
     *
     * @return the tables executor service
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Get column attributes that were present in the old state but not in the
     * new one.
     *
     * @param oldState the old table state
     * @param newState the new table state
     * @return a set of column attributes that are present in the old state but
     * not the new one
     */
    protected Set<Tuple<String, Attribute>> getRemovedAttributes(final TableViewState oldState,
            final TableViewState newState) {
        return new HashSet<>(
                CollectionUtils.subtract(
                        oldState != null && oldState.getColumnAttributes() != null
                        ? oldState.getColumnAttributes() : new HashSet<>(),
                        newState != null && newState.getColumnAttributes() != null
                        ? newState.getColumnAttributes() : new HashSet<>()
                )
        );
    }

    /**
     * Get column attributes that were not present in the old state but are
     * present in the new state.
     *
     * @param oldState the old table state
     * @param newState the new table state
     * @return a set of column attributes that were not present in the old state
     * but are present in the new state
     */
    protected Set<Tuple<String, Attribute>> getAddedAttributes(final TableViewState oldState,
            final TableViewState newState) {
        return new HashSet<>(
                CollectionUtils.subtract(
                        newState != null && newState.getColumnAttributes() != null
                        ? newState.getColumnAttributes() : new HashSet<>(),
                        oldState != null && oldState.getColumnAttributes() != null
                        ? oldState.getColumnAttributes() : new HashSet<>()
                )
        );
    }

    /**
     * Update the current table state with the table state stored in the passed
     * graph attributes. If a table state does not exist in the graph attribute
     * then it will crate and new state and set it to the current state in the
     * table.
     *
     * @param graph the graph that the new state will be extracted from
     */
    protected void updateState(final Graph graph) {
        TableViewState state = null;
        boolean newState = false;

        if (graph == null) {
            currentState = state;
        } else {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final int stateAttribute = TableViewConcept.MetaAttribute.TABLE_VIEW_STATE.get(readableGraph);
                if (stateAttribute == Graph.NOT_FOUND) {
                    state = new TableViewState();
                    newState = true;
                } else {
                    state = readableGraph.getObjectValue(stateAttribute, 0);
                    if (state == null) {
                        state = new TableViewState();
                        newState = true;
                    }
                }

                if (newState) {
                    PluginExecution.withPlugin(
                            new UpdateStatePlugin(state)
                    ).executeLater(getCurrentGraph());
                }
            } finally {
                readableGraph.release();
            }
        }

        currentState = state;
    }

    @Override
    protected TablePane createContent() {
        return pane;
    }

    @Override
    protected String createStyle() {
        return "resources/table-view.css";
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        handleNewGraph(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Update the current state with the new state pulled from the passed
     * graph's attributes and update the attribute handlers so that the table is
     * only notified for attribute changes that it cares about. Then trigger a
     * table refresh using the new graph as its source of truth.
     *
     * @param graph the new graph
     */
    @Override
    protected void handleNewGraph(final Graph graph) {
        if (!needsUpdate()) {
            return;
        }

        // Take a copy of the current state that is associated with the current graph
        final TableViewState previousState = currentState;

        // Update the current state by pulling the table state attribute from
        // the new graph
        updateState(graph);

        // Determine the visible column changes
        final Set<Tuple<String, Attribute>> removedColumnAttributes
                = getRemovedAttributes(previousState, currentState);
        final Set<Tuple<String, Attribute>> addedColumnAttributes
                = getAddedAttributes(previousState, currentState);

        // Remove attribute handlers for columns in the table that will no longer be visible
        // with the state associated with the new graph
        if (columnAttributeMonitors != null && !removedColumnAttributes.isEmpty()) {
            final Set<AttributeValueMonitor> removeMonitors = columnAttributeMonitors.stream()
                    .filter(monitor -> removedColumnAttributes.stream()
                    .anyMatch(columnAttributeTuple
                            -> columnAttributeTuple.getSecond().getElementType() == monitor.getElementType()
                    && columnAttributeTuple.getSecond().getName().equals(monitor.getName())
                    )
                    )
                    .collect(Collectors.toSet());

            removeMonitors.forEach(monitor -> {
                removeAttributeValueChangeHandler(monitor);
                columnAttributeMonitors.remove(monitor);
            });
        }

        // Update the table data, columns and selection with the new graph
        pane.updateTable(graph, currentState);

        // Add attribute handlers that detect changes to the graph attributes that
        // represent visible columns in the table. When these attributes change,
        // the table should have its data refreshed
        if (currentState != null && currentState.getColumnAttributes() != null
                && !addedColumnAttributes.isEmpty()) {
            addedColumnAttributes.forEach(attributeTuple
                    -> columnAttributeMonitors.add(addAttributeValueChangeHandler(attributeTuple.getSecond().getElementType(),
                            attributeTuple.getSecond().getName(),
                            g -> executorService.submit(new TriggerDataUpdateTask(pane, g, getCurrentState()))
                    )
                    )
            );
        }
    }

    /**
     * When the graph is closed change the graphs pagination to a pagination
     * over nothing and update the table. This will essentially clear the table.
     *
     * @param graph the graph being closed
     */
    @Override
    protected void handleGraphClosed(final Graph graph) {
        getTablePane().getActiveTableReference().updatePagination(
                getTablePane().getActiveTableReference().getUserTablePreferences().getMaxRowsPerPage(), null, getTablePane());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
