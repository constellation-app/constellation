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
package au.gov.asd.tac.constellation.views.tableview2.service;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview2.factory.TableViewPageFactory;
import au.gov.asd.tac.constellation.views.tableview2.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.tuple.ImmutablePair;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableServiceNGTest {
    private SortedList<ObservableList<String>> sortedRowList;
    private Map<Integer, ObservableList<String>> elementIdToRowIndex;
    private Map<ObservableList<String>, Integer> rowToElementIdIndex;
    private TableViewPageFactory pageFactory;
    
    private TableService tableService;
    
    public TableServiceNGTest() {
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
        sortedRowList = new SortedList<>(FXCollections.observableArrayList());
        rowToElementIdIndex = new HashMap<>();
        elementIdToRowIndex = new HashMap<>();
        
        pageFactory = mock(TableViewPageFactory.class);
        
        tableService = new TableService(sortedRowList, elementIdToRowIndex, rowToElementIdIndex, pageFactory);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void init() {
        assertNotNull(tableService.getPagination());
        assertNotNull(tableService.getSelectedOnlySelectedRows());
        
        assertEquals(Integer.valueOf(500), tableService.getTablePreferences().getMaxRowsPerPage());
    }
    
    @Test
    public void saveSortDetails() {
        assertEquals(
                ImmutablePair.of("", TableColumn.SortType.ASCENDING), 
                tableService.getTablePreferences().getSortByColumn()
        );
        
        tableService.saveSortDetails("ABC", TableColumn.SortType.DESCENDING);
     
        assertEquals(
                ImmutablePair.of("ABC", TableColumn.SortType.DESCENDING), 
                tableService.getTablePreferences().getSortByColumn()
        );
    }
    
    @Test
    public void updateVisibleColumnsAdd() {
        try(MockedStatic<PluginExecution> pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class)) {
            final Graph graph = mock(Graph.class);
            final Attribute attribute = mock(Attribute.class);
            final PluginExecution pluginExecution = mock(PluginExecution.class);

            final List<Tuple<String, Attribute>> paramColumnAttributes = List.of(
                    Tuple.create("paramAttr", attribute)
            );

            final List<Tuple<String, Attribute>> stateColumnAttributes = List.of(
                    Tuple.create("stateAttr", attribute),
                    Tuple.create("paramAttr", attribute)
            );

            final TableViewState tableViewState = new TableViewState();
            tableViewState.setColumnAttributes(stateColumnAttributes);

            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(Plugin.class)))
                    .thenAnswer(executeUpdateStatePlugin(
                            pluginExecution, 
                            tableViewState, 
                            List.of(
                                    Tuple.create("stateAttr", attribute),
                                    Tuple.create("paramAttr", attribute),
                                    Tuple.create("paramAttr", attribute)
                            )
                    ));
            
            tableService.updateVisibleColumns(graph, tableViewState, paramColumnAttributes,
                    UpdateMethod.ADD);
            
            verify(pluginExecution).executeLater(graph);
        }
    }
    
    @Test
    public void updateVisibleColumnsRemove() {
        try(MockedStatic<PluginExecution> pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class)) {
            final Graph graph = mock(Graph.class);
            final Attribute attribute = mock(Attribute.class);
            final PluginExecution pluginExecution = mock(PluginExecution.class);

            final List<Tuple<String, Attribute>> paramColumnAttributes = List.of(
                    Tuple.create("paramAttr", attribute)
            );

            final List<Tuple<String, Attribute>> stateColumnAttributes = List.of(
                    Tuple.create("stateAttr", attribute),
                    Tuple.create("paramAttr", attribute)
            );

            final TableViewState tableViewState = new TableViewState();
            tableViewState.setColumnAttributes(stateColumnAttributes);

            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(Plugin.class)))
                    .thenAnswer(executeUpdateStatePlugin(
                            pluginExecution, 
                            tableViewState, 
                            List.of(Tuple.create("stateAttr", attribute))
                    ));
            
            tableService.updateVisibleColumns(graph, tableViewState, paramColumnAttributes,
                    UpdateMethod.REMOVE);
            
            verify(pluginExecution).executeLater(graph);
        }
    }
    
    @Test
    public void updateVisibleColumnsReplace() {
        try(MockedStatic<PluginExecution> pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class)) {
            final Graph graph = mock(Graph.class);
            final Attribute attribute = mock(Attribute.class);
            final PluginExecution pluginExecution = mock(PluginExecution.class);

            final List<Tuple<String, Attribute>> paramColumnAttributes = List.of(
                    Tuple.create("paramAttr", attribute)
            );

            final List<Tuple<String, Attribute>> stateColumnAttributes = List.of(
                    Tuple.create("stateAttr1", attribute),
                    Tuple.create("stateAttr2", attribute)
            );

            final TableViewState tableViewState = new TableViewState();
            tableViewState.setColumnAttributes(stateColumnAttributes);
            
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(any(Plugin.class)))
                    .thenAnswer(executeUpdateStatePlugin(
                            pluginExecution, 
                            tableViewState, 
                            List.of(Tuple.create("paramAttr", attribute))
                    ));
            
            tableService.updateVisibleColumns(graph, tableViewState, paramColumnAttributes,
                    UpdateMethod.REPLACE);
            
            verify(pluginExecution).executeLater(graph);
        }
    }
    
    @Test
    public void updatePaginationNoNewRowList() {
        final TableService spiedTableService = spy(tableService);
        
        final Pagination pagination = mock(Pagination.class);
        
        doReturn(pagination).when(spiedTableService)
                .updatePagination(eq(22), same(sortedRowList));
        
        assertSame(pagination, spiedTableService.updatePagination(22));
    }
    
    @Test(expectedExceptions = NullPointerException.class)
    public void updatePaginationPageFactoryNotSet() {
        final TableService localTableService = new TableService(sortedRowList,
                elementIdToRowIndex, rowToElementIdIndex, null);
        localTableService.updatePagination(22, sortedRowList);
    }
    
    @Test
    public void updatePagination() {
        final List<ObservableList<String>> newRowList = IntStream.range(0, 45)
                                .mapToObj(i -> FXCollections.observableList(List.of(Integer.toString(i))))
                                .collect(Collectors.toList());
        
        final Pagination pagination = tableService.updatePagination(22, newRowList);
        
        assertEquals(3, pagination.getPageCount());
        assertSame(pageFactory, pagination.getPageFactory());
        verify(pageFactory).update(same(newRowList), same(elementIdToRowIndex), eq(22));
    }
    
    @Test
    public void updatePaginationNewRowListNull() {
        final List<ObservableList<String>> newRowList = null;
        
        final Pagination pagination = tableService.updatePagination(22, newRowList);
        
        assertEquals(1, pagination.getPageCount());
        assertSame(pageFactory, pagination.getPageFactory());
        verify(pageFactory).update(same(newRowList), same(elementIdToRowIndex), eq(22));
    }
    
    @Test
    public void updatePaginationNewRowListEmpty() {
        final List<ObservableList<String>> newRowList = new ArrayList<>();
        
        final Pagination pagination = tableService.updatePagination(22, newRowList);
        
        assertEquals(1, pagination.getPageCount());
        assertSame(pageFactory, pagination.getPageFactory());
        verify(pageFactory).update(same(newRowList), same(elementIdToRowIndex), eq(22));
    }
    
    private Answer<PluginExecution> executeUpdateStatePlugin(final PluginExecution pluginExecution,
                                                             final TableViewState originalTableViewState,
                                                             final List<Tuple<String, Attribute>> expectedColumnAttributes) {
        return (InvocationOnMock mockInvocation) -> {
            final UpdateStatePlugin updateStatePlugin
                    = (UpdateStatePlugin) mockInvocation.getArgument(0);
            
            // This table state should be a copy
            assertNotSame(originalTableViewState, updateStatePlugin.getTableViewState());
            
            assertEquals(expectedColumnAttributes,
                    updateStatePlugin.getTableViewState().getColumnAttributes());
            
            return pluginExecution;
        };
    }
    
    
}
