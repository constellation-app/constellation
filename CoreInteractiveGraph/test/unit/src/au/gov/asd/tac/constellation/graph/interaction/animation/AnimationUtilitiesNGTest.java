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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import java.util.HashMap;
import java.util.Map;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class AnimationUtilitiesNGTest {
    private Graph graph;
    private GraphManager graphManagerMock;
    private AnimationManager animationManager;
    private VisualGraphTopComponent topComponentMock;

    private GraphNode graphNodeMock;
    private final String graphName = "Test Graph 1";
    private String graphId;
    private Animation colorWarpAnimation;
    private Animation directionIndicatorAnimation;
    private final Map<String, Graph> graphs = new HashMap<>();
    private MockedStatic<AnimationUtilities> animationUtilitiesMock;
    private MockedStatic<GraphNode> graphNodeStaticMock;
    private MockedStatic<GraphManager> graphManagerStaticMock;
    
    public AnimationUtilitiesNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema ss = SchemaFactoryUtilities.getSchemaFactory(
                VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new DualGraph(ss);
        graphId = graph.getId();
        graphs.put(graphName, graph);
        graphManagerMock = mock(GraphManager.class);
        graphNodeStaticMock = mockStatic(GraphNode.class);
 
        colorWarpAnimation = spy(ColorWarpAnimation.class);
        directionIndicatorAnimation = spy(DirectionIndicatorAnimation.class);
        colorWarpAnimation.graphID = graphId;
        animationManager = mock(AnimationManager.class);                
        graphNodeMock = mock(GraphNode.class);
        graphManagerMock = mock(GraphManager.class);
        graphManagerStaticMock = mockStatic(GraphManager.class);
        topComponentMock = mock(VisualGraphTopComponent.class);

        doReturn(graphName).when(graphNodeMock).getDisplayName();
        doReturn(graph).when(graphNodeMock).getGraph();        
        doNothing().when(colorWarpAnimation).interrupt();
        doNothing().when(directionIndicatorAnimation).interrupt();
        doReturn(true).when(animationManager).isAnimating();
        doReturn(true).when(animationManager).isAnimating(Mockito.anyString());
        
        animationUtilitiesMock = 
                mockStatic(AnimationUtilities.class, Mockito.CALLS_REAL_METHODS);
        
        graphNodeStaticMock.when(() 
                -> GraphNode.getGraphNode(graph))
                .thenReturn(graphNodeMock);
        
        graphManagerStaticMock.when(() 
                -> GraphManager.getDefault())
                .thenReturn(graphManagerMock);
        
        doReturn(topComponentMock).when(graphNodeMock).getTopComponent();
        doReturn(animationManager).when(topComponentMock).getAnimationManager();        
        doReturn(graphs).when(graphManagerMock).getAllGraphs();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        animationUtilitiesMock.close();
        graphManagerStaticMock.close();
        graphNodeStaticMock.close();
        animationManager = null;
        
    }
    
    @Test
    public void testAnimationUtilities_animationsEnabled() {
        assertTrue(AnimationUtilities.animationsEnabled());
    }
    
    @Test
    public void testAnimationUtilities_startAnimation() {

        animationUtilitiesMock.when(()
                -> AnimationUtilities.animationsEnabled())
                .thenReturn(false);

        AnimationUtilities.startAnimation(colorWarpAnimation, graphId);
        // expect skip
        verify(colorWarpAnimation, times(1)).skip(graphId);
        
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(animationManager);

        animationUtilitiesMock.when(()
                -> AnimationUtilities.animationsEnabled())
                .thenReturn(true);

        doNothing().when(animationManager).runAnimation(colorWarpAnimation);
        
        AnimationUtilities.startAnimation(colorWarpAnimation, graphId);
        // expect runAnimation
        verify(animationManager, times(1)).runAnimation(colorWarpAnimation);
        
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(null);
        AnimationUtilities.startAnimation(colorWarpAnimation, graphId);
        // expect another skip => times(2)
        verify(colorWarpAnimation, times(2)).skip(graphId);
    }
    
    @Test
    public void testAnimationUtilities_isAnimating() {
        
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(animationManager);

        boolean isAnimating = AnimationUtilities.isAnimating(colorWarpAnimation.getName(), graphId);
        
        // expect isAnimating to be called once with name
        verify(animationManager, times(1)).isAnimating(colorWarpAnimation.getName());
        assertTrue(isAnimating);
        
        isAnimating = AnimationUtilities.isAnimating(graphId);
        assertTrue(isAnimating);
        
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(null);
        
        isAnimating = AnimationUtilities.isAnimating(graphId);
        assertFalse(isAnimating);                
    }
    
    @Test
    public void testAnimationUtilities_interruptAllAnimations(){
        
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(animationManager);
        
        doNothing().when(animationManager).interruptAllAnimations();
                
        AnimationUtilities.interruptAllAnimations();
        verify(animationManager, times(1)).interruptAllAnimations();
    }
    
    @Test
    public void testAnimationUtilities_interruptAllAnimationsWithName(){
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(animationManager);
        
        doNothing().when(animationManager).interruptAllAnimations();
                
        AnimationUtilities.interruptAllAnimations(graphId);
        verify(animationManager, times(1)).interruptAllAnimations();
    }
    
    @Test
    public void testAnimationUtilities_pauseAllAnimations(){
       
         animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(null);
         
        AnimationUtilities.pauseAllAnimations(graphId, true);
        verify(animationManager, times(0)).pauseAllAnimations(true);
        
         animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(animationManager);
        
        doNothing().when(animationManager).pauseAllAnimations(true);
        AnimationUtilities.pauseAllAnimations(graphId, true);
        verify(animationManager, times(1)).pauseAllAnimations(true);
    }
    
    @Test
    public void testAnimationUtilities_isGraphAnimationsPaused(){
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(animationManager);
        
        doReturn(true).when(animationManager).isPaused();
                
        assertTrue(AnimationUtilities.isGraphAnimationsPaused(graphId));
        verify(animationManager, times(1)).isPaused();
        
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphId))
                .thenReturn(null);
        
        assertFalse(AnimationUtilities.isGraphAnimationsPaused(graphId));
    }
     
    
    @Test
    public void testAnimationUtilities_notifyComplete(){
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(colorWarpAnimation.graphID))
                .thenReturn(null);
        
        // it shouldn't be triggered when manager is null
        verify(animationManager, times(0)).notifyComplete(colorWarpAnimation);
        
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(colorWarpAnimation.graphID))
                .thenReturn(animationManager);
        
        AnimationUtilities.notifyComplete(colorWarpAnimation);
        verify(animationManager, times(1)).notifyComplete(colorWarpAnimation);                
    }
    
    @Test
    public void testAnimationUtilities_stopAllAnimations(){       
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphName))
                .thenReturn(animationManager);
        
        AnimationUtilities.stopAllAnimations(graphName);
        verify(animationManager, times(1)).stopAllAnimations();
        
        doReturn(graphs).when(graphManagerMock).getAllGraphs();
        AnimationUtilities.stopAllAnimations();
        verify(animationManager, times(2)).stopAllAnimations();   
    }
    
    @Test
    public void testAnimationUtilities_stopAnimation(){       
        animationUtilitiesMock.when(()
                -> AnimationUtilities.getGraphAnimationManager(graphName))
                .thenReturn(animationManager);
        
        AnimationUtilities.stopAnimation(colorWarpAnimation.getName(), graphName);
        verify(animationManager, times(1)).stopAnimation(colorWarpAnimation.getName());
    }
}
