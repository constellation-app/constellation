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
package au.gov.asd.tac.constellation.utilities.visual;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * A VisualProcessor is responsible for providing a visualisation of an object
 * satisfying the {@link VisualAccess} interface, and responding to updates
 * about that visualisation from a {@link VisualManager}.
 * <p>
 * A processor and its associated components typically contains most of the
 * graphical processing logic involved in visualising data. As a result, this
 * logic is kept separate from the rest of the visualisation framework. A
 * processor is free to 'visualise' however it sees fit; a text-only processor
 * would be possible. The only restriction is that the visualisation exist
 * inside some tangible AWT {@link Component} (which the application will put
 * inside a netbeans top component). CONSTELLATION's primary visual processor
 * implementation is a 3D renderer utilising openGL.
 * <p>
 * A processor is also responsible for processing {@link VisualChange} objects
 * sent to them from the associated {@link VisualManager}. It handles this in
 * the course of its update life-cycle which is as follows:
 * <ol>
 * <li> The manager calls {@link #update} with a list of changes and a visual
 * access.
 * </li><li> The processor gains a lock on the access by calling
 * {@link VisualAccess#beginUpdate} and processes each change, grabbing any
 * information it requires from the access.
 * </li><li> The processor unlocks the access by calling
 * {@link VisualAccess#endUpdate} and informs the manager that it has finished
 * processing the changes.
 * </li><li> The processor does the necessary graphical work to reflect the
 * processed changes by calling {@link #performVisualUpdate}.
 * </li><li> The processor informs the manager that it has finished updating
 * visually so that the manager can call {@link #update} again when necessary.
 * </ol>
 *
 * @author twilight_sparkle
 */
public abstract class VisualProcessor {

    protected boolean isInitialised = false;
    private VisualManager manager;
    private final Semaphore updateOccuring = new Semaphore(0);

    /**
     * Get the {@link Component} that this processor is using for its
     * visualisation.
     *
     * @return The {@link Component} that is being used for visualisation.
     */
    protected abstract Component getCanvas();

    /**
     * Destroy the Component that this processor is using for its visualisation.
     */
    protected abstract void destroyCanvas();

    /**
     * Perform the actual visual.
     * <p>
     * Note that this method has no access to the data being visualised - hence
     * the processor must have cached any required information during its change
     * processing phase.
     */
    protected abstract void performVisualUpdate();

    /**
     * Allows the processor to perform any implementation specific
     * initialisation (loading resources, external graphical code) etc.
     */
    protected abstract void initialise();

    /**
     * Allows the processor to perform any implementation specific to cleanup
     * like removing any held listeners etc.
     */
    protected abstract void cleanup();

    /**
     * Retrieve a minimal list of visual changes that when processed will cause
     * a complete redisplay of the object being visualised.
     *
     * @param access The visual access to the object being visualised.
     * @return
     */
    protected abstract List<VisualChange> getFullRefreshSet(final VisualAccess access);

    /**
     * Constructs a {@link VisualOperation} to export the current visualisation
     * to the specified image file.
     * <p>
     * Note: This operation need not be supported by implementations and may
     * throw an {@link UnsupportedOperationException}.
     *
     * @param imageFile The file to export to.
     * @return The {@link VisualOperation} to export to an image.
     */
    public abstract VisualOperation exportToImage(final File imageFile);

    /**
     * Constructs a {@link VisualOperation to export the current visualisation
     * to a BufferedImage.
     * <p>
     * Note: This operation need not be supported by implementations and may
     * throw an {@link UnsupportedOperationException}.
     *
     * @param img1 A single element array; the new BufferedImage is assigned to index 0.
     * @param waiter A Semaphore with no permits available; a permit is released
     * when the BufferedImage has been assigned.
     *
     * @return The {@link VisualOperation} to export to a BufferedImage.
     */
    public abstract VisualOperation exportToBufferedImage(final BufferedImage[] img1, final Semaphore waiter);

    /**
     * Allow this processor to start processing and updating based on visual
     * changes received from the supplied {@link VisualManager}.
     *
     * @param manager The {@link VisualManager} associated with this processor.
     */
    public final void startVisualising(final VisualManager manager) {
        this.manager = manager;
        isInitialised = true;
        initialise();
    }

    /**
     * Allow this processor to stop processing visual changes
     */
    public final void stopVisualising() {
        cleanup();
    }

    /**
     * Informs the manager that this processor is no longer doing any work and
     * hence can receive a new call to {@link #update}.
     * <p>
     * This should be called in the appropriate place in implementations of this
     * class, or in classes associated with implementations.
     */
    protected void signalProcessorIdle() {
        manager.signifyProcessorIdle();
    }

    /**
     * Informs the manager that this processor needs to recompute all visual
     * information.
     * <p>
     * This should be called in the appropriate place in implementations of this
     * class, or in classes associated with implementations.
     */
    protected void rebuild() {
        manager.refreshVisualProcessor();
    }

    /**
     * Perform one iteration of this processor's update life-cycle based on the
     * supplied changes.
     * <p>
     * Note that this should not be called explicitly; it is called
     * automatically inside the associated manager's life cycle.
     *
     * @param changes The list of visual changes to process
     * @param access The access to the object being visualised
     * @param indigenousChanges The
     * @param fullRefresh
     */
    final void update(final Collection<VisualChange> changes, final VisualAccess access, final boolean indigenousChanges, final boolean fullRefresh) {
        Thread.UncaughtExceptionHandler h = (Thread th, Throwable ex) -> {
            // if exception, try refreshing
            rebuild();
            
        };
        final Thread updateThread = new Thread(() -> {
            access.beginUpdate();
            try {
                if (indigenousChanges) {
                    changes.addAll(access.getIndigenousChanges());
                } else {
                    access.updateInternally();
                }
                if (fullRefresh) {
                    changes.addAll(getFullRefreshSet(access));
                }
                processChangeSet(changes, access);
            } finally {
                access.endUpdate();
            }
            updateOccuring.release();
            performVisualUpdate();
        });
        updateThread.setUncaughtExceptionHandler(h);
        updateThread.setName("Visual Processor");
        updateThread.start();
        updateOccuring.acquireUninterruptibly();
    }

    /**
     * An interface for processing a single {@link VisualChange} with regards to
     * a locked instance of a {@link VisualAccess}.
     * <p>
     * Implementations of a VisualProcessor usually contain a change processor
     * for each {@link VisualProperty} (sometimes a few properties share the one
     * processor). These processors should do all work depending on the visual
     * access synchronously, that is while the lock is valid.
     */
    @FunctionalInterface
    public static interface VisualChangeProcessor {

        /**
         * Process the given visual change using the given visual access.
         *
         * @param change
         * @param access
         */
        public void processChange(final VisualChange change, final VisualAccess access);
    }

    /**
     * Retrieves a set of visual properties that the given
     * {@link VisualProperty} trumps.
     * <p>
     * In each update cycle, any changes pertaining to trumped properties will
     * be ignored. Generally this is because processing the trumping change will
     * also perform the logic for processing the trumped change.
     *
     * @param property The {@link VisualProperty} to obtain the trumped
     * properties for.
     * @return The set of properties trumped by the supplied property.
     */
    @SuppressWarnings("unchecked")
    protected Set<VisualProperty> getTrumpedProperties(final VisualProperty property) {
        return Collections.emptySet();
    }

    /**
     * Retrieves the master property for the given {@link VisualProperty}.
     * <p>
     * The default behaviour for this method is to return the supplied property
     * itself. This can be overriden by implementations where properties share a
     * change processor to avoid duplicating code. For example, if a visual
     * processor needs to run the same code when either label sizes or colors
     * change, then it can return label sizes as the master property for both.
     * It is then not necessary to provide a change processor for label colors.
     *
     * @param property The {@link VisualProperty} to obtain the master property
     * for.
     * @return
     */
    protected VisualProperty getMasterProperty(final VisualProperty property) {
        return property;
    }

    /**
     * Retrieves the {@link VisualChangeProcessor} for a {@link VisualChange} to
     * the given {@link VisualProperty}.
     * <p>
     * This should return a change processor for any property that could be
     * returned from {@link #getMasterProperty} and requires this processor to
     * update visually. If a property doesn't require any visual updating to
     * occur (for example a black and white processor might ignore changes to
     * color properties) then this method should return a change processor that
     * does no work.
     *
     * @param property The {@link VisualProperty} to get the change processor
     * for.
     * @return a {@link VisualChangeProcessor} to perform the necessary
     * processing for a change to the specified property.
     */
    protected abstract VisualChangeProcessor getChangeProcessor(final VisualProperty property);

    private void processChangeSet(final Collection<VisualChange> changes, final VisualAccess access) {
        calculatePertinentChanges(changes).forEach((property, propertyChanges) -> {
            final VisualChangeProcessor processor = getChangeProcessor(property);
            propertyChanges.forEach(change -> processor.processChange(change, access));
        });
    }

    private Map<VisualProperty, List<VisualChange>> calculatePertinentChanges(final Collection<VisualChange> changes) {
        final Map<VisualProperty, List<VisualChange>> masterChangeMap = new HashMap<>();
        changes.forEach(change -> {
            if (!masterChangeMap.containsKey(getMasterProperty(change.property))) {
                masterChangeMap.put(getMasterProperty(change.property), new ArrayList<>());
            }
            final List<VisualChange> currentChanges = masterChangeMap.get(getMasterProperty(change.property));
            boolean equivilantChangePresent = false;
            for (final VisualChange currentChange : currentChanges) {
                if (currentChange.hasSameChangeList(change)) {
                    equivilantChangePresent = true;
                    break;
                }
            }
            if (!equivilantChangePresent) {
                currentChanges.add(change);
            }
        });

        final Set<VisualProperty> propertySet = new HashSet<>(masterChangeMap.keySet());
        propertySet.forEach(property -> getTrumpedProperties(property).forEach(trumpedProperty -> masterChangeMap.remove(trumpedProperty)));
        return masterChangeMap;
    }

}
