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
import au.gov.tac.constellation.views.mapview2.utillities.Vec3;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

/**
 *
 * @author altair1673
 */
public abstract class AbstractHeatmapLayer extends AbstractMapLayer {

    // Group to hold all heatmap graphical elements
    protected Group layerGroup;

    private final double xOffset = 96;
    private final double yOffset = 92;

    private static final Logger LOGGER = Logger.getLogger("AbstractHeatMapLayer");

    public AbstractHeatmapLayer(MapView parent, int id) {
        super(parent, id);
        layerGroup = new Group();
    }

    @Override
    public void setUp() {
        // Get queried markers
        Map<String, AbstractMarker> markers = parent.getAllMarkers();

        // Loop through all the markers
        for (Object value : parent.getAllMarkers().values()) {

            // If marker is a point marker
            if (value instanceof PointMarker) {

                Text markerWeight = new Text();

                PointMarker marker = (PointMarker) value;

                // Get the "weight" of the marker in a graphical text element
                markerWeight.setText(Integer.toString(getWeight(marker)));

                // Offset the text element so that it lines up with the markers
                double startingX = marker.getX() - xOffset;
                double startingY = marker.getY() + yOffset;

                markerWeight.setX(startingX);
                markerWeight.setY(startingY);

                markerWeight.setFill(Color.BLACK);

                // Add text to group
                layerGroup.getChildren().add(markerWeight);
            }
        }


    }




    @Override
    public Group getLayer() {
        return layerGroup;
    }

    public int getWeight(AbstractMarker marker) {
        return 0;
    }

}
