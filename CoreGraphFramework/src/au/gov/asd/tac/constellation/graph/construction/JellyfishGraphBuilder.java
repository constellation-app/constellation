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
public class JellyfishGraphBuilder extends GraphBuilder {

    private static final int[] TENTACLE_LENGTHS_DEFAULT = {2, 2, 2, 2, 2};
    private static final TentacleDirection TENTACLE_DIRECTION_DEFAULT = TentacleDirection.UNDIRECTED;

    public enum TentacleDirection {

        AWAY_FROM_CENTRE(StarGraphBuilder.StarDirection.CENTRE_IS_SOURCE),
        TOWARD_CENTRE(StarGraphBuilder.StarDirection.CENTRE_IS_SINK),
        UNDIRECTED(StarGraphBuilder.StarDirection.UNDIRECTED);

        private final StarGraphBuilder.StarDirection starDirection;

        TentacleDirection(final StarGraphBuilder.StarDirection starDirection) {
            this.starDirection = starDirection;
        }
    }

    public static JellyfishGraphBuilder addJellyfish(final GraphWriteMethods graph) {
        return addJellyfish(graph, TENTACLE_LENGTHS_DEFAULT, TENTACLE_DIRECTION_DEFAULT);
    }

    public static JellyfishGraphBuilder addJellyfish(final int numTentacles, final int tentacleLength, final TentacleDirection direction) {
        return addJellyfish(new StoreGraph(), numTentacles, tentacleLength, direction);
    }

    public static JellyfishGraphBuilder addJellyfish(final GraphWriteMethods graph, final int numTentacles, final int tentacleLength, final TentacleDirection direction) {
        final int[] tentacleLengths = new int[numTentacles];
        for (int i = 0; i < numTentacles; i++) {
            tentacleLengths[i] = tentacleLength;
        }
        return addJellyfish(graph, tentacleLengths, direction);
    }

    public static JellyfishGraphBuilder addJellyfish(final int[] tentacleLengths, final TentacleDirection direction) {
        return addJellyfish(new StoreGraph(), tentacleLengths, direction);
    }

    public static JellyfishGraphBuilder addJellyfish(final GraphWriteMethods graph, final int[] tentacleLengths, final TentacleDirection direction) {

        final int numTentacles = tentacleLengths.length;

        final StarGraphBuilder star = StarGraphBuilder.addStar(graph, numTentacles, direction.starDirection);

        final int[][] tentacleNodes = new int[numTentacles][];
        final int[][] tentacleTransactions = new int[numTentacles][];
        final int[] pendants = new int[numTentacles];
        final boolean directed = direction != TentacleDirection.UNDIRECTED;
        final boolean reverse = direction == TentacleDirection.TOWARD_CENTRE;
        for (int i = 0; i < numTentacles; i++) {
            tentacleNodes[i] = new int[tentacleLengths[i]];
            tentacleTransactions[i] = new int[tentacleLengths[i]];
            tentacleNodes[i][0] = star.pendants[i];
            tentacleTransactions[i][0] = graph.getVertexTransaction(star.pendants[i], 0);
            for (int j = 1; j < tentacleLengths[i]; j++) {
                tentacleNodes[i][j] = constructVertex(graph);
                tentacleTransactions[i][j] = constructTransaction(graph, tentacleNodes[i][j - 1], tentacleNodes[i][j], directed, reverse);
            }
            pendants[i] = tentacleNodes[i][tentacleLengths[i] - 1];
        }

        return new JellyfishGraphBuilder(graph, star, pendants, tentacleNodes, tentacleTransactions);
    }

    public final int[] nodes;
    public final int[] transactions;
    public final int centre;
    public final int[] pendants;
    public final int[][] tentacleNodes;
    public final int[][] tentacleTransactions;

    private JellyfishGraphBuilder(final GraphWriteMethods graph, final StarGraphBuilder star, final int[] pendants, final int[][] tentacleNodes, int[][] tentacleTransactions) {
        super(graph);
        this.centre = star.centre;
        this.pendants = pendants;
        this.tentacleNodes = tentacleNodes;
        final int[] squashedTentacleNodes = squashGrouping(tentacleNodes);
        nodes = new int[squashedTentacleNodes.length + 1];
        nodes[0] = centre;
        System.arraycopy(squashedTentacleNodes, 0, nodes, 1, squashedTentacleNodes.length);
        this.tentacleTransactions = tentacleTransactions;
        transactions = squashGrouping(tentacleTransactions);
    }

}
