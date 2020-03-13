/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.monitor.AttributeValueMonitor;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewConcept;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
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
    @ActionReference(path = "Menu/Views", position = 1300),
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

    private TableViewState currentState;
    private final TableViewPane pane;
    private final Set<AttributeValueMonitor> columnAttributeMonitors;

    public TableViewTopComponent() {
        setName(Bundle.CTL_TableView2TopComponent());
        setToolTipText(Bundle.HINT_TableView2TopComponent());
        initComponents();

        this.currentState = null;
        this.pane = new TableViewPane(TableViewTopComponent.this);
        this.columnAttributeMonitors = new HashSet<>();
        initContent();

        addStructureChangeHandler(graph -> {
            final Thread thread = new Thread("Table View: Update Data") {
                @Override
                public void run() {
                    pane.updateData(graph, currentState);
                }
            };
            thread.start();
        });

        addAttributeCountChangeHandler(graph -> {
            final Thread thread = new Thread("Table View: Update Data") {
                @Override
                public void run() {
                    pane.updateTable(graph, currentState);
                }
            };
            thread.start();
        });

        addAttributeValueChangeHandler(VisualConcept.VertexAttribute.SELECTED, graph -> {
            if (currentState != null && currentState.getElementType() == GraphElementType.VERTEX) {
                if (currentState.isSelectedOnly()) {
                    final Thread thread = new Thread("Table View: Update Data") {
                        @Override
                        public void run() {
                            pane.updateData(graph, currentState);
                        }
                    };
                    thread.start();
                } else {
                    final Thread thread = new Thread("Table View: Update Selection") {
                        @Override
                        public void run() {
                            pane.updateSelection(graph, currentState);
                        }
                    };
                    thread.start();
                }
            }
        });

        addAttributeValueChangeHandler(VisualConcept.TransactionAttribute.SELECTED, graph -> {
            if (currentState != null && currentState.getElementType() == GraphElementType.TRANSACTION) {
                final Thread thread;
                if (currentState.isSelectedOnly()) {
                    thread = new Thread("Table View: Update Data") {
                        @Override
                        public void run() {
                            pane.updateData(graph, currentState);
                        }
                    };
                    thread.start();
                } else {
                    thread = new Thread("Table View: Update Selection") {
                        @Override
                        public void run() {
                            pane.updateSelection(graph, currentState);
                        }
                    };
                    thread.start();
                }
            }
        });

        addAttributeValueChangeHandler(TableViewConcept.MetaAttribute.TABLE_VIEW_STATE, graph -> {
            final TableViewState previousState = currentState;
            updateState(graph);
            final Tuple<Set<Tuple<String, Attribute>>, Set<Tuple<String, Attribute>>> columnAttributeChanges = getColumnAttributeChanges(previousState, currentState);

            if (columnAttributeMonitors != null && !columnAttributeChanges.getFirst().isEmpty()) {
                final Set<AttributeValueMonitor> removeMonitors = columnAttributeMonitors.stream()
                        .filter(monitor -> columnAttributeChanges.getFirst().stream()
                        .anyMatch(columnAttributeTuple
                                -> columnAttributeTuple.getSecond().getElementType() == monitor.getElementType()
                        && columnAttributeTuple.getSecond().getName().equals(monitor.getName()))
                        ).collect(Collectors.toSet());

                removeMonitors.forEach(monitor -> {
                    removeAttributeValueChangeHandler(monitor);
                    columnAttributeMonitors.remove(monitor);
                });
            }

            final Thread tableUpdateThread = new Thread("Table View: Update Table") {
                @Override
                public void run() {
                    pane.updateTable(graph, currentState);
                }
            };
            tableUpdateThread.start();

            if (currentState != null && currentState.getColumnAttributes() != null && !columnAttributeChanges.getSecond().isEmpty()) {
                columnAttributeChanges.getSecond().forEach(attributeTuple -> {
                    columnAttributeMonitors.add(addAttributeValueChangeHandler(
                            attributeTuple.getSecond().getElementType(),
                            attributeTuple.getSecond().getName(),
                            g -> {
                                final Thread dataUpdateThread = new Thread("Table View: Update Data") {
                                    @Override
                                    public void run() {
                                        pane.updateData(g, currentState);
                                    }
                                };
                                dataUpdateThread.start();
                            }));
                });
            }
        });

        addIgnoredEvent(TableViewUtilities.SELECT_ON_GRAPH_PLUGIN);
    }

    public void showSelected(final GraphElementType elementType, final int elementId) {
        final TableViewState stateSnapshot = currentState;
        final Future<?> stateLock;
        if (currentState != null && currentState.getElementType() != elementType) {
            final TableViewState newState = new TableViewState(currentState);
            newState.setElementType(elementType);
            newState.setSelectedOnly(true);
            stateLock = PluginExecution.withPlugin(new TableViewUtilities.UpdateStatePlugin(newState)).executeLater(currentGraph);
        } else {
            stateLock = null;
        }

        if (stateLock != null) {
            try {
                stateLock.get();
            } catch (final ExecutionException | InterruptedException ex) {
                // DO NOTHING
            }
        }

        final Thread thread = new Thread("Table View: Update Selection") {
            @Override
            public void run() {
                while (stateLock != null && currentState == stateSnapshot) {
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
                        // DO NOTHING
                    }
                }
                pane.updateSelection(currentGraph, currentState);
            }
        };
        thread.start();
    }

    /**
     * Get the current table view state.
     */
    public TableViewState getCurrentState() {
        return currentState;
    }

    /**
     * Calculates the intersection of the column attributes for old and new
     * {@link TableViewState} objects, and returns a {@link Tuple} of the column
     * attributes only present in the old state (that is, those removed in the
     * transition to the new state) and the attributes only present in the new
     * state (that is, those added in the transition to the new state).
     *
     * @param oldState a {@link TableViewState} representing the old state
     * @param newState a {@link TableViewState} representing the new state
     * @return a {@link Tuple} containing the column attributes which have been
     * removed and the column attributes which have been added in the transition
     * from the old state to the new state
     */
    private Tuple<Set<Tuple<String, Attribute>>, Set<Tuple<String, Attribute>>> getColumnAttributeChanges(final TableViewState oldState, final TableViewState newState) {
        final Set<Tuple<String, Attribute>> removedColumnAttributes;
        if (oldState != null && oldState.getColumnAttributes() != null) {
            removedColumnAttributes = new HashSet<>(oldState.getColumnAttributes());
        } else {
            removedColumnAttributes = new HashSet<>();
        }

        final Set<Tuple<String, Attribute>> addedColumnAttributes;
        if (newState != null && newState.getColumnAttributes() != null) {
            addedColumnAttributes = new HashSet<>(newState.getColumnAttributes());
        } else {
            addedColumnAttributes = new HashSet<>();
        }

        final Set<Tuple<String, Attribute>> intersection = new HashSet<>(removedColumnAttributes);
        intersection.retainAll(addedColumnAttributes);
        removedColumnAttributes.removeAll(intersection);
        addedColumnAttributes.removeAll(intersection);

        return Tuple.create(removedColumnAttributes, addedColumnAttributes);
    }

    /**
     * Update the current TableViewState for the given graph, including creating
     * one if none exists.
     *
     * @param graph the state will be read from the graph using this read lock.
     * @return the current TableViewState of the given graph.
     * @throws InterruptedException if the operation is interrupted or canceled.
     */
    private void updateState(final Graph graph) {
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
                    PluginExecution.withPlugin(new UpdateStatePlugin(state)).executeLater(currentGraph);
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
    protected void handleNewGraph(final Graph graph) {
        final TableViewState previousState = currentState;
        updateState(graph);
        final Tuple<Set<Tuple<String, Attribute>>, Set<Tuple<String, Attribute>>> columnAttributeChanges = getColumnAttributeChanges(previousState, currentState);

        if (columnAttributeMonitors != null && !columnAttributeChanges.getFirst().isEmpty()) {
            final Set<AttributeValueMonitor> removeMonitors = columnAttributeMonitors.stream()
                    .filter(monitor -> columnAttributeChanges.getFirst().stream()
                    .anyMatch(columnAttributeTuple
                            -> columnAttributeTuple.getSecond().getElementType() == monitor.getElementType()
                    && columnAttributeTuple.getSecond().getName().equals(monitor.getName()))
                    ).collect(Collectors.toSet());

            removeMonitors.forEach(monitor -> {
                removeAttributeValueChangeHandler(monitor);
                columnAttributeMonitors.remove(monitor);
            });
        }

        final Thread tableUpdateThread = new Thread("Table View: Update Table") {
            @Override
            public void run() {
                pane.updateTable(graph, currentState);
            }
        };
        tableUpdateThread.start();

        if (currentState != null && currentState.getColumnAttributes() != null && !columnAttributeChanges.getSecond().isEmpty()) {
            columnAttributeChanges.getSecond().forEach(attributeTuple -> {
                columnAttributeMonitors.add(addAttributeValueChangeHandler(
                        attributeTuple.getSecond().getElementType(),
                        attributeTuple.getSecond().getName(),
                        g -> {
                            final Thread dataUpdateThread = new Thread("Table View: Update Data") {
                                @Override
                                public void run() {
                                    pane.updateData(g, currentState);
                                }
                            };
                            dataUpdateThread.start();
                        }));
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
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
