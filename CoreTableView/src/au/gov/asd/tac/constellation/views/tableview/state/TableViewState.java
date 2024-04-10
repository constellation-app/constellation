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
package au.gov.asd.tac.constellation.views.tableview.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An object representing the state of the Table View.
 *
 * @author cygnus_x-1
 */
public final class TableViewState {

    private boolean selectedOnly;
    private GraphElementType elementType;
    private List<Tuple<String, Attribute>> vertexColumnAttributes;
    private List<Tuple<String, Attribute>> transactionColumnAttributes;

    public TableViewState() {
        this.selectedOnly = false;
        this.elementType = GraphElementType.TRANSACTION;
        this.transactionColumnAttributes = null;
        this.vertexColumnAttributes = null;
    }

    public TableViewState(final TableViewState state) {
        this.selectedOnly = state != null && state.selectedOnly;
        this.elementType = state == null
                ? GraphElementType.TRANSACTION : state.elementType;
        this.transactionColumnAttributes = state == null
                ? null : state.transactionColumnAttributes;
        this.vertexColumnAttributes = state == null
                ? null : state.vertexColumnAttributes;
    }

    public boolean isSelectedOnly() {
        return selectedOnly;
    }

    public void setSelectedOnly(final boolean selectedOnly) {
        this.selectedOnly = selectedOnly;
    }

    public GraphElementType getElementType() {
        return elementType;
    }

    public void setElementType(final GraphElementType elementType) {
        this.elementType = elementType;
    }

    public List<Tuple<String, Attribute>> getTransactionColumnAttributes() {
        return transactionColumnAttributes;
    }

    public void setTransactionColumnAttributes(final List<Tuple<String, Attribute>> transactionColumnAttributes) {
        this.transactionColumnAttributes = transactionColumnAttributes;
    }

    public List<Tuple<String, Attribute>> getVertexColumnAttributes() {
        return vertexColumnAttributes;
    }

    public void setVertexColumnAttributes(final List<Tuple<String, Attribute>> vertexColumnAttributes) {
        this.vertexColumnAttributes = vertexColumnAttributes;
    }

    public List<Tuple<String, Attribute>> getColumnAttributes() {
        return elementType == GraphElementType.VERTEX
                ? vertexColumnAttributes : transactionColumnAttributes;
    }
    
    public void setColumnAttributes(final List<Tuple<String, Attribute>> columnAttributes) {
        switch (elementType) {
            case GraphElementType.TRANSACTION:
                this.transactionColumnAttributes = columnAttributes;
                break;
            case GraphElementType.VERTEX:
                this.vertexColumnAttributes = columnAttributes;
                break;
            case LINK: // TODO if I can get custom link columns
            case EDGE:
                this.transactionColumnAttributes = columnAttributes;
                break;
            default:
                this.transactionColumnAttributes = columnAttributes;
                break;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TableViewState rhs = (TableViewState) o;

        return new EqualsBuilder()
                .append(isSelectedOnly(), rhs.isSelectedOnly())
                .append(getElementType(), rhs.getElementType())
                .append(getTransactionColumnAttributes(), rhs.getTransactionColumnAttributes())
                .append(getVertexColumnAttributes(), rhs.getVertexColumnAttributes())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(isSelectedOnly())
                .append(getElementType())
                .append(getVertexColumnAttributes())
                .append(getTransactionColumnAttributes())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("selectedOnly", isSelectedOnly())
                .append("elementType", getElementType())
                .append("transactionColumnAttributes", getTransactionColumnAttributes())
                .append("vertexColumnAttributes", getVertexColumnAttributes())
                .toString();
    }
}
