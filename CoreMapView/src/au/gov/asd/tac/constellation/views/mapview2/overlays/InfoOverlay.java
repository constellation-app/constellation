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
package au.gov.asd.tac.constellation.views.mapview2.overlays;

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MarkerUtilities;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javafx.scene.control.Label;

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

    private static final double LOCATION_Y_OFFSET = 149;

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
        overlayPane.setOpacity(0);
    }

    /**
     * Update the location UI
     *
     * @param x
     * @param y
     */
    public void updateLocation(final double x, final double y) {
        final double lon = MarkerUtilities.xToLong(x, MapView.MIN_LONG, MapView.MAP_WIDTH, MapView.MAX_LONG - MapView.MIN_LONG);
        final double lat = MarkerUtilities.yToLat(y + LOCATION_Y_OFFSET, MapView.MAP_WIDTH, MapView.MAP_HEIGHT);

        final DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);

        lonText.setText(df.format(lon) + "°");
        latText.setText(df.format(lat) + "°");
    }

    public Label getLonText() {
        return lonText;
    }

    public Label getLatText() {
        return latText;
    }

}
