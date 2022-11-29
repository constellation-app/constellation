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
package au.gov.asd.tac.constellation.graph;

import javax.swing.undo.UndoableEdit;

/**
 * A GraphUndoableEdit encapsulates a sequence of changes to a graph.
 * <p>
 * An edit is obtained from a Graph by calling {@code createNewEdit()}. Once
 * created, an edit remains active on the graph until {@code finish()} is
 * called. The edit may be paused and resumed if required: for instance,
 * multiple changes to the same attribute are redundant, and would use
 * significant resources.
 * <p>
 * Example code is shown below. Note that if no changes were actually made, it
 * is not necessary to call edit.undo() because there is nothing to undo. Be
 * careful; if changes were made, and edit.undo() isn't called, then the changes
 * to the graph won't be undone, an there will be no undo available to undo to
 * the graph's previous state.
 *
 * <pre>
 * <code>
 * graph.getWriteLock();
 * final GraphUndoableEdit edit = graph.createNewEdit("Brief reason for editing", true);
 * try {
 *     // Make changes  to graph.
 * } finally {
 *     edit.finish();
 *     graph.releaseWriteLock();
 * }
 *
 * if (rollback) {
 *     edit.undo();
 *     edit.die();
 * } else {
 *     undoRedoManager.undoableEditHappened(new UndoableEditEvent(graph, edit));
 * }
 * </code>
 * </pre>
 *
 * @author sirius
 */
public interface GraphUndoableEdit extends UndoableEdit {

    /**
     * Return the graph that the edit is collecting events from.
     * <p>
     * This is the graph that createNewEdit(() was called on.
     *
     * @return The graph that the edit is collecting events from.
     */
    Graph getGraph();

    /**
     * Temporarily stops the edit from collecting events.
     * <p>
     * The edit remains valid. Resume collecting events by calling
     * {@code resume()}
     *
     * @see GraphUndoableEdit#resume()
     */
    void pause();

    /**
     * Causes a paused edit to resume collecting events.
     *
     * @see GraphUndoableEdit#pause()
     */
    void resume();

    /**
     * Finishes the edit.
     * <p>
     * This must be called once and only once for each graph.startEdit().
     */
    void finish();
}
