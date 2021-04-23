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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import au.gov.asd.tac.constellation.plugins.importexport.NewAttribute;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class NewAttributeDialog extends Stage {

    private final ComboBox<String> typeBox;
    private final TextField labelText;
    private final TextArea descriptionText;

    private Attribute attribute = null;

    public NewAttributeDialog(final Stage owner, final GraphElementType elementType) {

        initStyle(StageStyle.UTILITY);
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        setTitle("New Attribute");

        final BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #DDDDDD;");
        final Scene scene = new Scene(root);
        setScene(scene);

        final GridPane fieldPane = new GridPane();
        fieldPane.setHgap(5);
        fieldPane.setVgap(5);
        fieldPane.setPadding(new Insets(10));
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
        labelText.setPrefSize(200, 30);
        GridPane.setConstraints(labelText, 1, 1);
        fieldPane.getChildren().add(labelText);
        labelText.requestFocus();

        final Label descriptionLabel = new Label("Description:");
        GridPane.setConstraints(descriptionLabel, 0, 2);
        fieldPane.getChildren().add(descriptionLabel);

        descriptionText = new TextArea();
        descriptionText.setPromptText("Attribute Description");
        descriptionText.setPrefSize(300, 100);
        GridPane.setConstraints(descriptionText, 1, 2);
        fieldPane.getChildren().add(descriptionText);

        final FlowPane buttonPane = new FlowPane();
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(new Insets(5));
        buttonPane.setHgap(5);
        root.setBottom(buttonPane);

        final Button okButton = new Button("Ok");
        okButton.setOnAction(event -> {
            attribute = new NewAttribute(elementType, typeBox.getSelectionModel().getSelectedItem(), labelText.getText(), descriptionText.getText());
            NewAttributeDialog.this.hide();
        });
        buttonPane.getChildren().add(okButton);

        final Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> {
            NewAttributeDialog.this.hide();
        });
        buttonPane.getChildren().add(cancelButton);
    }

    public Attribute getAttribute() {
        return attribute;
    }
}
