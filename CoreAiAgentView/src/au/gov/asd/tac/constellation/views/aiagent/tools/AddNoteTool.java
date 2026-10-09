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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import au.gov.asd.tac.constellation.views.notes.api.NotesViewApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Writes a Markdown note to the graph's Notes View.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 600)
public class AddNoteTool extends AbstractAgentTool {

    public static final String AI_TAG = "AI";

    @Override
    public String name() {
        return "add_note";
    }

    @Override
    public String description() {
        return "Write a note (Markdown) to the graph's Notes View, e.g. to record findings for the analyst. Notes are tagged 'AI'.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        property(schema, "title", "string", "A short title.");
        property(schema, "content", "string", "The note, in Markdown.");
        final ObjectNode tags = property(schema, "tags", "array", "Extra tags.");
        tags.putObject("items").put("type", "string");
        property(schema, "graph_id", "string", GRAPH_ID_DESCRIPTION);
        required(schema, "title", "content");
        return schema;
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.MODIFIES_GRAPH;
    }

    @Override
    public ToolPolicy defaultPolicy() {
        return ToolPolicy.AUTO;
    }

    @Override
    public String describeCall(final JsonNode args) {
        return "Add note \"" + text(args, "title", "") + "\"";
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) throws InterruptedException, PluginException {
        final Graph graph = GraphSupport.requireGraph(context, args);
        final List<String> tags = new ArrayList<>();
        tags.add(AI_TAG);
        if (args.has("tags") && args.get("tags").isArray()) {
            args.get("tags").forEach(tag -> {
                if (!tag.asText().isBlank() && !tags.contains(tag.asText())) {
                    tags.add(tag.asText());
                }
            });
        }
        final String title = text(args, "title", "AI Assistant note");
        NotesViewApi.addNote(graph, title, text(args, "content", ""), true, tags);
        return ToolResult.ok("Note \"" + title + "\" added to the Notes View.", "Note added: " + title);
    }
}
