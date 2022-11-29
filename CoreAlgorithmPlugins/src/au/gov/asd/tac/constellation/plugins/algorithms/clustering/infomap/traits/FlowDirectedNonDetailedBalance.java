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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits;

/**
 *
 * @author algol
 */
public class FlowDirectedNonDetailedBalance implements FlowBase {

    private double flow;
    private double enterFlow;
    private double exitFlow;

    public FlowDirectedNonDetailedBalance() {
        this(1, 1);
    }

    public FlowDirectedNonDetailedBalance(final double flow, final double teleportWeight) {
        this.flow = flow;
        this.enterFlow = 0;
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
        this.enterFlow = enterFlow;
    }

    @Override
    public double getEnterFlow() {
        return enterFlow;
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
        final FlowDirectedNonDetailedBalance f = (FlowDirectedNonDetailedBalance) other;
        flow += f.flow;
        enterFlow += f.enterFlow;
        exitFlow += f.exitFlow;
    }

    @Override
    public void sub(final FlowBase other) {
        final FlowDirectedNonDetailedBalance f = (FlowDirectedNonDetailedBalance) other;
        flow -= f.flow;
        enterFlow -= f.enterFlow;
        exitFlow -= f.exitFlow;
    }

    @Override
    public FlowBase copy() {
        final FlowDirectedNonDetailedBalance f = new FlowDirectedNonDetailedBalance();
        f.flow = flow;
        f.enterFlow = enterFlow;
        f.exitFlow = exitFlow;

        return f;
    }

    @Override
    public String toString() {
        return String.format("[%s: flow=%f enter=%f exit=%f]",
                getClass().getSimpleName(),
                flow, enterFlow, exitFlow);
    }
}
