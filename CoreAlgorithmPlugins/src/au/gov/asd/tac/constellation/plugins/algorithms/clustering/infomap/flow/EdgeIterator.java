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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Edge Iterator
 *
 * @author algol
 */
public final class EdgeIterator implements Iterator<Connection> {

    private final GraphReadMethods rg;

    private final int edgeCount;
    private int position = 0;

    public EdgeIterator(final GraphReadMethods rg) {
        this.rg = rg;
        edgeCount = rg.getEdgeCount();
        position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < edgeCount;
    }

    @Override
    public Connection next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        final int edgeId = rg.getEdge(position);

        // Note: use vertex position so we get ids in 0..n-1 instead of whatever the vertex ids are.
        final int end1 = rg.getVertexPosition(rg.getEdgeSourceVertex(edgeId));
        final int end2 = rg.getVertexPosition(rg.getEdgeDestinationVertex(edgeId));
        final double weight = rg.getEdgeTransactionCount(edgeId);

        position++;

        return new Connection(end1, end2, weight);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
