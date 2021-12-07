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
package au.gov.asd.tac.constellation.views.find2.plugins.advanced;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.find2.components.advanced.utilities.AdvancedSearchParameters;
import java.util.List;

/**
 *
 * @author Atlas139mkm
 */
@PluginInfo(pluginType = PluginType.SEARCH, tags = {"SEARCH"})
public class AdvancedSearchPlugin extends SimpleEditPlugin {

    private final AdvancedSearchParameters parameters;
    private final boolean selectAll;
    private final boolean selectNext;

    public AdvancedSearchPlugin(final AdvancedSearchParameters parameters, boolean selectAll, boolean selectNext) {
        this.parameters = parameters;
        this.selectAll = selectAll;
        this.selectNext = selectNext;
    }

    private void clearSelection(final GraphWriteMethods graph) {
        final int nodesCount = GraphElementType.VERTEX.getElementCount(graph);
        final int nodeSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int transactionsCount = GraphElementType.TRANSACTION.getElementCount(graph);
        final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        // loop through all nodes that are selected and deselect them
        if (nodeSelectedAttribute != Graph.NOT_FOUND) {
            for (int i = 0; i < nodesCount; i++) {
                final int currElement = GraphElementType.VERTEX.getElement(graph, i);
                graph.setBooleanValue(nodeSelectedAttribute, currElement, false);
            }
        }
        // loop through all transactions that are selected and deselect them
        if (transactionSelectedAttribute != Graph.NOT_FOUND) {
            for (int i = 0; i < transactionsCount; i++) {
                final int currElement = GraphElementType.TRANSACTION.getElement(graph, i);
                graph.setBooleanValue(transactionSelectedAttribute, currElement, false);
            }
        }
    }

    @Override
    protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getId() {
        return super.getId(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getOverriddenPlugins() {
        return super.getOverriddenPlugins(); //To change body of generated methods, choose Tools | Templates.
    }

}
