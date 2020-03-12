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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical;

/**
 * State variables used by the Hierarchical TopComponent.
 *
 * @author algol
 */
public final class HierarchicalState {

    int steps;
    int currentStep;
    int optimumStep = 1;
    FastNewman.Group[] groups;
    boolean excludeSingleVertices;
    boolean excludedElementsDimmed;
    int[] clusterNumbers;
    int[] clusterSeenBefore;
    int redrawCount = 0;
    transient long modificationCounter = -1;
    transient long strucModificationCount = -1;
    boolean interactive = true;
    boolean colored = true;

    /**
     * Required for AbstractGraphIOProvider.
     */
    public HierarchicalState() {
    }

    public HierarchicalState(final int steps, final int currentStep, final FastNewman.Group[] groups, final int vxCapacity) {
        this.steps = steps;
        optimumStep = this.currentStep = currentStep;
        this.groups = groups;

        excludeSingleVertices = false;
        excludedElementsDimmed = false;
        clusterNumbers = new int[vxCapacity];
        clusterSeenBefore = new int[vxCapacity];
        redrawCount = 0;

        // This is used by the HierarchicalTopComponent to keep track of the current graph.
        modificationCounter = -1;
    }

    public int getCurrentNumOfClusters() {
        int numClusters = 0;
        for (FastNewman.Group group : groups) {
            if (group != null && group.mergeStep > currentStep) {
                numClusters++;
            }
        }
        return numClusters;
    }

    @Override
    public String toString() {
        return String.format("CoI[steps=%d,currentStep=%d]", steps, currentStep);
    }
}
