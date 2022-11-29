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

import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import javax.swing.undo.UndoManager;

/**
 * A Graph object represents a graph in Constellation.
 * <p>
 * Graph read/write methods are not available directly from this interface, a
 * Graph interface provides methods for acquiring read and write locks that
 * return {@link ReadableGraph} and {@link WritableGraph} interfaces
 * respectively. It is these interfaces that allow for reading and writing to a
 * graph.
 * <p>
 * The typical method for reading from a graph is as follows:
 * <pre><code>
 *      // Acqure a read lock on the graph
 *      ReadableGraph rg = graph.getReadableGraph();
 *
 *      try {
 *          // All reading operations should happen inside a try/catch
 *          final int vertexCount = rg.getVertexCount();
 *          ...
 *      } finally {
 *          // The read lock should be released inside a finally block
 *          rg.release()
 *      }
 *      // The read lock is now released - read operations are now not allowed
 * </code></pre>
 * <p>
 * The typical method for writing to a graph is as follows:
 * <pre><code>
 *      // Acqure a write lock on the graph
 *      WritableGraph wg = graph.getWritableGraph("Add A Vertex", true);
 *
 *      try {
 *          // All writing operations should happen inside a try/catch
 *          final int newVertex = wg.addVertex();
 *          ...
 *      } finally {
 *          // The write lock should be committed inside a finally block
 *          wg.commit()
 *      }
 *      // The write lock is now released - read and write operations are now not allowed
 * </code></pre>
 * <p>
 * NOTE: This graph maintains the equality that the sum of degrees of all
 * vertices = 2 * the number of links. This causes a few counter-intuitive
 * results such as:
 * <ol>
 * <li>When a loop is created, the degree of a vertex increases by 2.
 * <li>When a loop is created, the vertex is now a neighbour of itself twice.
 * <li>When a loop is created, the transaction, edge and link counts are also
 * increased by 2.
 * </ol>
 *
 * @author sirius
 */
public interface Graph extends GraphConstants {

    /**
     * Return a unique identifier for this graph.
     *
     * @return a unique identifier for this graph.
     */
    public String getId();

    /**
     * Returns the {@link Schema} for this graph.
     *
     * @return the {@link Schema} of the graph.
     */
    public Schema getSchema();

    /**
     * Add a PropertyChangeListener to the graph.
     *
     * @param listener The PropertyChangeListener to add to the graph.
     */
    void addGraphChangeListener(final GraphChangeListener listener);

    /**
     * Remove a PropertyChangeListener from the graph.
     *
     * @param listener The PropertyChangeListener to remove from the graph.
     */
    void removeGraphChangeListener(final GraphChangeListener listener);

    public ReadableGraph getReadableGraph();

    /**
     * A convenience method that allows simple graph read operations to be
     * performed by specifying a closure. The locking of the graph is handled
     * automatically.
     *
     * @param <V> the type of Object the reader will return.
     * @param reader the operations to perform with the provided read lock.
     * @return the return value from the specified GraphReader.
     * @throws InterruptedException if the specified GraphReader throws an
     * InterruptedException.
     */
    public default <V> V readFromGraph(final GraphReader<V> reader) throws InterruptedException {
        final ReadableGraph rg = getReadableGraph();
        try {
            return reader.read(rg);
        } finally {
            rg.release();
        }
    }

    /**
     * Gets a write lock on the graph and returns a WritableGraph that provides
     * methods to modify the graph. This method will block until a write lock is
     * available.
     * <b>
     * To prevent deadlock situations, calling this method on the event dispatch
     * thread is not allowed and will cause an {@link IllegalStateException} to
     * be thrown.
     * </b>
     *
     * @param name The name of the edit used to modify the graph. This name will
     * be displayed to the user and used to identify the edit in situations such
     * as undo/redo.
     * @param significant True if this is a significant edit. Significant edits
     * appear as a distinct step in the undo/redo stack.
     *
     * @return a WritableGraph that provides methods to modify the graph.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     * for the write lock.
     * @throws IllegalStateException if this method is called on the event
     * dispatch thread.
     */
    public WritableGraph getWritableGraph(final String name, final boolean significant) throws InterruptedException;

    /**
     * A convenience method that allows simple graph write operations to be
     * performed by specifying a closure. The write lock is created
     * automatically and passed to the specified GraphWriter object. If the
     * writer completes successfully, the lock is committed and the method
     * returns the values returned from the Writer. If any error occurs, the
     * write lock is rolled-back and a runtime exception is thrown that holds
     * the original exception as its cause.
     *
     * @param <V> the type of object returned from the writer.
     * @param name the name of the edit.
     * @param significant is the edit significant as far as the undo stack is
     * concerned?
     * @param writer the GraphWriter to execute.
     * @return the returned object from the specified GraphWriter
     * @throws InterruptedException if the specified Writer throws an
     * InterruptedException.
     */
    public default <V> V writeToGraph(final String name, final boolean significant, final GraphWriter<V> writer) throws InterruptedException {
        final WritableGraph wg = getWritableGraph(name, significant);
        boolean error = false;
        try {
            return writer.write(wg);
        } catch (final InterruptedException ex) {
            error = true;
            throw ex;
        } catch (final Exception ex) {
            error = true;
            throw new RuntimeException(ex);
        } finally {
            if (error) {
                wg.rollBack();
            } else {
                wg.commit();
            }
        }
    }

    /**
     * Gets a write lock on the graph and returns a WritableGraph that provides
     * methods to modify the graph. This method will block until a write lock is
     * available.
     * <b>
     * To prevent deadlock situations, calling this method on the event dispatch
     * thread is not allowed and will cause an {@link IllegalStateException} to
     * be thrown.
     * </b>
     *
     * @param name The name of the edit used to modify the graph. This name will
     * be displayed to the user and used to identify the edit in situations such
     * as undo/redo.
     * @param significant True if this is a significant edit. Significant edits
     * appear as a distinct step in the undo/redo stack.
     * @param editor an object that can be used to identify the process
     * performing the edit. Other processes that receive update events as
     * modifications occur can use this value to influence their response to the
     * modification.
     *
     * @return a WritableGraph that provides methods to modify the graph.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     * for the write lock.
     * @throws IllegalStateException if this method is called on the event
     * dispatch thread.
     */
    public WritableGraph getWritableGraph(final String name, final boolean significant, final Object editor) throws InterruptedException;

    /**
     * Gets a write lock on the graph and returns a WritableGraph that provides
     * methods to modify the graph. This method does not block, returning null
     * if the write lock is not immediately available.
     * <b>
     * To prevent deadlock situations, calling this method on the event dispatch
     * thread is not allowed and will cause an {@link IllegalStateException} to
     * be thrown.
     * </b>
     *
     * @param name The name of the edit used to modify the graph. This name will
     * be displayed to the user and used to identify the edit in situations such
     * as undo/redo.
     * @param significant True if this is a significant edit. Significant edits
     * appear as a distinct step in the undo/redo stack.
     *
     * @return a WritableGraph that provides methods to modify the graph.
     *
     * @throws IllegalStateException if this method is called on the event
     * dispatch thread.
     */
    public WritableGraph getWritableGraphNow(final String name, final boolean significant);

    /**
     * Gets a write lock on the graph and returns a WritableGraph that provides
     * methods to modify the graph. This method does not block, returning null
     * if the write lock is not immediately available.
     * <b>
     * To prevent deadlock situations, calling this method on the event dispatch
     * thread is not allowed and will cause an {@link IllegalStateException} to
     * be thrown.
     * </b>
     *
     * @param name The name of the edit used to modify the graph. This name will
     * be displayed to the user and used to identify the edit in situations such
     * as undo/redo.
     * @param significant True if this is a significant edit. Significant edits
     * appear as a distinct step in the undo/redo stack.
     * @param editor an object that can be used to identify the process
     * performing the edit. Other processes that receive update events as
     * modifications occur can use this value to influence their response to the
     * modification.
     *
     * @return a WritableGraph that provides methods to modify the graph.
     *
     * @throws IllegalStateException if this method is called on the event
     * dispatch thread.
     */
    public WritableGraph getWritableGraphNow(final String name, final boolean significant, final Object editor);

    /**
     * Sets the UndoManager for this graph. The undo manager is responsible for
     * recording all modifications that occur on the graph and providing
     * undo/redo services.
     *
     * @param undoManager the new UndoManager.
     */
    public void setUndoManager(final UndoManager undoManager);
}
