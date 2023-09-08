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
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * A Pane that has all the controls that lets user create a new Note
 *
 * @author altair1673
 */
public class NewNotePane {

    private boolean isFirstTime = true;
    private final Pane dialogPane;
    private final static String FONT_SIZE_STRING = "-fx-font-size:%d;";
    private final String fontStyle = String.format(FONT_SIZE_STRING, FontUtilities.getApplicationFontSize());
    private static final String PROMPT_COLOR = "#909090";
    private static final double WIDTH = 500;
    private static final double HEIGHT = 285;

    private final TextArea contentField;
    private final TextField titleField = new TextField();
    private final CheckBox applyToSelection = new CheckBox("Link note to graph selection");

    private final ColorPicker newNoteColour;

    private boolean applySelected = true;
    private static String userChosenColour;

    private final Button addButton = new Button("Add Note");
    private final Button cancelButton = new Button("Cancel");
    private Stage stage = null;

    private Window parent = null;

    public NewNotePane(final String userChosenColour) {
        this.userChosenColour = userChosenColour;

        dialogPane = new Pane();
        dialogPane.setMinHeight(HEIGHT);
        dialogPane.setMaxHeight(HEIGHT);
        dialogPane.setMinWidth(WIDTH);
        dialogPane.setMaxWidth(WIDTH);

        if (!JavafxStyleManager.isDarkTheme()) {
            dialogPane.setStyle("-fx-background-color: #f4f4f4");
        } else {
            dialogPane.setStyle("-fx-background-color: #111111");
        }

        // TextField to enter new note title.
        titleField.setPromptText("Type a title...");
        titleField.setId("title-field");
        titleField.setMinWidth(WIDTH - 5);

        // Checkbox to apply note to selection.
        applyToSelection.setSelected(true);
        applyToSelection.setStyle("-fx-selected-box-color: #000000");
        applyToSelection.selectedProperty().addListener((ov, oldVal, newVal) -> applySelected = applyToSelection.isSelected());

        // TextArea to enter new note content.
        contentField = new TextArea();
        contentField.setMinWidth(WIDTH - 10);
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
        newNoteColour = new ColorPicker(ConstellationColor.fromHtmlColor(NewNotePane.userChosenColour).getJavaFXColor());
        newNoteColour.setOnAction(event -> NewNotePane.userChosenColour = ConstellationColor.fromFXColor(newNoteColour.getValue()).getHtmlColor());
        newNoteColour.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        addButton.setStyle(String.format(FONT_SIZE_STRING, FontUtilities.getApplicationFontSize()));
        addButton.setId("add-button");

        // Cancel button to stop creating a new note
        cancelButton.setStyle(String.format(FONT_SIZE_STRING, FontUtilities.getApplicationFontSize()));
        cancelButton.setId("add-button");
        cancelButton.setOnAction(event -> closePopUp());

        final Region gap = new Region();
        gap.setMinWidth(15);
        final HBox noteHBox = new HBox(5, newNoteColour, gap, addButton, cancelButton);
        HBox.setHgrow(gap, Priority.ALWAYS);
        noteHBox.setAlignment(Pos.CENTER_RIGHT);
        final VBox addNoteVBox = new VBox(5, titleField, contentField, applyToSelection, noteHBox);
        addNoteVBox.setAlignment(Pos.CENTER_LEFT);
        addNoteVBox.setStyle(fontStyle + "-fx-padding: 5px;");
        addNoteVBox.setMinHeight(220);

        dialogPane.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#222222").getJavaFXColor(), null, null)));

        dialogPane.getChildren().add(addNoteVBox);
    }

    /**
     * Instantiate stage for the pop up and set event handler to close it when consty closes
     */
    public void showPopUp(final Window window) {
        if (isFirstTime) {
            parent.setOnCloseRequest(event -> {
                addButton.setDisable(true);
                cancelButton.setDisable(true);
                closePopUp();
            });
            stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Create new note");
            stage.setMinHeight(HEIGHT);
            stage.setMinWidth(WIDTH);
            stage.setResizable(false);

            final Scene s = new Scene(dialogPane);
            s.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
            if (JavafxStyleManager.isDarkTheme()) {
                s.getStylesheets().add(getClass().getResource("resources/TimeFilterDark.css").toExternalForm());
            } else {
                s.getStylesheets().add(getClass().getResource("resources/TimeFilterLight.css").toExternalForm());
            }

            stage.setScene(s);

            isFirstTime = false;
        }

        final List<Screen> screens = Screen.getScreensForRectangle(window.getX(), window.getY(), window.widthProperty().get(), window.heightProperty().get());

        stage.setX((screens.get(0).getVisualBounds().getMinX() + screens.get(0).getVisualBounds().getWidth() / 2) - WIDTH / 2);
        stage.setY((screens.get(0).getVisualBounds().getMinY() + screens.get(0).getVisualBounds().getHeight() / 2) - (HEIGHT * 2.5) / 2);

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
        return userChosenColour;
    }

    public boolean isApplySelected() {
        return applySelected;
    }

    public void closePopUp() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setParent(final Window parent) {
        this.parent = parent;
    }

    public void clearTextFields() {
        titleField.clear();
        contentField.clear();
    }

}
