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
package au.gov.asd.tac.constellation.graph.operations;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;

/**
 * A GraphOperation is an alternative way to edit a graph. Instead of a plugin
 * editing a graph directly and the graph handling the creation of a
 * corresponding undo operation, the GraphOperation is responsible for both the
 * execute and undo. This is more work but is beneficial when there is a more
 * space efficient way to store the undo operation than simply recording all
 * graph operations.
 *
 * @author sirius
 */
public abstract class GraphOperation {

    /**
     * Execute the operation on the specified graph.
     *
     * @param graph the operation will be executed on this graph.
     */
    public abstract void execute(final GraphWriteMethods graph);

    /**
     * Undo the operation on the specified graph.
     *
     * @param graph the operation will be undone on this graph.
     */
    public abstract void undo(final GraphWriteMethods graph);

    /**
     * Is this operation more efficient than simply recording all graph
     * operations. If so, this graph operation will be used. Otherwise, it will
     * simply be executed on the graph but the graph will record the undo
     * operation. This method is called by the graph immediately before
     * execution meaning that the decision of whether or not to use the
     * undo/redo operations of this GraphOperation can be made at the last
     * minute.
     *
     * @return true if this operation is more efficient for the undo stack that
     * simply recording each operation.
     */
    public boolean isMoreEfficient() {
        return true;
    }

    /**
     * Returns the size in bytes of this GraphOperation object.
     *
     * @return the size in bytes of this GraphOperation object.
     */
    public int size() {
        return 0;
    }

}
