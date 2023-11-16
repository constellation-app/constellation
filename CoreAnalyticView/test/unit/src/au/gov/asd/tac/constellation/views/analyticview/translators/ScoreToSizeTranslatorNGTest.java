/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.SizeVisualisation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ScoreToSizeTranslator 
 * 
 * @author Delphinus8821
 */
public class ScoreToSizeTranslatorNGTest {

    private static final Logger LOGGER = Logger.getLogger(ScoreToSizeTranslatorNGTest.class.getName());
    private Graph graph;
    
    public ScoreToSizeTranslatorNGTest() {
    }

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
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getName method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        final String expResult = "Multi-Score -> Size Visualisation";
        final String result = instance.getName();
        assertEquals(result, expResult);
    }

    /**
     * Test of getResultType method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testGetResultType() {
        System.out.println("getResultType");
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        final Class expResult = ScoreResult.class;
        final Class result = instance.getResultType();
        assertEquals(result, expResult);
    }

    /**
     * Test of buildControl method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testBuildControl() {
        System.out.println("buildControl");
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        final SizeVisualisation expResult = new SizeVisualisation(instance);
        final SizeVisualisation result = instance.buildControl();
        assertEquals(result, expResult);
    }

    /**
     * Test of executePlugin method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testExecutePlugin() {
        System.out.println("executePlugin");
        try (final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class)) {
            final GraphManager graphManager = spy(GraphManager.class);
            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
            setupGraph();
            when(graphManager.getActiveGraph()).thenReturn(graph);
            
            final boolean reset = false;
            final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
            ScoreResult result = mock(ScoreResult.class);
            instance.setResult(result);
            instance.executePlugin(reset);
            final boolean vertexSizes = instance.getVertexSizes().isEmpty();
            final boolean transactionSizes = instance.getTransactionSizes().isEmpty();
            assertTrue(vertexSizes);
            assertTrue(transactionSizes);
            

        }

    }
    
    /**
     * Test of executePlugin method with a reset, of class ScoreToSizeTranslator.
     */
//    @Test
//    public void testExecutePluginWithReset() {
//        System.out.println("executePlugin");
//        try (final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class)) {
//            final GraphManager graphManager = spy(GraphManager.class);
//            graphManagerMockedStatic.when(GraphManager::getDefault).thenReturn(graphManager);
//            setupGraph();
//            when(graphManager.getActiveGraph()).thenReturn(graph);
//
//            final boolean reset = true;
//            final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
//            instance.executePlugin(reset);
//        }
//    }

    /**
     * Test of setVertexSizes method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testSetVertexSizes() {
        System.out.println("setVertexSizes");
        final Map<Integer, Float> sizes = new HashMap<>();
        sizes.put(25, 2.0F);
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        instance.setVertexSizes(sizes);
        final Map<Integer, Float> result = instance.getVertexSizes();
        assertEquals(result, sizes);
    }

    /**
     * Test of setTransactionSizes method, of class ScoreToSizeTranslator.
     */
    @Test
    public void testSetTransactionSizes() {
        System.out.println("setTransactionSizes");
        final Map<Integer, Float> sizes = new HashMap<>();
        sizes.put(25, 2.0F);
        final ScoreToSizeTranslator instance = new ScoreToSizeTranslator();
        instance.setTransactionSizes(sizes);
        final Map<Integer, Float> result = instance.getTransactionSizes();
        assertEquals(result, sizes);
    }
    
    public void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        try {
            WritableGraph wg = graph.getWritableGraph("", true);
            
            // ensure attributes
            final int vertexSizeAttribute = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(wg);
            final int transactionSizeAttribute = VisualConcept.TransactionAttribute.WIDTH.ensure(wg);
            final int vxId1 = wg.addVertex();
            final int vxId2 = wg.addVertex();
            final int txId1 = wg.addTransaction(vxId1, vxId2, true);
            final int txId2 = wg.addTransaction(vxId2, vxId2, true);
            
            wg.setFloatValue(vertexSizeAttribute, vxId1, 4F);
            wg.setFloatValue(vertexSizeAttribute, vxId2, 7F);
            wg.setFloatValue(transactionSizeAttribute, txId1, 2F);
            wg.setFloatValue(transactionSizeAttribute, txId2, 1F);
            
            wg.commit();

        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            Thread.currentThread().interrupt();
        }
    }

}
