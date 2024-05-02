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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.python.modules.math;

public class UncollideArrangement implements Arranger {
    
    private static final Logger LOGGER = Logger.getLogger(UncollideArrangement.class.getName());

    private final Dimensions dimensions;
    private PluginInteraction interaction;
    private boolean maintainMean = false;
    private final double twinScaling;

    public UncollideArrangement(final Dimensions dimensions, final int maxExpansions) {
        this.twinScaling = math.pow(1.1, -maxExpansions);
        this.dimensions = dimensions;

    }

    public void setInteraction(final PluginInteraction interaction) {
        this.interaction = interaction;
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        final int vxCount = wg.getVertexCount();

        if (vxCount > 0) {
            try {
                uncollide(wg, 2000);
            } catch (final PluginException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
            if (maintainMean) {
                ArrangementUtilities.moveMean(wg, oldMean);
            }
        }
    }

    private void uncollide(final GraphWriteMethods wg, final int iter) throws InterruptedException, PluginException {
        final int vertexCount = wg.getVertexCount();

        AbstractTree tree = TreeFactory.create(wg, dimensions);
        int countIterations = 0;
        int numberNoTwins = 0;
        while (numberNoTwins < vertexCount) {
            if (Objects.nonNull(interaction)) {
                final String msg = String.format("Nodes with \"Twins\" %d of %d; iteration %d", numberNoTwins, vertexCount, ++countIterations);
                interaction.setProgress(numberNoTwins, vertexCount, msg, true);
            }
            numberNoTwins = nudgeAllTwins(wg, tree);
            tree = TreeFactory.create(wg, dimensions);
        }

        if (Objects.nonNull(interaction)) {
            interaction.setBusy("Expanding graph until there are no more colllisions", true);
        }

        for (int i = 0; i < iter && tree.hasCollision(); i++) {

            PluginExecution.withPlugin(ArrangementPluginRegistry.EXPAND_GRAPH).executeNow(wg);

            tree = TreeFactory.create(wg, dimensions);

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
        if (Objects.nonNull(interaction)) {
            interaction.setBusy("Expanding graph until there are no more colllisions", false);
        }
    }

    private int nudgeAllTwins(final GraphWriteMethods wg, final AbstractTree tree) {
        List<Integer> twins;
        int numberNoTwins = 0;
        for (int subject = 0; subject < wg.getVertexCount(); subject++) {
            twins = tree.getTwins(subject, twinScaling);
            if (twins.isEmpty()) {
                numberNoTwins++;
            } else {
                nudgeTwins(wg, subject, twins.get(0));
            }
        }
        return numberNoTwins;
    }

    /**
     * Nudges two nodes in approximately the same place so that they do not
     * overlap.
     *
     * @param subject The vertex to check for twins.
     * @param padding The minimum distance between the vertex's edge and the
     * edges of each neighbor.
     */
    private void nudgeTwins(final GraphWriteMethods wg, final int subject, final int twin) {
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int rId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());

        double[] deltas;
        float deltaX = wg.getFloatValue(xId, subject) - wg.getFloatValue(xId, twin);
        float deltaY = wg.getFloatValue(yId, subject) - wg.getFloatValue(yId, twin);
        float deltaZ = 0;

        final double collisionDistance;
        final double delta;
        switch (dimensions) {
            case TWO:
                delta = math.sqrt(deltaX * deltaX + deltaY * deltaY);
                deltas = new double[2];
                collisionDistance = math.sqrt(2 * wg.getFloatValue(rId, subject)) + math.sqrt(2 * wg.getFloatValue(rId, twin));
                break;
            case THREE:
                if (zId == GraphConstants.NOT_FOUND) {
                    throw new IllegalArgumentException("Unable to perform 3D uncllide on 2D graph");
                }
                deltaZ = wg.getFloatValue(zId, subject) - wg.getFloatValue(zId, twin);
                delta = Math.cbrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                collisionDistance = Math.cbrt(3 * wg.getFloatValue(rId, subject)) + Math.cbrt(3 * wg.getFloatValue(rId, twin));
                deltas = new double[3];
                deltas[2] = deltaZ;
                break;
            default:
                throw new IllegalArgumentException("Invalid number of dimensions");
        }
        deltas[0] = deltaX;
        deltas[1] = deltaY;

        final double twinDistance = collisionDistance * twinScaling; // The required distance for the nodes to be uncollided after maxExpansions number of expansions (each expansions scaled the graph by a factor of 1.1

        // If they are in the same spot we will nudge in a random direction
        if (delta == 0) {
            deltaX = ThreadLocalRandom.current().nextInt(-1, 2);
            deltaY = ThreadLocalRandom.current().nextInt(-1, 2);
            if (dimensions.equals(Dimensions.THREE)) {
                deltaZ = ThreadLocalRandom.current().nextInt(-1, 2);
            }
        }

        deltas = Arrays.stream(deltas).filter(x -> x != 0).toArray();
        // nudge needed to move them to just beyond the minimum distance so that are at least a padding apart.
        final double nudge = switch (deltas.length) {
            case 2 -> 0.5 * ((twinDistance - delta) + 0.002); // Nudge needed if only moving along one axis
            case 1 -> 0.5 * ((twinDistance - delta) + 0.002) / math.sqrt(2); // Nudge needed if moving along two axis
            case 0 -> 0.5 * ((twinDistance - delta) + 0.002) / Math.cbrt(3); // Nudge needed if moving along 3 axis
            default -> 0.5 * ((twinDistance - delta) + 0.002); // Should never reach this but need to maske the compiler happy.
        };

        // Nudge horizontally based on relative position.
        nudge(wg, xId, deltaX, subject, twin, nudge);
        // Nudge vertically based on relative position.
        nudge(wg, yId, deltaY, subject, twin, nudge);
        // Nudge depth based on relative position
        if (dimensions.equals(Dimensions.THREE)) {
            nudge(wg, zId, deltaZ, subject, twin, nudge);
        }
    }

    private void nudge(final GraphWriteMethods wg, final int axisId, final double delta, final int subject, final int twin, final double nudge) {
        if (delta > 0) {
            wg.setFloatValue(axisId, subject, wg.getFloatValue(axisId, subject) + (float) nudge);
            wg.setFloatValue(axisId, twin, wg.getFloatValue(axisId, twin) - (float) nudge);
        } else if (delta < 0) {
            wg.setFloatValue(axisId, subject, wg.getFloatValue(axisId, subject) - (float) nudge);
            wg.setFloatValue(axisId, twin, wg.getFloatValue(axisId, twin) + (float) nudge);
        }
    }

    @Override
    public void setMaintainMean(final boolean b) {
        maintainMean = b;
    }
}
