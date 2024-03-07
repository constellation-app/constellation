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
package au.gov.asd.tac.constellation.views.tableview.components;

import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
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
 public class RightClickContextMenuNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(RightClickContextMenuNGTest.class.getName());

    private Table table;
    private TableView<ObservableList<String>> tableView;

    private RightClickContextMenu rightClickContextMenu;

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
        table = mock(Table.class);
        tableView = mock(TableView.class);

        when(table.getTableView()).thenReturn(tableView);

        rightClickContextMenu = new RightClickContextMenu(table);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void allUIComponentsNullBeforeInit() {
        assertNull(rightClickContextMenu.getContextMenu());
        assertNull(rightClickContextMenu.getCopyCell());
        assertNull(rightClickContextMenu.getCopyColumn());
        assertNull(rightClickContextMenu.getCopyRow());
        assertNull(rightClickContextMenu.getCopyColumnUnique());
    }

    @Test
    public void createExportButtons() {
        final TableCell<ObservableList<String>, String> cell = mock(TableCell.class);
        final TableRow<ObservableList<String>> row = mock(TableRow.class);
        final TableColumn<ObservableList<String>, String> column = mock(TableColumn.class);

        final int column2Index = 1;

        final ObservableList<String> row1 = FXCollections.observableList(
                List.of("row1Column1", "row1Column2", "row1Column3"));
        final ObservableList<String> row2 = FXCollections.observableList(
                List.of("row2Column1", "row2Column2", "row2Column3"));
        final ObservableList<String> row3 = FXCollections.observableList(
                List.of("row3Column1", "row3Column2", "row3Column3"));

        // Column2 is purposefully the same as row 3 in order to verify "copy column UNIQUE"
        final ObservableList<String> row4 = FXCollections.observableList(
                List.of("row4Column1", "row3Column2", "row4Column3"));

        when(tableView.getItems()).thenReturn(FXCollections.observableList(
                List.of(row1, row2, row3, row4)
        ));

        when(cell.getItem()).thenReturn(row2.get(column2Index)); // a.k.a row2Column2

        when(cell.getTableRow()).thenReturn(row);
        when(row.getItem()).thenReturn(FXCollections.observableList(row2));

        when(cell.getTableColumn()).thenReturn(column);
        when(column.getCellObservableValue(row1)).thenReturn(new SimpleStringProperty(row1.get(column2Index)));
        when(column.getCellObservableValue(row2)).thenReturn(new SimpleStringProperty(row2.get(column2Index)));
        when(column.getCellObservableValue(row3)).thenReturn(new SimpleStringProperty(row3.get(column2Index)));
        when(column.getCellObservableValue(row4)).thenReturn(new SimpleStringProperty(row4.get(column2Index)));

        rightClickContextMenu.init(cell);

        assertNotNull(rightClickContextMenu.getContextMenu());
        assertNotNull(rightClickContextMenu.getCopyCell());
        assertNotNull(rightClickContextMenu.getCopyColumn());
        assertNotNull(rightClickContextMenu.getCopyRow());
        assertNotNull(rightClickContextMenu.getCopyColumnUnique());

        assertEquals(
                FXCollections.observableList(
                        List.of(
                                rightClickContextMenu.getCopyCell(),
                                rightClickContextMenu.getCopyRow(),
                                rightClickContextMenu.getCopyColumn(),
                                rightClickContextMenu.getCopyColumnUnique()
                        )
                ),
                rightClickContextMenu.getContextMenu().getItems()
        );

        assertEquals("Copy Cell", rightClickContextMenu.getCopyCell().getText());
        verifyCopyAction(rightClickContextMenu.getCopyCell().getOnAction(), "row2Column2");

        assertEquals("Copy Row", rightClickContextMenu.getCopyRow().getText());
        verifyCopyAction(rightClickContextMenu.getCopyRow().getOnAction(),
                "row2Column1,row2Column2,row2Column3");

        assertEquals("Copy Column", rightClickContextMenu.getCopyColumn().getText());
        verifyCopyAction(rightClickContextMenu.getCopyColumn().getOnAction(),
                "row1Column2,row2Column2,row3Column2,row3Column2");

        assertEquals("Copy Column (Unique)", rightClickContextMenu.getCopyColumnUnique().getText());
        verifyCopyAction(rightClickContextMenu.getCopyColumnUnique().getOnAction(),
                "row1Column2,row2Column2,row3Column2");
    }

    /**
     * Verify that the passed event handler copies the correct table cells in
     * CSV form to the OS clipboard.
     *
     * @param eventHandler the handler to test
     * @param expectedClipboardText the expected CSV copied to the clipboard
     */
    private void verifyCopyAction(final EventHandler<ActionEvent> eventHandler,
            final String expectedClipboardText) {

        try (MockedStatic<TableViewUtilities> tableViewUtilsMockedStatic
                = Mockito.mockStatic(TableViewUtilities.class)) {

            final ActionEvent actionEvent = mock(ActionEvent.class);

            eventHandler.handle(actionEvent);

            tableViewUtilsMockedStatic.verify(() -> TableViewUtilities
                    .copyToClipboard(expectedClipboardText));

            verify(actionEvent).consume();
        }
    }
}
