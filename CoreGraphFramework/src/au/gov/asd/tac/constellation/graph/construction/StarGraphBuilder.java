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
package au.gov.asd.tac.constellation.graph.construction;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;

/**
 *
 * @author twilight_sparkle
 */
public class StarGraphBuilder extends GraphBuilder {

    private static final int PENDANTS_DEFAULT = 5;
    private static final StarDirection DIRECTION_DEFAULT = StarDirection.UNDIRECTED;

    public enum StarDirection {

        CENTRE_IS_SOURCE,
        CENTRE_IS_SINK,
        UNDIRECTED;
    }

    public static StarGraphBuilder addStar(final GraphWriteMethods graph) {
        return addStar(graph, PENDANTS_DEFAULT, DIRECTION_DEFAULT);
    }

    public static StarGraphBuilder addStar(final int numPendants, final StarDirection direction) {
        return addStar(new StoreGraph(), PENDANTS_DEFAULT, DIRECTION_DEFAULT);
    }

    public static StarGraphBuilder addStar(final GraphWriteMethods graph, final int numPendants, final StarDirection direction) {
        final int centre = constructVertex(graph);
        final int[] pendants = new int[numPendants];
        final int[] rays = new int[numPendants];
        final boolean directed = direction != StarDirection.UNDIRECTED;
        final boolean reverse = direction == StarDirection.CENTRE_IS_SINK;
        for (int i = 0; i < numPendants; i++) {
            pendants[i] = constructVertex(graph);
            rays[i] = constructTransaction(graph, centre, pendants[i], directed, reverse);
        }
        return new StarGraphBuilder(graph, centre, pendants, rays);
    }

    public final int[] nodes;
    public final int[] transactions;
    public final int centre;
    public final int[] pendants;

    private StarGraphBuilder(final GraphWriteMethods graph, final int centre, final int[] pendants, final int[] rays) {
        super(graph);
        this.centre = centre;
        this.pendants = pendants;
        nodes = new int[pendants.length + 1];
        nodes[0] = centre;
        System.arraycopy(pendants, 0, nodes, 1, pendants.length);
        this.transactions = rays;
    }
}
