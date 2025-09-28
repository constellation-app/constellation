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
 * Execute one hop (two half hops).
 *
 * @author algol
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.hop.HopOutOneAction")
@ActionRegistration(displayName = "#CTL_HopOutOneAction", iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/hop/resources/hop_one.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 2200),
    @ActionReference(path = "Shortcuts", name = "C-Right")
})
@Messages("CTL_HopOutOneAction=Hop Out One")
public final class HopOutOneAction extends AbstractAction {

    private final GraphNode context;

    /**
     * Construct a new action.
     *
     * @param context The graph context.
     */
    public HopOutOneAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(VisualGraphPluginRegistry.HOP_OUT)
                .withParameter(HopOutPlugin.HOPS_PARAMETER_ID, HopUtilities.HOP_OUT_ONE)
                .executeLater(context.getGraph());
    }
}
