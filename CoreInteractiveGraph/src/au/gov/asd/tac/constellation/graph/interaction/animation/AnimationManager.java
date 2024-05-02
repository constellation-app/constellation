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
    
    public AnimationManager(final String graphId) {
        this.graphId = graphId;
    }

    /**
     * Stop the requested animation running on this AnimationManagers Graph.
     * 
     * @param animationName
     */
    public final void stopAnimation(final String animationName) {
        final Animation removedAnimation = animations.remove(animationName);
        if (removedAnimation != null) {
            removedAnimation.setFinished();
        }
    }
    
    /**
     * Stops animating all animations on an 
     */
    public void stopAllAnimations() {
        animations.values().forEach(animation -> {
            animation.setFinished();
        });
        animations.clear();
    }

    /**
     * Run the specified animation on the specified graph.
     * If the animation this attempt to run is ignored.
     *
     * @param animation The animation to run
     */
    public void runAnimation(final Animation animation) {
        
        if (animations.get(animation.getName()) == null) {
            animation.run(graphId);
            animations.put(animation.getName(), animation);
        }
    }
    
    /**
     * Checks if an animation is animating.
     * 
     * @param name
     * @return 
     */
    public boolean isAnimating(final String name) {
        return animations.get(name) != null;
    }
    
    /**
     * Interrupt this animation.
     */
    public void interruptAllAnimations() {
        animations.values().forEach(animation -> {
            animation.interrupt();
        });
    }

    /**
     * Notify the AnimationManger the animation has complete.
     * The animation manager will de-register the animation.
     * 
     * @param animation 
     */
    void notifyComplete(final Animation animation) {
        animations.remove(animation.getName());
    }

    void pauseAllAnimations(long time) {
        animations.values().forEach(animation -> {
            animation.pause(time);
        });
    }
}
