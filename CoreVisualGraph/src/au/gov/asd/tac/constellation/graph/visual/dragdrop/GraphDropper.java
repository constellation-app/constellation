/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.visual.dragdrop;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.awt.dnd.DropTargetDropEvent;
import java.util.function.BiConsumer;

/**
 * Graph Dropper
 *
 * @author algol
 */
public interface GraphDropper {

    /**
     * React to a drop on a graph.
     * <p>
     * Something has been dropped on the graph as the result of a drag-and-drop
     * operation. If the implementation supports a DataFlavor in the associated
     * Transferable, perform the drop, call dtde.dropComplete(true), and return
     * true.
     * <p>
     * If the implementation does not support any DataFlavor, or cannot perform
     * the drop, return false.
     * <p>
     * The drop target will call successive implementations until one of them
     * returns true, then stop. If all implementations return false, the drop
     * target will call dtde.dropComplete(false).
     * <p>
     * By convention, if the MIME type is "text/plain", the start of the string
     * is an indicator of which GraphDropper should handle the data. The string
     * should start with "indicator=". The rest of the string is in whatever
     * format the GraphDropper demands.
     * <p>
     * The drop target has already called dtde.acceptDrop(dtde.getDropAction()).
     *
     * @param dtde The DropTargetDropEvent passed to the drop target.
     *
     * @return True if the implementation can handle the drop, false otherwise.
     */
    public BiConsumer<Graph, DropInfo> drop(final DropTargetDropEvent dtde);

    /**
     * Information about the drop.
     */
    public static class DropInfo {

        /**
         * The (x,y,z) location (in graph coordinates) that the drop occurred
         * at.
         */
        public final Vector3f location;

        /**
         * The id of the element that the drop occurred on (less than zero if no
         * element was dropped on).
         */
        public final int id;

        /**
         * True if the drop was on a vertex element.
         */
        private final boolean isVertex;

        /**
         * True if the drop was on a transaction element.
         */
        private final boolean isTransaction;

        public DropInfo(final Vector3f location, final int id, final boolean isVertex, final boolean isTransaction) {
            this.location = location;
            this.id = id;
            this.isVertex = isVertex;
            this.isTransaction = isTransaction;
        }

        public boolean isIsVertex() {
            return isVertex;
        }

        public boolean isIsTransaction() {
            return isTransaction;
        }

    }
}
