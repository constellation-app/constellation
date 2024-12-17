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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class AnimateStopActionNGTest {
    private MockedStatic<AnimationUtilities> animationUtilitiesMocked;
    
    public AnimateStopActionNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        animationUtilitiesMocked = mockStatic(AnimationUtilities.class);
        
        animationUtilitiesMocked.when(() 
                -> AnimationUtilities.stopAllAnimations())
                .then((Answer<Void>) invocation -> null);
        
        animationUtilitiesMocked.when(() 
                -> AnimationUtilities.stopAllAnimations(Mockito.any()))
                .then((Answer<Void>) invocation -> null);  
        
        animationUtilitiesMocked.when(() 
                -> AnimationUtilities.isAnimating(Mockito.any()))
                .thenReturn(true);    
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        animationUtilitiesMocked.close();
    }

    /**
     * Test of updateValue method, of class AnimateStopAction.
     */
    @Test
    public void testUpdateValue() {
        System.out.println("AnimateStopAction updateValue");
        AnimateStopAction instance = spy(new AnimateStopAction());
        GraphNode gnMock = mock(GraphNode.class);
        Graph graphMock = mock(Graph.class);
        String testMock = "test id";
        doReturn(gnMock).when(instance).getContext();
        doReturn(graphMock).when(gnMock).getGraph();
        doReturn(testMock).when(graphMock).getId();
        
        instance.updateValue();
        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.stopAllAnimations(testMock), times(1));      
    }
}
