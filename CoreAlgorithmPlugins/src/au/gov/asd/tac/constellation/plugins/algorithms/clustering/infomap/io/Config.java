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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author algol
 */
public final class Config {
    
    // Core algorithm
    private boolean directed;
    private boolean undirdir;
    private boolean outdirdir;
    private boolean rawdir;

    // Performance and accuracy
    private int numTrials;
    private int levelAggregationLimit;
    private int tuneIterationLimit; // num iterations of fine-tune/coarse-tune in two-level partition)
    private boolean fastCoarseTunePartition;
    private boolean alternateCoarseTuneLevel;
    private int coarseTuneLevel;
    private int fastHierarchicalSolution;
    
    // Custom for Graph.
    private ConnectionType connectionType;

    public enum ConnectionType {
        
        LINKS("Links"),
        EDGES("Edges"), 
        TRANSACTIONS("Transactions");
        
        private final String value;
        
        private static final Map<String, ConnectionType> STRING_MAP = new HashMap<>();
        static {
            for (final ConnectionType conn : ConnectionType.values()) {
                STRING_MAP.put(conn.toString(), conn);
            }
        }
        
        private ConnectionType(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
        
        public static ConnectionType fromString(final String str) {
            return STRING_MAP.get(str);
        }
    }

    public Config() {
        numTrials = 1;
        coarseTuneLevel = 1;

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
            case 0 -> {
                // full coarse-tune
                levelAggregationLimit = 0;
                tuneIterationLimit = 0;
                fastCoarseTunePartition = false;
                alternateCoarseTuneLevel = true;
                coarseTuneLevel = 3;
            }
            case 1 -> {
                // fast coarse-tune
                levelAggregationLimit = 0;
                tuneIterationLimit = 0;
                fastCoarseTunePartition = true;
                alternateCoarseTuneLevel = false;
                coarseTuneLevel = 1;
            }
            case 2 -> {
                // no tuning
                levelAggregationLimit = 0;
                tuneIterationLimit = 1;
                fastCoarseTunePartition = true;
                alternateCoarseTuneLevel = false;
                coarseTuneLevel = 1;
            }
            case 3 -> {
                // no aggregation nor any tuning
                levelAggregationLimit = 1;
                tuneIterationLimit = 1;
                fastCoarseTunePartition = true;
                alternateCoarseTuneLevel = false;
                coarseTuneLevel = 1;
            }
            default -> throw new IllegalArgumentException("Optimization level must be in 0..3");
        }
    }

    public boolean isUndirected() {
        return !directed && !undirdir && !outdirdir && !rawdir;
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
    
    public int getNumTrials() {
        return numTrials;
    }

    public void setNumTrials(final int numTrials) {
        this.numTrials = numTrials;
    }
    
    public int getLevelAggregationLimit() {
        return levelAggregationLimit;
    }
    
    public int getTuneIterationLimit() {
        return tuneIterationLimit;
    }
    
    public boolean isFastCoarseTunePartition() {
        return fastCoarseTunePartition;
    }
    
    public boolean isAlternateCoarseTuneLevel() {
        return alternateCoarseTuneLevel;
    }
    
    public int getCoarseTuneLevel() {
        return coarseTuneLevel;
    }
    
    public int getFastHierarchicalSolution() {
        return fastHierarchicalSolution;
    }
    
    public void setFastHierarchicalSolution(final int fastHierarchicalSolution) {
        this.fastHierarchicalSolution = fastHierarchicalSolution;
    }
    
    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(final ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
}
