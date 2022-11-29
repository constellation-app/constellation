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
package au.gov.asd.tac.constellation.views.timeline.clustering;

/**
 *
 * @author betelgeuse
 */
public class TreeNode extends TreeElement implements Comparable<TreeNode> {

    TreeElement firstChild;
    TreeElement lastChild;

    TreeNode next;
    TreeNode previous;

    private int leafCount;
    private int selectedLeafCount;
    private long lowerTimeExtent;
    private long upperTimeExtent;
    private int lowerDisplayPos;
    private int upperDisplayPos;
    private boolean nodesSelectedInLeaves;

    public TreeNode(final TreeElement firstChild, final TreeElement lastChild) {
        this.firstChild = firstChild;
        this.lastChild = lastChild;

        leafCount = firstChild.getCount() + lastChild.getCount();
        selectedLeafCount = firstChild.getSelectedCount() + lastChild.getSelectedCount();
        nodesSelectedInLeaves = firstChild.anyNodesSelected() || lastChild.anyNodesSelected();
        lowerTimeExtent = firstChild.getLowerTimeExtent();
        upperTimeExtent = lastChild.getUpperTimeExtent();
        lowerDisplayPos = Math.min(firstChild.getLowerDisplayPos(), lastChild.getLowerDisplayPos());
        upperDisplayPos = Math.max(firstChild.getUpperDisplayPos(), lastChild.getUpperDisplayPos());
    }

    @Override
    public int compareTo(final TreeNode o) {
        final long thisGap = lastChild.getLowerTimeExtent() - firstChild.getUpperTimeExtent();
        final long otherGap = o.lastChild.getLowerTimeExtent() - o.firstChild.getUpperTimeExtent();

        if (thisGap < otherGap) {
            return -1;
        } else if (thisGap == otherGap) {
            return 0;
        } else {
            return 1;
        }
    }

    public void setFirstChild(final TreeNode firstChild) {
        this.firstChild = firstChild;

        leafCount = firstChild.getCount() + lastChild.getCount();
        selectedLeafCount = firstChild.getSelectedCount() + lastChild.getSelectedCount();
        nodesSelectedInLeaves = firstChild.anyNodesSelected() || lastChild.anyNodesSelected();
        lowerTimeExtent = firstChild.getLowerTimeExtent();
        upperTimeExtent = lastChild.getUpperTimeExtent();

        lowerDisplayPos = Math.min(firstChild.getLowerDisplayPos(), lastChild.getLowerDisplayPos());
        upperDisplayPos = Math.max(firstChild.getUpperDisplayPos(), lastChild.getUpperDisplayPos());
    }

    public void setLastChild(final TreeNode lastChild) {
        this.lastChild = lastChild;

        leafCount = firstChild.getCount() + lastChild.getCount();
        selectedLeafCount = firstChild.getSelectedCount() + lastChild.getSelectedCount();
        nodesSelectedInLeaves = firstChild.anyNodesSelected() || lastChild.anyNodesSelected();
        lowerTimeExtent = firstChild.getLowerTimeExtent();
        upperTimeExtent = lastChild.getUpperTimeExtent();

        lowerDisplayPos = Math.min(firstChild.getLowerDisplayPos(), lastChild.getLowerDisplayPos());
        upperDisplayPos = Math.max(firstChild.getUpperDisplayPos(), lastChild.getUpperDisplayPos());
    }

    @Override
    public long getLowerTimeExtent() {
        return lowerTimeExtent;
    }

    @Override
    public long getUpperTimeExtent() {
        return upperTimeExtent;
    }

    @Override
    public int getCount() {
        return leafCount;
    }

    @Override
    public int getLowerDisplayPos() {
        return lowerDisplayPos;
    }

    @Override
    public int getUpperDisplayPos() {
        return upperDisplayPos;
    }

    @Override
    public int getSelectedCount() {
        return selectedLeafCount;
    }

    @Override
    public boolean anyNodesSelected() {
        return nodesSelectedInLeaves;
    }
}
