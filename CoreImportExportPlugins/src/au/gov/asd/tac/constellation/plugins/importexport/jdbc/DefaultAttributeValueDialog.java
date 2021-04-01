/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class DefaultAttributeValueDialog extends Stage {

    private final TextField labelText;
    private String defaultValue = null;

    public DefaultAttributeValueDialog(final Window owner, final String attributeName, final String initialValue) {

        defaultValue = initialValue;

        initStyle(StageStyle.UTILITY);
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        setTitle("Set Default Value: " + attributeName);

        final BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #DDDDDD;");
        final Scene scene = new Scene(root);
        setScene(scene);

        final GridPane fieldPane = new GridPane();
        fieldPane.setHgap(5);
        fieldPane.setVgap(5);
        fieldPane.setPadding(new Insets(10));
        root.setCenter(fieldPane);

        final Label labelLabel = new Label("Label:");
        GridPane.setConstraints(labelLabel, 0, 1);
        fieldPane.getChildren().add(labelLabel);

        labelText = new TextField();
        if (defaultValue == null) {
            labelText.setPromptText("Enter attribute default value");
        } else {
            labelText.setText(defaultValue);
        }
        labelText.setPrefSize(200, 30);
        GridPane.setConstraints(labelText, 1, 1);
        fieldPane.getChildren().add(labelText);
        labelText.requestFocus();

        final FlowPane buttonPane = new FlowPane();
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(new Insets(5));
        buttonPane.setHgap(5);
        root.setBottom(buttonPane);

        final Button okButton = new Button("Ok");
        okButton.setOnAction(event -> {
            defaultValue = labelText.getText();
            DefaultAttributeValueDialog.this.hide();
        });
        buttonPane.getChildren().add(okButton);

        final Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> {
            DefaultAttributeValueDialog.this.hide();
        });
        buttonPane.getChildren().add(cancelButton);

        final Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {
            defaultValue = null;
            DefaultAttributeValueDialog.this.hide();
        });
        buttonPane.getChildren().add(clearButton);
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
