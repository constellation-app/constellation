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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

/**
 *
 * @author Quasar985
 */
public class TreeArranger {

    protected GraphWriteMethods graph;

    /**
     * For each vertex, record its children in order of the number of their descendants.
     * <p>
     * Record result an AtomicQueue stored by parent in hash table. Returns number of children for vertex.
     *
     * Returns the number of children
     */
    protected int orderChildren(final int vxId, final BitSet vxsToGo, final MutableIntObjectMap<List<VxInfo>> orderedChildren, final BitSet onlyChildren) {
        final List<VxInfo> children = new ArrayList<>();

        // For the specified vertex, get its children and record how many children they have.
        for (int position = 0; position < graph.getVertexNeighbourCount(vxId); position++) {
            final int nbId = graph.getVertexNeighbour(vxId, position);
            if (vxsToGo.get(nbId)) {
                children.add(new VxInfo(nbId, 0));
                vxsToGo.clear(nbId);
            }
        }

        if (children.isEmpty()) {
            return 1;
        }

        if (children.size() == 1) {
            onlyChildren.set(children.iterator().next().vxId);
        }

        // Remove these children from consideration.
        removeChildren(vxsToGo, children);

        int result = 1;
        final List<VxInfo> vxInfoList = new ArrayList<>();
        for (final VxInfo vxInfo : children) {
            final int numChildren = orderChildren(vxInfo.vxId, vxsToGo, orderedChildren, onlyChildren);
            vxInfoList.add(new VxInfo(vxInfo.vxId, numChildren));
            result += numChildren;
        }

        Collections.sort(vxInfoList);
        orderedChildren.put(vxId, vxInfoList);

        return result;
    }

    /**
     * Remove the children from the set of vertex ids.
     *
     * @param vxs
     * @param children
     */
    protected static void removeChildren(final BitSet vxs, final List<VxInfo> children) {
        for (final VxInfo vxInfo : children) {
            vxs.clear(vxInfo.vxId);
        }
    }

    /**
     * A vertex id and the number of children the vertex has.
     */
    protected static class VxInfo implements Comparable<VxInfo> {

        final int vxId;
        final int nChildren;

        public VxInfo(final int vxId, final int nChildren) {
            this.vxId = vxId;
            this.nChildren = nChildren;
        }

        @Override
        public int compareTo(final VxInfo o) {
            return nChildren - o.nChildren;
        }

        @Override
        public String toString() {
            return String.format("VxInfo[vxId=%d,nChildren=%d]", vxId, nChildren);
        }
    }
}
