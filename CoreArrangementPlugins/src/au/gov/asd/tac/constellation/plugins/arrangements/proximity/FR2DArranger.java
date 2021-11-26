/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.proximity;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.awt.geom.Point2D;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * main module to arrange a graph using the FR2D algorithm
 *
 * @author algol
 */
class FR2DArranger implements Arranger {

    public static final int MAX_ITERATIONS = 10;
    private static final int BORDER = 1;

    private double forceConstant;
    private double temperature;
    private static final double ATTRACTION_MULTIPLIER = 0.75 / 0.67;
    private static final double REPULSION_MULTIPLIER = 0.75 * 0.67;
    private double attractionConstant;
    private double repulsionConstant;
    private static final double EPSILON = 0.000001;

    private GraphWriteMethods graph;
    private int vxCount;
    private ArrayList<Point2D.Float> points;
    private ArrayList<Point2D.Float> offsets;
    private boolean maintainMean;

    private final PluginInteraction interaction;

    private final SecureRandom r = new SecureRandom();

    /**
     *
     * @param graph The graph to be laid out.
     * @param tc The TopComponent that owns this graph, so it can be notified
     * when the layout is finished.
     */
    public FR2DArranger(final PluginInteraction interaction) {
        this.interaction = interaction;
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        this.graph = wg;

        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        vxCount = wg.getVertexCount();
        if (vxCount > 0) {
            // Guess an initial size of the layout based on the number of nodes in the graph.
            final int size = 3 * (int) Math.floor(Math.sqrt(vxCount));
            initialise(size, size);
            layout();
            writeBackXYZ();

            if (maintainMean) {
                ArrangementUtilities.moveMean(wg, oldMean);
            }
        }
    }

    private void initialise(final int width, final int height) {
        temperature = width / 10.0;
        forceConstant = Math.pow(height * width / (double) vxCount, 1.0 / 2.0);
        attractionConstant = ATTRACTION_MULTIPLIER * forceConstant;
        repulsionConstant = REPULSION_MULTIPLIER * forceConstant;

        // Create an array of points to match the array of nodes.
        // This means we have to allow for gaps in the array where nodes have been removed.
        // Pre-fill the ArrayLists so we can treat them like arrays.
        final int capacity = graph.getVertexCapacity();
        points = new ArrayList<>(capacity);
        offsets = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            points.add(null);
            offsets.add(null);
        }

        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            // Start each point at a random position.
            final Point2D.Float p = new Point2D.Float(
                    BORDER + (float) r.nextInt(width - BORDER * 2),
                    BORDER + (float) r.nextInt(height - BORDER * 2));
            points.set(vxId, p);
            offsets.set(vxId, new Point2D.Float(0, 0));
        }
    }

    public void layout() throws InterruptedException {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            interaction.setProgress(i + 1, MAX_ITERATIONS, "Arranging...", true);

            for (int position = 0; position < vxCount; position++) {
                final int vxId = graph.getVertex(position);

                repulse(vxId);
            }

            for (int position = 0; position < graph.getLinkCount(); position++) {
                final int linkId = graph.getLink(position);
                final int vxlId = graph.getLinkLowVertex(linkId);
                final int vxhId = graph.getLinkHighVertex(linkId);
                attract(vxlId, vxhId);
            }

            for (int position = 0; position < vxCount; position++) {
                final int vxId = graph.getVertex(position);

                position(vxId);
            }

            cool(i);
        }
    }

    public void writeBackXYZ() {
        final int xAttr = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(graph);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(graph);
        if (VisualConcept.VertexAttribute.X2.get(graph) == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2", "x2", null, null);
        }
        if (VisualConcept.VertexAttribute.Y2.get(graph) == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2", "y2", null, null);
        }
        if (VisualConcept.VertexAttribute.Z2.get(graph) == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2", "z2", null, null);
        }
        final int x2Attr = VisualConcept.VertexAttribute.X2.get(graph);
        final int y2Attr = VisualConcept.VertexAttribute.Y2.get(graph);
        final int z2Attr = VisualConcept.VertexAttribute.Z2.get(graph);

        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);
            final Point2D.Float point = points.get(vxId);

            graph.setFloatValue(x2Attr, vxId, graph.getFloatValue(xAttr, vxId));
            graph.setFloatValue(y2Attr, vxId, graph.getFloatValue(yAttr, vxId));
            graph.setFloatValue(z2Attr, vxId, graph.getFloatValue(zAttr, vxId));

            graph.setFloatValue(xAttr, vxId, point.x);
            graph.setFloatValue(yAttr, vxId, point.y);
            graph.setFloatValue(zAttr, vxId, 0);
        }
    }

    /**
     * Repulse a node from the other nodes.
     *
     * @param vxOrigin The vertex to repulse from.
     */
    private void repulse(final int vxOrigin) {
        final Point2D.Float p1 = points.get(vxOrigin);
        final Point2D.Float offset = new Point2D.Float(0, 0);

        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            if (vxId != vxOrigin) {
                final Point2D p2 = points.get(vxId);
                final double xDelta = p1.getX() - p2.getX();
                final double yDelta = p1.getY() - p2.getY();
                final double lenDelta = Math.max(EPSILON, Math.sqrt(xDelta * xDelta + yDelta * yDelta));
                final double force = (repulsionConstant * repulsionConstant) / lenDelta;
                if (Double.isNaN(force)) {
                    throw new IllegalArgumentException("Bad value: isNaN(force)");
                }

                offset.setLocation(
                        offset.getX() + (xDelta / lenDelta) * force,
                        offset.getY() + (yDelta / lenDelta) * force);
            }
        }

        offsets.set(vxOrigin, offset);
    }

    /**
     * Attract nodes along their edges.
     *
     * @param txId
     */
    private void attract(final int vx0Id, final int vx1Id) {
        final Point2D point1 = points.get(vx0Id);
        final Point2D point2 = points.get(vx1Id);
        final double xDelta = point1.getX() - point2.getX();
        final double yDelta = point1.getY() - point2.getY();
        final double lenDelta = Math.max(EPSILON, Math.sqrt(xDelta * xDelta + yDelta * yDelta));
        final double force = (lenDelta * lenDelta) / attractionConstant;
        if (Double.isNaN(force)) {
            throw new IllegalArgumentException(String.format("Bad value: force %f %f isNan(force)", lenDelta, attractionConstant));
        }

        final double dx = (xDelta / lenDelta) * force;
        final double dy = (yDelta / lenDelta) * force;
        final Point2D.Float offset1 = offsets.get(vx0Id);
        final Point2D.Float offset2 = offsets.get(vx1Id);
        offset1.setLocation(offset1.getX() - dx, offset1.getY() - dy);
        offset2.setLocation(offset2.getX() + dx, offset2.getY() + dy);
    }

    private void position(final int vxId) {
        final Point2D.Float p = offsets.get(vxId);
        final Point2D.Float xyDelta = points.get(vxId);
        final double lenDelta = Math.max(EPSILON, Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY()));
        final double xDelta = p.getX() / lenDelta * Math.min(lenDelta, temperature);
        final double yDelta = p.getY() / lenDelta * Math.min(lenDelta, temperature);
        final double newX = xyDelta.getX() + xDelta;
        final double newY = xyDelta.getY() + yDelta;
        xyDelta.setLocation(newX, newY);
    }

    private void cool(final int i) {
        temperature += (1.0 - i / (float) MAX_ITERATIONS);
    }

    @Override
    public void setMaintainMean(boolean b) {
        maintainMean = b;
    }
}
