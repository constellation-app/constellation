/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.views.mapview2.MapDetails;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;

/**
 * Marker that represent the shapes the user draws
 *
 * @author altair1673
 */
public class PolygonMarker extends AbstractMarker {

    private final List<Line> polygonLineUI = new ArrayList<>();
    private Line currentLine = null;
    private String rawPath = "";

    public PolygonMarker(final MapView parent, final int markerID) {
        super(parent, markerID, NO_MARKER_NODE_ID, AbstractMarker.MarkerType.POLYGON_MARKER);

        markerPath.setFill(MapDetails.MARKER_USER_DRAWING_FILL_COLOUR);
        markerPath.setStroke(MapDetails.MARKER_STROKE_COLOUR);
        markerPath.setStrokeWidth(MapDetails.MARKER_LINE_WIDTH * this.scalingFactor);
        this.scaleMarker(parent.getCurrentScale());

        // Event handler for the polygon marker
        markerPath.setOnMouseEntered((final MouseEvent e) -> {
            markerPath.setFill(MapDetails.MARKER_HIGHLIGHTED_FILL_COLOUR);

            e.consume();
        });

        markerPath.setOnMouseExited((final MouseEvent e) -> {
            markerPath.setFill(MapDetails.MARKER_USER_DRAWN_FILL_COLOUR);

            e.consume();
        });

        markerPath.setOnMouseClicked((final MouseEvent e) -> {
            if (parent.TOOLS_OVERLAY.getDrawingEnabled().get()) {
                parent.removeUserMarker(markerID);
            }
            e.consume();
        });

    }

    /**
     * Adds a new line to the polygon marker
     *
     * @param prevLineEndX
     * @param prevLineEndY
     * @return
     */
    public Line addNewLine(final double prevLineEndX, final double prevLineEndY) {
        // If current line is null it mean the first line is being drawn
        // Else finish off the current line and add it to the UI
        if (currentLine != null) {
            setEnd(prevLineEndX, prevLineEndY);
            polygonLineUI.add(currentLine);
        }

        currentLine = new Line();
        currentLine.setStartX(prevLineEndX);
        currentLine.setStartY(prevLineEndY);
        setEnd(prevLineEndX, prevLineEndY);
        currentLine.setStrokeWidth(MapDetails.MARKER_LINE_WIDTH * this.scalingFactor);
        currentLine.setStroke(MapDetails.MARKER_STROKE_COLOUR);
        return currentLine;
    }

    /**
     * Clear the gui that shows the polygon the user is drawing
     */
    public void endDrawing() {
        currentLine = null;
        polygonLineUI.clear();
    }

    public void setEnd(final double x, final double y) {
        if (currentLine != null) {
            currentLine.setEndX(x);
            currentLine.setEndY(y);
        }
    }

    /**
     * Generate the polygon SVG shape
     */
    public void generatePath() {
        String path = "";

        // generate raw path based on vertices
        if (!polygonLineUI.isEmpty()) {
            for (int i = 0; i < polygonLineUI.size(); i++) {
                if (i == 0) {
                    path = "M" + polygonLineUI.get(i).getStartX() + SeparatorConstants.COMMA + polygonLineUI.get(i).getStartY();
                }

                path += "L" + polygonLineUI.get(i).getEndX() + SeparatorConstants.COMMA + polygonLineUI.get(i).getEndY();
            }

            // Connect polygon mmarker back to the start to complete the shape
            path += "L" + polygonLineUI.get(0).getStartX() + SeparatorConstants.COMMA + polygonLineUI.get(0).getStartY();
            if (polygonLineUI.size() == 1) {
                markerPath.setStroke(MapDetails.MARKER_USER_DRAWN_LINE_COLOUR);
                this.type = AbstractMarker.MarkerType.LINE_MARKER;
            }
        }

        rawPath = path;
        markerPath.setFill(MapDetails.MARKER_USER_DRAWN_FILL_COLOUR);
        markerPath.setContent(path);
    }

    public Line getCurrentLine() {
        return currentLine;
    }

    public String getRawPath() {
        return rawPath;
    }
    
    @Override
    public void scaleMarker(final double scalingFactor) {
        // As the map increases in scale, marker lines need to reduce to ensure they continue to appear the same size.
        this.scalingFactor = 1 / scalingFactor;
        markerPath.setStrokeWidth(MapDetails.MARKER_LINE_WIDTH * this.scalingFactor);
    }
}