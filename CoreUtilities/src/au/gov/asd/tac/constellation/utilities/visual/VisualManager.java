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

import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import au.gov.asd.tac.constellation.utilities.threadpool.ConstellationGlobalThreadPool;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.ref.Cleaner;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * The {@link VisualManager} class handles all interaction between components that generate visual change information
 * about an object satisfying the {@link VisualAccess} interface (usually a graph) and the {@link VisualProcessor} that
 * is visualising that object.
 * <p>
 * For each object satisfying {@link VisualAccess} interface, there should be exactly one processor and one manager
 * object. However multiple clients may simultaneously interact with the manager to inform it of visual changes.
 * <p>
 * The bulk of the managers work in relaying visual information from clients to the processor is done inside a
 * processing thread, whose life-cycle is as follows:
 * <ol>
 * <li> The manager receives and processes a {@link VisualOperation}, calling {@link VisualOperation#apply} and then
 * adding all of the operations changes to a set.
 * </li><li> The manager repeats step 1, accumulating visual changes, until the processor informs it that it is idle
 * (has finished any visual updating it was doing).
 * </li><li> The manager calls {@link VisualProcessor#update} with the accumulated set of changes and the visual access,
 * waiting until the processor has finished processing them.
 * </li><li> The manager clears the aggregated list of changes and goes back to step one to process more operations.
 * Note that while the processor has finished processing the changes, it may still be performing its visual update.
 * </li>
 * </ol>
 *
 * @see VisualProcessor
 * @see VisualAccess
 * @author twilight_sparkle
 */
public final class VisualManager {

    private static int threadIdIndex = 0;
    private final VisualAccess access;
    private final VisualProcessor processor;
    private final PriorityBlockingQueue<VisualOperation> operationQueue = new PriorityBlockingQueue<>();
    private final ExecutorService executorService = ConstellationGlobalThreadPool.getThreadPool().getFixedThreadPool(("GraphThread-" + (threadIdIndex++)), 1);
    private CompletableFuture<Void> processingFuture;
    private boolean isProcessing = false;
    private boolean rendererIdle = true;
    private boolean indigenousChanges = false;
    private boolean refreshProcessor = false;

    protected final Runnable processTask = () -> process();

    // For cleaning up object for garbage collection. Replaced finalize
    private static final Cleaner cleaner = Cleaner.create();
    private static final Runnable cleanupAction = () -> MemoryManager.finalizeObject(VisualManager.class);

    /**
     * Construct a VisualManager to delegate between the supplied {@link VisualAccess} and {@link VisualProcessor}.
     *
     * @param access The object being visualised.
     * @param processor The processor carrying out the visualisation.
     */
    public VisualManager(final VisualAccess access, final VisualProcessor processor) {
        this.access = access;
        this.processor = processor;
        MemoryManager.newObject(VisualManager.class);
        cleaner.register(this, cleanupAction);
    }

    /**
     * Get the {@link Component} that this manager's corresponding processor is using for its visualisation.
     *
     * @return The {@link Component} that the {@link VisualProcessor} is using.
     */
    public Component getVisualComponent() {
        return processor.getCanvas();
    }

    /**
     * Destroy the Component that this manager's corresponding processor is using for its visualisation.
     * <p>
     * This is called when the application knows that it no longer wants visualisation to occur via this
     * manager-processor pair.
     */
    public void destroy() {
        processor.stopVisualising();
        processor.destroyCanvas();
    }

    /**
     * Begins the processing life-cycle of this manager. The results of enqueued visual operations will be reflected in
     * the associated {@link VisualProcessor}.
     */
    public final void startProcessing() {
        if (!isProcessing()) {
            processingFuture = CompletableFuture.runAsync(processTask, executorService);
            isProcessing = true;
            rendererIdle = true;
        }
    }

    public final void process() {
        while (isProcessing()) {
            final NavigableSet<VisualChange> changes = new TreeSet<>();
            // Loops after each operation is available, and waits on .take()
            // Depending on the operation, will perform a different type of update.
            // Breaks the while loop to notify the processor to update.
            while (true) {
                try {
                    final VisualOperation operation = getOperations().take();
                    if (operation == signifyProcessorIdleOperation) {
                        rendererIdle = true;
                    } else if (operation == indigenousChangesUpdateOperation) {
                        indigenousChanges = true;
                    } else if (operation == refreshProcessorOperation) {
                        refreshProcessor = true;
                    } else {
                        operation.apply();
                    }
                    changes.addAll(operation.getVisualChanges());
                    if (isRendererIdle() && !changes.isEmpty()) {
                        rendererIdle = false;
                        break;
                    }
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    if (!isProcessing()) {
                        break;
                    }
                }
            }
            if (isProcessing()) {
                // this call blocks until the updating is done
                getProcessor().update(changes, getAccess(), isIndigenousChanges(), isRefreshProcessor());
                indigenousChanges = false;
                refreshProcessor = false;
                changes.clear();
            }
        }
    }

    /**
     * Stops the processing life-cycle of this manager. The {@link VisualProcessor} will no longer reflect any more
     * visual updates.
     */
    public final void stopProcessing() {
        isProcessing = false;
        cancelProcessing(true);
    }

    protected final void cancelProcessing(final boolean cancel) {
        if (processingFuture != null) {
            processingFuture.cancel(cancel);
        }
    }

    protected final boolean isProcessing() {
        return isProcessing;
    }

    public final boolean isRefreshProcessor() {
        return refreshProcessor;
    }

    protected final boolean isIndigenousChanges() {
        return indigenousChanges;
    }

    public final boolean isRendererIdle() {
        return rendererIdle;
    }

    protected PriorityBlockingQueue<VisualOperation> getOperations() {
        return operationQueue;
    }

    protected VisualProcessor getProcessor() {
        return processor;
    }

    public CompletableFuture<Void> getProcessingFuture() {
        return processingFuture;
    }

    protected VisualAccess getAccess() {
        return access;
    }

    /**
     * Add a {@link VisualOperation} to this manager's queue of operations to be processed.
     * <p>
     * The most common use case for adding a {@link VisualOperation} is to inform the manager of one or more
     * {@link VisualChange} objects. Such an operation can be created by {@link #constructSingleChangeOperation} or
     * {@link #constructMultiChangeOperation}.
     *
     * @param operation A {@link VisualOperation} to add.
     */
    public void addOperation(final VisualOperation operation) {
        operationQueue.add(operation);
    }

    /**
     * Request the visual processor to export its visualisation to the specified image.
     * <p>
     * Note: The {@link VisualProcessor} need not support this operation and may throw an
     * {@link UnsupportedOperationException}.
     * <p>
     * This is achieved by queuing a processor specific {@link VisualOperation} to export to an image, which will be
     * processed in the next iteration of this manager's life-cycle.
     *
     * @param imageFile The image file to export to.
     */
    public void exportToImage(final File imageFile) {
        addOperation(processor.exportToImage(imageFile));
    }

    /**
     * Request the visual processor to export its visualisation to the specified BufferedImage.
     * <p>
     * Note: The {@link VisualProcessor} need not support this operation and may throw an
     * {@link UnsupportedOperationException}.
     * <p>
     * This is achieved by queuing a processor specific {@link VisualOperation} to export to an image, which will be
     * processed in the next iteration of this manager's life-cycle.
     *
     * @param img1 A single element array; the new BufferedImage is assigned to index 0.
     * @param waiter A Semaphore with no permits available; a permit is released when the BufferedImage has been
     * assigned.
     */
    public void exportToBufferedImage(final BufferedImage[] img1, final Semaphore waiter) {
        addOperation(processor.exportToBufferedImage(img1, waiter));
    }

    /**
     * Requires the associated {@link VisualProcessor} to completely recompute its visualisation.
     * <p>
     * This is typically used by visual processors themselves when one of the components on which the processor is
     * drawing is resized or becomes invalid. Both when it is called and what needs to be recomputed is implementation
     * specific.
     */
    public void refreshVisualProcessor() {
        addOperation(refreshProcessorOperation);
    }

    /**
     * Informs the {@link VisualProcessor} that the {@link VisualAccess} has indigenous changes, ie. its data has
     * changed in a way that will affect its visualisation, but no other component will be informing this manager of the
     * fact.
     * <p>
     * This is typically called by the object that is doing change detection on the data underlying the
     * {@link VisualAccess}.
     */
    public void updateFromIndigenousChanges() {
        addOperation(indigenousChangesUpdateOperation);
    }

    protected void signifyProcessorIdle() {
        addOperation(signifyProcessorIdleOperation);
    }

    /**
     * Construct a {@link VisualOperation} to notify the manager of the specified {@link VisualChange}. This operation
     * can be added to this manager at any time.
     *
     * @param change The {@link VisualChange} that has occurred.
     * @return A {@link VisualOperation} to inform of the specified change.
     */
    public final VisualOperation constructSingleChangeOperation(final VisualChange change) {
        return () -> Arrays.asList(change);
    }

    /**
     * Construct a {@link VisualOperation} to notify the manager of multiple {@link VisualChange} objects. This
     * operation can be added to this manager at any time.
     *
     * @param changes The list of changes that have occurred.
     * @return A {@link VisualOperation} to inform of the specified changes.
     */
    public final VisualOperation constructMultiChangeOperation(final List<VisualChange> changes) {
        return () -> changes;
    }

    /**
     * Construct and enqueue a {@link VisualOperation} to notify the manager of the specified {@link VisualChange}.
     *
     * @param change The {@link VisualChange} that has occurred.
     */
    public final void addSingleChangeOperation(final VisualChange change) {
        addOperation(constructSingleChangeOperation(change));
    }

    /**
     * Construct and enqueue a {@link VisualOperation} to notify the manager of multiple {@link VisualChange} object.
     *
     * @param changes The list of changes that have occurred.
     */
    public final void addMultiChangeOperation(final List<VisualChange> changes) {
        addOperation(constructMultiChangeOperation(changes));
    }

    protected final VisualOperation signifyProcessorIdleOperation = new VisualOperation() {

        @Override
        public int getPriority() {
            return VisualPriority.SIGNIFY_IDLE_PRIORITY.getValue();
        }

        @Override
        public List<VisualChange> getVisualChanges() {
            return Collections.emptyList();
        }
    };

    protected final VisualOperation refreshProcessorOperation = new VisualOperation() {

        @Override
        public int getPriority() {
            return VisualPriority.REFRESH_PRIORITY.getValue();
        }

        @Override
        public List<VisualChange> getVisualChanges() {
            return Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE).build());
        }
    };

    protected final VisualOperation indigenousChangesUpdateOperation
            = () -> Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE).build());
}
