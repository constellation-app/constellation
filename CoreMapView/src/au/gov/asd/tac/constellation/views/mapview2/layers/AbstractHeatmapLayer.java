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
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.tac.constellation.views.mapview2.utillities.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Group;
import javafx.scene.paint.Color;
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

        Map<String, AbstractMarker> markers = parent.getAllMarkers();

        float max = 1;
        float min = 0;

        for (Object value : markers.values()) {
            if (value instanceof PointMarker) {
                PointMarker marker = (PointMarker) value;

                max = marker.getWeight() > max ? marker.getWeight() : max;
                min = marker.getWeight() < min ? marker.getWeight() : min;
            }
        }

        LOGGER.log(Level.SEVERE, "min: " + min + " max: " + max);

        Vec3[][] heatMapColours = new Vec3[25][25];


        for (Object value : parent.getAllMarkers().values()) {

            if (value instanceof PointMarker) {
                PointMarker marker = (PointMarker) value;
                int x = 1;

                int startingX = (int) marker.getX() - 108;
                int startingY = (int) marker.getY() + 80;

                float normalizedValue = ((marker.getWeight() - min) / (max - min));

                Vec3 centerColour = getHeatmapColour(normalizedValue);

                populateHeatmapColours(heatMapColours, centerColour);

                LOGGER.log(Level.SEVERE, "normalized value: " + normalizedValue);

                LOGGER.log(Level.SEVERE, "X coord: " + marker.getX() + "," + marker.getY());

                int lineCounter = 1;
                for (int i = 0; i < 625; ++i) {
                    Rectangle rect = new Rectangle();
                    rect.setX(startingX);
                    rect.setY(startingY);
                    rect.setWidth(1);
                    rect.setHeight(1);


                    rect.setFill(new Color(centerColour.x, centerColour.y, centerColour.z, 1.0));
                    //rect.setFill(Color.ORANGE);
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

    private void populateHeatmapColours(Vec3[][] heatmapColours, Vec3 centerColour) {
        int row = (int) ((heatmapColours.length + 1) / 2);
        int col = (int) ((heatmapColours[0].length + 1) / 2);
        heatmapColours[row][col] = centerColour;

        while () {

        }
    }

    private Vec3 getHeatmapColour(float value) {
        //float[][] colours = new float[4][3];

        LOGGER.log(Level.SEVERE, "Value passed in: " + value);

        float[][] colours = {{0, 0, 1}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}};

        int id1;
        int id2;
        float fractBetween = 0;

        if (value <= 0) {
            id1 = id2 = 0;
        } else if (value >= 1) {
            id1 = id2 = colours.length - 1;
        } else {
            value = value * (colours.length - 1);
            id1 = (int) Math.floor(value);
            id2 = id1 + 1;
            fractBetween = value - (float) id1;
        }
        Vec3 heatMapColour = new Vec3();

        LOGGER.log(Level.SEVERE, "id1: " + id1);
        LOGGER.log(Level.SEVERE, "id2: " + id2);

        heatMapColour.x = (colours[id2][0] - colours[id1][0]) * fractBetween + colours[id1][0];
        heatMapColour.y = (colours[id2][1] - colours[id1][1]) * fractBetween + colours[id1][1];
        heatMapColour.z = (colours[id2][2] - colours[id1][2]) * fractBetween + colours[id1][2];

        return heatMapColour;

    }


    @Override
    public Group getLayer() {
        return layerGroup;
    }

}
