/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.attributecalculator.panes;

import au.gov.asd.tac.constellation.views.attributecalculator.plugins.CalculatorVariable;
import au.gov.asd.tac.constellation.views.attributecalculator.script.ScriptIO;
import au.gov.asd.tac.constellation.views.attributecalculator.tutorial.AbstractCalculatorTutorial;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author sirius
 */
public final class AttributeCalculatorPane extends GridPane {

    private ComboBox<GraphElementType> elementTypeComboBox = new ComboBox<>();
    private ComboBox<String> attributeComboBox = new ComboBox<>();
    private ComboBox<String> attributeTypeComboBox = new ComboBox<>();
    private CheckBox selectedOnlyCheckBox = new CheckBox("Selected Elements Only");
    private CheckBox completeWithSchemaCheckBox = new CheckBox("Complete with Schema");
    private TextArea scriptTextArea = new TextArea();
    private Button executeButton = new Button("Execute");
    private ComboBox<InsertListCategory> insertComboBox = new ComboBox<>();
    private ListView<InsertListItem> variablesListView = new ListView<>();
    private final Map<String, Attribute> vertexAttributeTypes = new TreeMap<>();
    private final Map<String, Attribute> transactionAttributeTypes = new TreeMap<>();
    private Map<String, Attribute> attributeTypes = vertexAttributeTypes;
    private VBox calculatorControls = new VBox();
    private HBox calculatorExecution = new HBox();
    private SplitPane scriptingSplitPane = new SplitPane();

    private Button saveButton;
    private Button loadButton;
    private MenuButton helpButton;
    private Button deleteButton;
    private ToggleButton sideHelpButton;

    private ScrollPane helpSideBarScrollPane = new ScrollPane();
    private VBox helpSideBar = new VBox();
    private TextFlow attributeDescriptionHelp = new TextFlow();
    private TextFlow templateCategoryHelp = new TextFlow();
    private TextFlow templateObjectHelp = new TextFlow();
    private boolean helpSideBarHidden = false;

    private InsertListCategory savedScriptsCat;

    public AttributeCalculatorPane(final AttributeCalculatorController controller) {

        setPadding(new Insets(5, 5, 5, 5));
        setHgap(5);
        setVgap(5);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setMaxWidth(Double.MAX_VALUE);
        getColumnConstraints().addAll(col2);

        RowConstraints topRow = new RowConstraints();
        topRow.setVgrow(Priority.ALWAYS);
        topRow.setMaxHeight(Double.MAX_VALUE);
        topRow.setFillHeight(true);

        RowConstraints bottomRow = new RowConstraints();
        bottomRow.setVgrow(Priority.NEVER);
        bottomRow.setMaxHeight(USE_PREF_SIZE);
        bottomRow.setFillHeight(false);
        getRowConstraints().addAll(topRow, bottomRow);

        helpSideBarScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        helpSideBarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        helpSideBarScrollPane.setContent(helpSideBar);
        helpSideBarScrollPane.setMinWidth(0);

        helpSideBar.setSpacing(30);
        helpSideBar.setMaxWidth(Double.MAX_VALUE);
        helpSideBar.setMinWidth(0);
        helpSideBar.prefWidthProperty().bind(helpSideBarScrollPane.widthProperty().subtract(15));
        helpSideBar.getChildren().addAll(attributeDescriptionHelp, templateCategoryHelp, templateObjectHelp);

        scriptingSplitPane.getItems().addAll(calculatorControls, scriptTextArea, helpSideBarScrollPane);
        SplitPane.setResizableWithParent(calculatorControls, false);
        scriptingSplitPane.setDividerPositions(220 / (double) 1000, 600 / (double) 1000);
        GridPane.setHalignment(scriptingSplitPane, HPos.LEFT);
        GridPane.setValignment(scriptingSplitPane, VPos.TOP);
        add(scriptingSplitPane, 0, 0);

        Label elementTypeLabel = new Label("Graph Element:");
        elementTypeLabel.setAlignment(Pos.TOP_LEFT);
        elementTypeLabel.setLabelFor(elementTypeComboBox);
        calculatorControls.getChildren().add(elementTypeLabel);

        elementTypeComboBox.getStyleClass().add("uneditableCombo");
        elementTypeComboBox.setItems(FXCollections.observableArrayList(GraphElementType.VERTEX, GraphElementType.TRANSACTION));
        elementTypeComboBox.setCellFactory((ListView<GraphElementType> param) -> {
            return new ListCell<GraphElementType>() {
                @Override
                protected void updateItem(GraphElementType item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getShortLabel());
                    } else {
                        setText("");
                    }
                }
            };
        });
        elementTypeComboBox.setButtonCell(elementTypeComboBox.getCellFactory().call(null));

        elementTypeComboBox.setMinWidth(220);
        elementTypeComboBox.setOnAction((ActionEvent event) -> {
            final GraphElementType elementType = elementTypeComboBox.getSelectionModel().getSelectedItem();
            attributeTypes = elementType == GraphElementType.VERTEX ? vertexAttributeTypes : transactionAttributeTypes;
            updateAttributeComboBox();
        });
        calculatorControls.getChildren().add(elementTypeComboBox);

        Label attributeLabel = new Label("Attribute to Set:");
        attributeLabel.setAlignment(Pos.TOP_LEFT);
        attributeLabel.setLabelFor(attributeComboBox);
        calculatorControls.getChildren().add(attributeLabel);

        attributeComboBox.setEditable(true);
        attributeComboBox.setMinWidth(220);
        attributeComboBox.getEditor().textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            String a = attributeComboBox.getEditor().getText();
            Attribute attribute = attributeTypes.get(a);
            if (attribute != null) {
                attributeTypeComboBox.getSelectionModel().select(attribute.getAttributeType());
                attributeTypeComboBox.setDisable(true);
                updateAttributeDescriptionHelp(a, attribute.getDescription(), attribute.getAttributeType());
            } else {
                attributeTypeComboBox.setDisable(false);
                updateAttributeDescriptionHelp(a, "<new attribute>", attributeTypeComboBox.getSelectionModel().getSelectedItem());
            }
        });
        attributeComboBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            String a = attributeComboBox.getEditor().getText();
            Attribute attribute = attributeTypes.get(a);
            if (attribute != null) {
                attributeTypeComboBox.getSelectionModel().select(attribute.getAttributeType());
                attributeTypeComboBox.setDisable(true);
                updateAttributeDescriptionHelp(a, attribute.getDescription(), attribute.getAttributeType());
            } else {
                attributeTypeComboBox.setDisable(false);
                updateAttributeDescriptionHelp(a, "<new attribute>", attributeTypeComboBox.getSelectionModel().getSelectedItem());
            }
        });
        calculatorControls.getChildren().add(attributeComboBox);

        Label attributeTypeLabel = new Label("Attribute Type:");
        attributeTypeLabel.setAlignment(Pos.TOP_LEFT);
        attributeTypeLabel.setLabelFor(attributeTypeComboBox);
        calculatorControls.getChildren().add(attributeTypeLabel);

        attributeTypeComboBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            String a = attributeComboBox.getEditor().getText();
            Attribute attribute = attributeTypes.get(a);
            if (attribute != null) {
                updateAttributeDescriptionHelp(a, attribute.getDescription(), attribute.getAttributeType());
            } else {
                updateAttributeDescriptionHelp(a, "<new attribute>", attributeTypeComboBox.getSelectionModel().getSelectedItem());
            }
        });
        attributeTypeComboBox.getStyleClass().add("uneditableCombo");
        attributeTypeComboBox.setMinWidth(220);
        AttributeCalculatorController.ATTRIBUTE_TYPES.sort((String o1, String o2) -> {
            return o1.compareTo(o2);
        });
        attributeTypeComboBox.getItems().addAll(AttributeCalculatorController.ATTRIBUTE_TYPES);
        attributeTypeComboBox.getSelectionModel().select("string");
        calculatorControls.getChildren().add(attributeTypeComboBox);

        Label insertLabel = new Label("Insert in script:");
        insertLabel.setAlignment(Pos.TOP_LEFT);
        insertLabel.setLabelFor(insertComboBox);
        calculatorControls.getChildren().add(insertLabel);

        insertComboBox.setMinWidth(220);
        insertComboBox.getStyleClass().add("uneditableCombo");
        insertComboBox.setOnAction((ActionEvent event) -> {
            InsertListCategory current = insertComboBox.getSelectionModel().getSelectedItem();
            if (current != null && current.equals(savedScriptsCat)) {
                if (loadButton != null) {
                    loadButton.disableProperty().set(false);
                }
                if (deleteButton != null) {
                    deleteButton.disableProperty().set(false);
                }
            } else {
                if (loadButton != null) {
                    loadButton.disableProperty().set(true);
                }
                if (deleteButton != null) {
                    deleteButton.disableProperty().set(true);
                }
            }
            updateVariablesListView();
            updateTemplateCategoryHelp();
        });
        insertComboBox.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                scriptTextArea.requestFocus();
            }
        });

        createCategories();
        ObservableList insertItems = FXCollections.observableArrayList(insertCategories);
        insertComboBox.setItems(insertItems);
        insertComboBox.getSelectionModel().select(savedScriptsCat);
        calculatorControls.getChildren().add(insertComboBox);

        variablesListView.setItems(FXCollections.observableArrayList());
        variablesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        variablesListView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends InsertListItem> observable, InsertListItem oldValue, InsertListItem newValue) -> {
            if (newValue != null) {
                newValue.updateValues();
            }
        });
        variablesListView.setCellFactory((ListView<InsertListItem> list) -> {
            ListCell<InsertListItem> l = new InsertListCell();
            l.getStyleClass().add("variableListCell");
            l.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    if (l.getItem() != null) {
                        l.getItem().insertValue();
                    }
                }
            });
            return l;
        });
        variablesListView.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                scriptTextArea.requestFocus();
            }
        });
        variablesListView.setMinWidth(220);
        variablesListView.setMaxWidth(Double.MAX_VALUE);
        variablesListView.setMinHeight(160);
        variablesListView.setMaxHeight(Double.MAX_VALUE);
        calculatorControls.getChildren().add(variablesListView);
        VBox.setVgrow(variablesListView, Priority.ALWAYS);

        final HBox buttonBar = new HBox();
        buttonBar.setSpacing(4);

        saveButton = new Button("Save");
        saveButton.setOnAction((ActionEvent event) -> {
            String name = null;
            String description = null;
            final ObservableList<InsertListItem> selectedItems = variablesListView.getSelectionModel().getSelectedItems();
            // Who would have thought that a selected item could be null?
            if (insertComboBox.getSelectionModel().getSelectedItem().equals(savedScriptsCat) && !selectedItems.isEmpty() && selectedItems.get(0) != null) {
                name = selectedItems.get(0).getText();
                description = (String) selectedItems.get(0).description;
            }

            ScriptIO.saveScript(this, name, description);
            updateInsertCompleteScripts();
        });

        loadButton = new Button("Load");
        loadButton.setOnAction((ActionEvent event) -> {

            final ObservableList<InsertListItem> selectedItems = variablesListView.getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty() || selectedItems.get(0) == null) {
                return;
            }
            final String scriptFile = ((InsertScriptListItem) selectedItems.get(0)).filename;
            ScriptIO.loadScript(scriptFile, this);
        });

        deleteButton = new Button("Del");
        deleteButton.setOnAction((ActionEvent event) -> {

            final ObservableList<InsertListItem> selectedItems = variablesListView.getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty() || selectedItems.get(0) == null) {
                return;
            }
            final String scriptFile = ((InsertScriptListItem) selectedItems.get(0)).filename;
            ScriptIO.deleteScript(scriptFile, this);
            updateInsertCompleteScripts();
        });

        final ImageView helpView = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16));
        helpButton = new MenuButton("", helpView);
        MenuItem helpItem = new MenuItem("Help");
        MenuItem tutorialItem = new MenuItem("Tutorial");
        helpButton.getItems().addAll(helpItem, tutorialItem);
        helpItem.setOnAction((ActionEvent event) -> {
            AbstractCalculatorTutorial.getDefault().openCalculatorHelp();
        });
        tutorialItem.setOnAction((ActionEvent event) -> {
            AbstractCalculatorTutorial.getDefault().displayCalculatorTutorial(this);
        });

        sideHelpButton = new ToggleButton("Docs");
        sideHelpButton.setSelected(true);
        sideHelpButton.setOnAction((ActionEvent event) -> {
            helpSideBarHidden = !helpSideBarHidden;
            if (helpSideBarHidden) {
                scriptingSplitPane.getItems().remove(helpSideBarScrollPane);
            } else {
                scriptingSplitPane.getItems().add(helpSideBarScrollPane);
                scriptingSplitPane.setDividerPositions(scriptingSplitPane.getDividerPositions()[0], 0.6);
            }
        });

        buttonBar.getChildren().addAll(saveButton, loadButton, deleteButton, helpButton, sideHelpButton);
        calculatorControls.getChildren().add(buttonBar);

        GridPane.setValignment(scriptTextArea, VPos.TOP);
        scriptTextArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        calculatorExecution.setAlignment(Pos.CENTER_RIGHT);
        calculatorExecution.setSpacing(15);
        GridPane.setHalignment(calculatorExecution, HPos.RIGHT);
        add(calculatorExecution, 0, 1);

        completeWithSchemaCheckBox.setSelected(false);
        calculatorExecution.getChildren().add(completeWithSchemaCheckBox);

        selectedOnlyCheckBox.setSelected(false);
        calculatorExecution.getChildren().add(selectedOnlyCheckBox);

        executeButton.setOnAction((ActionEvent event) -> {
            controller.execute();
        });
        calculatorExecution.getChildren().add(executeButton);

        // This is done at the very end after the selection models for the other dependent comboboxes (attribute to set, attribute type) have been built.
        elementTypeComboBox.getSelectionModel().select(GraphElementType.VERTEX);
    }

    private final AbstractCalculatorTemplateCategoryDescriptions descriptions = AbstractCalculatorTemplateCategoryDescriptions.getDefault();

    private static final String FONT_FAMILY = Font.getDefault().getFamily();

    private void updateTemplateCategoryHelp() {
        templateCategoryHelp.getChildren().clear();
        InsertListCategory category = insertComboBox.getSelectionModel().getSelectedItem();
        if (category != null) {
            final String categoryName = category.name;
            final Text categoryHeadingText = new Text(categoryName + ":\t");
            categoryHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 16));
            categoryHeadingText.setFill(Color.web("#0096C9"));
            templateCategoryHelp.getChildren().add(categoryHeadingText);
            final String categoryContext = category.contextName;
            final Text categoryContextText = new Text(categoryContext + "\n");
            categoryContextText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
            categoryContextText.setFill(Color.WHITE);
            templateCategoryHelp.getChildren().add(categoryContextText);
            String[] descriptionStrs = descriptions.getDescriptions(categoryName);
            final String[] noDescriptions = {""};
            if (descriptionStrs == null) {
                descriptionStrs = noDescriptions;
            }
            boolean firstDescription = true;
            for (final String str : descriptionStrs) {
                final Text categoryDescritpionText;
                if (!firstDescription) {
                    categoryDescritpionText = new Text("\n\n" + str);
                } else {
                    categoryDescritpionText = new Text(str);
                    firstDescription = false;
                }
                categoryDescritpionText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
                categoryDescritpionText.setFill(Color.WHITE);
                categoryDescritpionText.setFontSmoothingType(FontSmoothingType.LCD);
                templateCategoryHelp.getChildren().add(categoryDescritpionText);
            }
            String[] usageStrs = descriptions.getUsageExamples(categoryName);
            if (usageStrs.length != 0) {
                final Text examplesHeadingText = new Text("\n\nExamples Use Cases:");
                examplesHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
                examplesHeadingText.setFill(Color.WHITE);
                examplesHeadingText.setFontSmoothingType(FontSmoothingType.LCD);
                templateCategoryHelp.getChildren().add(examplesHeadingText);
                for (String str : usageStrs) {
                    final Text usageText = new Text("\n- " + str);
                    usageText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
                    usageText.setFill(Color.WHITE);
                    usageText.setFontSmoothingType(FontSmoothingType.LCD);
                    templateCategoryHelp.getChildren().add(usageText);
                }
            }
            updateTemplateObjectHelp(null);
        }
    }

    private void updateTemplateObjectHelp(final String itemName, final String itemDescription) {

        templateObjectHelp.getChildren().clear();

        final Text objectNameText = new Text(itemName + ":\n");
        objectNameText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 16));
        objectNameText.setFill(Color.web("#0096C9"));
        templateObjectHelp.getChildren().add(objectNameText);

        String objDescrip = "   " + itemDescription + "\n";
        final Text objectDescripText = new Text(objDescrip + "\n");
        objectDescripText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        objectDescripText.setFill(Color.WHITE);
        objectDescripText.setFontSmoothingType(FontSmoothingType.LCD);
        templateObjectHelp.getChildren().add(objectDescripText);

    }

    private void updateTemplateObjectHelp(final CalculatorTemplateDescription description) {

        templateObjectHelp.getChildren().clear();

        if (description == null) {
            return;
        }

        final Text objectNameText = new Text(description.templateName + ":\n");
        objectNameText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 16));
        objectNameText.setFill(Color.web("#0096C9"));
        templateObjectHelp.getChildren().add(objectNameText);

        String objUsage = "Usage(s):\n";
        for (String str : description.usage) {
            objUsage += "\t" + str + "\n";
        }
        final Text objectUsageText = new Text(objUsage + "\n");
        objectUsageText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        objectUsageText.setFill(Color.WHITE);
        templateObjectHelp.getChildren().add(objectUsageText);

        String objDescrip = "   " + description.description + "\n";
        final Text objectDescripText = new Text(objDescrip + "\n");
        objectDescripText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        objectDescripText.setFill(Color.WHITE);
        objectDescripText.setFontSmoothingType(FontSmoothingType.LCD);
        templateObjectHelp.getChildren().add(objectDescripText);

        if (description.arguments.length != 0) {
            String objArguments = "Arguments:\n";
            for (String str : description.arguments) {
                objArguments += "\t" + str + "\n";
            }
            final Text objectArgumentsText = new Text(objArguments + "\n");
            objectArgumentsText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
            objectArgumentsText.setFill(Color.WHITE);
            objectArgumentsText.setFontSmoothingType(FontSmoothingType.LCD);
            templateObjectHelp.getChildren().add(objectArgumentsText);
        }

        String objReturn = "Returns:\n\t" + description.returns + "\n";
        final Text objectReturnsText = new Text(objReturn + "\n");
        objectReturnsText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
        objectReturnsText.setFill(Color.WHITE);
        objectReturnsText.setFontSmoothingType(FontSmoothingType.LCD);
        templateObjectHelp.getChildren().add(objectReturnsText);

        if (description.notes.length != 0) {
            String objNotes = "Notes:\n";
            for (String str : description.notes) {
                objNotes += "\t- " + str + "\n";
            }
            final Text objectNotesText = new Text(objNotes + "\n");
            objectNotesText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
            objectNotesText.setFill(Color.WHITE);
            objectNotesText.setFontSmoothingType(FontSmoothingType.LCD);
            templateObjectHelp.getChildren().add(objectNotesText);
        }

    }

    private void updateAttributeDescriptionHelp(String attributeName, String attributeDescription, String attributeType) {
        Platform.runLater(() -> {
            attributeDescriptionHelp.getChildren().clear();
            final Text attributeHeadingText = new Text("Attribute to set:\n");
            attributeHeadingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 16));
            attributeHeadingText.setFill(Color.web("#0096C9"));
            final Text attributeLabelText = new Text(attributeName);
            attributeLabelText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
            attributeLabelText.setFill(Color.WHITE);
            final Text attributeSpacingText = new Text("  -  ");
            attributeSpacingText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
            attributeSpacingText.setFill(Color.WHITE);
            final Text attributeDescriptionText = new Text(attributeDescription);
            attributeDescriptionText.setFont(Font.font(FONT_FAMILY, FontPosture.ITALIC, 12));
            attributeDescriptionText.setFill(Color.WHITE);
            final Text attributeTypeText = new Text("    (type:  " + attributeType + ")");
            attributeTypeText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 12));
            attributeTypeText.setFill(Color.WHITE);
            attributeDescriptionHelp.getChildren().addAll(attributeHeadingText, attributeLabelText, attributeSpacingText, attributeDescriptionText, attributeTypeText);
        });
    }

    private static class InsertListCell extends ListCell<InsertListItem> {

        @Override
        public void updateItem(InsertListItem item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                setText(item.getText());
            } else {
                setText("");
            }
        }

    }

    public GraphElementType getElementType() {
        return elementTypeComboBox.getSelectionModel().getSelectedItem();
    }

    public String getAttribute() {
        return attributeComboBox.getEditor().getText();
    }

    public void setAttributes(String current, Collection<String> choices) {
        attributeComboBox.getItems().clear();
        attributeComboBox.getItems().addAll(choices);
        attributeComboBox.getSelectionModel().select(current);
    }

    public String getAttributeType() {
        return attributeTypeComboBox.getSelectionModel().getSelectedItem();
    }

    public void setAttributeType(String attributeType) {
        attributeTypeComboBox.getSelectionModel().select(attributeType);
    }

    public boolean isSelectedOnly() {
        return selectedOnlyCheckBox.isSelected();
    }

    public boolean isCompleteWithSchema() {
        return completeWithSchemaCheckBox.isSelected();
    }

    public void setSelectedOnly(boolean selectedOnly) {
        selectedOnlyCheckBox.setSelected(selectedOnly);
    }

    public String getScript() {
        return scriptTextArea.getText();
    }

    public void setScript(String script) {
        scriptTextArea.setText(script);
    }

    public void setScriptAndDestination(GraphElementType elementType, String attrToSet, String attrType, String script) {
        Platform.runLater(() -> {
            elementTypeComboBox.getSelectionModel().select(elementType);
            // we need to set the type before the attribute itself, since setting the attribute itself
            // needs to be able to override type if an attribute with that type already exists in the graph.
            attributeTypeComboBox.getSelectionModel().select(attrType);
            attributeComboBox.getSelectionModel().select(attrToSet);
            scriptTextArea.setText(script);
        });
    }

    public void updateAttributes(final Graph graph) {
        Platform.runLater(() -> {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                updateInsertAttributeVariables(readableGraph, GraphElementType.VERTEX, "", "Node Attributes");
                updateInsertAttributeVariables(readableGraph, GraphElementType.TRANSACTION, "", "Transaction Attributes");
                updateInsertAttributeVariables(readableGraph, GraphElementType.VERTEX, "source_", "Source Node Attributes");
                updateInsertAttributeVariables(readableGraph, GraphElementType.VERTEX, "dest_", "Destination Node Attributes");

                vertexAttributeTypes.clear();
                int attributeCount = readableGraph.getAttributeCount(GraphElementType.VERTEX);
                for (int i = 0; i < attributeCount; i++) {
                    int attributeId = readableGraph.getAttribute(GraphElementType.VERTEX, i);
                    Attribute attribute = new GraphAttribute(readableGraph, attributeId);
                    if (AttributeCalculatorController.ATTRIBUTE_TYPES.contains(attribute.getAttributeType())) {
                        vertexAttributeTypes.put(attribute.getName(), attribute);
                    }
                }

                transactionAttributeTypes.clear();
                attributeCount = readableGraph.getAttributeCount(GraphElementType.TRANSACTION);
                for (int i = 0; i < attributeCount; i++) {
                    int attributeId = readableGraph.getAttribute(GraphElementType.TRANSACTION, i);
                    Attribute attribute = new GraphAttribute(readableGraph, attributeId);
                    if (AttributeCalculatorController.ATTRIBUTE_TYPES.contains(attribute.getAttributeType())) {
                        transactionAttributeTypes.put(attribute.getName(), attribute);
                    }
                }
            } finally {
                readableGraph.release();
            }

            updateAttributeComboBox();
        });
    }

    public void updateVariablesListView() {
        InsertListCategory category = insertComboBox.getSelectionModel().getSelectedItem();
        if (category != null) {
            variablesListView.setItems(FXCollections.observableArrayList(insertComboBox.getSelectionModel().getSelectedItem().insertlistItems));
        } else {
            variablesListView.setItems(FXCollections.observableArrayList());
        }
    }

    public void updateAttributeComboBox() {
//        Platform.runLater(() -> {
        String currentSelection = getAttribute();
        if (currentSelection == null || !attributeTypes.containsKey(currentSelection)) {
            if (attributeTypes.containsKey("selected")) {
                currentSelection = "selected";
            } else {
                Iterator<String> i = attributeTypes.keySet().iterator();
                if (i.hasNext()) {
                    currentSelection = i.next();
                }
            }
        }
        setAttributes(currentSelection, attributeTypes.keySet());
        updateVariablesListView();
//        });
    }

    private List<InsertListCategory> insertCategories = new LinkedList<>();
    private Map<String, InsertListCategory> categoryLookup = new HashMap<>();

    private void createCategories() {

        savedScriptsCat = new InsertListCategory(null, "Complete Scripts");
        insertCategories.add(savedScriptsCat);
        categoryLookup.put("Complete Scripts", savedScriptsCat);
        updateInsertCompleteScripts();

        InsertListCategory vertexCat = new InsertListCategory(GraphElementType.VERTEX, "Node Attributes");
        insertCategories.add(vertexCat);
        categoryLookup.put("Node Attributes", vertexCat);
        InsertListCategory transCat = new InsertListCategory(GraphElementType.TRANSACTION, "Transaction Attributes");
        insertCategories.add(transCat);
        categoryLookup.put("Transaction Attributes", transCat);
        InsertListCategory sourceCat = new InsertListCategory(GraphElementType.TRANSACTION, "Source Node Attributes");
        insertCategories.add(sourceCat);
        categoryLookup.put("Source Node Attributes", sourceCat);
        InsertListCategory destCat = new InsertListCategory(GraphElementType.TRANSACTION, "Destination Node Attributes");
        insertCategories.add(destCat);
        categoryLookup.put("Destination Node Attributes", destCat);

        for (CalculatorVariable variable : CalculatorVariable.values()) {
            final String name = variable.getDirectoryString();
            if (!categoryLookup.containsKey(name)) {
                InsertListCategory newCat = new InsertListCategory(variable.getElementType(), name);
                categoryLookup.put(name, newCat);
                insertCategories.add(newCat);
            }
            categoryLookup.get(name).insertlistItems.add(new InsertListItem(variable.getVariableLabel(), variable.getVariableName(), variable.getDescription(), variable.getSelectionIndices()));
        }

        for (CalculatorConstant constant : CalculatorConstant.values()) {
            final String name = constant.getDirectoryString();
            if (!categoryLookup.containsKey(name)) {
                InsertListCategory newCat = new InsertListCategory(constant.getElementType(), name);
                categoryLookup.put(name, newCat);
                insertCategories.add(newCat);
            }
            categoryLookup.get(name).insertlistItems.add(new InsertListItem(constant.getConstantLabel(), constant.getConstantValue(), constant.getDescription(), constant.getSelectionIndices()));
        }
        insertCategories.sort((InsertListCategory o1, InsertListCategory o2) -> {
            return o1.name.compareTo(o2.name);
        });
    }

    private class InsertListCategory {

        public final String contextName;
        public final String name;
        public final Collection<InsertListItem> insertlistItems;

        public InsertListCategory(final GraphElementType elementType, final String name) {
            this(elementType, name, new LinkedList<>());
        }

        @Override
        public String toString() {
            return name;
        }

        public InsertListCategory(final GraphElementType elementType, final String name, final Collection<InsertListItem> insertListItems) {
            this.contextName = elementType == null ? "Any context" : elementType.getShortLabel() + " context";
            this.name = name;
            this.insertlistItems = insertListItems;
        }

    }

    private void updateInsertCompleteScripts() {
        InsertListCategory category = categoryLookup.get("Complete Scripts");
        category.insertlistItems.clear();
        Map<String, String[]> savedScripts = ScriptIO.getScriptNamesAndDescriptions();
        for (Entry<String, String[]> nameDesc : savedScripts.entrySet()) {
            category.insertlistItems.add(new InsertScriptListItem(nameDesc.getKey(), nameDesc.getValue()[0], nameDesc.getValue()[1]));
        }
        // Code block which seemingly does nothing, only here to ensure that the list of saved scripts actually updates when "Complete Scripts" is the active category, as javafx doesn't seem to be doing the right thing by default.
        InsertListCategory current = insertComboBox.getSelectionModel().getSelectedItem();
        if (current != null && current.equals(savedScriptsCat)) {
            insertComboBox.getSelectionModel().select(null);
            insertComboBox.getSelectionModel().select(current);
        }
    }

    private void updateInsertAttributeVariables(GraphReadMethods rg, GraphElementType elementType, String prefix, String categoryName) {
        InsertListCategory category = categoryLookup.get(categoryName);
        category.insertlistItems.clear();
        int attributeCount = rg.getAttributeCount(elementType);
        for (int i = 0; i < attributeCount; i++) {
            int attribute = rg.getAttribute(elementType, i);
            String attributeName = rg.getAttributeName(attribute);
            String attributeDescription = rg.getAttributeDescription(attribute);
            category.insertlistItems.add(new InsertAttributeListItem(attributeName, prefix + attributeName, attributeDescription));
        }
    }

    private class InsertScriptListItem extends InsertListItem {

        private final String filename;

        public InsertScriptListItem(final String scriptName, final String filename, final String description) {
            super(scriptName, null, description, null);
            this.filename = filename;
        }

        @Override
        public void updateValues() {
            updateTemplateObjectHelp(getText(), (String) description);
        }

    }

    private class InsertAttributeListItem extends InsertListItem {

        public InsertAttributeListItem(final String attributeName, final String insertText, final String description) {
            super(attributeName, insertText, description, null);
        }

        @Override
        public void updateValues() {
            updateTemplateObjectHelp(getText(), (String) description);
        }

    }

    private class InsertListItem extends Text {

        private final String insertText;
        private final int selectionStart;
        private final int selectionEnd;
        private final boolean shouldSpace;
        protected final Object description;

        public InsertListItem(final String label, final String insertText, final Object description, final int[] selectionIndices) {
            super(label);
            this.insertText = insertText;
            this.description = description;
            if (insertText == null) {
                this.shouldSpace = false;
                this.selectionStart = -1;
                this.selectionEnd = -1;
            } else {
                this.shouldSpace = insertText.charAt(0) != '.' && insertText.charAt(0) != '[';
                this.selectionStart = selectionIndices == null ? insertText.length() : selectionIndices[0];
                this.selectionEnd = selectionIndices == null || selectionIndices.length == 1 ? selectionStart : selectionIndices[1];
            }
        }

        public void insertValue() {

            if (insertText == null) {
                return;
            }

            int anchor = scriptTextArea.getAnchor();
            int caret = scriptTextArea.getCaretPosition();
            if (anchor > caret) {
                int t = anchor;
                anchor = caret;
                caret = t;
            }

            String text = scriptTextArea.getText();
            String prefix = text.substring(0, anchor);
            String postfix = text.substring(caret);
            if (shouldSpace && !prefix.endsWith(" ")) {
                prefix += " ";
            }
            if (shouldSpace && !postfix.startsWith(" ")) {
                postfix = " " + postfix;
            }
            scriptTextArea.setText(prefix + insertText + postfix);
            scriptTextArea.selectRange(prefix.length() + selectionStart, prefix.length() + selectionEnd);
        }

        public void updateValues() {
            updateTemplateObjectHelp((CalculatorTemplateDescription) description);
        }

    }
}
