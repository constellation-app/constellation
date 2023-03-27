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

import java.util.List;
import javafx.scene.Group;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author altair1673
 */
public class OverviewOverlay extends AbstractOverlay {

    //private static final Pane map = new Pane();
    private final static double MAP_SCALE = 0.2;

    public OverviewOverlay(double positionX, double positionY, final List<SVGPath> countrySVGPaths) {
        super(positionX, positionY);

        overlayPane.setMinWidth(203);
        overlayPane.setMaxWidth(203);
        overlayPane.setMinHeight(245);
        overlayPane.setMaxHeight(245);

        overlayPane.getChildren().clear();
        overlayPane.setBackground(new Background(new BackgroundFill(new Color(0.722, 0.871, 0.902, 1), null, null)));
        final Group countryGroup = new Group();
        for (int i = 0; i < countrySVGPaths.size(); ++i) {
            SVGPath p = new SVGPath();
            p.setContent(countrySVGPaths.get(i).getContent());
            p.setStroke(Color.BLACK);
            p.setFill(Color.WHITE);
            //p.setScaleX(0.5);
            //p.setScaleY(0.5);
            p.setStrokeWidth(0.025);
            countryGroup.getChildren().add(p);
        }
        overlayPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        countryGroup.setScaleX(MAP_SCALE);
        countryGroup.setScaleY(MAP_SCALE);
        //overlayPane.setScaleX(0.2);
        //overlayPane.setScaleY(0.2);
        overlayPane.setCenter(countryGroup);
    }

    @Override
    public Pane getOverlayPane() {
        return overlayPane;
    }

}
