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
package au.gov.asd.tac.constellation.plugins.update;

import java.util.TreeSet;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An UpdateController is a pattern to gather and synchronously process update
 * requests coming from a variety of sources. In practice, this means updating a
 * CONSTELLATION view based on changes to its model arising from user
 * interaction with the view, and the changes to the current graph.
 * <p>
 * An UpdateController delegates the actual work to a number of
 * {@link UpdateComponent update components}, each of which are responsible for
 * performing a specific update tasks.
 * <p>
 * The typical workflow is to call
 * <code>{@link UpdateController#registerChange registerChange}</code> whenever
 * an update component needs to respond to a change, and then call
 * <code>{@link UpdateController#update update}</code> to cause the controller
 * to propagate all changes that have been registered. Note that
 * UpdateComponents can have dependencies, so registering a change for a parent
 * component will cause all descendants of that component to be updated when in
 * the update cycle.
 *
 * @param <U> The type of update state objects used by this controller to inform
 * the UpdateComponents it manages. Often an explicit type is not used for the
 * controller, but it is important to note that the type parameter of any
 * registered UpdateComponents must be subtypes of this parameter.
 * @see UpdateComponent
 * @see GraphUpdateController
 * @author sirius
 */
public class UpdateController<U> {

    private static final boolean VERBOSE = false;

    private TreeSet<UpdateComponent<U>> componentsToUpdate = new TreeSet<>();
    private Semaphore lock = new Semaphore(1);
    private U updateState;

    private static final Logger LOGGER = Logger.getLogger(UpdateController.class.getName());

    /**
     * Registers a change for the given UpdateComponent. This will allow the
     * UpdateComponent to perform an update the next time the
     * <code>update</code> method is called. The update controller is locked
     * while this change is being registered.
     *
     * @param component The UpdateComponent that needs to respond to the change
     */
    public void registerChange(UpdateComponent<U> component) {
        registerChange(component, true);
    }

    /**
     * Registers a change for the given UpdateComponent. This will allow the
     * UpdateComponent to perform an update the next time the
     * <code>update</code> method is called.
     *
     * @param component The UpdateComponent that needs to respond to the change
     * @param lock Whether or not to lock the update controller so that the
     * update cycle can't begin while this change is being registered.
     */
    public void registerChange(UpdateComponent<U> component, boolean lock) {
        if (lock) {
            lock();
        }

        componentsToUpdate.add(component);

        if (VERBOSE) {
            LOGGER.log(Level.INFO, "register change: {0}", component);
        }

        if (lock) {
            release();
        }
    }

    /**
     * Locks the update component so that no update cycles can begin (and no
     * changes can be registered that require locking).
     * <p>
     * <code>update</code> will call this method automatically. If the component
     * is currently in an update cycle, a lock will not be acquired until after
     * it has finished. It can also be useful to call it when you are manually
     * manipulating some data in a view that might also be used by an
     * UpdateComponent.
     *
     */
    public void lock() {
        if (VERBOSE) {
            LOGGER.info("lock()");
        }
        lock.acquireUninterruptibly();
    }

    /**
     * Unlocks the update component so that changes can be registered or
     * processed.
     * <p>
     * <code>update</code> will call this method automatically. If the component
     * is currently in an update cycle, the lock will be released after it has
     * finished.
     */
    public void release() {
        lock.release();
        if (VERBOSE) {
            LOGGER.info("released()");
        }
    }

    /**
     * Start the update cycle of this controller. This will update any
     * UpdateComponents that have had changes registered since the last update
     * cycle, as well as any descendants of those UpdateComponents.
     * <p>
     * For the duration of the update cycle, no other changes can be registered
     * or processed by this controller.
     */
    public void update() {
        update(null);
    }

    /**
     * Start the update cycle of this controller. This will update any
     * UpdateComponents that have had changes registered since the last update
     * cycle, as well as any descendants of those UpdateComponents.
     * <p>
     * For the duration of the update cycle, no other changes can be registered
     * or processed by this controller.
     *
     * @param updateState An object providing extra information to the
     * UpdateComponents about the update.
     */
    public void update(U updateState) {
        lock();
        this.updateState = updateState;
        updater.run();
    }

    /**
     * A Runnable which performs the updating logic for this UpdateController.
     */
    private final Runnable updater = new Runnable() {

        private UpdateComponent<U> currentComponent = null;

        @Override
        public void run() {
            if (currentComponent != null) {
                try {
                    processCurrentComponent();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    release();
                    return;
                }
            }
            while (!componentsToUpdate.isEmpty()) {
                currentComponent = componentsToUpdate.first();
                if (currentComponent.updateThread(this)) {
                    return;
                }
                try {
                    processCurrentComponent();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    release();
                    return;
                }
            }
            updateState = null;
            release();
        }

        private void processCurrentComponent() throws Exception {
            componentsToUpdate.remove(currentComponent);
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "updating: {0}", currentComponent);
            }
            if (currentComponent.update(updateState)) {
                for (UpdateComponent<U> child : currentComponent.children) {
                    if (VERBOSE) {
                        LOGGER.log(Level.INFO, "\tchanged: {0}", child);
                    }
                    componentsToUpdate.add(child);
                }
            }
            currentComponent = null;
        }
    };

}
