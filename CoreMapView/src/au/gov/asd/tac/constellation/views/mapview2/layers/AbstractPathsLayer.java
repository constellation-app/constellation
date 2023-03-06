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
import java.util.Map;
import javafx.scene.Group;

/**
 *
 * @author altair1673
 */
public class AbstractPathsLayer extends AbstractMapLayer {

    // Group to hold all the line markers representing paths
    protected Group entityPaths;

    protected Map<String, AbstractMarker> queriedMarkers;

    public AbstractPathsLayer(final MapView parent, final int id, final Map<String, AbstractMarker> queriedMarkers) {
        super(parent, id);

        entityPaths = new Group();
        this.queriedMarkers = queriedMarkers;
    }


    @Override
    public Group getLayer() {
        return entityPaths;
    }

}
