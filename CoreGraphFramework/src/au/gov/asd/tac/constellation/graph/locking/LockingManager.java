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

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.undo.UndoGraphEdit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * The LockingManager manages the locking and unlocking of a graph in response
 * to requests for read and/or write access by plugins.
 *
 * @author sirius
 * @param <T>
 */
public class LockingManager<T extends LockingTarget> implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(LockingManager.class.getName());

    public static final boolean VERBOSE = false;
    private final ReentrantLock globalWriteLock = new ReentrantLock(true);
    private Context a;
    private Context b;
    private volatile Context readContext;
    private volatile Context writeContext;
    private LockingEdit currentEdit = null;
    private LockingEdit initialEdit = null;
    private UndoManager undoManager;

    public void setTargets(final T targetA, final T targetB) {
        a = readContext = new Context(targetA);
        b = writeContext = new Context(targetB);
        readContext.target.lock = readContext.lock.readLock();
        writeContext.target.lock = writeContext.lock.readLock();
    }

    public final void setUndoManager(final UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    private final class Context {

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
        private final T target;

        public Context(final T target) {
            this.target = target;
        }
    }

    public T startWriting(final String name, final boolean significant, final Object source) throws InterruptedException {
        if (a.lock.getReadHoldCount() > 0 || b.lock.getReadHoldCount() > 0) {
            throw new IllegalMonitorStateException("attempting to write while reading");
        }

        globalWriteLock.lockInterruptibly();
        if (currentEdit == null) {
            currentEdit = new LockingEdit(name, significant, source);
            initialEdit = currentEdit;
        } else {
            LockingEdit childEdit = new LockingEdit(name, significant, source);
            childEdit.parent = currentEdit;
            currentEdit = childEdit;
        }

        writeContext.target.setGraphEdit(currentEdit.graphEdit);
        currentEdit.setModificationCounter(writeContext.target.getModificationCounter());

        if (VERBOSE) {
            final String log = String.format("Write lock acquired for " + name + " by " + Thread.currentThread());
            LOGGER.log(Level.INFO, log);
        }

        return writeContext.target;
    }

    public T tryStartWriting(final String name, final boolean significant, final Object source) {

        if (a.lock.getReadHoldCount() > 0 || b.lock.getReadHoldCount() > 0) {
            throw new IllegalMonitorStateException("attempting to write while reading");
        }

        try {
            if (globalWriteLock.tryLock(0, TimeUnit.SECONDS)) {
                if (currentEdit == null) {
                    currentEdit = new LockingEdit(name, significant, source);
                    initialEdit = currentEdit;
                } else {
                    LockingEdit childEdit = new LockingEdit(name, significant, source);
                    childEdit.parent = currentEdit;
                    currentEdit = childEdit;
                }
                writeContext.target.setGraphEdit(currentEdit.graphEdit);
                currentEdit.setModificationCounter(writeContext.target.getModificationCounter());

                if (VERBOSE) {
                    final String log = String.format("Write lock acquired for " + name + " by " + Thread.currentThread());
                    LOGGER.log(Level.INFO, log);
                }

                return writeContext.target;
            } else {
                return null;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public T startReading() {
        final Context c = globalWriteLock.isHeldByCurrentThread() ? writeContext : readContext;
        c.lock.readLock().lock();

        if (VERBOSE) {
            LOGGER.log(Level.INFO,"Read lock aquired by {0}",Thread.currentThread());
        }

        return c.target;
    }

    protected void update(final Object description, Object editor) {
        // Overridden in class DualGraph
    }

    public void commit(final Object description, final String commitName) throws DuplicateKeyException {
        if (currentEdit == null || !globalWriteLock.isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException("commit: attempt to unlock write lock, not locked by current thread");
        }
        if (currentEdit.hasChanged(writeContext.target.getModificationCounter())) {
            currentEdit.commit(description, commitName);
        } else {
            currentEdit.rollBack(currentEdit.parent == null);
        }
    }

    public T flush(final Object description, final boolean announce) {
        if (currentEdit == null || !globalWriteLock.isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException("flush: attempt to unlock write lock, not locked by current thread");
        }
        if (currentEdit.hasChanged(writeContext.target.getModificationCounter())) {
            return currentEdit.flush(description, announce);
        }
        return writeContext.target;
    }

    public void rollBack() {
        if (currentEdit == null || !globalWriteLock.isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException("rollback: attempt to unlock write lock, not locked by current thread");
        }
        currentEdit.rollBack();
    }

    public final class LockingEdit implements UndoableEdit {

        private String name;
        private final boolean significant;
        private final Object editor;
        private final AtomicBoolean executed = new AtomicBoolean(true);
        private boolean alive = true;
        private LockingEdit parent;

        private long modificationCounter;

        public void setModificationCounter(final long modificationCounter) {
            this.modificationCounter = modificationCounter;
        }

        public boolean hasChanged(final long modificationCounter) {
            return this.modificationCounter != modificationCounter;
        }

        private List<LockingEdit> followingChildren = null;

        private UndoGraphEdit graphEdit = new UndoGraphEdit();

        private void finished() {
            graphEdit.finish();
        }

        private void execute(final T target) {
            graphEdit.execute((GraphWriteMethods) target);

            if (followingChildren != null) {
                for (LockingEdit followingChild : followingChildren) {
                    followingChild.execute(target);
                }
            }
        }

        private void undo(final T target) {

            if (followingChildren != null) {
                for (int i = followingChildren.size() - 1; i >= 0; i--) {
                    followingChildren.get(i).undo(target);
                }
            }

            graphEdit.undo((GraphWriteMethods) target);
        }

        public LockingEdit(final String name, final boolean significant, final Object editor) {
            this.name = name;
            this.significant = significant;
            this.editor = editor;
        }

        @Override
        public void undo() {
            if (!canUndo() || !executed.compareAndSet(true, false)) {
                throw new CannotUndoException();
            }

            new Thread(() -> {
                // Get the global write lock because we will change the graph
                globalWriteLock.lock();
                try {
                    writeContext.target.setOperationMode(GraphOperationMode.UNDO);
                    undo(writeContext.target);
                    writeContext.target.validateKeys();
                    writeContext.target.setOperationMode(GraphOperationMode.EXECUTE);

                    // Switch the read context to the write context
                    final Context originalReadContext = readContext;
                    readContext = writeContext;

                    originalReadContext.lock.writeLock().lock();
                    try {
                        originalReadContext.target.setOperationMode(GraphOperationMode.UNDO);
                        undo(originalReadContext.target);
                        originalReadContext.target.validateKeys();
                        originalReadContext.target.setOperationMode(GraphOperationMode.EXECUTE);
                    } finally {
                        originalReadContext.lock.writeLock().unlock();
                    }

                    // Switch the write context
                    writeContext = originalReadContext;
                } finally {
                    // Unlock the global write lock so new write requests can begin on the new write context
                    globalWriteLock.unlock();
                }
                fireUndoRedoReport("Undo", (GraphWriteMethods) writeContext.target, getPresentationName());
            }).start();

            update(null, null);
        }

        private void fireUndoRedoReport(String actionType, GraphWriteMethods target, String presentationName) {
            UndoRedoReport undoRedoReport = new UndoRedoReport(target.getId());
            undoRedoReport.setActionDescription(presentationName);
            undoRedoReport.setActionType(actionType);
            UndoRedoReportManager.fireNewUndoRedoReport(undoRedoReport);
        }

        @Override
        public boolean canUndo() {
            return alive && executed.get();
        }

        @Override
        public void redo() {
            if (!canRedo() || !executed.compareAndSet(false, true)) {
                throw new CannotRedoException();
            }

            new Thread(() -> {
                // Get the global write lock because we will change the graph
                globalWriteLock.lock();
                try {
                    writeContext.target.setOperationMode(GraphOperationMode.REDO);
                    execute(writeContext.target);
                    writeContext.target.validateKeys();
                    writeContext.target.setOperationMode(GraphOperationMode.EXECUTE);

                    // Switch the read context to the write context
                    final Context originalReadContext = readContext;
                    readContext = writeContext;

                    originalReadContext.lock.writeLock().lock();
                    try {
                        originalReadContext.target.setOperationMode(GraphOperationMode.REDO);
                        execute(originalReadContext.target);
                        originalReadContext.target.validateKeys();
                        originalReadContext.target.setOperationMode(GraphOperationMode.EXECUTE);
                    } finally {
                        originalReadContext.lock.writeLock().unlock();
                    }

                    // Switch the write context
                    writeContext = originalReadContext;
                } finally {
                    // Unlock the global write lock so new write requests can begin on the new write context
                    globalWriteLock.unlock();
                }
                //if isSignificant){
                fireUndoRedoReport("Redo", (GraphWriteMethods) writeContext.target, getPresentationName());
            }).start();

            update(null, null);
        }

        @Override
        public boolean canRedo() {
            return alive && !executed.get();
        }

        @Override
        public void die() {
            alive = false;
        }

        @Override
        public boolean addEdit(final UndoableEdit edit) {
            if (edit.getClass() == LockingEdit.class) {
                @SuppressWarnings("unchecked") // Type is manually checked.
                LockingEdit lockingEdit = (LockingEdit) edit;
                if (!lockingEdit.significant) {
                    if (followingChildren == null) {
                        followingChildren = new ArrayList<>();
                    }
                    followingChildren.add(lockingEdit);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean replaceEdit(final UndoableEdit anEdit) {
            return false;
        }

        @Override
        public boolean isSignificant() {
            return significant;
        }

        @Override
        public String getPresentationName() {
            return name;
        }

        @Override
        public String getUndoPresentationName() {
            return "Undo " + name;
        }

        @Override
        public String getRedoPresentationName() {
            return "Redo " + name;
        }

        public void commit(final Object description, final String commitName) throws DuplicateKeyException {
            try {
                writeContext.target.validateKeys();
            } catch (DuplicateKeyException ex) {
                rollBack(parent == null);
                throw ex;
            }

            finished();

            if (commitName != null) {
                initialEdit.name = commitName;
            }

            if (parent == null) {

                writeContext.target.setGraphEdit(null);

                Context originalReadContext = readContext;
                readContext = writeContext;

                originalReadContext.lock.writeLock().lock();
                try {
                    execute(originalReadContext.target);
                    originalReadContext.target.validateKeys();
                } finally {
                    originalReadContext.lock.writeLock().unlock();
                }

                writeContext = originalReadContext;

                if (undoManager != null) {
                    SwingUtilities.invokeLater(() -> undoManager.undoableEditHappened(new UndoableEditEvent(LockingManager.this, LockingEdit.this)));
                }
                currentEdit = null;
                initialEdit = null;
                globalWriteLock.unlock();

                update(description, editor);

            } else {
                parent.graphEdit.addChild(graphEdit);
                currentEdit = parent;
                writeContext.target.setGraphEdit(currentEdit.graphEdit);
                globalWriteLock.unlock();

            }
        }

        public T flush(final Object description, final boolean announce) {
            try {
                writeContext.target.validateKeys();
            } catch (DuplicateKeyException ex) {
                rollBack(parent == null);
                throw ex;
            }

            writeContext.target.setGraphEdit(null);
            finished();

            if (parent == null) {

                Context originalReadContext = readContext;
                readContext = writeContext;

                originalReadContext.lock.writeLock().lock();
                try {
                    execute(originalReadContext.target);
                    originalReadContext.target.validateKeys();
                } finally {
                    originalReadContext.lock.writeLock().unlock();
                }

                writeContext = originalReadContext;

                if (undoManager != null) {
                    SwingUtilities.invokeLater(() -> undoManager.undoableEditHappened(new UndoableEditEvent(LockingManager.this, LockingEdit.this)));
                }
                currentEdit = new LockingEdit(name, false, editor);
                writeContext.target.setGraphEdit(currentEdit.graphEdit);

                if (announce) {
                    update(description, editor);
                }

            } else {

                parent.graphEdit.addChild(graphEdit);
                LockingEdit childEdit = new LockingEdit(name, false, editor);
                childEdit.parent = currentEdit;
                currentEdit = childEdit;
                writeContext.target.setGraphEdit(currentEdit.graphEdit);

            }
            return writeContext.target;
        }

        public void rollBack() {
            rollBack(true);
        }

        public void rollBack(final boolean validateKeys) {
            writeContext.target.setGraphEdit(null);
            finished();
            writeContext.target.setOperationMode(GraphOperationMode.UNDO);
            undo(writeContext.target);
            if (validateKeys) {
                writeContext.target.validateKeys();
            }
            writeContext.target.setOperationMode(GraphOperationMode.EXECUTE);
            currentEdit = parent;
            if (currentEdit == null) {
                initialEdit = null;
            }
            globalWriteLock.unlock();
        }
    }
}
