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
package au.gov.asd.tac.constellation.views.mapview2.overlays;

import au.gov.asd.tac.constellation.views.mapview2.utilities.MapConversions;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Overlay that shows the exact geo-coordinates of wherever the user hovers
 * their mouse on the map. This appears on a second line under the toolbar The
 * positionX and positionY variables needed to pass to the abstract overlay have
 * no effect
 *
 * @author altair1673
 */
public class InfoOverlay extends AbstractOverlay {


    private Label lonText = null;
    private Label latText = null;
    private Label zoomFactor = null;
    private Label mapScaleText = null;
    private Group mapScaleImage = null;
    private final DecimalFormat df = new DecimalFormat("#.###");
    final SVGPath pathImage = new SVGPath();
    //private final GridPane infoGrid = new GridPane();

    /**
     * Set up the UI
     *
     * @param positionX
     * @param positionY
     */
    public InfoOverlay() {
        super(0, 0);
        lonText = new Label();
        latText = new Label();
        zoomFactor = new Label();
        mapScaleText = new Label();
        mapScaleImage = new Group();
        final String path = "M 0,0 l 0,4 m 0,-2 l 50,0 m 0,-2 l 0,4 ";
        
        pathImage.setContent(path);
        pathImage.setStroke(Color.AQUAMARINE);
        pathImage.setStrokeWidth(1.5);
        mapScaleImage.getChildren().add(pathImage);
        mapScaleText.setGraphic(mapScaleImage);
        // create a container/pane/group/box and put all the info components in there, then show that as the info panel
        gridPane.add(latText, 0, 0);
        gridPane.add(lonText, 1, 0);
        gridPane.add(zoomFactor, 2, 0);
        gridPane.add(mapScaleText, 3, 0);
        gridPane.setPadding(new Insets(0,0,4,4));

        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(120), new ColumnConstraints(120), new ColumnConstraints(180), new ColumnConstraints(380));
        
        overlayPane.setOpacity(0);
    }

    /**
     * Update the location UI
     *
     * @param x
     * @param y
     */
    public void updateLocation(final double x, final double y) {
        final double lon = MapConversions.mapXToLon(x);
        final double lat = MapConversions.mapYToLat(y);

        df.setRoundingMode(RoundingMode.CEILING);

        lonText.setText("Longitude: " + df.format(lon) + "°");
        latText.setText("Latitude: " + df.format(lat) + "°");
    }

    public void updateZoomLabel(final double zoomLevel) {        
        zoomFactor.setText("Zoom Level: " + df.format(zoomLevel));
    }
    
    public void updateMapScaleText(final double scaledDistance){
        double equivalentDistance = 1000.0;
        double toggleDivisor = 2.0; // toggle between 2 and 5
        double plotDistance = scaledDistance;
        while (plotDistance > 300) { // limited space to draw the scale line
            plotDistance /= toggleDivisor;
            equivalentDistance /= toggleDivisor;
            toggleDivisor = 10/toggleDivisor;
        }
        final String path = "M 0,0 l 0,4 m 0,-2 l %d,0 m 0,-2 l 0,4 ".formatted((int) plotDistance);
        mapScaleImage.getChildren().remove(pathImage);
        pathImage.setContent(path);
        mapScaleImage.getChildren().add(pathImage);        
        mapScaleText.setText(" %d km".formatted((int) equivalentDistance));
    }
    
    public Label getLonText() {
        return lonText;
    }

    public Label getLatText() {
        return latText;
    }

    public Pane getInfoGrid() {
        return gridPane;
    }
}
