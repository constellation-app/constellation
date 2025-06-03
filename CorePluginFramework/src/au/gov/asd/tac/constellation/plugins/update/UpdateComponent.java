/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.update;

import java.util.ArrayList;
import java.util.List;

/**
 * An UpdateComponent is a pattern for responding to a specific type of update
 * in the context of a large collection of updates that are managed by an
 * {@link UpdateController}.
 * <p>
 * The <code>{@link UpdateComponent#update update}</code> method should be
 * implemented to contain the actual logic of handling the update. This method
 * will be called when an {@link UpdateController} with which this component (or
 * an ancestor of this component) has registered a change, executes its
 * <code>{@link UpdateController#update update}</code> method.
 * <p>
 * The following example shows how to use an UpdateController with two
 * UpdateComponents that expect GraphReadMethods objects to do updating:
 *
 * <pre><code>
 *      UpdateController controller = new UpdateController();
 *      UpdateComponent&lt;GraphReadMethods&gt; updateComponent1 = new UpdateComponent&lt;GraphReadMethods&gt;() {
 *          &#62;Override
 *          protected boolean update(GraphReadMethods graph) {
 *              doSomething();
 *          }
 *      };
 *      UpdateComponent2&lt;GraphReadMethods&gt; updateComponent2 = new UpdateComponent&lt;GraphReadMethods&gt;() {
 *          &#64;Override
 *          protected boolean update(GraphReadMethods graph) {
 *              doSomethingWithGraph(graph);
 *          }
 *      };
 *
 *      // Make the 1st component the parent of the second
 *      updateComponent2.dependOn(updateComponent1);
 *      // Register that a change has occurred meaning that the 1st component needs a change to update
 *      controller.registerChange(updateComponent1);
 *      // Enter the update cycle with the supplied graph
 *      // the first component will execute doSomething()
 *      // followed by the second component executing doSomethingWithGraph(graph)
 *      controller.update(graph);
 * </code></pre>
 *
 * While it is not enforced that UpdateComponents only register changes with a
 * single UpdateController, this is usually advisable.
 *
 * @param <U> The type of update state objects used by this component to receive
 * extra information from an UpdateController. Must be a subtype of the type
 * parameter for the UpdateController that it registers changes with.
 * @see UpdateController
 * @see GraphUpdateController
 * @author sirius
 */
public abstract class UpdateComponent<U> implements Comparable<UpdateComponent<U>> {

    private static int nextID = 0;

    private static synchronized int getNextId() {
        return nextID++;
    }

    private final String name;
    private final int stage;
    private int position = 0;
    private final int id = getNextId();

    List<UpdateComponent<U>> parents = new ArrayList<>();
    List<UpdateComponent<U>> children = new ArrayList<>();

    /**
     * Creates a new UpdateComponent.
     */
    protected UpdateComponent() {
        this(null, 0);
    }

    /**
     * Creates a new UpdateComponent with the given name.
     *
     * @param name A String representing the name of this UpdateComponent.
     */
    protected UpdateComponent(String name) {
        this(name, 0);
    }

    /**
     * Creates a new UpdateComponent component to be processed at the specified
     * stage.
     *
     * @param stage The stage at which to process this UpdateComponent in an
     * UpdateController's update cycle. This component will be guaranteed to be
     * processed after all UpdateComponents in earlier stages.
     */
    protected UpdateComponent(int stage) {
        this(null, stage);
    }

    /**
     * Creates a new UpdateComponent component with the given name to be
     * processed at the specified stage.
     *
     * @param name A String representing the name of this UpdateComponent.
     * @param stage The stage at which to process this UpdateComponent in an
     * UpdateController's update cycle. This component will be guaranteed to be
     * processed after all UpdateComponents in earlier stages.
     */
    protected UpdateComponent(String name, int stage) {
        this.name = name;
        this.stage = stage;
    }

    /**
     * Set this UpdateComponent to depend upon another UpdateComponent. This
     * will cause this UpdateComponent to be updated whenever the other
     * UpdateComponent (or any of its ancestors) have had changes registered
     * with an UpdateController.
     * <p>
     * An UpdateComponent is guaranteed to update after a component it depends
     * upon, unless the component it depends upon belongs to a strictly later
     * stage of the update cycle.
     *
     * @param parent The UpdateComponent to depend upon.
     */
    public void dependOn(UpdateComponent<U> parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
            parent.children.add(this);
            ensureIdAfter(parent.position);
        }
    }

    private void ensureIdAfter(int parentPosition) {
        if (position < parentPosition) {
            position = parentPosition + 1;
            for (UpdateComponent<U> child : children) {
                child.ensureIdAfter(position);
            }
        }
    }

    /**
     * Compares this UpdateComponent to the other specified component. This is
     * used to determine the order in which components should update.
     * <p>
     * Components in an earlier stage always update first, if two components are
     * in the same stage but related the ancestor will update first, and finally
     * if two components are in the same stage and unrelated, whichever was
     * created first will run first.
     *
     * @param o The other UpdateComponent to compare against.
     * @return An int indicating which is positive if this component runs after
     * the other component, negative if this component runs before the other
     * component, and zero only if they are the same component.
     */
    @Override
    public int compareTo(UpdateComponent<U> o) {
        int c = Integer.compare(stage, o.stage);
        if (c != 0) {
            return c;
        }

        c = Integer.compare(position, o.position);
        if (c != 0) {
            return c;
        }

        return Integer.compare(id, o.id);
    }

    /**
     * Responds to an update on a specific thread if required.
     * <p>
     * This method should be overriden if there is a need for this
     * UpdateComponent to respond to an update on a specific thread (for example
     * if it is interacting with swing or javafx). Typically it should just run
     * the passed in Runnable in the desired thread.
     * <p>
     * Note that the runnable will update all subsequent components as well as
     * this one, so all subsequent updates will occur on this thread unless
     * another component implements this method and specifies a different
     * thread.
     *
     * @param runnable A runnable which will update this component and all
     * components which are to be updated subsequently.
     * @return True if the runnable has been run on a specific thread, False if
     * no action was taken.
     */
    protected boolean updateThread(Runnable runnable) {
        return false;
    }

    /**
     * Respond to an update, described by the specified state object.
     * <p>
     * This method should be implemented to contain the actual logic of handling
     * the update.
     *
     * @param updateState An object representing state information about the
     * update that needs to occur.
     * @return true if this component needed to change anything as a result of
     * the update, False if no action was required.
     */
    public abstract boolean update(U updateState);

    @Override
    public String toString() {
        String orderString = " (" + stage + ", " + position + ", " + id + ")";
        if (name == null) {
            return super.toString() + orderString;
        } else {
            return name + orderString;
        }
    }
}
