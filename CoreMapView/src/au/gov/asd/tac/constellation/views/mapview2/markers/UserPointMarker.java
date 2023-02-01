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
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author altair1673
 */
public class UserPointMarker extends AbstractMarker {
    private String path = "c-20.89-55.27-83.59-81.74-137-57.59-53.88,24.61-75.7,87.77-47.83,140.71,12.54,23.69,26.47,46.44,39.93,70.12,15.79,27.4,32,55.27,50.16,87.31a101.37,101.37,0,0,1,4.65-9.76c27.86-49.23,56.66-98,84-147.68,14.86-26,16.72-54.8,6-83.12z";
    private double x;
    private double y;
    private double scale;

    public UserPointMarker(MapView parent, int markerID, double x, double y, double scale, double xOffset, double yOffset) {
        super(parent, markerID, -99, xOffset, yOffset, AbstractMarker.MarkerType.POINT_MARKER);


        this.x = x;
        this.y = y;

        this.scale = scale;

        markerPath.setScaleX(scale);
        markerPath.setScaleY(scale);
        markerPath.setOpacity(0.5);

        markerPath.setFill(Color.ORANGE);
        markerPath.setStroke(Color.BLACK);
        //markerPath.setOpacity(0.5);

        markerPath.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                markerPath.setFill(Color.YELLOW);

                e.consume();
            }
        });

        markerPath.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                markerPath.setFill(Color.ORANGE);

                e.consume();
            }
        });

        markerPath.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                parent.removeUserMarker(markerID);
                //Sparent.drawPointMarkerOnMap();

                // NOT CORRECT WAY LOL
                //parent.clusterMarkerBuilder.update(parent.pointMarkerGroup);
                //parent.addClusterMarkers(parent.clusterMarkerBuilder.getClusterMarkers(), parent.clusterMarkerBuilder.getClusterValues());

                e.consume();
            }
        });
    }

    @Override
    public void setMarkerPosition(double mapWidth, double mapHeight) {

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
