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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Selection Context Menu
 *
 * @author sirius
 */
@ServiceProvider(service = ContextMenuProvider.class, position = 800)
public class SelectionContextMenu implements ContextMenuProvider {

    private static final String SELECT_ALL = "Select All";
    private static final String DESELECT_ALL = "Deselect All";
    private static final String DESELECT_VERTICES = "Deselect Nodes";
    private static final String DESELECT_TRANSACTIONS = "Deselect Transactions";
    private static final String INVERT_SELECTION = "Invert Selection";
    private static final String SELECT_BLAZES = "Select Blazes";
    private static final String DESELECT_BLAZES = "Deselect Blazes";
    private static final String SELECT_DIMMED = "Select Dimmed";
    private static final String SELECT_UNDIMMED = "Select Undimmed";

    @StaticResource
    private static final String SELECT_ALL_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/select/resources/select_all.png";
    @StaticResource
    private static final String DESELECT_ALL_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/select/resources/deselect_all.png";
    @StaticResource
    private static final String DESELECT_VERTICES_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/select/resources/deselectNodes.png";
    @StaticResource
    private static final String DESELECT_TRANSACTIONS_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/select/resources/deselectTransactions.png";
    @StaticResource
    private static final String INVERT_SELECTION_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/select/resources/invert_selection.png";
    @StaticResource
    private static final String SELECT_BLAZES_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/selectblazes.png";
    @StaticResource
    private static final String DESELECT_BLAZES_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/blaze.png";
    @StaticResource
    private static final String SELECT_DIMMED_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/select_dimmed.png";
    @StaticResource
    private static final String SELECT_UNDIMMED_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/select_undimmed.png";

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Arrays.asList("Selection");
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        if (elementType == GraphElementType.GRAPH) {
            return Arrays.asList(SELECT_ALL, DESELECT_ALL, DESELECT_VERTICES, DESELECT_TRANSACTIONS, INVERT_SELECTION, SELECT_BLAZES, DESELECT_BLAZES, SELECT_DIMMED, SELECT_UNDIMMED);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void selectItem(String item, Graph graph, GraphElementType elementType, int entity, Vector3f unprojected) {
        if (SELECT_ALL.equals(item)) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_ALL).executeLater(graph);
        } else if (DESELECT_ALL.equals(item)) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_ALL).executeLater(graph);
        } else if (DESELECT_VERTICES.equals(item)) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_VERTICES).executeLater(graph);
        } else if (DESELECT_TRANSACTIONS.equals(item)) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_TRANSACTIONS).executeLater(graph);
        } else if (INVERT_SELECTION.equals(item)) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.INVERT_SELECTION).executeLater(graph);
        } else if (SELECT_BLAZES.equals(item)) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_BLAZES).executeLater(graph);
        } else if (DESELECT_BLAZES.equals(item)) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_BLAZES).executeLater(graph);
        } else if (SELECT_DIMMED.equals(item)) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_DIMMED).executeLater(graph);
        } else if (SELECT_UNDIMMED.equals(item)) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_UNDIMMED).executeLater(graph);
        }
    }

    /**
     * Produce the list of icons for the selection menu
     *
     * @param graph the graph that has been right-clicked on.
     * @param elementType the type of element that has been right-clicked on.
     * @param elementId the id of the element that has been right-clicked on.
     * @return list of selection icons
     */
    @Override
    public List<ImageIcon> getIcons(final GraphReadMethods graph, final GraphElementType elementType, final int elementId) {
        if (elementType == GraphElementType.GRAPH) {
            final List<ImageIcon> icons = new ArrayList<>();
            icons.add(ImageUtilities.loadImageIcon(SELECT_ALL_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(DESELECT_ALL_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(DESELECT_VERTICES_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(DESELECT_TRANSACTIONS_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(INVERT_SELECTION_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(SELECT_BLAZES_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(DESELECT_BLAZES_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(SELECT_DIMMED_ICON, false));
            icons.add(ImageUtilities.loadImageIcon(SELECT_UNDIMMED_ICON, false));
            return icons;
        } else {
            return Collections.emptyList();
        }
    }
}
