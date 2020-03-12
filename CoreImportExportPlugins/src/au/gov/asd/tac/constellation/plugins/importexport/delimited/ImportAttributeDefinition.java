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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.translator.AttributeTranslator;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;

/**
 * An ImportAttributeDefinition provides all the information and functionality
 * to extract a given field out of a table, translate it if necessary, and use
 * it to set a given attribute value on the graph.
 *
 * @author sirius
 */
public class ImportAttributeDefinition {

    private final String columnLabel;
    private final int columnIndex;
    private final Attribute attribute;
    private final AttributeTranslator translator;
    private final String defaultValue;
    private final PluginParameters parameters;

    private static int ATTRIBUTE_NOT_DEFINED = -93459;
    private static int ROWID_COLUMN_INDEX = -1;

    /**
     * Immutable attributes are defined in {@code ImportController} as mockup
     * attributes when the "Show all schema attributes" checkbox is selected. No
     * attribute id is defined at this point because it is not "ensured" to the
     * graph. When an attribute needs to be added to the graph this
     * overriddenAttributeId will be used to store the id. This approach
     * prevents any modifications to the {@code Attribute} interface.
     */
    private int overriddenAttributeId = ATTRIBUTE_NOT_DEFINED;

    public ImportAttributeDefinition(final int columnIndex, final Attribute attribute, final AttributeTranslator translator, final String defaultValue, final PluginParameters parameters) {
        this.columnLabel = null;
        this.columnIndex = columnIndex;
        this.attribute = attribute;
        this.translator = translator;
        this.defaultValue = defaultValue;
        this.parameters = parameters;
    }

    public ImportAttributeDefinition(final String defaultValue, final Attribute attribute, final AttributeTranslator translator, final PluginParameters parameters) {
        this.columnLabel = null;
        this.columnIndex = ImportDelimitedPlugin.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN;
        this.attribute = attribute;
        this.translator = translator;
        this.defaultValue = defaultValue;
        this.parameters = parameters;
    }

    /**
     * Use when loading a template.
     * <p>
     * The template knows the column name, but not the column index, because the
     * user might have moved the columns around. We need to remember the column
     * name so we can fix up the index when assigning this to the correct
     * column.
     *
     * @param columnLabel the label of the column that provides the data.
     * @param defaultValue the default value for this column.
     * @param attribute the attribute that will store the data.
     * @param translator a translator that can modify the values before storing
     * on the graph.
     * @param parameters parameters that are passed to the translator.
     */
    public ImportAttributeDefinition(final String columnLabel, final String defaultValue, final Attribute attribute, final AttributeTranslator translator, final PluginParameters parameters) {
        this.columnLabel = columnLabel;
        this.columnIndex = Integer.MAX_VALUE;
        this.attribute = attribute;
        this.translator = translator;
        this.defaultValue = defaultValue;
        this.parameters = parameters;
    }

    public String getColumnLabel() {
        return columnLabel;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public AttributeTranslator getTranslator() {
        return translator;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public PluginParameters getParameters() {
        return parameters;
    }

    /**
     * If the attribute id in {@code attribute} is defined then return the id or
     * return the overridden id
     *
     * @return The attribute id
     */
    public int getOverriddenAttributeId() {
        if (overriddenAttributeId == ATTRIBUTE_NOT_DEFINED) {
            return attribute.getId();
        }

        return overriddenAttributeId;
    }

    /**
     * Set the new attribute id which can be set after an "ensure" is done to
     * the graph
     *
     * @param overriddenAttributeId the id of the overridden attribute.
     */
    public void setOverriddenAttributeId(int overriddenAttributeId) {
        this.overriddenAttributeId = overriddenAttributeId;
    }

    public void setValue(GraphWriteMethods graph, int elementId, String[] row, int rowIndex) {
        if (columnIndex == ImportDelimitedPlugin.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN && defaultValue != null) {
            graph.setStringValue(getOverriddenAttributeId(), elementId, translator.translate(defaultValue, parameters));
        } else if (columnIndex >= 0) {
            final String cell = columnIndex < row.length ? row[columnIndex] : "";
            graph.setStringValue(getOverriddenAttributeId(), elementId, translator.translate(cell, parameters));
        } else if (columnIndex == ROWID_COLUMN_INDEX) {
            graph.setStringValue(getOverriddenAttributeId(), elementId, Integer.toString(rowIndex));
        }
    }

    @Override
    public String toString() {
        return String.format("[IAD column %s %s (column %d); attr %s; translator %s]", attribute.getElementType(), columnLabel, columnIndex, attribute.getName(), translator.getLabel());
    }
}
