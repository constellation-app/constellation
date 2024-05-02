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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.chinesewhispers;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.ClusterUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author algol
 */
@ActionID(category = "Cluster", id = "au.gov.asd.tac.constellation.plugins.algorithms.clustering.chinesewhispers.ChineseWhispersAction")
@ActionRegistration(displayName = "#CTL_ChineseWhispersAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/clustering/chinesewhispers/chineseWhispers.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Cluster", position = 300)
})
@NbBundle.Messages({
    "CTL_ChineseWhispersAction=Chinese Whispers"
})
public class ChineseWhispersAction extends AbstractAction {

    private final GraphNode context;

    public ChineseWhispersAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecutor.startWith(AlgorithmPluginRegistry.CLUSTER_CHINESE_WHISPERS)
                .followedBy(new ChineseWhispersCleanupPlugin())
                .executeWriteLater(context.getGraph());
    }

    /**
     * Color the clusters and arrange graph after clustering has been performed.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static class ChineseWhispersCleanupPlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Chinese Whispers: Cleanup";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            // When the clustering is done, make the graph look nice.
            final int clusterId = ClusteringConcept.VertexAttribute.CHINESE_WHISPERS_CLUSTER.ensure(graph);
            final int vxColorId = ClusteringConcept.VertexAttribute.CHINESE_WHISPERS_COLOR.ensure(graph);
            final int txColorId = ClusteringConcept.TransactionAttribute.CHINESE_WHISPERS_COLOR.ensure(graph);
            ClusterUtilities.colorClusters(graph, clusterId, vxColorId, txColorId);
            ClusterUtilities.explodeGraph(graph, clusterId);
        }

    }
}
