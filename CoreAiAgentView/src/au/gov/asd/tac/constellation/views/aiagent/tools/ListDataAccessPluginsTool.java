/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.tools;

import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Lists the Data Access View plugins by category.
 *
 * @author constellation
 */
@ServiceProvider(service = AgentTool.class, position = 400)
public class ListDataAccessPluginsTool extends AbstractAgentTool {

    static final Set<String> HIDDEN_BY_DEFAULT = Set.of(DataAccessPluginCoreType.EXPERIMENTAL, DataAccessPluginCoreType.DEVELOPER,
            DataAccessPluginCoreType.FAVOURITES);
    private static final int MAX_DESCRIPTION = 200;

    @Override
    public String name() {
        return "list_data_access_plugins";
    }

    @Override
    public String description() {
        return "List the Data Access View plugins, grouped by category (Import, Extend, Enrichment, Clean, Utility, Workflow, ...). "
                + "These query or import data from external sources, or clean up the graph.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        property(schema, "category", "string", "Only list this category, e.g. 'Import' or 'Clean'.");
        property(schema, "include_experimental", "boolean", "Include the Experimental and Developer categories (default false).");
        return schema;
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.READ_ONLY;
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) {
        final String category = text(args, "category", null);
        final boolean includeExperimental = bool(args, "include_experimental", false);
        final ObjectNode result = NODES.objectNode();
        int count = 0;
        for (final Map.Entry<String, List<DataAccessPlugin>> entry : DataAccessUtilities.getAllPlugins().entrySet()) {
            final String type = entry.getKey();
            if (entry.getValue().isEmpty() || category != null && !category.equalsIgnoreCase(type)
                    || category == null && !includeExperimental && HIDDEN_BY_DEFAULT.contains(type)) {
                continue;
            }
            final ArrayNode plugins = result.putArray(type);
            for (final DataAccessPlugin plugin : entry.getValue()) {
                final PluginCatalog.Entry info = PluginCatalog.describe(plugin);
                final ObjectNode node = plugins.addObject();
                node.put("name", info.name());
                node.put("plugin", info.alias());
                final String description = info.description();
                node.put("description", description.length() > MAX_DESCRIPTION ? description.substring(0, MAX_DESCRIPTION) + "..." : description);
                count++;
            }
        }
        return ToolResult.ok(json(result), count + " Data Access View plugins");
    }
}
