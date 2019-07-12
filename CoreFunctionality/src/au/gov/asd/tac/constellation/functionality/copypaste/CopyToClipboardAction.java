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
package au.gov.asd.tac.constellation.functionality.copypaste;

import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginExecution;
import au.gov.asd.tac.constellation.pluginframework.PluginGraphs;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.templates.SimplePlugin;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle.Messages;

/**
 * Implement copying selected elements of a graph to the clipboard.
 * <p>
 * There are two pieces of functionality being implemented: copying the graph to
 * the local clipboard, and copying text from the graph to the system clipboard.
 *
 * @author algol
 */
@Messages({
    "# {0} - nodes copied",
    "# {1} - transactions copied",
    "MSG_Copied=Nodes copied: {0}; Transactions copied {1}."
})
public final class CopyToClipboardAction extends AbstractAction {

    private final GraphNode context;

    public CopyToClipboardAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Graph graph = context.getGraph();

        // TODO Make this a SimpleReadPlugin when sirius allows for it in the framework.
        PluginExecution.withPlugin(new SimplePlugin("Copy To Clipboard") {
            @Override
            protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                final Graph graph = graphs.getGraph();
                PluginExecution.withPlugin(CorePluginRegistry.COPY).executeNow(graph);

            }
        }).executeLater(graph);
    }
}
