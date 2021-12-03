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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.tree;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeFactoryBase;
import java.util.ArrayList;

/**
 *
 * @author algol
 */
public class TreeData {

    private final NodeFactoryBase nodeFactory;
    private final NodeBase root;
    private final ArrayList<NodeBase> leafNodes;
    private int nLeafEdges;

    public TreeData(final NodeFactoryBase nodeFactory) {
        this.nodeFactory = nodeFactory;

        nLeafEdges = 0;
        root = nodeFactory.createNode("root", 1, 1);
        leafNodes = new ArrayList<>();
    }

    public int addNewNode(final int position, final String name, final double flow, final double teleportWeight) {
        final NodeBase node = nodeFactory.createNode(name, flow, teleportWeight);
        node.setOriginalIndex(position);
        root.addChild(node);
        leafNodes.add(node);

        return node.getOriginalIndex();
    }

    public void addClonedNode(final NodeBase node) {
        root.addChild(node);

        leafNodes.add(node);
    }

    public void addEdge(final int sourceIndex, final int targetIndex, final double weight, final double flow) {
        final NodeBase source = leafNodes.get(sourceIndex);
        final NodeBase target = leafNodes.get(targetIndex);
        source.addOutEdge(target, weight, flow);
        nLeafEdges++;
    }

    public int getNumLeafNodes() {
        return leafNodes.size();
    }

    public int getNumLeafEdges() {
        return nLeafEdges;
    }

    public NodeBase getRoot() {
        return root;
    }

    public NodeBase getLeafNode(final int index) {
        if (index < leafNodes.size()) {
            return leafNodes.get(index);
        } else {
            throw new IllegalArgumentException();
        }

    }

    public NodeBase getFirstLeaf() {
        return leafNodes.get(0);
    }

    public ArrayList<NodeBase> getLeaves() {
        return leafNodes;
    }

    public NodeFactoryBase getNodeFactory() {
        return nodeFactory;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();

        buf.append("[\n");
        buf.append(String.format("%s\n", root));
        for (final NodeBase node : getLeaves()) {
            buf.append(String.format("  %s\n", node));
        }

        buf.append("]\n");

        return buf.toString();
    }
}
