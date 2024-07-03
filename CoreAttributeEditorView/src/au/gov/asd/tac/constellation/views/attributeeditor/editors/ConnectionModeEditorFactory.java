/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ConnectionModeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class ConnectionModeEditorFactory extends AttributeValueEditorFactory<ConnectionMode> {

    @Override
    public AbstractEditor<ConnectionMode> createEditor(final EditOperation editOperation, final DefaultGetter<ConnectionMode> defaultGetter, final ValueValidator<ConnectionMode> validator, final String editedItemName, final ConnectionMode initialValue) {
        return new ConnectionModeEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return ConnectionModeAttributeDescription.ATTRIBUTE_NAME;
    }

    public class ConnectionModeEditor extends AbstractEditor<ConnectionMode> {

        private ComboBox<ConnectionMode> connectionModeComboBox;

        protected ConnectionModeEditor(final EditOperation editOperation, final DefaultGetter<ConnectionMode> defaultGetter, final ValueValidator<ConnectionMode> validator, final String editedItemName, final ConnectionMode initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        protected boolean canSet(final ConnectionMode value) {
            // As ConnectionMode is an enum, we want one of its constants, not null.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final ConnectionMode value) {
            connectionModeComboBox.getSelectionModel().select(value);
        }

        @Override
        protected ConnectionMode getValueFromControls() {
            return connectionModeComboBox.getValue();
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setHgap(CONTROLS_DEFAULT_HORIZONTAL_SPACING);

            final Label connectionModeLabel = new Label("Connection Mode:");
            final ObservableList<ConnectionMode> connectionModes = FXCollections.observableArrayList(ConnectionMode.values());
            connectionModeComboBox = new ComboBox<>(connectionModes);
            final Callback<ListView<ConnectionMode>, ListCell<ConnectionMode>> cellFactory = (final ListView<ConnectionMode> p) -> new ListCell<ConnectionMode>() {
                @Override
                protected void updateItem(final ConnectionMode item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.name());
                    }
                }
            };
            connectionModeComboBox.setCellFactory(cellFactory);
            connectionModeComboBox.setButtonCell(cellFactory.call(null));
            connectionModeLabel.setLabelFor(connectionModeComboBox);
            connectionModeComboBox.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());

            controls.addRow(0, connectionModeLabel, connectionModeComboBox);
            return controls;
        }

        @Override
        public boolean noValueCheckBoxAvailable() {
            return false;
        }
    }
}
