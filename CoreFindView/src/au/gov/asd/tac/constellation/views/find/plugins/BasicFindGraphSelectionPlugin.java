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
package au.gov.asd.tac.constellation.views.find.plugins;

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
import au.gov.asd.tac.constellation.views.find.state.FindViewConcept;
import au.gov.asd.tac.constellation.views.find.utilities.ActiveFindResultsList;
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find.utilities.FindViewUtilities;

/**
 * Updates the graph selection based on the results of the basic find plugin
 * 
 * @author Delphinus8821
 */
public class BasicFindGraphSelectionPlugin extends SimpleEditPlugin {

    private final GraphElementType elementType;
    private final boolean removeFromCurrentSelection;
    private final boolean selectAll;
    private final boolean searchAllGraphs;
    private final boolean zoomToSelection;

    public BasicFindGraphSelectionPlugin(final BasicFindReplaceParameters parameters, final boolean selectAll, final boolean zoomToSelection) {
        this.elementType = parameters.getGraphElement();
        this.selectAll = selectAll;
        this.removeFromCurrentSelection = parameters.isRemoveFrom();
        this.searchAllGraphs = parameters.isSearchAllGraphs();
        this.zoomToSelection = zoomToSelection;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(graph);

        if (!selectAll) {
            /**
             * If the list isn't empty, and the user clicked find next, increment the found lists index by 1, otherwise decrement it by 1. Set the
             * element at the specified index to selected.
             */
            if (!ActiveFindResultsList.getBasicResultsList().isEmpty()) {
                final int elementId = ActiveFindResultsList.getBasicResultsList().get(ActiveFindResultsList.getBasicResultsList().getCurrentIndex()).getID();                
                final int selectedAttribute = graph.getAttribute(elementType, VisualConcept.VertexAttribute.SELECTED.getName());
                graph.setBooleanValue(selectedAttribute, elementId, !removeFromCurrentSelection);
            }
            graph.setObjectValue(stateId, 0, ActiveFindResultsList.getBasicResultsList());
        }
 
        // Swap to view the graph where the element is selected
        if (searchAllGraphs && !ActiveFindResultsList.getBasicResultsList().isEmpty()) {
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
