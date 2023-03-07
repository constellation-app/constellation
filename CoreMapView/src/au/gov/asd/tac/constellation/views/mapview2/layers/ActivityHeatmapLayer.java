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
 * Heatmap layer that tracks amount of transactions for each node
 *
 * @author altair1673
 */
public class ActivityHeatmapLayer extends AbstractHeatmapLayer {

    public ActivityHeatmapLayer(final MapView parent, final int id) {
        super(parent, id);
    }

    /**
     * Gets the specific attribute to act as the "weight" for the marker This
     * code gets the amount of transactions each node has as the weight
     *
     * @param marker
     * @returns the weight value
     */
    @Override
    public int getWeight(final AbstractMarker marker) {
        // Holds the ammount of transactions
        int activityCount = 0;

        if (currentGraph != null) {
            // Get the current readable graph
            final ReadableGraph readableGraph = currentGraph.getReadableGraph();
            try {
                final GraphElementType[] elementTypes = new GraphElementType[]{GraphElementType.VERTEX, GraphElementType.TRANSACTION};

                // For each type of graph element
                for (final GraphElementType element : elementTypes) {
                    int elementCount = 0;

                    switch (element) {
                        case VERTEX:
                            elementCount = readableGraph.getVertexCount();
                            break;
                        case TRANSACTION:

                            break;
                        default:
                            break;
                    }

                    // For every vertex
                    if (element == GraphElementType.VERTEX) {
                        for (int position = 0; position < elementCount; ++position) {
                            int vertexID = readableGraph.getVertex(position);

                            // Check if the node represented by the current marker has the current vertext as its neighbour
                            if (marker.getConnectedNodeIdList().contains(vertexID)) {
                                // The activitiy count is the ammount of transactions this neighbour has
                                activityCount += readableGraph.getVertexTransactionCount(vertexID);
                            }
                        }
                    }
                }
            } finally {
                readableGraph.release();
            }
        }

        return activityCount;
    }

}
