/*
 * Copyright 2010-2021 Australian Signals Directorate
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
        getGraphAnimationManager(graphId).stopAllAnimations();
    }
    
    /**
     * Stops all running animations on the specified.
     * 
     * @param animationName
     * @param graphId
     */
    public static final void stopAnimation(final String animationName, final String graphId) {
        getGraphAnimationManager(graphId).stopAnimation(animationName);
    }

    /**
     * Gets the animationManager for a specific graph.
     * 
     * @param graphId
     * @return 
     */
    private static AnimationManager getGraphAnimationManager(final String graphId){
        if (StringUtils.isNotBlank(graphId)){
            GraphNode gn = GraphNode.getGraphNode(graphId);
            if (gn != null) {
             return ((VisualGraphTopComponent) gn.getTopComponent()).getAnimationManager();
            }
        }
        return null; 
    }
    
    /**
     * Start the specified animation on the specified graph.
     * If the specified animation is already running, nothing will occur.
     * If animations have ben disabled, the animation may skip and 
     * update the graph to the final frame of the animation.
     *
     * @param animation The animation to run
     * @param graphId The graph to run the animation on.
     */
    public static final void startAnimation(final Animation animation, final String graphId) {
        // Run the animation
        if (animationsEnabled()){
            AnimationManager am = getGraphAnimationManager(graphId);
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
        return GraphPreferenceKeys.isAnimatable(NbPreferences.forModule(GraphPreferenceKeys.class));
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
     * Interrupt all running animations on the specified graph animated graphs.
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
        AnimationUtilities.getGraphAnimationManager(animation.graphID).notifyComplete(animation);
    }
}
