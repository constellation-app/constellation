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
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for animations as well as static utilities for running animations.
 * <p>
 * The life-cycle of an animation is follows (this is a simplified version of
 * the logic that actually runs in an animation's thread):
 * <pre>
 * animation.initialise()
 * while (true) {
 *     animation.animate()
 *     // wait for this long
 *     animation.getIntervalInMillis()
 * }
 * animation.reset()
 * </pre>
 * </p>
 * All animations run on their own thread, enabling concurrent animations.
 * Each time an animation modifies the graph it must lock, modify and release the graph as quickly as possible. 
 *
 * @author twilight_sparkle
 */
public abstract class Animation {

    private static final Map<String, Map<String, Animation>> ANIMATED_GRAPHS = new HashMap<String, Map<String, Animation>>();

    private WritableGraph wg;;
    private String graphID;
    private boolean finished = false;
    private Thread animationThread;
    
    public void setGraphID(final String graphID){
        this.graphID = graphID;
    }
    
    /**
     * Stop this animation on the currently active graph.
     */
    public synchronized void stopAnimation() {
        stopAnimation(this.getName());
    }
    
    /**
     * Stop the requested animation on the currently active graph.
     * 
     * @param animationName
     */
    public static final synchronized void stopAnimation(final String animationName) {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        final Map<String, Animation> runningAnimations = ANIMATED_GRAPHS.get(activeGraph.getId());
        if (!runningAnimations.isEmpty()) {
            runningAnimations.remove(animationName).stop();
        }
    }
    
    /**
     * Stops all running animations on the currently active graph.
     */
    public static final synchronized void stopAllAnimation() {
        Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        Map<String, Animation> runningAnimations = ANIMATED_GRAPHS.get(activeGraph.getId());
        runningAnimations.keySet().forEach(animationName -> {
            runningAnimations.get(animationName).stop();
        });
    }

    /**
     * Start the specified animation on the active graph.
     *
     * @param animation The animation to run
     */
    public static final synchronized void startAnimation(final Animation animation) {
        startAnimation(animation, GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Start the specified animation on the specified graph.
     * if the specified animation is already running, nothing will occur.
     *
     * @param animation The animation to run
     * @param graph The graph to run the animation on.
     */
    public static final synchronized void startAnimation(final Animation animation, final Graph graph) {
        
        //Set the graph ID for this animation for easy reference over the ainimations s lifetime
        animation.setGraphID(graph.getId());
        
        //Get hte running animations for this animations graph
        Map<String, Animation> runningAnimations = ANIMATED_GRAPHS.get(animation.graphID);
        
        //No animations are running on this graph so a new map to store animations for this graph is created
        if (runningAnimations == null){
            runningAnimations = new HashMap<>();
            ANIMATED_GRAPHS.put(graph.getId(), runningAnimations);
        }
        
        //The animation is not running on the current graph so it can be run and registered
        if (runningAnimations.get(animation.getName()) == null){
            animation.run(graph);
            runningAnimations.put(animation.getName(), animation);
        }
    }
    
    /**
     * Checks if an animation is animating on the a graph.
     * @param name
     * @param graphID
     * @return 
     */
    public static boolean isAnimating(String name, String graphID) {
        Map<String, Animation> runningAnimations = ANIMATED_GRAPHS.get(graphID);
        if (runningAnimations != null){
            if (runningAnimations.get(name) != null){
                return true;
            } 
        }
        return false;
    }

    /**
     * Initialize this animation.
     * <p>
     * This method is called prior to any calls to {@link #animate} and allows
     * the animation to store any data that it will use over the course of the
     * animation. This method may also store initial state data for resetting 
     * after the animation.
     *
     * @param wg A write lock on the graph to initialize the animation with.
     */
    public abstract void initialise(GraphWriteMethods wg);

    /**
     * Run one frame of the animation.
     * <p>
     * This method should modify the graph as necessary and return a list of
     * visual changes that allow the relevant {@link VisualProcessor} to respond
     * quickly to these changes (without having to wait until the lock on the
     * graph is released).
     *
     * @param wg A write lock on the graph the animation is running on.
     * @return
     */
    public abstract void animate(GraphWriteMethods wg);

    /**
     * Reset any ephemeral changes made by the animation.
     * <p>
     * This method is called after all calls to {@link #animate} and allows the
     * animation to clean up any changes it made to the graph that should not
     * persist after the animation has concluded.
     *
     * @param wg A write lock on the graph to reset the animation on.
     */
    public abstract void reset(GraphWriteMethods wg);

    /**
     * Get the interval in milliseconds between each frame of the animation.
     * Note: Standard film frame rate is 24 fps which equates to 40 milliseconds per frame  
     *
     * @return The interval between frames for this animation.
     */
    public long getIntervalInMillis(){
        return 40;
    }

    /**
     * Get the name of this animation.
     * <p>
     * This name will be used to retrieve the write lock on the graph, and
     * consequentially if this animation is also significant, the name by which
     * the result of the animation can be undone/redone.
     *
     * @return The name of this animation.
     */
    protected abstract String getName();

    /**
     * Get whether or not this animation is 'significant'.
     * <p>
     * Significant animations will make significant edits on the graph, meaning
     * that their results can be undone/redone atomically.
     *
     * @return The significance of this animation.
     */
    protected boolean isSignificant() {
        return false;
    }

    /**
     * Marks this animation as finished, indicating that the next (or current)
     * call to animate will be the last.
     * <p>
     * This method is often called by {@link #animate} in implementations with a
     * finite number of frames.
     */
    protected final void setFinished() {
        finished = true;
    }

    /**
     * Handles the variables that persist for the life-cycle of the animation
     * and creates the animation thread.
     * @param graph 
     */
    private void run(final Graph graph) {
        if (GraphNode.getGraphNode(graph) != null) {
            
            // Chreate the Thread for this animaton
            animationThread = new Thread(() -> {
                try {
                    // Intialise the animation
                    if (lockGraphSafely(graph)){
                        initialise(wg);
                        wg.commit();
                        // Perform the animation
                        editGraph(graph);
                    } 
                } catch (InterruptedException ex) {
                    // Do nothing here
                    // Idealy woud throw a PluginException explaining why it was interrupted 
                    // but can't do this in a thread
                } finally {
                    if (lockGraphSafely(graph)) {                    
                        reset(wg);
                        wg.commit();
                    }
                    
                    final Map<String, Animation> runningAnimations = ANIMATED_GRAPHS.get(this.graphID);
                    runningAnimations.remove(this.getName());

                    if (runningAnimations.isEmpty()){
                        ANIMATED_GRAPHS.remove(this.graphID);
                    }
                }    
            });
            animationThread.start();
        }
    }

    /**
     * Handles the flow of the life-cycle of the animation
     * To enable concurrent animation as well as graph manipulation during animation 
     * the animation must only hold graph locks when necessary
     * @param graph
     * @throws InterruptedException 
     */
    private void editGraph(final Graph graph) throws InterruptedException{
        
        while (!finished) {
                
            // Animate a frame
            if (lockGraphSafely(graph)){
                animate(wg);
                wg.commit();
            }
            
            // Sleep untill it is time for the next frame
            Thread.sleep(getIntervalInMillis());
        }
    }
    
    /**
     * Attempts to put a lock on the graph. 
     * If a lock on the graph has been applied, will return true.
     * @param graph
     * @return 
     */
    private boolean lockGraphSafely(final Graph graph){
        try {
            wg = graph.getWritableGraph(getName(), isSignificant());
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    private void stop() {
        setFinished();
    }
    
    /**
     * Interrupt all running animations on all animated graphs.
     */
    public static final synchronized void interruptAllAnimation() {
        
        // Itterate over all animated graphs
        ANIMATED_GRAPHS.keySet().forEach(graphKey -> {
            
            // Get the animations curently running on that grah
            final Map<String, Animation> runningAnimations = ANIMATED_GRAPHS.get(graphKey);
            
            //Itterate over all animations on that graph and interrupt the annimation
            runningAnimations.keySet().forEach(animationName -> {
                runningAnimations.get(animationName).interrupt();
            });
            
            //Remove all reverences to animations from the graph reference
            runningAnimations.clear();
        });
        
        //Remove all references to graphs being animated
        ANIMATED_GRAPHS.clear();
    }
    
    /**
     * Interrupt all running animations on all animated graphs.
     * @param graphId
     */
    public static final synchronized void interruptGraphAnimation(final String graphId) {
        
        // Get the animations curently running on that grah
        final Map<String, Animation> runningAnimations = ANIMATED_GRAPHS.remove(graphId);
            if (runningAnimations != null){

            //Itterate over all animations on that graph and interrupt the annimation
            runningAnimations.keySet().forEach(animationName -> {
                runningAnimations.get(animationName).interrupt();
            });

            //Remove all reverences to animations from the graph reference
            runningAnimations.clear();
        }
    
    }
    
    /**
     * Interrupt this animation.
     */
    public void interrupt(){
        this.animationThread.interrupt();
    }
}
