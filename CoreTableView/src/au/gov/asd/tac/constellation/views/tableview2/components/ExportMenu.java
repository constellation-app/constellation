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
import javafx.scene.control.Pagination;
import javafx.scene.image.ImageView;

/**
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
    
    public ExportMenu(final TableViewTopComponent tableTopComponent,
                      final Table table,
                      final TableService tableService) {
        this.tableTopComponent = tableTopComponent;
        this.table = table;
        this.tableService = tableService;
    }
    
    public void init() {
        exportButton = createMenuButton(EXPORT_ICON);
        exportCsvMenu = createExportMenuItem(EXPORT_CSV, ()
                -> TableViewUtilities.exportToCsv(table.getTableView(), tableService.getPagination(), false));
        exportCsvSelectionMenu = createExportMenuItem(EXPORT_CSV_SELECTION, ()
                -> TableViewUtilities.exportToCsv(table.getTableView(), tableService.getPagination(), true));
        exportExcelMenu = createExportMenuItem(EXPORT_XLSX, ()
                -> TableViewUtilities.exportToExcel(table.getTableView(), tableService.getPagination(),
                        tableService.getTablePreferences().getMaxRowsPerPage(), false,
                        tableTopComponent.getCurrentGraph().getId()));
        exportExcelSelectionMenu = createExportMenuItem(EXPORT_XLSX_SELECTION, ()
                -> TableViewUtilities.exportToExcel(table.getTableView(), tableService.getPagination(),
                        tableService.getTablePreferences().getMaxRowsPerPage(), true,
                        tableTopComponent.getCurrentGraph().getId()));
        exportButton.getItems().addAll(exportCsvMenu, exportCsvSelectionMenu,
                exportExcelMenu, exportExcelSelectionMenu);
    }
    
    public MenuButton getExportButton() {
        return exportButton;
    }

    public MenuItem getExportCsvMenu() {
        return exportCsvMenu;
    }

    public MenuItem getExportCsvSelectionMenu() {
        return exportCsvSelectionMenu;
    }

    public MenuItem getExportExcelMenu() {
        return exportExcelMenu;
    }

    public MenuItem getExportExcelSelectionMenu() {
        return exportExcelSelectionMenu;
    }
    
    private MenuButton createMenuButton(final ImageView icon) {
        final MenuButton button = new MenuButton();
        
        button.setGraphic(icon);
        button.setMaxWidth(WIDTH);
        button.setPopupSide(Side.RIGHT);
        
        return button;
    }
    
    private MenuItem createExportMenuItem(final String menuTitle,
                                          final Runnable runnable) {
        final MenuItem menuItem = new MenuItem(menuTitle);
        
        menuItem.setOnAction(new ExportMenuItemActionHandler(runnable));
        
        return menuItem;
    }
    
    class ExportMenuItemActionHandler implements EventHandler<ActionEvent> {
        private final Runnable runnable;
        
        public ExportMenuItemActionHandler(final Runnable runnable) {
            this.runnable = runnable;
        }
        
        @Override
        public void handle(ActionEvent event) {
            if (tableTopComponent.getCurrentGraph() != null) {
                runnable.run();
            }
            event.consume();
        }
    }

    
}
