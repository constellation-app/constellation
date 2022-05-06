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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.RecentParameterValues;
import au.gov.asd.tac.constellation.plugins.parameters.RecentValuesChangeEvent;
import au.gov.asd.tac.constellation.plugins.parameters.RecentValuesListener;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;

/**
 * A text box allowing entry of single line text, multiple line text, or
 * passwords corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType}.
 * <p>
 * Editing the value in the text box will set the string value for the
 * underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType
 *
 * @author ruby_crucis
 */
public class ValueInputPane extends HBox implements RecentValuesListener {

    public static final int DEFAULT_WIDTH = 300;
    public static final int INTEGER_WIDTH = 75;

    private final ChangeListener<Number> recentValueSelectionListener;
    private final ComboBox<String> recentValuesCombo;
    private final TextInputControl field;
    private final String parameterId;
    private final boolean required;
    private static final Logger LOGGER = Logger.getLogger(ValueInputPane.class.getName());

    public ValueInputPane(final PluginParameter<?> parameter) {
        this(parameter, DEFAULT_WIDTH, null);
    }

    public ValueInputPane(final PluginParameter<?> parameter, final int defaultWidth) {
        this(parameter, defaultWidth, null);
    }

    /**
     * Primary constructor
     *
     * @param parameter parameter to link to value
     * @param defaultWidth default width (in pixels)
     * @param suggestedHeight suggested hight (in lines)
     */
    public ValueInputPane(final PluginParameter<?> parameter, final int defaultWidth, Integer suggestedHeight) {
        if (suggestedHeight == null) {
            suggestedHeight = 1;
        }
        parameterId = parameter.getId();
        required = parameter.isRequired();

        final boolean isLabel = StringParameterType.isLabel(parameter);
        if (isLabel) {
            field = null;
            recentValuesCombo = null;
            recentValueSelectionListener = null;
            final Label l = new Label(parameter.getStringValue().replace(SeparatorConstants.NEWLINE, " "));
            l.setWrapText(true);
            l.setPrefWidth(defaultWidth);
            getChildren().add(l);
            parameter.addListener((pluginParameter, change) -> Platform.runLater(() -> {
                    switch (change) {
                        case VALUE:
                            // Don't change the value if it isn't necessary.
                            // Setting the text changes the cursor position, which makes it look like text is
                            // being entered right-to-left.
                            final String param = parameter.getStringValue();
                            if (!l.getText().equals(param)) {
                                l.setText(param);
                            }
                            break;
                        case VISIBLE:
                            l.setManaged(parameter.isVisible());
                            l.setVisible(parameter.isVisible());
                            this.setVisible(parameter.isVisible());
                            this.setManaged(parameter.isVisible());
                            break;
                        default:
                            break;
                    }
                }));
        } else {
            final boolean isPassword = PasswordParameterType.ID.equals(parameter.getType().getId());
            if (isPassword) {
                recentValuesCombo = null;
            } else {
                recentValuesCombo = new ComboBox<>();
                recentValuesCombo.setEditable(false);

                recentValuesCombo.setTooltip(new Tooltip("Recent values"));
                recentValuesCombo.setMaxWidth(5);
                final List<String> recentValues = RecentParameterValues.getRecentValues(parameterId);
                if (recentValues != null) {
                    recentValuesCombo.setItems(FXCollections.observableList(recentValues));
                } else {
                    recentValuesCombo.setDisable(true);
                }

                final ListCell<String> button = new ListCell<String>() {
                    @Override
                    protected void updateItem(final String item, final boolean empty) {
                        super.updateItem(item, empty);

                        setText("...");

                    }
                };
                recentValuesCombo.setButtonCell(button);
            }

            if (isPassword) {
                field = new PasswordField();
            } else if (suggestedHeight > 1) {
                field = new TextArea();
                ((TextArea) field).setWrapText(true);
                ((TextArea) field).setPrefRowCount(suggestedHeight);
            } else {
                field = new TextField();
                Platform.runLater(() -> TextFields.bindAutoCompletion((TextField) field, recentValuesCombo.getItems()));
            }

            field.setPromptText(parameter.getDescription());
            if (parameter.getObjectValue() != null) {
                field.setText(parameter.getStringValue());
            }

            field.setPrefWidth(defaultWidth);

            if (recentValuesCombo != null) {
                recentValueSelectionListener = (ov, t, t1) -> {
                    final String value = recentValuesCombo.getValue();
                    if (value != null) {
                        field.setText(recentValuesCombo.getValue());
                    }
                };
                recentValuesCombo.getSelectionModel().selectedIndexProperty().addListener(recentValueSelectionListener);
            } else {
                recentValueSelectionListener = null;
            }

            if (parameter.getParameterValue().getGuiInit() != null) {
                parameter.getParameterValue().getGuiInit().init(field);
            }
            // If parameter is enabled, ensure widget is both enabled and editable.
            field.setEditable(parameter.isEnabled());
            field.setDisable(!parameter.isEnabled());
            field.setManaged(parameter.isVisible());
            field.setVisible(parameter.isVisible());
            this.setManaged(parameter.isVisible());
            this.setVisible(parameter.isVisible());
            if (recentValuesCombo != null) {
                recentValuesCombo.setDisable(!parameter.isEnabled());
            }

            field.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.DELETE) {
                    final IndexRange selection = field.getSelection();
                    if (selection.getLength() == 0) {
                        field.deleteNextChar();
                    } else {
                        field.deleteText(selection);
                    }
                    event.consume();
                } else if (event.isShortcutDown() && event.isShiftDown() && (event.getCode() == KeyCode.RIGHT)) {
                    field.selectNextWord();
                    event.consume();
                } else if (event.isShortcutDown() && event.isShiftDown() && (event.getCode() == KeyCode.LEFT)) {
                    field.selectPreviousWord();
                    event.consume();
                } else if (event.isShortcutDown() && (event.getCode() == KeyCode.RIGHT)) {
                    field.nextWord();
                    event.consume();
                } else if (event.isShortcutDown() && (event.getCode() == KeyCode.LEFT)) {
                    field.previousWord();
                    event.consume();
                } else if (event.isShiftDown() && (event.getCode() == KeyCode.RIGHT)) {
                    field.selectForward();
                    event.consume();
                } else if (event.isShiftDown() && (event.getCode() == KeyCode.LEFT)) {
                    field.selectBackward();
                    event.consume();
                } else if (event.isShortcutDown() && (event.getCode() == KeyCode.A)) {
                    field.selectAll();
                    event.consume();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    event.consume();
                } else {
                    // Do nothing
                }
            });

            final Tooltip tooltip = new Tooltip("");
            tooltip.setStyle("-fx-text-fill: white;");
            field.textProperty().addListener((ov, t, t1) -> {
                final String error = parameter.validateString(field.getText());
                if ((required && StringUtils.isBlank(field.getText())) || error != null) {
                    // if error is blank, the situation must be that a required parameter is blank
                    tooltip.setText(StringUtils.isNotBlank(error) ? error : "Value is required!");
                    field.setTooltip(tooltip);
                    field.setId("invalid");
                } else {
                    tooltip.setText("");
                    field.setTooltip(null);
                    field.setId("");
                }

                parameter.setStringValue(field.getText());
            });

            parameter.addListener((pluginParameter, change) -> Platform.runLater(() -> {
                    switch (change) {
                        case VALUE:
                            // Don't change the value if it isn't necessary.
                            // Setting the text changes the cursor position, which makes it look like text is
                            // being entered right-to-left.
                            final String param = parameter.getStringValue();
                            if (!field.getText().equals(param)) {
                                field.setText(param != null ? param : "");
                            }
                            break;
                        case ENABLED:
                            // If enabled, then ensure widget is both editable and enabled.
                            field.setEditable(pluginParameter.isEnabled());
                            field.setDisable(!pluginParameter.isEnabled());
                            recentValuesCombo.setDisable(!pluginParameter.isEnabled());
                            break;
                        case VISIBLE:
                            field.setManaged(parameter.isVisible());
                            field.setVisible(parameter.isVisible());
                            this.setVisible(parameter.isVisible());
                            this.setManaged(parameter.isVisible());
                            break;
                        default:
                            LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
                            break;
                    }
                }));

            final HBox fieldAndRecentValues = new HBox();
            fieldAndRecentValues.setSpacing(2);
            fieldAndRecentValues.getChildren().add(field);
            if (!isPassword) {
                fieldAndRecentValues.getChildren().add(recentValuesCombo);
                getChildren().add(fieldAndRecentValues);
                RecentParameterValues.addListener(this);
            } else {
                getChildren().add(fieldAndRecentValues);
            }
        }
    }

    @Override
    public void recentValuesChanged(final RecentValuesChangeEvent e) {
        if (recentValuesCombo != null && parameterId.equals(e.getId())) {
            //JavaFX code allows updating the UI from an JavaFX application thread.
             Platform.runLater(() -> {             
                recentValuesCombo.getSelectionModel().selectedIndexProperty().removeListener(recentValueSelectionListener);
                final List<String> recentValues = e.getNewValues();
                if (recentValues != null) {
                    recentValuesCombo.setItems(FXCollections.observableList(recentValues));
                    recentValuesCombo.setDisable(false);
                } else {
                    final List<String> empty = Collections.emptyList();
                    recentValuesCombo.setItems(FXCollections.observableList(empty));
                    recentValuesCombo.setDisable(true);
                }
                recentValuesCombo.setPromptText("...");
                recentValuesCombo.getSelectionModel().selectedIndexProperty().addListener(recentValueSelectionListener);
            });
        }
    }
}
