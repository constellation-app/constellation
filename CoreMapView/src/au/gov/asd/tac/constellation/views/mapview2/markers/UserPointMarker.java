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
package au.gov.asd.tac.constellation.views.mapview2.markers;

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Point marker that get generated wherever the user clicks on the map
 *
 * @author altair1673
 */
public class UserPointMarker extends AbstractMarker {

    // Raw marker string for the user point marker
    private String path = "c-20.89-55.27-83.59-81.74-137-57.59-53.88,24.61-75.7,87.77-47.83,140.71,12.54,23.69,26.47,46.44,39.93,70.12,15.79,27.4,32,55.27,50.16,87.31a101.37,101.37,0,0,1,4.65-9.76c27.86-49.23,56.66-98,84-147.68,14.86-26,16.72-54.8,6-83.12z";
    private double x;
    private double y;

    public UserPointMarker(final MapView parent, final int markerID, final double x, final double y, final double scale, final double xOffset, final double yOffset) {
        super(parent, markerID, -99, xOffset, yOffset, AbstractMarker.MarkerType.POINT_MARKER);


        this.x = x;
        this.y = y;


        markerPath.setScaleX(scale);
        markerPath.setScaleY(scale);
        markerPath.setOpacity(0.5);

        markerPath.setFill(Color.ORANGE);
        markerPath.setStroke(Color.BLACK);

        // Set up event handlers for user draw point marker
        markerPath.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {

                markerPath.setFill(Color.YELLOW);

                e.consume();
            }
        });

        markerPath.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {

                markerPath.setFill(Color.ORANGE);

                e.consume();
            }
        });

        markerPath.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {

                parent.removeUserMarker(markerID);

                e.consume();
            }
        });
    }

    /**
     * Sets marker position on the map
     *
     * @param mapWidth
     * @param mapHeight
     */
    @Override
    public void setMarkerPosition(final double mapWidth, final double mapHeight) {

        x += xOffset;
        y += yOffset;

        super.setX(x);
        super.setY(y);

        path = "M " + x + ", " + y + " Z " + path;

        markerPath.setContent(path);
    }

    public String getPath() {
        return path;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }


}
