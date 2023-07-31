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

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 *
 * @author altair1673
 */
public class ArcTree {

    private BlineElement root;
    private final List<Line> completedEdges = new ArrayList<>();
    private final PriorityQueue<VoronoiEvent> eventQueue;
    private static final Logger LOGGER = Logger.getLogger(ArcTree.class.getName());


    public ArcTree(final PriorityQueue<VoronoiEvent> eventQueue) {
        this.eventQueue = eventQueue;
        root = new BaseLine(new Vec3(0, 0), new Vec3(MapView.MAP_WIDTH, 0));
    }

    private BlineElement addArc(final Vec3 focus) {
        final BlineElement current = root;
        final BlineElement intersectingArc = findIntersectingArc(current, focus.getX(), focus.getY());
        if (intersectingArc instanceof BaseLine) {
            final Arc newArc = new Arc(focus);

            final BaseLine splitLeft = new BaseLine(((BaseLine) intersectingArc).getStart(), new Vec3(focus.getX(), ((BaseLine) intersectingArc).getStart().getY()));
            final BaseLine splitRight = new BaseLine(new Vec3(focus.getX(), ((BaseLine) intersectingArc).getStart().getY()), ((BaseLine) intersectingArc).getEnd());

            final Vec3 edgeStart = new Vec3(focus.getX(), ((BaseLine) intersectingArc).getStart().getY());
            final Vec3 dirVect = new Vec3(1, 0);

            final HalfEdge e1 = new HalfEdge(newArc, intersectingArc, edgeStart, dirVect);
            final HalfEdge e2 = new HalfEdge(newArc, intersectingArc, edgeStart, new Vec3(-dirVect.getX(), -dirVect.getY()));

            e1.setParentArc(newArc);
            e1.setHomeArc(splitRight);

            e2.setParentArc(newArc);
            e2.setHomeArc(splitLeft);

            splitLeft.setRightEdge(e2);
            splitRight.setLeftEdge(e1);

            newArc.setLeft(splitLeft);
            newArc.setRight(splitRight);

            splitRight.setRight(intersectingArc.getRight());
            splitLeft.setLeft(intersectingArc.getLeft());

            addEdgeIntersectionEvent(splitLeft);
            addEdgeIntersectionEvent(splitRight);

            if (intersectingArc == root) {
                return newArc;
            }

            newArc.setParentFromItem(intersectingArc);
        } else {
            final Arc newArc = new Arc(focus);

            final Arc splitLeft = new Arc(((Arc) intersectingArc).getFocus());
            final Arc splitRight = new Arc(((Arc) intersectingArc).getFocus());

            final Vec3 edgeStart = new Vec3(focus.getX(), ((Arc) intersectingArc).getY(focus.getX(), focus.getY()));
            final Vec3 direction = new Vec3(((Arc) intersectingArc).getFocus().getX() - newArc.getFocus().getX(), ((Arc) intersectingArc).getFocus().getY() - newArc.getFocus().getY());
            final Vec3 dirVect = new Vec3(direction.getY(), -direction.getX());
            dirVect.normalizeVec2();

            final HalfEdge e1 = new HalfEdge(newArc, intersectingArc, edgeStart, dirVect);
            final HalfEdge e2 = new HalfEdge(newArc, intersectingArc, edgeStart, new Vec3(-dirVect.getX(), -dirVect.getY()));

            e1.setParentArc(newArc);
            e1.setHomeArc(splitRight);

            e2.setParentArc(newArc);
            e2.setHomeArc(splitLeft);

            splitLeft.setRightEdge(e2);
            splitRight.setLeftEdge(e1);


            newArc.setLeft(splitLeft);
            newArc.setRight(splitRight);

            splitRight.setRight(intersectingArc.getRight());
            splitLeft.setLeft(intersectingArc.getLeft());

            addEdgeIntersectionEvent(splitLeft);
            addEdgeIntersectionEvent(splitRight);

            if (intersectingArc == root) {
                return newArc;
            }

            newArc.setParentFromItem(intersectingArc);
        }

        return current;
    }

    private void addEdgeIntersectionEvent(final BlineElement arc) {
        final HalfEdge left = arc.getLeftEdge();
        final HalfEdge right = arc.getRightEdge();

        if (left == null || right == null) {
            return;
        }

        final Vec3 intersectionPoint = findEdgeIntersectionPoint(left, right);

        if (intersectionPoint == null) {
            LOGGER.log(Level.SEVERE, "No intersection");
            return;
        }

        final Vec3 eventOffset = new Vec3(left.getParentArc().getFocus().getX() - intersectionPoint.getX(), left.getParentArc().getFocus().getY() - intersectionPoint.getY());

        final double distance = Math.sqrt(eventOffset.getX() * eventOffset.getX() + eventOffset.getY() + eventOffset.getY());

        final double directrixY = intersectionPoint.getY() + distance;

        if (arc.getCurrentEvent() != null) {
            if (directrixY >= arc.getCurrentEvent().getYCoord()) {
                return;
            } else {
                arc.getCurrentEvent().setValid(false);
            }
        }

        final EdgeEvent e = new EdgeEvent(directrixY, left, right, intersectionPoint);
        arc.setCurrentEvent(e);
        eventQueue.add(e);

    }

    private BlineElement removeElement(BlineElement current, final BlineElement remove) {
        if (current == null) {
            return current;
        }

        current = removeElement(current.getLeft(), remove);
        current = removeElement(current.getRight(), remove);

        if (current == remove) {

        }

        return current;
    }

    private void removeArc(final EdgeEvent e) {
        final HalfEdge e1 = e.getEdge1();
        final HalfEdge e2 = e.getEdge2();

        final BlineElement squeezedArc = e1.getHomeArc();

        final BlineElement leftArc = getLeftNeighbour(squeezedArc);
        final BlineElement rightArc = getRightNeighbour(squeezedArc);

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

        final Vec3 leftFocus = ((Arc) leftArc).getFocus();
        final Vec3 rightFocus = ((Arc) rightArc).getFocus();

        final Vec3 direction = new Vec3(rightFocus.getX() - leftFocus.getX(), rightFocus.getY() - leftFocus.getY());
        final Vec3 dirNewEdge = new Vec3(direction.getY(), -direction.getX());
        dirNewEdge.normalizeVec2();

        final HalfEdge newEdge = new HalfEdge((Arc) rightArc, leftArc, intersectionPoint, dirNewEdge);

        leftArc.setRightEdge(newEdge);
        rightArc.setLeftEdge(newEdge);


        addEdgeIntersectionEvent(leftArc);
        addEdgeIntersectionEvent(rightArc);
    }

    private Vec3 findEdgeIntersectionPoint(final HalfEdge h1, final HalfEdge h2) {
        final double dx = h2.getStart().getX() - h1.getStart().getX();
        final double dy = h2.getStart().getY() - h1.getStart().getY();
        final double det = h2.getDirVect().getX() * h1.getDirVect().getY() - h2.getDirVect().getY() * h1.getDirVect().getX();

        final double u = (dy * h2.getDirVect().getX() - dx * h2.getDirVect().getY()) / det;
        final double v = (dy * h1.getDirVect().getX() - dx * h1.getDirVect().getY()) / det;

        if (u < 0 && !h1.extendsUp()) {
            return null;
        }

        if (v < 0 && !h2.extendsUp()) {
            return null;
        }

        if (u == 0 && v == 0 && !h1.extendsUp() && !h2.extendsUp()) {
            return null;
        }

        return new Vec3(h1.getStart().getX() + h1.getDirVect().getX() * u, h1.getStart().getY() + h1.getDirVect().getY() * u);
    }

    private BlineElement findIntersectingArc(final BlineElement root, final double x, final double directrix) {
        BlineElement current = root;

        if (current.getLeft() == null && current.getRight() == null) {
            return current;
        }

        if (current instanceof BaseLine) {
            HalfEdge leftEdge = current.getLeftEdge();
            HalfEdge rightEdge = current.getRightEdge();

            Vec3 leftIntersection = getEdgeBaselineIntersection(leftEdge, (BaseLine) current, directrix);
            Vec3 rightIntersection = getEdgeBaselineIntersection(rightEdge, (BaseLine) current, directrix);

            if (leftEdge == null && leftIntersection == null) {
                final BlineElement leftClosest = getLeftNeighbour(current);

                if (leftClosest != null) {
                    final HalfEdge h = new HalfEdge(null, null, ((BaseLine) current).getStart(), ((BaseLine) current).getStart());
                    h.setDirVect(new Vec3(-1, 0));
                    leftIntersection = getEdgeArcIntersection(h, (Arc) leftClosest, directrix);
                }
            }

            if (rightEdge == null && rightIntersection == null) {
                final BlineElement rightClosest = getRightNeighbour(current);

                if (rightClosest != null) {
                    final HalfEdge h = new HalfEdge(null, null, ((BaseLine) current).getStart(), ((BaseLine) current).getStart());
                    h.setDirVect(new Vec3(1, 0));
                    rightIntersection = getEdgeArcIntersection(h, (Arc) rightClosest, directrix);
                }

            }


            final double leftX = leftIntersection == null ? Double.MIN_VALUE : leftIntersection.getX();
            final double rightX = rightIntersection == null ? Double.MAX_VALUE : rightIntersection.getX();

            if (x > rightX) {
                return findIntersectingArc(current.getRight(), x, directrix);
            } else if (x < leftX) {
                return findIntersectingArc(current.getLeft(), x, directrix);
            } else {
                return current;
            }

        } else {
            HalfEdge leftHalfEdge = current.getLeftEdge();
            HalfEdge rightHalfEdge = current.getRightEdge();

            if (leftHalfEdge == null) {
                leftHalfEdge = getLeftNeighbour(current).getRightEdge();
            }

            if (rightHalfEdge == null) {
                rightHalfEdge = getRightNeighbour(current).getLeftEdge();
            }

            Vec3 leftIntersection;
            Vec3 rightIntersection;

            if (leftHalfEdge.getHomeArc() instanceof Arc) {
                leftIntersection = getEdgeArcIntersection(leftHalfEdge, (Arc) leftHalfEdge.getHomeArc(), directrix);
            } else {
                leftIntersection = getEdgeBaselineIntersection(leftHalfEdge, (BaseLine) leftHalfEdge.getHomeArc(), directrix);
            }

            if (rightHalfEdge.getHomeArc() instanceof Arc) {
                rightIntersection = getEdgeArcIntersection(rightHalfEdge, (Arc) rightHalfEdge.getHomeArc(), directrix);
            } else {
                rightIntersection = getEdgeBaselineIntersection(rightHalfEdge, (BaseLine) rightHalfEdge.getHomeArc(), directrix);
            }

            final double leftX = leftIntersection == null ? Double.MIN_VALUE : leftIntersection.getX();
            final double rightX = rightIntersection == null ? Double.MAX_VALUE : rightIntersection.getX();

            if (x > rightX) {
                return findIntersectingArc(current.getRight(), x, directrix);
            } else if (x < leftX) {
                return findIntersectingArc(current.getLeft(), x, directrix);
            } else {
                return current;
            }
        }

    }

    private Vec3 getEdgeBaselineIntersection(HalfEdge edge, BaseLine line, double directrix) {
        if (edge == null || line == null) {
            return null;
        }

        final Arc arc = edge.getParentArc();
        final HalfEdge e = new HalfEdge(arc, arc, line.getStart(), new Vec3(-edge.getDirVect().getX(), -edge.getDirVect().getY()));

        return getEdgeArcIntersection(e, arc, directrix);
    }

    private Vec3 getEdgeArcIntersection(HalfEdge edge, Arc arc, double directrix) {
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

            if (intersectionXOffset * edge.getDirVect().getX() < 0) {
                return null;
            }
            return new Vec3(arc.getFocus().getX(), m * arc.getFocus().getX() + b);
        }

        // y = ax^2 + bx + c
        double a = 1.0f / (2.0 * (arc.getFocus().getY() - directrix));
        double b2 = -m - 2.0 * a * arc.getFocus().getX();
        double c = a * arc.getFocus().getX() * arc.getFocus().getX() + (arc.getFocus().getY() + directrix) * 0.5 - b;

        double discriminant = b2 * b2 - 4.0 * a * c;
        if (discriminant < 0) {
            return null;
        }
        double rootDisc = Math.sqrt(discriminant);
        double x1 = (-b2 + rootDisc) / (2.0f * a);
        double x2 = (-b2 - rootDisc) / (2.0f * a);

        double x1Offset = x1 - edge.getStart().getX();
        double x2Offset = x2 - edge.getStart().getX();
        double x1Dot = x1Offset * edge.getDirVect().getX();
        double x2Dot = x2Offset * edge.getDirVect().getX();

        double x;
        if ((x1Dot >= 0.0f) && (x2Dot < 0.0f)) {
            x = x1;
        } else if ((x1Dot < 0.0f) && (x2Dot >= 0.0f)) {
            x = x2;
        } else if ((x1Dot >= 0.0f) && (x2Dot >= 0.0f)) {
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

    private BlineElement getRightNeighbour(final BlineElement item) {
        BlineElement current = item;

        if (current.getLeft() == null && current.getRight() == null && current.getParent() == null) {
            return null;
        }

        if (current.getRight() != null) {
            BlineElement right = current.getRight();

            while (right.getLeft() != null) {
                right = right.getLeft();
            }

            return right;
        }

        if (current.getParent() != null && current.getParent().getLeft() == current) {
            return current.getParent();
        }

        if (current.getParent() != null && current.getParent().getRight() == current) {
            while (current.getParent().getRight() == current) {
                current = current.getParent();

                if (current.getParent() == null) {
                    return null;
                }
            }

            return current;
        }

        return null;
    }

    private BlineElement getLeftNeighbour(final BlineElement item) {
        BlineElement current = item;

        if (current.getLeft() == null && current.getRight() == null && current.getParent() == null) {
            return null;
        }

        if (current.getLeft() != null) {
            BlineElement left = current.getLeft();

            while (left.getRight() != null) {
                left = left.getRight();
            }

            return left;
        }

        if (current.getParent() != null && current.getParent().getRight() == current) {
            return current.getParent();
        }

        if (current.getParent() != null && current.getParent().getLeft() == current) {
            while (current.getParent().getLeft() == current) {
                current = current.getParent();

                if (current.getParent() == null) {
                    return null;
                }
            }

            return current;
        }

        return null;
    }

    public List<Line> getCompletedEdges() {
        return completedEdges;
    }

    public void run() {
        while (!eventQueue.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Y coord: " + eventQueue.peek().getYCoord());

            if (eventQueue.peek() instanceof SiteEvent) {
                final SiteEvent e = (SiteEvent) eventQueue.poll();

                root = addArc(e.getSite());

            } else if (eventQueue.peek() instanceof EdgeEvent) {
                final EdgeEvent e = (EdgeEvent) eventQueue.poll();

                if (e.isValid()) {
                    removeArc(e);
                }
            }
        }
    }

}
