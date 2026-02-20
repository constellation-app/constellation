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
package au.gov.asd.tac.constellation.graph.construction;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import java.util.Arrays;

/**
 *
 * @author twilight_sparkle
 */
public class CycleGraphBuilder extends GraphBuilder {

    private static final int LENGTH_DEFAULT = 5;
    private static final boolean DIRECTED_DEFAULT = false;

    public static CycleGraphBuilder addCycle(final GraphWriteMethods graph) {
        return addCycle(graph, LENGTH_DEFAULT, DIRECTED_DEFAULT);
    }

    public static CycleGraphBuilder addCycle(final int length, final boolean directed) {
        return addCycle(new StoreGraph(), length, directed);
    }

    public static CycleGraphBuilder addCycle(final GraphWriteMethods graph, final int length, final boolean directed) {
        final PathGraphBuilder path = PathGraphBuilder.addPath(graph, length, directed);
        final int[] transactions = Arrays.copyOf(path.transactions, length);
        transactions[length - 1] = constructTransaction(graph, path.end, path.start, directed);

        return new CycleGraphBuilder(graph, path, transactions);
    }

    public final int[] nodes;
    public final int[] transactions;

    private CycleGraphBuilder(final GraphWriteMethods graph, final PathGraphBuilder path, final int[] transactions) {
        super(graph);
        this.nodes = path.nodes;
        this.transactions = transactions;
    }
}
