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

/**
 *
 * @author twilight_sparkle
 */
public class ConnectionBuilder extends GraphBuilder {

    private static final ConnectionDirection DIRECTION_DEFAULT = ConnectionDirection.UNDIRECTED;

    public enum ConnectionDirection {

        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        UNDIRECTED;
    }

    public static ConnectionBuilder makeConnection(final GraphWriteMethods graph, final int[] leftToConnect, final int[] rightToConnect) {
        return makeConnection(graph, leftToConnect, rightToConnect, DIRECTION_DEFAULT);
    }

    public static ConnectionBuilder makeConnection(final GraphWriteMethods graph, final int[] leftToConnect, final int[] rightToConnect, final ConnectionDirection direction) {
        // Create the new transactions connecting specified vertices in g1 to specified vertices in g2.
        final int[] connectingTransactions = new int[leftToConnect.length * rightToConnect.length];
        int currentTrans = 0;
        final boolean directed = direction != ConnectionDirection.UNDIRECTED;
        final boolean reverse = direction == ConnectionDirection.RIGHT_TO_LEFT;
        for (final int leftID : leftToConnect) {
            for (final int rightID : rightToConnect) {
                connectingTransactions[currentTrans++] = constructTransaction(graph, leftID, rightID, directed, reverse);
            }
        }

        // Return the ConnectionBuilder associated with the transformations to g1.
        return new ConnectionBuilder(graph, connectingTransactions);

    }

    public final int[] connectingTransactions;

    private ConnectionBuilder(final GraphWriteMethods graph, final int[] connectingTransactions) {
        super(graph);
        this.connectingTransactions = connectingTransactions;
    }
}
