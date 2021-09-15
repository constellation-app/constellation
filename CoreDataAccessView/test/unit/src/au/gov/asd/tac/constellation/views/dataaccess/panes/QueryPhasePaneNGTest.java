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

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class QueryPhasePaneNGTest {

    private MenuItem item1;
    private MenuItem item2;
    private MenuItem item3;

    private Map<String, List<DataAccessPlugin>> plugins;
    private List<DataAccessPlugin> pluginList;

    public QueryPhasePaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.showStage();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FxToolkit.hideStage();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        item1 = new MenuItem("test1");
        item2 = new MenuItem("test2");
        item3 = new MenuItem("test2");

        pluginList = Arrays.asList(new TestDataAccessPlugin(), new AnotherTestDataAccessPlugin());
        plugins = new HashMap<>();
        plugins.put("test", pluginList);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
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
     * Test of setHeadingsExpanded method, of class QueryPhasePane. Expand and
     * Contract Headings Only
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
     * Test of setHeadingsExpanded method, of class QueryPhasePane. Expand and
     * contract headings and children
     */
    @Test
    public void testSetHeadingsExpandedHeadChild() {
        System.out.println("setHeadingsExpandedHeadChild");

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
     * Test of showMatchingPlugins method, of class QueryPhasePane. Only one
     * plugin match
     */
    @Test
    public void testShowMatchingPluginsOneMatch() {
        System.out.println("showMatchingPluginsOneMatch");

        plugins.put("test", Arrays.asList(new TestDataAccessPlugin()));
        plugins.put("anothertest", Arrays.asList(new AnotherTestDataAccessPlugin()));

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
     * Test of showMatchingPlugins method, of class QueryPhasePane. Multiple
     * plugin matches
     */
    @Test
    public void testShowMatchingPluginsMultipleMatch() {
        System.out.println("showMatchingPluginsMultipleMatch");

        plugins.put("test", Arrays.asList(new TestDataAccessPlugin()));
        plugins.put("anothertest", Arrays.asList(new AnotherTestDataAccessPlugin()));

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
     * This class is added purely so that a ScrollPane can be a parent (doesn't
     * seem to be a public-facing way to do so otherwise)
     */
    private class ExtendedScrollPane extends ScrollPane {

        public ExtendedScrollPane(final Node node) {
            super(node);
            getChildren().add(node);
        }
    }
}
