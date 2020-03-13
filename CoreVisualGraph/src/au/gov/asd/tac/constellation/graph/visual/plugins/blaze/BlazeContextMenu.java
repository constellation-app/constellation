/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import javafx.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 * Add, remove and colour blazes.
 * <p>
 * Modify blazes on one or more vertices. If no vertices are selected, modify
 * the right-clicked vertex. If some vertices are selected, modify them as well
 * as the right-clicked vertex.
 * <p>
 * The blaze popup menu provides four default colour options, as well as options
 * to add a custom colour or remove the blaze(s) entirely.
 *
 * @author algol
 * @author elnath
 */
@ServiceProvider(service = ContextMenuProvider.class, position = 200)
public class BlazeContextMenu implements ContextMenuProvider {

    private static final String BLAZE_MENU = "Blazes";
    private static final String ADD_BLUE_BLAZE = "Add Blue Blazes";
    private static final String ADD_RED_BLAZE = "Add Red Blazes";
    private static final String ADD_YELLOW_BLAZE = "Add Yellow Blazes";
    private static final String ADD_CUSTOM_BLAZE = "Color Blazes...";
    private static final String UNSET_BLAZE = "Remove Blazes";

    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int elementId, final Vector3f unprojected) {

        Blaze clickedBlaze = null;
        int clickedVertexId = Graph.NOT_FOUND;
        BitSet selectedVertices = null;
        Plugin plugin = null;
        PluginParameters parameters = null;

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final int blazeAttributeId = VisualConcept.VertexAttribute.BLAZE.get(rg);
            if (blazeAttributeId != Graph.NOT_FOUND) {
                clickedBlaze = rg.getObjectValue(blazeAttributeId, elementId);
            }

            final int selectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(rg);
            if (selectedAttributeId == Graph.NOT_FOUND || !rg.getBooleanValue(selectedAttributeId, elementId)) {
                clickedVertexId = elementId;
            } else {
                selectedVertices = new BitSet();
                final int vertexCount = rg.getVertexCount();
                for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                    final int vertexId = rg.getVertex(vertexPosition);
                    if (rg.getBooleanValue(selectedAttributeId, vertexId)) {
                        selectedVertices.set(vertexId);
                    }
                }
            }
        } finally {
            rg.release();
        }

        if (clickedBlaze == null) {
            clickedBlaze = BlazeUtilities.DEFAULT_BLAZE;
        }

        switch (item) {
            case ADD_BLUE_BLAZE:
                final ConstellationColor colorB = ConstellationColor.LIGHT_BLUE;
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.setObjectValue(BlazeUtilities.COLOR_PARAMETER_ID, colorB);
                break;
            case ADD_RED_BLAZE:
                final ConstellationColor colorR = ConstellationColor.RED;
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.setObjectValue(BlazeUtilities.COLOR_PARAMETER_ID, colorR);
                break;
            case ADD_YELLOW_BLAZE:
                final ConstellationColor colorY = ConstellationColor.YELLOW;
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.setObjectValue(BlazeUtilities.COLOR_PARAMETER_ID, colorY);
                break;
            case ADD_CUSTOM_BLAZE:
                final ConstellationColor defaultColor = clickedBlaze == null
                        ? BlazeUtilities.DEFAULT_BLAZE.getColor()
                        : clickedBlaze.getColor();
                final Pair<Boolean, ConstellationColor> colorResult = BlazeUtilities.colorDialog(defaultColor);
                if (colorResult.getKey()) {
                    plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                    parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                    parameters.setObjectValue(BlazeUtilities.COLOR_PARAMETER_ID, colorResult.getValue());
                }
                break;
            case UNSET_BLAZE:
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.REMOVE_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                break;
        }

        if (plugin != null && parameters != null) {
            parameters.getParameters().get(BlazeUtilities.VERTEX_ID_PARAMETER_ID).setIntegerValue(clickedVertexId);
            parameters.getParameters().get(BlazeUtilities.VERTEX_IDS_PARAMETER_ID).setObjectValue(selectedVertices);
            PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
        }
    }

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Arrays.asList(BLAZE_MENU);
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        if (elementType == GraphElementType.VERTEX) {
            return Arrays.asList(ADD_BLUE_BLAZE, ADD_RED_BLAZE, ADD_YELLOW_BLAZE, ADD_CUSTOM_BLAZE, UNSET_BLAZE);
        } else {
            return null;
        }
    }
}
