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
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.BaseLine;
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
    private final ArcTree beachLine;

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

        beachLine = new ArcTree(eventQueue);
    }

    @Override
    public void setUp() {
        layer.getChildren().clear();

        if (nodesOnScreen.size() == 1) {
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
        } else {
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

        /*final SiteEvent firstEvent = (SiteEvent) eventQueue.peek();

        final BeachLineElement firstArc = new Parabola(firstEvent.getSite(), firstEvent.getSite(), firstEvent.getSite().getX(), firstEvent.getSite());
        ((Parabola) firstArc).setCurrentEdgeEvent(null);
        //final BeachLineElement firstArc = new Parabola(new Vec3(0, 0), new Vec3(0, 0), MapView.MAP_WIDTH / 2, new Vec3(MapView.MAP_WIDTH / 2, -100));
        beachLine.setRoot(firstArc);
        eventQueue.poll();
        final double startCaseY = ((Parabola) firstArc).getSite().getY() + 1;

        while (!eventQueue.isEmpty() && (eventQueue.peek().getYCoord() < startCaseY)) {
            LOGGER.log(Level.SEVERE, "Running first while loop");
            if (eventQueue.peek().getYCoord() > MapView.MAP_HEIGHT) {
                break;
            }

            final VoronoiEvent event = eventQueue.poll();

            final Vec3 focus = ((SiteEvent) event).getSite();

            final BeachLineElement newArc = new Parabola(new Vec3(focus.getX(), 0), new Vec3(focus.getX(), 0), focus.getX(), focus);

            final BeachLineElement intersectingArc = beachLine.searchArcs((Parabola) newArc, focus.getY());
            final Vec3 edgeStart = new Vec3((focus.getX() + ((Parabola) intersectingArc).getSite().getX()) / 2, focus.getY() - 200); // + 100
            final Vec3 edgeDir = new Vec3(0, -1);

            final Edge newEdge = new Edge(edgeStart, edgeStart, edgeStart.getX());
            newEdge.setDirVect(edgeDir);
            newEdge.setExtendsUp(true);

            if (intersectingArc.getParent() != null) {
                if (intersectingArc.getParent().getLeft() == intersectingArc) {
                    intersectingArc.getParent().setLeft(newEdge);
                    LOGGER.log(Level.SEVERE, "The intersecting's arcs parent's left child is the new edge");
                } else {
                    intersectingArc.getParent().setRight(newEdge);
                    LOGGER.log(Level.SEVERE, "The intersecting's arcs parent's right child is the new edge");
                }
            } else {
                LOGGER.log(Level.SEVERE, "setting the new root to be an edge inside this first while loop");
                beachLine.setRoot(newEdge);
            }

            if (focus.getX() < ((Parabola) intersectingArc).getSite().getX()) {
                newEdge.setLeft(newArc);
                LOGGER.log(Level.SEVERE, "Setting Left newArc and right intersectingArc");
                newEdge.setRight(intersectingArc);
            } else {
                newEdge.setLeft(intersectingArc);
                LOGGER.log(Level.SEVERE, "Setting right newArc and left intersectingArc");
                newEdge.setRight(newArc);
            }
        }*/
        MapView.testKeyPressed.addListener((newVal, oldVal, obj) -> {
            beachLine.run();
            arcLayer.getChildren().clear();
            layer.getChildren().clear();
            layer.getChildren().add(arcLayer);
            final List<Line> generatedLines = beachLine.getCompletedEdges();
            generatedLines.forEach(line -> layer.getChildren().add(line));

            generateAllArcs(beachLine.root);

        });


        /*final Arc arc = new Arc(new Vec3(MapView.MAP_WIDTH / 2, MapView.MAP_HEIGHT / 2));
        arc.calculateArc(MapView.MAP_HEIGHT * 0.75);
        layer.getChildren().add(arc.getArc());

        final HalfEdge h = new HalfEdge(null, null, new Vec3(0, 0), new Vec3(0.35, 0.35));
        final Line l = new Line();
        l.setStartX(h.getStart().getX());
        l.setStartY(h.getStart().getY());
        l.setEndX(h.getStart().getX() + h.getDirVect().getX() * 5000);
        l.setEndY(h.getStart().getY() + h.getDirVect().getY() * 5000);

        l.setFill(Color.BLUE);
        layer.getChildren().add(l);

        final Vec3 intersectionPoint = beachLine.getEdgeArcIntersection(h, arc, MapView.MAP_HEIGHT * 0.75);

        if (intersectionPoint != null) {
            final Rectangle r = new Rectangle();
            r.setWidth(5);
            r.setHeight(5);
            r.setX(intersectionPoint.getX());
            r.setY(intersectionPoint.getY());

            r.setFill(Color.GREEN);

            //layer.getChildren().add(r);
        }


        generatedLines.forEach(line -> layer.getChildren().add(line));*/

        //final BaseLine bLine = new BaseLine(new Vec3(0, 0), new Vec3(MapView.MAP_WIDTH, 0));
        //layer.getChildren().add(bLine.getLine());
    }

    private void generateAllArcs(final BlineElement root) {
        if (root == null) {
            return;
        }

        generateAllArcs(root.getLeft());
        generateAllArcs(root.getRight());

        final HalfEdge left = root.getLeftEdge() != null ? root.getLeftEdge() : beachLine.getLeftNeighbour(root).getRightEdge();
        final HalfEdge right = root.getRightEdge() != null ? root.getRightEdge() : beachLine.getRightNeighbour(root).getLeftEdge();

        final Vec3 leftIntersection = beachLine.getEdgeArcIntersection(left, (Arc) root, beachLine.directrixPos);
        final Vec3 rightIntersection = beachLine.getEdgeArcIntersection(right, (Arc) root, beachLine.directrixPos);

        if (leftIntersection != null && rightIntersection != null) {
            final Arc arc = (Arc) root;
            arc.calculateArc(leftIntersection.getX(), rightIntersection.getX(), beachLine.directrixPos);

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
        }
    }

    @Override
    public Group getLayer() {
        return layer;
    }

}
