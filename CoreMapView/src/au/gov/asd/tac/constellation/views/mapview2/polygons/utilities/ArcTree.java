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
        /*root = new Arc(new Vec3(MapView.MAP_WIDTH / 2, -500));
        final HalfEdge left = new HalfEdge((Arc) root, null, new Vec3(MapView.MAP_WIDTH / 2, -1000), new Vec3(-1, 0));
        final HalfEdge right = new HalfEdge((Arc) root, null, new Vec3(MapView.MAP_WIDTH / 2, -1000), new Vec3(1, 0));
        root.setLeftEdge(left);
        root.setRightEdge(right);*/
    }

    private BlineElement addArc(final Vec3 focus) {
        BlineElement current = root;
        final BlineElement intersectingElement = findIntersectingArc(current, focus.getX(), focus.getY());
        final Arc newArc = new Arc(focus);
        if (intersectingElement instanceof BaseLine) {
            final BaseLine baseLine = (BaseLine) intersectingElement;
            final BaseLine splitLeft = new BaseLine(baseLine.getStart(), new Vec3(focus.getX(), baseLine.getStart().getY()));
            final BaseLine splitRight = new BaseLine(new Vec3(focus.getX(), baseLine.getStart().getY()), baseLine.getEnd());

            newArc.setLeft(splitLeft);
            newArc.setRight(splitRight);
            splitLeft.setParent(newArc);
            splitRight.setParent(newArc);

            final Vec3 edgeStart = new Vec3(focus.getX(), baseLine.getStart().getY());
            final Vec3 dirVect = new Vec3(1, 0);
            LOGGER.log(Level.SEVERE, "Edge start: " + edgeStart.getX() + ", " + edgeStart.getY());
            final Line l = new Line();
            l.setStartX(edgeStart.getX() - 5);
            l.setStartY(edgeStart.getY());
            l.setEndX(edgeStart.getX() + 5);
            l.setEndY(edgeStart.getY());

            l.setStroke(Color.GREEN);
            l.setStrokeWidth(5);
            completedEdges.add(l);

            final HalfEdge e1 = new HalfEdge(newArc, intersectingElement, edgeStart, dirVect);
            final HalfEdge e2 = new HalfEdge(newArc, intersectingElement, edgeStart, new Vec3(-dirVect.getX(), -dirVect.getY()));

            e1.setParentArc(newArc);
            //e1.setHomeArc(splitRight);

            e2.setParentArc(newArc);
            //e2.setHomeArc(splitLeft);

            splitLeft.setRightEdge(e2);
            splitLeft.setLeftEdge(baseLine.getLeftEdge());

            splitRight.setLeftEdge(e1);
            splitRight.setRightEdge(baseLine.getRightEdge());


            splitRight.setRight(baseLine.getRight());
            splitLeft.setLeft(baseLine.getLeft());

            // set the parents of intersecting left and right
            if (baseLine.getRight() != null) {
                baseLine.getRight().setParent(splitRight);
            }

            if (baseLine.getLeft() != null) {
                baseLine.getLeft().setParent(splitLeft);
            }

            if (baseLine.getCurrentEvent() != null) {
                baseLine.getCurrentEvent().setValid(false);
            }


            if (intersectingElement == root) {
                LOGGER.log(Level.SEVERE, "Intersecting arc is root now");
                current = newArc;
            }


            newArc.setParentFromItem(baseLine);

            addEdgeIntersectionEvent(splitLeft, focus.getY());
            addEdgeIntersectionEvent(splitRight, focus.getY());

        } else {
            LOGGER.log(Level.SEVERE, "Arc intersected");
            final Arc intersectingArc = (Arc) intersectingElement;

            final Arc splitLeft = new Arc(intersectingArc.getFocus());
            final Arc splitRight = new Arc(intersectingArc.getFocus());

            final Vec3 edgeStart = new Vec3(focus.getX(), intersectingArc.getY(focus.getX(), focus.getY()));
            final Vec3 direction = new Vec3(newArc.getFocus().getX() - intersectingArc.getFocus().getX(), newArc.getFocus().getY() - intersectingArc.getFocus().getY());
            //final Vec3 direction = new Vec3(newArc.getFocus().getX() - intersectingArc.getFocus().getX(), newArc.getFocus().getY() - intersectingArc.getFocus().getY());
            LOGGER.log(Level.SEVERE, "Edge start: " + edgeStart.getX() + ", " + edgeStart.getY());
            final Line l = new Line();
            l.setStartX(edgeStart.getX() - 5);
            l.setStartY(edgeStart.getY());
            l.setEndX(edgeStart.getX() + 5);
            l.setEndY(edgeStart.getY());

            l.setStroke(Color.GREEN);
            l.setStrokeWidth(5);
            completedEdges.add(l);

            final Vec3 dirVect = new Vec3(direction.getY(), -direction.getX());
            dirVect.normalizeVec2();

            final HalfEdge e1 = new HalfEdge(newArc, intersectingArc, edgeStart, dirVect);
            final HalfEdge e2 = new HalfEdge(newArc, intersectingArc, edgeStart, new Vec3(-dirVect.getX(), -dirVect.getY()));

            newArc.setLeft(splitLeft);
            newArc.setRight(splitRight);
            splitLeft.setParent(newArc);
            splitRight.setParent(newArc);

            e1.setParentArc(newArc);
            //e1.setHomeArc(splitRight);

            e2.setParentArc(newArc);
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
                intersectingArc.getCurrentEvent().setValid(false);
            }

            newArc.setParentFromItem(intersectingElement);

            if (intersectingElement == root) {
                LOGGER.log(Level.SEVERE, "Intersecting arc is root now");
                current = newArc;
            }
            addEdgeIntersectionEvent(splitLeft, focus.getY());
            addEdgeIntersectionEvent(splitRight, focus.getY());

        }

        return current;
    }

    private void addEdgeIntersectionEvent(final BlineElement arc, final double directrix) {
        if (arc == null) {
            return;
        }
        LOGGER.log(Level.SEVERE, "Checking for edge intersection event");
        final HalfEdge left = arc.getLeftEdge();
        final HalfEdge right = arc.getRightEdge();

        if (left == null || right == null) {
            LOGGER.log(Level.SEVERE, "left and right edge are both null so no intersection");
            return;
        }

        LOGGER.log(Level.SEVERE, "Left edge dirvect: " + left.getDirVect().getX() + ", " + left.getDirVect().getY() + " right edge dirvect: " + right.getDirVect().getX() + ", " + right.getDirVect().getY());

        final Vec3 intersectionPoint;

        if (arc instanceof BaseLine) {
            final Arc leftArc = left.getParentArc();
            final Arc rightArc = right.getParentArc();

            final Vec3 leftIntersection = getEdgeArcIntersection(left, leftArc, directrix);
            final Vec3 rightIntersection = getEdgeArcIntersection(right, rightArc, directrix);

            intersectionPoint = new Vec3(leftIntersection.getX() + ((rightIntersection.getX() - leftIntersection.getX()) / 2), leftIntersection.getY());

        } else {
            intersectionPoint = findEdgeIntersectionPoint(left, right);
        }


        if (intersectionPoint == null) {
            LOGGER.log(Level.SEVERE, "No intersection");
            return;
        }
        LOGGER.log(Level.SEVERE, "Possible intersection");
        LOGGER.log(Level.SEVERE, "Intersection point is: " + intersectionPoint.getX() + ", " + intersectionPoint.getY());


        final Line l = new Line();
        l.setStartX(intersectionPoint.getX() - 5);
        l.setStartY(intersectionPoint.getY());
        l.setEndX(intersectionPoint.getX() + 5);
        l.setEndY(intersectionPoint.getY());
        l.setStroke(Color.PURPLE);
        l.setStrokeWidth(5);
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

        final EdgeEvent e = new EdgeEvent(directrixY, left, right, arc, intersectionPoint);
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

                remove.setParent(null);

                return current;
            } else {
                return null;
            }
        } else if (remove.getLeft() != null && remove.getRight() == null) {
            final BlineElement left = remove.getLeft();
            left.setParent(null);

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
            LOGGER.log(Level.SEVERE, "Removing node with 2 children");
            BlineElement iSucc = remove.getRight();
            while (iSucc.getLeft() != null) {
                iSucc = iSucc.getLeft();
            }
            final BlineElement leftChild = remove.getLeft();
            final BlineElement rightChild = remove.getRight();

            BlineElement temp = removeElement(current, iSucc);


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
        LOGGER.log(Level.SEVERE, "Called edge event handler");
        final BlineElement removed = e.getSqueezed();
        final HalfEdge e1 = e.getEdge1();
        final HalfEdge e2 = e.getEdge2();

        if (removed instanceof Arc) {
            LOGGER.log(Level.SEVERE, "squeezed element is an arc");
        } else if (removed instanceof BaseLine) {
            LOGGER.log(Level.SEVERE, "squeezed element is a baseline");
        } else {
            LOGGER.log(Level.SEVERE, "squeezed element is null");
        }

        final BlineElement left = getLeftNeighbour(removed);

        final BlineElement right = getRightNeighbour(removed);

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

        final Vec3 direction;
        final Vec3 dirNewEdge;
        final HalfEdge newEdge;

        if (left instanceof Arc && right instanceof Arc) {
            final Arc leftArc = (Arc) left;
            final Arc rightArc = (Arc) right;

            final Vec3 leftFocus = leftArc.getFocus();
            final Vec3 rightFocus = rightArc.getFocus();

            direction = new Vec3(leftFocus.getX() - rightFocus.getX(), leftFocus.getY() - rightFocus.getY());
            dirNewEdge = new Vec3(direction.getY(), -direction.getX());
            dirNewEdge.normalizeVec2();

            newEdge = new HalfEdge((Arc) rightArc, leftArc, intersectionPoint, dirNewEdge);

            LOGGER.log(Level.SEVERE, "Merged 2 Arcs at edge start: " + newEdge.getStart().getX() + ", " + newEdge.getStart().getY() + " edge direction: " + newEdge.getDirVect().getX() + ", " + newEdge.getDirVect().getY());

            left.setRightEdge(newEdge);
            right.setLeftEdge(newEdge);
        } else if (left instanceof BaseLine && right instanceof Arc) {
            final BaseLine leftLine = (BaseLine) left;
            final Arc rightArc = (Arc) right;
            newEdge = new HalfEdge(rightArc, leftLine, intersectionPoint, new Vec3(-1, 0));
            leftLine.setRightEdge(newEdge);
        } else if (right instanceof BaseLine && left instanceof Arc) {
            final BaseLine rightLine = (BaseLine) right;
            final Arc leftArc = (Arc) left;
            newEdge = new HalfEdge(leftArc, rightLine, intersectionPoint, new Vec3(1, 0));
            right.setLeftEdge(newEdge);
        }

        if (removed.getCurrentEvent() != null) {
            removed.getCurrentEvent().setValid(false);
        }

        root = removeElement(root, removed);

        addEdgeIntersectionEvent(left, e.getYCoord());
        addEdgeIntersectionEvent(right, e.getYCoord());
    }

    private Vec3 findEdgeIntersectionPoint(final HalfEdge h1, final HalfEdge h2) {

        if (h1.getDirVect().getX() == 1.0 && h1.getDirVect().getY() == 0.0 && h2.getDirVect().getX() == -1.0 && h2.getDirVect().getY() == 0.0) {
            return new Vec3(h1.getStart().getX() + ((h2.getStart().getX() - h1.getStart().getX())) / 2, h1.getStart().getY());
        }

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
            final BaseLine baseLine = (BaseLine) current;
            HalfEdge leftEdge = current.getLeftEdge();
            HalfEdge rightEdge = current.getRightEdge();

            final Arc leftN = (Arc) getLeftNeighbour(baseLine);
            final Arc rightN = (Arc) getRightNeighbour(baseLine);

            final HalfEdge tempLeft = new HalfEdge(null, null, baseLine.getEnd(), new Vec3(-1, 0));
            final HalfEdge tempRight = new HalfEdge(null, null, baseLine.getStart(), new Vec3(1, 0));


            Vec3 leftIntersection = getEdgeArcIntersection(tempLeft, leftN, directrix);
            Vec3 rightIntersection = getEdgeArcIntersection(tempRight, rightN, directrix);

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
            final Arc arc = (Arc) current;
            HalfEdge leftHalfEdge = arc.getLeftEdge();
            HalfEdge rightHalfEdge = arc.getRightEdge();

            if (leftHalfEdge == null) {
                leftHalfEdge = getLeftNeighbour(current).getRightEdge();
            }

            if (rightHalfEdge == null) {
                rightHalfEdge = getRightNeighbour(current).getLeftEdge();
            }

            Vec3 leftIntersection = getEdgeArcIntersection(leftHalfEdge, arc, directrix);
            Vec3 rightIntersection = getEdgeArcIntersection(rightHalfEdge, arc, directrix);

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

    public Vec3 getEdgeArcIntersection(HalfEdge edge, Arc arc, double directrix) {
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
            if (eventQueue.peek() instanceof SiteEvent) {
                LOGGER.log(Level.SEVERE, "PROCESSING SITE EVENT AT Y: " + eventQueue.peek().getYCoord());
                final SiteEvent e = (SiteEvent) eventQueue.poll();

                root = addArc(e.getSite());

                //printInOrder(root);
                printBFS(root);

            } else if (eventQueue.peek() instanceof EdgeEvent) {

                final EdgeEvent e = (EdgeEvent) eventQueue.poll();

                if (e.isValid()) {
                    LOGGER.log(Level.SEVERE, "PROCESSING EDGE EVENT AT Y: " + e.getYCoord());
                    removeArc(e);
                    printBFS(root);
                }
            }
        }
    }

}
