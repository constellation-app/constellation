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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.llm.ChatTurn;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmProvider;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmRequest;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmResponse;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmStream;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmStreamListener;
import au.gov.asd.tac.constellation.views.aiagent.llm.ToolCall;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A headless, UI-agnostic conversation with a model that can call tools.
 * <p>
 * The agent loop runs on a dedicated executor thread. Each user message
 * starts a turn: the model is called, any tool calls it asks for are approved
 * (or not) and run, their results are added to the history and the model is
 * called again, until the model answers without asking for tools, the step
 * limit is reached or the user stops the run. Progress is published as
 * {@link AgentEvent}s.
 * <p>
 * Tool denials and errors are returned to the model as tool results rather
 * than thrown, so the model can change course.
 *
 * @author doubtsec
 */
public final class AgentSession {

    private static final Logger LOGGER = Logger.getLogger(AgentSession.class.getName());
    private static final ObjectMapper MAPPER = JsonFactoryUtilities.getObjectMapper();
    static final String CONTINUE_NUDGE = "[Constellation] You described a next step but didn't call a tool. "
            + "If you meant to, call the tool now. Otherwise give your final answer.";
    static final String EMPTY_NUDGE = "[Constellation] Your last response was empty. If you tried to call a tool, the call could not be "
            + "read: call it again with valid JSON arguments. Otherwise give your final answer.";
    private static final java.util.regex.Pattern ANNOUNCEMENT = java.util.regex.Pattern.compile(
            "\\b(?:i will now|i'll now|attempting to|"
            + "(?:i will|i'll|let me|i am going to|i'm going to|now i will|next,? i will|i need to|i should)"
            + "(?:\\s+\\w+){0,3}?\\s+(?:run|call|use|select|query|calculate|compute|execute|apply|add|search|check|arrange|create|describe|look|find|retrieve|fetch|get))\\b");

    private final ToolRegistry registry;
    private final AgentEnvironment environment;
    private final Consumer<AgentEvent> listener;
    private final ExecutorService executor;

    private volatile LlmProvider provider;
    private volatile AgentConfig config;
    private volatile boolean debugLogging;
    private volatile boolean contextWarningShown;
    static final double CONTEXT_FULL_FRACTION = 0.95;

    private final List<ChatTurn> history = new ArrayList<>();
    private final Set<String> alwaysAllowed = ConcurrentHashMap.newKeySet();
    private final Set<String> seenCallIds = ConcurrentHashMap.newKeySet();
    private final Map<String, CompletableFuture<Decision>> pendingDecisions = new ConcurrentHashMap<>();
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicInteger messageCounter = new AtomicInteger();

    private volatile LlmStream currentStream;
    private volatile CompletableFuture<LlmResponse> currentResponse;
    private volatile Thread workerThread;
    private volatile boolean executingTool;
    private Future<?> currentTurn;

    public AgentSession(final LlmProvider provider, final AgentConfig config, final ToolRegistry registry,
            final AgentEnvironment environment, final Consumer<AgentEvent> listener) {
        this.provider = provider;
        this.config = config;
        this.registry = registry;
        this.environment = environment;
        this.listener = listener;
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "AI Agent Session");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Change the provider; takes effect from the next model call.
     *
     * @param provider the provider.
     */
    public void setProvider(final LlmProvider provider) {
        this.provider = provider;
    }

    public LlmProvider getProvider() {
        return provider;
    }

    /**
     * Change the configuration; takes effect from the next turn.
     *
     * @param config the configuration.
     */
    public void setConfig(final AgentConfig config) {
        this.config = config;
    }

    public AgentConfig getConfig() {
        return config;
    }

    public void setDebugLogging(final boolean debugLogging) {
        this.debugLogging = debugLogging;
    }

    public ToolRegistry getRegistry() {
        return registry;
    }

    /**
     * @return true while a turn is running.
     */
    public synchronized boolean isRunning() {
        return currentTurn != null && !currentTurn.isDone();
    }

    /**
     * Send a user message and start a turn.
     *
     * @param userText the message.
     * @return the running turn.
     * @throws IllegalStateException if a turn is already running.
     */
    public synchronized Future<?> send(final String userText) {
        if (isRunning()) {
            throw new IllegalStateException("The assistant is still working on the previous message");
        }
        cancelled.set(false);
        currentTurn = executor.submit(() -> runTurn(userText));
        return currentTurn;
    }

    /**
     * Stop the current turn, wherever it is: streaming from the model, waiting
     * for approval, or running a tool.
     */
    public void cancel() {
        cancelled.set(true);
        final LlmStream stream = currentStream;
        if (stream != null) {
            stream.cancel();
        }
        final CompletableFuture<LlmResponse> response = currentResponse;
        if (response != null) {
            response.cancel(true);
        }
        pendingDecisions.values().forEach(decision -> decision.complete(new Decision.Stop()));
        final Thread worker = workerThread;
        if (executingTool && worker != null) {
            // plugins respond to interruption
            worker.interrupt();
        }
    }

    /**
     * Answer a pending approval request.
     *
     * @param callId the id of the tool call.
     * @param decision the decision.
     * @return true if a call with that id was waiting for a decision.
     */
    public boolean decide(final String callId, final Decision decision) {
        final CompletableFuture<Decision> pending = pendingDecisions.get(callId);
        return pending != null && pending.complete(decision);
    }

    /**
     * Stop any running turn and forget the conversation.
     *
     * @return a future that completes when the session is ready for a new
     * conversation.
     */
    public Future<?> newChat() {
        cancel();
        return executor.submit(() -> {
            synchronized (history) {
                history.clear();
            }
            alwaysAllowed.clear();
            seenCallIds.clear();
            contextWarningShown = false;
        });
    }

    /**
     * @return a copy of the conversation history.
     */
    public List<ChatTurn> getHistory() {
        synchronized (history) {
            return List.copyOf(history);
        }
    }

    /**
     * @param toolName a tool name.
     * @return true if the user chose "Always allow" for the tool this session.
     */
    public boolean isAlwaysAllowed(final String toolName) {
        return alwaysAllowed.contains(toolName);
    }

    /**
     * Stop the session and release its thread.
     */
    public void shutdown() {
        cancel();
        executor.shutdownNow();
    }

    /**
     * The policy that applies to a tool in this session.
     *
     * @param tool the tool.
     * @return the policy.
     */
    public ToolPolicy policyFor(final AgentTool tool) {
        final ToolPolicy policy = config.policyOverrides().getOrDefault(tool.name(), tool.defaultPolicy());
        if (policy == ToolPolicy.ASK && alwaysAllowed.contains(tool.name())) {
            return ToolPolicy.AUTO;
        }
        return policy;
    }

    private void emit(final AgentEvent event) {
        try {
            listener.accept(event);
        } catch (final RuntimeException ex) {
            LOGGER.log(Level.WARNING, "AI agent event listener failed", ex);
        }
    }

    private void addHistory(final ChatTurn turn) {
        synchronized (history) {
            history.add(turn);
        }
    }

    private void runTurn(final String userText) {
        workerThread = Thread.currentThread();
        emit(new AgentEvent.TurnStarted(UUID.randomUUID().toString(), userText));
        FinishReason reason;
        try {
            final AgentConfig turnConfig = config;
            synchronized (history) {
                if (history.isEmpty() || !(history.get(0) instanceof ChatTurn.Instructions)) {
                    history.add(0, new ChatTurn.Instructions(turnConfig.systemPrompt()));
                }
            }
            String context = null;
            try {
                context = environment.describeContext();
            } catch (final RuntimeException ex) {
                LOGGER.log(Level.WARNING, "Couldn't describe the graph context", ex);
            }
            addHistory(new ChatTurn.User(context == null || context.isBlank() ? userText : context + "\n\n" + userText));
            reason = loop(turnConfig);
        } catch (final InterruptedException | CancellationException ex) {
            reason = FinishReason.CANCELLED;
        } catch (final Exception ex) {
            if (cancelled.get()) {
                reason = FinishReason.CANCELLED;
            } else {
                LOGGER.log(Level.INFO, "AI agent turn failed", ex);
                emit(new AgentEvent.Error(ex));
                reason = FinishReason.ERROR;
            }
        } finally {
            // clear any interrupt left over from cancelling a tool
            Thread.interrupted();
            workerThread = null;
            currentStream = null;
            currentResponse = null;
        }
        emit(new AgentEvent.TurnFinished(reason));
    }

    private FinishReason loop(final AgentConfig turnConfig) throws InterruptedException, LlmCallException {
        boolean nudged = false;
        for (int step = 1; step <= turnConfig.maxSteps(); step++) {
            if (cancelled.get()) {
                return FinishReason.CANCELLED;
            }
            synchronized (history) {
                final List<ChatTurn> compacted = HistoryCompactor.compact(history, turnConfig.maxHistoryChars());
                if (compacted != history) {
                    history.clear();
                    history.addAll(compacted);
                }
            }

            final String messageId = "m" + messageCounter.incrementAndGet();
            emit(new AgentEvent.ModelCallStarted(messageId, step));
            final LlmResponse response = normaliseCallIds(callModel(turnConfig, messageId));
            emit(new AgentEvent.AssistantMessageFinished(messageId, response.text(), response.reasoning()));
            if (response.inputTokens() >= 0 || response.outputTokens() >= 0) {
                emit(new AgentEvent.Usage(response.inputTokens(), response.outputTokens()));
            }
            checkContextWindow(turnConfig, response);
            addHistory(new ChatTurn.Assistant(response.text(), response.toolCalls()));

            if (response.toolCalls().isEmpty()) {
                if (!nudged && step < turnConfig.maxSteps() && announcesAction(response.text())) {
                    // small models often say what they will do next and then stop; ask once to carry on
                    nudged = true;
                    emit(new AgentEvent.Notice("The assistant described a next step without doing it; asking it to continue."));
                    addHistory(new ChatTurn.User(CONTINUE_NUDGE));
                    continue;
                }
                if (!nudged && step < turnConfig.maxSteps() && response.text().isBlank()) {
                    // an empty response usually means the server couldn't parse a tool call the model tried to make
                    nudged = true;
                    emit(new AgentEvent.Notice("The model returned an empty response; asking it to try again."));
                    addHistory(new ChatTurn.User(EMPTY_NUDGE));
                    continue;
                }
                if (response.text().isBlank()) {
                    emit(new AgentEvent.Notice("The model returned an empty response."));
                }
                return FinishReason.STOP;
            }

            // the model acted, so it may be nudged again later in the turn (but never twice in a row)
            nudged = false;
            // run sequentially: graph tools share locks, and approvals are one at a time
            for (final ToolCall call : response.toolCalls()) {
                final String result;
                if (cancelled.get()) {
                    result = "The user stopped the run before this call ran.";
                    emit(new AgentEvent.ToolCallRejected(call, "Stopped"));
                } else {
                    result = handleCall(call, turnConfig);
                }
                // every call needs a result or the next request is invalid
                addHistory(new ChatTurn.ToolResult(call.id(), call.name(), HistoryCompactor.truncate(result, turnConfig.maxToolResultChars())));
            }
            if (cancelled.get()) {
                return FinishReason.CANCELLED;
            }
        }
        emit(new AgentEvent.Notice(String.format("Stopped after %d steps. Send another message to let the assistant continue.", turnConfig.maxSteps())));
        return FinishReason.MAX_STEPS;
    }

    /**
     * Whether a response ends by announcing an action rather than doing it,
     * e.g. "I will now run the plugin." or "Attempting to select them:".
     */
    static boolean announcesAction(final String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        final String trimmed = text.trim();
        if (trimmed.endsWith("?")) {
            // a question for the user is a legitimate place to stop
            return false;
        }
        final int start = Math.max(0, trimmed.length() - 240);
        final String tail = trimmed.substring(start).toLowerCase(java.util.Locale.ROOT);
        return trimmed.endsWith(":") || ANNOUNCEMENT.matcher(tail).find();
    }

    /**
     * Warn once if the prompt filled the server's context window: servers such
     * as Ollama then silently drop the start of the conversation, including the
     * system prompt, and the assistant stops behaving sensibly.
     */
    private void checkContextWindow(final AgentConfig turnConfig, final LlmResponse response) {
        if (contextWarningShown || response.inputTokens() <= 0) {
            return;
        }
        final java.util.OptionalInt window = provider.contextWindow(turnConfig.model());
        if (window.isPresent() && response.inputTokens() >= window.getAsInt() * CONTEXT_FULL_FRACTION) {
            contextWarningShown = true;
            emit(new AgentEvent.Notice(String.format("The model's context window (%,d tokens) is full, so the server is dropping the start "
                    + "of the conversation, including the assistant's instructions. Answers will be unreliable. Increase the context length "
                    + "(for Ollama, start it with OLLAMA_CONTEXT_LENGTH=32768 or more), or start a new chat.", window.getAsInt())));
        }
    }

    private LlmResponse normaliseCallIds(final LlmResponse response) {
        if (response.toolCalls().isEmpty()) {
            return response;
        }
        final List<ToolCall> calls = new ArrayList<>();
        for (final ToolCall call : response.toolCalls()) {
            String id = call.id();
            if (id == null || id.isBlank() || !seenCallIds.add(id)) {
                id = "call_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                seenCallIds.add(id);
            }
            calls.add(new ToolCall(id, call.name(), call.arguments()));
        }
        return new LlmResponse(response.text(), response.reasoning(), calls, response.inputTokens(), response.outputTokens(), response.finishReason());
    }

    private LlmResponse callModel(final AgentConfig turnConfig, final String messageId) throws InterruptedException, LlmCallException {
        final LlmRequest request = new LlmRequest(turnConfig.model(), getHistory(), registry.specs(this::policyFor),
                turnConfig.temperature(), turnConfig.reasoningEffort(), null);
        if (debugLogging) {
            LOGGER.log(Level.INFO, "AI agent model call: provider={0}, model={1}, messages={2}, tools={3}, chars={4}",
                    new Object[]{provider.id(), request.model(), request.history().size(), request.tools().size(), HistoryCompactor.size(request.history())});
        }
        final CompletableFuture<LlmResponse> future = new CompletableFuture<>();
        currentResponse = future;
        final Set<Integer> announcedToolCalls = ConcurrentHashMap.newKeySet();
        final LlmStream stream = provider.stream(request, new LlmStreamListener() {
            @Override
            public void onTextDelta(final String delta) {
                if (delta != null && !delta.isEmpty()) {
                    emit(new AgentEvent.TextDelta(messageId, delta));
                }
            }

            @Override
            public void onReasoningDelta(final String delta) {
                if (delta != null && !delta.isEmpty()) {
                    emit(new AgentEvent.ReasoningDelta(messageId, delta));
                }
            }

            @Override
            public void onToolCallDelta(final int index, final String id, final String name, final String argumentsDelta) {
                if (name != null && announcedToolCalls.add(index)) {
                    emit(new AgentEvent.ToolCallStreaming(messageId, name));
                }
            }

            @Override
            public void onComplete(final LlmResponse response) {
                future.complete(response);
            }

            @Override
            public void onError(final Throwable error) {
                future.completeExceptionally(error);
            }
        });
        currentStream = stream;
        if (cancelled.get()) {
            stream.cancel();
            future.cancel(true);
        }
        try {
            return future.get();
        } catch (final ExecutionException ex) {
            throw new LlmCallException(describeError(ex.getCause()), ex.getCause());
        } finally {
            currentStream = null;
            currentResponse = null;
        }
    }

    static String describeError(final Throwable error) {
        if (error == null) {
            return "The model call failed";
        }
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        final String message = error.getMessage() != null ? error.getMessage() : root.getMessage();
        if (root instanceof java.net.ConnectException) {
            return "Couldn't connect to the model endpoint. Check that it is running and the URL is correct. (" + message + ")";
        }
        return message == null ? error.getClass().getSimpleName() : message;
    }

    private String handleCall(final ToolCall proposed, final AgentConfig turnConfig) throws InterruptedException {
        ToolCall call = proposed;
        final AgentTool tool = registry.get(call.name());
        if (tool == null) {
            final String reason = "Unknown tool: " + call.name();
            emit(new AgentEvent.ToolCallRejected(call, reason));
            final List<String> names = new ArrayList<>();
            registry.getTools().stream().filter(t -> policyFor(t) != ToolPolicy.NEVER).forEach(t -> names.add(t.name()));
            return "Error: " + reason + ". The available tools are: " + String.join(", ", names) + ".";
        }
        final ToolPolicy policy = policyFor(tool);
        if (policy == ToolPolicy.NEVER) {
            emit(new AgentEvent.ToolCallRejected(call, "Disabled in options"));
            return "Error: the user has disabled the " + tool.name() + " tool.";
        }

        JsonNode args;
        try {
            args = parseArguments(call.arguments());
        } catch (final JsonProcessingException ex) {
            final String reason = "The arguments are not valid JSON: " + ex.getOriginalMessage();
            emit(new AgentEvent.ToolCallRejected(call, reason));
            return "Error: " + reason;
        }
        args = normalise(tool, args);
        List<String> problems = ArgumentValidator.validate(args, tool.inputSchema());
        if (!problems.isEmpty()) {
            final String reason = "Invalid arguments: " + String.join("; ", problems) + describeProvided(args);
            if (debugLogging) {
                LOGGER.log(Level.INFO, "AI agent rejected {0} call with arguments {1}", new Object[]{tool.name(), call.arguments()});
            }
            emit(new AgentEvent.ToolCallRejected(call, reason));
            return "Error: " + reason;
        }
        final String problem = check(tool, args);
        if (problem != null) {
            emit(new AgentEvent.ToolCallRejected(call, problem));
            return "Error: " + problem;
        }

        final Graph graph = environment.getActiveGraph();
        final boolean ask = policy == ToolPolicy.ASK;
        // register before announcing the call, so a decision can't arrive before we're waiting for it
        final CompletableFuture<Decision> pending = new CompletableFuture<>();
        if (ask) {
            pendingDecisions.put(call.id(), pending);
        }
        emit(new AgentEvent.ToolCallProposed(call, describeCall(tool, args), tool.risk(), policy, ask,
                pretty(Redactor.redact(args)), graph == null ? null : environment.graphName(graph)));

        if (ask) {
            if (cancelled.get()) {
                pending.complete(new Decision.Stop());
            }
            final Decision decision;
            try {
                decision = pending.get();
            } catch (final ExecutionException ex) {
                throw new IllegalStateException(ex.getCause());
            } finally {
                pendingDecisions.remove(call.id());
            }
            switch (decision) {
                case Decision.Stop stop -> {
                    cancelled.set(true);
                    emit(new AgentEvent.ToolCallRejected(call, "Stopped"));
                    return "The user stopped the run before this call ran.";
                }
                case Decision.Deny deny -> {
                    final String reason = deny.reason() == null || deny.reason().isBlank() ? "" : deny.reason().trim();
                    emit(new AgentEvent.ToolCallRejected(call, reason.isEmpty() ? "Denied" : "Denied: " + reason));
                    return "DENIED: the analyst did not allow this call, so it was NOT run."
                            + (reason.isEmpty() ? "" : " The analyst said: \"" + reason + "\".")
                            + " Do not make this call again. " + (reason.isEmpty() ? "Ask the analyst what they would like instead."
                            : "Follow the analyst's instruction instead.");
                }
                case Decision.AlwaysAllow always ->
                    alwaysAllowed.add(tool.name());
                case Decision.Edit edit -> {
                    try {
                        args = parseArguments(edit.arguments());
                    } catch (final JsonProcessingException ex) {
                        final String reason = "The edited arguments are not valid JSON: " + ex.getOriginalMessage();
                        emit(new AgentEvent.ToolCallRejected(call, reason));
                        return "Error: " + reason;
                    }
                    args = normalise(tool, args);
                    problems = ArgumentValidator.validate(args, tool.inputSchema());
                    if (!problems.isEmpty()) {
                        final String reason = "Invalid edited arguments: " + String.join("; ", problems);
                        emit(new AgentEvent.ToolCallRejected(call, reason));
                        return "Error: " + reason;
                    }
                    final String editProblem = check(tool, args);
                    if (editProblem != null) {
                        emit(new AgentEvent.ToolCallRejected(call, editProblem));
                        return "Error: " + editProblem;
                    }
                    call = new ToolCall(call.id(), call.name(), args.toString());
                }
                case Decision.Approve approve -> {
                    // run as proposed
                }
            }
        }
        final String result = execute(tool, call, args, turnConfig);
        return call == proposed ? result : "(The user edited the arguments to " + call.arguments() + ")\n" + result;
    }

    private String execute(final AgentTool tool, final ToolCall call, final JsonNode args, final AgentConfig turnConfig) throws InterruptedException {
        emit(new AgentEvent.ToolCallStarted(call));
        final long start = System.nanoTime();
        executingTool = true;
        ToolResult result;
        try {
            result = tool.execute(args, new SessionToolContext(call, turnConfig));
            if (result == null) {
                result = ToolResult.ok("");
            }
        } catch (final InterruptedException ex) {
            cancelled.set(true);
            result = ToolResult.error("Stopped by the user while running");
        } catch (final Exception ex) {
            LOGGER.log(Level.FINE, "AI agent tool failed", ex);
            result = ToolResult.error(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        } finally {
            executingTool = false;
        }
        if (cancelled.get()) {
            Thread.interrupted();
        }
        emit(new AgentEvent.ToolCallFinished(call, result, (System.nanoTime() - start) / 1_000_000));
        return result.text();
    }

    private static String check(final AgentTool tool, final JsonNode args) {
        try {
            return tool.checkArguments(args);
        } catch (final RuntimeException ex) {
            return ex.getMessage() == null ? ex.toString() : ex.getMessage();
        }
    }

    private static JsonNode normalise(final AgentTool tool, final JsonNode args) {
        try {
            final JsonNode normalised = tool.normaliseArguments(args);
            return normalised == null ? args : normalised;
        } catch (final RuntimeException ex) {
            return args;
        }
    }

    /**
     * Tell the model which properties it actually sent, so it can see what it
     * got wrong.
     */
    static String describeProvided(final JsonNode args) {
        if (args == null || !args.isObject()) {
            return "";
        }
        final List<String> names = new ArrayList<>();
        args.fieldNames().forEachRemaining(names::add);
        return names.isEmpty() ? " (no arguments were provided)" : " (you provided: " + String.join(", ", names) + ")";
    }

    private static String describeCall(final AgentTool tool, final JsonNode args) {
        try {
            final String description = tool.describeCall(args);
            return description == null || description.isBlank() ? tool.name() : description;
        } catch (final RuntimeException ex) {
            return tool.name();
        }
    }

    static JsonNode parseArguments(final String arguments) throws JsonProcessingException {
        if (arguments == null || arguments.isBlank()) {
            return JsonNodeFactory.instance.objectNode();
        }
        final JsonNode node = MAPPER.readTree(arguments);
        return node == null || node.isNull() ? JsonNodeFactory.instance.objectNode() : node;
    }

    private static String pretty(final JsonNode node) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (final JsonProcessingException ex) {
            return String.valueOf(node);
        }
    }

    /**
     * The model call failed.
     */
    static final class LlmCallException extends Exception {

        private static final long serialVersionUID = 1L;

        LlmCallException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    private final class SessionToolContext implements ToolContext {

        private final ToolCall call;
        private final AgentConfig turnConfig;

        SessionToolContext(final ToolCall call, final AgentConfig turnConfig) {
            this.call = call;
            this.turnConfig = turnConfig;
        }

        @Override
        public Graph getActiveGraph() {
            return environment.getActiveGraph();
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get() || Thread.currentThread().isInterrupted();
        }

        @Override
        public void progress(final String message) {
            emit(new AgentEvent.ToolCallProgress(call, message));
        }

        @Override
        public int getMaxResultChars() {
            return turnConfig.maxToolResultChars();
        }
    }
}
