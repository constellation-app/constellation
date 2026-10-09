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
 * A complete response from a model.
 *
 * @param text the response text, may be empty.
 * @param reasoning the reasoning text where the provider exposes it, may be
 * null.
 * @param toolCalls the tool calls requested, may be empty.
 * @param inputTokens the number of input tokens used, or -1 if unknown.
 * @param outputTokens the number of output tokens used, or -1 if unknown.
 * @param finishReason the provider's finish reason, may be null.
 *
 * @author doubtsec
 */
public record LlmResponse(String text, String reasoning, List<ToolCall> toolCalls,
        int inputTokens, int outputTokens, String finishReason) {

    public LlmResponse {
        text = text == null ? "" : text;
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }
}
