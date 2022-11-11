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

    private int heatMapWidth = 25;
    private int heatMapHeight = 25;

    private static final Logger LOGGER = Logger.getLogger("ABstractHeatMapLayer");

    /*protected static final String[] RAINBOW = {
        "0x0034f8", "0x0037f6", "0x003af3", "0x003df0", "0x003fed", "0x0041ea", "0x0044e7", "0x0046e4",
        "0x0048e1", "0x004ade", "0x004cdb", "0x004fd8", "0x0051d5", "0x0053d2", "0x0054d0", "0x0056cd",
        "0x0058ca", "0x005ac7", "0x005cc4", "0x005ec1", "0x0060be", "0x0061bb", "0x0063b8", "0x0065b6",
        "0x0066b3", "0x0068b0", "0x006aad", "0x006baa", "0x006da7", "0x006ea5", "0x006fa2", "0x00719f",
        "0x00729d", "0x00739a", "0x007598", "0x007695", "0x077793", "0x0d7890", "0x13798e", "0x187a8b",
        "0x1c7b89", "0x1f7c87", "0x237d84", "0x267e82", "0x287f7f", "0x2b807d", "0x2d817b", "0x2f8278",
        "0x318376", "0x328473", "0x348571", "0x35866f", "0x36876c", "0x37886a", "0x388967", "0x398a65",
        "0x3a8b62", "0x3b8c60", "0x3c8e5d", "0x3c8f5b", "0x3d9058", "0x3d9155", "0x3e9253", "0x3e9350",
        "0x3e944d", "0x3e954a", "0x3e9647", "0x3f9745", "0x3f9842", "0x3e993e", "0x3e9a3b", "0x3e9b38",
        "0x3e9c35", "0x3e9d32", "0x3e9e2e", "0x3e9f2b", "0x3fa027", "0x3fa124", "0x40a221", "0x41a31d",
        "0x42a41a", "0x44a517", "0x45a615", "0x47a713", "0x4aa711", "0x4ca80f", "0x4fa90e", "0x51a90d",
        "0x54aa0d", "0x57ab0d", "0x5aab0d", "0x5dac0d", "0x5fad0d", "0x62ad0e", "0x65ae0e", "0x67ae0e",
        "0x6aaf0f", "0x6db00f", "0x6fb00f", "0x72b110", "0x74b110", "0x77b211", "0x79b211", "0x7cb311",
        "0x7eb412", "0x80b412", "0x83b512", "0x85b513", "0x88b613", "0x8ab613", "0x8cb714", "0x8fb814",
        "0x91b815", "0x93b915", "0x95b915", "0x98ba16", "0x9aba16", "0x9cbb16", "0x9fbb17", "0xa1bc17",
        "0xa3bc18", "0xa5bd18", "0xa7be18", "0xaabe19", "0xacbf19", "0xaebf19", "0xb0c01a", "0xb2c01a",
        "0xb5c11b", "0xb7c11b", "0xb9c21b", "0xbbc21c", "0xbdc31c", "0xc0c31c", "0xc2c41d", "0xc4c41d",
        "0xc6c51d", "0xc8c51e", "0xcac61e", "0xcdc61f", "0xcfc71f", "0xd1c71f", "0xd3c820", "0xd5c820",
        "0xd7c920", "0xd9c921", "0xdcca21", "0xdeca22", "0xe0ca22", "0xe2cb22", "0xe4cb23", "0xe6cc23",
        "0xe8cc23", "0xeacc24", "0xeccd24", "0xeecd24", "0xf0cd24", "0xf2cd24", "0xf3cd24", "0xf5cc24",
        "0xf6cc24", "0xf8cb24", "0xf9ca24", "0xf9c923", "0xfac823", "0xfbc722", "0xfbc622", "0xfcc521",
        "0xfcc421", "0xfcc220", "0xfdc120", "0xfdc01f", "0xfdbe1f", "0xfdbd1e", "0xfebb1d", "0xfeba1d",
        "0xfeb91c", "0xfeb71b", "0xfeb61b", "0xfeb51a", "0xffb31a", "0xffb219", "0xffb018", "0xffaf18",
        "0xffae17", "0xffac16", "0xffab16", "0xffa915", "0xffa815", "0xffa714", "0xffa513", "0xffa413",
        "0xffa212", "0xffa111", "0xff9f10", "0xff9e10", "0xff9c0f", "0xff9b0e", "0xff9a0e", "0xff980d",
        "0xff970c", "0xff950b", "0xff940b", "0xff920a", "0xff9109", "0xff8f08", "0xff8e08", "0xff8c07",
        "0xff8b06", "0xff8905", "0xff8805", "0xff8604", "0xff8404", "0xff8303", "0xff8102", "0xff8002",
        "0xff7e01", "0xff7c01", "0xff7b00", "0xff7900", "0xff7800", "0xff7600", "0xff7400", "0xff7200",
        "0xff7100", "0xff6f00", "0xff6d00", "0xff6c00", "0xff6a00", "0xff6800", "0xff6600", "0xff6400",
        "0xff6200", "0xff6100", "0xff5f00", "0xff5d00", "0xff5b00", "0xff5900", "0xff5700", "0xff5500",
        "0xff5300", "0xff5000", "0xff4e00", "0xff4c00", "0xff4a00", "0xff4700", "0xff4500", "0xff4200",
        "0xff4000", "0xff3d00", "0xff3a00", "0xff3700", "0xff3400", "0xff3100", "0xff2d00", "0xff2a00"};*/
    //protected static final String[] RAINBOW = {"0x0034f8", "0x42a41a", "0xf6cc24", "0xff8b06", "0xff2a00"};
    protected static final String[] RAINBOW = {"0x0000ff", "0x00ff00", "0xffff00", "0xff0000"};

    public AbstractHeatmapLayer(MapView parent, int id) {
        super(parent, id);
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

        Vec3[][] heatMapColours = new Vec3[heatMapHeight][heatMapWidth];
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

                double normalizedValue = ((getWeight(marker) - min) / (max - min));

                Vec3 centerColour = getHeatmapColour(normalizedValue);

                double[][] heatMapWeights = new double[heatMapHeight][heatMapWidth];
                heatMapWeights[(heatMapHeight + 1) / 2][(heatMapHeight + 1) / 2] = normalizedValue;

                //LOGGER.log(Level.SEVERE, "Heatmap center x: " + heatMapColours[13][13].x);

                //LOGGER.log(Level.SEVERE, "normalized value: " + normalizedValue);

                //LOGGER.log(Level.SEVERE, "X coord: " + marker.getX() + "," + marker.getY());

                calculateHeatmapWeights(heatMapWeights, (heatMapHeight + 1) / 2, (heatMapHeight + 1) / 2);
                populateHeatmapColours(heatMapColours, heatMapWeights);
                gaussianBlur(heatMapColours);

                int heatMapRowIndex = 0;
                int heatMapColIndex = 0;

                int lineCounter = 1;
                for (int i = 0; i < heatMapHeight * heatMapWidth; ++i) {
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

                    if ((i + 1) % heatMapWidth == 0) {
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
                        heatMapWeights[(int) neighbours[i].x][(int) neighbours[i].y] = heatMapWeights[(int) current.x][(int) current.y] - 0.085;
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
        //heatmapColours = blurredColours;

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
            id1 = id2 = RAINBOW.length - 1;
        } else {
            value = value * (RAINBOW.length - 1);
            id1 = (int) Math.floor(value);
            id2 = id1 + 1;
            fractBetween = value - id1;
        }
        Vec3 heatMapColour = new Vec3();

        LOGGER.log(Level.SEVERE, "id1: " + id1);
        LOGGER.log(Level.SEVERE, "id2: " + id2);

        Color col1 = Color.web(RAINBOW[id1]);
        Color col2 = Color.web(RAINBOW[id2]);


        /*heatMapColour.x = (col2.getRed() - col1.getRed()) * fractBetween + col1.getRed();
        heatMapColour.y = (col2.getBlue() - col1.getBlue()) * fractBetween + col1.getBlue();
        heatMapColour.z = (col2.getGreen() - col1.getGreen()) * fractBetween + col1.getGreen();*/
        heatMapColour.x = (colours[id2][0] - colours[id1][0]) * fractBetween + colours[id1][0];
        heatMapColour.y = (colours[id2][1] - colours[id1][1]) * fractBetween + colours[id1][1];
        heatMapColour.z = (colours[id2][2] - colours[id1][2]) * fractBetween + colours[id1][2];

        return heatMapColour;

    }


    @Override
    public Group getLayer() {
        return layerGroup;
    }

    public int getWeight(AbstractMarker marker) {
        return 0;
    }

}
