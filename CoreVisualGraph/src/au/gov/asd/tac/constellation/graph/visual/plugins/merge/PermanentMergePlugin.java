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
package au.gov.asd.tac.constellation.graph.visual.plugins.merge;

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author altair
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages({
    "PermanentMergePlugin=Merge Nodes Plugin",
    "ErrorInsufficientItems=There must be at least 2 nodes selected to perform a merge."
})
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public class PermanentMergePlugin extends SimpleEditPlugin implements HelpCtx.Provider {

    public static final String PRIMARY_NODE_PARAMETER_ID = PluginParameter.buildId(PermanentMergePlugin.class, "primary_vertex_id");
    public static final String SELECTED_NODES_PARAMETER_ID = PluginParameter.buildId(PermanentMergePlugin.class, "selected_vertex_ids");
    public static final String ATTTRIBUTES_PARAMETER_ID = PluginParameter.buildId(PermanentMergePlugin.class, "attributes");
    public static final String CREATE_NEW_NODE_PARAMETER_ID = PluginParameter.buildId(PermanentMergePlugin.class, "create_new_node");
    public static final String CREATE_LOOPS_PARAMETER_ID = PluginParameter.buildId(PermanentMergePlugin.class, "create_loops");
    public static final String KEEP_SIMPLE_PARAMETER_ID = PluginParameter.buildId(PermanentMergePlugin.class, "keep_simple");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> primaryNodeParam = IntegerParameterType.build(PRIMARY_NODE_PARAMETER_ID);
        primaryNodeParam.setName("Primary Vertex ID");
        primaryNodeParam.setDescription("The vertex id of the primary node");
        parameters.addParameter(primaryNodeParam);

        final PluginParameter<ObjectParameterValue> selectedNodesParam = ObjectParameterType.build(SELECTED_NODES_PARAMETER_ID);
        selectedNodesParam.setName("Selected Vertex Ids");
        selectedNodesParam.setDescription("A list of the vertex ids to merge");
        parameters.addParameter(selectedNodesParam);

        final PluginParameter<ObjectParameterValue> attributesParam = ObjectParameterType.build(ATTTRIBUTES_PARAMETER_ID);
        attributesParam.setName("Attributes");
        attributesParam.setDescription("A Map of the attributes to merge which is the attribute id to the value to replace");
        parameters.addParameter(attributesParam);

        final PluginParameter<BooleanParameterValue> createNewNodeParam = BooleanParameterType.build(CREATE_NEW_NODE_PARAMETER_ID);
        createNewNodeParam.setName("Create New Node");
        createNewNodeParam.setDescription("If True, create a new node. The default is True.");
        createNewNodeParam.setBooleanValue(true);
        parameters.addParameter(createNewNodeParam);

        final PluginParameter<BooleanParameterValue> createLoopsParam = BooleanParameterType.build(CREATE_LOOPS_PARAMETER_ID);
        createLoopsParam.setName("Create Loops");
        createLoopsParam.setDescription("If True, create loops. The default is True.");
        createLoopsParam.setBooleanValue(true);
        parameters.addParameter(createLoopsParam);

        final PluginParameter<BooleanParameterValue> keepSimpleParam = BooleanParameterType.build(KEEP_SIMPLE_PARAMETER_ID);
        keepSimpleParam.setName("Keep Simple");
        keepSimpleParam.setDescription("If True, only include directed transactions. The default is False.");
        parameters.addParameter(keepSimpleParam);

        return parameters;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.functionality.attributes.mergeNodes");
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        int selectedNode = parameters.getParameters().get(PRIMARY_NODE_PARAMETER_ID).getIntegerValue();
        @SuppressWarnings("unchecked") //SELECTED_NODES_PARAMETER will generate list of integers which extends from object type
        final List<Integer> selections = (List<Integer>) parameters.getParameters().get(SELECTED_NODES_PARAMETER_ID).getObjectValue();
        @SuppressWarnings("unchecked") //ATTRIBUTES_PARAMETER will generate map of integers to strings which extends from object type
        final Map<Integer, String> attributes = (Map<Integer, String>) parameters.getParameters().get(ATTTRIBUTES_PARAMETER_ID).getObjectValue();
        final boolean createNode = parameters.getParameters().get(CREATE_NEW_NODE_PARAMETER_ID).getBooleanValue();
        final boolean createLoops = parameters.getParameters().get(CREATE_LOOPS_PARAMETER_ID).getBooleanValue();
        final boolean keepSimple = parameters.getParameters().get(KEEP_SIMPLE_PARAMETER_ID).getBooleanValue();

        if (selections.size() > 1 || (selections.size() == 1 && !createNode && selectedNode != Graph.NOT_FOUND)) {
            if (createNode || selectedNode == Graph.NOT_FOUND) {
                selectedNode = this.createVertex(graph, attributes);
            } else if (selections.contains(selectedNode)) {
                selections.remove((Integer) selectedNode);
            }

            this.processTransactions(graph, selections, selectedNode, createLoops, keepSimple);
        } else {
            final NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.ErrorInsufficientItems(), NotifyDescriptor.ERROR_MESSAGE);
            nd.setTitle(Bundle.PermanentMergePlugin());
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    /**
     * Create a new vertex and populate its attributes with the ones selected by
     * the user
     *
     * @param graph containing the created vertex
     * @param attributes set of selected attributes
     *
     * @return vertex id
     */
    private int createVertex(final GraphWriteMethods graph, final Map<Integer, String> attributes) {
        final int vxId = graph.addVertex();
        for (final Map.Entry<Integer, String> entry : attributes.entrySet()) {
            graph.setObjectValue(entry.getKey(), vxId, entry.getValue());
        }
        return vxId;
    }

    /**
     * Transfer all the transactions from the selected nodes to the new node.
     *
     * @param wg the current graph
     * @param selections set of selected nodes to be merged
     * @param newVxId id of the new vertex
     */
    private void processTransactions(final GraphWriteMethods wg, final List<Integer> selections, final int newVxId, final boolean createLoops, final boolean keepSimple) {
        final List<Integer> transactionAttributes = new ArrayList<>();
        for (int i = 0; i < wg.getAttributeCount(GraphElementType.TRANSACTION); i++) {
            transactionAttributes.add(wg.getAttribute(GraphElementType.TRANSACTION, i));
        }

        final Set<Integer> usedNodes = new HashSet<>();
        for (final Integer selectedVxId : selections) {
            if (!wg.vertexExists(selectedVxId)) {
                continue;
            }

            // For each transaction connected to the given vertex...
            int txCount = wg.getVertexTransactionCount(selectedVxId);
            for (int position = 0; position < txCount; position++) {
                final int txId = wg.getVertexTransaction(selectedVxId, position);

                // Get transaction info.
                final int srcVxId = wg.getTransactionSourceVertex(txId);
                final int dstVxId = wg.getTransactionDestinationVertex(txId);
                final int direction = wg.getTransactionDirection(txId);

                // Skip this transaction if its replacement will be a loop and we are not creating loops.
                if (!createLoops && (srcVxId == newVxId || dstVxId == newVxId)) {
                    continue;
                }

                // Create the new transaction.
                final int newTxId;
                if ((srcVxId == selectedVxId) && (dstVxId == selectedVxId)) {
                    // Create a loop.
                    newTxId = wg.addTransaction(newVxId, newVxId, direction != Graph.UNDIRECTED);
                } else if (srcVxId == selectedVxId) {
                    if (keepSimple && usedNodes.contains(dstVxId)) {
                        continue;
                    }

                    newTxId = wg.addTransaction(newVxId, dstVxId, direction != Graph.UNDIRECTED);
                    usedNodes.add(dstVxId);
                } else {
                    if (keepSimple && usedNodes.contains(srcVxId)) {
                        continue;
                    }

                    newTxId = wg.addTransaction(srcVxId, newVxId, direction != Graph.UNDIRECTED);
                    usedNodes.add(srcVxId);
                }

                try {
                    // Copy attributes from old to new transaction.
                    for (final Integer attrId : transactionAttributes) {
                        wg.setObjectValue(attrId, newTxId, wg.getObjectValue(attrId, txId));
                    }

                    // Don't validate the transaction key here.
                    // The transaction key includes the src+dst vertex keys. The new merged vertex probably has the same keys as
                    // one of the existing vertices, which means the new transaction will have the same key as the existing
                    // transaction. Validating here will get a DuplicateKeyException.
                    // Instead, just forge ahead and duplicate the transactions. When the old vertices are removed below,
                    // the associated transactions that have been duplicated will implicitly be removed, and the problem
                    // will be gone. We then allow the graph commit to merge duplicate new transactions, and everyone's happy.
                } catch (final DuplicateKeyException ex) {
                    wg.removeTransaction(ex.getNewId());
                }
            }
        }

        // Remove all the selected vertices (and implicitly their transactions).
        for (final Integer selectedVxId : selections) {
            wg.removeVertex(selectedVxId);
        }
    }
}
