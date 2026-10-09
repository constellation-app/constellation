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

import au.gov.asd.tac.constellation.views.aiagent.llm.LlmProvider;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmRequest;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmResponse;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmStream;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmStreamListener;
import au.gov.asd.tac.constellation.views.aiagent.llm.ModelInfo;
import au.gov.asd.tac.constellation.views.aiagent.llm.ToolCall;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A provider that plays back scripted responses, for testing the agent loop
 * without a network.
 *
 * @author constellation
 */
public final class FakeLlmProvider implements LlmProvider {

    /**
     * One scripted step.
     */
    public sealed interface Step {
    }

    /**
     * Stream text (in chunks) then complete with the given tool calls.
     */
    public record Respond(String text, List<ToolCall> toolCalls) implements Step {
    }

    /**
     * Fail with an error.
     */
    public record Fail(String message) implements Step {
    }

    /**
     * Never respond (until cancelled).
     */
    public record Hang() implements Step {
    }

    private final Deque<Step> script = new ArrayDeque<>();
    private final List<LlmRequest> requests = Collections.synchronizedList(new ArrayList<>());
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private OptionalInt contextWindow = OptionalInt.empty();

    public FakeLlmProvider withContextWindow(final int tokens) {
        contextWindow = OptionalInt.of(tokens);
        return this;
    }

    @Override
    public OptionalInt contextWindow(final String model) {
        return contextWindow;
    }

    public FakeLlmProvider then(final Step step) {
        script.add(step);
        return this;
    }

    public FakeLlmProvider thenText(final String text) {
        return then(new Respond(text, List.of()));
    }

    public FakeLlmProvider thenCall(final String id, final String name, final String arguments) {
        return then(new Respond("", List.of(new ToolCall(id, name, arguments))));
    }

    public List<LlmRequest> getRequests() {
        return requests;
    }

    public boolean wasCancelled() {
        return cancelled.get();
    }

    @Override
    public String id() {
        return "fake";
    }

    @Override
    public List<ModelInfo> listModels() {
        return List.of(new ModelInfo("fake-model", "Fake model"));
    }

    @Override
    public LlmStream stream(final LlmRequest request, final LlmStreamListener listener) {
        requests.add(request);
        final AtomicBoolean streamCancelled = new AtomicBoolean(false);
        final LlmStream stream = new LlmStream() {
            @Override
            public void cancel() {
                streamCancelled.set(true);
                cancelled.set(true);
            }

            @Override
            public boolean isCancelled() {
                return streamCancelled.get();
            }
        };
        final Step step = script.isEmpty() ? new Respond("(script exhausted)", List.of()) : script.poll();
        switch (step) {
            case Respond respond -> {
                final String text = respond.text() == null ? "" : respond.text();
                for (int i = 0; i < text.length(); i += 4) {
                    listener.onTextDelta(text.substring(i, Math.min(text.length(), i + 4)));
                }
                for (int i = 0; i < respond.toolCalls().size(); i++) {
                    final ToolCall call = respond.toolCalls().get(i);
                    listener.onToolCallDelta(i, call.id(), call.name(), call.arguments());
                }
                listener.onComplete(new LlmResponse(text, null, respond.toolCalls(), 10, 5, respond.toolCalls().isEmpty() ? "STOP" : "TOOL_EXECUTION"));
            }
            case Fail fail ->
                listener.onError(new IllegalStateException(fail.message()));
            case Hang hang -> {
                // never completes
            }
        }
        return stream;
    }
}
