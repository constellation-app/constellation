/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.notes;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Window;

/**
 *
 * @author altair1673
 */
public class NewNotePane {

    private final Pane dialogPane;
    private final String fontStyle = String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize());
    private static final String PROMPT_COLOR = "#909090";
    private static final double WIDTH = 300;
    private static final double HEIGHT = 120;
    final TextField titleField = new TextField();
    private Window window = null;
    private final CheckBox applyToSelection = new CheckBox("Link note to graph selection");
    private boolean applySelected;
    private static String userChosenColour = "#942483";

    public NewNotePane(final Window window, final String userChosenColour) {

        this.window = window;
        this.userChosenColour = userChosenColour;
        dialogPane = new Pane();
        //dialogPane.setMinHeight(HEIGHT);
        //dialogPane.setMaxHeight(HEIGHT);
        //dialogPane.setMinWidth(WIDTH);
        //dialogPane.setMaxWidth(WIDTH);

        // TextField to enter new note title.
        titleField.setPromptText("Type a title...");
        titleField.setStyle(fontStyle + "-fx-prompt-text-fill: " + PROMPT_COLOR + ";");

        // Checkbox to apply note to selection.
        applyToSelection.setSelected(true);
        applySelected = true;
        applyToSelection.selectedProperty().addListener((ov, oldVal, newVal) -> applySelected = applyToSelection.isSelected());

        // TextArea to enter new note content.
        final TextArea contentField = new TextArea();
        contentField.setPromptText("Type a note...");
        contentField.setStyle(fontStyle + "-fx-prompt-text-fill: " + PROMPT_COLOR + ";");
        contentField.setWrapText(true);
        contentField.setOnKeyPressed(key -> {
            // If tab is typed and shift isn't being held dowm.
            if (key.getCode() == KeyCode.TAB && !key.isShiftDown()) {
                // Backspace any tabs typed.
                contentField.fireEvent(new KeyEvent(null, null, KeyEvent.KEY_PRESSED, "", "", KeyCode.BACK_SPACE, false, false, false, false));
                // Move focus to the next UI element.
                contentField.getParent().getChildrenUnmodifiable().get(contentField.getParent().getChildrenUnmodifiable().indexOf(contentField) + 1).requestFocus();
            }
        });

        // Colourpicker to set colour of new note
        ColorPicker newNoteColour = new ColorPicker(ConstellationColor.fromHtmlColor(userChosenColour).getJavaFXColor());
        newNoteColour.setOnAction(event -> this.userChosenColour = ConstellationColor.fromFXColor(newNoteColour.getValue()).getHtmlColor());

        final HBox noteHBox = new HBox(30, applyToSelection, newNoteColour);

        final VBox addNoteVBox = new VBox(5, titleField, contentField, noteHBox);
        addNoteVBox.setAlignment(Pos.CENTER_RIGHT);
        addNoteVBox.setStyle(fontStyle + "-fx-padding: 5px;");
        addNoteVBox.setMinHeight(200);

        dialogPane.getChildren().add(addNoteVBox);
    }

    public void showPopUp() {
        final Alert popUp = new Alert(Alert.AlertType.CONFIRMATION);
        popUp.initModality(Modality.APPLICATION_MODAL);
        popUp.initOwner(window);

        popUp.getDialogPane().setContent(dialogPane);
        popUp.setHeaderText("Create a new note");
        popUp.setGraphic(null);
        popUp.setTitle("Create a new note");



        final Optional<ButtonType> option = popUp.showAndWait();
    }
}
