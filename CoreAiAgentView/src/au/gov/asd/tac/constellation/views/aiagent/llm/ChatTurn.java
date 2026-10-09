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
 * One entry in the conversation history sent to the model.
 *
 * @author doubtsec
 */
public sealed interface ChatTurn {

    /**
     * System instructions.
     *
     * @param text the instructions.
     */
    record Instructions(String text) implements ChatTurn {
    }

    /**
     * A message from the user.
     *
     * @param text the message.
     */
    record User(String text) implements ChatTurn {
    }

    /**
     * A response from the model.
     *
     * @param text the response text, may be empty.
     * @param toolCalls the tool calls the model asked for, may be empty.
     */
    record Assistant(String text, List<ToolCall> toolCalls) implements ChatTurn {

        public Assistant {
            toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        }
    }

    /**
     * The result of a tool call.
     *
     * @param callId the id of the call this is the result of.
     * @param toolName the tool name.
     * @param text the result text.
     */
    record ToolResult(String callId, String toolName, String text) implements ChatTurn {
    }
}
