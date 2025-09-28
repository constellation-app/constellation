/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;

/**
 * Creates a context menu that will appear when right clicking in a table cell.
 * The menu provides options for converting cell, row and column data
 * surrounding the selected cell into CSV. The CSV is placed into the OS
 * clipboard.
 *
 * @author formalhaunt
 */
public class RightClickContextMenu {

    private static final String COPY_CELL = "Copy Cell";
    private static final String COPY_ROW = "Copy Row";
    private static final String COPY_COLUMN = "Copy Column";
    private static final String COPY_COLUMN_UNIQUE = "Copy Column (Unique)";

    private final Table table;

    private ContextMenu contextMenu;

    private MenuItem copyCell;
    private MenuItem copyRow;
    private MenuItem copyColumn;
    private MenuItem copyColumnUnique;

    /**
     * Creates a new right click context menu.
     *
     * @param table the table that the context menu will be attached to
     */
    public RightClickContextMenu(final Table table) {
        this.table = table;
    }

    /**
     * Initializes the context menu. Until this method is called, all menu UI
     * components will be null.
     *
     * @param cell the table cell that was clicked to initialize the menu
     */
    public void init(final TableCell<ObservableList<String>, String> cell) {
        contextMenu = new ContextMenu();

        copyCell = new MenuItem(COPY_CELL);
        copyCell.setOnAction(e -> {
            final String cellData = cell.getItem();
            TableViewUtilities.copyToClipboard(cellData);
            e.consume();
        });

        copyRow = new MenuItem(COPY_ROW);
        copyRow.setOnAction(e -> {
            final String rowData = cell.getTableRow().getItem().stream()
                    .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2).get();
            TableViewUtilities.copyToClipboard(rowData);
            e.consume();
        });

        copyColumn = new MenuItem(COPY_COLUMN);
        copyColumn.setOnAction(e -> {
            final String columnData = table.getTableView().getItems().stream()
                    .map(item -> cell.getTableColumn().getCellObservableValue(item).getValue())
                    .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2).get();
            TableViewUtilities.copyToClipboard(columnData);
            e.consume();
        });

        copyColumnUnique = new MenuItem(COPY_COLUMN_UNIQUE);
        copyColumnUnique.setOnAction(e -> {
            final String uniqueColumnData = table.getTableView().getItems().stream()
                    .map(item -> cell.getTableColumn().getCellObservableValue(item).getValue())
                    .collect(Collectors.toSet()).stream()
                    .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2).get();
            TableViewUtilities.copyToClipboard(uniqueColumnData);
            e.consume();
        });

        // Add the created menus to the context menu
        contextMenu.getItems().addAll(copyCell, copyRow, copyColumn, copyColumnUnique);
    }

    /**
     * Get the context menu containing the copy options.
     *
     * @return the right click context menu
     */
    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    /**
     * Get the menu item that will copy just the data in the clicked cell.
     *
     * @return the copy cell menu item
     */
    public MenuItem getCopyCell() {
        return copyCell;
    }

    /**
     * Get the menu item that will copy all the in the row of the clicked cell
     * (in CSV).
     *
     * @return the copy row menu item
     */
    public MenuItem getCopyRow() {
        return copyRow;
    }

    /**
     * Get the menu item that will copy all values in the column of the clicked
     * cell (in CSV).
     *
     * @return the copy column menu item
     */
    public MenuItem getCopyColumn() {
        return copyColumn;
    }

    /**
     * Get the menu item that will copy all values in the column of the clicked
     * cell (in CSV). The returned values will be unique.
     *
     * @return the copy column unique values menu item
     */
    public MenuItem getCopyColumnUnique() {
        return copyColumnUnique;
    }
}
