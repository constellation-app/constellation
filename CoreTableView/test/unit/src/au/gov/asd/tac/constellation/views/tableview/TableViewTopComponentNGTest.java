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
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.UserTablePreferences;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Pagination;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableViewTopComponentNGTest {
    private static final Logger LOGGER = Logger.getLogger(TableViewTopComponentNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @Test
    public void createStyle() {
        final TableViewTopComponent tableViewTopComponent = mock(TableViewTopComponent.class);

        doCallRealMethod().when(tableViewTopComponent).createStyle();

        assertEquals("resources/table-view-light.css", tableViewTopComponent.createStyle());
    }

    @Test
    public void updateStatePresentInGraphAttributes() {
        final TableViewTopComponent tableViewTopComponent = mock(TableViewTopComponent.class);

        doCallRealMethod().when(tableViewTopComponent).updateState(any(Graph.class));
        doCallRealMethod().when(tableViewTopComponent).updateState(isNull());
        doCallRealMethod().when(tableViewTopComponent).getCurrentState();

        final Graph graph = mock(Graph.class);
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        final TableViewState state = new TableViewState();
        state.setElementType(GraphElementType.META); // <-- Non Default Value

        when(graph.getReadableGraph()).thenReturn(readableGraph);
        when(readableGraph.getAttribute(GraphElementType.META, "table_view_state")).thenReturn(42);
        when(readableGraph.getObjectValue(42, 0)).thenReturn(state);

        try (final MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class)) {

            tableViewTopComponent.updateState(graph);

            pluginExecutionMockedStatic.verifyNoInteractions();
        }
        assertEquals(state, tableViewTopComponent.getCurrentState());

        tableViewTopComponent.updateState(null);

        assertEquals(null, tableViewTopComponent.getCurrentState());

        // Should only be called once. Second call has null graph
        verify(graph).getReadableGraph();
        verify(readableGraph).release();
    }

    @Test
    public void updateStateNotPresentInGraphAttributes() {
        final TableViewTopComponent tableViewTopComponent = mock(TableViewTopComponent.class);

        doCallRealMethod().when(tableViewTopComponent).updateState(any(Graph.class));
        doCallRealMethod().when(tableViewTopComponent).updateState(isNull());
        doCallRealMethod().when(tableViewTopComponent).getCurrentState();

        final Graph graph = mock(Graph.class);
        final Graph currentGraph = mock(Graph.class);
        final ReadableGraph readableGraph = mock(ReadableGraph.class);

        when(graph.getReadableGraph()).thenReturn(readableGraph);
        when(readableGraph.getAttribute(GraphElementType.META, "table_view_state")).thenReturn(42);
        when(readableGraph.getObjectValue(42, 0)).thenReturn(null);
        when(tableViewTopComponent.getCurrentGraph()).thenReturn(currentGraph);

        try (final MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class)) {

            final PluginExecution pluginExecution = mock(PluginExecution.class);

            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(UpdateStatePlugin.class)))
                    .thenAnswer(mockitoInvocation -> {
                        final UpdateStatePlugin plugin = (UpdateStatePlugin) mockitoInvocation.getArgument(0);

                        assertEquals(new TableViewState(), plugin.getTableViewState());

                        return pluginExecution;
                    });

            tableViewTopComponent.updateState(graph);

            verify(pluginExecution).executeLater(currentGraph);
        }

        assertEquals(new TableViewState(), tableViewTopComponent.getCurrentState());

        verify(graph).getReadableGraph();
        verify(readableGraph).release();
    }

    @Test
    public void showSelected() {
        final TableViewTopComponent tableViewTopComponent = mock(TableViewTopComponent.class);

        final TableViewState currentState = new TableViewState();
        final Graph currentGraph = mock(Graph.class);

        final TableViewState expectedNewState = new TableViewState();
        expectedNewState.setElementType(GraphElementType.META);
        expectedNewState.setSelectedOnly(true);

        final TablePane tablePane = mock(TablePane.class);

        final Table table = mock(Table.class);

        doCallRealMethod().when(tableViewTopComponent).showSelected(any(GraphElementType.class), anyInt());
        when(tableViewTopComponent.getCurrentState()).thenReturn(currentState);
        when(tableViewTopComponent.getCurrentGraph()).thenReturn(currentGraph);
        when(tableViewTopComponent.getTablePane()).thenReturn(tablePane);
        when(tableViewTopComponent.getExecutorService()).thenReturn(Executors.newSingleThreadExecutor());

        when(tablePane.getTable()).thenReturn(table);

        try (final MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class)) {

            final PluginExecution pluginExecution = mock(PluginExecution.class);
            when(pluginExecution.executeLater(any(Graph.class)))
                    .thenReturn(CompletableFuture.completedFuture(null));

            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(UpdateStatePlugin.class)))
                    .thenAnswer(mockitoInvocation -> {
                        final UpdateStatePlugin plugin = (UpdateStatePlugin) mockitoInvocation.getArgument(0);

                        assertEquals(expectedNewState, plugin.getTableViewState());

                        // Change the mock now that the "Update Plugin" has run
                        when(tableViewTopComponent.getCurrentState()).thenReturn(expectedNewState);

                        return pluginExecution;
                    });

            final Future<?> updateTask = tableViewTopComponent.showSelected(GraphElementType.META, 42);

            // Ensure that the update selection task is completed
            try {
                updateTask.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                fail("The submitted task in show selected did not complete in the "
                        + "allowed time. Something is probably wrong.");
            }

            verify(pluginExecution).executeLater(currentGraph);
            verify(table).updateSelection(
                    currentGraph,
                    expectedNewState
            );
        }
    }

    @Test
    public void componentShowing() throws InterruptedException {
        final TableViewTopComponent tableViewTopComponent = mock(TableViewTopComponent.class);

        doCallRealMethod().when(tableViewTopComponent).componentShowing();
        doNothing().when(tableViewTopComponent).handleNewGraph(any(Graph.class));

        try (final MockedStatic<GraphManager> graphManagerMockedStatic
                = Mockito.mockStatic(GraphManager.class)) {
            final GraphManager graphManager = mock(GraphManager.class);
            final Graph activeGraph = mock(Graph.class);

            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            when(graphManager.getActiveGraph()).thenReturn(activeGraph);

            tableViewTopComponent.componentShowing();

            verify(tableViewTopComponent).handleNewGraph(activeGraph);
        }
    }

    @Test
    public void handleGraphClosed() throws InterruptedException {
        final TableViewTopComponent tableViewTopComponent = mock(TableViewTopComponent.class);
        final TablePane tablePane = mock(TablePane.class);
        final ActiveTableReference activeTableReference = mock(ActiveTableReference.class);
        final Pagination pagination = mock(Pagination.class);

        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setMaxRowsPerPage(42);

        when(tableViewTopComponent.getTablePane()).thenReturn(tablePane);
        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);
        when(activeTableReference.getUserTablePreferences()).thenReturn(userTablePreferences);
        when(activeTableReference.getPagination()).thenReturn(pagination);

        doCallRealMethod().when(tableViewTopComponent).handleGraphClosed(any(Graph.class));

        tableViewTopComponent.handleGraphClosed(mock(Graph.class));

        verify(activeTableReference).updatePagination(42, null, tablePane);
    }

    @Test
    public void getColumnAttributeChanges() {
        final TableViewTopComponent tableViewTopComponent = mock(TableViewTopComponent.class);

        when(tableViewTopComponent.getRemovedAttributes(or(any(TableViewState.class), isNull()), or(any(TableViewState.class), isNull())))
                .thenCallRealMethod();
        when(tableViewTopComponent.getAddedAttributes(or(any(TableViewState.class), isNull()), or(any(TableViewState.class), isNull())))
                .thenCallRealMethod();

        final Attribute attribute1 = mock(Attribute.class);
        final Attribute attribute2 = mock(Attribute.class);
        final Attribute attribute3 = mock(Attribute.class);

        final TableViewState oldState = new TableViewState();
        oldState.setColumnAttributes(List.of(
                Tuple.create(".source", attribute1),
                Tuple.create(".destination", attribute2)
        ));

        final TableViewState newState = new TableViewState();
        newState.setColumnAttributes(List.of(
                Tuple.create(".destination", attribute2),
                Tuple.create(".transaction", attribute3)
        ));

        // Removed column attributes
        assertEquals(Set.of(
                Tuple.create(".source", attribute1)
        ),
                tableViewTopComponent.getRemovedAttributes(oldState, newState)
        );

        assertEquals(
                Set.of(),
                tableViewTopComponent.getRemovedAttributes(null, newState)
        );

        assertEquals(
                Set.of(
                        Tuple.create(".source", attribute1),
                        Tuple.create(".destination", attribute2)
                ),
                tableViewTopComponent.getRemovedAttributes(oldState, null)
        );

        // Added column attributes
        assertEquals(Set.of(
                Tuple.create(".transaction", attribute3)
        ),
                tableViewTopComponent.getAddedAttributes(oldState, newState)
        );

        assertEquals(
                Set.of(
                        Tuple.create(".destination", attribute2),
                        Tuple.create(".transaction", attribute3)
                ),
                tableViewTopComponent.getAddedAttributes(null, newState)
        );

        assertEquals(
                Set.of(),
                tableViewTopComponent.getAddedAttributes(oldState, null)
        );

    }
}
