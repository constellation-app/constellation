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
package au.gov.asd.tac.constellation.views.notes.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
    private final String titleFontStyle = String.format(FONT_SIZE_STRING, 20);
    private static final String PROMPT_COLOR = "#909090";
    private static final double WIDTH = 1005;
    private static final double HEIGHT = 190;

    private final TextArea contentField;
    private final TabPane tabPane;
    private final TextFlow preview;
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
        //titleField.setPadding(new Insets(5, 5, 5, 5));

        // Checkbox to apply note to selection.
        applyToSelection.setSelected(true);
        applyToSelection.setTextFill(Color.WHITE);
        applyToSelection.setStyle("-fx-selected-box-color: #000000");
        applyToSelection.selectedProperty().addListener((ov, oldVal, newVal) -> applySelected = applyToSelection.isSelected());

        // TextArea to enter new note content.
        contentField = new TextArea();
        contentField.setMinWidth(WIDTH - 5);
        //contentField.setPadding(new Insets(5, 5, 5, 5));
        contentField.setPromptText("Type a note...");
        contentField.setStyle(fontStyle + "-fx-prompt-text-fill: " + PROMPT_COLOR + ";" + " -fx-control-inner-background:#000000;");
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

        preview = new TextFlow();
        preview.setPrefWidth(WIDTH - 5);
        preview.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));


        tabPane = new TabPane();
        Tab writeTab = new Tab();
        writeTab.setText("Write");
        writeTab.setContent(contentField);
        writeTab.setClosable(false);

        Tab previewTab = new Tab();
        previewTab.setText("Preview");
        previewTab.setContent(preview);
        previewTab.setClosable(false);

        previewTab.setOnSelectionChanged(event -> {
            if (previewTab.isSelected()) {
                preview.getChildren().clear();
                final MarkdownTree mdTree = new MarkdownTree(contentField.getText());
                mdTree.parse();
                final List<Text> textNodes = mdTree.getTextNodes();

                for (int i = 0; i < textNodes.size(); ++i) {
                    preview.getChildren().add(textNodes.get(i));
                }
            }
        });
        tabPane.getTabs().addAll(writeTab, previewTab);

        // Colourpicker to set colour of new note
        newNoteColour = new ColorPicker(ConstellationColor.fromHtmlColor(NewNotePane.userChosenColour).getJavaFXColor());
        newNoteColour.setOnAction(event -> NewNotePane.userChosenColour = ConstellationColor.fromFXColor(newNoteColour.getValue()).getHtmlColor());
        newNoteColour.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        addButton.setStyle(String.format(FONT_SIZE_STRING, FontUtilities.getApplicationFontSize()) + "-fx-background-color: #26ED49;");
        addButton.setPadding(new Insets(0, 15, 0, 15));
        addButton.setMinHeight(25);
        addButton.setTextFill(Color.BLACK);
        addButton.setOnMouseEntered(event -> addButton.setStyle("-fx-background-color: #86ED26; "));
        addButton.setOnMouseExited(event -> addButton.setStyle("-fx-background-color: #26ED49;  "));

        // Cancel button to stop creating a new note
        cancelButton.setStyle(String.format(FONT_SIZE_STRING, FontUtilities.getApplicationFontSize()) + "-fx-background-color: #DEC20B;");
        cancelButton.setPadding(new Insets(0, 15, 0, 15));
        cancelButton.setMinHeight(25);
        cancelButton.setTextFill(Color.BLACK);
        cancelButton.setOnAction(event -> closePopUp());
        cancelButton.setOnMouseEntered(event -> cancelButton.setStyle("-fx-background-color: #DBA800; "));
        cancelButton.setOnMouseExited(event -> cancelButton.setStyle("-fx-background-color: #DEC20B;  "));

        final HBox noteHBox = new HBox(30, applyToSelection, newNoteColour, addButton, cancelButton);

        final VBox addNoteVBox = new VBox(5, titleLabel, titleField, tabPane, noteHBox);
        addNoteVBox.setAlignment(Pos.CENTER_LEFT);
        addNoteVBox.setStyle(fontStyle + "-fx-padding: 5px;");
        addNoteVBox.setMinHeight(200);

        dialogPane.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#222222").getJavaFXColor(), null, null)));

        dialogPane.getChildren().add(addNoteVBox);
    }

    /**
     * Instantiate stage for the pop up and set event handler to close it when
     * consty closes
     */
    public void showPopUp() {
        if (isFirstTime) {
            parent.setOnCloseRequest(event -> {
                addButton.setDisable(true);
                cancelButton.setDisable(true);
                closePopUp();
            });
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

    public void setParent(Window parent) {
        this.parent = parent;
    }

    public void clearTextFields() {
        titleField.clear();
        contentField.clear();
    }

}
