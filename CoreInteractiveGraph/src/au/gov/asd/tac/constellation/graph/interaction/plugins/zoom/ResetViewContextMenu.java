/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.plugins.zoom;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.GraphContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Reset View Context Menu
 *
 * @author algol
 */
@ServiceProvider(service = GraphContextMenuProvider.class, position = 10000)
public class ResetViewContextMenu implements GraphContextMenuProvider {

    private static final String RESET_VIEW = "Reset View";
    private static final String X_AXIS = "X Axis";
    private static final String NEGATIVE_X_AXIS = "-X Axis";
    private static final String Y_AXIS = "Y Axis";
    private static final String NEGATIVE_Y_AXIS = "-Y Axis";
    private static final String Z_AXIS = "Z Axis";
    private static final String NEGATIVE_Z_AXIS = "-Z Axis";

    private static final String X_AXIS_ICON = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/axis_x.png";
    private static final String NEGATIVE_X_AXIS_ICON = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/axis_x_negative.png";
    private static final String Y_AXIS_ICON = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/axis_y.png";
    private static final String NEGATIVE_Y_AXIS_ICON = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/axis_y_negative.png";
    private static final String Z_AXIS_ICON = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/axis_z.png";
    private static final String NEGATIVE_Z_AXIS_ICON = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/axis_z_negative.png";

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Arrays.asList(RESET_VIEW);
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        if (elementType == GraphElementType.GRAPH) {
            return Arrays.asList(X_AXIS, NEGATIVE_X_AXIS, Y_AXIS, NEGATIVE_Y_AXIS, Z_AXIS, NEGATIVE_Z_AXIS);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int element, final Vector3f unprojected) {
        if (RESET_VIEW.equals(item)) {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeLater(graph);
        } else if (X_AXIS.equals(item)) {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .withParameter(ResetViewPlugin.AXIS_PARAMETER_ID, "x")
                    .withParameter(ResetViewPlugin.NEGATIVE_PARAMETER_ID, false)
                    .withParameter(ResetViewPlugin.SIGNIFICANT_PARAMETER_ID, true)
                    .executeLater(graph);
        } else if (Y_AXIS.equals(item)) {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .withParameter(ResetViewPlugin.AXIS_PARAMETER_ID, "y")
                    .withParameter(ResetViewPlugin.NEGATIVE_PARAMETER_ID, false)
                    .withParameter(ResetViewPlugin.SIGNIFICANT_PARAMETER_ID, true)
                    .executeLater(graph);
        } else if (Z_AXIS.equals(item)) {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .withParameter(ResetViewPlugin.AXIS_PARAMETER_ID, "z")
                    .withParameter(ResetViewPlugin.NEGATIVE_PARAMETER_ID, false)
                    .withParameter(ResetViewPlugin.SIGNIFICANT_PARAMETER_ID, true)
                    .executeLater(graph);
        } else if (NEGATIVE_X_AXIS.equals(item)) {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .withParameter(ResetViewPlugin.AXIS_PARAMETER_ID, "x")
                    .withParameter(ResetViewPlugin.NEGATIVE_PARAMETER_ID, true)
                    .withParameter(ResetViewPlugin.SIGNIFICANT_PARAMETER_ID, true)
                    .executeLater(graph);
        } else if (NEGATIVE_Y_AXIS.equals(item)) {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .withParameter(ResetViewPlugin.AXIS_PARAMETER_ID, "y")
                    .withParameter(ResetViewPlugin.NEGATIVE_PARAMETER_ID, true)
                    .withParameter(ResetViewPlugin.SIGNIFICANT_PARAMETER_ID, true)
                    .executeLater(graph);
        } else if (NEGATIVE_Z_AXIS.equals(item)) {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .withParameter(ResetViewPlugin.AXIS_PARAMETER_ID, "z")
                    .withParameter(ResetViewPlugin.NEGATIVE_PARAMETER_ID, true)
                    .withParameter(ResetViewPlugin.SIGNIFICANT_PARAMETER_ID, true)
                    .executeLater(graph);
        }
    }

    /**
     * Produce the list of icons for the reset view menu
     *
     * @param graph the graph that has been right-clicked on.
     * @param elementType the type of element that has been right-clicked on.
     * @param elementId the id of the element that has been right-clicked on.
     * @return list of reset view icons
     */
    @Override
    public List<ImageIcon> getIcons(final GraphReadMethods graph, final GraphElementType elementType, final int elementId) {
        if (elementType == GraphElementType.GRAPH) {
            final List<ImageIcon> icons = new ArrayList<>();
            icons.add(ImageUtilities.loadImageIcon(X_AXIS_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(NEGATIVE_X_AXIS_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(Y_AXIS_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(NEGATIVE_Y_AXIS_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(Z_AXIS_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(NEGATIVE_Z_AXIS_ICON, false));
            return icons;
        } else {
            return Collections.emptyList();
        }
    }
}
