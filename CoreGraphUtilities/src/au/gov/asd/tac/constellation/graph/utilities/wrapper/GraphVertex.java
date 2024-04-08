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
package au.gov.asd.tac.constellation.graph.utilities.wrapper;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author capella
 */
public class GraphVertex extends GraphElement {

    public GraphVertex(GraphWrapper graph, int id) {
        super(GraphElementType.VERTEX, graph, id);
    }

    @Override
    public void unsafeImmediateRemove() {
        graph.getWritableGraph().removeVertex(id);
    }

    public SchemaVertexType getTypeValue() {
        SchemaVertexType vertexType = getObjectValue(AnalyticConcept.VertexAttribute.TYPE.getName());
        if (vertexType == null) {
            vertexType = SchemaVertexTypeUtilities.getDefaultType();
        }
        return vertexType;
    }

    public void setTypeValue(SchemaVertexType type) {
        setObjectValue(AnalyticConcept.VertexAttribute.TYPE.getName(), type);
    }

    public RawData getRawValue() {
        return (RawData) getObjectValue(AnalyticConcept.VertexAttribute.RAW.getName());
    }

    public void setRawValue(RawData raw) {
        setObjectValue(AnalyticConcept.VertexAttribute.RAW.getName(), raw);
    }

    public Stream<GraphLink> streamLinks() {
        return IntStream.range(0, graph.getReadableGraph().getVertexLinkCount(id))
                .mapToObj(i -> new GraphLink(graph, graph.getReadableGraph().getVertexLink(id, i)));
    }

    public Stream<GraphTransaction> streamTransactions() {
        return IntStream.range(0, graph.getReadableGraph().getVertexTransactionCount(id))
                .mapToObj(i -> new GraphTransaction(graph, graph.getReadableGraph().getVertexTransaction(id, i)));
    }

    public Stream<GraphTransaction> streamTransactions(GraphDirection direction) {
        return IntStream.range(0, graph.getReadableGraph().getVertexTransactionCount(id, direction.getValue()))
                .mapToObj(i -> new GraphTransaction(graph, graph.getReadableGraph().getVertexTransaction(id, direction.getValue(), i)));
    }

    public Stream<GraphStep> streamNeighbours() {
        GraphDirection[] directions = new GraphDirection[]{GraphDirection.OUTGOING, GraphDirection.INCOMING, GraphDirection.UNDIRECTED};
        return Arrays.stream(directions).flatMap(d -> streamTransactions(d).map(t -> {
            if (t.getSourceVertex().equals(this)) {
                return new GraphStep(t, this, t.getDestinationVertex(), d);
            } else {
                return new GraphStep(t, this, t.getSourceVertex(), d);
            }
        }));
    }

    private Stream<GraphStep> walkNeighbours(Predicate<GraphStep> p, Set<Integer> visited) {
        List<GraphStep> neighbours = streamNeighbours()
                .filter(n -> !visited.contains(n.getDestinationVertex().getId()))
                .filter(p)
                .collect(Collectors.toList());
        visited.addAll(neighbours.stream().map(n -> n.getDestinationVertex().getId()).collect(Collectors.toSet()));
        return Stream.concat(
                neighbours.stream(),
                neighbours.stream().flatMap(n -> n.getDestinationVertex().walkNeighbours(p, visited))
        );
    }

    public Stream<GraphStep> walkNeighbours(Predicate<GraphStep> p, boolean includeThis) {
        Set<Integer> visited = new HashSet<>();
        visited.add(this.getId());
        Stream<GraphStep> walk = walkNeighbours(p, visited);
        if (includeThis) {
            return Stream.concat(Stream.of(new GraphStep(null, null, this, GraphDirection.START)), walk);
        } else {
            return walk;
        }
    }

    public void completeWithSchema() {
        graph.getWritableGraph().getSchema().completeVertex(graph.getWritableGraph(), id);
    }

    @Override
    public String toString() {
        return getStringValue(VisualConcept.VertexAttribute.IDENTIFIER.getName()) + "<" + getStringValue(AnalyticConcept.VertexAttribute.TYPE.getName()) + ">";
    }
}
