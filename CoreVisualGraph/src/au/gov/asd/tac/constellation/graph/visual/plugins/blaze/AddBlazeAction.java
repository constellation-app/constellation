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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.event.ActionEvent;
import java.util.BitSet;
import javafx.util.Pair;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Action to add default blazes to selected vertices.
 *
 * @author algol
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.blaze.AddBlazeAction")
@ActionRegistration(displayName = "#CTL_AddBlazeAction", iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_blue.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display", position = 600, separatorBefore = 599)
})
@NbBundle.Messages("CTL_AddBlazeAction=Add Blazes")
public final class AddBlazeAction extends AbstractAction {

    private final GraphNode context;

    public AddBlazeAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Pair<BitSet, ConstellationColor> selectionResult = BlazeUtilities.getSelection(context.getGraph(), null);
        final BitSet selectedVertices = selectionResult.getKey();
        final ConstellationColor blazeColor = selectionResult.getValue();

        if (!selectedVertices.isEmpty()) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE)
                    .withParameter(BlazeUtilities.VERTEX_IDS_PARAMETER_ID, selectedVertices)
                    .withParameter(BlazeUtilities.COLOR_PARAMETER_ID, blazeColor)
                    .executeLater(context.getGraph());
        }
    }
}
