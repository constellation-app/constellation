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

/**
 * A WritableGraph represents a write lock on the graph. It has methods for
 * editing the graph and also for releasing the write lock. Once the write lock
 * has been released by either commit, flush or rollback methods then the
 * WritableGraph is considered closed and should not be used again.
 *
 * @author sirius
 */
public interface WritableGraph extends GraphWriteMethods {

    /**
     * Causes the changes made on this writable graph to be committed so that
     * other processes can see them. This writable graph is now considered
     * closed and should not be used again.
     */
    public void commit();

    /**
     * Causes the changes made on this writable graph to be committed so that
     * other processes can see them. This writable graph is now considered
     * closed and should not be used again.
     *
     * @param description the description object that will be passed to all
     * GraphChangeListeners that are listening to this graph.
     */
    public void commit(final Object description);

    /**
     * Causes the changes made on this writable graph to be committed so that
     * other processes can see them. This writable graph is now considered
     * closed and should not be used again.
     *
     * @param description the description object that will be passed to all
     * GraphChangeListeners that are listening to this graph.
     * @param commitName A new name from the commit to override that given when
     * this WritableGraph was first acquired.
     */
    public void commit(final Object description, final String commitName);

    /**
     * Causes the changes made on this writable graph to be committed so that
     * other processes can see them. A new write lock is immediately acquired to
     * allow continued writing. However, the current WritableGraph is considered
     * closed and should not be used again. Further writing should be performed
     * on the returned WritableGraph.
     *
     * @param announce Whether or not to announce to
     * {@link GraphChangeListener GraphChangeListeners} that the change has
     * occurred.
     * @return a new WritableGraph for any further writing.
     */
    public WritableGraph flush(final boolean announce);

    /**
     * Causes the changes made on this writable graph to be committed so that
     * other processes can see them. A new write lock is immediately acquired to
     * allow continued writing. However, the current WritableGraph is considered
     * closed and should not be used again. Further writing should be performed
     * on the returned WritableGraph.
     *
     * @param description the description object that will be passed to all
     * GraphChangeListeners
     * @param announce Whether or not to announce to
     * {@link GraphChangeListener GraphChangeListeners} that the change has
     * occurred.
     * @return a new WritableGraph for any further writing.
     */
    public WritableGraph flush(final Object description, final boolean announce);

    /**
     * Causes all changes made on this WritableGraph to be discarded. This
     * WritableGraph is now considered closed and should not be used again.
     */
    public void rollBack();
}
