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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.util.BitSet;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * An action to remove blazes from a selection of vertices.
 *
 * @author algol
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.blaze.RemoveBlazeAction")
@ActionRegistration(displayName = "#CTL_RemoveBlazeAction", iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/removeblaze.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display", position = 700)
})
@NbBundle.Messages("CTL_RemoveBlazeAction=Remove Blazes")
public final class RemoveBlazeAction extends AbstractAction {

    private final GraphNode context;

    /**
     * Construct a new action.
     *
     * @param context Graph node.
     */
    public RemoveBlazeAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final BitSet vertices = new BitSet();
        final ReadableGraph rg = context.getGraph().getReadableGraph();
        try {
            final int selectedAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
            final int vxCount = rg.getVertexCount();
            for (int position = 0; position < vxCount; position++) {
                final int vxId = rg.getVertex(position);

                final boolean selected = rg.getBooleanValue(selectedAttr, vxId);
                if (selected) {
                    vertices.set(vxId);
                }
            }
        } finally {
            rg.release();
        }

        PluginExecution.withPlugin(VisualGraphPluginRegistry.REMOVE_BLAZE)
                .withParameter(BlazeUtilities.VERTEX_IDS_PARAMETER_ID, vertices)
                .executeLater(context.getGraph());
    }
}
