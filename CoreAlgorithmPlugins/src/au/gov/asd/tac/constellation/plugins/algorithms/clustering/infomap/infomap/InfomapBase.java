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

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeFactoryBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.PartitionQueue;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow.Connection;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow.FlowNetwork;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow.Network;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.tree.TreeData;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Lcg;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Logf;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Resizer;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import org.openide.util.Exceptions;

/**
 *
 * @author algol
 */
public abstract class InfomapBase {

    protected static final boolean DEBUG = false;

    protected final Config config;
    protected final GraphReadMethods rg;

    protected TreeData treeData;

    // Points either to m_nonLeafActiveNetwork or m_treeData.m_leafNodes
    protected ArrayList<NodeBase> activeNetwork;
    protected ArrayList<Integer> moveTo;

    protected double oneLevelCodelength;
    protected double codelength;
    protected double indexCodelength;
    protected boolean isCoarseTune;
    protected int iterationCount;
    protected int numNonTrivialTopModules;
    protected int subLevel;
    protected static final int TOP_LEVEL_ADDITION = 1 << 20;

    protected double hierarchicalCodelength;
    protected double moduleCodelength;
    protected double bestHierarchicalCodelength;
    protected double bestIntermediateCodelength;

    protected StringBuilder bestIntermediateStatistics;

    private final ArrayList<NodeBase> nonLeafActiveNetwork;

    // Use a custom random number generator so we can compare the results to the C++ code.
    //    protected Random rand;
    protected Lcg rand;

    public InfomapBase(final Config config, final NodeFactoryBase nodeFactory, final GraphReadMethods rg) {
        this.config = config;
        this.rg = rg;

        treeData = new TreeData(nodeFactory);
        nonLeafActiveNetwork = new ArrayList<>();
        activeNetwork = nonLeafActiveNetwork;
        moveTo = new ArrayList<>();
        isCoarseTune = false;
        iterationCount = 0;
        numNonTrivialTopModules = 0;

        bestHierarchicalCodelength = Double.MAX_VALUE;
        bestIntermediateCodelength = Double.MAX_VALUE;

//        rand = new Random();
        rand = new Lcg();
        rand.seed(config.seedToRandomNumberGenerator);
    }

    /**
     * Public access to the tree data.
     *
     * @return the {@link TreeData} for this run of the algorithm.
     */
    public TreeData getTreeData() {
        return treeData;
    }

    public void run() {
        if (!initNetwork()) {
            return;
        }

        indexCodelength = calcCodelengthFromFlowWithinOrExit(getRoot());
        getRoot().codelength = indexCodelength;
        Logf.printf("One-level codelength: %.6f\n", indexCodelength);
        oneLevelCodelength = indexCodelength;

        final double[] codelengths = new double[config.numTrials];
        final StringBuilder bestSolutionStatistics = new StringBuilder();

        for (int iTrial = 0; iTrial < config.numTrials; iTrial++) {
            Logf.printf("\nAttempt %d/%d\n", iTrial + 1, config.numTrials);
            iterationCount = 0;

            // First clear existing modular structure.
            while (treeData.getFirstLeaf().parent != getRoot()) {
                getRoot().replaceChildrenWithGrandChildren();
            }

            if (config.clusterDataFile != null) {
                throw new UnsupportedOperationException("Not supported.");
            }

            if (!config.noInfomap) {
                runPartition();
            }

            codelengths[iTrial] = hierarchicalCodelength;

            if (hierarchicalCodelength < bestHierarchicalCodelength) {
                bestHierarchicalCodelength = hierarchicalCodelength;
                bestSolutionStatistics.setLength(0);
                printNetworkData(false);
                printPerLevelCodelength(bestSolutionStatistics);
            }
        }

        if (Logf.DEBUGF) {
            Logf.printf("\n\n");
            if (config.numTrials > 1) {
                double averageCodelength = 0;
                double minCodelength = codelengths[0];
                double maxCodelength = 0;
                Logf.printf("Codelengths for %d trials: [", config.numTrials);
                for (final double mdl : codelengths) {
                    Logf.printf("%.9f, ", mdl);
                    averageCodelength += mdl;
                    minCodelength = Math.min(minCodelength, mdl);
                    maxCodelength = Math.max(maxCodelength, mdl);
                }

                averageCodelength /= config.numTrials;
                Logf.printf("\b\b]\n");
                Logf.printf("[min, average, max] codelength: [%.9f, %.9f, %.9f]\n\n",
                        minCodelength, averageCodelength, maxCodelength);
            }

            if (bestIntermediateStatistics != null) {
                Logf.printf("Best intermediate solution:\n");
                Logf.printf("%s\n\n", bestIntermediateStatistics.toString());
            }

            Logf.printf("Best end solution:\n%s\n", bestSolutionStatistics.toString());
        }
    }

    public void runPartition() {
        hierarchicalCodelength = oneLevelCodelength;
        indexCodelength = oneLevelCodelength;
        moduleCodelength = 0;

        if (config.twoLevel) {
            partition();
            hierarchicalCodelength = codelength;
            for (final NodeBase module : getRoot().getChildren()) {
                module.codelength = calcCodelengthFromFlowWithinOrExit(module);
            }

            return;
        }

        final PartitionQueue partitionQueue = new PartitionQueue();

        if (config.fastHierarchicalSolution != 0) {
            final int numLevelsCreated = findSuperModulesIterativelyFast(partitionQueue);

            // Print current hierarchical solution.
            if (config.fastHierarchicalSolution < 3 && hierarchicalCodelength < bestIntermediateCodelength) {
                bestIntermediateCodelength = hierarchicalCodelength;
                bestIntermediateStatistics = new StringBuilder();
                printPerLevelCodelength(bestIntermediateStatistics);
                if (config.networkFile != null) {
                    printNetworkData(new File(config.networkFile).getName() + "_fast");
                }
            }

            if (config.fastHierarchicalSolution == 1) {
                deleteSubLevels();
                queueTopModules(partitionQueue);
            } else {
                resetModuleFlowFromLeafNodes();
                partitionQueue.level = numLevelsCreated;
            }
        } else {
            partitionAndQueueNextLevel(partitionQueue);
        }

        if (config.fastHierarchicalSolution > 2 || partitionQueue.size() == 0) {
            return;
        }

        // TODO: write out initial codelength (two-level/hierarchical) on which the compression rate depends.
        if (config.verbosity == 0) {
            Logf.printf("\nRecursive sub-structure compression: ");
        } else {
            System.out.printf("Current codelength: %f + %f = %f\n",
                    indexCodelength, hierarchicalCodelength - indexCodelength, hierarchicalCodelength);
            System.out.printf("\nTrying to find deeper structure under current modules recursively... \n");
        }

        double sumConsolidatedCodelength = hierarchicalCodelength - partitionQueue.moduleCodelength;

        while (partitionQueue.size() > 0) {
            if (config.verbosity > 0) {
                System.out.printf("Level %d: %f%% of the flow in %d modules. Partitioning... ",
                        partitionQueue.level, partitionQueue.flow * 100, partitionQueue.size());
            }

            final PartitionQueue nextLevelQueue = new PartitionQueue();

            // Partition all modules in the queue and fill up the next level queue.
            processPartitionQueue(partitionQueue, nextLevelQueue);

            final double leftToImprove = partitionQueue.moduleCodelength;
            sumConsolidatedCodelength += partitionQueue.indexCodelength + partitionQueue.leafCodelength;
            final double limitCodelength = sumConsolidatedCodelength + leftToImprove;

            if (config.verbosity == 0) {
                Logf.printf("%.4f%% ", ((hierarchicalCodelength - limitCodelength) / hierarchicalCodelength) * 100);
            } else {
                System.out.printf("done! Codelength: %f + %f (+ %f left to improve) -> limit: %.10f bits.\n",
                        partitionQueue.indexCodelength, partitionQueue.leafCodelength, leftToImprove, limitCodelength);
            }

            hierarchicalCodelength = limitCodelength;

            partitionQueue.swap(nextLevelQueue);
        }

        if (config.verbosity == 0) {
            Logf.printf("to codelength %f\n", hierarchicalCodelength);
        } else {
            System.out.printf("\n");
        }
    }

    private double partitionAndQueueNextLevel(final PartitionQueue partitionQueue) {
        return partitionAndQueueNextLevel(partitionQueue, true);
    }

    private double partitionAndQueueNextLevel(final PartitionQueue partitionQueue, final boolean tryIndexing) {
        if (DEBUG) {
            System.out.printf("%s.hierarchicalPartition(%s)...\n", getClass().getSimpleName(), tryIndexing);
        }

        codelength = getRoot().codelength;
        hierarchicalCodelength = codelength;

        if (getNumLeafNodes() == 1) {
            return hierarchicalCodelength;
        }

        // Two-level partition --> index codebook + module codebook.
        partition();

        // Instead of a flat codelength, use the two-level structure found.
        hierarchicalCodelength = codelength;

        if (getNumTopModules() == 1) {
            getRoot().firstChild.codelength = codelength;
            return hierarchicalCodelength;
        } else if (tryIndexing) {
            tryIndexingIteratively();
        }

        queueTopModules(partitionQueue);

        return hierarchicalCodelength;
    }

    private void queueTopModules(final PartitionQueue partitionQueue) {
        // Add modules to partition queue.
        partitionQueue.numNonTrivialModules = getNumNonTrivialTopModules();
        partitionQueue.flow = getNodeData(getRoot()).getFlow();
        partitionQueue.resize(getRoot().getChildDegree());
        double nonTrivialFlow = 0;
        int moduleIndex = 0;
        for (final NodeBase module : getRoot().getChildren()) {
            partitionQueue.set(moduleIndex, module);
            if (module.getChildDegree() > 1) {
                nonTrivialFlow += getNodeData(module).getFlow();
            }

            moduleIndex++;
        }

        partitionQueue.nonTrivialFlow = nonTrivialFlow;
        partitionQueue.indexCodelength = indexCodelength;
        partitionQueue.moduleCodelength = moduleCodelength;
    }

    private void tryIndexingIteratively() {
        if (DEBUG) {
            System.out.printf("%s.tryIndexingIteratively\n", getClass().getSimpleName());
        }

        int numIndexingCompleted = 0;
        final boolean verbose = subLevel == 0;

        if (verbose) {
            Logf.printf("%s", config.verbosity == 0 ? "Finding " : "\n");
        }

        double minHierarchicalCodelength = hierarchicalCodelength;
        // Add index codebooks as long as the code gets shorter (and collapse each iteration).
        boolean tryIndexing = true;
        final boolean replaceExistingModules = config.fastHierarchicalSolution == 0;
        while (tryIndexing) {
            if (verbose) {
                if (config.verbosity > 0) {
                    System.out.printf("Trying to find super modules... ");
                    if (config.verbosity >= 3) {
                        System.out.printf("\n");
                    }
                }
            }

            final InfomapBase superInfomap = getNewInfomapInstance(config, rg);
//            superInfomap.reseed(getSeedFromCodelength(minHierarchicalCodelength));
            superInfomap.reseed(NodeBase.uid());
            superInfomap.subLevel = subLevel + TOP_LEVEL_ADDITION;
            superInfomap.initSuperNetwork(getRoot());
            superInfomap.partition();

            // Break if trivial super structure.
            if (superInfomap.numNonTrivialTopModules == 1 || superInfomap.getNumTopModules() == getNumTopModules()) {
                if (verbose && config.verbosity > 0) {
                    System.out.printf("failed to find non-trivial super modules.\n");
                }

                break;
            } else if (superInfomap.codelength > indexCodelength - config.minimumCodelengthImprovement) {
                if (verbose && config.verbosity > 0) {
                    System.out.printf("two-level index codebook not improved over one-level.\n");
                }

                break;
            }

            minHierarchicalCodelength += superInfomap.codelength - indexCodelength;

            if (verbose) {
                if (config.verbosity == 0) {
                    System.out.printf("%d ", superInfomap.getNumTopModules());
                } else {
                    System.out.printf("succeeded. Found %d super modules with estimated hierarchical codelength %f.\n",
                            superInfomap.getNumTopModules(), minHierarchicalCodelength);
                }
            }

            // Replace current module structure with the super structure.
            setActiveNetworkFromLeafs();
            initModuleOptimization();

            int i = 0;
            for (final NodeBase node : treeData.getLeaves()) {
                node.index = i++;
            }

            // Collect the super module indices on the leaf nodes.
            final TreeData superTree = superInfomap.treeData;
            final Iterator<NodeBase> superLeafIt = superTree.getLeaves().iterator();
            int leafIndex = 0;
            for (final NodeBase module : getRoot().getChildren()) {
                final int superModuleIndex = superLeafIt.next().parent.index;
                for (final NodeBase node : module.getChildren()) {
                    moveTo.set(node.index, superModuleIndex);
                    leafIndex++;
                }
            }

            // Move the leaf nodes to the modules collected above.
            moveNodesToPredefinedModules();

            // Replace the old modular structure with the super structure generated above.
            consolidateModules(replaceExistingModules);

            numIndexingCompleted++;
            tryIndexing = numNonTrivialTopModules > 1 && getNumTopModules() != getNumLeafNodes();
        }

        if (verbose && config.verbosity == 0) {
            Logf.printf("super modules with estimated codelength %f.", minHierarchicalCodelength);
        }

        hierarchicalCodelength = replaceExistingModules ? codelength : minHierarchicalCodelength;
    }

    /**
     * Like mergeAndConsolidateRepeatedly but let it build up the tree for each
     * new level of aggregation. It doesn't create new Infomap instances.
     */
    private int findSuperModulesIterativelyFast(final PartitionQueue partitionQueue) {
        final boolean verbose = subLevel == 0;

        if (verbose) {
            if (config.verbosity < 2) {
                Logf.printf("Index module compression: ");
            } else {
                Logf.printf("Trying to find fast hierarchy... ");
                if (config.verbosity > 1) {
                    Logf.printf("\n");
                }
            }
        }

        int networkLevel = 0;
        int numLevelsCreated = 0;

        boolean isLeafLevel = treeData.getFirstLeaf().parent.equals(getRoot());
        String nodesLabel = isLeafLevel ? "nodes" : "modules";

        // Add index codebooks as long as the code gets shorter
        do {
            double oldIndexLength = indexCodelength;
            double workingHierarchicalCodelength = hierarchicalCodelength;

            if (isLeafLevel) {
                setActiveNetworkFromLeafs();
            } else {
                setActiveNetworkFromChildrenOfRoot();
                transformNodeFlowToEnterFlow(getRoot());
            }

            initConstantInfomapTerms();
            initModuleOptimization();

            if (verbose && config.verbosity > 1) {
                Logf.printf("Level %d, moving %d %s... ", ++networkLevel, activeNetwork.size(), nodesLabel);
            }

            final int numOptimizationLoops = optimizeModules();

            boolean acceptSolution = codelength < oldIndexLength - config.minimumCodelengthImprovement;

            // Force at least one modular level!
            final boolean acceptByForce = !acceptSolution && numLevelsCreated == 0;
            if (acceptByForce) {
                acceptSolution = true;
            }

            workingHierarchicalCodelength += codelength - oldIndexLength;

            if (verbose) {
                if (config.verbosity < 2) {
                    if (acceptSolution) {
                        Logf.printf("%.2f%% ",
                                ((hierarchicalCodelength - workingHierarchicalCodelength) / hierarchicalCodelength * 100));
                    }
                } else {
                    Logf.printf("found %d modules in %d loops with hierarchical codelength %f + %f = %f%s",
                            getNumDynamicModules(), numOptimizationLoops,
                            indexCodelength, workingHierarchicalCodelength - indexCodelength, workingHierarchicalCodelength,
                            acceptSolution ? "\n" : ", discarding the solution.\n");
                }
            }

            if (!acceptSolution) {
                indexCodelength = oldIndexLength;
                break;
            }

            // Consolidate the dynamic modules without replacing any existing ones.
            consolidateModules(false);

            hierarchicalCodelength = workingHierarchicalCodelength;
            oldIndexLength = indexCodelength;

            // Store the individual codelengths on each module.
            for (final NodeBase module : getRoot().getChildren()) {
                module.codelength = calcCodelengthFromFlowWithinOrExit(module);
            }

            if (isLeafLevel && config.fastHierarchicalSolution > 1) {
                queueTopModules(partitionQueue);
            }

            nodesLabel = "modules";
            isLeafLevel = false;
            ++numLevelsCreated;

        } while (numNonTrivialTopModules != 1);

        if (verbose) {
            if (config.verbosity == 0) {
                Logf.printf("to codelength %f in %d top modules. ", hierarchicalCodelength, getNumTopModules());
            } else {
                Logf.printf("done! Added %d levels with %d top modules to codelength: %f ",
                        numLevelsCreated, getNumTopModules(), hierarchicalCodelength);
            }
        }

        return numLevelsCreated;
    }

    private int deleteSubLevels() {
        NodeBase node = treeData.getFirstLeaf();
        int numLevels = 0;
        while (node.parent != null) {
            node = node.parent;
            ++numLevels;
        }

        if (numLevels <= 1) {
            return 0;
        }

        if (subLevel == 0 && config.verbosity > 0) {
            Logf.printf("Clearing %d levels of sub-modules", numLevels - 1);
        }

        // Clear all sub-modules.
        for (final NodeBase module : getRoot().getChildren()) {
            for (int i = numLevels - 1; i != 0; --i) {
                module.replaceChildrenWithGrandChildren();
            }
        }

        // Reset to leaf-level codelength terms.
        setActiveNetworkFromLeafs();
        initConstantInfomapTerms();

        // recalculateCodelengthFromConsolidatedNetwork();
        resetModuleFlowFromLeafNodes();
        double sumModuleLength = 0;
        for (final NodeBase module : getRoot().getChildren()) {
            module.codelength = calcCodelengthFromFlowWithinOrExit(module);
            sumModuleLength += module.codelength;
        }
        moduleCodelength = sumModuleLength;
        hierarchicalCodelength = codelength = indexCodelength + moduleCodelength;

        if (subLevel == 0) {
            if (config.verbosity == 0) {
                Logf.printf("Clearing sub-modules to codelength %d\n", codelength);
            } else {
                Logf.printf("done! Two-level codelength %f + %f = %f in %d modules.\n",
                        indexCodelength, moduleCodelength, codelength, getNumTopModules());
            }
        }

        return numLevels - 1;
    }

    private boolean processPartitionQueue(final PartitionQueue queue, final PartitionQueue nextLevelQueue) {
        return processPartitionQueue(queue, nextLevelQueue, true);
    }

    private boolean processPartitionQueue(final PartitionQueue queue, final PartitionQueue nextLevelQueue, final boolean tryIndexing) {
        final int numModules = queue.size();
        final double[] indexCodelengths = new double[numModules];
        final double[] moduleCodelengths = new double[numModules];
        final double[] leafCodelengths = new double[numModules];
        final PartitionQueue[] subQueues = new PartitionQueue[numModules];
        for (int i = 0; i < numModules; i++) {
            subQueues[i] = new PartitionQueue();
        }

        // This loop can be parallelised?
        for (int moduleIndex = 0; moduleIndex < numModules; moduleIndex++) {
            final NodeBase module = queue.get(moduleIndex);

            // Delete former sub-structure if exists.
            module.getSubStructure().subInfomap = null;
            module.codelength = calcCodelengthFromFlowWithinOrExit(module);

            // If only trivial substructure is to be found, no need to create infomap instance to find sub-module structures.
            if (module.getChildDegree() <= 2) {
                leafCodelengths[moduleIndex] = module.codelength;
                continue;
            }

            final PartitionQueue subQueue = subQueues[moduleIndex];
            subQueue.level = queue.level + 1;

            final InfomapBase subInfomap = getNewInfomapInstance(config, rg);
            subInfomap.subLevel = subLevel + 1;

            subInfomap.initSubNetwork(module, false);

            subInfomap.partitionAndQueueNextLevel(subQueue, tryIndexing);

            // If non-trivial substructure is found which improves the codelength, store it on the module.
            final boolean nonTrivialSubstructure = subInfomap.getNumTopModules() > 1
                    && subInfomap.getNumTopModules() < subInfomap.getNumLeafNodes();
            final boolean improvement = nonTrivialSubstructure
                    && (subInfomap.hierarchicalCodelength < module.codelength - config.minimumCodelengthImprovement);

            if (improvement) {
                indexCodelengths[moduleIndex] = subInfomap.indexCodelength;
                moduleCodelengths[moduleIndex] = subInfomap.moduleCodelength;
                module.getSubStructure().subInfomap = subInfomap;
            } else {
                leafCodelengths[moduleIndex] = module.codelength;
                module.getSubStructure().exploredWithoutImprovement = true;
                subQueue.skip = true;
                // Else use the codelength from the flat substructure
            }
        }

        double sumLeafCodelength = 0.0;
        double sumIndexCodelength = 0.0;
        double sumModuleCodelengths = 0.0;

        int nextLevelSize = 0;
        for (int moduleIndex = 0; moduleIndex < numModules; moduleIndex++) {
            nextLevelSize += subQueues[moduleIndex].skip ? 0 : subQueues[moduleIndex].size();
            sumLeafCodelength += leafCodelengths[moduleIndex];
            sumIndexCodelength += indexCodelengths[moduleIndex];
            sumModuleCodelengths += moduleCodelengths[moduleIndex];
        }

        queue.indexCodelength = sumIndexCodelength;
        queue.leafCodelength = sumLeafCodelength;
        queue.moduleCodelength = sumModuleCodelengths;

        // Collect the sub-queues and build the next-level queue.
        nextLevelQueue.level = queue.level + 1;
        nextLevelQueue.resize(nextLevelSize);
        int nextLevelIndex = 0;
        for (int moduleIndex = 0; moduleIndex < numModules; moduleIndex++) {
            final PartitionQueue subQueue = subQueues[moduleIndex];
            if (!subQueue.skip) {
                for (int subIndex = 0; subIndex < subQueue.size(); ++subIndex) {
                    nextLevelQueue.set(nextLevelIndex++, subQueue.get(subIndex));
                }

                nextLevelQueue.flow += subQueue.flow;
                nextLevelQueue.nonTrivialFlow += subQueue.nonTrivialFlow;
                nextLevelQueue.numNonTrivialModules += subQueue.numNonTrivialModules;
            }
        }

        return nextLevelSize > 0;
    }

    private void partition() {
        partition(0, false, true);
    }

    private void partition(final int recursiveCount, final boolean fast) {
        partition(recursiveCount, fast, true);
    }

    private void partition(final int recursiveCount, final boolean fast, final boolean forceConsolidation) {
        if (DEBUG) {
            System.out.printf("%s.partition(%d,%s,%s)\n", getClass().getSimpleName(), recursiveCount, fast, forceConsolidation);
        }

        final boolean verbose = (subLevel == 0 && config.verbosity != 0) || (isSuperLevelOnTopLevel() && config.verbosity == 2);
//        verbose = subLevel==0;
        if (treeData.getFirstLeaf().parent != getRoot()) {
            System.out.printf("Already partitioned with codelength %d in %d modules.\n", codelength, getNumTopModules());

            return;
        }

        setActiveNetworkFromChildrenOfRoot();

        initConstantInfomapTerms();
        initModuleOptimization();

        if (verbose) {
            if (config.verbosity == 0) {
                System.out.printf("Two-level compression: ");
            } else {
                System.out.printf("\nTrying to find modular structure... \n");
                System.out.printf("Initiated to codelength %f + %f = %f in %d modules.\n",
                        indexCodelength, moduleCodelength, codelength, getNumTopModules());
            }
        }

        final double initialCodelength = codelength;

        mergeAndConsolidateRepeatedly(forceConsolidation, fast);

        if (DEBUG) {
            System.out.printf("[codelength, initialCodelength = %f,%f]\n", codelength, initialCodelength);
        }

        if (codelength > initialCodelength) {
            System.out.printf("*");
        }

        double oldCodelength = oneLevelCodelength;
        double compression = (oldCodelength - codelength) / oldCodelength;
        if (verbose && config.verbosity == 0) {
            System.out.printf("%.4f%% ", compression * 100);
        }

        if (!fast && config.tuneIterationLimit != 1 && getNumTopModules() != getNumLeafNodes()) {
            int tuneIterationCount = 1;
            int coarseTuneLevel = config.coarseTuneLevel - 1;
            boolean doFineTune = true;
            oldCodelength = codelength;
            while (getNumTopModules() > 1) {
                if (doFineTune) {
                    fineTune();
                    if (codelength > oldCodelength - initialCodelength * config.minimumRelativeTuneIterationImprovement
                            || codelength > oldCodelength - config.minimumCodelengthImprovement) {
                        break;
                    }

                    compression = (oldCodelength - codelength) / oldCodelength;
                    if (verbose && config.verbosity == 0) {
                        System.out.printf("%.4f%% ", compression * 100);
                    }

                    oldCodelength = codelength;
                } else {
                    coarseTune(config.alternateCoarseTuneLevel ? (++coarseTuneLevel % config.coarseTuneLevel)
                            : config.coarseTuneLevel - 1);
                    if (codelength > oldCodelength - initialCodelength * config.minimumRelativeTuneIterationImprovement
                            || codelength > oldCodelength - config.minimumCodelengthImprovement) {
                        break;
                    }
                    compression = (oldCodelength - codelength) / oldCodelength;
                    if (verbose && config.verbosity == 0) {
                        System.out.printf("%.4f%% ", compression * 100);
                    }

                    oldCodelength = codelength;
                }

                ++tuneIterationCount;
                if (config.tuneIterationLimit == tuneIterationCount) {
                    break;
                }

                doFineTune = !doFineTune;
            }
        }

        if (verbose) {
            if (config.verbosity == 0) {
                final String fmt = String.format("%s.%df", "%", config.verboseNumberPrecision);
                System.out.printf("to %d modules with codelength " + fmt + "\n", getNumTopModules(), codelength);
            } else {
                System.out.printf("Two-level codelength: %f + %f = %f\n", indexCodelength, moduleCodelength, codelength);
            }
        }

        if (!fast && recursiveCount > 0 && getNumTopModules() != 1 && getNumTopModules() != getNumLeafNodes()) {
            partitionEachModule(recursiveCount - 1);

            // Prepare leaf network to move into the sub-module structure given from partitioning each module.
            setActiveNetworkFromLeafs();
            int i = 0;
            for (final NodeBase leaf : treeData.getLeaves()) {
                moveTo.set(i, leaf.index);
                assert moveTo.get(i) < activeNetwork.size();
                i++;
            }

            initModuleOptimization();
            moveNodesToPredefinedModules();

            // Consolidate the sub-modules and store the current module structure in the sub-modules before replacing it.
            consolidateModules(true, true);

            // Set module indices from a zero-based contiguous set.
            int packedModuleIndex = 0;
            for (final NodeBase module : getRoot().getChildren()) {
                module.index = module.originalIndex = packedModuleIndex++;
            }
        }
    }

    private void mergeAndConsolidateRepeatedly() {
        mergeAndConsolidateRepeatedly(false, false);
    }

    private void mergeAndConsolidateRepeatedly(final boolean forceConsolidation) {
        mergeAndConsolidateRepeatedly(forceConsolidation, false);
    }

    private void mergeAndConsolidateRepeatedly(final boolean forceConsolidation, final boolean fast) {
        if (DEBUG) {
            System.out.printf("%s.mergeAndConsolidateRepeatedly(%s,%s)\n", getClass().getSimpleName(), forceConsolidation, fast);
        }

        iterationCount++;
        final boolean verbose = (subLevel == 0 && config.verbosity != 0)
                || (isSuperLevelOnTopLevel() && config.verbosity >= 3);
        // Merge and collapse repeatedly until no code improvement or only one big cluster left.
        if (verbose) {
            System.out.printf("Iteration %d, moving %d*", iterationCount, activeNetwork.size());
        }

        // Core loop, merging modules.
        int numOptimizationLoops = optimizeModules();

        if (verbose) {
            System.out.printf("%d, ", numOptimizationLoops);
        }

        // Force create modules even if worse (don't mix modules and leaf nodes under the same parent).
        consolidateModules();
        int numLevelsConsolidated = 1;

        // Reapply core algorithm on modular network, replacing modules with super modules.
        while (getNumTopModules() > 1 && numLevelsConsolidated != config.levelAggregationLimit) {
            double consolidatedCodelength = codelength;
            double consolidatedIndexLength = indexCodelength;
            double consolidatedModuleLength = moduleCodelength;

            if (verbose) {
                System.out.printf("%d*", getNumTopModules());
            }

            setActiveNetworkFromChildrenOfRoot();
            initModuleOptimization();
            numOptimizationLoops = optimizeModules();

            if (verbose) {
                System.out.printf("%d, ", numOptimizationLoops);
            }

            // If no improvement, revert codelength terms to the actual structure.
            if (!(codelength < consolidatedCodelength - config.minimumCodelengthImprovement)) {
                indexCodelength = consolidatedIndexLength;
                moduleCodelength = consolidatedModuleLength;
                codelength = consolidatedCodelength;
                break;
            }

            consolidateModules();
            ++numLevelsConsolidated;
        }

        if (verbose) {
            System.out.printf("%s*loops to codelength %.6f in %d modules. (%d non-trivial modules)\n",
                    isCoarseTune ? "modules" : "nodes", codelength, getNumTopModules(), numNonTrivialTopModules);
        }

        // Set module indices from a zero-based contiguous set.
        int packedModuleIndex = 0;
        for (final NodeBase module : getRoot().getChildren()) {
            module.index = module.originalIndex = packedModuleIndex++;
        }
    }

    private void fineTune() {
        if (DEBUG) {
            System.out.printf("%s.fineTune(%s)\n", getClass().getSimpleName(), getRoot());
        }

        isCoarseTune = false;
        setActiveNetworkFromLeafs();

        // Init dynamic modules from existing modular structure.
        assert activeNetwork.get(0).parent.parent.equals(getRoot());

        int i = 0;
        for (final NodeBase leaf : treeData.getLeaves()) {
            moveTo.set(i, leaf.parent.index);
            assert moveTo.get(i) < activeNetwork.size();
            i++;
        }

        initModuleOptimization();
        moveNodesToPredefinedModules();

        mergeAndConsolidateRepeatedly();
    }

    /**
     * Coarse-tune: 1. Partition each cluster to find optimal modules in each
     * module, i.e. sub modules. 2. Move the leaf-nodes into the sub-module
     * structure. 3. Consolidate the sub-modules. 3a.	Consolidate the
     * sub-modules under their modules in the tree. 3b.	Store their module index
     * and delete the top module level. 4. Move the sub-modules into the former
     * module structure. 5. Optimize by trying to move and merge sub-modules. 6.
     * Consolidate the result.
     */
    private void coarseTune(final int recursiveCount) {
        if (DEBUG) {
            System.out.printf("%s.coarseTune(%s)\n", getClass().getSimpleName(), getRoot());
        }

        if (getNumTopModules() == 1) {
            return;
        }

        isCoarseTune = true;
        partitionEachModule(recursiveCount, config.fastCoarseTunePartition);

        // Prepare leaf network to move into the sub-module structure given from partitioning each module.
        setActiveNetworkFromLeafs();
        int i = 0;
        for (final NodeBase leaf : treeData.getLeaves()) {
            moveTo.set(i, leaf.index);
            assert moveTo.get(i) < activeNetwork.size();
            i++;
        }

        initModuleOptimization();
        moveNodesToPredefinedModules();

        // Consolidate the sub-modules and store the current module structure in the sub-modules before replacing it.
        consolidateModules(true, true);

        // Prepare the sub-modules to move into the former module structure and begin optimization from there.
        setActiveNetworkFromChildrenOfRoot();
        Resizer.resizeInteger(moveTo, activeNetwork.size(), 0);
        i = 0;
        for (final NodeBase subModule : getRoot().getChildren()) {
            moveTo.set(i, subModule.index);
            assert moveTo.get(i) < activeNetwork.size();
            i++;
        }
        initModuleOptimization();
        moveNodesToPredefinedModules();

        mergeAndConsolidateRepeatedly(true);
    }

    private void partitionEachModule(final int recursiveCount) {
        partitionEachModule(recursiveCount, false);
    }

    /**
     * For each module, create a new infomap instance and clone the interior
     * structure of the module as the new network to partition. Collect the
     * results by populating the index member of each leaf node with the
     * sub-module structure found by partitioning each module.
     */
    private void partitionEachModule(final int recursiveCount, final boolean fast) {
        int moduleIndexOffset = 0;
        for (final NodeBase module : getRoot().getChildren()) {
            // If only one child in the module, no need to create infomap instance to find sub-module structures.
            if (module.getChildDegree() == 1) {
                for (final NodeBase node : module.getChildren()) {
                    node.index = moduleIndexOffset;
                }

                moduleIndexOffset += 1;
                continue;
            }

            if (DEBUG) {
                System.out.printf(">>>>>>>>>>>>>>>>>> RUN SUB_INFOMAP on node n%d with childDegree: %d >>>>>>>>>>>>\n", module.id, module.getChildDegree());
            }

            final InfomapBase subInfomap = getNewInfomapInstance(config, rg);

            // To not happen to get back the same network with the same seed.
            subInfomap.reseed(NodeBase.uid());
            subInfomap.subLevel = subLevel + 1;
            subInfomap.initSubNetwork(module, false);
            subInfomap.partition(recursiveCount, fast);

            if (DEBUG) {
                System.out.printf("<<<<<<<<<<<<<<<<<<< BACK FROM SUB_INFOMAP!!!! <<<<<<<<<<<<<<<<<<<\n");
                System.out.printf("Node n%d with %d leaf-nodes gave %d sub-clusters\n",
                        module.id, subInfomap.treeData.getNumLeafNodes(), subInfomap.treeData.getRoot().getChildDegree());
            }

            final Iterator<NodeBase> originalLeafNodeIt = module.getChildren().iterator();
            for (final NodeBase node : subInfomap.treeData.getLeaves()) {
                originalLeafNodeIt.next().index = node.parent.index + moduleIndexOffset;
            }

            moduleIndexOffset += subInfomap.treeData.getRoot().getChildDegree();
        }
    }

    private void initSubNetwork(final NodeBase parent, final boolean recalculateFlow) {
        if (DEBUG) {
            System.out.printf("%s.initSubNetwork()\n", getClass().getSimpleName());
        }

        cloneFlowData(parent, getRoot());
        generateNetworkFromChildren(parent); // Updates the exitNetworkFlow for the nodes
        getRoot().setChildDegree(getNumLeafNodes());
    }

    private void initSuperNetwork(final NodeBase parent) {
        if (DEBUG) {
            System.out.printf("%s.initSuperNetwork()...\n", getClass().getSimpleName());
        }

        generateNetworkFromChildren(parent);
        getRoot().setChildDegree(getNumLeafNodes());

        transformNodeFlowToEnterFlow(getRoot());
    }

    private void setActiveNetworkFromChildrenOfRoot() {
        if (DEBUG) {
            System.out.printf("%s.setActiveNetworkFromChildrenOfRoot() with child degree %d...\n",
                    getClass().getSimpleName(), getRoot().getChildDegree());
        }

        final int numNodes = getRoot().getChildDegree();
        activeNetwork = nonLeafActiveNetwork;
        Resizer.resizeNodeBase(activeNetwork, numNodes);
        assert nonLeafActiveNetwork.size() == numNodes;
        int i = 0;
        for (final NodeBase child : getRoot().getChildren()) {
            activeNetwork.set(i, child);

            i++;
        }
    }

    private void setActiveNetworkFromLeafs() {
        if (DEBUG) {
            System.out.printf("%s.setActiveNetworkFromLeafs(), numNodes: %d\n", getClass().getSimpleName(), treeData.getNumLeafNodes());
        }

        activeNetwork = treeData.getLeaves();
        Resizer.resizeInteger(moveTo, activeNetwork.size(), 0);
        assert moveTo.size() == treeData.getLeaves().size();
    }

    public boolean initNetwork() {
        final Network network = new Network(config, rg);
        network.read();

        final FlowNetwork flowNetwork = new FlowNetwork();
        flowNetwork.calculateFlow(network, config);

        final double[] nodeFlow = flowNetwork.getNodeFlow();
        final double[] nodeTeleportWeights = flowNetwork.getNodeTeleportRates();
        for (int position = 0; position < network.getNumNodes(); position++) {
            treeData.addNewNode(position, network.getNodeName(position), nodeFlow[position], nodeTeleportWeights[position]);
        }

        final Connection[] connections = flowNetwork.getFlowConnections();
        for (final Connection c : connections) {
            treeData.addEdge(c.source, c.target, c.weight, c.flow);
        }

        initEnterExitFlow();

        return true;
    }

    private void printNetworkData(final boolean sort) {
        try {
            printNetworkData("", sort);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void printNetworkData(final String filename) {
        try {
            printNetworkData(filename, true);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void printNetworkData(String filename, final boolean sort) throws FileNotFoundException {
        if (config.noFileOutput) {
            return;
        }

        if (filename.isEmpty()) {
            final File f = new File(config.networkFile);
            final String name = f.getName();
            final int p = name.lastIndexOf('.');
            filename = p > 0 ? name.substring(0, p) : name;
        }

        if (sort) {
            // Sort tree on flow.
            // Note that this will change the cluster numbering of (for instance) printClusterVector().
            // The clustering will be the same, just the numbers may be different.
            sortTree();
        }
        // Print .tree.
        if (config.printTree) {
            final File outName = new File(config.outDirectory, filename + ".tree");
            if (config.verbosity == 0) {
                Logf.printf("(Writing .tree file..");
            } else {
                System.out.printf("Print hierarchical cluster data to %s... ", outName);
            }

            try (PrintWriter out = new PrintWriter(outName)) {
                out.printf("# Codelength %f bits. Network size: %d nodes and %d links.\n",
                        hierarchicalCodelength, getNumLeafNodes(), treeData.getNumLeafEdges());

                printSubInfomapTree(out, treeData);
            }

            if (config.verbosity == 0) {
                Logf.printf(") ");
            } else {
                System.out.printf("done!\n");
            }
        }

        // Print .clu.
        if (config.printClu) {
            final File outName = new File(config.outDirectory, filename + ".clu");
            if (config.verbosity == 0) {
                Logf.printf("(Writing .clu file.. ) ");
            } else {
                System.out.printf("Print cluster data to %s... ", outName);
            }

            try (PrintWriter out = new PrintWriter(outName)) {
                printClusterVector(out);
            }

            if (config.verbosity > 0) {
                System.out.printf("done!\n");
            }
        }

        if (config.printNodeRanks) {
            final File outName = new File(config.outDirectory, filename + ".rank");
            if (config.verbosity > 0) {
                System.out.printf("Print node ranks to %s...", outName);
            }

            try (PrintWriter out = new PrintWriter(outName)) {
                printNodeRanks(out);
            }

            if (config.verbosity > 0) {
                System.out.printf("done!\n");
            }
        }

        if (config.printFlowNetwork) {
            final File outName = new File(config.outDirectory, filename + ".flow");
            if (config.verbosity == 0) {
                Logf.printf("(Writing .flow file.. ", outName);
            } else {
                System.out.printf("Print flow network to %s... ", outName);
            }

            try (PrintWriter out = new PrintWriter(outName)) {
                printFlowNetwork(out);
            }

            if (config.verbosity == 0) {
                Logf.printf(") ");
            } else {
                System.out.printf("done!\n");
            }
        }
    }

    private void printClusterVector(final PrintWriter out) {
        out.printf("*Vertices %d\n", treeData.getNumLeafNodes());
        int i = 0;
        for (final NodeBase node : treeData.getLeaves()) {
            final int index = node.parent.index;
            out.printf("%d %d %d\n", i++, node.originalIndex, index + 1);
        }
    }

    /**
     * Return an int[] such that int[i] is the cluster number of the vertex at
     * position i.
     *
     * @return the cluster vector.
     */
    public int[] getClusterVector() {
        final int[] clusters = new int[treeData.getNumLeafNodes()];
        int i = 0;
        for (final NodeBase node : treeData.getLeaves()) {
            final int index = node.parent.index;
            clusters[i++] = index;
        }

        return clusters;
    }

    protected void sortTree() {
        sortTree(getRoot());
    }

    private void printSubInfomapTree(final PrintWriter out, final TreeData originalData) {
        printSubInfomapTree(out, originalData, "");
    }

    private void printSubInfomapTree(final PrintWriter out, final TreeData originalData, final String prefix) {
        int moduleIndex = 0;
        for (final NodeBase module : getRoot().getChildren()) {
            final String subPrefix = String.format("%s%d:", prefix, moduleIndex);
            if (module.getSubInfomap() == null) {
                int nodeIndex = 0;
                for (final NodeBase child : module.getChildren()) {
                    out.printf("%s%d %s (%d)\n", subPrefix, nodeIndex, originalData.getLeafNode(child.originalIndex), child.originalIndex);

                    nodeIndex++;
                }
            } else {
                module.getSubInfomap().printSubInfomapTree(out, originalData, subPrefix);
            }

            moduleIndex++;
        }
    }

    public NodeBase getRoot() {
        return treeData.getRoot();
    }

    protected int getNumNonTrivialTopModules() {
        return numNonTrivialTopModules;
    }

    protected int getNumTopModules() {
        return treeData.getRoot().getChildDegree();
    }

    protected int getNumLeafNodes() {
        return treeData.getNumLeafNodes();
    }

    protected boolean isTopLevel() {
        return (subLevel & (TOP_LEVEL_ADDITION - 1)) == 0;
    }

    protected boolean isSuperLevelOnTopLevel() {
        return subLevel == TOP_LEVEL_ADDITION;
    }

    private void printPerLevelCodelength(final StringBuilder buf) {
        final ArrayList<Double> indexLengths = new ArrayList<>();
        final ArrayList<Double> leafLengths = new ArrayList<>();
        aggregatePerLevelCodelength(indexLengths, leafLengths);

        final int numLevels = leafLengths.size();
        Resizer.resizeDouble(indexLengths, numLevels, 0);

        buf.append("Per level codelength for modules:    [");
        for (int i = 0; i < numLevels - 1; ++i) {
            buf.append(String.format("%.9f, ", indexLengths.get(i)));
        }
        buf.append(String.format("%.9f]", indexLengths.get(numLevels - 1)));

        double sumIndexLengths = 0.0;
        for (int i = 0; i < numLevels; ++i) {
            sumIndexLengths += indexLengths.get(i);
        }
        buf.append(String.format(" (sum: %.9f)\n", sumIndexLengths));

        buf.append("Per level codelength for leaf nodes: [");
        for (int i = 0; i < numLevels - 1; ++i) {
            buf.append(String.format("%.9f, ", leafLengths.get(i)));
        }
        buf.append(String.format("%.9f]", leafLengths.get(numLevels - 1)));

        double sumLeafLengths = 0.0;
        for (int i = 0; i < numLevels; ++i) {
            sumLeafLengths += leafLengths.get(i);
        }
        buf.append(String.format(" (sum: %.9f)\n", sumLeafLengths));

        final double[] codelengths = new double[leafLengths.size()];
        for (int i = 0; i < codelengths.length; i++) {
            codelengths[i] = leafLengths.get(i);
        }
        for (int i = 0; i < numLevels; ++i) {
            codelengths[i] += indexLengths.get(i);
        }
        buf.append("Per level codelength total:          [");
        for (int i = 0; i < numLevels - 1; ++i) {
            buf.append(String.format("%.9f, ", codelengths[i]));
        }
        buf.append(String.format("%.9f]", codelengths[numLevels - 1]));

        double sumCodelengths = 0.0;
        for (int i = 0; i < numLevels; ++i) {
            sumCodelengths += codelengths[i];
        }
        buf.append(String.format(" (sum: %.9f)\n", sumCodelengths));
    }

    private void aggregatePerLevelCodelength(final ArrayList<Double> indexLengths,
            final ArrayList<Double> leafLengths) {
        aggregatePerLevelCodelength(indexLengths, leafLengths, 0);
    }

    private void aggregatePerLevelCodelength(final ArrayList<Double> indexLengths,
            final ArrayList<Double> leafLengths, final int level) {
        aggregatePerLevelCodelength(getRoot(), indexLengths, leafLengths, level);
    }

    private void aggregatePerLevelCodelength(final NodeBase parent, final ArrayList<Double> indexLengths,
            final ArrayList<Double> leafLengths, final int level) {
        if (indexLengths.size() < level + 1) {
            Resizer.resizeDouble(indexLengths, level + 1, 0);
        }
        if (leafLengths.size() < level + 2) {
            Resizer.resizeDouble(leafLengths, level + 2, 0);
        }
        indexLengths.set(level, indexLengths.get(level) + (parent.isRoot() ? indexCodelength : parent.codelength));

        for (final NodeBase module : parent.getChildren()) {
            if (module.getSubInfomap() != null) {
                module.getSubInfomap().aggregatePerLevelCodelength(indexLengths, leafLengths, level + 1);
            } else if (!module.isLeaf()) {
                if (module.firstChild.isLeaf()) {
                    leafLengths.set(level + 1, leafLengths.get(level + 1) + module.codelength);

                    final String f = leafLengths.get(level) == 0 ? "0" : String.format("%.6f", leafLengths.get(level));
                } else {
                    aggregatePerLevelCodelength(module, indexLengths, leafLengths, level + 1);
                }
            }
        }
    }

    private void reseed(long seed) {
        rand.seed(seed);
    }

    /**
     * Take the non-empty dynamic modules from the optimization of the active
     * network and create module nodes to insert above the active network in the
     * tree. Also aggregate the links from the active network to inter-module
     * links in the new modular network.
     *
     * @param replaceExistingStructure If true, it doesn't add any depth to the
     * tree but replacing either the existing modular parent structure (if
     * <code>asSubModules</code> is true) or the active network itself if it's
     * not the leaf level, in which case it will add a level of depth to the
     * tree anyway.
     *
     * @param asSubModules Set to true to consolidate the dynamic modules as
     * submodules under existing modules, and store existing parent structure on
     * the index member of the submodules. Presupposes that the active network
     * already have a modular parent structure, and that the dynamic structure
     * that will be consolidated actually contains nothing but strict
     * sub-structures of the existing modules, i.e. that the index property of
     * two nodes with different parent must be different. Presumably the
     * sub-module structure initiated on the active network is found by
     * partitioning each existing module.
     *
     * @return The number of created modules
     */
    protected abstract int consolidateModules(final boolean replaceExistingStructure, final boolean asSubModules);

    protected int consolidateModules() {
        return consolidateModules(true, false);
    }

    protected int consolidateModules(final boolean replaceExistingStructure) {
        return consolidateModules(replaceExistingStructure, false);
    }

    protected abstract InfomapBase getNewInfomapInstance(final Config config, final GraphReadMethods rg);

    protected abstract FlowBase getNodeData(final NodeBase node);

    /*
     * Set the exit (and enter) flow on the nodes.
     *
     * Note 1: Each node data has a enterFlow and exitFlow member, but
     * for models with detailed balance (undirected and directed with teleportation)
     * the enterFlow is equal to the exitFlow and declared as a reference to the exitFlow
     * to simplify output methods. Thus, don't add to enterFlow for those cases if it should
     * not add to the exitFlow.
     *
     * Note 2: The enter and exit flow defines what should be coded for in the
     * map equation for the *modular levels*, not the leaf node level.
     *
     * The reason for that special case is to be able to code self-flow (self-links and
     * self-teleportation). If adding self-flow to enter and exit flow though, the enclosing
     * module will also aggregate those initial values, but self-flow should not be seen
     * as exiting the enclosing module, just the node.
     *
     * Instead of having special cases for the flow aggregation to modular levels, take
     * the special case when calculating the codelength, using the flow values rather
     * than the enter/exit flow values for the leaf nodes.
     *
     */
    protected abstract void initEnterExitFlow();

    protected abstract void resetModuleFlowFromLeafNodes();

    protected abstract void initConstantInfomapTerms();

    protected abstract void initModuleOptimization();

    protected abstract void moveNodesToPredefinedModules();

    /**
     * Loop through each node and move it to the module that reduces the total
     * codelength the most. Start over until converged.
     *
     * @return The number of effective optimization rounds, i.e. zero if no move
     * was made.
     */
    protected abstract int optimizeModules();

    protected abstract double calcCodelengthFromFlowWithinOrExit(final NodeBase parent);

    protected abstract void generateNetworkFromChildren(final NodeBase parent);

    protected abstract boolean isDirected();

    protected abstract boolean hasDetailedBalance();

    protected abstract void transformNodeFlowToEnterFlow(final NodeBase parent);

    protected abstract void cloneFlowData(final NodeBase source, final NodeBase target);

    protected abstract void printNodeRanks(final PrintWriter out);

    protected abstract void printFlowNetwork(final PrintWriter out);

    protected abstract void sortTree(final NodeBase parent);

    protected abstract int getNumDynamicModules();

    @Override
    public String toString() {
        return String.format("[%s: %s]", this.getClass().getName(), rg);
    }
}
