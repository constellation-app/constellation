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
package au.gov.asd.tac.constellation.views.find.advanced;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.find.utilities.FindResult;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle.Messages;

/**
 * This class provides access to the Quick Query Services provided on the given
 * graph.
 *
 * @author betelgeuse
 * @see SimpleReadPlugin
 */
@PluginInfo(pluginType = PluginType.SEARCH, tags = {PluginTags.SEARCH})
@Messages("QuickFindPlugin=Find: Quick Search")
public class QuickFindPlugin extends SimplePlugin {

    private final GraphElementType type;
    private final String content;
    private List<FindResult> results;

    /**
     * Constructs a new <code>QuickFindPlugin</code>, and passes in the *
     * relevant find criterion.
     *
     * @param type The type of GraphElements that the search operations will be
     * performed on.
     * @param content The particular string to be searched for.
     *
     * @see GraphElementType
     */
    public QuickFindPlugin(final GraphElementType type, final String content) {
        this.type = type;
        this.content = content;
    }

    @Override
    protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final QueryServices qs = new QueryServices(graphs.getGraph());
        results = qs.quickQuery(type, content);
    }

    /**
     * Returns an <code>&lt;ArrayList&gt;FindResult</code> containing all of the
     * graph elements that were matched in the search operation.
     *
     * @return List of results that matched the search operation.
     * @see ArrayList
     * @see FindResult
     */
    public List<FindResult> getResults() {
        return results;
    }
}
