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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.Edge;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.Node;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeFactoryBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.InfoMath;
import static au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.InfoMath.plogp;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Logf;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.MultiMap;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Resizer;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author algol
 */
public abstract class InfomapGreedy extends InfomapBase {

    protected FlowBase[] moduleFlowData;
    protected int[] moduleMembers;
    protected ArrayList<Integer> emptyModules;

    protected double nodeFlow_log_nodeFlow; // Constant while the leaf network is the same.
    protected double flow_log_flow; // node.(flow + exitFlow)
    protected double exit_log_exit;
    protected double enter_log_enter;
    protected double enterFlow;
    protected double enterFlow_log_enterFlow;

    // For hierarchical.
    protected double exitNetworkFlow;
    protected double exitNetworkFlow_log_exitNetworkFlow;

    public InfomapGreedy(final Config config, final NodeFactoryBase nodeFactory, final GraphReadMethods rg) {
        super(config, nodeFactory, rg);

        moduleFlowData = new FlowBase[0];
    }

    @Override
    public void initEnterExitFlow() {
        if (DEBUG) {
            System.out.printf("%s.initEnterExitFlow()\n", getClass().getSimpleName());
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
        nodeFlow_log_nodeFlow = 0;

        // For each module...
        for (final NodeBase nodeBase : activeNetwork) {
            final Node node = getNode(nodeBase);
            nodeFlow_log_nodeFlow += plogp(node.getData().getFlow());
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
            node.index = i; // Unique module index for each node
            moduleFlowData[i] = node.getData().copy();
            i++;
        }

        // Initiate codelength terms for the initial state of one module per node.
        calculateCodelengthFromActiveNetwork(hasDetailedBalance());
    }

    void calculateCodelengthFromActiveNetwork(final boolean detailedBalance) {
        if (DEBUG) {
            System.out.printf("%s.calculateCodelengthFromActiveNetwork(%s)\n", getClass().getSimpleName(), detailedBalance);
        }

        flow_log_flow = 0;
        exit_log_exit = 0;
        enterFlow = 0;

        if (detailedBalance) {
            // For each module...
            for (final NodeBase nodeBase : activeNetwork) {
                final Node node = getNode(nodeBase);

                // Own node/module codebook.
                flow_log_flow += plogp(node.getData().getFlow() + node.getData().getExitFlow());

                // Use of index codebook.
                enterFlow += node.getData().getExitFlow();
                exit_log_exit += plogp(node.getData().getExitFlow());
            }

            enterFlow += exitNetworkFlow;
            enterFlow_log_enterFlow = plogp(enterFlow);

            indexCodelength = enterFlow_log_enterFlow - exit_log_exit - exitNetworkFlow_log_exitNetworkFlow;
            moduleCodelength = -exit_log_exit + flow_log_flow - nodeFlow_log_nodeFlow;
            codelength = indexCodelength + moduleCodelength;
        } else {
            enter_log_enter = 0;

            // For each module...
            for (final NodeBase nodeBase : activeNetwork) {
                final Node node = getNode(nodeBase);

                // Own node/module codebook.
                flow_log_flow += plogp(node.getData().getFlow() + node.getData().getExitFlow());

                // Use of index codebook.
                enter_log_enter += plogp(node.getData().getEnterFlow());
                exit_log_exit += plogp(node.getData().getExitFlow());
                enterFlow += node.getData().getEnterFlow();
            }

            enterFlow += exitNetworkFlow;
            enterFlow_log_enterFlow = plogp(enterFlow);

            indexCodelength = enterFlow_log_enterFlow - enter_log_enter - exitNetworkFlow_log_exitNetworkFlow;
            moduleCodelength = -exit_log_exit + flow_log_flow - nodeFlow_log_nodeFlow;
            codelength = indexCodelength + moduleCodelength;
        }
    }

    @Override
    protected int optimizeModules() {
        int numOptimizationRounds = 0;
        double oldCodelength = codelength;
        int loopLimit = config.coreLoopLimit;
        if (config.coreLoopLimit > 0 && config.randomizeCoreLoopLimit) {
            loopLimit = (int) (rand.nextDouble() * config.coreLoopLimit) + 1;
        }

        // Iterate while the optimization loop moves some nodes within the dynamic modular structure.
        do {
            oldCodelength = codelength;
            tryMoveEachNodeIntoBestModule(); // returns numNodesMoved
            ++numOptimizationRounds;
        } while (numOptimizationRounds != loopLimit
                && codelength < oldCodelength - config.minimumCodelengthImprovement);

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
        final int oldModule = oldModuleDelta.module;
        final int newModule = newModuleDelta.module;
        double deltaEnterExitOldModule = oldModuleDelta.deltaEnter + oldModuleDelta.deltaExit;
        double deltaEnterExitNewModule = newModuleDelta.deltaEnter + newModuleDelta.deltaExit;

        final double delta_enter = plogp(enterFlow + deltaEnterExitOldModule - deltaEnterExitNewModule) - enterFlow_log_enterFlow;

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

        double deltaL = delta_enter - delta_enter_log_enter - delta_exit_log_exit + delta_flow_log_flow;

        return deltaL;
    }

    /**
     * Update the codelength to reflect the move of node current in
     * oldModuleDelta to newModuleDelta (Specialized for undirected flow and
     * when exitFlow == enterFlow
     *
     * @param current the current node.
     * @param oldModuleDelta the old module delta flow.
     * @param newModuleDelta the new module delta flow.
     */
    protected void updateCodelength(final Node current, final DeltaFlow oldModuleDelta, final DeltaFlow newModuleDelta) {
        final int oldModule = oldModuleDelta.module;
        final int newModule = newModuleDelta.module;
        final double deltaEnterExitOldModule = oldModuleDelta.deltaEnter + oldModuleDelta.deltaExit;
        final double deltaEnterExitNewModule = newModuleDelta.deltaEnter + newModuleDelta.deltaExit;

        enterFlow
                -= moduleFlowData[oldModule].getEnterFlow()
                + moduleFlowData[newModule].getEnterFlow();
        enter_log_enter
                -= plogp(moduleFlowData[oldModule].getEnterFlow())
                + plogp(moduleFlowData[newModule].getEnterFlow());
        exit_log_exit
                -= plogp(moduleFlowData[oldModule].getExitFlow())
                + plogp(moduleFlowData[newModule].getExitFlow());
        flow_log_flow
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
        enter_log_enter
                += plogp(moduleFlowData[oldModule].getEnterFlow())
                + plogp(moduleFlowData[newModule].getEnterFlow());
        exit_log_exit
                += plogp(moduleFlowData[oldModule].getExitFlow())
                + plogp(moduleFlowData[newModule].getExitFlow());
        flow_log_flow
                += plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow())
                + plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow());

        enterFlow_log_enterFlow = plogp(enterFlow);

        indexCodelength = enterFlow_log_enterFlow - enter_log_enter - exitNetworkFlow_log_exitNetworkFlow;
        moduleCodelength = -exit_log_exit + flow_log_flow - nodeFlow_log_nodeFlow;
        codelength = indexCodelength + moduleCodelength;
    }

    @Override
    protected void resetModuleFlowFromLeafNodes() {
        // Reset from top to bottom.
        resetModuleFlow(getRoot());

        // Aggregate from bottom to top.
        for (NodeBase node : treeData.getLeaves()) {
            final double flow = getNode(node).getData().getFlow();
            while (node.parent != null) {
                node = node.parent;
                final double parentFlow = getNode(node).getData().getFlow();
                getNode(node).getData().setFlow(parentFlow + flow);
//                getNode(*node).data.flow += flow;
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
            System.out.printf("%s.generateNetworkFromChildren\n", getClass().getSimpleName());
        }

        exitNetworkFlow = 0;

        // Clone all nodes.
        int i = 0;
        for (final NodeBase child : parent.getChildren()) {
            final NodeBase node = treeData.getNodeFactory().createNode(child);
            node.originalIndex = child.originalIndex;
            treeData.addClonedNode(node);

            // Set index to its place in this subnetwork to be able to find edge target below.
            child.index = i;
            node.index = i;
            i++;
        }

        final NodeBase parentPtr = parent;

        // Clone edges.
        for (final NodeBase node : parent.getChildren()) {
            for (final Edge<NodeBase> edge : node.getOutEdges()) {
                // If neighbour node is within the same module, add the link to this subnetwork.
                if (edge.getTarget().parent == parentPtr) {
                    treeData.addEdge(node.index, edge.getTarget().index, edge.getData().weight, edge.getData().flow);
                }
            }
        }

        final double parentExit = getNode(parent).getData().getExitFlow();

        exitNetworkFlow = parentExit;
        exitNetworkFlow_log_exitNetworkFlow = plogp(exitNetworkFlow);
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
            System.out.printf("%s.moveNodesToPredefinedModules\n", getClass().getSimpleName());
        }

        // Size of active network and cluster array should match.
        assert moveTo.size() == activeNetwork.size();

        final int numNodes = activeNetwork.size();

        if (DEBUG) {
            System.out.printf("Begin moving %d nodes to predefined modules, starting with codelength %f...\n",
                    numNodes, codelength);
        }

        int numMoved = 0;
        for (int k = 0; k < numNodes; k++) {
            final Node current = getNode(activeNetwork.get(k));
            final int oldM = current.index; // == k
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

                    final int otherModule = edge.getTarget().index;
                    if (otherModule == oldM) {
                        oldModuleDelta.deltaExit += edge.getData().flow;
                    } else if (otherModule == newM) {
                        newModuleDelta.deltaExit += edge.getData().flow;
                    }
                }

                // For all inlinks.
                for (final Edge<NodeBase> edge : current.getInEdges()) {
                    if (edge.isSelfPointing()) {
                        continue;
                    }

                    final int otherModule = edge.getSource().index;
                    if (otherModule == oldM) {
                        oldModuleDelta.deltaEnter += edge.getData().flow;
                    } else if (otherModule == newM) {
                        newModuleDelta.deltaEnter += edge.getData().flow;
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

                current.index = newM;
                numMoved++;
            }
        }

        if (DEBUG) {
            System.out.printf("Done! Moved %d nodes into %d modules to codelength: %.5f\n",
                    numMoved, getNumActiveModules(), codelength);
        }
    }

    /**
     * Try to minimize the codelength by trying to move nodes into the same
     * modules as neighbouring nodes.
     *
     * For each node: 1. Calculate the change in codelength for a move to each
     * of its neighbouring modules or to an empty module 2. Move to the one that
     * reduces the codelength the most, if any.
     *
     * The first step would require O(d^2), where d is the degree, if
     * calculating the full change at each neighbour, but a special data
     * structure is used to accumulate the marginal effect of each link on its
     * target, giving O(d).
     *
     * @return The number of nodes moved.
     */
    int tryMoveEachNodeIntoBestModule() {
        if (DEBUG) {
            System.out.printf("%s.tryMoveEachNodeIntoBestModule\n", getClass().getSimpleName());
        }

        final int numNodes = activeNetwork.size();
        dumpActiveNetwork("in");

        // Get random enumeration of nodes.
        final int[] randomOrder = new int[numNodes];
        InfoMath.getRandomizedIndexVector(randomOrder, rand);

        final DeltaFlow[] moduleDeltaEnterExit = new DeltaFlow[numNodes];
        for (int i = 0; i < numNodes; i++) {
            moduleDeltaEnterExit[i] = new DeltaFlow();
        }

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
                    || (config.includeSelfLinks
                    && (current.getOutDegree() == 1 && current.getInDegree() == 1)
                    && current.getOutEdges().get(0).getTarget().equals(current))) {
                Logf.printf("SKIPPING isolated node %s\n", current);
                //TODO: if not skipping self-links, this yields different results from moveNodesToPredefinedModules!!
                assert !config.includeSelfLinks;
                continue;
            }

            // Create vector with module links.
            int numModuleLinks = 0;
            if (current.isDangling()) {
                redirect[current.index] = offset + numModuleLinks;
                moduleDeltaEnterExit[numModuleLinks].module = current.index;
                moduleDeltaEnterExit[numModuleLinks].deltaExit = 0;
                moduleDeltaEnterExit[numModuleLinks].deltaEnter = 0;
                numModuleLinks++;
            } else {
                // For all outlinks.
                for (final Edge<NodeBase> edge : current.getOutEdges()) {
                    if (edge.isSelfPointing()) {
                        continue;
                    }

                    final NodeBase neighbour = edge.getTarget();

                    if (redirect[neighbour.index] >= offset) {
                        moduleDeltaEnterExit[redirect[neighbour.index] - offset].deltaExit += edge.getData().flow;
                    } else {
                        redirect[neighbour.index] = offset + numModuleLinks;
                        moduleDeltaEnterExit[numModuleLinks].module = neighbour.index;
                        moduleDeltaEnterExit[numModuleLinks].deltaExit = edge.getData().flow;
                        moduleDeltaEnterExit[numModuleLinks].deltaEnter = 0;
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

                if (redirect[neighbour.index] >= offset) {
                    moduleDeltaEnterExit[redirect[neighbour.index] - offset].deltaEnter += edge.getData().flow;
                } else {
                    redirect[neighbour.index] = offset + numModuleLinks;
                    moduleDeltaEnterExit[numModuleLinks].module = neighbour.index;
                    moduleDeltaEnterExit[numModuleLinks].deltaExit = 0;
                    moduleDeltaEnterExit[numModuleLinks].deltaEnter = edge.getData().flow;
                    numModuleLinks++;
                }
            }

            // If alone in the module, add virtual link to the module (used when adding teleportation).
            if (redirect[current.index] < offset) {
                redirect[current.index] = offset + numModuleLinks;
                moduleDeltaEnterExit[numModuleLinks].module = current.index;
                moduleDeltaEnterExit[numModuleLinks].deltaExit = 0;
                moduleDeltaEnterExit[numModuleLinks].deltaEnter = 0;
                numModuleLinks++;
            }

            // Empty function if no teleportation coding model.
            addTeleportationDeltaFlowIfMove(current, moduleDeltaEnterExit, numModuleLinks);

            // Option to move to empty module (if node not already alone).
            if (moduleMembers[current.index] > 1 && emptyModules.size() > 0) {
                moduleDeltaEnterExit[numModuleLinks].module = emptyModules.get(emptyModules.size() - 1);
                moduleDeltaEnterExit[numModuleLinks].deltaExit = 0;
                moduleDeltaEnterExit[numModuleLinks].deltaEnter = 0;
                numModuleLinks++;
            }

            // Store the DeltaFlow of the current module.
            final DeltaFlow oldModuleDelta = new DeltaFlow(moduleDeltaEnterExit[redirect[current.index] - offset]);

            if (Logf.DEBUGF) {
                for (int j = 0; j < numModuleLinks - 1; ++j) {
                    Logf.printf("%d ", moduleDeltaEnterExit[j].module);
                }
                Logf.printf("\n");
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
                final int otherModule = moduleDeltaEnterExit[j].module;
                if (otherModule != current.index) {
                    double deltaCodelength = getDeltaCodelength(current, oldModuleDelta, moduleDeltaEnterExit[j]);

                    if (deltaCodelength < bestDeltaCodelength) {
                        bestDeltaModule = new DeltaFlow(moduleDeltaEnterExit[j]);
                        bestDeltaCodelength = deltaCodelength;
                    }

                }
            }

            // Make best possible move.
            if (bestDeltaModule.module != current.index) {
                final int bestModuleIndex = bestDeltaModule.module;
                //Update empty module vector.
                if (moduleMembers[bestModuleIndex] == 0) {
                    emptyModules.remove(emptyModules.size() - 1);
                }
                if (moduleMembers[current.index] == 1) {
                    emptyModules.add(current.index);
                }

                updateCodelength(current, oldModuleDelta, bestDeltaModule);

                moduleMembers[current.index] -= 1;
                moduleMembers[bestModuleIndex] += 1;

                current.index = bestModuleIndex;
                numMoved++;
            }

            offset += numNodes;
        }

        dumpActiveNetwork("");
        return numMoved;
    }

    @Override
    protected int consolidateModules(final boolean replaceExistingStructure, final boolean asSubModules) {
        if (DEBUG) {
            System.out.printf("%s.consolidateModules(%s,%s)\n", getClass().getSimpleName(), replaceExistingStructure, asSubModules);
        }

        final int numNodes = activeNetwork.size();
        final NodeBase[] modules = new NodeBase[numNodes];

        final boolean activeNetworkAlreadyHaveModuleLevel = activeNetwork.get(0).parent != getRoot();
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
                    System.out.printf("Replace existing %d modules with its children before consolidating the %d dynamic modules...\n",
                            getNumTopModules(), getNumActiveModules());
                }
                getRoot().replaceChildrenWithGrandChildren();
                assert activeNetwork.get(0).parent == getRoot();
            }

            getRoot().releaseChildren();
        }

        // Create the new module nodes and re-parent the active network from its common parent to the new module level.
        for (int i = 0; i < numNodes; ++i) {
            final NodeBase node = activeNetwork.get(i);
            final int moduleIndex = node.index;
            if (modules[moduleIndex] == null) {
                modules[moduleIndex] = treeData.getNodeFactory().createNode(moduleFlowData[moduleIndex]);
                node.parent.addChild(modules[moduleIndex]);
                modules[moduleIndex].index = moduleIndex;
            }

            modules[moduleIndex].addChild(node);
        }

        if (asSubModules) {
            if (DEBUG) {
                System.out.printf("Consolidated %d submodules under %d modules, store module structure before releasing it...\n",
                        getNumActiveModules(), getNumTopModules());
            }

            // Store the module structure on the submodules.
            int moduleIndex = 0;
            for (final NodeBase module : getRoot().getChildren()) {
                for (final NodeBase subModule : module.getChildren()) {
                    subModule.index = moduleIndex;
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
         typedef std::pair<NodeBase*, NodeBase*> NodePair;
         typedef std::map<NodePair, double> EdgeMap;
         EdgeMap moduleLinks;
         */
        final TreeMap<Tuple<NodeBase, NodeBase>, Double> moduleLinks = new TreeMap<>(new Comparator<Tuple<NodeBase, NodeBase>>() {
            @Override
            public int compare(final Tuple<NodeBase, NodeBase> lhs, final Tuple<NodeBase, NodeBase> rhs) {
                if (lhs.getFirst().id < rhs.getFirst().id) {
                    return -1;
                }
                if (lhs.getFirst().id > rhs.getFirst().id) {
                    return 1;
                }
                if (lhs.getSecond().id < rhs.getSecond().id) {
                    return -1;
                }
                if (lhs.getSecond().id > rhs.getSecond().id) {
                    return 1;
                }
                return 0;
            }
        });
        for (final NodeBase node : activeNetwork) {
            final NodeBase parent = node.parent;

            for (Edge<NodeBase> edge : node.getOutEdges()) {
                final NodeBase otherParent = edge.getTarget().parent;

                if (otherParent != parent) {
                    NodeBase m1 = parent;
                    NodeBase m2 = otherParent;
                    // If undirected, the order may be swapped to aggregate the edge on an opposite one.
                    if (config.isUndirected() && m1.index > m2.index) {
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
        out.printf("#node-flow\n");
        for (final NodeBase node : treeData.getLeaves()) {
            out.printf("%f\n", getNode(node).getData().getFlow());
        }
    }

    @Override
    protected void printFlowNetwork(final PrintWriter out) {
        for (final NodeBase node : treeData.getLeaves()) {
            out.printf("%d (%s)\n", node.originalIndex, getNode(node).getData());
            for (final Edge<NodeBase> edge : node.getOutEdges()) {
                out.printf("  --> %d (%.9f)\n", edge.getTarget().originalIndex, edge.getData().flow);
            }
            for (final Edge<NodeBase> edge : node.getInEdges()) {
                out.printf("  <-- %d (%.9f)\n", edge.getSource().originalIndex, edge.getData().flow);
            }
        }
    }

    @Override
    protected void sortTree(final NodeBase parent) {
        if (parent.getSubInfomap() != null) {
            parent.getSubInfomap().sortTree();
        }

        final MultiMap<Double, NodeBase> sortedModules = new MultiMap<>(new Comparator<Double>() {
            @Override
            public int compare(final Double d1, final Double d2) {
                return (int) Math.signum(d2 - d1);
            }
        });

        if (Logf.DEBUGF && parent.getChildDegree() > 0) {
            for (final NodeBase child : parent.getChildren()) {
                Logf.printf("[%d]", child.id);
            }
            Logf.printf("\n");
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
            entry.getValue().index = sortedIndex;

            sortedIndex++;
        }
    }

    int getNumActiveModules() {
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
            Logf.printf("  %s %s\n", prefix, node);
        }
    }
}
