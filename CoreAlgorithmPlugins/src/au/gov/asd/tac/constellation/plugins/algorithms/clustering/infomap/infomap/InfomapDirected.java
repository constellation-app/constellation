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
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.traits.FlowDirectedWithTeleportation;
import static au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.InfoMath.plogp;

/**
 *
 * @author algol
 */
public class InfomapDirected extends InfomapGreedy {

    public final double alpha;
    public final double beta;

    public InfomapDirected(final Config config, final GraphReadMethods rg) {
        super(config, new NodeFactoryDirected(), rg);

        alpha = config.getTeleportationProbability();
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
        final FlowDirectedWithTeleportation oldModuleFlowData = (FlowDirectedWithTeleportation) moduleFlowData[oldModuleDeltaFlow.getModule()];
        oldModuleDeltaFlow.setDeltaExit(oldModuleDeltaFlow.getDeltaExit() + ((alpha * nodeToMoveData.getTeleportSourceFlow() + beta * nodeToMoveData.getDanglingFlow()) * (oldModuleFlowData.getTeleportWeight() - nodeToMoveData.getTeleportWeight())));
        oldModuleDeltaFlow.setDeltaEnter(oldModuleDeltaFlow.getDeltaEnter()
                + ((alpha * (oldModuleFlowData.getTeleportSourceFlow() - nodeToMoveData.getTeleportSourceFlow()) + beta * (oldModuleFlowData.getDanglingFlow() - nodeToMoveData.getDanglingFlow()))
                * nodeToMoveData.getTeleportWeight()));
    }

    @Override
    protected void addTeleportationDeltaFlowOnNewModuleIfMove(final Node nodeToMove, final DeltaFlow newModuleDeltaFlow) {
        final FlowDirectedWithTeleportation nodeToMoveData = (FlowDirectedWithTeleportation) nodeToMove.getData();
        final FlowDirectedWithTeleportation newModuleFlowData = (FlowDirectedWithTeleportation) moduleFlowData[newModuleDeltaFlow.getModule()];
        newModuleDeltaFlow.setDeltaExit(newModuleDeltaFlow.getDeltaExit() + ((alpha * nodeToMoveData.getTeleportSourceFlow() + beta * nodeToMoveData.getDanglingFlow()) * newModuleFlowData.getTeleportWeight()));
        newModuleDeltaFlow.setDeltaEnter(newModuleDeltaFlow.getDeltaEnter() + ((alpha * newModuleFlowData.getTeleportSourceFlow() + beta * newModuleFlowData.getDanglingFlow()) * nodeToMoveData.getTeleportWeight()));
    }

    @Override
    protected void addTeleportationDeltaFlowIfMove(final Node current, final DeltaFlow[] moduleDeltaExits, final int numModuleLinks) {
        for (int j = 0; j < numModuleLinks; ++j) {
            final int moduleIndex = moduleDeltaExits[j].getModule();
            if (moduleIndex == current.getIndex()) {
                addTeleportationDeltaFlowOnOldModuleIfMove(current, moduleDeltaExits[j]);
            } else {
                addTeleportationDeltaFlowOnNewModuleIfMove(current, moduleDeltaExits[j]);
            }
        }
    }

    @Override
    protected double getDeltaCodelength(final Node current, final DeltaFlow oldModuleDelta, final DeltaFlow newModuleDelta) {
        final int oldModule = oldModuleDelta.getModule();
        final int newModule = newModuleDelta.getModule();
        final double deltaEnterExitOldModule = oldModuleDelta.getDeltaEnter() + oldModuleDelta.getDeltaExit();
        final double deltaEnterExitNewModule = newModuleDelta.getDeltaEnter() + newModuleDelta.getDeltaExit();

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
        final double deltaEnterExitOldModule = oldModuleDelta.getDeltaEnter() + oldModuleDelta.getDeltaExit();
        final double deltaEnterExitNewModule = newModuleDelta.getDeltaEnter() + newModuleDelta.getDeltaExit();

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
            return new Node(node.getName(), (FlowDirectedWithTeleportation) ((Node) node).getData());
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
