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
package au.gov.asd.tac.constellation.views.timeline.clustering;

/**
 *
 * @author betelgeuse
 */
public class TreeLeaf extends TreeElement implements Comparable<TreeLeaf> {

    private final int id;
    private final long datetime;

    private final int lowerDisplayPos;
    private final int upperDisplayPos;

    int vertexIdA;
    int vertexIdB;

    private int selectionCount = 0;
    private final boolean nodesSelected;

    TreeLeaf(final int transactionID, final long transactionValue, final boolean isSelected, final boolean nodesSelected, final int lowerDisplayPos,
            final int upperDisplayPos, final int vertexIdA, final int vertexIdB) {
        this.id = transactionID;
        this.datetime = transactionValue;
        this.lowerDisplayPos = lowerDisplayPos;
        this.upperDisplayPos = upperDisplayPos;
        this.vertexIdA = vertexIdA;
        this.vertexIdB = vertexIdB;

        if (isSelected) {
            selectionCount = 1;
        }
        this.nodesSelected = nodesSelected;
    }

    public int getId() {
        return id;
    }

    public long getDatetime() {
        return datetime;
    }

    @Override
    public long getLowerTimeExtent() {
        return getDatetime();
    }

    @Override
    public long getUpperTimeExtent() {
        return getDatetime();
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
    public int getCount() {
        return 1;
    }

    @Override
    public int getSelectedCount() {
        return selectionCount;
    }

    @Override
    public boolean anyNodesSelected() {
        return nodesSelected;
    }

    @Override
    public int compareTo(final TreeLeaf o) {
        if (this.datetime < o.datetime) {
            return -1;
        }
        return this.datetime == o.datetime ? 0 : 1;
    }
}
