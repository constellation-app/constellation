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
import au.gov.tac.constellation.views.mapview2.utillities.IntersectionNode;
import au.gov.tac.constellation.views.mapview2.utillities.Vec3;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 *
 * @author altair1673
 */
public class ThiessenPolygonsLayer extends AbstractMapLayer {

    private final Group layer;

    private ImageView imageView;
    private Image img;

    private int nodeID = 0;

    private static final Logger LOGGER = Logger.getLogger("ThiessenPolygons");

    private Map<String, AbstractMarker> markers;

    private Map<String, IntersectionNode> intersectionMap = new HashMap<String, IntersectionNode>();
    private Map<Line, ArrayList<IntersectionNode>> lineMap = new HashMap<Line, ArrayList<IntersectionNode>>();

    private final Map<Integer, PointMarker> nodesOnScreen = new HashMap<Integer, PointMarker>();
    private Map<String, Line> bisectorLines = new HashMap<String, Line>();
    private Set<String> calculatedPairs = new HashSet<>();
    private Map<String, Line> finalBisectorLines = new HashMap<String, Line>();

    private final Line top;
    private final Line left;
    private final Line right;
    private final Line bottom;

    public ThiessenPolygonsLayer(MapView parent, int id, Map<String, AbstractMarker> markers) {
        super(parent, id);
        layer = new Group();

        top = new Line();
        top.setStartX(0);
        top.setStartY(0);
        top.setEndX(MapView.mapWidth);
        top.setEndY(0);
        top.setStroke(Color.RED);
        top.setStrokeWidth(5);
        layer.getChildren().add(top);

        left = new Line();
        left.setStartX(0);
        left.setStartY(0);
        left.setEndX(0);
        left.setEndY(MapView.mapHeight);
        left.setStroke(Color.RED);
        left.setStrokeWidth(5);
        layer.getChildren().add(left);

        right = new Line();
        right.setStartX(MapView.mapWidth);
        right.setStartY(0);
        right.setEndX(MapView.mapWidth);
        right.setEndY(MapView.mapHeight);
        right.setStroke(Color.RED);
        right.setStrokeWidth(5);
        layer.getChildren().add(right);

        bottom = new Line();
        bottom.setStartX(0);
        bottom.setStartY(MapView.mapHeight);
        bottom.setEndX(MapView.mapWidth);
        bottom.setEndY(MapView.mapHeight);
        bottom.setStroke(Color.RED);
        bottom.setStrokeWidth(5);
        layer.getChildren().add(bottom);

        this.markers = markers;

    }

    private void sortNodes(Map<String, AbstractMarker> markers) {
        nodesOnScreen.clear();

        for (AbstractMarker marker : markers.values()) {
            if (marker instanceof PointMarker) {
                PointMarker p = (PointMarker) marker;

                /*Rectangle r = new Rectangle();
                r.setWidth(5);
                r.setHeight(5);

                r.setX(p.getX() - 97);
                r.setY(p.getY() + 93);
                r.setFill(Color.RED);
                layer.getChildren().addAll(r);*/

                nodesOnScreen.put(nodeID++, p);
            }
        }

    }

    private void calculateBisectors() {
        bisectorLines.clear();
        calculatedPairs.clear();
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

                        /*Circle c = new Circle();
                        c.setRadius(5);

                        c.setCenterX(midPoint.x);
                        c.setCenterY(midPoint.y);
                        c.setFill(Color.BLUE);
                        layer.getChildren().addAll(c);*/


                        Vec3 slope = new Vec3((coords2.y - coords1.y), (coords2.x - coords1.x));

                        double reciprocal = -1 * (slope.y / slope.x);

                        double b = midPoint.y - (reciprocal * midPoint.x);

                        Vec3 lineStart = new Vec3(0, b);
                        Vec3 lineEnd = new Vec3(-b / reciprocal, 0);

                        double distance = Vec3.getDistance(lineStart, lineEnd);

                        Vec3 directVect = new Vec3((lineEnd.x - lineStart.x) / distance, (lineEnd.y - lineStart.y) / distance);

                        lineStart.x = midPoint.x + (directVect.x * 1000);
                        lineStart.y = midPoint.y + (directVect.y * 1000);

                        lineEnd.x = midPoint.x - (directVect.x * 1000);
                        lineEnd.y = midPoint.y - (directVect.y * 1000);

                        Line line = new Line();
                        line.setStartX(lineStart.x);
                        line.setStartY(lineStart.y);

                        line.setEndX(lineEnd.x);
                        line.setEndY(lineEnd.y);

                        /*Circle sc = new Circle();
                        sc.setRadius(5);
                        sc.setFill(Color.YELLOW);
                        sc.setCenterX(line.getStartX());
                        sc.setCenterY(line.getStartY());
                        layer.getChildren().addAll(sc);*/

                        //LOGGER.log(Level.SEVERE, "Before scaling y: " + line.getEndY());

                        //line.setScaleX(30);


                        //line.setScaleY(30);

                        //LOGGER.log(Level.SEVERE, "After scaling y: " + line.getEndY());

                        //line.setScaleX(-30);
                        //line.setScaleY(-30);

                        //LOGGER.log(Level.SEVERE, "After scaling y: " + line.getEndY());

                        //line.setStrokeWidth(0.03);

                        /*if (line.getStartX() < 0) {
                            line.setStartX(0);
                        }

                        if (line.getStartY() < 0) {
                            line.setStartY(0);
                        }*/

                        line.setStroke(Color.RED);
                        bisectorLines.put(idPair, line);
                    }
                }
            }
        }
    }

    private void shortenBisectorLines() {
        int count = 0;
        for (String key : bisectorLines.keySet()) {
            Integer id1 = Integer.parseInt(key.split("-")[0]);
            Integer id2 = Integer.parseInt(key.split("-")[1]);

            Line bisector = bisectorLines.get(key);

            Vec3 start = new Vec3(bisector.getStartX(), bisector.getStartY());
            Vec3 end = new Vec3(bisector.getEndX(), bisector.getEndY());

            double distance = Vec3.getDistance(start, end);
            Vec3 dirVect = new Vec3((end.x - start.x) / distance, (end.y - start.y) / distance);

            Vec3[] shortLine = {null, null};

            int index = 0;

            Vec3 marker1 = new Vec3(nodesOnScreen.get(id1).getX() - 97, nodesOnScreen.get(id1).getY() + 93);
            Vec3 marker2 = new Vec3(nodesOnScreen.get(id2).getX() - 97, nodesOnScreen.get(id2).getY() + 93);
            for (int i = 0; i < distance; ++i) {

                if (start.x > MapView.mapWidth + 2 || start.x < -2 || start.y < -2 || start.y > MapView.mapHeight + 2) {
                    start.x += dirVect.x;
                    start.y += dirVect.y;
                    continue;
                }

                Integer shortestDistanceID = null;
                for (Integer id : nodesOnScreen.keySet()) {
                    if (id.intValue() != id1.intValue() && id.intValue() != id2.intValue()) {
                        Vec3 markerPos = new Vec3(nodesOnScreen.get(id).getX() - 97, nodesOnScreen.get(id).getY() + 93);

                        if (Vec3.getDistance(start, markerPos) < Vec3.getDistance(start, marker1) && Vec3.getDistance(start, markerPos) < Vec3.getDistance(start, marker2)) {
                            shortestDistanceID = id;
                            break;
                        }
                    }

                }

                if (shortestDistanceID == null && index == 0) {
                    shortLine[index] = new Vec3(start.x, start.y);
                    ++index;
                } else if (shortestDistanceID != null && index == 1) {
                    start.x += dirVect.x;
                    start.y += dirVect.y;
                    shortLine[0].x -= dirVect.x;
                    shortLine[0].y -= dirVect.y;
                    shortLine[index] = new Vec3(start.x, start.y);

                    break;
                }

                start.x += dirVect.x;
                start.y += dirVect.y;


            }


            if (shortLine[1] == null) {
                //LOGGER.log(Level.SEVERE, "Line end is null");
                shortLine[1] = end;
            }



            if (shortLine[0] != null && shortLine[1] != null) {
                Line l = new Line();
                l.setStartX(shortLine[0].x);
                l.setStartY(shortLine[0].y);

                l.setEndX(shortLine[1].x);
                l.setEndY(shortLine[1].y);

                finalBisectorLines.put(id1 + "-" + id2, l);
                lineMap.put(finalBisectorLines.get(id1 + "-" + id2), new ArrayList<IntersectionNode>());
            }

        }
        //finalBisectorLines.add(top);
        //finalBisectorLines.add(bottom);
        //finalBisectorLines.add(left);
        //finalBisectorLines.add(right);
    }

    private void calculateIntersectionCircles() {
        int duplicateIntersectionPoint = 0;
        Map<String, String> bisectPairs = new HashMap<String, String>();
        Set<String> intersections = new HashSet<String>();
        for (String bisectID1 : finalBisectorLines.keySet()) {
            for (String bisectID2 : finalBisectorLines.keySet()) {
                Line bisect1 = finalBisectorLines.get(bisectID1);
                Line bisect2 = finalBisectorLines.get(bisectID2);
                if (!bisectID1.equals(bisectID2) && doesIntersect(bisect1, bisect2)) {
                    Vec3 slope = new Vec3((bisect1.getEndY() - bisect1.getStartY()), (bisect1.getEndX() - bisect1.getStartX()));

                    double m1 = slope.x / slope.y;

                    double b1 = bisect1.getStartY() - (m1 * bisect1.getStartX());

                    Vec3 slope2 = new Vec3((bisect2.getEndY() - bisect2.getStartY()), (bisect2.getEndX() - bisect2.getStartX()));

                    double m2 = slope2.x / slope2.y;

                    double b2 = bisect2.getStartY() - (m2 * bisect2.getStartX());

                    double x = (b2 - b1) / (m1 - m2);

                    double y = m1 * x + b1;

                    String intersectionPoint = x + "-" + y;

                    if (!intersections.contains(intersectionPoint)) {
                        intersections.add(intersectionPoint);
                        Circle c = new Circle();


                        c.setRadius(5);
                        c.setCenterX(x);
                        c.setCenterY(y);

                        c.setFill(Color.GREEN);

                        layer.getChildren().add(c);
                    } else {
                        //LOGGER.log(Level.SEVERE, "Duplicate: " + (++duplicateIntersectionPoint));
                    }

                    if (!intersectionMap.containsKey(intersectionPoint)) {
                        IntersectionNode intersectionNode = new IntersectionNode(x, y);
                        intersectionNode.getRelevantMarkers().add(Integer.parseInt(bisectID1.split("-")[0]));
                        intersectionNode.getRelevantMarkers().add(Integer.parseInt(bisectID1.split("-")[1]));


                        intersectionMap.put(intersectionPoint, intersectionNode);
                    } else {
                        IntersectionNode intersectionNode = intersectionMap.get(intersectionPoint);
                        intersectionNode.getRelevantMarkers().add(Integer.parseInt(bisectID1.split("-")[0]));
                        intersectionNode.getRelevantMarkers().add(Integer.parseInt(bisectID1.split("-")[1]));
                    }

                    //for(Line l : lineMap.keySet())
                    //{
                    if (bisect2.contains(intersectionMap.get(intersectionPoint).getX(), intersectionMap.get(intersectionPoint).getY())) {
                        for (IntersectionNode i : lineMap.get(bisect2)) {
                            i.addConncectedPoint(intersectionMap.get(intersectionPoint));
                        }
                    }
                    //}
                }
            }
        }

        /*for (String key : intersectionMap.keySet()) {
            LOGGER.log(Level.SEVERE, key);
        }*/

    }

    private Set<String> visited = new HashSet<String>();

    public void printGraph(IntersectionNode node) {
        if (visited.contains(node.getKey())) {
            return;
        }

        visited.add(node.getKey());

        Circle c = new Circle();

        c.setRadius(5);
        c.setCenterX(node.getX());
        c.setCenterY(node.getY());

        c.setFill(Color.BLACK);

        layer.getChildren().add(c);

        for (IntersectionNode connected : node.getConnectedPoints()) {
            printGraph(connected);
        }

    }

    private boolean doesIntersect(Line l1, Line l2) {
        return l1.intersects(l2.getBoundsInLocal());
    }

    @Override
    public void setUp() {
        layer.getChildren().clear();

        sortNodes(markers);
        calculateBisectors();
        shortenBisectorLines();
        calculateIntersectionCircles();
        printGraph(intersectionMap.entrySet().iterator().next().getValue());

        for (Line line : finalBisectorLines.values()) {
            layer.getChildren().add(line);
        }

        Line line = new Line();
        line.setStartX(200);
        line.setStartY(200);
        line.setEndX(201);
        line.setEndY(200);
        line.setStroke(Color.RED);
        layer.getChildren().add(line);

        /*Rectangle border = new Rectangle();
        border.setX(0);
        border.setY(0);
        border.setWidth(MapView.mapWidth);
        border.setHeight(MapView.mapHeight);
        border.setStroke(Color.GREEN);
        border.setStrokeWidth(10);
        border.setOpacity(0);
        layer.getChildren().add(border);*/

        //layer.getChildren().addAll(imageView);
    }

    @Override
    public Group getLayer() {
        return layer;
    }
}
