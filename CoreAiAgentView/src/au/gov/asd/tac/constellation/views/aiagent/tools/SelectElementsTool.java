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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Changes which vertices or transactions are selected. Runs as an undoable
 * plugin.
 *
 * @author constellation
 */
@ServiceProvider(service = AgentTool.class, position = 200)
public class SelectElementsTool extends AbstractAgentTool {

    @Override
    public String name() {
        return "select_elements";
    }

    @Override
    public String description() {
        return "Select vertices or transactions in the graph, by element id and/or attribute filters (same filter format as query_elements). "
                + "If you give both ids and filters, only the listed ids that also match the filters are selected. "
                + "mode 'replace' (default) selects exactly the matches, 'add' adds them to the selection, 'remove' deselects them. "
                + "Set clear=true with no ids or filters to deselect everything of that element type. The change is undoable.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        enumProperty(schema, "element_type", List.of("vertex", "transaction"), "What to select (default vertex).");
        enumProperty(schema, "mode", List.of("replace", "add", "remove"), "How to combine with the current selection (default replace).");
        final ObjectNode ids = property(schema, "ids", "array", "Element ids to select, from query_elements.");
        ids.putObject("items").put("type", "integer");
        final ObjectNode filters = property(schema, "filters", "array", "Attribute filters: [{attribute, op, value}], see query_elements.");
        final ObjectNode filter = filters.putObject("items");
        filter.put("type", "object");
        enumProperty(schema, "match", List.of("all", "any"), "Whether every filter (default) or any filter must match.");
        property(schema, "clear", "boolean", "Deselect everything of this element type.");
        property(schema, "graph_id", "string", GRAPH_ID_DESCRIPTION);
        return schema;
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.MODIFIES_GRAPH;
    }

    @Override
    public ToolPolicy defaultPolicy() {
        // selection is a visual-only change
        return ToolPolicy.AUTO;
    }

    @Override
    public String describeCall(final JsonNode args) {
        if (bool(args, "clear", false)) {
            return "Clear the " + text(args, "element_type", "vertex") + " selection";
        }
        final int ids = args.has("ids") ? args.get("ids").size() : 0;
        final int filters = args.has("filters") ? args.get("filters").size() : 0;
        final GraphElementType type = GraphSupport.elementType(text(args, "element_type", "vertex"), GraphElementType.VERTEX);
        final String what = ids > 0 ? String.format("%d %s", ids, GraphSupport.plural(type, ids)) : "matching " + GraphSupport.plural(type, 2);
        final String verb = switch (text(args, "mode", "replace")) {
            case "add" ->
                "Add to the selection: ";
            case "remove" ->
                "Deselect ";
            default ->
                "Select only ";
        };
        return verb + what + (filters > 0 && ids > 0 ? String.format(" that match %d filter%s", filters, filters == 1 ? "" : "s")
                : filters > 0 ? String.format(" (%d filter%s)", filters, filters == 1 ? "" : "s") : "");
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) throws InterruptedException, PluginException {
        final Graph graph = GraphSupport.requireGraph(context, args);
        final boolean clear = bool(args, "clear", false);
        final boolean hasIds = args.has("ids") && args.get("ids").isArray() && !args.get("ids").isEmpty();
        final boolean hasFilters = args.has("filters") && args.get("filters").isArray() && !args.get("filters").isEmpty();
        if (!clear && !hasIds && !hasFilters) {
            return ToolResult.error("Give ids or filters to select, or set clear=true to deselect everything.");
        }
        final SelectPlugin plugin = new SelectPlugin(args);
        PluginExecution.withPlugin(plugin).executeNow(graph);
        final ObjectNode result = NODES.objectNode();
        result.put("element_type", GraphSupport.typeName(plugin.type));
        result.put("matched", plugin.matched);
        result.put("selected_before", plugin.selectedBefore);
        result.put("selected_after", plugin.selectedAfter);
        return ToolResult.ok(json(result), String.format("%d %s selected (was %d)", plugin.selectedAfter,
                GraphSupport.plural(plugin.type, plugin.selectedAfter), plugin.selectedBefore));
    }

    /**
     * The undoable selection change.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    static final class SelectPlugin extends SimpleEditPlugin {

        private final JsonNode args;
        private GraphElementType type;
        private int matched;
        private int selectedBefore;
        private int selectedAfter;

        SelectPlugin(final JsonNode args) {
            this.args = args;
        }

        @Override
        public String getName() {
            return "AI Assistant: Select Elements";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            type = GraphSupport.elementType(text(args, "element_type", "vertex"), GraphElementType.VERTEX);
            final int selected = type == GraphElementType.VERTEX
                    ? VisualConcept.VertexAttribute.SELECTED.ensure(graph)
                    : VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
            final boolean clear = bool(args, "clear", false);
            final String mode = text(args, "mode", "replace");
            final boolean matchAll = !"any".equalsIgnoreCase(text(args, "match", "all"));
            final List<GraphSupport.BoundFilter> filters = GraphSupport.bind(graph, type, GraphSupport.Filter.parse(args.get("filters")));
            final Set<Integer> ids = new HashSet<>();
            if (args.get("ids") instanceof ArrayNode array) {
                array.forEach(node -> ids.add(node.asInt()));
            }
            final boolean byIds = !ids.isEmpty();
            final boolean byFilters = !filters.isEmpty();

            final int count = type.getElementCount(graph);
            for (int position = 0; position < count; position++) {
                final int element = type.getElement(graph, position);
                final boolean was = graph.getBooleanValue(selected, element);
                if (was) {
                    selectedBefore++;
                }
                // ids pick elements; filters narrow them (or pick on their own when there are no ids)
                final boolean match = !clear && (!byIds || ids.contains(element))
                        && (!byFilters || GraphSupport.matches(graph, element, filters, matchAll));
                if (match) {
                    matched++;
                }
                final boolean now;
                if (clear) {
                    now = false;
                } else {
                    now = switch (mode) {
                        case "add" ->
                            was || match;
                        case "remove" ->
                            was && !match;
                        default ->
                            match;
                    };
                }
                if (now != was) {
                    graph.setBooleanValue(selected, element, now);
                }
                if (now) {
                    selectedAfter++;
                }
            }
        }
    }
}
