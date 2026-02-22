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
package au.gov.asd.tac.constellation.views.tableview.components;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class ElementTypeContextMenuNGTest {

    private static final Logger LOGGER = Logger.getLogger(ElementTypeContextMenuNGTest.class.getName());

    private TableViewTopComponent tableViewTopComponent;
    private TablePane tablePane;
    private Table table;
    private ActiveTableReference activeTableReference;
    private Graph graph;
    private ReadableGraph readableGraph;
    private TableViewState tableViewState;
    private ProgressBar progressBar;

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

    private ElementTypeContextMenu elementTypeContextMenu;

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
        activeTableReference = mock(ActiveTableReference.class);
        graph = mock(Graph.class);
        readableGraph = mock(ReadableGraph.class);

        tableViewState = new TableViewState();

        progressBar = mock(ProgressBar.class);

        when(graph.getReadableGraph()).thenReturn(readableGraph);

        // These two will define which columns are shown when the "Key Columns" button is pressed
        when(readableGraph.getPrimaryKey(GraphElementType.VERTEX)).thenReturn(new int[]{2, 3});
        when(readableGraph.getPrimaryKey(GraphElementType.TRANSACTION)).thenReturn(new int[]{5});

        // Define the columns available in the table
        // I think typically the value for Attribut.getAttribute and
        // TableColumn.getText will be the same. But for the purpose of these
        // tests they are different so it can be differentiated in the assertions
        columnType1 = "source.";
        when(readableGraph.getAttributeName(1)).thenReturn("Location Name");
        attribute1 = new GraphAttribute(readableGraph, 1);
        column1 = mock(TableColumn.class);
        when(column1.getText()).thenReturn("Text from Column 1");

        columnType2 = "destination.";
        when(readableGraph.getAttributeName(2)).thenReturn("Number of Visitors");
        attribute2 = new GraphAttribute(readableGraph, 2);
        column2 = mock(TableColumn.class);
        when(column2.getText()).thenReturn("Text from Column 2");

        columnType3 = "source.";
        when(readableGraph.getAttributeName(3)).thenReturn("personal notes");
        attribute3 = new GraphAttribute(readableGraph, 3);
        column3 = mock(TableColumn.class);
        when(column3.getText()).thenReturn("Text from Column 3");

        columnType4 = "transaction.";
        when(readableGraph.getAttributeName(4)).thenReturn("Related To");
        attribute4 = new GraphAttribute(readableGraph, 4);
        column4 = mock(TableColumn.class);
        when(column4.getText()).thenReturn("Text from Column 4");

        columnType5 = "transaction.";
        when(readableGraph.getAttributeName(5)).thenReturn("personal notes");
        attribute5 = new GraphAttribute(readableGraph, 5);
        column5 = mock(TableColumn.class);
        when(column5.getText()).thenReturn("Text from Column 5");

        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column(columnType1, attribute1, column1));
        columnIndex.add(new Column(columnType2, attribute2, column2));
        columnIndex.add(new Column(columnType3, attribute3, column3));
        columnIndex.add(new Column(columnType4, attribute4, column4));
        columnIndex.add(new Column(columnType5, attribute5, column5));

        when(table.getColumnIndex()).thenReturn(columnIndex);
        when(table.getParentComponent()).thenReturn(tablePane);

        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);
        when(tablePane.getParentComponent()).thenReturn(tableViewTopComponent);

        when(tableViewTopComponent.getCurrentGraph()).thenReturn(graph);
        when(tableViewTopComponent.getCurrentState()).thenReturn(tableViewState);
        when(tablePane.getProgressBar()).thenReturn(progressBar);

        elementTypeContextMenu = spy(new ElementTypeContextMenu(table));
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void allUIComponentsNullBeforeInit() {
        assertNull(elementTypeContextMenu.getContextMenu());
    }

    @Test
    public void testInit() {
        elementTypeContextMenu.init();
        assertNotNull(elementTypeContextMenu.getContextMenu());

        assertEquals(
                elementTypeContextMenu.getContextMenu().getItems(),
                FXCollections.observableList(
                        List.of(
                                elementTypeContextMenu.getVerticesMenu(),
                                elementTypeContextMenu.getTransactionsMenu(),
                                elementTypeContextMenu.getEdgesMenu(),
                                elementTypeContextMenu.getLinksMenu()
                        )
                )
        );

        // Verify handler actaully functions
        final ActionEvent actionEvent = mock(ActionEvent.class);
        elementTypeContextMenu.getTransactionsMenu().getOnAction().handle(actionEvent);
        verify(actionEvent).consume();
    }

}
