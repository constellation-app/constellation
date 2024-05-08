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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import javafx.util.Pair;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Add, remove and color blazes.
 * <p>
 * Modify blazes on one or more vertices. If no vertices are selected, modify
 * the right-clicked vertex. If some vertices are selected, modify them as well
 * as the right-clicked vertex.
 * <p>
 * The blaze popup menu provides default color options (Blue, Red and Yellow),
 * as well as options to add a custom color or remove the blaze(s) entirely.
 * The remaining list will be populated with colors saved from the users
 * presets.
 *
 * @author algol
 * @author elnath
 */
@ServiceProvider(service = ContextMenuProvider.class, position = 200)
public class BlazeContextMenu implements ContextMenuProvider {

    private static final String BLAZE_MENU = "Blazes";
    private static final String ADD_CUSTOM_BLAZE = "Add Custom Blazes";
    private static final String UNSET_BLAZE = "Remove Blazes";
    private static final int BLACK_COLOR = (new Color(0, 0, 0)).getRGB();

    @StaticResource
    private static final String ADD_RECENT_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_recent.png";
    @StaticResource
    private static final String ADD_CUSTOM_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_custom.png";
    @StaticResource
    private static final String REMOVE_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/removeblaze.png";

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
            case ADD_CUSTOM_BLAZE -> {
                final ConstellationColor defaultColor = clickedBlaze == null
                        ? BlazeUtilities.DEFAULT_BLAZE.getColor()
                        : clickedBlaze.getColor();
                final Pair<Boolean, ConstellationColor> colorResult = BlazeUtilities.colorDialog(defaultColor);
                if (colorResult.getKey()) {
                    plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                    parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                    parameters.setObjectValue(BlazeUtilities.COLOR_PARAMETER_ID, colorResult.getValue());
                }
            }
            case UNSET_BLAZE -> {
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.REMOVE_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
            }
            default -> {
                final ConstellationColor color = ConstellationColor.getColorValue(item);
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.setObjectValue(BlazeUtilities.COLOR_PARAMETER_ID, color);
            }
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
            final List<String> colorList = new ArrayList<>();
            for (final ConstellationColor color : BlazeActions.getPresetCustomColors()) {
                if (color != null) {
                    final Color javaColor = color.getJavaColor();
                    String colorName = "#" + String.format("%02x", javaColor.getRed())
                            + String.format("%02x", javaColor.getGreen())
                            + String.format("%02x", javaColor.getBlue());
                    if (color.getName() != null) {
                        colorName = color.getName();
                    }
                    colorList.add(colorName);
                }

            }

            colorList.add(ADD_CUSTOM_BLAZE);
            colorList.add(UNSET_BLAZE);
            return colorList;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Produce list of custom blaze color icons based on supplied color.
     *
     * @param graph the graph that has been right-clicked on.
     * @param elementType the type of element that has been right-clicked on.
     * @param elementId the id of the element that has been right-clicked on.
     * @return list of custom colored blaze color icons.
     */
    @Override
    public List<ImageIcon> getIcons(final GraphReadMethods graph, final GraphElementType elementType, final int elementId) {
        if (elementType == GraphElementType.VERTEX) {
            final List<ImageIcon> icons = new ArrayList<>();
            for (final ConstellationColor color : BlazeActions.getPresetCustomColors()) {
                if (color != null) {
                    final Color javaColor = color.getJavaColor();
                    final BufferedImage customImage = BlazeActions.copyImageBuffer(
                            (BufferedImage) ImageUtilities.loadImage(ADD_RECENT_BLAZE_ICON, false));

                    for (int x = 0; x < customImage.getWidth(); x++) {
                        for (int y = 0; y < customImage.getHeight(); y++) {
                            if (customImage.getRGB(x, y) == BLACK_COLOR) {
                                customImage.setRGB(x, y, javaColor.getRGB());
                            }
                        }
                    }
                    final ImageIcon icon = new ImageIcon(customImage);
                    icons.add(icon);
                }

            }
            icons.add(ImageUtilities.loadImageIcon(ADD_CUSTOM_BLAZE_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(REMOVE_BLAZE_ICON, false));
            return icons;
        } else {
            return Collections.emptyList();
        }
    }
}
