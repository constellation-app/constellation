/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;

/**
 * Heatmap layer that also represent node neighbours
 *
 * @author altair1673
 */
public class PopularityHeatmapLayer extends AbstractHeatmapLayer {

    public PopularityHeatmapLayer(final MapView parent, final int id) {
        super(parent, id);
    }

    /**
     * Get he weight of the current marker
     *
     * @param marker
     * @return the weight
     */
    @Override
    public int getWeight(final AbstractMarker marker) {
        int popularityCount = 0;

        if (currentGraph != null) {
            final ReadableGraph readableGraph = currentGraph.getReadableGraph();
            try {
                // Get graph element types
                final GraphElementType[] elementTypes = new GraphElementType[]{GraphElementType.VERTEX, GraphElementType.TRANSACTION};

                // For each element type
                for (final GraphElementType element : elementTypes) {
                    int elementCount = 0;

                    // Get the count of that element type
                    switch (element) {
                        case VERTEX:
                            elementCount = readableGraph.getVertexCount();
                            // For every element of that type
                            for (int position = 0; position < elementCount; ++position) {
                                final int vertexID = readableGraph.getVertex(position);

                                // Get its transaction count and record it as its popularity
                                if (marker.getConnectedNodeIdList().contains(vertexID)) {
                                    popularityCount += readableGraph.getVertexNeighbourCount(vertexID);
                                }
                            }
                            break;
                        case TRANSACTION:
                        default:
                            break;
                    }
                }
            } finally {
                readableGraph.release();
            }
        }

        return popularityCount;
    }
}
