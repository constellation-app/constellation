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
package au.gov.asd.tac.constellation.views.histogram.rewrite;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.histogram.AttributeType;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.BinCollection;
import au.gov.asd.tac.constellation.views.histogram.BinComparator;
import au.gov.asd.tac.constellation.views.histogram.BinCreator;
import au.gov.asd.tac.constellation.views.histogram.BinIconMode;
import au.gov.asd.tac.constellation.views.histogram.BinSelectionMode;
import static au.gov.asd.tac.constellation.views.histogram.HistogramControls.CURRENT_PARAMETER_IDS;
import au.gov.asd.tac.constellation.views.histogram.HistogramState;
import au.gov.asd.tac.constellation.views.histogram.formats.BinFormatter;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.MenuItem;
import java.util.EnumMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

/**
 * Pane which holds the controls and display of the histogram
 *
 * @author Quasar985
 */
public class HistogramPane extends BorderPane {

    private static final String HISTOGRAM_TOP_COMPONENT_CLASS_NAME = HistogramTopComponent2.class.getName();

    // Styles
    private static final String HELP_STYLE = "-fx-border-color: transparent; -fx-background-color: transparent; -fx-effect: null;";
    private static final String BUTTON_STYLE = "-fx-border-color: grey; -fx-border-width: 1px;  -fx-border-radius: 3px;";

    // Images
    private static final ImageView HELP_ICON = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor()));

    private static final ImageView NODE_ICON = new ImageView(UserInterfaceIconProvider.NODES.buildImage(16));
    private static final ImageView TRANSACTION_ICON = new ImageView(UserInterfaceIconProvider.TRANSACTIONS.buildImage(16));
    private static final ImageView EDGE_ICON = new ImageView(UserInterfaceIconProvider.EDGES.buildImage(16));
    private static final ImageView LINK_ICON = new ImageView(UserInterfaceIconProvider.LINKS.buildImage(16));

    // Tooltips
    private static final String HELP_TOOLTIP = "Display help for Data Access";

    private final Button helpButton;
    private final HistogramTopComponent2 topComponent;

    private final VBox viewPane;

    private boolean isAdjusting = false;
    private HistogramState currentHistogramState = null;

    private final ToggleButton vertexToggle;
    private final ToggleButton transactionToggle;
    private final ToggleButton edgeToggle;
    private final ToggleButton linkToggle;

    private final EnumMap<GraphElementType, ToggleButton> etToggles;

    private final ComboBox categoryChoice;
    private final ComboBox propertyChoice;

    private final ComboBox binFormatterCombo;

    private final ComboBox sortChoice;
    private final ToggleButton descendingButton = new ToggleButton();

    private final ComboBox selectionModeChoice;

    private final Button selectButton;

    private final Button filterSelectionButton;
    private final Button clearFilterButton;

    private final Button actionButton;

    private final ContextMenu actionsMenu;

    private final HistogramDisplay2 display;

    private static final int DISPLAY_PADDING = 10;

    private static final int SELECT_BUTTON_WIDTH = 100;
    private static final int DESCENDING_BUTTON_WIDTH = 100;

    private static final int MIN_LABEL_WIDTH = 90;
    private static final int PREF_LABEL_WIDTH = 90;

    public HistogramPane(final HistogramTopComponent2 histogramTopComponent) {
        topComponent = histogramTopComponent;

        // DISPLAY
        display = new HistogramDisplay2(topComponent);
        final ScrollPane displayScroll = new ScrollPane();
        displayScroll.setContent(display);

        // Binds the width of the display to the width of viewable content in the scroll pane
        displayScroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> display.setPrefWidth(newVal.getWidth()));

        display.setPadding(new Insets(0, DISPLAY_PADDING, 0, DISPLAY_PADDING)); // padding of 10 on left and right
        displayScroll.setHbarPolicy(ScrollBarPolicy.NEVER);

        ////////////////////
        // Help Button
        ////////////////////
        helpButton = new Button("", HELP_ICON);
        helpButton.paddingProperty().set(new Insets(0, 8, 0, 0));
        helpButton.setTooltip(new Tooltip(HELP_TOOLTIP));
        helpButton.setStyle(HELP_STYLE);

        helpButton.setOnAction(actionEvent -> {
            new HelpCtx(HISTOGRAM_TOP_COMPONENT_CLASS_NAME).display();
            actionEvent.consume();
        });

        ////////////////////
        // Graph Element
        ////////////////////
        vertexToggle = new ToggleButton("Node", NODE_ICON);
        vertexToggle.setOnAction(e -> toggleStateChanged(GraphElementType.VERTEX));

        transactionToggle = new ToggleButton("Transaction", TRANSACTION_ICON);
        transactionToggle.setOnAction(e -> toggleStateChanged(GraphElementType.TRANSACTION));

        edgeToggle = new ToggleButton("Edge", EDGE_ICON);
        edgeToggle.setOnAction(e -> toggleStateChanged(GraphElementType.EDGE));

        linkToggle = new ToggleButton("Link", LINK_ICON);
        linkToggle.setOnAction(e -> toggleStateChanged(GraphElementType.LINK));

        vertexToggle.setStyle(BUTTON_STYLE);
        transactionToggle.setStyle(BUTTON_STYLE);
        edgeToggle.setStyle(BUTTON_STYLE);
        linkToggle.setStyle(BUTTON_STYLE);

        // Put all buttons in a group
        final ToggleGroup graphElementGroup = new ToggleGroup();
        vertexToggle.setToggleGroup(graphElementGroup);
        transactionToggle.setToggleGroup(graphElementGroup);
        edgeToggle.setToggleGroup(graphElementGroup);
        linkToggle.setToggleGroup(graphElementGroup);

        // Put in enum map
        etToggles = new EnumMap<>(GraphElementType.class);
        etToggles.put(GraphElementType.VERTEX, vertexToggle);
        etToggles.put(GraphElementType.TRANSACTION, transactionToggle);
        etToggles.put(GraphElementType.EDGE, edgeToggle);
        etToggles.put(GraphElementType.LINK, linkToggle);

        final Label graphElementLabel = new Label("Graph Element");
        graphElementLabel.setMinWidth(MIN_LABEL_WIDTH);
        graphElementLabel.setPrefWidth(PREF_LABEL_WIDTH);

        final HBox graphElementHBox = new HBox(4);
        final Pane elementBoxSpacer = new Pane();
        HBox.setHgrow(elementBoxSpacer, Priority.ALWAYS);
        graphElementHBox.getChildren().addAll(graphElementLabel, vertexToggle, transactionToggle, edgeToggle, linkToggle, elementBoxSpacer, helpButton);

        ////////////////////
        // Category
        ////////////////////
        categoryChoice = new ComboBox();
        categoryChoice.getItems().addAll("");
        categoryChoice.setOnAction(e -> categoryChoiceHandler());
        categoryChoice.setMaxWidth(Double.MAX_VALUE);

        final Label categoryLabel = new Label("Category:");
        categoryLabel.setMinWidth(MIN_LABEL_WIDTH);
        categoryLabel.setPrefWidth(PREF_LABEL_WIDTH);
        final HBox categoryHBox = new HBox(4);
        HBox.setHgrow(categoryChoice, Priority.ALWAYS);
        categoryHBox.getChildren().addAll(categoryLabel, categoryChoice);

        ////////////////////
        // Property and Format
        ////////////////////
        propertyChoice = new ComboBox();
        propertyChoice.setOnAction(e -> propertyChoiceHandler());
        propertyChoice.getItems().add("");
        propertyChoice.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(propertyChoice, Priority.ALWAYS);

        binFormatterCombo = new ComboBox();
        binFormatterCombo.setOnAction(e -> binFormatterComboHandler());
        binFormatterCombo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(binFormatterCombo, Priority.ALWAYS);

        final Label propertyLabel = new Label("Property:");
        propertyLabel.setMinWidth(MIN_LABEL_WIDTH);
        propertyLabel.setPrefWidth(PREF_LABEL_WIDTH);
        final HBox propertyHBox = new HBox(4);
        propertyHBox.getChildren().addAll(propertyLabel, propertyChoice, binFormatterCombo);

        ////////////////////
        // Sort
        ////////////////////
        sortChoice = new ComboBox();
        sortChoice.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(sortChoice, Priority.ALWAYS);
        sortChoice.setOnAction(e -> sortChoiceHandler());
        for (final BinComparator binComparator : BinComparator.values()) {
            if (binComparator.isAscending()) {
                sortChoice.getItems().add(binComparator);
            }
        }

        final Label sortLabel = new Label("Sort:");
        sortLabel.setMinWidth(MIN_LABEL_WIDTH);
        sortLabel.setPrefWidth(PREF_LABEL_WIDTH);
        final HBox sortHBox = new HBox(4);

        final Image unselected = new Image("/au/gov/asd/tac/constellation/views/histogram/resources/down.png");
        final Image selected = new Image("/au/gov/asd/tac/constellation/views/histogram/resources/up.png");
        final ImageView toggleImage = new ImageView();

        toggleImage.imageProperty().bind(Bindings
                .when(descendingButton.selectedProperty())
                .then(selected)
                .otherwise(unselected)
        );

        descendingButton.setGraphic(toggleImage);
        descendingButton.setMinWidth(DESCENDING_BUTTON_WIDTH);
        descendingButton.setOnAction(e -> descendingButtonHandler());

        sortHBox.getChildren().addAll(sortLabel, sortChoice, descendingButton);

        ////////////////////
        // Selection Mode
        ////////////////////
        selectionModeChoice = new ComboBox();
        selectionModeChoice.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(selectionModeChoice, Priority.ALWAYS);
        selectionModeChoice.setOnAction(e -> selectionModeChoiceHandler());

        selectionModeChoice.getItems().addAll((Object[]) BinSelectionMode.values());

        final Label selectionModeLabel = new Label("Selection Mode:");
        selectionModeLabel.setMinWidth(MIN_LABEL_WIDTH);
        selectionModeLabel.setPrefWidth(PREF_LABEL_WIDTH);
        final HBox selectionModeHBox = new HBox(4);

        selectButton = new Button("Select");
        selectButton.setMinWidth(SELECT_BUTTON_WIDTH);
        selectButton.setOnAction(e -> selectButtonHandler());

        selectionModeHBox.getChildren().addAll(selectionModeLabel, selectionModeChoice, selectButton);

        ////////////////////
        // Filter
        ////////////////////
        filterSelectionButton = new Button("Filter On Selection");
        filterSelectionButton.setOnAction(e -> filterSelection());

        clearFilterButton = new Button("Clear Filter");
        clearFilterButton.setOnAction(e -> clearFilter());

        actionButton = new Button("Action");
        actionButton.setOnMouseClicked(e -> actionButtonMousePressed(e));
        final Label filterLabel = new Label("Filter:");
        filterLabel.setMinWidth(MIN_LABEL_WIDTH);
        filterLabel.setPrefWidth(PREF_LABEL_WIDTH);
        final HBox filterHBox = new HBox(4);
        final Pane filterSpacer = new Pane();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        filterHBox.getChildren().addAll(filterLabel, filterSelectionButton, clearFilterButton, filterSpacer, actionButton);

        actionsMenu = new ContextMenu();

        final MenuItem saveBinsToGraphMenuItem = new MenuItem("Save Bins to Graph");
        saveBinsToGraphMenuItem.setOnAction(e -> saveBinsToGraph());
        actionsMenu.getItems().add(saveBinsToGraphMenuItem);

        final MenuItem saveBinsToClipboardMenuItem = new MenuItem("Save Bins to Clipboard");
        saveBinsToClipboardMenuItem.setOnAction(e -> topComponent.saveBinsToClipboard());
        actionsMenu.getItems().add(saveBinsToClipboardMenuItem);

        final MenuItem decreaseHeightBarMenuItem = new MenuItem("Decrease Height of Each Bin");
        decreaseHeightBarMenuItem.setOnAction(e -> decreaseBarHeight());
        actionsMenu.getItems().add(decreaseHeightBarMenuItem);

        final MenuItem increaseHeightBarMenuItem = new MenuItem("Increase Height of Each Bin");
        increaseHeightBarMenuItem.setOnAction(e -> increaseBarHeight());
        actionsMenu.getItems().add(increaseHeightBarMenuItem);
        //// END Filter

        // Add everything to this viewPane
        viewPane = new VBox(4);
        viewPane.prefWidthProperty().bind(this.widthProperty());

        displayScroll.prefWidthProperty().bind(viewPane.widthProperty());

        viewPane.getChildren().addAll(
                displayScroll,
                graphElementHBox,
                categoryHBox,
                propertyHBox,
                sortHBox,
                selectionModeHBox,
                filterHBox
        );

        this.setCenter(viewPane);

        // Update display for initial values
        updateDisplay();
        disableControls();
        setHistogramState(null, null);
    }

    private void disableControls() {
        vertexToggle.setSelected(true);
        etToggles.values().stream().forEach(toggle -> toggle.setDisable(true));

        categoryChoice.getSelectionModel().select(0);
        categoryChoice.setDisable(true);

        propertyChoice.getItems().clear();
        propertyChoice.getItems().add("");
        propertyChoice.getSelectionModel().select(0);
        propertyChoice.setDisable(true);

        // Clear all items
        binFormatterCombo.setItems(FXCollections.observableArrayList());
        binFormatterCombo.getItems().add(BinFormatter.DEFAULT_BIN_FORMATTER);
        binFormatterCombo.getSelectionModel().select(0);
        binFormatterCombo.setDisable(true);

        sortChoice.getSelectionModel().select(0);
        sortChoice.setDisable(true);

        descendingButton.setSelected(false);
        descendingButton.setDisable(true);

        selectionModeChoice.getSelectionModel().select(0);
        selectionModeChoice.setDisable(true);

        selectButton.setDisable(true);

        filterSelectionButton.setDisable(true);
        clearFilterButton.setDisable(true);
    }

    public final void setHistogramState(final HistogramState histogramState, final Map<String, BinCreator> attributes) {
        isAdjusting = true;

        if (histogramState != currentHistogramState) {
            if (histogramState == null) {
                Platform.runLater(() -> disableControls());
            } else {
                if (currentHistogramState == null) {
                    etToggles.values().stream().forEach(toggle -> toggle.setDisable(false));
                    categoryChoice.setDisable(false);
                    propertyChoice.setDisable(false);
                    binFormatterCombo.setDisable(false);
                    sortChoice.setDisable(false);
                    descendingButton.setDisable(false);
                    selectionModeChoice.setDisable(false);
                }

                etToggles.get(histogramState.getElementType()).setSelected(true);

                Platform.runLater(() -> {
                    // Clear all items
                    categoryChoice.setItems(FXCollections.observableArrayList());

                    for (final AttributeType attributeType : AttributeType.values()) {
                        if (attributeType.appliesToElementType(histogramState.getElementType())) {
                            categoryChoice.getItems().add(attributeType);
                        }
                    }
                    categoryChoice.getSelectionModel().select(histogramState.getAttributeType());

                    // Clear all items
                    propertyChoice.setItems(FXCollections.observableArrayList());

                    attributes.keySet().stream().forEach(attribute -> propertyChoice.getItems().add(attribute));
                    propertyChoice.getSelectionModel().select(histogramState.getAttribute());

                    // Clear all items
                    binFormatterCombo.setItems(FXCollections.observableArrayList());

                    if (histogramState.getAttribute() != null) {
                        Bin checkBin = attributes.get(histogramState.getAttribute()).getBin();
                        if (checkBin != null) {
                            checkBin = checkBin.create();

                            try (final ReadableGraph rg = topComponent.getCurrentGraph().getReadableGraph()) {
                                if (histogramState.getAttributeType().getBinCreatorsGraphElementType() == null) {
                                    checkBin.init(rg, rg.getAttribute(histogramState.getElementType(), histogramState.getAttribute()));
                                } else {
                                    checkBin.init(rg, rg.getAttribute(histogramState.getAttributeType().getBinCreatorsGraphElementType(), histogramState.getAttribute()));
                                }
                            }
                        }

                        BinFormatter.getFormatters(checkBin).stream().forEach(formatter -> binFormatterCombo.getItems().add(formatter));
                        binFormatterCombo.getSelectionModel().select(histogramState.getBinFormatter());
                    }
                });

                if (histogramState.getBinComparator().isAscending()) {
                    descendingButton.setSelected(false);
                    sortChoice.getSelectionModel().select(histogramState.getBinComparator());
                } else {
                    descendingButton.setSelected(true);
                    Platform.runLater(() -> sortChoice.getSelectionModel().select(histogramState.getBinComparator().getReverse()));
                }

                Platform.runLater(() -> selectionModeChoice.getSelectionModel().select(histogramState.getBinSelectionMode()));

                selectButton.setDisable(histogramState.getBinSelectionMode() == BinSelectionMode.FREE_SELECTION);

                filterSelectionButton.setDisable(false);
                clearFilterButton.setDisable(histogramState.getFilter(histogramState.getElementType()) == null);
            }

            currentHistogramState = histogramState;

        } else {
            Platform.runLater(() -> {
                // Clear all items
                propertyChoice.setItems(FXCollections.observableArrayList());

                if (attributes != null) {
                    attributes.keySet().stream().forEach(attribute -> propertyChoice.getItems().add(attribute));
                    if (currentHistogramState != null) {
                        propertyChoice.getSelectionModel().select(currentHistogramState.getAttribute());
                    } else {
                        Platform.runLater(() -> propertyChoice.getSelectionModel().select(0));
                    }
                } else {
                    propertyChoice.getItems().add("");
                    Platform.runLater(() -> propertyChoice.getSelectionModel().select(0));
                }
            });
        }

        // Run later to avoid endless loop of updating because clear lists cause updates
        Platform.runLater(() -> isAdjusting = false);
    }

    public void updateDisplay() {
        display.updateDisplay();
    }

    protected void decreaseBarHeight() {
        display.decreaseBarHeight();
    }

    protected void increaseBarHeight() {
        display.increaseBarHeight();
    }

    public void setBinCollection(final BinCollection binCollection, final BinIconMode binIconMode) {
        display.setBinCollection(binCollection, binIconMode);
    }

    public void setBinSelectionMode(final BinSelectionMode binSelectionMode) {
        display.setBinSelectionMode(binSelectionMode);
    }

    public void updateBinCollection() {
        display.updateBinCollection();
    }

    protected void clearFilter() {
        if (!isAdjusting) {
            topComponent.clearFilter();
            updateDisplay();
        }
    }

    protected void filterSelection() {
        if (!isAdjusting) {
            topComponent.filterOnSelection();
            updateDisplay();
        }
    }

    // Handles changes in the category choice combo box
    protected void categoryChoiceHandler() {
        if (!isAdjusting) {
            final AttributeType newValue = (AttributeType) categoryChoice.getValue();
            if (newValue != null) {
                topComponent.setAttributeType(newValue);
                updateDisplay();
            }
        }
    }

    // Handles changes in the select button
    protected void selectButtonHandler() {
        if (!isAdjusting) {
            currentHistogramState.getBinSelectionMode().select(topComponent);
            updateDisplay();
        }
    }

    // Handles changes in the selection mode choice combo box
    protected void selectionModeChoiceHandler() {
        if (!isAdjusting) {
            topComponent.setBinSelectionMode((BinSelectionMode) selectionModeChoice.getValue());
            updateDisplay();
        }
    }

    // Handles changes in the descending button
    protected void descendingButtonHandler() {
        if (!isAdjusting) {
            updateBinComparator();
            updateDisplay();
        }
    }

    // Handles changes in the sort choice combo box
    protected void sortChoiceHandler() {
        if (!isAdjusting) {
            updateBinComparator();
            updateDisplay();
        }
    }

    // Handles changes in the property choice combo box
    protected void propertyChoiceHandler() {
        if (!isAdjusting) {
            topComponent.setAttribute((String) propertyChoice.getValue());
            updateDisplay();
        }
    }

    // Handles changes in the bin formatter combo box
    private void binFormatterComboHandler() {
        if (isAdjusting) {
            return;
        }

        final BinFormatter binFormatter = (BinFormatter) binFormatterCombo.getValue();
        if (binFormatter == null) {
            return;
        }

        final PluginParameters parameters;
        if (currentHistogramState != null && binFormatter.getClass() == currentHistogramState.getBinFormatter().getClass()) {
            PluginParameters p = currentHistogramState.getBinFormatterParameters();
            parameters = p == null ? null : p.copy();
            binFormatter.updateParameters(parameters);
        } else if (CURRENT_PARAMETER_IDS.containsKey(binFormatter)) {
            parameters = CURRENT_PARAMETER_IDS.get(binFormatter).copy();
            binFormatter.updateParameters(parameters);
        } else {
            parameters = binFormatter.createParameters();
            binFormatter.updateParameters(parameters);
        }

        if (parameters != null) {
            final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(binFormatter.getLabel(), parameters);

            final Thread newThread = new Thread(() -> {
                final Object r = dialog.showAndReturnDisplay(true);

                if (r == DialogDescriptor.OK_OPTION) {
                    dialog.storeRecentParameterValues();
                    CURRENT_PARAMETER_IDS.put(binFormatter, parameters.copy());
                    Platform.runLater(() -> topComponent.setBinFormatter((BinFormatter) binFormatterCombo.getValue(), parameters));
                } else if (currentHistogramState != null) {
                    Platform.runLater(() -> binFormatterCombo.getSelectionModel().select(currentHistogramState.getBinFormatter()));
                }
            });

            newThread.start();
        } else {
            topComponent.setBinFormatter((BinFormatter) binFormatterCombo.getValue(), null);
        }
        updateDisplay();
    }

    // Handle when action button is pressed
    protected void actionButtonMousePressed(final MouseEvent evt) {
        actionsMenu.show(actionButton, evt.getScreenX(), evt.getScreenY());
        updateDisplay();
    }

    // Handle when vertex, transaction, edge or link toggle buttons change state
    protected void toggleStateChanged(final GraphElementType state) {
        if (!isAdjusting) {
            topComponent.setGraphElementType(state);
            updateDisplay();
        }
    }

    protected void saveBinsToGraph() {
        topComponent.saveBinsToGraph();
    }

    protected void updateBinComparator() {
        BinComparator binComparator = (BinComparator) sortChoice.getValue();
        topComponent.setBinComparator(this.descendingButton.isSelected() ? binComparator.getReverse() : binComparator);
    }

}
