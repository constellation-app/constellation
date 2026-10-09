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
package au.gov.asd.tac.constellation.views.aiagent.llm.langchain4j;

import au.gov.asd.tac.constellation.utilities.https.ConstellationHttpClients;
import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import au.gov.asd.tac.constellation.views.aiagent.llm.ApiMode;
import au.gov.asd.tac.constellation.views.aiagent.llm.ChatTurn;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointConfig;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointKind;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmProvider;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmRequest;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmResponse;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmStream;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmStreamListener;
import au.gov.asd.tac.constellation.views.aiagent.llm.ModelInfo;
import au.gov.asd.tac.constellation.views.aiagent.llm.ToolCall;
import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.PartialToolCallContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.model.openai.OpenAiResponsesStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.TokenUsage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An {@link LlmProvider} built on LangChain4j's low-level streaming chat
 * models. This package is the only place that imports {@code dev.langchain4j}.
 * <p>
 * Every HTTP client is built from
 * {@link ConstellationHttpClients#newBuilder(boolean)} so that requests use the
 * current Constellation security context (client certificate) and proxy
 * settings.
 *
 * @author doubtsec
 */
public final class LangChain4jProvider implements LlmProvider {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);
    // reasoning models can be silent for a long time before the first token
    static final Duration READ_TIMEOUT = Duration.ofMinutes(10);
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    // OpenAI-compatible servers such as Ollama don't need a key, but the client requires one
    private static final String PLACEHOLDER_API_KEY = "not-needed";

    private final EndpointConfig endpoint;
    private final String apiKey;
    private final Map<String, StreamingChatModel> models = new ConcurrentHashMap<>();
    private final Map<String, OptionalInt> contextWindows = new ConcurrentHashMap<>();

    public LangChain4jProvider(final EndpointConfig endpoint, final String apiKey) {
        this.endpoint = Objects.requireNonNull(endpoint);
        this.apiKey = apiKey;
    }

    @Override
    public String id() {
        return switch (endpoint.kind()) {
            case OPENAI ->
                "openai";
            case AZURE_OPENAI ->
                "azure:" + endpoint.id();
            case ANTHROPIC ->
                "anthropic:" + endpoint.id();
            case OPENAI_COMPATIBLE ->
                "compatible:" + endpoint.id();
        };
    }

    /**
     * Build the HTTP client builder used by every LangChain4j model this
     * provider creates.
     *
     * @return the builder.
     */
    JdkHttpClientBuilder httpClientBuilder() {
        return JdkHttpClient.builder()
                .httpClientBuilder(jdkClientBuilder())
                .connectTimeout(CONNECT_TIMEOUT)
                .readTimeout(READ_TIMEOUT);
    }

    private HttpClient.Builder jdkClientBuilder() {
        final HttpClient.Builder builder = ConstellationHttpClients.newBuilder(endpoint.requireSecurityContext());
        if (endpoint.forceHttp11()) {
            builder.version(HttpClient.Version.HTTP_1_1);
        }
        return builder;
    }

    private String effectiveApiKey() {
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        if (endpoint.kind() == EndpointKind.OPENAI_COMPATIBLE) {
            return PLACEHOLDER_API_KEY;
        }
        throw new IllegalStateException("No API key is configured for " + endpoint.name()
                + ". Set one in Tools > Options > Constellation > Assistant.");
    }

    private Map<String, String> headers() {
        final Map<String, String> headers = new HashMap<>(endpoint.extraHeaders());
        if (endpoint.kind() == EndpointKind.AZURE_OPENAI && apiKey != null && !apiKey.isBlank()) {
            headers.putIfAbsent("api-key", apiKey);
        }
        return headers;
    }

    private StreamingChatModel model(final LlmRequest request) {
        final String key = String.join("|", request.model(), String.valueOf(request.temperature()),
                String.valueOf(request.reasoningEffort()), String.valueOf(request.maxOutputTokens()));
        return models.computeIfAbsent(key, k -> buildModel(request));
    }

    private StreamingChatModel buildModel(final LlmRequest request) {
        final String effort = request.reasoningEffort() == null || request.reasoningEffort().isBlank() ? null : request.reasoningEffort();
        if (endpoint.kind() == EndpointKind.ANTHROPIC) {
            return AnthropicStreamingChatModel.builder()
                    .httpClientBuilder(httpClientBuilder())
                    .baseUrl(endpoint.baseUrl())
                    .apiKey(effectiveApiKey())
                    .version(ANTHROPIC_VERSION)
                    .modelName(request.model())
                    .temperature(request.temperature())
                    .maxTokens(request.maxOutputTokens() == null ? 8192 : request.maxOutputTokens())
                    .timeout(READ_TIMEOUT)
                    .build();
        }
        if (endpoint.apiMode() == ApiMode.RESPONSES) {
            return OpenAiResponsesStreamingChatModel.builder()
                    .httpClientBuilder(httpClientBuilder())
                    .baseUrl(endpoint.baseUrl())
                    .apiKey(effectiveApiKey())
                    .customHeaders(headers())
                    .modelName(request.model())
                    .temperature(request.temperature())
                    .maxOutputTokens(request.maxOutputTokens())
                    .reasoningEffort(effort)
                    .store(false)
                    .parallelToolCalls(false)
                    .build();
        }
        return OpenAiStreamingChatModel.builder()
                .httpClientBuilder(httpClientBuilder())
                .baseUrl(endpoint.baseUrl())
                .apiKey(effectiveApiKey())
                .customHeaders(headers())
                .modelName(request.model())
                .temperature(request.temperature())
                .maxCompletionTokens(request.maxOutputTokens())
                .reasoningEffort(effort)
                .returnThinking(true)
                .parallelToolCalls(false)
                .timeout(READ_TIMEOUT)
                .build();
    }

    @Override
    public LlmStream stream(final LlmRequest request, final LlmStreamListener listener) {
        final HandleStream stream = new HandleStream();
        final ChatRequest.Builder builder = ChatRequest.builder().messages(toMessages(request.history()));
        if (!request.tools().isEmpty()) {
            builder.toolSpecifications(ToolSpecAdapter.toToolSpecifications(request.tools()));
        }
        final ChatRequest chatRequest = builder.build();

        try {
            model(request).chat(chatRequest, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(final PartialResponse partialResponse, final PartialResponseContext context) {
                    if (stream.attach(context.streamingHandle())) {
                        listener.onTextDelta(partialResponse.text());
                    }
                }

                @Override
                public void onPartialThinking(final PartialThinking partialThinking, final PartialThinkingContext context) {
                    if (stream.attach(context.streamingHandle())) {
                        listener.onReasoningDelta(partialThinking.text());
                    }
                }

                @Override
                public void onPartialToolCall(final PartialToolCall partialToolCall, final PartialToolCallContext context) {
                    if (stream.attach(context.streamingHandle())) {
                        listener.onToolCallDelta(partialToolCall.index(), partialToolCall.id(), partialToolCall.name(), partialToolCall.partialArguments());
                    }
                }

                @Override
                public void onCompleteResponse(final ChatResponse response) {
                    if (!stream.isCancelled()) {
                        listener.onComplete(toResponse(response));
                    }
                }

                @Override
                public void onError(final Throwable error) {
                    if (!stream.isCancelled()) {
                        listener.onError(error);
                    }
                }
            });
        } catch (final RuntimeException ex) {
            listener.onError(ex);
        }
        return stream;
    }

    @Override
    public List<ModelInfo> listModels() throws IOException {
        final String base = endpoint.baseUrl().endsWith("/") ? endpoint.baseUrl() : endpoint.baseUrl() + "/";
        final HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + "models"))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET();
        headers().forEach(request::header);
        if (endpoint.kind() == EndpointKind.ANTHROPIC) {
            request.header("x-api-key", effectiveApiKey());
            request.header("anthropic-version", ANTHROPIC_VERSION);
        } else if (endpoint.kind() != EndpointKind.AZURE_OPENAI) {
            request.header("Authorization", "Bearer " + effectiveApiKey());
        }

        final HttpClient client = jdkClientBuilder().build();
        final HttpResponse<String> response;
        try {
            response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while listing models", ex);
        }
        if (response.statusCode() / 100 != 2) {
            throw new IOException(String.format("Listing models failed with HTTP %d: %s", response.statusCode(),
                    abbreviate(response.body(), 300)));
        }
        return parseModels(response.body());
    }

    /**
     * Ollama reports the context length of loaded models from its native
     * {@code /api/ps} endpoint, next to the OpenAI-compatible {@code /v1}.
     */
    @Override
    public OptionalInt contextWindow(final String model) {
        if (endpoint.kind() != EndpointKind.OPENAI_COMPATIBLE || model == null) {
            return OptionalInt.empty();
        }
        // providers are created per message, so caching here only saves repeated lookups within a turn
        return contextWindows.computeIfAbsent(model, this::fetchContextWindow);
    }

    private OptionalInt fetchContextWindow(final String model) {
        final String base = endpoint.baseUrl().replaceAll("/+$", "").replaceAll("/v1$", "");
        try {
            final HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + "/api/ps"))
                    .timeout(Duration.ofSeconds(5))
                    .GET();
            headers().forEach(request::header);
            final HttpResponse<String> response = jdkClientBuilder().build().send(request.build(), HttpResponse.BodyHandlers.ofString());
            return response.statusCode() / 100 == 2 ? parseContextWindow(response.body(), model) : OptionalInt.empty();
        } catch (final IOException | IllegalArgumentException | IllegalStateException ex) {
            return OptionalInt.empty();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return OptionalInt.empty();
        }
    }

    static OptionalInt parseContextWindow(final String body, final String model) throws IOException {
        final JsonNode models = JsonFactoryUtilities.getObjectMapper().readTree(body).path("models");
        for (final JsonNode node : models) {
            if ((model.equals(node.path("name").asText()) || model.equals(node.path("model").asText())) && node.path("context_length").canConvertToInt()
                    && node.path("context_length").asInt() > 0) {
                return OptionalInt.of(node.path("context_length").asInt());
            }
        }
        return OptionalInt.empty();
    }

    static List<ModelInfo> parseModels(final String body) throws IOException {
        final JsonNode root = JsonFactoryUtilities.getObjectMapper().readTree(body);
        final JsonNode data = root.has("data") ? root.get("data") : root.get("models");
        final List<ModelInfo> result = new ArrayList<>();
        if (data != null && data.isArray()) {
            data.forEach(node -> {
                final String modelId = node.hasNonNull("id") ? node.get("id").asText() : node.path("name").asText(null);
                if (modelId != null && !modelId.isBlank()) {
                    final String displayName = node.hasNonNull("display_name") ? node.get("display_name").asText() : modelId;
                    result.add(new ModelInfo(modelId, displayName));
                }
            });
        }
        result.sort((a, b) -> a.id().compareToIgnoreCase(b.id()));
        return result;
    }

    static List<ChatMessage> toMessages(final List<ChatTurn> history) {
        final List<ChatMessage> messages = new ArrayList<>(history.size());
        for (final ChatTurn turn : history) {
            switch (turn) {
                case ChatTurn.Instructions instructions ->
                    messages.add(SystemMessage.from(instructions.text()));
                case ChatTurn.User user ->
                    messages.add(UserMessage.from(user.text()));
                case ChatTurn.Assistant assistant -> {
                    final List<ToolExecutionRequest> requests = new ArrayList<>();
                    assistant.toolCalls().forEach(call -> requests.add(ToolExecutionRequest.builder()
                            .id(call.id())
                            .name(call.name())
                            .arguments(call.arguments() == null || call.arguments().isBlank() ? "{}" : call.arguments())
                            .build()));
                    final String text = assistant.text() == null || assistant.text().isBlank() ? null : assistant.text();
                    if (requests.isEmpty()) {
                        messages.add(AiMessage.from(text == null ? "" : text));
                    } else {
                        messages.add(AiMessage.builder().text(text).toolExecutionRequests(requests).build());
                    }
                }
                case ChatTurn.ToolResult result ->
                    messages.add(ToolExecutionResultMessage.from(result.callId(), result.toolName(), result.text()));
            }
        }
        return messages;
    }

    static LlmResponse toResponse(final ChatResponse response) {
        final AiMessage message = response.aiMessage();
        final List<ToolCall> calls = new ArrayList<>();
        if (message != null && message.hasToolExecutionRequests()) {
            int index = 0;
            for (final ToolExecutionRequest request : message.toolExecutionRequests()) {
                final String callId = request.id() == null || request.id().isBlank() ? "call_" + index : request.id();
                calls.add(new ToolCall(callId, request.name(), request.arguments()));
                index++;
            }
        }
        final TokenUsage usage = response.tokenUsage();
        final int in = usage != null && usage.inputTokenCount() != null ? usage.inputTokenCount() : -1;
        final int out = usage != null && usage.outputTokenCount() != null ? usage.outputTokenCount() : -1;
        return new LlmResponse(message == null ? "" : message.text(), message == null ? null : message.thinking(), calls,
                in, out, response.finishReason() == null ? null : response.finishReason().name());
    }

    private static String abbreviate(final String text, final int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    /**
     * Tracks the LangChain4j streaming handle, which is only available from
     * the first partial callback.
     */
    private static final class HandleStream implements LlmStream {

        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicReference<StreamingHandle> handle = new AtomicReference<>();

        /**
         * Remember the handle and cancel it if the stream was cancelled before
         * the handle was known.
         *
         * @return true if callbacks should continue to be delivered.
         */
        boolean attach(final StreamingHandle streamingHandle) {
            if (streamingHandle != null) {
                handle.compareAndSet(null, streamingHandle);
            }
            if (cancelled.get()) {
                cancelHandle();
                return false;
            }
            return true;
        }

        @Override
        public void cancel() {
            cancelled.set(true);
            cancelHandle();
        }

        private void cancelHandle() {
            final StreamingHandle h = handle.get();
            if (h != null && !h.isCancelled()) {
                h.cancel();
            }
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }
    }
}
