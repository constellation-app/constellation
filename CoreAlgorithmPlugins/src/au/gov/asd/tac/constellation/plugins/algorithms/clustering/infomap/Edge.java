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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

/**
 *
 * @author algol
 *
 * @param <Node> the type of nodes this edge connects to.
 */
public class Edge<Node extends NodeBase> {

    private final Node source;
    private final Node target;
    private final EdgeData data;

    public Edge(final Node source, final Node target, final double weight, final double flow) {
        this.source = source;
        this.target = target;
        this.data = new EdgeData(weight, flow);
    }

    public Node other(final Node node) {
        return node == source ? target : source;
    }

    public boolean isSelfPointing() {
        return source.equals(target);
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public EdgeData getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("[Edge: %s -> %s, flow=%f]", source, target, data.flow);
    }
}
