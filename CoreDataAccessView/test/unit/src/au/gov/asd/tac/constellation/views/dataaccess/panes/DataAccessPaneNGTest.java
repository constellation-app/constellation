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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.components.ButtonToolbar;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.components.OptionsMenuBar;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(DataAccessPaneNGTest.class.getName());

    private DataAccessViewTopComponent dataAccessViewTopComponent;

    private DataAccessPane dataAccessPane;

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        dataAccessViewTopComponent = mock(DataAccessViewTopComponent.class);

        dataAccessPane = spy(new DataAccessPane(dataAccessViewTopComponent));
    }

    @Test
    public void init() {
        assertSame(dataAccessPane.getParentComponent(), dataAccessViewTopComponent);

        assertNotNull(dataAccessPane.getOptionsMenuBar());
        assertNotNull(dataAccessPane.getButtonToolbar());

        assertNotNull(dataAccessPane.getSearchPluginTextField());
        assertEquals(dataAccessPane.getSearchPluginTextField().promptTextProperty().get(),
                "Type to search for a plugin");

        assertNotNull(dataAccessPane.getDataAccessTabPane());
        assertEquals(dataAccessPane.getDataAccessTabPane().getTabPane().getTabs().size(), 1);

        // TODO listener for date/range params
        // TODO update called
    }

    /**
     * I suspect this doesn't work because the action handler is initialized during the constructor and the spy is not
     * applied until after the construction is complete.
     */
    @Test(enabled = false)
    public void searchTextFieldListener() {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getQueryPhasePaneOfCurrentTab()).thenReturn(queryPhasePane);

        dataAccessPane.getSearchPluginTextField().setText("example");

        verify(queryPhasePane).showMatchingPlugins("example");
    }

    /**
     * I suspect this doesn't work because the action handler is initialized during the constructor and the spy is not
     * applied until after the construction is complete.
     */
    @Test(enabled = false)
    public void contextMenuEvent() {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final Tab currentTab = mock(Tab.class);
        final ContextMenu contextMenu = mock(ContextMenu.class);
        final ContextMenuEvent contextMenuEvent = mock(ContextMenuEvent.class);

        doNothing().when(contextMenu).show(any(Node.class), anyDouble(), anyDouble());

        when(contextMenuEvent.getScreenX()).thenReturn(22.0);
        when(contextMenuEvent.getScreenY()).thenReturn(11.0);

        doReturn(dataAccessTabPane).when(dataAccessPane).getDataAccessTabPane();

        when(dataAccessTabPane.getCurrentTab()).thenReturn(currentTab);
        when(currentTab.getContextMenu()).thenReturn(contextMenu);

        dataAccessPane.getOnContextMenuRequested().handle(contextMenuEvent);

        verify(contextMenu).show(dataAccessPane, 22.0, 11.0);
        verify(contextMenuEvent).consume();
    }

    @Test
    public void addUIComponents() {
        dataAccessPane.addUIComponents();

        assertEquals(dataAccessPane.getChildren().size(), 2);

        final VBox vbox = (VBox) dataAccessPane.getChildren().get(0);

        assertEquals(vbox.getChildren().size(), 4);

        assertSame(vbox.getChildren().get(0), dataAccessPane.getOptionsMenuBar().getMenuBar());
        assertSame(vbox.getChildren().get(1), dataAccessPane.getSearchPluginTextField());
        assertSame(vbox.getChildren().get(2), dataAccessPane.getDataAccessTabPane().getTabPane());
        assertSame(vbox.getChildren().get(3), dataAccessPane.getButtonToolbar().getRabRegionExectueHBoxBottom());

        assertEquals(AnchorPane.getTopAnchor(vbox), 0.0);
        assertEquals(AnchorPane.getBottomAnchor(vbox), 0.0);
        assertEquals(AnchorPane.getLeftAnchor(vbox), 0.0);
        assertEquals(AnchorPane.getRightAnchor(vbox), 0.0);

        final GridPane buttonToolbar = (GridPane) dataAccessPane.getChildren().get(1);

        assertSame(buttonToolbar, dataAccessPane.getButtonToolbar().getOptionsToolbar());

        assertEquals(AnchorPane.getTopAnchor(buttonToolbar), 5.0);
        assertEquals(AnchorPane.getRightAnchor(buttonToolbar), 5.0);

        // Verify Width Property Listener
        final ButtonToolbar mockButtonToolbar = mock(ButtonToolbar.class);
        final OptionsMenuBar mockOptionsMenuBar = mock(OptionsMenuBar.class);
        final MenuBar mockMenuBar = mock(MenuBar.class);
        when(dataAccessPane.getButtonToolbar()).thenReturn(mockButtonToolbar);
        when(dataAccessPane.getOptionsMenuBar()).thenReturn(mockOptionsMenuBar);
        when(mockOptionsMenuBar.getMenuBar()).thenReturn(mockMenuBar);

        // Shrink the Pane
        dataAccessPane.resize(400, 500);

        verify(mockButtonToolbar).handleShrinkingPane();
        verify(mockMenuBar).setMinHeight(60);

        // Grow the Pane
        dataAccessPane.resize(500, 500);

        verify(mockButtonToolbar).handleGrowingPane();
        verify(mockMenuBar).setMinHeight(36);
    }

    @Test
    public void update_active_graph_present() {
        doNothing().when(dataAccessPane).update(isNull(String.class));
        doNothing().when(dataAccessPane).update(isNull(Graph.class));

        try (final MockedStatic<GraphManager> graphManageMockedStatic = Mockito.mockStatic(GraphManager.class)) {
            final GraphManager graphManager = mock(GraphManager.class);
            graphManageMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);

            final Graph graph = mock(Graph.class);
            when(graphManager.getActiveGraph()).thenReturn(graph);
            when(graph.getId()).thenReturn("graphId");

            dataAccessPane.update();

            verify(dataAccessPane).update("graphId");
        }
    }

    @Test
    public void update_active_graph_not_present() {
        doNothing().when(dataAccessPane).update(isNull(String.class));

        try (final MockedStatic<GraphManager> graphManageMockedStatic = Mockito.mockStatic(GraphManager.class)) {
            final GraphManager graphManager = mock(GraphManager.class);
            graphManageMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);

            when(graphManager.getActiveGraph()).thenReturn(null);

            dataAccessPane.update();

            verify(dataAccessPane).update((String) null);
        }
    }

    @Test
    public void update_pass_null_graph() {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final Tab currentTab = mock(Tab.class);
        final QueryPhasePane currentQueryPhasePane = mock(QueryPhasePane.class);

        final DataSourceTitledPane dataSourceTitledPane = mock(DataSourceTitledPane.class);
        final Plugin plugin = mock(Plugin.class);
        final PluginParameters pluginParameters = mock(PluginParameters.class);

        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);

        when(dataAccessTabPane.getCurrentTab()).thenReturn(currentTab);
        when(dataAccessTabPane.getQueryPhasePaneOfCurrentTab()).thenReturn(currentQueryPhasePane);

        when(currentQueryPhasePane.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane));

        when(dataSourceTitledPane.getPlugin()).thenReturn(plugin);
        when(dataSourceTitledPane.getParameters()).thenReturn(pluginParameters);

        dataAccessPane.update((Graph) null);

        verify(plugin).updateParameters(null, pluginParameters);
        verify(dataAccessPane).update((String) null);
    }

    @Test
    public void update_pass_non_null_graph() {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);

        final Tab currentTab = mock(Tab.class);
        final QueryPhasePane currentQueryPhasePane = mock(QueryPhasePane.class);

        final DataSourceTitledPane dataSourceTitledPane = mock(DataSourceTitledPane.class);
        final Plugin plugin = mock(Plugin.class);
        final PluginParameters pluginParameters = mock(PluginParameters.class);

        final Graph graph = mock(Graph.class);

        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);

        when(dataAccessTabPane.getCurrentTab()).thenReturn(currentTab);
        when(dataAccessTabPane.getQueryPhasePaneOfCurrentTab()).thenReturn(currentQueryPhasePane);

        when(currentQueryPhasePane.getDataAccessPanes()).thenReturn(List.of(dataSourceTitledPane));

        when(dataSourceTitledPane.getPlugin()).thenReturn(plugin);
        when(dataSourceTitledPane.getParameters()).thenReturn(pluginParameters);

        when(graph.getId()).thenReturn("graphId");

        dataAccessPane.update(graph);

        verify(plugin).updateParameters(graph, pluginParameters);
        verify(dataAccessPane).update("graphId");
    }

    @Test
    public void update_with_graph_no_tab() {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final Graph graph = mock(Graph.class);

        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getCurrentTab()).thenReturn(null);

        when(graph.getId()).thenReturn("graphId");

        dataAccessPane.update(graph);

        verify(dataAccessPane, never()).update(anyString());
    }

    @Test
    public void update_pass_graph_id() {
        clearInvocations(dataAccessPane);
        verifyUpdateWithGraphId("graphId", true, true,
                disable -> verify(dataAccessPane).setExecuteButtonToStop(disable));

        clearInvocations(dataAccessPane);
        verifyUpdateWithGraphId(null, true, true,
                disable -> verify(dataAccessPane).setExecuteButtonToGo(disable));

        clearInvocations(dataAccessPane);
        verifyUpdateWithGraphId("graphId", true, false,
                disable -> verify(dataAccessPane).setExecuteButtonToGo(disable));
    }

    @Test
    public void hierarchicalUpdate() {
        doNothing().when(dataAccessPane).update();

        dataAccessPane.hierarchicalUpdate();

        verify(dataAccessPane).update();
    }

    @Test
    public void updateExecuteButtonEnablement() {
        verifyUpdateExecuteButtonEnablement(true, true, false);
        verifyUpdateExecuteButtonEnablement(true, false, false);
        verifyUpdateExecuteButtonEnablement(false, true, false);
        verifyUpdateExecuteButtonEnablement(false, false, true);
    }

    @Test
    public void setExecuteButtonToGo() {
        DataAccessPaneState.setCurrentGraphId("graphId");

        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);

        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);

        dataAccessPane.setExecuteButtonToGo(false);

        assertTrue(DataAccessPaneState.isExecuteButtonIsGo());
        verify(buttonToolbar).changeExecuteButtonState(ButtonToolbar.ExecuteButtonState.GO, false);
    }

    @Test
    public void setExecuteButtonToStop() {
        DataAccessPaneState.setCurrentGraphId("graphId");

        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);

        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);

        dataAccessPane.setExecuteButtonToStop(false);

        assertFalse(DataAccessPaneState.isExecuteButtonIsGo());
        verify(buttonToolbar).changeExecuteButtonState(ButtonToolbar.ExecuteButtonState.STOP, false);
    }

    @Test
    public void setExecuteButtonToContinue() {
        DataAccessPaneState.setCurrentGraphId("graphId");

        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);

        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);

        dataAccessPane.setExecuteButtonToContinue(false);

        assertFalse(DataAccessPaneState.isExecuteButtonIsGo());
        verify(buttonToolbar).changeExecuteButtonState(ButtonToolbar.ExecuteButtonState.CONTINUE, false);
    }

    @Test
    public void qualityControlRuleChanged_can_run() {
        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);

        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);

        dataAccessPane.qualityControlRuleChanged(true);

        verify(buttonToolbar).changeExecuteButtonState(ButtonToolbar.ExecuteButtonState.GO, false);
    }

    @Test
    public void qualityControlRuleChanged_cant_run() {
        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);

        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);

        dataAccessPane.qualityControlRuleChanged(false);

        verify(buttonToolbar).changeExecuteButtonState(ButtonToolbar.ExecuteButtonState.CALCULATING, true);
    }

    /**
     * Verifies the updateExecuteButtonEnablement method. If queryIsRunning and canExecuteTabPane are false then the
     * button is disabled other wise it is enabled.
     *
     * @param queryIsRunning true if there are queries running for the current graph
     * @param canExecuteTabPane the can execute parameter passed into the method
     * @param expected true if the execute button should be disabled, false otherwise
     */
    private void verifyUpdateExecuteButtonEnablement(final boolean queryIsRunning,
            final boolean canExecuteTabPane,
            final boolean expected) {
        DataAccessPaneState.setCurrentGraphId("graphId");
        DataAccessPaneState.setQueriesRunning(queryIsRunning);

        assertEquals(dataAccessPane.determineExecuteButtonDisableState(canExecuteTabPane), expected);
    }

    /**
     * Verifies the graph ID is set and the correct execute button setting is made.
     *
     * @param graphId the graph ID
     * @param tabPaneValid
     * @param queriesRunning true if the queries are running, false otherwise
     * @param verification a function with verifications to make after the code is run
     */
    private void verifyUpdateWithGraphId(final String graphId,
            final boolean disableExecuteButton,
            final boolean queriesRunning,
            final Consumer<Boolean> verification) {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);

        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessPane.determineExecuteButtonDisableState(anyBoolean()))
                .thenReturn(disableExecuteButton);
        when(dataAccessTabPane.isTabPaneExecutable()).thenReturn(true);

        DataAccessPaneState.setQueriesRunning(graphId, queriesRunning);
        dataAccessPane.update(graphId);

        verification.accept(disableExecuteButton);

        verify(dataAccessTabPane).updateTabMenus();
    }
}
