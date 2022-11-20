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
package au.gov.asd.tac.constellation.views.find2.components.advanced.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.find2.state.FindViewConcept;
import au.gov.asd.tac.constellation.views.find2.utilities.ActiveFindResultsList;
import au.gov.asd.tac.constellation.views.find2.utilities.FindViewUtilities;

/**
 * Updates the graph selection based on the results of the advanced find plugin
 * 
 * @author Delphinus8821
 */
public class AdvancedFindGraphSelectionPlugin extends SimpleEditPlugin {

    private final boolean selectAll;
    private final boolean getNext;
    private final boolean searchAllGraphs;
    private final GraphElementType elementType;
    private final String currentSelection;
    private static final String IGNORE = "Ignore";

    public AdvancedFindGraphSelectionPlugin(final AdvancedSearchParameters parameters, final boolean selectAll, final boolean getNext) {
        this.selectAll = selectAll;
        this.getNext = getNext;
        this.searchAllGraphs = parameters.isSearchAllGraphs();
        this.elementType = parameters.getGraphElementType();
        this.currentSelection = parameters.getCurrentSelection();
    }

    @Override
    protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(graph);
        final int selectedAttribute = graph.getAttribute(elementType, VisualConcept.VertexAttribute.SELECTED.getName());

        if (!selectAll) {
            // do this if ignore selection
            if (IGNORE.equals(currentSelection)) {
                clearSelection(graph);
            }
            
            /**
             * If the list isn't empty, and the user clicked find next,
             * increment the found lists index by 1, otherwise decrement it by
             * 1. Set the element at the specified index to selected.
             */
            if (!ActiveFindResultsList.getAdvancedResultsList().isEmpty()) {
                if (getNext) {
                    ActiveFindResultsList.getAdvancedResultsList().incrementCurrentIndex();
                } else {
                    ActiveFindResultsList.getAdvancedResultsList().decrementCurrentIndex();
                }
                final int elementId = ActiveFindResultsList.getAdvancedResultsList().get(ActiveFindResultsList.getAdvancedResultsList().getCurrentIndex()).getID();                
                graph.setBooleanValue(selectedAttribute, elementId, true);
            }
            graph.setObjectValue(stateId, 0, ActiveFindResultsList.getAdvancedResultsList());
        }

        // Swap to view the graph where the element is selected
        if (searchAllGraphs && !ActiveFindResultsList.getAdvancedResultsList().isEmpty()) {
            FindViewUtilities.searchAllGraphs(graph);
        }
    }

    @Override
    public String getName() {
        return "Find: Graph Selection";
    }
    
    /**
     * This function clears all the currently selected elements on the graph.
     *
     * @param graph
     */
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
}
