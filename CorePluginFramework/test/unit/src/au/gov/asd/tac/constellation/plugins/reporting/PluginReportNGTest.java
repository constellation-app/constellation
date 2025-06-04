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
package au.gov.asd.tac.constellation.plugins.reporting;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class PluginReportNGTest {
    
    private PluginReport report;
    private GraphReport graphReport;

    private Plugin plugin;
    private Graph graph;
    private long startTimeMillis;
    private static final String PLUGIN_TEST_NAME = "Test";
    private static final String PLUGIN_TEST_DESCRIPTION = "DESCRIPTION";
    private static final String PLUGIN_TEST_MESSAGE = "TEST MESSAGE";
    
    @BeforeMethod
    public void setUpMethod() throws Exception {
        plugin = new TestPlugin();
        graph = new DualGraph(null);
        graphReport = new GraphReport(graph.getId());

        report = new PluginReport(graphReport, plugin);
        startTimeMillis = System.currentTimeMillis();
    }
    
    @Test
    public void testGetPluginName() {
        assertEquals(report.getPluginName(), PLUGIN_TEST_NAME);
    }

    @Test
    public void testGetPluginDescription() {
        assertEquals(report.getPluginDescription(), PLUGIN_TEST_DESCRIPTION);
    }
    
    @Test
    public void testGetGraphReport() {
        assertEquals(report.getGraphReport(), graphReport);
    }
    
    @Test
    public void testAddMessage() {
        report.addMessage(PLUGIN_TEST_MESSAGE);
        assertEquals(report.getLastMessage(), PLUGIN_TEST_MESSAGE);
        assertEquals(report.getAllMessages(), PLUGIN_TEST_MESSAGE);
    }
    
    @Test
    public void testCurrentStep() {
        report.setCurrentStep(101);
        assertEquals(report.getCurrentStep(), 101);
    }
    
    @Test
    public void testTotalSteps() {
        report.setTotalSteps(1002);
        assertEquals(report.getTotalSteps(), 1002);
    }
    
    @Test
    public void testTags() {
        assertEquals(Arrays.toString(report.getTags()).contains(PluginTags.ANALYTIC), true);
        assertEquals(Arrays.toString(report.getTags()).contains(PluginTags.GENERAL), true);
        assertEquals(Arrays.toString(report.getTags()).contains(PluginTags.SEARCH), true);
        
        assertEquals(report.hasLowLevelTag(), false);
        final List<String> filteredTags = Arrays.asList(PluginTags.ANALYTIC, 
                PluginTags.GENERAL,
                PluginTags.SEARCH);
        assertEquals(report.containsAllTags(filteredTags), true);
        
        final List<String> anyTags = Arrays.asList(PluginTags.LOW_LEVEL,
                PluginTags.EXPORT);
        assertEquals(report.containsAnyTag(anyTags), false);
    }
    
    @Test
    public void testExecutionStage() {
        report.setExecutionStage(PluginExecutionStageConstants.RUNNING);
        assertEquals(report.getExecutionStage(), PluginExecutionStageConstants.RUNNING);
    }
    
    @Test
    public void testUndone() {
        report.setUndone(true);
        assertEquals(report.isUndone(), true);
    }
    
    @Test
    public void testAddListener() {
        final PluginReportListener mockListener = Mockito.mock(PluginReportListener.class);
        report.addPluginReportListener(mockListener);
        report.firePluginReportChangedEvent();
        Mockito.verify(mockListener, Mockito.times(1)).pluginReportChanged(report);
    }
        
    @Test
    public void testRemoveListener() {
        final PluginReportListener mockListener = Mockito.mock(PluginReportListener.class);
        report.addPluginReportListener(mockListener);
        report.removePluginReportListener(mockListener);
        report.firePluginReportChangedEvent();        
        Mockito.verify(mockListener, Mockito.times(0)).pluginReportChanged(report);
    }
    
    @Test
    public void testTime() {
        // test start time with some tolerance
        assertEquals(report.getStartTime(), startTimeMillis, 3); 
        //test stop time
        final long currentTimeMillis = System.currentTimeMillis();
        report.stop();
        assertEquals(report.getCurrentStep(), 1);
        assertEquals(report.getTotalSteps(), 0);
        assertEquals(report.getStopTime(), currentTimeMillis);
    }
    
    @Test
    public void testError() {
        final InterruptedException intEx = Mockito.mock(InterruptedException.class);
        report.setError(intEx);
        assertEquals(report.getExecutionStage(), PluginExecutionStageConstants.STOPPED);
        assertEquals(report.getError(), intEx);
        final PluginException pluginEx = Mockito.mock(PluginException.class);
        report.setError(pluginEx);
        assertEquals(report.getExecutionStage(), PluginExecutionStageConstants.STOPPED);
        assertEquals(report.getError(), pluginEx);
        final IOException ioEx = Mockito.mock(IOException.class);
        report.setError(ioEx);
        assertEquals(report.getError(), ioEx);
        assertEquals(report.getExecutionStage(), PluginExecutionStageConstants.STOPPED);
    }
     
    @Test
    public void testChildReport() {
        assertEquals(report.getUChildReports().size(), 0);
        final SimplePlugin newPlugin = Mockito.mock(SimplePlugin.class);
        when(newPlugin.getName()).thenReturn("simple_plugin_name");
        final String[] testTags = {PluginTags.ANALYTIC};
        when(newPlugin.getTags()).thenReturn(testTags);
        final PluginReport addedChildReport = report.addChildReport(newPlugin);
        assertEquals(addedChildReport.getPluginName(), newPlugin.getName());
        assertEquals(report.getUChildReports().size(), 1);
        assertEquals(report.getUChildReports().getFirst(), addedChildReport);
    }
    
    private class TestPlugin extends SimplePlugin {

        public TestPlugin() {
            //Do nothing
        }

        @Override
        public String getName() {
            return PLUGIN_TEST_NAME;
        }
        
        @Override
        public String getDescription() {
            return PLUGIN_TEST_DESCRIPTION;
        }

        @Override
        public String[] getTags() {
            final String[] testTags = {PluginTags.ANALYTIC, PluginTags.GENERAL,
                PluginTags.SEARCH};
            return testTags;
        }
        
        @Override
        protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) {
            //Do nothing
        }
    }
}
