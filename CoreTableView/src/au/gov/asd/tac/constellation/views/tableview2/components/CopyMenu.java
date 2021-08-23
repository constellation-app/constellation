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

import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.image.ImageView;

/**
 *
 * @author formalhaunt
 */
public class CopyMenu {
    private static final String COPY_TABLE = "Copy Table";
    private static final String COPY_TABLE_SELECTION = "Copy Table (Selection)";
    
    private static final ImageView COPY_ICON = new ImageView(UserInterfaceIconProvider.COPY.buildImage(16));
    
    private static final int WIDTH = 120;
    
    private final Table table;
    private final Pagination pagination;
    
    private MenuButton copyButton;
    private MenuItem copyTableMenu;
    private MenuItem copyTableSelectionMenu;
    
    public CopyMenu(final Table table,
                    final Pagination pagination) {
        this.table = table;
        this.pagination = pagination;
    }
    
    public void init() {
        copyButton = createMenuButton(COPY_ICON);
        copyTableMenu = createCopyMenuItem(COPY_TABLE, false);
        copyTableSelectionMenu = createCopyMenuItem(COPY_TABLE_SELECTION, true);
        copyButton.getItems().addAll(getCopyTableMenu(), getCopyTableSelectionMenu());
    }
    
    public MenuButton getCopyButton() {
        return copyButton;
    }

    public MenuItem getCopyTableMenu() {
        return copyTableMenu;
    }

    public MenuItem getCopyTableSelectionMenu() {
        return copyTableSelectionMenu;
    }
    
    private MenuButton createMenuButton(final ImageView icon) {
        final MenuButton button = new MenuButton();
        
        button.setGraphic(icon);
        button.setMaxWidth(WIDTH);
        button.setPopupSide(Side.RIGHT);
        
        return button;
    }
    
    private MenuItem createCopyMenuItem(final String menuTitle,
                                        final boolean selected) {
        final MenuItem menuItem = new MenuItem(menuTitle);
        
        menuItem.setOnAction(new CopyMenuItemActionHandler(selected));
        
        return menuItem;
    }
    
    class CopyMenuItemActionHandler implements EventHandler<ActionEvent> {
        private final boolean selected;
        
        public CopyMenuItemActionHandler(final boolean selected) {
            this.selected = selected;
        }
        
        @Override
        public void handle(ActionEvent event) {
            final String data = TableViewUtilities.getTableData(table.getTableView(),
                    pagination, false, selected);
            TableViewUtilities.copyToClipboard(data);
            event.consume();
        }
    }
}
