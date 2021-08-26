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
package au.gov.asd.tac.constellation.views.tableview2.listeners;

import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private SelectedOnlySelectionListener selectedOnlySelectionListener;
    
    private TableViewTopComponent tableTopComponent;
    private TableView<ObservableList<String>> tableView;
    private Set<ObservableList<String>> selectedOnlySelectedRows;
    
    public SelectedOnlySelectionListenerNGTest() {
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
        tableTopComponent = mock(TableViewTopComponent.class);
        tableView = mock(TableView.class);
        
        selectedOnlySelectedRows = new HashSet<>();
        
        selectedOnlySelectionListener = new SelectedOnlySelectionListener(tableTopComponent,
                tableView, selectedOnlySelectedRows);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void onChangedCurrentStateNull() {
        when(tableTopComponent.getCurrentState()).thenReturn(null);
        
        selectedOnlySelectionListener.onChanged(null);
        
        assertTrue(selectedOnlySelectedRows.isEmpty());
    }
    
    @Test
    public void onChangedCurrentStateSelectedOnlyModeFalse() {
        final TableViewState currentState = new TableViewState();
        currentState.setSelectedOnly(false);
        
        when(tableTopComponent.getCurrentState()).thenReturn(currentState);
        
        selectedOnlySelectionListener.onChanged(null);
        
        assertTrue(selectedOnlySelectedRows.isEmpty());
    }
    
    @Test
    public void onChanged() {
        final TableViewState currentState = new TableViewState();
        currentState.setSelectedOnly(true);
        
        final TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableViewSelectionModel.class);
        
        when(tableTopComponent.getCurrentState()).thenReturn(currentState);
        
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
