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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for animations.
 *
 * The flow of an animation is as follows: 
 * <ol><li> Animations initialize themselves by ensuring that the graph is in a state that the animation can modify.
 * Animations may check for specific attributes or node and transaction quantities. </li>
 * <li> Animations run in frames. With each frame obtaining a graph lock modifying the graph and releasing 
 * its lock to then wait a predefined amount of time before the next frame. </li>
 * <li> When an animation finishes, It may reset the graph to a particular state or trigger another animation. 
 * </li></ol>
 * All animations run on their own thread, enabling concurrent animations.
 * Animations may be disabled by an application option. In these cases animations with a finite number of 
 * steps can simply skip and perform the required graph modification in a single frame.
 * 
 * @author twilight_sparkle
 * @author capricornunicorn123
 */
public abstract class Animation {

    private WritableGraph wg;
    public String graphID;
    private boolean finished = false;
    private Thread animationThread;
    private static final Logger LOGGER = Logger.getLogger(Animation.class.getName());
    
    public void setGraphID(final String graphID){
        this.graphID = graphID;
    }

    /**
     * Initialize this animation.
     * This method is called prior to any calls to {@link #animate} and allows
     * the animation to store any data that it will use over the course of the
     * animation. This method may also store initial state data for resetting 
     * after the animation.
     *
     * @param wg A write lock on the graph to initialize the animation with.
     */
    public abstract void initialise(final GraphWriteMethods wg);

    /**
     * Run one frame of the animation.
     * This method should modify the graph as necessary writing any changes 
     * to the provided grapj.
     *
     * @param wg A write lock on the graph the animation is running on.
     * @return
     */
    public abstract void animate(final GraphWriteMethods wg);

    /**
     * Reset any ephemeral changes made by the animation.
     * This method is called after all calls to {@link #animate} and allows the
     * animation to clean up any changes it made to the graph that should not
     * persist after the animation has concluded.
     *
     * @param wg A write lock on the graph to reset the animation on.
     */
    public abstract void reset(final GraphWriteMethods wg);

    /**
     * Get the interval in milliseconds between each frame of the animation.
     * Note: Standard film frame rate is 24 fps which equates to 41.6 milliseconds per frame  
     *
     * @return The interval between frames for this animation.
     */
    public long getIntervalInMillis() {
        return 40;
    }

    /**
     * Get the name of this animation.
     * This name will be used to retrieve the write lock on the graph, and
     * consequentially if this animation is also significant, the name by which
     * the result of the animation can be undone/redone.
     *
     * @return The name of this animation.
     */
    protected abstract String getName();

    /**
     * Get whether or not this animation is 'significant'.
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
     * This method is called by {@link #animate} in implementations with a
     * finite number of frames.
     */
    protected final void setFinished() {
        finished = true;
    }

    /**
     * Handles the variables that persist for the life-cycle of the animation
     * and creates the animation thread.
     * 
     * @param graphId
     */
    public void run(final String graphId) {
        final GraphNode gn = GraphNode.getGraphNode(graphId);
        
        if (GraphNode.getGraphNode(graphId) != null) {
            this.graphID = graphId;
            final Graph graph = gn.getGraph();
            // Create the Thread for this animaton
            animationThread = new Thread(() -> {
                try {
                    if (lockGraphSafely(graph)) {
                        initialise(wg);
                        wg.commit();
                        editGraph(graph);
                    } 
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.INFO, String.format("Animation %s was interrupted", this.getName()));
                } finally {
                    if (lockGraphSafely(graph)) {                    
                        reset(wg);
                        wg.commit();
                    }
                    AnimationUtilities.notifyComplete(this);
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
    private void editGraph(final Graph graph) throws InterruptedException {
        while (!finished) {
            
            if (!AnimationUtilities.isGraphAnimationsPaused(this.graphID)){    
                // Animate a frame
                if (lockGraphSafely(graph)) {
                    animate(wg);
                    wg.commit();
                }
            }
            
            // Sleep until it is time for the next frame
            Thread.sleep(getIntervalInMillis());

        }
    }
    
    /**
     * Attempts to put a lock on the graph. 
     * If a lock on the graph has been applied, will return true.
     * @param graph
     * @return 
     */
    private boolean lockGraphSafely(final Graph graph) {
        try {
            wg = graph.getWritableGraph(getName(), isSignificant());
            return true;
        } catch (final InterruptedException ex) {
            return false;
        }
    }

    protected void stop() {
        setFinished();
    }
    
    /**
     * Interrupt this animation.
     */
    public void interrupt() {
        this.animationThread.interrupt();
    }

    /**
     * This method enables finite animations to execute when animations have been disabled.
     * Essentially the animation occurs in one frame. 
     * @param graphId 
     */
    public void skip(final String graphId) {
        final GraphNode gn = GraphNode.getGraphNode(graphId);
        
        if (gn != null) {
            final Graph graph = gn.getGraph();
            
            if (lockGraphSafely(graph)) {
                initialise(wg);
                setFinalFrame(wg);
                wg.commit();
            }
        }
    };
    
    public abstract void setFinalFrame(final GraphWriteMethods wg);
}
