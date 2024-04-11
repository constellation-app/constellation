/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.ClipboardUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The main JavaFx Pane with which to visualise word clouds and run the plugin
 * to generate them
 *
 * @author twilight_sparkle
 */
public class WordCloudPane extends BorderPane {

    private final WordCloudController controller;

    private final SplitPane everything;
    private final VBox theCloud;
    private final Label queryInfoLabel;
    private final ScrollPane wordHolder;
    private final FlowPane words;
    private final BorderPane buttonBarPane;
    private final Slider slider;
    private final ScrollPane topPart;
    private final BorderPane sliderBarPane;

    private final ToggleGroup modeButtons;
    private final ToggleButton union;
    private final ToggleButton intersection;
    private final ToggleGroup sortingButtons;
    private final ToggleButton alphabetical;
    private final ToggleButton frequency;
    private final Map<String, Hyperlink> wordButtons;

    private final ProgressIndicator spinner = new ProgressIndicator();
    private final WordCloudParametersPane paramPane;
    private final ScrollPane paramScrollPane;

    private static final int CLOUD_WIDTH = 500;
    private static final int HGAP_BETWEEN_WORDS = 4;
    private static final int VGAP_BETWEEN_WORDS = 2;
    private static final Insets WORDCLOUD_PADDING = new Insets(10, 5, 10, 5);
    private static final int CLOUD_SPACING = 10;

    private final TooltipPane tipsPane = new TooltipPane();
    private final StackPane cloudStackPane = new StackPane();
    protected CheckBox showTooltipsCheckbox;

    private final Hyperlink noWord;

    // The mutliplicative size difference between the smallest and largest words in the cloud 
    private static final int FONT_EXPANSION_FACTOR = 3;

    /**
     * Constructs a WordCloudPane to be controlled by the specified controller
     */
    public WordCloudPane(final WordCloudController controller) {
        everything = new SplitPane();
        everything.setOrientation(Orientation.VERTICAL);
        everything.setDividerPositions(0.5F);
        this.controller = controller;
        setPadding(WORDCLOUD_PADDING);
        setTop(everything);

        // Scroll pane for entire top of word cloud view
        topPart = new ScrollPane();
        topPart.setFitToHeight(true);
        topPart.setFitToWidth(true);

        // Create container for vertical spacing, holds wordHolder, buttons and sliders
        theCloud = new VBox(CLOUD_SPACING);

        // add the tips pane and the cloud to a stack pane
        cloudStackPane.getChildren().add(theCloud);
        cloudStackPane.getChildren().add(tipsPane);
        topPart.setContent(cloudStackPane);

        // Create the label used to give information about the parameters used to generate the word cloud 
        queryInfoLabel = new Label("");
        queryInfoLabel.prefWidthProperty().bind(theCloud.widthProperty());
        queryInfoLabel.setAlignment(Pos.CENTER);
        queryInfoLabel.setId("query-label");
        theCloud.getChildren().add(queryInfoLabel);

        // Create a flow pane to display the words themselves, and a scroll pane to allow scrolling on the flow pane. Set and bind widths and heights correctly
        wordHolder = new ScrollPane();
        words = new FlowPane(HGAP_BETWEEN_WORDS, VGAP_BETWEEN_WORDS);
        words.setPrefWrapLength(CLOUD_WIDTH);
        wordHolder.setContent(words);
        wordHolder.prefViewportHeightProperty().bind(words.heightProperty());
        wordHolder.setFitToWidth(true);
        wordHolder.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        theCloud.getChildren().add(wordHolder);

        // Create two toggle buttons in a toggle group to change the selection mode
        union = new ToggleButton("Union");
        intersection = new ToggleButton("Intersection");
        modeButtons = new ToggleGroup();
        union.setToggleGroup(modeButtons);
        intersection.setToggleGroup(modeButtons);

        // Add the change listener to the toggle group which sets selection mode in the controller 
        modeButtons.selectedToggleProperty().addListener((final ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) -> {
            if (t1 != null) {
                if (((ToggleButton) t1).equals(union)) {
                    controller.setIsUnionSelect(true);
                } else if (((ToggleButton) t1).equals(intersection)) {
                    controller.setIsUnionSelect(false);
                }
            }

            // Disallow deselection
            if (t1 == null) {
                modeButtons.selectToggle(t);
            }
        });

        // Create two toggle buttons in a toggle group to change the sorting mode
        alphabetical = new ToggleButton("Alphabetical");
        frequency = new ToggleButton("Frequency");
        sortingButtons = new ToggleGroup();
        alphabetical.setToggleGroup(sortingButtons);
        frequency.setToggleGroup(sortingButtons);

        // Add the change listener to the toggle group which sets sorting mode in the controller
        sortingButtons.selectedToggleProperty().addListener((final ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) -> {
            if (t1 != null) {
                if (((ToggleButton) t1).equals(alphabetical)) {
                    controller.setIsSizeSorted(false);
                } else if (((ToggleButton) t1).equals(frequency)) {
                    controller.setIsSizeSorted(true);
                }
            }

            // Disallow deselection
            if (t1 == null) {
                sortingButtons.selectToggle(t);
            }
        });

        // Create border pane for the buttons that were just constructed and add these buttons to the pane 
        buttonBarPane = new BorderPane();

        // Group buttons together
        final HBox modeButtonBar = new HBox();
        modeButtonBar.getChildren().addAll(union, intersection);
        final HBox sortingButtonBar = new HBox();
        sortingButtonBar.getChildren().addAll(alphabetical, frequency);
        showTooltipsCheckbox = new CheckBox("Hovering translations");
        showTooltipsCheckbox.setSelected(true);
        showTooltipsCheckbox.setOnAction((final ActionEvent t)
                -> tipsPane.setEnabled(showTooltipsCheckbox.isSelected()));

        showTooltipsCheckbox.setStyle("fx-text-fill: white; -fx-padding: 2;");

        // Add buttons to bar
        buttonBarPane.setLeft(modeButtonBar);
        buttonBarPane.setCenter(showTooltipsCheckbox);
        buttonBarPane.setRight(sortingButtonBar);

        // Add bar to cloud
        theCloud.getChildren().add(buttonBarPane);

        // Create a slider beneath the buttons to be displayed when significance levels are in play 
        sliderBarPane = new BorderPane();

        slider = new Slider(0, 1, 0.5);
        final Label significanceLabel = new Label(String.format("Significance: %.2g", 0.5));
        significanceLabel.setStyle("-fx-text-fill: #d4d4d4;");
        slider.setPrefWidth(400);

        slider.valueProperty().addListener((final ObservableValue<? extends Number> obv, final Number oldVal, final Number newVal) -> {
            double newSignificance = newVal.doubleValue();
            significanceLabel.setText(String.format("Significance: %.2g", newSignificance));
            controller.setSignificance(newSignificance);
        });

        // Add label and slider to pane
        sliderBarPane.setLeft(significanceLabel);
        sliderBarPane.setRight(slider);

        // Create the pane allowing the word cloud analytic to be run
        paramPane = new WordCloudParametersPane(this);
        paramScrollPane = new ScrollPane();
        paramScrollPane.setContent(paramPane);
        paramScrollPane.setFitToWidth(true);
        paramScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Add params and cloud to split pane
        everything.getItems().addAll(topPart, paramScrollPane);

        spinner.setMaxSize(50, 50);
        wordButtons = new HashMap<>();
        noWord = new Hyperlink();
        noWord.setMaxSize(0, 0);
    }

    /**
     * Sets the height of the whole word cloud pane and adjusts elements to fit
     *
     * @param height the new height of the pane
     */
    public void setContentHeight(final int height) {
        everything.setMaxHeight(height - 10);// need 10 pixels for scroll bar on bottom
        paramScrollPane.setPrefHeight(height);
        theCloud.setPrefHeight(height);
    }

    public double getContentHeight() {
        return everything.getMaxHeight();
    }

    protected StackPane getCloudStackPane() {
        return cloudStackPane;
    }

    protected ProgressIndicator getSpinner() {
        return spinner;
    }

    protected VBox getTheCloud() {
        return theCloud;
    }

    protected BorderPane getSliderBarPane() {
        return sliderBarPane;
    }

    protected Map<String, Hyperlink> getWordButtons() {
        return wordButtons;
    }

    protected FlowPane getWords() {
        return words;
    }

    public void setInProgress() {
        cloudStackPane.getChildren().add(spinner);
        StackPane.setAlignment(spinner, Pos.CENTER);
    }

    public void setProgressComplete() {
        cloudStackPane.getChildren().remove(spinner);
        WordCloudController.getDefault().updateGraph();
    }

    /**
     * Displays and correctly sizes the actual word cloud panes so that the
     * parameters pane is beneath it. Called by the controller when it begins
     * managing a word cloud.
     */
    public void enableTheCloud(final boolean unionButtonSelected, final boolean frequencyButtonSelected, final boolean hasSignificances) {
        theCloud.setVisible(true);
        theCloud.setManaged(true);
        everything.setDividerPositions(0.5F);
        if (unionButtonSelected) {
            modeButtons.selectToggle(union);
        } else {
            modeButtons.selectToggle(intersection);
        }
        if (frequencyButtonSelected) {
            sortingButtons.selectToggle(frequency);
        } else {
            sortingButtons.selectToggle(alphabetical);
        }
        if (hasSignificances && !theCloud.getChildren().contains(sliderBarPane)) {
            theCloud.getChildren().add(sliderBarPane);
        } else if (!hasSignificances && theCloud.getChildren().contains(sliderBarPane)) {
            theCloud.getChildren().remove(sliderBarPane);
        }
    }

    /**
     * Calculates the absolute font size for a word in this cloud based on its
     * "relative size" and the current user font size
     */
    private static int getFontSize(final float relativeSize, final int baseFontSize) {
        return (int) (baseFontSize * (1 + relativeSize * FONT_EXPANSION_FACTOR));
    }

    /**
     * Hides the actual word cloud panes so that the parameters pane is at the
     * top. Called by the controller when it is not managing a word cloud.
     */
    public void disableTheCloud() {
        wordHolder.setMinHeight(0);
        topPart.setMinHeight(0);
        theCloud.setVisible(false);
        theCloud.setManaged(false);
        everything.setDividerPositions(0.0F);
    }

    /**
     * Sets whether the attribute parameters combo-box is enabled on the
     * parameter pane
     */
    public void setAttributeSelectionEnabled(final boolean val) {
        paramPane.setAttributeSelectionEnabled(val);
    }

    /**
     * Request to run the word cloud analytic itself. Called from within
     * parameters pane, this method simple passes the request up to the
     * controller
     */
    public void runPlugin(final PluginParameters params) {
        controller.runPlugin(params);
    }

    /**
     * Request to update the parameters on the parameters pane. Called by the
     * controller upon graph attribute changes, this method simple passes the
     * request down to the parameters pane
     */
    public void updateParameters(final List<String> vertTextAttributes, final List<String> transTextAttributes) {
        paramPane.updateParameters(vertTextAttributes, transTextAttributes);
    }

    public void createWords(final SortedMap<String, Float> wordListWithSizes, final String queryInfo, final int baseFontSize) {
        // Set the text in the info label at the top 
        queryInfoLabel.setText(queryInfo);
        // Clear the map of word buttons
        wordButtons.clear();
        // If there are no words to display, we are done
        if (wordListWithSizes == null) {
            return;
        }
        // For each word to display
        wordListWithSizes.keySet().forEach(word -> {
            // Create a hyperlink for the word
            final Hyperlink h = new Hyperlink(word);
            h.setWrapText(true);
            // Set the word's font based on its prescribed size 
            final Font f = Font.font("Arial", FontWeight.BOLD, getFontSize(wordListWithSizes.get(word), baseFontSize));
            h.setFont(f);

            // Set the context menu for copying 
            final ContextMenu menu = new ContextMenu();
            final MenuItem copy = new MenuItem("copy");
            copy.setOnAction((final ActionEvent event)
                    -> ClipboardUtilities.copyToClipboard(word));
            menu.getItems().add(copy);
            h.setContextMenu(menu);
            // Add the event handler for clicking the word. This handler tells the controller to add teh wrod to its word cloud's currently selected word list 
            h.setOnMouseClicked((final MouseEvent event) -> {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    final Hyperlink source = (Hyperlink) event.getSource();
                    boolean deselect = false;
                    // If the control button is not down, display all words as deselected
                    if (!event.isControlDown()) {
                        words.getChildren().forEach(h1 -> ((Hyperlink) h1).setVisited(false));
                    } else {
                        // If the control button is down, check whether the current click is a deselection of the word rather than a selection
                        deselect = source.isVisited();
                    }
                    // Set the selected/deselected display of the clicked work correctly
                    source.setVisited(!deselect);
                    // Inform the controller of the selection change
                    controller.alterSelection(source.getText(), event.isControlDown(), deselect);
                }
            });
            wordButtons.put(word, h);
        });
    }

    public void updateSelection(final Set<String> selectedWords) {
        wordButtons.values().forEach(h
                -> h.setVisited(false));

        if (selectedWords == null) {
            return;
        }

        // Display the word as selected if necessary
        selectedWords.stream().filter(word -> (selectedWords.contains(word))).forEachOrdered(word
                -> wordButtons.get(word).setVisited(true));
    }

    /**
     * Removes and then adds again all words from the flow pane in the specified
     * order
     */
    public void updateWords(final SortedSet<String> wordsToDisplay, final boolean reapplySort) {
        // Delete any existing words from the word cloud flow pane 
        if (reapplySort) {
            words.getChildren().clear();
        }

        // If there are no words to display, we are done
        if (wordsToDisplay == null) {
            return;
        }

        int i = 0;
        final List<Node> wordList = words.getChildren();
        final boolean remove = wordsToDisplay.size() < wordList.size();

        for (final String word : wordsToDisplay) {
            final Hyperlink h = wordButtons.get(word);
            if (remove) {
                while (wordList.size() <= i) {
                    wordList.remove(i);
                }
            } else if (wordList.size() <= i) {
                words.getChildren().add(h);
            } else if (!wordList.get(i).equals(h)) {
                words.getChildren().add(i, h);
            }
            i++;
        }

        if (remove) {
            while (i < wordList.size()) {
                wordList.remove(i);
            }
        }
    }
}
