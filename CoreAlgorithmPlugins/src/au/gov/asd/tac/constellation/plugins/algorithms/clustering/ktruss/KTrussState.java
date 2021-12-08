/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores all the information about the visualisation of k-trusses in a graph.
 * This includes information about the connected components of the various
 * k-trusses. It is used by KTrussControllerTopComponent and
 * NestedKTrussDisplayPanel to query and update the current state of
 * visualisation of k-trusses.
 *
 * @author twilight_sparkle
 */
public class KTrussState {

    transient long modificationCounter;
    transient long strucModificationCount;
    // The value of k indidcating which k-trusses are being visualised.
    private int currentK;
    // The highest value of k available to be visualised (is set to one more than the heighest k-truss in the graph)
    private final int highestK;
    // Whether to dim or hide the nodes and transactions that do not belong to the k-trusses being visualised
    private boolean excludedElementsDimmed;
    // Keeps track of how many times the display options have changed.
    private int displayOptionToggles = 0;
    // Records the significant values of k, that is the values of k for which there are (k-1)-trusses in the graph that are not k-trusses.
    private final boolean[] extantKTrusses;
    // Convert from singificant values of k to an index in the ordered list of signiifacnt values of k.
    private final int[] kTrussToIndex;
    // Convert from an index in the ordered list of significant values of k, to a significant value of k.
    private final int[] indexToKTruss;
    // The smallest connected component each node is in.
    private Map<Integer, Integer> nodeToComponent;
    // The smallest connected component each link is in.
    private Map<Integer, Integer> linkToComponent;
    // The heirarchy of nested connected components
    private Map<Integer, Integer> componentTree;
    // The number of nodes in each connected component.
    private Map<Integer, Integer> componentSizes;
    // Whether or not the rectangles representing the nesting of connected components is visible. Used by KTrussControllerTopComponent to show or hide its NestedKTrussDisplayPanel
    private boolean isNestedTrussesVisible;
    // The total number of connected components
    private int highestComponentNum;
    // The total number of nodes in the graph - this could be retrieved from the graph directly, but it is convenient to store it here.
    private int totalVerts;
    // The total number of nodes in a k-truss for some k >= 3.
    private int totalVertsInTrusses;
    // Whether or not to draw connected components of the whole graph as well as connected components of the k-trusses.
    private boolean drawAllComponents = false;
    // Whether to color k-trusses different colors for different values of k, indicating nesting.
    private boolean nestedTrussesColored;
    // Whether to actually allow visualisation and itneractivity based on this KTRussState via the KTrussController
    private boolean interactive;

    public KTrussState(final int highestK, final boolean[] extantKTrusses) {
        this.highestK = highestK;
        this.extantKTrusses = extantKTrusses;
        kTrussToIndex = new int[extantKTrusses.length];
        indexToKTruss = new int[highestK];
        indicesOfKTrusses();
        excludedElementsDimmed = true;
        currentK = 3;
        modificationCounter = -1;
        isNestedTrussesVisible = false;
        nestedTrussesColored = false;
        interactive = true;
    }

    // deep copy constructor
    public KTrussState(final KTrussState prevState) {
        modificationCounter = prevState.modificationCounter;
        currentK = prevState.currentK;
        highestK = prevState.highestK;
        excludedElementsDimmed = prevState.excludedElementsDimmed;
        displayOptionToggles = prevState.displayOptionToggles;
        extantKTrusses = prevState.extantKTrusses;
        kTrussToIndex = Arrays.copyOf(prevState.kTrussToIndex, prevState.kTrussToIndex.length);
        indexToKTruss = Arrays.copyOf(prevState.indexToKTruss, prevState.indexToKTruss.length);
        nodeToComponent = new HashMap<>(prevState.getNodeToComponent());
        linkToComponent = new HashMap<>(prevState.getLinkToComponent());
        componentTree = new HashMap<>(prevState.getComponentTree());
        componentSizes = new HashMap<>(prevState.getComponentSizes());
        isNestedTrussesVisible = prevState.isNestedTrussesVisible;
        highestComponentNum = prevState.highestComponentNum;
        totalVerts = prevState.totalVerts;
        totalVertsInTrusses = prevState.totalVertsInTrusses;
        nestedTrussesColored = prevState.nestedTrussesColored;
        interactive = prevState.interactive;
    }

    // Copy constructor: used for keeping visualisation options the same when
    // The k-truss plugin is run for a second time on a (possibly modified) graph.
    // Only the highest value of k and the signficiant values of k need be changed.
    public KTrussState(final KTrussState prevState, final int highestK, final boolean[] extantKTrusses) {
        this.highestK = highestK;
        this.extantKTrusses = extantKTrusses;
        kTrussToIndex = new int[extantKTrusses.length];
        indexToKTruss = new int[extantKTrusses.length];
        indicesOfKTrusses();
        displayOptionToggles = prevState.displayOptionToggles;
        excludedElementsDimmed = prevState.excludedElementsDimmed;
        currentK = Math.min(prevState.currentK, highestK);
        modificationCounter = prevState.modificationCounter;
        isNestedTrussesVisible = prevState.isNestedTrussesVisible;
        nestedTrussesColored = prevState.nestedTrussesColored;
        interactive = prevState.interactive;
    }

    public KTrussState(final long modificationCounter, final long strucModificationCount, final int currentK, final int highestK, final boolean excludedElementsDimmed, final int displayOptionToggles, final boolean[] extantKTrusses, final int[] kTrussToIndex, final int[] indexToKTruss, final Map<Integer, Integer> nodeToComponent, final Map<Integer, Integer> linkToComponent, final Map<Integer, Integer> componentTree, final Map<Integer, Integer> componentSizes, final boolean isNestedTrussesVisible, final int highestComponentNum, final int totalVerts, final int totalVertsInTrusses, final boolean drawAllComponents, final boolean nestedTrussesColored, final boolean interactive) {
        this.modificationCounter = modificationCounter;
        this.strucModificationCount = strucModificationCount;
        this.currentK = currentK;
        this.highestK = highestK;
        this.excludedElementsDimmed = excludedElementsDimmed;
        this.displayOptionToggles = displayOptionToggles;
        this.extantKTrusses = extantKTrusses;
        this.kTrussToIndex = kTrussToIndex;
        this.indexToKTruss = indexToKTruss;
        this.nodeToComponent = nodeToComponent;
        this.linkToComponent = linkToComponent;
        this.componentTree = componentTree;
        this.componentSizes = componentSizes;
        this.isNestedTrussesVisible = isNestedTrussesVisible;
        this.highestComponentNum = highestComponentNum;
        this.totalVerts = totalVerts;
        this.totalVertsInTrusses = totalVertsInTrusses;
        this.nestedTrussesColored = nestedTrussesColored;
        this.interactive = interactive;
    }

    // Sets all the information related to the connected components of the k-trusses
    public void setComponentInformation(final Map<Integer, Integer> nodeToComponent, final Map<Integer, Integer> linkToComponent, final Map<Integer, Integer> componentTree, final Map<Integer, Integer> componentSizes, final int highestComponentNum, final int totalVerts, final int totalVertsInTrusses) {
        this.nodeToComponent = nodeToComponent;
        this.linkToComponent = linkToComponent;
        this.componentTree = componentTree;
        this.componentSizes = componentSizes;
        this.highestComponentNum = highestComponentNum;
        this.totalVerts = totalVerts;
        this.totalVertsInTrusses = totalVertsInTrusses;
    }

    public boolean isNestedTrussesColored() {
        return nestedTrussesColored;
    }

    public void toggleNestedTrussesColored() {
        nestedTrussesColored = !nestedTrussesColored;
    }

    public void toggleDrawAllComponents() {
        drawAllComponents = !drawAllComponents;
    }

    public boolean isDrawAllComponents() {
        return drawAllComponents;
    }

    public int getTotalVertsInTrusses() {
        return totalVertsInTrusses;
    }

    public void toggleNestedTrussesVisible() {
        isNestedTrussesVisible = !isNestedTrussesVisible;
    }

    public boolean isNestedTrussesVisible() {
        return isNestedTrussesVisible;
    }

    private void indicesOfKTrusses() {
        int index = -1;
        for (int i = 0; i < getExtantKTrusses().length; i++) {
            if (getExtantKTrusses()[i]) {
                kTrussToIndex[i] = ++index;
                indexToKTruss[index] = i;
            } else {
                kTrussToIndex[i] = index;
            }
        }
    }

    public int getNumUniqueValuesOfK() {
        return getkTrussToIndex()[getkTrussToIndex().length - 1] + 1;
    }

    public boolean isNodeInComponent(final int vxID, final int component) {
        if (getNodeToComponent().get(vxID) == null) {
            return false;
        }
        if (getNodeToComponent().get(vxID) == component) {
            return true;
        } else {
            return hasComponentParent(getNodeToComponent().get(vxID), component);
        }
    }

    private boolean hasComponentParent(final int component, final int parent) {
        return (component == parent
                || (!componentTree.get(component).equals(component)
                && hasComponentParent(getComponentTree().get(component), parent)));
    }

    public boolean isLinkInComponent(final int lnID, final int component) {
        if (getLinkToComponent().get(lnID) == null) {
            return false;
        }
        if (getLinkToComponent().get(lnID) == component) {
            return true;
        } else {
            return hasComponentParent(getLinkToComponent().get(lnID), component);
        }
    }

    public int getTotalVerts() {
        return totalVerts;
    }

    public int getNumComponents() {
        return getHighestComponentNum();
    }

    public int getComponentSize(final int i) {
        return getComponentSizes().get(i);
    }

    public int getComponentParent(final int i) {
        return getComponentTree().get(i);
    }

    public int getIndexOfKTruss(final int k) {
        return getkTrussToIndex()[k];
    }

    public int getKTrussFromIndex(final int i) {
        return getIndexToKTruss()[i];
    }

    public void displayOptionHasToggled() {
        displayOptionToggles++;
    }

    public void displayOptionToggleHandled() {
        displayOptionToggles--;
    }

    public boolean hasDisplayOptionToggled() {
        return (getDisplayOptionToggles() > 0);
    }

    public boolean isKTrussExtant(final int k) {
        try {
            return getExtantKTrusses()[k];
        } catch (final ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    public int getCurrentK() {
        return currentK;
    }

    public void setCurrentK(final int value) {
        // never let the value be set to anything below 3 other than 0.
        // 0 means 'ignore k-trusses'. A 1-truss or 2-truss doesn't make sense.
        this.currentK = (value < 3) ? 0 : value;
    }

    public int getHighestK() {
        return highestK;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive(final boolean value) {
        interactive = value;
    }

    public void setExcludedElementsDimmed(final boolean value) {
        this.excludedElementsDimmed = value;
    }

    /**
     * @return the excludedElementsDimmed
     */
    public boolean isExcludedElementsDimmed() {
        return excludedElementsDimmed;
    }

    /**
     * @return the displayOptionToggles
     */
    public int getDisplayOptionToggles() {
        return displayOptionToggles;
    }

    /**
     * @return the extantKTrusses
     */
    public boolean[] getExtantKTrusses() {
        return extantKTrusses;
    }

    /**
     * @return the kTrussToIndex
     */
    public int[] getkTrussToIndex() {
        return kTrussToIndex;
    }

    /**
     * @return the indexToKTruss
     */
    public int[] getIndexToKTruss() {
        return indexToKTruss;
    }

    /**
     * @return the nodeToComponent
     */
    public Map<Integer, Integer> getNodeToComponent() {
        return nodeToComponent;
    }

    /**
     * @return the linkToComponent
     */
    public Map<Integer, Integer> getLinkToComponent() {
        return linkToComponent;
    }

    /**
     * @return the componentTree
     */
    public Map<Integer, Integer> getComponentTree() {
        return componentTree;
    }

    /**
     * @return the componentSizes
     */
    public Map<Integer, Integer> getComponentSizes() {
        return componentSizes;
    }

    /**
     * @return the highestComponentNum
     */
    public int getHighestComponentNum() {
        return highestComponentNum;
    }
}
