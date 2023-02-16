/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview2.plugins;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author altair1673
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
@NbBundle.Messages("SelectOnGraphPlugin=Highlights nodes on their corresponding marker gets selected on the map")
public class SelectOnGraphPlugin extends SimpleEditPlugin {

    // IDs of the selected nodes
    private List<Integer> selectedNodeList = new ArrayList<>();
    private boolean isSelectingVertex = true;

    public SelectOnGraphPlugin(List<Integer> selectedNodeList, boolean isSelectingVertex) {
        this.selectedNodeList = new ArrayList<>(selectedNodeList);
        this.isSelectingVertex = isSelectingVertex;
    }

    public SelectOnGraphPlugin() {

    }

    /**
     * Select vertices on graph which correspond to selected markers on the map
     *
     * @param graph
     * @param interaction
     * @param parameters
     * @throws InterruptedException
     * @throws PluginException
     */
    @Override
    protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {

        if (graph != null) {
            // Select vertex
            if (isSelectingVertex) {
                final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
                final int vertexCount = graph.getVertexCount();

                for (int i = 0; i < vertexCount; ++i) {
                    final int vertexID = graph.getVertex(i);
                    graph.setBooleanValue(vertexSelectedAttribute, vertexID, selectedNodeList.contains(vertexID));
                }

                // Select transactions
            } else {
                final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(graph);
                final int transactionCount = graph.getTransactionCount();

                for (int i = 0; i < transactionCount; ++i) {
                    final int transactionID = graph.getTransaction(i);
                    graph.setBooleanValue(transactionSelectedAttribute, transactionID, selectedNodeList.contains(transactionID));
                }
            }
        }
    }

    @Override
    public String getName() {
        return "SelectOnGraphPlugin2";
    }

}
