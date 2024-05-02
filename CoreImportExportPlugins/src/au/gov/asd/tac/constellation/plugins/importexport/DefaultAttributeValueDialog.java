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

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import javafx.event.ActionEvent;
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

/**
 * The DefaultAttributeValueDialog is a dialog that allows the user to specify a
 * default (constant) value for a given graph attribute. This attribute will be
 * given a constant value for all graph elements rather than getting its
 * attribute values from the imported data. This is useful when it is known that
 * all graph elements will have a constant value for a given attribute but that
 * information is not present in the data.
 *
 * @author sirius
 */
public class DefaultAttributeValueDialog extends Stage {

    private static final int GAP = 5;
    private static final int TEXT_WIDTH = 200;
    private static final int TEXT_HEIGHT = 30;

    private static final Insets GRIDPANE_PADDING = new Insets(10);
    private static final Insets BUTTONPANE_PADDING = new Insets(5);

    private final TextField labelText;
    private String defaultValue;

    public DefaultAttributeValueDialog(final Window owner, final String attributeName, final String initialValue) {

        defaultValue = initialValue;

        initStyle(StageStyle.UTILITY);
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        setTitle("Set Default Value: " + attributeName);

        final BorderPane root = new BorderPane();
        final Scene scene = new Scene(root);
        scene.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        setScene(scene);

        final GridPane fieldPane = new GridPane();
        fieldPane.setHgap(GAP);
        fieldPane.setVgap(GAP);
        fieldPane.setPadding(GRIDPANE_PADDING);
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
        labelText.setPrefSize(TEXT_WIDTH, TEXT_HEIGHT);
        GridPane.setConstraints(labelText, 1, 1);
        fieldPane.getChildren().add(labelText);
        labelText.requestFocus();

        final FlowPane buttonPane = new FlowPane();
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(BUTTONPANE_PADDING);
        buttonPane.setHgap(GAP);
        root.setBottom(buttonPane);

        final Button okButton = new Button("OK");
        okButton.setOnAction((ActionEvent event) -> {
            defaultValue = labelText.getText();
            DefaultAttributeValueDialog.this.hide();
        });
        buttonPane.getChildren().add(okButton);

        final Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> DefaultAttributeValueDialog.this.hide());
        buttonPane.getChildren().add(cancelButton);

        final Button clearButton = new Button("Clear");
        clearButton.setOnAction((ActionEvent event) -> {
            defaultValue = null;
            DefaultAttributeValueDialog.this.hide();
        });
        buttonPane.getChildren().add(clearButton);
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
