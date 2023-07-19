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
package au.gov.asd.tac.constellation.views.find.plugins;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.find.state.FindViewConcept;
import au.gov.asd.tac.constellation.views.find.utilities.FindResultsList;

/**
 *
 * @author Atlas139mkm
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {"UPDATE", "MODIFY"})
public class ResetStatePlugin extends SimpleEditPlugin {

    // the starting index for the FindResultsList
    private static final int STARTING_INDEX = -1;

    public ResetStatePlugin() {
        /**
         * This plugin is used with no variables, as does not explicitly need
         * any.
         */
    }

    /**
     * This edit function retrieves the FindViewConcept MetaAttribute
     * (FindResultsList) and resets the current index back to -1 as the graph
     * element count has changed.
     *
     * @param graph
     * @param interaction
     * @param parameters
     * @throws InterruptedException
     * @throws PluginException
     */
    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int stateId = FindViewConcept.MetaAttribute.FINDVIEW_STATE.ensure(graph);
        FindResultsList foundResult = graph.getObjectValue(stateId, 0);
        if (foundResult != null) {
            foundResult.setCurrentIndex(STARTING_INDEX);
            graph.setObjectValue(stateId, 0, foundResult);
        }
    }

    /**
     * Gets the name of the plugin
     *
     * @return
     */
    @Override
    public String getName() {
        return "Find: Update State";
    }

}
