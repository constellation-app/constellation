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
import javax.swing.JMenuItem;
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
public class AnimateDirectionIndicatorActionNGTest {
    private MockedStatic<AnimationUtilities> animationUtilitiesMocked;
    private GraphNode graphNodeMock;
    private final String mockGraphId = "Test graph Id";
    private Graph mockGraph;
    
    public AnimateDirectionIndicatorActionNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {

        animationUtilitiesMocked = mockStatic(AnimationUtilities.class);
        animationUtilitiesMocked.when(() 
                -> AnimationUtilities.startAnimation(Mockito.any(), Mockito.any()))
                .then((Answer<Void>) invocation -> null);
        animationUtilitiesMocked.when(() 
                -> AnimationUtilities.stopAnimation(Mockito.any(), Mockito.any()))
                .then((Answer<Void>) invocation -> null);
        
        animationUtilitiesMocked.when(() 
                -> AnimationUtilities.isAnimating(Mockito.any(), Mockito.any()))
                .thenReturn(true);
        
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
     * Test of updateValue method, of class AnimateDirectionIndicatorAction.
     */
    @Test
    public void testUpdateValue() {
        System.out.println("AnimateDirectionIndicatorAction updateValue");
        GraphNode contextMock = mock(GraphNode.class);       
        AnimateDirectionIndicatorAction animateDirectionIndicatorAction = spy(new AnimateDirectionIndicatorAction());
        animateDirectionIndicatorAction.menuButton = mock(JMenuItem.class);
        doReturn(contextMock).when(animateDirectionIndicatorAction).getContext();
        doReturn(mockGraph).when(contextMock).getGraph();
        doReturn(true).when(animateDirectionIndicatorAction.menuButton).isSelected();
        animateDirectionIndicatorAction.updateValue();
        
        // when true, startAnimation is called
        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.startAnimation(Mockito.any(), Mockito.any()), times(1));
        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.stopAnimation(Mockito.any(), Mockito.any()), times(0));
        
        // when false, stopAnimation is called
        doReturn(false).when(animateDirectionIndicatorAction.menuButton).isSelected();
        animateDirectionIndicatorAction.updateValue();
        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.stopAnimation(Mockito.any(), Mockito.any()), times(1));
        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.startAnimation(Mockito.any(), Mockito.any()), times(1));
    }

    /**
     * Test of displayValue method, of class AnimateDirectionIndicatorAction.
     */
    @Test
    public void testDisplayValue() {
        System.out.println("AnimateDirectionIndicatorAction displayValue");
        GraphNode contextMock = mock(GraphNode.class);       
        AnimateDirectionIndicatorAction animateDirectionIndicatorAction = spy(new AnimateDirectionIndicatorAction());
        doReturn(contextMock).when(animateDirectionIndicatorAction).getContext();
        doReturn(mockGraph).when(contextMock).getGraph();
        
        animateDirectionIndicatorAction.displayValue();
        assertTrue(animateDirectionIndicatorAction.menuButton.isSelected());
    }

    /**
     * Test of stopAnimation method, of class AnimateDirectionIndicatorAction.
     */
    @Test
    public void testStopAnimation() {
        System.out.println("AnimateDirectionIndicatorAction stopAnimation");
        AnimateDirectionIndicatorAction instance = new AnimateDirectionIndicatorAction();
        instance.stopAnimation(mockGraphId);
        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.stopAnimation(
                        Mockito.any(), Mockito.any()), times(1));
        
    }
}
