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

import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.api.UpdateMethod;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ColumnVisibilityContextMenuNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(ColumnVisibilityContextMenuNGTest.class.getName());

    private TableViewTopComponent tableViewTopComponent;
    private TablePane tablePane;
    private Table table;
    private ActiveTableReference activeTableReference;
    private Graph graph;
    private ReadableGraph readableGraph;
    private TableViewState tableViewState;
    private Schema schema;

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

    private ColumnVisibilityContextMenu columnVisibilityContextMenu;

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
        schema = mock(Schema.class);      

        tableViewState = new TableViewState();

        when(graph.getReadableGraph()).thenReturn(readableGraph);
        when(graph.getSchema()).thenReturn(schema);

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

        columnVisibilityContextMenu = spy(new ColumnVisibilityContextMenu(table));
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    @Test
    public void allUIComponentsNullBeforeInit() {
        assertNull(columnVisibilityContextMenu.getContextMenu());
        assertNull(columnVisibilityContextMenu.getShowAllColumnsMenu());
        assertNull(columnVisibilityContextMenu.getShowDefaultColumnsMenu());
        assertNull(columnVisibilityContextMenu.getShowPrimaryColumnsMenu());
        assertNull(columnVisibilityContextMenu.getHideAllColumnsMenu());
        assertNull(columnVisibilityContextMenu.getSourceVertexColumnsMenu());
        assertNull(columnVisibilityContextMenu.getDestinationVertexColumnMenu());
        assertNull(columnVisibilityContextMenu.getTransactionColumnMenu());
    }

    @Test
    public void createExportButtons() {
        // To remove extra complexity from this test it will stub the createColumnVisibilityMenuItem
        // method and test it separately
        final CustomMenuItem columnVisibilityMenuItemColumn1 = mock(CustomMenuItem.class);
        final CustomMenuItem columnVisibilityMenuItemColumn2 = mock(CustomMenuItem.class);
        final CustomMenuItem columnVisibilityMenuItemColumn3 = mock(CustomMenuItem.class);
        final CustomMenuItem columnVisibilityMenuItemColumn4 = mock(CustomMenuItem.class);
        final CustomMenuItem columnVisibilityMenuItemColumn5 = mock(CustomMenuItem.class);

        doReturn(columnVisibilityMenuItemColumn1).when(columnVisibilityContextMenu)
                .createColumnVisibilityMenu(new Column(columnType1, attribute1, column1));

        doReturn(columnVisibilityMenuItemColumn2).when(columnVisibilityContextMenu)
                .createColumnVisibilityMenu(new Column(columnType2, attribute2, column2));

        doReturn(columnVisibilityMenuItemColumn3).when(columnVisibilityContextMenu)
                .createColumnVisibilityMenu(new Column(columnType3, attribute3, column3));

        doReturn(columnVisibilityMenuItemColumn4).when(columnVisibilityContextMenu)
                .createColumnVisibilityMenu(new Column(columnType4, attribute4, column4));

        doReturn(columnVisibilityMenuItemColumn5).when(columnVisibilityContextMenu)
                .createColumnVisibilityMenu(new Column(columnType5, attribute5, column5));

        columnVisibilityContextMenu.init();

        assertNotNull(columnVisibilityContextMenu.getContextMenu());
        assertNotNull(columnVisibilityContextMenu.getShowAllColumnsMenu());
        assertNotNull(columnVisibilityContextMenu.getShowDefaultColumnsMenu());
        assertNotNull(columnVisibilityContextMenu.getShowPrimaryColumnsMenu());
        assertNotNull(columnVisibilityContextMenu.getHideAllColumnsMenu());
        assertNotNull(columnVisibilityContextMenu.getSourceVertexColumnsMenu());
        assertNotNull(columnVisibilityContextMenu.getDestinationVertexColumnMenu());
        assertNotNull(columnVisibilityContextMenu.getTransactionColumnMenu());

        // No equality in separator so we have to pull it out and place it back in
        final Optional<MenuItem> separator = columnVisibilityContextMenu.getContextMenu().getItems().stream()
                .filter(SeparatorMenuItem.class::isInstance)
                .findFirst();

        assertTrue(separator.isPresent());

        assertEquals(
                FXCollections.observableList(
                        List.of(
                                columnVisibilityContextMenu.getShowAllColumnsMenu(),
                                columnVisibilityContextMenu.getShowDefaultColumnsMenu(),
                                columnVisibilityContextMenu.getShowPrimaryColumnsMenu(),
                                columnVisibilityContextMenu.getHideAllColumnsMenu(),
                                separator.get(),
                                columnVisibilityContextMenu.getSourceVertexColumnsMenu(),
                                columnVisibilityContextMenu.getDestinationVertexColumnMenu(),
                                columnVisibilityContextMenu.getTransactionColumnMenu()
                        )
                ),
                columnVisibilityContextMenu.getContextMenu().getItems()
        );

        /////////////////
        // ALL COLUMNS
        /////////////////
        verifyCustomMenu(columnVisibilityContextMenu.getShowAllColumnsMenu(), "Show All Columns", false);

        // All columns are passed
        verifyAllColumnsMenuClicked(columnVisibilityContextMenu.getShowAllColumnsMenu(), List.of(
                Tuple.create(columnType1, attribute1),
                Tuple.create(columnType2, attribute2),
                Tuple.create(columnType3, attribute3),
                Tuple.create(columnType4, attribute4),
                Tuple.create(columnType5, attribute5)
        ));

        /////////////////
        // DEFAULT COLUMNS
        /////////////////
        verifyCustomMenu(columnVisibilityContextMenu.getShowDefaultColumnsMenu(), "Show Default Columns", false);
        
        // See TestTableDefaultColumnsProvider, it has attribute2 and attribute5 set as default attributes
        verifyProvidedColumnVisibilityActions(columnVisibilityContextMenu.getShowDefaultColumnsMenu(), List.of(
                Tuple.create(columnType2, attribute2),
                Tuple.create(columnType5, attribute5)
        ), null);

        /////////////////
        // KEY COLUMNS
        /////////////////
        verifyCustomMenu(columnVisibilityContextMenu.getShowPrimaryColumnsMenu(), "Show Key Columns", false);

        // Only pass the key or primary columns as defined by readableGraph#getPrimaryKey(GraphElement)
        verifyProvidedColumnVisibilityActions(columnVisibilityContextMenu.getShowPrimaryColumnsMenu(), List.of(
                Tuple.create(columnType2, attribute2),
                Tuple.create(columnType3, attribute3),
                Tuple.create(columnType5, attribute5)
        ), null);

        /////////////////
        // NO COLUMNS
        /////////////////
        verifyCustomMenu(columnVisibilityContextMenu.getHideAllColumnsMenu(), "Show No Columns", false);

        // No columns are passed and the actual table columns visibility is set to false
        verifyProvidedColumnVisibilityActions(columnVisibilityContextMenu.getHideAllColumnsMenu(), Collections.emptyList(),
                () -> {
                    verify(column1).setVisible(false);
                    verify(column2).setVisible(false);
                    verify(column3).setVisible(false);
                    verify(column4).setVisible(false);
                    verify(column5).setVisible(false);
                }
        );

        /////////////////
        // SPLIT SOURCE
        /////////////////
        verifyDynamicColumnMenu(columnVisibilityContextMenu.getSourceVertexColumnsMenu(),
                List.of(columnVisibilityMenuItemColumn1, columnVisibilityMenuItemColumn3));

        //////////////////////
        // SPLIT DESTINATION
        //////////////////////
        verifyDynamicColumnMenu(columnVisibilityContextMenu.getDestinationVertexColumnMenu(),
                List.of(columnVisibilityMenuItemColumn2));

        //////////////////////
        // SPLIT TRANSACTION
        //////////////////////
        verifyDynamicColumnMenu(columnVisibilityContextMenu.getTransactionColumnMenu(),
                List.of(columnVisibilityMenuItemColumn4, columnVisibilityMenuItemColumn5));
    }

    @Test
    public void createColumnVisibilityMenuItemSelected() {
        final String columnText = "Some Column Text";
        final boolean isSelected = true;

        when(column1.getText()).thenReturn(columnText);
        when(column1.visibleProperty()).thenReturn(new SimpleBooleanProperty(isSelected));

        final CustomMenuItem columnVisibilityMenuItem = columnVisibilityContextMenu
                .createColumnVisibilityMenu(new Column(columnType1, attribute1, column1));

        verifyColumnVisibilityCheckBox(columnVisibilityMenuItem, isSelected, columnText,
                List.of(Tuple.create(columnType1, attribute1)), UpdateMethod.ADD);
    }

    @Test
    public void createColumnVisibilityMenuItemNotSelected() {
        final String columnText = "Some Column Text";
        final boolean isSelected = false;

        when(column1.getText()).thenReturn(columnText);
        when(column1.visibleProperty()).thenReturn(new SimpleBooleanProperty(isSelected));

        final CustomMenuItem columnVisibilityMenuItem = columnVisibilityContextMenu
                .createColumnVisibilityMenu(new Column(columnType1, attribute1, column1));

        verifyColumnVisibilityCheckBox(columnVisibilityMenuItem, isSelected, columnText,
                List.of(Tuple.create(columnType1, attribute1)), UpdateMethod.REMOVE);
    }

    @Test
    public void createColumnFilterMenuItem() {
        final CustomMenuItem columnCheckBox1 = mock(CustomMenuItem.class);
        final CustomMenuItem columnCheckBox2 = mock(CustomMenuItem.class);
        final CustomMenuItem columnCheckBox3 = mock(CustomMenuItem.class);

        when(columnCheckBox1.getId()).thenReturn("Some Value");
        when(columnCheckBox2.getId()).thenReturn("Other Some Values");
        when(columnCheckBox3.getId()).thenReturn("Something Else");

        final CustomMenuItem columnFilterMenuItem = columnVisibilityContextMenu.createColumnFilterMenu(List.of(columnCheckBox1, columnCheckBox2, columnCheckBox3));

        assertFalse(columnFilterMenuItem.isHideOnClick());

        final HBox box = (HBox) columnFilterMenuItem.getContent();

        assertEquals(2, box.getChildren().size());

        assertTrue(box.getChildren().get(0) instanceof Label);
        assertTrue(box.getChildren().get(1) instanceof TextField);

        assertEquals("Filter:", ((Label) box.getChildren().get(0)).getText());

        final TextField textField = (TextField) box.getChildren().get(1);

        final KeyEvent keyEvent = mock(KeyEvent.class);

        when(keyEvent.getSource()).thenReturn(textField);

        textField.setText("SomE VaLue");

        textField.getOnKeyReleased().handle(keyEvent);

        verify(columnCheckBox1).setVisible(true);
        verify(columnCheckBox2).setVisible(true);
        verify(columnCheckBox3).setVisible(false);

        verify(keyEvent).consume();
    }

    /**
     * Verify the construction of a checkbox that allows the visibility updating
     * of an individual column. Verifies its current selection, text and
     * expected actions.
     *
     * @param menuItem the column visibility checkbox
     * @param isSelected true if the checkbox is selected, false otherwise
     * @param expectedText the expected text to be associated with the check box
     * @param expectedColumnAttributes the expected columns to be updated when
     * the menu item is clicked
     * @param expectedUpdateMethod the expected method of column visibility
     * updating when the menu item is clicked
     */
    private void verifyColumnVisibilityCheckBox(final CustomMenuItem menuItem, final boolean isSelected, final String expectedText,
            final List<Tuple<String, Attribute>> expectedColumnAttributes, final UpdateMethod expectedUpdateMethod) {
        assertEquals(expectedText, menuItem.getId());
        assertFalse(menuItem.isHideOnClick());

        final CheckBox columnCheckBox = (CheckBox) menuItem.getContent();

        assertEquals(expectedText, columnCheckBox.getText());
        assertEquals(isSelected, columnCheckBox.isSelected());

        final ActionEvent actionEvent = mock(ActionEvent.class);
        when(actionEvent.getSource()).thenReturn(columnCheckBox);

        columnCheckBox.getOnAction().handle(actionEvent);

        verify(activeTableReference).updateVisibleColumns(graph, tableViewState, expectedColumnAttributes, expectedUpdateMethod);
    }

    /**
     * Verifies the actions taken when button is clicked that provides standard
     * column visibility settings like show all.
     *
     * @param customMenuItem the standard column visibility menu item
     * @param expectedColumnAttributes the expected columns to be updated
     */
    private void verifyAllColumnsMenuClicked(final CustomMenuItem customMenuItem, final List<Tuple<String, Attribute>> expectedColumnAttributes) {
        verifyProvidedColumnVisibilityActions(customMenuItem, expectedColumnAttributes, null);
    }

    /**
     * Verifies the actions taken when button is clicked that provides standard
     * column visibility settings like show all.
     *
     * @param customMenuItem the standard column visibility menu item
     * @param expectedColumnAttributes the expected columns to be updated
     * @param extraVerifications extra custom verifications if needed
     */
    private void verifyProvidedColumnVisibilityActions(final CustomMenuItem customMenuItem, final List<Tuple<String, Attribute>> expectedColumnAttributes,
            final Runnable extraVerifications) {
        final ActionEvent actionEvent = mock(ActionEvent.class);

        customMenuItem.getOnAction().handle(actionEvent);

        verify(activeTableReference).updateVisibleColumns(graph, tableViewState, expectedColumnAttributes, UpdateMethod.REPLACE);

        verify(actionEvent).consume();

        if (extraVerifications != null) {
            extraVerifications.run();
        }
    }

    /**
     * Verifies that the custom menu item has the correct text set.
     *
     * @param customMenuItem the menu item to verify
     * @param expectedText the expected text on the menu item
     * @param isHideOnClick true if hideOnClick is expected to be true,
     * otherwise false
     */
    private void verifyCustomMenu(final CustomMenuItem customMenuItem, final String expectedText, final boolean isHideOnClick) {
        assertEquals(expectedText, ((Label) customMenuItem.getContent()).getText());
        assertEquals(isHideOnClick, customMenuItem.isHideOnClick());
    }

    /**
     * Verifies a menu that has a list of items representing columns and one
     * representing a filter that will effect those other items. For the
     * purposes of this, it ignores the filter (other than verify it is there)
     * and just verifies that the correct columns are present.
     *
     * @param customMenuItem the menu containing the columns and filter
     * @param expectedColumnCheckBoxes the expected column check boxes in the
     * menu
     */
    private void verifyDynamicColumnMenu(final CustomMenuItem customMenuItem, final List<CustomMenuItem> expectedColumnCheckBoxes) {
        final List<CustomMenuItem> columnVisibilityMenuItems = new ArrayList<>();

        for (MenuItem menuItem : ((MenuButton) customMenuItem.getContent()).getItems()) {
            final CustomMenuItem columnVisibilityMenuItem = (CustomMenuItem) menuItem;

            if (columnVisibilityMenuItem.getContent() instanceof HBox columnVisibilityContent) {
                assertEquals(2, columnVisibilityContent.getChildren().size());
                assertTrue(columnVisibilityContent.getChildren().get(0) instanceof Label);
                assertTrue(columnVisibilityContent.getChildren().get(1) instanceof TextField);

                assertEquals("Filter:", ((Label) columnVisibilityContent.getChildren().get(0)).getText());
            } else {
                columnVisibilityMenuItems.add(columnVisibilityMenuItem);
            }

            assertFalse(columnVisibilityMenuItem.isHideOnClick());
        }

        // There should only be one filter per menu. So the created columnVisibilityMenuItems
        // list should have a size one less than the original
        assertEquals(((MenuButton) customMenuItem.getContent()).getItems().size() - 1, columnVisibilityMenuItems.size());

        assertEquals(expectedColumnCheckBoxes, columnVisibilityMenuItems);
    }
}
