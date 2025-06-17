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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.Edge;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.Node;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeFactoryBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.InfoMath;
import static au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.InfoMath.plogp;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.MultiMap;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Resizer;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author algol
 */
public abstract class InfomapGreedy extends InfomapBase {

    private static final Logger LOGGER = Logger.getLogger(InfomapGreedy.class.getName());

    protected FlowBase[] moduleFlowData;
    protected int[] moduleMembers;
    protected ArrayList<Integer> emptyModules;

    protected double nodeFlowLogNodeFlow; // Constant while the leaf network is the same.
    protected double flowLogFlow; // node.(flow + exitFlow)
    protected double exitLogExit;
    protected double enterLogEnter;
    protected double enterFlow;
    protected double enterFlowLogEnterFlow;

    // For hierarchical.
    protected double exitNetworkFlow;
    protected double exitNetworkFlowLogExitNetworkFlow;

    protected InfomapGreedy(final Config config, final NodeFactoryBase nodeFactory, final GraphReadMethods rg) {
        super(config, nodeFactory, rg);

        moduleFlowData = new FlowBase[0];
    }

    @Override
    public void initEnterExitFlow() {
        if (DEBUG) {
            LOGGER.log(Level.INFO, "initEnterExitFlow() {0}", getClass().getSimpleName());
        }

        for (final NodeBase node : treeData.getLeaves()) {
            for (final Edge<NodeBase> edge : node.getOutEdges()) {
                // Possible self-links should not add to enter and exit flow in its enclosing module.
                if (!edge.isSelfPointing()) {
                    // For undirected links, this automatically adds to both direction (as enterFlow = &exitFlow).
                    final double sourceExitFlow = getNode(edge.getSource()).getData().getExitFlow();
                    final double targetEnterFlow = getNode(edge.getTarget()).getData().getEnterFlow();
                    getNode(edge.getSource()).getData().setExitFlow(sourceExitFlow + edge.getData().flow);
                    getNode(edge.getTarget()).getData().setEnterFlow(targetEnterFlow + edge.getData().flow);
                }
            }
        }
    }

    @Override
    protected void initConstantInfomapTerms() {
        nodeFlowLogNodeFlow = 0;

        // For each module...
        for (final NodeBase nodeBase : activeNetwork) {
            final Node node = getNode(nodeBase);
            nodeFlowLogNodeFlow += plogp(node.getData().getFlow());
        }
    }

    @Override
    protected void initModuleOptimization() {
        final int numNodes = activeNetwork.size();
        moduleFlowData = Resizer.resizeFlowBase(moduleFlowData, numNodes, treeData.getNodeFactory());
        moduleMembers = new int[numNodes];
        Arrays.fill(moduleMembers, 1);
        emptyModules = new ArrayList<>(numNodes);

        int i = 0;
        for (final NodeBase nodeBase : activeNetwork) {
            final Node node = getNode(nodeBase);
            node.setIndex(i); // Unique module index for each node
            moduleFlowData[i] = node.getData().copy();
            i++;
        }

        // Initiate codelength terms for the initial state of one module per node.
        calculateCodelengthFromActiveNetwork(hasDetailedBalance());
    }

    protected void calculateCodelengthFromActiveNetwork(final boolean detailedBalance) {
        if (DEBUG) {
            final String log = String.format("%s.calculateCodelengthFromActiveNetwork(%s)%n", getClass().getSimpleName(), detailedBalance);
            LOGGER.log(Level.INFO, log);
        }

        flowLogFlow = 0;
        exitLogExit = 0;
        enterFlow = 0;

        if (detailedBalance) {
            // For each module...
            for (final NodeBase nodeBase : activeNetwork) {
                final Node node = getNode(nodeBase);

                // Own node/module codebook.
                flowLogFlow += plogp(node.getData().getFlow() + node.getData().getExitFlow());

                // Use of index codebook.
                enterFlow += node.getData().getExitFlow();
                exitLogExit += plogp(node.getData().getExitFlow());
            }

            enterFlow += exitNetworkFlow;
            enterFlowLogEnterFlow = plogp(enterFlow);

            indexCodelength = enterFlowLogEnterFlow - exitLogExit - exitNetworkFlowLogExitNetworkFlow;
            moduleCodelength = -exitLogExit + flowLogFlow - nodeFlowLogNodeFlow;
            codelength = indexCodelength + moduleCodelength;
        } else {
            enterLogEnter = 0;

            // For each module...
            for (final NodeBase nodeBase : activeNetwork) {
                final Node node = getNode(nodeBase);

                // Own node/module codebook.
                flowLogFlow += plogp(node.getData().getFlow() + node.getData().getExitFlow());

                // Use of index codebook.
                enterLogEnter += plogp(node.getData().getEnterFlow());
                exitLogExit += plogp(node.getData().getExitFlow());
                enterFlow += node.getData().getEnterFlow();
            }

            enterFlow += exitNetworkFlow;
            enterFlowLogEnterFlow = plogp(enterFlow);

            indexCodelength = enterFlowLogEnterFlow - enterLogEnter - exitNetworkFlowLogExitNetworkFlow;
            moduleCodelength = -exitLogExit + flowLogFlow - nodeFlowLogNodeFlow;
            codelength = indexCodelength + moduleCodelength;
        }
    }

    @Override
    protected int optimizeModules() {
        int numOptimizationRounds = 0;
        final double oldCodelength = codelength;
        int loopLimit = config.getCoreLoopLimit();
        if (config.getCoreLoopLimit() > 0 && config.isRandomizeCoreLoopLimit()) {
            loopLimit = (int) (rand.nextDouble() * config.getCoreLoopLimit()) + 1;
        }

        // Iterate while the optimization loop moves some nodes within the dynamic modular structure.
        do {
            tryMoveEachNodeIntoBestModule(); // returns numNodesMoved
            ++numOptimizationRounds;
        } while (numOptimizationRounds != loopLimit
                && codelength < oldCodelength - config.getMinimumCodelengthImprovement());

        return numOptimizationRounds;
    }

    protected void addTeleportationDeltaFlowOnOldModuleIfMove(final Node nodeToMove, final DeltaFlow oldModuleDeltaFlow) {
        // This method intentionally left blank.
        // Specialized implementation for InfomapDirected.
    }

    protected void addTeleportationDeltaFlowOnNewModuleIfMove(final Node nodeToMove, final DeltaFlow newModuleDeltaFlow) {
        // This method intentionally left blank.
        // Specialized implementation for InfomapDirected.
    }

    protected void addTeleportationDeltaFlowIfMove(final Node current, final DeltaFlow[] moduleDeltaExits, final int numModuleLinks) {
        // This method intentionally left blank.
        // Specialized implementation for InfomapDirected.
    }

    // --- Helper methods ---
    protected double getDeltaCodelength(final Node current, final DeltaFlow oldModuleDelta, final DeltaFlow newModuleDelta) {
        final int oldModule = oldModuleDelta.getModule();
        final int newModule = newModuleDelta.getModule();
        double deltaEnterExitOldModule = oldModuleDelta.getDeltaEnter() + oldModuleDelta.getDeltaExit();
        double deltaEnterExitNewModule = newModuleDelta.getDeltaEnter() + newModuleDelta.getDeltaExit();

        final double delta_enter = plogp(enterFlow + deltaEnterExitOldModule - deltaEnterExitNewModule) - enterFlowLogEnterFlow;

        final double delta_enter_log_enter
                = -plogp(moduleFlowData[oldModule].getEnterFlow())
                - plogp(moduleFlowData[newModule].getEnterFlow())
                + plogp(moduleFlowData[oldModule].getEnterFlow() - current.getData().getEnterFlow() + deltaEnterExitOldModule)
                + plogp(moduleFlowData[newModule].getEnterFlow() + current.getData().getEnterFlow() - deltaEnterExitNewModule);

        final double delta_exit_log_exit
                = -plogp(moduleFlowData[oldModule].getExitFlow())
                - plogp(moduleFlowData[newModule].getExitFlow())
                + plogp(moduleFlowData[oldModule].getExitFlow() - current.getData().getExitFlow() + deltaEnterExitOldModule)
                + plogp(moduleFlowData[newModule].getExitFlow() + current.getData().getExitFlow() - deltaEnterExitNewModule);

        final double delta_flow_log_flow
                = -plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow())
                - plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow())
                + plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow()
                        - current.getData().getExitFlow() - current.getData().getFlow() + deltaEnterExitOldModule)
                + plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow()
                        + current.getData().getExitFlow() + current.getData().getFlow() - deltaEnterExitNewModule);

        return delta_enter - delta_enter_log_enter - delta_exit_log_exit + delta_flow_log_flow;
    }

    /**
     * Update the codelength to reflect the move of node current in oldModuleDelta to newModuleDelta (Specialized for
     * undirected flow and when exitFlow == enterFlow
     *
     * @param current the current node.
     * @param oldModuleDelta the old module delta flow.
     * @param newModuleDelta the new module delta flow.
     */
    protected void updateCodelength(final Node current, final DeltaFlow oldModuleDelta, final DeltaFlow newModuleDelta) {
        final int oldModule = oldModuleDelta.getModule();
        final int newModule = newModuleDelta.getModule();
        final double deltaEnterExitOldModule = oldModuleDelta.getDeltaEnter() + oldModuleDelta.getDeltaExit();
        final double deltaEnterExitNewModule = newModuleDelta.getDeltaEnter() + newModuleDelta.getDeltaExit();

        enterFlow
                -= moduleFlowData[oldModule].getEnterFlow()
                + moduleFlowData[newModule].getEnterFlow();
        enterLogEnter
                -= plogp(moduleFlowData[oldModule].getEnterFlow())
                + plogp(moduleFlowData[newModule].getEnterFlow());
        exitLogExit
                -= plogp(moduleFlowData[oldModule].getExitFlow())
                + plogp(moduleFlowData[newModule].getExitFlow());
        flowLogFlow
                -= plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow())
                + plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow());

        moduleFlowData[oldModule].sub(current.getData());
        moduleFlowData[newModule].add(current.getData());

        moduleFlowData[oldModule].setEnterFlow(moduleFlowData[oldModule].getEnterFlow() + deltaEnterExitOldModule);
        moduleFlowData[oldModule].setExitFlow(moduleFlowData[oldModule].getExitFlow() + deltaEnterExitOldModule);
        moduleFlowData[newModule].setEnterFlow(moduleFlowData[newModule].getEnterFlow() - deltaEnterExitNewModule);
        moduleFlowData[newModule].setExitFlow(moduleFlowData[newModule].getExitFlow() - deltaEnterExitNewModule);

        enterFlow
                += moduleFlowData[oldModule].getEnterFlow()
                + moduleFlowData[newModule].getEnterFlow();
        enterLogEnter
                += plogp(moduleFlowData[oldModule].getEnterFlow())
                + plogp(moduleFlowData[newModule].getEnterFlow());
        exitLogExit
                += plogp(moduleFlowData[oldModule].getExitFlow())
                + plogp(moduleFlowData[newModule].getExitFlow());
        flowLogFlow
                += plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow())
                + plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow());

        enterFlowLogEnterFlow = plogp(enterFlow);

        indexCodelength = enterFlowLogEnterFlow - enterLogEnter - exitNetworkFlowLogExitNetworkFlow;
        moduleCodelength = -exitLogExit + flowLogFlow - nodeFlowLogNodeFlow;
        codelength = indexCodelength + moduleCodelength;
    }

    @Override
    protected void resetModuleFlowFromLeafNodes() {
        // Reset from top to bottom.
        resetModuleFlow(getRoot());

        // Aggregate from bottom to top.
        for (NodeBase node : treeData.getLeaves()) {
            final double flow = getNode(node).getData().getFlow();
            while (node.getParent() != null) {
                node = node.getParent();
                final double parentFlow = getNode(node).getData().getFlow();
                getNode(node).getData().setFlow(parentFlow + flow);
            }
        }
    }

    private void resetModuleFlow(final NodeBase node) {
        getNode(node).getData().setFlow(0);
        for (final NodeBase child : node.getChildren()) {
            if (!child.isLeaf()) {
                resetModuleFlow(child);
            }
        }
    }

    @Override
    protected double calcCodelengthFromFlowWithinOrExit(final NodeBase parent) {
        final FlowBase parentData = getNode(parent).getData();
        final double parentFlow = parentData.getFlow();
        final double parentExit = parentData.getExitFlow();
        final double totalParentFlow = parentFlow + parentExit;
        if (totalParentFlow < 1e-16) {
            return 0;
        }

        double indexLength = 0;

        // For each child...
        for (final NodeBase node : parent.getChildren()) {
            indexLength -= plogp(getNode(node).getData().getFlow() / totalParentFlow);
        }

        indexLength -= plogp(parentExit / totalParentFlow);

        indexLength *= totalParentFlow;

        return indexLength;
    }

    @Override
    protected void generateNetworkFromChildren(final NodeBase parent) {
        if (DEBUG) {
            LOGGER.log(Level.INFO, "{0}.generateNetworkFromChildren", getClass().getSimpleName());
        }

        exitNetworkFlow = 0;

        // Clone all nodes.
        int i = 0;
        for (final NodeBase child : parent.getChildren()) {
            final NodeBase node = treeData.getNodeFactory().createNode(child);
            node.setOriginalIndex(child.getOriginalIndex());
            treeData.addClonedNode(node);

            // Set index to its place in this subnetwork to be able to find edge target below.
            child.setIndex(i);
            node.setIndex(i);
            i++;
        }

        final NodeBase parentPtr = parent;

        // Clone edges.
        for (final NodeBase node : parent.getChildren()) {
            for (final Edge<NodeBase> edge : node.getOutEdges()) {
                // If neighbour node is within the same module, add the link to this subnetwork.
                if (edge.getTarget().getParent() == parentPtr) {
                    treeData.addEdge(node.getIndex(), edge.getTarget().getIndex(), edge.getData().weight, edge.getData().flow);
                }
            }
        }

        final double parentExit = getNode(parent).getData().getExitFlow();

        exitNetworkFlow = parentExit;
        exitNetworkFlowLogExitNetworkFlow = plogp(exitNetworkFlow);
    }

    @Override
    protected void transformNodeFlowToEnterFlow(final NodeBase parent) {
        for (final NodeBase moduleBase : parent.getChildren()) {
            final Node module = getNode(moduleBase);
            module.getData().setFlow(module.getData().getEnterFlow());
        }
    }

    @Override
    protected void cloneFlowData(final NodeBase source, final NodeBase target) {
        getNode(target).setData(getNode(source).getData());
    }

    @Override
    protected void moveNodesToPredefinedModules() {
        if (DEBUG) {
            LOGGER.log(Level.INFO, "{0}.moveNodesToPredefinedModules", getClass().getSimpleName());
        }

        // Size of active network and cluster array should match.
        assert moveTo.size() == activeNetwork.size();

        final int numNodes = activeNetwork.size();

        if (DEBUG) {
            final String log = String.format("Begin moving %d nodes to predefined modules, starting with codelength %f...\n",
                    numNodes, codelength);
            LOGGER.log(Level.INFO, log);
        }

        int numMoved = 0;
        for (int k = 0; k < numNodes; k++) {
            final Node current = getNode(activeNetwork.get(k));
            final int oldM = current.getIndex(); // == k
            assert oldM == k;
            final int newM = moveTo.get(k);

            if (newM != oldM) {
                final DeltaFlow oldModuleDelta = new DeltaFlow(oldM, 0, 0);
                final DeltaFlow newModuleDelta = new DeltaFlow(newM, 0, 0);

                addTeleportationDeltaFlowOnOldModuleIfMove(current, oldModuleDelta);
                addTeleportationDeltaFlowOnNewModuleIfMove(current, newModuleDelta);

                // For all outlinks.
                for (final Edge<NodeBase> edge : current.getOutEdges()) {
                    if (edge.isSelfPointing()) {
                        continue;
                    }

                    final int otherModule = edge.getTarget().getIndex();
                    if (otherModule == oldM) {
                        oldModuleDelta.setDeltaExit(oldModuleDelta.getDeltaExit() + edge.getData().flow);
                    } else if (otherModule == newM) {
                        newModuleDelta.setDeltaExit(newModuleDelta.getDeltaExit() + edge.getData().flow);
                    }
                }

                // For all inlinks.
                for (final Edge<NodeBase> edge : current.getInEdges()) {
                    if (edge.isSelfPointing()) {
                        continue;
                    }

                    final int otherModule = edge.getSource().getIndex();
                    if (otherModule == oldM) {
                        oldModuleDelta.setDeltaEnter(oldModuleDelta.getDeltaEnter() + edge.getData().flow);
                    } else if (otherModule == newM) {
                        newModuleDelta.setDeltaEnter(newModuleDelta.getDeltaEnter() + edge.getData().flow);
                    }
                }

                // Update empty module vector.
                if (moduleMembers[newM] == 0) {
                    // Remove last element.
                    emptyModules.remove(emptyModules.size() - 1);
                }

                if (moduleMembers[oldM] == 1) {
                    emptyModules.add(oldM);
                }

                updateCodelength(current, oldModuleDelta, newModuleDelta);

                moduleMembers[oldM] -= 1;
                moduleMembers[newM] += 1;

                current.setIndex(newM);
                numMoved++;
            }
        }

        if (DEBUG) {
            final String log = String.format("Done! Moved %d nodes into %d modules to codelength: %.5f%n",
                    numMoved, getNumActiveModules(), codelength);
            LOGGER.log(Level.INFO, log);
        }
    }

    /**
     * Try to minimize the codelength by trying to move nodes into the same modules as neighbouring nodes.
     *
     * For each node: 1. Calculate the change in codelength for a move to each of its neighbouring modules or to an
     * empty module 2. Move to the one that reduces the codelength the most, if any.
     *
     * The first step would require O(d^2), where d is the degree, if calculating the full change at each neighbour, but
     * a special data structure is used to accumulate the marginal effect of each link on its target, giving O(d).
     *
     * @return The number of nodes moved.
     */
    public int tryMoveEachNodeIntoBestModule() {
        if (DEBUG) {
            LOGGER.log(Level.INFO, "{0}.tryMoveEachNodeIntoBestModule", getClass().getSimpleName());
        }

        final int numNodes = activeNetwork.size();
        if (DEBUG) {
            dumpActiveNetwork("in");
        }

        // Get random enumeration of nodes.
        final int[] randomOrder = new int[numNodes];
        InfoMath.getRandomizedIndexVector(randomOrder, rand);

        final DeltaFlow[] moduleDeltaEnterExit = new DeltaFlow[numNodes];
        Arrays.fill(moduleDeltaEnterExit, new DeltaFlow());

        final int[] redirect = new int[numNodes];
        Arrays.fill(redirect, 0);
        int offset = 1;
        final int maxOffset = Integer.MAX_VALUE - 1 - numNodes;

        int numMoved = 0;
        for (int i = 0; i < numNodes; i++) {
            // Reset offset before overflow.
            if (offset > maxOffset) {
                Arrays.fill(redirect, 0);
                offset = 1;
            }

            // Pick nodes in random order.
            final int flip = randomOrder[i];
            final Node current = getNode(activeNetwork.get(flip));

            // If no links connecting this node with other nodes, it won't move into others,
            // and others won't move into this. TODO: always best leave it alone?
            if (current.getDegree() == 0
                    || (config.isIncludeSelfLinks()
                    && (current.getOutDegree() == 1 && current.getInDegree() == 1)
                    && current.getOutEdges().get(0).getTarget().equals(current))) {
                if (DEBUG) {
                    LOGGER.log(Level.INFO, "SKIPPING isolated node {0}", current);
                }
                //TODO: if not skipping self-links, this yields different results from moveNodesToPredefinedModules!!
                assert !config.isIncludeSelfLinks();
                continue;
            }

            // Create vector with module links.
            int numModuleLinks = 0;
            if (current.isDangling()) {
                redirect[current.getIndex()] = offset + numModuleLinks;
                moduleDeltaEnterExit[numModuleLinks].setModule(current.getIndex());
                moduleDeltaEnterExit[numModuleLinks].setDeltaExit(0);
                moduleDeltaEnterExit[numModuleLinks].setDeltaEnter(0);
                numModuleLinks++;
            } else {
                // For all outlinks.
                for (final Edge<NodeBase> edge : current.getOutEdges()) {
                    if (edge.isSelfPointing()) {
                        continue;
                    }

                    final NodeBase neighbour = edge.getTarget();

                    if (redirect[neighbour.getIndex()] >= offset) {
                        moduleDeltaEnterExit[redirect[neighbour.getIndex()] - offset].setDeltaExit(
                                moduleDeltaEnterExit[redirect[neighbour.getIndex()] - offset].getDeltaExit() + edge.getData().flow);
                    } else {
                        redirect[neighbour.getIndex()] = offset + numModuleLinks;
                        moduleDeltaEnterExit[numModuleLinks].setModule(neighbour.getIndex());
                        moduleDeltaEnterExit[numModuleLinks].setDeltaExit(edge.getData().flow);
                        moduleDeltaEnterExit[numModuleLinks].setDeltaEnter(0);
                        numModuleLinks++;
                    }
                }
            }

            // For all inlinks.
            for (final Edge<NodeBase> edge : current.getInEdges()) {
                if (edge.isSelfPointing()) {
                    continue;
                }

                final Node neighbour = getNode(edge.getSource());

                if (redirect[neighbour.getIndex()] >= offset) {
                    moduleDeltaEnterExit[redirect[neighbour.getIndex()] - offset].setDeltaEnter(
                            moduleDeltaEnterExit[redirect[neighbour.getIndex()] - offset].getDeltaEnter() + edge.getData().flow);
                } else {
                    redirect[neighbour.getIndex()] = offset + numModuleLinks;
                    moduleDeltaEnterExit[numModuleLinks].setModule(neighbour.getIndex());
                    moduleDeltaEnterExit[numModuleLinks].setDeltaExit(0);
                    moduleDeltaEnterExit[numModuleLinks].setDeltaEnter(edge.getData().flow);
                    numModuleLinks++;
                }
            }

            // If alone in the module, add virtual link to the module (used when adding teleportation).
            if (redirect[current.getIndex()] < offset) {
                redirect[current.getIndex()] = offset + numModuleLinks;
                moduleDeltaEnterExit[numModuleLinks].setModule(current.getIndex());
                moduleDeltaEnterExit[numModuleLinks].setDeltaExit(0);
                moduleDeltaEnterExit[numModuleLinks].setDeltaEnter(0);
                numModuleLinks++;
            }

            // Empty function if no teleportation coding model.
            addTeleportationDeltaFlowIfMove(current, moduleDeltaEnterExit, numModuleLinks);

            // Option to move to empty module (if node not already alone).
            if (moduleMembers[current.getIndex()] > 1 && !emptyModules.isEmpty()) {
                moduleDeltaEnterExit[numModuleLinks].setModule(emptyModules.get(emptyModules.size() - 1));
                moduleDeltaEnterExit[numModuleLinks].setDeltaExit(0);
                moduleDeltaEnterExit[numModuleLinks].setDeltaEnter(0);
                numModuleLinks++;
            }

            // Store the DeltaFlow of the current module.
            final DeltaFlow oldModuleDelta = new DeltaFlow(moduleDeltaEnterExit[redirect[current.getIndex()] - offset]);

            if (DEBUG) {
                for (int j = 0; j < numModuleLinks - 1; ++j) {
                    LOGGER.log(Level.INFO, "{0}", moduleDeltaEnterExit[j].getModule());
                }
            }

            // Randomize link order for optimized search.
            for (int j = 0; j < numModuleLinks - 1; ++j) {
                final int randPos = j + rand.randInt(numModuleLinks - j - 1);
                final DeltaFlow t = moduleDeltaEnterExit[j];
                moduleDeltaEnterExit[j] = moduleDeltaEnterExit[randPos];
                moduleDeltaEnterExit[randPos] = t;
            }

            DeltaFlow bestDeltaModule = new DeltaFlow(oldModuleDelta);
            double bestDeltaCodelength = 0;

            // Find the move that minimizes the description length.
            for (int j = 0; j < numModuleLinks; ++j) {
                final int otherModule = moduleDeltaEnterExit[j].getModule();
                if (otherModule != current.getIndex()) {
                    double deltaCodelength = getDeltaCodelength(current, oldModuleDelta, moduleDeltaEnterExit[j]);

                    if (deltaCodelength < bestDeltaCodelength) {
                        bestDeltaModule = new DeltaFlow(moduleDeltaEnterExit[j]);
                        bestDeltaCodelength = deltaCodelength;
                    }

                }
            }

            // Make best possible move.
            if (bestDeltaModule.getModule() != current.getIndex()) {
                final int bestModuleIndex = bestDeltaModule.getModule();
                // Update empty module vector.
                if (moduleMembers[bestModuleIndex] == 0) {
                    emptyModules.remove(emptyModules.size() - 1);
                }
                if (moduleMembers[current.getIndex()] == 1) {
                    emptyModules.add(current.getIndex());
                }

                updateCodelength(current, oldModuleDelta, bestDeltaModule);

                moduleMembers[current.getIndex()] -= 1;
                moduleMembers[bestModuleIndex] += 1;

                current.setIndex(bestModuleIndex);
                numMoved++;
            }

            offset += numNodes;
        }
        if (DEBUG) {
            dumpActiveNetwork("");
        }
        return numMoved;
    }

    @Override
    protected int consolidateModules(final boolean replaceExistingStructure, final boolean asSubModules) {
        if (DEBUG) {
            final String log = String.format("%s.consolidateModules(%s,%s)%n", getClass().getSimpleName(), replaceExistingStructure, asSubModules);
            LOGGER.log(Level.INFO, log);
        }

        final int numNodes = activeNetwork.size();
        final NodeBase[] modules = new NodeBase[numNodes];

        final boolean activeNetworkAlreadyHaveModuleLevel = activeNetwork.get(0).getParent() != getRoot();
        final boolean activeNetworkIsLeafNetwork = activeNetwork.get(0).isLeaf();

        if (asSubModules) {
            assert activeNetworkAlreadyHaveModuleLevel;

            // Release the pointers from modules to leaf nodes so that the new submodules will be inserted as its only children.
            for (final NodeBase module : getRoot().getChildren()) {
                module.releaseChildren();
            }
        } else {
            // Happens after optimizing fine-tune and when moving leaf nodes to super clusters.
            if (activeNetworkAlreadyHaveModuleLevel) {
                if (DEBUG) {
                    final String log = String.format("Replace existing %d modules with its children before consolidating the %d dynamic modules...%n",
                            getNumTopModules(), getNumActiveModules());
                    LOGGER.log(Level.INFO, log);
                }
                getRoot().replaceChildrenWithGrandChildren();
                assert activeNetwork.get(0).getParent() == getRoot();
            }

            getRoot().releaseChildren();
        }

        // Create the new module nodes and re-parent the active network from its common parent to the new module level.
        for (int i = 0; i < numNodes; ++i) {
            final NodeBase node = activeNetwork.get(i);
            final int moduleIndex = node.getIndex();
            if (modules[moduleIndex] == null) {
                modules[moduleIndex] = treeData.getNodeFactory().createNode(moduleFlowData[moduleIndex]);
                node.getParent().addChild(modules[moduleIndex]);
                modules[moduleIndex].setIndex(moduleIndex);
            }

            modules[moduleIndex].addChild(node);
        }

        if (asSubModules) {
            if (DEBUG) {
                final String log = String.format("Consolidated %d submodules under %d modules, store module structure before releasing it...%n",
                        getNumActiveModules(), getNumTopModules());
                LOGGER.log(Level.INFO, log);
            }

            // Store the module structure on the submodules.
            int moduleIndex = 0;
            for (final NodeBase module : getRoot().getChildren()) {
                for (final NodeBase subModule : module.getChildren()) {
                    subModule.setIndex(moduleIndex);
                }
                moduleIndex++;
            }

            if (replaceExistingStructure) {
                // Remove the module level.
                getRoot().replaceChildrenWithGrandChildren();
            }
        }

        // Aggregate links from lower level to the new modular level
        /*
         typedef std::pair<NodeBase*, NodeBase*> NodePair
         typedef std::map<NodePair, double> EdgeMap
         EdgeMap moduleLinks
         */
        final Map<Tuple<NodeBase, NodeBase>, Double> moduleLinks = new TreeMap<>((lhs, rhs) -> {
            if (lhs.getFirst().getId() < rhs.getFirst().getId()) {
                return -1;
            }
            if (lhs.getFirst().getId() > rhs.getFirst().getId()) {
                return 1;
            }
            if (lhs.getSecond().getId() < rhs.getSecond().getId()) {
                return -1;
            }
            if (lhs.getSecond().getId() > rhs.getSecond().getId()) {
                return 1;
            }
            return 0;
        });
        for (final NodeBase node : activeNetwork) {
            final NodeBase parent = node.getParent();

            for (final Edge<NodeBase> edge : node.getOutEdges()) {
                final NodeBase otherParent = edge.getTarget().getParent();

                if (otherParent != parent) {
                    NodeBase m1 = parent;
                    NodeBase m2 = otherParent;
                    // If undirected, the order may be swapped to aggregate the edge on an opposite one.
                    if (config.isUndirected() && m1.getIndex() > m2.getIndex()) {
                        NodeBase t = m1;
                        m1 = m2;
                        m2 = t;
                    }

                    // Insert the node pair in the edge map.
                    // If not inserted, add the flow value to existing node pair.
                    final Tuple<NodeBase, NodeBase> key = Tuple.create(m1, m2);
                    if (moduleLinks.containsKey(key)) {
                        final double d = moduleLinks.get(key);
                        moduleLinks.put(key, d + edge.getData().flow);
                    } else {
                        moduleLinks.put(key, edge.getData().flow);
                    }
                }
            }
        }

        // Add the aggregated edge flow structure to the new modules.
        for (final Map.Entry<Tuple<NodeBase, NodeBase>, Double> entry : moduleLinks.entrySet()) {
            final Tuple<NodeBase, NodeBase> nodePair = entry.getKey();
            final double value = entry.getValue();

            nodePair.getFirst().addOutEdge(nodePair.getSecond(), 0, value);
        }

        // Replace active network with its children if not at leaf level.
        if (!activeNetworkIsLeafNetwork && replaceExistingStructure) {
            for (final NodeBase node : activeNetwork) {
                node.replaceWithChildren();
            }
        }

        // Calculate the number of non-trivial modules.
        numNonTrivialTopModules = 0;
        for (final NodeBase module : getRoot().getChildren()) {
            if (module.getChildDegree() != 1) {
                numNonTrivialTopModules++;
            }
        }

        return getNumActiveModules();
    }

    @Override
    protected void printNodeRanks(final PrintWriter out) {
        out.printf("#node-flow%n");
        for (final NodeBase node : treeData.getLeaves()) {
            out.printf("%f\n", getNode(node).getData().getFlow());
        }
    }

    @Override
    protected void printFlowNetwork(final PrintWriter out) {
        for (final NodeBase node : treeData.getLeaves()) {
            out.printf("%d (%s)\n", node.getOriginalIndex(), getNode(node).getData());
            for (final Edge<NodeBase> edge : node.getOutEdges()) {
                out.printf("  --> %d (%.9f)\n", edge.getTarget().getOriginalIndex(), edge.getData().flow);
            }
            for (final Edge<NodeBase> edge : node.getInEdges()) {
                out.printf("  <-- %d (%.9f)\n", edge.getSource().getOriginalIndex(), edge.getData().flow);
            }
        }
    }

    @Override
    protected void sortTree(final NodeBase parent) {
        if (parent.getSubInfomap() != null) {
            parent.getSubInfomap().sortTree();
        }

        final MultiMap<Double, NodeBase> sortedModules = new MultiMap<>((d1, d2) -> (int) Math.signum(d2 - d1));

        if (DEBUG && parent.getChildDegree() > 0) {
            for (final NodeBase child : parent.getChildren()) {
                LOGGER.log(Level.INFO, "{0}", child.getId());
            }
        }

        for (final NodeBase child : parent.getChildren()) {
            sortTree(child);
            final double rank = getNode(child).getData().getFlow();
            sortedModules.put(rank, child);
        }

        parent.releaseChildren();

        int sortedIndex = 0;
        for (final Map.Entry<Double, NodeBase> entry : sortedModules.entrySet()) {
            parent.addChild(entry.getValue());
            entry.getValue().setIndex(sortedIndex);

            sortedIndex++;
        }
    }

    protected int getNumActiveModules() {
        return activeNetwork.size() - emptyModules.size();
    }

    @Override
    protected int getNumDynamicModules() {
        return activeNetwork.size() - emptyModules.size();
    }

    protected Node getNode(final NodeBase node) {
        return (Node) node;
    }

    @Override
    protected FlowBase getNodeData(final NodeBase node) {
        return getNode(node).getData();
    }

    private void dumpActiveNetwork(final String prefix) {
        for (final NodeBase node : activeNetwork) {
            LOGGER.log(Level.INFO, prefix + " " + node);
        }
    }
}
