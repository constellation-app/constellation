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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeFactoryBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.PartitionQueue;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow.Connection;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow.FlowNetwork;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow.Network;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.tree.TreeData;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Resizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author algol
 */
public abstract class InfomapBase {
    
    private static final Logger LOGGER = Logger.getLogger(InfomapBase.class.getName());

    protected final Config config;
    protected final GraphReadMethods rg;

    protected TreeData treeData;

    // Points either to m_nonLeafActiveNetwork or m_treeData.m_leafNodes
    protected List<NodeBase> activeNetwork;
    protected List<Integer> moveTo;

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

    private final List<NodeBase> nonLeafActiveNetwork;

    private static final String NINE_FORMAT1 = "%.9f, ";
    private static final String NINE_FORMAT2 = "%.9f]";
    private static final String NINE_FORMAT3 = " (sum: %.9f)%n";
    private static final String FOUR_FORMAT = "%.4f%% ";
    private static final long SEED_TO_RANDOM_NUMBER_GENERATOR = 123;
    
    protected static final double MIN_CODE_LENGTH_IMPROVEMENT = 1e-10;
    
    protected Random rand;

    protected InfomapBase(final Config config, final NodeFactoryBase nodeFactory, final GraphReadMethods rg) {
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
        rand = new Random(SEED_TO_RANDOM_NUMBER_GENERATOR);
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
        getRoot().setCodelength(indexCodelength);
        LOGGER.log(Level.INFO, "One-level codelength: {0}", indexCodelength);
        oneLevelCodelength = indexCodelength;
        
        final StringBuilder bestSolutionStatistics = new StringBuilder();

        for (int iTrial = 0; iTrial < config.getNumTrials(); iTrial++) {
            LOGGER.log(Level.INFO, "Attempt: {0}", iTrial + 1);
            iterationCount = 0;

            // First clear existing modular structure.
            while (treeData.getFirstLeaf().getParent() != getRoot()) {
                getRoot().replaceChildrenWithGrandChildren();
            }

            runPartition();

            if (hierarchicalCodelength < bestHierarchicalCodelength) {
                bestHierarchicalCodelength = hierarchicalCodelength;
                bestSolutionStatistics.setLength(0);
                printPerLevelCodelength(bestSolutionStatistics);
            }
        }
    }

    protected void runPartition() {
        hierarchicalCodelength = oneLevelCodelength;
        indexCodelength = oneLevelCodelength;
        moduleCodelength = 0;
        
        final PartitionQueue partitionQueue = new PartitionQueue();

        if (config.getFastHierarchicalSolution() != 0) {
            final int numLevelsCreated = findSuperModulesIterativelyFast(partitionQueue);

            // Print current hierarchical solution.
            if (config.getFastHierarchicalSolution() < 3 && hierarchicalCodelength < bestIntermediateCodelength) {
                bestIntermediateCodelength = hierarchicalCodelength;
                bestIntermediateStatistics = new StringBuilder();
                printPerLevelCodelength(bestIntermediateStatistics);
            }

            if (config.getFastHierarchicalSolution() == 1) {
                deleteSubLevels();
                queueTopModules(partitionQueue);
            } else {
                resetModuleFlowFromLeafNodes();
                partitionQueue.setLevel(numLevelsCreated);
            }
        } else {
            partitionAndQueueNextLevel(partitionQueue);
        }

        if (config.getFastHierarchicalSolution() > 2 || partitionQueue.size() == 0) {
            return;
        }

        LOGGER.log(Level.INFO, "Recursive sub-structure compression: ");

        double sumConsolidatedCodelength = hierarchicalCodelength - partitionQueue.getModuleCodelength();

        while (partitionQueue.size() > 0) {
            final PartitionQueue nextLevelQueue = new PartitionQueue();

            // Partition all modules in the queue and fill up the next level queue.
            processPartitionQueue(partitionQueue, nextLevelQueue);

            final double leftToImprove = partitionQueue.getModuleCodelength();
            sumConsolidatedCodelength += partitionQueue.getIndexCodelength() + partitionQueue.getLeafCodelength();
            final double limitCodelength = sumConsolidatedCodelength + leftToImprove;

            final String formattedString = String.format(FOUR_FORMAT, ((hierarchicalCodelength - limitCodelength) / hierarchicalCodelength) * 100);
            LOGGER.log(Level.INFO, formattedString);

            hierarchicalCodelength = limitCodelength;

            partitionQueue.swap(nextLevelQueue);
        }

        LOGGER.log(Level.INFO, "to codelength {0}", hierarchicalCodelength);
    }

    private double partitionAndQueueNextLevel(final PartitionQueue partitionQueue) {
        return partitionAndQueueNextLevel(partitionQueue, true);
    }

    private double partitionAndQueueNextLevel(final PartitionQueue partitionQueue, final boolean tryIndexing) {
        codelength = getRoot().getCodelength();
        hierarchicalCodelength = codelength;

        if (getNumLeafNodes() == 1) {
            return hierarchicalCodelength;
        }

        // Two-level partition --> index codebook + module codebook.
        partition();

        // Instead of a flat codelength, use the two-level structure found.
        hierarchicalCodelength = codelength;

        if (getNumTopModules() == 1) {
            getRoot().getFirstChild().setCodelength(codelength);
            return hierarchicalCodelength;
        } else if (tryIndexing) {
            tryIndexingIteratively();
        }

        queueTopModules(partitionQueue);

        return hierarchicalCodelength;
    }

    private void queueTopModules(final PartitionQueue partitionQueue) {
        // Add modules to partition queue.
        partitionQueue.setNumNonTrivialModules(getNumNonTrivialTopModules());
        partitionQueue.setFlow(getNodeData(getRoot()).getFlow());
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

        partitionQueue.setNonTrivialFlow(nonTrivialFlow);
        partitionQueue.setIndexCodelength(indexCodelength);
        partitionQueue.setModuleCodelength(moduleCodelength);
    }

    private void tryIndexingIteratively() {
        final boolean verbose = subLevel == 0;

        if (verbose) {
            LOGGER.log(Level.INFO, "Finding");
        }

        double minHierarchicalCodelength = hierarchicalCodelength;
        // Add index codebooks as long as the code gets shorter (and collapse each iteration).
        boolean tryIndexing = true;
        final boolean replaceExistingModules = config.getFastHierarchicalSolution() == 0;
        while (tryIndexing) {
            final InfomapBase superInfomap = getNewInfomapInstance(config, rg);
            superInfomap.reseed(NodeBase.uid());
            superInfomap.subLevel = subLevel + TOP_LEVEL_ADDITION;
            superInfomap.initSuperNetwork(getRoot());
            superInfomap.partition();

            // Break if trivial super structure.
            if (superInfomap.numNonTrivialTopModules == 1 || superInfomap.getNumTopModules() == getNumTopModules()
                    || superInfomap.codelength > indexCodelength - MIN_CODE_LENGTH_IMPROVEMENT) {
                break;
            }

            minHierarchicalCodelength += superInfomap.codelength - indexCodelength;

            if (verbose) {
                LOGGER.log(Level.INFO, "{0}", superInfomap.getNumTopModules());
            }

            // Replace current module structure with the super structure.
            setActiveNetworkFromLeafs();
            initModuleOptimization();

            int i = 0;
            for (final NodeBase node : treeData.getLeaves()) {
                node.setIndex(i++);
            }

            // Collect the super module indices on the leaf nodes.
            final TreeData superTree = superInfomap.treeData;
            final Iterator<NodeBase> superLeafIt = superTree.getLeaves().iterator();
            for (final NodeBase module : getRoot().getChildren()) {
                final int superModuleIndex = superLeafIt.next().getParent().getIndex();
                for (final NodeBase node : module.getChildren()) {
                    moveTo.set(node.getIndex(), superModuleIndex);
                }
            }

            // Move the leaf nodes to the modules collected above.
            moveNodesToPredefinedModules();

            // Replace the old modular structure with the super structure generated above.
            consolidateModules(replaceExistingModules);

            tryIndexing = numNonTrivialTopModules > 1 && getNumTopModules() != getNumLeafNodes();
        }

        if (verbose) {
            LOGGER.log(Level.INFO, "Super modules with estimated codelength {0}", minHierarchicalCodelength);
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
            LOGGER.log(Level.INFO, "Index module compression: ");
        }
        
        int numLevelsCreated = 0;

        boolean isLeafLevel = treeData.getFirstLeaf().getParent().equals(getRoot());

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

            boolean acceptSolution = codelength < oldIndexLength - MIN_CODE_LENGTH_IMPROVEMENT;

            // Force at least one modular level!
            final boolean acceptByForce = !acceptSolution && numLevelsCreated == 0;
            if (acceptByForce) {
                acceptSolution = true;
            }

            workingHierarchicalCodelength += codelength - oldIndexLength;

            if (verbose && acceptSolution) {
                final String formattedString = String.format("%.2f%% ",
                        ((hierarchicalCodelength - workingHierarchicalCodelength) / hierarchicalCodelength * 100));
                LOGGER.log(Level.INFO, formattedString);
            }

            if (!acceptSolution) {
                indexCodelength = oldIndexLength;
                break;
            }

            // Consolidate the dynamic modules without replacing any existing ones.
            consolidateModules(false);

            hierarchicalCodelength = workingHierarchicalCodelength;

            // Store the individual codelengths on each module.
            for (final NodeBase module : getRoot().getChildren()) {
                module.setCodelength(calcCodelengthFromFlowWithinOrExit(module));
            }

            if (isLeafLevel && config.getFastHierarchicalSolution() > 1) {
                queueTopModules(partitionQueue);
            }
            
            isLeafLevel = false;
            ++numLevelsCreated;

        } while (numNonTrivialTopModules != 1);

        if (verbose) {
            LOGGER.log(Level.INFO, "to codelength {0} in top modules", hierarchicalCodelength);
        }

        return numLevelsCreated;
    }

    private int deleteSubLevels() {
        NodeBase node = treeData.getFirstLeaf();
        int numLevels = 0;
        while (node.getParent() != null) {
            node = node.getParent();
            ++numLevels;
        }

        if (numLevels <= 1) {
            return 0;
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

        resetModuleFlowFromLeafNodes();
        double sumModuleLength = 0;
        for (final NodeBase module : getRoot().getChildren()) {
            module.setCodelength(calcCodelengthFromFlowWithinOrExit(module));
            sumModuleLength += module.getCodelength();
        }
        moduleCodelength = sumModuleLength;
        hierarchicalCodelength = codelength = indexCodelength + moduleCodelength;

        if (subLevel == 0) {
            LOGGER.log(Level.INFO, "Clearing {0} of codelength", codelength);
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
            module.setSubInfomap(null);
            module.setCodelength(calcCodelengthFromFlowWithinOrExit(module));

            // If only trivial substructure is to be found, no need to create infomap instance to find sub-module structures.
            if (module.getChildDegree() <= 2) {
                leafCodelengths[moduleIndex] = module.getCodelength();
                continue;
            }

            final PartitionQueue subQueue = subQueues[moduleIndex];
            subQueue.setLevel(queue.getLevel() + 1);

            final InfomapBase subInfomap = getNewInfomapInstance(config, rg);
            subInfomap.subLevel = subLevel + 1;

            subInfomap.initSubNetwork(module);

            subInfomap.partitionAndQueueNextLevel(subQueue, tryIndexing);

            // If non-trivial substructure is found which improves the codelength, store it on the module.
            final boolean nonTrivialSubstructure = subInfomap.getNumTopModules() > 1
                    && subInfomap.getNumTopModules() < subInfomap.getNumLeafNodes();
            final boolean improvement = nonTrivialSubstructure
                    && (subInfomap.hierarchicalCodelength < module.getCodelength() - MIN_CODE_LENGTH_IMPROVEMENT);

            if (improvement) {
                indexCodelengths[moduleIndex] = subInfomap.indexCodelength;
                moduleCodelengths[moduleIndex] = subInfomap.moduleCodelength;
                module.setSubInfomap(subInfomap);
            } else {
                leafCodelengths[moduleIndex] = module.getCodelength();
                subQueue.setSkip(true);
                // Else use the codelength from the flat substructure
            }
        }

        double sumLeafCodelength = 0.0;
        double sumIndexCodelength = 0.0;
        double sumModuleCodelengths = 0.0;

        int nextLevelSize = 0;
        for (int moduleIndex = 0; moduleIndex < numModules; moduleIndex++) {
            nextLevelSize += subQueues[moduleIndex].isSkip() ? 0 : subQueues[moduleIndex].size();
            sumLeafCodelength += leafCodelengths[moduleIndex];
            sumIndexCodelength += indexCodelengths[moduleIndex];
            sumModuleCodelengths += moduleCodelengths[moduleIndex];
        }

        queue.setIndexCodelength(sumIndexCodelength);
        queue.setLeafCodelength(sumLeafCodelength);
        queue.setModuleCodelength(sumModuleCodelengths);

        // Collect the sub-queues and build the next-level queue.
        nextLevelQueue.setLevel(queue.getLevel() + 1);
        nextLevelQueue.resize(nextLevelSize);
        int nextLevelIndex = 0;
        for (int moduleIndex = 0; moduleIndex < numModules; moduleIndex++) {
            final PartitionQueue subQueue = subQueues[moduleIndex];
            if (!subQueue.isSkip()) {
                for (int subIndex = 0; subIndex < subQueue.size(); ++subIndex) {
                    nextLevelQueue.set(nextLevelIndex++, subQueue.get(subIndex));
                }

                nextLevelQueue.setFlow(nextLevelQueue.getFlow() + subQueue.getFlow());
                nextLevelQueue.setNonTrivialFlow(nextLevelQueue.getNonTrivialFlow() + subQueue.getNonTrivialFlow());
                nextLevelQueue.setNumNonTrivialModules(
                        nextLevelQueue.getNumNonTrivialModules() + subQueue.getNumNonTrivialModules());
            }
        }

        return nextLevelSize > 0;
    }

    private void partition() {
        partition(0, false);
    }

    private void partition(final int recursiveCount, final boolean fast) {
        if (treeData.getFirstLeaf().getParent() != getRoot()) {
            final String log = String.format("Already partitioned with codelength %f in %d modules.%n", codelength, getNumTopModules());
            LOGGER.log(Level.INFO, log);
            return;
        }

        setActiveNetworkFromChildrenOfRoot();

        initConstantInfomapTerms();
        initModuleOptimization();

        mergeAndConsolidateRepeatedly();
        
        if (!fast && config.getTuneIterationLimit() != 1 && getNumTopModules() != getNumLeafNodes()) {
            int tuneIterationCount = 1;
            int coarseTuneLevel = config.getCoarseTuneLevel() - 1;
            boolean doFineTune = true;
            while (getNumTopModules() > 1) {
                if (doFineTune) {
                    fineTune();
                } else {
                    coarseTune(config.isAlternateCoarseTuneLevel() ? (++coarseTuneLevel % config.getCoarseTuneLevel()) : config.getCoarseTuneLevel() - 1);
                }

                ++tuneIterationCount;
                if (config.getTuneIterationLimit() == tuneIterationCount) {
                    break;
                }

                doFineTune = !doFineTune;
            }
        }

        if (!fast && recursiveCount > 0 && getNumTopModules() != 1 && getNumTopModules() != getNumLeafNodes()) {
            partitionEachModule(recursiveCount - 1);

            // Prepare leaf network to move into the sub-module structure given from partitioning each module.
            setActiveNetworkFromLeafs();
            int i = 0;
            for (final NodeBase leaf : treeData.getLeaves()) {
                moveTo.set(i, leaf.getIndex());
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
                module.setOriginalIndex(packedModuleIndex++);
                module.setIndex(module.getOriginalIndex());
            }
        }
    }

    /**
     * Merge and collapse repeatedly until no code improvement or only one big cluster left.
     */
    private void mergeAndConsolidateRepeatedly() {
        iterationCount++;

        // Force create modules even if worse (don't mix modules and leaf nodes under the same parent).
        consolidateModules();
        int numLevelsConsolidated = 1;

        // Reapply core algorithm on modular network, replacing modules with super modules.
        while (getNumTopModules() > 1 && numLevelsConsolidated != config.getLevelAggregationLimit()) {
            double consolidatedCodelength = codelength;
            double consolidatedIndexLength = indexCodelength;
            double consolidatedModuleLength = moduleCodelength;
            
            setActiveNetworkFromChildrenOfRoot();
            initModuleOptimization();

            // If no improvement, revert codelength terms to the actual structure.
            if (codelength >= consolidatedCodelength - MIN_CODE_LENGTH_IMPROVEMENT) {
                indexCodelength = consolidatedIndexLength;
                moduleCodelength = consolidatedModuleLength;
                codelength = consolidatedCodelength;
                break;
            }

            consolidateModules();
            ++numLevelsConsolidated;
        }
        
        // Set module indices from a zero-based contiguous set.
        int packedModuleIndex = 0;
        for (final NodeBase module : getRoot().getChildren()) {
            module.setOriginalIndex(packedModuleIndex++);
            module.setIndex(module.getOriginalIndex());
        }
    }

    private void fineTune() {
        isCoarseTune = false;
        setActiveNetworkFromLeafs();

        // Init dynamic modules from existing modular structure.
        assert activeNetwork.get(0).getParent().getParent().equals(getRoot());

        int i = 0;
        for (final NodeBase leaf : treeData.getLeaves()) {
            moveTo.set(i, leaf.getParent().getIndex());
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
        if (getNumTopModules() == 1) {
            return;
        }

        isCoarseTune = true;
        partitionEachModule(recursiveCount, config.isFastCoarseTunePartition());

        // Prepare leaf network to move into the sub-module structure given from partitioning each module.
        setActiveNetworkFromLeafs();
        int i = 0;
        for (final NodeBase leaf : treeData.getLeaves()) {
            moveTo.set(i, leaf.getIndex());
            assert moveTo.get(i) < activeNetwork.size();
            i++;
        }

        initModuleOptimization();
        moveNodesToPredefinedModules();

        // Consolidate the sub-modules and store the current module structure in the sub-modules before replacing it.
        consolidateModules(true, true);

        // Prepare the sub-modules to move into the former module structure and begin optimization from there.
        setActiveNetworkFromChildrenOfRoot();
        Resizer.resize(moveTo, activeNetwork.size(), 0);
        i = 0;
        for (final NodeBase subModule : getRoot().getChildren()) {
            moveTo.set(i, subModule.getIndex());
            assert moveTo.get(i) < activeNetwork.size();
            i++;
        }
        initModuleOptimization();
        moveNodesToPredefinedModules();

        mergeAndConsolidateRepeatedly();
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
                    node.setIndex(moduleIndexOffset);
                }

                moduleIndexOffset += 1;
                continue;
            }
            
            final InfomapBase subInfomap = getNewInfomapInstance(config, rg);

            // To not happen to get back the same network with the same seed.
            subInfomap.reseed(NodeBase.uid());
            subInfomap.subLevel = subLevel + 1;
            subInfomap.initSubNetwork(module);
            subInfomap.partition(recursiveCount, fast);
            
            final Iterator<NodeBase> originalLeafNodeIt = module.getChildren().iterator();
            for (final NodeBase node : subInfomap.treeData.getLeaves()) {
                originalLeafNodeIt.next().setIndex(node.getParent().getIndex() + moduleIndexOffset);
            }

            moduleIndexOffset += subInfomap.treeData.getRoot().getChildDegree();
        }
    }

    private void initSubNetwork(final NodeBase parent) {
        cloneFlowData(parent, getRoot());
        generateNetworkFromChildren(parent); // Updates the exitNetworkFlow for the nodes
        getRoot().setChildDegree(getNumLeafNodes());
    }

    private void initSuperNetwork(final NodeBase parent) {
        generateNetworkFromChildren(parent);
        getRoot().setChildDegree(getNumLeafNodes());

        transformNodeFlowToEnterFlow(getRoot());
    }

    private void setActiveNetworkFromChildrenOfRoot() {
        final int numNodes = getRoot().getChildDegree();
        activeNetwork = nonLeafActiveNetwork;
        Resizer.resize(activeNetwork, numNodes, null);
        assert nonLeafActiveNetwork.size() == numNodes;
        int i = 0;
        for (final NodeBase child : getRoot().getChildren()) {
            activeNetwork.set(i, child);

            i++;
        }
    }

    private void setActiveNetworkFromLeafs() {
        activeNetwork = treeData.getLeaves();
        Resizer.resize(moveTo, activeNetwork.size(), 0);
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
            treeData.addEdge(c.getSource(), c.getTarget(), c.getWeight(), c.getFlow());
        }

        initEnterExitFlow();

        return true;
    }

    protected void sortTree() {
        sortTree(getRoot());
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

    private void printPerLevelCodelength(final StringBuilder buf) {
        final List<Double> indexLengths = new ArrayList<>();
        final List<Double> leafLengths = new ArrayList<>();
        aggregatePerLevelCodelength(indexLengths, leafLengths);

        final int numLevels = leafLengths.size();
        Resizer.resize(indexLengths, numLevels, 0D);

        buf.append("Per level codelength for modules:    [");
        for (int i = 0; i < numLevels - 1; ++i) {
            buf.append(String.format(NINE_FORMAT1, indexLengths.get(i)));
        }
        buf.append(String.format(NINE_FORMAT2, indexLengths.get(numLevels - 1)));

        double sumIndexLengths = 0.0;
        for (int i = 0; i < numLevels; ++i) {
            sumIndexLengths += indexLengths.get(i);
        }
        buf.append(String.format(NINE_FORMAT3, sumIndexLengths));

        buf.append("Per level codelength for leaf nodes: [");
        for (int i = 0; i < numLevels - 1; ++i) {
            buf.append(String.format(NINE_FORMAT1, leafLengths.get(i)));
        }
        buf.append(String.format(NINE_FORMAT2, leafLengths.get(numLevels - 1)));

        double sumLeafLengths = 0.0;
        for (int i = 0; i < numLevels; ++i) {
            sumLeafLengths += leafLengths.get(i);
        }
        buf.append(String.format(NINE_FORMAT3, sumLeafLengths));

        final double[] codelengths = new double[leafLengths.size()];
        for (int i = 0; i < codelengths.length; i++) {
            codelengths[i] = leafLengths.get(i);
        }
        for (int i = 0; i < numLevels; ++i) {
            codelengths[i] += indexLengths.get(i);
        }
        buf.append("Per level codelength total:          [");
        for (int i = 0; i < numLevels - 1; ++i) {
            buf.append(String.format(NINE_FORMAT1, codelengths[i]));
        }
        buf.append(String.format(NINE_FORMAT2, codelengths[codelengths.length - 1]));

        double sumCodelengths = 0.0;
        for (int i = 0; i < numLevels; ++i) {
            sumCodelengths += codelengths[i];
        }
        buf.append(String.format(NINE_FORMAT3, sumCodelengths));
    }

    private void aggregatePerLevelCodelength(final List<Double> indexLengths,
            final List<Double> leafLengths) {
        aggregatePerLevelCodelength(indexLengths, leafLengths, 0);
    }

    private void aggregatePerLevelCodelength(final List<Double> indexLengths,
            final List<Double> leafLengths, final int level) {
        aggregatePerLevelCodelength(getRoot(), indexLengths, leafLengths, level);
    }

    private void aggregatePerLevelCodelength(final NodeBase parent, final List<Double> indexLengths,
            final List<Double> leafLengths, final int level) {
        if (indexLengths.size() < level + 1) {
            Resizer.resize(indexLengths, level + 1, 0D);
        }
        if (leafLengths.size() < level + 2) {
            Resizer.resize(leafLengths, level + 2, 0D);
        }
        indexLengths.set(level, indexLengths.get(level) + (parent.isRoot() ? indexCodelength : parent.getCodelength()));

        for (final NodeBase module : parent.getChildren()) {
            if (module.getSubInfomap() != null) {
                module.getSubInfomap().aggregatePerLevelCodelength(indexLengths, leafLengths, level + 1);
            } else if (!module.isLeaf()) {
                if (module.getFirstChild().isLeaf()) {
                    leafLengths.set(level + 1, leafLengths.get(level + 1) + module.getCodelength());
                } else {
                    aggregatePerLevelCodelength(module, indexLengths, leafLengths, level + 1);
                }
            }
        }
    }

    private void reseed(final long seed) {
        rand.setSeed(seed);
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

    /**
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

    protected abstract double calcCodelengthFromFlowWithinOrExit(final NodeBase parent);

    protected abstract void generateNetworkFromChildren(final NodeBase parent);

    protected abstract boolean hasDetailedBalance();

    protected abstract void transformNodeFlowToEnterFlow(final NodeBase parent);

    protected abstract void cloneFlowData(final NodeBase source, final NodeBase target);
    
    protected abstract void sortTree(final NodeBase parent);
    
    @Override
    public String toString() {
        return String.format("[%s: %s]", this.getClass().getName(), rg);
    }
}
