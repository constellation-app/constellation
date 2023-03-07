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

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;

/**
 * Heatmap layers showing how many locations one marker points at
 *
 * @author altair1673
 */
public class StandardHeatmapLayer extends AbstractHeatmapLayer {

    public StandardHeatmapLayer(final MapView parent, final int id) {
        super(parent, id);
    }

    /**
     * standard heat-map is the amount of nodes the marker points to
     *
     * @param marker
     * @return the weight
     */
    @Override
    public int getWeight(final AbstractMarker marker) {
        return marker.getWeight();
    }
}
