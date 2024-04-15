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
package au.gov.asd.tac.constellation.views.tableview.plugins;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class SelectionToGraphPluginNGTest {
    private static final Logger LOGGER = Logger.getLogger(SelectionToGraphPluginNGTest.class.getName());

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
    public void selectionToGraph() throws InterruptedException, PluginException {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        final TableView<ObservableList<String>> table = mock(TableView.class);
        final TableView.TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableView.TableViewSelectionModel.class);
        final GraphWriteMethods graph = mock(GraphWriteMethods.class);
        final Map<ObservableList<String>, Integer> index = new HashMap<>();

        index.put(row1, 1);
        index.put(row2, 2);

        // Two rows. Row 1 is selected
        when(table.getItems()).thenReturn(FXCollections.observableList(List.of(row1, row2)));
        when(table.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(FXCollections.observableList(List.of(row1)));

        final SelectionToGraphPlugin selectionToGraph
                = new SelectionToGraphPlugin(table, index, GraphElementType.VERTEX);

        selectionToGraph.edit(graph, null, null);

        verify(graph).setBooleanValue(0, 1, true);
        verify(graph).setBooleanValue(0, 2, false);

        assertEquals("Table View: Select on Graph", selectionToGraph.getName());
    }
    
    @Test
    public void selectionToGraphEdge() throws InterruptedException, PluginException {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        final TableView<ObservableList<String>> table = mock(TableView.class);
        final TableView.TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableView.TableViewSelectionModel.class);
        final GraphWriteMethods graph = mock(GraphWriteMethods.class);
        final Map<ObservableList<String>, Integer> index = new HashMap<>();

        index.put(row1, 1);
        index.put(row2, 2);

        // Two rows. Row 1 is selected
        when(table.getItems()).thenReturn(FXCollections.observableList(List.of(row1, row2)));
        when(table.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(FXCollections.observableList(List.of(row1)));
        
        when(VisualConcept.TransactionAttribute.SELECTED.ensure(graph)).thenReturn(1);
        // Specifiy transition count of each mocked element as 1
        when(graph.getEdgeTransactionCount(1)).thenReturn(1);
        when(graph.getEdgeTransactionCount(2)).thenReturn(1);
        
        when(graph.getEdgeTransaction(1, 0)).thenReturn(1);
        when(graph.getEdgeTransaction(2, 0)).thenReturn(2);

        final SelectionToGraphPlugin selectionToGraph
                = new SelectionToGraphPlugin(table, index, GraphElementType.EDGE);

        selectionToGraph.edit(graph, null, null);

        verify(graph).setBooleanValue(1, 1, true);
        verify(graph).setBooleanValue(1, 2, false);

        assertEquals("Table View: Select on Graph", selectionToGraph.getName());
    }
    
    @Test
    public void selectionToGraphLink() throws InterruptedException, PluginException {
        final ObservableList<String> row1 = FXCollections.observableList(List.of("row1Column1", "row1Column2"));
        final ObservableList<String> row2 = FXCollections.observableList(List.of("row2Column1", "row2Column2"));

        final TableView<ObservableList<String>> table = mock(TableView.class);
        final TableView.TableViewSelectionModel<ObservableList<String>> selectionModel = mock(TableView.TableViewSelectionModel.class);
        final GraphWriteMethods graph = mock(GraphWriteMethods.class);
        final Map<ObservableList<String>, Integer> index = new HashMap<>();

        index.put(row1, 1);
        index.put(row2, 2);

        // Two rows. Row 1 is selected
        when(table.getItems()).thenReturn(FXCollections.observableList(List.of(row1, row2)));
        when(table.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(FXCollections.observableList(List.of(row1)));
        
        when(VisualConcept.TransactionAttribute.SELECTED.ensure(graph)).thenReturn(1);
        // Specifiy transition count of each mocked element as 1
        when(graph.getLinkTransactionCount(1)).thenReturn(1);
        when(graph.getLinkTransactionCount(2)).thenReturn(1);
        
        when(graph.getLinkTransaction(1, 0)).thenReturn(1);
        when(graph.getLinkTransaction(2, 0)).thenReturn(2);

        final SelectionToGraphPlugin selectionToGraph
                = new SelectionToGraphPlugin(table, index, GraphElementType.LINK);

        selectionToGraph.edit(graph, null, null);

        verify(graph).setBooleanValue(1, 1, true);
        verify(graph).setBooleanValue(1, 2, false);

        assertEquals("Table View: Select on Graph", selectionToGraph.getName());
    }
}
