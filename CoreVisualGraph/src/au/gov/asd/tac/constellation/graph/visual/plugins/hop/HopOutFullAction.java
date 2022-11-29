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
package au.gov.asd.tac.constellation.graph.visual.plugins.hop;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Execute a full hop (keep hopping until there's nothing left to hop to). This
 * selects an entire component of the graph.
 *
 * @author algol
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.hop.HopOutFullAction")
@ActionRegistration(displayName = "#CTL_HopOutFullAction", iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/hop/resources/hop_full.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 2300)
})
@Messages("CTL_HopOutFullAction=Hop Out Full")
public final class HopOutFullAction extends AbstractAction {

    private final GraphNode context;

    /**
     * Construct a new action.
     *
     * @param context The graph context.
     */
    public HopOutFullAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(VisualGraphPluginRegistry.HOP_OUT)
                .withParameter(HopOutPlugin.HOPS_PARAMETER_ID, HopUtilities.HOP_OUT_FULL)
                .executeLater(context.getGraph());
    }
}
