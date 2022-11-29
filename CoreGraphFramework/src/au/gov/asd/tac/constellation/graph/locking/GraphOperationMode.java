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

/**
 * The GraphOperationMode represents the three possible modification modes of a
 * graph.
 *
 * @author sirius
 */
public enum GraphOperationMode {

    /**
     * Execution mode. This represents the normal mode of operation for a graph
     * where operations are executed in response to user calls. The modification
     * counters are adjusted in the positive direction and the operations are
     * typically recorded onto an undo/redo stack.
     */
    EXECUTE(1) {
    },
    /**
     * Undo mode. This represents the mode where previously performed operations
     * are being undone by the undo stack. The graph modification counters are
     * adjusted in the negative direction to restore them to their pre-operation
     * values and there is no need to record these operations to an undo stack.
     */
    UNDO(-1) {
    },
    /**
     * Redo mode. This represents the mode where previously undone operations
     * are being re-done by the undo stack as part of a redo operation. The
     * graph modification counters are adjusted in the positive direction to
     * restore them to their pre-undo values and there is no need to record
     * these operations to an undo stack.
     */
    REDO(1) {
    };

    private final long modificationIncrement;

    /**
     * Creates a new GraphOperationMode
     *
     * @param modificationIncrement the direction that the graph modification
     * counters should be adjusted when operations are performed in this mode.
     */
    private GraphOperationMode(final long modificationIncrement) {
        this.modificationIncrement = modificationIncrement;
    }

    /**
     * Returns the modification increment for this mode. This is the direction
     * that the graph modification counters should be adjusted when operations
     * are performed in this mode.
     *
     * @return the modification increment for this mode.
     */
    public long getModificationIncrement() {
        return modificationIncrement;
    }
}
