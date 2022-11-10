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
package au.gov.asd.tac.constellation.views.find2.utilities;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.find2.FindViewController;
import au.gov.asd.tac.constellation.views.find2.components.advanced.utilities.AdvancedSearchParameters;
import au.gov.asd.tac.constellation.views.find2.state.FindViewConcept;
import javafx.application.Platform;

/**
 * Updates the graph selection based on the results of the advanced find plugin
 * 
 * @author Delphinus8821
 */
public class AdvancedFindGraphSelectionPlugin extends SimpleEditPlugin {

    private final boolean selectAll;
    private final boolean getNext;
    private final boolean searchAllGraphs;

    public AdvancedFindGraphSelectionPlugin(final AdvancedSearchParameters parameters, final boolean selectAll, final boolean getNext) {

        this.selectAll = selectAll;
        this.getNext = getNext;

        this.searchAllGraphs = parameters.isSearchAllGraphs();
    }

    @Override
    protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(graph);

        if (!selectAll) {

            /**
             * If the list isn't empty, and the user clicked find next, increment the found lists index by 1, otherwise decrement it by 1. Set the
             * element at the specified index to selected.
             */
            if (!ActiveFindResultsList.getBasicResultsList().isEmpty()) {
                if (getNext) {
                    ActiveFindResultsList.getBasicResultsList().incrementCurrentIndex();
                } else {
                    ActiveFindResultsList.getBasicResultsList().decrementCurrentIndex();
                }

                final int elementId = ActiveFindResultsList.getBasicResultsList().get(ActiveFindResultsList.getAdvancedResultsList().getCurrentIndex()).getID();
              //  final int selectedAttribute = graph.getAttribute(elementType, VisualConcept.VertexAttribute.SELECTED.getName());
              //  graph.setBooleanValue(selectedAttribute, elementId, !removeFromCurrentSelection);
            }

            graph.setObjectValue(stateId, 0, ActiveFindResultsList.getAdvancedResultsList());
        }

        // Swap to view the graph where the element is selected
        if (searchAllGraphs && !ActiveFindResultsList.getAdvancedResultsList().isEmpty()) {
            FindViewUtilities.searchAllGraphs(graph);
        }

        //If no results are found, set the meta attribute to null
        graph.setObjectValue(stateId, 0, ActiveFindResultsList.getAdvancedResultsList().isEmpty() ? null : ActiveFindResultsList.getAdvancedResultsList());
        final int foundResultsLength = ActiveFindResultsList.getAdvancedResultsList().size();
        Platform.runLater(() -> FindViewController.getDefault().setNumResultsFound(foundResultsLength));
    }

    @Override
    public String getName() {
        return "Find: Graph Selection";
    }

}
