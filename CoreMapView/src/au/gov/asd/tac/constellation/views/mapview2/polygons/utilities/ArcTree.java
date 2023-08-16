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
package au.gov.asd.tac.constellation.views.mapview2.polygons.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.util.Pair;
import org.assertj.core.util.Arrays;

/**
 *
 * @author altair1673
 */
public class ArcTree {

    public BlineElement root;
    private final List<Line> completedEdges = new ArrayList<>();
    private final List<Polygon> completedShapes = new ArrayList<>();

    private final List<HalfEdge> edges = new ArrayList<>();
    private final PriorityQueue<VoronoiEvent> eventQueue;
    private final Map<Vec3, Set<Vec3>> siteMap;

    private final List<List<Vec3>> finalShapes = new ArrayList<>();

    public double directrixPos = 0;
    private int id = 0;

    private static final Logger LOGGER = Logger.getLogger(ArcTree.class.getName());

    private boolean finishedEdges = false;

    private final Vec3 edgeCaseFocus = new Vec3(MapView.MAP_WIDTH / 2, -1000);

    private final HalfEdge left = new HalfEdge(null, null, new Vec3(0, 0), new Vec3(0, 1));
    private final HalfEdge right = new HalfEdge(null, null, new Vec3(MapView.MAP_WIDTH, 0), new Vec3(0, 0.95));
    private final HalfEdge top = new HalfEdge(null, null, new Vec3(0, 0), new Vec3(1, 0));
    private final HalfEdge bottom = new HalfEdge(null, null, new Vec3(0, MapView.MAP_HEIGHT), new Vec3(1, 0));

    private final Vec3 topLeft = new Vec3(0, 0);
    private final Vec3 topRight = new Vec3(MapView.MAP_WIDTH, 0);
    private final Vec3 bottomLeft = new Vec3(0, MapView.MAP_HEIGHT);
    private final Vec3 bottomRight = new Vec3(MapView.MAP_WIDTH, MapView.MAP_HEIGHT);


    private final Map<Vec3, List<Pair<Double, Vec3>>> cornerMap = new HashMap<>();

    public ArcTree(final PriorityQueue<VoronoiEvent> eventQueue) {
        siteMap = new HashMap<>();
        this.eventQueue = eventQueue;
        //root = new BaseLine(new Vec3(Double.MIN_VALUE, 0), new Vec3(Double.MAX_VALUE, 0));
        root = new Arc(new Vec3(MapView.MAP_WIDTH / 2, -500), id++);
        final HalfEdge left = new HalfEdge(((Arc) root).getFocus(), ((Arc) root).getFocus(), new Vec3(MapView.MAP_WIDTH / 2, -1000), new Vec3(-1, 0));
        final HalfEdge right = new HalfEdge(((Arc) root).getFocus(), ((Arc) root).getFocus(), new Vec3(MapView.MAP_WIDTH / 2, -1000), new Vec3(1, 0));
        root.setLeftEdge(left);
        root.setRightEdge(right);
        siteMap.put(((Arc) root).getFocus(), new HashSet<>());

        cornerMap.put(topLeft, new ArrayList<>());
        cornerMap.get(topLeft).add(new Pair<>(Double.MAX_VALUE, bottomLeft));
        cornerMap.get(topLeft).add(new Pair<>(Double.MAX_VALUE, topRight));

        cornerMap.put(topRight, new ArrayList<>());
        cornerMap.get(topRight).add(new Pair<>(Double.MAX_VALUE, bottomRight));
        cornerMap.get(topRight).add(new Pair<>(Double.MAX_VALUE, topLeft));

        cornerMap.put(bottomLeft, new ArrayList<>());
        cornerMap.get(bottomLeft).add(new Pair<>(Double.MAX_VALUE, topLeft));
        cornerMap.get(bottomLeft).add(new Pair<>(Double.MAX_VALUE, bottomRight));

        cornerMap.put(bottomRight, new ArrayList<>());
        cornerMap.get(bottomRight).add(new Pair<>(Double.MAX_VALUE, topRight));
        cornerMap.get(bottomRight).add(new Pair<>(Double.MAX_VALUE, bottomLeft));
    }

    private BlineElement addArc(final Vec3 focus) {
        BlineElement current = root;
        final BlineElement intersectingElement = findIntersectingArc(current, focus.getX(), focus.getY());
        final Arc newArc = new Arc(focus, id++);
        //LOGGER.log(Level.SEVERE, "Created new arc: " + id);
        //LOGGER.log(Level.SEVERE, "Arc intersected");
        final Arc intersectingArc = (Arc) intersectingElement;

        final Arc splitLeft = new Arc(intersectingArc.getFocus(), id++);
        //LOGGER.log(Level.SEVERE, "Created left arc with ID: " + id);

        final Arc splitRight = new Arc(intersectingArc.getFocus(), id++);
        //LOGGER.log(Level.SEVERE, "Created right arc with ID: " + id);

        final Vec3 edgeStart = new Vec3(focus.getX(), intersectingArc.getY(focus.getX(), focus.getY()));
        final Vec3 direction = new Vec3(newArc.getFocus().getX() - intersectingArc.getFocus().getX(), newArc.getFocus().getY() - intersectingArc.getFocus().getY());
        //LOGGER.log(Level.SEVERE, "Edge start: " + edgeStart.getX() + ", " + edgeStart.getY());

        if (!siteMap.containsKey(newArc.getFocus())) {
            siteMap.put(newArc.getFocus(), new HashSet<>());
        }

        final Vec3 relatedIntersection = new Vec3(edgeStart.getX(), edgeStart.getY() < 0 ? 0 : edgeStart.getY());

        siteMap.get(newArc.getFocus()).add(relatedIntersection);
        if (relatedIntersection.getY() != 0) {
            siteMap.get(intersectingArc.getFocus()).add(relatedIntersection);
        }

        final Line l = new Line();
        l.setStartX(relatedIntersection.getX() - 5);
        l.setStartY(relatedIntersection.getY());
        l.setEndX(relatedIntersection.getX() + 5);
        l.setEndY(relatedIntersection.getY());

        l.setStroke(Color.GREEN);
        l.setStrokeWidth(2);
        completedEdges.add(l);

        final Vec3 dirVect = new Vec3(direction.getY(), -direction.getX());
        dirVect.normalizeVec2();

        final HalfEdge e1 = new HalfEdge(newArc.getFocus(), intersectingArc.getFocus(), edgeStart, dirVect);
        final HalfEdge e2 = new HalfEdge(newArc.getFocus(), intersectingArc.getFocus(), edgeStart, new Vec3(-dirVect.getX(), -dirVect.getY()));


        edges.add(e1);
        edges.add(e2);

        newArc.setLeft(splitLeft);
        newArc.setRight(splitRight);
        splitLeft.setParent(newArc);
        splitRight.setParent(newArc);

        //e1.setParentArc(newArc);
        //e1.setHomeArc(splitRight);

        //e2.setParentArc(newArc);
        //e2.setHomeArc(splitLeft);

        splitLeft.setRightEdge(e2);
        splitRight.setLeftEdge(e1);

        splitLeft.setLeftEdge(intersectingArc.getLeftEdge() != null ? intersectingArc.getLeftEdge() : getLeftNeighbour(intersectingArc).getRightEdge());
        splitRight.setRightEdge(intersectingArc.getRightEdge() != null ? intersectingArc.getRightEdge() : getRightNeighbour(intersectingArc).getLeftEdge());

        splitRight.setRight(intersectingArc.getRight());
        splitLeft.setLeft(intersectingArc.getLeft());

        // set the parents of intersecting left and right
        if (intersectingArc.getRight() != null) {
            intersectingArc.getRight().setParent(splitRight);
        }

        if (intersectingArc.getLeft() != null) {
            intersectingArc.getLeft().setParent(splitLeft);
        }

        if (intersectingArc.getCurrentEvent() != null) {
            //LOGGER.log(Level.SEVERE, "Invalidating intersection at: " + intersectingArc.getCurrentEvent().getYCoord());
            intersectingArc.getCurrentEvent().setValid(false);
        }

        newArc.setParentFromItem(intersectingElement);

        if (intersectingElement == root) {
            //LOGGER.log(Level.SEVERE, "Intersecting arc is root now");
            current = newArc;
        }
        addEdgeIntersectionEvent(splitLeft, focus.getY());
        addEdgeIntersectionEvent(splitRight, focus.getY());


        return current;
    }

    private void addEdgeIntersectionEvent(final BlineElement arc, final double directrix) {
        if (arc == null) {
            return;
        }
        //LOGGER.log(Level.SEVERE, "Checking for edge intersection event");
        final HalfEdge left = arc.getLeftEdge() != null ? arc.getLeftEdge() : getLeftNeighbour(arc) != null ? getLeftNeighbour(arc).getRightEdge() : null;
        final HalfEdge right = arc.getRightEdge() != null ? arc.getRightEdge() : getRightNeighbour(arc) != null ? getRightNeighbour(arc).getLeftEdge() : null;

        if (left == null || right == null) {
            if (left == null) {
                //LOGGER.log(Level.SEVERE, "Left edge is null");
            }

            if (right == null) {
                //LOGGER.log(Level.SEVERE, "Right edge is null");
            }
            return;
        }

        //LOGGER.log(Level.SEVERE, "Left edge dirvect: " + left.getDirVect().getX() + ", " + left.getDirVect().getY() + " right edge dirvect: " + right.getDirVect().getX() + ", " + right.getDirVect().getY());

        final Vec3 intersectionPoint = findEdgeIntersectionPoint(left, right);



        if (intersectionPoint == null) {
            //LOGGER.log(Level.SEVERE, "No intersection");
            return;
        }
        //LOGGER.log(Level.SEVERE, "Possible intersection");
        //LOGGER.log(Level.SEVERE, "Intersection point is: " + intersectionPoint.getX() + ", " + intersectionPoint.getY());


        //final Vec3 eventOffset = new Vec3(left.getParentArc().getFocus().getX() - intersectionPoint.getX(), left.getParentArc().getFocus().getY() - intersectionPoint.getY());

        final double distance = Vec3.getDistance(left.getParentArc(), intersectionPoint);


        final double directrixY = intersectionPoint.getY() + distance;

        if (arc.getCurrentEvent() != null) {
            if (directrixY >= arc.getCurrentEvent().getYCoord()) {
                //LOGGER.log(Level.SEVERE, "Invalid intersection");
                return;
            }
            //LOGGER.log(Level.SEVERE, "Invalidating intersection at: " + arc.getCurrentEvent().getYCoord());
            arc.getCurrentEvent().setValid(false);
        }

        //LOGGER.log(Level.SEVERE, "Setting edge intersection event at = " + directrixY);

        final EdgeEvent e = new EdgeEvent(directrixY, left, right, arc, intersectionPoint);
        arc.setCurrentEvent(e);
        eventQueue.add(e);
        //LOGGER.log(Level.SEVERE, "Added edge intersection event");
    }

    private BlineElement removeElement(BlineElement current, final BlineElement remove) {
        if (current == null) {
            return current;
        }

        if (remove.getLeft() == null && remove.getRight() == null) {
            if (remove.getParent() != null) {
                if (remove.getParent().getLeft() == remove) {
                    remove.getParent().setLeft(null);
                } else {
                    remove.getParent().setRight(null);
                }

                remove.setParent(null);

                return current;
            } else {
                return null;
            }
        } else if (remove.getLeft() != null && remove.getRight() == null) {
            final BlineElement left = remove.getLeft();
            left.setParent(null);
            //remove.setLeft(null);

            if (remove.getParent() != null) {
                    if (remove.getParent().getLeft() == remove) {
                        remove.getParent().setLeft(left);
                    } else {
                        remove.getParent().setRight(left);
                    }

                    left.setParent(remove.getParent());
                    return current;

            } else
                return left;

        } else if (remove.getRight() != null && remove.getLeft() == null) {
            final BlineElement right = remove.getRight();
            right.setParent(null);
            //remove.setRight(null);

            if (remove.getParent() != null) {
                if (remove.getParent().getLeft() == remove) {
                    remove.getParent().setLeft(right);
                } else {
                    remove.getParent().setRight(right);
                }

                right.setParent(remove.getParent());
                return current;

            } else {
                return right;
            }

        } else {
            //LOGGER.log(Level.SEVERE, "Removing node with 2 children");
            BlineElement iSucc = remove.getRight();
            while (iSucc.getLeft() != null) {
                iSucc = iSucc.getLeft();
            }

            final BlineElement leftChild = remove.getLeft();

            BlineElement temp = removeElement(root, iSucc);
            final BlineElement rightChild = remove.getRight();

            if (leftChild != null) {
                leftChild.setParent(iSucc);
                iSucc.setLeft(leftChild);
            }

            if (iSucc != rightChild && rightChild != null) {
                iSucc.setRight(rightChild);
                rightChild.setParent(iSucc);
            }

            if (remove.getParent() == null) {
                iSucc.setParent(null);
                return iSucc;
            } else if (remove.getParent().getLeft() == remove) {
                remove.getParent().setLeft(iSucc);
            } else {
                remove.getParent().setRight(iSucc);
            }
            iSucc.setParent(remove.getParent());

        }

        return current;
    }

    private void removeArc(final EdgeEvent e) {
        //LOGGER.log(Level.SEVERE, "Removing element");
        final BlineElement removed = e.getSqueezed();
        final HalfEdge e1 = e.getEdge1();
        final HalfEdge e2 = e.getEdge2();

        e1.setComplete(true);
        e2.setComplete(true);
        //LOGGER.log(Level.SEVERE, "squeezed element is an arc with id: " + ((Arc) removed).getId());

        //LOGGER.log(Level.SEVERE, "Getting left neighbour of removed arc");
        final BlineElement left = getLeftNeighbour(removed);
        if (left == null) {
            //LOGGER.log(Level.SEVERE, "Left is null");

        }

        //LOGGER.log(Level.SEVERE, "Getting right neighbour of removed arc");
        final BlineElement right = getRightNeighbour(removed);
        if (right == null) {
            //LOGGER.log(Level.SEVERE, "Right is null");
        }

        final Vec3 intersectionPoint = e.getIntersectionPoint();

        //LOGGER.log(Level.SEVERE, "Intersection point : " + intersectionPoint.getX() + ", " + intersectionPoint.getY());

        final Line line = new Line();
        line.setStartX(e1.getStart().getX());
        line.setStartY(e1.getStart().getY());
        line.setEndX(intersectionPoint.getX());
        line.setEndY(intersectionPoint.getY());
        line.setStroke(Color.RED);

        final Line line2 = new Line();
        line2.setStartX(e2.getStart().getX());
        line2.setStartY(e2.getStart().getY());
        line2.setEndX(intersectionPoint.getX());
        line2.setEndY(intersectionPoint.getY());
        line2.setStroke(Color.RED);


        if (e1.extendsUp()) {
            line.setStartY(Double.MAX_VALUE);
        }

        if (e2.extendsUp()) {
            line2.setStartY(Double.MAX_VALUE);
        }

        completedEdges.add(line);
        completedEdges.add(line2);

        final Vec3 direction;
        final Vec3 dirNewEdge;
        HalfEdge newEdge = null;

        //if (left instanceof Arc && right instanceof Arc) {
        final Arc leftArc = (Arc) left;
        final Arc rightArc = (Arc) right;

        final Vec3 leftFocus = leftArc != null ? leftArc.getFocus() : edgeCaseFocus;
        final Vec3 rightFocus = rightArc != null ? rightArc.getFocus() : edgeCaseFocus;

        direction = new Vec3(leftFocus.getX() - rightFocus.getX(), leftFocus.getY() - rightFocus.getY());
        dirNewEdge = new Vec3(direction.getY(), -direction.getX());
        dirNewEdge.normalizeVec2();

        newEdge = new HalfEdge(rightFocus, leftFocus, intersectionPoint, dirNewEdge);

        if (left != null) {
            //LOGGER.log(Level.SEVERE, "id of left Arc: " + leftArc.getId());
            left.setRightEdge(newEdge);
        }

        if (right != null) {
            //LOGGER.log(Level.SEVERE, "id of right Arc: " + rightArc.getId());
            right.setLeftEdge(newEdge);
        }

        Vec3 renderedIntersectionPoint = intersectionPoint;


        /*if (intersectionPoint.getX() <= 0 || intersectionPoint.getX() >= MapView.MAP_WIDTH || intersectionPoint.getY() <= 0 || intersectionPoint.getY() >= MapView.MAP_HEIGHT) {
            renderedIntersectionPoint = getBoundaryIntersection(intersectionPoint, e1, e2, newEdge);
        } else {
            renderedIntersectionPoint = intersectionPoint;
        }*/
        if ((isOutsideMap(intersectionPoint) && !isOutsideMap(e1.getStart()))) {
            LOGGER.log(Level.SEVERE, "Checking intersection of first edge becasue intersection point is outside of the map");
            addBoundaryIntersections(((Arc) removed).getFocus(), e1);
        } else if ((!isOutsideMap(intersectionPoint) && isOutsideMap(e1.getStart()))) {
            LOGGER.log(Level.SEVERE, "Intersection point inside, start is outside of edge 1");
            siteMap.get(e1.getParentArc()).add(intersectionPoint);
            siteMap.get(e1.getHomeArc()).add(intersectionPoint);
            addBoundaryIntersections(((Arc) removed).getFocus(), e1);
        } else if (left != null && !isOutsideMap(intersectionPoint) && !isOutsideMap(e1.getStart())) {
            siteMap.get(((Arc) removed).getFocus()).add(intersectionPoint);
            siteMap.get(leftArc.getFocus()).add(intersectionPoint);
        }

        if ((isOutsideMap(intersectionPoint) && !isOutsideMap(e2.getStart()))) {
            LOGGER.log(Level.SEVERE, "Checking intersection of second edge becasue intersection point is outside of the map");
            addBoundaryIntersections(((Arc) removed).getFocus(), e2);
        } else if ((!isOutsideMap(intersectionPoint) && isOutsideMap(e2.getStart()))) {
            LOGGER.log(Level.SEVERE, "Intersection point inside, start is outside of edge 2");
            siteMap.get(e2.getParentArc()).add(intersectionPoint);
            siteMap.get(e2.getParentArc()).add(intersectionPoint);
            addBoundaryIntersections(((Arc) removed).getFocus(), e2);
        } else if (right != null && !isOutsideMap(intersectionPoint) && !isOutsideMap(e2.getStart())) {
            siteMap.get(((Arc) removed).getFocus()).add(intersectionPoint);
            siteMap.get(rightArc.getFocus()).add(intersectionPoint);
        }

        //LOGGER.log(Level.SEVERE, "Merged 2 Arcs at edge start: " + newEdge.getStart().getX() + ", " + newEdge.getStart().getY() + " edge direction: " + newEdge.getDirVect().getX() + ", " + newEdge.getDirVect().getY());
        /*if (renderedIntersectionPoint != null) {
            siteMap.get(((Arc) removed).getFocus()).add(renderedIntersectionPoint);

            if (left != null) {
                siteMap.get(leftArc.getFocus()).add(renderedIntersectionPoint);
            }

            if (right != null) {
                siteMap.get(rightArc.getFocus()).add(renderedIntersectionPoint);
            }
        }*/


        Line l = new Line();
        l.setStartX(leftFocus.getX());
        l.setStartY(leftFocus.getY());
        l.setEndX(intersectionPoint.getX());
        l.setEndY(intersectionPoint.getY());
        l.setStroke(Color.PURPLE);

        Line l2 = new Line();
        l2.setStartX(rightFocus.getX());
        l2.setStartY(rightFocus.getY());
        l2.setEndX(intersectionPoint.getX());
        l2.setEndY(intersectionPoint.getY());
        l2.setStroke(Color.PURPLE);

        Line l3 = new Line();
        l3.setStartX(intersectionPoint.getX());
        l3.setStartY(intersectionPoint.getY());
        l3.setEndX(intersectionPoint.getX() + dirNewEdge.getX() * 1000);
        l3.setEndY(intersectionPoint.getY() + dirNewEdge.getY() * 1000);
        l3.setStroke(Color.LIME);

        Line l4 = new Line();
        l4.setStartX(intersectionPoint.getX() - 5);
        l4.setStartY(intersectionPoint.getY());
        l4.setEndX(intersectionPoint.getX() + 5);
        l4.setEndY(intersectionPoint.getY());
        l4.setStroke(Color.PURPLE);

        //completedEdges.add(l);
        //completedEdges.add(l2);
        //completedEdges.add(l3);
        completedEdges.add(l4);

        //printBFS(root);
        //printInOrder(root);


        if (newEdge != null) {
            edges.add(newEdge);
        }

        if (removed.getCurrentEvent() != null) {
            removed.getCurrentEvent().setValid(false);
        }

        root = removeElement(root, removed);

        //LOGGER.log(Level.SEVERE, "After removal");
        //printBFS(root);
        //exists(root, ((Arc) removed).getId());
        //printInOrder(root);

        addEdgeIntersectionEvent(left, e.getYCoord());
        addEdgeIntersectionEvent(right, e.getYCoord());

    }

    private Vec3 findEdgeIntersectionPoint(final HalfEdge h1, final HalfEdge h2) {

        if ((h1.getDirVect().getY() == 1 || h1.getDirVect().getY() == -1) && h2.getDirVect().getX() == 0) {
            return new Vec3(h1.getStart().getX(), h2.getStart().getY());
        } else if ((h2.getDirVect().getY() == 1 || h2.getDirVect().getY() == -1) && h1.getDirVect().getX() == 0) {
            return new Vec3(h2.getStart().getX(), h1.getStart().getY());
        }

        final double dx = h2.getStart().getX() - h1.getStart().getX();

        //LOGGER.log(Level.SEVERE, "DX: " + dx);

        final double dy = h2.getStart().getY() - h1.getStart().getY();

        //LOGGER.log(Level.SEVERE, "DY: " + dy);

        final double det = h2.getDirVect().getX() * h1.getDirVect().getY() - h2.getDirVect().getY() * h1.getDirVect().getX();

        //LOGGER.log(Level.SEVERE, "DET: " + det);

        final double u = (dy * h2.getDirVect().getX() - dx * h2.getDirVect().getY()) / det;

        //LOGGER.log(Level.SEVERE, "U: " + u);

        final double v = (dy * h1.getDirVect().getX() - dx * h1.getDirVect().getY()) / det;

        //LOGGER.log(Level.SEVERE, "V: " + v);

        if (u < 0d && !h1.extendsUp()) {
            return null;
        }

        if (v < 0d && !h2.extendsUp()) {
            return null;
        }

        if (u == 0d && v == 0d && !h1.extendsUp() && !h2.extendsUp()) {
            return null;
        }


        return new Vec3(h1.getStart().getX() + h1.getDirVect().getX() * u, h1.getStart().getY() + h1.getDirVect().getY() * u);
        //return new Vec3(x, y);
    }

    private BlineElement findIntersectingArc(final BlineElement currentNode, final double x, final double directrix) {
        //LOGGER.log(Level.SEVERE, "Finding intersecting arc");
        BlineElement current = currentNode;

        if (current.getLeft() == null && current.getRight() == null) {
            return current;
        }

        //LOGGER.log(Level.SEVERE, "Checking if new site falls within an arc");
        final Arc arc = (Arc) current;
        HalfEdge leftHalfEdge = arc.getLeftEdge();
        HalfEdge rightHalfEdge = arc.getRightEdge();

        if (leftHalfEdge == null && getLeftNeighbour(current) != null) {
            leftHalfEdge = getLeftNeighbour(current).getRightEdge();
        }

        if (rightHalfEdge == null && getRightNeighbour(current) != null) {
            rightHalfEdge = getRightNeighbour(current).getLeftEdge();
        }

        Vec3 leftIntersection = getEdgeArcIntersection(leftHalfEdge, arc, directrix);
        Vec3 rightIntersection = getEdgeArcIntersection(rightHalfEdge, arc, directrix);

        final double leftX = leftIntersection == null ? 0 : leftIntersection.getX();
        final double rightX = rightIntersection == null ? MapView.MAP_WIDTH : rightIntersection.getX();

        //LOGGER.log(Level.SEVERE, "Left X: " + leftX);
        //LOGGER.log(Level.SEVERE, "Right X: " + rightX);

        //LOGGER.log(Level.SEVERE, "X: " + x);

        if (x > rightX) {
            //LOGGER.log(Level.SEVERE, "Checking if anything on the right is intersecting");
            return findIntersectingArc(current.getRight(), x, directrix);
        } else if (x < leftX) {
            //LOGGER.log(Level.SEVERE, "Checking if anything on the left is intersecting");
            return findIntersectingArc(current.getLeft(), x, directrix);
        } else {
            return current;
        }


    }

    public Vec3 getEdgeArcIntersection(HalfEdge edge, Arc arc, double directrix) {
        //LOGGER.log(Level.SEVERE, "Checking edge arc intersection on arc: " + arc.getId());
        if (edge.getDirVect().getX() == 0.0) {
            if (directrix == arc.getFocus().getY()) {

                if (edge.getStart().getX() == arc.getFocus().getX()) {
                    return new Vec3(arc.getFocus());
                } else {
                    return null;
                }
            }
            final double arcY = arc.getY(edge.getStart().getX(), directrix);
            return new Vec3(edge.getStart().getX(), arcY);
        }

        // y = mx + b
        double m = edge.getDirVect().getY() / edge.getDirVect().getX();
        double b = edge.getStart().getY() - m * edge.getStart().getX();

        if (arc.getFocus().getY() == directrix) {
            double intersectionXOffset = arc.getFocus().getX() - edge.getStart().getX();

            if (intersectionXOffset * edge.getDirVect().getX() < 0.0) {
                return null;
            }
            return new Vec3(arc.getFocus().getX(), m * arc.getFocus().getX() + b);
        }

        // y = ax^2 + bx + c
        double a = 1.0 / (2.0 * (arc.getFocus().getY() - directrix));
        double b2 = -m - 2.0 * a * arc.getFocus().getX();
        double c = a * arc.getFocus().getX() * arc.getFocus().getX() + (arc.getFocus().getY() + directrix) * 0.5 - b;

        double discriminant = b2 * b2 - 4.0 * a * c;
        if (discriminant < 0) {
            return null;
        }
        double rootDisc = Math.sqrt(discriminant);
        double x1 = (-b2 + rootDisc) / (2.0 * a);
        double x2 = (-b2 - rootDisc) / (2.0 * a);

        double x1Offset = x1 - edge.getStart().getX();
        double x2Offset = x2 - edge.getStart().getX();
        double x1Dot = x1Offset * edge.getDirVect().getX();
        double x2Dot = x2Offset * edge.getDirVect().getX();

        double x;
        if ((x1Dot >= 0.0) && (x2Dot < 0.0)) {
            x = x1;
        } else if ((x1Dot < 0.0) && (x2Dot >= 0.0)) {
            x = x2;
        } else if ((x1Dot >= 0.0) && (x2Dot >= 0.0)) {
            if (x1Dot < x2Dot) {
                x = x1;
            } else {
                x = x2;
            }
        } else {
            if (x1Dot < x2Dot) {
                x = x2;
            } else {
                x = x1;
            }
        }

        double y = arc.getY(x, directrix);

        return new Vec3(x, y);
    }

    private Vec3 getBoundaryIntersection(final Vec3 intersectionPoint, final HalfEdge h1, final HalfEdge h2, final HalfEdge resultingEdge) {
        final Vec3 topLineIntersection = findEdgeIntersectionPoint(resultingEdge, top);

        if (topLineIntersection != null && resultingEdge.getStart().getY() <= 0) {
            Line l4 = new Line();
            l4.setStartX(resultingEdge.getStart().getX());
            l4.setStartY(resultingEdge.getStart().getY());
            l4.setEndX(topLineIntersection.getX());
            l4.setEndY(topLineIntersection.getY());
            l4.setStroke(Color.YELLOW);

            LOGGER.log(Level.SEVERE, "Top intersection X: " + topLineIntersection.getX() + " Y: " + topLineIntersection.getY());

            completedEdges.add(l4);

            if (Vec3.getDistance(topLeft, resultingEdge.getStart()) < cornerMap.get(topLeft).get(1).getKey()) {
                final Vec3 relatedPoint = getRelatedPointToCorner(topLeft, resultingEdge);
                cornerMap.get(topLeft).set(1, new Pair<>(Vec3.getDistance(topLeft, resultingEdge.getStart()), relatedPoint));
            }
            //setRelatedPointToCorner(resultingEdge, resultingEdge.getStart(), topLeft, 1);

            if (Vec3.getDistance(topRight, resultingEdge.getStart()) < cornerMap.get(topRight).get(1).getKey()) {
                final Vec3 relatedPoint = getRelatedPointToCorner(topRight, resultingEdge);
                cornerMap.get(topRight).set(1, new Pair<>(Vec3.getDistance(topRight, resultingEdge.getStart()), relatedPoint));
            }
            //setRelatedPointToCorner(resultingEdge, resultingEdge.getStart(), topLeft, 1);

            return topLineIntersection;
        } else {
            addBoundaryIntersections(intersectionPoint, h1);
            addBoundaryIntersections(intersectionPoint, h2);

            return null;
        }

    }

    private Vec3 getRelatedPointToCorner(final Vec3 corner, final HalfEdge h) {
        return Vec3.getDistance(corner, h.getParentArc()) < Vec3.getDistance(corner, h.getHomeArc()) ? h.getParentArc() : h.getHomeArc();
    }

    private void setRelatedPointToCorner(final HalfEdge edge, final Vec3 intersectionPoint, final Vec3 corner, final int position) {
        if (Vec3.getDistance(corner, intersectionPoint) < cornerMap.get(corner).get(position).getKey()) {
            final Vec3 relatedPoint = getRelatedPointToCorner(corner, edge);
            cornerMap.get(corner).set(position, new Pair<>(Vec3.getDistance(corner, intersectionPoint), relatedPoint));
        }
    }

    private void addBoundaryIntersections(final Vec3 squeezedArcFocus, final HalfEdge h1) {
        final Vec3 topLineH1 = findEdgeIntersectionPoint(h1, top);
        final Vec3 bottomLineH1 = findEdgeIntersectionPoint(h1, bottom);
        final Vec3 leftLineH1 = findEdgeIntersectionPoint(h1, left);
        final Vec3 rightLineH1 = findEdgeIntersectionPoint(h1, right);

        final List<Vec3> h1IntersectionPoints = new ArrayList<>();

        if (topLineH1 != null) {
            topLineH1.setZ(Vec3.getDistance(h1.getStart(), topLineH1));
            h1IntersectionPoints.add(topLineH1);
        }
        if (bottomLineH1 != null) {
            LOGGER.log(Level.SEVERE, "Bottom line intersection X: " + bottomLineH1.getX() + " Y: " + bottomLineH1.getY());
            bottomLineH1.setZ(Vec3.getDistance(h1.getStart(), bottomLineH1));
            h1IntersectionPoints.add(bottomLineH1);
        }
        if (leftLineH1 != null) {
            LOGGER.log(Level.SEVERE, "Left line intersection X: " + leftLineH1.getX() + " Y: " + leftLineH1.getY());
            leftLineH1.setX(0);
            leftLineH1.setZ(Vec3.getDistance(h1.getStart(), leftLineH1));
            h1IntersectionPoints.add(leftLineH1);
        }
        if (rightLineH1 != null) {
            rightLineH1.setZ(Vec3.getDistance(h1.getStart(), rightLineH1));
            h1IntersectionPoints.add(rightLineH1);
        }

        Collections.sort(h1IntersectionPoints, (v1, v2) -> {
            if (v1.getZ() > v2.getZ()) {
                return 1;
            }

            return -1;
        });

        if (!h1IntersectionPoints.isEmpty()) {
            if ((h1IntersectionPoints.get(0).getX() > MapView.MAP_WIDTH || h1IntersectionPoints.get(0).getX() < 0) || (h1IntersectionPoints.get(0).getY() < 0 || h1IntersectionPoints.get(0).getY() > MapView.MAP_HEIGHT)) {
                return;
            }

            if (h1IntersectionPoints.get(0) == topLineH1) {
                setRelatedPointToCorner(h1, topLineH1, topLeft, 1);
                setRelatedPointToCorner(h1, topLineH1, topRight, 1);
            } else if (h1IntersectionPoints.get(0) == leftLineH1) {
                setRelatedPointToCorner(h1, leftLineH1, topLeft, 0);
                setRelatedPointToCorner(h1, leftLineH1, bottomLeft, 0);
            } else if (h1IntersectionPoints.get(0) == bottomLineH1) {
                setRelatedPointToCorner(h1, bottomLineH1, bottomLeft, 1);
                setRelatedPointToCorner(h1, bottomLineH1, bottomRight, 1);
            } else if (h1IntersectionPoints.get(0) == rightLineH1) {
                setRelatedPointToCorner(h1, rightLineH1, topRight, 0);
                setRelatedPointToCorner(h1, rightLineH1, bottomRight, 0);
            }

            Line l4 = new Line();
            l4.setStartX(h1IntersectionPoints.get(0).getX() - 1);
            l4.setStartY(h1IntersectionPoints.get(0).getY());
            l4.setEndX(h1IntersectionPoints.get(0).getX() + 1);
            l4.setEndY(h1IntersectionPoints.get(0).getY());
            l4.setStroke(Color.BROWN);
            l4.setStrokeWidth(5);

            completedEdges.add(l4);

            final Line l1 = new Line();
            l1.setStartX(h1IntersectionPoints.get(0).getX());
            l1.setStartY(h1IntersectionPoints.get(0).getY());
            l1.setEndX(h1.getHomeArc().getX());
            l1.setEndY(h1.getHomeArc().getY());
            l1.setStroke(Color.PINK);
            l1.setVisible(false);
            completedEdges.add(l1);

            final Line l2 = new Line();
            l2.setStartX(h1IntersectionPoints.get(0).getX());
            l2.setStartY(h1IntersectionPoints.get(0).getY());
            l2.setEndX(h1.getParentArc().getX());
            l2.setEndY(h1.getParentArc().getY());
            l2.setStroke(Color.PINK);
            l2.setVisible(false);
            completedEdges.add(l2);

            final Line l3 = new Line();
            l3.setStartX(h1IntersectionPoints.get(0).getX());
            l3.setStartY(h1IntersectionPoints.get(0).getY());
            l3.setEndX(squeezedArcFocus.getX());
            l3.setEndY(squeezedArcFocus.getY());
            l3.setStroke(Color.PINK);
            l3.setVisible(false);
            completedEdges.add(l3);

            l4.setOnMouseEntered(event -> {
                l1.setVisible(true);
                l2.setVisible(true);
                //l3.setVisible(true);
            });

            l4.setOnMouseExited(event -> {
                l1.setVisible(false);
                l2.setVisible(false);
                //l3.setVisible(false);
            });

            siteMap.get(h1.getHomeArc()).add(h1IntersectionPoints.get(0));
            siteMap.get(h1.getParentArc()).add(h1IntersectionPoints.get(0));
            //siteMap.get(squeezedArcFocus).add(h1IntersectionPoints.get(0));
        } else {
            LOGGER.log(Level.SEVERE, "The line has no intersection");
        }
    }

    private void renderMapCorners() {
        cornerMap.entrySet().forEach(entry -> {
            for (int i = 0; i < entry.getValue().size(); i++) {
                if (siteMap.containsKey(entry.getValue().get(i).getValue())) {
                    siteMap.get(entry.getValue().get(i).getValue()).add(entry.getKey());
                } else if (i == 0) {
                    if (siteMap.containsKey(entry.getValue().get(1).getValue())) {
                        siteMap.get(entry.getValue().get(1).getValue()).add(entry.getKey());
                    }
                } else if (i == 1) {
                    if (siteMap.containsKey(entry.getValue().get(0).getValue())) {
                        siteMap.get(entry.getValue().get(0).getValue()).add(entry.getKey());
                    }
                }
            }
        });
    }

    public BlineElement getRightNeighbour(final BlineElement item) {
        BlineElement current = item;

        if (current.getLeft() == null && current.getRight() == null && current.getParent() == null) {
            return null;
        }

        if (current.getRight() != null) {
            BlineElement right = current.getRight();

            while (right.getLeft() != null) {
                right = right.getLeft();
            }

            if (right == item) {
                //LOGGER.log(Level.SEVERE, "Found item and passed in item the same in getRightN");
                return null;
            }

            return right;
        }

        if (current.getParent() != null && current.getParent().getLeft() == current) {

            if (current.getParent() == item) {
                //LOGGER.log(Level.SEVERE, "parent is item in getRightN");
                return null;
            }

            return current.getParent();
        }

        if (current.getParent() != null && current.getParent().getRight() == current) {
            while (current.getParent().getRight() == current) {
                current = current.getParent();

                if (current.getParent() == null) {
                    return null;
                }
            }

            if (current.getParent() == item) {
                //LOGGER.log(Level.SEVERE, "Parent in 2 children loop is equal to item in getLeftN");
                return null;
            }

            return current.getParent();
        }

        return null;
    }

    public BlineElement getLeftNeighbour(final BlineElement item) {
        BlineElement current = item;

        if (current.getLeft() == null && current.getRight() == null && current.getParent() == null) {
            return null;
        }

        if (current.getLeft() != null) {
            BlineElement left = current.getLeft();

            while (left.getRight() != null) {
                left = left.getRight();
            }

            if (left == item) {
                //LOGGER.log(Level.SEVERE, "Found item and passed in item the same in getLeftN");
                return null;
            }

            return left;
        }

        if (current.getParent() != null && current.getParent().getRight() == current) {

            if (current.getParent() == item) {
                //LOGGER.log(Level.SEVERE, "parent is item in getLeftN");
                return null;
            }

            return current.getParent();
        }

        if (current.getParent() != null && current.getParent().getLeft() == current) {
            while (current.getParent().getLeft() == current) {
                current = current.getParent();

                if (current.getParent() == null) {
                    return null;
                }
            }

            if (current.getParent() == item) {
                //LOGGER.log(Level.SEVERE, "Parent in 2 children loop is equal to item in getLeftN");
                return null;
            }

            return current.getParent();
        }

        return null;
    }

    private void finishEdges() {
        edges.forEach(edge -> {
            if (!edge.isComplete()) {
                edge.setComplete(true);
                final Line l = new Line();
                l.setStartX(edge.getStart().getX());
                l.setStartY(edge.getStart().getY());
                l.setEndX(edge.getStart().getX() + edge.getDirVect().getX() * 5000);
                l.setEndY(edge.getStart().getY() + edge.getDirVect().getY() * 5000);

                l.setStroke(Color.RED);
                completedEdges.add(l);

                final Line l2 = new Line();
                l2.setStartX(edge.getStart().getX() + 5);
                l2.setStartY(edge.getStart().getY());
                l2.setEndX(edge.getStart().getX() - 5);
                l2.setEndY(edge.getStart().getY());

                l2.setStroke(Color.BLUE);
                l2.setStrokeWidth(5);
                completedEdges.add(l2);

                if ((edge.getStart().getX() >= 0 && edge.getStart().getX() <= MapView.MAP_WIDTH) && (edge.getStart().getY() >= 0 && edge.getStart().getY() <= MapView.MAP_HEIGHT)) {
                    addBoundaryIntersections(edge.getParentArc(), edge);
                }

            }
        });
    }

    private void renderIntersectionPoints() {
        siteMap.values().forEach(intersectionSet -> {
            double x = 0;
            double y = 0;

            final Iterator<Vec3> pointsIterator = (intersectionSet.iterator());
            while (pointsIterator.hasNext()) {
                final Vec3 point = pointsIterator.next();
                x += point.getX();
                y += point.getY();
            }

            x = x / intersectionSet.size();
            y = y / intersectionSet.size();

            final List<Vec3> shape = new ArrayList<>();
            final Iterator<Vec3> pointsIterator2 = intersectionSet.iterator();
            while (pointsIterator2.hasNext()) {
                final Vec3 currentPoint = pointsIterator2.next();
                final double dx = currentPoint.getX() - x;
                final double dy = currentPoint.getY() - y;

                double degrees = Math.toDegrees(Math.atan2(dy, dx));

                if (degrees < 0) {
                    degrees += 360;
                }

                currentPoint.setZ(degrees);
                shape.add(currentPoint);
            }

            Collections.sort(shape, (v1, v2) -> {
                if (v1.getZ() > v2.getZ()) {
                    return 1;
                }

                // TODO sort by distance to mean IF the angle is the same

                return -1;
            });
            finalShapes.add(shape);
        });
    }

    private void generateShapes() {
        final ConstellationColor[] palette = ConstellationColor.createPalette(finalShapes.size());

        for (int i = 0; i < finalShapes.size(); i++) {
            final Polygon shape = new Polygon();

            shape.setStroke(palette[i].getJavaFXColor());
            shape.setFill(palette[i].getJavaFXColor());
            shape.setOpacity(0.5);

            shape.setMouseTransparent(true);

            finalShapes.get(i).forEach(point -> shape.getPoints().addAll(new Double[]{point.getX(), point.getY()}));

            completedShapes.add(shape);
        }
    }

    public List<Line> getCompletedEdges() {
        return completedEdges;
    }

    public List<Polygon> getCompletedShapes() {
        return completedShapes;
    }

    private void printBFS(BlineElement current) {
        final Deque<BlineElement> bfs = new LinkedList<>();

        bfs.push(current);

        String nodeTypeString = "";

        int nodesOnLevel = 1;
        int nodeCounter = 0;
        int validNodes = 0;
        while (!bfs.isEmpty()) {
            final BlineElement item = bfs.poll();

            if (item == null) {
                nodeTypeString += " NULL ";
            } else if (item instanceof Arc) {
                nodeTypeString += " ARC " + ((Arc) item).getId();
            } else {
                nodeTypeString += " BASELINE ";
            }

            ++nodeCounter;

            if (item != null) {
                bfs.addLast(item.getLeft());
                bfs.addLast(item.getRight());
                ++validNodes;
            }


            if (nodeCounter == nodesOnLevel) {
                nodeCounter = 0;
                nodesOnLevel = validNodes * 2;
                validNodes = 0;
                LOGGER.log(Level.SEVERE, "Nodes on this level: " + nodeTypeString);
                nodeTypeString = "";
            }
        }
    }

    private boolean isOutsideMap(final Vec3 point) {
        return point.getX() < 0 || point.getX() > MapView.MAP_WIDTH || point.getY() < 0 || point.getY() > MapView.MAP_HEIGHT;
    }

    private void exists(final BlineElement current, final int id) {
        if (current == null) {
            return;
        }

        exists(current.getLeft(), id);
        exists(current.getRight(), id);

        if (current.getParent() != null && ((Arc) current.getParent()).getId() == id) {
            LOGGER.log(Level.SEVERE, "Node with id: " + id + " is still parent of node with id: " + ((Arc) current).getId());
        }

        if (((Arc) current).getId() == id) {
            LOGGER.log(Level.SEVERE, "Node with id: " + id + " still exists");
        }
    }

    private void printInOrder(BlineElement current) {
        if (current == null) {
            return;
        }

        printInOrder(current.getLeft());
        printInOrder(current.getRight());

        if (current instanceof Arc) {
            LOGGER.log(Level.SEVERE, "Node is an arc");
        } else {
            LOGGER.log(Level.SEVERE, "Node is a baseline");
        }
        LOGGER.log(Level.SEVERE, "-----------------------");
    }

    public void run() {
        final Line directrixLine = new Line();
        while (!eventQueue.isEmpty()) {
            if (eventQueue.peek() instanceof SiteEvent) {
                //LOGGER.log(Level.SEVERE, "PROCESSING SITE EVENT AT X: " + ((SiteEvent) eventQueue.peek()).getSite().getX() + " Y: " + eventQueue.peek().getYCoord());
                final SiteEvent e = (SiteEvent) eventQueue.poll();

                if (completedEdges.contains(directrixLine)) {
                    completedEdges.remove(directrixLine);
                }

                directrixLine.setStartX(e.getSite().getX() - 5);
                directrixLine.setStartY(e.getSite().getY());
                directrixLine.setEndX(e.getSite().getX() + 5);
                directrixLine.setEndY(e.getSite().getY());
                directrixLine.setStroke(Color.BLUE);

                completedEdges.add(directrixLine);
                directrixPos = e.getYCoord();

                root = addArc(e.getSite());
                //printBFS(root);

            } else if (eventQueue.peek() instanceof EdgeEvent) {

                final EdgeEvent e = (EdgeEvent) eventQueue.poll();

                if (e.isValid()) {
                    //LOGGER.log(Level.SEVERE, "PROCESSING EDGE EVENT AT Y: " + e.getYCoord());
                    removeArc(e);

                    //printBFS(root);
                }
                directrixPos = e.getYCoord();
            }
        }
        //else if (!finishedEdges) {
            finishEdges();
            renderMapCorners();
            renderIntersectionPoints();
            generateShapes();
            finishedEdges = true;

        //}

    }

}
