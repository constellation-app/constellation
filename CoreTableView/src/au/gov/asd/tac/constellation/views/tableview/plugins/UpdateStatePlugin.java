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
package au.gov.asd.tac.constellation.views.tableview.plugins;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewConcept;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;

/**
 * Write the given {@link TableViewState} to the graph.
 *
 * @author formalhaunt
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
public class UpdateStatePlugin extends SimpleEditPlugin {

    private static final String UPDATE_STATE_PLUGIN = "Table View: Update State";

    private final TableViewState tableViewState;

    public UpdateStatePlugin(final TableViewState tableViewState) {
        this.tableViewState = tableViewState;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction,
            final PluginParameters parameters) throws InterruptedException, PluginException {
        final int tableViewStateAttribute = TableViewConcept.MetaAttribute.TABLE_VIEW_STATE.ensure(graph);
        graph.setObjectValue(tableViewStateAttribute, 0, new TableViewState(tableViewState));
    }

    @Override
    protected boolean isSignificant() {
        return true;
    }

    @Override
    public String getName() {
        return UPDATE_STATE_PLUGIN;
    }

    public TableViewState getTableViewState() {
        return tableViewState;
    }
}
