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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.functionality.dialog.ConstellationDialog;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

/**
 * The NewAttributeDialog provides a dialog box allowing the user to create a
 * new attribute that does not currently exist in the {@link GraphDestination}.
 * This is typically an attribute that does not exist in a currently existing
 * graph or an attribute that does not exist in a destination schema.
 *
 * @author sirius
 */
public class NewAttributeDialog extends ConstellationDialog {

    private static final int GRIDPANE_GAP = 5;
    private static final Insets GRIDPANE_PADDING = new Insets(10);
    private static final int LABEL_PREFWIDTH = 200;
    private static final int LABEL_PREFHEIGHT = 30;
    private static final int DESC_PREFWIDTH = 300;
    private static final int DESC_PREFHEIGHT = 100;
    private static final Insets BUTTONPANE_PADDING = new Insets(5);

    private final ComboBox<String> typeBox;
    private final TextField labelText;
    private final TextArea descriptionText;

    private final Button okButton;

    public NewAttributeDialog() {
        final BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #DDDDDD;");

        final GridPane fieldPane = new GridPane();
        fieldPane.setHgap(GRIDPANE_GAP);
        fieldPane.setVgap(GRIDPANE_GAP);
        fieldPane.setPadding(GRIDPANE_PADDING);
        root.setCenter(fieldPane);

        final Label typeLabel = new Label("Type:");
        GridPane.setConstraints(typeLabel, 0, 0);
        fieldPane.getChildren().add(typeLabel);

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll(AttributeRegistry.getDefault().getAttributes().keySet());
        typeBox.getSelectionModel().select("string");
        GridPane.setConstraints(typeBox, 1, 0);
        fieldPane.getChildren().add(typeBox);

        final Label labelLabel = new Label("Label:");
        GridPane.setConstraints(labelLabel, 0, 1);
        fieldPane.getChildren().add(labelLabel);

        labelText = new TextField();
        labelText.setPromptText("Attribute Label");
        labelText.setPrefSize(LABEL_PREFWIDTH, LABEL_PREFHEIGHT);
        GridPane.setConstraints(labelText, 1, 1);
        fieldPane.getChildren().add(labelText);
        labelText.requestFocus();

        final Label descriptionLabel = new Label("Description:");
        GridPane.setConstraints(descriptionLabel, 0, 2);
        fieldPane.getChildren().add(descriptionLabel);

        descriptionText = new TextArea();
        descriptionText.setPromptText("Attribute Description");
        descriptionText.setPrefSize(DESC_PREFWIDTH, DESC_PREFHEIGHT);
        GridPane.setConstraints(descriptionText, 1, 2);
        fieldPane.getChildren().add(descriptionText);

        final FlowPane buttonPane = new FlowPane();
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(BUTTONPANE_PADDING);
        buttonPane.setHgap(GRIDPANE_GAP);
        root.setBottom(buttonPane);

        okButton = new Button("OK");
        buttonPane.getChildren().add(okButton);

        final Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction((ActionEvent event) -> hideDialog());
        buttonPane.getChildren().add(cancelButton);

        final Scene scene = new Scene(root);
        fxPanel.setScene(scene);
    }

    /**
     * Implement the {@code setOnAction} that happens when OK is pressed. This
     * should create the new attribute and use the get methods in this class to
     * retrieve the values set in the attribute dialog.
     *
     * @param event The {@code setOnAction} that will run when the OK button is
     * pressed.
     */
    public void setOkButtonAction(EventHandler<ActionEvent> event) {
        okButton.setOnAction(event);
    }

    /**
     * Get the attribute type
     *
     * @return The attribute type
     */
    public String getType() {
        return typeBox.getSelectionModel().getSelectedItem();
    }

    /**
     * The attribute label
     *
     * @return The attribute label
     */
    public String getLabel() {
        return labelText.getText();
    }

    /**
     * The attribute description
     *
     * @return The attribute description
     */
    public String getDescription() {
        return descriptionText.getText();
    }

}
