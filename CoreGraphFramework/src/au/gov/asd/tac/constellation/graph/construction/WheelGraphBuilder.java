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
public class WheelGraphBuilder extends GraphBuilder {

    private static final int SPOKES_DEFAULT = 5;
    private static final StarGraphBuilder.StarDirection STAR_DIRECTION_DEFAULT = StarGraphBuilder.StarDirection.UNDIRECTED;
    private static final boolean DIRECTED_DEFAULT = false;

    public static WheelGraphBuilder addWheel(final GraphWriteMethods graph) {
        return addWheel(graph, SPOKES_DEFAULT, STAR_DIRECTION_DEFAULT, DIRECTED_DEFAULT);
    }

    public static WheelGraphBuilder addWheel(final int numSpokes, final StarGraphBuilder.StarDirection starDirection, final boolean directed) {
        return addWheel(new StoreGraph(), numSpokes, starDirection, directed);
    }

    public static WheelGraphBuilder addWheel(final GraphWriteMethods graph, final int numSpokes, final StarGraphBuilder.StarDirection starDirection, final boolean directed) {

        final StarGraphBuilder star = StarGraphBuilder.addStar(graph, numSpokes, starDirection);
        final int[] circumferenceTransactions = new int[numSpokes];

        for (int i = 0; i < numSpokes; i++) {
            circumferenceTransactions[i] = constructTransaction(graph, star.pendants[i], star.pendants[(i + 1) % numSpokes], directed);
        }

        return new WheelGraphBuilder(graph, star, circumferenceTransactions);
    }

    public final int[] nodes;
    public final int[] transactions;

    public final int centre;
    public final int[] spokes;
    public final int[] circumferenceNodes;
    public final int[] circumferenceTransactions;

    private WheelGraphBuilder(final GraphWriteMethods graph, final StarGraphBuilder star, final int[] circumferenceTransactions) {
        super(graph);
        this.centre = star.centre;
        this.spokes = star.transactions;
        this.circumferenceNodes = star.pendants;
        this.circumferenceTransactions = circumferenceTransactions;

        nodes = new int[circumferenceNodes.length + 1];
        nodes[0] = centre;
        System.arraycopy(circumferenceNodes, 0, nodes, 1, circumferenceNodes.length);

        transactions = new int[circumferenceTransactions.length + spokes.length];
        System.arraycopy(circumferenceTransactions, 0, transactions, 0, circumferenceTransactions.length);
        System.arraycopy(spokes, 0, transactions, circumferenceTransactions.length, spokes.length);
    }

}
