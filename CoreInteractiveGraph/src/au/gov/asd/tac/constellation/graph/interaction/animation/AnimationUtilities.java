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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author capricornunicorn123
 */
public class AnimationUtilities {
    
    /**
     * Stops all running animations on all graphs.
     */
    public static final void stopAllAnimations() {
        GraphManager.getDefault().getAllGraphs().values().forEach(graph -> {
            stopAllAnimations(graph);
        });
    }
    
    /**
     * Stops all running animations on the specified.
     * @param graph
     */
    public static final void stopAllAnimations(final Graph graph) {
        AnimationManager am = getGraphAnimationManager(graph);
        am.stopAllAnimations();
    }
    
    /**
     * Stops all running animations on the specified.
     * @param graph
     */
    public static final void stopAnimation(final String animationName, final Graph graph) {
        AnimationManager am = getGraphAnimationManager(graph);
        am.stopAnimation(animationName);
    }

    private static AnimationManager getGraphAnimationManager(Graph graph){
        if (graph != null){
            GraphNode gn = GraphNode.getGraphNode(graph);
            VisualGraphTopComponent vgtc = (VisualGraphTopComponent) gn.getTopComponent();
            return vgtc.getAnimationManager();
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
    public static final void startAnimation(final Animation animation, final Graph graph) {
        AnimationManager am = getGraphAnimationManager(graph);
        am.runAnimation(animation, graph);
    }
    
    /**
     * Checks if an animation is animating on the a graph.
     * @param name
     * @param graphID
     * @return 
     */
    public static boolean isAnimating(String name, Graph graph) {
        AnimationManager am = getGraphAnimationManager(graph);
        return am.isAnimating(name);
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
    public static final synchronized void interruptAllAnimations(Graph graph) {
        
        AnimationManager manager = getGraphAnimationManager(graph);
        
        manager.interruptAllAnimations();

    }

    public static void notifyComplete(final Animation animation) {
        AnimationUtilities.getGraphAnimationManager(GraphNode.getGraph(animation.graphID)).notifyComplete(animation);
    }
}
