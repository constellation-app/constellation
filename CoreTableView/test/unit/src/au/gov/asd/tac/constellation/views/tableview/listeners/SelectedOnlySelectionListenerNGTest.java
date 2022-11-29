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
package au.gov.asd.tac.constellation.views.tableview.listeners;

import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class SelectedOnlySelectionListenerNGTest {
    private static final Logger LOGGER = Logger.getLogger(SelectedOnlySelectionListenerNGTest.class.getName());

    private SelectedOnlySelectionListener selectedOnlySelectionListener;

    private TableViewTopComponent tableViewTopComponent;
    private TablePane tablePane;
    private Table table;
    private TableView<ObservableList<String>> tableView;
    private ActiveTableReference activeTableReference;
    private Set<ObservableList<String>> selectedOnlySelectedRows;

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
        tablePane = mock(TablePane.class);
        table = mock(Table.class);
        tableView = mock(TableView.class);
        activeTableReference = mock(ActiveTableReference.class);

        selectedOnlySelectedRows = new HashSet<>();

        when(table.getParentComponent()).thenReturn(tablePane);
        when(table.getTableView()).thenReturn(tableView);

        when(tablePane.getParentComponent()).thenReturn(tableViewTopComponent);
        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);

        when(activeTableReference.getSelectedOnlySelectedRows()).thenReturn(selectedOnlySelectedRows);

        selectedOnlySelectionListener = new SelectedOnlySelectionListener(table);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void onChangedCurrentStateNull() {
        when(tableViewTopComponent.getCurrentState()).thenReturn(null);

        selectedOnlySelectionListener.onChanged(null);

        assertTrue(selectedOnlySelectedRows.isEmpty());
    }

    @Test
    public void onChangedCurrentStateSelectedOnlyModeFalse() {
        final TableViewState currentState = new TableViewState();
        currentState.setSelectedOnly(false);

        when(tableViewTopComponent.getCurrentState()).thenReturn(currentState);

        selectedOnlySelectionListener.onChanged(null);

        assertTrue(selectedOnlySelectedRows.isEmpty());
    }

    @Test
    public void onChanged() {
        final TableViewState currentState = new TableViewState();
        currentState.setSelectedOnly(true);

        final TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableViewSelectionModel.class);

        when(tableViewTopComponent.getCurrentState()).thenReturn(currentState);

        // 4 rows. Rows 1 and 3 are currently selected. Row 2 was previously selected. Row 4 is not
        // selected and never was.
        final ObservableList<ObservableList<String>> items = FXCollections.observableList(
                List.of(
                        FXCollections.observableList(List.of("row1Column1", "row1Column2")),
                        FXCollections.observableList(List.of("row2Column1", "row2Column2")),
                        FXCollections.observableList(List.of("row3Column1", "row3Column2")),
                        FXCollections.observableList(List.of("row4Column1", "row4Column2"))
                )
        );

        final ObservableList<ObservableList<String>> selectedItems = FXCollections.observableList(
                List.of(
                        FXCollections.observableList(List.of("row1Column1", "row1Column2")),
                        FXCollections.observableList(List.of("row3Column1", "row3Column2"))
                )
        );

        selectedOnlySelectedRows.add(FXCollections.observableList(List.of("row2Column1", "row2Column2")));

        when(tableView.getItems()).thenReturn(items);

        when(tableView.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(selectedItems);

        selectedOnlySelectionListener.onChanged(null);

        final Set<ObservableList<String>> expectedSelectedRows = Set.of(
                FXCollections.observableList(List.of("row1Column1", "row1Column2")),
                FXCollections.observableList(List.of("row3Column1", "row3Column2"))
        );

        assertEquals(expectedSelectedRows, selectedOnlySelectedRows);
    }
}
