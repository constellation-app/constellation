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
package au.gov.asd.tac.constellation.views.aiagent.api;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A capability the AI agent can use. Register implementations with
 * {@code @ServiceProvider(service = AgentTool.class)} so that other modules can
 * contribute tools.
 * <p>
 * Tools run on the agent's executor thread, never on the Swing or JavaFX
 * thread. Tools that change the graph should do so through a plugin so that
 * the change is undoable and recorded in the Notes View.
 *
 * @author doubtsec
 */
public interface AgentTool {

    /**
     * @return the unique, snake_case name of the tool (at most 64 characters).
     */
    String name();

    /**
     * @return a model-facing description that says when to use the tool.
     */
    String description();

    /**
     * @return a JSON Schema (type object) describing the tool's arguments.
     */
    JsonNode inputSchema();

    /**
     * @return how risky the tool is.
     */
    ToolRisk risk();

    /**
     * @return the policy to use unless the user overrides it.
     */
    default ToolPolicy defaultPolicy() {
        return risk().getDefaultPolicy();
    }

    /**
     * Tidy up arguments before they are validated, for example by accepting
     * common alternative names for a property. Models, especially small
     * ones, don't always use the exact property names in the schema.
     *
     * @param args the arguments as the model sent them.
     * @return the arguments to validate and run with.
     */
    default JsonNode normaliseArguments(final JsonNode args) {
        return args;
    }

    /**
     * Check arguments that are valid against the schema but may still be
     * wrong, e.g. a plugin name that doesn't exist. Problems found here are
     * returned to the model without asking the user to approve a call that
     * can't work.
     *
     * @param args the normalised arguments.
     * @return a description of the problem, or null if the arguments look
     * usable.
     */
    default String checkArguments(final JsonNode args) {
        return null;
    }

    /**
     * A short, plain-English description of what a call with the given
     * arguments will do, for display on approval cards.
     *
     * @param args the call arguments.
     * @return the description, or null to use the tool name.
     */
    default String describeCall(final JsonNode args) {
        return null;
    }

    /**
     * Run the tool.
     *
     * @param args the call arguments, already parsed.
     * @param context the context to run in.
     * @return the result.
     * @throws Exception if the tool fails; the message is returned to the
     * model.
     */
    ToolResult execute(JsonNode args, ToolContext context) throws Exception;
}
