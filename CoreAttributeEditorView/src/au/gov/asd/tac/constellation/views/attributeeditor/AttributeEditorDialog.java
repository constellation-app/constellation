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
package au.gov.asd.tac.constellation.views.attributeeditor;

import au.gov.asd.tac.constellation.functionality.dialog.ConstellationDialog;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Dialog for the editing of attribute values through CONSTELLATION's attribute
 * editor.
 * <br>
 * This is a modal javafx dialog.
 *
 * @author twinkle2_little
 */
public class AttributeEditorDialog extends ConstellationDialog {

    private static final String DARK_THEME = "/au/gov/asd/tac/constellation/views/attributeeditor/resources/editor-dark.css";
    private final HBox okCancelHBox;
    private final Label errorLabel;
    private final Button okButton;
    private final Button cancelButton;
    private final Button defaultButton;

    public AttributeEditorDialog(final boolean restoreDefaultButton, final AbstractEditor<?> editor) {
        final VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        root.setFillWidth(true);

        errorLabel = new Label("");
        errorLabel.setId("error");

        okButton = new Button("Ok");
        cancelButton = new Button("Cancel");
        defaultButton = new Button("Restore Default");

        okButton.setOnAction(e -> {
            editor.performEdit();
            hideDialog();
        });

        cancelButton.setOnAction(e -> {
            hideDialog();
        });

        defaultButton.setOnAction(e -> {
            editor.setDefaultValue();
        });

        okCancelHBox = new HBox(20);
        okCancelHBox.setPadding(new Insets(10));
        okCancelHBox.setAlignment(Pos.CENTER);
        if (restoreDefaultButton) {
            okCancelHBox.getChildren().addAll(okButton, cancelButton, defaultButton);
        } else {
            okCancelHBox.getChildren().addAll(okButton, cancelButton);
        }

        okButton.disableProperty().bind(editor.getEditDisabledProperty());
        errorLabel.visibleProperty().bind(editor.getEditDisabledProperty());
        errorLabel.textProperty().bind(editor.getErrorMessageProperty());
        final Node ec = editor.getEditorControls();
        VBox.setVgrow(ec, Priority.ALWAYS);
        root.getChildren().addAll(editor.getEditorHeading(), ec, errorLabel, okCancelHBox);

        final Scene scene = new Scene(root);
        scene.setFill(Color.rgb(0, 0, 0, 0));
        scene.getStylesheets().add(AttributeEditorDialog.class.getResource(DARK_THEME).toExternalForm());
        fxPanel.setScene(scene);
    }

}
