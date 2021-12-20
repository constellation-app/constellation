/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find2.components.advanced.utilities;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.find2.components.advanced.StringCriteriaPanel;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This class contains the pop up window for the StrinCriteriaPanels useList
 * Button.
 *
 * @author Atlas139mkm
 */
public class UseListInputWindow extends Stage {

    private StringCriteriaPanel parentComponent;
    private String chosenSplitLineOption;

    private BorderPane bp = new BorderPane();

    private final VBox vbox = new VBox();
    private final HBox hbox = new HBox();

    private final Label textAreaLabel = new Label("Input a list of delimited terms to be searched:");
    private final TextArea textArea = new TextArea();

    private final HBox splitLinesHbox = new HBox();
    private final Label splitStringTextFieldLabel = new Label("String:");
    private final TextField splitStringTextField = new TextField();
    private final Label splitLinesChoiceBoxLabel = new Label("Split lines on:");
    private final ChoiceBox<String> splitLinesChoiceBox = new ChoiceBox<>();
    private final Button splitLinesButton = new Button("Split Lines");

    private BorderPane buttonsBp = new BorderPane();
    private HBox buttonsHbox = new HBox();
    private Button saveButton = new Button("Save");
    private Button cancelButton = new Button("Cancel");

    private static final String DARK_THEME = "/au/gov/asd/tac/constellation/views/find2/resources/editor-dark.css";

    public UseListInputWindow(StringCriteriaPanel parentComponent, String text) {
        this.parentComponent = parentComponent;
        this.textArea.setText(text);
        updateText(textArea.getText());

        setContent();
        setAlwaysOnTop(true);

        splitLinesChoiceBox.getSelectionModel().selectedItemProperty().addListener((final ObservableValue<? extends String> observableValue, final String oldElement, final String newElement) -> {
            splitLinesChoiceAction(newElement);
        });

        splitLinesButton.setOnAction(action -> {
            splitLinesAction();
        });

        saveButton.setOnAction(action -> {
            parentComponent.setSearchFieldText(getTextAreaText());
            close();
        });

        cancelButton.setOnAction(action -> {
            close();
        });
    }

    /**
     * Sets the UI content of the window
     */
    private void setContent() {
        textArea.setText(parentComponent.getSearchFieldText());
        splitLinesChoiceBox.getItems().addAll("Comma", "Tab", "Semi-colon", "String");
        splitLinesChoiceBox.getSelectionModel().selectFirst();
        chosenSplitLineOption = splitLinesChoiceBox.getSelectionModel().getSelectedItem();

        splitLinesHbox.getChildren().addAll(splitLinesChoiceBoxLabel, splitLinesChoiceBox, splitLinesButton);
        splitLinesHbox.setSpacing(10);

        vbox.getChildren().addAll(textAreaLabel, textArea, splitLinesHbox);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10));

        buttonsHbox.getChildren().addAll(saveButton, cancelButton);
        buttonsHbox.setSpacing(10);
        buttonsHbox.setPadding(new Insets(0, 10, 10, 0));
        buttonsBp.setRight(buttonsHbox);
        bp.setBottom(buttonsBp);

        bp.setCenter(vbox);
        final Scene scene = new Scene(bp);
        scene.getStylesheets().add(IconSelector.class.getResource(DARK_THEME).toExternalForm());

        setScene(scene);
    }

    /**
     * This is called by the parent object when opening this window. It updates
     * the textAreas text to be a formatted version of the parents textField
     * text.
     *
     * This,Is,a,test = This\nIs\n\ntest
     *
     * @param text
     */
    public void updateText(String text) {
        final StringBuilder sb = new StringBuilder();
        final String[] splitText = text.split(SeparatorConstants.COMMA);
        for (int i = 0; i < splitText.length; i++) {
            sb.append(splitText[i]);
            sb.append(i == splitText.length - 1 ? "" : SeparatorConstants.NEWLINE);
        }
        textArea.setText(sb.toString());
    }

    /**
     * Gets the textAreas current text
     *
     * @return
     */
    public String getTextAreaText() {
        return textArea.getText();
    }

    /**
     * This is called when the split lines button is pressed. It splits the text
     * areas text based on the splitLinesChoiceBox selection. For example if the
     * text area contains This,Is,a,test and the user has split by commas
     * selected. The text are will now contain This\nIs\n\ntest
     */
    private void splitLinesAction() {
        final String selectedSplitChoice = splitLinesChoiceBox.getSelectionModel().getSelectedItem();
        final String splitAt;
        switch (selectedSplitChoice) {
            case "Comma":
                splitAt = SeparatorConstants.COMMA;
                break;
            case "Tab":
                splitAt = SeparatorConstants.TAB;
                break;
            case "Semi-colon":
                splitAt = SeparatorConstants.SEMICOLON;
                break;
            case "String":
                splitAt = splitStringTextField.getText();
                break;
            default:
                splitAt = SeparatorConstants.COMMA;
                break;
        }
        final String[] splitTextField = textArea.getText().split(splitAt);
        textArea.clear();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < splitTextField.length; i++) {
            sb.append(splitTextField[i]);
            sb.append(i == splitTextField.length - 1 ? "" : SeparatorConstants.NEWLINE);
        }
        textArea.setText(sb.toString());

    }

    /**
     * This is called when the splitLinesChoiceBox selection changes. This adds
     * and removes the relevant UI elements based on the selection. It will add
     * the splitLineStringText field and splitLinesStringTextFeild Label on
     * choiceSelection "String"
     *
     * @param choiceSelection
     */
    private void splitLinesChoiceAction(String choiceSelection) {
        chosenSplitLineOption = choiceSelection;
        splitLinesHbox.getChildren().clear();

        if (choiceSelection.equals("String")) {
            splitLinesHbox.getChildren().addAll(splitLinesChoiceBoxLabel, splitLinesChoiceBox,
                    splitStringTextFieldLabel, splitStringTextField, splitLinesButton);
        } else {
            splitLinesHbox.getChildren().addAll(splitLinesChoiceBoxLabel, splitLinesChoiceBox,
                    splitLinesButton);
        }
    }
}
