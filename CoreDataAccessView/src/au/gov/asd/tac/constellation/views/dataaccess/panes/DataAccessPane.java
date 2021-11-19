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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPaneListener;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.components.ButtonToolbar;
import au.gov.asd.tac.constellation.views.dataaccess.components.ButtonToolbar.ExecuteButtonState;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.components.OptionsMenuBar;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlAutoVetterListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * JavaFX Data Access View. The main pane containing the tab pane, button tool
 * bars and menus.
 *
 * @author cygnus_x-1
 * @author ruby_crucis
 * @author arcturus
 * @author antares
 */
public class DataAccessPane extends AnchorPane implements PluginParametersPaneListener, QualityControlAutoVetterListener {
  
    private final DataAccessViewTopComponent parentComponent;
    private final DataAccessTabPane dataAccessTabPane;
    private final OptionsMenuBar optionsMenuBar;
    private final ButtonToolbar buttonToolbar;

    private final TextField searchPluginTextField;
    
    /**
     * Creates a new data access pane.
     *
     * @param parentComponent the top level Data Access View component
     */
    public DataAccessPane(final DataAccessViewTopComponent parentComponent) {
        this.parentComponent = parentComponent;
        
        // Pointless call but will initialize the data acces pane state which
        // will cause the plugin load to start and then continue initializing
        // the UI components here while it does that
        DataAccessPaneState.getCurrentGraphId();
        
        this.optionsMenuBar = new OptionsMenuBar(this);
        this.optionsMenuBar.init();

        this.buttonToolbar = new ButtonToolbar(this);
        this.buttonToolbar.init();
        
        searchPluginTextField = new TextField();
        searchPluginTextField.setPromptText("Type to search for a plugin");
        searchPluginTextField.textProperty().addListener((observable, oldValue, newValue) -> 
            getDataAccessTabPane().getQueryPhasePaneOfCurrentTab()
                    .showMatchingPlugins(newValue)
        );

        // Plugins are now needed, so wait until the load is complete
        final Map<String, List<DataAccessPlugin>> plugins;
        try {
            plugins = DataAccessPaneState.getPlugins();
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Failed to load data access plugins. "
                    + "Data Access View cannot be created.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            
            throw new IllegalStateException("Failed to load data access plugins. "
                    + "Data Access View cannot be created.");
        }
        
        this.dataAccessTabPane = new DataAccessTabPane(this, plugins);
        this.dataAccessTabPane.newTab();

        // Update the execute button when the user changes the time range
        this.dataAccessTabPane.getQueryPhasePaneOfCurrentTab()
                .getGlobalParametersPane().getParams().getParameters()
                .get(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID)
                .addListener((oldValue, newValue) -> update());

        // Right click anywhere and get the tab's context menu
        setOnContextMenuRequested(contextMenuEvent -> {
            getDataAccessTabPane().getCurrentTab().getContextMenu()
                    .show(
                            DataAccessPane.this,
                            contextMenuEvent.getScreenX(),
                            contextMenuEvent.getScreenY()
                    );
            contextMenuEvent.consume();
        });

        // Refresh all the status of menu items, execute buttons etc.
        // based on the current state of the data access view
        update();
    }

    /**
     * Adds the UI components constructed during initialization to the pane.
     */
    public void addUIComponents() {
        final VBox vbox = new VBox(
                getOptionsMenuBar().getMenuBar(),
                getSearchPluginTextField(),
                getDataAccessTabPane().getTabPane(),
                getButtonToolbar().getRabRegionExectueHBoxBottom()
        );
        VBox.setVgrow(getDataAccessTabPane().getTabPane(), Priority.ALWAYS);
        
        AnchorPane.setTopAnchor(vbox, 0.0);
        AnchorPane.setBottomAnchor(vbox, 0.0);
        AnchorPane.setLeftAnchor(vbox, 0.0);
        AnchorPane.setRightAnchor(vbox, 0.0);
        
        getChildren().add(vbox);
        
        AnchorPane.setTopAnchor(getButtonToolbar().getOptionsToolbar(), 5.0);
        AnchorPane.setRightAnchor(getButtonToolbar().getOptionsToolbar(), 5.0);
        
        getChildren().add(getButtonToolbar().getOptionsToolbar());
        
        // Modifies the menu sizes and positions as the overall pane size is
        // either shrunk or grown
        widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() <= 460) {
                getButtonToolbar().handleShrinkingPane();
                
                getOptionsMenuBar().getMenuBar().setMinHeight(60);
            } else {
                getButtonToolbar().handleGrowingPane();
                
                getOptionsMenuBar().getMenuBar().setMinHeight(36);
            }
        });
    }
    
    /**
     * Update executeButton, tab contextual menus, etc base on the current graph.
     * This will allow the user to run plugins if 
     * <ul>
     * <li>There is a graph open</li>
     * <li>Plugins are enabled/selected and have valid properties</li>
     * <li>There are no plugins already running on the active graph</li>
     * </ul>
     */
    public final void update() {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            update((String) null);
        } else {
            update(graph.getId());
        }
    }

    /**
     * Update executeButton, tab contextual menus, etc based on the passed graph.
     * This will allow the user to run plugins on that graph provided the requirements
     * specified on {@link #update()} are met.
     *
     * @param graph the Data Access view will be updated to reflect the state of
     *     this graph.
     */
    public final void update(final Graph graph) {
        if (getDataAccessTabPane().getCurrentTab() != null) {
            getDataAccessTabPane().getQueryPhasePaneOfCurrentTab().getDataAccessPanes()
                    .forEach(pane -> pane.getPlugin().updateParameters(graph, pane.getParameters()));

            if (graph == null) {
                update((String) null);
            } else {
                update(graph.getId());
            }
        }
    }
    
    /**
     * Update executeButton, tab contextual menus, etc based on the passed graph ID.
     * This will allow the user to run plugins on that graph provided the requirements
     * specified on {@link #update()} are met.
     *
     * @param newGraphId the Data Access view will be updated to reflect the state of
     *     this graph.
     */
    public final void update(final String newGraphId) {
        DataAccessPaneState.setCurrentGraphId(newGraphId);
        
        // Validates the tab pane and determines if the execute button should
        // be enabled or disabled
        final boolean disable = determineExecuteButtonDisableState(
                getDataAccessTabPane().isTabPaneExecutable()
        );
        
        // Determine the text that should be applied to the execute button
        if (DataAccessPaneState.getCurrentGraphId() == null) {
            setExecuteButtonToGo(disable);
        } else {
            if (DataAccessPaneState.isQueriesRunning()) {
                setExecuteButtonToStop(disable);
            } else {
                setExecuteButtonToGo(disable);
            }
        }

        // Update the tab menus now that the state above has been updated
        getDataAccessTabPane().updateTabMenus();
    }
    
    /**
     * Get the top level component for the Data Access view.
     *
     * @return the Data Access view top level component
     */
    public DataAccessViewTopComponent getParentComponent() {
        return parentComponent;
    }

    /**
     * Get the options menu bar that is accessible on the Data Access view. This
     * contains the load and save menu items among others.
     *
     * @return the options menu bar
     */
    public OptionsMenuBar getOptionsMenuBar() {
        return optionsMenuBar;
    }

    /**
     * Get the button tool bar that is accessible on the Data Access view. This
     * contains the add, favourite and execute buttons among others.
     *
     * @return the button tool bar
     */
    public ButtonToolbar getButtonToolbar() {
        return buttonToolbar;
    }

    /**
     * Get the text field that users can enter a plugin search phrase into.
     *
     * @return the plugin search text field
     */
    public TextField getSearchPluginTextField() {
        return searchPluginTextField;
    }
    
    /**
     * Get the {@link DataAccessTabPane} representing the tabs on the Data
     * Access view.
     *
     * @return the data access view tab pane
     */
    public DataAccessTabPane getDataAccessTabPane() {
        return dataAccessTabPane;
    }
    
    /**
     * Set executeButton to function as "Go". Updates the current graph
     * state for the executeButtonIsGo property to true, then changes the text
     * and style of the execute button.
     * 
     * @param disable true if the execute button should be disabled, false otherwise
     */
    public void setExecuteButtonToGo(final boolean disable) {
        DataAccessPaneState.updateExecuteButtonIsGo(true);

        getButtonToolbar().changeExecuteButtonState(ExecuteButtonState.GO, disable);
    }

    /**
     * Set executeButton to function as "Stop". Updates the current graph
     * state for the executeButtonIsGo property to false, then changes the text
     * and style of the execute button.
     * 
     * @param disable true if the execute button should be disabled, false otherwise
     */
    public void setExecuteButtonToStop(final boolean disable) {
        DataAccessPaneState.updateExecuteButtonIsGo(false);
        
        getButtonToolbar().changeExecuteButtonState(ExecuteButtonState.STOP, disable);
    }

    /**
     * Set executeButton to function as "Continue".Updates the current graph
     * state for the executeButtonIsGo property to false, then changes the text
     * and style of the execute button.
     *
     * @param disable true if the execute button should be disabled, false otherwise
     */
    public void setExecuteButtonToContinue(final boolean disable) {
        DataAccessPaneState.updateExecuteButtonIsGo(false);
        
        getButtonToolbar().changeExecuteButtonState(ExecuteButtonState.CONTINUE, disable);
    }
    
    /**
     * Called when a field is enabling it's parent plugin, to enable
     * executeButton, etc., if there is an open graph.
     */
    @Override
    public void hierarchicalUpdate() {
        update();
    }

    @Override
    public void validityChanged(boolean enabled) {
        // Must be overriden to implement PluginParametersPaneListener
    }

    /**
     * Something has changed in the graph that may mean the data access view can
     * no longer be run. This will update the execute button state to "Go" if
     * it can be run, or "Calculating" if it cannot.
     *
     * @param canRun true if the data access view can be run, false otherwise
     */
    @Override
    public void qualityControlRuleChanged(final boolean canRun) {
        if (canRun) {
            getButtonToolbar().changeExecuteButtonState(ExecuteButtonState.GO, !canRun);
        } else {
            getButtonToolbar().changeExecuteButtonState(ExecuteButtonState.CALCULATING, !canRun);
        }
    }

    /**
     * Determines if the execute button should be enabled or disabled. It should
     * only be disabled if there are currently no queries running and the tab
     * pane can be executed.
     * <p/>
     * If plugins are running the executeButton (actually the stop button) must
     * remain enabled.
     *
     * @param canExecuteTabPane true if the tab pane has enabled and valid plugins
     *     to run on every tab, false otherwise
     * @return true if the execute button should be disabled, false otherwise
     */
    protected boolean determineExecuteButtonDisableState(final boolean canExecuteTabPane) {
        final boolean queryIsRunning = DataAccessPaneState.isQueriesRunning();

        // The button cannot be disabled if a query is running or if one tab
        // in the pane has no selected plugin, an invalid time range,
        // or the selected plugins contain invalid parameter values.
        return !queryIsRunning && !canExecuteTabPane;
    }
}
