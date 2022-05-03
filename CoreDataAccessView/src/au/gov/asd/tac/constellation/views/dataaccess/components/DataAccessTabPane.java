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
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
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
import java.util.stream.Collectors;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import org.apache.commons.lang3.StringUtils;

/**
 * Wrapper of the JavaFX tab pane for the CONSTELLATION Data Access View. Provides
 * support for adding, removing and updating the tabs. Also provides functionality
 * to execute one or more tabs with data access plugins.
 *
 * @author formalhaunt
 */
public class DataAccessTabPane {
    private static final Logger LOGGER = Logger.getLogger(DataAccessTabPane.class.getName());
    
    private static final String TAB_TITLE = "Step %d";
    public static final String LOCAL_DATE_PARAMETER_TYPE = "LocalDateParameterType";
    private static final int DOUBLE_CLICK_COUNT = 2;
    
    /**
     * Function for determining if a tab is executable. A tab is executable
     * if it has enabled plugins and all enabled plugins are valid. The tab's
     * date range also needs to be valid.
     */
    private final Function<Tab, Boolean> isExecutableTab = tab -> {
        final boolean hasEnabledPlugins = tabHasEnabledPlugins(tab);
        final boolean allEnabledPluginsValid = !hasEnabledPlugins
                || (hasEnabledPlugins && validateTabEnabledPlugins(tab));
        final boolean hasValidTimeRange = validateTabTimeRange(tab);

        return hasEnabledPlugins && allEnabledPluginsValid && hasValidTimeRange;
    };
    
    private final DataAccessPane dataAccessPane;
    private final Map<String, List<DataAccessPlugin>> plugins;
    private final TabPane tabPane;
    
    /**
     * Creates a new data access tab pane.
     *
     * @param dataAccessPane the data access pane that this tab pane will be added to
     * @param plugins the data access plugins found at startup
     * @see DataAccessPaneState#getPlugins() 
     */
    public DataAccessTabPane(final DataAccessPane dataAccessPane,
                             final Map<String, List<DataAccessPlugin>> plugins) {
        this.dataAccessPane = dataAccessPane;
        this.plugins = plugins;
        
        tabPane = new TabPane();
        tabPane.setSide(Side.TOP);
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) ->
                        storeParameterValues()
        );

        // Update the button when the user adds/removes tabs.
        tabPane.getTabs().addListener(
                (final ListChangeListener.Change<? extends Tab> change) -> this.dataAccessPane.update()
        );
    }
    
    /**
     * Create a new tab with a default caption, "Step 1", "Step 2" etc. The
     * {@link QueryPhasePane} that is added to the tab will have no global
     * parameters initially. It will have the data access plugin     * map generated at start up and the {@link DataAccessPane} that this tab pane
     * resides on.
     *
     * @return the new {@link QueryPhasePane} that was added to the tab
     * @see #newTab(PluginParameters)
     */
    public QueryPhasePane newTab() {
        return newTab((PluginParameters) null, "");
    }

    /**
     * Create a new tab with the specified caption. The {@link QueryPhasePane}
     * that is added to the tab will have no global parameters initially. It
     * will have the data access plugin map generated at start up and the
     * {@link DataAccessPane} that this tab pane resides on.
     *
     * @param tabCaption the caption of the new tab
     * @return the new {@link QueryPhasePane} that was added to the tab
     * @see #newTab(PluginParameters)
     */
    public QueryPhasePane newTab(final String tabCaption) {
        return newTab((PluginParameters) null, tabCaption);
    }
    
    /**
     * Create a new tab with passed global parameters added to the new tabs
     * {@link QueryPhasePane}. The new pane is created with the data access plugin
     * map generated at start up and the {@link DataAccessPane} that this tab
     * pane resides on.
     *
     * @param globalPrameters the global parameters to add to the new pane
     * @return the new {@link QueryPhasePane} that was added to the tab
     * @see #newTab(QueryPhasePane)
     * @see DataAccessPaneState#getPlugins()
     */
    public QueryPhasePane newTab(final PluginParameters globalPrameters, final String tabCaption) {
        final QueryPhasePane pane = new QueryPhasePane(plugins, getDataAccessPane(), globalPrameters);
        newTab(pane, tabCaption);
        return pane;
    }
    
    /**
     * Create a new tab and adds it to the end of the tab pane. The new tab will
     * have a context menu created and added to it.
     * 
     * @param queryPane the pane that will be added as the content of the new tab
     */
    public void newTab(final QueryPhasePane queryPane, final String userCaption) {
        final Label label = new Label(userCaption);
        final Label defaultLabel = new Label();
        final String defaultCaption = String.format(TAB_TITLE, getTabPane().getTabs().size() + 1);

        final Tab newTab = new Tab();

        setTabCaption(label, defaultLabel, defaultCaption);

        label.setGraphic(defaultLabel);
        label.setContentDisplay(ContentDisplay.LEFT);
        newTab.setGraphic(label);

        label.setOnMouseClicked(event -> labelClickEvent(newTab, label, event));

        // Get a copy of the existing on closed handler. When a tab is closed,
        // it will be called after the tab names are corrected and updated.
        final Optional<EventHandler<Event>> origOnClose
                = Optional.ofNullable(newTab.getOnClosed());
        newTab.setOnClosed(event -> {
            int queryNum = 1;

            for (final Tab tab : getTabPane().getTabs()) {
                final String newDefaultCaption = String.format(TAB_TITLE, queryNum);
                final Label tabLabel = (Label) tab.getGraphic();
                final Label tabDefaultLabel = (Label) tabLabel.getGraphic();

                setTabCaption(tabLabel, tabDefaultLabel, newDefaultCaption);
                queryNum++;
            }

            origOnClose.ifPresent(handler -> handler.handle(event));
        });

        // Create a context menu for the new tab and add it
        final TabContextMenu tabContextMenu = new TabContextMenu(this, newTab);
        tabContextMenu.init();

        queryPane.addGraphDependentMenuItems(
                tabContextMenu.getRunMenuItem(),
                tabContextMenu.getRunFromHereMenuItem(),
                tabContextMenu.getRunToHereMenuItem()
        );
        queryPane.addPluginDependentMenuItems(
                tabContextMenu.getDeactivateAllPluginsMenuItem()
        );

        final ScrollPane queryPhaseScroll = new ScrollPane();
        queryPhaseScroll.setFitToWidth(true);
        queryPhaseScroll.setContent(queryPane);
        queryPhaseScroll.setStyle("-fx-background-color: black;");

        newTab.setContextMenu(tabContextMenu.getContextMenu());
        newTab.setContent(queryPhaseScroll);
        newTab.setTooltip(new Tooltip("Right click for more options"));
        newTab.setClosable(true);

        // Update the context menu enablement statuses.
        // Must be called after setting the scroll pane and not before.
        final boolean hasEnabledPlugins = tabHasEnabledPlugins(newTab);
        final boolean isExecuteButtonIsGo = DataAccessPaneState.isExecuteButtonIsGo();
        final boolean isExecuteButtonEnabled = !getDataAccessPane().getButtonToolbar()
                .getExecuteButtonTop().isDisabled();

        updateTabMenu(
                newTab,
                hasEnabledPlugins && isExecuteButtonIsGo && isExecuteButtonEnabled,
                hasEnabledPlugins
        );

        // Add the new tab to the pane
        getTabPane().getTabs().add(newTab);
    }

    protected void setTabCaption(final Label tabLabel, final Label tabDefaultLabel, final String newDefaultCaption) {
        if (StringUtils.isBlank(tabLabel.getText()) || newDefaultCaption.equals(tabLabel.getText().trim())) {
            tabDefaultLabel.setText(newDefaultCaption);
            tabLabel.setText("");
        } else {
            tabDefaultLabel.setText(newDefaultCaption + SeparatorConstants.BLANKSPACE + SeparatorConstants.HYPHEN);
        }
    }

    protected void labelClickEvent(final Tab tab, final Label label, final MouseEvent event) {
        if (event.getClickCount() == DOUBLE_CLICK_COUNT) {
            final TextField field = new TextField(label.getText());
            final Label defaultLabel = (Label) label.getGraphic();

            field.setOnAction(e -> {
                label.setText(field.getText());
                tab.setGraphic(label);
            });

            field.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue,
                    Boolean newValue) -> {
                if (!newValue) {
                    final String defaultCaption = String.format(TAB_TITLE, getTabPane().getTabs().indexOf(tab) + 1);
                    label.setText(field.getText().trim());
                    setTabCaption(label, defaultLabel, defaultCaption);
                    tab.setGraphic(label);
                }
            });

            field.setPromptText("Enter Step Description");
            tab.setGraphic(field);
            field.selectAll();
            field.requestFocus();
        }
    }

    /**
     * Run the given range of tabs inclusively. As each tab is run, the jobs from
     * the previous tab are passed so that it can block if necessary until they
     * are complete.
     * <p/>
     * Before returning a {@link WaitForQueriesToCompleteTask} will be started
     * that will deal with the clean up of the run once it is complete.
     *
     * @param firstTab the first tab to be run
     * @param lastTab the last tab to be run
     */
    public void runTabs(final int firstTab, final int lastTab) {
        // Change execute button to stop but do not disable
        getDataAccessPane().setExecuteButtonToStop(false);
        
        // Need to take a copy for when it changes while this thread is still running
        final String activeGraphId = GraphManager.getDefault().getActiveGraph().getId();
        
        DataAccessPaneState.setQueriesRunning(activeGraphId, true);

        // TODO This was being called every time runPlugins is called but can't see the point..could break!!!
        storeParameterValues();
        
        List<Future<?>> barrier = null;
        for (int i = firstTab; i <= lastTab; i++) {
            final Tab tab = getTabPane().getTabs().get(i);
            
            LOGGER.log(Level.INFO, String.format("Running tab: %s", tab.getText()));
            
            barrier = getQueryPhasePane(tab).runPlugins(barrier);
        }

        CompletableFuture.runAsync(() -> new WaitForQueriesToCompleteTask(getDataAccessPane(), activeGraphId),
                getDataAccessPane().getParentComponent().getExecutorService());
    }
    
    /**
     * Enable or disable the items in the contextual menu for every tab. There are
     * two types of menu items in the context menu. Menu items relating specifically
     * to plugins and menu items relating specifically to the graph.
     * <p/>
     * Plugin menu items will be enabled if the tab has enabled plugins.
     * <p/>
     * Graph menu items will be enabled if the tab has enabled plugins and the
     * execute button is fully enabled.
     */
    public void updateTabMenus() {
        final boolean isExecuteButtonIsGo = DataAccessPaneState.isExecuteButtonIsGo();
        final boolean isExecuteButtonEnabled = !getDataAccessPane().getButtonToolbar()
                .getExecuteButtonTop().isDisabled();
                    
        getTabPane().getTabs().forEach(tab -> {
            final boolean hasEnabledPlugins = tabHasEnabledPlugins(tab);
                    
            updateTabMenu(
                    tab,
                    hasEnabledPlugins && isExecuteButtonIsGo && isExecuteButtonEnabled,
                    hasEnabledPlugins
            );
        });
    }
    
    /**
     * Determines if the tab pane is executable by verifying the following for each tab
     * <ol>
     * <li>There are enabled plugins</li>
     * <li>All enabled plugins have valid configuration</li>
     * <li>The date/time range is valid</li>
     * </ol>
     *
     * @return true if all tabs are valid and executable, false otherwise
     */
    public boolean isTabPaneExecutable() {
        return getTabPane().getTabs().stream()
                .map(isExecutableTab)
                .filter(b -> !b) // Reduce to only tabs that are invalid
                .findAny()
                .isEmpty(); // For the pane to be valid then all tabs need to be valid
    }
    
    /**
     * Checks if any tab that contains enabled plugins has any invalid
     * configurations. If there are no enabled plugins or enabled plugins
     * with invalid configuration then false is returned.
     *
     * @param tabPane the tab pane to be validated
     * @return true if there are active plugins and they are valid, false otherwise
     */
    public boolean hasActiveAndValidPlugins() {
        final List<Tab> tabsWithEnabledPlugins = getTabPane().getTabs().stream()
                .filter(tab -> tabHasEnabledPlugins(tab))
                .collect(Collectors.toList());
                
        return !tabsWithEnabledPlugins.isEmpty()
                && tabsWithEnabledPlugins.stream()
                        .allMatch(tab -> validateTabEnabledPlugins(tab));
    }
    
    /**
     * Gets the {@link QueryPhasePane} of the currently visible tab in the tab pane.
     *
     * @return the {@link QueryPhasePane} of the current tab
     */
    public QueryPhasePane getQueryPhasePaneOfCurrentTab() {
        return getQueryPhasePane(getCurrentTab());
    }
    
    /**
     * Get the currently visible tab in the tab pane.
     *
     * @return the current tab
     */
    public Tab getCurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    /**
     * Removes all tabs from the tab pane.
     */
    public void removeTabs() {
        tabPane.getTabs().clear();
    }
    
    /**
     * Get the tab pane.
     *
     * @return the tab pane
     */
    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     * Get the data access pane that this tab pane is rendered on.
     *
     * @return the data access pane
     */
    public DataAccessPane getDataAccessPane() {
        return dataAccessPane;
    }

    /**
     * Get the currently available loaded and available data access plugins.
     *
     * @return the data access plugins
     * @see DataAccessPaneState#getPlugins() 
     */
    public Map<String, List<DataAccessPlugin>> getPlugins() {
        return plugins;
    }
    
    /**
     * Store current parameter values for all tabs and plug-ins in the
     * {@link RecentParameterValues} repository. It will store both global and
     * plugin parameters.
     */
    protected void storeParameterValues() {
        getTabPane().getTabs().parallelStream().forEach(tab -> {
            final QueryPhasePane pluginPane = getQueryPhasePane(tab);
            pluginPane.storeParameterValues();
        });
    }
    
    /**
     * Enable or disable the items in the contextual menu for a tab. There are
     * two types of menu items in the context menu. Menu items relating specifically
     * to plugins and menu items relating specifically to the graph.
     * <p/>
     * Plugin menu items will be enabled if the tab has enabled plugins.
     * <p/>
     * Graph menu items will be enabled if the tab has enabled plugins and the
     * execute button is fully enabled.
     *
     * @param tab the tab to update the menu item status on
     * @param enabled true if the menu items are to be enabled, false otherwise
     * @see #updateTabMenus() 
     */
    protected void updateTabMenu(final Tab tab,
                                 final boolean graphDependentMenuItemsEnabled,
                                 final boolean pluginDependentMenuItemsEnabled) {
        final QueryPhasePane queryPhasePane = getQueryPhasePane(tab);
        queryPhasePane.enableGraphDependentMenuItems(graphDependentMenuItemsEnabled);
        queryPhasePane.enablePluginDependentMenuItems(pluginDependentMenuItemsEnabled);
    }
    
    /**
     * Convenience method for accessing the {@link QueryPhasePane} of a tab.
     * This prevents all the casting being littered through the code.
     *
     * @param tab the tab to get the {@link QueryPhasePane} from
     * @return the found {@link QueryPhasePane}
     */
    public static QueryPhasePane getQueryPhasePane(final Tab tab) {
        return (QueryPhasePane) ((ScrollPane) tab.getContent()).getContent();
    }
    
    /**
     * Check if a tab has any plugins that are enabled.
     *
     * @param tab the tab to check
     * @return true if the tab has any enabled plugins, false otherwise
     */
    public static boolean tabHasEnabledPlugins(final Tab tab) {
        return getQueryPhasePane(tab).getDataAccessPanes().stream()
                .filter(DataSourceTitledPane::isQueryEnabled)
                .findAny()
                .isPresent();
    }

    /**
     * Validate a tab's enabled plugins to see if they contain any parameters
     * with values that are currently in error.
     *
     * @param tab the tab to validate
     * @return true if the tab's enabled plug-ins have valid parameters, false otherwise
     */
    public static boolean validateTabEnabledPlugins(final Tab tab) {
        return getQueryPhasePane(tab).getDataAccessPanes().stream()
                .filter(DataSourceTitledPane::isQueryEnabled)
                .map(DataSourceTitledPane::getParameters)
                .filter(Objects::nonNull)
                .map(PluginParameters::getParameters)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .allMatch(entry -> entry.getValue().getError() == null);
    }
    
    /**
     * Validates the current date/time range settings on the passed tab. A date/time
     * range is valid if the start date/time is before the end date/time.
     *
     * @param tab the tab to validate
     * @return true if the tab's date/time range is valid, false otherwise
     */
    public static boolean validateTabTimeRange(final Tab tab) {
        final DateTimeRange range = getQueryPhasePane(tab).getGlobalParametersPane().getParams()
                .getDateTimeRangeValue(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID);
        
        return !range.getZonedStartEnd()[0].isAfter(range.getZonedStartEnd()[1]);
    }
}
