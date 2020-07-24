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
import org.openide.util.Exceptions;

public class UncollideArrangement implements Arranger {

    private final int dimensions;
    private final boolean setXyz2;
    private float minPadding;
    private PluginInteraction interaction;
    private boolean maintainMean = false;
    private final int maxExpansions;

    public UncollideArrangement(final int dimensions, final boolean setXyz2, final int maxExpansions) {
        this.dimensions = dimensions;
        this.setXyz2 = setXyz2;
        this.maxExpansions = maxExpansions;
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
                    uncollide2d(wg, 2000, maxExpansions);
                    
                    // Move x,y,z to x2,y2,z2.
                    // Set x,y to uncollided x,y.
                    // Deliberately leave the z value alone: someone may be doing a 2D uncollide on a 3D graph.
//                for (int position = 0; position < vxCount; position++) {
//                    final int vxId = wg.getVertex(position);
//
//                    final Orb2D orb = orbs[position];
//
//                    if (setXyz2) {
//                        wg.setFloatValue(x2Id, vxId, wg.getFloatValue(xId, vxId));
//                        wg.setFloatValue(y2Id, vxId, wg.getFloatValue(yId, vxId));
//                        wg.setFloatValue(z2Id, vxId, wg.getFloatValue(zId, vxId));
//                    }
//
//                    wg.setFloatValue(xId, vxId, orb.getX());
//                    wg.setFloatValue(yId, vxId, orb.getY());
//                }
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

    private void uncollide2d(final GraphWriteMethods wg, final int iter, final int maxExpansions) throws InterruptedException, PluginException {
        int maxCollided = -1;
        boolean isEnd = false;
        int vertexCount = wg.getVertexCount();
        
        QuadTree qt = new QuadTree(wg);
        boolean foundTwin = true;
        int countIterations = 0;
        int numberNoTwins = 0;
        while (numberNoTwins < vertexCount) {
            numberNoTwins = qt.nudgeAllTwins(minPadding, maxExpansions);
            if (interaction != null) {
                final String msg = String.format("Nodes with \"Twins\" %d of %d; iteration %d", numberNoTwins, vertexCount, ++countIterations);
                interaction.setProgress(numberNoTwins, vertexCount, msg, true);
            }
            qt = new QuadTree(wg);
        }
        
        int verticiesBeforeCollision = qt.findCollision(minPadding);
        
        for (int i = 0; i < iter && verticiesBeforeCollision>0; i++) {
            if (interaction != null) {
                final String msg = String.format("2D step %3d; pad %f; minimum uncollided verticies %6d of %6d", i, minPadding, verticiesBeforeCollision, wg.getVertexCount());
                interaction.setProgress(verticiesBeforeCollision, wg.getVertexCount(), msg, true);
            }
            
            PluginExecution.withPlugin(ArrangementPluginRegistry.EXPAND_GRAPH).executeNow(wg);

            qt = new QuadTree(wg);
            
            verticiesBeforeCollision = qt.findCollision(minPadding);

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }      
        
//        final BoundingBox2D.Box2D bb = BoundingBox2D.getBox(wg);
//        final QuadTree qt = new QuadTree(bb, wg);
//        qt.insertAll();
//        qt.nudgeAllTwins(minPadding);
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

    @Override
    public void setMaintainMean(boolean b) {
        maintainMean = b;
    }
}
