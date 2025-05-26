/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.keyboardshortcut;

import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.io.File;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;

/**
 * Customised TextInputDialog to read pressed keyboard shortcut with options
 * buttons
 *
 * @author spica
 */
public class TextInputDialogWithKeybordShortcut extends Dialog<String> {

    /* ************************************************************************
     *
     * Fields
     *
     **************************************************************************/
    private final GridPane grid;
    private final Label label;
    private final TextField textField;
    private final String defaultValue;

    private final Label keyboardShortcutLabel;
    private final Button keyboardShortcutButton;

    private final Label shorcutWarningLabel;
    private final Label shorcutWarningIconLabel;
    private KeyboardShortcutSelectionResult keyboardShortcutSelectionResult;


    /* ************************************************************************
     *
     * Constructors
     *
     **************************************************************************/
    /**
     * Creates a new TextInputDialog without a default value entered into the
     * dialog {@link TextField}.
     */
    public TextInputDialogWithKeybordShortcut(final File preferenceDirectory, final Optional<String> ks) {
        this("", preferenceDirectory, ks);
    }

    /**
     * Creates a new TextInputDialog with the default value entered into the
     * dialog {@link TextField}.
     *
     * @param defaultValue the default value entered into the dialog
     */
    public TextInputDialogWithKeybordShortcut(@NamedArg("defaultValue") final String defaultValue, final File preferenceDirectory, final Optional<String> ks) {
        final DialogPane dialogPane = getDialogPane();

        // -- textfield
        this.textField = new TextField(defaultValue);
        this.textField.setMaxWidth(Double.MAX_VALUE);
        this.textField.setMinWidth(250);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        GridPane.setFillWidth(textField, true);

        // -- label
        label = createContentLabel(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        // Leyboard shortcut label. Showing existing/ptoposed shortcut assigned to the template
        keyboardShortcutLabel = createLabel();
        keyboardShortcutLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

        final ImageView warningImage = new ImageView(UserInterfaceIconProvider.WARNING.buildImage(20, new java.awt.Color(255, 128, 0)));
        final Tooltip warningToolTip = new Tooltip("This shortcut is currently assigned to another template");
        if (!StringUtils.isBlank(keyboardShortcutLabel.getText())) {
            keyboardShortcutLabel.setStyle(" -fx-text-alignment: center; -fx-font-size: 13px; -fx-border-style: solid; -fx-border-width: 1; -fx-border-color: #909090;");
        }
        keyboardShortcutLabel.setPadding(new Insets(2, 10, 2, 10));
        keyboardShortcutLabel.setGraphic(null);
        keyboardShortcutLabel.setTooltip(null);
        keyboardShortcutLabel.setContentDisplay(ContentDisplay.RIGHT);

        shorcutWarningLabel = createLabel();
        shorcutWarningLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        GridPane.setHgrow(shorcutWarningLabel, Priority.ALWAYS);
        GridPane.setFillWidth(shorcutWarningLabel, true);

        shorcutWarningIconLabel = createLabel();
        shorcutWarningIconLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

        keyboardShortcutSelectionResult = new KeyboardShortcutSelectionResult();

        if (ks.isPresent()) {
            keyboardShortcutLabel.setText(ks.get());
            keyboardShortcutSelectionResult.setKeyboardShortcut(ks.get());
        }

        keyboardShortcutButton = new Button("Shortcut");

        keyboardShortcutButton.setOnAction(e -> {//NOSONAR
            final RecordKeyboardShortcut rk = new RecordKeyboardShortcut();
            Optional<KeyboardShortcutSelectionResult> keyboardShortcut = getKeyboardShortcut(preferenceDirectory, rk);
            if (keyboardShortcut.isPresent()) {
                KeyboardShortcutSelectionResult ksResult = keyboardShortcut.get();
                keyboardShortcutLabel.setStyle(" -fx-text-alignment: center; -fx-font-size: 13px; -fx-border-style: solid; -fx-border-width: 1; -fx-border-color: #909090;");
                keyboardShortcutLabel.setText(ksResult.getKeyboardShortcut());
                keyboardShortcutSelectionResult.setKeyboardShortcut(ksResult.getKeyboardShortcut());

                if (ksResult.isAlreadyAssigned() && ksResult.getExisitngTemplateWithKs() != null) {
                    shorcutWarningLabel.setStyle(" -fx-text-alignment: center; -fx-font-size: 11.5px; -fx-text-fill: " + ConstellationColor.DARK_ORANGE.getHtmlColor() + ";");
                    shorcutWarningLabel.setText(String.format(RecordKeyboardShortcut.KEYBOARD_SHORTCUT_EXISTS_ALERT_ERROR_MSG_FORMAT, ksResult.getKeyboardShortcut()));
                    keyboardShortcutSelectionResult.setAlreadyAssigned(true);
                    keyboardShortcutSelectionResult.setExisitngTemplateWithKs(ksResult.getExisitngTemplateWithKs());
                    shorcutWarningIconLabel.setGraphic(warningImage);
                    shorcutWarningIconLabel.setTooltip(warningToolTip);
                } else {
                    shorcutWarningLabel.setText(null);
                    keyboardShortcutSelectionResult.setAlreadyAssigned(false);
                    keyboardShortcutSelectionResult.setExisitngTemplateWithKs(null);
                    shorcutWarningIconLabel.setGraphic(null);
                    shorcutWarningIconLabel.setTooltip(null);
                }
            }
        });

        this.defaultValue = defaultValue;

        this.grid = new GridPane();
        this.grid.setHgap(20);
        this.grid.setVgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        updateGrid();

        setResultConverter(dialogButton -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();

            if (data == ButtonBar.ButtonData.OK_DONE) {
                if (keyboardShortcutSelectionResult.isAlreadyAssigned() && keyboardShortcutSelectionResult.getExisitngTemplateWithKs() != null) {
                    //remove shortcut from existing template to be re-assign to new template
                    String rename = keyboardShortcutSelectionResult.getExisitngTemplateWithKs()
                            .getName().replaceAll("\\[" + keyboardShortcutSelectionResult.getKeyboardShortcut() + "\\]", StringUtils.EMPTY);
                    keyboardShortcutSelectionResult.getExisitngTemplateWithKs().renameTo(new File(preferenceDirectory, FilenameEncoder.encode(rename.trim())));
                }

                keyboardShortcutSelectionResult.setFileName(textField.getText());
                return textField.getText();
            } else {
                return null;
            }
        });
    }

    /* ************************************************************************
     *
     * Public API
     *
     **************************************************************************/
    /**
     * Returns the {@link TextField} used within this dialog.
     *
     * @return the {@link TextField} used within this dialog
     */
    public final TextField getEditor() {
        return textField;
    }

    /**
     * Returns the default value that was specified in the constructor.
     *
     * @return the default value that was specified in the constructor
     */
    public final String getDefaultValue() {
        return defaultValue;
    }

    /* ************************************************************************
     *
     * Private Implementation
     *
     **************************************************************************/
    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(textField, 0, 0, 7, 1);

        grid.add(keyboardShortcutButton, 0, 1, 2, 1);
        grid.add(keyboardShortcutLabel, 2, 1, 4, 1);
        grid.add(shorcutWarningIconLabel, 6, 1, 1, 1);

        grid.add(shorcutWarningLabel, 0, 2, 7, 1);

        getDialogPane().setContent(grid);

        Platform.runLater(() -> textField.requestFocus());
    }

    private static Label createContentLabel(final String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

    static Label createLabel() {
        Label label = new Label();
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

    public void setKSLabelText(final String ks) {
        this.keyboardShortcutLabel.setText(ks);
    }

    public static Optional<KeyboardShortcutSelectionResult> getKeyboardShortcut(final File preferenceDirectory, final RecordKeyboardShortcut rk) {
        return rk.start(preferenceDirectory);
    }

    public KeyboardShortcutSelectionResult getKeyboardShortcutSelectionResult() {
        return keyboardShortcutSelectionResult;
    }

}
