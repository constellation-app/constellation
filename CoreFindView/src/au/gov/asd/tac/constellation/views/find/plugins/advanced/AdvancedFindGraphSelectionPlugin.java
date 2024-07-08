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
package au.gov.asd.tac.constellation.views.find.plugins.advanced;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.find.components.advanced.utilities.AdvancedSearchParameters;
import au.gov.asd.tac.constellation.views.find.state.FindViewConcept;
import au.gov.asd.tac.constellation.views.find.utilities.ActiveFindResultsList;
import au.gov.asd.tac.constellation.views.find.utilities.FindViewUtilities;

/**
 * Updates the graph selection based on the results of the advanced find plugin
 * 
 * @author Delphinus8821
 */
public class AdvancedFindGraphSelectionPlugin extends SimpleEditPlugin {

    private final boolean selectAll;
    private final String searchInLocation;
    private final GraphElementType elementType;
    private final String postSearchAction;
    private static final String REPLACE = "Replace Selection";
    private static final String ALL_GRAPHS = "All Open Graphs";
    private final boolean zoomToSelection;

    public AdvancedFindGraphSelectionPlugin(final AdvancedSearchParameters parameters, final boolean selectAll, final boolean getNext, final boolean zoomToSelection) {
        this.selectAll = selectAll;
        this.searchInLocation = parameters.getSearchInLocation();
        this.elementType = parameters.getGraphElementType();
        this.postSearchAction = parameters.getPostSearchAction();
        this.zoomToSelection = zoomToSelection;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(graph);
        final int selectedAttribute = graph.getAttribute(elementType, VisualConcept.VertexAttribute.SELECTED.getName());

        if (!selectAll) {
            // do this if ignore selection
            if (REPLACE.equals(postSearchAction)) {
                FindViewUtilities.clearSelection(graph);
            }
            
            /**
             * If the list isn't empty, and the user clicked find next,
             * increment the found lists index by 1, otherwise decrement it by
             * 1. Set the element at the specified index to selected.
             */
            if (!ActiveFindResultsList.getAdvancedResultsList().isEmpty()) {
                final int elementId = ActiveFindResultsList.getAdvancedResultsList().get(ActiveFindResultsList.getAdvancedResultsList().getCurrentIndex()).getID();                
                graph.setBooleanValue(selectedAttribute, elementId, true);
            }
            graph.setObjectValue(stateId, 0, ActiveFindResultsList.getAdvancedResultsList());
        }

        // Swap to view the graph where the element is selected
        if (searchInLocation.equals(ALL_GRAPHS) && !ActiveFindResultsList.getAdvancedResultsList().isEmpty()) {
            FindViewUtilities.searchAllGraphs(graph, zoomToSelection);
        } else if (zoomToSelection) {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.ZOOM_TO_SELECTION).executeLater(GraphManager.getDefault().getActiveGraph());
        } else {
            PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeLater(GraphManager.getDefault().getActiveGraph());
        }
        
        
    }

    @Override
    public String getName() {
        return "Find: Graph Selection";
    }
    
}
