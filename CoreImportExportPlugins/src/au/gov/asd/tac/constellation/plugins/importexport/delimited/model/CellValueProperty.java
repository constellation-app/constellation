/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.delimited.model;

import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author sirius
 */
public class CellValueProperty extends SimpleObjectProperty<CellValue> {

    public CellValueProperty(String label) {
        super(new CellValue(label));
    }

    public CellValueProperty(int value) {
        super(new CellValue(value));
    }

    public void setMessage(String message, boolean error) {
        get().setMessage(message, error);
        fireValueChangedEvent();
    }

    public void setText(String text) {
        get().setText(text);
        fireValueChangedEvent();
    }

    public void setIncluded(boolean included) {
        CellValue value = get();
        if (value.isIncluded() != included) {
            value.setIncluded(included);
            fireValueChangedEvent();
        }
    }
}
