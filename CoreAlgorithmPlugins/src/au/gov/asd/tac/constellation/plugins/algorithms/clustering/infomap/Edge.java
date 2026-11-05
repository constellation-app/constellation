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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

/**
 *
 * @author algol
 *
 * @param <T> the type of nodes this edge connects to.
 */
public class Edge<T extends NodeBase> {

    private final T source;
    private final T target;
    private final double weight;
    private final double flow;

    public Edge(final T source, final T target, final double weight, final double flow) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.flow = flow;
    }

    public boolean isSelfPointing() {
        return source.equals(target);
    }

    public T getSource() {
        return source;
    }

    public T getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    public double getFlow() {
        return flow;
    }
    
    @Override
    public String toString() {
        return String.format("[Edge: %s -> %s, flow=%f]", source, target, flow);
    }
}
