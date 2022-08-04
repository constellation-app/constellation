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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author cygnus_x-1
 */
@ActionID(category = "Schema", id = "au.gov.asd.tac.constellation.testing.construction.IconGraphBuilderAction")
@ActionRegistration(displayName = "#CTL_IconGraphBuilderAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Build Graph", position = 0)
})
@NbBundle.Messages("CTL_IconGraphBuilderAction=Icon Graph")
public class IconGraphBuilderAction extends AbstractAction {

    private final GraphNode context;

    public IconGraphBuilderAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Graph graph = context.getGraph();
        PluginExecutor.startWith(new BuildIconGraphPlugin()).followedBy(ArrangementPluginRegistry.GRID_COMPOSITE)
                .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW).executeWriteLater(graph);
    }

    /**
     * An icon graph structure.
     */
    private static class IconGraph {

        /**
         * Make the graph structure.
         *
         * @param graph The graph to build the structure in.
         */
        public static void makeGraph(final GraphWriteMethods graph) {
            final int labelAttribute = VisualConcept.VertexAttribute.LABEL.get(graph);
            final int identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
            final int colorAttribute = VisualConcept.VertexAttribute.COLOR.get(graph);
            final int backgroundIconAttribute = VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph);
            final int foregroundIconAttribute = VisualConcept.VertexAttribute.FOREGROUND_ICON.get(graph);

            final Set<ConstellationIcon> allIcons = IconManager.getIcons();
            for (final ConstellationIcon icon : allIcons) {
                final int vertexId = graph.addVertex();
                graph.setStringValue(labelAttribute, vertexId, icon.getExtendedName());
                graph.setStringValue(identifierAttribute, vertexId, icon.getExtendedName());
                graph.setObjectValue(colorAttribute, vertexId, ConstellationColor.BLUEBERRY);
                graph.setObjectValue(backgroundIconAttribute, vertexId, DefaultIconProvider.FLAT_SQUARE);
                graph.setObjectValue(foregroundIconAttribute, vertexId, icon);
            }
        }
    }

    /**
     * Plugin to create an icon graph
     */
    @PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE, PluginTags.EXPERIMENTAL})
    public static class BuildIconGraphPlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Build Icon Graph";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            IconGraph.makeGraph(graph);
        }

    }

}
