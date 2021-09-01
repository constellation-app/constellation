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

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview2.components.Table;
import au.gov.asd.tac.constellation.views.tableview2.components.TableViewPane;
import au.gov.asd.tac.constellation.views.tableview2.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.state.TablePreferences;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Pagination;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableViewTopComponentNGTest {
   
    public TableViewTopComponentNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.showStage();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FxToolkit.hideStage();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void createStyle() {
        final TableViewTopComponent tableTopComponent = mock(TableViewTopComponent.class);
        
        doCallRealMethod().when(tableTopComponent).createStyle();
        
        assertEquals("resources/table-view.css", tableTopComponent.createStyle());
    }
    
    @Test
    public void updateStatePresentInGraphAttributes() {
        final TableViewTopComponent tableTopComponent = mock(TableViewTopComponent.class);
        
        doCallRealMethod().when(tableTopComponent).updateState(any(Graph.class));
        doCallRealMethod().when(tableTopComponent).updateState(isNull());
        doCallRealMethod().when(tableTopComponent).getCurrentState();
        
        final Graph graph = mock(Graph.class);
        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        
        final TableViewState state = new TableViewState();
        state.setElementType(GraphElementType.META); // <-- Non Default Value
        
        when(graph.getReadableGraph()).thenReturn(readableGraph);
        when(readableGraph.getAttribute(GraphElementType.META, "table_view_state")).thenReturn(42);
        when(readableGraph.getObjectValue(42, 0)).thenReturn(state);
        
        try(final MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class)) {
            
            tableTopComponent.updateState(graph);
            
            pluginExecutionMockedStatic.verifyNoInteractions();
        }
        assertEquals(state, tableTopComponent.getCurrentState());
        
        tableTopComponent.updateState(null);
        
        assertEquals(null, tableTopComponent.getCurrentState());
        
        // Should only be called once. Second call has null graph
        verify(graph).getReadableGraph();
        verify(readableGraph).release();
    }
    
    @Test
    public void updateStateNotPresentInGraphAttributes() {
        final TableViewTopComponent tableTopComponent = mock(TableViewTopComponent.class);
        
        doCallRealMethod().when(tableTopComponent).updateState(any(Graph.class));
        doCallRealMethod().when(tableTopComponent).updateState(isNull());
        doCallRealMethod().when(tableTopComponent).getCurrentState();
        
        final Graph graph = mock(Graph.class);
        final Graph currentGraph = mock(Graph.class);
        final ReadableGraph readableGraph = mock(ReadableGraph.class);
        
        when(graph.getReadableGraph()).thenReturn(readableGraph);
        when(readableGraph.getAttribute(GraphElementType.META, "table_view_state")).thenReturn(42);
        when(readableGraph.getObjectValue(42, 0)).thenReturn(null);
        when(tableTopComponent.getCurrentGraph()).thenReturn(currentGraph);
        
        try(final MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class)) {
            
            final PluginExecution pluginExecution = mock(PluginExecution.class);
            
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(UpdateStatePlugin.class)))
                    .thenAnswer(mockitoInvocation -> {
                        final UpdateStatePlugin plugin = (UpdateStatePlugin) mockitoInvocation.getArgument(0);
                        
                        assertEquals(new TableViewState(), plugin.getTableViewState());
                        
                        return pluginExecution;
                    });
            
            tableTopComponent.updateState(graph);
            
            verify(pluginExecution).executeLater(currentGraph);
        }
        
        assertEquals(new TableViewState(), tableTopComponent.getCurrentState());
        
        verify(graph).getReadableGraph();
        verify(readableGraph).release();
    }
    
    @Test
    public void showSelected() {
        final TableViewTopComponent tableTopComponent = mock(TableViewTopComponent.class);
        
        final TableViewState currentState = new TableViewState();
        final Graph currentGraph = mock(Graph.class);
        
        final TableViewState expectedNewState = new TableViewState();
        expectedNewState.setElementType(GraphElementType.META);
        expectedNewState.setSelectedOnly(true);
        
        final TableViewPane tablePane = mock(TableViewPane.class);
        
        final Table table = mock(Table.class);
        final ChangeListener<ObservableList<String>> tableSelectionListener = mock(ChangeListener.class);
        final ListChangeListener selectedOnlySelectionListener = mock(ListChangeListener.class);
        
        doCallRealMethod().when(tableTopComponent).showSelected(any(GraphElementType.class), anyInt());
        when(tableTopComponent.getCurrentState()).thenReturn(currentState);
        when(tableTopComponent.getCurrentGraph()).thenReturn(currentGraph);
        when(tableTopComponent.getTablePane()).thenReturn(tablePane);
        when(tableTopComponent.getExecutorService()).thenReturn(Executors.newSingleThreadExecutor());
        
        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getTableSelectionListener()).thenReturn(tableSelectionListener);
        when(tablePane.getSelectedOnlySelectionListener()).thenReturn(selectedOnlySelectionListener);
        
        try(final MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class)) {
            
            final PluginExecution pluginExecution = mock(PluginExecution.class);
            when(pluginExecution.executeLater(any(Graph.class)))
                    .thenReturn(CompletableFuture.completedFuture(null));
            
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(UpdateStatePlugin.class)))
                    .thenAnswer(mockitoInvocation -> {
                        final UpdateStatePlugin plugin = (UpdateStatePlugin) mockitoInvocation.getArgument(0);
                        
                        assertEquals(expectedNewState, plugin.getTableViewState());
                        
                        // Change the mock now that the "Update Plugin" has run
                        when(tableTopComponent.getCurrentState()).thenReturn(expectedNewState);
                        
                        return pluginExecution;
                    });
            
            final Future<?> updateTask = tableTopComponent.showSelected(GraphElementType.META, 42);
            
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
                    expectedNewState, 
                    tableSelectionListener, 
                    selectedOnlySelectionListener
            );
        }
    }
    
    @Test
    public void componentShowing() throws InterruptedException {
        final TableViewTopComponent tableTopComponent = mock(TableViewTopComponent.class);
        
        doCallRealMethod().when(tableTopComponent).componentShowing();
        doNothing().when(tableTopComponent).handleNewGraph(any(Graph.class));
        
        try (final MockedStatic<GraphManager> graphManagerMockedStatic
                = Mockito.mockStatic(GraphManager.class)) {
            final GraphManager graphManager = mock(GraphManager.class);
            final Graph activeGraph = mock(Graph.class);

            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            when(graphManager.getActiveGraph()).thenReturn(activeGraph);
            
            tableTopComponent.componentShowing();
            
            verify(tableTopComponent).handleNewGraph(activeGraph);
        }
    }
    
    @Test
    public void handleGraphClosed() throws InterruptedException {
        final TableViewTopComponent tableTopComponent = mock(TableViewTopComponent.class);
        final TableViewPane tablePane = mock(TableViewPane.class);
        final TableService tableService = mock(TableService.class);
        final Pagination pagination = mock(Pagination.class);
        
        final TablePreferences tablePreferences = new TablePreferences();
        tablePreferences.setMaxRowsPerPage(42);
        
        when(tableTopComponent.getTablePane()).thenReturn(tablePane);
        when(tablePane.getTableService()).thenReturn(tableService);
        when(tableService.getTablePreferences()).thenReturn(tablePreferences);
        when(tableService.getPagination()).thenReturn(pagination);
        
        doCallRealMethod().when(tableTopComponent).handleGraphClosed(any(Graph.class));
        
        tableTopComponent.handleGraphClosed(mock(Graph.class));
        
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();
        
        verify(tableService).updatePagination(42, null);
        verify(tablePane).setCenter(pagination);
    }
    
    @Test
    public void getColumnAttributeChanges() {
        final TableViewTopComponent tableTopComponent = mock(TableViewTopComponent.class);
        
        when(tableTopComponent.getRemovedAttributes(any(TableViewState.class), any(TableViewState.class)))
                .thenCallRealMethod();
        when(tableTopComponent.getAddedAttributes(any(TableViewState.class), any(TableViewState.class)))
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
        assertEquals(
                Set.of(
                        Tuple.create(".source", attribute1)
                ),
                tableTopComponent.getRemovedAttributes(oldState, newState)
        );
        
        // Added column attributes
        assertEquals(
                Set.of(
                        Tuple.create(".transaction", attribute3)
                ),
                tableTopComponent.getAddedAttributes(oldState, newState)
        );
        
    }
}
