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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.GraphObjectUtilities;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.io.CloseGraphPlugin.FORCED_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.io.CloseGraphPlugin.GRAPH_PARAMETER_ID;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
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
public class CloseGraphPluginNGTest {
    
    private DualGraph graph;
    private VisualGraphTopComponent tc;
    private GraphNode graphNode;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new DualGraph(schema);
        
        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject("closeTestGraph", true);
        tc = spy(new VisualGraphTopComponent(gdo, graph));
        graphNode = new GraphNode(graph, gdo, tc, null);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graphNode.destroy();
    }

    /**
     * Test of createParameters method, of class CloseGraphPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final CloseGraphPlugin instance = new CloseGraphPlugin();
        
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 2);
        assertTrue(params.getParameters().containsKey(GRAPH_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(FORCED_PARAMETER_ID));
    }

    /**
     * Test of execute method, of class CloseGraphPlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExecute() throws InterruptedException, PluginException {
        System.out.println("execute");
        
        final CloseGraphPlugin instance = new CloseGraphPlugin();
        
        PluginExecution.withPlugin(instance)
                .withParameter(GRAPH_PARAMETER_ID, graph.getId())
                .executeNow(graph);
        
        // this part ensures that the close has been given a chance to run in the AWT before continuing with the rest of the test
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(latch::countDown);
        
        latch.await();
        
        // ideally we check whether the top component was actually closed but since its a challenge to have it open in the first place
        // this is the next best thing (since this is what we expect to be called in order to successfully close the graph)
        verify(tc).close();
        verify(tc, never()).forceClose();
    }
    
    /**
     * Test of execute method, of class CloseGraphPlugin. Forced close
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExecuteForced() throws InterruptedException, PluginException {
        System.out.println("execute");
        
        final CloseGraphPlugin instance = new CloseGraphPlugin();  
        
        PluginExecution.withPlugin(instance)
                .withParameter(GRAPH_PARAMETER_ID, graph.getId())
                .withParameter(FORCED_PARAMETER_ID, true)
                .executeNow(graph);
        
        // this part ensures that the close has been given a chance to run in the AWT before continuing with the rest of the test
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(latch::countDown);
        
        latch.await();
        
        // ideally we check whether the top component was actually closed but since it's a challenge to have it open in the first place
        // this is the next best thing (since this is what we expect to be called in order to successfully close the graph)
        verify(tc).close();
        verify(tc).forceClose();
    }
}
