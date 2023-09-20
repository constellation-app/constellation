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
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.Window;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * A Pane that has all the controls that lets user create a new Note
 *
 * @author altair1673
 */
public class NewNotePane {

    private boolean isFirstTime = true;
    private final Pane dialogPane;
    private static final String FONT_SIZE_STRING = "-fx-font-size:%d;";
    private final String fontStyle = String.format(FONT_SIZE_STRING, FontUtilities.getApplicationFontSize());
    private static final String PROMPT_COLOR = "#909090";
    private static final double WIDTH = 500;
    private static final double HEIGHT = 300;

    private static final Logger LOGGER = Logger.getLogger(NewNotePane.class.getName());

    private int currentlyEditedNoteId = 0;

    private final TextArea contentField;
    private final TextField titleField = new TextField();
    private final CheckBox applyToSelection = new CheckBox("Link note to graph selection");
    private final CheckBox enableMarkdown = new CheckBox("Markdown");

    private final TabPane tabPane;
    private TextFlow previewTextFlow;

    private final ColorPicker newNoteColour;
    private String previousColour = "#942483";

    private boolean applySelected = true;
    private boolean markdownSelected = false;
    private static String userChosenColour;

    private BooleanProperty inEditMode = new SimpleBooleanProperty(false);

    private final Button addButton = new Button("Add Note");
    private final Button cancelButton = new Button("Cancel");
    private final Button saveButton = new Button("Save");
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

        tabPane = new TabPane();
        previewTextFlow = new TextFlow();

        previewTextFlow.setMinWidth(495);
        previewTextFlow.setTextAlignment(TextAlignment.LEFT);

        final Pane previewTabPane = new Pane();
        previewTabPane.getChildren().add(previewTextFlow);

        final ScrollPane previewTabScrollPane = new ScrollPane();
        previewTabScrollPane.setContent(previewTabPane);
        previewTabScrollPane.setMaxWidth(WIDTH);
        previewTabScrollPane.setMaxHeight(202);
        previewTabScrollPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        final VBox editVBox = new VBox(5, titleField, contentField);

        final Tab writeTab = new Tab();
        writeTab.setText("Write");
        writeTab.setContent(editVBox);
        writeTab.setClosable(false);

        final Tab previewTab = new Tab();
        previewTab.setText("Preview");
        previewTab.setContent(previewTabScrollPane);
        previewTab.setClosable(false);
        previewTab.setOnSelectionChanged(event -> {
            if (previewTab.isSelected()) {
                previewTextFlow.getChildren().clear();
                previewTabPane.getChildren().clear();
                final MarkdownTree mdTree = new MarkdownTree(titleField.getText() + "\n\n" + contentField.getText());
                mdTree.setMarkdownEnabled(markdownSelected);
                mdTree.parse();
                previewTextFlow = mdTree.getRenderedText();
                previewTextFlow.setMinWidth(495);
                previewTextFlow.setPrefWidth(495);
                previewTextFlow.setMaxWidth(495);

                resizeTextFlows(previewTextFlow, 2.0);
                previewTabPane.getChildren().add(previewTextFlow);
            }
        });
        tabPane.getTabs().addAll(writeTab, previewTab);

        enableMarkdown.setSelected(false);
        enableMarkdown.setTextFill(Color.WHITE);
        enableMarkdown.setStyle("-fx-selected-box-color: #000000");
        enableMarkdown.selectedProperty().addListener((ov, oldVal, newVal) -> markdownSelected = enableMarkdown.isSelected());

        // Colourpicker to set colour of new note
        newNoteColour = new ColorPicker(ConstellationColor.fromHtmlColor(NewNotePane.userChosenColour).getJavaFXColor());
        newNoteColour.setOnAction(event -> NewNotePane.userChosenColour = ConstellationColor.fromFXColor(newNoteColour.getValue()).getHtmlColor());
        newNoteColour.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        addButton.setStyle(String.format(FONT_SIZE_STRING, FontUtilities.getApplicationFontSize()));
        addButton.setId("add-button");

        saveButton.setStyle(String.format(FONT_SIZE_STRING, FontUtilities.getApplicationFontSize()) + "-fx-background-color: #26ED49;");
        saveButton.setPadding(new Insets(0, 15, 0, 15));
        saveButton.setMinHeight(25);
        saveButton.setTextFill(Color.BLACK);
        saveButton.setOnMouseEntered(event -> saveButton.setStyle("-fx-background-color: #86ED26; "));
        saveButton.setOnMouseExited(event -> saveButton.setStyle("-fx-background-color: #26ED49;  "));

        // Cancel button to stop creating a new note
        cancelButton.setStyle(String.format(FONT_SIZE_STRING, FontUtilities.getApplicationFontSize()));
        cancelButton.setId("add-button");
        cancelButton.setOnAction(event -> {
            clearTextFields();
            closePopUp();
        });

        final Region gap = new Region();
        gap.setMinWidth(15);
        final HBox noteHBox = new HBox(5, newNoteColour, gap, addButton, cancelButton);
        HBox.setHgrow(gap, Priority.ALWAYS);
        noteHBox.setAlignment(Pos.CENTER_RIGHT);

        final HBox cbHBox = new HBox(30, applyToSelection, enableMarkdown);
        final VBox addNoteVBox = new VBox(5, tabPane, cbHBox, noteHBox);
        addNoteVBox.setAlignment(Pos.CENTER_LEFT);
        addNoteVBox.setStyle(fontStyle + "-fx-padding: 5px;");
        addNoteVBox.setMinHeight(220);

        dialogPane.setBackground(new Background(new BackgroundFill(ConstellationColor.fromHtmlColor("#222222").getJavaFXColor(), null, null)));

        dialogPane.getChildren().add(addNoteVBox);

        inEditMode.addListener((obj, old, newVal) -> {
            noteHBox.getChildren().clear();
            cbHBox.getChildren().clear();
            if (newVal) {
                noteHBox.getChildren().addAll(newNoteColour, gap, saveButton, cancelButton);
                cbHBox.getChildren().add(enableMarkdown);
            } else {
                noteHBox.getChildren().addAll(newNoteColour, gap, addButton, cancelButton);
                cbHBox.getChildren().addAll(applyToSelection, enableMarkdown);
            }
        });

    }

    private void resizeTextFlows(final TextFlow textFlow, final double scale) {
        for (int i = 0; i < textFlow.getChildren().size(); ++i) {
            if (textFlow.getChildren().get(i) instanceof TextFlow) {
                resizeTextFlows((TextFlow) textFlow.getChildren().get(i), scale + 0.5);
            }
        }
        textFlow.setMaxHeight(textFlow.getMaxHeight() / scale);
        textFlow.setMinWidth(495);
    }

    /**
     * Instantiate stage for the pop up and set event handler to close it when consty closes
     */
    public void showPopUp() {
        if (isFirstTime) {
            parent.setOnCloseRequest(event -> {
                clearTextFields();
                addButton.setDisable(true);
                cancelButton.setDisable(true);
                closePopUp();
            });
            stage = new Stage();

            stage.setOnCloseRequest(event -> clearTextFields());

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(parent);
            stage.setAlwaysOnTop(true);
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

        final JDialog hiddenDialog = new JDialog();
        hiddenDialog.setModal(true);
        hiddenDialog.setUndecorated(true);
        hiddenDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        final List<Screen> screens = Screen.getScreensForRectangle(parent.getX(), parent.getY(), parent.widthProperty().get(), parent.heightProperty().get());

        stage.setTitle(inEditMode.get() ? "Edit note" : "Create new note");

        stage.setX((screens.get(0).getVisualBounds().getMinX() + screens.get(0).getVisualBounds().getWidth() / 2) - WIDTH / 2);
        stage.setY((screens.get(0).getVisualBounds().getMinY() + screens.get(0).getVisualBounds().getHeight() / 2) - (HEIGHT * 2.5) / 2);

        try {
            SwingUtilities.invokeLater(() -> hiddenDialog.setVisible(true));
            if (!stage.isShowing()) {
                stage.showAndWait();
            }
        } catch (final IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Error opening popup", e);
        } finally {
            hiddenDialog.dispose();
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

    public Button getSaveButton() {
        return saveButton;
    }


    public String getUserChosenColour() {
        return userChosenColour;
    }

    public boolean isApplySelected() {
        return applySelected;
    }

    public boolean isMarkdownSelected() {
        return markdownSelected;
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

    public void setEditMode(final boolean edit) {
        inEditMode.set(edit);
    }

    public int getCurrentlyEditedNoteId() {
        return currentlyEditedNoteId;
    }

    public void setCurrentlyEditedNoteId(final int currentlyEditedNoteId) {
        this.currentlyEditedNoteId = currentlyEditedNoteId;
    }

    public ColorPicker getColourPicker() {
        return newNoteColour;
    }

    public CheckBox getMarkdownCheckbox() {
        return enableMarkdown;
    }

    public String getPreviousColour() {
        return previousColour;
    }

    public void setPreviousColour(final String previousColour) {
        this.previousColour = previousColour;
    }


}
