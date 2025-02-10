/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    private static final Logger LOGGER = Logger.getLogger(TableSelectionListenerNGTest.class.getName());

    private TableSelectionListener tableSelectionListener;

    private TableViewTopComponent tableViewTopComponent;
    private TablePane tablePane;
    private Table table;
    private TableView<ObservableList<String>> tableView;
    private ActiveTableReference activeTableReference;
    private Map<ObservableList<String>, Integer> rowToElementIdIndex;

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

        rowToElementIdIndex = new HashMap<>();

        when(table.getParentComponent()).thenReturn(tablePane);
        when(table.getTableView()).thenReturn(tableView);

        when(tablePane.getParentComponent()).thenReturn(tableViewTopComponent);
        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);

        when(activeTableReference.getRowToElementIdIndex()).thenReturn(rowToElementIdIndex);

        tableSelectionListener = new TableSelectionListener(table);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    @Test
    public void changedCurrentStateNull() {
        try (MockedStatic<TableViewUtilities> tableViewUtilitiesMockedStatic = Mockito.mockStatic(TableViewUtilities.class)) {
            when(tableViewTopComponent.getCurrentState()).thenReturn(null);

            tableSelectionListener.changed(null, null, null);

            tableViewUtilitiesMockedStatic.verifyNoInteractions();
        }
    }

    @Test
    public void changedCurrentStateSelectedOnlyModeTrue() {
        try (MockedStatic<TableViewUtilities> tableViewUtilitiesMockedStatic = Mockito.mockStatic(TableViewUtilities.class)) {
            final TableViewState currentState = new TableViewState();
            currentState.setSelectedOnly(true);

            when(tableViewTopComponent.getCurrentState()).thenReturn(currentState);

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

            when(tableViewTopComponent.getCurrentState()).thenReturn(currentState);
            when(tableViewTopComponent.getCurrentGraph()).thenReturn(graph);

            tableSelectionListener.changed(null, null, null);

            tableViewUtilitiesMockedStatic.verify(() -> TableViewUtilities.copySelectionToGraph(same(tableView), same(rowToElementIdIndex), eq(GraphElementType.META), same(graph)));
        }
    }
}
