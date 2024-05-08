/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.Point3D;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Implements a 3D version of the Fruchterman-Reingold force-directed algorithm
 * for node layout.
 *
 * <p>
 * Behavior is determined by the following settable parameters:
 * <ul>
 * <li>attraction multiplier: how much edges try to keep their vertices
 * together</li>
 * <li>repulsion multiplier: how much vertices try to push each other apart</li>
 * <li>maximum iterations: how many iterations this algorithm will use before
 * stopping</li>
 * </ul>
 * Each of the first two defaults to 0.75; the maximum number of iterations
 * defaults to 700.
 * <p>
 *
 * "Fruchterman and Reingold, 'Graph Drawing by Force-directed Placement'"
 * "http://i11www.ilkd.uni-karlsruhe.de/teaching/SS_04/visualisierung/papers/fruchterman91graph.pdf"
 *
 * <p>
 * The class is implemented as a SwingWorker so it can execute off the EDT.
 * <p>
 * The intermediate result type (Integer) is used to publish progress workunits,
 * so the ProgressHandle can be updated on the EDT.
 * <p>
 * The return result type is irrelevant; it isn't used.
 *
 * @author algol
 */
public class FR3DArranger implements Arranger {

    private static final String ARRANGING_INTERACTION = "Arranging...";

    private static final int MAX_PSEUDO_SIZE = 100;
    public static final int MAX_ITERATIONS = 10;
    private static final int BORDER = 1;
    private double temperature;
    private static final double ATTRACTION_MULTIPLIER = 0.75 / 0.67;
    private static final double REPULSION_MULTIPLIER = 0.75 * 0.67;
    private double attractionConstant;
    private double repulsionConstant;
    private static final double EPSILON = 0.000001;
    private ArrayList<Point3D.Float> points;
    private ArrayList<Point3D.Float> offsets;
    private volatile boolean stopWork;

    private final PluginInteraction interaction;

    private GraphWriteMethods wg;
    boolean maintainMean = false;

    private final SecureRandom r = new SecureRandom();

    /**
     * Creates a new arranger using the specified {@link PluginInteraction}.
     *
     * @param interaction The {@link PluginInteraction} that this arranger will
     * use.
     */
    public FR3DArranger(final PluginInteraction interaction) {
        stopWork = false;
        this.interaction = interaction;
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        this.wg = wg;

        interaction.setProgress(0, MAX_ITERATIONS, ARRANGING_INTERACTION, true);

        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        final int vxCount = wg.getVertexCount();

        // Guess an initial size of the layout based on the number of nodes in the graph.
        final int size = 3 * (int) Math.floor(Math.sqrt(Math.min(vxCount, MAX_PSEUDO_SIZE)));
        initialise(size, size, size);
        layout();
        if (!stopWork) {
            writeBackXYZ();
        }

        if (maintainMean) {
            ArrangementUtilities.moveMean(wg, oldMean);
        }
    }

    /**
     * Do the layout in the background.
     *
     * @return null; the value is not used.
     *
     * @throws InterruptedException if the operation is canceled during
     * execution.
     */
    //@Override
    protected Object run() throws InterruptedException {
        interaction.setProgress(0, MAX_ITERATIONS, ARRANGING_INTERACTION, true);

        // Guess an initial size of the layout based on the number of nodes in the graph.
        final int size = 3 * (int) Math.floor(Math.sqrt(Math.min(wg.getVertexCount(), MAX_PSEUDO_SIZE)));
        initialise(size, size, size);
        layout();
        if (!stopWork) {
            writeBackXYZ();
        }

        return null;
    }

    private void initialise(final int width, final int height, final int depth) {
        final double forceConstant = Math.pow(height * width * depth / (double) wg.getVertexCount(), 1.0 / 3.0);
        temperature = width / 10.0;
        attractionConstant = ATTRACTION_MULTIPLIER * forceConstant;
        repulsionConstant = REPULSION_MULTIPLIER * forceConstant;

        // Create an array of points to match the array of nodes.
        // This means we have to allow for gaps in the array where nodes have been removed.
        // Pre-fill the ArrayLists so we can treat them like arrays.
        final int capacity = wg.getVertexCapacity();
        points = new ArrayList<>(capacity);
        offsets = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            points.add(null);
            offsets.add(null);
        }

        wg.vertexStream().parallel().forEach(node -> {
            // Start each point at a random position.
            final Point3D.Float p = new Point3D.Float(
                    BORDER + r.nextInt(width - BORDER * 2),
                    BORDER + r.nextInt(height - BORDER * 2),
                    BORDER + r.nextInt(depth - BORDER * 2));
            points.set(node, p);
            offsets.set(node, new Point3D.Float(0.0F, 0.0F, 0.0F));
        });
    }

    public void layout() throws InterruptedException {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            interaction.setProgress(i + 1, MAX_ITERATIONS, ARRANGING_INTERACTION, true);

            wg.vertexStream().parallel().forEach(vertexId -> repulse(vertexId)
            );

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            wg.linkStream().parallel().forEach(txId -> attract(txId)
            );

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            wg.vertexStream().parallel().forEach(vertexId -> position(vertexId)
            );

            cool(i);
        }
    }

    public void writeBackXYZ() {
        final int xAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        if (wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X2.getName()) == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2", "x2", 0, null);
        }
        if (wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y2.getName()) == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2", "y2", 0, null);
        }
        if (wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z2.getName()) == Graph.NOT_FOUND) {
            wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2", "z2", 0, null);
        }
        final int x2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X2.getName());
        final int y2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y2.getName());
        final int z2Attr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z2.getName());

        for (int position = 0; position < wg.getVertexCount(); position++) {
            final int nodeId = wg.getVertex(position);
            final Point3D.Float point = points.get(nodeId);

            wg.setFloatValue(x2Attr, nodeId, wg.getFloatValue(xAttr, nodeId));
            wg.setFloatValue(y2Attr, nodeId, wg.getFloatValue(yAttr, nodeId));
            wg.setFloatValue(z2Attr, nodeId, wg.getFloatValue(zAttr, nodeId));

            wg.setFloatValue(xAttr, nodeId, point.getFloatX());
            wg.setFloatValue(yAttr, nodeId, point.getFloatY());
            wg.setFloatValue(zAttr, nodeId, point.getFloatZ());
        }
    }

    /**
     * Repulse a node from the other nodes.
     *
     * @param nodeOrigin The node that other nodes will be repulsed from.
     */
    private void repulse(final int nodeOrigin) {
        final Point3D.Float p1 = points.get(nodeOrigin);
        final Point3D.Float offset = new Point3D.Float(0, 0, 0);

        for (int position = 0; position < wg.getVertexCount(); position++) {
            final int node = wg.getVertex(position);
            if (node != nodeOrigin) {
                final Point3D p2 = points.get(node);
                final double xDelta = p1.getX() - p2.getX();
                final double yDelta = p1.getY() - p2.getY();
                final double zDelta = p1.getZ() - p2.getZ();
                final double lenDelta = Math.max(EPSILON, Math.sqrt(xDelta * xDelta + yDelta * yDelta + zDelta * zDelta));
                final double force = (repulsionConstant * repulsionConstant) / lenDelta;
                if (Double.isNaN(force)) {
                    throw new IllegalArgumentException("Bad value: isNaN(force)");
                }

                offset.setLocation(
                        offset.getX() + (xDelta / lenDelta) * force,
                        offset.getY() + (yDelta / lenDelta) * force,
                        offset.getZ() + (zDelta / lenDelta) * force);
            }
        }

        offsets.set(nodeOrigin, offset);
    }

    /**
     * Attract nodes along their edges.
     *
     * @param edge
     */
    private void attract(final int edge) {
        final int[] endNodes = new int[]{
            wg.getLinkLowVertex(edge), wg.getLinkHighVertex(edge)
        };
        final int node1 = endNodes[0];
        final int node2 = endNodes[1];
        final Point3D point1 = points.get(node1);
        final Point3D point2 = points.get(node2);
        final double xDelta = point1.getX() - point2.getX();
        final double yDelta = point1.getY() - point2.getY();
        final double zDelta = point1.getZ() - point2.getZ();
        final double lenDelta = Math.max(EPSILON, Math.sqrt(xDelta * xDelta + yDelta * yDelta + zDelta * zDelta));
        final double force = (lenDelta * lenDelta) / attractionConstant;
        if (Double.isNaN(force)) {
            throw new IllegalArgumentException(String.format("Bad value: force %f %f isNan(force)", lenDelta, attractionConstant));
        }

        final double dx = (xDelta / lenDelta) * force;
        final double dy = (yDelta / lenDelta) * force;
        final double dz = (zDelta / lenDelta) * force;
        final Point3D.Float offset1 = offsets.get(node1);
        final Point3D.Float offset2 = offsets.get(node2);
        offset1.setLocation(offset1.getX() - dx, offset1.getY() - dy, offset1.getZ() - dz);
        offset2.setLocation(offset2.getX() + dx, offset2.getY() + dy, offset2.getZ() + dz);
    }

    private void position(final int node) {
        final Point3D.Float p = offsets.get(node);
        final Point3D.Float xyzDelta = points.get(node);
        final double lenDelta = Math.max(EPSILON, Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY() + p.getZ() * p.getZ()));
        final double xDelta = p.getX() / lenDelta * Math.min(lenDelta, temperature);
        final double yDelta = p.getY() / lenDelta * Math.min(lenDelta, temperature);
        final double zDelta = p.getZ() / lenDelta * Math.min(lenDelta, temperature);
        final double newX = xyzDelta.getX() + xDelta;
        final double newY = xyzDelta.getY() + yDelta;
        final double newZ = xyzDelta.getZ() + zDelta;
        xyzDelta.setLocation(newX, newY, newZ);
    }

    private void cool(final int i) {
        temperature += (1.0 - i / (float) MAX_ITERATIONS);
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }
}
