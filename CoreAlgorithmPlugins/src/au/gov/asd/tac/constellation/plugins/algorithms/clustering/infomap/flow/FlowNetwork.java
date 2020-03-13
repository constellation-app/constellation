/*
 * Copyright 2010-2020 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Logf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Flow Network
 *
 * @author algol
 */
public class FlowNetwork {

    private int[] nodeOutDegree;
    private double[] sumLinkOutWeight; // Per leaf nodes

    private double[] nodeFlow;
    private double[] nodeTeleportRates;
    private Connection[] flowConns;

    public void calculateFlow(final Network network, final Config config) {
        Logf.printf("Calculating global flow... ");

        // Prepare data in sequence containers for fast access of individual elements.
        final int numNodes = network.getNumNodes();
        nodeOutDegree = new int[numNodes];
        sumLinkOutWeight = new double[numNodes];
        nodeFlow = new double[numNodes];
        nodeTeleportRates = new double[numNodes];

        final TreeMap<NodePair, Double> connMap = network.getMap();
        final int numConns = connMap.size();
        flowConns = new Connection[numConns];
        double totalConnWeight = network.getTotalWeight();
        double sumUndirConnWeight = (config.isUndirected() ? 1 : 2) * totalConnWeight;

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
            if (!config.outdirdir) {
                nodeFlow[ends.end2] += weight / sumUndirConnWeight;
            }
            flowConns[connIndex] = new Connection(ends.end1, ends.end2, weight);

            connIndex++;
        }

        if (config.rawdir) {
            // Treat the link weights as flow (after global normalization) and
            // do one power iteration to set the node flow.
            Arrays.fill(nodeFlow, 0);
            for (final Connection conn : flowConns) {
                conn.flow /= totalConnWeight;
                nodeFlow[conn.target] += conn.flow;
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

            System.out.printf("using directed links with raw flow... done!\n");
            System.out.printf("Total link weight: %f\n", totalConnWeight);

            return;
        }

        if (!config.directed) {
            if (config.undirdir || config.outdirdir) {
                //Take one last power iteration.
                final double[] nodeFlowSteadyState = Arrays.copyOf(nodeFlow, numNodes);
                Arrays.fill(nodeFlow, 0);
                for (final Connection conn : flowConns) {
                    nodeFlow[conn.target] += nodeFlowSteadyState[conn.source] * conn.flow / sumLinkOutWeight[conn.source];
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
                }

                // Update link data to represent flow instead of weight.
                if (sumNodeRank != 0) {
                    for (final Connection conn : flowConns) {
                        conn.flow *= nodeFlowSteadyState[conn.source] / sumLinkOutWeight[conn.source] / sumNodeRank;
                    }
                }
            } else { // undirected
                for (int i = 0; i < numConns; ++i) {
                    flowConns[i].flow /= sumUndirConnWeight;
                }
            }

            if (config.outdirdir) {
                Logf.printf("counting only ingoing links... done!\n");
            } else {
                Logf.printf("using undirected links%s\n", config.undirdir ? ", switching to directed after steady state... done!"
                        : "... done!");
            }

            return;
        }

        System.out.printf("using %s teleportation to %s... ", config.recordedTeleportation ? "recorded" : "unrecorded",
                config.teleportToNodes ? "nodes" : "links");

        // Calculate the teleport rate distribution.
        if (config.teleportToNodes) {
            final double[] nodeWeights = network.getNodeTeleportRates();
            for (int i = 0; i < numNodes; ++i) {
                nodeTeleportRates[i] = nodeWeights[i] / network.getSumNodeWeights();
            }
        } else {
            // Teleport proportionally to out-degree, or in-degree if recorded teleportation.
            for (final Connection conn : flowConns) {
                final int toNode = config.recordedTeleportation ? conn.target : conn.source;
                nodeTeleportRates[toNode] += conn.flow / totalConnWeight;
            }
        }

        // Normalize link weights with respect to its source nodes total out-link weight.
        for (final Connection conn : flowConns) {
            conn.flow /= sumLinkOutWeight[conn.source];
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
        double alpha = config.teleportationProbability;
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
                nodeFlowTmp[conn.target] += beta * conn.flow * nodeFlow[conn.source];
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
            if (sum != 0) {
                if (Math.abs(sum - 1.0) > 1.0e-10) {
                    System.out.printf("(Normalizing ranks after %d power iterations with error) ", numIterations, sum - 1.0);
                    for (int i = 0; i < numNodes; ++i) {
                        nodeFlow[i] /= sum;
                    }
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

        if (!config.recordedTeleportation) {
            //Take one last power iteration excluding the teleportation (and normalize node flow to sum 1.0).
            sumNodeRank = 1.0 - danglingRank;
            Arrays.fill(nodeFlow, 0);
            for (final Connection conn : flowConns) {
                nodeFlow[conn.target] += conn.flow * nodeFlowTmp[conn.source] / sumNodeRank;
            }

            beta = 1.0;
        }

        // Update the links with their global flow from the PageRank values. (Note: beta is set to 1 if unrec).
        for (final Connection conn : flowConns) {
            conn.flow *= beta * nodeFlowTmp[conn.source] / sumNodeRank;
        }

        System.out.printf("done in %d iterations!\n", numIterations);
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
