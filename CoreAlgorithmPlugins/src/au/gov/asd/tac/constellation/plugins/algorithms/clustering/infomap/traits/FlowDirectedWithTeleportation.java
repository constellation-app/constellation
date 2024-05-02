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
public class FlowDirectedWithTeleportation implements FlowBase {

    private double flow;
    private double exitFlow;
    private double teleportWeight;
    private double danglingFlow;
    private double teleportSourceFlow;

    public FlowDirectedWithTeleportation() {
        this(1, 1);
    }

    public FlowDirectedWithTeleportation(final double flow, final double teleportWeight) {
        this.flow = flow;
        this.exitFlow = 0;
        this.teleportWeight = teleportWeight;
        this.danglingFlow = 0;
        this.teleportSourceFlow = 0;
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
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public double getEnterFlow() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setExitFlow(final double exitFlow) {
        this.exitFlow = exitFlow;
    }

    @Override
    public double getExitFlow() {
        return exitFlow;
    }

    public double getTeleportWeight() {
        return teleportWeight;
    }

    public double getDanglingFlow() {
        return danglingFlow;
    }

    public double getTeleportSourceFlow() {
        return teleportSourceFlow;
    }

    @Override
    public void add(final FlowBase other) {
        final FlowDirectedWithTeleportation f = (FlowDirectedWithTeleportation) other;
        flow += f.flow;
        exitFlow += f.exitFlow;
        teleportWeight += f.teleportWeight;
        danglingFlow += f.danglingFlow;
        teleportSourceFlow += f.teleportSourceFlow;
    }

    @Override
    public void sub(final FlowBase other) {
        final FlowDirectedWithTeleportation f = (FlowDirectedWithTeleportation) other;
        flow -= f.flow;
        exitFlow -= f.exitFlow;
        teleportWeight -= f.teleportWeight;
        danglingFlow -= f.danglingFlow;
        teleportSourceFlow -= f.teleportSourceFlow;
    }

    @Override
    public FlowBase copy() {
        final FlowDirectedWithTeleportation f = new FlowDirectedWithTeleportation();
        f.flow = flow;
        f.exitFlow = exitFlow;
        f.teleportWeight = teleportWeight;
        f.danglingFlow = danglingFlow;
        f.teleportSourceFlow = teleportSourceFlow;

        return f;
    }

    @Override
    public String toString() {
        return String.format("[%s: flow=%f exit=%f teleportWeight=%f danglingFlow=%f teleportSourceFlow=%f]",
                getClass().getSimpleName(),
                flow, exitFlow, teleportWeight, danglingFlow, teleportSourceFlow);
    }
}
