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

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.views.tableview.components.RightClickContextMenu;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;

/**
 * A {@link TableCell} that updates the cells text and style classes on change.
 * It also sets up the right click context menu for cell clicks.
 *
 * @author formalhaunt
 */
public class TableCellFactory extends TableCell<ObservableList<String>, String> {

    private static final String ELEMENT_SOURCE_CLASS = "element-source";
    private static final String ELEMENT_DESTINATION_CLASS = "element-destination";
    private static final String ELEMENT_TRANSACTION_CLASS = "element-transaction";
    private static final String NULL_VALUE_CLASS = "null-value";

    private static final String NO_VALUE_TEXT = "<No Value>";

    private final TableColumn<ObservableList<String>, String> cellColumn;
    private final Table table;

    private RightClickContextMenu rightClickContextMenuInstance;

    /**
     * Creates a new table cell factory.
     *
     * @param cellColumn the column that the cells belong to
     * @param table the table that the cells belong to
     */
    public TableCellFactory(final TableColumn<ObservableList<String>, String> cellColumn, final Table table) {
        this.cellColumn = cellColumn;
        this.table = table;
    }

    /**
     * Sets the cells text to the passed item and then updates the cells style
     * classes based on the cells column attributes.
     *
     * @param item the string to set the cells text to
     * @param empty true and the item will not be set to the cells text, false
     * and it will
     */
    @Override
    public void updateItem(final String item, final boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            // set text in cell and style if it is null
            this.getStyleClass().remove(NULL_VALUE_CLASS);
            if (item != null) {           
                Text text = getWrappingText(item);                
                this.setGraphic(text);               
            } else {
                this.setText(NO_VALUE_TEXT);
                this.getStyleClass().add(NULL_VALUE_CLASS);
            }

            // color cell based on the attribute it represents
            this.getStyleClass().remove(ELEMENT_SOURCE_CLASS);
            this.getStyleClass().remove(ELEMENT_TRANSACTION_CLASS);
            this.getStyleClass().remove(ELEMENT_DESTINATION_CLASS);

            // based on the column name prefixes ".source", ".destination" and
            // ".transaction" set the appropriate style class
            final String columnPrefix = table.getColumnIndex().stream()
                    .filter(column -> column.getTableColumn().equals(cellColumn))
                    .map(column -> column.getAttributeNamePrefix())
                    .findFirst().orElse("");
            switch (columnPrefix) {
                case GraphRecordStoreUtilities.SOURCE -> this.getStyleClass().add(ELEMENT_SOURCE_CLASS);
                case GraphRecordStoreUtilities.TRANSACTION -> this.getStyleClass().add(ELEMENT_TRANSACTION_CLASS);
                case GraphRecordStoreUtilities.DESTINATION -> this.getStyleClass().add(ELEMENT_DESTINATION_CLASS);
                default -> {
                    // do nothing
                }
            }

            // enable context menu on right-click
            this.setOnMouseClicked(me -> {
                if (me.getButton() == MouseButton.SECONDARY) {
                    final RightClickContextMenu rightClickContextMenu = getRightClickContextMenu();

                    // open the context menu at the mouses current location
                    rightClickContextMenu.getContextMenu().show(table.getTableView(), me.getScreenX(), me.getScreenY());
                }
            });
        }
    }

    /**
     * Create a new Text object to wrap item in and set text wrapping.
     * @param item string to set in Text object.
     * @return Text object
     */
    protected Text getWrappingText(final String item) {
        Text text = new Text(item);
        text.wrappingWidthProperty().bind(cellColumn.widthProperty());
        return text;
    }

    /**
     * Gets a initialized {@link RightClickContextMenu}. If the context menu has
     * already been initialized it will use that otherwise it will create and
     * initialize the menu.
     *
     * @return the right click context menu for this cell
     */
    protected final RightClickContextMenu getRightClickContextMenu() {
        if (rightClickContextMenuInstance == null) {
            rightClickContextMenuInstance = new RightClickContextMenu(table);
            rightClickContextMenuInstance.init(this);
        }
        return rightClickContextMenuInstance;
    }

}
