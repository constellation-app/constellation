/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/*
 * Setup and withPlugin a k-truss action
 */
@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss.KTrussAction")
@ActionRegistration(displayName = "#CTL_KTrussAction", iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/clustering/ktruss/ktruss.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Toolbars/Display", position = 600)
})
@Messages("CTL_KTrussAction=K-Truss")
public final class KTrussAction extends AbstractAction {

    private final GraphNode context;

    public KTrussAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Graph graph = context.getGraph();
        PluginExecution.withPlugin(new KTrussPlugin()).executeLater(graph);
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.GENERAL})
    public static class KTrussPlugin extends SimpleReadPlugin {

        @Override
        public String getName() {
            return "K-Truss";
        }

        @Override
        protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            SwingUtilities.invokeLater(() -> {
                final TopComponent tc = WindowManager.getDefault().findTopComponent(KTrussControllerTopComponent.class.getSimpleName());
                if (tc != null) {
                    if (!tc.isOpened()) {
                        tc.open();
                    }
                    tc.setEnabled(true);
                    tc.requestActive();
                }
            });
        }
    }
}
