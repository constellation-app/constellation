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

import au.gov.asd.tac.constellation.graph.interaction.animation.AnimationUtilities;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.*;
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
        AnimateStopAction instance = new AnimateStopAction();
        instance.updateValue();
        animationUtilitiesMocked.verify(() 
                -> AnimationUtilities.stopAllAnimations(), times(1));      
    }
}
