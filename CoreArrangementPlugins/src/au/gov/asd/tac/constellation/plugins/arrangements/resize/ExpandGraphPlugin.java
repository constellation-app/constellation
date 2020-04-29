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
package au.gov.asd.tac.constellation.plugins.arrangements.resize;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.BitSet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin for expanding the elements in a graph
 */
@ServiceProvider(service = Plugin.class)
@Messages("ExpandGraphPlugin=Expand Graph")
public class ExpandGraphPlugin extends SimpleEditPlugin {

    private static final float SCALE = 11f / 10f;

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int vertexXAttributeId = VisualConcept.VertexAttribute.X.get(graph);
        final int vertexYAttributeId = VisualConcept.VertexAttribute.Y.get(graph);
        final int vertexZAttributeId = VisualConcept.VertexAttribute.Z.get(graph);
        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);

        final int vertexCount = graph.getVertexCount();
        final BitSet selectedVertices = new BitSet(vertexCount);
        if (vertexSelectedAttributeId != GraphConstants.NOT_FOUND) {
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);
                final boolean vxSelected = graph.getBooleanValue(vertexSelectedAttributeId, vertexId);
                if (vxSelected) {
                    selectedVertices.set(vertexPosition);
                }
            }
        }

        int currentStep = 0;
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            int newStep = vertexPosition * 10 / vertexCount;
            if (newStep > currentStep) {
                currentStep = newStep;
                interaction.setProgress(currentStep, 10, "Working...", true);
            }

            final int vertexId = graph.getVertex(vertexPosition);
            if (selectedVertices.isEmpty() || selectedVertices.get(vertexPosition)) {
                if (vertexXAttributeId != Graph.NOT_FOUND) {
                    final float x = graph.getFloatValue(vertexXAttributeId, vertexId);
                    graph.setFloatValue(vertexXAttributeId, vertexId, x * SCALE);
                }
                if (vertexYAttributeId != Graph.NOT_FOUND) {
                    final float y = graph.getFloatValue(vertexYAttributeId, vertexId);
                    graph.setFloatValue(vertexYAttributeId, vertexId, y * SCALE);
                }
                if (vertexZAttributeId != Graph.NOT_FOUND) {
                    final float z = graph.getFloatValue(vertexZAttributeId, vertexId);
                    graph.setFloatValue(vertexZAttributeId, vertexId, z * SCALE);
                }
            }
        }
    }
}
