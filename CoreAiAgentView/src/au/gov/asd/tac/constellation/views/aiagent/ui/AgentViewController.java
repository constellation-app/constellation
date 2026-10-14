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
package au.gov.asd.tac.constellation.views.aiagent.ui;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import au.gov.asd.tac.constellation.views.aiagent.agent.AgentConfig;
import au.gov.asd.tac.constellation.views.aiagent.agent.AgentEnvironment;
import au.gov.asd.tac.constellation.views.aiagent.agent.AgentEvent;
import au.gov.asd.tac.constellation.views.aiagent.agent.AgentSession;
import au.gov.asd.tac.constellation.views.aiagent.agent.FinishReason;
import au.gov.asd.tac.constellation.views.aiagent.agent.SystemPrompt;
import au.gov.asd.tac.constellation.views.aiagent.agent.ToolRegistry;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointConfig;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmProvider;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmProviders;
import au.gov.asd.tac.constellation.views.aiagent.llm.ModelInfo;
import au.gov.asd.tac.constellation.views.aiagent.preferences.AgentSettings;
import au.gov.asd.tac.constellation.views.aiagent.preferences.ApiKeyStore;
import au.gov.asd.tac.constellation.views.aiagent.tools.GraphSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeListener;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.HelpCtx;

/**
 * Connects an {@link AgentSession} to the {@link AgentViewPane}: turns user
 * actions into session calls and session events into transcript updates.
 * <p>
 * Agent events arrive on the agent's threads; they are queued and applied on
 * the JavaFX thread in batches (at most every {@value #FLUSH_MILLIS} ms) so a
 * fast stream doesn't flood the JavaFX event queue.
 *
 * @author doubtsec
 */
public final class AgentViewController implements AgentEnvironment {

    private static final Logger LOGGER = Logger.getLogger(AgentViewController.class.getName());
    static final int FLUSH_MILLIS = 30;
    static final String OPTIONS_PATH = "constellation/AiAssistant";
    private static final String LAST_MODEL_PREFIX = "aiagent.lastModel.";

    private final AgentViewPane pane;
    private final ToolRegistry registry;
    private final ExecutorService background = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "Assistant Background");
        thread.setDaemon(true);
        return thread;
    });
    private final Queue<AgentEvent> events = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean flushScheduled = new AtomicBoolean(false);
    private final List<ObjectNode> transcriptLog = new ArrayList<>();
    private final PreferenceChangeListener preferenceListener;
    private final BiFunction<EndpointConfig, String, LlmProvider> providerFactory;
    private final Function<EndpointConfig, String> keyReader;

    private AgentSettings settings;
    private AgentSession session;
    private volatile Graph currentGraph;
    private volatile String currentGraphName;
    private long inputTokens;
    private long outputTokens;
    private boolean updatingEndpoints;

    public AgentViewController(final AgentViewPane pane) {
        this(pane, ToolRegistry.fromLookup(), LlmProviders::create, ApiKeyStore::read);
    }

    /**
     * Create a controller with explicit dependencies, for testing.
     *
     * @param pane the view.
     * @param registry the tools.
     * @param providerFactory creates a provider for an endpoint and API key.
     * @param keyReader reads the API key for an endpoint.
     */
    AgentViewController(final AgentViewPane pane, final ToolRegistry registry,
            final BiFunction<EndpointConfig, String, LlmProvider> providerFactory, final Function<EndpointConfig, String> keyReader) {
        this.pane = pane;
        this.registry = registry;
        this.providerFactory = providerFactory;
        this.keyReader = keyReader;
        this.settings = AgentSettings.load();
        this.preferenceListener = event -> {
            if (event.getKey() != null && event.getKey().startsWith("aiagent.") && !event.getKey().startsWith(LAST_MODEL_PREFIX)) {
                Platform.runLater(this::reloadSettings);
            }
        };
        AgentSettings.preferences().addPreferenceChangeListener(preferenceListener);
        Platform.runLater(this::initialise);
    }

    // ----- set up -----
    private void initialise() {
        pane.getEndpointCombo().valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!updatingEndpoints) {
                endpointChanged(newValue);
            }
        });
        pane.getModelCombo().valueProperty().addListener((obs, oldValue, newValue) -> {
            final EndpointConfig endpoint = pane.getEndpointCombo().getValue();
            if (endpoint != null && newValue != null && !newValue.isBlank()) {
                AgentSettings.preferences().put(LAST_MODEL_PREFIX + endpoint.id(), newValue.trim());
                pane.setBanner(null);
            }
        });
        pane.getRefreshModelsButton().setOnAction(event -> loadModels(pane.getEndpointCombo().getValue()));
        pane.getSendButton().setOnAction(event -> send(pane.getInput().getText()));
        pane.getStopButton().setOnAction(event -> stop());
        pane.getNewChatButton().setOnAction(event -> newChat());
        pane.getCopyTranscriptItem().setOnAction(event -> copyTranscript());
        pane.getExportTranscriptItem().setOnAction(event -> exportTranscript());
        pane.getOptionsItem().setOnAction(event -> openOptions());
        pane.getConfigureButton().setOnAction(event -> openOptions());
        pane.getHelpItem().setOnAction(event -> SwingUtilities.invokeLater(() -> new HelpCtx(AgentViewTopComponent.class.getName()).display()));
        pane.setOnSuggestion(this::send);
        applyChatDefaults();
        populateEndpoints();
        pane.setRunning(false);
    }

    private static void openOptions() {
        SwingUtilities.invokeLater(() -> OptionsDisplayer.getDefault().open(OPTIONS_PATH));
    }

    private void applyChatDefaults() {
        pane.getTemperatureField().setText(settings.temperature() == null ? "" : settings.temperature().toString());
        final String effort = settings.reasoningEffort().isBlank() ? "default" : settings.reasoningEffort();
        pane.getReasoningCombo().getSelectionModel().select(AgentViewPane.REASONING_EFFORTS.contains(effort) ? effort : "default");
        pane.getMaxStepsSpinner().getValueFactory().setValue(settings.maxSteps());
    }

    private void reloadSettings() {
        final AgentSettings previous = settings;
        settings = AgentSettings.load();
        if (previous.maxSteps() != settings.maxSteps() || !java.util.Objects.equals(previous.temperature(), settings.temperature())
                || !previous.reasoningEffort().equals(settings.reasoningEffort())) {
            applyChatDefaults();
        }
        populateEndpoints();
    }

    private void populateEndpoints() {
        pane.setConfigured(settings.isConfigured());
        final EndpointConfig selected = pane.getEndpointCombo().getValue();
        updatingEndpoints = true;
        try {
            pane.getEndpointCombo().getItems().setAll(settings.endpoints());
            EndpointConfig choice = null;
            if (selected != null) {
                choice = settings.endpoints().stream().filter(e -> e.id().equals(selected.id())).findFirst().orElse(null);
            }
            if (choice == null) {
                choice = settings.defaultEndpoint();
            }
            pane.getEndpointCombo().setValue(choice);
        } finally {
            updatingEndpoints = false;
        }
        endpointChanged(pane.getEndpointCombo().getValue());
    }

    private void endpointChanged(final EndpointConfig endpoint) {
        // with no endpoints at all the transcript shows the set up prompt instead
        pane.setBanner(endpoint == null && settings.isConfigured() ? "Choose an endpoint." : null);
        pane.getModelCombo().getItems().clear();
        if (endpoint == null) {
            pane.getModelCombo().setValue(null);
            return;
        }
        final String remembered = AgentSettings.preferences().get(LAST_MODEL_PREFIX + endpoint.id(), "");
        pane.getModelCombo().setValue(!remembered.isBlank() ? remembered : endpoint.defaultModel().isBlank() ? null : endpoint.defaultModel());
        showModelPrompt();
        loadModels(endpoint);
    }

    /**
     * There is no default model, so ask the user to choose one until they
     * have.
     */
    private void showModelPrompt() {
        final String model = pane.getModelCombo().getValue();
        if (pane.getEndpointCombo().getValue() != null && (model == null || model.isBlank())) {
            pane.setBanner("Choose a model to use, or set one for this endpoint in Options.");
        }
    }

    private void loadModels(final EndpointConfig endpoint) {
        if (endpoint == null) {
            return;
        }
        pane.getRefreshModelsButton().setDisable(true);
        background.submit(() -> {
            List<ModelInfo> models = List.of();
            String error = null;
            try {
                models = providerFactory.apply(endpoint, keyReader.apply(endpoint)).listModels();
            } catch (final IOException | RuntimeException ex) {
                LOGGER.log(Level.INFO, "Couldn't list models for {0}: {1}", new Object[]{endpoint.name(), ex.getMessage()});
                error = ex.getMessage();
            }
            final List<ModelInfo> result = models;
            final String failure = error;
            Platform.runLater(() -> {
                pane.getRefreshModelsButton().setDisable(false);
                if (endpoint != pane.getEndpointCombo().getValue()) {
                    return;
                }
                final String current = pane.getModelCombo().getValue();
                pane.getModelCombo().getItems().setAll(result.stream().map(ModelInfo::id).toList());
                // never pick a model for the user: keep whatever they chose, even if it isn't listed
                pane.getModelCombo().setValue(current);
                pane.setBanner(failure != null && pane.isTranscriptEmpty() ? "Couldn't list models: " + failure : null);
                if (failure == null) {
                    showModelPrompt();
                }
            });
        });
    }

    // ----- actions -----
    /**
     * Send a message to the assistant.
     *
     * @param text the message.
     */
    public void send(final String text) {
        if (text == null || text.isBlank() || session != null && session.isRunning()) {
            return;
        }
        final EndpointConfig endpoint = pane.getEndpointCombo().getValue();
        final String model = pane.getModelCombo().getEditor().getText() != null && !pane.getModelCombo().getEditor().getText().isBlank()
                ? pane.getModelCombo().getEditor().getText().trim() : pane.getModelCombo().getValue();
        if (!settings.isConfigured()) {
            pane.addError("Configure a model in Options before using the assistant.");
            return;
        }
        if (endpoint == null) {
            pane.addError("Choose an endpoint first.");
            return;
        }
        if (model == null || model.isBlank()) {
            pane.addError("Choose or type a model first.");
            return;
        }
        final String message = text.trim();
        pane.getInput().clear();
        pane.addUserMessage(message);
        logEntry("user", message);
        pane.setRunning(true);
        final AgentConfig config = buildConfig(model);
        background.submit(() -> {
            try {
                final LlmProvider provider = providerFactory.apply(endpoint, keyReader.apply(endpoint));
                synchronized (this) {
                    if (session == null) {
                        session = new AgentSession(provider, config, registry, this, this::enqueue);
                    } else {
                        session.setProvider(provider);
                        session.setConfig(config);
                    }
                    session.setDebugLogging(settings.debugLogging());
                    session.send(message);
                }
            } catch (final RuntimeException ex) {
                LOGGER.log(Level.INFO, "Couldn't start the assistant", ex);
                Platform.runLater(() -> {
                    pane.addError(ex.getMessage() == null ? ex.toString() : ex.getMessage());
                    pane.setRunning(false);
                });
            }
        });
    }

    AgentConfig buildConfig(final String model) {
        Double temperature = null;
        final String temperatureText = pane.getTemperatureField().getText();
        if (temperatureText != null && !temperatureText.isBlank()) {
            try {
                temperature = Double.valueOf(temperatureText.trim());
            } catch (final NumberFormatException ex) {
                pane.addNotice("Ignoring invalid temperature '" + temperatureText + "'.");
            }
        }
        final String effort = pane.getReasoningCombo().getValue();
        final Integer maxSteps = pane.getMaxStepsSpinner().getValue();
        return new AgentConfig(model, SystemPrompt.resolve(settings.systemPromptOverride()), maxSteps == null ? settings.maxSteps() : maxSteps,
                settings.maxToolResultChars(), temperature, effort == null || "default".equals(effort) ? null : effort,
                settings.toolPolicies(), AgentConfig.DEFAULT_MAX_HISTORY_CHARS);
    }

    public void stop() {
        final AgentSession current = session;
        if (current != null) {
            current.cancel();
        }
    }

    public void newChat() {
        final AgentSession current = session;
        if (current != null) {
            current.newChat();
        }
        pane.clearTranscript();
        transcriptLog.clear();
        inputTokens = 0;
        outputTokens = 0;
        pane.setUsage("");
        pane.setRunning(false);
        pane.focusInput();
    }

    public void shutdown() {
        AgentSettings.preferences().removePreferenceChangeListener(preferenceListener);
        final AgentSession current = session;
        if (current != null) {
            current.shutdown();
        }
        background.shutdownNow();
    }

    // ----- events -----
    private void enqueue(final AgentEvent event) {
        events.add(event);
        if (flushScheduled.compareAndSet(false, true)) {
            Platform.runLater(() -> {
                final PauseTransition pause = new PauseTransition(Duration.millis(FLUSH_MILLIS));
                pause.setOnFinished(e -> flush());
                pause.play();
            });
        }
    }

    private void flush() {
        flushScheduled.set(false);
        AgentEvent event;
        String pendingTextId = null;
        final StringBuilder pendingText = new StringBuilder();
        while ((event = events.poll()) != null) {
            // merge consecutive text deltas for the same message into one update
            if (event instanceof AgentEvent.TextDelta delta) {
                if (pendingTextId != null && !pendingTextId.equals(delta.messageId())) {
                    pane.assistantMessage(pendingTextId).appendText(pendingText.toString());
                    pendingText.setLength(0);
                }
                pendingTextId = delta.messageId();
                pendingText.append(delta.delta());
                continue;
            }
            if (pendingTextId != null) {
                pane.assistantMessage(pendingTextId).appendText(pendingText.toString());
                pendingText.setLength(0);
                pendingTextId = null;
            }
            apply(event);
        }
        if (pendingTextId != null) {
            pane.assistantMessage(pendingTextId).appendText(pendingText.toString());
        }
    }

    private void apply(final AgentEvent event) {
        switch (event) {
            case AgentEvent.TurnStarted started ->
                pane.setRunning(true);
            case AgentEvent.ModelCallStarted started ->
                pane.assistantMessage(started.messageId());
            case AgentEvent.TextDelta delta ->
                pane.assistantMessage(delta.messageId()).appendText(delta.delta());
            case AgentEvent.ReasoningDelta delta ->
                pane.assistantMessage(delta.messageId()).appendReasoning(delta.delta());
            case AgentEvent.ToolCallStreaming streaming ->
                pane.assistantMessage(streaming.messageId()).setStatus("Preparing " + streaming.toolName() + "…");
            case AgentEvent.AssistantMessageFinished finished -> {
                pane.assistantMessage(finished.messageId()).finish(finished.text(), finished.reasoning());
                if (finished.text() != null && !finished.text().isBlank()) {
                    logEntry("assistant", finished.text());
                }
            }
            case AgentEvent.ToolCallProposed proposed -> {
                final ToolCallCard card = pane.toolCard(proposed.call().id(), proposed.call().name());
                card.setOnDecision(decision -> {
                    final AgentSession current = session;
                    if (current != null) {
                        current.decide(proposed.call().id(), decision);
                    }
                });
                card.propose(proposed);
                card.activeGraphChanged(currentGraphName);
                final ObjectNode entry = logEntry("tool_call", proposed.description());
                entry.put("tool", proposed.call().name());
                entry.put("arguments", proposed.prettyArguments());
            }
            case AgentEvent.ToolCallStarted started ->
                pane.toolCard(started.call().id(), started.call().name()).started(started.call().arguments());
            case AgentEvent.ToolCallProgress progress ->
                pane.toolCard(progress.call().id(), progress.call().name()).progress(progress.message());
            case AgentEvent.ToolCallFinished finished -> {
                pane.toolCard(finished.call().id(), finished.call().name()).finished(finished.result(), finished.durationMillis());
                final ObjectNode entry = logEntry("tool_result", finished.result().text());
                entry.put("tool", finished.call().name());
                entry.put("error", finished.result().error());
            }
            case AgentEvent.ToolCallRejected rejected -> {
                pane.toolCard(rejected.call().id(), rejected.call().name()).rejected(rejected.reason(), rejected.call().arguments());
                logEntry("tool_rejected", rejected.reason()).put("tool", rejected.call().name());
            }
            case AgentEvent.Usage usage -> {
                inputTokens += Math.max(0, usage.inputTokens());
                outputTokens += Math.max(0, usage.outputTokens());
                final NumberFormat format = NumberFormat.getIntegerInstance();
                pane.setUsage(String.format("Tokens: %s in · %s out", format.format(inputTokens), format.format(outputTokens)));
            }
            case AgentEvent.Notice notice -> {
                pane.addNotice(notice.message());
                logEntry("notice", notice.message());
            }
            case AgentEvent.Error error -> {
                final String message = error.error().getMessage() == null ? error.error().toString() : error.error().getMessage();
                pane.addError(message);
                logEntry("error", message);
            }
            case AgentEvent.TurnFinished finished ->
                turnFinished(finished.reason());
        }
    }

    private void turnFinished(final FinishReason reason) {
        final String note = switch (reason) {
            case CANCELLED ->
                "Stopped";
            case ERROR ->
                "Failed";
            default ->
                "";
        };
        pane.getAssistantMessages().forEach(node -> {
            if (!node.isFinished()) {
                node.abandon(note.isEmpty() ? "No response" : note);
            }
        });
        pane.getToolCards().forEach(card -> {
            if (card.isAwaitingApproval()) {
                card.rejected("Stopped", null);
            }
        });
        if (reason == FinishReason.CANCELLED) {
            pane.addNotice("Stopped by you.");
        }
        pane.setRunning(false);
        pane.focusInput();
    }

    // ----- graph context -----
    /**
     * The active graph changed.
     *
     * @param graph the new active graph, may be null.
     */
    public void setGraph(final Graph graph) {
        currentGraph = graph;
        updateContext();
    }

    /**
     * Refresh the graph summary shown under the transcript.
     */
    public void updateContext() {
        final Graph graph = currentGraph;
        background.submit(() -> {
            final String name = graph == null ? null : GraphSupport.graphName(graph);
            currentGraphName = name;
            final String text;
            if (graph == null) {
                text = "No graph is open";
            } else {
                final NumberFormat format = NumberFormat.getIntegerInstance();
                try (final ReadableGraph rg = graph.getReadableGraph()) {
                    text = String.format("Graph: \"%s\" · %s nodes · %s transactions · %s selected", name,
                            format.format(rg.getVertexCount()), format.format(rg.getTransactionCount()),
                            format.format(GraphSupport.countSelected(rg, GraphElementType.VERTEX)
                                    + GraphSupport.countSelected(rg, GraphElementType.TRANSACTION)));
                }
            }
            Platform.runLater(() -> {
                pane.setContext(text);
                pane.getToolCards().forEach(card -> card.activeGraphChanged(name));
            });
        });
    }

    @Override
    public Graph getActiveGraph() {
        return GraphManager.getDefault().getActiveGraph();
    }

    @Override
    public String graphName(final Graph graph) {
        return GraphSupport.graphName(graph);
    }

    @Override
    public String describeContext() {
        final Graph graph = getActiveGraph();
        if (graph == null) {
            return "[Context — generated by Constellation, not written by the user]\nNo graph is open.";
        }
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final StringBuilder context = new StringBuilder("[Context — generated by Constellation, not written by the user]\n");
            context.append(String.format("Active graph: \"%s\" (graph_id %s), schema %s.%n", GraphSupport.graphName(graph), graph.getId(),
                    rg.getSchema() == null ? "none" : rg.getSchema().getFactory().getLabel()));
            context.append(String.format("%d vertices, %d transactions", rg.getVertexCount(), rg.getTransactionCount()));
            if (settings.includeSelection()) {
                context.append(String.format("; %d vertices and %d transactions selected",
                        GraphSupport.countSelected(rg, GraphElementType.VERTEX), GraphSupport.countSelected(rg, GraphElementType.TRANSACTION)));
            }
            context.append('.');
            return context.toString();
        }
    }

    // ----- transcript export -----
    private ObjectNode logEntry(final String role, final String text) {
        final ObjectNode entry = JsonNodeFactory.instance.objectNode();
        entry.put("role", role);
        entry.put("text", text == null ? "" : text);
        transcriptLog.add(entry);
        return entry;
    }

    String transcriptMarkdown() {
        final StringBuilder markdown = new StringBuilder("# Assistant transcript\n\n");
        for (final ObjectNode entry : transcriptLog) {
            final String text = entry.path("text").asText();
            switch (entry.path("role").asText()) {
                case "user" ->
                    markdown.append("**You:** ").append(text).append("\n\n");
                case "assistant" ->
                    markdown.append("**Assistant:** ").append(text).append("\n\n");
                case "tool_call" ->
                    markdown.append("> Tool call `").append(entry.path("tool").asText()).append("`: ").append(text).append("\n\n");
                case "tool_result" ->
                    markdown.append("> Result").append(entry.path("error").asBoolean() ? " (error)" : "").append(": ")
                            .append(text.length() > 500 ? text.substring(0, 500) + "…" : text).append("\n\n");
                case "tool_rejected" ->
                    markdown.append("> Not run: ").append(text).append("\n\n");
                default ->
                    markdown.append("_").append(text).append("_\n\n");
            }
        }
        return markdown.toString();
    }

    String transcriptJson() {
        final ArrayNode array = JsonNodeFactory.instance.arrayNode();
        transcriptLog.forEach(array::add);
        try {
            return JsonFactoryUtilities.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(array);
        } catch (final JsonProcessingException ex) {
            return array.toString();
        }
    }

    private void copyTranscript() {
        final ClipboardContent content = new ClipboardContent();
        content.putString(transcriptMarkdown());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void exportTranscript() {
        final String markdown = transcriptMarkdown();
        final String json = transcriptJson();
        SwingUtilities.invokeLater(() -> {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export Assistant transcript");
            final FileNameExtensionFilter md = new FileNameExtensionFilter("Markdown (*.md)", "md");
            final FileNameExtensionFilter js = new FileNameExtensionFilter("JSON (*.json)", "json");
            chooser.addChoosableFileFilter(md);
            chooser.addChoosableFileFilter(js);
            chooser.setFileFilter(md);
            chooser.setSelectedFile(new File("ai-assistant-transcript.md"));
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                final boolean asJson = chooser.getFileFilter() == js || file.getName().toLowerCase(Locale.ROOT).endsWith(".json");
                if (!file.getName().contains(".")) {
                    file = new File(file.getParentFile(), file.getName() + (asJson ? ".json" : ".md"));
                }
                try {
                    Files.writeString(file.toPath(), asJson ? json : markdown, StandardCharsets.UTF_8);
                } catch (final IOException ex) {
                    LOGGER.log(Level.WARNING, "Couldn't export the transcript", ex);
                    Platform.runLater(() -> pane.addError("Couldn't export the transcript: " + ex.getMessage()));
                }
            }
        });
    }

    AgentSession getSession() {
        return session;
    }
}
