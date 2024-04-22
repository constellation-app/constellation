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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Experimental",
        id = "au.gov.asd.tac.constellation.testing.SynchronizerAction")
@ActionRegistration(displayName = "#CTL_SynchronizerAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Developer", position = 0)
})
@Messages("CTL_SynchronizerAction=Test Synchronizer")
public final class SynchronizerAction implements ActionListener {

    private final GraphNode context;

    public SynchronizerAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Graph graph = context.getGraph();

        final int count = 3;
        final PluginSynchronizer pluginSynchronizer = new PluginSynchronizer(count);

        for (int i = 0; i < count; i++) {
            PluginExecution.withPlugin(new SynchronizerPlugin())
                    .withParameter(SynchronizerPlugin.NAME_PARAMETER_ID, "I am number " + i)
                    .interactively(true)
                    .synchronizingOn(pluginSynchronizer)
                    .executeLater(graph);
        }
    }
}
