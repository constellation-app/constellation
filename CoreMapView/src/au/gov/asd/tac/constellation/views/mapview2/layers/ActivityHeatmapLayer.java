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
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author altair1673
 */
public class ActivityHeatmapLayer extends AbstractHeatmapLayer {

    public ActivityHeatmapLayer(MapView parent) {
        super(parent);
    }

    @Override
    public int getWeight(AbstractMarker marker) {
        int activityCount = 0;

        if (currentGraph != null) {
            final ReadableGraph readableGraph = currentGraph.getReadableGraph();
            try {
                final GraphElementType[] elementTypes = new GraphElementType[]{GraphElementType.VERTEX, GraphElementType.TRANSACTION};

                final Set<Integer> seenLinks = new HashSet<>();
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

                    if (element == GraphElementType.VERTEX) {
                        for (int position = 0; position < elementCount; ++position) {
                            int vertexID = readableGraph.getVertex(position);

                            if (marker.getIdList().contains(vertexID)) {
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
