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
package au.gov.asd.tac.constellation.graph.visual.plugins.merge;

import au.gov.asd.tac.constellation.graph.Attribute;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * The table model for the import table in the preview panel. All column classes
 * will default to String when the headers are first created.
 */
public final class PermanentMergeTableModel extends DefaultTableModel {

    private Class<?>[] headerClass;
    private List<Attribute> vertexAttributes = new ArrayList<>();

    /**
     * Initializes the table model.
     *
     * @param attributes the attributes to be displayed in the table.
     */
    public void initialise(final List<Attribute> attributes) {
        vertexAttributes = attributes;
        setupAttributeHeaders();
        getDataVector().clear();
    }

    /**
     * setup the header names for the table.
     */
    public void setupAttributeHeaders() {
        headerClass = new Class<?>[vertexAttributes.size() + 1];
        headerClass[0] = Boolean.class;
        for (int i = 0; i < (vertexAttributes.size()); i++) {
            headerClass[i + 1] = String.class;
        }
        fireTableStructureChanged();
    }

    /**
     * @return true if the there is at least 1 entry in the header, false
     * otherwise
     */
    public boolean headerExists() {
        return vertexAttributes != null && !vertexAttributes.isEmpty();
    }

    /**
     * @return number of columns
     */
    @Override
    public int getColumnCount() {
        return vertexAttributes.size();
    }

    /**
     * @param index
     * @return column name
     */
    @Override
    public String getColumnName(final int index) {
        return index == 0 ? "" : vertexAttributes.get(index).getName();
    }

    @Override
    public Class<?> getColumnClass(final int index) {
        return headerClass[index];
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return column == 0;
    }
}
