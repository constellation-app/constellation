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
import org.openide.util.NbPreferences;

/**
 * Provides static methods to start and stop animations.
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
     * @param graph
     */
    public static final void stopAllAnimations(final String graphId) {
        AnimationManager am = getGraphAnimationManager(graphId);
        am.stopAllAnimations();
    }
    
    /**
     * Stops all running animations on the specified.
     * @param graph
     */
    public static final void stopAnimation(final String animationName, final String graphId) {
        AnimationManager am = getGraphAnimationManager(graphId);
        am.stopAnimation(animationName);
    }

    private static AnimationManager getGraphAnimationManager(final String graphId){
        if (!graphId.isBlank()){
            return ((VisualGraphTopComponent) GraphNode.getGraphNode(graphId).getTopComponent()).getAnimationManager();
        } else {
            return null;
        }
    }
    
    /**
     * Start the specified animation on the specified graph.
     * if the specified animation is already running, nothing will occur.
     *
     * @param animation The animation to run
     * @param graph The graph to run the animation on.
     */
    public static final void startAnimation(final Animation animation, final String graphId) {
        if (animationsEnabled()){
            AnimationManager am = getGraphAnimationManager(graphId);
            am.runAnimation(animation);
        }
    }
    
    /**
     * Checks if an animation is animating on the a graph.
     * @param name
     * @param graphID
     * @return 
     */
    public static boolean isAnimating(final String name, final String graphId) {
        AnimationManager am = getGraphAnimationManager(graphId);
        return am.isAnimating(name);
    }
    
    public static boolean animationsEnabled(){
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
     * Interrupt all running animations on all animated graphs.
     */
    public static final synchronized void interruptAllAnimations(final String graphId) {
        
        AnimationManager manager = getGraphAnimationManager(graphId);
        
        manager.interruptAllAnimations();

    }

    public static void notifyComplete(final Animation animation) {
        AnimationUtilities.getGraphAnimationManager(animation.graphID).notifyComplete(animation);
    }
}
