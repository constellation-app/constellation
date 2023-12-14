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
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor;
import java.util.List;

/**
 * Base class for animations as well as static utilities for running animations.
 * <p>
 * The life-cycle of an animation is follows (this is a simplified version of
 * the logic that actually runs in an animation's thread):
 * <pre>
 * wg = graph.getWritableGraph()
 * animation.initialise(wg)
 * while (true) {
 *     animation.animate(wg)
 *     // wait for this long
 *     animation.getIntervalInMillis()
 * }
 * animation.reset(wg)
 * wg.commit()
 * </pre>
 * </p>
 * All animations run on their own thread, but at the moment only one animation
 * can be run at a time (this may be changed in the future if there is a
 * requirement).
 *
 * @author twilight_sparkle
 */
public abstract class Animation {

    private static Animation runningAnimation = null;

    /**
     * If there is a currently running animation, stop it.
     */
    public static final synchronized void stopAnimation() {
        if (runningAnimation != null) {
            runningAnimation.stop();
        }
        runningAnimation = null;
    }

    /**
     * Start the specified animation on the active graph. If an animation is
     * currently running, stop it fist.
     *
     * @param animation The animation to run
     */
    public static final synchronized void startAnimation(final Animation animation) {
        startAnimation(animation, GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Start the specified animation on the specified graph. If an animation is
     * currently running, stop it fist.
     *
     * @param animation The animation to run
     * @param graph The graph to run the animation on.
     */
    public static final synchronized void startAnimation(final Animation animation, final Graph graph) {
        stopAnimation();
        animation.run(graph);
        runningAnimation = animation;
    }

    /**
     * Initialise this animation.
     * <p>
     * This method is called prior to any calls to {@link #animate} and allows
     * the animation to store any data that it will use over the course of the
     * animation.
     *
     * @param wg A write lock on the graph to initialise the animation with.
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
     *
     * @return The interval between frames for this animation.
     */
    public abstract long getIntervalInMillis();

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

    private void run(final Graph graph) {
        if (GraphNode.getGraphNode(graph) != null) {
            final VisualManager manager = GraphNode.getGraphNode(graph).getVisualManager();
            animationThread = new Thread(() -> {
                WritableGraph wg;
                while (true) {
                    try {
                        wg = graph.getWritableGraph(getName(), isSignificant());
                        break;
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                try {
                    editGraph(manager, wg);
                } finally {
                    wg.commit();
                }
            });
            animationThread.setName("Animation");
            animationThread.start();
        }
    }

    private void editGraph(final VisualManager manager, WritableGraph wg) {
        initialise(wg);

        while (true) {
            final List<VisualChange> changes = animate(wg);
            if (!changes.isEmpty() && manager != null) {
                manager.addMultiChangeOperation(changes);
                wg = wg.flush(false);
            }
            if (finished) {
                reset(wg);
                break;
            }
            try {
                Thread.sleep(getIntervalInMillis());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                if (finished) {
                    reset(wg);
                    break;
                }
            }
        }
    }

    private void stop() {
        setFinished();
        if (animationThread != null) {
            animationThread.interrupt();
        }
    }
}
