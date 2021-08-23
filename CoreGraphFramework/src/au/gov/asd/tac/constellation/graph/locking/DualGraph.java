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
package au.gov.asd.tac.constellation.graph.locking;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import javax.swing.undo.UndoManager;

/**
 * A DualGraph manages a pair of graph objects to improve read-lock performance.
 * <p>
 * In its resting state, both graphs will be in an identical state.
 * <p>
 * One graph is designated the reading graph and may have one or more threads
 * currently holding a read lock on this graph. These threads have permission to
 * read from the reading graph while they hold the read lock. There should be no
 * threads holding a write lock on the reading graph.
 * <p>
 * The other graph is designated the writing graph and may have a single thread
 * holding the write lock on this graph. This thread has permission to modify
 * the writing graph. While this thread is modifying the writing graph, none of
 * the reading threads will see any changes as they are all accessing the
 * reading graph. When the writing thread is finished modifying the writing
 * graph, it can choose to either commit its changes, or rollback.
 * <p>
 * If the writing thread chooses to commit its changes, a sequence of events
 * occurs which results in both graphs arriving at an identical state.
 * <ol>
 * <li>The dual graph stops granting read locks on the reading graph and instead
 * grants read locks on the writing graph. This means that new read locks see
 * the changes made by the writing thread.
 * <li>The dual graph waits until all existing read locks on the reading graph
 * have been released.
 * <li>The dual graph swaps the graphs so that the reading graph is now the
 * writing graph and vice versa.
 * <li>The dual graph now grants a write lock on the new writing graph to the
 * next waiting thread.
 * </ol>
 * <p>
 * If the writing thread chooses to roll back its changes then the writing graph
 * is reverted back to the state of the reading graph. In this case the graphs
 * do not need to be swapped and a new write lock can be granted on the original
 * writing graph.
 *
 * @author sirius
 */
public class DualGraph implements Graph, Serializable {

    private static final Logger LOGGER = Logger.getLogger(DualGraph.class.getName());

    private final ArrayList<GraphChangeListener> graphChangeListeners = new ArrayList<>();
    private final LockingStoreGraph a;
    private final LockingStoreGraph b;
    private final LockingManager<LockingStoreGraph> lockingManager;
    private final String id;
    private GraphChangeEvent previousEvent = null;
    private final Schema schema;

    private LockingManager<LockingStoreGraph> createLockingManager() {
        return new LockingManager<LockingStoreGraph>() {
            @Override
            protected void update(final Object description, final Object editor) {
                final GraphChangeEvent event = new GraphChangeEvent(previousEvent, DualGraph.this, editor, description);
                previousEvent = event;
                SwingUtilities.invokeLater(() -> {
                    synchronized (graphChangeListeners) {
                        for (final GraphChangeListener listener : graphChangeListeners) {
                            listener.graphChanged(event);
                        }
                    }
                });
            }
        };
    }

    public DualGraph(final Schema schema) {

        lockingManager = createLockingManager();

        a = new LockingStoreGraph(lockingManager, 0, schema);
        b = new LockingStoreGraph(lockingManager, 1, schema, a.getId());

        lockingManager.setTargets(a, b);

        this.id = a.getId();

        this.schema = schema;

        MemoryManager.newObject(DualGraph.class);
    }

    public DualGraph(final Schema schema, final StoreGraph target) {
        this(schema, target, false);
    }

    /**
     * Creates a new DualGraph from a target StoreGraph. The new DualGraph gets
     * a copy of the target's schema.
     *
     * @param target the StoreGraph representing the initial state of the
     * DualGraph.
     * @param newId if true then the DualGraph gets a new id, otherwise it is
     * copied from the target.
     */
    public DualGraph(final StoreGraph target, final boolean newId) {
        this(target.getSchema(), target, newId);
    }

    public DualGraph(final Schema schema, final StoreGraph target, final boolean newId) {

        target.validateKeys();

        lockingManager = createLockingManager();

        a = new LockingStoreGraph(lockingManager, 0, target, newId);
        b = new LockingStoreGraph(lockingManager, 1, target, a.getId());

        lockingManager.setTargets(a, b);

        this.id = a.getId();

        this.schema = schema == null ? null : schema.getFactory().createSchema();

        MemoryManager.newObject(DualGraph.class);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            MemoryManager.finalizeObject(DualGraph.class);
        } finally {
            super.finalize();
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public void addGraphChangeListener(final GraphChangeListener listener) {
        synchronized (graphChangeListeners) {
            if (listener != null && !graphChangeListeners.contains(listener)) {
                graphChangeListeners.add(listener);
            }
        }
        LOGGER.log(Level.FINE, "Added GraphChangeListener, count is {0}", graphChangeListeners.size());
    }

    @Override
    public void removeGraphChangeListener(final GraphChangeListener listener) {
        synchronized (graphChangeListeners) {
            graphChangeListeners.remove(listener);
        }
        LOGGER.log(Level.FINE, "Removed GraphChangeListener, count is {0}\nListeners are: {1}", new Object[]{graphChangeListeners.size(), graphChangeListeners});
    }

    @Override
    public ReadableGraph getReadableGraph() {
        return lockingManager.startReading();
    }

    @Override
    public WritableGraph getWritableGraph(final String name, final boolean significant) throws InterruptedException {
        return getWritableGraph(name, significant, null);
    }

    @Override
    public WritableGraph getWritableGraph(final String name, final boolean significant, final Object editor) throws InterruptedException {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Attempting to write on the EDT");
        }
        if (Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Attempting to write on the JavaFX Application Thread");
        }
        return lockingManager.startWriting(name, significant, editor);
    }

    @Override
    public WritableGraph getWritableGraphNow(final String name, final boolean significant) {
        return getWritableGraphNow(name, significant, null);
    }

    @Override
    public WritableGraph getWritableGraphNow(final String name, final boolean significant, final Object editor) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Attempting to write on the EDT");
        }

        return lockingManager.tryStartWriting(name, significant, editor);
    }

    @Override
    public void setUndoManager(final UndoManager undoManager) {
        lockingManager.setUndoManager(undoManager);
    }
}
