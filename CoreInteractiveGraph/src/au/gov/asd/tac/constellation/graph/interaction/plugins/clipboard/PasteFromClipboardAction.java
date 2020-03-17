/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 *
 * @author algol
 */
@NbBundle.Messages({
    "# {0} - nodes pasted",
    "# {1} - transactions pasted",
    "MSG_Pasted=Nodes pasted: {0}; Transactions pasted {1}"})
public final class PasteFromClipboardAction extends AbstractAction {

    private final GraphNode context;

    public PasteFromClipboardAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Graph graph = context.getGraph();
        PluginExecution.withPlugin(new SimpleEditPlugin() {
            @Override
            protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                boolean isEmpty = wg.getVertexCount() == 0;
                PluginExecution.withPlugin(InteractiveGraphPluginRegistry.PASTE).executeNow(wg);
                if (isEmpty) {
                    PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(wg);
                }
            }

            @Override
            public String getName() {
                return "Paste";
            }
        }).executeLater(graph);
    }
}
