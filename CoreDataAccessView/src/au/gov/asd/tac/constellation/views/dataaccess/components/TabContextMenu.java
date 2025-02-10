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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import au.gov.asd.tac.constellation.views.dataaccess.panes.PluginFinder;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;

/**
 * Creates a context menu for a specific tab.
 *
 * @author formalhaunt
 */
public final class TabContextMenu {
    private static final String DEACTIVATE_ALL_PLUGINS_TEXT = "Deactivate all plugins";
    private static final String FIND_PLUGIN_TEXT = "Find plugin...";
    private static final String OPEN_ALL_SECTIONS_TEXT = "Open all sections";
    private static final String CLOSE_ALL_SECTIONS_TEXT = "Close all sections";
    private static final String RUN_TEXT = "Run this tab only";
    private static final String RUN_FROM_HERE_TEXT = "Run from this tab";
    private static final String RUN_TO_HERE_TEXT = "Run to this tab";
    
    private final DataAccessTabPane dataAccessTabPane;
    private final Tab tab;
    
    private MenuItem deactivateAllPluginsMenuItem;
    private MenuItem findPluginMenuItem;
    private MenuItem openAllSectionsMenuItem;
    private MenuItem closeAllSectionsMenuItem;
    private MenuItem runMenuItem;
    private MenuItem runFromHereMenuItem;
    private MenuItem runToHereMenuItem;
    
    private ContextMenu contextMenu;
    
    /**
     * Creates a new context menu for the passed tab that exists on the passed
     * tab pane.
     *
     * @param dataAccessTabPane the tab pane that the passed tab belongs to
     * @param tab the tab that this context menu will open on
     */
    public TabContextMenu(final DataAccessTabPane dataAccessTabPane,
                          final Tab tab) {
        this.dataAccessTabPane = dataAccessTabPane;
        this.tab = tab;
    }
    
    /**
     * Initializes the context menu. Until this method is called, all context menu
     * UI components will be null.
     */
    public void init() {
        
        ///////////////////////////////////////
        // De-Activate All Plugins Menu Item
        ///////////////////////////////////////
        
        deactivateAllPluginsMenuItem = new MenuItem(DEACTIVATE_ALL_PLUGINS_TEXT);
        deactivateAllPluginsMenuItem.setOnAction(event -> {
            DataAccessTabPane.getQueryPhasePane(tab).getDataAccessPanes().stream()
                    .filter(dataSourceTitledPane -> (dataSourceTitledPane.isQueryEnabled()))
                    .forEachOrdered(dataSourceTitledPane -> 
                        dataSourceTitledPane.validityChanged(false)
                    );
            
            event.consume();
        });

        ////////////////////////////
        // Find Plugin Menu Item
        ////////////////////////////
        
        findPluginMenuItem = new MenuItem(FIND_PLUGIN_TEXT);
        findPluginMenuItem.setOnAction(event -> {
            // Run it later to allow the menu to close.
            Platform.runLater(() -> {
                final PluginFinder pluginFinder = new PluginFinder();
                pluginFinder.find(
                        DataAccessTabPane.getQueryPhasePane(tab)
                );
            });
            
            event.consume();
        });

        ////////////////////////////////
        // Open All Sections Menu Item
        ////////////////////////////////
        
        openAllSectionsMenuItem = new MenuItem(OPEN_ALL_SECTIONS_TEXT);
        openAllSectionsMenuItem.setOnAction(event -> {
            DataAccessTabPane.getQueryPhasePane(tab)
                    .setHeadingsExpanded(true, false);
            
            event.consume();
        });

        ////////////////////////////////
        // Close All Sections Menu Item
        ////////////////////////////////
        
        closeAllSectionsMenuItem = new MenuItem(CLOSE_ALL_SECTIONS_TEXT);
        closeAllSectionsMenuItem.setOnAction(event -> {
            DataAccessTabPane.getQueryPhasePane(tab)
                    .setHeadingsExpanded(false, false);
            
            event.consume();
        });

        ////////////////////////////////
        // Run Single Tab Menu Item
        ////////////////////////////////
        
        runMenuItem = new MenuItem(RUN_TEXT);
        runMenuItem.setOnAction(event -> {
            final int index = dataAccessTabPane.getTabPane().getTabs().indexOf(tab);
            dataAccessTabPane.runTabs(index, index);
            
            event.consume();
        });

        ////////////////////////////////
        // Run Tabs From Here Menu Item
        ////////////////////////////////
        
        runFromHereMenuItem = new MenuItem(RUN_FROM_HERE_TEXT);
        runFromHereMenuItem.setOnAction(event -> {
            final int index = dataAccessTabPane.getTabPane().getTabs().indexOf(tab);
            dataAccessTabPane.runTabs(index, dataAccessTabPane.getTabPane().getTabs().size() - 1);
            
            event.consume();
        });

        ////////////////////////////////
        // Run Tabs To Here Menu Item
        ////////////////////////////////
        
        runToHereMenuItem = new MenuItem(RUN_TO_HERE_TEXT);
        runToHereMenuItem.setOnAction(event -> {
            final int index = dataAccessTabPane.getTabPane().getTabs().indexOf(tab);
            dataAccessTabPane.runTabs(0, index);
            
            event.consume();
        });
        
        ////////////////////////////////
        // Context Menu
        ////////////////////////////////
        
        /**
         * The position order of the menu options has been considered carefully
         * based on feedback. For instance the "Deactivate all plugins" exists
         * as the first entry because it is the most common use case and also
         * makes it less likely for one of the run* options to be clicked
         * accidentally.
         */
        contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(deactivateAllPluginsMenuItem,
                new SeparatorMenuItem(),
                findPluginMenuItem,
                openAllSectionsMenuItem,
                closeAllSectionsMenuItem,
                new SeparatorMenuItem(),
                runMenuItem,
                runFromHereMenuItem,
                runToHereMenuItem
        );
    }

    /**
     * Gets the menu item that will disable all plugins on this tab that are
     * currently enabled.
     *
     * @return the deactivate all plugins menu item
     */
    public MenuItem getDeactivateAllPluginsMenuItem() {
        return deactivateAllPluginsMenuItem;
    }

    /**
     * Gets the menu item that will open a dialog listing all the available
     * plugins for the associated tab. The user can then use that dialog to
     * search for and select a plugin to reduce to (collapse all other plugins)
     * on the tab.
     * 
     * @return the find plugin menu item
     * @see PluginFinder
     */
    public MenuItem getFindPluginMenuItem() {
        return findPluginMenuItem;
    }

    /**
     * Gets the menu item that when clicked will expand all the plugin sections
     * on the associated tab.
     * 
     * @return the open all sections menu item
     */
    public MenuItem getOpenAllSectionsMenuItem() {
        return openAllSectionsMenuItem;
    }

    /**
     * Gets the menu item that when clicked will collapse all the plugin sections
     * on the associated tab.
     * 
     * @return the close all sections menu item
     */
    public MenuItem getCloseAllSectionsMenuItem() {
        return closeAllSectionsMenuItem;
    }

    /**
     * Gets the menu item that runs this tab's plugins.
     *
     * @return the run menu item
     */
    public MenuItem getRunMenuItem() {
        return runMenuItem;
    }

    /**
     * Gets the menu item that runs all tabs from this tab (left to right).
     *
     * @return the run from here menu item
     */
    public MenuItem getRunFromHereMenuItem() {
        return runFromHereMenuItem;
    }

    /**
     * Gets the menu item that runs all the tabs up to this tab (left to right).
     *
     * @return the run to here menu item
     */
    public MenuItem getRunToHereMenuItem() {
        return runToHereMenuItem;
    }

    /**
     * Gets the context menu to display for this tab.
     *
     * @return this tab's context menu
     */
    public ContextMenu getContextMenu() {
        return contextMenu;
    }
}
