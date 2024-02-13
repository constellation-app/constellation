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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;

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

    private static final List<Animation> runningAnimations = new ArrayList<Animation>();
    
    private WritableGraph wg;
    private VisualManager manager;
    
    /**
     * Stop this animation.
     */
    public synchronized void stopAnimation() {
        stopAnimation(this);
    }
    
    /**
     * Stop the requested animation.
     * 
     * @param currentlyRunningAnimation
     */
    public static final synchronized void stopAnimation(final Animation currentlyRunningAnimation) {
        if (!runningAnimations.isEmpty()) {
            runningAnimations.remove(runningAnimations.indexOf(currentlyRunningAnimation)).stop();
        }
    }
    
    /**
     * Stop all running animations.
     */
    public static final synchronized void stopAllAnimation() {
        runningAnimations.forEach(animation -> {
            animation.stop();
        });
        runningAnimations.clear();
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
     *
     * @param animation The animation to run
     * @param graph The graph to run the animation on.
     */
    public static final synchronized void startAnimation(final Animation animation, final Graph graph) {
        animation.run(graph);
        runningAnimations.add(animation);
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
    public abstract List<VisualChange> animate(GraphWriteMethods wg);

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

    private boolean finished = false;

    private Thread animationThread;

    /**
     * Handles the variables that persist for the life-cycle of the animation
     * and creates the animation thread.
     * @param graph 
     */
    private void run(final Graph graph) {
        if (GraphNode.getGraphNode(graph) != null) {
            
            // Set the VisualManager for the graph of this animation
            manager = GraphNode.getGraphNode(graph).getVisualManager();
            
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
                    if (lockGraphSafely(graph)){                    
                        reset(wg);
                        wg.commit();
                    }
                }    
            });
            animationThread.setName("Animation");
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
                final List<VisualChange> changes = animate(wg);
                wg.commit();

                // Notify the VisualManager of changes from the animation
                if (!changes.isEmpty() && manager != null) {
                    manager.addMultiChangeOperation(changes);
                }
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
     * Interrupt all running animations.
     */
    public static final synchronized void interruptAllAnimation() {
        runningAnimations.forEach(animation -> {
            animation.interrupt();
        });
        runningAnimations.clear();
    }
    
    /**
     * Interrupt this animations.
     */
    public void interrupt(){
        this.animationThread.interrupt();
    }
}
