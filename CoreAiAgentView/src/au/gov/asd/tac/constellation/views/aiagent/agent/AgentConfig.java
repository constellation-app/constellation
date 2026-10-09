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
package au.gov.asd.tac.constellation.views.aiagent.agent;

import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import java.util.Map;

/**
 * Settings for an {@link AgentSession}.
 *
 * @param model the model id.
 * @param systemPrompt the system prompt.
 * @param maxSteps the maximum number of model calls per user message.
 * @param maxToolResultChars the maximum length of a tool result sent to the
 * model.
 * @param temperature the temperature, or null for the model default.
 * @param reasoningEffort the reasoning effort, or null/empty for the model
 * default.
 * @param policyOverrides per-tool policy overrides.
 * @param maxHistoryChars compact old tool results when the history grows
 * beyond this many characters.
 *
 * @author constellation
 */
public record AgentConfig(String model, String systemPrompt, int maxSteps, int maxToolResultChars,
        Double temperature, String reasoningEffort, Map<String, ToolPolicy> policyOverrides, int maxHistoryChars) {

    public static final int DEFAULT_MAX_HISTORY_CHARS = 120_000;

    public AgentConfig {
        policyOverrides = policyOverrides == null ? Map.of() : Map.copyOf(policyOverrides);
        maxSteps = Math.max(1, maxSteps);
        maxToolResultChars = Math.max(500, maxToolResultChars);
        maxHistoryChars = maxHistoryChars <= 0 ? DEFAULT_MAX_HISTORY_CHARS : maxHistoryChars;
    }
}
