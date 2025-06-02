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
package au.gov.asd.tac.constellation.views.tableview.api;

import au.gov.asd.tac.constellation.graph.Attribute;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Description of a column in the table and its link back to the graph. A vertex
 * attribute in the table will generally have two columns in the table. A source
 * and a destination. A transaction attribute will only have one column in the
 * table.
 * <p/>
 * The {@link #attributeNamePrefix} will be one of "source.", "destination." or
 * "transaction.". This represents the type of vertex or transaction that the
 * attribute and column represent.
 * <p/>
 * The {@link #attribute} is the attribute associated to a vertex or
 * transaction.
 * <p/>
 * The {@link #tableColumn} is the actual table column representing the
 * attribute for the element type with the "direction" where applicable.
 * <p/>
 * Together {@link #attributeNamePrefix} and {@link #attribute} are a primary
 * key pointing to a single table column.
 *
 * @author formalhaunt
 */
public final class Column {

    private String attributeNamePrefix;
    private Attribute attribute;
    private TableColumn<ObservableList<String>, String> tableColumn;

    /**
     * Creates a new column.
     */
    public Column() {
        //DO NOTHING
    }

    /**
     * Creates a new column.
     *
     * @param attributeNamePrefix the prefix for the attribute name when set to
     * the column
     * @param attribute the attribute the column represents
     * @param tableColumn the actual JavaFX column in the table
     */
    public Column(final String attributeNamePrefix, final Attribute attribute,
            final TableColumn<ObservableList<String>, String> tableColumn) {
        this.attributeNamePrefix = attributeNamePrefix;
        this.attribute = attribute;
        this.tableColumn = tableColumn;
    }

    /**
     * Gets the prefix that is added to the attribute name when setting the
     * table column text.
     *
     * @return the attribute name prefix
     */
    public String getAttributeNamePrefix() {
        return attributeNamePrefix;
    }

    public void setAttributeNamePrefix(final String attributeNamePrefix) {
        this.attributeNamePrefix = attributeNamePrefix;
    }

    /**
     * Gets the graph attribute that this column represents.
     *
     * @return the graph attribute
     */
    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(final Attribute attribute) {
        this.attribute = attribute;
    }

    /**
     * Gets the actual JavaFX table column that represents this attribute.
     *
     * @return the JavaFX table column
     */
    public TableColumn<ObservableList<String>, String> getTableColumn() {
        return tableColumn;
    }

    public void setTableColumn(final TableColumn<ObservableList<String>, String> tableColumn) {
        this.tableColumn = tableColumn;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Column rhs = (Column) o;

        return new EqualsBuilder()
                .append(getAttributeNamePrefix(), rhs.getAttributeNamePrefix())
                .append(getAttribute(), rhs.getAttribute())
                .append(getTableColumn(), rhs.getTableColumn())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getAttributeNamePrefix())
                .append(getAttribute())
                .append(getTableColumn())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("attributeNamePrefix", getAttributeNamePrefix())
                .append("attribute", getAttribute())
                .append("tableColumn", getTableColumn())
                .toString();
    }
}
