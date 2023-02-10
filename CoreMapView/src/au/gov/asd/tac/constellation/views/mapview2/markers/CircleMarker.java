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

import au.gov.asd.tac.constellation.utilities.geospatial.Distance;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import au.gov.tac.constellation.views.mapview2.utillities.MarkerUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author altair1673
 */
public class CircleMarker extends AbstractMarker {

    private double centerX = 0;
    private double centerY = 0;
    private double radius = 0;

    private final double centerYOffset = 149;

    private final Circle circle = new Circle();
    private final Line line = new Line();

    private static final Logger LOGGER = Logger.getLogger("CircleMarkerLogger");

    public CircleMarker(MapView parent, int markerID, double centerX, double centerY, double radius, int xOffset, int yOffset) {
        super(parent, markerID, -99, xOffset, yOffset, AbstractMarker.MarkerType.POLYGON_MARKER);

        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;

        // Set up circle svg
        circle.setCenterX(this.centerX);
        circle.setCenterY(this.centerY);

        // Line that extends from center to edge of circle
        line.setStartX(this.centerX);
        line.setStartY(this.centerY);

        line.setEndX(this.centerX);
        line.setEndY(this.centerY);

        line.setStroke(Color.YELLOW);

        circle.setRadius(radius);
        circle.setOpacity(0.5);
        circle.setFill(Color.BLACK);
        circle.setStroke(Color.BLACK);

        // Set up the SVG path to represent the projected circle
        markerPath.setStroke(Color.BLACK);
        markerPath.setFill(Color.ORANGE);
        markerPath.setOpacity(0.4);

        // Event handler for changing colours when mouse hovers over the projected circle
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

        // Event handler for removing handler when clicked
        markerPath.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                parent.removeUserMarker(markerID);
                e.consume();
            }
        });

    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public Circle getUICircle() {
        return circle;
    }

    public Line getUILine() {
        return line;
    }

    public double getRadius() {
        return radius;
    }

    /**
     * Generates the projected circle
     */
    public void generateCircle() {
        final int EARTH_RADIUS_M = 6_371_008;
        centerY += centerYOffset;

        // Calculate lattitude and longitude fro x and y
        double centerYLat = MarkerUtilities.YToLat(centerY, MapView.mapWidth, MapView.mapHeight);
        double centerXLon = MarkerUtilities.XToLong(centerX, MapView.minLong, MapView.mapWidth, MapView.maxLong - MapView.minLong);

        double vertexY = centerYLat;
        double vertexX = centerXLon + (radius / EARTH_RADIUS_M) * (180 / Math.PI) / Math.cos(centerYLat * (Math.PI / 180));


        String path = "";
        boolean first = true;

        // Projected circle will have 60 vertices
        final int points = 60;
        final double spacing = (2 * Math.PI) / points;

        // Generate vertices
        for (int i = 0; i < points + 1; i++) {
            final double angle = spacing * i;

            // Edge of circle
            vertexX = centerXLon + radius * Math.cos(angle);
            vertexY = centerYLat + radius * Math.sin(angle);

            // Convert edge to x and y from geo coordinates
            vertexX = MarkerUtilities.longToX(vertexX, MapView.minLong, MapView.mapWidth, MapView.maxLong - MapView.minLong);
            vertexY = MarkerUtilities.latToY(vertexY, MapView.mapWidth, MapView.mapHeight) - centerYOffset;


            if (Double.isNaN(vertexX) || Double.isNaN(vertexY)) {
                continue;
            }

            // Append vertex to path string
            if (first) {
                path = "M" + vertexX + "," + vertexY;

                first = false;
            } else {
                path += "L" + vertexX + "," + vertexY;
            }

        }

        markerPath.setContent(path);
    }

    public void setRadius(double radius) {
        this.radius = radius;
        circle.setRadius(radius);
    }

    /**
     * Sets the radius by calculating the distance from the center of the circle
     * to an edge
     *
     * @param edgeX - x coordinate of edge
     * @param edgeY - y coordinate of edge
     */
    public void setRadius(double edgeX, double edgeY) {

        // Set on screen radius to the circle marker
        circle.setRadius(Math.sqrt(Math.pow(edgeX - centerX, 2) + Math.pow(edgeY - centerY, 2)));

        // Calculate radius in geo coordinates
        edgeY += centerYOffset;
        edgeX = MarkerUtilities.XToLong(edgeX, MapView.minLong, MapView.mapWidth, MapView.maxLong - MapView.minLong);
        edgeY = MarkerUtilities.YToLat(edgeY, MapView.mapWidth, MapView.mapHeight) - centerYOffset;

        double newCenterY = centerY + centerYOffset;
        double X = MarkerUtilities.XToLong(centerX, MapView.minLong, MapView.mapWidth, MapView.maxLong - MapView.minLong);
        double Y = MarkerUtilities.YToLat(newCenterY, MapView.mapWidth, MapView.mapHeight) - centerYOffset;

        // Caclulate distance with geo coordinates
        double distance = Math.sqrt(
                Math.pow((edgeX - X), 2)
                + Math.pow((edgeY - Y), 2));

        // Set the radius variable to the class to the geo distance
        this.radius = distance;

    }

    public void setLineEnd(double x, double y) {
        line.setEndX(x);
        line.setEndY(y);
    }

}
