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
package au.gov.asd.tac.constellation.plugins.reporting;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.util.Arrays;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
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
    private static final String PLUGIN_TEST_NAME = "Test";
    private static final String PLUGIN_TEST_DESCRIPTION = "DESCRIPTION";
    private static final String PLUGIN_TEST_MESSAGE = "TEST MESSAGE";
    
    public PluginReportNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        plugin = new TestPlugin();
        graph = new DualGraph(null);
        graphReport = new GraphReport(graph.getId());

        report = new PluginReport(graphReport, plugin);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
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
            String[] testTags = {PluginTags.ANALYTIC, PluginTags.GENERAL,
                PluginTags.SEARCH};
            return testTags;
        }
        
        @Override
        protected void execute(final PluginGraphs graphs,
                final PluginInteraction interaction,
                final PluginParameters parameters) {
            //Do nothing
        }
    }
}
