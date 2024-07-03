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
package au.gov.asd.tac.constellation.plugins.arrangements.circle;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin framework for arrange in a circle with pendants in a circle around
 * their single neighbour.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("ArrangeInCirclesPlugin=Arrange in Circles")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class ArrangeInCirclesPlugin extends SimpleEditPlugin {

    private int xAttribute;
    private int yAttribute;
    private int zAttribute;

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        run(graph);
    }

    private void run(final GraphWriteMethods graph) {
        xAttribute = VisualConcept.VertexAttribute.X.get(graph);
        yAttribute = VisualConcept.VertexAttribute.Y.get(graph);
        zAttribute = VisualConcept.VertexAttribute.Z.get(graph);
        final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);

        final int[] parents = new int[graph.getVertexCapacity()];
        final int[] depths = new int[graph.getVertexCapacity()];
        final int[] cycles = new int[graph.getVertexCapacity()];
        final int[][] children = new int[graph.getVertexCapacity()][];
        final float[] radii = new float[graph.getVertexCapacity()];

        final int firstVertex = graph.getVertex(0);
        parents[firstVertex] = -1;
        depths[firstVertex] = 1;
        extendSpanningTree(graph, firstVertex, parents, depths, cycles);

        final int vertexCount = graph.getVertexCount();
        for (int position = 0; position < vertexCount; position++) {
            final int vertex = graph.getVertex(position);

            if (cycles[vertex] == 0) {
                final int parent = parents[vertex];
                if (parent > 0) {
                    int[] parentChildren = children[parent];
                    if (parentChildren == null) {
                        parentChildren = children[parent] = new int[graph.getVertexNeighbourCount(parent) + 1];
                    }
                    parentChildren[++parentChildren[0]] = vertex;
                }
            }

            graph.setBooleanValue(selectedAttribute, vertex, cycles[vertex] > 0);
        }

        float totalRadius = 4;
        for (int position = 0; position < vertexCount; position++) {
            final int vertex = graph.getVertex(position);

            if (cycles[vertex] > 0) {
                totalRadius += layout(graph, vertex, children, radii);
            }
        }

        totalRadius /= Math.PI;

        float angle = 0;
        for (int position = 0; position < vertexCount; position++) {
            final int vertex = graph.getVertex(position);

            if (cycles[vertex] > 0) {
                angle += radii[vertex] / totalRadius;
                offset(graph, vertex, children, (float) Math.sin(angle) * totalRadius, (float) Math.cos(angle) * totalRadius, 0);
                angle += radii[vertex] / totalRadius;
            }
        }
    }

    private static void extendSpanningTree(final GraphWriteMethods graph, final int vertex, final int[] parents, final int[] depths, final int[] cycles) {
        final int parent = parents[vertex];
        final int neighbourCount = graph.getVertexNeighbourCount(vertex);
        
        for (int position = 0; position < neighbourCount; position++) {
            int neighbour = graph.getVertexNeighbour(vertex, position);
            
            if (neighbour != parent) {
                if (depths[neighbour] > 0) {
                    int v = vertex;
                    while (v != neighbour) {
                        if (depths[v] < depths[neighbour]) {
                            cycles[neighbour] = 1;
                            neighbour = parents[neighbour];
                        } else {
                            cycles[v] = 1;
                            v = parents[v];
                        }
                    }
                    cycles[v] = 1;
                } else {
                    parents[neighbour] = vertex;
                    depths[neighbour] = depths[vertex] + 1;
                    extendSpanningTree(graph, neighbour, parents, depths, cycles);
                }
            }
        }
    }

    private float layout(final GraphWriteMethods graph, final int vertex, final int[][] children, final float[] radii) {

        graph.setFloatValue(xAttribute, vertex, 0);
        graph.setFloatValue(yAttribute, vertex, 0);
        graph.setFloatValue(zAttribute, vertex, 0);

        float totalRadius = 0;

        final int[] vertexChildren = children[vertex];
        if (vertexChildren != null) {
            final int childCount = vertexChildren[0];

            for (int position = 0; position < childCount; position++) {
                totalRadius += layout(graph, vertexChildren[position + 1], children, radii);
            }

            totalRadius /= Math.PI;
            float angle = 0;
            for (int position = 0; position < childCount; position++) {
                final int child = vertexChildren[position + 1];
                angle += totalRadius != 0 ? radii[child] / totalRadius : radii[child];
                offset(graph, child, children, (float) Math.sin(angle) * totalRadius, (float) Math.cos(angle) * totalRadius, 0);
                angle += totalRadius != 0 ? radii[child] / totalRadius : radii[child];
            }

        } else {
            totalRadius = 0F;
        }
        radii[vertex] = totalRadius + 2;
        return radii[vertex];
    }

    private void offset(final GraphWriteMethods graph, final int vertex, final int[][] children, final float x, final float y, final float z) {
        graph.setFloatValue(xAttribute, vertex, graph.getFloatValue(xAttribute, vertex) + x);
        graph.setFloatValue(yAttribute, vertex, graph.getFloatValue(yAttribute, vertex) + y);
        graph.setFloatValue(zAttribute, vertex, graph.getFloatValue(zAttribute, vertex) + z);

        final int[] vertexChildren = children[vertex];
        if (vertexChildren != null) {
            final int childCount = vertexChildren[0];
            for (int position = 0; position < childCount; position++) {
                offset(graph, vertexChildren[position + 1], children, x, y, z);
            }
        }
    }
}
