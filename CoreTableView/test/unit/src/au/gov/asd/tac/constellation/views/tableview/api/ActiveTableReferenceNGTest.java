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
package au.gov.asd.tac.constellation.views.tableview.api;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.factory.TableViewPageFactory;
import au.gov.asd.tac.constellation.views.tableview.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ActiveTableReferenceNGTest {
    private static final Logger LOGGER = Logger.getLogger(ActiveTableReferenceNGTest.class.getName());

    private SortedList<ObservableList<String>> sortedRowList;
    private Map<Integer, ObservableList<String>> elementIdToRowIndex;
    private Map<ObservableList<String>, Integer> rowToElementIdIndex;
    private TableViewPageFactory pageFactory;

    private ActiveTableReference activeTableReference;

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

    @BeforeMethod
    public void setUpMethod() throws Exception {
        sortedRowList = new SortedList<>(FXCollections.observableArrayList());
        rowToElementIdIndex = new HashMap<>();
        elementIdToRowIndex = new HashMap<>();

        pageFactory = mock(TableViewPageFactory.class);

        activeTableReference = spy(new ActiveTableReference(pageFactory));

        doReturn(sortedRowList).when(activeTableReference).getSortedRowList();
        doReturn(rowToElementIdIndex).when(activeTableReference).getRowToElementIdIndex();
        doReturn(elementIdToRowIndex).when(activeTableReference).getElementIdToRowIndex();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Verify the initial state of all accessible properties.
     */
    @Test
    public void init() {
        doCallRealMethod().when(activeTableReference).getSortedRowList();
        doCallRealMethod().when(activeTableReference).getRowToElementIdIndex();
        doCallRealMethod().when(activeTableReference).getElementIdToRowIndex();

        assertEquals(new SortedList<>(FXCollections.observableArrayList()), activeTableReference.getSortedRowList());
        assertEquals(new HashMap<>(), activeTableReference.getRowToElementIdIndex());
        assertEquals(new HashMap<>(), activeTableReference.getElementIdToRowIndex());

        assertNotNull(activeTableReference.getPagination());
        assertNotNull(activeTableReference.getSelectedOnlySelectedRows());

        assertEquals(500, activeTableReference.getUserTablePreferences().getMaxRowsPerPage());
    }

    /**
     * Verifies save sort detail updates the user table preferences correctly.
     */
    @Test
    public void saveSortDetails() {
        assertEquals(ImmutablePair.of("", TableColumn.SortType.ASCENDING),
                activeTableReference.getUserTablePreferences().getSortByColumn()
        );

        activeTableReference.saveSortDetails("ABC", TableColumn.SortType.DESCENDING);

        assertEquals(ImmutablePair.of("ABC", TableColumn.SortType.DESCENDING),
                activeTableReference.getUserTablePreferences().getSortByColumn()
        );
    }

    @Test
    public void updateVisibleColumnsAdd() {
        try (MockedStatic<PluginExecution> pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class)) {
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

            activeTableReference.updateVisibleColumns(graph, tableViewState, paramColumnAttributes,
                    UpdateMethod.ADD);

            verify(pluginExecution).executeLater(graph);
        }
    }

    @Test
    public void updateVisibleColumnsRemove() {
        try (MockedStatic<PluginExecution> pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class)) {
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

            activeTableReference.updateVisibleColumns(graph, tableViewState, paramColumnAttributes,
                    UpdateMethod.REMOVE);

            verify(pluginExecution).executeLater(graph);
        }
    }

    @Test
    public void updateVisibleColumnsReplace() {
        try (MockedStatic<PluginExecution> pluginExecutionMockedStatic = Mockito.mockStatic(PluginExecution.class)) {
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

            activeTableReference.updateVisibleColumns(graph, tableViewState, paramColumnAttributes,
                    UpdateMethod.REPLACE);

            verify(pluginExecution).executeLater(graph);
        }
    }

    @Test
    public void updatePaginationNoNewRowList() {
        final Pagination pagination = mock(Pagination.class);
        final TablePane tablePane = mock(TablePane.class);

        doReturn(pagination).when(activeTableReference)
                .updatePagination(eq(22), same(sortedRowList), same(tablePane));

        assertSame(pagination, activeTableReference.updatePagination(22, tablePane));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void updatePaginationPageFactoryNotSet() {
        final ActiveTableReference localTableService = new ActiveTableReference(null);
        localTableService.updatePagination(22, sortedRowList, mock(TablePane.class));
    }

    @Test
    public void updatePagination() throws InterruptedException {
        final List<ObservableList<String>> newRowList = IntStream.range(0, 45)
                .mapToObj(i -> FXCollections.observableList(List.of(Integer.toString(i))))
                .collect(Collectors.toList());
        final TablePane tablePane = mock(TablePane.class);

        final Pagination pagination = activeTableReference.updatePagination(22, newRowList, tablePane);

        assertEquals(3, pagination.getPageCount());
        assertSame(pageFactory, pagination.getPageFactory());
        verify(pageFactory).update(same(newRowList), eq(22));

        // This verification is dependent on code completing in the UI thread so
        // the following ensures that the verification does not occur until
        // the required code is run.
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();

        verify(tablePane).setCenter(same(pagination));
    }

    @Test
    public void updatePaginationNewRowListNull() throws InterruptedException {
        final List<ObservableList<String>> newRowList = null;
        final TablePane tablePane = mock(TablePane.class);

        final Pagination pagination = activeTableReference.updatePagination(22, newRowList, tablePane);

        assertEquals(1, pagination.getPageCount());
        assertSame(pageFactory, pagination.getPageFactory());
        verify(pageFactory).update(same(newRowList), eq(22));

        // This verification is dependent on code completing in the UI thread so
        // the following ensures that the verification does not occur until
        // the required code is run.
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();

        verify(tablePane).setCenter(same(pagination));
    }

    @Test
    public void updatePaginationNewRowListEmpty() throws InterruptedException {
        final List<ObservableList<String>> newRowList = new ArrayList<>();
        final TablePane tablePane = mock(TablePane.class);

        final Pagination pagination = activeTableReference.updatePagination(22, newRowList, tablePane);

        assertEquals(1, pagination.getPageCount());
        assertSame(pageFactory, pagination.getPageFactory());
        verify(pageFactory).update(same(newRowList), eq(22));

        // This verification is dependent on code completing in the UI thread so
        // the following ensures that the verification does not occur until
        // the required code is run.
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();

        verify(tablePane).setCenter(same(pagination));
    }

    /**
     * Verifies the correct column attributes are updated when null null null     {@link ActiveTableReference#updateVisibleColumns(Graph, TableViewState, List, UpdateMethod)
     * is called.
     *
     * @param pluginExecution the plugin execution that will execut the update state plugin
     * @param originalTableViewState the old table state that is current before
     * the update plugin runs
     * @param expectedColumnAttributes the expected column attributes that will
     * be in the new state
     * @return a mockito answer that can be executed when the plugin is passed
     * to the plugin executor
     */
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
