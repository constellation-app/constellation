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
package au.gov.asd.tac.constellation.views.tableview2.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author formalhaunt
 */
public final class TablePreferences {
    
    @JsonProperty(value = "PageSize")
    private Integer maxRowsPerPage;
    
    @JsonProperty("ColumnOrder")
    private List<String> columnOrder = new ArrayList<>();
    
    @JsonProperty("SortByColumn")
    private Map.Entry<String, TableColumn.SortType> sortByColumn
            = Pair.of("", TableColumn.SortType.ASCENDING);

    public synchronized Integer getMaxRowsPerPage() {
        return maxRowsPerPage;
    }

    public synchronized void setMaxRowsPerPage(Integer pageSize) {
        this.maxRowsPerPage = pageSize;
    }

    public synchronized List<String> getColumnOrder() {
        return columnOrder;
    }

    public synchronized void setColumnOrder(List<String> columnOrder) {
        this.columnOrder = columnOrder;
    }

    public synchronized Map.Entry<String, TableColumn.SortType> getSortByColumn() {
        return sortByColumn;
    }

    public synchronized void setSortByColumn(Map.Entry<String, TableColumn.SortType> sortByColumn) {
        this.sortByColumn = sortByColumn;
    }
    
    @JsonIgnore
    public synchronized String getSortColumn() {
        if (getSortByColumn() != null) {
            return getSortByColumn().getKey();
        }
        return null;
    }
    
    @JsonIgnore
    public synchronized TableColumn.SortType getSortDirection() {
        if (getSortByColumn() != null) {
            return getSortByColumn().getValue();
        }
        return null;
    }
    
    @JsonIgnore
    public synchronized Pair<String, TableColumn.SortType> getSort() {
        if (getSortByColumn() != null) {
            return ImmutablePair.of(getSortByColumn().getKey(),
                    getSortByColumn().getValue());
        }
        return null;
    }
    
    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TablePreferences rhs = (TablePreferences) o;

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
}
