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
package au.gov.asd.tac.constellation.functionality.dialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Defines an item row which contains the Object of type I, a label and a
 * description property
 *
 * @author sirius
 * @param <I>
 */
public class ItemsRow<I> {

    private final I item;
    private final StringProperty label;
    private final StringProperty description;

    public ItemsRow(final I item, final String label, final String description) {
        this.item = item;
        this.label = new SimpleStringProperty(label);
        this.description = new SimpleStringProperty(description);
    }

    public I getItem() {
        return item;
    }

    public StringProperty labelProperty() {
        return label;
    }

    public StringProperty descriptionProperty() {
        return description;
    }
}
