/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
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
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Display previously selected custom blaze colors for re-use.
 * <p>
 * As custom blaze colors are used, a running set of previously used colors is
 * maintained for easy access.
 * <p>
 * @author serpens24
 */
@ServiceProvider(service = ContextMenuProvider.class, position = 205)
public class BlazeRecentBlazesContextMenu implements ContextMenuProvider {

    private static final String BLAZE_MENU = "Blazes";
    private static final String CUSTOM_BLAZE_MENU = "Recent Custom Blazes";

    private static final int BLACK_COLOR = (new Color(0, 0, 0)).getRGB();

    @StaticResource
    private static final String ADD_RECENT_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_recent.png";

    /**
     * Select given menu item. In this case, loop through the known custom color
     * names and look for one matching the name of the menu item. If found hook
     * in a plugin to apply the given color as a blaze.
     *
     * @param item the item that has been selected.
     * @param graph the graph that has been right-clicked on.
     * @param elementType the type of element that has been right-clicked on.
     * @param elementId the id of the element that has been right-clicked on.
     * @param unprojected the unprojected location of the mouse where the click
     * occurred.
     */
    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int elementId, final Vector3f unprojected) {

        int clickedVertexId = Graph.NOT_FOUND;
        BitSet selectedVertices = null;
        Plugin plugin = null;
        PluginParameters parameters = null;

        final ReadableGraph rg = graph.getReadableGraph();
        try {
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

        for (final ConstellationColor color : BlazeActions.getRecentCustomColors()) {
            final Color javaColor = color.getJavaColor();
            String colorName = color.getName();
            if (colorName == null) {
                colorName = "#" + Integer.toHexString(javaColor.getRed())
                        + Integer.toHexString(javaColor.getGreen())
                        + Integer.toHexString(javaColor.getBlue());
            }

            if (colorName.equals(item)) {
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
        return Arrays.asList(BLAZE_MENU, CUSTOM_BLAZE_MENU);
    }

    /**
     * Generate list of custom blaze color menu items based on the known list of
     * blaze colors.
     *
     * @param graph the graph that has been right-clicked on.
     * @param elementType the type of element that has been right-clicked on.
     * @param elementId the id of the element that has been right-clicked on.
     * @return a list of icons to be placed into the context menu aligned to
     * items provided by getItems.
     */
    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        if (elementType == GraphElementType.VERTEX) {

            // Generate a list of menu items based on the set of stored custom colors
            final List<String> colorList = new ArrayList<>();
            for (final ConstellationColor color : BlazeActions.getRecentCustomColors()) {
                final Color javaColor = color.getJavaColor();
                String colorName = color.getName();
                if (colorName == null) {
                    colorName = "#" + Integer.toHexString(javaColor.getRed()) + Integer.toHexString(javaColor.getGreen()) + Integer.toHexString(javaColor.getBlue());
                }
                colorList.add(0, colorName);
            }
            return colorList;
        } else {
            return Collections.EMPTY_LIST;
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
            for (final ConstellationColor color : BlazeActions.getRecentCustomColors()) {
                final Color javaColor = color.getJavaColor();

                final BufferedImage customImage = BlazeActions.copyImageBuffer((BufferedImage) ImageUtilities.loadImage(ADD_RECENT_BLAZE_ICON, false));
                for (int x = 0; x < customImage.getWidth(); x++) {
                    for (int y = 0; y < customImage.getHeight(); y++) {
                        if (customImage.getRGB(x, y) == BLACK_COLOR) {
                            customImage.setRGB(x, y, javaColor.getRGB());
                        }
                    }
                }
                final ImageIcon icon = new ImageIcon(customImage);
                icons.add(0, icon);
            }
            return icons;
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
