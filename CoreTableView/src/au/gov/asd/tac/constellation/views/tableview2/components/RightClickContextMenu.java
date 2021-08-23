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
package au.gov.asd.tac.constellation.views.tableview2.components;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;

/**
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
    
    public RightClickContextMenu(final Table table) {
        this.table = table;
    }
    
    public void init(final TableCell<ObservableList<String>, String> cell) {
        contextMenu = new ContextMenu();

        final MenuItem copyCell = new MenuItem(COPY_CELL);
        copyCell.setOnAction(e -> {
            final String cellData = cell.getItem();
            TableViewUtilities.copyToClipboard(cellData);
            e.consume();
        });

        final MenuItem copyRow = new MenuItem(COPY_ROW);
        copyRow.setOnAction(e -> {
            final String rowData = cell.getTableRow().getItem().stream()
                    .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2).get();
            TableViewUtilities.copyToClipboard(rowData);
            e.consume();
        });

        final MenuItem copyColumn = new MenuItem(COPY_COLUMN);
        copyColumn.setOnAction(e -> {
            final String columnData = table.getTableView().getItems().stream()
                    .map(item -> cell.getTableColumn().getCellObservableValue(item).getValue())
                    .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2).get();
            TableViewUtilities.copyToClipboard(columnData);
            e.consume();
        });

        final MenuItem copyColumnUnique = new MenuItem(COPY_COLUMN_UNIQUE);
        copyColumnUnique.setOnAction(e -> {
            final String uniqueColumnData = table.getTableView().getItems().stream()
                    .map(item -> cell.getTableColumn().getCellObservableValue(item).getValue())
                    .collect(Collectors.toSet()).stream()
                    .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2).get();
            TableViewUtilities.copyToClipboard(uniqueColumnData);
            e.consume();
        });

        contextMenu.getItems().addAll(copyCell, copyRow, copyColumn, copyColumnUnique);
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }
}
