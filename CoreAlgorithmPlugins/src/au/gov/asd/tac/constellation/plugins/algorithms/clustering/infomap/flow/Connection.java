/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow;

/**
 *
 * @author algol
 */
public class Connection {

    private int source;
    private int target;
    private double weight;
    private double flow;

    public Connection(final int end1, final int end2, final double weight) {
        this.source = end1;
        this.target = end2;
        this.weight = weight;
        flow = weight;
    }

    public int getSource() {
        return source;
    }

    public void setSource(final int source) {
        this.source = source;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(final int target) {
        this.target = target;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(final double weight) {
        this.weight = weight;
    }

    public double getFlow() {
        return flow;
    }

    public void setFlow(final double flow) {
        this.flow = flow;
    }
}
