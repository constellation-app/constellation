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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author altair1673
 */
public abstract class AbstractHeatmapLayer extends AbstractMapLayer {

    protected Group layerGroup;

    private static final Logger LOGGER = Logger.getLogger("Test");

    public AbstractHeatmapLayer(MapView parent) {
        super(parent);
        layerGroup = new Group();
    }

    @Override
    public void setUp() {

        for (Object value : parent.getAllMarkers().values()) {

            if (value instanceof PointMarker) {
                PointMarker marker = (PointMarker) value;
                int x = 1;

                int startingX = (int) marker.getX() - 108;
                int startingY = (int) marker.getY() + 80;

                LOGGER.log(Level.SEVERE, "X coord: " + marker.getX() + "," + marker.getY());

                int lineCounter = 1;
                for (int i = 0; i < 625; ++i) {
                    Rectangle rect = new Rectangle();
                    rect.setX(startingX);
                    rect.setY(startingY);
                    rect.setWidth(1);
                    rect.setHeight(1);
                    rect.setOpacity(0.1);

                    //startingX += 1;

                    if ((i + 1) % 25 == 0) {
                        startingY += 1;
                        lineCounter += 1;
                    } else {
                        startingX += x;
                    }


                    if (lineCounter % 2 == 0) {

                        x = -1;
                    } else {
                        x = 1;
                    }

                    layerGroup.getChildren().add(rect);
                }
            }
        }


        /*path = "M100,100" + path;

        SVGPath box = new SVGPath();
        box.setStrokeWidth(15);
        box.setContent(path);

        layerGroup.getChildren().add(box);*/
    }

    @Override
    public Group getLayer() {
        return layerGroup;
    }

}
