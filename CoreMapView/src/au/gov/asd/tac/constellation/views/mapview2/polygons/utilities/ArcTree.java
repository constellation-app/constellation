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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
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
        final BlineElement intersectingElement = findIntersectingArc(current, focus.getX(), focus.getY());
        if (intersectingElement instanceof BaseLine) {
            final Arc newArc = new Arc(focus);

            final BaseLine splitLeft = new BaseLine(((BaseLine) intersectingElement).getStart(), new Vec3(focus.getX(), ((BaseLine) intersectingElement).getStart().getY()));
            final BaseLine splitRight = new BaseLine(new Vec3(focus.getX(), ((BaseLine) intersectingElement).getStart().getY()), ((BaseLine) intersectingElement).getEnd());

            final Vec3 edgeStart = new Vec3(focus.getX(), ((BaseLine) intersectingElement).getStart().getY());
            final Vec3 dirVect = new Vec3(1, 0);

            final HalfEdge e1 = new HalfEdge(newArc, intersectingElement, edgeStart, dirVect);
            final HalfEdge e2 = new HalfEdge(newArc, intersectingElement, edgeStart, new Vec3(-dirVect.getX(), -dirVect.getY()));

            e1.setParentArc(newArc);
            e1.setHomeArc(splitRight);

            e2.setParentArc(newArc);
            e2.setHomeArc(splitLeft);

            splitLeft.setRightEdge(e2);
            splitLeft.setLeftEdge(intersectingElement.getLeftEdge());

            splitRight.setLeftEdge(e1);
            splitRight.setRightEdge(intersectingElement.getRightEdge());

            newArc.setLeft(splitLeft);
            newArc.setRight(splitRight);
            splitLeft.setParent(newArc);
            splitRight.setParent(newArc);

            splitRight.setRight(intersectingElement.getRight());
            splitLeft.setLeft(intersectingElement.getLeft());

            // set the parents of intersecting left and right
            if (intersectingElement.getRight() != null) {
                intersectingElement.getRight().setParent(splitRight);
            }

            if (intersectingElement.getLeft() != null) {
                intersectingElement.getLeft().setParent(splitLeft);
            }

            addEdgeIntersectionEvent(splitLeft);
            addEdgeIntersectionEvent(splitRight);

            final Line l = new Line();
            l.setStartX(((BaseLine) intersectingElement).getStart().getX());
            l.setStartY(((BaseLine) intersectingElement).getStart().getY());
            l.setEndX(((BaseLine) intersectingElement).getEnd().getX());
            l.setEndY(((BaseLine) intersectingElement).getEnd().getY());

            l.setStroke(Color.RED);
            completedEdges.add(l);

            if (intersectingElement == root) {
                LOGGER.log(Level.SEVERE, "Intersecting arc is root now");
                return newArc;
            }

            newArc.setParentFromItem(intersectingElement);
        } else {
            LOGGER.log(Level.SEVERE, "Arc intersected");
            final Arc intersectingArc = (Arc) intersectingElement;
            final Arc newArc = new Arc(focus);

            final Arc splitLeft = new Arc(intersectingArc.getFocus());
            final Arc splitRight = new Arc(intersectingArc.getFocus());

            final Vec3 edgeStart = new Vec3(focus.getX(), intersectingArc.getY(focus.getX(), focus.getY()));
            final Vec3 direction = new Vec3(intersectingArc.getFocus().getX() - newArc.getFocus().getX(), intersectingArc.getFocus().getY() - newArc.getFocus().getY());
            //final Vec3 direction = new Vec3(newArc.getFocus().getX() - intersectingArc.getFocus().getX(), newArc.getFocus().getY() - intersectingArc.getFocus().getY());
            final Vec3 dirVect = new Vec3(direction.getY(), -direction.getX());
            dirVect.normalizeVec2();

            final HalfEdge e1 = new HalfEdge(newArc, intersectingArc, edgeStart, dirVect);
            final HalfEdge e2 = new HalfEdge(newArc, intersectingArc, edgeStart, new Vec3(-dirVect.getX(), -dirVect.getY()));

            e1.setParentArc(newArc);
            e1.setHomeArc(splitRight);

            e2.setParentArc(newArc);
            e2.setHomeArc(splitLeft);

            splitLeft.setRightEdge(e2);

            splitLeft.setLeftEdge(intersectingArc.getLeftEdge());

            if (intersectingArc.getLeftEdge() == null) {
                final BlineElement leftNeighbour = getLeftNeighbour(intersectingArc);
                if (leftNeighbour != null) {
                    splitLeft.setLeftEdge(leftNeighbour.getRightEdge());
                }
            }

            splitRight.setLeftEdge(e1);
            splitRight.setRightEdge(intersectingArc.getRightEdge());

            if (intersectingArc.getRightEdge() == null) {
                final BlineElement rightNeighbour = getRightNeighbour(intersectingArc);
                if (rightNeighbour != null) {
                    splitLeft.setLeftEdge(rightNeighbour.getLeftEdge());
                }
            }

            newArc.setLeft(splitLeft);
            newArc.setRight(splitRight);
            splitLeft.setParent(newArc);
            splitRight.setParent(newArc);

            splitRight.setRight(intersectingArc.getRight());
            splitLeft.setLeft(intersectingArc.getLeft());

            // set the parents of intersecting left and right
            if (intersectingArc.getRight() != null) {
                intersectingArc.getRight().setParent(splitRight);
            }

            if (intersectingArc.getLeft() != null) {
                intersectingArc.getLeft().setParent(splitLeft);
            }

            addEdgeIntersectionEvent(splitLeft);
            addEdgeIntersectionEvent(splitRight);

            final Line l = new Line();
            l.setStartX(intersectingArc.getFocus().getX() - 5);
            l.setStartY(intersectingArc.getFocus().getY());
            l.setEndX(intersectingArc.getFocus().getX() + 5);
            l.setEndY(intersectingArc.getFocus().getY());

            l.setStroke(Color.RED);
            completedEdges.add(l);

            if (intersectingElement == root) {
                LOGGER.log(Level.SEVERE, "Intersecting arc is root now");
                return newArc;
            }

            newArc.setParentFromItem(intersectingElement);
        }

        return current;
    }

    private void addEdgeIntersectionEvent(final BlineElement arc) {
        LOGGER.log(Level.SEVERE, "Checking for edge intersection event");
        final HalfEdge left = arc.getLeftEdge();
        final HalfEdge right = arc.getRightEdge();

        if (left == null || right == null) {
            LOGGER.log(Level.SEVERE, "left and right edge are both null so no intersection");
            return;
        }

        final Vec3 intersectionPoint = findEdgeIntersectionPoint(left, right);

        if (intersectionPoint == null) {
            LOGGER.log(Level.SEVERE, "No intersection");
            return;
        }
        LOGGER.log(Level.SEVERE, "Possible intersection");

        final Line l = new Line();
        l.setStartX(intersectionPoint.getX() - 5);
        l.setStartY(intersectionPoint.getY());
        l.setEndX(intersectionPoint.getX() + 5);
        l.setEndY(intersectionPoint.getY());
        l.setStroke(Color.RED);
        completedEdges.add(l);

        final Vec3 eventOffset = new Vec3(left.getParentArc().getFocus().getX() - intersectionPoint.getX(), left.getParentArc().getFocus().getY() - intersectionPoint.getY());

        final double distance = Vec3.getDistance(left.getParentArc().getFocus(), intersectionPoint);

        LOGGER.log(Level.SEVERE, "Distance to directrix = " + distance);

        final double directrixY = intersectionPoint.getY() + distance;

        if (arc.getCurrentEvent() != null) {
            if (directrixY >= arc.getCurrentEvent().getYCoord()) {
                LOGGER.log(Level.SEVERE, "Invalid intersection");
                return;
            } else {
                arc.getCurrentEvent().setValid(false);
            }
        }

        final EdgeEvent e = new EdgeEvent(directrixY, left, right, intersectionPoint);
        arc.setCurrentEvent(e);
        eventQueue.add(e);
        LOGGER.log(Level.SEVERE, "Added edge intersection event");
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
            } else {
                return null;
            }
        } else if (remove.getLeft() != null && remove.getRight() == null) {
            if (remove.getParent() == null) {
                return remove.getLeft();
            } else if (remove.getParent().getLeft() == remove) {
                remove.getParent().setLeft(remove.getLeft());
            } else {
                remove.getParent().setRight(remove.getLeft());
            }
            remove.getLeft().setParent(remove.getParent());
        } else if (remove.getRight() != null && remove.getLeft() == null) {
            if (remove.getParent() == null) {
                return remove.getRight();
            } else if (remove.getParent().getLeft() == remove) {

                remove.getParent().setLeft(remove.getRight());
            } else {
                remove.getParent().setRight(remove.getRight());
            }
            remove.getRight().setParent(remove.getParent());
        } else {
            LOGGER.log(Level.SEVERE, "Removing node with 2 children");
            BlineElement iSucc = remove.getRight();
            while (iSucc.getLeft() != null) {
                iSucc = iSucc.getLeft();
            }

            current = removeElement(current, iSucc);

            final BlineElement leftChild = remove.getLeft();
            final BlineElement rightChild = remove.getRight();
            leftChild.setParent(iSucc);
            iSucc.setLeft(leftChild);

            if (iSucc != rightChild) {
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
        LOGGER.log(Level.SEVERE, "Called edge event handler");
        final HalfEdge e1 = e.getEdge1();
        final HalfEdge e2 = e.getEdge2();

        final BlineElement squeezedArc = e1.getHomeArc();

        LOGGER.log(Level.SEVERE, "Test variable of squeezed arc: " + squeezedArc.test);

        if (squeezedArc instanceof Arc) {
            LOGGER.log(Level.SEVERE, "squeezed element is an arc");
        } else if (squeezedArc instanceof BaseLine) {
            LOGGER.log(Level.SEVERE, "squeezed element is a baseline");
        } else {
            LOGGER.log(Level.SEVERE, "squeezed element is null");
        }

        final BlineElement leftArc = getLeftNeighbour(squeezedArc);

        final BlineElement rightArc = getRightNeighbour(squeezedArc);

        final Vec3 intersectionPoint = e.getIntersectionPoint();

        LOGGER.log(Level.SEVERE, "Intersection point : " + intersectionPoint.getX() + ", " + intersectionPoint.getY());

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

        root = removeElement(root, squeezedArc);

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
        LOGGER.log(Level.SEVERE, "Finding intersecting arc");
        BlineElement current = root;

        if (current.getLeft() == null && current.getRight() == null) {
            if (current instanceof BaseLine) {
                LOGGER.log(Level.SEVERE, "returning baseline because left and right is null");
            } else {
                LOGGER.log(Level.SEVERE, "returning arc because left and right is null");
            }
            return current;
        }

        if (current instanceof BaseLine) {
            LOGGER.log(Level.SEVERE, "Checking if new site falls within a base line");
            HalfEdge leftEdge = current.getLeftEdge();
            HalfEdge rightEdge = current.getRightEdge();

            Vec3 leftIntersection = getEdgeBaselineIntersection(leftEdge, (BaseLine) current, directrix);
            Vec3 rightIntersection = getEdgeBaselineIntersection(rightEdge, (BaseLine) current, directrix);

            /*if (leftEdge == null && leftIntersection == null) {
                LOGGER.log(Level.SEVERE, "left edge and left intersection is null");
                final BlineElement leftClosest = getLeftNeighbour(current);

                if (leftClosest != null) {
                    final HalfEdge h = new HalfEdge(null, null, ((BaseLine) current).getStart(), ((BaseLine) current).getStart());
                    h.setDirVect(new Vec3(-1, 0));
                    leftIntersection = getEdgeArcIntersection(h, (Arc) leftClosest, directrix);
                }
            }

            if (rightEdge == null && rightIntersection == null) {
                LOGGER.log(Level.SEVERE, "right edge and right intersection is null");
                final BlineElement rightClosest = getRightNeighbour(current);

                if (rightClosest != null) {
                    final HalfEdge h = new HalfEdge(null, null, ((BaseLine) current).getStart(), ((BaseLine) current).getStart());
                    h.setDirVect(new Vec3(1, 0));
                    rightIntersection = getEdgeArcIntersection(h, (Arc) rightClosest, directrix);
                }

            }*/


            final double leftX = leftIntersection == null ? 0 : leftIntersection.getX();
            final double rightX = rightIntersection == null ? MapView.MAP_WIDTH : rightIntersection.getX();

            LOGGER.log(Level.SEVERE, "Left X: " + leftX);
            LOGGER.log(Level.SEVERE, "Right X: " + rightX);

            LOGGER.log(Level.SEVERE, "X: " + x);

            if (x > rightX) {
                return findIntersectingArc(current.getRight(), x, directrix);
            } else if (x < leftX) {
                return findIntersectingArc(current.getLeft(), x, directrix);
            } else {
                if (current instanceof BaseLine) {
                    LOGGER.log(Level.SEVERE, "returning baseline because intersection point falls within bounds");
                } else {
                    LOGGER.log(Level.SEVERE, "returning arc because intersection point falls within bounds");
                }
                return current;
            }

        } else {
            LOGGER.log(Level.SEVERE, "Checking if new site falls within an arc");
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
                LOGGER.log(Level.SEVERE, "Left edge is on an arc");
                leftIntersection = getEdgeArcIntersection(leftHalfEdge, (Arc) leftHalfEdge.getHomeArc(), directrix);
            } else {
                leftIntersection = getEdgeBaselineIntersection(leftHalfEdge, (BaseLine) leftHalfEdge.getHomeArc(), directrix);
            }

            if (rightHalfEdge.getHomeArc() instanceof Arc) {
                LOGGER.log(Level.SEVERE, "right edge is on an arc");
                rightIntersection = getEdgeArcIntersection(rightHalfEdge, (Arc) rightHalfEdge.getHomeArc(), directrix);
            } else {
                rightIntersection = getEdgeBaselineIntersection(rightHalfEdge, (BaseLine) rightHalfEdge.getHomeArc(), directrix);
            }

            final double leftX = leftIntersection == null ? 0 : leftIntersection.getX();
            final double rightX = rightIntersection == null ? MapView.MAP_WIDTH : rightIntersection.getX();

            LOGGER.log(Level.SEVERE, "Left X: " + leftX);
            LOGGER.log(Level.SEVERE, "Right X: " + rightX);

            LOGGER.log(Level.SEVERE, "X: " + x);

            if (x > rightX) {
                LOGGER.log(Level.SEVERE, "Checking if anything on the right is intersecting");
                return findIntersectingArc(current.getRight(), x, directrix);
            } else if (x < leftX) {
                LOGGER.log(Level.SEVERE, "Checking if anything on the left is intersecting");
                return findIntersectingArc(current.getLeft(), x, directrix);
            } else {
                if (current instanceof BaseLine) {
                    LOGGER.log(Level.SEVERE, "returning baseline because intersection point falls within bounds");
                } else {
                    LOGGER.log(Level.SEVERE, "returning arc because intersection point falls within bounds");
                }
                return current;
            }
        }

    }

    private Vec3 getEdgeBaselineIntersection(HalfEdge edge, BaseLine line, double directrix) {
        if (edge == null || line == null) {
            return null;
        }

        final Arc arc = edge.getParentArc();
        // Change the dirVect x and y to negatives
        final HalfEdge e = new HalfEdge(arc, arc, line.getStart(), new Vec3(edge.getDirVect().getX(), edge.getDirVect().getY()));

        return getEdgeArcIntersection(edge, arc, directrix);
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
        if ((x1Dot >= 0) && (x2Dot < 0)) {
            x = x1;
        } else if ((x1Dot < 0) && (x2Dot >= 0)) {
            x = x2;
        } else if ((x1Dot >= 0) && (x2Dot >= 0)) {
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

            return current.getParent();
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

            return current.getParent();
        }

        return null;
    }

    public List<Line> getCompletedEdges() {
        return completedEdges;
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
                nodeTypeString += " ARC ";
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
        while (!eventQueue.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Y coord: " + eventQueue.peek().getYCoord());

            if (eventQueue.peek() instanceof SiteEvent) {
                final SiteEvent e = (SiteEvent) eventQueue.poll();

                root = addArc(e.getSite());

                //printInOrder(root);
                printBFS(root);

            } else if (eventQueue.peek() instanceof EdgeEvent) {
                final EdgeEvent e = (EdgeEvent) eventQueue.poll();

                if (e.isValid()) {
                    removeArc(e);
                }
            }
        }
    }

}
