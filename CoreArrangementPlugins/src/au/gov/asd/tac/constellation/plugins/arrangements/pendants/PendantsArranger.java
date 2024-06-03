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
package au.gov.asd.tac.constellation.plugins.arrangements.pendants;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Arrange pendants so they stand out.
 *
 * @author algol
 * @author sirius
 */
public class PendantsArranger implements Arranger {

    @Override
    public void arrange(final GraphWriteMethods wg) throws InterruptedException {
        // Map neighbours to the set of vertices that have the same neighbours.
        final Map<Set<Integer>, Set<Integer>> neighbourMap = new HashMap<>();
        final int vxCount = wg.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vertex = wg.getVertex(position);
            if (wg.vertexExists(vertex)) {
                // Find the neighbours of vertex.
                final Set<Integer> neighbours = new HashSet<>();
                final int neighbourCount = wg.getVertexNeighbourCount(vertex);
                for (int n = 0; n < neighbourCount; n++) {
                    final int neighbour = wg.getVertexNeighbour(vertex, n);
                    neighbours.add(neighbour);
                }

                Set<Integer> existingMembers = neighbourMap.get(neighbours);
                if (existingMembers == null) {
                    existingMembers = new HashSet<>();
                    neighbourMap.put(neighbours, existingMembers);
                }

                existingMembers.add(vertex);
            }
        }

        for (final Map.Entry<Set<Integer>, Set<Integer>> e : neighbourMap.entrySet()) {
            final Set<Integer> neighbours = e.getKey();
            final Set<Integer> vertices = e.getValue();

            switch (neighbours.size()) {
                case 0:
                    break;

                case 1:
                    // If this is a doublet A->B, and we just do the layout, then first we'll layout B with A at the centre,
                    // then A with B at the centre, and the doublet will creep across the landscape on successive layouts.
                    // Therefore we do something so doublet layout only happens once.
                    boolean doLayout = true;
                    if (vertices.size() == 1 && neighbours.size() == 1) {
                        final int v = vertices.iterator().next();
                        final int n = neighbours.iterator().next();
                        doLayout = v < n;
                    }

                    if (doLayout) {
                        layoutPendants(wg, vertices, neighbours);
                    }
                    break;

                default:
                    break;

            }
        }
    }

    private void layoutPendants(final GraphWriteMethods graph, final Set<Integer> vertices, final Set<Integer> neighbours) {
        final int xAttr = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(graph);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(graph);

        final int x2Attr = VisualConcept.VertexAttribute.X2.get(graph);
        final int y2Attr = VisualConcept.VertexAttribute.Y2.get(graph);
        final int z2Attr = VisualConcept.VertexAttribute.Z2.get(graph);
        final boolean exists2 = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;

        final int neighbour = neighbours.iterator().next();
        final float neighbourX = graph.getFloatValue(xAttr, neighbour);
        final float neighbourY = graph.getFloatValue(yAttr, neighbour);
        final float neighbourZ = graph.getFloatValue(zAttr, neighbour);

        final int vertexCount = vertices.size();
        double angle = 0;
        double space = Math.PI * 2 / vertexCount;
        double radius = Math.max(3.0, vertexCount * 1.5 / Math.PI);

        // If x2,y2,z2 attributes do not exist, do not set them.
        if (exists2) {
            // Set the xyz2 values so pendants converge to their centres.
            // (This may cause OpenGL problems if a lot of pendant nodes end up at the same coordinates.)
            graph.setFloatValue(x2Attr, neighbour, neighbourX);
            graph.setFloatValue(y2Attr, neighbour, neighbourY);
            graph.setFloatValue(z2Attr, neighbour, neighbourZ);

            for (final int vertex : vertices) {
                graph.setFloatValue(x2Attr, vertex, neighbourX);
                graph.setFloatValue(y2Attr, vertex, neighbourY);
                graph.setFloatValue(z2Attr, vertex, neighbourZ);
            }
        }

        for (final int vertex : vertices) {
            graph.setFloatValue(xAttr, vertex, neighbourX + (float) (Math.sin(angle) * radius));
            graph.setFloatValue(yAttr, vertex, neighbourY + (float) (Math.cos(angle) * radius));
            graph.setFloatValue(zAttr, vertex, neighbourZ);

            angle += space;
        }
    }

    @Override
    public void setMaintainMean(final boolean b) {
        // Method intentionally left blank
    }
}
