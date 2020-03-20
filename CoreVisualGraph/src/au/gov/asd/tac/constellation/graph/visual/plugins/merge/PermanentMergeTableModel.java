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
package au.gov.asd.tac.constellation.graph.visual.plugins.merge;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 * The table model for the import table in the preview panel. All column classes
 * will default to String when the headers are first created.
 */
public final class PermanentMergeTableModel extends DefaultTableModel {

    private Class<?>[] headerClass;
    private Graph graph;
    private ArrayList<Attribute> vertex_attributes = new ArrayList<>();

    /**
     * Default constructor
     */
    public PermanentMergeTableModel() {
    }

    /**
     * Initializes the table model.
     *
     * @param graph the graph that holds the vertices to be merged.
     * @param attributes the attributes to be displayed in the table.
     */
    public void initialise(final Graph graph, final ArrayList<Attribute> attributes) {
        this.graph = graph;
        vertex_attributes = attributes;
        setupAttributeHeaders();
        getDataVector().clear();
    }

    /**
     * setup the header names for the table.
     */
    public void setupAttributeHeaders() {
        headerClass = new Class<?>[vertex_attributes.size() + 1];

        headerClass[0] = Boolean.class;
        for (int i = 0; i < (vertex_attributes.size()); i++) {
            headerClass[i + 1] = String.class;
        }
        fireTableStructureChanged();
    }

    /**
     * @return true if the there is at least 1 entry in the header, false
     * otherwise
     */
    public boolean headerExists() {
        return vertex_attributes != null && vertex_attributes.size() > 0 ? true : false;
    }

    /**
     * @return number of columns
     */
    @Override
    public int getColumnCount() {
        return vertex_attributes.size();
    }

    /**
     * @param index
     * @return column name
     */
    @Override
    public String getColumnName(final int index) {
        if (index == 0) {
            return "";
        } else {
            return vertex_attributes.get(index).getName();
        }
    }

    @Override
    public Class<?> getColumnClass(final int index) {
        return headerClass[index];
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return (column == 0);
    }
}
