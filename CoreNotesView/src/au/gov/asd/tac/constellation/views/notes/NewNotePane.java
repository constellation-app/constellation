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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author altair1673
 */
public class NewNotePane {
    private boolean isFirstTime = true;
    private final Pane dialogPane;
    private final String fontStyle = String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize());
    private final String titleFontStyle = String.format("-fx-font-size:%d;", 20);
    private static final String PROMPT_COLOR = "#909090";
    private static final double WIDTH = 1000;
    private static final double HEIGHT = 175;

    private final TextArea contentField;
    private final TextField titleField = new TextField();
    private final CheckBox applyToSelection = new CheckBox("Link note to graph selection");

    private final ColorPicker newNoteColour;

    private boolean applySelected = true;
    private static String USER_CHOSEN_COLOUR = "#942483";

    private final Button addButton = new Button("Add Note");
    private final Button cancelButton = new Button("Cancel");
    private Stage stage = null;

    private Window parent = null;

    public NewNotePane(final String userChosenColour) {
        USER_CHOSEN_COLOUR = userChosenColour;

        //parent.setOnCloseRequest(event -> closePopUp());

        dialogPane = new Pane();
        dialogPane.setMinHeight(HEIGHT);
        dialogPane.setMaxHeight(HEIGHT);
        dialogPane.setMinWidth(WIDTH);
        dialogPane.setMaxWidth(WIDTH);

        Label titleLabel = new Label("Enter note info");
        titleLabel.setStyle("-fx-text-fill:WHITE; " + titleFontStyle);
        titleLabel.setFont(Font.font(24));

        // TextField to enter new note title.
        titleField.setPromptText("Type a title...");
        titleField.setStyle(fontStyle + "-fx-prompt-text-f\n" +
 "        titleField.setStyle(ill: " + PROMPT_COLOR + ";");
        titleField.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        titleField.setStyle("-fx-text-fill: #FFFFFF;");
        titleField.setMinWidth(WIDTH - 5);


        // Checkbox to apply note to selection.
        applyToSelection.setSelected(true);
        applyToSelection.setTextFill(Color.WHITE);
        applyToSelection.setStyle("-fx-selected-box-color: #000000");
        applyToSelection.selectedProperty().addListener((ov, oldVal, newVal) -> applySelected = applyToSelection.isSelected());

        // TextArea to enter new note content.
        contentField = new TextArea();

        contentField.setMinWidth(WIDTH - 5);
        contentField.setPromptText("Type a note...");
        contentField.setStyle(fontStyle + "-fx-prompt-text-fill: " + PROMPT_COLOR + ";" + " -fx-control-inner-background:#000000;");
        //contentField.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
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
        newNoteColour = new ColorPicker(ConstellationColor.fromHtmlColor(userChosenColour).getJavaFXColor());
        newNoteColour.setOnAction(event -> USER_CHOSEN_COLOUR = ConstellationColor.fromFXColor(newNoteColour.getValue()).getHtmlColor());
        newNoteColour.setStyle("-fx-background-color: #000000;");
        newNoteColour.setOnMouseEntered(event -> newNoteColour.setStyle("-fx-background-color: #242124; "));
        newNoteColour.setOnMouseExited(event -> newNoteColour.setStyle("-fx-background-color: #000000;  "));
        newNoteColour.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

        addButton.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()) + "-fx-background-color: #26ED49;");
        addButton.setPadding(new Insets(0, 15, 0, 15));
        addButton.setMinHeight(25);
        addButton.setTextFill(Color.BLACK);
        addButton.setOnMouseEntered(event -> addButton.setStyle("-fx-background-color: #86ED26; "));
        addButton.setOnMouseExited(event -> addButton.setStyle("-fx-background-color: #26ED49;  "));


        cancelButton.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()) + "-fx-background-color: #DEC20B;");
        cancelButton.setPadding(new Insets(0, 15, 0, 15));
        cancelButton.setMinHeight(25);
        cancelButton.setTextFill(Color.BLACK);
        cancelButton.setOnAction(event -> closePopUp());
        cancelButton.setOnMouseEntered(event -> cancelButton.setStyle("-fx-background-color: #DBA800; "));
        cancelButton.setOnMouseExited(event -> cancelButton.setStyle("-fx-background-color: #DEC20B;  "));

        final HBox noteHBox = new HBox(30, applyToSelection, newNoteColour, addButton, cancelButton);

        final VBox addNoteVBox = new VBox(5, titleLabel, titleField, contentField, noteHBox);
        addNoteVBox.setAlignment(Pos.CENTER_LEFT);
        addNoteVBox.setStyle(fontStyle + "-fx-padding: 5px;");
        addNoteVBox.setMinHeight(200);

        dialogPane.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#222222").getJavaFXColor(), null, null)));

        dialogPane.getChildren().add(addNoteVBox);
    }

    public void showPopUp() {
        if (isFirstTime) {
            parent.setOnCloseRequest(event -> closePopUp());
            stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Create new note");
            stage.setMinHeight(HEIGHT * 2);
            stage.setMinWidth(WIDTH);
            stage.setResizable(true);
            stage.setScene(new Scene(dialogPane));
            isFirstTime = false;
        }

        if (!stage.isShowing()) {
            stage.show();
        }

    }

    public TextField getTitleField() {
        return titleField;
    }

    public TextArea getContentField() {
        return contentField;
    }

    public Button getAddButtion() {
        return addButton;
    }

    public String getUserChosenColour() {
        return USER_CHOSEN_COLOUR;
    }

    public boolean isApplySelected() {
        return applySelected;
    }

    public void closePopUp() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setParent(Window parent) {
        this.parent = parent;
    }

}
