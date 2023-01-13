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
package au.gov.asd.tac.constellation.views.find2.plugins;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.find2.utilities.FindResult;

/**
 * Delete elements from the graph using the find view
 * 
 * @author Delphinus8821
 */
public class DeleteResultsPlugin extends SimpleEditPlugin {

    private final FindResult result;

    public DeleteResultsPlugin(final FindResult result) {
        this.result = result;

    }
     
    @Override
    protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        if (result.getType() == GraphElementType.VERTEX) {
            graph.removeVertex(result.getID());
        } else if (result.getType() == GraphElementType.TRANSACTION) {
            graph.removeTransaction(result.getID());
        } 

        if (graph.getSchema() != null) {
            graph.getSchema().completeGraph(graph);
        }
    }

    @Override
    public String getName() {
        return "Find: Delete Results Plugin";
    }

}
