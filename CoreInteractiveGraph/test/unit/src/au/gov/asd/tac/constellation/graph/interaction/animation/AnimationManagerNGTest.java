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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class AnimationManagerNGTest {

    private AnimationManager animationManager;
    private String graphId;
    private Animation colorWarpAnimation;
    private Animation directionIndicatorAnimation;
    
    public AnimationManagerNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        colorWarpAnimation = spy(ColorWarpAnimation.class);
        directionIndicatorAnimation = spy(DirectionIndicatorAnimation.class);       
        animationManager = new AnimationManager(graphId);
                
        doNothing().when(colorWarpAnimation).interrupt();
        doNothing().when(directionIndicatorAnimation).interrupt();        
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        animationManager = null;
    }

    @Test
    public void testAnimationManager_RunAnimation() {
        animationManager.runAnimation(colorWarpAnimation);  
        assertTrue(animationManager.getAnimations().size() == 1);
        assertTrue(animationManager.getAnimations().containsValue(colorWarpAnimation));        
    }

    @Test
    public void testAnimationUtilities_isAnimating() {
        assertFalse(animationManager.isAnimating());
        animationManager.runAnimation(colorWarpAnimation);  
        assertTrue(animationManager.isAnimating());        
        animationManager.stopAllAnimations();
        assertFalse(animationManager.isAnimating());       
    }
      
    
    @Test
    public void testAnimationManager_isAnimatingName() {
        assertFalse(animationManager.isAnimating(colorWarpAnimation.getName()));
        
        animationManager.runAnimation(colorWarpAnimation);
        animationManager.runAnimation(directionIndicatorAnimation);
        assertTrue(animationManager.isAnimating(ColorWarpAnimation.NAME));
        assertTrue(animationManager.isAnimating(
                DirectionIndicatorAnimation.NAME));
        assertTrue(animationManager.isAnimating(colorWarpAnimation.getName()));
        assertTrue(animationManager.isAnimating(directionIndicatorAnimation.getName()));        
        assertFalse(animationManager.isAnimating("Some random name"));
        
        // stop animation 1
        animationManager.stopAnimation(colorWarpAnimation.getName());
        assertFalse(animationManager.isAnimating(colorWarpAnimation.getName()));
        assertTrue(animationManager.isAnimating(directionIndicatorAnimation.getName()));        
        
    }
    
    @Test
    public void testAnimationManager_pauseAllAnimations() {
        animationManager.runAnimation(colorWarpAnimation);
        animationManager.runAnimation(directionIndicatorAnimation);
        
        assertTrue(animationManager.isAnimating(colorWarpAnimation.getName()));
        assertTrue(animationManager.isAnimating(directionIndicatorAnimation.getName()));
        animationManager.pauseAllAnimations(true);
        assertTrue(animationManager.isPaused());
        assertTrue(animationManager.isAnimating(colorWarpAnimation.getName()));
        assertTrue(animationManager.isAnimating(directionIndicatorAnimation.getName()));
        
        
        animationManager.pauseAllAnimations(false);
        assertFalse(animationManager.isPaused());
    }
    
    @Test
    public void testAnimationManager_stopAnimations() {
        assertFalse(animationManager.isAnimating());
        animationManager.runAnimation(colorWarpAnimation);  
        animationManager.runAnimation(directionIndicatorAnimation);
        
        assertTrue(animationManager.isAnimating());
        
        animationManager.stopAnimation(colorWarpAnimation.getName());
        
        // colorWarpAnimation should stop animating and be removed from animations list
        assertFalse(animationManager.isAnimating(colorWarpAnimation.getName()));
        assertTrue(animationManager.getAnimations().size() == 1);
        assertTrue(animationManager.getAnimations()
                .get(colorWarpAnimation.getName()) == null);
        assertTrue(animationManager.isAnimating(directionIndicatorAnimation.getName()));
                
        animationManager.stopAllAnimations();
        assertFalse(animationManager.isAnimating(directionIndicatorAnimation.getName()));
        assertTrue(animationManager.getAnimations().isEmpty());
        
    }
    
    @Test
    public void testAnimationManager_interruptAllAnimations() {
        assertFalse(animationManager.isAnimating());
        animationManager.runAnimation(colorWarpAnimation);  
        animationManager.runAnimation(directionIndicatorAnimation);
        
        assertTrue(animationManager.isAnimating());
        animationManager.interruptAllAnimations();
        assertTrue(animationManager.getAnimations().isEmpty());
        assertTrue(!animationManager.isPaused());
    }
    
    @Test
    public void testAnimationManager_notifyComplete() {
        animationManager.runAnimation(colorWarpAnimation);  
        animationManager.runAnimation(directionIndicatorAnimation);
        assertTrue(animationManager.getAnimations().size() == 2);
        
        animationManager.notifyComplete(colorWarpAnimation);
        assertTrue(animationManager.getAnimations().size() == 1);
        
        animationManager.notifyComplete(directionIndicatorAnimation);
        assertTrue(animationManager.getAnimations().isEmpty());
        assertFalse(animationManager.isPaused());
    }
}
