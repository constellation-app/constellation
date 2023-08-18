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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.Arc;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.ArcTree;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.BlineElement;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.HalfEdge;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.SiteEvent;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.VoronoiEvent;
import au.gov.asd.tac.constellation.views.mapview2.utilities.BeachLine;

import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * More efficient away of calculating closest zones but will implement in the
 * future
 *
 * @author altair1673
 */
public class ThiessenPolygonsLayer2 extends AbstractMapLayer {

    private static final Logger LOGGER = Logger.getLogger(ThiessenPolygonsLayer2.class.getName());

    private final Group layer;
    private final Group arcLayer;
    private final Map<Integer, AbstractMarker> nodesOnScreen = new HashMap<>();

    private List<AbstractMarker> markers = new ArrayList<>();

    private final PriorityQueue<VoronoiEvent> eventQueue;
    private ArcTree arcTree;

    public ThiessenPolygonsLayer2(final MapView parent, final int id, final List<AbstractMarker> markers) {
        super(parent, id);

        layer = new Group();
        arcLayer = new Group();
        layer.getChildren().add(arcLayer);
        this.markers = markers;

        eventQueue = new PriorityQueue<VoronoiEvent>((v1, v2) -> {
            if (v1.getYCoord() > v2.getYCoord()) {
                return 1;
            }

            return -1;
        });

    }

    @Override
    public void setUp() {
        layer.getChildren().clear();

        if (markers.size() == 1) {
            final ConstellationColor[] palette = ConstellationColor.createPalette(1);

            Rectangle r = new Rectangle();
            r.setX(0);
            r.setY(0);
            r.setWidth(MapView.MAP_WIDTH);
            r.setHeight(MapView.MAP_HEIGHT);
            r.setFill(palette[0].getJavaFXColor());
            r.setStroke(palette[0].getJavaFXColor());
            r.setOpacity(0.5);
            r.setMouseTransparent(true);
            layer.getChildren().add(r);
        } else if (markers.size() > 1) {
            for (final AbstractMarker m : markers) {
                if (m instanceof PointMarker || m instanceof UserPointMarker) {

                    double x = m.getX() - 97;
                    double y = m.getY() + 93;

                    final SiteEvent siteEvent = new SiteEvent(y, new Vec3(x, y));

                    eventQueue.add(siteEvent);
                }
            }

            calculateVoronoi();
        }

    }

    private void calculateVoronoi() {

        //MapView.testKeyPressed.addListener((oldVal, newVal, changed) -> {
        arcTree = new ArcTree(eventQueue);
        arcTree.run();
        arcLayer.getChildren().clear();
        layer.getChildren().clear();
        layer.getChildren().add(arcLayer);
        final List<Polygon> generatedShapes = arcTree.getCompletedShapes();
        generatedShapes.forEach(shape -> layer.getChildren().add(shape));

        //generateAllArcs(arcTree.root);

        //final List<Line> debugLines = arcTree.getCompletedEdges();
        //debugLines.forEach(line -> layer.getChildren().add(line));
        //});


    }

    private void generateAllArcs(final BlineElement root) {
        if (root == null) {
            return;
        }

        generateAllArcs(root.getLeft());
        generateAllArcs(root.getRight());

        final HalfEdge left = root.getLeftEdge() != null ? root.getLeftEdge() : arcTree.getLeftNeighbour(root) != null ? arcTree.getLeftNeighbour(root).getRightEdge() : null;
        final HalfEdge right = root.getRightEdge() != null ? root.getRightEdge() : arcTree.getRightNeighbour(root) != null ? arcTree.getRightNeighbour(root).getLeftEdge() : null;

        if (left == null || right == null) {
            return;
        }

        final Vec3 leftIntersection = arcTree.getEdgeArcIntersection(left, (Arc) root, arcTree.directrixPos);
        final Vec3 rightIntersection = arcTree.getEdgeArcIntersection(right, (Arc) root, arcTree.directrixPos);

        if (leftIntersection != null && rightIntersection != null) {
            final Arc arc = (Arc) root;
            arc.calculateArc(leftIntersection.getX(), rightIntersection.getX(), arcTree.directrixPos);

            final Line leftLine = new Line();
            leftLine.setStartX(left.getStart().getX());
            leftLine.setStartY(left.getStart().getY());
            leftLine.setEndX(leftIntersection.getX());
            leftLine.setEndY(leftIntersection.getY());
            leftLine.setStroke(Color.GREEN);

            final Line rightLine = new Line();
            rightLine.setStartX(right.getStart().getX());
            rightLine.setStartY(right.getStart().getY());
            rightLine.setEndX(rightIntersection.getX());
            rightLine.setEndY(rightIntersection.getY());
            rightLine.setStroke(Color.GREEN);

            arcLayer.getChildren().add(leftLine);
            arcLayer.getChildren().add(rightLine);

            arcLayer.getChildren().add(arc.getArc());

            arc.getArc().setOnMouseEntered((e) -> {
                leftLine.setStroke(Color.ORANGE);
                rightLine.setStroke(Color.ORANGE);

                final double leftDirX = (leftLine.getEndX() - leftLine.getStartX()) / Math.sqrt((leftLine.getEndX() - leftLine.getStartX()) * (leftLine.getEndX() - leftLine.getStartX()) + (leftLine.getEndY() - leftLine.getStartY()) * (leftLine.getEndY() - leftLine.getStartY()));
                final double leftDirY = (leftLine.getEndY() - leftLine.getStartY()) / Math.sqrt((leftLine.getEndX() - leftLine.getStartX()) * (leftLine.getEndX() - leftLine.getStartX()) + (leftLine.getEndY() - leftLine.getStartY()) * (leftLine.getEndY() - leftLine.getStartY()));
                //LOGGER.log(Level.SEVERE, "Left Line start: " + leftLine.getStartX() + ", " + leftLine.getStartY());
                leftLine.setEndX(leftLine.getStartX() + left.getDirVect().getX() * 1000);
                leftLine.setEndY(leftLine.getStartY() + left.getDirVect().getY() * 1000);

                rightLine.setEndX(rightLine.getStartX() + right.getDirVect().getX() * 1000);
                rightLine.setEndY(rightLine.getStartY() + right.getDirVect().getY() * 1000);

                //LOGGER.log(Level.SEVERE, "Left line Direction X : " + left.getDirVect().getX() + " Direction Y: " + left.getDirVect().getY());
                //LOGGER.log(Level.SEVERE, "Right line Direction X : " + right.getDirVect().getX() + " Direction Y: " + right.getDirVect().getY());

            });

            arc.getArc().setOnMouseExited((e) -> {
                leftLine.setEndX(leftIntersection.getX());
                leftLine.setEndY(leftIntersection.getY());
                rightLine.setEndX(rightIntersection.getX());
                rightLine.setEndY(rightIntersection.getY());
                leftLine.setStroke(Color.GREEN);
                rightLine.setStroke(Color.GREEN);
            });
        }
    }

    @Override
    public Group getLayer() {
        return layer;
    }

}
