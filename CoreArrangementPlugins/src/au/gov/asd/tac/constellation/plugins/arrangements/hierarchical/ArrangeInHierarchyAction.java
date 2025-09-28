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
package au.gov.asd.tac.constellation.plugins.arrangements.hierarchical;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.views.namedselection.NamedSelection;
import au.gov.asd.tac.constellation.views.namedselection.state.NamedSelectionState;
import au.gov.asd.tac.constellation.views.namedselection.utilities.SelectNamedSelectionPanel;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
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
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.hierarchical.ArrangeInHierarchyAction")
@ActionRegistration(displayName = "#CTL_ArrangeInHierarchyAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/hierarchical/resources/arrangeInHierarchy.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Arrange", position = 400),
    @ActionReference(path = "Shortcuts", name = "C-H")
})
@NbBundle.Messages("CTL_ArrangeInHierarchyAction=Hierarchy")
public class ArrangeInHierarchyAction extends AbstractAction {

    private final GraphNode context;
    private static final String HELP_LOCATION = "au.gov.asd.tac.constellation.plugins.arrangements.hierarchy";

    public ArrangeInHierarchyAction(final GraphNode context) {
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
                    selectElementsAndRunArrangement(rg, nsState.getNamedSelections());
                }
            }
            
            if (nsState == null) {
                selectElementsAndRunArrangement(rg, null);
            }
        } finally {
            rg.release();
        }
    }

    private Set<Integer> getSelectedIds(final ReadableGraph rg) {
        final Set<Integer> selectedIds = new HashSet<>();
        final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(rg);
        for (int position = 0; position < rg.getVertexCount(); position++) {
            final int vxId = rg.getVertex(position);
            if (rg.getBooleanValue(vxSelectedAttr, vxId)) {
                selectedIds.add(vxId);
            }
        }
        return selectedIds;
    }
    
    private void selectElementsAndRunArrangement(final ReadableGraph rg, final List<NamedSelection> namedSelections) {
        final Set<Integer> rootVxIds = getSelectedIds(rg);
        final SelectNamedSelectionPanel ssp = new SelectNamedSelectionPanel(namedSelections, "Which element(s) will represent the TOP of the hierarchy ?", rootVxIds.isEmpty());
        final DialogDescriptor dd = new DialogDescriptor(ssp, Bundle.CTL_ArrangeInHierarchyAction());
        dd.setHelpCtx(new HelpCtx(HELP_LOCATION));
        final Object result = DialogDisplayer.getDefault().notify(dd);
        
        if (result == DialogDescriptor.OK_OPTION) {
            final long selectionId = ssp.getNamedSelectionId();

            if (selectionId == -2) {
                PluginExecutor.startWith(VisualGraphPluginRegistry.DESELECT_ALL)
                        .followedBy(ArrangementPluginRegistry.HIERARCHICAL)
                        .set(ArrangeInHierarchyPlugin.ROOTS_PARAMETER_ID, rootVxIds)
                        .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                        .executeWriteLater(context.getGraph(), Bundle.CTL_ArrangeInHierarchyAction());

            } else if (selectionId != -1) {
                final int namedSelectionId = rg.getAttribute(GraphElementType.VERTEX, "named_selection");
                final long mask = 1L << selectionId;
                rootVxIds.clear();
                for (int position = 0; position < rg.getVertexCount(); position++) {
                    final int vxId = rg.getVertex(position);

                    final long selections = rg.getLongValue(namedSelectionId, vxId);
                    if ((selections & mask) != 0) {
                        rootVxIds.add(vxId);
                    }
                }
                PluginExecutor.startWith(ArrangementPluginRegistry.HIERARCHICAL)
                        .set(ArrangeInHierarchyPlugin.ROOTS_PARAMETER_ID, rootVxIds)
                        .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                        .executeWriteLater(context.getGraph(), Bundle.CTL_ArrangeInHierarchyAction());
                
            }
        }
        
    }
}
