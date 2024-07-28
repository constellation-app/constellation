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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.collections.FXCollections.observableArrayList;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class QueryPhasePaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(QueryPhasePaneNGTest.class.getName());

    private MenuItem item1;
    private MenuItem item2;
    private MenuItem item3;

    private Map<String, Pair<Integer, List<DataAccessPlugin>>> plugins;
    private List<DataAccessPlugin> pluginList;

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }catch (ArrayStoreException ex){
            LOGGER.log(Level.WARNING, "CAUGHT ARRAY OUT OF BOUNDS", ex);
            System.out.println("CAUGHT ARRAY OUT OF BOUNDS: ");
            System.out.println(ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        item1 = new MenuItem("test1");
        item2 = new MenuItem("test2");
        item3 = new MenuItem("test2");

        pluginList = Arrays.asList(new TestDataAccessPlugin(), new AnotherTestDataAccessPlugin());
        plugins = new HashMap<>();
        plugins.put("test", new Pair(1, pluginList));
    }

    /**
     * Test of enableGraphDependentMenuItems method, of class QueryPhasePane.
     */
    @Test
    public void testEnableGraphDependentMenuItems() {
        System.out.println("enableGraphDependentMenuItems");

        final QueryPhasePane instance = new QueryPhasePane(new HashMap<>(), null, null);

        instance.addGraphDependentMenuItems(item1, item2);
        instance.enableGraphDependentMenuItems(true);
        for (final MenuItem item : instance.getGraphDependentMenuItems()) {
            assertFalse(item.isDisable());
        }

        instance.enableGraphDependentMenuItems(false);
        for (final MenuItem item : instance.getGraphDependentMenuItems()) {
            assertTrue(item.isDisable());
        }
    }

    /**
     * Test of addGraphDependentMenuItems method, of class QueryPhasePane.
     */
    @Test
    public void testAddGraphDependentMenuItems() {
        System.out.println("addGraphDependentMenuItems");

        final QueryPhasePane instance = new QueryPhasePane(new HashMap<>(), null, null);

        instance.addGraphDependentMenuItems(item1);
        assertEquals(instance.getGraphDependentMenuItems().size(), 1);

        instance.addGraphDependentMenuItems(item2, item3);
        assertEquals(instance.getGraphDependentMenuItems().size(), 3);

        instance.addGraphDependentMenuItems(item2);
        assertEquals(instance.getGraphDependentMenuItems().size(), 3);
    }

    /**
     * Test of enablePluginDependentMenuItems method, of class QueryPhasePane.
     */
    @Test
    public void testEnablePluginDependentMenuItems() {
        System.out.println("enablePluginDependentMenuItems");

        final QueryPhasePane instance = new QueryPhasePane(new HashMap<>(), null, null);

        instance.addPluginDependentMenuItems(item1, item2);
        instance.enablePluginDependentMenuItems(true);
        for (final MenuItem item : instance.getPluginDependentMenuItems()) {
            assertFalse(item.isDisable());
        }

        instance.enablePluginDependentMenuItems(false);
        for (final MenuItem item : instance.getPluginDependentMenuItems()) {
            assertTrue(item.isDisable());
        }
    }

    /**
     * Test of addPluginDependentMenuItems method, of class QueryPhasePane.
     */
    @Test
    public void testAddPluginDependentMenuItems() {
        System.out.println("addPluginDependentMenuItems");

        final QueryPhasePane instance = new QueryPhasePane(new HashMap<>(), null, null);

        instance.addPluginDependentMenuItems(item1);
        assertEquals(instance.getPluginDependentMenuItems().size(), 1);

        instance.addPluginDependentMenuItems(item2, item3);
        assertEquals(instance.getPluginDependentMenuItems().size(), 3);

        instance.addPluginDependentMenuItems(item2);
        assertEquals(instance.getPluginDependentMenuItems().size(), 3);
    }

    /**
     * Test of setHeadingsExpanded method, of class QueryPhasePane. Expand and Contract Headings Only
     */
    @Test
    public void testSetHeadingsExpandedHeadOnly() {
        System.out.println("setHeadingsExpandedHeadOnly");

        final QueryPhasePane instance = new QueryPhasePane(plugins, null, null);
        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;
            //default value for HeadingPane expansion is true
            assertTrue(heading.isExpanded());

            for (final DataSourceTitledPane dataSource : heading.getDataSources()) {
                //default value for DataSourceTitledPane expansion is false
                assertFalse(dataSource.isExpanded());
            }
        }

        instance.setHeadingsExpanded(false, false);

        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;
            assertFalse(heading.isExpanded());

            for (final DataSourceTitledPane dataSource : heading.getDataSources()) {
                assertFalse(dataSource.isExpanded());
            }
        }

        instance.setHeadingsExpanded(true, false);

        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;
            assertTrue(heading.isExpanded());

            for (final DataSourceTitledPane dataSource : heading.getDataSources()) {
                assertFalse(dataSource.isExpanded());
            }
        }
    }

    /**
     * Test of setHeadingsExpanded method, of class QueryPhasePane. Expand and contract headings and children
     */
    @Test
    public void testSetHeadingsExpandedHeadChild() {
        System.out.println("setHeadingsExpandedHeadChild");

        final QueryPhasePane instance = new QueryPhasePane(plugins, null, null);
        // Need to make a copy of list because expanding heading or something also calls getChildren
        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;
            //default value for HeadingPane expansion is true
            assertTrue(heading.isExpanded());

            for (final DataSourceTitledPane dataSource : heading.getDataSources()) {
                //default value for DataSourceTitledPane expansion is false
                assertFalse(dataSource.isExpanded());
            }
        }

        instance.setHeadingsExpanded(false, true);

        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;
            assertFalse(heading.isExpanded());

            for (final DataSourceTitledPane dataSource : heading.getDataSources()) {
                assertFalse(dataSource.isExpanded());
            }
        }

        instance.setHeadingsExpanded(true, true);

        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;
            assertTrue(heading.isExpanded());

            for (final DataSourceTitledPane dataSource : heading.getDataSources()) {
                assertTrue(dataSource.isExpanded());
            }
        }
    }

    /**
     * Test of expandPlugin method, of class QueryPhasePane.
     */
    @Test
    public void testExpandPlugin() {
        System.out.println("expandPlugin");

        final QueryPhasePane instance = new QueryPhasePane(plugins, null, null);
        for (final Node child : instance.getDataSourceList().getChildren()) {
            System.out.println("expandPlugin CHILD: ");
            System.out.println(child);
            final HeadingPane heading = (HeadingPane) child;
            //default value for HeadingPane expansion is true
            assertTrue(heading.isExpanded());

            for (final DataSourceTitledPane dataSource : heading.getDataSources()) {
                //default value for DataSourceTitledPane expansion is false
                assertFalse(dataSource.isExpanded());
            }
        }

        // these are added purely to get the function working
        final VBox parent = new VBox(instance);
        final VBox grandparent = new VBox(parent);
        new ExtendedScrollPane(grandparent);

        instance.expandPlugin("Test Plugin");

        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;

            for (final DataSourceTitledPane dataSource : heading.getDataSources()) {
                if ("Test Plugin".equals(dataSource.getPlugin().getName())) {
                    assertTrue(heading.isExpanded());
                    assertTrue(dataSource.isExpanded());
                } else {
                    assertFalse(dataSource.isExpanded());
                }
            }
        }

        instance.expandPlugin("Another Test Plugin");

        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;

            for (final DataSourceTitledPane dataSource : heading.getDataSources()) {
                if ("Another Test Plugin".equals(dataSource.getPlugin().getName())) {
                    assertTrue(heading.isExpanded());
                    assertTrue(dataSource.isExpanded());
                } else {
                    assertFalse(dataSource.isExpanded());
                }
            }
        }
    }

    /**
     * Test of showMatchingPlugins method, of class QueryPhasePane. Only one plugin match
     */
    @Test
    public void testShowMatchingPluginsOneMatch() {
        System.out.println("showMatchingPluginsOneMatch");

        plugins.put("test", new Pair(2, Arrays.asList(new TestDataAccessPlugin())));
        plugins.put("anothertest", new Pair(3, Arrays.asList(new AnotherTestDataAccessPlugin())));

        final QueryPhasePane instance = new QueryPhasePane(plugins, null, null);
        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;
            //default value for HeadingPane expansion is true so setting to false
            heading.setExpanded(false);
        }

        instance.showMatchingPlugins("Another Test Plugin");

        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;

            assertEquals(heading.isExpanded(), "anothertest".equals(heading.getText()));
        }
    }

    /**
     * Test of showMatchingPlugins method, of class QueryPhasePane. Multiple plugin matches
     */
    @Test
    public void testShowMatchingPluginsMultipleMatch() {
        System.out.println("showMatchingPluginsMultipleMatch");

        plugins.put("test", new Pair(4, Arrays.asList(new TestDataAccessPlugin())));
        plugins.put("anothertest", new Pair(5, Arrays.asList(new AnotherTestDataAccessPlugin())));

        final QueryPhasePane instance = new QueryPhasePane(plugins, null, null);
        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;
            //default value for HeadingPane expansion is true so setting to false
            heading.setExpanded(false);
        }

        instance.showMatchingPlugins("Test Plugin");

        for (final Node child : instance.getDataSourceList().getChildren()) {
            final HeadingPane heading = (HeadingPane) child;

            assertTrue(heading.isExpanded());
        }
    }

    @Test
    public void testRunPlugins() {
        // Initialize the current graph in the state.
        DataAccessPaneState.setCurrentGraphId("GraphId");

        final QueryPhasePane instance = spy(new QueryPhasePane(new HashMap<>(), null, null));

        // Setup the graph manager
        final GraphManager graphManager = mock(GraphManager.class);
        final Graph graph = mock(Graph.class);
        when(graphManager.getActiveGraph()).thenReturn(graph);

        // Setup the global parameters
        final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
        final PluginParameters globalPluginParameters = mock(PluginParameters.class);
        final PluginParameter globalPluginParameter1 = mock(PluginParameter.class);
        final PluginParameter globalPluginParameter2 = mock(PluginParameter.class);

        doReturn(globalParametersPane).when(instance).getGlobalParametersPane();
        when(globalParametersPane.getParams()).thenReturn(globalPluginParameters);
        when(globalPluginParameters.getParameters()).thenReturn(Map.of(
                "abc.parameter1", globalPluginParameter1,
                "global.parameter1", globalPluginParameter2
        ));
        when(globalPluginParameter1.getObjectValue()).thenReturn("GLOBAL PARAMETER 1");

        // Setup data access panes
        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);

        doReturn(List.of(dataSourceTitledPane1, dataSourceTitledPane2)).when(instance)
                .getDataAccessPanes();

        // Pane 1 is disabled so will not be run
        when(dataSourceTitledPane1.isQueryEnabled()).thenReturn(false);
        when(dataSourceTitledPane2.isQueryEnabled()).thenReturn(true);

        // Setup the plugin for pane 2
        final Plugin plugin = mock(Plugin.class);
        when(plugin.getName()).thenReturn("Plugin Name");

        when(dataSourceTitledPane2.getPlugin()).thenReturn(plugin);

        // Pane 2 has two parameters. One of them matches in name to one of
        // the global parameters which means its value will be overriden with
        // the global value.
        final PluginParameters pluginParameters = mock(PluginParameters.class);
        final PluginParameter pluginParameter1 = mock(PluginParameter.class);
        final PluginParameter pluginParameter2 = mock(PluginParameter.class);
        when(pluginParameters.getParameters()).thenReturn(Map.of(
                "abc.parameter1", pluginParameter1,
                "abc.parameter2", pluginParameter2
        ));
        when(pluginParameters.copy()).thenReturn(pluginParameters);
        when(dataSourceTitledPane2.getParameters()).thenReturn(pluginParameters);

        try (
                final MockedStatic<PluginRegistry> pluginRegistry
                = Mockito.mockStatic(PluginRegistry.class); final MockedStatic<PluginExecution> pluginExecutionMockedStatic
                = Mockito.mockStatic(PluginExecution.class); final MockedStatic<GraphManager> graphManagerMockedStatic
                = Mockito.mockStatic(GraphManager.class); final MockedConstruction<PluginSynchronizer> pluginSynchMocks
                = Mockito.mockConstruction(PluginSynchronizer.class, (pluginSyncMock, cnxt) -> {
                    assertEquals(cnxt.arguments(), List.of(1));
                });) {
            // Not sure why this is being done but just retuning the same plugin
            // to save creating another mock.
            pluginRegistry.when(() -> PluginRegistry.get(plugin.getClass().getName()))
                    .thenReturn(plugin);

            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);

            // This is the future that will be returned when the plugin begins execution
            final Future future = CompletableFuture.completedFuture("Plugin Complete!");

            // This is the future of a plugin that was run previously
            final Future existingFuture = CompletableFuture.completedFuture("Previous Plugin Complete!");

            final PluginExecution pluginExecution = mock(PluginExecution.class);
            pluginExecutionMockedStatic.when(() -> PluginExecution.withPlugin(plugin))
                    .thenReturn(pluginExecution);

            // We don't need to verify any of these again (with the exception of the plugin synchronizer)
            // because a null pointer will happen if any of the params don't match up.
            when(pluginExecution.withParameters(pluginParameters)).thenReturn(pluginExecution);
            when(pluginExecution.waitingFor(List.of(existingFuture))).thenReturn(pluginExecution);
            when(pluginExecution.synchronizingOn(any(PluginSynchronizer.class))).thenReturn(pluginExecution);
            when(pluginExecution.executeLater(graph)).thenReturn(future);

            // Verify that the return contains the plugin future as defined above
            assertEquals(instance.runPlugins(List.of(existingFuture)), List.of(future));

            // Verify the state's running plugin list has been updated
            assertEquals(DataAccessPaneState.getRunningPlugins(), Map.of(future, "Plugin Name"));

            // Verify the local plugin parameter was updated with the global parameter value
            verify(pluginParameter1).setObjectValue("GLOBAL PARAMETER 1");

            // Verify the created plugin synchronizer is passed to the plugin execution
            verify(pluginExecution).synchronizingOn(pluginSynchMocks.constructed().get(0));
        }
    }

    private class TestDataAccessPlugin extends SimplePlugin implements DataAccessPlugin {

        @Override
        protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            //Do nothing
        }

        @Override
        public String getName() {
            return "Test Plugin";
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public int getPosition() {
            return 0;
        }

    }

    private class AnotherTestDataAccessPlugin extends SimplePlugin implements DataAccessPlugin {

        @Override
        protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            //Do nothing
        }

        @Override
        public String getName() {
            return "Another Test Plugin";
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public int getPosition() {
            return 0;
        }

    }

    /**
     * This class is added purely so that a ScrollPane can be a parent (doesn't seem to be a public-facing way to do so
     * otherwise)
     */
    private class ExtendedScrollPane extends ScrollPane {

        public ExtendedScrollPane(final Node node) {
            super(node);
            getChildren().add(node);
        }
    }
}
