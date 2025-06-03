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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io;

/**
 *
 * @author algol
 */
public final class Config {

    public enum ConnectionType {
        TRANSACTIONS, EDGES, LINKS
    }

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
    private String networkFile;
    private boolean includeSelfLinks;
    private String clusterDataFile;
    private boolean noInfomap;

    // Core algorithm
    private boolean twoLevel;
    private boolean directed;
    private boolean undirdir;
    private boolean outdirdir;
    private boolean rawdir;
    private boolean recordedTeleportation;
    private boolean teleportToNodes;
    private double teleportationProbability;
    private double selfTeleportationProbability;
    private long seedToRandomNumberGenerator;

    // Performance and accuracy
    private int numTrials;
    private double minimumCodelengthImprovement;
    private boolean randomizeCoreLoopLimit;
    private int coreLoopLimit;
    private int levelAggregationLimit;
    private int tuneIterationLimit; // num iterations of fine-tune/coarse-tune in two-level partition)
    private double minimumRelativeTuneIterationImprovement;
    private boolean fastCoarseTunePartition;
    private boolean alternateCoarseTuneLevel;
    private int coarseTuneLevel;
    private int fastHierarchicalSolution;

    // Output
    private String outDirectory;
    private boolean printTree;
    private boolean printMap;
    private boolean printClu;
    private boolean printNodeRanks;
    private boolean printFlowNetwork;
    private boolean printBinaryTree;
    private boolean printBinaryFlowTree; // tree including horizontal links (hierarchical network)
    private boolean noFileOutput;
    private int verbosity;
    private int verboseNumberPrecision;

    // Custom for Graph.
    private ConnectionType connectionType;

    public String getNetworkFile() {
        return networkFile;
    }

    public void setNetworkFile(final String networkFile) {
        this.networkFile = networkFile;
    }

    public boolean isIncludeSelfLinks() {
        return includeSelfLinks;
    }

    public void setIncludeSelfLinks(final boolean includeSelfLinks) {
        this.includeSelfLinks = includeSelfLinks;
    }

    public String getClusterDataFile() {
        return clusterDataFile;
    }

    public void setClusterDataFile(final String clusterDataFile) {
        this.clusterDataFile = clusterDataFile;
    }

    public boolean isNoInfomap() {
        return noInfomap;
    }

    public void setNoInfomap(final boolean noInfomap) {
        this.noInfomap = noInfomap;
    }

    public boolean isTwoLevel() {
        return twoLevel;
    }

    public void setTwoLevel(final boolean twoLevel) {
        this.twoLevel = twoLevel;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(final boolean directed) {
        this.directed = directed;
    }

    public boolean isUndirdir() {
        return undirdir;
    }

    public void setUndirdir(final boolean undirdir) {
        this.undirdir = undirdir;
    }

    public boolean isOutdirdir() {
        return outdirdir;
    }

    public void setOutdirdir(final boolean outdirdir) {
        this.outdirdir = outdirdir;
    }

    public boolean isRawdir() {
        return rawdir;
    }

    public void setRawdir(final boolean rawdir) {
        this.rawdir = rawdir;
    }

    public boolean isRecordedTeleportation() {
        return recordedTeleportation;
    }

    public void setRecordedTeleportation(final boolean recordedTeleportation) {
        this.recordedTeleportation = recordedTeleportation;
    }

    public boolean isTeleportToNodes() {
        return teleportToNodes;
    }

    public void setTeleportToNodes(final boolean teleportToNodes) {
        this.teleportToNodes = teleportToNodes;
    }

    public double getTeleportationProbability() {
        return teleportationProbability;
    }

    public void setTeleportationProbability(final double teleportationProbability) {
        this.teleportationProbability = teleportationProbability;
    }

    public double getSelfTeleportationProbability() {
        return selfTeleportationProbability;
    }

    public void setSelfTeleportationProbability(final double selfTeleportationProbability) {
        this.selfTeleportationProbability = selfTeleportationProbability;
    }

    public long getSeedToRandomNumberGenerator() {
        return seedToRandomNumberGenerator;
    }

    public void setSeedToRandomNumberGenerator(final long seedToRandomNumberGenerator) {
        this.seedToRandomNumberGenerator = seedToRandomNumberGenerator;
    }

    public int getNumTrials() {
        return numTrials;
    }

    public void setNumTrials(final int numTrials) {
        this.numTrials = numTrials;
    }

    public double getMinimumCodelengthImprovement() {
        return minimumCodelengthImprovement;
    }

    public void setMinimumCodelengthImprovement(final double minimumCodelengthImprovement) {
        this.minimumCodelengthImprovement = minimumCodelengthImprovement;
    }

    public boolean isRandomizeCoreLoopLimit() {
        return randomizeCoreLoopLimit;
    }

    public void setRandomizeCoreLoopLimit(final boolean randomizeCoreLoopLimit) {
        this.randomizeCoreLoopLimit = randomizeCoreLoopLimit;
    }

    public int getCoreLoopLimit() {
        return coreLoopLimit;
    }

    public void setCoreLoopLimit(final int coreLoopLimit) {
        this.coreLoopLimit = coreLoopLimit;
    }

    public int getLevelAggregationLimit() {
        return levelAggregationLimit;
    }

    public void setLevelAggregationLimit(final int levelAggregationLimit) {
        this.levelAggregationLimit = levelAggregationLimit;
    }

    public int getTuneIterationLimit() {
        return tuneIterationLimit;
    }

    public void setTuneIterationLimit(final int tuneIterationLimit) {
        this.tuneIterationLimit = tuneIterationLimit;
    }

    public double getMinimumRelativeTuneIterationImprovement() {
        return minimumRelativeTuneIterationImprovement;
    }

    public void setMinimumRelativeTuneIterationImprovement(final double minimumRelativeTuneIterationImprovement) {
        this.minimumRelativeTuneIterationImprovement = minimumRelativeTuneIterationImprovement;
    }

    public boolean isFastCoarseTunePartition() {
        return fastCoarseTunePartition;
    }

    public void setFastCoarseTunePartition(final boolean fastCoarseTunePartition) {
        this.fastCoarseTunePartition = fastCoarseTunePartition;
    }

    public boolean isAlternateCoarseTuneLevel() {
        return alternateCoarseTuneLevel;
    }

    public void setAlternateCoarseTuneLevel(final boolean alternateCoarseTuneLevel) {
        this.alternateCoarseTuneLevel = alternateCoarseTuneLevel;
    }

    public int getCoarseTuneLevel() {
        return coarseTuneLevel;
    }

    public void setCoarseTuneLevel(final int coarseTuneLevel) {
        this.coarseTuneLevel = coarseTuneLevel;
    }

    public int getFastHierarchicalSolution() {
        return fastHierarchicalSolution;
    }

    public void setFastHierarchicalSolution(final int fastHierarchicalSolution) {
        this.fastHierarchicalSolution = fastHierarchicalSolution;
    }

    public String getOutDirectory() {
        return outDirectory;
    }

    public void setOutDirectory(final String outDirectory) {
        this.outDirectory = outDirectory;
    }

    public boolean isPrintTree() {
        return printTree;
    }

    public void setPrintTree(final boolean printTree) {
        this.printTree = printTree;
    }

    public boolean isPrintMap() {
        return printMap;
    }

    public void setPrintMap(final boolean printMap) {
        this.printMap = printMap;
    }

    public boolean isPrintClu() {
        return printClu;
    }

    public void setPrintClu(final boolean printClu) {
        this.printClu = printClu;
    }

    public boolean isPrintNodeRanks() {
        return printNodeRanks;
    }

    public void setPrintNodeRanks(final boolean printNodeRanks) {
        this.printNodeRanks = printNodeRanks;
    }

    public boolean isPrintFlowNetwork() {
        return printFlowNetwork;
    }

    public void setPrintFlowNetwork(final boolean printFlowNetwork) {
        this.printFlowNetwork = printFlowNetwork;
    }

    public boolean isPrintBinaryTree() {
        return printBinaryTree;
    }

    public void setPrintBinaryTree(final boolean printBinaryTree) {
        this.printBinaryTree = printBinaryTree;
    }

    public boolean isPrintBinaryFlowTree() {
        return printBinaryFlowTree;
    }

    public void setPrintBinaryFlowTree(final boolean printBinaryFlowTree) {
        this.printBinaryFlowTree = printBinaryFlowTree;
    }

    public boolean isNoFileOutput() {
        return noFileOutput;
    }

    public void setNoFileOutput(final boolean noFileOutput) {
        this.noFileOutput = noFileOutput;
    }

    public int getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(final int verbosity) {
        this.verbosity = verbosity;
    }

    public int getVerboseNumberPrecision() {
        return verboseNumberPrecision;
    }

    public void setVerboseNumberPrecision(final int verboseNumberPrecision) {
        this.verboseNumberPrecision = verboseNumberPrecision;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(final ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
}
