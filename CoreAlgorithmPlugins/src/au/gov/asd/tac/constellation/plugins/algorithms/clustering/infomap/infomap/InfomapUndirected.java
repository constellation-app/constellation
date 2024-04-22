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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.Node;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeFactoryBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowUndirected;
import static au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.InfoMath.plogp;

/**
 *
 * @author algol
 */
public class InfomapUndirected extends InfomapGreedy {

    public InfomapUndirected(final Config config, final GraphReadMethods rg) {
        super(config, new NodeFactoryUndirected(), rg);
    }

    @Override
    protected boolean isDirected() {
        return false;
    }

    @Override
    protected boolean hasDetailedBalance() {
        return true;
    }

    @Override
    protected InfomapBase getNewInfomapInstance(final Config config, final GraphReadMethods rg) {
        return new InfomapUndirected(config, rg);
    }

    @Override
    protected double getDeltaCodelength(final Node current, final DeltaFlow oldModuleDelta, final DeltaFlow newModuleDelta) {
        final int oldModule = oldModuleDelta.getModule();
        final int newModule = newModuleDelta.getModule();
        double deltaEnterExitOldModule = oldModuleDelta.getDeltaEnter() + oldModuleDelta.getDeltaExit();
        double deltaEnterExitNewModule = newModuleDelta.getDeltaEnter() + newModuleDelta.getDeltaExit();

        // Double the effect as each link works in both directions.
        deltaEnterExitOldModule *= 2;
        deltaEnterExitNewModule *= 2;

        final double delta_exit = plogp(enterFlow + deltaEnterExitOldModule - deltaEnterExitNewModule) - enterFlowLogEnterFlow;

        final double delta_exit_log_exit
                = -plogp(moduleFlowData[oldModule].getExitFlow())
                - plogp(moduleFlowData[newModule].getExitFlow())
                + plogp(moduleFlowData[oldModule].getExitFlow() - current.getData().getExitFlow() + deltaEnterExitOldModule)
                + plogp(moduleFlowData[newModule].getExitFlow() + current.getData().getExitFlow() - deltaEnterExitNewModule);

        final double delta_flow_log_flow
                = -plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow())
                - plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow())
                + plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow()
                        - current.getData().getExitFlow() - current.getData().getFlow() + deltaEnterExitOldModule)
                + plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow()
                        + current.getData().getExitFlow() + current.getData().getFlow() - deltaEnterExitNewModule);

        return delta_exit - 2.0 * delta_exit_log_exit + delta_flow_log_flow;
    }

    @Override
    protected void updateCodelength(final Node current, final DeltaFlow oldModuleDelta, final DeltaFlow newModuleDelta) {
        final int oldModule = oldModuleDelta.getModule();
        final int newModule = newModuleDelta.getModule();
        double deltaEnterExitOldModule = oldModuleDelta.getDeltaEnter() + oldModuleDelta.getDeltaExit();
        double deltaEnterExitNewModule = newModuleDelta.getDeltaEnter() + newModuleDelta.getDeltaExit();

        // Double the effect as each link works in both directions.
        deltaEnterExitOldModule *= 2;
        deltaEnterExitNewModule *= 2;

        enterFlow
                -= moduleFlowData[oldModule].getEnterFlow()
                + moduleFlowData[newModule].getEnterFlow();
        exitLogExit
                -= plogp(moduleFlowData[oldModule].getExitFlow())
                + plogp(moduleFlowData[newModule].getExitFlow());
        flowLogFlow
                -= plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow())
                + plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow());

        moduleFlowData[oldModule].sub(current.getData());
        moduleFlowData[newModule].add(current.getData());

        moduleFlowData[oldModule].setExitFlow(moduleFlowData[oldModule].getExitFlow() + deltaEnterExitOldModule);
        moduleFlowData[newModule].setExitFlow(moduleFlowData[newModule].getExitFlow() - deltaEnterExitNewModule);

        enterFlow
                += moduleFlowData[oldModule].getEnterFlow()
                + moduleFlowData[newModule].getEnterFlow();
        exitLogExit
                += plogp(moduleFlowData[oldModule].getExitFlow())
                + plogp(moduleFlowData[newModule].getExitFlow());
        flowLogFlow
                += plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow())
                + plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow());

        enterFlowLogEnterFlow = plogp(enterFlow);

        indexCodelength = enterFlowLogEnterFlow - exitLogExit - exitNetworkFlowLogExitNetworkFlow;
        moduleCodelength = -exitLogExit + flowLogFlow - nodeFlowLogNodeFlow;
        codelength = indexCodelength + moduleCodelength;
    }

    public static class NodeFactoryUndirected implements NodeFactoryBase {

        @Override
        public NodeBase createNode() {
            return createNode("", 0, 0);
        }

        @Override
        public NodeBase createNode(final String name, final double flow, final double teleportWeight) {
            final FlowUndirected data = new FlowUndirected(flow);
            return new Node(name, data);
        }

        @Override
        public NodeBase createNode(final NodeBase node) {
            return new Node((FlowUndirected) ((Node) node).getData());
        }

        @Override
        public NodeBase createNode(final FlowBase data) {
            return new Node((FlowUndirected) data);
        }

        @Override
        public FlowBase createFlow() {
            return new FlowUndirected();
        }
    }
}
