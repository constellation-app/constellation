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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
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
    @ActionReference(path = "Toolbars/Visualisation", position = 600)
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

        PluginExecution.withPlugin(new SimpleEditPlugin("K-Truss") {
            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final TopComponent tc = WindowManager.getDefault().findTopComponent(KTrussControllerTopComponent.class.getSimpleName());
                        if (tc != null) {
                            if (!tc.isOpened()) {
                                tc.open();
                            }
                            tc.setEnabled(true);
                            tc.requestActive();
                        }
                    }
                });
            }
        }).executeLater(graph);
    }
}
