/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.operations.SetFloatValuesOperation;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.graphics.FloatArray;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import org.openide.util.NbBundle;

/**
 *
 * @author twilight_sparkle
 */
@NbBundle.Messages("DragElementsPlugin=Drag Graph Elements")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public final class DragElementsPlugin extends SimpleEditPlugin {

    final IntArray vertexIds;
    final FloatArray newPositions;

    public DragElementsPlugin(final IntArray vertexIds, final FloatArray newPositions) {
        this.vertexIds = vertexIds;
        this.newPositions = newPositions;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int length = vertexIds.size();
        final int xAttribute = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttribute = VisualConcept.VertexAttribute.Y.get(graph);
        final int zAttribute = VisualConcept.VertexAttribute.Z.get(graph);
        final int x2Attribute = VisualConcept.VertexAttribute.X2.get(graph);
        final int y2Attribute = VisualConcept.VertexAttribute.Y2.get(graph);
        final int z2Attribute = VisualConcept.VertexAttribute.Z2.get(graph);
        final SetFloatValuesOperation setXOperation = new SetFloatValuesOperation(graph, GraphElementType.VERTEX, xAttribute);
        final SetFloatValuesOperation setYOperation = new SetFloatValuesOperation(graph, GraphElementType.VERTEX, yAttribute);
        final SetFloatValuesOperation setZOperation = new SetFloatValuesOperation(graph, GraphElementType.VERTEX, zAttribute);

        SetFloatValuesOperation setX2Operation = null;
        SetFloatValuesOperation setY2Operation = null;
        SetFloatValuesOperation setZ2Operation = null;
        if (x2Attribute != Graph.NOT_FOUND) {
            setX2Operation = new SetFloatValuesOperation(graph, GraphElementType.VERTEX, x2Attribute);
            setY2Operation = new SetFloatValuesOperation(graph, GraphElementType.VERTEX, y2Attribute);
            setZ2Operation = new SetFloatValuesOperation(graph, GraphElementType.VERTEX, z2Attribute);
        }

        int ix = 0;
        for (int i = 0; i < length; i++) {
            final int vertexId = vertexIds.get(i);
            setXOperation.setValue(vertexId, newPositions.get(ix++));
            setYOperation.setValue(vertexId, newPositions.get(ix++));
            setZOperation.setValue(vertexId, newPositions.get(ix++));

            if (x2Attribute != Graph.NOT_FOUND) {
                setX2Operation.setValue(vertexId, newPositions.get(ix++));
                setY2Operation.setValue(vertexId, newPositions.get(ix++));
                setZ2Operation.setValue(vertexId, newPositions.get(ix++));
            } else {
                ix += 3;
            }
        }

        setXOperation.finish();
        setYOperation.finish();
        setZOperation.finish();
        graph.executeGraphOperation(setXOperation);
        graph.executeGraphOperation(setYOperation);
        graph.executeGraphOperation(setZOperation);
        if (x2Attribute != Graph.NOT_FOUND) {
            setX2Operation.finish();
            setY2Operation.finish();
            setZ2Operation.finish();
            graph.executeGraphOperation(setX2Operation);
            graph.executeGraphOperation(setY2Operation);
            graph.executeGraphOperation(setZ2Operation);
        }
    }
}
