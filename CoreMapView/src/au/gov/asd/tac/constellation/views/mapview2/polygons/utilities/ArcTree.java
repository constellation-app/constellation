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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.util.Pair;

/**
 * This class generates a voronoi diagram for all the markers in the map using
 * fortunes algorithm Fortunes algorithm is an event based algorithm where
 * parabolas are generated for each markers and the intersection point of these
 * arcs is where the edges of the shapes are formed
 *
 * @author altair1673
 */
public class ArcTree {

    // The root node of the tree
    private BlineElement root;
    private final List<Line> completedEdges = new ArrayList<>();

    // The shapes that will be shown on screen
    private final List<Polygon> completedShapes = new ArrayList<>();

    // List of all edges of the shape
    private final List<HalfEdge> edges = new ArrayList<>();

    // The queue events for fortunes algorithm
    private final PriorityQueue<VoronoiEvent> eventQueue;

    // A map of site with there coordinate as the key AND a set of Vec3's representing the corners of the shape they are in
    private final Map<Vec3, Set<Vec3>> siteMap;

    // The corners of all the shapes ordered clockwise
    private final List<List<Vec3>> finalShapeCorners = new ArrayList<>();

    private int id = 0;

    private static final Logger LOGGER = Logger.getLogger(ArcTree.class.getName());

    private final Vec3 rootFocus = new Vec3(MapView.MAP_WIDTH / 2, -MapView.MAP_HEIGHT - 200);
    private final Vec3 edgeCaseFocus;

    // The boundaries of the map used to cut shapes that go over the edge
    private final HalfEdge leftBorder = new HalfEdge(null, null, new Vec3(0, 0), new Vec3(0, 1));
    private final HalfEdge rightBorder = new HalfEdge(null, null, new Vec3(MapView.MAP_WIDTH, 0), new Vec3(0, 0.95));
    private final HalfEdge topBorder = new HalfEdge(null, null, new Vec3(0, 0), new Vec3(1, 0));
    private final HalfEdge bottomBorder = new HalfEdge(null, null, new Vec3(0, MapView.MAP_HEIGHT), new Vec3(1, 0));

    // The corners of the map that are manually assigned to shapes since they are eisting and do not form naturally
    private final Vec3 topLeft = new Vec3(0, 0);
    private final Vec3 topRight = new Vec3(MapView.MAP_WIDTH, 0);
    private final Vec3 bottomLeft = new Vec3(0, MapView.MAP_HEIGHT);
    private final Vec3 bottomRight = new Vec3(MapView.MAP_WIDTH, MapView.MAP_HEIGHT);

    // Map of corner coords with an array containing there closest intersection points
    private final Map<Vec3, List<Pair<Double, Vec3>>> cornerMap = new HashMap<>();

    public ArcTree(final PriorityQueue<VoronoiEvent> eventQueue) {
        siteMap = new HashMap<>();
        this.eventQueue = eventQueue;

        edgeCaseFocus = new Vec3(rootFocus.getX(), rootFocus.getY() * 2);
        // Create a default arc way above the map so that the arcs beloging to the markers at the topBorder have something to intersect
        root = new Arc(new Vec3(rootFocus.getX(), rootFocus.getY()), id++);

        // Set up the edges belonging to the arc and assign them to it. Even though a new arc being created is not assigned an edge we need to make an exception
        // for this initial default arc
        final HalfEdge rootLeftEdge = new HalfEdge(((Arc) root).getFocus(), ((Arc) root).getFocus(), new Vec3(rootFocus.getX(), rootFocus.getY() * 2), new Vec3(-1, 0));
        final HalfEdge rootRightEdge = new HalfEdge(((Arc) root).getFocus(), ((Arc) root).getFocus(), new Vec3(rootFocus.getX(), rootFocus.getY() * 2), new Vec3(1, 0));
        root.setLeftEdge(rootLeftEdge);
        root.setRightEdge(rootRightEdge);
        siteMap.put(((Arc) root).getFocus(), new HashSet<>());

        // Put the corners in the corner map
        // Assign the closest intersection point in both directions to be the other corners
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

    /**
     * Add a new arc to the tree
     *
     * @param focus - the focus of the parabola, this is the location of a
     * marker. The arc is added in place of the arc whose boundaries this point
     * falls under.
     * @return
     */
    private BlineElement addArc(final Vec3 focus) {
        BlineElement current = root;

        // Find the arc which this new point falls within
        final BlineElement intersectingElement = findIntersectingArc(current, focus.getX(), focus.getY());

        // The new arc to be added to the tree
        final Arc newArc = new Arc(focus, id++);

        final Arc intersectingArc = (Arc) intersectingElement;

        // The arc that the new point falls under is split into 2 with the new arc placed in between
        final Arc splitLeft = new Arc(intersectingArc.getFocus(), id++);
        final Arc splitRight = new Arc(intersectingArc.getFocus(), id++);

        // Two edges needs to be created at the point of the intersecting arc that has the same X coordinate of the focus
        final Vec3 edgeStart = new Vec3(focus.getX(), intersectingArc.getY(focus.getX(), focus.getY()));

        // The direction between the new focus and the focus of the intersecting arc
        final Vec3 direction = new Vec3(newArc.getFocus().getX() - intersectingArc.getFocus().getX(), newArc.getFocus().getY() - intersectingArc.getFocus().getY());

        // Create a new entry in the sitemap with the newFocus as the key and a HashSet which will hold all the corners of its shape
        if (!siteMap.containsKey(newArc.getFocus())) {
            siteMap.put(newArc.getFocus(), new HashSet<>());
        }

        // Since the initial intersection eill lie outside the map, move it back to the topBorder edge of the map
        final Vec3 relatedIntersection = new Vec3(edgeStart.getX(), edgeStart.getY() < 0 ? 0 : edgeStart.getY());

        // Add the intersection point to both the newArc's focus and the intersecting arc's focus in the site map
        siteMap.get(newArc.getFocus()).add(relatedIntersection);
        if (edgeStart.getY() >= 0) {
            addShapeVertex(intersectingArc.getFocus(), relatedIntersection);
        }

        final Line l = new Line();
        l.setStartX(relatedIntersection.getX() - 5);
        l.setStartY(relatedIntersection.getY());
        l.setEndX(relatedIntersection.getX() + 5);
        l.setEndY(relatedIntersection.getY());

        l.setStroke(Color.GREEN);
        l.setStrokeWidth(2);
        completedEdges.add(l);

        // The direction of the new edge will be perpendicular to the direction vector from the newFocus to the intersecing arc's focus
        final Vec3 dirVect = new Vec3(direction.getY(), -direction.getX());
        dirVect.normalizeVec2();

        // Create 2 edges with same starting point but go in the opposite direction to each other
        final HalfEdge e1 = new HalfEdge(newArc.getFocus(), intersectingArc.getFocus(), edgeStart, dirVect);
        final HalfEdge e2 = new HalfEdge(newArc.getFocus(), intersectingArc.getFocus(), edgeStart, new Vec3(-dirVect.getX(), -dirVect.getY()));

        edges.add(e1);
        edges.add(e2);

        // Set the rootLeftEdge and rootRightEdge child if the new arc to the split up intersection arcs
        newArc.setLeft(splitLeft);
        newArc.setRight(splitRight);
        splitLeft.setParent(newArc);
        splitRight.setParent(newArc);

        // Set the rootRightEdge edge of the rootLeftEdge split to be the new edge going rootLeftEdge
        splitLeft.setRightEdge(e2);

        // Set the rootLeftEdge edge of the rootRightEdge split to be the other new edge going rootRightEdge
        splitRight.setLeftEdge(e1);

        // Set the rootLeftEdge edge of the rootLeftEdge split to be the existing rootLeftEdge edge of the intersecting arc and vice versa for the the rootRightEdge split
        splitLeft.setLeftEdge(intersectingArc.getLeftEdge() != null ? intersectingArc.getLeftEdge() : getLeftNeighbour(intersectingArc).getRightEdge());
        splitRight.setRightEdge(intersectingArc.getRightEdge() != null ? intersectingArc.getRightEdge() : getRightNeighbour(intersectingArc).getLeftEdge());

        // Set any existing rootLeftEdge or rootRightEdge children to either the splitRight and splitLeft arcs
        // The rootLeftEdge child of the intersecting arc will be the rootLeftEdge child of the rootLeftEdge split and the same foes for the rootRightEdge child and rootRightEdge split
        // Also make sure to set the existing childrens parent to either leftSplit or rightSplit
        if (intersectingArc.getRight() != null) {
            splitRight.setRight(intersectingArc.getRight());
            intersectingArc.getRight().setParent(splitRight);
        }

        if (intersectingArc.getLeft() != null) {
            splitLeft.setLeft(intersectingArc.getLeft());
            intersectingArc.getLeft().setParent(splitLeft);
        }

        // Invalidate any event assigned to the intersected arc
        if (intersectingArc.getCurrentEvent() != null) {
            intersectingArc.getCurrentEvent().setValid(false);
        }

        // Set the parent of the new arc to be the parent of the intersected arc effectively removing it from the tree crudely
        newArc.setParentFromItem(intersectingElement);

        if (intersectingElement == root) {
            current = newArc;
        }

        // Add new edge intersection event to the rootRightEdge and rootLeftEdge split of the intersected arc
        addEdgeIntersectionEvent(splitLeft);
        addEdgeIntersectionEvent(splitRight);


        return current;
    }

    /**
     * Add an intersection event to an arc if an intersection exists. An
     * intersection event is when the edges to either side of the arc meets
     * making the arc disappear
     *
     * @param arc - The arc on which we are seeing if there will be an
     * intersection
     * @param directrix
     */
    private void addEdgeIntersectionEvent(final BlineElement arc) {
        if (arc == null) {
            return;
        }

        // Get the rootLeftEdge and rootRightEdge edge of an arc
        final HalfEdge edgeLeft = arc.getLeftEdge() != null ? arc.getLeftEdge() : getLeftNeighbour(arc) != null ? getLeftNeighbour(arc).getRightEdge() : null;
        final HalfEdge edgeRight = arc.getRightEdge() != null ? arc.getRightEdge() : getRightNeighbour(arc) != null ? getRightNeighbour(arc).getLeftEdge() : null;

        // If either of those edges do not exist it means to intersection event will occur on this arc
        if (edgeLeft == null || edgeRight == null) {

            return;
        }

        // Find out where the 2 edges will intersect
        final Vec3 intersectionPoint = findEdgeIntersectionPoint(edgeLeft, edgeRight);

        if (intersectionPoint == null) {
            return;
        }

        // Calculate the distance from the intersection point to an arc focus
        final double distance = Vec3.getDistance(edgeLeft.getParentFocus(), intersectionPoint);

        // Calculate the position of the directrix line when this interseciton occurs. This will be used to prioritize the event in the event queue.
        final double directrixY = intersectionPoint.getY() + distance;

        // If the arc has a current event
        if (arc.getCurrentEvent() != null) {
            // If this new event falls after the existing edge event then discard this new event
            if (directrixY >= arc.getCurrentEvent().getYCoord()) {
                return;
            }

            // Invalidate the currently assigned event in the arc
            arc.getCurrentEvent().setValid(false);
        }

        // Create an edge event containing its position, the edges involved, the arc to be removed and the intersection point
        final EdgeEvent e = new EdgeEvent(directrixY, edgeLeft, edgeRight, arc, intersectionPoint);
        arc.setCurrentEvent(e);
        eventQueue.add(e);
    }

    /**
     * Remove an arc from the tree
     *
     * @param current - The current root
     * @param remove - The arc to be removed
     * @return
     */
    private BlineElement removeElement(final BlineElement current, final BlineElement remove) {
        if (current == null) {
            return current;
        }

        // Case 1: Where both children of remove is null
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
            // Case 2: When it only has a rootLeftEdge child
        } else if (remove.getLeft() != null && remove.getRight() == null) {
            final BlineElement leftChild = remove.getLeft();
            leftChild.setParent(null);

            if (remove.getParent() != null) {
                if (remove.getParent().getLeft() == remove) {
                    remove.getParent().setLeft(leftChild);
                } else {
                    remove.getParent().setRight(leftChild);
                }

                leftChild.setParent(remove.getParent());
                return current;

            } else {
                return leftChild;
            }
            // Case 3: When it only has a rootRightEdge child
        } else if (remove.getRight() != null && remove.getLeft() == null) {
            final BlineElement right = remove.getRight();
            right.setParent(null);

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
            // Case 4: When there is 2 children
        } else {
            // Find in order successor
            BlineElement iSucc = remove.getRight();
            while (iSucc.getLeft() != null) {
                iSucc = iSucc.getLeft();
            }

            final BlineElement leftChild = remove.getLeft();

            // Remove successor
            removeElement(root, iSucc);
            final BlineElement rightChild = remove.getRight();

            // Set the successor as the replacement of the removed arc
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

    /**
     * Process an edge intersection event by removing an arc and recording an
     * intersection point
     *
     * @param e - The edge event being processed
     */
    private void removeArc(final EdgeEvent e) {
        // Get the arc to be removed and the edges involved in the intersection from the event
        final BlineElement removed = e.getSqueezed();
        final HalfEdge e1 = e.getEdge1();
        final HalfEdge e2 = e.getEdge2();

        e1.setComplete(true);
        e2.setComplete(true);

        // Get the rootLeftEdge arc of the removed arc
        final BlineElement left = getLeftNeighbour(removed);

        // Get the rootRightEdge arc of the removed arc
        final BlineElement right = getRightNeighbour(removed);

        final Vec3 intersectionPoint = e.getIntersectionPoint();


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

        final Arc leftArc = (Arc) left;
        final Arc rightArc = (Arc) right;

        // Get focuses of the rootLeftEdge and eight arc
        final Vec3 leftFocus = leftArc != null ? leftArc.getFocus() : edgeCaseFocus;
        final Vec3 rightFocus = rightArc != null ? rightArc.getFocus() : edgeCaseFocus;

        // Calculate the direction of the resulting edge to be perpendicular to the dirVect betweent he rootLeftEdge and rootRightEdge arcs
        final Vec3 direction = new Vec3(leftFocus.getX() - rightFocus.getX(), leftFocus.getY() - rightFocus.getY());
        final Vec3 dirNewEdge = new Vec3(direction.getY(), -direction.getX());
        dirNewEdge.normalizeVec2();

        // Create the new edge
        final HalfEdge newEdge = new HalfEdge(rightFocus, leftFocus, intersectionPoint, dirNewEdge);

        if (left != null) {
            left.setRightEdge(newEdge);
        }

        if (right != null) {
            right.setLeftEdge(newEdge);
        }

        // These 2 chunky ifs set the appropriate intersection point to the involved focuses of the intersection event
        // The intersection point in the event will belong to the shape of the focus of the arc being removed as well as its rootLeftEdge and rootRightEdge arcs
        // If the intersection point is outside of the map and the edges starting point is inside the map
        if ((isOutsideMap(intersectionPoint) && !isOutsideMap(e1.getStart()))) {
            // Move the intersection point to be on one of the map edges
            addBoundaryIntersections(e1);

            // If the intersection point is within the map and the start of the edge is outside the map
        } else if ((!isOutsideMap(intersectionPoint) && isOutsideMap(e1.getStart()))) {
            addShapeVertex(e1.getParentFocus(), intersectionPoint);
            addShapeVertex(e1.getHomeFocus(), intersectionPoint);

            // Calculate which boundary the edge intersects with and set a corner of the shapes belonging to the 2 focuses belonging to the edge
            addBoundaryIntersections(e1);

            // If the both the edge start and intersection point is withing the map then assign the intersection point to the sets of the removed arc and
            // rootLeftEdge arc in the site map
        } else if (left != null && !isOutsideMap(intersectionPoint) && !isOutsideMap(e1.getStart())) {
            addShapeVertex(((Arc) removed).getFocus(), intersectionPoint);
            addShapeVertex(leftArc.getFocus(), intersectionPoint);
        }

        // Same logic as above but for the second edge
        if ((isOutsideMap(intersectionPoint) && !isOutsideMap(e2.getStart()))) {
            addBoundaryIntersections(e2);
        } else if ((!isOutsideMap(intersectionPoint) && isOutsideMap(e2.getStart()))) {
            addShapeVertex(e2.getParentFocus(), intersectionPoint);
            addShapeVertex(e2.getHomeFocus(), intersectionPoint);
            addBoundaryIntersections(e2);
        } else if (right != null && !isOutsideMap(intersectionPoint) && !isOutsideMap(e2.getStart())) {
            addShapeVertex(((Arc) removed).getFocus(), intersectionPoint);
            addShapeVertex(rightArc.getFocus(), intersectionPoint);
        }


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

        completedEdges.add(l);
        completedEdges.add(l2);
        completedEdges.add(l3);
        completedEdges.add(l4);

        if (newEdge != null) {
            edges.add(newEdge);
        }

        // Invalidate any event of the removed from edge
        if (removed.getCurrentEvent() != null) {
            removed.getCurrentEvent().setValid(false);
        }

        root = removeElement(root, removed);

        // Find and add edge intersection events for the rootLeftEdge and rootRightEdge arc of the removed arc since a new edge has been created between them
        addEdgeIntersectionEvent(left);
        addEdgeIntersectionEvent(right);
    }

    /**
     * Find the intersection point between 2 edges
     *
     * @param h1 - half edge 1
     * @param h2 - half edge 2
     * @return
     */
    private Vec3 findEdgeIntersectionPoint(final HalfEdge h1, final HalfEdge h2) {
        final Vec3 dirLeft = new Vec3(-1, 0);
        final Vec3 dirRight = new Vec3(1, 0);
        final Vec3 dirUp = new Vec3(-1, 0);
        final Vec3 dirDown = new Vec3(1, 0);

        // Edge cases for when one edge is completely horizontal and the other complete vertical
        if (h1.getDirVect() == dirDown && h2.getDirVect() == dirRight && h1.getStart().getX() >= h2.getStart().getX() && h1.getStart().getY() <= h2.getStart().getY()) {
            return new Vec3(h1.getStart().getX(), h2.getStart().getY());
        } else if (h2.getDirVect() == dirDown && h1.getDirVect() == dirRight && h2.getStart().getX() >= h1.getStart().getX() && h2.getStart().getY() <= h1.getStart().getY()) {
            return new Vec3(h2.getStart().getX(), h1.getStart().getY());
        } else if (h1.getDirVect() == dirUp && h2.getDirVect() == dirRight && h1.getStart().getX() >= h2.getStart().getX() && h1.getStart().getY() >= h2.getStart().getY()) {
            return new Vec3(h1.getStart().getX(), h2.getStart().getY());
        } else if (h2.getDirVect() == dirUp && h1.getDirVect() == dirRight && h2.getStart().getX() >= h1.getStart().getX() && h2.getStart().getY() >= h1.getStart().getY()) {
            return new Vec3(h2.getStart().getX(), h1.getStart().getY());
        } else if (h1.getDirVect() == dirDown && h2.getDirVect() == dirLeft && h1.getStart().getX() <= h2.getStart().getX() && h1.getStart().getY() <= h2.getStart().getY()) {
            return new Vec3(h1.getStart().getX(), h2.getStart().getY());
        } else if (h2.getDirVect() == dirDown && h1.getDirVect() == dirLeft && h2.getStart().getX() <= h1.getStart().getX() && h2.getStart().getY() <= h1.getStart().getY()) {
            return new Vec3(h2.getStart().getX(), h1.getStart().getY());
        } else if (h1.getDirVect() == dirUp && h2.getDirVect() == dirLeft && h1.getStart().getX() <= h2.getStart().getX() && h1.getStart().getY() >= h2.getStart().getY()) {
            return new Vec3(h1.getStart().getX(), h2.getStart().getY());
        } else if (h2.getDirVect() == dirUp && h1.getDirVect() == dirLeft && h2.getStart().getX() <= h1.getStart().getX() && h2.getStart().getY() >= h1.getStart().getY()) {
            return new Vec3(h2.getStart().getX(), h1.getStart().getY());
        }

        final double dx = h2.getStart().getX() - h1.getStart().getX();

        final double dy = h2.getStart().getY() - h1.getStart().getY();

        final double det = h2.getDirVect().getX() * h1.getDirVect().getY() - h2.getDirVect().getY() * h1.getDirVect().getX();

        final double u = (dy * h2.getDirVect().getX() - dx * h2.getDirVect().getY()) / det;

        final double v = (dy * h1.getDirVect().getX() - dx * h1.getDirVect().getY()) / det;


        if (u < 0D && !h1.extendsUp()) {
            return null;
        }

        if (v < 0D && !h2.extendsUp()) {
            return null;
        }

        if (u == 0D && v == 0D && !h1.extendsUp() && !h2.extendsUp()) {
            return null;
        }

        return new Vec3(h1.getStart().getX() + h1.getDirVect().getX() * u, h1.getStart().getY() + h1.getDirVect().getY() * u);
    }

    /**
     * Find which arc a certain x coordinate falls under
     *
     * @param currentNode - the current root node
     * @param x - the x coord being search against
     * @param directrix - The position of the directrix (event) line used to
     * calculate the limits of both sides of the arc
     * @return - the arc in which the x coordinate fall under
     */
    private BlineElement findIntersectingArc(final BlineElement currentNode, final double x, final double directrix) {

        BlineElement current = currentNode;

        // If arc has no children it means it is the intersecting arc
        if (current.getLeft() == null && current.getRight() == null) {
            return current;
        }

        final Arc arc = (Arc) current;

        // Get rootLeftEdge and rootRightEdge edge of arc
        HalfEdge leftHalfEdge = arc.getLeftEdge();
        HalfEdge rightHalfEdge = arc.getRightEdge();

        if (leftHalfEdge == null && getLeftNeighbour(current) != null) {
            leftHalfEdge = getLeftNeighbour(current).getRightEdge();
        }

        if (rightHalfEdge == null && getRightNeighbour(current) != null) {
            rightHalfEdge = getRightNeighbour(current).getLeftEdge();
        }

        // Find out where those edges intersect with the arc to figure out the rootLeftEdge and rootRightEdge boundaries
        Vec3 leftIntersection = getEdgeArcIntersection(leftHalfEdge, arc, directrix);
        Vec3 rightIntersection = getEdgeArcIntersection(rightHalfEdge, arc, directrix);

        final double leftX = leftIntersection == null ? 0 : leftIntersection.getX();
        final double rightX = rightIntersection == null ? MapView.MAP_WIDTH : rightIntersection.getX();


        if (x > rightX) {
            // If the x coordinated is greater than the rootRightEdge x boundary of the arc then the intersecting arc exists on the rootRightEdge
            return findIntersectingArc(current.getRight(), x, directrix);
        } else if (x < leftX) {
            /// If the x coordinated is less than the rootLeftEdge x boundary of the arc then the intersecting arc exists on the rootLeftEdge
            return findIntersectingArc(current.getLeft(), x, directrix);
        } else {
            return current;
        }


    }

    /**
     * Find the intersection point between an edge and an arc
     *
     * @param edge - The edge that may be intersecting the arc
     * @param arc - The arc the edge is being tested against
     * @param directrix - directrix line to calculate growth of arc
     * @return
     */
    public Vec3 getEdgeArcIntersection(final HalfEdge edge, final Arc arc, final double directrix) {
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
        final double m = edge.getDirVect().getY() / edge.getDirVect().getX();
        final double b = edge.getStart().getY() - m * edge.getStart().getX();

        if (arc.getFocus().getY() == directrix) {
            final double intersectionXOffset = arc.getFocus().getX() - edge.getStart().getX();

            if (intersectionXOffset * edge.getDirVect().getX() < 0.0) {
                return null;
            }
            return new Vec3(arc.getFocus().getX(), m * arc.getFocus().getX() + b);
        }

        // y = ax^2 + bx + c
        final double a = 1.0 / (2.0 * (arc.getFocus().getY() - directrix));
        final double b2 = -m - 2.0 * a * arc.getFocus().getX();
        final double c = a * arc.getFocus().getX() * arc.getFocus().getX() + (arc.getFocus().getY() + directrix) * 0.5 - b;

        final double discriminant = b2 * b2 - 4.0 * a * c;
        if (discriminant < 0) {
            return null;
        }
        final double rootDisc = Math.sqrt(discriminant);
        final double x1 = (-b2 + rootDisc) / (2.0 * a);
        final double x2 = (-b2 - rootDisc) / (2.0 * a);

        final double x1Offset = x1 - edge.getStart().getX();
        final double x2Offset = x2 - edge.getStart().getX();
        final double x1Dot = x1Offset * edge.getDirVect().getX();
        final double x2Dot = x2Offset * edge.getDirVect().getX();

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

        final double y = arc.getY(x, directrix);

        return new Vec3(x, y);
    }

    /**
     * Get coordinate of marker that is closest to a corner
     *
     * @param corner - corner of map
     * @param h - half edge containing focus information
     * @return
     */
    private Vec3 getRelatedPointToCorner(final Vec3 corner, final HalfEdge h) {
        // Since an edge is the division of 2 markers only one marker can be close to the corner so return the one with the shortest distance
        return Vec3.getDistance(corner, h.getParentFocus()) < Vec3.getDistance(corner, h.getHomeFocus()) ? h.getParentFocus() : h.getHomeFocus();
    }

    /**
     * Set a marker to a corner
     *
     * @param edge - the edge containing maker focus information
     * @param intersectionPoint - The intersection point to a map edge
     * @param corner - The corner a marker will be assigned to
     * @param position - The position variable indicates which direction the
 intersection point is in. 0 means the intersection point is either above
     * or below the corner. 1 Means that the intersection is either to the
     * rootLeftEdge or rootRightEdge of the corner
     */
    private void setRelatedPointToCorner(final HalfEdge edge, final double distance, final Vec3 corner, final int position) {
        // If the distance to the intersection point is less than the distance to another point on the edge dictaded by the position
        if (distance < cornerMap.get(corner).get(position).getKey()) {
            // Get the marker coordinate related to the corner
            final Vec3 relatedPoint = getRelatedPointToCorner(corner, edge);

            // Put the marker coordinate in the appropriate position in the list which belongs to the specific corner we are tyring to set it to
            // Also make sure to store the new distance
            cornerMap.get(corner).set(position, new Pair<>(distance, relatedPoint));
        }
    }

    /**
     * Calculate which map boundary an edge intersects
     *
     * @param squeezedArcFocus
     * @param edge - The edge we are checking intersections for
     */
    private void addBoundaryIntersections(final HalfEdge edge) {
        final Vec3 topLineIntersection = findEdgeIntersectionPoint(edge, topBorder);
        final Vec3 bottomLineInteraction = findEdgeIntersectionPoint(edge, bottomBorder);
        final Vec3 leftLineInteraction = findEdgeIntersectionPoint(edge, leftBorder);
        final Vec3 rightLineInteraction = findEdgeIntersectionPoint(edge, rightBorder);

        // An edge can intersect multiple boundaries so have a list of intersection points
        final List<Vec3> boundaryIntersectionPoints = new ArrayList<>();

        // Based on which boundaries the edge intersects, record the distance between the starting point and the intersection point
        if (topLineIntersection != null) {
            topLineIntersection.setY(0);
            topLineIntersection.setZ(Vec3.getDistance(edge.getStart(), topLineIntersection));
            boundaryIntersectionPoints.add(topLineIntersection);
        }
        if (bottomLineInteraction != null) {
            bottomLineInteraction.setY(MapView.MAP_HEIGHT);
            bottomLineInteraction.setZ(Vec3.getDistance(edge.getStart(), bottomLineInteraction));
            boundaryIntersectionPoints.add(bottomLineInteraction);
        }
        if (leftLineInteraction != null) {
            leftLineInteraction.setX(0);
            leftLineInteraction.setZ(Vec3.getDistance(edge.getStart(), leftLineInteraction));
            boundaryIntersectionPoints.add(leftLineInteraction);
        }
        if (rightLineInteraction != null) {
            rightLineInteraction.setX(MapView.MAP_WIDTH);
            rightLineInteraction.setZ(Vec3.getDistance(edge.getStart(), rightLineInteraction));
            boundaryIntersectionPoints.add(rightLineInteraction);
        }

        // Sort the interseciton points in ascending order based on distance
        Collections.sort(boundaryIntersectionPoints, (v1, v2) -> {
            if (v1.getZ() > v2.getZ()) {
                return 1;
            }

            return -1;
        });

        // If intersections exist
        if (!boundaryIntersectionPoints.isEmpty()) {
            for (int i = 0; i < boundaryIntersectionPoints.size(); i++) {
                if (isOutsideMap(boundaryIntersectionPoints.get(i))) {
                    return;
                }

                // Based on which map edge the intersection point lies on
                // try to set the markers seperated by the edge to the four map corners that lie on the map boundary
                if (boundaryIntersectionPoints.get(i) == topLineIntersection) {
                    final double distanceToTopLeft = Vec3.getDistance(topLeft, topLineIntersection);
                    final double distanceToTopRight = Vec3.getDistance(topRight, topLineIntersection);

                    final double distanceToBottomLeft = distanceToTopLeft + MapView.MAP_HEIGHT;
                    final double distanceToBottomRight = distanceToTopRight + MapView.MAP_HEIGHT;

                    setRelatedPointToCorner(edge, distanceToTopLeft, topLeft, 1);
                    setRelatedPointToCorner(edge, distanceToTopRight, topRight, 1);
                    setRelatedPointToCorner(edge, distanceToBottomLeft, bottomLeft, 0);
                    setRelatedPointToCorner(edge, distanceToBottomRight, bottomRight, 0);
                } else if (boundaryIntersectionPoints.get(i) == leftLineInteraction) {
                    final double distanceToTopLeft = Vec3.getDistance(topLeft, leftLineInteraction);
                    final double distanceToBottomLeft = Vec3.getDistance(bottomLeft, leftLineInteraction);

                    final double distanceToTopRight = distanceToTopLeft + MapView.MAP_WIDTH;
                    final double distanceToBottomRight = distanceToBottomLeft + MapView.MAP_WIDTH;

                    setRelatedPointToCorner(edge, distanceToTopLeft, topLeft, 0);
                    setRelatedPointToCorner(edge, distanceToBottomLeft, bottomLeft, 0);
                    setRelatedPointToCorner(edge, distanceToTopRight, topRight, 1);
                    setRelatedPointToCorner(edge, distanceToBottomRight, bottomRight, 1);
                } else if (boundaryIntersectionPoints.get(i) == bottomLineInteraction) {
                    final double distanceToBottomRight = Vec3.getDistance(bottomRight, bottomLineInteraction);
                    final double distanceToBottomLeft = Vec3.getDistance(bottomLeft, bottomLineInteraction);

                    final double distanceToTopLeft = distanceToBottomLeft + MapView.MAP_HEIGHT;
                    final double distanceToTopRight = distanceToBottomLeft + MapView.MAP_HEIGHT;

                    setRelatedPointToCorner(edge, distanceToBottomLeft, bottomLeft, 1);
                    setRelatedPointToCorner(edge, distanceToBottomRight, bottomRight, 1);
                    setRelatedPointToCorner(edge, distanceToTopLeft, topLeft, 0);
                    setRelatedPointToCorner(edge, distanceToTopRight, topRight, 0);
                } else if (boundaryIntersectionPoints.get(i) == rightLineInteraction) {
                    final double distanceToTopRight = Vec3.getDistance(topRight, rightLineInteraction);
                    final double distanceToBottomRight = Vec3.getDistance(bottomRight, rightLineInteraction);

                    final double distanceToTopLeft = distanceToTopRight + MapView.MAP_WIDTH;
                    final double distanceToBottomLeft = distanceToBottomRight + MapView.MAP_WIDTH;

                    setRelatedPointToCorner(edge, distanceToTopRight, topRight, 0);
                    setRelatedPointToCorner(edge, distanceToBottomRight, bottomRight, 0);
                    setRelatedPointToCorner(edge, distanceToTopLeft, topLeft, 1);
                    setRelatedPointToCorner(edge, distanceToBottomLeft, bottomLeft, 1);
                }

                Line l4 = new Line();
                l4.setStartX(boundaryIntersectionPoints.get(i).getX() - 1);
                l4.setStartY(boundaryIntersectionPoints.get(i).getY());
                l4.setEndX(boundaryIntersectionPoints.get(i).getX() + 1);
                l4.setEndY(boundaryIntersectionPoints.get(i).getY());
                l4.setStroke(Color.BROWN);
                l4.setStrokeWidth(5);

                completedEdges.add(l4);

                final Line l1 = new Line();
                l1.setStartX(boundaryIntersectionPoints.get(i).getX());
                l1.setStartY(boundaryIntersectionPoints.get(i).getY());
                l1.setEndX(edge.getHomeFocus().getX());
                l1.setEndY(edge.getHomeFocus().getY());
                l1.setStroke(Color.PINK);
                l1.setVisible(false);
                completedEdges.add(l1);

                final Line l2 = new Line();
                l2.setStartX(boundaryIntersectionPoints.get(i).getX());
                l2.setStartY(boundaryIntersectionPoints.get(i).getY());
                l2.setEndX(edge.getParentFocus().getX());
                l2.setEndY(edge.getParentFocus().getY());
                l2.setStroke(Color.PINK);
                l2.setVisible(false);
                completedEdges.add(l2);

                l4.setOnMouseEntered(event -> {
                    l1.setVisible(true);
                    l2.setVisible(true);
                });

                l4.setOnMouseExited(event -> {
                    l1.setVisible(false);
                    l2.setVisible(false);
                });

                // Add intersection point as shape corner of the 2 markers focuses seperated by the line
                addShapeVertex(edge.getHomeFocus(), boundaryIntersectionPoints.get(i));
                addShapeVertex(edge.getParentFocus(), boundaryIntersectionPoints.get(i));

                if (edge.isComplete()) {
                    break;
                }
            }
        }
    }

    /**
     * Add the corner of the map to the correct shape in the site map
     */
    private void addMapCornersToShapes() {
        // Loop through each corner
        cornerMap.entrySet().forEach(entry -> {
            // Loop through the focuses related to each corner
            for (int i = 0; i < entry.getValue().size(); i++) {
                // If the site map contains the marker related to the corner then add the corner to its set
                if (siteMap.containsKey(entry.getValue().get(i).getValue())) {
                    siteMap.get(entry.getValue().get(i).getValue()).add(entry.getKey());
                }
            }
        });
    }

    private void addShapeVertex(final Vec3 siteCoordinate, final Vec3 vertex) {
        if (siteCoordinate.getX() != rootFocus.getX() && siteCoordinate.getY() != rootFocus.getY()) {
            siteMap.get(siteCoordinate).add(vertex);
        }
    }

    /**
     * Get in order successor
     *
     * @param item
     * @return the in-order successor of item
     */
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
                return null;
            }

            return right;
        }

        if (current.getParent() != null && current.getParent().getLeft() == current) {

            if (current.getParent() == item) {
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
                return null;
            }

            return current.getParent();
        }

        return null;
    }

    /**
     * Get pre-order successor the passed in element
     *
     * @param item
     * @return - Pre-order successor
     */
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
                return null;
            }

            return left;
        }

        if (current.getParent() != null && current.getParent().getRight() == current) {

            if (current.getParent() == item) {
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
                return null;
            }

            return current.getParent();
        }

        return null;
    }

    /**
     * Extends out any of the incomplete edges that are in the map
     */
    private void finishEdges() {
        edges.forEach(edge -> {
            if (!edge.isComplete()) {

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

                addBoundaryIntersections(edge);

                edge.setComplete(true);
            }
        });
    }

    /**
     * Sort the intersection points in the correct order so that the shapes
     * render properly
     */
    private void sortIntersectionPoints() {
        // For each set of shape corners in the site map
        siteMap.values().forEach(intersectionSet -> {
            double x = 0;
            double y = 0;

            final Iterator<Vec3> pointsIterator = (intersectionSet.iterator());

            // Calculate the average point of all the intersection points within the set
            while (pointsIterator.hasNext()) {
                final Vec3 point = pointsIterator.next();
                x += point.getX();
                y += point.getY();
            }

            x = x / intersectionSet.size();
            y = y / intersectionSet.size();

            // Array list to store shape coordinates with angle information
            final List<Vec3> shape = new ArrayList<>();

            final Iterator<Vec3> pointsIterator2 = intersectionSet.iterator();

            // Loop through the intersection points again
            while (pointsIterator2.hasNext()) {
                final Vec3 currentPoint = pointsIterator2.next();

                // Calculate the direction vector from the average point to this intersection point
                final double dx = currentPoint.getX() - x;
                final double dy = currentPoint.getY() - y;

                // Calculate the degree between this direction vector and a horizontal line lying on the average point
                double degrees = Math.toDegrees(Math.atan2(dy, dx));

                if (degrees < 0) {
                    degrees += 360;
                }

                // Store the degree
                currentPoint.setZ(degrees);

                // Add the intersection point to the shape list
                shape.add(currentPoint);
            }

            // Sort the intersections via the stored angle so they are in a clockwise order
            Collections.sort(shape, (v1, v2) -> {
                if (v1.getZ() > v2.getZ()) {
                    return 1;
                }

                return -1;
            });

            // Add this new sorted intersection point array to another array list contining other sorted lists of intersection points
            finalShapeCorners.add(shape);
        });
    }

    /**
     * Generate the actual polygon object
     */
    private void generateShapes() {
        // Get colours
        final ConstellationColor[] palette = ConstellationColor.createPalette(finalShapeCorners.size());

        // Loop through all the shapes
        for (int i = 0; i < finalShapeCorners.size(); i++) {
            // Create shape object, set colour and opacity
            final Polygon shape = new Polygon();

            shape.setStroke(palette[i].getJavaFXColor());
            shape.setFill(palette[i].getJavaFXColor());
            shape.setOpacity(0.5);

            shape.setMouseTransparent(true);

            // Add the corners to the shape object
            finalShapeCorners.get(i).forEach(point -> shape.getPoints().addAll(new Double[]{point.getX(), point.getY()}));

            completedShapes.add(shape);
        }
    }

    public List<Line> getCompletedEdges() {
        return completedEdges;
    }

    public List<Polygon> getCompletedShapes() {
        return completedShapes;
    }


    /**
     * Check to see if a point lies outside the map or not
     *
     * @param point
     * @return
     */
    private boolean isOutsideMap(final Vec3 point) {
        return point.getX() < 0 || point.getX() > MapView.MAP_WIDTH || point.getY() < 0 || point.getY() > MapView.MAP_HEIGHT;
    }

    /**
     * This function runs fortune's algorithm
     */
    public void run() {
        while (!eventQueue.isEmpty()) {
            // If the event is a site event then add an arc, else if it is an edge event
            // then remove the arc
            if (eventQueue.peek() instanceof SiteEvent) {
                final SiteEvent e = (SiteEvent) eventQueue.poll();

                final Line l = new Line();
                l.setStartX(e.getSite().getX() - 2);
                l.setStartY(e.getSite().getY());
                l.setEndX(e.getSite().getX() + 2);
                l.setEndY(e.getSite().getY());
                l.setStroke(Color.BLUE);
                completedEdges.add(l);

                root = addArc(e.getSite());
            } else if (eventQueue.peek() instanceof EdgeEvent) {
                final EdgeEvent e = (EdgeEvent) eventQueue.poll();

                if (e.isValid()) {
                    removeArc(e);
                }

            }
        }

        finishEdges();
        addMapCornersToShapes();
        sortIntersectionPoints();
        generateShapes();


    }

}
