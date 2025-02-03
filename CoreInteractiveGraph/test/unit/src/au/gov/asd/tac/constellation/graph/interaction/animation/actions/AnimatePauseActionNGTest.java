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
package au.gov.asd.tac.constellation.graph.interaction.animation.actions;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.animation.AnimationUtilities;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class AnimatePauseActionNGTest {
    private MockedStatic<AnimationUtilities> animationUtilitiesMocked;
    private GraphNode graphNodeMock;
    private final String mockGraphId = "Test graph Id";
    private Graph mockGraph;
    
    public AnimatePauseActionNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {

        animationUtilitiesMocked = mockStatic(AnimationUtilities.class);
                
        graphNodeMock = mock(GraphNode.class);
        mockGraph = mock(Graph.class);
        doReturn(mockGraph).when(graphNodeMock).getGraph();
        doReturn(mockGraphId).when(mockGraph).getId();       
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        animationUtilitiesMocked.close();
    }

    /**
     * Test of updateValue method, of class AnimatePauseAction.
     */
    @Test
    public void testUpdateValue() {
        System.out.println("AnimatePauseAction updateValue");

        animationUtilitiesMocked.when(()
                -> AnimationUtilities.pauseAllAnimations(Mockito.anyString(), Mockito.anyBoolean()))
                .then((Answer<Void>) invocation -> null);

        GraphNode contextMock = mock(GraphNode.class);       
        AnimatePauseAction animatePauseAction = spy(new AnimatePauseAction());
        doReturn(contextMock).when(animatePauseAction).getContext();
        doReturn(mockGraph).when(contextMock).getGraph();
        
        // toggle pause from true to false
        animationUtilitiesMocked.when(()
                -> AnimationUtilities.isGraphAnimationsPaused(Mockito.any()))
                .thenReturn(true);

        animatePauseAction.updateValue();

        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.pauseAllAnimations(mockGraphId, false), times(1));
        
        // toggle pause from false to true
        animationUtilitiesMocked.when(()
                -> AnimationUtilities.isGraphAnimationsPaused(Mockito.any()))
                .thenReturn(false);
        
        animatePauseAction.updateValue();
        
        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.pauseAllAnimations(mockGraphId, true), times(1));               
    }

    /**
     * Test of displayValue method, of class AnimatePauseAction.
     */
    @Test
    public void testDisplayValue() {
        System.out.println("AnimatePauseAction displayValue");
        GraphNode contextMock = mock(GraphNode.class);    
     
        AnimatePauseAction animatePauseAction = spy(new AnimatePauseAction());
        doReturn(contextMock).when(animatePauseAction).getContext();
        doReturn(mockGraph).when(contextMock).getGraph();
        animationUtilitiesMocked.when(()
                -> AnimationUtilities.isGraphAnimationsPaused(Mockito.any()))
                .thenReturn(true);
        
        animatePauseAction.displayValue();
        assertTrue(animatePauseAction.menuButton.getText().equals("Resume Animating"));
        
        animationUtilitiesMocked.when(()
                -> AnimationUtilities.isGraphAnimationsPaused(Mockito.any()))
                .thenReturn(false);
        
        animatePauseAction.displayValue();
        assertTrue(animatePauseAction.menuButton.getText().equals("Pause Animating"));
    }   
}
