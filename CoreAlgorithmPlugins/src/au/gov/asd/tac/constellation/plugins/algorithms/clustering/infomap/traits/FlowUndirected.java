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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits;

/**
 *
 * @author algol
 */
public class FlowUndirected implements FlowBase {

    private double flow;
    private double exitFlow;

    public FlowUndirected() {
        this(1, 1);
    }

    public FlowUndirected(final double flow, final double teleportWeight) {
        this.flow = flow;
        this.exitFlow = 0;
    }

    @Override
    public void setFlow(final double flow) {
        this.flow = flow;
    }

    @Override
    public double getFlow() {
        return flow;
    }

    @Override
    public void setEnterFlow(final double enterFlow) {
        this.exitFlow = enterFlow;
    }

    @Override
    public double getEnterFlow() {
        return exitFlow;
    }

    @Override
    public void setExitFlow(final double exitFlow) {
        this.exitFlow = exitFlow;
    }

    @Override
    public double getExitFlow() {
        return exitFlow;
    }

    @Override
    public void add(final FlowBase other) {
        final FlowUndirected f = (FlowUndirected) other;
        flow += f.flow;
        exitFlow += f.exitFlow;
    }

    @Override
    public void sub(final FlowBase other) {
        final FlowUndirected f = (FlowUndirected) other;
        flow -= f.flow;
        exitFlow -= f.exitFlow;
    }

    @Override
    public FlowBase copy() {
        final FlowUndirected f = new FlowUndirected();
        f.flow = flow;
        f.exitFlow = exitFlow;

        return f;
    }

    @Override
    public String toString() {
        return String.format("[%s: flow=%f exit=%f]",
                getClass().getSimpleName(),
                flow, exitFlow);
    }
}
