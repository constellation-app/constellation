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
package au.gov.asd.tac.constellation.graph.visual.plugins.select.structure;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Select all nodes with no incoming transactions, but with outgoing
 * transactions
 *
 * @author aquila
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("SelectSourcesPlugin=Add to Selection: Sources")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class SelectSourcesPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int vxSelected = VisualConcept.VertexAttribute.SELECTED.get(graph);

        if (vxSelected != Graph.NOT_FOUND) {
            final int vertexCount = graph.getVertexCount();

            for (int position = 0; position < vertexCount; position++) {
                //Convert the position to a vertex
                final int vertex = graph.getVertex(position);

                //Discover the number of incoming and outgoing transactions
                final int numberOutgoing = graph.getVertexTransactionCount(vertex, Graph.OUTGOING);
                final int numberIncoming = graph.getVertexTransactionCount(vertex, Graph.INCOMING);

                //Set the Nodes that are sources to be selected.
                //DO NOT UNSELECT ANY EXISTING VERTICES
                if (numberOutgoing > 0 && numberIncoming == 0) {
                    graph.setBooleanValue(vxSelected, vertex, true);
                }
            }
        }
    }
}
