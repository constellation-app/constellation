/*
 * Copyright 2010-2026 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
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

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_InfoMapGeneralAction(), params, HELP_LOCATION);
        dialog.setSize(SIZE);
        dialog.showAndWait();
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            final Graph graph = context.getGraph();
            PluginExecutor.startWith(plugin)
                    .set(params)
                    .followedBy(AlgorithmPluginRegistry.CLUSTER_INFO_MAP_CLEANUP)
                    .executeWriteLater(graph);
        }
    }
}
