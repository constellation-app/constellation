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
package au.gov.asd.tac.constellation.views.tableview.factory;

import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.components.RightClickContextMenu;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
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
public class TableCellFactoryNGTest {
    private static final Logger LOGGER = Logger.getLogger(TableCellFactoryNGTest.class.getName());

    private Table table;
    private TableColumn<ObservableList<String>, String> cellColumn;

    private TableCellFactory tableCellFactory;

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
        cellColumn = mock(TableColumn.class);

        tableCellFactory = spy(new TableCellFactory(cellColumn, table));
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void updateItemIsEmpty() {
        Text t = mock(Text.class);
        t.setText("Hello World");
        doReturn(t).when(tableCellFactory).getWrappingText("Hello World");
        tableCellFactory.updateItem("Hello World", true);        
        verify(tableCellFactory, times(0)).setGraphic(t);
    }

    @Test
    public void updateItemIsNotEmpty() {
        final String test_value = "Test Value";
        verifyStyle(test_value, "source.", List.of("element-source"));
        verifyStyle(test_value, "destination.", List.of("element-destination"));
        verifyStyle(test_value, "transaction.", List.of("element-transaction"));
        verifyStyle(null, "transaction.", List.of("null-value", "element-transaction"));
    }

    @Test
    public void updateItemTestMouseClick() {
        final ObservableList<String> styleClass = spy(FXCollections.observableArrayList());
        doReturn(styleClass).when(tableCellFactory).getStyleClass();

        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();

        when(table.getColumnIndex()).thenReturn(columnIndex);

        Text t = mock(Text.class);
        t.setText("Test Value");
        doReturn(t).when(tableCellFactory).getWrappingText("Test Value");
        tableCellFactory.updateItem("Test Value", false);

        final TableView<ObservableList<String>> tableView = mock(TableView.class);

        final RightClickContextMenu rightClickContextMenu = mock(RightClickContextMenu.class);
        final ContextMenu contextMenu = mock(ContextMenu.class);

        final MouseEvent mouseEvent = mock(MouseEvent.class);

        doReturn(rightClickContextMenu).when(tableCellFactory).getRightClickContextMenu();

        when(rightClickContextMenu.getContextMenu()).thenReturn(contextMenu);

        when(mouseEvent.getScreenX()).thenReturn(20.0d);
        when(mouseEvent.getScreenY()).thenReturn(40.0d);
        when(mouseEvent.getButton()).thenReturn(MouseButton.SECONDARY);

        when(table.getTableView()).thenReturn(tableView);

        tableCellFactory.getOnMouseClicked().handle(mouseEvent);

        verify(contextMenu).show(tableView, 20.0d, 40.0d);
        assertEquals(FXCollections.observableArrayList(), styleClass);
    }

    @Test
    public void updateItemTestMouseClickWrongButton() {
        final CopyOnWriteArrayList<Column> columnIndex = new CopyOnWriteArrayList<>();

        when(table.getColumnIndex()).thenReturn(columnIndex);

        Text t = mock(Text.class);
        t.setText("Test Value");
        doReturn(t).when(tableCellFactory).getWrappingText("Test Value");
        tableCellFactory.updateItem("Test Value", false);

        final MouseEvent mouseEvent = mock(MouseEvent.class);

        when(mouseEvent.getButton()).thenReturn(MouseButton.PRIMARY);

        tableCellFactory.getOnMouseClicked().handle(mouseEvent);

        verify(tableCellFactory, times(0)).getRightClickContextMenu();
    }

    @Test
    public void getRightClickContextMenu() {
        final RightClickContextMenu menu = tableCellFactory.getRightClickContextMenu();

        assertNotNull(menu);
        assertNotNull(menu.getContextMenu());

        assertSame(menu, tableCellFactory.getRightClickContextMenu());
    }

    /**
     * Verifies that the cell text is set correctly and the correct style for
     * the column is added to the style class list.
     *
     * @param item the string passed in to be set in the table cell
     * @param columnPrefix the column prefix for this cells column
     * @param expectedStyles the expected styles that should be present in the
     * style class list
     */
    private void verifyStyle(final String item,
            final String columnPrefix,
            final List<String> expectedStyles) {
        clearInvocations(tableCellFactory, table);

        final ObservableList<String> styleClass = spy(FXCollections.observableArrayList());
        doReturn(styleClass).when(tableCellFactory).getStyleClass();

        final CopyOnWriteArrayList<Column> columnIndex
                = new CopyOnWriteArrayList<>();
        columnIndex.add(new Column("source.", null, mock(TableColumn.class)));
        columnIndex.add(new Column(columnPrefix, null, cellColumn));
        columnIndex.add(new Column("transaction.", null, mock(TableColumn.class)));

        when(table.getColumnIndex()).thenReturn(columnIndex);
        Text t = mock(Text.class);
        t.setText(item);
        doReturn(t).when(tableCellFactory).getWrappingText(item);

        tableCellFactory.updateItem(item, false);
        verify(tableCellFactory).updateItem(item, false);
        if (item != null) {
            verify(tableCellFactory).setGraphic(t);
        } else {
            verify(tableCellFactory).setText("<No Value>");
        }

        verify(styleClass).remove("null-value");
        verify(styleClass).remove("element-source");
        verify(styleClass).remove("element-destination");
        verify(styleClass).remove("element-transaction");

        expectedStyles.forEach(expectedStyle -> verify(styleClass).add(expectedStyle));

        assertEquals(FXCollections.observableList(expectedStyles), styleClass);
    }
}
