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

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview2.TableViewTopComponent;
import static au.gov.asd.tac.constellation.views.tableview2.TableViewUtilities.TABLE_LOCK;
import au.gov.asd.tac.constellation.views.tableview2.io.TableViewPreferencesIOUtilities;
import au.gov.asd.tac.constellation.views.tableview2.service.PreferenceService;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.UpdateMethod;
import au.gov.asd.tac.constellation.views.tableview2.state.TablePreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;

/**
 *
 * @author formalhaunt
 */
public class PreferencesMenu {
    private static final Logger LOGGER = Logger.getLogger(PreferencesMenu.class.getName());
    
    private static final String SAVE_PREFERENCES = "Save Table Preferences";
    private static final String LOAD_PREFERENCES = "Load Table Preferences...";
    private static final String PAGE_SIZE_PREFERENCES = "Set Page Size";
    
    private static final ImageView SETTINGS_ICON = new ImageView(UserInterfaceIconProvider.SETTINGS.buildImage(16));
    
    private static final int WIDTH = 120;
    
    private final ToggleGroup pageSizeToggle = new ToggleGroup();
    
    private final TableViewTopComponent tableTopComponent;
    private final TableViewPane tablePane;
    private final Table table;
    private final TableService tableService;
    private final PreferenceService preferenceService;
    
    private MenuButton preferencesButton;
    private MenuItem savePreferencesMenu;
    private MenuItem loadPreferencesMenu;
    private Menu pageSizeMenu;
    
    public PreferencesMenu(final TableViewTopComponent tableTopComponent,
                           final TableViewPane tablePane,
                           final Table table,
                           final TableService tableService,
                           final PreferenceService preferenceService) {
        this.tableTopComponent = tableTopComponent;
        this.tablePane = tablePane;
        this.table = table;
        this.tableService = tableService;
        this.preferenceService = preferenceService;
        
    }
    
    public void init() {
        preferencesButton = createMenuButton(SETTINGS_ICON);
        pageSizeMenu = createPageSizeMenu();
        
        savePreferencesMenu = createPreferencesMenuItem(SAVE_PREFERENCES, e -> {
            if ((!table.getTableView().getColumns().isEmpty()) 
                    && (GraphManager.getDefault().getActiveGraph() != null)) {
                TableViewPreferencesIOUtilities.savePreferences(
                        tableTopComponent.getCurrentState().getElementType(), table.getTableView(),
                        preferenceService.getMaxRowsPerPage()
                );
            }
            e.consume();
        });
        
        loadPreferencesMenu = createPreferencesMenuItem(LOAD_PREFERENCES, e -> {
            if (GraphManager.getDefault().getActiveGraph() != null) {
                loadPreferences();
                //TODO: Replace need to sleep before paginating
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
                tableService.updatePagination(preferenceService.getMaxRowsPerPage());
                Platform.runLater(() -> {
                    tablePane.setCenter(tableService.getPagination());
                });
            }
            e.consume();
        });
    }
    
    public MenuButton getPreferencesButton() {
        return preferencesButton;
    }

    public MenuItem getSavePreferencesMenu() {
        return savePreferencesMenu;
    }

    public MenuItem getLoadPreferencesMenu() {
        return loadPreferencesMenu;
    }

    public Menu getPageSizeMenu() {
        return pageSizeMenu;
    }
    
    private Menu createPageSizeMenu() {
        final Menu menu = new Menu(PAGE_SIZE_PREFERENCES);
        menu.getItems().addAll(
                IntStream.of(100, 250, 500, 1000)
                        .mapToObj(pageSize -> {
                            final RadioMenuItem pageSizeOption
                                    = new RadioMenuItem(Integer.toString(pageSize));
                            pageSizeOption.setToggleGroup(pageSizeToggle);
                            pageSizeOption.setOnAction(e -> {
                                if (preferenceService.getMaxRowsPerPage() != pageSize) {
                                    preferenceService.setMaxRowsPerPage(pageSize);
                                    tableService.updatePagination(preferenceService.getMaxRowsPerPage());
                                    Platform.runLater(() -> {
                                        tablePane.setCenter(tableService.getPagination());
                                    });
                                }
                            });
                            if (pageSize == TablePreferences.DEFAULT_MAX_ROWS_PER_PAGE) {
                                pageSizeOption.setSelected(true); // initially set the default as selected
                            }
                            return pageSizeOption;
                        }).collect(Collectors.toList())
        );
        
        return menu;
    }
    
    private MenuButton createMenuButton(final ImageView icon) {
        final MenuButton button = new MenuButton();
        
        button.setGraphic(icon);
        button.setMaxWidth(WIDTH);
        button.setPopupSide(Side.RIGHT);
        
        return button;
    }
    
    private MenuItem createPreferencesMenuItem(final String menuTitle,
                                               final EventHandler<ActionEvent> action) {
        final MenuItem menuItem = new MenuItem(menuTitle);
        
        menuItem.setOnAction(action);
        
        return menuItem;
    }
    
    /**
     * Allow user to select saved preferences file and update table view format
     * (displayed column/column order and sort order) to match values found in
     * saved preferences file.
     */
    private void loadPreferences() {
        synchronized (TABLE_LOCK) {
            if (tableTopComponent.getCurrentState() != null) {

                final List<TableColumn<ObservableList<String>, ?>> newColumnOrder = new ArrayList<>();
                final ThreeTuple<List<String>, Tuple<String, TableColumn.SortType>, Integer> tablePrefs
                        = TableViewPreferencesIOUtilities.getPreferences(
                                tableTopComponent.getCurrentState().getElementType(),
                                preferenceService.getMaxRowsPerPage());

                // If no columns were found then the user abandoned load as saves cannot occur with 0 columns
                if (tablePrefs.getFirst().isEmpty()) {
                    return;
                }

                tablePrefs.getFirst().forEach(columnName -> {
                    // Loop through column names found in prefs and add associated columns to newColumnOrder list all set to visible.
                    table.getTableView().getColumns().stream()
                            .filter(column -> column.getText().equals(columnName))
                            .forEachOrdered(column -> {
                                // TODO This is not a copy. Copy is the same ref. Pointless?? Is that an issue??
                                final TableColumn<ObservableList<String>, ?> copy = column;
                                copy.setVisible(true);
                                newColumnOrder.add(copy);
                            });
                });

                // Populate orderedColumns with full column ThreeTuples corresponding to entires in newVolumnOrder and call updateVisibleColumns
                // to update table.
                final List<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> orderedColumns
                        = newColumnOrder.stream().map(c -> {
                            for (final ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>> col : table.getColumnIndex()) {
                                if (c.getText().equals(col.getThird().getText())) {
                                    return col;
                                }
                            }
                            // The following can only happen
                            return table.getColumnIndex().get(newColumnOrder.indexOf(c));
                        }).collect(Collectors.toList());
                
                tableService.saveSortDetails(
                        tablePrefs.getSecond().getFirst(),
                        tablePrefs.getSecond().getSecond()
                );
                
                tableService.updateVisibleColumns(
                        tableTopComponent.getCurrentGraph(),
                        tableTopComponent.getCurrentState(),
                        orderedColumns.stream()
                                .map(columnTuple -> Tuple.create(
                                        columnTuple.getFirst(),
                                        columnTuple.getSecond()
                                ))
                                .collect(Collectors.toList()),
                        UpdateMethod.REPLACE
                );
                
                for (final Toggle t : pageSizeToggle.getToggles()) {
                    final RadioMenuItem pageSizeOption = (RadioMenuItem) t;
                    if (Integer.parseInt(pageSizeOption.getText()) == tablePrefs.getThird()) {
                        pageSizeOption.setSelected(true);
                        preferenceService.setMaxRowsPerPage(tablePrefs.getThird());
                        break;
                    }
                }
            }
        }
    }

    
}
