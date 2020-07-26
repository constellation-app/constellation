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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.utilities.ArrangementUtilities;
import au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d3.BoundingBox3D;
import au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d3.Octree;
import au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d3.Orb3D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.Exceptions;
import org.python.modules.math;

public class UncollideArrangement implements Arranger {

    private final int dimensions;
    private final boolean setXyz2;
    private float minPadding;
    private PluginInteraction interaction;
    private boolean maintainMean = false;
    private final double twinScaling;
    

    public UncollideArrangement(final int dimensions, final boolean setXyz2, final int maxExpansions) {
        this.dimensions = dimensions;
        this.setXyz2 = setXyz2;
        this.twinScaling = math.pow(1.1, -maxExpansions);
        minPadding = (float) 0;
    }

    public void setInteraction(final PluginInteraction interaction) {
        this.interaction = interaction;
    }

    public void setMinPadding(final float minPadding) {
        this.minPadding = minPadding;
    }

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        final float[] oldMean = maintainMean ? ArrangementUtilities.getXyzMean(wg) : null;

        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());
        final int rId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
        final int x2Id = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2", "x2", 0, null);
        final int y2Id = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2", "y2", 0, null);
        final int z2Id = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2", "z2", 0, null);

        final int vxCount = wg.getVertexCount();

        if (vxCount > 0) {
            if (dimensions == 2) {
                try {
                    uncollide2d(wg, 2000);
                } catch (PluginException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                final Orb3D[] orbs = new Orb3D[vxCount];
                for (int position = 0; position < vxCount; position++) {
                    final int vxId = wg.getVertex(position);

                    orbs[position] = new Orb3D(wg.getFloatValue(xId, vxId), wg.getFloatValue(yId, vxId), wg.getFloatValue(zId, vxId), rId != Graph.NOT_FOUND ? wg.getFloatValue(rId, vxId) : 1);
                }

                uncollide3d(orbs, 2000);

                // Move x,y,z to x2,y2,z2.
                // Set x,y,z to uncollided x,y,z.
                for (int position = 0; position < vxCount; position++) {
                    final int vxId = wg.getVertex(position);

                    final Orb3D orb = orbs[position];

                    if (setXyz2) {
                        wg.setFloatValue(x2Id, vxId, wg.getFloatValue(xId, vxId));
                        wg.setFloatValue(y2Id, vxId, wg.getFloatValue(yId, vxId));
                        wg.setFloatValue(z2Id, vxId, wg.getFloatValue(zId, vxId));
                    }

                    wg.setFloatValue(xId, vxId, orb.getX());
                    wg.setFloatValue(yId, vxId, orb.getY());
                    wg.setFloatValue(zId, vxId, orb.getZ());
                }
            }

            if (maintainMean) {
                ArrangementUtilities.moveMean(wg, oldMean);
            }
        }
    }

    private void uncollide2d(final GraphWriteMethods wg, final int iter) throws InterruptedException, PluginException {
        final int vertexCount = wg.getVertexCount();

        QuadTree qt = new QuadTree(wg);
        boolean foundTwin = true;
        int countIterations = 0;
        int numberNoTwins = 0;
        while (numberNoTwins < vertexCount) {
            numberNoTwins = nudgeAllTwins(wg, qt);
            if (interaction != null) {
                final String msg = String.format("Nodes with \"Twins\" %d of %d; iteration %d", numberNoTwins, vertexCount, ++countIterations);
                interaction.setProgress(numberNoTwins, vertexCount, msg, true);
            }
            qt = new QuadTree(wg);
        }

        interaction.setBusy("Expanding graph until there are no more colllisions", true);
        
        for (int i = 0; i < iter && qt.hasCollision(minPadding); i++) {
//            if (interaction != null) {
//                interaction.
//                final String msg = String.format("2D step %3d of maximum %3d; pad %f", i, iter,minPadding);
//                interaction.setProgress(verticiesBeforeCollision, wg.getVertexCount(), msg, true);
//            }
            
            PluginExecution.withPlugin(ArrangementPluginRegistry.EXPAND_GRAPH).executeNow(wg);

            qt = new QuadTree(wg);

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }      
    }

    private void uncollide3d(final Orb3D[] orbs, final int iter) throws InterruptedException {
        int maxCollided = -1;
        boolean isEnd = false;
        for (int i = 0; i < iter && !isEnd; i++) {
            final BoundingBox3D.Box3D bb = BoundingBox3D.getBox(orbs);
            final Octree qt = new Octree(bb);
            for (final Orb3D orb : orbs) {
                qt.insert(orb);
            }

            // Vary the padding to see if we can make things use fewer steps.
            final float padding = 1;
//                    final float padding = 9-i%10;
//                    final float padding = prevCollisions<0 ? 1 : 1+2*prevCollisions/orbs.length;
//            final float padding = prevCollisions<0 ? 1 : 1+prevCollisions/orbs.length;
//                    final float padding = 0;

            int totalCollided = 0;
            for (final Orb3D orb : orbs) {
                final int collided = qt.uncollide(orb, padding);
                totalCollided += collided;
            }

            if (interaction != null) {
                maxCollided = Math.max(maxCollided, totalCollided);
                final String msg = String.format("3D step %3d; pad %f; collisions %6d of %6d", i, padding, maxCollided - totalCollided, maxCollided);
                interaction.setProgress(maxCollided - totalCollided, maxCollided, msg, true);
            }

            isEnd = totalCollided == 0;

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }
    
    private int nudgeAllTwins(GraphWriteMethods wg, QuadTree qt) {
        Set<Integer> twins;
        int numberNoTwins = 0;
        for (int subject = 0; subject < wg.getVertexCount(); subject++) {
            twins = qt.getTwins(subject, minPadding, twinScaling);
            if (twins.isEmpty()) {
                numberNoTwins++;
            } else {
                for (int twin : twins) {
                    nudgeTwins(wg, subject, twin, minPadding, twinScaling); 
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
    private void nudgeTwins(final GraphWriteMethods wg, final int subject, final int twin, final float padding, final double twinThreshold) {
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int rId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());
        
        float deltaX = wg.getFloatValue(xId, subject) - wg.getFloatValue(xId, twin);
        float deltaY = wg.getFloatValue(yId, subject) - wg.getFloatValue(yId, twin);
        final double delta = math.sqrt(deltaX * deltaX + deltaY * deltaY);
        final double r = math.sqrt(2*wg.getFloatValue(rId, subject)) + math.sqrt(2*wg.getFloatValue(rId, twin)) + padding;
        final double criticalValue = r*twinThreshold; // The required distance for the nodes to be uncollided after maxExpansions number of expansions (each expansions scaled the graph by a factor of 1.1

        // If they are in the same spot we will nudge both horizontally and vertically
        if (deltaX == 0 && deltaY == 0){
            deltaX = deltaY = 1;
        }

        double nudge; // nudge needed to move them to just beyond the minimum distance so that are at least a padding apart.
        if (deltaX == 0 || deltaY == 0){
            nudge = ((criticalValue-delta)+0.002); // Nudge needed if only moving along one axis
        } else {
            nudge = ((criticalValue-delta)+0.002)/ math.sqrt(2); // Nudge needed if moving along both axis
        }
        // Nudge horizontally based on relative position.
        if (deltaX > 0){ 
//                        wg.setFloatValue(XID, subject, wg.getFloatValue(XID, subject) + (float) nudge);
            wg.setFloatValue(xId, twin, wg.getFloatValue(xId, twin) - (float) nudge);
        } else if (deltaX < 0) {
//                        wg.setFloatValue(XID, subject, wg.getFloatValue(XID, subject) - (float) nudge);
            wg.setFloatValue(xId, twin, wg.getFloatValue(xId, twin) + (float) nudge);  
        }
        // Nudge vertically based on relative position.
        if (deltaY > 0){
//                        wg.setFloatValue(YID, subject, wg.getFloatValue(YID, subject) + (float) nudge);
            wg.setFloatValue(yId, twin, wg.getFloatValue(yId, twin) - (float) nudge);
        } else if (deltaY < 0){
//                        wg.setFloatValue(YID, subject, wg.getFloatValue(YID, subject) - (float) nudge);
            wg.setFloatValue(yId, twin, wg.getFloatValue(yId, twin) + (float) nudge);  
        }
    }


    @Override
    public void setMaintainMean(boolean b) {
        maintainMean = b;
    }
}
