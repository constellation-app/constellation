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
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author altair1673
 */
public class LocationPathsLayer extends AbstractPathsLayer {

    public LocationPathsLayer(MapView parent, int id, Map<String, AbstractMarker> queriedMarkers) {
        super(parent, id, queriedMarkers);
    }

    @Override
    public void setUp() {
        final List<Integer> idList = new ArrayList<Integer>();

        for (Object value : queriedMarkers.values()) {
            AbstractMarker m = (AbstractMarker) value;

            if (m instanceof PointMarker) {
                m.getIdList().forEach(id -> idList.add(id));
            }
        }

        for (int i = 0; i < idList.size(); ++i) {

        }
    }

}
