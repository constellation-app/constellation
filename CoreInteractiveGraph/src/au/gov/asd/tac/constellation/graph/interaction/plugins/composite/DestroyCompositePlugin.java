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
package au.gov.asd.tac.constellation.graph.interaction.plugins.composite;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.CompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.utilities.CompositeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
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
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * This plugin destroys a single contracted or expanded composite node. It is
 * added to the right-click context menu for nodes that are composites or
 * constituents of composite nodes.
 *
 * @author twilight_sparkle
 */
@ServiceProviders({
    @ServiceProvider(service = ContextMenuProvider.class, position = 600),
    @ServiceProvider(service = Plugin.class)
})
@NbBundle.Messages("DestroyCompositePlugin=Destroy Composite")
@PluginInfo(pluginType = PluginType.DELETE, tags = {PluginTags.DELETE})
public class DestroyCompositePlugin extends SimpleEditPlugin implements ContextMenuProvider {

    private int selectedItem;

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final int compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(graph);
        final int uniqueIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);
        CompositeUtilities.destroyComposite(graph, compositeAttr, uniqueIdAttr, selectedItem);
        PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
    }

    @Override
    public void selectItem(String item, final Graph graph, GraphElementType elementType, int elementId, final Vector3f unprojected) {
        selectedItem = elementId;
        PluginExecution.withPlugin(this).executeLater(graph);
    }

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        final int compositeAttr = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.get(graph);
        if (elementType == GraphElementType.VERTEX && compositeAttr != Graph.NOT_FOUND) {
            final CompositeNodeState compositeNodeState = (CompositeNodeState) graph.getObjectValue(compositeAttr, entity);
            if (compositeNodeState != null) {
                return Arrays.asList("Destroy Composite");
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }
}
