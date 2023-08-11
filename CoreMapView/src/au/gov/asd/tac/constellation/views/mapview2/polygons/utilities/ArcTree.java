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

    public BlineElement root;
    private final List<Line> completedEdges = new ArrayList<>();
    private final List<HalfEdge> edges = new ArrayList<>();
    private final PriorityQueue<VoronoiEvent> eventQueue;

    public double directrixPos = 0;
    private int id = 0;

    private static final Logger LOGGER = Logger.getLogger(ArcTree.class.getName());

    private boolean finishedEdges = false;

    private final Vec3 edgeCaseFocus = new Vec3(MapView.MAP_WIDTH / 2, -1000);


    public ArcTree(final PriorityQueue<VoronoiEvent> eventQueue) {
        this.eventQueue = eventQueue;
        //root = new BaseLine(new Vec3(Double.MIN_VALUE, 0), new Vec3(Double.MAX_VALUE, 0));
        root = new Arc(new Vec3(MapView.MAP_WIDTH / 2, -500), id++);
        final HalfEdge left = new HalfEdge((Arc) root, null, new Vec3(MapView.MAP_WIDTH / 2, -1000), new Vec3(-1, 0));
        final HalfEdge right = new HalfEdge((Arc) root, null, new Vec3(MapView.MAP_WIDTH / 2, -1000), new Vec3(1, 0));
        root.setLeftEdge(left);
        root.setRightEdge(right);
    }

    private BlineElement addArc(final Vec3 focus) {
        BlineElement current = root;
        final BlineElement intersectingElement = findIntersectingArc(current, focus.getX(), focus.getY());
        final Arc newArc = new Arc(focus, id++);
        LOGGER.log(Level.SEVERE, "Created new arc: " + id);
        //LOGGER.log(Level.SEVERE, "Arc intersected");
        final Arc intersectingArc = (Arc) intersectingElement;

        final Arc splitLeft = new Arc(intersectingArc.getFocus(), id++);
        LOGGER.log(Level.SEVERE, "Created left arc with ID: " + id);

        final Arc splitRight = new Arc(intersectingArc.getFocus(), id++);
        LOGGER.log(Level.SEVERE, "Created right arc with ID: " + id);

        final Vec3 edgeStart = new Vec3(focus.getX(), intersectingArc.getY(focus.getX(), focus.getY()));
        final Vec3 direction = new Vec3(newArc.getFocus().getX() - intersectingArc.getFocus().getX(), newArc.getFocus().getY() - intersectingArc.getFocus().getY());
        LOGGER.log(Level.SEVERE, "Edge start: " + edgeStart.getX() + ", " + edgeStart.getY());
        final Line l = new Line();
        l.setStartX(edgeStart.getX() - 5);
        l.setStartY(edgeStart.getY());
        l.setEndX(edgeStart.getX() + 5);
        l.setEndY(edgeStart.getY());

        l.setStroke(Color.GREEN);
        l.setStrokeWidth(5);
        //completedEdges.add(l);

        final Vec3 dirVect = new Vec3(direction.getY(), -direction.getX());
        dirVect.normalizeVec2();

        final HalfEdge e1 = new HalfEdge(newArc, intersectingArc, edgeStart, dirVect);
        final HalfEdge e2 = new HalfEdge(newArc, intersectingArc, edgeStart, new Vec3(-dirVect.getX(), -dirVect.getY()));

        edges.add(e1);
        edges.add(e2);

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
            LOGGER.log(Level.SEVERE, "Invalidating intersection at: " + intersectingArc.getCurrentEvent().getYCoord());
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
        LOGGER.log(Level.SEVERE, "Checking for edge intersection event");
        final HalfEdge left = arc.getLeftEdge() != null ? arc.getLeftEdge() : getLeftNeighbour(arc).getRightEdge();
        final HalfEdge right = arc.getRightEdge() != null ? arc.getRightEdge() : getRightNeighbour(arc).getLeftEdge();

        if (left == null || right == null) {
            if (left == null) {
                LOGGER.log(Level.SEVERE, "Left edge is null");
            }

            if (right == null) {
                LOGGER.log(Level.SEVERE, "Right edge is null");
            }
            return;
        }

        LOGGER.log(Level.SEVERE, "Left edge dirvect: " + left.getDirVect().getX() + ", " + left.getDirVect().getY() + " right edge dirvect: " + right.getDirVect().getX() + ", " + right.getDirVect().getY());

        final Vec3 intersectionPoint;

        if (arc instanceof BaseLine) {
            final Arc leftArc = left.getParentArc();
            final Arc rightArc = right.getParentArc();

            final Vec3 leftIntersection = getEdgeArcIntersection(left, leftArc, directrix);
            final Vec3 rightIntersection = getEdgeArcIntersection(right, rightArc, directrix);

            intersectionPoint = new Vec3((rightIntersection.getX() - leftIntersection.getX()) / 2, leftIntersection.getY());

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
        //completedEdges.add(l);

        final Vec3 eventOffset = new Vec3(left.getParentArc().getFocus().getX() - intersectionPoint.getX(), left.getParentArc().getFocus().getY() - intersectionPoint.getY());

        final double distance = Vec3.getDistance(left.getParentArc().getFocus(), intersectionPoint);


        final double directrixY = intersectionPoint.getY() + distance;

        if (arc.getCurrentEvent() != null) {
            if (directrixY >= arc.getCurrentEvent().getYCoord()) {
                LOGGER.log(Level.SEVERE, "Invalid intersection");
                return;
            }
            LOGGER.log(Level.SEVERE, "Invalidating intersection at: " + arc.getCurrentEvent().getYCoord());
            arc.getCurrentEvent().setValid(false);
        }

        LOGGER.log(Level.SEVERE, "Setting edge intersection event at = " + directrixY);

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
            LOGGER.log(Level.SEVERE, "Removing node with 2 children");
            BlineElement iSucc = remove.getRight();
            while (iSucc.getLeft() != null) {
                iSucc = iSucc.getLeft();
            }

            final BlineElement leftChild = remove.getLeft();


            /*if (iSucc.getParent() != remove && iSucc.getParent() != null) {
                if (iSucc.getRight() != null) {
                    iSucc.getParent().setLeft(iSucc.getRight());
                    iSucc.getRight().setParent(iSucc.getParent());
                    iSucc.setRight(null);
                }
            }*/
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
        LOGGER.log(Level.SEVERE, "Removing element");
        final BlineElement removed = e.getSqueezed();
        final HalfEdge e1 = e.getEdge1();
        final HalfEdge e2 = e.getEdge2();

        e1.setComplete(true);
        e2.setComplete(true);
        LOGGER.log(Level.SEVERE, "squeezed element is an arc with id: " + ((Arc) removed).getId());

        LOGGER.log(Level.SEVERE, "Getting left neighbour of removed arc");
        final BlineElement left = getLeftNeighbour(removed);
        if (left == null) {
            LOGGER.log(Level.SEVERE, "Left is null");

        }

        LOGGER.log(Level.SEVERE, "Getting right neighbour of removed arc");
        final BlineElement right = getRightNeighbour(removed);
        if (right == null) {
            LOGGER.log(Level.SEVERE, "Right is null");
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

        newEdge = new HalfEdge((Arc) rightArc, leftArc, intersectionPoint, dirNewEdge);

        LOGGER.log(Level.SEVERE, "Merged 2 Arcs at edge start: " + newEdge.getStart().getX() + ", " + newEdge.getStart().getY() + " edge direction: " + newEdge.getDirVect().getX() + ", " + newEdge.getDirVect().getY());

        if (left != null) {
            LOGGER.log(Level.SEVERE, "id of left Arc: " + leftArc.getId());
            left.setRightEdge(newEdge);
        }

        if (right != null) {
            LOGGER.log(Level.SEVERE, "id of right Arc: " + rightArc.getId());
            right.setLeftEdge(newEdge);
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

        //completedEdges.add(l);
        //completedEdges.add(l2);
        //completedEdges.add(l3);

        printBFS(root);
        //printInOrder(root);


        if (newEdge != null) {
            edges.add(newEdge);
        }

        if (removed.getCurrentEvent() != null) {
            removed.getCurrentEvent().setValid(false);
        }

        root = removeElement(root, removed);

        LOGGER.log(Level.SEVERE, "After removal");
        printBFS(root);
        exists(root, ((Arc) removed).getId());
        //printInOrder(root);

        addEdgeIntersectionEvent(left, e.getYCoord());
        addEdgeIntersectionEvent(right, e.getYCoord());

    }

    private Vec3 findEdgeIntersectionPoint(final HalfEdge h1, final HalfEdge h2) {

        /*if (h1.getDirVect().getX() == 1.0 && h1.getDirVect().getY() == 0.0 && h2.getDirVect().getX() == -1.0 && h2.getDirVect().getY() == 0.0) {
            return new Vec3(h1.getStart().getX() + ((h2.getStart().getX() - h1.getStart().getX())) / 2, h1.getStart().getY());
        }*/

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
        LOGGER.log(Level.SEVERE, "Finding intersecting arc");
        BlineElement current = currentNode;

        if (current.getLeft() == null && current.getRight() == null) {
            if (current instanceof BaseLine) {
                LOGGER.log(Level.SEVERE, "returning baseline because left and right is null");
            } else {
                LOGGER.log(Level.SEVERE, "returning arc because left and right is null");
            }
            return current;
        }

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

        final double max = leftX < rightX ? rightX : leftX;
        final double min = leftX < rightX ? leftX : rightX;

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
                LOGGER.log(Level.SEVERE, "Found item and passed in item the same in getRightN");
                return null;
            }

            return right;
        }

        if (current.getParent() != null && current.getParent().getLeft() == current) {

            if (current.getParent() == item) {
                LOGGER.log(Level.SEVERE, "parent is item in getRightN");
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
                LOGGER.log(Level.SEVERE, "Parent in 2 children loop is equal to item in getLeftN");
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
                LOGGER.log(Level.SEVERE, "Found item and passed in item the same in getLeftN");
                return null;
            }

            return left;
        }

        if (current.getParent() != null && current.getParent().getRight() == current) {

            if (current.getParent() == item) {
                LOGGER.log(Level.SEVERE, "parent is item in getLeftN");
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
                LOGGER.log(Level.SEVERE, "Parent in 2 children loop is equal to item in getLeftN");
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
            }
        });
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
        if (!eventQueue.isEmpty()) {
            if (eventQueue.peek() instanceof SiteEvent) {
                LOGGER.log(Level.SEVERE, "PROCESSING SITE EVENT AT Y: " + eventQueue.peek().getYCoord());
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
                printBFS(root);

            } else if (eventQueue.peek() instanceof EdgeEvent) {

                final EdgeEvent e = (EdgeEvent) eventQueue.poll();

                if (e.isValid()) {
                    LOGGER.log(Level.SEVERE, "PROCESSING EDGE EVENT AT Y: " + e.getYCoord());
                    removeArc(e);

                    //printBFS(root);
                }
                directrixPos = e.getYCoord();
            }
        } else if (!finishedEdges) {
            finishEdges();
            finishedEdges = true;
        }
    }

}
