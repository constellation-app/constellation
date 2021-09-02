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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities;
import au.gov.asd.tac.constellation.views.tableview2.components.Table;
import au.gov.asd.tac.constellation.views.tableview2.components.TableViewPane;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableSelectionListenerNGTest {
    private TableSelectionListener tableSelectionListener;
    
    private TableViewTopComponent tableTopComponent;
    private TableViewPane tablePane;
    private Table table;
    private TableView<ObservableList<String>> tableView;
    private TableService tableService;
    private Map<ObservableList<String>, Integer> rowToElementIdIndex;
    
    public TableSelectionListenerNGTest() {
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
        tablePane = mock(TableViewPane.class);
        table = mock(Table.class);
        tableView = mock(TableView.class);
        tableService = mock(TableService.class);
        
        rowToElementIdIndex = new HashMap<>();
        
        when(table.getParentComponent()).thenReturn(tablePane);
        when(table.getTableView()).thenReturn(tableView);
        
        when(tablePane.getParentComponent()).thenReturn(tableTopComponent);
        when(tablePane.getTableService()).thenReturn(tableService);
        
        when(tableService.getRowToElementIdIndex()).thenReturn(rowToElementIdIndex);
        
        tableSelectionListener = new TableSelectionListener(table);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void changedCurrentStateNull() {
        try (MockedStatic<TableViewUtilities> tableViewUtilitiesMockedStatic = Mockito.mockStatic(TableViewUtilities.class)) {
            when(tableTopComponent.getCurrentState()).thenReturn(null);

            tableSelectionListener.changed(null, null, null);

            tableViewUtilitiesMockedStatic.verifyNoInteractions();
        }
    }
    
    @Test
    public void changedCurrentStateSelectedOnlyModeTrue() {
        try (MockedStatic<TableViewUtilities> tableViewUtilitiesMockedStatic = Mockito.mockStatic(TableViewUtilities.class)) {
        
            final TableViewState currentState = new TableViewState();
            currentState.setSelectedOnly(true);

            when(tableTopComponent.getCurrentState()).thenReturn(currentState);

            tableSelectionListener.changed(null, null, null);

            tableViewUtilitiesMockedStatic.verifyNoInteractions();
        }
    }
    
    @Test
    public void changedCurrentStateSelectedOnlyModeFalse() {
        try (MockedStatic<TableViewUtilities> tableViewUtilitiesMockedStatic = Mockito.mockStatic(TableViewUtilities.class)) {
        
            final TableViewState currentState = new TableViewState();
            currentState.setSelectedOnly(false);
            currentState.setElementType(GraphElementType.META);

            final Graph graph = mock(Graph.class);
            
            when(tableTopComponent.getCurrentState()).thenReturn(currentState);
            when(tableTopComponent.getCurrentGraph()).thenReturn(graph);

            tableSelectionListener.changed(null, null, null);

            tableViewUtilitiesMockedStatic.verify(() -> 
                    TableViewUtilities.copySelectionToGraph(same(tableView), same(rowToElementIdIndex),
                            eq(GraphElementType.META), same(graph))
            );
        }
    }

}
