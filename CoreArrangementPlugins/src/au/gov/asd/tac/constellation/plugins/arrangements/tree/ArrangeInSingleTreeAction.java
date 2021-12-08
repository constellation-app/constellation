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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimpleAction;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.security.SecureRandom;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * action framework supporting the Single circular tree arrangement
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.tree.ArrangeInSingleTreeAction")
@ActionRegistration(displayName = "#CTL_ArrangeInSingleTreeAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Arrangements", position = 0)
})
@Messages("CTL_ArrangeInSingleTreeAction=Single Tree")
public final class ArrangeInSingleTreeAction extends SimpleAction {

    public ArrangeInSingleTreeAction(GraphNode context) {
        super(context);
    }

    @Override
    protected void edit(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph graph = graphs.getGraph();
        final WritableGraph wg = graph.getWritableGraph(Bundle.CTL_ArrangeInSingleTreeAction(), true);
        try {
            final Worker worker = new Worker(wg);
            worker.run();

            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graphs.getGraph());
        } finally {
            wg.commit();
        }
    }

    private static class Worker {

        private final GraphWriteMethods graph;

        private final SecureRandom r = new SecureRandom();

        private Worker(final GraphWriteMethods graph) {
            this.graph = graph;
        }

        private void run() throws InterruptedException {
            final TreeTaxonArranger treeArranger = new TreeTaxonArranger(null, null);

            final GraphTaxonomy tax = treeArranger.getTaxonomy(graph);

            if (VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph) == Graph.NOT_FOUND) {
                graph.addAttribute(GraphElementType.VERTEX, IconAttributeDescription.ATTRIBUTE_NAME, "background_icon", "background_icon", null, null);
            }
            final int bgiconAttr = VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph);

            if (VisualConcept.VertexAttribute.COLOR.get(graph) == Graph.NOT_FOUND) {
                graph.addAttribute(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, ColorAttributeDescription.ATTRIBUTE_NAME, ColorAttributeDescription.ATTRIBUTE_NAME, null, null);
            }
            final int colorAttr = VisualConcept.VertexAttribute.COLOR.get(graph);

            // Colour the taxonomies so we can see what's going on.
            if (tax != null) {
                for (final Integer subvxId : tax.getTaxa().keySet()) {
                    final ConstellationColor color = ConstellationColor.getColorValue(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1F);
                    final Set<Integer> subgraph = tax.getTaxa().get(subvxId);
                    for (final int vxId : subgraph) {
                        graph.setStringValue(bgiconAttr, vxId, "Background.Round Circle");
                        graph.setObjectValue(colorAttr, vxId, color);
                    }
                }
            }

            final CircTreeArranger arranger = new CircTreeArranger(CircTreeChoiceParameters.getDefaultParameters());
            arranger.arrange(graph);
        }
    }
}
