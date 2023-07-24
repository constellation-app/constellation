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
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;

/**
 *
 * @author altair1673
 */
public class BeachLine {
    private BeachLineElement root;
    private final PriorityQueue<VoronoiEvent> eventQueue;
    private static final Logger LOGGER = Logger.getLogger(BeachLine.class.getName());


    public BeachLine(final PriorityQueue<VoronoiEvent> eventQueue) {
        root = new Parabola(new Vec3(-100, -100), new Vec3(MapView.MAP_WIDTH + 100, -100), MapView.MAP_WIDTH / 2, new Vec3(MapView.MAP_WIDTH / 2, -35));
        root.setParent(null);

        this.eventQueue = eventQueue;
    }

    public void splitArc(final Parabola element) {
        insertNewArcSequence(root, element);
    }

    private BeachLineElement searchArcs(final Parabola element, double directrix) {
        BeachLineElement current = root;

        while (!(current instanceof Parabola)) {
            BeachLineElement left = findLeftLeaf(current);
            BeachLineElement right = findRightLeaf(current);

            BeachLineElement leftParent = findLeftParent(left);
            BeachLineElement rightParent = findRightParent(right);

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
        final BeachLineElement leftEdge = findLeftParent(arc);
        final BeachLineElement rightEdge = findRightParent(arc);

        if (leftEdge == null || rightEdge == null) {
            return;
        }

        final Vec3 intersectionPoint = getEdgeIntersection((Edge) leftEdge, (Edge) rightEdge);

        if (intersectionPoint == null) {
            return;
        }

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

        final EdgeEvent edgeEvent = new EdgeEvent(directrixY, intersectionPoint, (Edge) leftEdge, (Edge) rightEdge, (Parabola) arc);


        eventQueue.add(edgeEvent);

        ((Parabola) arc).setCurrentEdgeEvent(edgeEvent);
    }

    private Vec3 getEdgeIntersection(final Edge e1, final Edge e2) {
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
        final Edge newEdge = new Edge(start, start, start.getX());
        newEdge.setDirVect(dirVect);
        return newEdge;
    }

    private BeachLineElement getNewArc(final Vec3 focus) {
        final BeachLineElement newArc = new Parabola(new Vec3(focus.getX(), 0), new Vec3(focus.getX(), 0), focus.getX(), focus);
        return newArc;
    }

    private BeachLineElement findLeftParent(final BeachLineElement current) {
        BeachLineElement lParent = current;

        while (lParent.getParent() != null && lParent.getParent().getLeft() == lParent) {
            lParent = lParent.getParent();
        }

        return lParent.getParent();
    }

    private BeachLineElement findRightParent(final BeachLineElement current) {
        BeachLineElement rParent = current;

        while (rParent.getParent() != null && rParent.getParent().getRight() == rParent) {
            rParent = rParent.getParent();
        }

        return rParent;
    }

    private BeachLineElement findLeftLeaf(final BeachLineElement current) {
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
        if (current.getRight() == null) {
            return null;
        }

        BeachLineElement rLeaf = current.getRight();
        while (rLeaf.getLeft() != null) {
            rLeaf = rLeaf.getLeft();
        }

        return rLeaf;
    }

    private BeachLineElement removeArc() {
        return null;
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

}
