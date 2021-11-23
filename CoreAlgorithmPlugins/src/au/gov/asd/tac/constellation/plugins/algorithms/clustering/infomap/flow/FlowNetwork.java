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

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Flow Network
 *
 * @author algol
 */
public class FlowNetwork {

    private static final Logger LOGGER = Logger.getLogger(FlowNetwork.class.getName());

    private int[] nodeOutDegree;
    private double[] sumLinkOutWeight; // Per leaf nodes

    private double[] nodeFlow;
    private double[] nodeTeleportRates;
    private Connection[] flowConns;

    public void calculateFlow(final Network network, final Config config) {
        LOGGER.log(Level.INFO, "Calculating global flow... ");

        // Prepare data in sequence containers for fast access of individual elements.
        final int numNodes = network.getNumNodes();
        nodeOutDegree = new int[numNodes];
        sumLinkOutWeight = new double[numNodes];
        nodeFlow = new double[numNodes];
        nodeTeleportRates = new double[numNodes];

        final TreeMap<NodePair, Double> connMap = network.getMap();
        final int numConns = connMap.size();
        flowConns = new Connection[numConns];
        final double totalConnWeight = network.getTotalWeight();
        final double sumUndirConnWeight = (config.isUndirected() ? 1 : 2) * totalConnWeight;

        int connIndex = 0;
        for (final Map.Entry<NodePair, Double> entry : connMap.entrySet()) {
            final NodePair ends = entry.getKey();
            nodeOutDegree[ends.end1]++;
            final double weight = entry.getValue();
            sumLinkOutWeight[ends.end1] += weight;
            if (config.isUndirected()) {
                sumLinkOutWeight[ends.end2] += weight;
            }
            nodeFlow[ends.end1] += weight / sumUndirConnWeight;
            if (!config.isOutdirdir()) {
                nodeFlow[ends.end2] += weight / sumUndirConnWeight;
            }
            flowConns[connIndex] = new Connection(ends.end1, ends.end2, weight);

            connIndex++;
        }

        if (config.isRawdir()) {
            // Treat the link weights as flow (after global normalization) and
            // do one power iteration to set the node flow.
            Arrays.fill(nodeFlow, 0);
            for (final Connection conn : flowConns) {
                conn.setFlow(conn.getFlow() / totalConnWeight);
                nodeFlow[conn.getTarget()] += conn.getFlow();
            }

            // Normalize node flow.
            double sumNodeRank = 0;
            for (int i = 0; i < numNodes; ++i) {
                sumNodeRank += nodeFlow[i];
            }

            if (sumNodeRank != 0) {
                for (int i = 0; i < numNodes; ++i) {
                    nodeFlow[i] /= sumNodeRank;
                }
            }

            LOGGER.log(Level.INFO, "using directed links with raw flow... done!");
            LOGGER.log(Level.INFO, "Total link weight: {0}", totalConnWeight);

            return;
        }

        if (!config.isDirected()) {
            if (config.isUndirdir() || config.isOutdirdir()) {
                //Take one last power iteration.
                final double[] nodeFlowSteadyState = Arrays.copyOf(nodeFlow, numNodes);
                Arrays.fill(nodeFlow, 0);
                for (final Connection conn : flowConns) {
                    nodeFlow[conn.getTarget()] += nodeFlowSteadyState[conn.getSource()] * conn.getFlow() / sumLinkOutWeight[conn.getSource()];
                }

                //Normalize node flow.
                double sumNodeRank = 0.0;
                for (int i = 0; i < numNodes; ++i) {
                    sumNodeRank += nodeFlow[i];
                }

                if (sumNodeRank != 0) {
                    for (int i = 0; i < numNodes; ++i) {
                        nodeFlow[i] /= sumNodeRank;
                    }

                    // Update link data to represent flow instead of weight.
                    for (final Connection conn : flowConns) {
                        conn.setFlow(conn.getFlow() * (nodeFlowSteadyState[conn.getSource()] / sumLinkOutWeight[conn.getSource()] / sumNodeRank));
                    }
                }

            } else { // undirected
                for (int i = 0; i < numConns; ++i) {
                    flowConns[i].setFlow(flowConns[i].getFlow() / sumUndirConnWeight);
                }
            }

            if (config.isOutdirdir()) {
                LOGGER.log(Level.INFO, "counting only ingoing links... done!");
            } else {
                LOGGER.log(Level.INFO, "using undirected links%s\n", config.isUndirdir() ? ", switching to directed after steady state... done!"
                        : "... done!");
            }

            return;
        }

        LOGGER.log(Level.INFO, String.format("using %s teleportation to %s... ", config.isRecordedTeleportation() ? "recorded" : "unrecorded",
                config.isTeleportToNodes() ? "nodes" : "links"));

        // Calculate the teleport rate distribution.
        if (config.isTeleportToNodes()) {
            final double[] nodeWeights = network.getNodeTeleportRates();
            for (int i = 0; i < numNodes; ++i) {
                nodeTeleportRates[i] = nodeWeights[i] / network.getSumNodeWeights();
            }
        } else {
            // Teleport proportionally to out-degree, or in-degree if recorded teleportation.
            for (final Connection conn : flowConns) {
                final int toNode = config.isRecordedTeleportation() ? conn.getTarget() : conn.getSource();
                nodeTeleportRates[toNode] += conn.getFlow() / totalConnWeight;
            }
        }

        // Normalize link weights with respect to its source nodes total out-link weight.
        for (final Connection conn : flowConns) {
            conn.setFlow(conn.getFlow() / sumLinkOutWeight[conn.getSource()]);
        }

        // Collect dangling nodes.
        final ArrayList<Integer> danglings = new ArrayList<>();
        for (int i = 0; i < numNodes; ++i) {
            if (nodeOutDegree[i] == 0) {
                danglings.add(i);
            }
        }

        // Calculate PageRank.
        final double[] nodeFlowTmp = new double[numNodes];
        Arrays.fill(nodeFlowTmp, 0);
        int numIterations = 0;
        double alpha = config.getTeleportationProbability();
        double beta = 1.0 - alpha;
        double sqdiff = 1;
        double danglingRank = 0;
        do {
            // Calculate dangling rank.
            danglingRank = 0;
            for (final int dangling : danglings) {
                danglingRank += nodeFlow[dangling];
            }

            // Flow from teleportation.
            for (int i = 0; i < numNodes; ++i) {
                nodeFlowTmp[i] = (alpha + beta * danglingRank) * nodeTeleportRates[i];
            }

            // Flow from links.
            for (final Connection conn : flowConns) {
                nodeFlowTmp[conn.getTarget()] += beta * conn.getFlow() * nodeFlow[conn.getSource()];
            }

            // Update node flow from the power iteration above and check if converged.
            double sum = 0.0;
            double sqdiff_old = sqdiff;
            sqdiff = 0.0;
            for (int i = 0; i < numNodes; ++i) {
                sum += nodeFlowTmp[i];
                sqdiff += Math.abs(nodeFlowTmp[i] - nodeFlow[i]);
                nodeFlow[i] = nodeFlowTmp[i];
            }

            // Normalize if needed.
            final double adjustedSum =  sum - 1.0;
            if (sum != 0 && Math.abs(adjustedSum) > 1.0e-10) {
                final String logMsg = String.format("Normalizing ranks after %d power iterations with error %e ", numIterations, adjustedSum);
                LOGGER.log(Level.INFO, logMsg);
                for (int i = 0; i < numNodes; ++i) {
                    nodeFlow[i] /= sum;
                }
            }

            // Perturb the system if equilibrium.
            if (sqdiff == sqdiff_old) {
                alpha += 1.0e-10;
                beta = 1.0 - alpha;
            }

            numIterations++;
        } while ((numIterations < 200) && (sqdiff > 1.0e-15 || numIterations < 50));

        double sumNodeRank = 1.0;

        if (!config.isRecordedTeleportation()) {
            //Take one last power iteration excluding the teleportation (and normalize node flow to sum 1.0).
            sumNodeRank = 1.0 - danglingRank;
            Arrays.fill(nodeFlow, 0);
            for (final Connection conn : flowConns) {
                nodeFlow[conn.getTarget()] += conn.getFlow() * nodeFlowTmp[conn.getSource()] / sumNodeRank;
            }

            beta = 1.0;
        }

        // Update the links with their global flow from the PageRank values. (Note: beta is set to 1 if unrec).
        for (final Connection conn : flowConns) {
            conn.setFlow(conn.getFlow() * (beta * nodeFlowTmp[conn.getSource()] / sumNodeRank));
        }

        LOGGER.log(Level.INFO, "done in {0} iterations!", numIterations);
    }

    public double[] getNodeFlow() {
        return nodeFlow;
    }

    public double[] getNodeTeleportRates() {
        return nodeTeleportRates;
    }

    public Connection[] getFlowConnections() {
        return flowConns;
    }
}
