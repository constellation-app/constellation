/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
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
import org.openide.util.Exceptions;
import org.python.modules.math;

public class UncollideArrangement implements Arranger {

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
            } catch (PluginException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (maintainMean) {
                ArrangementUtilities.moveMean(wg, oldMean);
            }
        }
    }

    private void uncollide(final GraphWriteMethods wg,final int iter) throws InterruptedException, PluginException {
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

        if(Objects.nonNull(interaction)) {
            interaction.setBusy("Expanding graph until there are no more colllisions", true);
        }
        
        for (int i = 0; i < iter && tree.hasCollision(); i++) {

            PluginExecution.withPlugin(ArrangementPluginRegistry.EXPAND_GRAPH).executeNow(wg);

            tree = TreeFactory.create(wg, dimensions);

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
        if(Objects.nonNull(interaction)) {
            interaction.setBusy("Expanding graph until there are no more colllisions", false);
        }
    }
    
    private int nudgeAllTwins(GraphWriteMethods wg, AbstractTree tree) {
        List<Integer> twins;
        int numberNoTwins = 0;
        for (int subject = 0; subject < wg.getVertexCount(); subject++) {
            twins = tree.getTwins(subject, twinScaling);
            if (twins.isEmpty()) {
                numberNoTwins++;
            } else {
                if (dimensions.equals(Dimensions.TWO)){
                    nudgeTwins2D(wg, subject, twins.get(0), twinScaling); 
                }
                else {
                    nudgeTwins3D(wg, subject, twins.get(0), twinScaling); 
                }
            }                  
        }
        return numberNoTwins;
    }

    /**
     * Nudges two nodes in approximately the same place so that they do not overlap.
     *
     * @param subject The vertex to check for twins.
     * @param padding The minimum distance between the vertex's edge and the edges
     * of each neighbor.
     */
    private void nudgeTwins2D(final GraphWriteMethods wg, final int subject, final int twin, final double twinThreshold) {
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int rId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
        
        float deltaX = wg.getFloatValue(xId, subject) - wg.getFloatValue(xId, twin);
        float deltaY = wg.getFloatValue(yId, subject) - wg.getFloatValue(yId, twin);
        final double delta = math.sqrt(deltaX * deltaX + deltaY * deltaY);
        final double r = math.sqrt(2*wg.getFloatValue(rId, subject)) + math.sqrt(2*wg.getFloatValue(rId, twin));
        final double criticalValue = r*twinThreshold; // The required distance for the nodes to be uncollided after maxExpansions number of expansions (each expansions scaled the graph by a factor of 1.1

        // If they are in the same spot we will nudge in a random direction
        if (deltaX == 0 && deltaY == 0){
            deltaX = ThreadLocalRandom.current().nextInt(-1, 2);
            deltaY = ThreadLocalRandom.current().nextInt(-1, 2);
        }

        double nudge; // nudge needed to move them to just beyond the minimum distance so that are at least a padding apart.
        if (deltaX == 0 || deltaY == 0){
            nudge = 0.5*((criticalValue-delta)+0.002); // Nudge needed if only moving along one axis
        } else {
            nudge = 0.5*((criticalValue-delta)+0.002)/ math.sqrt(2); // Nudge needed if moving along both axis
        }
        // Nudge horizontally based on relative position.
        if (deltaX > 0){ 
            wg.setFloatValue(xId, subject, wg.getFloatValue(xId, subject) + (float) nudge);
            wg.setFloatValue(xId, twin, wg.getFloatValue(xId, twin) - (float) nudge);
        } else if (deltaX < 0) {
            wg.setFloatValue(xId, subject, wg.getFloatValue(xId, subject) - (float) nudge);
            wg.setFloatValue(xId, twin, wg.getFloatValue(xId, twin) + (float) nudge);  
        }
        // Nudge vertically based on relative position.
        if (deltaY > 0){
            wg.setFloatValue(yId, subject, wg.getFloatValue(yId, subject) + (float) nudge);
            wg.setFloatValue(yId, twin, wg.getFloatValue(yId, twin) - (float) nudge);
        } else if (deltaY < 0){
            wg.setFloatValue(yId, subject, wg.getFloatValue(yId, subject) - (float) nudge);
            wg.setFloatValue(yId, twin, wg.getFloatValue(yId, twin) + (float) nudge);  
        }
    }
    /**
     * Nudges two nodes in approximately the same place so that they do not overlap.
     *
     * @param subject The vertex to check for twins.
     * @param padding The minimum distance between the vertex's edge and the edges
     * of each neighbor.
     */
    private void nudgeTwins3D(final GraphWriteMethods wg, final int subject, final int twin, final double twinThreshold) {
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int rId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
        
        float deltaX = wg.getFloatValue(xId, subject) - wg.getFloatValue(xId, twin);
        float deltaY = wg.getFloatValue(yId, subject) - wg.getFloatValue(yId, twin);
        float deltaZ = wg.getFloatValue(zId, subject) - wg.getFloatValue(zId, twin);
        double delta = Math.cbrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        
        final double collisionDistance = Math.cbrt(3*wg.getFloatValue(rId, subject)) + Math.cbrt(3*wg.getFloatValue(rId, twin));;
        final double twinDistance = collisionDistance*twinThreshold; // The required distance for the nodes to be uncollided after maxExpansions number of expansions (each expansions scaled the graph by a factor of 1.1

        // If they are in the same spot we will nudge in a random direction
        if (deltaX == 0 && deltaY == 0 && deltaZ == 0){
            deltaX = ThreadLocalRandom.current().nextInt(-1, 2);
            deltaY = ThreadLocalRandom.current().nextInt(-1, 2);
            deltaZ = ThreadLocalRandom.current().nextInt(-1, 2);
        }

        double[] deltas = {deltaX, deltaY, deltaZ}; 
        deltas = Arrays.stream(deltas).filter(x -> x!=0).toArray();
        double nudge; // nudge needed to move them to just beyond the minimum distance so that are at least a padding apart.
        switch (deltas.length) {
            case 2:
                nudge = 0.5*((twinDistance - delta) + 0.002); // Nudge needed if only moving along one axis
                break;
            case 1:
                nudge = 0.5*((twinDistance - delta) + 0.002) / math.sqrt(2); // Nudge needed if moving along two axis
                break;
            case 0:
                nudge = 0.5*((twinDistance - delta) + 0.002) / Math.cbrt(3); // Nudge needed if moving along 3 axis
                break;
            default:
                nudge = 0.5*((twinDistance - delta) + 0.002); // Should never reach this but need to maske the compiler happy.
        }
        // Nudge horizontally based on relative position.
        if (deltaX > 0){ 
            wg.setFloatValue(xId, subject, wg.getFloatValue(xId, subject) + (float) nudge);
            wg.setFloatValue(xId, twin, wg.getFloatValue(xId, twin) - (float) nudge);
        } else if (deltaX < 0) {
            wg.setFloatValue(xId, subject, wg.getFloatValue(xId, subject) - (float) nudge);
            wg.setFloatValue(xId, twin, wg.getFloatValue(xId, twin) + (float) nudge);  
        }
        // Nudge vertically based on relative position.
        if (deltaY > 0){
            wg.setFloatValue(yId, subject, wg.getFloatValue(yId, subject) + (float) nudge);
            wg.setFloatValue(yId, twin, wg.getFloatValue(yId, twin) - (float) nudge);
        } else if (deltaY < 0){
            wg.setFloatValue(yId, subject, wg.getFloatValue(yId, subject) - (float) nudge);
            wg.setFloatValue(yId, twin, wg.getFloatValue(yId, twin) + (float) nudge);  
        }
        // Nudge depth based on relative position
        if (deltaZ > 0){
            wg.setFloatValue(yId, subject, wg.getFloatValue(zId, subject) + (float) nudge);
            wg.setFloatValue(yId, twin, wg.getFloatValue(zId, twin) - (float) nudge);
        } else if (deltaZ < 0){
            wg.setFloatValue(yId, subject, wg.getFloatValue(zId, subject) - (float) nudge);
            wg.setFloatValue(yId, twin, wg.getFloatValue(zId, twin) + (float) nudge);  
        }
    }

    @Override
    public void setMaintainMean(boolean b) {
        maintainMean = b;
    }
}
