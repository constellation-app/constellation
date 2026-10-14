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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Keyword search over the registered plugins.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 300)
public class SearchPluginsTool extends AbstractAgentTool {

    private static final int MAX_DESCRIPTION = 240;

    @Override
    public String name() {
        return "search_plugins";
    }

    @Override
    public String description() {
        return "Search Constellation's plugins (analytics such as centrality and clustering, arrangements/layouts, selection, "
                + "editing and Data Access View plugins) by keywords. Returns plugin names to pass to describe_plugin and run_plugin.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        property(schema, "query", "string", "Keywords, e.g. 'centrality', 'arrange tree', 'cluster', 'merge nodes'.");
        property(schema, "limit", "integer", "Maximum results (default 15).");
        property(schema, "include_low_level", "boolean", "Include low level internal plugins (default false).");
        required(schema, "query");
        return schema;
    }

    @Override
    public JsonNode normaliseArguments(final JsonNode args) {
        return alias(args, "query", "keywords", "keyword", "search", "q", "text", "name");
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.READ_ONLY;
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) {
        final List<PluginCatalog.Entry> matches = PluginCatalog.search(text(args, "query", ""), bool(args, "include_low_level", false),
                integer(args, "limit", 15, 1, 100));
        final ArrayNode result = NODES.arrayNode();
        for (final PluginCatalog.Entry entry : matches) {
            final ObjectNode node = result.addObject();
            node.put("name", entry.name());
            node.put("plugin", entry.alias());
            final String description = entry.description();
            node.put("description", description.length() > MAX_DESCRIPTION ? description.substring(0, MAX_DESCRIPTION) + "..." : description);
            final ArrayNode tags = node.putArray("tags");
            entry.tags().forEach(tags::add);
            if (entry.isDataAccess()) {
                node.put("data_access_category", entry.dataAccessType());
                node.put("run_with", "run_data_access_plugin");
            }
        }
        return ToolResult.ok(json(result), matches.size() + " matching plugins");
    }
}
