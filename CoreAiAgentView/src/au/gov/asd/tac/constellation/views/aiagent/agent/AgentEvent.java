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
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import au.gov.asd.tac.constellation.views.aiagent.llm.ToolCall;

/**
 * Events published by an {@link AgentSession} for the user interface to
 * render. Events are published on the agent's executor thread or a provider
 * thread, never on the Swing or JavaFX thread.
 *
 * @author doubtsec
 */
public sealed interface AgentEvent {

    /**
     * A user message has been accepted and the agent is starting work.
     *
     * @param turnId the turn id.
     * @param userText the user's message.
     */
    record TurnStarted(String turnId, String userText) implements AgentEvent {
    }

    /**
     * The agent is waiting for the model.
     *
     * @param messageId the id of the assistant message that will follow.
     * @param step the step number, starting at 1.
     */
    record ModelCallStarted(String messageId, int step) implements AgentEvent {
    }

    /**
     * More response text has arrived.
     *
     * @param messageId the assistant message id.
     * @param delta the new text.
     */
    record TextDelta(String messageId, String delta) implements AgentEvent {
    }

    /**
     * More reasoning text has arrived.
     *
     * @param messageId the assistant message id.
     * @param delta the new text.
     */
    record ReasoningDelta(String messageId, String delta) implements AgentEvent {
    }

    /**
     * Part of a tool call has been streamed.
     *
     * @param messageId the assistant message id.
     * @param toolName the tool name, may be null.
     */
    record ToolCallStreaming(String messageId, String toolName) implements AgentEvent {
    }

    /**
     * An assistant message is complete.
     *
     * @param messageId the assistant message id.
     * @param text the full text, may be empty.
     * @param reasoning the full reasoning text, may be null.
     */
    record AssistantMessageFinished(String messageId, String text, String reasoning) implements AgentEvent {
    }

    /**
     * The model has asked for a tool call.
     *
     * @param call the call.
     * @param description a plain-English description of the call.
     * @param risk the tool's risk.
     * @param policy the policy that applies.
     * @param awaitingApproval true if the agent is waiting for
     * {@link AgentSession#decide}.
     * @param prettyArguments the arguments, pretty printed and redacted.
     * @param graphName the active graph when the call was proposed, may be
     * null.
     */
    record ToolCallProposed(ToolCall call, String description, ToolRisk risk, ToolPolicy policy,
            boolean awaitingApproval, String prettyArguments, String graphName) implements AgentEvent {
    }

    /**
     * A tool call has started running.
     *
     * @param call the call (with edited arguments, if any).
     */
    record ToolCallStarted(ToolCall call) implements AgentEvent {
    }

    /**
     * A tool reported progress.
     *
     * @param call the call.
     * @param message the progress message.
     */
    record ToolCallProgress(ToolCall call, String message) implements AgentEvent {
    }

    /**
     * A tool call has finished.
     *
     * @param call the call.
     * @param result the result.
     * @param durationMillis how long it took.
     */
    record ToolCallFinished(ToolCall call, ToolResult result, long durationMillis) implements AgentEvent {
    }

    /**
     * A tool call was denied or couldn't run.
     *
     * @param call the call.
     * @param reason why.
     */
    record ToolCallRejected(ToolCall call, String reason) implements AgentEvent {
    }

    /**
     * Token usage for one model call.
     *
     * @param inputTokens input tokens, or -1 if unknown.
     * @param outputTokens output tokens, or -1 if unknown.
     */
    record Usage(int inputTokens, int outputTokens) implements AgentEvent {
    }

    /**
     * Something the user should know that isn't part of the conversation.
     *
     * @param message the notice.
     */
    record Notice(String message) implements AgentEvent {
    }

    /**
     * Something went wrong.
     *
     * @param error the error.
     */
    record Error(Throwable error) implements AgentEvent {
    }

    /**
     * The turn has ended.
     *
     * @param reason why.
     */
    record TurnFinished(FinishReason reason) implements AgentEvent {
    }
}
