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
package au.gov.asd.tac.constellation.views.tableview.api;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.TransactionTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * POJO representing the table user preferences that can be persisted to disk as
 * a JSON file.
 *
 * @author formalhaunt
 */
public final class UserTablePreferences implements TableDefaultColumns{

    public static final int DEFAULT_MAX_ROWS_PER_PAGE = 500;

    @JsonProperty(value = "PageSize")
    private int maxRowsPerPage = DEFAULT_MAX_ROWS_PER_PAGE;

    @JsonProperty("ColumnOrder")
    private List<String> columnOrder = new ArrayList<>();

    @JsonProperty("SortByColumn")
    @JsonDeserialize(using = ColumnSortOrderDeserializer.class)
    private Pair<String, TableColumn.SortType> sortByColumn = ImmutablePair.of("", TableColumn.SortType.ASCENDING);
    private List<GraphAttribute> defaultColumns = new ArrayList<>();

    public synchronized int getMaxRowsPerPage() {
        return maxRowsPerPage;
    }

    public synchronized void setMaxRowsPerPage(final int pageSize) {
        this.maxRowsPerPage = pageSize;
    }

    public synchronized List<String> getColumnOrder() {
        return columnOrder;
    }

    public synchronized void setColumnOrder(final List<String> columnOrder) {
        this.columnOrder = columnOrder;
    }

    public synchronized Pair<String, TableColumn.SortType> getSortByColumn() {
        return sortByColumn;
    }

    public synchronized void setSortByColumn(final Pair<String, TableColumn.SortType> sortByColumn) {
        this.sortByColumn = sortByColumn;
    }

    /**
     * Gets just the sort column in a thread safe way, or null if no sort is
     * set.
     *
     * @return the sort column or null if no sort is set
     */
    @JsonIgnore
    public synchronized String getSortColumn() {
        return getSortByColumn() != null ? getSortByColumn().getKey() : null;
    }

    /**
     * Gets just the sort direction in a thread safe way, or null if no sort is
     * set.
     *
     * @return the sort direction or null if no sort is set
     */
    @JsonIgnore
    public synchronized TableColumn.SortType getSortDirection() {
        return getSortByColumn() != null ? getSortByColumn().getValue() : null;
    }

    /**
     * Thread safe update of the max rows per page. Performs a check to see if
     * the new value is different to the current one and if it is performs the
     * update.
     *
     * @param pageSize the new page size to set
     * @return true if the page size was changed, false otherwise
     */
    @JsonIgnore
    public synchronized boolean updateMaxRowsPerPage(final int pageSize) {
        if (getMaxRowsPerPage() != pageSize) {
            setMaxRowsPerPage(pageSize);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final UserTablePreferences rhs = (UserTablePreferences) o;

        return new EqualsBuilder()
                .append(getMaxRowsPerPage(), rhs.getMaxRowsPerPage())
                .append(getColumnOrder(), rhs.getColumnOrder())
                .append(getSortByColumn(), rhs.getSortByColumn())
                .isEquals();
    }

    @Override
    public synchronized int hashCode() {
        return new HashCodeBuilder()
                .append(getMaxRowsPerPage())
                .append(getColumnOrder())
                .append(getSortByColumn())
                .toHashCode();
    }

    @Override
    public synchronized String toString() {
        return new ToStringBuilder(this)
                .append("pageSize", getMaxRowsPerPage())
                .append("columnOrder", getColumnOrder())
                .append("sortByColumn", getSortByColumn())
                .toString();
    }

    @Override
    public void setDefaultColumns(Graph graph) {
        final List<GraphAttribute> attributes = new ArrayList<>();
        if (graph != null && graph.getSchema() != null) {
            try (final ReadableGraph readableGraph = graph.getReadableGraph()) {
                final int attributeCount = readableGraph.getAttributeCount(GraphElementType.TRANSACTION);
                for (int i = 0; i < attributeCount; i++) {
                    attributes.add(new GraphAttribute(readableGraph, readableGraph.getAttribute(GraphElementType.TRANSACTION, i)));
                }
            }
            try (final ReadableGraph readableGraph = graph.getReadableGraph()) {
                final int attributeCount = readableGraph.getAttributeCount(GraphElementType.VERTEX);
                for (int i = 0; i < attributeCount; i++) {
                    attributes.add(new GraphAttribute(readableGraph, readableGraph.getAttribute(GraphElementType.VERTEX, i)));
                }
            }
        }

        List<String> selectedNames = new ArrayList<>();
        // set default columns to identifier, type, transaction_type, and
        // transaction datetime
        selectedNames.add(VisualConcept.VertexAttribute.IDENTIFIER.getName());
        selectedNames.add(TransactionTypeAttributeDescription.ATTRIBUTE_NAME);
        selectedNames.add(AnalyticConcept.TransactionAttribute.TYPE.getName());
        selectedNames.add(TemporalConcept.VertexAttribute.DATETIME.getName());
        defaultColumns = attributes.stream()
                .filter(attribute -> selectedNames.contains(attribute.getName()))
                .toList();        
    }

    @Override
    public List<GraphAttribute> getDefaultColumns() {
        return defaultColumns;
    }
}
