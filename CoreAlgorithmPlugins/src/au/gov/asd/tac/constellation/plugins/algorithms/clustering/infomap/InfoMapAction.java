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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.ClusterUtilities;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.ClusteringConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Cluster", id = "au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.InfoMapAction")
@ActionRegistration(displayName = "#CTL_InfoMapAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/clustering/infomap/resources/infoMap.png",
        surviveFocusChange = true)
@ActionReference(path = "Menu/Tools/Cluster", position = 400)
@Messages("CTL_InfoMapAction=Info Map")
public final class InfoMapAction implements ActionListener {

    private final GraphNode context;

    public InfoMapAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final Graph graph = context.getGraph();

        PluginExecutor.startWith(AlgorithmPluginRegistry.CLUSTER_INFO_MAP)
                .followedBy(new InfoMapCleanupPlugin())
                .executeWriteLater(graph);
    }

    /**
     * Color the clusters and arrange graph after clustering has been performed.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static class InfoMapCleanupPlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Info Map: Cleanup";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            final int clusterId = ClusteringConcept.VertexAttribute.INFOMAP_CLUSTER.ensure(graph);
            final int vxColorId = ClusteringConcept.VertexAttribute.INFOMAP_COLOR.ensure(graph);
            final int txColorId = ClusteringConcept.TransactionAttribute.INFOMAP_COLOR.ensure(graph);
            ClusterUtilities.colorClusters(graph, clusterId, vxColorId, txColorId);
            ClusterUtilities.explodeGraph(graph, clusterId);
        }

    }
}
