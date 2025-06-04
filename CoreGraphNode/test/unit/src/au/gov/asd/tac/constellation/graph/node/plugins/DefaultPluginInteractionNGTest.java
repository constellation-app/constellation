/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.node.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginInteraction.Timer;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.windows.TopComponent;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
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
public class DefaultPluginInteractionNGTest {

    private PluginManager manager;
    private PluginReport report;

    private DefaultPluginEnvironment environment;
    private Plugin plugin;
    private Graph graph;
    private PluginSynchronizer synchroniser;

    private GraphReport graphReport;
    
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
        environment = new DefaultPluginEnvironment();
        plugin = new TestPlugin();
        graph = new DualGraph(null);
        synchroniser = new PluginSynchronizer(1);
        graphReport = new GraphReport(graph.getId());

        manager = new PluginManager(environment, plugin, graph, false, synchroniser);
        report = new PluginReport(graphReport, plugin);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of createProgressTitle method, of class DefaultPluginInteraction.
     */
    @Test
    public void testCreateProgressTitle() {
        System.out.println("createProgressTitle");

        final DefaultPluginInteraction interaction = new DefaultPluginInteraction(manager, report);

        final String progressTitleNoGraphNode = interaction.createProgressTitle();
        assertEquals(progressTitleNoGraphNode, "Test");

        // creates a GraphNode for the given graph which will store a value in a static field we need to access
        new GraphNode(graph, null, new TestTopComponent(), null);

        final String progressTitleWithGraphNode = interaction.createProgressTitle();
        assertEquals(progressTitleWithGraphNode, "test: Test");
    }
    
    @Test
    public void testGetCurrentMessage() {
        final PluginInteraction interaction = Mockito.mock(DefaultPluginInteraction.class);
        final String currentMessage = interaction.getCurrentMessage();
        verify(interaction, times(1)).getCurrentMessage();
        assertEquals(currentMessage, null);
    }

    /**
     * Test of setProgress method, of class DefaultPluginInteraction.
     *
     * @throws InterruptedException
     */
    @Test
    public void testSetProgress() throws InterruptedException {
        System.out.println("setProgress");

        final String message = "this is a test";
        DefaultPluginInteraction interaction = new DefaultPluginInteraction(manager, report);

        assertEquals(interaction.getTimer(), null);
        assertEquals(interaction.getProgress(), null);

        interaction.setProgress(1, 2, message, false);
        
        assertEquals(interaction.getTimer().isAlive(), true);
        assertNotEquals(interaction.getProgress(), null);

        assertEquals(interaction.getPluginReport().getCurrentStep(), 1);
        assertEquals(interaction.getPluginReport().getTotalSteps(), 2);
        assertEquals(interaction.getPluginReport().getLastMessage().contains(message), true);
        
        interaction = new DefaultPluginInteraction(manager, report);
        interaction.setProgress(1, 1, message, false);

        assertEquals(interaction.getTimer().isAlive(), true);
        assertNotEquals(interaction.getProgress(), null);

        interaction = new DefaultPluginInteraction(manager, report);
        interaction.setProgress(1, 0, message, false);

        assertEquals(interaction.getProgress(), null);
        
        // add test for params
        final PluginParameters params = spy(PluginParameters.class);
        final PluginParameter testParam = Mockito.mock(PluginParameter.class);
        when(testParam.getName()).thenReturn("key_name");
        when(testParam.getStringValue()).thenReturn("string_value");
        params.addParameter(testParam);

        interaction = new DefaultPluginInteraction(manager, report);
        interaction.setProgress(1, 1, message, false, params);
        assertEquals(interaction.getTimer().isAlive(), true);
        assertNotEquals(interaction.getProgress(), null);
        assertTrue(interaction.getPluginReport().getReportLog().contains("key_name"));
        assertTrue(interaction.getPluginReport().getReportLog().contains("string_value"));
        
        // add test for selected items parameter
        interaction = new DefaultPluginInteraction(manager, report);
        interaction.setProgress(1, 1, message, false, params, 1001);
        assertEquals(interaction.getTimer().isAlive(), true);
        assertNotEquals(interaction.getProgress(), null);
        assertTrue(interaction.getPluginReport().getReportLog().contains("key_name"));
        assertTrue(interaction.getPluginReport().getReportLog().contains("string_value"));
        System.out.println(interaction.getPluginReport().getLastMessage());
        assertTrue(interaction.getPluginReport().getReportLog().contains("1001"));
        
    }

    /**
     * Test of setProgress method, of class DefaultPluginInteraction with total
     * steps being 0.
     *
     * @throws InterruptedException
     */
    @Test
    public void testSetProgressTotalStepsZero() throws InterruptedException {
        System.out.println("setProgressTotalStepsZero");

        final String message = "this is a test";

        final DefaultPluginInteraction interaction = new DefaultPluginInteraction(manager, report);

        assertEquals(interaction.getTimer(), null);
        assertEquals(interaction.getProgress(), null);

        interaction.setProgress(0, 0, message, false);

        assertEquals(interaction.getTimer().isAlive(), true);
        assertNotEquals(interaction.getProgress(), null);

        interaction.setProgress(0, 0, message, false);

        assertEquals(interaction.getTimer().isAlive(), true);
        assertNotEquals(interaction.getProgress(), null);

        interaction.setProgress(1, 0, message, false);

        assertEquals(interaction.getProgress(), null);
    }

    /**
     * Test of cancel method, of class DefaultPluginInteraction.
     */
    @Test
    public void testCancel() {
        System.out.println("cancel");

        final DefaultPluginInteraction interaction = new DefaultPluginInteraction(manager, null);

        assertEquals(manager.getPluginThread().isInterrupted(), false);
        interaction.cancel();
        assertEquals(manager.getPluginThread().isInterrupted(), true);
    }

    @Test
    public void testGetTime() {
        System.out.println("getTime");

        final DefaultPluginInteraction interaction = new DefaultPluginInteraction(null, null);
        final Timer timer = interaction.new Timer();

        final String time = timer.getTime(1000, 36611000);
        assertEquals(time, "10:10:10");

        final String timeLessThanTen = timer.getTime(1000, 32950000);
        assertEquals(timeLessThanTen, "09:09:09");

        final String timeStartGreaterThanEnd = timer.getTime(10, 0);
        assertEquals(timeStartGreaterThanEnd, "00:00:00");

        final String timeNegativeStart = timer.getTime(-10, 10);
        assertEquals(timeNegativeStart, "00:00:00");

        final String timeNegativeEnd = timer.getTime(10, -10);
        assertEquals(timeNegativeEnd, "00:00:00");
    }

    @Test
    public void testIsInteractive() {
        final DefaultPluginInteraction interaction =
                new DefaultPluginInteraction(manager, null);
        assertEquals(interaction.isInteractive(), false);
        
    }
    
    private class TestPlugin extends SimplePlugin {

        public TestPlugin() {
            //Do nothing
        }

        @Override
        public String getName() {
            return "Test";
        }

        @Override
        protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) {
            //Do nothing
        }
    }

    private class TestTopComponent extends TopComponent {

        public TestTopComponent() {
            //Do nothing
        }

        @Override
        public String getName() {
            return "test";
        }
    }
}
