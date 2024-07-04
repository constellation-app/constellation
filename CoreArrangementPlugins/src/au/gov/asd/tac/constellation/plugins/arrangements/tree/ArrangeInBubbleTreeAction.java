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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.views.namedselection.state.NamedSelectionState;
import au.gov.asd.tac.constellation.views.namedselection.utilities.SelectNamedSelectionPanel;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author algol
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.tree.ArrangeInBubbleTreeAction")
@ActionRegistration(displayName = "#CTL_ArrangeInBubbleTreeAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/tree/resources/arrangeInTree3D.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Arrange", position = 1200),
    @ActionReference(path = "Shortcuts", name = "C-A-T")
})
@NbBundle.Messages("CTL_ArrangeInBubbleTreeAction=Bubble Tree 3D")
public class ArrangeInBubbleTreeAction extends AbstractAction {

    private static final String HELP_LOCATION = "au.gov.asd.tac.constellation.plugins.arrangements.trees.BubbleTreeAction";
    private final GraphNode context;

    public ArrangeInBubbleTreeAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Graph graph = context.getGraph();
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            NamedSelectionState nsState = null;
            final int namedSelectionId = rg.getAttribute(GraphElementType.VERTEX, "named_selection");
            if (namedSelectionId != Graph.NOT_FOUND) {
                final int namedSelectionStateId = rg.getAttribute(GraphElementType.META, NamedSelectionState.ATTRIBUTE_NAME);
                if (namedSelectionStateId != Graph.NOT_FOUND) {
                    nsState = rg.getObjectValue(namedSelectionStateId, 0);

                    final SelectNamedSelectionPanel ssp = new SelectNamedSelectionPanel(nsState.getNamedSelections(), "Select a named selection to represent the tree roots.");
                    final DialogDescriptor dd = new DialogDescriptor(ssp, Bundle.CTL_ArrangeInBubbleTreeAction());
                    dd.setHelpCtx(new HelpCtx(HELP_LOCATION));
                    final Object result = DialogDisplayer.getDefault().notify(dd);
                    if (result == DialogDescriptor.OK_OPTION) {
                        final long selectionId = ssp.getNamedSelectionId();

                        if (selectionId != -1) {
                            final long mask = 1L << selectionId;
                            final Set<Integer> rootVxIds = new HashSet<>();
                            for (int position = 0; position < rg.getVertexCount(); position++) {
                                final int vxId = rg.getVertex(position);

                                final long selections = rg.getLongValue(namedSelectionId, vxId);
                                if ((selections & mask) != 0) {
                                    rootVxIds.add(vxId);
                                }
                            }

                            PluginExecutor.startWith(ArrangementPluginRegistry.BUBBLE_TREE)
                                    .set(ArrangeInBubbleTreePlugin.ROOTS_PARAMETER_ID, rootVxIds)
                                    .set(ArrangeInBubbleTreePlugin.IS_MINIMAL_PARAMETER_ID, true)
                                    .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                                    .executeWriteLater(context.getGraph(), Bundle.CTL_ArrangeInBubbleTreeAction());
                        }
                    }
                }
            }

            if (nsState == null) {
                NotifyDisplayer.display("There must be a named selection to specify the tree roots", NotifyDescriptor.WARNING_MESSAGE);
            }
        } finally {
            rg.release();
        }
    }
}
