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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.PluginFinder;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;

/**
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
    
    private MenuItem deactivateAllPlugins;
    private MenuItem findPlugin;
    private MenuItem openAllSections;
    private MenuItem closeAllSections;
    private MenuItem run;
    private MenuItem runFromHere;
    private MenuItem runToHere;
    
    private ContextMenu contextMenu;
    
    /**
     * 
     * @param dataAccessTabPane
     * @param tab 
     */
    public TabContextMenu(final DataAccessTabPane dataAccessTabPane,
                          final Tab tab) {
        this.dataAccessTabPane = dataAccessTabPane;
        this.tab = tab;
    }
    
    /**
     * 
     */
    public void init() {
        deactivateAllPlugins = new MenuItem(DEACTIVATE_ALL_PLUGINS_TEXT);
        deactivateAllPlugins.setOnAction((ActionEvent event) -> {
            QueryPhasePane queryPhasePane = dataAccessTabPane.getQueryPhasePane(tab);
            for (DataSourceTitledPane dataSourceTitledPane : queryPhasePane.getDataAccessPanes()) {
                if (dataSourceTitledPane.isQueryEnabled()) {
                    dataSourceTitledPane.validityChanged(false);
                }
            }
        });

        findPlugin = new MenuItem(FIND_PLUGIN_TEXT);
        findPlugin.setOnAction(event -> {
            // Run it later to allow the menu to close.
            Platform.runLater(() -> {
                final PluginFinder pluginFinder = new PluginFinder();
                pluginFinder.find(
                        dataAccessTabPane.getDataAccessPane(),
                        dataAccessTabPane.getQueryPhasePane(tab)
                );
            });
        });

        openAllSections = new MenuItem(OPEN_ALL_SECTIONS_TEXT);
        openAllSections.setOnAction(event -> {
            final QueryPhasePane queryPhasePane = dataAccessTabPane.getQueryPhasePane(tab);
            queryPhasePane.setHeadingsExpanded(true, false);
        });

        closeAllSections = new MenuItem(CLOSE_ALL_SECTIONS_TEXT);
        closeAllSections.setOnAction(event -> {
            final QueryPhasePane queryPhasePane = dataAccessTabPane.getQueryPhasePane(tab);
            queryPhasePane.setHeadingsExpanded(false, false);
        });

        run = new MenuItem(RUN_TEXT);
        run.setOnAction((ActionEvent event) -> {
            int index = dataAccessTabPane.getTabPane().getTabs().indexOf(tab);
            dataAccessTabPane.runTabs(index, index);
        });

        runFromHere = new MenuItem(RUN_FROM_HERE_TEXT);
        runFromHere.setOnAction((ActionEvent event) -> {
            ObservableList<Tab> allTabs = dataAccessTabPane.getTabPane().getTabs();
            int index = allTabs.indexOf(tab);
            dataAccessTabPane.runTabs(index, allTabs.size() - 1);
        });

        runToHere = new MenuItem(RUN_TO_HERE_TEXT);
        runToHere.setOnAction((ActionEvent event) -> {
            int index = dataAccessTabPane.getTabPane().getTabs().indexOf(tab);
            dataAccessTabPane.runTabs(0, index);
        });
        
        /**
         * The position order of the menu options has been considered carefully
         * based on feedback. For instance the "Deactivate all plugins" exists
         * as the first entry because it is the most common use case and also
         * makes it less likely for one of the run* options to be clicked
         * accidentally.
         */
        contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
                deactivateAllPlugins,
                new SeparatorMenuItem(),
                findPlugin,
                openAllSections,
                closeAllSections,
                new SeparatorMenuItem(),
                run,
                runFromHere,
                runToHere
        );
    }

    public MenuItem getDeactivateAllPlugins() {
        return deactivateAllPlugins;
    }

    public void setDeactivateAllPlugins(MenuItem deactivateAllPlugins) {
        this.deactivateAllPlugins = deactivateAllPlugins;
    }

    public MenuItem getFindPlugin() {
        return findPlugin;
    }

    public MenuItem getOpenAllSections() {
        return openAllSections;
    }

    public MenuItem getCloseAllSections() {
        return closeAllSections;
    }

    public MenuItem getRun() {
        return run;
    }

    public MenuItem getRunFromHere() {
        return runFromHere;
    }

    public MenuItem getRunToHere() {
        return runToHere;
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }
}
