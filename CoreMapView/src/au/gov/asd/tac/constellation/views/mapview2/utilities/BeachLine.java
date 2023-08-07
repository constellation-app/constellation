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
package au.gov.asd.tac.constellation.views.mapview2.utilities;

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.VoronoiEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

/**
 *
 * @author altair1673
 */
public class BeachLine {
    private BeachLineElement root;
    private final PriorityQueue<VoronoiEvent> eventQueue;
    private static final Logger LOGGER = Logger.getLogger(BeachLine.class.getName());
    private final List<Line> completedEdges = new ArrayList<>();

    public BeachLine(final PriorityQueue<VoronoiEvent> eventQueue) {
        //root = new Parabola(new Vec3(-100, -100), new Vec3(MapView.MAP_WIDTH + 100, -100), MapView.MAP_WIDTH / 2, new Vec3(MapView.MAP_WIDTH / 2, -35));

        this.eventQueue = eventQueue;
    }

    public BeachLineElement searchArcs(final Parabola element, double directrix) {
        LOGGER.log(Level.SEVERE, "Called search arcs");
        BeachLineElement current = root;

        if (current instanceof Parabola) {
            LOGGER.log(Level.SEVERE, "Beachline Element is a parabola so no searching required");
        }

        while (!(current instanceof Parabola)) {
            final BeachLineElement left = findLeftLeaf(current);
            final BeachLineElement right = findRightLeaf(current);

            final BeachLineElement leftParent = findLeftParent(left);
            final BeachLineElement rightParent = findRightParent(right);

            final Vec3 leftIntersect = getEdgeArcIntersection((Edge) leftParent, (Parabola) left, directrix);
            final Vec3 rightIntersect = getEdgeArcIntersection((Edge) leftParent, (Parabola) right, directrix);

            double intersectionX = leftIntersect.getX();
            if (leftIntersect == null && rightIntersect != null) {
                intersectionX = rightIntersect.getX();
            }

            if (element.getSite().getX() < intersectionX) {
                current = current.getLeft();
            } else {
                current = current.getRight();
            }
        }

        return current;
    }

    private boolean checkIfSplitArcIsRemoved(BeachLineElement currentRoot, BeachLineElement currentArc) {
        if (currentRoot == null) {
            return true;
        } else if (currentRoot instanceof Parabola) {
            return true;
        }

        return checkIfSplitArcIsRemoved(currentRoot.getLeft(), currentArc) && checkIfSplitArcIsRemoved(currentRoot.getRight(), currentArc);
    }

    private Vec3 getEdgeArcIntersection(final Edge edge, final Parabola arc, final double directrix) {
        LOGGER.log(Level.SEVERE, "Checking for edge to arc intersection");
        if (edge == null) {
            LOGGER.log(Level.SEVERE, "Edge is null");
        }
        if (edge.getDirVect().getX() == 0) {
            if (directrix == arc.getSite().getY()) {
                if (edge.getStart().getX() == arc.getSite().getX()) {
                    return new Vec3(arc.getSite());
                } else {
                    return null;
                }
            }

            return new Vec3(edge.getStart().getX(), arc.getY(edge.getStart().getX(), directrix));
        }

        // y = mx + b
        final double m = edge.getDirVect().getY() / edge.getDirVect().getX();
        final double b = edge.getStart().getY() - m * edge.getStart().getX();

        if (arc.getSite().getY() == directrix) {
            final double intersectionXOffset = arc.getSite().getX() - edge.getStart().getX();

            if (intersectionXOffset * edge.getDirVect().getX() < 0) {
                return null;
            }

            return new Vec3(arc.getSite().getX(), (m * arc.getSite().getX()) + b);
        }

        double yMiddle = arc.getY(arc.getSite().getX(), directrix);

        // y = a(x-h)^2 + k
        // a(x - arc.getSite().getX())^2 + yMiddle = y
        // a(-arc.getSite().getX())^2 = c - yMiddle
        // y = ax^2 + bPx + c
        final double c = arc.getY(0, directrix);
        final double a = (c - yMiddle) / Math.pow(-arc.getSite().getX(), 2);

        // a * arc.getSite().getX()^2 + bPx = (yMiddle - c)
        // bPx = (yMiddle - c) / a * arc.getSite().getX() ^ 2
        // bP = ((yMiddle - c) / a * arc.getSite().getX() ^ 2) / arc.getSite().getX()
        final double bP = ((yMiddle - c) / (a * Math.pow(arc.getSite().getX(), 2))) / arc.getSite().getX();

        // ax^2 + bPx + c = mx + b
        // ax^2 + bPx - mx + (c - b) = 0;
        // ax^2 + (bP - m)x + (c-b) = 0;
        // a = a, b = bP - m, c = c - b
        final double discriminant = Math.pow(bP - m, 2) - 4 * a * (c - b);

        if (discriminant < 0) {
            return null;
        }

        final double x1 = (-(bP - m) + Math.sqrt(discriminant)) / 2 * a;
        final double x2 = (-(bP - m) - Math.sqrt(discriminant)) / 2 * a;

        // Figure out which intersection point we need
        final double x1Offset = x1 - edge.getStart().getX();
        final double x2Offset = x2 - edge.getStart().getX();

        final double x1Dot = x1Offset * edge.getDirVect().getX();
        final double x2Dot = x2Offset * edge.getDirVect().getX();

        if (x1Dot >= 0 && x2Dot < 0) {
            return new Vec3(x1, arc.getY(x1, directrix));
        } else if (x1Dot < 0 && x2Dot >= 0) {
            return new Vec3(x2, arc.getY(x2, directrix));
        } else if (x1Dot >= 0 && x2Dot >= 0) {
            if (x1Dot < x2Dot) {
                return new Vec3(x1, arc.getY(x1, directrix));
            } else {
                return new Vec3(x2, arc.getY(x2, directrix));
            }
        } else {
            if (x1Dot < x2Dot) {
                return new Vec3(x2, arc.getY(x1, directrix));
            } else {
                return new Vec3(x1, arc.getY(x2, directrix));
            }
        }
    }

    private BeachLineElement insertNewArcSequence(BeachLineElement currentNode, final Parabola element) {
        LOGGER.log(Level.SEVERE, "Trying to insert a new arc");
        if (currentNode == null) {
            return null;
        }

        BeachLineElement searchedArc = searchArcs(element, element.getSite().getY());
        BeachLineElement splitLeft = getNewArc(((Parabola) searchedArc).getSite());
        BeachLineElement splitRight = getNewArc(((Parabola) searchedArc).getSite());
        BeachLineElement newArc = getNewArc(element.getSite());

        final double yIntersect = ((Parabola) searchedArc).getY(element.getSite().getX(), element.getSite().getY());
        final Vec3 edgeStart = new Vec3(element.getSite().getY(), yIntersect);
        final Vec3 focusVect = new Vec3(element.getSite().getX() - ((Parabola) searchedArc).getSite().getX(), element.getSite().getY() - ((Parabola) searchedArc).getSite().getY());

        final double distance = Vec3.getDistance(((Parabola) searchedArc).getSite(), element.getSite());
        final Vec3 normFocusVect = new Vec3(focusVect.getX() / distance, focusVect.getY() / distance);

        final Vec3 edgeDirVect = new Vec3(normFocusVect.getY(), -normFocusVect.getX());
        final BeachLineElement leftEdge = getNewEdge(edgeStart, edgeDirVect);
        final BeachLineElement rightEdge = getNewEdge(edgeStart, new Vec3(-edgeDirVect.getX(), -edgeDirVect.getY()));

        leftEdge.setParentFromItem(searchedArc);

        leftEdge.setLeft(splitLeft);
        leftEdge.setRight(rightEdge);

        rightEdge.setLeft(newArc);
        rightEdge.setRight(splitRight);

        BeachLineElement currentRoot = root;

        if (root == searchedArc) {
            currentRoot = searchedArc;
        }
        addEdgeIntersectionEvent(splitLeft);
        addEdgeIntersectionEvent(splitRight);

        return currentRoot;
    }

    private void addEdgeIntersectionEvent(final BeachLineElement arc) {
        LOGGER.log(Level.SEVERE, "Trying to add an edge intersection event");
        final BeachLineElement leftEdge = findLeftParent(arc);
        final BeachLineElement rightEdge = findRightParent(arc);

        if (leftEdge == null || rightEdge == null) {
            LOGGER.log(Level.SEVERE, "edges do not intersect because left or right edge is null");
            return;
        }

        final Vec3 intersectionPoint = getEdgeIntersection((Edge) leftEdge, (Edge) rightEdge);

        if (intersectionPoint == null) {
            LOGGER.log(Level.SEVERE, "edges do not have interseciton point");
            return;
        }

        LOGGER.log(Level.SEVERE, "The edges to intersect");

        final Vec3 eventOffset = new Vec3(((Parabola) arc).getSite().getX() - intersectionPoint.getX(), ((Parabola) arc).getSite().getY() - intersectionPoint.getY());

        final double distance = Math.sqrt(Math.pow(eventOffset.getX(), 2) + Math.pow(eventOffset.getY(), 2));
        final double directrixY = intersectionPoint.getY() + distance;

        if (((Parabola) arc).getCurrentEdgeEvent() != null) {
            if (((Parabola) arc).getCurrentEdgeEvent().getYCoord() <= directrixY) {
                return;
            } else {
                ((Parabola) arc).getCurrentEdgeEvent().setValid(false);
            }
        }

        //final EdgeEvent edgeEvent = new EdgeEvent(directrixY, intersectionPoint, (Edge) leftEdge, (Edge) rightEdge, (Parabola) arc);


        //eventQueue.add(edgeEvent);

        //((Parabola) arc).setCurrentEdgeEvent(edgeEvent);
    }

    private Vec3 getEdgeIntersection(final Edge e1, final Edge e2) {
        LOGGER.log(Level.SEVERE, "Checking if 2 edges intersect");
        // mx + b = m2x + b2
        // mx + b - m2x - b2 = 0
        // mx - m2x = b2 - b
        // x = (b2-b) / (m-m2)

        // TODO refactor to be less lines of code
        final double m = e1.getDirVect().getY() / e1.getDirVect().getX();
        final double b = e1.getStart().getY() - m * e1.getStart().getX();

        final double m2 = e2.getDirVect().getY() / e2.getDirVect().getX();
        final double b2 = e2.getStart().getY() - m2 * e2.getStart().getX();

        if (m == m2) {
            return null;
        }

        final double x = (b2 - b) / (m - m2);
        final double y = m * x + b;

        final Vec3 intersectionPoint = new Vec3(x, y);

        final Vec3 dirVect = new Vec3(intersectionPoint.getX() - e1.getStart().getX(), intersectionPoint.getY() - e1.getStart().getY());
        final Vec3 dirVect2 = new Vec3(intersectionPoint.getX() - e2.getStart().getX(), intersectionPoint.getY() - e2.getStart().getY());

        dirVect.setX(dirVect.getX() / Vec3.getDistance(intersectionPoint, e1.getStart()));
        dirVect.setY(dirVect.getY() / Vec3.getDistance(intersectionPoint, e1.getStart()));

        dirVect2.setX(dirVect.getX() / Vec3.getDistance(intersectionPoint, e2.getStart()));
        dirVect2.setY(dirVect.getY() / Vec3.getDistance(intersectionPoint, e2.getStart()));

        if ((e1.getDirVect().getX() != dirVect.getX() && e1.getDirVect().getY() != dirVect.getY()) || (e2.getDirVect().getX() != dirVect2.getX() && e2.getDirVect().getY() != dirVect2.getY())) {
            return null;
        }

        return intersectionPoint;
    }

    private BeachLineElement getNewEdge(final Vec3 start, final Vec3 dirVect) {
        LOGGER.log(Level.SEVERE, "Creating new edge");
        final Edge newEdge = new Edge(start, start, start.getX());
        newEdge.setDirVect(dirVect);

        final Line line = new Line();

        line.setStartX(start.getX());
        line.setStartY(start.getY());

        line.setEndX(start.getX() + (dirVect.getX() * 20));
        line.setEndY(start.getY() + (dirVect.getY() * 20));

        line.setStroke(Color.RED);
        completedEdges.add(line);

        return newEdge;
    }

    private BeachLineElement getNewArc(final Vec3 focus) {
        LOGGER.log(Level.SEVERE, "Checking new arc");
        final BeachLineElement newArc = new Parabola(new Vec3(focus.getX(), 0), new Vec3(focus.getX(), 0), focus.getX(), focus);

        final Line line = new Line();

        line.setStartX(focus.getX() - 5);
        line.setStartY(focus.getY());

        line.setEndX(focus.getX() + 5);
        line.setEndY(focus.getY());

        line.setStroke(Color.RED);
        //completedEdges.add(line);

        return newArc;
    }

    private BeachLineElement findLeftParent(final BeachLineElement current) {
        LOGGER.log(Level.SEVERE, "Looking for first left parent");
        BeachLineElement lParent = current;

        while (lParent.getParent() != null && lParent.getParent().getLeft() == lParent) {
            lParent = lParent.getParent();
        }

        if (lParent.getParent() == null) {
            LOGGER.log(Level.SEVERE, "I will return null");
        }
        return lParent.getParent();
    }

    private BeachLineElement findRightParent(final BeachLineElement current) {
        LOGGER.log(Level.SEVERE, "Looking for first right parent");
        BeachLineElement rParent = current;

        while (rParent.getParent() != null && rParent.getParent().getRight() == rParent) {
            rParent = rParent.getParent();
        }

        if (rParent.getParent() == null) {
            LOGGER.log(Level.SEVERE, "I will return null");
        }
        return rParent.getParent();
    }

    private BeachLineElement findLeftLeaf(final BeachLineElement current) {
        LOGGER.log(Level.SEVERE, "Looking for first left leaf");
        if (current.getLeft() == null) {
            return null;
        }

        BeachLineElement lLeaf = current.getLeft();
        while (lLeaf.getRight() != null) {
            lLeaf = lLeaf.getRight();
        }

        return lLeaf;
    }

    private BeachLineElement findRightLeaf(final BeachLineElement current) {
        LOGGER.log(Level.SEVERE, "Looking for first right leaf");
        if (current.getRight() == null) {
            return null;
        }

        BeachLineElement rLeaf = current.getRight();
        while (rLeaf.getLeft() != null) {
            rLeaf = rLeaf.getLeft();
        }

        return rLeaf;
    }

    private BeachLineElement removeArc(/*final EdgeEvent evt*/) {
        /*LOGGER.log(Level.SEVERE, "Removing arc");
        final Parabola remArc = evt.getInvolvedArc();

        final BeachLineElement leftEdge = findLeftParent(remArc);
        final BeachLineElement rightEdge = findRightParent(remArc);

        final BeachLineElement leftArc = findLeftLeaf(leftEdge);
        final BeachLineElement rightArc = findRightLeaf(rightEdge);

        final Vec3 intersectionPoint = evt.getIntersectionPoint();

        final Line l1 = new Line();

        l1.setStartX(((Edge) leftArc).getStart().getX());
        l1.setStartY(((Edge) leftArc).getStart().getY());
        l1.setEndX(intersectionPoint.getX());
        l1.setEndY(intersectionPoint.getY());

        final Line l2 = new Line();

        l2.setStartX(((Edge) rightArc).getStart().getX());
        l2.setStartY(((Edge) rightArc).getStart().getY());
        l2.setEndX(intersectionPoint.getX());
        l2.setEndY(intersectionPoint.getY());

        if (((Edge) leftEdge).isExtendsUp()) {
            l1.setEndY(Double.MAX_VALUE);
        }

        if (((Edge) rightEdge).isExtendsUp()) {
            l2.setEndY(Double.MAX_VALUE);
        }

        l1.setStroke(Color.BLUE);
        l2.setStroke(Color.BLUE);

        completedEdges.add(l1);
        completedEdges.add(l2);

        LOGGER.log(Level.SEVERE, "Added completed edges");

        final Vec3 directionArcs = new Vec3(((Parabola) leftArc).getStart().getX() - ((Parabola) rightArc).getStart().getX(), ((Parabola) leftArc).getStart().getY() - ((Parabola) rightArc).getStart().getY());
        final Vec3 dirVect = new Vec3(directionArcs.getY(), -directionArcs.getX());
        dirVect.normalizeVec2();

        final BeachLineElement newEdge = getNewEdge(intersectionPoint, dirVect);

        BeachLineElement higherEdge = null;
        BeachLineElement temp = remArc;

        while (temp.getParent() != null) {
            temp = temp.getParent();

            if (temp == leftEdge) {
                higherEdge = leftEdge;
            } else if (temp == rightEdge) {
                higherEdge = rightEdge;
            }
        }

        newEdge.setParentFromItem(higherEdge);
        newEdge.setLeft(higherEdge.getLeft());
        newEdge.setRight(higherEdge.getRight());

        BeachLineElement remainingElement = null;
        BeachLineElement parent = remArc.getParent();

        if (parent.getLeft() == remArc) {
            remainingElement = parent.getRight();
        } else {
            remainingElement = parent.getLeft();
        }

        remainingElement.setParentFromItem(parent);

        BeachLineElement newRoot = root;

        if (root == leftEdge || root == rightEdge) {
            newRoot = newEdge;
        }

        if (((Parabola) newRoot).getCurrentEdgeEvent() != null) {
            ((Parabola) newRoot).getCurrentEdgeEvent().setValid(false);
        }

        addEdgeIntersectionEvent(leftArc);
        addEdgeIntersectionEvent(rightArc);

        return newRoot;*/
        return null;
    }

    public void processSiteEvent(final Parabola p) {
        root = insertNewArcSequence(root, p);
    }

    public void processEdgeIntersectionEvent(/*final EdgeEvent e*/) {
        //root = removeArc(e);
    }

    public List<Polyline> getArcs() {
        return getArcs(root);
    }

    private List<Polyline> getArcs(BeachLineElement current) {
        final List<Polyline> allArcs = new ArrayList<>();

        if (current instanceof Parabola) {
            final Parabola p = (Parabola) current;

            final Polyline pl = p.getParabola();
            pl.setStroke(Color.RED);
            allArcs.add(pl);
        } else if (current instanceof Edge) {
            LOGGER.log(Level.SEVERE, "Arc is instance of edge");
            final Edge e = (Edge) current;
            List<Polyline> childArcs = new ArrayList();
            if (e.getLeft() != null) {
                childArcs = getArcs(e.getLeft());
            }
            childArcs.forEach(arc -> allArcs.add(arc));
            if (e.getRight() != null) {
                childArcs = getArcs(e.getRight());
            }

            childArcs.forEach(arc -> allArcs.add(arc));
        }

        return allArcs;
    }

    public List<Line> getCompletedEdges() {
        return completedEdges;
    }

    public void setRoot(BeachLineElement root) {
        this.root = root;
    }

}
