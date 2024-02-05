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
package au.gov.asd.tac.constellation.graph.interaction.plugins.composite;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.CompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.ExpandedCompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.utilities.CompositeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.GraphContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * This plugin makes a composite node from the currently selected nodes. It is
 * added to the right-click context menu for any currently selected nodes.
 *
 * @author twilight_sparkle
 */
@ServiceProviders({
    @ServiceProvider(service = GraphContextMenuProvider.class, position = 300),
    @ServiceProvider(service = Plugin.class)
})
@Messages("CreateCompositeFromSelectionPlugin=Composite Selected Nodes")
@PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
public class CreateCompositeFromSelectionPlugin extends SimpleEditPlugin implements GraphContextMenuProvider {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (selectedAttr != Graph.NOT_FOUND) {
            final Set<Integer> selectedVerts = new HashSet<>();
            for (int i = 0; i < graph.getVertexCount(); i++) {
                final int vxId = graph.getVertex(i);
                if (graph.getBooleanValue(selectedAttr, vxId)) {
                    selectedVerts.add(vxId);
                }
            }

            if (selectedVerts.size() > 1) {

                final int compositeStateAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.ensure(graph);
                final int uniqueIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);
                final int identifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);

                // We first destroy any composites which are selected or expanded and have constituents selected.
                // For any composites, we also add any expanded ids and remove the destroyed composite id from the list of selected ids.
                final Set<Integer> addedVerts = new HashSet<>();
                final Set<Integer> removedVerts = new HashSet<>();
                selectedVerts.forEach(id -> {

                    final List<Integer> resultingVerts = CompositeUtilities.destroyComposite(graph, compositeStateAttr, uniqueIdAttr, id);
                    if (!resultingVerts.isEmpty()) {
                        removedVerts.add(id);
                        addedVerts.addAll(resultingVerts);
                    }
                });
                // NOTE:: Remove before adding, because of id reuse!
                selectedVerts.removeAll(removedVerts);
                selectedVerts.addAll(addedVerts);

                final String compositeIdentifier = String.format("%s + %d more...", graph.getStringValue(identifierAttr, selectedVerts.iterator().next()), selectedVerts.size() - 1);

                String copyId = "";
                for (int primarykeyAttr : graph.getPrimaryKey(GraphElementType.VERTEX)) {
                    final String val = graph.getStringValue(primarykeyAttr, selectedVerts.iterator().next());
                    copyId += graph.getAttributeName(primarykeyAttr) + "<" + StringUtils.defaultString(val) + ">";
                }
                final String compositeId = copyId;

                // Make a record store representing the new composite that is about to be added
                RecordStore newCompositeStore = new GraphRecordStore();
                newCompositeStore.add();
                newCompositeStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, compositeId);
                newCompositeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, compositeIdentifier);
                newCompositeStore.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, SchemaVertexTypeUtilities.getDefaultType());

                // Construct and set an expanded composite node state for each of the nodes that will constitute the composite.
                selectedVerts.forEach(id -> {
                    ExpandedCompositeNodeState expandedState = new ExpandedCompositeNodeState(newCompositeStore, compositeId, true, selectedVerts.size());
                    graph.setObjectValue(compositeStateAttr, id, new CompositeNodeState(id, expandedState));
                });

                // Create the composite by calling contract on the first node's expanded composite state.
                ((CompositeNodeState) graph.getObjectValue(compositeStateAttr, selectedVerts.iterator().next())).expandedState.contract(graph);
            }
        }

        PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);

    }

    @Override
    public void selectItem(String item, final Graph graph, GraphElementType elementType, int elementId, final Vector3f unprojected) {
        PluginExecution.withPlugin(this).executeLater(graph);
    }

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (selectedAttr != Graph.NOT_FOUND && elementType == GraphElementType.VERTEX && graph.getBooleanValue(selectedAttr, entity)) {
            return Arrays.asList("Composite Selected Nodes");
        } else {
            return Collections.emptyList();
        }
    }
}
