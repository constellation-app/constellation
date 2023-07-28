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

/**
 *
 * @author altair1673
 */
public class ArcTree {

    private BlineElement root;

    public ArcTree() {
        root = new BaseLine(new Vec3(0, 0), new Vec3(MapView.MAP_WIDTH, 0));
    }

    public BlineElement addArc(final Vec3 focus) {
        final BlineElement current = root;
        final BlineElement intersectingArc = findIntersectingArc(current, focus.getX(), focus.getY());
        if (intersectingArc instanceof BaseLine) {
            final Arc newArc = new Arc(focus);

            final BaseLine splitLeft = new BaseLine(((BaseLine) intersectingArc).getStart(), new Vec3(focus.getX(), ((BaseLine) intersectingArc).getStart().getY()));
            final BaseLine splitRight = new BaseLine(new Vec3(focus.getX(), ((BaseLine) intersectingArc).getStart().getY()), ((BaseLine) intersectingArc).getEnd());

            newArc.setLeft(splitLeft);
            newArc.setRight(splitRight);

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

            if (intersectingArc == root) {
                return newArc;
            }

            splitRight.setRight(intersectingArc.getRight());
            splitLeft.setLeft(intersectingArc.getLeft());

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


            newArc.setLeft(splitLeft);
            newArc.setRight(splitRight);

            splitRight.setRight(intersectingArc.getRight());
            splitLeft.setLeft(intersectingArc.getLeft());

            newArc.setParentFromItem(intersectingArc);
        }

        return current;
    }


    private BlineElement findIntersectingArc(final BlineElement root, final double x, final double directrix) {
        BlineElement current = root;

        if (current.getLeft() == null && current.getRight() == null) {
            return current;
        }

        if (current instanceof BaseLine) {
            HalfEdge leftEdge = current.getLeftEdge();

            if (leftEdge == null) {
                final BlineElement leftClosest = getLeftNeighbour(current);

                if (leftClosest != null) {
                    //((BaseLine) current).setStart(start);
                }
            }

            HalfEdge rightEdge = current.getRightEdge();
            if (rightEdge == null) {
                final BlineElement rightClosest = getRightNeighbour(current);

                if (rightClosest != null) {

                }

            }

            Vec3 leftIntersection = getEdgeBaselineIntersection(leftEdge, (BaseLine) current, directrix);
            Vec3 rightIntersection = getEdgeBaselineIntersection(rightEdge, (BaseLine) current, directrix);

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

}
