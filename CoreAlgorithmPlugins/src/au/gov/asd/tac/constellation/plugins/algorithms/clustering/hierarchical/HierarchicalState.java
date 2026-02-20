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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical;

/**
 * State variables used by the Hierarchical TopComponent.
 *
 * @author algol
 */
public final class HierarchicalState {

    private int steps;
    private int currentStep;
    private int optimumStep = 1;
    private FastNewman.Group[] groups;
    private boolean excludeSingleVertices;
    private boolean excludedElementsDimmed;
    private int[] clusterNumbers;
    private int[] clusterSeenBefore;
    private int redrawCount = 0;
    private long modificationCounter = -1;
    private long strucModificationCount = -1;
    private boolean interactive = true;
    private boolean colored = true;

    public enum ExcludedState {
        SHOW,
        HIDDEN,
        DIMMED
    }
    private ExcludedState excludedElementsState = ExcludedState.SHOW;

    /**
     * Required for AbstractGraphIOProvider.
     */
    public HierarchicalState() {
    }

    public HierarchicalState(final int steps, final int currentStep, final FastNewman.Group[] groups, final int vxCapacity) {
        this.steps = steps;
        optimumStep = this.currentStep = currentStep;
        this.groups = groups.clone();

        excludeSingleVertices = false;
        excludedElementsDimmed = false;
        clusterNumbers = new int[vxCapacity];
        clusterSeenBefore = new int[vxCapacity];
        redrawCount = 0;

        // This is used by the HierarchicalTopComponent to keep track of the current graph.
        modificationCounter = -1;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(final int currentStep) {
        this.currentStep = currentStep;
    }

    public int getOptimumStep() {
        return optimumStep;
    }

    public void setOptimumStep(final int optimumStep) {
        this.optimumStep = optimumStep;
    }

    public boolean isExcludeSingleVertices() {
        return excludeSingleVertices;
    }

    public void setExcludeSingleVertices(final boolean excludeSingleVertices) {
        this.excludeSingleVertices = excludeSingleVertices;
    }

    public boolean isExcludedElementsDimmed() {
        return excludedElementsDimmed;
    }

    public void setExcludedElementsDimmed(final boolean excludedElementsDimmed) {
        this.excludedElementsDimmed = excludedElementsDimmed;
    }

    public ExcludedState getExcludedElementsState() {
        return this.excludedElementsState;
    }

    public void setExcludedElementsState(final ExcludedState newState) {
        this.excludedElementsState = newState;
    }

    public int[] getClusterNumbers() {
        return clusterNumbers.clone();
    }

    public void setClusterNumbers(final int[] clusterNumbers) {
        this.clusterNumbers = clusterNumbers.clone();
    }

    public int[] getClusterSeenBefore() {
        return clusterSeenBefore.clone();
    }

    public void setClusterSeenBefore(final int[] clusterSeenBefore) {
        this.clusterSeenBefore = clusterSeenBefore.clone();
    }

    public int getRedrawCount() {
        return redrawCount;
    }

    public void setRedrawCount(final int redrawCount) {
        this.redrawCount = redrawCount;
    }

    public long getModificationCounter() {
        return modificationCounter;
    }

    public void setModificationCounter(final long modificationCounter) {
        this.modificationCounter = modificationCounter;
    }

    public long getStrucModificationCount() {
        return strucModificationCount;
    }

    public void setStrucModificationCount(final long strucModificationCount) {
        this.strucModificationCount = strucModificationCount;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive(final boolean interactive) {
        this.interactive = interactive;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(final int steps) {
        this.steps = steps;
    }

    /**
     * These groups cannot be cloned as the state uses this call to update the list of groups by index
     *
     * @return groups
     */
    public FastNewman.Group[] getGroups() {
        return groups;
    }

    public void setGroups(final FastNewman.Group[] groups) {
        this.groups = groups;
    }

    public boolean isColored() {
        return colored;
    }

    public void setColored(final boolean colored) {
        this.colored = colored;
    }

    public int getCurrentNumOfClusters() {
        int numClusters = 0;
        if (groups != null) {
            for (final FastNewman.Group group : groups) {
                if (group != null && group.getMergeStep() > currentStep) {
                    numClusters++;
                }
            }
        }
        return numClusters;
    }

    @Override
    public String toString() {
        return String.format("CoI[steps=%d,currentStep=%d]", steps, currentStep);
    }
}
