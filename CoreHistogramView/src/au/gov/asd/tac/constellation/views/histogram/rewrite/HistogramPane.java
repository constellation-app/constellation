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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openide.util.HelpCtx;

/**
 *
 * @author Quasar985
 */
public class HistogramPane extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(HistogramPane.class.getName());
    private static final String HISTOGRAM_TOP_COMPONENT_CLASS_NAME = HistogramTopComponent2.class.getName();

    private static final String NO_PLUGINS_SELECTED_TITLE = "No plugins selected.";

    // Styles
    private static final String HELP_STYLE = "-fx-border-color: transparent; -fx-background-color: transparent; -fx-effect: null;";
    private static final String BUTTON_STYLE = "-fx-border-color: grey; -fx-border-width: 1px;  -fx-border-radius: 3px; -fx-background-color: transparent;";

    // Images
    private static final ImageView HELP_ICON = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor()));

    private static final ImageView NODE_ICON = new ImageView(UserInterfaceIconProvider.NODES.buildImage(16));
    private static final ImageView TRANSACTION_ICON = new ImageView(UserInterfaceIconProvider.TRANSACTIONS.buildImage(16));
    private static final ImageView EDGE_ICON = new ImageView(UserInterfaceIconProvider.EDGES.buildImage(16));
    private static final ImageView LINK_ICON = new ImageView(UserInterfaceIconProvider.LINKS.buildImage(16));

    // Tooltips
    private static final String HELP_TOOLTIP = "Display help for Data Access";

    private final Button helpButton;
    //private final HistogramTopComponent2 topComponent;

    private final VBox viewPane;

    public HistogramPane(final HistogramController histogramContoller) {

        // TODO: replace set style for each button with a stylesheet
        //getStylesheets().add(getClass().getResource("resources/rule-pane-dark.css").toExternalForm());
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

        AnchorPane.setRightAnchor(helpButton, 5.0);

        ////////////////////
        // Graph Element
        ////////////////////
        final Button nodeButton = new Button("Node", NODE_ICON);
        final Button transactionButton = new Button("Transaction", TRANSACTION_ICON);
        final Button edgeButton = new Button("Edge", EDGE_ICON);
        final Button linkButton = new Button("Link", LINK_ICON);

        nodeButton.setStyle(BUTTON_STYLE);
        transactionButton.setStyle(BUTTON_STYLE);
        edgeButton.setStyle(BUTTON_STYLE);
        linkButton.setStyle(BUTTON_STYLE);

        final Label graphElementLabel = new Label("Graph Element");

        final HBox graphElementHBox = new HBox(4);
        graphElementHBox.getChildren().addAll(graphElementLabel, nodeButton, transactionButton, edgeButton, linkButton);

        ////////////////////
        // Category
        ////////////////////
        final ComboBox categoryChoice = new ComboBox();
        categoryChoice.getItems().addAll("");
        final Label categoryLabel = new Label("Category:");
        final HBox categoryHBox = new HBox(4);
        categoryHBox.getChildren().addAll(categoryLabel, categoryChoice);

        ////////////////////
        // Property and Format
        ////////////////////
        final ComboBox propertyChoice = new ComboBox();
        propertyChoice.getItems().addAll("");
        final ComboBox formatChoice = new ComboBox();
        formatChoice.getItems().addAll("");

        final Label propertyLabel = new Label("Property:");
        final HBox propertyHBox = new HBox(4);
        propertyHBox.getChildren().addAll(propertyLabel, propertyChoice, formatChoice);

        ////////////////////
        // Sort
        ////////////////////
        final ComboBox sortChoice = new ComboBox();
//        for (BinComparator binComparator : BinComparator.values()) {
//            if (binComparator.isAscending()) {
//                sortChoice.add(binComparator);
//            }
//        }
        final Label sortLabel = new Label("Sort:");
        final HBox sortHBox = new HBox(4);
        sortHBox.getChildren().addAll(sortLabel, sortChoice);

        ////////////////////
        // Selection Mode
        ////////////////////
        final ComboBox selectionModeChoice = new ComboBox();
        final Label selectionModeLabel = new Label("Selection Mode:");
        final HBox selectionModeHBox = new HBox(4);
        selectionModeHBox.getChildren().addAll(selectionModeLabel, selectionModeChoice);

        ////////////////////
        // Filter
        ////////////////////
        final Button filterOnSelectionButton = new Button("Filter On Selection");
        final Button clearFilterButton = new Button("Clear Filter");
        final Label filterLabel = new Label("Filter:");
        final HBox filterHBox = new HBox(4);
        filterHBox.getChildren().addAll(filterLabel, filterOnSelectionButton, clearFilterButton);

        // Add everything to this viewPane
        viewPane = new VBox();
        viewPane.prefWidthProperty().bind(this.widthProperty());

        viewPane.getChildren().addAll(
                helpButton,
                graphElementHBox,
                categoryHBox,
                propertyHBox,
                sortHBox,
                selectionModeHBox,
                filterHBox
        );

        this.setCenter(viewPane);
    }

    /*
public final void setHistogramState(HistogramState histogramState, Map<String, BinCreator> attributes) {

        isAdjusting = true;

        if (histogramState != currentHistogramState) {

            if (histogramState == null) {

                vertexToggle.setSelected(true);
                etToggles.values().stream().forEach(toggle -> toggle.setEnabled(false));

                attributeTypeChoice.setSelectedIndex(0);
                attributeTypeChoice.setEnabled(false);

                attributeChoiceModel.removeAllElements();
                attributeChoiceModel.addElement("");
                attributeChoice.setSelectedIndex(0);
                attributeChoice.setEnabled(false);

                binFormatterChoiceModel.removeAllElements();
                binFormatterChoiceModel.addElement(BinFormatter.DEFAULT_BIN_FORMATTER);
                binFormatterCombo.setSelectedIndex(0);
                binFormatterCombo.setEnabled(false);

                sortChoice.setSelectedIndex(0);
                sortChoice.setEnabled(false);

                descendingButton.setSelected(false);
                descendingButton.setEnabled(false);

                selectionModeChoice.setSelectedIndex(0);
                selectionModeChoice.setEnabled(false);

                selectButton.setEnabled(false);

                filterSelectionButton.setEnabled(false);
                clearFilterButton.setEnabled(false);

            } else {

                if (currentHistogramState == null) {
                    etToggles.values().stream().forEach(toggle -> toggle.setEnabled(true));
                    attributeTypeChoice.setEnabled(true);
                    attributeChoice.setEnabled(true);
                    binFormatterCombo.setEnabled(true);
                    sortChoice.setEnabled(true);
                    descendingButton.setEnabled(true);
                    selectionModeChoice.setEnabled(true);
                }

                etToggles.get(histogramState.getElementType()).setSelected(true);

                attributeTypeChoice.removeAllItems();
                for (AttributeType attributeType : AttributeType.values()) {
                    if (attributeType.appliesToElementType(histogramState.getElementType())) {
                        attributeTypeChoice.addItem(attributeType);
                    }
                }
                attributeTypeChoice.setSelectedItem(histogramState.getAttributeType());

                attributeChoiceModel.removeAllElements();
                attributes.keySet().stream().forEach(attribute -> attributeChoiceModel.addElement(attribute));
                attributeChoice.setSelectedItem(histogramState.getAttribute());

                binFormatterChoiceModel.removeAllElements();
                if (histogramState.getAttribute() != null) {
                    Bin checkBin = attributes.get(histogramState.getAttribute()).getBin();
                    if (checkBin != null) {
                        checkBin = checkBin.create();
                        final ReadableGraph rg = topComponent.currentGraph.getReadableGraph();
                        try {
                            if (histogramState.getAttributeType().getBinCreatorsGraphElementType() == null) {
                                checkBin.init(rg, rg.getAttribute(histogramState.getElementType(), histogramState.getAttribute()));
                            } else {
                                checkBin.init(rg, rg.getAttribute(histogramState.getAttributeType().getBinCreatorsGraphElementType(), histogramState.getAttribute()));
                            }
                        } finally {
                            rg.release();
                        }
                    }
                    BinFormatter.getFormatters(checkBin).stream().forEach(formatter -> binFormatterChoiceModel.addElement(formatter));
                    binFormatterCombo.setSelectedItem(histogramState.getBinFormatter());
                }

                if (histogramState.getBinComparator().isAscending()) {
                    descendingButton.setSelected(false);
                    sortChoice.setSelectedItem(histogramState.getBinComparator());
                } else {
                    descendingButton.setSelected(true);
                    sortChoice.setSelectedItem(histogramState.getBinComparator().getReverse());
                }

                selectionModeChoice.setSelectedItem(histogramState.getBinSelectionMode());

                selectButton.setEnabled(histogramState.getBinSelectionMode() != BinSelectionMode.FREE_SELECTION);

                filterSelectionButton.setEnabled(true);
                clearFilterButton.setEnabled(histogramState.getFilter(histogramState.getElementType()) != null);
            }

            currentHistogramState = histogramState;

        } else {

            attributeChoiceModel.removeAllElements();
            if (attributes != null) {
                attributes.keySet().stream().forEach(attribute -> attributeChoiceModel.addElement(attribute));
                if (currentHistogramState != null) {
                    attributeChoice.setSelectedItem(currentHistogramState.getAttribute());
                } else {
                    attributeChoice.setSelectedIndex(0);
                }
            } else {
                attributeChoiceModel.addElement("");
                attributeChoice.setSelectedIndex(0);
            }
        }

        isAdjusting = false;
    }
     */
}
