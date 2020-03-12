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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.Node;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeFactoryBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowDirectedWithTeleportation;
import static au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.InfoMath.plogp;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;

/**
 *
 * @author algol
 */
public class InfomapDirected extends InfomapGreedy {

    public final double alpha;
    public final double beta;

    public InfomapDirected(final Config config, GraphReadMethods rg) {
        super(config, new NodeFactoryDirected(), rg);

        alpha = config.teleportationProbability;
        beta = 1 - alpha;
    }

    @Override
    public void initEnterExitFlow() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    protected boolean isDirected() {
        return true;
    }

    @Override
    protected boolean hasDetailedBalance() {
        return true;
    }

    @Override
    protected InfomapBase getNewInfomapInstance(final Config config, final GraphReadMethods rg) {
        return new InfomapDirected(config, rg);
    }

    @Override
    protected void addTeleportationDeltaFlowOnOldModuleIfMove(final Node nodeToMove, final DeltaFlow oldModuleDeltaFlow) {
        final FlowDirectedWithTeleportation nodeToMoveData = (FlowDirectedWithTeleportation) nodeToMove.getData();
        final FlowDirectedWithTeleportation oldModuleFlowData = (FlowDirectedWithTeleportation) moduleFlowData[oldModuleDeltaFlow.module];
        oldModuleDeltaFlow.deltaExit += (alpha * nodeToMoveData.teleportSourceFlow + beta * nodeToMoveData.danglingFlow) * (oldModuleFlowData.teleportWeight - nodeToMoveData.teleportWeight);
        oldModuleDeltaFlow.deltaEnter += (alpha * (oldModuleFlowData.teleportSourceFlow - nodeToMoveData.teleportSourceFlow)
                + beta * (oldModuleFlowData.danglingFlow - nodeToMoveData.danglingFlow)) * nodeToMoveData.teleportWeight;
    }

    @Override
    protected void addTeleportationDeltaFlowOnNewModuleIfMove(final Node nodeToMove, final DeltaFlow newModuleDeltaFlow) {
        final FlowDirectedWithTeleportation nodeToMoveData = (FlowDirectedWithTeleportation) nodeToMove.getData();
        final FlowDirectedWithTeleportation newModuleFlowData = (FlowDirectedWithTeleportation) moduleFlowData[newModuleDeltaFlow.module];
        newModuleDeltaFlow.deltaExit += (alpha * nodeToMoveData.teleportSourceFlow + beta * nodeToMoveData.danglingFlow) * newModuleFlowData.teleportWeight;
        newModuleDeltaFlow.deltaEnter += (alpha * newModuleFlowData.teleportSourceFlow + beta * newModuleFlowData.danglingFlow) * nodeToMoveData.teleportWeight;
    }

    @Override
    protected void addTeleportationDeltaFlowIfMove(final Node current, final DeltaFlow[] moduleDeltaExits, final int numModuleLinks) {
        for (int j = 0; j < numModuleLinks; ++j) {
            final int moduleIndex = moduleDeltaExits[j].module;
            if (moduleIndex == current.index) {
                addTeleportationDeltaFlowOnOldModuleIfMove(current, moduleDeltaExits[j]);
            } else {
                addTeleportationDeltaFlowOnNewModuleIfMove(current, moduleDeltaExits[j]);
            }
        }
    }

    @Override
    protected double getDeltaCodelength(final Node current, final DeltaFlow oldModuleDelta, final DeltaFlow newModuleDelta) {
        final int oldModule = oldModuleDelta.module;
        final int newModule = newModuleDelta.module;
        final double deltaEnterExitOldModule = oldModuleDelta.deltaEnter + oldModuleDelta.deltaExit;
        final double deltaEnterExitNewModule = newModuleDelta.deltaEnter + newModuleDelta.deltaExit;

        final double delta_exit = plogp(enterFlow + deltaEnterExitOldModule - deltaEnterExitNewModule) - enterFlow_log_enterFlow;

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

        final double deltaL = delta_exit - 2.0 * delta_exit_log_exit + delta_flow_log_flow;

        return deltaL;
    }

    @Override
    protected void updateCodelength(final Node current, final DeltaFlow oldModuleDelta, final DeltaFlow newModuleDelta) {
        final int oldModule = oldModuleDelta.module;
        final int newModule = newModuleDelta.module;
        final double deltaEnterExitOldModule = oldModuleDelta.deltaEnter + oldModuleDelta.deltaExit;
        final double deltaEnterExitNewModule = newModuleDelta.deltaEnter + newModuleDelta.deltaExit;

        enterFlow
                -= moduleFlowData[oldModule].getEnterFlow()
                + moduleFlowData[newModule].getEnterFlow();
        exit_log_exit
                -= plogp(moduleFlowData[oldModule].getExitFlow())
                + plogp(moduleFlowData[newModule].getExitFlow());
        flow_log_flow
                -= plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow())
                + plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow());

        moduleFlowData[oldModule].sub(current.getData());
        moduleFlowData[newModule].add(current.getData());

        moduleFlowData[oldModule].setExitFlow(moduleFlowData[oldModule].getExitFlow() + deltaEnterExitOldModule);
        moduleFlowData[newModule].setExitFlow(moduleFlowData[newModule].getExitFlow() - deltaEnterExitNewModule);

        enterFlow
                += moduleFlowData[oldModule].getEnterFlow()
                + moduleFlowData[newModule].getEnterFlow();
        exit_log_exit
                += plogp(moduleFlowData[oldModule].getExitFlow())
                + plogp(moduleFlowData[newModule].getExitFlow());
        flow_log_flow
                += plogp(moduleFlowData[oldModule].getExitFlow() + moduleFlowData[oldModule].getFlow())
                + plogp(moduleFlowData[newModule].getExitFlow() + moduleFlowData[newModule].getFlow());

        enterFlow_log_enterFlow = plogp(enterFlow);

        indexCodelength = enterFlow_log_enterFlow - exit_log_exit - exitNetworkFlow_log_exitNetworkFlow;
        moduleCodelength = -exit_log_exit + flow_log_flow - nodeFlow_log_nodeFlow;
        codelength = indexCodelength + moduleCodelength;
    }

    public static class NodeFactoryDirected implements NodeFactoryBase {

        @Override
        public NodeBase createNode() {
            return createNode("", 0, 0);
        }

        @Override
        public NodeBase createNode(final String name, final double flow, final double teleportWeight) {
            final FlowDirectedWithTeleportation data = new FlowDirectedWithTeleportation(flow, teleportWeight);
            return new Node(name, data);
        }

        @Override
        public NodeBase createNode(final NodeBase node) {
            return new Node(node.name, (FlowDirectedWithTeleportation) ((Node) node).getData());
        }

        @Override
        public NodeBase createNode(final FlowBase data) {
            return new Node((FlowDirectedWithTeleportation) data);
        }

        @Override
        public FlowBase createFlow() {
            return new FlowDirectedWithTeleportation();
        }
    }
}
