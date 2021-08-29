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
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.UpdateMethod;
import au.gov.asd.tac.constellation.views.tableview2.state.TablePreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
import org.apache.commons.collections4.CollectionUtils;

/**
 * Creates a preferences menu that allows users to load, save and set different
 * preferences like page size.
 *
 * @author formalhaunt
 */
public class PreferencesMenu {
    private static final Logger LOGGER = Logger.getLogger(PreferencesMenu.class.getName());
    
    private static final String SAVE_PREFERENCES = "Save Table Preferences";
    private static final String LOAD_PREFERENCES = "Load Table Preferences...";
    private static final String PAGE_SIZE_PREFERENCES = "Set Page Size";
    
    private static final ImageView SETTINGS_ICON = new ImageView(UserInterfaceIconProvider.SETTINGS.buildImage(16));
    
    private static final Integer DEFAULT_MAX_ROWS_PER_PAGE = 500;
    
    private static final int WIDTH = 120;
    
    private final ToggleGroup pageSizeToggle = new ToggleGroup();
    
    private final TableViewTopComponent tableTopComponent;
    private final TableViewPane tablePane;
    private final Table table;
    private final TableService tableService;
    
    private MenuButton preferencesButton;
    private MenuItem savePreferencesMenu;
    private MenuItem loadPreferencesMenu;
    private Menu pageSizeMenu;
    
    /**
     * Creates a new preferences menu.
     *
     * @param tableTopComponent
     * @param tablePane
     * @param table
     * @param tableService 
     */
    public PreferencesMenu(final TableViewTopComponent tableTopComponent,
                           final TableViewPane tablePane,
                           final Table table,
                           final TableService tableService) {
        this.tableTopComponent = tableTopComponent;
        this.tablePane = tablePane;
        this.table = table;
        this.tableService = tableService;
        
    }
    
    /**
     * Initializes the preferences menu. Until this method is called, all menu UI components
     * will be null.
     */
    public void init() {
        preferencesButton = createMenuButton(SETTINGS_ICON);
        pageSizeMenu = createPageSizeMenu();
        
        savePreferencesMenu = createPreferencesMenu(SAVE_PREFERENCES, e -> {
            if ((!table.getTableView().getColumns().isEmpty()) 
                    && (GraphManager.getDefault().getActiveGraph() != null)) {
                TableViewPreferencesIOUtilities.savePreferences(
                        tableTopComponent.getCurrentState().getElementType(), table.getTableView(),
                        tableService.getTablePreferences().getMaxRowsPerPage()
                );
            }
            e.consume();
        });
        
        loadPreferencesMenu = createPreferencesMenu(LOAD_PREFERENCES, e -> {
            if (GraphManager.getDefault().getActiveGraph() != null) {
                loadPreferences();
                
                // Load preferences starts work on JavaFX thread. We cannot update
                // the pagination until that work is complete.
                final CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> latch.countDown());
                
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    LOGGER.warning("Thread interrupted whilst waiting for JavaFX "
                            + "thread work to complete.");
                }
                
                tableService.updatePagination(tableService.getTablePreferences().getMaxRowsPerPage());
                Platform.runLater(() -> {
                    tablePane.setCenter(tableService.getPagination());
                });
            }
            e.consume();
        });
        
        preferencesButton.getItems().addAll(pageSizeMenu, savePreferencesMenu, loadPreferencesMenu);
    }
    
    /**
     * Get the preferences button that the menu items have been added to.
     *
     * @return the table preferences button
     */
    public MenuButton getPreferencesButton() {
        return preferencesButton;
    }

    /**
     * Get the save preferences menu which when clicked allows the user to save
     * all their stored table preferences to JSON on disk, allowing them to be
     * persisted.
     *
     * @return the save preferences menu
     */
    public MenuItem getSavePreferencesMenu() {
        return savePreferencesMenu;
    }

    /**
     * Get the load preferences menu which when clicked will load a users table
     * preferences from a JSON file stored on disk.
     *
     * @return the load preferences menu
     */
    public MenuItem getLoadPreferencesMenu() {
        return loadPreferencesMenu;
    }

    /**
     * Gets the page size menu which allows the user to change the pagination
     * size in the table.
     *
     * @return the page size menu
     */
    public Menu getPageSizeMenu() {
        return pageSizeMenu;
    }
    
    /**
     * Get the page size toggle group. All page size menu items belong to this
     * toggle group.
     *
     * @return the page size toggle group
     */
    public ToggleGroup getPageSizeToggle() {
        return pageSizeToggle;
    }
    
    /**
     * Creates the page size menu that will be added to the preferences menu. This
     * menu provides 4 options for page sizes. When one of the page size menu items
     * is clicked, the table is refreshed and displays the date with the new pagination
     * settings.
     *
     * @return the page size preference menu
     */
    private Menu createPageSizeMenu() {
        final Menu menu = new Menu(PAGE_SIZE_PREFERENCES);
        menu.getItems().addAll(
                IntStream.of(100, 250, 500, 1000)
                        .mapToObj(pageSize -> {
                            final RadioMenuItem pageSizeOption
                                    = new RadioMenuItem(Integer.toString(pageSize));
                            pageSizeOption.setToggleGroup(pageSizeToggle);
                            pageSizeOption.setOnAction(e -> {
                                // TODO potential race condition with the setting of the preferences???
                                if (tableService.getTablePreferences().getMaxRowsPerPage() != pageSize) {
                                    tableService.getTablePreferences().setMaxRowsPerPage(pageSize);
                                    tableService.updatePagination(tableService.getTablePreferences().getMaxRowsPerPage());
                                    Platform.runLater(() -> {
                                        tablePane.setCenter(tableService.getPagination());
                                    });
                                }
                            });
                            if (pageSize == DEFAULT_MAX_ROWS_PER_PAGE) {
                                pageSizeOption.setSelected(true); // initially set the default as selected
                            }
                            return pageSizeOption;
                        }).collect(Collectors.toList())
        );
        
        return menu;
    }
    
    /**
     * Creates a menu button to store the preferences menu items under. Sets the icon
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
     * Creates a preferences {@link MenuItem}. Sets the title text and the action
     * handler to a new {@link EventHandler}.
     *
     * @param menuTitle the title to put on the menu item
     * @param runnable the {@link Runnable} that will be executed when the menu
     *     item is selected.
     * @return the created menu item 
     */
    private MenuItem createPreferencesMenu(final String menuTitle,
                                           final EventHandler<ActionEvent> action) {
        final MenuItem menuItem = new MenuItem(menuTitle);
        
        menuItem.setOnAction(action);
        
        return menuItem;
    }
    
    /**
     * Loads a saved table preferences JSON file and updates the table format
     * (displayed column/column order and sort order) to match the values found.
     * <p/>
     * This method will place a lock on the table to prevent any updates to the
     * preferences whilst this load is happening.
     * <p/>
     * This method will start work on the JavaFX thread to update certain parts
     * of the table like column visibility. Once the method returns it is
     * recommended that the current thread waits for that work to complete before
     * initiating any other actions.
     */
    protected void loadPreferences() {
        synchronized (TABLE_LOCK) {
            if (tableTopComponent.getCurrentState() != null) {

                // Load the local table preferences JSON file
                final TablePreferences tablePrefs
                        = TableViewPreferencesIOUtilities.getPreferences(
                                tableTopComponent.getCurrentState().getElementType(),
                                tableService.getTablePreferences().getMaxRowsPerPage() != null
                                        ? tableService.getTablePreferences().getMaxRowsPerPage()
                                        : DEFAULT_MAX_ROWS_PER_PAGE);

                // If no columns were found then the user abandoned the load as saves
                // cannot occur with 0 columns
                if (CollectionUtils.isEmpty(tablePrefs.getColumnOrder())) {
                    return;
                }

                final List<TableColumn<ObservableList<String>, ?>> newColumnOrder = new ArrayList<>();
                
                // Loop through column names in the loaded preferences and add the
                // associated columns to newColumnOrder (if found). Also set the
                // found columns to visible.
                tablePrefs.getColumnOrder().forEach(columnName -> {
                    table.getTableView().getColumns().stream()
                            .filter(column -> column.getText().equals(columnName))
                            .forEachOrdered(column -> {
                                column.setVisible(true);
                                newColumnOrder.add(column);
                            });
                });

                // Populate orderedColumns with entries from column index that match
                // the names of the columns in the loaded preferences.
                final List<Tuple<String, Attribute>> orderedColumns
                        = newColumnOrder.stream()
                                .map(c -> {
                                    for (final ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>> col : table.getColumnIndex()) {
                                        if (c.getText().equals(col.getThird().getText())) {
                                            return col;
                                        }
                                    }
                                    
                                    // TODO This seems like a bad fallback. I think returning null
                                    //      and adding a filter for nonNull objects would be better
                                    return table.getColumnIndex().get(newColumnOrder.indexOf(c));
                                }).map(columnTuple -> Tuple.create(
                                        columnTuple.getFirst(),
                                        columnTuple.getSecond()
                                ))
                                .collect(Collectors.toList());
                
                // Update the sort preferences
                tableService.saveSortDetails(
                        tablePrefs.getSortColumn(),
                        tablePrefs.getSortDirection()
                );
                
                // Update the visibile columns
                tableService.updateVisibleColumns(
                        tableTopComponent.getCurrentGraph(),
                        tableTopComponent.getCurrentState(),
                        orderedColumns,
                        UpdateMethod.REPLACE
                );
                
                // Update the page size menu selection and page size preferences
                for (final Toggle t : getPageSizeToggle().getToggles()) {
                    final RadioMenuItem pageSizeOption = (RadioMenuItem) t;
                    if (Integer.parseInt(pageSizeOption.getText()) == tablePrefs.getMaxRowsPerPage()) {
                        pageSizeOption.setSelected(true);
                        tableService.getTablePreferences().setMaxRowsPerPage(tablePrefs.getMaxRowsPerPage());
                        break;
                    }
                }
            }
        }
    }

    

    
}
