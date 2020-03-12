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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io;

/**
 *
 * @author algol
 */
public final class Config {

    public static enum ConnectionType {

        TRANSACTIONS, EDGES, LINKS
    };

    public Config() {
        noInfomap = false;
        noFileOutput = true;
        verbosity = 0;

        teleportationProbability = 0.15;
        selfTeleportationProbability = -1;
        seedToRandomNumberGenerator = 123;
        numTrials = 1;
        minimumCodelengthImprovement = 1e-10;
        minimumRelativeTuneIterationImprovement = 1e-5;
        coarseTuneLevel = 1;
        verboseNumberPrecision = 6;

        connectionType = ConnectionType.LINKS;

        setOptimizationLevel(1);
    }

    /**
     * Set all optimization options at once with different accuracy to
     * performance trade-off.
     *
     * @param level the desired optimization level.
     */
    public void setOptimizationLevel(final int level) {
        switch (level) {
            case 0: // full coarse-tune
                randomizeCoreLoopLimit = false;
                coreLoopLimit = 0;
                levelAggregationLimit = 0;
                tuneIterationLimit = 0;
                minimumRelativeTuneIterationImprovement = 1.0e-6;
                fastCoarseTunePartition = false;
                alternateCoarseTuneLevel = true;
                coarseTuneLevel = 3;
                break;
            case 1: // fast coarse-tune
                randomizeCoreLoopLimit = true;
                coreLoopLimit = 10;
                levelAggregationLimit = 0;
                tuneIterationLimit = 0;
                minimumRelativeTuneIterationImprovement = 1.0e-5;
                fastCoarseTunePartition = true;
                alternateCoarseTuneLevel = false;
                coarseTuneLevel = 1;
                break;
            case 2: // no tuning
                randomizeCoreLoopLimit = true;
                coreLoopLimit = 10;
                levelAggregationLimit = 0;
                tuneIterationLimit = 1;
                fastCoarseTunePartition = true;
                alternateCoarseTuneLevel = false;
                coarseTuneLevel = 1;
                break;
            case 3: // no aggregation nor any tuning
                randomizeCoreLoopLimit = true;
                coreLoopLimit = 10;
                levelAggregationLimit = 1;
                tuneIterationLimit = 1;
                fastCoarseTunePartition = true;
                alternateCoarseTuneLevel = false;
                coarseTuneLevel = 1;
                break;
            default:
                throw new IllegalArgumentException("Optimization level must be in 0..3");
        }
    }

    public boolean isUndirected() {
        return !directed && !undirdir && !outdirdir && !rawdir;
    }

    public boolean haveModularResultOutput() {
        return printTree
                || printMap
                || printClu
                || printBinaryTree
                || printBinaryFlowTree;
    }

    // Input
    public String networkFile;
    public String inputFormat;
    public boolean parseWithoutIOStreams;
    public boolean zeroBasedNodeNumbers;
    public boolean includeSelfLinks;
    public boolean ignoreEdgeWeights;
    public int nodeLimit;
    public String clusterDataFile;
    public boolean noInfomap;

    // Core algorithm
    public boolean twoLevel;
    public boolean directed;
    public boolean undirdir;
    public boolean outdirdir;
    public boolean rawdir;
    public boolean recordedTeleportation;
    public boolean teleportToNodes;
    public double teleportationProbability;
    public double selfTeleportationProbability;
    public long seedToRandomNumberGenerator;

    // Performance and accuracy
    public int numTrials;
    public double minimumCodelengthImprovement;
    public boolean randomizeCoreLoopLimit;
    public int coreLoopLimit;
    public int levelAggregationLimit;
    public int tuneIterationLimit; // num iterations of fine-tune/coarse-tune in two-level partition)
    public double minimumRelativeTuneIterationImprovement;
    public boolean fastCoarseTunePartition;
    public boolean alternateCoarseTuneLevel;
    public int coarseTuneLevel;
    public int fastHierarchicalSolution;

    // Output
    public String outDirectory;
    public boolean printTree;
    public boolean printMap;
    public boolean printClu;
    public boolean printNodeRanks;
    public boolean printFlowNetwork;
    public boolean printPajekNetwork;
    public boolean printBinaryTree;
    public boolean printBinaryFlowTree; // tree including horizontal links (hierarchical network)
    public boolean noFileOutput;
    public int verbosity;
    public int verboseNumberPrecision;
    public boolean benchmark;

    // Custom for Graph.
    public ConnectionType connectionType;
};
