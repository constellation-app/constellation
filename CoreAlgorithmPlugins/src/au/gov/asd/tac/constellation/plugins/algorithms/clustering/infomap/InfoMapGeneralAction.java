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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.ClusterUtilities;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Cluster", id = "au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.InfoMapGeneralAction")
@ActionRegistration(displayName = "#CTL_InfoMapGeneralAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/clustering/infomap/resources/infoMapOptions.png")
@ActionReference(path = "Menu/Tools/Cluster", position = 500)
@Messages("CTL_InfoMapGeneralAction=Info Map...")
public final class InfoMapGeneralAction implements ActionListener {

    private final GraphNode context;

    private static final Dimension SIZE = new Dimension(550, 250);
    
    private static final String HELP_LOCATION = "au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.InfoMap";

    public InfoMapGeneralAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final Plugin plugin = PluginRegistry.get(AlgorithmPluginRegistry.CLUSTER_INFO_MAP);
        final PluginParameters params = plugin.createParameters();
        final Graph graph = context.getGraph();

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_InfoMapGeneralAction(), params, HELP_LOCATION);
        dialog.setSize(SIZE);
        dialog.showAndWait();
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            PluginExecutor.startWith(plugin)
                    .set(params)
                    .followedBy(new InfoMapGeneralPlugin())
                    .executeWriteLater(graph);
        }
    }

    /**
     * Color the clusters and arrange graph after clustering has been performed.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static class InfoMapGeneralPlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Info Map: General";
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
