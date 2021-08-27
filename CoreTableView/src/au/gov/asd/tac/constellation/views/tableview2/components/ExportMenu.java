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
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

/**
 * Creates export menu components that can be attached to the table view for the purposes
 * of exporting table data in CSV or Excel formats.
 *
 * @author formalhaunt
 */
public class ExportMenu {
    private static final String EXPORT_CSV = "Export to CSV";
    private static final String EXPORT_CSV_SELECTION = "Export to CSV (Selection)";
    private static final String EXPORT_XLSX = "Export to Excel";
    private static final String EXPORT_XLSX_SELECTION = "Export to Excel (Selection)";
    
    private static final ImageView EXPORT_ICON = new ImageView(UserInterfaceIconProvider.UPLOAD.buildImage(16));
    
    private static final int WIDTH = 120;
    
    private final TableViewTopComponent tableTopComponent;
    private final Table table;
    private final TableService tableService;
    
    private MenuButton exportButton;
    private MenuItem exportCsvMenu;
    private MenuItem exportCsvSelectionMenu;
    private MenuItem exportExcelMenu;
    private MenuItem exportExcelSelectionMenu;
    
    /**
     * Creates a new export menu.
     *
     * @param tableTopComponent the top component that the table is embedded into
     * @param table the table that the export menu will be attached to
     * @param tableService the table service associated to the table
     */
    public ExportMenu(final TableViewTopComponent tableTopComponent,
                      final Table table,
                      final TableService tableService) {
        this.tableTopComponent = tableTopComponent;
        this.table = table;
        this.tableService = tableService;
    }
    
    /**
     * Initializes the export menu. Until this method is called, all menu UI components
     * will be null.
     */
    public void init() {
        exportButton = createMenuButton(EXPORT_ICON);
        exportCsvMenu = createExportMenu(EXPORT_CSV, ()
                -> TableViewUtilities.exportToCsv(table.getTableView(), tableService.getPagination(), false));
        exportCsvSelectionMenu = createExportMenu(EXPORT_CSV_SELECTION, ()
                -> TableViewUtilities.exportToCsv(table.getTableView(), tableService.getPagination(), true));
        exportExcelMenu = createExportMenu(EXPORT_XLSX, ()
                -> TableViewUtilities.exportToExcel(table.getTableView(), tableService.getPagination(),
                        tableService.getTablePreferences().getMaxRowsPerPage(), false,
                        tableTopComponent.getCurrentGraph().getId()));
        exportExcelSelectionMenu = createExportMenu(EXPORT_XLSX_SELECTION, ()
                -> TableViewUtilities.exportToExcel(table.getTableView(), tableService.getPagination(),
                        tableService.getTablePreferences().getMaxRowsPerPage(), true,
                        tableTopComponent.getCurrentGraph().getId()));
        exportButton.getItems().addAll(exportCsvMenu, exportCsvSelectionMenu,
                exportExcelMenu, exportExcelSelectionMenu);
    }
    
    /**
     * Get the export button that the menu items have been added to.
     *
     * @return the export table data button
     */
    public MenuButton getExportButton() {
        return exportButton;
    }

    /**
     * Get the menu item that will export all the table data in CSV format.
     *
     * @return the export all rows to CSV menu item
     */
    public MenuItem getExportCsvMenu() {
        return exportCsvMenu;
    }

    /**
     * Get the menu item that will export only the selected rows in CSV format.
     *
     * @return the export selected rows only to CSV menu item
     */
    public MenuItem getExportCsvSelectionMenu() {
        return exportCsvSelectionMenu;
    }

    /**
     * Get the menu item that will export all the table data in Excel format.
     *
     * @return the export all rows to Excel menu item 
     */
    public MenuItem getExportExcelMenu() {
        return exportExcelMenu;
    }

    /**
     * Get the menu item that will export only the selected rows in Excel format.
     *
     * @return the export selected rows only to Excel menu item
     */
    public MenuItem getExportExcelSelectionMenu() {
        return exportExcelSelectionMenu;
    }
    
    /**
     * Creates a menu button to store the export menu items under. Sets the icon
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
     * Creates a export {@link MenuItem}. Sets the title text and the action
     * handler to a new {@link ExportMenuItemActionHandler}.
     *
     * @param menuTitle the title to put on the menu item
     * @param runnable the {@link Runnable} that will be executed when the menu
     *     item is selected. This will contain code to perform the export.
     * @return the created menu item 
     */
    private MenuItem createExportMenu(final String menuTitle,
                                      final Runnable runnable) {
        final MenuItem menuItem = new MenuItem(menuTitle);
        
        menuItem.setOnAction(new ExportMenuItemActionHandler(runnable));
        
        return menuItem;
    }
    
    /**
     * Action handler for menu items that will export table rows to either CSV or Excel.
     */
    class ExportMenuItemActionHandler implements EventHandler<ActionEvent> {
        private final Runnable runnable;
        
        /**
         * Creates a new export menu item action handler.
         *
         * @param runnable the runnable to execute when this handler is activated
         */
        public ExportMenuItemActionHandler(final Runnable runnable) {
            this.runnable = runnable;
        }
        
        /**
         * Executes the {@link Runnable} passed in during construction. The runnable
         * will only be executed if the current graph is not null.
         * 
         * @param event the event that triggered this action
         * @see EventHandler#handle(javafx.event.Event)
         */
        @Override
        public void handle(ActionEvent event) {
            if (tableTopComponent.getCurrentGraph() != null) {
                runnable.run();
            }
            event.consume();
        }
    }

    
}
