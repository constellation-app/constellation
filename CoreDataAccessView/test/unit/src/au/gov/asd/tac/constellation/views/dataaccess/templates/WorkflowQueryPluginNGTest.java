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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author arcturus
 */
public class WorkflowQueryPluginNGTest {

    /**
     * Test of execute method, of class WorkflowQueryPlugin.
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExecute() throws InterruptedException, PluginException {
        System.out.println("execute");

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final DualGraph graph = new DualGraph(schema);

        // mock PluginGraphs
        final PluginGraphs pluginGraphs = mock(PluginGraphs.class);
        when(pluginGraphs.getGraph()).thenReturn(graph);

        final PluginInteraction interaction = new TextPluginInteraction();
        final WorkflowQueryPlugin instance = new WorkflowQueryPluginImpl();
        final PluginParameters parameters = instance.createParameters();
        instance.execute(pluginGraphs, interaction, parameters);
    }

    /**
     * Test of createParameters method, of class WorkflowQueryPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");

        final WorkflowQueryPlugin instance = new WorkflowQueryPluginImpl();
        final PluginParameters result = instance.createParameters();
        assertTrue(result.getParameters().containsKey(WorkflowQueryPlugin.BATCH_SIZE_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(WorkflowQueryPlugin.MAX_CONCURRENT_PLUGINS_PARAMETER_ID));
    }

    /**
     * Test of getDefaultBatchSize method, of class WorkflowQueryPlugin.
     */
    @Test
    public void testGetDefaultBatchSize() {
        System.out.println("getDefaultBatchSize");

        final WorkflowQueryPlugin instance = new WorkflowQueryPluginImpl();
        final int expResult = 100;
        final int result = instance.getDefaultBatchSize();
        assertEquals(result, expResult);
    }

    /**
     * Test of getDefaultConcurrentThreads method, of class WorkflowQueryPlugin.
     */
    @Test
    public void testGetDefaultConcurrentThreads() {
        System.out.println("getDefaultConcurrentThreads");

        final WorkflowQueryPlugin instance = new WorkflowQueryPluginImpl();
        final int expResult = 25;
        final int result = instance.getDefaultConcurrentThreads();
        assertEquals(result, expResult);
    }

    /**
     * Test of getWorkflow method, of class WorkflowQueryPlugin.
     */
    @Test
    public void testGetWorkflow() {
        System.out.println("getWorkflow");

        final WorkflowQueryPlugin instance = new WorkflowQueryPluginImpl();
        final List<String> expResult = Collections.emptyList();
        final List<String> result = instance.getWorkflow();
        assertEquals(result, expResult);
    }

    /**
     * Test of getErrorHandlingPlugin method, of class WorkflowQueryPlugin.
     */
    @Test
    public void testGetErrorHandlingPlugin() {
        System.out.println("getErrorHandlingPlugin");

        final WorkflowQueryPlugin instance = new WorkflowQueryPluginImpl();
        final String expResult = "SelectAll";
        final String result = instance.getErrorHandlingPlugin();
        assertEquals(result, expResult);
    }

    /**
     * Test of addPartialResults method, of class WorkflowQueryPlugin.
     */
    @Test
    public void testAddPartialResults() {
        System.out.println("addPartialResults");

        final WorkflowQueryPlugin instance = new WorkflowQueryPluginImpl();
        final boolean expResult = false;
        final boolean result = instance.addPartialResults();
        assertEquals(result, expResult);
    }

    private class WorkflowQueryPluginImpl extends WorkflowQueryPlugin {

        @Override
        public List<String> getWorkflow() {
            return Collections.emptyList();
        }

        @Override
        public String getErrorHandlingPlugin() {
            return "SelectAll";
        }
    }

}
