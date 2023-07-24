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
    private static final Logger LOGGER = Logger.getLogger(BeachLine.class.getName());


    public BeachLine() {
        root = new Parabola(new Vec3(-100, -100), new Vec3(MapView.MAP_WIDTH + 100, -100), MapView.MAP_WIDTH / 2, new Vec3(MapView.MAP_WIDTH / 2, -35));
        root.setParent(null);
    }

    public void splitArc(final Parabola element) {
        insertNewArcSequence(root, element);
    }

    private void searchArcs(final Parabola element) {
        BeachLineElement current = root;

        while (!(current instanceof Parabola)) {
            BeachLineElement left = findLeftLeaf(current);
            BeachLineElement right = findRightLeaf(current);

            BeachLineElement leftParent = findLeftParent(left);
            BeachLineElement rightParent = findRightParent(right);


        }

    }

    private void insertNewArcSequence(BeachLineElement currentNode, final Parabola element) {
        if (currentNode == null) {
            return;
        }
        if (element.getSpawnX() <= currentNode.getEnd().getX() && element.getSpawnX() >= currentNode.getStart().getX() && currentNode instanceof Parabola) {

            if (currentNode.getLeft() == null && currentNode.getRight() == null && currentNode instanceof Parabola) {
                LOGGER.log(Level.SEVERE, "Left, right and parent is equal to null");
                final Parabola splitArc = (Parabola) currentNode;

                final double intersectionPoint = splitArc.getY(element.getSpawnX(), element.getSite().getY());

                element.setStart(element.getSpawnX(), intersectionPoint);
                element.setEnd(element.getSpawnX(), intersectionPoint);

                final Edge eLeft = new Edge(new Vec3(element.getSpawnX(), intersectionPoint), new Vec3(element.getSpawnX(), intersectionPoint), element.getSpawnX());

                eLeft.addPoint(element);
                eLeft.addPoint((Parabola) currentNode);

                final Edge eRight = new Edge(new Vec3(element.getSpawnX(), intersectionPoint), new Vec3(element.getSpawnX(), intersectionPoint), element.getSpawnX());

                eRight.addPoint(element);
                eRight.addPoint((Parabola) currentNode);

                final Parabola arcLeft = new Parabola(new Vec3(splitArc.getStart()), new Vec3(element.getSpawnX(), intersectionPoint), splitArc.getSpawnX(), splitArc.getSite());

                final Parabola arcRight = new Parabola(new Vec3(element.getSpawnX(), intersectionPoint), new Vec3(splitArc.getEnd()), splitArc.getSpawnX(), splitArc.getSite());

                eLeft.setRight(eRight);
                eRight.setParent(eLeft);

                eLeft.setLeft(arcLeft);
                arcLeft.setParent(eLeft);

                eRight.setRight(arcRight);
                arcRight.setParent(eRight);

                eRight.setLeft(element);
                element.setParent(eRight);
                element.addEdges(eLeft);
                element.addEdges(eRight);

                if (currentNode.getParent() == null) {
                    LOGGER.log(Level.SEVERE, "Replacing root with edge");
                    currentNode = new Edge(eLeft);

                    if (root.getLeft() instanceof Parabola) {
                        LOGGER.log(Level.SEVERE, "Left child of root is parabola");
                    }

                    if (root.getRight() instanceof Edge) {
                        LOGGER.log(Level.SEVERE, "Right child of root is edge");
                    }
                } else if (currentNode.getParent().getLeft() == currentNode) {
                    LOGGER.log(Level.SEVERE, "Adding arc as left child");
                    currentNode.getParent().setLeft(eLeft);
                } else {
                    LOGGER.log(Level.SEVERE, "Adding arc as right child");
                    currentNode.getParent().setRight(eLeft);
                }

            }

        } else if (element.getSpawnX() > currentNode.getEnd().getX()) {
            LOGGER.log(Level.SEVERE, "Itterating right through the tree");
            insertNewArcSequence(currentNode.getRight(), element);
        } else if (element.getSpawnX() < currentNode.getStart().getX()) {
            LOGGER.log(Level.SEVERE, "Itterating left through the tree");
            insertNewArcSequence(currentNode.getLeft(), element);
        }

    }

    public void updateArcIntersections(final double directrix) {
        updateArcIntersections(root, directrix);
    }

    private void updateArcIntersections(final BeachLineElement current, final double directrix) {
        if (current == null) {
            return;
        } else if (current instanceof Parabola) {
            LOGGER.log(Level.SEVERE, "Editing arc");
            final Parabola p = (Parabola) current;
            final Parabola rightNeighbour = findRightNeighbour(current);
            final Parabola leftNeighbour = findLeftNeighbour(current);

            if (leftNeighbour != null) {
                LOGGER.log(Level.SEVERE, "Editing with left neighbour");
                final double posXIntersectionLeft = p.getXPositiveIntersection(leftNeighbour, directrix);
                final double negXIntersectionLeft = p.getXNegativeIntersection(leftNeighbour, directrix);

                final double actualIntersectionX = posXIntersectionLeft < negXIntersectionLeft ? posXIntersectionLeft : negXIntersectionLeft;

                p.setStart(actualIntersectionX, leftNeighbour.getY(actualIntersectionX, directrix));
                leftNeighbour.setEnd(p.getStart().getX(), p.getStart().getY());

                if (!p.getCreatedEdges().isEmpty()) {
                    p.getCreatedEdges().get(0).setEnd(actualIntersectionX, leftNeighbour.getY(actualIntersectionX, directrix));
                }
            } else {
                LOGGER.log(Level.SEVERE, "Left neighbour is null");
            }

            if (rightNeighbour != null) {
                LOGGER.log(Level.SEVERE, "Editing with right neighbour");
                final double posXIntersectionRight = p.getXPositiveIntersection(rightNeighbour, directrix);
                final double negXIntersectionRight = p.getXNegativeIntersection(rightNeighbour, directrix);

                final double actualIntersectionX = posXIntersectionRight > negXIntersectionRight ? posXIntersectionRight : negXIntersectionRight;

                p.setEnd(actualIntersectionX, rightNeighbour.getY(actualIntersectionX, directrix));
                rightNeighbour.setStart(p.getEnd().getX(), p.getEnd().getY());

                if (!p.getCreatedEdges().isEmpty()) {
                    p.getCreatedEdges().get(1).setEnd(actualIntersectionX, rightNeighbour.getY(actualIntersectionX, directrix));
                }

            } else {
                LOGGER.log(Level.SEVERE, "Right neighbour is null");
            }

            return;
        }

        updateArcIntersections(current.getLeft(), directrix);
        updateArcIntersections(current.getRight(), directrix);
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

    private Parabola findRightNeighbour(BeachLineElement middle) {

        if (middle.getParent() == null && middle.getRight() == null) {
            return null;
        }

        // Find parent with right child
        while (true) {
            if (middle == null) {
                break;
            }
            while (middle.getParent() != null && middle.getParent().getRight() == middle && middle.getRight() == null) {
                middle = middle.getParent();
            }

            if (middle.getRight() == null && middle.getParent() == null) {
                break;
            } else if (middle.getRight() == null) {
                middle = middle.getParent();
            } else if (middle.getRight() != null) {
                while (middle.getRight() != null && middle.getLeft() != null) {
                    if (middle.getLeft() != null) {
                        middle = middle.getLeft();
                    }

                    if (middle.getRight() != null) {
                        middle = middle.getRight();
                    }
                }

                return (Parabola) middle;
            }

        }

        return null;
    }

    private Parabola findLeftNeighbour(BeachLineElement middle) {
        if (middle.getParent() == null && middle.getLeft() == null) {
            return null;
        }

        while (true) {
            if (middle == null) {
                break;
            }
            while (middle.getParent() != null && middle.getParent().getLeft() == middle && middle.getLeft() == null) {
                middle = middle.getParent();
            }

            if (middle.getLeft() == null && middle.getParent() == null) {
                break;
            } else if (middle.getLeft() == null) {
                middle = middle.getParent();
            } else if (middle.getLeft() != null) {
                while (middle.getRight() != null && middle.getLeft() != null) {
                    if (middle.getLeft() != null) {
                        middle = middle.getLeft();
                    }

                    if (middle.getRight() != null) {
                        middle = middle.getRight();
                    }
                }

                return (Parabola) middle;
            }

        }

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
