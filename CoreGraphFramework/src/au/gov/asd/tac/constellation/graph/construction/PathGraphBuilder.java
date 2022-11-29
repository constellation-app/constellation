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
package au.gov.asd.tac.constellation.graph.construction;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;

/**
 *
 * @author twilight_sparkle
 */
public class PathGraphBuilder extends GraphBuilder {

    private static final int LENGTH_DEFAULT = 5;
    private static final boolean DIRECTED_DEFAULT = false;

    public static PathGraphBuilder addPath(final GraphWriteMethods graph) {
        return addPath(graph, LENGTH_DEFAULT, DIRECTED_DEFAULT);
    }

    public static PathGraphBuilder addPath(final int length, final boolean directed) {
        return addPath(new StoreGraph(), length, directed);
    }

    public static PathGraphBuilder addPath(final GraphWriteMethods graph, final int length, final boolean directed) {
        final int[] nodes = new int[length];
        final int[] transactions = new int[length - 1];

        final int start = constructVertex(graph);
        nodes[0] = start;
        for (int i = 1; i < length; i++) {
            nodes[i] = constructVertex(graph);
            transactions[i - 1] = constructTransaction(graph, nodes[i - 1], nodes[i], directed);
        }
        final int end = nodes[length - 1];

        return new PathGraphBuilder(graph, start, end, nodes, transactions);
    }

    public final int start;
    public final int end;
    public final int[] nodes;
    public final int[] transactions;

    private PathGraphBuilder(final GraphWriteMethods graph, final int start, final int end, final int[] nodes, final int[] transactions) {
        super(graph);
        this.start = start;
        this.end = end;
        this.nodes = nodes;
        this.transactions = transactions;
    }

}
