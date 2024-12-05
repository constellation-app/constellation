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

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for managing animations on a {@link VisualGraph}. 
 * Stores a reference to all running animations on the respective {@link VisualGraph}.
 * 
 * @author twilight_sparkle
 * @author capricornunicorn123
 */
public class AnimationManager {

    private final Map<String, Animation> animations = new HashMap<>();
    private final String graphId;
    private boolean isPaused = false;
    
    public AnimationManager(final String graphId) {
        this.graphId = graphId;
    }

    /**
     * Keep track of animations as a set. If an animation is in the set,
     * assume that it will be animating, unless paused. On stopping the
     * specific animation, it will be removed from the set.
     * @return the animations
     */
    public Map<String, Animation> getAnimations() {
        return animations;
    }

    /**
     * Stop the requested animation running on this AnimationManagers Graph.
     * Specified animation will be removed from the animations set.
     * @param animationName
     */
    public final void stopAnimation(final String animationName) {
        final Animation removedAnimation = getAnimations().get(animationName);
        if (removedAnimation != null) {
            if (isPaused){
                removedAnimation.interrupt();
                removedAnimation.stop();
            } else {
                removedAnimation.stop();
            }
            getAnimations().remove(animationName);            
        }
        
        if (getAnimations().isEmpty()) {
            this.isPaused = false;
        }
    }
    
    /**
     * Stops animating all animations on an graph.
     */
    public void stopAllAnimations() {
        getAnimations().values().forEach(animation -> {
            if (isPaused){
                animation.interrupt();
            } else {
                animation.setFinished();
            }
        });
        getAnimations().clear();
        this.isPaused = false;
    }

    /**
     * Run the specified animation on the specified graph.
     * If the animation this attempt to run is ignored.
     *
     * @param animation The animation to run
     */
    public void runAnimation(final Animation animation) {        
        if (getAnimations().get(animation.getName()) == null) {
            animation.run(graphId);
            getAnimations().put(animation.getName(), animation);
        }
    }
    
    /**
     * Checks if an animation with specified name is animating.
     * 
     * @param name
     * @return 
     */
    public boolean isAnimating(final String name) {
        return getAnimations().get(name) != null;
    }
    
    /**
     * Are there any animations?
     * @return 
     */
    public boolean isAnimating() {
        return !this.animations.isEmpty();
    }
    
    /**
     * Interrupt all animations.
     */
    public void interruptAllAnimations() {
        getAnimations().values().forEach(animation -> {
            animation.interrupt();
        });
        getAnimations().clear();
        this.isPaused = false;
    }

    /**
     * Notify the AnimationManger the animation has completed.
     * The animation manager will de-register the animation.
     * 
     * @param animation 
     */
    void notifyComplete(final Animation animation) {
        getAnimations().remove(animation.getName());
        if (getAnimations().isEmpty()) {
            this.isPaused = false;
        }
    }

    /**
     * Pause/Unpause all animations.
     * @param pause True/False
     */
    void pauseAllAnimations(final boolean pause) {
        this.isPaused = pause;
    }
    
    public boolean isPaused(){
        return isPaused;
    }
}
