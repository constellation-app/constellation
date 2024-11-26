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

import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.preferences.GraphPreferenceKeys;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;

/**
 * Provides static utilities for running animations.
 * This utility class enables animations to be started and stopped 
 * without the need of references to their specific managers.
 * 
 * @author capricornunicorn123
 */
public class AnimationUtilities {
    
    /**
     * Stops all running animations on all graphs.
     */
    public static final void stopAllAnimations() {
        GraphManager.getDefault().getAllGraphs().keySet().forEach(graphId -> {
            stopAllAnimations(graphId);
        });
    }
    
    /**
     * Stops all running animations on the specified.
     * 
     * @param graphId
     */
    public static final void stopAllAnimations(final String graphId) {
        final AnimationManager graphAnimationManager = getGraphAnimationManager(graphId);
        if (graphAnimationManager != null) {
            graphAnimationManager.stopAllAnimations();
        }
    }
    
    /**
     * Stops all running animations on the specified graph.
     * 
     * @param animationName
     * @param graphId
     */
    public static final void stopAnimation(final String animationName, final String graphId) {
        final AnimationManager graphAnimationManager = getGraphAnimationManager(graphId);
        if (graphAnimationManager != null) {
            graphAnimationManager.stopAnimation(animationName);
        }
    }

    /**
     * Gets the animationManager for a specific graph.
     * 
     * @param graphId
     * @return 
     */
    protected static AnimationManager getGraphAnimationManager(final String graphId){
        if (StringUtils.isNotBlank(graphId)){
            final GraphNode gn = GraphNode.getGraphNode(graphId);
            if (gn != null) {
             return ((VisualGraphTopComponent) gn.getTopComponent()).getAnimationManager();
            }
        }
        return null; 
    }
    
    /**
     * Start the specified animation on the specified graph.
     * If the specified animation is already running, nothing will occur.
     * If animations have been disabled, the animation may skip and 
     * update the graph to the final frame of the animation.
     *
     * @param animation The animation to run
     * @param graphId The graph to run the animation on.
     */
    public static final void startAnimation(final Animation animation, final String graphId) {
        // Run the animation
        if (animationsEnabled()){
            final AnimationManager am = getGraphAnimationManager(graphId);
            if (am != null){
                    am.runAnimation(animation);
            } else {
                animation.skip(graphId);
            }
        } else {
            animation.skip(graphId);
        }
    }
    
    /**
     * Checks if an animation is animating on the a graph.
     * 
     * @param name
     * @param graphId
     * @return 
     */
    public static boolean isAnimating(final String name, final String graphId) {
        return getGraphAnimationManager(graphId).isAnimating(name);
    }
    
    /**
     * Checks if the user settings currently have animations enabled.
     * 
     * @return 
     */
    public static boolean animationsEnabled() {
        return NbPreferences.forModule(GraphPreferenceKeys.class).getBoolean(GraphPreferenceKeys.ENABLE_ANIMATIONS, GraphPreferenceKeys.ENABLE_ANIMATIONS_DEFAULT);
    }
    
    /**
     * Interrupt all running animations on all animated graphs.
     */
    public static final synchronized void interruptAllAnimations() {
        GraphManager.getDefault().getAllGraphs().values().forEach(graph -> {
            ((VisualGraphTopComponent) GraphNode.getGraphNode(graph).getTopComponent()).getAnimationManager().interruptAllAnimations();
        });
    }
    
    /**
     * Interrupt all running animations on the specified graph.
     * 
     * @param graphId
     */
    public static final synchronized void interruptAllAnimations(final String graphId) {
        getGraphAnimationManager(graphId).interruptAllAnimations();
    }

    /**
     * Notify the relevant graph manager that the specified animation has completed.
     * 
     * @param animation 
     */
    public static void notifyComplete(final Animation animation) {
        final AnimationManager manager = 
                AnimationUtilities.getGraphAnimationManager(animation.graphID);
        if (manager != null){
                manager.notifyComplete(animation);
        }
    }

    /**
     * Stops animations for a predefined period of time
     * @param graphId
     * @param pause 
     */
    public static void pauseAllAnimations(final String graphId, final boolean pause) {
        final AnimationManager manager = getGraphAnimationManager(graphId);
        if (manager != null){
            manager.pauseAllAnimations(pause);
        }
    }
    
    public static boolean isGraphAnimationsPaused(final String graphId){
        final AnimationManager manager = getGraphAnimationManager(graphId);
        if (manager != null){
            return manager.isPaused();
        } else {
            return false;
        }
    }

    public static boolean isAnimating(final String graphId){
        final AnimationManager manager = getGraphAnimationManager(graphId);
        if (manager != null){
            return manager.isAnimating();
        } else {
            return false;
        }
    }
}
