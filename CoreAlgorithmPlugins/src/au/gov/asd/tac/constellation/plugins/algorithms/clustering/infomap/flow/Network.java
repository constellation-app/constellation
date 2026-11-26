/*
 * Copyright 2010-2026 Australian Signals Directorate
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;

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

    private final Map<IntIntPair, Double> connectionMap;

    private final int vxNameId;

    private final double[] nodeWeights;
    private double totalWeight;

    public Network(final Config config, final GraphReadMethods rg) {
        this.config = config;
        this.rg = rg;

        vxNameId = rg.getAttribute(GraphElementType.VERTEX, "Name");

        connectionMap = new TreeMap<>();

        // We assume that all vertices have weight 1.
        nodeWeights = new double[rg.getVertexCount()];
        Arrays.fill(nodeWeights, 1);

        graphConnections = () -> {
            switch (config.getConnectionType()) {
                case LINKS -> {
                    return new LinkIterator(rg);
                }
                case EDGES -> {
                    return new EdgeIterator(rg);
                }
                case TRANSACTIONS -> {
                    return new TransactionIterator(rg);
                }
                case null, default -> throw new IllegalStateException(String.format("Unexpected connection type %s", config.getConnectionType()));
            }
        };
    }

    public void read() {
        connectionMap.clear();

        int numDoubleLinks = 0;
        totalWeight = 0;

        // Iterate over connections (transactions, edges, or links, depending on the config).
        // Note that connection ends are StoreGraph positions, not vertex ids.
        // This gives us a nice 0..n-1 numbering which the algorithm pretty much relies on.
        // Don't forget to convert back when looking at the results.
        for (final Connection conn : graphConnections) {
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
            final IntIntPair nodePair = PrimitiveTuples.pair(conn.getSource(), conn.getTarget());
            final Double d = connectionMap.get(nodePair);
            if (d == null) {
                connectionMap.put(nodePair, conn.getWeight());
            } else {
                connectionMap.put(nodePair, d + conn.getWeight());
                numDoubleLinks++;
            }
        }

        final String formattedString = String.format("done! Found %d nodes and %d connections. ", rg.getVertexCount(), connectionMap.size());
        LOGGER.log(Level.INFO, formattedString);
        if (numDoubleLinks > 0) {
            LOGGER.log(Level.INFO, "{0} connections was aggregated to existing connections.", numDoubleLinks);
        }
    }

    public int getNumNodes() {
        return rg.getVertexCount();
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public Map<IntIntPair, Double> getMap() {
        return Collections.unmodifiableMap(connectionMap);
    }
    
    public String getNodeName(final int position) {
        return vxNameId == Graph.NOT_FOUND ? "" 
                : String.format("[Node position=%d, vxId=%d]", position, rg.getVertex(position));
    }
}
