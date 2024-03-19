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
package au.gov.asd.tac.constellation.views.tableview.tasks;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.api.UpdateMethod;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class UpdateColumnsTaskNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(UpdateColumnsTaskNGTest.class.getName());

    private TableViewTopComponent tableViewTopComponent;
    private TableView<ObservableList<String>> tableView;
    private TablePane tablePane;
    private Table table;
    private ActiveTableReference activeTableReference;

    private ChangeListener<ObservableList<String>> tableSelectionListener;
    private ListChangeListener selectedOnlySelectionListener;

    private TableViewSelectionModel<ObservableList<String>> selectionModel;
    private ReadOnlyObjectProperty<ObservableList<String>> selectedItemProperty;
    private ObservableList<ObservableList<String>> selectedItems;

    private String columnType1;
    private String columnType2;
    private String columnType3;
    private String columnType4;
    private String columnType5;

    private Attribute attribute1;
    private Attribute attribute2;
    private Attribute attribute3;
    private Attribute attribute4;
    private Attribute attribute5;

    private TableColumn<ObservableList<String>, String> column1;
    private TableColumn<ObservableList<String>, String> column2;
    private TableColumn<ObservableList<String>, String> column3;
    private TableColumn<ObservableList<String>, String> column4;
    private TableColumn<ObservableList<String>, String> column5;

    private UpdateColumnsTask updateColumnsTask;

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
        tableViewTopComponent = mock(TableViewTopComponent.class);
        tableView = mock(TableView.class);
        tablePane = mock(TablePane.class);
        table = mock(Table.class);
        activeTableReference = mock(ActiveTableReference.class);
        selectionModel = mock(TableViewSelectionModel.class);
        selectedItemProperty = mock(ReadOnlyObjectProperty.class);
        selectedItems = mock(ObservableList.class);
        tableSelectionListener = mock(ChangeListener.class);
        selectedOnlySelectionListener = mock(ListChangeListener.class);

        columnType1 = "source.";
        attribute1 = mock(Attribute.class);
        column1 = mock(TableColumn.class);

        columnType2 = "destination.";
        attribute2 = mock(Attribute.class);
        column2 = mock(TableColumn.class);

        columnType3 = "source.";
        attribute3 = mock(Attribute.class);
        column3 = mock(TableColumn.class);

        columnType4 = "transaction.";
        attribute4 = mock(Attribute.class);
        column4 = mock(TableColumn.class);

        columnType5 = "transaction.";
        attribute5 = mock(Attribute.class);
        column5 = mock(TableColumn.class);

        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column(columnType1, attribute1, column1));
        columnIndex.add(new Column(columnType2, attribute2, column2));
        columnIndex.add(new Column(columnType3, attribute3, column3));
        columnIndex.add(new Column(columnType4, attribute4, column4));
        columnIndex.add(new Column(columnType5, attribute5, column5));

        when(tableViewTopComponent.getTablePane()).thenReturn(tablePane);

        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);
        when(tablePane.getParentComponent()).thenReturn(tableViewTopComponent);

        when(activeTableReference.getColumnIndex()).thenReturn(columnIndex);

        when(table.getTableView()).thenReturn(tableView);
        when(table.getSelectedOnlySelectionListener()).thenReturn(selectedOnlySelectionListener);
        when(table.getTableSelectionListener()).thenReturn(tableSelectionListener);
        when(table.getParentComponent()).thenReturn(tablePane);

        when(tableView.getSelectionModel()).thenReturn(selectionModel);

        when(selectionModel.selectedItemProperty()).thenReturn(selectedItemProperty);
        when(selectionModel.getSelectedItems()).thenReturn(selectedItems);

        updateColumnsTask = spy(new UpdateColumnsTask(table));
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void runWithoutReset() {
        updateColumnsTask.run();
    }

    @Test
    public void run() {
        final TableViewState tableViewState = new TableViewState();
        tableViewState.setColumnAttributes(List.of(
                Tuple.create(columnType1, attribute1),
                Tuple.create(columnType4, attribute4),
                Tuple.create(columnType5, attribute5)
        ));

        final Map<String, TableColumn<ObservableList<String>, String>> columnReferenceMap = new HashMap<>();

        columnReferenceMap.put("column header 1", column1);
        columnReferenceMap.put("column header 2", column3);

        updateColumnsTask.reset(columnReferenceMap, tableViewState);

        final List<TableColumn<ObservableList<String>, ?>> columnList = new ArrayList<>();
        columnList.add(mock(TableColumn.class));
        columnList.add(mock(TableColumn.class));

        final ObservableList<TableColumn<ObservableList<String>, ?>> columns
                = spy(FXCollections.observableList(columnList));
        when(tableView.getColumns()).thenReturn(columns);

        updateColumnsTask.run();

        // Verify that the listeners were removed
        verify(selectedItemProperty).removeListener(tableSelectionListener);
        verify(selectedItems).removeListener(selectedOnlySelectionListener);

        // Verify that only the columns in columnReferenceMap have their
        // graphic set to null
        verify(column1).setGraphic(isNull());
        verify(column2, times(0)).setGraphic(isNull());
        verify(column3).setGraphic(isNull());
        verify(column4, times(0)).setGraphic(isNull());
        verify(column5, times(0)).setGraphic(isNull());

        // Visibility set based on what columns are present in table view state
        verify(column1).setVisible(true);
        verify(column2).setVisible(false);
        verify(column3).setVisible(false);
        verify(column4).setVisible(true);
        verify(column5).setVisible(true);

        // Verify that the existing columns are cleared and replaced with what is in
        // the columnIndex
        assertEquals(List.of(column1, column2, column3, column4, column5), columns);

        final ArgumentCaptor<ListChangeListener<TableColumn<ObservableList<String>, ?>>> captor = ArgumentCaptor.forClass(ListChangeListener.class);

        verify(columns).addListener(captor.capture());
        assertNotNull(captor.getValue());
        verifyColumnChangeListenerAction(captor.getValue());
        verifyColumnChangeListenerAvoidsDuplicateActions(captor.getValue());

        // Verify that the listeners are added
        verify(selectedItemProperty).addListener(tableSelectionListener);
        verify(selectedItems).addListener(selectedOnlySelectionListener);
    }

    @Test
    public void saveSortDetails() {

        // Null sort order
        when(tableView.getSortOrder()).thenReturn(null);

        updateColumnsTask.saveSortDetails();

        verify(activeTableReference).saveSortDetails("", TableColumn.SortType.ASCENDING);

        reset(activeTableReference);

        // Empty sort order
        when(tableView.getSortOrder()).thenReturn(FXCollections.observableList(Collections.emptyList()));

        updateColumnsTask.saveSortDetails();

        verify(activeTableReference).saveSortDetails("", TableColumn.SortType.ASCENDING);

        reset(activeTableReference);

        // Valid sort order
        final TableColumn<ObservableList<String>, String> column = mock(TableColumn.class);
        when(column.getText()).thenReturn("COLUMN_NAME");
        when(column.getSortType()).thenReturn(TableColumn.SortType.DESCENDING);
        when(tableView.getSortOrder()).thenReturn(FXCollections.observableList(List.of(column)));

        updateColumnsTask.saveSortDetails();

        verify(activeTableReference).saveSortDetails("COLUMN_NAME", TableColumn.SortType.DESCENDING);
    }

    /**
     * Verifies that the listener maintains state and if the last change is
     * equal to the current one then the change is not iterated over.
     *
     * @param listener the listener to be tested
     */
    private void verifyColumnChangeListenerAvoidsDuplicateActions(final ListChangeListener<TableColumn<ObservableList<String>, ?>> listener) {
        final ListChangeListener.Change<TableColumn<ObservableList<String>, String>> change
                = mock(ListChangeListener.Change.class);

        when(change.next())
                .thenReturn(false);

        listener.onChanged(change);
        listener.onChanged(change);

        // Next is only called once because the same change is passed each time
        verify(change).next();
    }

    /**
     * Sets up mocks so that the change will be iterated 3 times. For only one
     * iteration (the final one) will the code be able to pass the conditional
     * that allows an update to occur.
     *
     * @param listener the listener to be tested
     */
    private void verifyColumnChangeListenerAction(final ListChangeListener<TableColumn<ObservableList<String>, ?>> listener) {
        final ListChangeListener.Change<TableColumn<ObservableList<String>, String>> change
                = mock(ListChangeListener.Change.class);

        // It will iterate 3 times. But only get past the if statement once
        when(change.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(change.wasReplaced())
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(true);

        when(change.getRemovedSize())
                .thenReturn(10)
                .thenReturn(5)
                .thenReturn(5);

        when(change.getAddedSize())
                .thenReturn(5)
                .thenReturn(5)
                .thenReturn(5);

        doNothing().when(updateColumnsTask).saveSortDetails();

        when(change.getAddedSubList()).thenReturn(List.of(column2, column3, column5));

        final Graph graph = mock(Graph.class);
        when(tableViewTopComponent.getCurrentGraph()).thenReturn(graph);

        final TableViewState tableViewState = new TableViewState();
        tableViewState.setColumnAttributes(List.of(
                Tuple.create(columnType2, attribute2),
                Tuple.create(columnType4, attribute4),
                Tuple.create(columnType5, attribute5)
        ));
        when(tableViewTopComponent.getCurrentState()).thenReturn(tableViewState);

        listener.onChanged(change);

        // This should only be called ONCE due to the conditionals on the other
        // two loops
        verify(activeTableReference).updateVisibleColumns(
                graph,
                tableViewState,
                List.of(
                        Tuple.create(columnType2, attribute2),
                        Tuple.create(columnType5, attribute5)
                ),
                UpdateMethod.REPLACE
        );
        verify(updateColumnsTask).saveSortDetails();
    }
}
