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

    private final Circle circle = new Circle();
    private final Line line = new Line();
    private final SVGPath projectedCircle = new SVGPath();

    private static final Logger LOGGER = Logger.getLogger("CircleMarkerLogger");

    public CircleMarker(MapViewTopComponent parentComponent, int markerID, double centerX, double centerY, double radius, int xOffset, int yOffset) {
        super(parentComponent, markerID, -99, xOffset, yOffset);

        this.centerX = centerX; //+ xOffset;
        this.centerY = centerY; //+ yOffset;
        this.radius = radius;

        circle.setCenterX(this.centerX);
        circle.setCenterY(this.centerY);

        line.setStartX(this.centerX);
        line.setStartY(this.centerY);

        line.setEndX(this.centerX);
        line.setEndY(this.centerY);

        line.setStroke(Color.YELLOW);

        circle.setRadius(radius);
        circle.setOpacity(0.5);
        //circle.setStrokeWidth(5);
        circle.setFill(Color.BLACK);
        circle.setStroke(Color.BLACK);

        projectedCircle.setStroke(Color.BLACK);
        projectedCircle.setFill(Color.ORANGE);
        projectedCircle.setOpacity(0.4);

        projectedCircle.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                projectedCircle.setFill(Color.YELLOW);

                e.consume();
            }
        });

        projectedCircle.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                projectedCircle.setFill(Color.ORANGE);

                e.consume();
            }
        });

        projectedCircle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                parentComponent.removeUserMarker(markerID);
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

    public void generateCircle() {
        final int EARTH_RADIUS_M = 6_371_008;
        double temp = centerX;
        centerY += 149;
        double centerYLat = super.YToLat(centerY, 1010.33, 1224);
        double centerXLon = super.XToLong(centerX, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);

        double vertexY = centerYLat;
        double vertexX = centerXLon + (radius / EARTH_RADIUS_M) * (180 / Math.PI) / Math.cos(centerYLat * (Math.PI / 180));

        double newX = super.longToX(vertexX, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);
        LOGGER.log(Level.SEVERE, "Circle radius: " + radius);

        //radius = Math.sqrt((Math.pow((newX - centerX), 2) + Math.pow(0, 2)));
        //double edgeLat = centerY + (radius / EARTH_RADIUS_M) * (180 / Math.PI);
        //double edgeLon = centerX + (radius / EARTH_RADIUS_M) * (180 / Math.PI) / Math.cos(centerY * Math.PI / 180);

        //LOGGER.log(Level.SEVERE, "Edge long: " + edgeLon + ", Edge lat: " + edgeLat);

        String path = "";
        boolean first = true;
        final int points = 60;
        final double spacing = (2 * Math.PI) / points;
        for (int i = 0; i < points + 1; i++) {
            final double angle = spacing * i;

            //radius = radius - ((EARTH_RADIUS_M * 2 * Math.PI) / 360) * angle;
            vertexX = centerXLon + radius * Math.cos(angle);
            vertexY = centerYLat + radius * Math.sin(angle);

            //vertexY = centerYLat + ((vertexY - centerYLat) / EARTH_RADIUS_M) * (180 / Math.PI);
            //vertexX = centerXLon + ((vertexX - centerXLon) / EARTH_RADIUS_M) * (180 / Math.PI) / Math.cos(centerYLat * (Math.PI / 180));

            /*double coef = radius / 111.32;
            double vertexY = centerY + coef;
            double vertexX = centerX + coef / Math.cos(centerY * (Math.PI / 180));*/

            vertexX = super.longToX(vertexX, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);
            vertexY = super.latToY(vertexY, 1010.33, 1224) - 149;


            if (Double.isNaN(vertexX) || Double.isNaN(vertexY)) {
                continue;
            }

            if (first) {
                path = "M" + vertexX + "," + vertexY;

                first = false;
            } else {
                path += "L" + vertexX + "," + vertexY;
            }

        }
        LOGGER.log(Level.SEVERE, "Circle path: " + path);
        projectedCircle.setContent(path);
    }

    public void setRadius(double radius) {
        this.radius = radius;
        circle.setRadius(radius);
    }

    public void setRadius(double edgeX, double edgeY) {

        circle.setRadius(Math.sqrt(Math.pow(edgeX - centerX, 2) + Math.pow(edgeY - centerY, 2)));
        edgeY += 149;
        edgeX = super.XToLong(edgeX, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);
        edgeY = super.YToLat(edgeY, 1010.33, 1224) - 149;

        double newCenterY = centerY + 149;

        //centerY += 149;
        double X = super.XToLong(centerX, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);
        double Y = super.YToLat(newCenterY, 1010.33, 1224) - 149;

        /*edgeX = edgeX / (180 / Math.PI);
        edgeY = edgeY / (180 / Math.PI);
        X = X / (180 / Math.PI);
        Y = Y / (180 / Math.PI);

        double dlon = edgeX - X;
        double dlat = edgeY - Y;

        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(Y) * Math.cos(edgeY) * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        double distance = 6371 * c;*/
        double distance = Math.sqrt(
                Math.pow((edgeX - X), 2)
                + Math.pow((edgeY - Y), 2));

        this.radius = distance;
        //circle.setRadius(distance);
    }

    public void setLineEnd(double x, double y) {
        line.setEndX(x);
        line.setEndY(y);
    }

    @Override
    public SVGPath getMarker() {
        return projectedCircle;
    }
}
