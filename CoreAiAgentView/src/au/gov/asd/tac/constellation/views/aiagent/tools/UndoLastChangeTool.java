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
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.openide.awt.UndoRedo;
import org.openide.util.lookup.ServiceProvider;

/**
 * Undoes the most recent change to the graph.
 *
 * @author doubtsec
 */
@ServiceProvider(service = AgentTool.class, position = 700)
public class UndoLastChangeTool extends AbstractAgentTool {

    @Override
    public String name() {
        return "undo_last_change";
    }

    @Override
    public String description() {
        return "Undo the most recent change to the graph (the same as Edit > Undo), whether it was made by you or the user.";
    }

    @Override
    protected ObjectNode buildSchema() {
        final ObjectNode schema = objectSchema();
        property(schema, "graph_id", "string", GRAPH_ID_DESCRIPTION);
        return schema;
    }

    @Override
    public ToolRisk risk() {
        return ToolRisk.MODIFIES_GRAPH;
    }

    @Override
    public String describeCall(final JsonNode args) {
        return "Undo the last change to the graph";
    }

    @Override
    public ToolResult execute(final JsonNode args, final ToolContext context) throws Exception {
        final Graph graph = GraphSupport.requireGraph(context, args);
        final GraphNode node = GraphNode.getGraphNode(graph);
        if (node == null) {
            return ToolResult.error("This graph has no undo history.");
        }
        final AtomicReference<String> undone = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            final UndoRedo.Manager manager = node.getUndoRedoManager();
            if (manager != null && manager.canUndo()) {
                // the presentation name is "Undo <edit>"
                undone.set(manager.getUndoPresentationName().replaceFirst("^(?i)undo\\s+", ""));
                manager.undo();
            }
        });
        if (undone.get() == null) {
            return ToolResult.error("There is nothing to undo.");
        }
        return ToolResult.ok("Undid: " + undone.get(), "Undid " + undone.get());
    }
}
