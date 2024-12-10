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
package au.gov.asd.tac.constellation.graph.interaction.animation;

import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class AnimationNGTest {

    private String graphId;
    private ColorWarpAnimation colorWarpAnimation;
    private DirectionIndicatorAnimation directionIndicatorAnimation;
    private FlyingAnimation flyingAnimation;
    private PanAnimation panAnimation;
    private ThrobbingNodeAnimation throbbingNodeAnimation;
    private DualGraph graph;
    private WritableGraph wg;
    private MockedStatic<ConstellationColor> constyColor;
    private MockedStatic<AnimationUtilities> animationUtilitiesMocked;
    private ConstellationColor mockedColor;
    
    
    public AnimationNGTest() {
    }


    @BeforeMethod
    public void setUpMethod() throws Exception {
        colorWarpAnimation = spy(ColorWarpAnimation.class);
        directionIndicatorAnimation = spy(DirectionIndicatorAnimation.class);
        flyingAnimation = spy(FlyingAnimation.class);
        throbbingNodeAnimation = spy(ThrobbingNodeAnimation.class);
        panAnimation = new PanAnimation("Test Name", new Camera(),
                new Camera(), false);

        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(
                VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        wg =  graph.getWritableGraph("", true);
        
        constyColor = mockStatic(ConstellationColor.class);
        mockedColor = mock(ConstellationColor.class);
        constyColor.when(()
            -> ConstellationColor.fromFXColor(Mockito.any()))
            .thenReturn(mockedColor);
        
        animationUtilitiesMocked = mockStatic(AnimationUtilities.class);
        animationUtilitiesMocked.when(() 
                -> AnimationUtilities.startAnimation(Mockito.any(), Mockito.any()))
                .then((Answer<Void>) invocation -> null);
        animationUtilitiesMocked.when(() 
                -> AnimationUtilities.stopAnimation(Mockito.any(), Mockito.any()))
                .then((Answer<Void>) invocation -> null);
        
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        wg.rollBack();
        wg = null;
        constyColor.close();
        animationUtilitiesMocked.close();
    }

    @Test
    public void testAnimation_getIntervalInMilis() {
        assertEquals(colorWarpAnimation.getIntervalInMillis(), 40);
        assertEquals(directionIndicatorAnimation.getIntervalInMillis(), 60);
        assertEquals(panAnimation.getIntervalInMillis(), 40);
        assertEquals(flyingAnimation.getIntervalInMillis(), 35);
        assertEquals(throbbingNodeAnimation.getIntervalInMillis(), 35);
    }

    @Test
    public void testAnimation_getName() {
        assertEquals(colorWarpAnimation.getName(), ColorWarpAnimation.NAME);
        assertEquals(directionIndicatorAnimation.getName(), DirectionIndicatorAnimation.NAME);        
        assertEquals(flyingAnimation.getName(), FlyingAnimation.NAME);
        assertEquals(throbbingNodeAnimation.getName(), ThrobbingNodeAnimation.NAME);
        assertEquals(panAnimation.getName(), "Test Name");
    }
      
    
    @Test
    public void testAnimation_initialiseWithoutElements() {
        colorWarpAnimation.initialise(wg);
        verify(colorWarpAnimation, times(1)).initialise(wg);
        verify(colorWarpAnimation, times(1)).stop();
        directionIndicatorAnimation.initialise(wg);
        verify(directionIndicatorAnimation, times(1)).initialise(wg);
        verify(directionIndicatorAnimation, times(1)).stop();
        flyingAnimation.initialise(wg);
        verify(flyingAnimation, times(1)).initialise(wg);
        verify(flyingAnimation, times(1)).stop();
        throbbingNodeAnimation.initialise(wg);
        verify(throbbingNodeAnimation, times(1)).initialise(wg);
        verify(throbbingNodeAnimation, times(1)).stop();
        panAnimation = mock(PanAnimation.class);
        panAnimation.initialise(wg);
        verify(panAnimation, times(1)).initialise(wg);
    }  
    
@Test
    public void testColorWarpAnimation_reset() {
        int vxId0 = wg.addVertex();
        int vxId1 = wg.addVertex();
        int txId0 = wg.addTransaction(vxId0, vxId1, false);
                
        int attributeId = VisualConcept.VertexAttribute.COLOR.ensure(wg);
        int attributeTxId = VisualConcept.TransactionAttribute.COLOR.ensure(wg);
        Object result0 = wg.getObjectValue(attributeId, vxId0);
        Object result1 = wg.getObjectValue(attributeId, vxId1);
        Object result2 = wg.getObjectValue(attributeTxId, txId0);

        // originally they will be set to null
        assertTrue(result0 == null);
        assertTrue(result1 == null);
        assertTrue(result2 == null);

        // set to initial colors
        wg.setObjectValue(attributeId, vxId0, ConstellationColor.AMETHYST);
        result0 = wg.getObjectValue(attributeId, vxId0);
        assertTrue(result0 == ConstellationColor.AMETHYST);
        
        wg.setObjectValue(attributeId, vxId1, ConstellationColor.BANANA);
        result1 = wg.getObjectValue(attributeId, vxId1);
        assertTrue(result1 == ConstellationColor.BANANA);
        
        wg.setObjectValue(attributeTxId, txId0, ConstellationColor.CHERRY);                
        result2 = wg.getObjectValue(attributeTxId, txId0);
        assertTrue(result2 == ConstellationColor.CHERRY);
        
        assertTrue(wg.getVertexCount() == 2);
        assertTrue(wg.getTransactionCount() == 1);

        colorWarpAnimation.initialise(wg);
        assertTrue(wg.getVertexCount() == 2);
        assertTrue(wg.getTransactionCount() == 1);
        // verify it's not gone into stop()
        verify(colorWarpAnimation, times(0)).stop();

        // Set to different colors
        wg.setObjectValue(attributeId, vxId0, ConstellationColor.AZURE);
        result0 = wg.getObjectValue(attributeId, vxId0);
        assertTrue(result0 == ConstellationColor.AZURE);
        
        wg.setObjectValue(attributeId, vxId1, ConstellationColor.BLUEBERRY);
        result1 = wg.getObjectValue(attributeId, vxId1);
        assertTrue(result1 == ConstellationColor.BLUEBERRY);
        
        wg.setObjectValue(attributeTxId, txId0, ConstellationColor.CARROT);                
        result2 = wg.getObjectValue(attributeTxId, txId0);
        assertTrue(result2 == ConstellationColor.CARROT);
        
        colorWarpAnimation.reset(wg);
        
        // verify that it was returned to original colors
        result0 = wg.getObjectValue(attributeId, vxId0);
        assertTrue(result0 == ConstellationColor.AMETHYST);
        
        result1 = wg.getObjectValue(attributeId, vxId1);
        assertTrue(result1 == ConstellationColor.BANANA);
        
        result2 = wg.getObjectValue(attributeTxId, txId0);
        assertTrue(result2 == ConstellationColor.CHERRY);     
    }
    
    @Test
    public void testFlyingAnimation_reset() {
        // Giving your static mock some behaviour if you want
        VisualConcept.GraphAttribute.CAMERA.ensure(wg);
        flyingAnimation.graphID = graphId;
        flyingAnimation.reset(wg);
        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.startAnimation(Mockito.any(),
                        Mockito.any()), times(1));
    }
 
    @Test
    public void DirectionIndicatorAnimation_reset() {
        WritableGraph graphWriteMethods = mock(WritableGraph.class);
        doNothing().when(graphWriteMethods).setFloatValue(0, 0, -1.0f);
        directionIndicatorAnimation.initialise(graphWriteMethods);        
        directionIndicatorAnimation.reset(graphWriteMethods);
        verify(graphWriteMethods, times(1)).setFloatValue(0, 0, -1.0f);
    }
    
    @Test
    public void testThrobbingNodeAnimation_reset() {

        WritableGraph graphWriteMethods = mock(WritableGraph.class);
        int vxId0 = graphWriteMethods.addVertex();
        doReturn(1).when(graphWriteMethods).getVertexCount();
        doReturn(vxId0).when(graphWriteMethods).getVertex(Mockito.anyInt());
        doReturn(1.0f).when(graphWriteMethods).getFloatValue(Mockito.anyInt(),
                Mockito.anyInt());
        doNothing().when(graphWriteMethods).setObjectValue(Mockito.anyInt(),
                Mockito.anyInt(), Mockito.any());

        throbbingNodeAnimation.initialise(graphWriteMethods);
        assertTrue(graphWriteMethods.getVertexCount() == 1);
        throbbingNodeAnimation.reset(graphWriteMethods);
        verify(graphWriteMethods, times(1)).setObjectValue(Mockito.anyInt(),
                Mockito.anyInt(), Mockito.any());        
    }
    
    @Test
    public void testGraphId() {
        colorWarpAnimation.setGraphID("TestGraph");
        assertTrue(colorWarpAnimation.graphID.equals("TestGraph"));
        
        directionIndicatorAnimation.setGraphID("TestGraph");
        assertTrue(directionIndicatorAnimation.graphID.equals("TestGraph"));
        
        flyingAnimation.setGraphID("TestGraph");
        assertTrue(flyingAnimation.graphID.equals("TestGraph"));
        
        throbbingNodeAnimation.setGraphID("TestGraph");
        assertTrue(throbbingNodeAnimation.graphID.equals("TestGraph"));
        
        panAnimation.setGraphID("TestGraph");
        assertTrue(panAnimation.graphID.equals("TestGraph"));
        
    }
    
    @Test
    public void testIsSignificant() {
        assertFalse(colorWarpAnimation.isSignificant());        
        assertFalse(directionIndicatorAnimation.isSignificant());
        assertFalse(flyingAnimation.isSignificant());
        assertFalse(throbbingNodeAnimation.isSignificant());
        assertFalse(panAnimation.isSignificant());
    }
    
}
