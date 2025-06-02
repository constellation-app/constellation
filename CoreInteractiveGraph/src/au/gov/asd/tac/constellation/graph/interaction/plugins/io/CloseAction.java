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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Close the current graph window.
 *
 * @author altair
 * @author antares
 */
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.functionality.CloseAction")
@ActionRegistration(displayName = "#CTL_CloseAction",
        iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/io/closeGraph.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 500),
    @ActionReference(path = "Shortcuts", name = "C-W")
})
@Messages("CTL_CloseAction=Close")
public final class CloseAction extends AbstractAction {
    
    private static final Logger LOGGER = Logger.getLogger(CloseAction.class.getName());

    private final GraphNode context;

    /**
     * close the current graph window
     *
     * @param context The graph context.
     */
    public CloseAction(final GraphNode context) {
        this.context = context;

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            PluginExecution
                    .withPlugin(InteractiveGraphPluginRegistry.CLOSE_GRAPH)
                    .withParameter(CloseGraphPlugin.GRAPH_PARAMETER_ID, context.getGraph().getId())
                    .executeNow(context.getGraph());
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Close Graph interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final PluginException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}
