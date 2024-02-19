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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.PluginSequenceEditOperation;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 *
 * @author twilight_sparkle
 * @param <V>
 */
public abstract class AbstractEditorFactory<V> {

    public AbstractEditor<V> createEditor(final EditOperation editOperation, final ValueValidator<V> validator, final String editedItemName, final V initialValue) {
        return createEditor(editOperation, DefaultGetter.getDefaultUnsupported(), validator, editedItemName, initialValue);
    }

    public AbstractEditor<V> createEditor(final EditOperation editOperation, final String editedItemName, final V initialValue) {
        return createEditor(editOperation, DefaultGetter.getDefaultUnsupported(), ValueValidator.getAlwaysSucceedValidator(), editedItemName, initialValue);
    }

    public AbstractEditor<V> createEditor(final EditOperation editOperation, final DefaultGetter<V> defaultGetter, final String editedItemName, final V initialValue) {
        return createEditor(editOperation, defaultGetter, ValueValidator.getAlwaysSucceedValidator(), editedItemName, initialValue);
    }

    public abstract AbstractEditor<V> createEditor(final EditOperation editOperation, final DefaultGetter<V> defaultGetter, final ValueValidator<V> validator, final String editedItemName, final V initialValue);

    public abstract static class AbstractEditor<V> {

        private static final String HEADING_DEFAULT_STYLE = "header";
        protected static final int CONTROLS_DEFAULT_HORIZONTAL_SPACING = 5;
        protected static final int CONTROLS_DEFAULT_VERTICAL_SPACING = 10;

        protected final EditOperation editOperation;
        protected final DefaultGetter<V> defaultGetter;
        protected final ValueValidator<V> validator;
        protected final BooleanProperty disableEditProperty;
        protected final StringProperty errorMessageProperty;
        protected final String editedItemName;
        protected V currentValue;
        protected V savedValue;
        protected Node editorHeading = null;
        protected Node editorControls = null;

        protected boolean updateInProgress = false;

        protected AbstractEditor(final EditOperation editOperation, final DefaultGetter<V> defaultGetter, final ValueValidator<V> validator, final String editedItemName, final V initialValue) {
            this.editOperation = editOperation;
            this.defaultGetter = defaultGetter;
            this.validator = validator;
            this.disableEditProperty = new SimpleBooleanProperty();
            this.errorMessageProperty = new SimpleStringProperty();
            this.editedItemName = editedItemName;
            setCurrentValue(initialValue);
        }

        public final void storeValue() {
            savedValue = currentValue;
        }

        public final void restoreValue() {
            setCurrentValue(savedValue);
        }

        protected final V getCurrentValue() {
            return currentValue;
        }

        public final ReadOnlyBooleanProperty getEditDisabledProperty() {
            return disableEditProperty;
        }

        public final ReadOnlyStringProperty getErrorMessageProperty() {
            return errorMessageProperty;
        }

        /**
         * Should be called when anything in the gui changes (and on the javafx
         * thread).
         */
        protected final void update() {
            if (!updateInProgress) {
                updateInProgress = true;
                try {
                    currentValue = getValueFromControls();
                    final String error = validateCurrentValue();
                    errorMessageProperty.set(error);
                    disableEditProperty.set(error != null);
                } catch (final ControlsInvalidException ex) {
                    disableEditProperty.set(true);
                    errorMessageProperty.set(ex.getReason());
                }

                updateInProgress = false;
            }
        }

        /**
         * Prevents values being explicitly set which are not valid for this
         * type and can't be reflected in the controls.
         *
         * @param value the candidate value to be set.
         * @return true only if the candidate value is valid for this type.
         */
        protected boolean canSet(final V value) {
            return true;
        }

        protected final void setCurrentValue(final V value) {
            if (canSet(value)) {
                this.currentValue = value;
                if (editorControls != null) {
                    updateInProgress = true;
                    updateControlsWithValue(currentValue);
                    updateInProgress = false;
                }
                final String error = validateCurrentValue();
                errorMessageProperty.set(error);
                disableEditProperty.set(error != null);
            }
        }

        public final void setDefaultValue() {
            setCurrentValue(defaultGetter.getDefaultValue());
        }

        public final Node getEditorControls() {
            if (editorControls == null) {
                editorControls = createEditorControls();
                updateInProgress = true;
                updateControlsWithValue(currentValue);
                updateInProgress = false;
                update();
            }
            return editorControls;
        }

        public final Node getEditorHeading() {
            if (editorHeading == null) {
                editorHeading = createEditorHeading();
            }

            return editorHeading;
        }

        public Plugin preEdit() {
            return null;
        }

        public Plugin postEdit() {
            return null;
        }

        public final void performEdit() {
            if (!disableEditProperty.get()) {
                if (editOperation instanceof PluginSequenceEditOperation) {
                    ((PluginSequenceEditOperation) editOperation).setPreEdit(preEdit());
                    ((PluginSequenceEditOperation) editOperation).setPostEdit(postEdit());
                }

                editOperation.performEdit(getCurrentValue());
            }
        }

        /**
         * Whether or not the currentValue of type V is suitable to be used to
         * performEdit()
         *
         * @return true if the current value is valid.
         */
        public final String validateCurrentValue() {
            return validator.validateValue(currentValue);
        }

        /**
         * Attempt to get a value of type V that is represented by the current
         * configuration of this editor's controls. If the current configuration
         * does not represent a value of type V (for example alpha characters
         * have been entered into a text box used to specify an integer field),
         * then a {@link ControlsInvalidException} should be thrown.
         * <br>
         * Note that exceptions should only be thrown when the controls do not
         * represent a value of type V. Cases where certain values of V should
         * not be set by a given instance of an editor should be handled by
         * creating the editor with an appropriate {@link ValueValidator}.
         *
         * @return The value of type V represented by the current configuration
         * of the controls.
         * @throws ControlsInvalidException If the current configuration of the
         * controls does not represent a value of type V.
         */
        protected abstract V getValueFromControls() throws ControlsInvalidException;

        protected abstract void updateControlsWithValue(final V value);

        protected Node createEditorHeading() {
            final Label heading = new Label(String.format("%s:", editedItemName));
            heading.setId(HEADING_DEFAULT_STYLE);
            return heading;
        }

        protected abstract Node createEditorControls();

    }

    /**
     * An Exception to be thrown by {@link AbstractEditorFactory.AbstractEditor}
     * objects when there is a request for their current value, but their
     * controls do not currently represent a valid value.
     */
    protected static class ControlsInvalidException extends Exception {

        private final String reason;

        public ControlsInvalidException(final String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }
}
