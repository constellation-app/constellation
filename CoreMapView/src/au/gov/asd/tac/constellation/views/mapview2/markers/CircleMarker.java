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

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MapConversions;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * Circle marker that is projected onto the map
 *
 * @author altair1673
 */
public class CircleMarker extends AbstractMarker {

    private double centerX = 0;
    private double centerY = 0;
    private double radius = 0;

    private final Circle circle = new Circle();
    private final Line line = new Line();

    public CircleMarker(final MapView parent, final int markerID, final double centerX, final double centerY, final double radius, final double xOffset, final double yOffset) {
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
        line.setStrokeWidth(parent.getScaledMapLineWidth() * 20);

        circle.setRadius(radius);
        circle.setOpacity(0.5);
        circle.setFill(Color.BLACK);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(parent.getScaledMapLineWidth() * 20);


        // Set up the SVG path to represent the projected circle
        markerPath.setStroke(Color.BLACK);
        markerPath.setFill(Color.ORANGE);
        markerPath.setOpacity(0.4);
        markerPath.setStrokeWidth(parent.getScaledMapLineWidth() * 20);


        // Event handler for changing colours when mouse hovers over the projected circle
        markerPath.setOnMouseEntered(e -> {
            markerPath.setFill(Color.YELLOW);
            e.consume();
        });

        markerPath.setOnMouseExited(e -> {
            markerPath.setFill(Color.ORANGE);
            e.consume();
        });

        // Event handler for removing handler when clicked
        markerPath.setOnMouseClicked(e -> {
            if (parent.TOOLS_OVERLAY.getDrawingEnabled().get()) {
                parent.removeUserMarker(markerID);
            }
            e.consume();
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
        // Calculate lattitude and longitude from x and y
        final double centerYLat = MapConversions.mapYToLat(centerY);
        final double centerXLon = MapConversions.mapXToLon(centerX);

        boolean first = true;
        final StringBuilder pathStringBuilder = new StringBuilder();
        // Projected circle will have 60 vertices
        final int points = 60;
        final double spacing = (2 * Math.PI) / points;

        // Generate vertices
        for (int i = 0; i < points + 1; i++) {
            final double angle = spacing * i;

            // Edge of circle
            final double vertexX = centerXLon + radius * Math.cos(angle);
            final double vertexY = centerYLat + radius * Math.sin(angle);

            // Convert edge to x and y from geo coordinates
            final double convertedVertexX = MapConversions.lonToMapX(vertexX);
            final double convertedVertexY = MapConversions.latToMapY(vertexY);

            if (Double.isNaN(convertedVertexX) || Double.isNaN(convertedVertexY)) {
                continue;
            }


            // Append vertex to path string
            if (first) {
                pathStringBuilder.append("M");
                first = false;
            } else {
                pathStringBuilder.append("L");
            }
            pathStringBuilder.append(convertedVertexX);
            pathStringBuilder.append(SeparatorConstants.COMMA);
            pathStringBuilder.append(convertedVertexY);
        }

        markerPath.setContent(pathStringBuilder.toString());
    }

    public void setRadius(final double radius) {
        this.radius = radius;
        circle.setRadius(radius);
    }

    /**
     * Sets the radius by calculating the distance from the center of the circle
     * to an edge
     *
     * @param eX - x coordinate of edge
     * @param eY - y coordinate of edge
     */
    public void setRadius(final double eX, final double eY) {
        // Set on screen radius to the circle marker
        circle.setRadius(Math.sqrt(Math.pow(eX - centerX, 2) + Math.pow(eY - centerY, 2)));

        // Calculate radius in geo coordinates
        final double edgeX = MapConversions.mapXToLon(eX);
        final double edgeY = MapConversions.mapYToLat(eY);

        final double x = MapConversions.mapXToLon(centerX);
        final double y = MapConversions.mapYToLat(centerY);

        // Caclulate distance with geo coordinates
        this.radius = Math.sqrt(
                Math.pow((edgeX - x), 2)
                + Math.pow((edgeY - y), 2));
    }

    public void setLineEnd(final double x, final double y) {
        line.setEndX(x);
        line.setEndY(y);
    }

}
