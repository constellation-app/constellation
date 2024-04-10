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
    private List<Tuple<String, Attribute>> edgeColumnAttributes;
    private List<Tuple<String, Attribute>> linkColumnAttributes;

    public TableViewState() {
        this.selectedOnly = false;
        this.elementType = GraphElementType.TRANSACTION;
        this.transactionColumnAttributes = null;
        this.vertexColumnAttributes = null;
        this.edgeColumnAttributes = null;
        this.linkColumnAttributes = null;
    }

    public TableViewState(final TableViewState state) {
        this.selectedOnly = state != null && state.selectedOnly;
        this.elementType = state == null
                ? GraphElementType.TRANSACTION : state.elementType;
        this.transactionColumnAttributes = state == null
                ? null : state.transactionColumnAttributes;
        this.vertexColumnAttributes = state == null
                ? null : state.vertexColumnAttributes;
        this.edgeColumnAttributes = state == null
                ? null : state.edgeColumnAttributes;
        this.linkColumnAttributes = state == null
                ? null : state.linkColumnAttributes;
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
    
    public List<Tuple<String, Attribute>> getEdgeColumnAttributes() {
        return edgeColumnAttributes;
    }

    public void setEdgeColumnAttributes(final List<Tuple<String, Attribute>> edgeColumnAttributes) {
        this.edgeColumnAttributes = edgeColumnAttributes;
    }
    
    public List<Tuple<String, Attribute>> getLinkColumnAttributes() {
        return linkColumnAttributes;
    }

    public void setLinkColumnAttributes(final List<Tuple<String, Attribute>> linkColumnAttributes) {
        this.linkColumnAttributes = linkColumnAttributes;
    }

    public List<Tuple<String, Attribute>> getColumnAttributes() {
        return switch (elementType) {
            case GraphElementType.TRANSACTION ->
                transactionColumnAttributes;
            case GraphElementType.VERTEX ->
                vertexColumnAttributes;
            case GraphElementType.LINK ->
                linkColumnAttributes;
            case GraphElementType.EDGE ->
                edgeColumnAttributes;
            default ->
                transactionColumnAttributes;
        };
    }

    public void setColumnAttributes(final List<Tuple<String, Attribute>> columnAttributes) {
        switch (elementType) {
            case GraphElementType.TRANSACTION:
                this.transactionColumnAttributes = columnAttributes;
                break;
            case GraphElementType.VERTEX:
                this.vertexColumnAttributes = columnAttributes;
                break;
            case GraphElementType.LINK: 
                this.linkColumnAttributes = columnAttributes;
                break;
            case GraphElementType.EDGE:
                this.edgeColumnAttributes = columnAttributes;
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
                .append(getEdgeColumnAttributes(), rhs.getEdgeColumnAttributes())
                .append(getLinkColumnAttributes(), rhs.getLinkColumnAttributes())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(isSelectedOnly())
                .append(getElementType())
                .append(getVertexColumnAttributes())
                .append(getTransactionColumnAttributes())
                .append(getEdgeColumnAttributes())
                .append(getLinkColumnAttributes())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("selectedOnly", isSelectedOnly())
                .append("elementType", getElementType())
                .append("transactionColumnAttributes", getTransactionColumnAttributes())
                .append("vertexColumnAttributes", getVertexColumnAttributes())
                .append("edgeColumnAttributes", getEdgeColumnAttributes())
                .append("linkColumnAttributes", getLinkColumnAttributes())
                .toString();
    }
}
