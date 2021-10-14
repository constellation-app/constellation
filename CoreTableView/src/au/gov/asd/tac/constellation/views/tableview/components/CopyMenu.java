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

import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview.utilities.TableViewUtilities;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

/**
 * Creates copy menu components that can be attached to the table view for the
 * purposes of copying table data to the OS clipboard in CSV format.
 *
 * @author formalhaunt
 */
public class CopyMenu {

    private static final String COPY_TABLE = "Copy Table";
    private static final String COPY_TABLE_SELECTION = "Copy Table (Selection)";

    private static final ImageView COPY_ICON = new ImageView(UserInterfaceIconProvider.COPY.buildImage(16));

    private static final int WIDTH = 120;

    private final TablePane tablePane;

    private MenuButton copyButton;
    private MenuItem copyTableMenu;
    private MenuItem copyTableSelectionMenu;

    /**
     * Creates a new copy menu.
     *
     * @param tablePane the pane that contains the table
     */
    public CopyMenu(final TablePane tablePane) {
        this.tablePane = tablePane;
    }

    /**
     * Initializes the copy menu. Until this method is called, all menu UI
     * components will be null.
     */
    public void init() {
        copyButton = createMenuButton(COPY_ICON);
        copyTableMenu = createCopyMenu(COPY_TABLE, false);
        copyTableSelectionMenu = createCopyMenu(COPY_TABLE_SELECTION, true);
        copyButton.getItems().addAll(getCopyTableMenu(), getCopyTableSelectionMenu());
    }

    /**
     * Get the copy button that the menu items have been added to.
     *
     * @return the copy table data button
     */
    public MenuButton getCopyButton() {
        return copyButton;
    }

    /**
     * Get the menu item that will copy all the table data to the OS clipboard
     * in CSV format.
     *
     * @return the copy all rows menu item
     */
    public MenuItem getCopyTableMenu() {
        return copyTableMenu;
    }

    /**
     * Get the menu item that will copy only the selected row to the OS
     * clipboard in CSV format.
     *
     * @return the copy selected rows only menu item
     */
    public MenuItem getCopyTableSelectionMenu() {
        return copyTableSelectionMenu;
    }

    /**
     * Creates a menu button to store the copy menu items under. Sets the icon
     * and max width.
     *
     * @param icon the icon to display on the button
     * @return the created menu button
     */
    private MenuButton createMenuButton(final ImageView icon) {
        final MenuButton button = new MenuButton();

        button.setGraphic(icon);
        button.setMaxWidth(WIDTH);
        button.setPopupSide(Side.RIGHT);

        return button;
    }

    /**
     * Creates a copy {@link MenuItem}. Sets the title text and the action
     * handler to a new {@link CopyMenuItemActionHandler}.
     *
     * @param menuTitle the title to put on the menu item
     * @param selected true if clicking this menu item will only copy the
     * selected rows in the table, false otherwise
     * @return the created menu item
     */
    private MenuItem createCopyMenu(final String menuTitle,
            final boolean selected) {
        final MenuItem menuItem = new MenuItem(menuTitle);

        menuItem.setOnAction(new CopyMenuItemActionHandler(selected));

        return menuItem;
    }

    /**
     * Action handler for menu items that will copy table rows to the OS
     * clipboard.
     */
    class CopyMenuItemActionHandler implements EventHandler<ActionEvent> {

        private final boolean selected;

        /**
         * Creates a new copy menu item action handler.
         *
         * @param selected if true, when this handler is activated only the rows
         * selected in the table will be copied.
         */
        public CopyMenuItemActionHandler(final boolean selected) {
            this.selected = selected;
        }

        /**
         * Copies the table data in CSV format to the OS clipboard. If the
         * variable {@link #selected} is true then only the selected table rows
         * will be copied.
         *
         * @param event the event that triggered this action
         * @see EventHandler#handle(javafx.event.Event)
         */
        @Override
        public void handle(final ActionEvent event) {
            final String data = TableViewUtilities.getTableData(tablePane.getTable().getTableView(),
                    tablePane.getActiveTableReference().getPagination(), false, selected);
            TableViewUtilities.copyToClipboard(data);
            event.consume();
        }
    }
}
