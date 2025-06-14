/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.MultiChoiceInputField;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.views.conversationview.state.ConversationState;
import au.gov.asd.tac.constellation.views.conversationview.state.ConversationViewConcept;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.HelpCtx;

/**
 * The ConversationBox represents the entire GUI for the conversation view.
 * <p>
 * It contains a Conversation, the model for the dynamically generated content based on the current graph selection, as
 * well as a list view of Bubbles to display this dynamic content, and some static controls used to interact with and
 * alter the content that is displayed.
 *
 * @see Conversation
 * @see ConversationBubble
 *
 * @author sirius
 * @author antares
 * @author sol695510
 */
public final class ConversationBox extends StackPane {

    public static final double PADDING = 5;

    private final Conversation conversation;

    private ListView<ConversationMessage> bubbles;
    private BorderPane contributionsPane;

    private TooltipPane tipsPane = new TooltipPane();
    private CheckBox showToolTip = new CheckBox("Hovering translations");
    private final ToolBar optionsPane = new ToolBar();

    // A cache to hold bubble for the listview.
    private final Map<ConversationMessage, BubbleBox> bubbleCache = new HashMap<>();

    // Allow the user to choose the displayed actor name.
    private final ObservableList<String> senderAttributesChoices = FXCollections.observableArrayList();
    private final MultiChoiceInputField<String> senderAttributesMultiChoiceInput = new MultiChoiceInputField<>(senderAttributesChoices);
    private final ChoiceBox<Integer> contentPerPageChoiceBox = new ChoiceBox<>();

    private HBox togglesPane;

    private volatile boolean isAdjustingContributionProviders = false;
    private volatile boolean isAdjustingSenderLabels;

    private static final String CSS_BACKGROUND_COLOR_TRANSPARENT = "-fx-background-color: transparent;";
    private int foundCount;
    private int searchCount;

    private static final boolean DARK_MODE = JavafxStyleManager.isDarkTheme();
    private static final String FOUND_TEXT = "Showing ";
    private static final String FOUND_PASS_COLOR = DARK_MODE ? "-fx-text-fill: yellow;" : "-fx-text-fill: blue;";
    private static final String FOUND_FAIL_COLOR = "-fx-text-fill: red;";
    private final Label foundLabel = new Label();

    private final TextField searchTextField = new TextField();
    private final Button nextButton = new Button("Next");
    private final Button prevButton = new Button("Previous");
    private final HBox searchHBox = new HBox();

    private final Pagination pagination = new Pagination(0);
    private final ProgressIndicator spinner = new ProgressIndicator();

    /**
     * Create a ConversationBox with the given Conversation.
     *
     * @param conversation The Conversation that this ConversationBox will display.
     */
    public ConversationBox(final Conversation conversation) {
        this.conversation = conversation;

        setPrefSize(500, 500);
        setCache(true);
        setCacheHint(CacheHint.SPEED);
        setStyle(CSS_BACKGROUND_COLOR_TRANSPARENT);

        final VBox content = new VBox();
        content.setStyle(CSS_BACKGROUND_COLOR_TRANSPARENT);

        // Hook up the bubbles pane to the conversation.
        final ObservableList<ConversationMessage> messages = FXCollections.observableArrayList();
        final Label contentLabel = new Label("Content per page:");

        contentPerPageChoiceBox.getItems().clear();
        contentPerPageChoiceBox.getItems().addAll(50, 100, 250, 1000);
        contentPerPageChoiceBox.getSelectionModel().select(50);
        contentPerPageChoiceBox.setValue(50);
        contentPerPageChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
            if (activeGraph != null) {
                final ReadableGraph graph = activeGraph.getReadableGraph();
                conversation.setContentPerPage(contentPerPageChoiceBox.getValue());
                messages.clear();
                conversation.setPageNumber(pagination.currentPageIndexProperty().get());
                messages.addAll(conversation.updateMessages(graph));
                updatePages(conversation.getTotalPages());
                graph.release();
            }
        });

        togglesPane = new HBox();
        togglesPane.getStylesheets().add(
                JavafxStyleManager.class.getResource("pillbutton/PillButton.css").toExternalForm()
        );
        togglesPane.setAlignment(Pos.CENTER);

        // Create toggle buttons that allow the user to turn on and off the content contributors.
        conversation.setContributorListener(contributors -> {
            isAdjustingContributionProviders = true;
            try {
                int buttonCount = 0;
                togglesPane.getChildren().clear();
                for (final Entry<String, Boolean> contributor : contributors.entrySet()) {
                    final ToggleButton button = new ToggleButton(contributor.getKey());
                    button.setSelected(contributor.getValue());
                    if (contributors.size() == 1) {
                        button.getStyleClass().add("center-pill");
                    } else if (buttonCount == 0) {
                        button.getStyleClass().add("left-pill");
                    } else if (buttonCount == contributors.size() - 1) {
                        button.getStyleClass().add("right-pill");
                    } else {
                        button.getStyleClass().add("center-pill");
                    }
                    button.setOnAction(event -> {
                        if (!isAdjustingContributionProviders) {
                            updateContributionProviderVisibility(contributor.getKey(), button.isSelected());
                        }
                    });
                    togglesPane.getChildren().add(button);
                    buttonCount++;
                }
            } finally {
                isAdjustingContributionProviders = false;
            }
        });

        spinner.setMaxSize(50, 50);

        pagination.setPageCount(1);
        pagination.setMaxPageIndicatorCount(1);

        // Create the bubbles pane.
        bubbles = new ListView<>();
        bubbles.setStyle(CSS_BACKGROUND_COLOR_TRANSPARENT);
        bubbles.setCellFactory(callback -> new BubbleCell());
        VBox.setVgrow(bubbles, Priority.ALWAYS);
        bubbles.setItems(messages);
        conversation.setResultList(messages);

        // When the list updates like during a selection, if there is any text
        // in the search field then highlight the text after the bubbles show
        // and update the found count label
        messages.addListener((Change<? extends ConversationMessage> c) -> {
            updatePages(conversation.getTotalPages());
            highlightRegions(false);
            refreshCountUI(false);
        });

        showToolTip.setSelected(true);
        showToolTip.setOnAction((final ActionEvent t) -> tipsPane.setEnabled(showToolTip.isSelected()));

        conversation.setSenderAttributeListener((possibleSenderAttributes, senderAttributes) -> {
            isAdjustingSenderLabels = true;
            senderAttributesMultiChoiceInput.getCheckModel().clearChecks();
            possibleSenderAttributes = possibleSenderAttributes.stream().filter(Objects::nonNull).toList();
            senderAttributesChoices.setAll(possibleSenderAttributes);
            for (final String senderAttribute : senderAttributes) {
                senderAttributesMultiChoiceInput.getCheckModel().check(senderAttribute);
            }
            isAdjustingSenderLabels = false;
        });

        senderAttributesMultiChoiceInput.getCheckModel().getCheckedItems().addListener((final ListChangeListener.Change<? extends String> c) -> {
            if (!isAdjustingSenderLabels) {
                updateSenderAttributes(senderAttributesMultiChoiceInput.getCheckModel().getCheckedItems());
            }
        });

        // Create controls to allow the user to search and highlight text within contributions.
        searchTextField.setPromptText("Type to search...");
        searchTextField.setStyle("-fx-padding: 5 5 5 5;");
        searchTextField.setOnKeyTyped(e -> {
            foundLabel.setText("searching...");
            foundLabel.setStyle(FOUND_PASS_COLOR);

            // If they hit enter iterate through the results
            searchCount = "\r".equals(e.getCharacter()) && foundCount > 0 ? (searchCount + 1) % foundCount : 0;

            highlightRegions(true);
            refreshCountUI(false);
        });

        prevButton.setOnAction(event -> {
            if (foundCount > 0) {
                searchCount = searchCount <= 0 ? foundCount - 1 : (searchCount - 1) % foundCount;
                highlightRegions(true);
                foundLabel.setText(StringUtils.isBlank(searchTextField.getText()) ? "" : FOUND_TEXT + (searchCount + 1) + " of " + foundCount);
            }
        });
        nextButton.setOnAction(event -> {
            if (foundCount > 0) {
                searchCount = Math.abs((searchCount + 1) % foundCount);
                highlightRegions(true);
                foundLabel.setText(StringUtils.isBlank(searchTextField.getText()) ? "" : FOUND_TEXT + (searchCount + 1) + " of " + foundCount);
            }
        });
        searchTextField.setPrefWidth(300);

        foundLabel.setPadding(new Insets(4, 8, 4, 8));
        searchHBox.setSpacing(6);
        searchHBox.getChildren().clear();
        searchHBox.getChildren().addAll(searchTextField, prevButton, nextButton, foundLabel);

        final ImageView helpImage = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor()));
        final Button helpButton = new Button("", helpImage);
        helpButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent; -fx-effect: null; ");
        helpButton.setOnAction(event
                -> new HelpCtx(this.getClass().getName()).display());

        final Button addAttributesButton = new Button("Add Content Attributes");
        addAttributesButton.setOnAction(event
                -> PluginExecution.withPlugin(new AddContentAttributesPlugin()).executeLater(GraphManager.getDefault().getActiveGraph()));
        final Tooltip aabt = new Tooltip("Adds content related transaction attributes to the graph.");
        addAttributesButton.setTooltip(aabt);

        optionsPane.getItems().addAll(senderAttributesMultiChoiceInput, showToolTip, addAttributesButton, helpButton, contentLabel, contentPerPageChoiceBox);

        contributionsPane = new BorderPane();
        contributionsPane.setPadding(new Insets(PADDING));
        contributionsPane.setCenter(togglesPane);

        pagination.setPageFactory(new Callback<Integer, Node>() {
            public Node call(final Integer pageIndex) {

                // Update the view to the next page and load new messages
                final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                if (activeGraph != null && conversation.getPageNumber() != pageIndex) {
                    final ReadableGraph graph = activeGraph.getReadableGraph();
                    conversation.setContentPerPage(contentPerPageChoiceBox.getValue());
                    conversation.setPageNumber(pageIndex);
                    messages.clear();
                    messages.addAll(conversation.updateMessages(graph));
                    updatePages(conversation.getTotalPages());
                    graph.release();
                }

                content.getChildren().clear();
                content.getChildren().addAll(optionsPane, searchHBox, contributionsPane, bubbles);
                return content;
            }
        });
        getChildren().addAll(pagination, tipsPane);
    }

    public Pagination getPagination() {
        return pagination;
    }

    protected Conversation getConversation() {
        return conversation;
    }

    public void setInProgress() {
        Platform.runLater(() -> getChildren().add(spinner));
        setAlignment(spinner, Pos.CENTER);
    }

    public void setProgressComplete() {
        Platform.runLater(() -> getChildren().remove(spinner));
    }

    public void updatePages(final int totalPages) {
        Platform.runLater(() -> {
            pagination.setPageCount(totalPages);
            if (totalPages > 10) {
                pagination.setMaxPageIndicatorCount(10);
            } else {
                pagination.setMaxPageIndicatorCount(totalPages);
            }
        });
    }

    /**
     * Refresh the UI for the count of 'found'.
     *
     * @param resetCount True if foundCount is to be set to 0, otherwise False to leave foundCount unchanged.
     */
    protected void refreshCountUI(final boolean resetCount) {
        if (resetCount) {
            foundCount = 0;
            searchCount = 0;
        }

        foundLabel.setText(
                StringUtils.isBlank(searchTextField.getText()) || foundCount == 0
                ? ""
                : FOUND_TEXT + (searchCount + 1) + " of " + foundCount
        );
        foundLabel.setStyle(foundCount > 0 ? FOUND_PASS_COLOR : FOUND_FAIL_COLOR);
    }

    /**
     * Highlights the currently visible regions in the Conversation View based on the text currently present in the
     * searchTextField.
     */
    private void highlightRegions(final boolean needsUpdate) {
        foundCount = 0;

        final Map<Integer, ConversationMessage> matches = new HashMap<>();
        final List<ConversationMessage> visibleMessages = conversation.getVisibleMessages();
        final List<ConversationMessage> senderMessages = conversation.getSenderMessages();

        senderMessages.forEach(message -> {
            final List<ConversationContribution> visibleContributions = message.getVisibleContributions();

            visibleContributions.forEach(contribution -> {
                final Region region = contribution.getContent(tipsPane);

                if (region instanceof EnhancedTextArea enhancedTextArea) {
                    foundCount += enhancedTextArea.highlightText(searchTextField.getText());
                    matches.put(foundCount, message);
                }

                if (region instanceof GridPane gridPane) {
                    gridPane.getChildren().forEach(child -> {
                        if (child instanceof EnhancedTextArea textArea) {
                            foundCount += textArea.highlightText(searchTextField.getText());
                            matches.put(foundCount, message);
                        }
                    });
                }
            });
        });
        if (foundCount > 0) {
            if (visibleMessages.contains(matches.get(searchCount))) {
                // If the current match is on the current page, scroll to the bubble
                bubbles.scrollTo(matches.get(searchCount));
            } else if (senderMessages.contains(matches.get(searchCount)) && needsUpdate) {
                // If the next match is on a future page, swap to that page
                final int indexOfSearch = senderMessages.indexOf(matches.get(searchCount));
                final int pageNumber = (int) Math.ceil(indexOfSearch / contentPerPageChoiceBox.getValue());

                pagination.setCurrentPageIndex(pageNumber);
                bubbles.scrollTo(matches.get(searchCount));
            }
        }
    }

    // A VBox to hold a bubble and a sender.
    private class BubbleBox extends GridPane {

        private ConversationSender currentSender = null;
        private List<ConversationContribution> currentContributions = null;

        private ConversationBubble currentBubble = null;
        private Region currentSenderRegion = null;

        private final int contentColumnIndex;

        public BubbleBox(final ConversationMessage message) {
            setVgap(3);

            final ColumnConstraints spaceColumn = new ColumnConstraints();
            spaceColumn.setHgrow(Priority.ALWAYS);
            spaceColumn.setMinWidth(50);
            spaceColumn.setPrefWidth(50);

            final ColumnConstraints contentColumn = new ColumnConstraints();
            contentColumn.setHalignment(message.getConversationSide() == ConversationSide.LEFT ? HPos.LEFT : HPos.RIGHT);
            contentColumn.setFillWidth(false);
            contentColumn.setHgrow(Priority.NEVER);

            final RowConstraints contentRow = new RowConstraints();
            contentRow.setFillHeight(true);
            contentRow.setValignment(VPos.TOP);
            contentRow.setVgrow(Priority.NEVER);

            getRowConstraints().addAll(contentRow);

            if (message.getConversationSide() == ConversationSide.LEFT) {
                contentColumnIndex = 0;
                getColumnConstraints().addAll(contentColumn, spaceColumn);
            } else {
                contentColumnIndex = 1;
                getColumnConstraints().addAll(spaceColumn, contentColumn);
            }

            update(message);
        }

        public final void update(final ConversationMessage message) {
            final List<ConversationContribution> newContributions = message.getVisibleContributions();

            if (!newContributions.equals(currentContributions)) {
                currentContributions = new ArrayList<>(newContributions);

                final List<Region> rendered = new ArrayList<>();

                newContributions.forEach(contribution -> {
                    final Region region = contribution.getContent(tipsPane);
                    rendered.add(region);
                });

                final ConversationBubble bubble = new ConversationBubble(rendered, message, tipsPane);
                if (currentBubble != null) {
                    getChildren().remove(currentBubble);
                }
                add(bubble, contentColumnIndex, 0);
                currentBubble = bubble;
            }

            final ConversationSender newSender = message.getSenderContent();
            if (newSender != currentSender) {
                currentSender = newSender;
                final Region senderContent = newSender.createContent();
                if (currentSenderRegion != null) {
                    getChildren().remove(currentSenderRegion);
                }
                add(senderContent, contentColumnIndex, 1);
                currentSenderRegion = senderContent;
            }
        }
    }

    // A ListView cell that holds a BubbleBox as its graphic.
    private class BubbleCell extends ListCell<ConversationMessage> {

        public BubbleCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setWrapText(true);
            setPrefWidth(0);
        }

        @Override
        protected double computePrefHeight(final double width) {
            final Node graphic = getGraphic();
            if (graphic == null) {
                return super.computePrefHeight(width);
            } else {
                final Insets padding = getPadding();
                final double newWidth = width - padding.getLeft() + padding.getRight();
                return graphic.prefHeight(newWidth) + padding.getTop() + padding.getBottom();
            }
        }

        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        @Override
        protected void updateItem(final ConversationMessage message, final boolean empty) {
            super.updateItem(message, empty);

            // Handle the case where the cell is empty.
            if (empty || message == null) {
                setStyle(CSS_BACKGROUND_COLOR_TRANSPARENT);
                setGraphic(null);
            } else {
                // Look for the bubble in the cache.
                BubbleBox bubbleBox = bubbleCache.get(message);

                if (bubbleBox != null) {
                    // If the bubble is in the cache then update it for
                    // and changes that may have occurred in the message.
                    bubbleBox.update(message);
                } else {
                    // Else make a new bubble for the message.
                    bubbleBox = new BubbleBox(message);
                    bubbleCache.put(message, bubbleBox);
                }
                setStyle("-fx-background-color: " + (DARK_MODE ? message.getBackgroundColor() : " #d8d8d8 ") + "; -fx-padding: 5 5 5 5;");
                setGraphic(bubbleBox);
            }
        }
    }

    /**
     * Sets the visibility of a contribution provider for the Conversation and updates the display of the Conversation
     * accordingly.
     * <p>
     * The update will be run as a plugin.
     *
     * @param contributionProviderName The name of the contribution to update
     * @param visible The desired visibility of the specified contribution provider.
     */
    private void updateContributionProviderVisibility(final String contributionProviderName, final boolean visible) {
        final Graph graph = conversation.getGraphUpdateManager().getActiveGraph();
        if (graph != null) {
            PluginExecution.withPlugin(new UpdateHiddenContributorProviders(visible, contributionProviderName)).executeLater(graph);
        }
    }

    /**
     * Sets the list of attributes to show for the sender in a Conversation and updates the display of the Conversation
     * accordingly.
     * <p>
     * The update will be run as a plugin.
     *
     * @param senderAttributes The list of String labels of graph attributes to show for the sender
     */
    private void updateSenderAttributes(final List<String> senderAttributes) {
        final Graph graph = conversation.getGraphUpdateManager().getActiveGraph();
        if (graph != null) {
            PluginExecution.withPlugin(new UpdateSenderAttributes(senderAttributes)).executeLater(graph);
        }
    }

    /**
     * Plugin to update hidden contribution providers.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
    private class UpdateHiddenContributorProviders extends SimpleEditPlugin {

        private final boolean visible;
        private final String contributionProviderName;

        public UpdateHiddenContributorProviders(final boolean visible, final String contributionProviderName) {
            this.visible = visible;
            this.contributionProviderName = contributionProviderName;
        }

        @Override
        public String getName() {
            return "Conversation View: Update Hidden Contribution Providers";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int stateAttribute = ConversationViewConcept.MetaAttribute.CONVERSATION_VIEW_STATE.ensure(graph);
            final ConversationState originalState = (ConversationState) graph.getObjectValue(stateAttribute, 0);
            final ConversationState newState = new ConversationState(originalState);
            if (originalState == null) {
                newState.setSenderAttributesToKeys(graph);
            }
            if (visible) {
                if (newState.getHiddenContributionProviders().remove(contributionProviderName)) {
                    graph.setObjectValue(stateAttribute, 0, newState);
                }
            } else if (newState.getHiddenContributionProviders().add(contributionProviderName)) {
                graph.setObjectValue(stateAttribute, 0, newState);
            }
        }
    }

    /**
     * Plugin to update sender attributes.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
    private class UpdateSenderAttributes extends SimpleEditPlugin {

        private final List<String> senderAttributes;

        public UpdateSenderAttributes(final List<String> senderAttributes) {
            this.senderAttributes = Collections.unmodifiableList(senderAttributes);
        }

        @Override
        public String getName() {
            return "Conversation View: Update Sender Attributes";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int stateAttribute = ConversationViewConcept.MetaAttribute.CONVERSATION_VIEW_STATE.ensure(graph);
            final ConversationState originalState = (ConversationState) graph.getObjectValue(stateAttribute, 0);
            final ConversationState newState = new ConversationState(originalState);
            if (!senderAttributes.equals(newState.getSenderAttributes())) {
                newState.getSenderAttributes().clear();
                newState.getSenderAttributes().addAll(senderAttributes);
                graph.setObjectValue(stateAttribute, 0, newState);
            }
        }
    }
}
