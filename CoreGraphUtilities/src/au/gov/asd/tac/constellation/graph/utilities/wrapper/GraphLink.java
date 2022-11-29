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
package au.gov.asd.tac.constellation.graph.utilities.wrapper;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author capella
 */
public class GraphLink extends GraphElement {

    public GraphLink(GraphWrapper graph, int id) {
        super(GraphElementType.LINK, graph, id);
    }

    public Stream<GraphTransaction> streamTransactions() {
        return IntStream.range(0, graph.getReadableGraph().getLinkTransactionCount(id))
                .mapToObj(i -> new GraphTransaction(graph, graph.getReadableGraph().getLinkTransaction(id, i)));
    }

    public Stream<GraphVertex> streamVertices() {
        return streamTransactions().findFirst().get().streamVertices();
    }

    @Override
    public void unsafeImmediateRemove() {
        throw new UnsupportedOperationException("You cannot directly remove a link.");
    }

    @Override
    public void deferRemove() {
        streamTransactions().forEach(GraphTransaction::deferRemove);
    }
}
