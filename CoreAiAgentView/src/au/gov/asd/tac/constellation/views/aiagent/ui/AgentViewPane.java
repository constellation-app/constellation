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

import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.awt.Color;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * The Assistant view: a header with endpoint and model selection, a
 * scrolling transcript, and an input area with Send and Stop buttons.
 * <p>
 * This class only lays out and updates nodes; {@link AgentViewController}
 * wires the actions. All methods must be called on the JavaFX thread.
 *
 * @author doubtsec
 */
public final class AgentViewPane extends BorderPane {

    private static final String INPUT_PROMPT = "Ask about the graph…  (Enter to send, Shift+Enter for a new line)";
    public static final List<String> REASONING_EFFORTS = List.of("default", "none", "low", "medium", "high");
    static final List<String> SUGGESTIONS = List.of(
            "Summarise this graph",
            "Which vertices are the most central?",
            "What types of entities are in the graph?",
            "Select the vertices with the most connections");

    private final ComboBox<EndpointConfig> endpointCombo = new ComboBox<>();
    private final ComboBox<String> modelCombo = new ComboBox<>();
    private final Button refreshModelsButton = new Button("", icon(UserInterfaceIconProvider.REFRESH));
    private final ToggleButton chatSettingsToggle = new ToggleButton("", icon(UserInterfaceIconProvider.SETTINGS));
    private final Button moreButton = new Button("", icon(UserInterfaceIconProvider.MENU));
    private final ContextMenu moreMenu = new ContextMenu();
    private final MenuItem copyTranscriptItem = new MenuItem("Copy transcript");
    private final MenuItem exportTranscriptItem = new MenuItem("Export transcript…");
    private final MenuItem optionsItem = new MenuItem("Options…");
    private final MenuItem helpItem = new MenuItem("Help");
    private final Button newChatButton = new Button("New chat", icon(UserInterfaceIconProvider.ADD));
    private final TextField temperatureField = new TextField();
    private final ComboBox<String> reasoningCombo = new ComboBox<>(FXCollections.observableArrayList(REASONING_EFFORTS));
    private final Spinner<Integer> maxStepsSpinner = new Spinner<>(1, 100, 20);
    private final HBox chatSettingsRow = new HBox();
    private final HBox banner = new HBox();
    private final Label bannerLabel = new Label();
    private final VBox transcript = new VBox();
    private final ScrollPane transcriptScroll = new ScrollPane(transcript);
    private final VBox emptyState = new VBox();
    private final List<Node> welcomeContent = new ArrayList<>();
    private final List<Node> setupContent = new ArrayList<>();
    private final Button configureButton = new Button("Configure a model…");
    private boolean configured = true;
    private boolean running;
    private final Label contextLabel = new Label("No graph is open");
    private final Label usageLabel = new Label();
    private final TextArea input = new TextArea();
    private final Button sendButton = new Button("Send");
    private final Button stopButton = new Button("Stop");
    private final Map<String, AssistantMessageNode> assistantMessages = new LinkedHashMap<>();
    private final Map<String, ToolCallCard> toolCards = new LinkedHashMap<>();
    private boolean pinnedToBottom = true;
    private Consumer<String> onSuggestion;

    private static ImageView icon(final ConstellationIcon icon) {
        final Color colour = JavafxStyleManager.isDarkTheme() ? ConstellationColor.WHITE.getJavaColor() : ConstellationColor.DARK_GREY.getJavaColor();
        // build at double size and scale down so the icon stays crisp on high DPI screens
        final ImageView view = new ImageView(icon.buildImage(36, colour));
        view.setFitWidth(18);
        view.setFitHeight(18);
        view.setSmooth(true);
        return view;
    }

    public AgentViewPane() {
        getStyleClass().add("agent-view");
        setTop(buildHeader());
        setCenter(buildTranscript());
        setBottom(buildFooter());
        showEmptyState();
    }

    private Node buildHeader() {
        endpointCombo.setPromptText("Endpoint");
        endpointCombo.setTooltip(new Tooltip("The model endpoint to talk to (configure in Options)"));
        endpointCombo.setMaxWidth(Double.MAX_VALUE);
        endpointCombo.setPrefWidth(170);
        modelCombo.setEditable(true);
        modelCombo.setPromptText("Model");
        modelCombo.setTooltip(new Tooltip("Choose a model, or type a model id"));
        modelCombo.setPrefWidth(190);
        refreshModelsButton.setTooltip(new Tooltip("Refresh the model list"));
        chatSettingsToggle.setTooltip(new Tooltip("Chat settings: temperature, reasoning effort, step limit"));
        moreButton.setTooltip(new Tooltip("More"));
        // a plain button and context menu: MenuButton's popup can get stuck when embedded in a JFXPanel
        moreMenu.getItems().addAll(copyTranscriptItem, exportTranscriptItem, new SeparatorMenuItem(), optionsItem, helpItem);
        moreButton.setOnAction(event -> {
            if (moreMenu.isShowing()) {
                moreMenu.hide();
            } else {
                moreMenu.show(moreButton, Side.BOTTOM, 0, 0);
            }
        });
        newChatButton.setTooltip(new Tooltip("Start a new conversation"));

        final FlowPane toolbar = new FlowPane(6, 6, endpointCombo, modelCombo, refreshModelsButton, chatSettingsToggle, moreButton, newChatButton);
        toolbar.getStyleClass().add("agent-toolbar");

        temperatureField.setPromptText("default");
        temperatureField.setPrefColumnCount(5);
        temperatureField.setTooltip(new Tooltip("Sampling temperature, e.g. 0.2; blank for the model default"));
        reasoningCombo.getSelectionModel().select(0);
        reasoningCombo.setTooltip(new Tooltip("Reasoning effort for reasoning models; 'none' turns reasoning off where supported"));
        maxStepsSpinner.setEditable(true);
        maxStepsSpinner.setPrefWidth(80);
        maxStepsSpinner.setTooltip(new Tooltip("Maximum model calls per message"));
        chatSettingsRow.getChildren().addAll(fieldLabel("Temperature"), temperatureField, fieldLabel("Reasoning"), reasoningCombo,
                fieldLabel("Max steps"), maxStepsSpinner);
        chatSettingsRow.getStyleClass().add("agent-chat-settings");
        chatSettingsRow.visibleProperty().bind(chatSettingsToggle.selectedProperty());
        chatSettingsRow.managedProperty().bind(chatSettingsToggle.selectedProperty());

        bannerLabel.setWrapText(true);
        bannerLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bannerLabel, Priority.ALWAYS);
        banner.getChildren().add(bannerLabel);
        banner.getStyleClass().add("agent-banner");
        banner.setVisible(false);
        banner.setManaged(false);

        final VBox header = new VBox(toolbar, chatSettingsRow, banner);
        header.getStyleClass().add("agent-header");
        return header;
    }

    private static Label fieldLabel(final String text) {
        final Label label = new Label(text);
        label.getStyleClass().add("agent-field-label");
        return label;
    }

    private Node buildTranscript() {
        transcript.getStyleClass().add("agent-transcript");
        transcript.setFillWidth(true);
        transcriptScroll.getStyleClass().add("agent-transcript-scroll");
        transcriptScroll.setFitToWidth(true);
        transcriptScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        transcriptScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // follow new content while the user is at the bottom; stop following when they scroll up.
        // Only user gestures change whether we follow, because the scroll pane also moves the
        // position itself as content grows.
        transcriptScroll.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() > 0) {
                pinnedToBottom = false;
            } else {
                Platform.runLater(this::updatePinned);
            }
        });
        transcriptScroll.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> Platform.runLater(this::updatePinned));
        transcript.heightProperty().addListener((obs, oldValue, newValue) -> {
            if (pinnedToBottom) {
                scrollToBottom();
            }
        });

        final Label title = new Label("Assistant");
        title.getStyleClass().add("agent-empty-title");
        final Label text = new Label("Ask about the active graph. The assistant can inspect the graph, run analytics, "
                + "arrangements and Data Access View plugins, and select elements. Changes it makes can be undone, "
                + "and you can approve or deny anything that changes the graph.");
        text.getStyleClass().add("agent-empty-text");
        text.setMaxWidth(420);
        final FlowPane suggestions = new FlowPane(6, 6);
        suggestions.setAlignment(javafx.geometry.Pos.CENTER);
        suggestions.setMaxWidth(440);
        for (final String suggestion : SUGGESTIONS) {
            final Button button = new Button(suggestion);
            button.getStyleClass().add("agent-suggestion");
            button.setOnAction(event -> {
                if (onSuggestion != null) {
                    onSuggestion.accept(suggestion);
                }
            });
            suggestions.getChildren().add(button);
        }
        welcomeContent.addAll(List.of(title, text, suggestions));

        final Label setupTitle = new Label("Set up the Assistant");
        setupTitle.getStyleClass().add("agent-empty-title");
        final Label setupText = new Label("The Assistant doesn't come with a model. Before you can use it, add an endpoint "
                + "that speaks the Anthropic API or an OpenAI-compatible API, and choose the model to use.");
        setupText.getStyleClass().add("agent-empty-text");
        setupText.setMaxWidth(420);
        configureButton.getStyleClass().add("agent-primary");
        setupContent.addAll(List.of(setupTitle, setupText, configureButton));

        emptyState.getChildren().setAll(welcomeContent);
        emptyState.getStyleClass().add("agent-empty");
        emptyState.setMaxWidth(Double.MAX_VALUE);
        return transcriptScroll;
    }

    private void updatePinned() {
        pinnedToBottom = transcriptScroll.getVvalue() >= transcriptScroll.getVmax() - 0.001
                || transcript.getHeight() <= transcriptScroll.getViewportBounds().getHeight();
    }

    private void scrollToBottom() {
        // after layout, so the new content height is known
        Platform.runLater(() -> {
            transcriptScroll.layout();
            transcriptScroll.setVvalue(transcriptScroll.getVmax());
        });
    }

    private Node buildFooter() {
        contextLabel.getStyleClass().add("agent-context");
        contextLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(contextLabel, Priority.ALWAYS);
        usageLabel.getStyleClass().add("agent-context");
        final HBox status = new HBox(6, contextLabel, usageLabel);

        input.setPromptText(INPUT_PROMPT);
        input.setWrapText(true);
        input.setPrefRowCount(3);
        input.getStyleClass().add("agent-input");
        HBox.setHgrow(input, Priority.ALWAYS);
        input.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                if (!sendButton.isDisabled()) {
                    sendButton.fire();
                }
            }
        });

        sendButton.getStyleClass().add("agent-primary");
        sendButton.setDefaultButton(false);
        sendButton.setMaxWidth(Double.MAX_VALUE);
        sendButton.setTooltip(new Tooltip("Send (Enter)"));
        stopButton.setMaxWidth(Double.MAX_VALUE);
        stopButton.setTooltip(new Tooltip("Stop the assistant"));
        stopButton.setDisable(true);
        final VBox buttons = new VBox(6, sendButton, stopButton);
        buttons.getStyleClass().add("agent-send-buttons");
        buttons.setMinWidth(Region.USE_PREF_SIZE);

        final HBox inputRow = new HBox(input, buttons);
        inputRow.getStyleClass().add("agent-input-row");

        final VBox footer = new VBox(status, inputRow);
        footer.getStyleClass().add("agent-footer");
        return footer;
    }

    // ----- accessors used by the controller -----
    public ComboBox<EndpointConfig> getEndpointCombo() {
        return endpointCombo;
    }

    public ComboBox<String> getModelCombo() {
        return modelCombo;
    }

    public Button getRefreshModelsButton() {
        return refreshModelsButton;
    }

    public MenuItem getCopyTranscriptItem() {
        return copyTranscriptItem;
    }

    public MenuItem getExportTranscriptItem() {
        return exportTranscriptItem;
    }

    public Button getConfigureButton() {
        return configureButton;
    }

    public MenuItem getOptionsItem() {
        return optionsItem;
    }

    public MenuItem getHelpItem() {
        return helpItem;
    }

    public Button getNewChatButton() {
        return newChatButton;
    }

    public TextField getTemperatureField() {
        return temperatureField;
    }

    public ComboBox<String> getReasoningCombo() {
        return reasoningCombo;
    }

    public Spinner<Integer> getMaxStepsSpinner() {
        return maxStepsSpinner;
    }

    public TextArea getInput() {
        return input;
    }

    public Button getSendButton() {
        return sendButton;
    }

    public Button getStopButton() {
        return stopButton;
    }

    public void setOnSuggestion(final Consumer<String> onSuggestion) {
        this.onSuggestion = onSuggestion;
    }

    // ----- state -----
    /**
     * Show whether a turn is running.
     *
     * @param running true while the assistant is working.
     */
    public void setRunning(final boolean running) {
        this.running = running;
        sendButton.setDisable(running || !configured);
        stopButton.setDisable(!running);
        newChatButton.setDisable(false);
    }

    /**
     * Show whether a model endpoint has been configured. Until one has, the
     * empty transcript asks the user to configure a model and messages can't
     * be sent.
     *
     * @param configured true if at least one endpoint is configured.
     */
    public void setConfigured(final boolean configured) {
        this.configured = configured;
        emptyState.getChildren().setAll(configured ? welcomeContent : setupContent);
        endpointCombo.setDisable(!configured);
        modelCombo.setDisable(!configured);
        refreshModelsButton.setDisable(!configured);
        input.setDisable(!configured);
        input.setPromptText(configured ? INPUT_PROMPT : "Configure a model to start chatting");
        setRunning(running);
    }

    public boolean isConfigured() {
        return configured;
    }

    /**
     * Show a problem with the selected endpoint under the toolbar, or hide the
     * banner when there is none.
     *
     * @param problem the problem, or null.
     */
    public void setBanner(final String problem) {
        final boolean show = problem != null && !problem.isBlank();
        bannerLabel.setText(show ? "\u26a0 " + problem : "");
        banner.setVisible(show);
        banner.setManaged(show);
    }

    public void setContext(final String text) {
        contextLabel.setText(text);
    }

    public void setUsage(final String text) {
        usageLabel.setText(text);
    }

    // ----- transcript -----
    private void showEmptyState() {
        transcript.getChildren().setAll(emptyState);
    }

    private void addToTranscript(final Node node) {
        transcript.getChildren().remove(emptyState);
        transcript.getChildren().add(node);
    }

    /**
     * Remove everything from the transcript.
     */
    public void clearTranscript() {
        assistantMessages.clear();
        toolCards.clear();
        showEmptyState();
        pinnedToBottom = true;
    }

    public boolean isTranscriptEmpty() {
        return transcript.getChildren().size() == 1 && transcript.getChildren().get(0) == emptyState;
    }

    public void addUserMessage(final String text) {
        final TextFlow flow = new TextFlow(new Text(text));
        final VBox bubble = new VBox(flow);
        bubble.getStyleClass().add("agent-user-bubble");
        bubble.setMaxWidth(Region.USE_PREF_SIZE);
        final HBox row = new HBox(bubble);
        row.getStyleClass().add("agent-user-row");
        // keep a margin on the left so user messages read as replies
        bubble.maxWidthProperty().bind(transcript.widthProperty().multiply(0.85));
        pinnedToBottom = true;
        addToTranscript(row);
        scrollToBottom();
    }

    /**
     * Get or create the node for an assistant message.
     *
     * @param messageId the message id.
     * @return the node.
     */
    public AssistantMessageNode assistantMessage(final String messageId) {
        return assistantMessages.computeIfAbsent(messageId, id -> {
            final AssistantMessageNode node = new AssistantMessageNode(id);
            addToTranscript(node);
            return node;
        });
    }

    public AssistantMessageNode findAssistantMessage(final String messageId) {
        return assistantMessages.get(messageId);
    }

    /**
     * Get or create the card for a tool call.
     *
     * @param callId the call id.
     * @param toolName the tool name.
     * @return the card.
     */
    public ToolCallCard toolCard(final String callId, final String toolName) {
        return toolCards.computeIfAbsent(callId, id -> {
            final ToolCallCard card = new ToolCallCard(id, toolName);
            addToTranscript(card);
            return card;
        });
    }

    public ToolCallCard findToolCard(final String callId) {
        return toolCards.get(callId);
    }

    public List<ToolCallCard> getToolCards() {
        return List.copyOf(toolCards.values());
    }

    public List<AssistantMessageNode> getAssistantMessages() {
        return List.copyOf(assistantMessages.values());
    }

    public void addNotice(final String text) {
        final Label label = new Label(text);
        label.getStyleClass().add("agent-notice-text");
        final HBox row = new HBox(label);
        row.getStyleClass().add("agent-notice");
        addToTranscript(row);
    }

    public void addError(final String text) {
        final Label label = new Label(text);
        label.getStyleClass().add("agent-error-text");
        label.setMaxWidth(Double.MAX_VALUE);
        final HBox row = new HBox(label);
        row.getStyleClass().add("agent-error");
        addToTranscript(row);
    }

    /**
     * Put the cursor in the input box.
     */
    public void focusInput() {
        requestFocusWhenShowing(input);
    }

    /**
     * Request focus for a node, but only once its scene is showing in a
     * window. Requesting focus before the embedding JFXPanel has a peer can
     * upset JavaFX's embedded scene.
     *
     * @param node the node.
     */
    static void requestFocusWhenShowing(final Node node) {
        Platform.runLater(() -> {
            if (node.getScene() != null && node.getScene().getWindow() != null && node.getScene().getWindow().isShowing()) {
                node.requestFocus();
            }
        });
    }
}
