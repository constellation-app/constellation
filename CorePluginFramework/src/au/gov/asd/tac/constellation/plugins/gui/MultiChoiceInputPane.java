/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import org.controlsfx.control.CheckComboBox;

/**
 * A drop-down combo box allowing multiple selections, which is the GUI element
 * corresponding to a {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType}.
 * <p>
 * Changing the selected items in the drop down will set the checked data for
 * the underlying {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType
 *
 * @author twinkle2_little
 */
public class MultiChoiceInputPane extends HBox {

    public static final int DEFAULT_WIDTH = 300;

    private final ObservableList<ParameterValue> options = FXCollections.observableArrayList();
    private final MultiChoiceComboBox<ParameterValue> field;
    private boolean isAdjusting = false;
    private static final Logger LOGGER = Logger.getLogger(MultiChoiceInputPane.class.getName());

    public MultiChoiceInputPane(final PluginParameter<MultiChoiceParameterValue> parameter) {
        options.addAll(MultiChoiceParameterType.getOptionsData(parameter));
        field = new MultiChoiceComboBox<>(options);
        field.setPromptText(parameter.getDescription());
        if (parameter.getParameterValue().getGuiInit() != null) {
            parameter.getParameterValue().getGuiInit().init(field);
        }
        field.setDisable(!parameter.isEnabled());
        field.setManaged(parameter.isVisible());
        field.setVisible(parameter.isVisible());
        this.setManaged(parameter.isVisible());
        this.setVisible(parameter.isVisible());

        // Set properties before adding listener to ensure unwanted onChanged events do not fire.
        Platform.runLater(() -> {
            @SuppressWarnings("unchecked") // Below cast will always work because getChoicesData returns List<? extends ParameterValue>.
            final List<ParameterValue> checkedItems = (List<ParameterValue>) MultiChoiceParameterType.getChoicesData(parameter);
            checkedItems.stream().forEach(checked -> {
                final BooleanProperty bp = field.getItemBooleanProperty(checked);
                if (bp != null) {
                    bp.setValue(true);
                }
            });

            field.getCheckModel().getCheckedItems().addListener((ListChangeListener.Change<? extends ParameterValue> c) -> {
                if (!isAdjusting) {
                    MultiChoiceParameterType.setChoicesData(parameter, field.getCheckModel().getCheckedItems());
                }
            });
        });

        field.setPrefWidth(DEFAULT_WIDTH);

        parameter.addListener((PluginParameter<?> pluginParameter, ParameterChange change) -> {
            Platform.runLater(() -> {
                PluginParameter<MultiChoiceParameterValue> mcPluginParameter = (PluginParameter<MultiChoiceParameterValue>) pluginParameter;
                switch (change) {
                    case VALUE:
                        isAdjusting = true;
                        options.clear();
                        options.addAll(MultiChoiceParameterType.getOptionsData(mcPluginParameter));
                        List<ParameterValue> checkedItems = (List<ParameterValue>) MultiChoiceParameterType.getChoicesData(mcPluginParameter);

                        field.getCheckModel().getCheckedItems();
                        checkedItems.forEach(checked -> {
                            field.getCheckModel().check(checked);
                        });

                        isAdjusting = false;
                        break;
                    case ENABLED:
                        field.setDisable(!pluginParameter.isEnabled());
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
            });
        });
        getChildren().add(field);
    }

    public class MultiChoiceComboBox<T extends Object> extends CheckComboBox<T> {

        public MultiChoiceComboBox() {
            super();
        }

        public MultiChoiceComboBox(final ObservableList<T> items) {
            super(items);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            // TODO: extend default skin to use prompt text property
            return super.createDefaultSkin();
        }

        // --- prompt text (taken from JavaFX's ComboBoxBase.java)
        /**
         * The {@code ComboBox} prompt text to display, or <tt>null</tt> if no
         * prompt text is displayed. Prompt text is not displayed in all
         * circumstances, it is dependent upon the subclasses of ComboBoxBase to
         * clarify when promptText will be shown. For example, in most cases
         * prompt text will never be shown when a combo box is non-editable
         * (that is, prompt text is only shown when user input is allowed via
         * text input). This has been copied from JavaFX's ComboBoxBase.java.
         */
        private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "") {
            @Override
            protected void invalidated() {
                // Strip out newlines
                String txt = get();
                if (txt != null && txt.contains("\n")) {
                    txt = txt.replace("\n", "");
                    set(txt);
                }
            }
        };

        public final StringProperty promptTextProperty() {
            return promptText;
        }

        public final String getPromptText() {
            return promptText.get();
        }

        public final void setPromptText(final String value) {
            promptText.set(value);
        }
    }
}
