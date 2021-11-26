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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config.ConnectionType;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parse graph connections into a map ordered by (end1, end2).
 *
 * @author algol
 */
public class Network {

    private static final Logger LOGGER = Logger.getLogger(Network.class.getName());

    private final Config config;
    private final GraphReadMethods rg;
    private final Iterable<Connection> graphConnections;

    private final TreeMap<NodePair, Double> connectionMap;

    private final int vxNameId;

    private final double[] nodeWeights;
    private final double sumNodeWeights;
    private double totalWeight;

    public Network(final Config config, final GraphReadMethods rg) {
        this.config = config;
        this.rg = rg;

        vxNameId = rg.getAttribute(GraphElementType.VERTEX, "Name");

        connectionMap = new TreeMap<>();

        // We assume that all vertices have weight 1.
        nodeWeights = new double[rg.getVertexCount()];
        Arrays.fill(nodeWeights, 1);
        sumNodeWeights = rg.getVertexCount();

        graphConnections = () -> {
            if (config.getConnectionType() == ConnectionType.LINKS) {
                return new LinkIterator(rg);
            } else if (config.getConnectionType() == ConnectionType.EDGES) {
                return new EdgeIterator(rg);
            } else if (config.getConnectionType() == ConnectionType.TRANSACTIONS) {
                return new TransactionIterator(rg);
            } else {
                throw new IllegalStateException(String.format("Unexpected connection type %s", config.getConnectionType()));
            }
        };
    }

    public void read() {
        connectionMap.clear();
        int numSelfLinks = 0;

        int numDoubleLinks = 0;
        totalWeight = 0;

        // Iterate over connections (transactions, edges, or links, depending on the config).
        // Note that connection ends are StoreGraph positions, not vertex ids.
        // This gives us a nice 0..n-1 numbering which the algorithm pretty much relies on.
        // Don't forget to convert back when looking at the results.
        for (final Connection conn : graphConnections) {
            if (conn.getTarget() == conn.getSource()) {
                numSelfLinks++;
                if (!config.isIncludeSelfLinks()) {
                    continue;
                }
            }

            // If undirected links, aggregate weight rather than adding an opposite link.
            if (config.isUndirected() && conn.getTarget() < conn.getSource()) {
                final int tmp = conn.getSource();
                conn.setSource(conn.getTarget());
                conn.setTarget(tmp);
            }

            totalWeight += conn.getWeight();
            if (config.isUndirected()) {
                totalWeight += conn.getWeight();
            }

            // Aggregate link weights if they are defined more than once.
            final NodePair nodePair = new NodePair(conn.getSource(), conn.getTarget());
            final Double d = connectionMap.get(nodePair);
            if (d == null) {
                connectionMap.put(nodePair, conn.getWeight());
            } else {
                connectionMap.put(nodePair, d + conn.getWeight());
                numDoubleLinks++;
                if (conn.getTarget() == conn.getSource()) {
                    numSelfLinks--;
                }
            }
        }

        final String formattedString = String.format("done! Found %d nodes and %d connections. ", rg.getVertexCount(), connectionMap.size());
        LOGGER.log(Level.INFO, formattedString);
        if (numDoubleLinks > 0) {
            LOGGER.log(Level.INFO, "{0} connections was aggregated to existing connections.", numDoubleLinks);
        }
        if (numSelfLinks > 0 && !config.isIncludeSelfLinks()) {
            LOGGER.log(Level.INFO, "self-connections was ignored.");
        }
    }

    public int getNumNodes() {
        return rg.getVertexCount();
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public TreeMap<NodePair, Double> getMap() {
        return connectionMap;
    }

    public double[] getNodeTeleportRates() {
        return nodeWeights;
    }

    public double getSumNodeWeights() {
        return sumNodeWeights;
    }

    public String getNodeName(final int position) {

        if (vxNameId == Graph.NOT_FOUND) {
            return "";
        }

        final int vxId = rg.getVertex(position);
        return String.format("[Node position=%d, vxId=%d]", position, vxId);
    }
}
