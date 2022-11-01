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

import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

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
        circle.setStrokeWidth(5);
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

    public void setRadius(double radius) {
        this.radius = radius;
        circle.setRadius(radius);
    }

    public void setLineEnd(double x, double y) {
        line.setEndX(x);
        line.setEndY(y);
    }
}
