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
package au.gov.asd.tac.constellation.views.aiagent.preferences;

/**
 * Preference keys for the AI agent, stored with
 * {@code NbPreferences.forModule(AgentPreferenceKeys.class)}.
 * <p>
 * API keys are never stored in preferences; see {@link ApiKeyStore}.
 *
 * @author doubtsec
 */
public final class AgentPreferenceKeys {

    /**
     * JSON array of endpoint configurations.
     */
    public static final String ENDPOINTS = "aiagent.endpoints";
    /**
     * The id of the endpoint selected by default.
     */
    public static final String DEFAULT_ENDPOINT = "aiagent.defaultEndpoint";
    /**
     * Maximum number of model calls per user message.
     */
    public static final String MAX_STEPS = "aiagent.maxSteps";
    public static final int MAX_STEPS_DEFAULT = 20;
    /**
     * Maximum number of characters returned to the model from a tool call.
     */
    public static final String MAX_TOOL_RESULT_CHARS = "aiagent.maxToolResultChars";
    public static final int MAX_TOOL_RESULT_CHARS_DEFAULT = 12000;
    /**
     * Include counts of selected elements in the context sent with each
     * message.
     */
    public static final String INCLUDE_SELECTION = "aiagent.includeSelection";
    public static final boolean INCLUDE_SELECTION_DEFAULT = true;
    /**
     * A replacement for the built in system prompt; empty for the default.
     */
    public static final String SYSTEM_PROMPT_OVERRIDE = "aiagent.systemPrompt";
    /**
     * JSON object mapping tool names to policies (AUTO, ASK, NEVER).
     */
    public static final String TOOL_POLICIES = "aiagent.toolPolicies";
    /**
     * Default sampling temperature; empty for the model default.
     */
    public static final String TEMPERATURE = "aiagent.temperature";
    /**
     * Default reasoning effort; empty for the model default.
     */
    public static final String REASONING_EFFORT = "aiagent.reasoningEffort";
    /**
     * Log redacted request summaries for debugging.
     */
    public static final String DEBUG_LOGGING = "aiagent.debugLogging";
    public static final boolean DEBUG_LOGGING_DEFAULT = false;

    private AgentPreferenceKeys() {
        throw new IllegalStateException("Utility class");
    }
}
