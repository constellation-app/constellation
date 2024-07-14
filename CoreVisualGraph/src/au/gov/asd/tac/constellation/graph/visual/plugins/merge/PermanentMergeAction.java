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
package au.gov.asd.tac.constellation.graph.visual.plugins.merge;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * This class is the main actin used to copy attributes from one field to
 * another. A UI panel is displayed to the user to select the columns.
 *
 * @author altair
 */
@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.functionality.merge.PermanentMergeAction")
@ActionRegistration(displayName = "#CTL_PermanentMergeAction",
        iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/merge/mergeNodes.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 1100)
})
@NbBundle.Messages({
    "CTL_PermanentMergeAction=Merge Nodes",
    "ErrorInsufficientSelections=There must be at least 2 nodes selected to perform a merge."
})
public final class PermanentMergeAction extends AbstractAction {

    private final GraphNode context;

    public PermanentMergeAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        new Thread(() -> {
            try {
                execute(Graph.NOT_FOUND);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * verify the number of selected nodes and then display the dialog to
     * confirm the attribute selection
     *
     * @param vxId id of the primary node
     *
     * @throws java.lang.InterruptedException if the process is interrupted
     * because it has been canceled.
     */
    public void execute(final int vxId) throws InterruptedException {
        assert !SwingUtilities.isEventDispatchThread();
        final Graph graph = context.getGraph();

        // make sure that selected node is part of selection set
        if (vxId != Graph.NOT_FOUND) {
            WritableGraph wg = graph.getWritableGraph("merge add", false);
            try {
                int attrId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
                wg.setBooleanValue(attrId, vxId, true);
            } finally {
                wg.commit();
            }
        }

        final ArrayList<Integer> selections = getSelectedVertexCount(graph);
        if (selections.size() < 2) {
            final NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.ErrorInsufficientSelections(), NotifyDescriptor.ERROR_MESSAGE);
            nd.setTitle(Bundle.CTL_PermanentMergeAction());
            DialogDisplayer.getDefault().notify(nd);
        } else {
            final PermanentMergePanel pmp = new PermanentMergePanel(graph, selections, vxId);
            final DialogDescriptor dd = new DialogDescriptor(pmp, Bundle.CTL_PermanentMergeAction());
            final Object result = DialogDisplayer.getDefault().notify(dd);
            if (result == DialogDescriptor.OK_OPTION) {
                final Plugin plugin = PluginRegistry.get(VisualGraphPluginRegistry.PERMANENT_MERGE);
                final PluginParameters params = plugin.createParameters();
                pmp.setParameterValues(params);
                PluginExecution.withPlugin(plugin).withParameters(params).executeLater(graph);
            }
        }
    }

    /**
     * This method will collect the set of node identifers into an array
     *
     * @param graph
     * @return array of selected node IDs
     */
    private ArrayList<Integer> getSelectedVertexCount(final Graph graph) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final ArrayList<Integer> list = new ArrayList<>();
            final int vxSelectedAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
            if (vxSelectedAttr != Graph.NOT_FOUND) {
                final int vxCount = rg.getVertexCount();
                for (int position = 0; position < vxCount; position++) {
                    final int vxId = rg.getVertex(position);
                    if (rg.getBooleanValue(vxSelectedAttr, vxId)) {
                        list.add(vxId);
                    }
                }
            }
            return list;

        } finally {
            rg.release();
        }
    }
}
