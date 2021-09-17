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

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.RecentParameterValues;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.WaitForQueriesToCompleteTask;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;

/**
 *
 * @author formalhaunt
 */
public final class DataAccessTabPane {
    private static final Logger LOGGER = Logger.getLogger(DataAccessTabPane.class.getName());
    
    private static final String TAB_TITLE = "Step";
    
    private final Function<Tab, Boolean> isExecutableTab = tab -> {
        final boolean hasEnabledPlugins = tabHasEnabledPlugins(tab);
        final boolean allEnabledPluginsValid = hasEnabledPlugins
                ? validateTabEnabledPlugins(tab) :  true;
        final boolean hasValidTimeRange = validateTabTimeRange(tab);

        return hasEnabledPlugins && allEnabledPluginsValid && hasValidTimeRange;
    };
    
    private final DataAccessPane dataAccessPane;
    private final Map<String, List<DataAccessPlugin>> plugins;
    private final TabPane tabPane;
    
    /**
     * 
     * @param dataAccessPane
     * @param plugins 
     */
    public DataAccessTabPane(final DataAccessPane dataAccessPane,
                             final Map<String, List<DataAccessPlugin>> plugins) {
        this.dataAccessPane = dataAccessPane;
        this.plugins = plugins;
        
        tabPane = new TabPane();
        tabPane.setSide(Side.TOP);
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (ObservableValue<? extends Number> ov, Number t, Number t1) -> {
                    storeParameterValues();
                }
        );

        // Update the button when the user adds/removes tabs.
        tabPane.getTabs().addListener(
                (ListChangeListener.Change<? extends Tab> c) -> {
                    this.dataAccessPane.update();
                }
        );
    }
    
    /**
     * 
     * @return 
     */
    public QueryPhasePane newTab() {
        final QueryPhasePane pane = new QueryPhasePane(plugins, getDataAccessPane(), null);
        newTab(pane);

        return pane;
    }
    
    /**
     * 
     * @param globalPrameters
     * @return 
     */
    public QueryPhasePane newTab(final PluginParameters globalPrameters) {
        final QueryPhasePane pane = new QueryPhasePane(plugins, getDataAccessPane(), globalPrameters);
        newTab(pane);

        return pane;
    }
    
    /**
     * Create a new tab, which will renumber other tabs when it is closed
     *
     * @param queryPane
     */
    public void newTab(final QueryPhasePane queryPane) {
        final Tab newTab = new Tab(TAB_TITLE + " " + (tabPane.getTabs().size() + 1));
        
        // Get a copy of the existing on closed handler and call it after
        // tab counts are corrected and updated.
        final Optional<EventHandler<Event>> origOnClose =
                Optional.ofNullable(newTab.getOnClosed());
        newTab.setOnClosed(event -> {
            int queryNum = 1;
            for (Tab tab : tabPane.getTabs()) {
                tab.setText(TAB_TITLE + " " + queryNum);
                queryNum++;
            }
            
            origOnClose.ifPresent(handler -> handler.handle(event));
        });

        final TabContextMenu tabContextMenu = new TabContextMenu(this, newTab);
        tabContextMenu.init();

        queryPane.addGraphDependentMenuItems(
                tabContextMenu.getRun(),
                tabContextMenu.getRunFromHere(),
                tabContextMenu.getRunToHere()
        );
        queryPane.addPluginDependentMenuItems(
                tabContextMenu.getDeactivateAllPlugins()
        );

        final ScrollPane queryPhaseScroll = new ScrollPane();
        queryPhaseScroll.setFitToWidth(true);
        queryPhaseScroll.setContent(queryPane);
        queryPhaseScroll.setStyle("-fx-background-color: black;");

        newTab.setContextMenu(tabContextMenu.getContextMenu());
        newTab.setContent(queryPhaseScroll);
        newTab.setTooltip(new Tooltip("Right click for more options"));
        newTab.setClosable(true);
        
        // Must be called after setting the scroll pane
        final boolean hasEnabledPlugins = tabHasEnabledPlugins(newTab);
        final boolean isExecuteButtonIsGo = DataAccessPaneState.isExecuteButtonIsGo();
        final boolean isExecuteButtonEnabled = !getDataAccessPane().getButtonToolbar()
                .getExecuteButton().isDisabled();
        
        updateTabMenu(
                newTab,
                hasEnabledPlugins && isExecuteButtonIsGo && isExecuteButtonEnabled,
                hasEnabledPlugins
        );
        
        tabPane.getTabs().add(newTab);
    }
    
    /**
     * Run a range of tabs, numbered inclusively from 0.
     *
     * @param firstTab
     * @param lastTab
     */
    public void runTabs(final int firstTab, final int lastTab) {
        getDataAccessPane().setExecuteButtonToStop();
        
        // Need to take a copy for when it changes while this thread is still running
        final String activeGraphId = GraphManager.getDefault().getActiveGraph().getId();
        
        DataAccessPaneState.setQueriesRunning(activeGraphId, true);

        // TODO This was being called every time runPlugins is called but can't
        // see the point..could break!!!
        storeParameterValues();
        
        List<Future<?>> barrier = null;
        for (int i = firstTab; i <= lastTab; i++) {
            final Tab tab = tabPane.getTabs().get(i);
            
            LOGGER.log(Level.INFO, String.format("Running tab: %s", tab.getText()));
            
            barrier = getQueryPhasePane(tab).runPlugins(barrier);
        }

        CompletableFuture.runAsync(() -> new WaitForQueriesToCompleteTask(getDataAccessPane(), activeGraphId),
                getDataAccessPane().getParentComponent().getExecutorService());
    }
    
    /**
     * 
     * @param tab
     * @return 
     */
    public static QueryPhasePane getQueryPhasePane(final Tab tab) {
        return (QueryPhasePane) ((ScrollPane) tab.getContent()).getContent();
    }
    
    /**
     * Enable or disable the items in the contextual menu for all tabs.
     * 
     * @return 
     */
    public boolean updateTabMenus() {
        final boolean isExecuteButtonIsGo = DataAccessPaneState.isExecuteButtonIsGo();
        final boolean isExecuteButtonEnabled = !getDataAccessPane().getButtonToolbar()
                .getExecuteButton().isDisabled();
                    
        return tabPane.getTabs().parallelStream()
                .map(tab -> {
                    // TODO This needs some more work to try and consolidate it
                    //      as the logic to disable/enable the execute button is
                    //      slightly different to this logic
                    final boolean hasEnabledPlugins = tabHasEnabledPlugins(tab);
                    final boolean allEnabledPluginsValid = hasEnabledPlugins
                            ? validateTabEnabledPlugins(tab) :  true;
                    final boolean hasValidTimeRange = validateTabTimeRange(tab);

                    updateTabMenu(
                            tab,
                            hasEnabledPlugins && isExecuteButtonIsGo && isExecuteButtonEnabled,
                            hasEnabledPlugins
                    );
                    
                    return hasEnabledPlugins && allEnabledPluginsValid && hasValidTimeRange;
                })
                .filter(b -> !b) // Reduce to only tabs that are invalid
                .findAny()
                .isEmpty(); // For the pane to be valid then all panes need to be valid;
    }
    
    /**
     * Store current parameter values for all tabs and plug-ins in the recent
     * values repository.
     */
    private void storeParameterValues() {
        getTabPane().getTabs().forEach(tab -> {
            final QueryPhasePane pluginPane = getQueryPhasePane(tab);
            
            pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet().parallelStream()
                    .filter(param ->
                            param.getValue().getStringValue() != null
                                    && !param.getValue().getStringValue().isEmpty()
                    )
                    .forEach(param -> RecentParameterValues.storeRecentValue(
                            param.getKey(), param.getValue().getStringValue()
                    ));
            
            pluginPane.getDataAccessPanes().parallelStream()
                    .map(DataSourceTitledPane::getParameters)
                    .filter(Objects::nonNull)
                    .map(PluginParameters::getParameters)
                    .map(Map::entrySet)
                    .flatMap(Collection::stream)
                    .filter(param -> param.getValue().getObjectValue() != null)
                    .forEach(param -> {
                        if (!param.getValue().getType().toString().contains("LocalDateParameterType")) {
                            RecentParameterValues.storeRecentValue(
                                    param.getKey(),
                                    param.getValue().getStringValue()
                            );
                        } else {
                            RecentParameterValues.storeRecentValue(
                                    param.getKey(),
                                    param.getValue().getObjectValue().toString()
                            );
                        }
                    });
        });
    }
    
    /**
     * Enable or disable the items in the contextual menu for a tab.
     *
     * @param tab the tab to update the menu item status on
     * @param enabled true if the menu items are to be enabled, false otherwise
     */
    private void updateTabMenu(final Tab tab,
                               final boolean graphDependentMenuItemsEnabled,
                               final boolean pluginDependentMenuItemsEnabled) {
        final QueryPhasePane queryPhasePane = getQueryPhasePane(tab);
        queryPhasePane.enableGraphDependentMenuItems(graphDependentMenuItemsEnabled);
        queryPhasePane.enablePluginDependentMenuItems(pluginDependentMenuItemsEnabled);
    }

    /**
     * 
     * @return 
     */
    public boolean isTabPaneExecutable() {
        return tabPane.getTabs().parallelStream()
                .map(isExecutableTab)
                .filter(b -> !b) // Reduce to only tabs that are invalid
                .findAny()
                .isEmpty(); // For the pane to be valid then all panes need to be valid
    }
    
    /**
     * Checks if if any tab contains an enabled plugin and all found enabled plugins
     * have valid configuration. If there are no enabled plugins or enabled plugins
     * with invalid configuration then false is returned.
     *
     * @param tabPane the tab pane to check for active and valid plugins
     * @return true if there are active plugins and they are valid, false otherwise
     */
    public boolean hasActiveAndValidPlugins() {
        return tabPane.getTabs().stream()
                .filter(tab -> tabHasEnabledPlugins(tab) && !validateTabEnabledPlugins(tab))
                .findAny() // Find any invalid plugins
                .isEmpty();
    }
    
    /**
     * Check if a tab has any plug-ins selected for running.
     *
     * @param tab the tab to check
     * @return true if the tab has any plug-ins running, false otherwise
     */
    public static boolean tabHasEnabledPlugins(final Tab tab) {
        return getQueryPhasePane(tab).getDataAccessPanes().parallelStream()
                .filter(DataSourceTitledPane::isQueryEnabled)
                .findAny()
                .isPresent();
    }

    /**
     * Check whether the selected plugins contain any parameters with invalid
     * values.
     *
     * @param tab the tab to check
     * @return true if the tab's plug-ins have valid parameters, false otherwise
     */
    public static boolean validateTabEnabledPlugins(final Tab tab) {
        return !getQueryPhasePane(tab).getDataAccessPanes().parallelStream()
                .filter(DataSourceTitledPane::isQueryEnabled)
                .map(DataSourceTitledPane::getParameters)
                .filter(Objects::nonNull)
                .map(PluginParameters::getParameters)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .anyMatch(entry -> entry.getValue().getError() == null);
    }
    
    /**
     * 
     * @param tab
     * @return 
     */
    public static boolean validateTabTimeRange(final Tab tab) {
        final DateTimeRange range = getQueryPhasePane(tab).getGlobalParametersPane().getParams()
                .getDateTimeRangeValue(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID);
        
        return !range.getZonedStartEnd()[0].isAfter(range.getZonedStartEnd()[1]);
    }
    
    public QueryPhasePane getQueryPhasePaneOfCurrentTab() {
        return getQueryPhasePane(getCurrentTab());
    }
    
    public Tab getCurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    public void removeTabs() {
        tabPane.getTabs().clear();
    }
    
    public TabPane getTabPane() {
        return tabPane;
    }

    public DataAccessPane getDataAccessPane() {
        return dataAccessPane;
    }
}
