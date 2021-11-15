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
import au.gov.asd.tac.constellation.graph.construction.ConnectionBuilder;
import au.gov.asd.tac.constellation.graph.construction.CycleGraphBuilder;
import au.gov.asd.tac.constellation.graph.construction.JellyfishGraphBuilder;
import au.gov.asd.tac.constellation.graph.construction.PathGraphBuilder;
import au.gov.asd.tac.constellation.graph.construction.ProductBuilder;
import au.gov.asd.tac.constellation.graph.construction.TreeGraphBuilder;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Create and arrange a graph using the construction framework.
 *
 * @author twilight_sparkle
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.construction.GraphConstructionDemoAction")
@ActionRegistration(displayName = "#CTL_GraphConstructionDemoAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Build Graph", position = 0)
})
@Messages("CTL_GraphConstructionDemoAction=Construction Demo")
public final class GraphConstructionDemoAction extends AbstractAction {
    
    private static final Logger LOGGER = Logger.getLogger(GraphConstructionDemoAction.class.getName());

    private final GraphNode context;

    /**
     * New context.
     *
     * @param context Graph context.
     */
    public GraphConstructionDemoAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            final Graph graph = context.getGraph();
            PluginExecution.withPlugin(new GraphConstructionPlugin()).executeLater(graph);
        } catch (final Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Plugin to demonstrate graph construction
     */
    @PluginInfo(pluginType = PluginType.CREATE, tags = {"DEVELOPER", PluginTags.EXPERIMENTAL, PluginTags.CREATE})
    public static class GraphConstructionPlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Graph Construction Demo";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

            final CycleGraphBuilder c = CycleGraphBuilder.addCycle(10, true);
            final PathGraphBuilder p = PathGraphBuilder.addPath(5, true);
            final ProductBuilder prod = ProductBuilder.formProduct(graph, c.graph, p.graph, ProductBuilder.ProductType.CARTESIAN_PRODUCT);

            final int xAttrID = VisualConcept.VertexAttribute.X.get(graph);
            final int yAttrID = VisualConcept.VertexAttribute.Y.get(graph);
            final int colorAttrID = VisualConcept.VertexAttribute.COLOR.get(graph);

            for (final int[] leftGraph : prod.leftGraphs) {
                AttributeFillBuilder.fillAttribute(graph, xAttrID, leftGraph, 0f, 40f);
            }
            for (final int[] rightGraph : prod.rightGraphs) {
                AttributeFillBuilder.fillAttribute(graph, yAttrID, rightGraph, 0f, 20f);
            }
            AttributeFillBuilder.fillAttribute(graph, colorAttrID, ConstellationColor.getColorValue(1f, 0f, 0f, 1f), ConstellationColor.getColorValue(0f, 0f, 1f, 1f));

            // Make a jellyfish and a tree and connect the pendants of the fish to the tree root
            final int[] tentacleLengths = {3, 4, 5};
            final JellyfishGraphBuilder j = JellyfishGraphBuilder.addJellyfish(graph, tentacleLengths, JellyfishGraphBuilder.TentacleDirection.UNDIRECTED);

            final int[] childrenAtDepths = {4, 3, 2};
            final TreeGraphBuilder t = TreeGraphBuilder.addTree(graph, childrenAtDepths, TreeGraphBuilder.TreeDirection.AWAY_FROM_ROOT);
            ConnectionBuilder.makeConnection(graph, t.nodesAtLevels[0], j.pendants);

            // Layout the jellyfish
            final float jellyCentreX = -5f;
            final float jellyCentreY = -5f;
            final float[] xTentacleDirs = {0f, -2.5f, 2.5f};
            final float[] yTentacleDirs = {3f, -1.5f, -1.5f};
            graph.setFloatValue(xAttrID, j.centre, jellyCentreX);
            graph.setFloatValue(yAttrID, j.centre, jellyCentreY);
            for (int i = 0; i < j.tentacleNodes.length; i++) {
                final int[] tentacle = j.tentacleNodes[i];
                AttributeFillBuilder.fillAttribute(graph, xAttrID, tentacle, jellyCentreX + xTentacleDirs[i], jellyCentreX + (xTentacleDirs[i] * tentacle.length));
                AttributeFillBuilder.fillAttribute(graph, yAttrID, tentacle, jellyCentreY + yTentacleDirs[i], jellyCentreY + (yTentacleDirs[i] * tentacle.length));
            }
            AttributeFillBuilder.fillAttribute(graph, colorAttrID, j.nodes, ConstellationColor.getColorValue(0f, 1f, 0f, 1f));

            // Layout the tree
            final float treeRootX = -5f;
            final float treeRootY = -10f;
            final float gapBetweenLevels = -4f;
            final float gapWithinLevels = 2f;
            for (int i = 0; i < t.nodesAtLevels.length; i++) {
                final int[] level = t.nodesAtLevels[i];
                AttributeFillBuilder.fillAttribute(graph, xAttrID, level, treeRootX - gapWithinLevels * (level.length - 1), treeRootX + gapWithinLevels * (level.length - 1));
                AttributeFillBuilder.fillAttribute(graph, yAttrID, level, treeRootY + gapBetweenLevels * i);
            }
            AttributeFillBuilder.fillAttribute(graph, colorAttrID, t.nodes, ConstellationColor.getColorValue(1f, 1f, 0f, 1f));

            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        }

    }

}
