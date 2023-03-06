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
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 *
 * @author altair1673
 */
public abstract class AbstractHeatmapLayer extends AbstractMapLayer {

    // Group to hold all heatmap graphical elements
    protected Group layerGroup;

    private static final double X_OFFSET = 96;
    private static final double Y_OFFSET = 92;

    protected AbstractHeatmapLayer(final MapView parent, final int id) {
        super(parent, id);
        layerGroup = new Group();
    }

    /**
     * Set up the heatmap layer
     */
    @Override
    public void setUp() {
        // Loop through all the markers
        for (final Object value : parent.getAllMarkers().values()) {

            // If marker is a point marker
            if (value instanceof PointMarker) {

                Text markerWeight = new Text();

                PointMarker marker = (PointMarker) value;

                // Get the "weight" of the marker in a graphical text element
                markerWeight.setText(Integer.toString(getWeight(marker)));

                // Offset the text element so that it lines up with the markers
                double startingX = marker.getX() - X_OFFSET;
                double startingY = marker.getY() + Y_OFFSET;

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

    public int getWeight(final AbstractMarker marker) {
        return 0;
    }

}
