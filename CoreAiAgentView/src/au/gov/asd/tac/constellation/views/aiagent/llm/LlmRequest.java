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
package au.gov.asd.tac.constellation.views.aiagent.llm;

import java.util.List;

/**
 * A request to stream a response from a model.
 *
 * @param model the model id.
 * @param history the conversation so far.
 * @param tools the tools the model may call.
 * @param temperature the sampling temperature, or null for the model default.
 * @param reasoningEffort the reasoning effort (e.g. "low", "medium", "high",
 * "none"), or null for the model default.
 * @param maxOutputTokens the maximum number of output tokens, or null.
 *
 * @author constellation
 */
public record LlmRequest(String model, List<ChatTurn> history, List<ToolSpec> tools,
        Double temperature, String reasoningEffort, Integer maxOutputTokens) {

    public LlmRequest {
        history = List.copyOf(history);
        tools = tools == null ? List.of() : List.copyOf(tools);
    }
}
