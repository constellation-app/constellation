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

/**
 * A ReadableGraph extends GraphReadMethods and provides a release() method
 * allowing the caller to release the read lock on the graph.
 *
 * @author sirius
 */
public interface ReadableGraph extends GraphReadMethods, AutoCloseable {

    /**
     * Releases the read lock on the graph. After this is called, this
     * ReadableGraph is now considered invalid and should not be used from that
     * point on.
     */
    public void release();

    @Override
    public default void close() {
        release();
    }
}
