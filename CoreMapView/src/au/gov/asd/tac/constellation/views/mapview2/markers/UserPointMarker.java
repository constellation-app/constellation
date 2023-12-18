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
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Point marker that get generated wherever the user clicks on the map
 *
 * @author altair1673
 */
public class UserPointMarker extends AbstractMarker {

    // Raw marker string for the user point marker
    private String path = "l-35-90 l-45-80 l-10-30 l0-45 l10-25 l15-20 l50-20 l30 0 l50 20 l15 20 l10 25 l0 45 l-10 30 l-45 80 l-35 90";
    private double x;
    private double y;
    private double originalClickY = 0;
    private double scale = 1.0;

    public UserPointMarker(final MapView parent, final int markerID, final double x, final double y, final double scale, final double xOffset, final double yOffset) {
        super(parent, markerID, NO_MARKER_NODE_ID, xOffset, yOffset, AbstractMarker.MarkerType.POINT_MARKER);

        originalClickY = y;
        this.x = x;
        this.y = y;
        this.scale = scale;

        markerPath.setScaleX(scale);
        markerPath.setScaleY(scale);
        markerPath.setOpacity(0.4);

        markerPath.setFill(Color.ORANGE);
        markerPath.setStroke(Color.BLACK);

        // Set up event handlers for user draw point marker
        markerPath.setOnMouseEntered((final MouseEvent e) -> {
            markerPath.setFill(Color.YELLOW);

            e.consume();
        });

        markerPath.setOnMouseExited((final MouseEvent e) -> {
            markerPath.setFill(Color.ORANGE);

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

        path = "M " + x + SeparatorConstants.COMMA + " " + y + " " + path;

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

    public double getScale() {
        return scale;
    }

    public void setScale(final double scale) {
        this.scale = scale;
    }

    public double getOriginalClickY() {
        return originalClickY;
    }

    public void scaleAndReposition(final double scale) {
        setScale(scale);
        markerPath.setScaleX(scale);
        markerPath.setScaleY(scale);

        final double heightDifference = (getY()) - (markerPath.getBoundsInParent().getCenterY() + (markerPath.getBoundsInParent().getHeight() / 2));
        markerPath.setTranslateY(markerPath.getTranslateY() + heightDifference);
    }
}
