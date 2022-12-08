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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author altair1673
 */
public class ThiessenPolygonsLayer extends AbstractMapLayer {

    private final Group layer;

    private ImageView imageView;
    private Image img;

    private int nodeID = 0;

    private final Map<Integer, PointMarker> nodesOnScreen = new HashMap<Integer, PointMarker>();
    private Map<String, Line> bisectorLines = new HashMap<String, Line>();
    private Set<String> calculatedPairs = new HashSet<>();
    private List<Line> finalBisectorLines = new ArrayList<Line>();

    public ThiessenPolygonsLayer(MapView parent, int id, Map<String, AbstractMarker> markers) {
        super(parent, id);

        imageView = new ImageView();
        imageView.setOpacity(0.5);
        imageView.setTranslateX(0);
        imageView.setTranslateY(0);
        /*imageView.prefWidth(MapView.mapWidth);
        imageView.prefHeight(MapView.mapHeight);
        imageView.maxWidth(MapView.mapWidth);
        imageView.maxHeight(MapView.mapHeight);
        imageView.minWidth(MapView.mapWidth);
        imageView.minHeight(MapView.mapHeight);*/
        imageView.setFitHeight(MapView.mapHeight);
        imageView.setFitWidth(MapView.mapWidth);
        //imageView.
        img = new Image("C:\\Users\\pmazumder\\OneDrive - DXC Production\\Documents\\Work\\cryingMeme.png");
        imageView.setImage(img);

        layer = new Group();
        sortNodes(markers);
        calculateBisectors();
        //shortenBisectorLines();
    }

    private void sortNodes(Map<String, AbstractMarker> markers) {
        nodesOnScreen.clear();

        for (AbstractMarker marker : markers.values()) {
            if (marker instanceof PointMarker) {
                PointMarker p = (PointMarker) marker;

                Rectangle r = new Rectangle();
                r.setWidth(5);
                r.setHeight(5);

                r.setX(p.getX() - 97);
                r.setY(p.getY() + 93);
                r.setFill(Color.RED);
                layer.getChildren().addAll(r);

                nodesOnScreen.put(nodeID++, p);
            }
        }

    }

    private void calculateBisectors() {
        bisectorLines.clear();
        for (Integer id : nodesOnScreen.keySet()) {
            for (Integer id2 : nodesOnScreen.keySet()) {
                if (id.intValue() != id2.intValue()) {
                    String idPair = id + "-" + id2;
                    String idPair2 = id2 + "-" + id;

                    if (!calculatedPairs.contains(idPair) && !calculatedPairs.contains(idPair2)) {
                        calculatedPairs.add(idPair);
                        calculatedPairs.add(idPair2);

                        PointMarker node1 = nodesOnScreen.get(id);
                        PointMarker node2 = nodesOnScreen.get(id2);

                        Vec3 coords1 = new Vec3(node1.getX() - 97, node1.getY() + 93);
                        Vec3 coords2 = new Vec3(node2.getX() - 97, node2.getY() + 93);

                        Vec3 midPoint = new Vec3((coords1.x + coords2.x) / 2, (coords1.y + coords2.y) / 2);

                        Circle c = new Circle();
                        c.setRadius(5);

                        c.setCenterX(midPoint.x);
                        c.setCenterY(midPoint.y);
                        c.setFill(Color.RED);
                        layer.getChildren().addAll(c);


                        Vec3 slope = new Vec3((coords2.y - coords1.y), (coords2.x - coords1.x));

                        double reciprocal = -1 * (slope.y / slope.x);

                        double b = midPoint.y - (reciprocal * midPoint.x);

                        Vec3 lineStart = new Vec3(0, b);
                        Vec3 lineEnd = new Vec3(-b / reciprocal, 0);

                        Line line = new Line();
                        line.setStartX(lineStart.x);
                        line.setStartY(lineStart.y);

                        line.setEndX(lineEnd.x);
                        line.setEndY(lineEnd.y);
                        line.setScaleX(30);
                        line.setScaleY(30);
                        line.setScaleX(-30);
                        line.setScaleY(-30);
                        line.setStrokeWidth(0.03);

                        line.setStroke(Color.RED);
                        bisectorLines.put(idPair, line);
                    }
                }
            }
        }
    }

    private void shortenBisectorLines() {
        for (String key : bisectorLines.keySet()) {
            Integer id1 = Integer.parseInt(key.split("-")[0]);
            Integer id2 = Integer.parseInt(key.split("-")[1]);

            Line bisector = bisectorLines.get(key);

            Vec3 start = new Vec3(bisector.getStartX(), bisector.getStartY());
            Vec3 end = new Vec3(bisector.getEndX(), bisector.getEndY());

            double distance = Vec3.getDistance(start, end);
            Vec3 dirVect = new Vec3((end.x - start.x) / distance, (end.y - start.y) / distance);

            boolean startFound = false;
            boolean endFouund = false;
            for (int i = 0; i < distance; ++i) {

            }

        }
    }

    @Override
    public void setUp() {
        //layer.getChildren().clear();

        for (Line line : bisectorLines.values()) {
            layer.getChildren().add(line);
        }

        Line line = new Line();
        line.setStartX(200);
        line.setStartY(200);
        line.setEndX(201);
        line.setEndY(200);
        line.setStroke(Color.RED);
        layer.getChildren().add(line);

        //layer.getChildren().addAll(imageView);
    }

    @Override
    public Group getLayer() {
        return layer;
    }
}
