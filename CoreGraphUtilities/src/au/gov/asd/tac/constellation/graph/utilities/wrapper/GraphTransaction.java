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
package au.gov.asd.tac.constellation.graph.utilities.wrapper;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import java.util.stream.Stream;

/**
 *
 * @author capella
 */
public class GraphTransaction extends GraphElement {

    public GraphTransaction(GraphWrapper graph, int id) {
        super(GraphElementType.TRANSACTION, graph, id);
    }

    @Override
    public void unsafeImmediateRemove() {
        graph.getWritableGraph().removeTransaction(id);
    }

    public SchemaTransactionType getTypeValue() {
        SchemaTransactionType transactionType = getObjectValue(AnalyticConcept.TransactionAttribute.TYPE.getName());
        if (transactionType == null) {
            transactionType = SchemaTransactionTypeUtilities.getDefaultType();
        }
        return transactionType;
    }

    public GraphLink getLink() {
        return new GraphLink(graph, graph.getReadableGraph().getTransactionLink(id));
    }

    public boolean isDirected() {
        return graph.getReadableGraph().getTransactionDirection(id) != Graph.FLAT;
    }

    public GraphVertex getSourceVertex() {
        return new GraphVertex(graph, graph.getReadableGraph().getTransactionSourceVertex(id));
    }

    public GraphVertex getDestinationVertex() {
        return new GraphVertex(graph, graph.getReadableGraph().getTransactionDestinationVertex(id));
    }

    public void setSourceVertex(GraphVertex vertex) {
        graph.getWritableGraph().setTransactionSourceVertex(id, vertex.getId());
    }

    public void setDestinationVertex(GraphVertex vertex) {
        graph.getWritableGraph().setTransactionDestinationVertex(id, vertex.getId());
    }

    public Stream<GraphVertex> streamVertices() {
        return Stream.of(getSourceVertex(), getDestinationVertex());
    }

    public void completeWithSchema() {
        graph.getWritableGraph().getSchema().completeTransaction(graph.getWritableGraph(), id);
    }
}
