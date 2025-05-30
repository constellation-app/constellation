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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.utilities.io.CopyGraphUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author capella
 */
public class GraphWrapper extends GraphElement {

    private final GraphReadMethods readableGraph;
    private final GraphWriteMethods writableGraph;
    private final List<GraphElement> deferredRemoval = new ArrayList<>();

    public GraphWrapper(GraphWriteMethods graph) {
        super(GraphElementType.GRAPH, null, 0);
        this.graph = this;
        this.readableGraph = graph;
        this.writableGraph = graph;
    }

    public GraphWrapper(GraphReadMethods graph) {
        super(GraphElementType.GRAPH, null, 0);
        this.graph = this;
        this.readableGraph = graph;
        this.writableGraph = null;
    }

    public GraphReadMethods getReadableGraph() {
        return readableGraph;
    }

    public GraphWriteMethods getWritableGraph() {
        if (writableGraph == null) {
            throw new RuntimeException("GraphWrapper must be constructed with a GraphWriteMethods to do write operations.");
        }
        return writableGraph;
    }

    public void deferRemove(GraphElement element) {
        deferredRemoval.add(element);
    }

    public void completeDeferred() {
        for (GraphElement element : deferredRemoval) {
            element.unsafeImmediateRemove();
        }
        deferredRemoval.clear();
    }

    public GraphLink getLink(GraphVertex v1, GraphVertex v2) {
        return new GraphLink(this, getReadableGraph().getLink(v1.getId(), v2.getId()));
    }

    public Stream<GraphTransaction> streamTransactions() {
        return IntStream.range(0, getReadableGraph().getTransactionCount())
                .mapToObj(i -> new GraphTransaction(this, getReadableGraph().getTransaction(i)));
    }

    public Stream<GraphLink> streamLinks() {
        return IntStream.range(0, getReadableGraph().getLinkCount())
                .mapToObj(i -> new GraphLink(this, getReadableGraph().getLink(i)));
    }

    public Stream<GraphVertex> streamVertices() {
        return IntStream.range(0, getReadableGraph().getVertexCount())
                .mapToObj(i -> new GraphVertex(this, getReadableGraph().getVertex(i)));
    }

    public Stream<GraphElement> streamVerticesAndTransactions() {
        return Stream.concat(streamVertices(), streamTransactions());
    }

    public Iterable<GraphTransaction> getTransactions() {
        return () -> streamTransactions().iterator();
    }

    public Iterable<GraphLink> getLinks() {
        return () -> streamLinks().iterator();
    }

    public Iterable<GraphVertex> getVertices() {
        return () -> streamVertices().iterator();
    }

    public GraphTransaction addTransaction(GraphVertex vertex1, GraphVertex vertex2) {
        return new GraphTransaction(this, getWritableGraph().addTransaction(vertex1.getId(), vertex2.getId(), true));
    }

    @Override
    public void unsafeImmediateRemove() {
        throw new UnsupportedOperationException("You cannot remove a graph.");
    }

    @Override
    public void deferRemove() {
        throw new UnsupportedOperationException("You cannot remove a graph.");
    }

    public void validateKeys() {
        getWritableGraph().validateKey(GraphElementType.VERTEX, true);
        getWritableGraph().validateKey(GraphElementType.TRANSACTION, true);
    }

    public void addGraph(GraphWrapper newGraph) {
        addGraph(newGraph.getReadableGraph());
    }

    public void addGraph(GraphReadMethods newGraph) {
        CopyGraphUtilities.copyGraphToGraph(newGraph, getWritableGraph(), true);
    }

    public void addTo(GraphWriteMethods existingGraph) {
        CopyGraphUtilities.copyGraphToGraph(getReadableGraph(), existingGraph, true);
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("You cannot compare graphs.");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.readableGraph);
        hash = 79 * hash + Objects.hashCode(this.writableGraph);
        hash = 79 * hash + Objects.hashCode(this.deferredRemoval);
        return hash;
    }
}
