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
public class GraphBuilder {

    public final GraphWriteMethods graph;

    public static GraphBuilder buildGraph() {
        return new GraphBuilder(new StoreGraph());
    }

    protected static int constructVertex(GraphWriteMethods graph) {
        int vxId = graph.addVertex();
        if (graph.getSchema() != null) {
            graph.getSchema().newVertex(graph, vxId);
        }
        return vxId;
    }

    protected static int constructTransaction(GraphWriteMethods graph, int notionalSourceId, int noitonalDestId, boolean directed) {
        return constructTransaction(graph, notionalSourceId, noitonalDestId, directed, false);
    }

    protected static int constructTransaction(GraphWriteMethods graph, int notionalSourceId, int noitonalDestId, boolean directed, boolean reverse) {
        final int sourceId = reverse ? noitonalDestId : notionalSourceId;
        final int destId = reverse ? notionalSourceId : noitonalDestId;
        int txId = graph.addTransaction(sourceId, destId, directed);
        if (graph.getSchema() != null) {
            graph.getSchema().newTransaction(graph, txId);
        }
        return txId;
    }

    protected static int[] squashGrouping(int[][][] elementGroupings) {
        final int[][] elements = new int[elementGroupings.length][];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = squashGrouping(elementGroupings[i]);
        }
        return squashGrouping(elements);
    }

    protected static int[] squashGrouping(int[][] elementGroupings) {
        final int[] elements;
        int totalVerts = 0;
        for (int[] group : elementGroupings) {
            totalVerts += group.length;
        }
        elements = new int[totalVerts];
        int currentPos = 0;
        for (int[] group : elementGroupings) {
            System.arraycopy(group, 0, elements, currentPos, group.length);
            currentPos += group.length;
        }
        return elements;
    }

    protected GraphBuilder(GraphWriteMethods graph) {
        this.graph = graph;
    }

}
