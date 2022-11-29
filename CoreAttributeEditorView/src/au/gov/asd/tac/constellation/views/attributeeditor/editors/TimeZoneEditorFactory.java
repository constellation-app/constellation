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

import au.gov.asd.tac.constellation.graph.attribute.TimeZoneAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class TimeZoneEditorFactory extends AttributeValueEditorFactory<ZoneId> {

    @Override
    public AbstractEditor<ZoneId> createEditor(final EditOperation editOperation, final DefaultGetter<ZoneId> defaultGetter, final ValueValidator<ZoneId> validator, final String editedItemName, final ZoneId initialValue) {
        return new TimeZoneEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
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

        protected TimeZoneEditor(final EditOperation editOperation, final DefaultGetter<ZoneId> defaultGetter, final ValueValidator<ZoneId> validator, final String editedItemName, final ZoneId initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
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
            final VBox controls = new VBox();
            controls.setSpacing(CONTROLS_DEFAULT_VERTICAL_SPACING);
            controls.setAlignment(Pos.CENTER);

            final ObservableList<ZoneId> timeZones = FXCollections.observableArrayList();
            ZoneId.getAvailableZoneIds().forEach(id -> timeZones.add(ZoneId.of(id)));
            timeZoneComboBox = new ComboBox<>();
            timeZoneComboBox.setItems(timeZones.sorted(zoneIdComparator));
            final Callback<ListView<ZoneId>, ListCell<ZoneId>> cellFactory = (final ListView<ZoneId> p) -> new ListCell<ZoneId>() {
                @Override
                protected void updateItem(final ZoneId item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(TimeZoneUtilities.getTimeZoneAsString(item));
                    }
                }
            };
            timeZoneComboBox.setCellFactory(cellFactory);
            timeZoneComboBox.setButtonCell(cellFactory.call(null));
            timeZoneComboBox.getSelectionModel().select(TimeZone.getTimeZone(ZoneOffset.UTC).toZoneId());
            timeZoneComboBox.getSelectionModel().selectedItemProperty().addListener((o, n, v) -> update());

            controls.getChildren().addAll(timeZoneComboBox);
            return controls;
        }
    }
}
