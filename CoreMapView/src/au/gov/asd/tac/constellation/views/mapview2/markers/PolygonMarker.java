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
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author altair1673
 */
public class PolygonMarker extends AbstractMarker {

    private List<Line> polygonLineUI = new ArrayList<Line>();
    private Line currentLine = null;
    private SVGPath polygonMarker = new SVGPath();

    public PolygonMarker(MapViewTopComponent parentComponent, int markerID, int xOffset, int yOffset) {
        super(parentComponent, markerID, -99, xOffset, yOffset);

        polygonMarker.setStroke(Color.BLACK);
        polygonMarker.setFill(Color.ORANGE);
        polygonMarker.setOpacity(0.4);

        polygonMarker.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                polygonMarker.setFill(Color.YELLOW);

                e.consume();
            }
        });

        polygonMarker.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                polygonMarker.setFill(Color.ORANGE);

                e.consume();
            }
        });

        polygonMarker.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                parentComponent.removeUserMarker(markerID);
                e.consume();
            }
        });
    }

    public Line addNewLine(double prevLineEndX, double prevLineEndY) {
        if (currentLine == null) {
            currentLine = new Line();
            currentLine.setStartX(prevLineEndX);
            currentLine.setStartY(prevLineEndY);
            currentLine.setEndX(prevLineEndX);
            currentLine.setEndY(prevLineEndY);
        } else {
            currentLine.setEndX(prevLineEndX);
            currentLine.setEndY(prevLineEndY);
            polygonLineUI.add(currentLine);
            currentLine = new Line();
            currentLine.setStartX(prevLineEndX);
            currentLine.setStartY(prevLineEndY);
            currentLine.setEndX(prevLineEndX);
            currentLine.setEndY(prevLineEndY);
        }
        currentLine.setStroke(Color.BLACK);
        return currentLine;
    }

    public void endDrawing() {
        currentLine = null;
        polygonLineUI.clear();
    }

    public void setEnd(double x, double y) {
        if (currentLine != null) {
            currentLine.setEndX(x);
            currentLine.setEndY(y);
        }
    }

    public void generatePath() {
        String path = "";

        if (!polygonLineUI.isEmpty()) {
            for (int i = 0; i < polygonLineUI.size(); ++i) {
                if (i == 0) {
                    path = "M" + polygonLineUI.get(i).getStartX() + "," + polygonLineUI.get(i).getStartY();
                    path += "L" + polygonLineUI.get(i).getEndX() + "," + polygonLineUI.get(i).getEndY();
                } else {
                    path += "L" + polygonLineUI.get(i).getEndX() + "," + polygonLineUI.get(i).getEndY();
                }
            }

            path += "L" + polygonLineUI.get(0).getStartX() + "," + polygonLineUI.get(0).getStartY();
        }

        polygonMarker.setContent(path);

    }

    public Line getCurrentLine() {
        return currentLine;
    }

    @Override
    public SVGPath getMarker() {
        return polygonMarker;
    }

}
