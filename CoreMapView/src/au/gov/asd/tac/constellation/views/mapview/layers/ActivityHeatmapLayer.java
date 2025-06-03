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
import org.openide.util.lookup.ServiceProvider;

/**
 * A heatmap weighted by the amount of activity at a location.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapLayer.class, position = 300)
public class ActivityHeatmapLayer extends AbstractHeatmapLayer {

    @Override
    public String getName() {
        return "Heatmap (Activity)";
    }

    @Override
    public float getWeight(final ConstellationAbstractMarker marker) {
        int activityCount = 0;

        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                for (final GraphElement element : renderer.getMarkerCache().get(marker)) {
                    switch (element.getType()) {
                        case VERTEX -> activityCount += readableGraph.getVertexTransactionCount(element.getId());
                        case TRANSACTION -> activityCount += 1;
                        default -> {
                            // do nothing
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
