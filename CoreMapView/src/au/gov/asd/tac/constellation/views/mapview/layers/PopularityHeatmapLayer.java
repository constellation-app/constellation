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
package au.gov.asd.tac.constellation.views.mapview.layers;

import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import au.gov.asd.tac.constellation.views.mapview.utilities.GraphElement;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * A heatmap weighted by the number of entities related to a location.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapLayer.class, position = 200)
public class PopularityHeatmapLayer extends AbstractHeatmapLayer {

    @Override
    public String getName() {
        return "Heatmap (Popularity)";
    }

    @Override
    public float getWeight(final ConstellationAbstractMarker marker) {
        int popularityCount = 0;

        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final Set<Integer> seenLinks = new HashSet<>();
                for (final GraphElement element : renderer.getMarkerCache().get(marker)) {
                    switch (element.getType()) {
                        case VERTEX -> popularityCount += readableGraph.getVertexNeighbourCount(element.getId());
                        case TRANSACTION -> {
                            final int linkId = readableGraph.getTransactionLink(element.getId());
                            if (!seenLinks.contains(linkId)) {
                                seenLinks.add(linkId);
                                popularityCount += 1;
                            }
                        }
                        default -> {
                            // do nothing
                        }
                    }
                }
            } finally {
                readableGraph.release();
            }
        }

        return popularityCount;
    }
}
