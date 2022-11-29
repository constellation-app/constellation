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
package au.gov.asd.tac.constellation.plugins.importexport.model;

import au.gov.asd.tac.constellation.plugins.importexport.RowFilter;

/**
 *
 * @author sirius
 */
public class TableRow {

    private static final CellValueProperty NULL_CELL = new CellValueProperty("");

    private final int row;
    private final String[] values;
    private final CellValueProperty[] properties;

    public TableRow(final int row, final String[] values) {
        this.row = row;
        this.values = values;
        properties = new CellValueProperty[values.length + 1];
        properties[0] = new CellValueProperty(row);
        for (int i = 0; i < values.length; i++) {
            properties[i + 1] = new CellValueProperty(values[i]);
        }
    }

    public int getRow() {
        return row;
    }

    public String[] getValues() {
        return values;
    }

    public CellValueProperty getProperty(final int column) {
        return properties.length > column ? properties[column] : NULL_CELL;
    }

    public void setIncluded(final boolean included) {
        for (final CellValueProperty property : properties) {
            property.setIncluded(included);
        }
    }

    public boolean filter(final RowFilter filter) {
        if (filter.passesFilter(row, values)) {
            setIncluded(true);
            return true;
        } else {
            setIncluded(false);
            return false;
        }
    }
}
