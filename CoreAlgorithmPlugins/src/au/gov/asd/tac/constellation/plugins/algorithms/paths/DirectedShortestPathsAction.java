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
package au.gov.asd.tac.constellation.plugins.algorithms.paths;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.gui.ScreenWindowsHelper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Hidden until we figure out how to specify source/destination vertices.
 *
 * @author algol
 */
@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.plugins.algorithms.paths.DirectedShortestPathsAction")
@ActionRegistration(displayName = "#CTL_DirectedShortestPathsAction", iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/paths/shortestpathsfd.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 1600)
})
@Messages("CTL_DirectedShortestPathsAction=Directed Shortest Paths")
public final class DirectedShortestPathsAction implements ActionListener {

    private final GraphNode context;

    private static final String ALERT_HEADER_TEXT = "No nodes selected!";
    private static final String ALERT_TEXT = "Directed Shortest Paths requires at least 1 node to be selected";

    public DirectedShortestPathsAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        if (!isAnyNodeSelected(context.getGraph())) {
            Platform.runLater(() -> NotifyDisplayer.displayAlert("Attention", ALERT_HEADER_TEXT, ALERT_TEXT, AlertType.WARNING, ScreenWindowsHelper.getMainWindowCentrePoint()));
            return;
        }
        PluginExecution.withPlugin(AlgorithmPluginRegistry.DIRECTED_SHORTEST_PATHS)
                .interactively(true)
                .executeLater(context.getGraph());
    }

    private boolean isAnyNodeSelected(final Graph graph) {
        if (graph == null) {
            return false;
        }

        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int vxSelectedAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());

            if (vxSelectedAttr == Graph.NOT_FOUND) {
                return false;
            }

            final int vxCount = rg.getVertexCount();
            for (int position = 0; position < vxCount; position++) {
                if (rg.getBooleanValue(vxSelectedAttr, rg.getVertex(position))) {
                    return true;
                }
            }
        }

        return false;
    }
}
