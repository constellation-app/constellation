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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

        double max = 1;
        double min = 0;

        for (Object value : markers.values()) {
            if (value instanceof PointMarker) {
                PointMarker marker = (PointMarker) value;

                max = marker.getWeight() > max ? marker.getWeight() : max;
                min = marker.getWeight() < min ? marker.getWeight() : min;
            }
        }

        LOGGER.log(Level.SEVERE, "min: " + min + " max: " + max);

        Vec3[][] heatMapColours = new Vec3[25][25];
        for (int row = 0; row < heatMapColours.length; ++row) {
            for (int col = 0; col < heatMapColours[0].length; ++col) {
                heatMapColours[row][col] = new Vec3();
            }
        }


        for (Object value : parent.getAllMarkers().values()) {

            if (value instanceof PointMarker) {
                PointMarker marker = (PointMarker) value;
                int x = 1;

                int startingX = (int) marker.getX() - 108;
                int startingY = (int) marker.getY() + 81;

                double normalizedValue = ((marker.getWeight() - min) / (max - min));

                Vec3 centerColour = getHeatmapColour(normalizedValue);

                double[][] heatMapWeights = new double[25][25];
                heatMapWeights[13][13] = normalizedValue;

                //LOGGER.log(Level.SEVERE, "Heatmap center x: " + heatMapColours[13][13].x);

                //LOGGER.log(Level.SEVERE, "normalized value: " + normalizedValue);

                //LOGGER.log(Level.SEVERE, "X coord: " + marker.getX() + "," + marker.getY());

                calculateHeatmapWeights(heatMapWeights, 13, 13);
                populateHeatmapColours(heatMapColours, heatMapWeights);
                gaussianBlur(heatMapColours);

                int heatMapRowIndex = 0;
                int heatMapColIndex = 0;

                int lineCounter = 1;
                for (int i = 0; i < 625; ++i) {
                    Rectangle rect = new Rectangle();
                    rect.setX(startingX);
                    rect.setY(startingY);
                    rect.setWidth(1);
                    rect.setHeight(1);

                    Vec3 boxColour = heatMapColours[heatMapRowIndex][heatMapColIndex];
                    rect.setFill(new Color(boxColour.x, boxColour.y, boxColour.z, 1.0));
                    //rect.setFill(Color.ORANGE);
                    rect.setOpacity(0.1);

                    //startingX += 1;

                    if ((i + 1) % 25 == 0) {
                        startingY += 1;
                        heatMapRowIndex += 1;
                        lineCounter += 1;
                    } else {
                        startingX += x;
                        heatMapColIndex += x;
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

    private void calculateHeatmapWeights(double[][] heatMapWeights, int row, int col) {
        Set<String> coordinateSet = new HashSet<>();

        coordinateSet.add(row + "," + col);
        Deque<Vec3> bfs = new ArrayDeque<>();

        Vec3 coordinate = new Vec3();
        coordinate.x = row;
        coordinate.y = col;

        bfs.addLast(coordinate);

        while (!bfs.isEmpty()) {
            Vec3 current = bfs.pop();
            if (current != null) {
                Vec3[] neighbours = new Vec3[8];

                for (int i = 0; i < neighbours.length; ++i) {
                    neighbours[i] = new Vec3();
                }

                neighbours[0].x = current.x - 1;
                neighbours[0].y = current.y - 1;
                neighbours[1].x = current.x - 1;
                neighbours[1].y = current.y;
                neighbours[2].x = current.x - 1;
                neighbours[2].y = current.y + 1;

                neighbours[3].x = current.x;
                neighbours[3].y = current.y - 1;
                neighbours[4].x = current.x;
                neighbours[4].y = current.y + 1;

                neighbours[5].x = current.x + 1;
                neighbours[5].y = current.y - 1;
                neighbours[6].x = current.x + 1;
                neighbours[6].y = current.y;
                neighbours[7].x = current.x + 1;
                neighbours[7].y = current.y + 1;

                for (int i = 0; i < neighbours.length; ++i) {
                    String currCoord = neighbours[i].x + "," + neighbours[i].y;

                    if (neighbours[i].x >= 0 && neighbours[i].x < heatMapWeights.length && neighbours[i].y >= 0 && neighbours[i].y < heatMapWeights[0].length && !coordinateSet.contains(currCoord)) {
                        coordinateSet.add(currCoord);
                        heatMapWeights[(int) neighbours[i].x][(int) neighbours[i].y] = heatMapWeights[(int) current.x][(int) current.y] - 0.1;
                        bfs.addLast(neighbours[i]);
                    }
                }
            }
        }
    }

    private void populateHeatmapColours(Vec3[][] heatmapColours, double[][] heatmapWeights) {
        if (heatmapWeights.length != heatmapColours.length || heatmapWeights[0].length != heatmapColours[0].length) {
            return;
        }

        for (int row = 0; row < heatmapColours.length; ++row) {
            for (int col = 0; col < heatmapWeights[0].length; ++col) {
                heatmapColours[row][col] = getHeatmapColour(heatmapWeights[row][col]);
            }
        }

    }

    private void gaussianBlur(Vec3[][] heatmapColours) {
        double[] blurKernel = {1.0 / 16.0, 2.0 / 16.0, 1.0 / 16.0, 2.0 / 16.0, 4.0 / 16.0, 2.0 / 16.0, 1.0 / 16.0, 2.0 / 16.0, 1.0 / 16.0};

        //double[] blurKernel = {1 / 5, 2 / 5, 1 / 5, 2 / 5, 4 / 5, 2 / 5, 1 / 5, 2 / 5, 1 / 5};

        int[][] offsets = {{-1, -1}, {-1, 0}, {-1, 1},
        {0, -1}, {0, 0}, {0, 1},
        {1, -1}, {1, 0}, {1, 1}};

        Vec3[] samples = new Vec3[9];

        Vec3[][] blurredColours = new Vec3[heatmapColours.length][heatmapColours[0].length];

        /*for (int i = 0; i < samples.length; ++i) {
            samples[i] = new Vec3();
        }*/

        for (int row = 0; row < heatmapColours.length; ++row) {
            for (int col = 0; col < heatmapColours[0].length; ++col) {
                for (int i = 0; i < offsets.length; ++i) {
                    Vec3 sampledColour = new Vec3();

                    int sampleRow = row + offsets[i][0];
                    int sampleCol = col + offsets[i][1];

                    if (sampleRow >= 0 && sampleRow < heatmapColours.length && sampleCol >= 0 && sampleCol < heatmapColours[0].length) {
                        //LOGGER.log(Level.SEVERE, "Sampling around box");
                        sampledColour = new Vec3(heatmapColours[sampleRow][sampleCol]);

                        if (sampledColour.z != 1.0) {
                            //LOGGER.log(Level.SEVERE, "X : " + sampledColour.x + " y: " + sampledColour.y + " z: " + sampledColour.z);
                        }
                    }

                    samples[i] = new Vec3(sampledColour);

                }

                blurredColours[row][col] = new Vec3();
                LOGGER.log(Level.SEVERE, "Original colour, x : " + heatmapColours[row][col].x + ", y: " + heatmapColours[row][col].y + ", z: " + heatmapColours[row][col].z);
                for (int i = 0; i < blurKernel.length; ++i) {
                    samples[i].multiplyDouble(blurKernel[i]);

                    blurredColours[row][col].addVector(samples[i]);

                    if (heatmapColours[row][col].z != 1.0) {
                        LOGGER.log(Level.SEVERE, "Blurring colour, x : " + blurredColours[row][col].x + ", y: " + blurredColours[row][col].y + ", z: " + blurredColours[row][col].z);
                    }
                }
            }
        }

        for (int row = 0; row < heatmapColours.length; ++row) {
            for (int col = 0; col < heatmapColours[0].length; ++col) {
                heatmapColours[row][col] = new Vec3(blurredColours[row][col]);
            }
        }

    }

    private Vec3 getHeatmapColour(double value) {
        //float[][] colours = new float[4][3];

        LOGGER.log(Level.SEVERE, "Value passed in: " + value);

        //float[][] colours = {{0, 52 / 255, 248 / 255}, {0, 1, 0}, {1, 1, 0}, {1, 42 / 255, 0}};
        double[][] colours = {{0, 0, 1}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}};

        int id1;
        int id2;
        double fractBetween = 0;

        if (value <= 0) {
            id1 = id2 = 0;
        } else if (value >= 1) {
            id1 = id2 = colours.length - 1;
        } else {
            value = value * (colours.length - 1);
            id1 = (int) Math.floor(value);
            id2 = id1 + 1;
            fractBetween = value - id1;
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
