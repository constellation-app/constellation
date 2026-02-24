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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.attribute.TimeZoneAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.openide.util.lookup.ServiceProvider;

/**
 * Editor Factory for attributes of type time_zone
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class TimeZoneEditorFactory extends AttributeValueEditorFactory<ZoneId> {

    @Override
    public AbstractEditor<ZoneId> createEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<ZoneId> validator, final ZoneId defaultValue, final ZoneId initialValue) {
        return new TimeZoneEditor(editedItemName, editOperation, validator, defaultValue, initialValue);
    }

    @Override
    public String getAttributeType() {
        return TimeZoneAttributeDescription.ATTRIBUTE_NAME;
    }

    public class TimeZoneEditor extends AbstractEditor<ZoneId> {

        private ComboBox<ZoneId> timeZoneComboBox;

        private final Comparator<ZoneId> zoneIdComparator = (t1, t2) -> {
            final int offsetCompare = Integer.compare(TimeZone.getTimeZone(t1).getRawOffset(), TimeZone.getTimeZone(t2).getRawOffset());
            return offsetCompare != 0 ? offsetCompare : t1.getId().compareTo(t2.getId());
        };

        protected TimeZoneEditor(final String editedItemName, final EditOperation editOperation, final ValueValidator<ZoneId> validator, final ZoneId defaultValue, final ZoneId initialValue) {
            super(editedItemName, editOperation, validator, defaultValue, initialValue);
        }

        @Override
        protected boolean canSet(final ZoneId value) {
            // Time zones cannot be null, so prevent null values being set.
            return value != null;
        }

        @Override
        public void updateControlsWithValue(final ZoneId value) {
            timeZoneComboBox.getSelectionModel().select(value);
        }

        @Override
        protected ZoneId getValueFromControls() {
            return timeZoneComboBox.getValue();
        }

        @Override
        protected Node createEditorControls() {
            final ObservableList<ZoneId> timeZones = FXCollections.observableArrayList();
            ZoneId.getAvailableZoneIds().forEach(id -> timeZones.add(ZoneId.of(id)));
            timeZoneComboBox = new ComboBox<>(timeZones.sorted(zoneIdComparator));
            timeZoneComboBox.getSelectionModel().select(TemporalUtilities.UTC);
            timeZoneComboBox.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());
            
            final Callback<ListView<ZoneId>, ListCell<ZoneId>> cellFactory = p -> new ListCell<>() {
                @Override
                protected void updateItem(final ZoneId item, final boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(TemporalUtilities.getTimeZoneAsString(item));
                    }
                }
            };
            timeZoneComboBox.setCellFactory(cellFactory);
            timeZoneComboBox.setButtonCell(cellFactory.call(null));
            
            final VBox controls = new VBox(timeZoneComboBox);
            controls.setAlignment(Pos.CENTER);
            
            return controls;
        }
    }
}
