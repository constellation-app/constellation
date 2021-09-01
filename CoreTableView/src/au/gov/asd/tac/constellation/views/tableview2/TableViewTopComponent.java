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
package au.gov.asd.tac.constellation.views.tableview2;

import au.gov.asd.tac.constellation.views.tableview2.components.TableViewPane;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.monitor.AttributeValueMonitor;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.plugins.SelectionToGraphPlugin;
import au.gov.asd.tac.constellation.views.tableview2.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewConcept;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import au.gov.asd.tac.constellation.views.tableview2.tasks.UpdateTableDataTask;
import au.gov.asd.tac.constellation.views.tableview2.tasks.UpdateTableSelectionTask;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javafx.application.Platform;
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
        preferredID = "TableView2TopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/tableview2/resources/table-view.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 1500),
    @ActionReference(path = "Shortcuts", name = "CS-Y")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_TableView2Action",
        preferredID = "TableView2TopComponent"
)
@Messages({
    "CTL_TableView2Action=Table View",
    "CTL_TableView2TopComponent=Table View",
    "HINT_TableView2TopComponent=Table View"
})
public final class TableViewTopComponent extends JavaFxTopComponent<TableViewPane> {

    private final ExecutorService executorService = Executors.newScheduledThreadPool(1);
    
    private TableViewState currentState;
    private final TableViewPane pane;
    private final Set<AttributeValueMonitor> columnAttributeMonitors;

    private static final String UPDATE_DATA = "Table View: Update Data";
    private static final String UPDATE_SELECTION = "Table View: Update Selection";

    public TableViewTopComponent() {
        setName(Bundle.CTL_TableView2TopComponent());
        setToolTipText(Bundle.HINT_TableView2TopComponent());
        initComponents();

        this.currentState = null;
        this.pane = new TableViewPane(TableViewTopComponent.this);
        this.columnAttributeMonitors = new HashSet<>();
        initContent();

        addStructureChangeHandler(graph -> {
            if (!needsUpdate()) {
                return;
            }
            executorService.submit(new UpdateTableDataTask(pane, graph, getCurrentState()));
        });

        addAttributeCountChangeHandler(graph -> {
            if (needsUpdate()) {
                pane.updateTable(graph, currentState);
            }
        });

        addAttributeValueChangeHandler(VisualConcept.VertexAttribute.SELECTED, graph -> {
            if (needsUpdate() && currentState != null
                    && currentState.getElementType() == GraphElementType.VERTEX) {
                if (currentState.isSelectedOnly()) {
                    executorService.submit(new UpdateTableDataTask(pane, graph, getCurrentState()));
                } else {
                    executorService.submit(new UpdateTableSelectionTask(pane, graph, getCurrentState()));
                }
            }
        });

        addAttributeValueChangeHandler(VisualConcept.TransactionAttribute.SELECTED, graph -> {
            if (needsUpdate() && currentState != null 
                    && currentState.getElementType() == GraphElementType.TRANSACTION) {
                if (currentState.isSelectedOnly()) {
                    executorService.submit(new UpdateTableDataTask(pane, graph, getCurrentState()));
                } else {
                    executorService.submit(new UpdateTableSelectionTask(pane, graph, getCurrentState()));
                }
            }
        });

        addAttributeValueChangeHandler(TableViewConcept.MetaAttribute.TABLE_VIEW_STATE, graph -> {
            handleNewGraph(graph);
        });

        addIgnoredEvent(SelectionToGraphPlugin.SELECT_ON_GRAPH_PLUGIN);
    }

    /**
     * 
     * @param elementType
     * @param elementId 
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
                getTablePane().getTable().updateSelection(getCurrentGraph(), getCurrentState(),
                        getTablePane().getTableSelectionListener(), getTablePane().getSelectedOnlySelectionListener());
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
    public TableViewPane getTablePane() {
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
     *     not the new one
     */
    protected Set<Tuple<String, Attribute>> getRemovedAttributes(final TableViewState oldState,
                                                                 final TableViewState newState) {
        return new HashSet<>(
                CollectionUtils.subtract(
                        oldState.getColumnAttributes(),
                        newState.getColumnAttributes()
                )
        );
    }
    
    /**
     * Get column attributes that were not present in the old state but are present
     * in the new state.
     *
     * @param oldState the old table state
     * @param newState the new table state
     * @return a set of column attributes that were not present in the old state
     *     but are present in the new state
     */
    protected Set<Tuple<String, Attribute>> getAddedAttributes(final TableViewState oldState,
                                                               final TableViewState newState) {
        return new HashSet<>(
                CollectionUtils.subtract(
                        newState.getColumnAttributes(),
                        oldState.getColumnAttributes()
                )
        );
    }

    /**
     * Update the current table state with the table state stored in the passed
     * graph attributes. If a table state does not exist in the graph attribute
     * then it will crate and new state and set it to the current state in
     * the table.
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
    protected TableViewPane createContent() {
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

    @Override
    protected void handleNewGraph(final Graph graph) {
        if (!needsUpdate()) {
            return;
        }
        
        final TableViewState previousState = currentState;
        
        updateState(graph);
        
        final Set<Tuple<String, Attribute>> removedColumnAttributes
                = getRemovedAttributes(previousState, currentState);
        final Set<Tuple<String, Attribute>> addedColumnAttributes
                = getAddedAttributes(previousState, currentState);

        if (columnAttributeMonitors != null && !removedColumnAttributes.isEmpty()) {
            final Set<AttributeValueMonitor> removeMonitors = columnAttributeMonitors.stream()
                    .filter(monitor -> removedColumnAttributes.stream()
                            .anyMatch(columnAttributeTuple -> 
                                    columnAttributeTuple.getSecond().getElementType() == monitor.getElementType()
                                            && columnAttributeTuple.getSecond().getName().equals(monitor.getName())
                            )
                    )
                    .collect(Collectors.toSet());

            removeMonitors.forEach(monitor -> {
                removeAttributeValueChangeHandler(monitor);
                columnAttributeMonitors.remove(monitor);
            });
        }

        pane.updateTable(graph, currentState);

        if (currentState != null && currentState.getColumnAttributes() != null 
                && !addedColumnAttributes.isEmpty()) {
            addedColumnAttributes.forEach(attributeTuple ->
                columnAttributeMonitors.add(
                        addAttributeValueChangeHandler(
                                attributeTuple.getSecond().getElementType(),
                                attributeTuple.getSecond().getName(),
                                g -> executorService.submit(new UpdateTableDataTask(pane, g, getCurrentState()))
                        )
                )
            );
        }
    }

    @Override
    protected void handleGraphClosed(final Graph graph) {
        getTablePane().getTableService().updatePagination(
                getTablePane().getTableService().getTablePreferences().getMaxRowsPerPage(), null);
        Platform.runLater(() -> {
            getTablePane().setCenter(getTablePane().getTableService().getPagination());
        });
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
