/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author sirius
 */
public class QueryManager implements LookupListener, GraphChangeListener {

    private final Lookup.Result<GraphNode> result;
    private GraphNode graphNode = null;

    private final BitMaskQueryCollection bitMasks = new BitMaskQueryCollection(GraphElementType.VERTEX);

    public QueryManager() {
        result = Utilities.actionsGlobalContext().lookupResult(GraphNode.class);
        result.addLookupListener(this);
        resultChanged(null);

        bitMasks.setQuery("Score > '5'", 0);
        bitMasks.setQuery("Count > '50'", 1);
        bitMasks.setActiveQueries(0xFFFFFFFFFFFFFFFFL);
    }

    @Override
    public final void resultChanged(LookupEvent le) {
        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        if (activatedNodes != null && activatedNodes.length == 1 && activatedNodes[0] instanceof GraphNode) {
            final GraphNode newGraphNode = ((GraphNode) activatedNodes[0]);
            setNode(newGraphNode);
        } else {
            setNode(null);
        }
    }

    private void setNode(GraphNode graphNode) {
        if (graphNode != this.graphNode) {
            if (this.graphNode != null) {
                this.graphNode.getGraph().removeGraphChangeListener(this);
            }
            this.graphNode = graphNode;
            if (this.graphNode != null) {
                this.graphNode.getGraph().addGraphChangeListener(this);
            }
        }
    }

    @Override
    public void graphChanged(GraphChangeEvent event) {
        if (graphNode != null) {
            final UpdateQueryPlugin plugin = new UpdateQueryPlugin(bitMasks);
            final Future<?> f = PluginExecution.withPlugin(plugin).executeLater(graphNode.getGraph());
            try {
                f.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static class UpdateQueryPlugin extends SimpleEditPlugin {

        private final BitMaskQueryCollection bitMaskQueries;

        public UpdateQueryPlugin(BitMaskQueryCollection bitMaskQueries) {
            this.bitMaskQueries = bitMaskQueries;
        }

        @Override
        public String getName() {
            return "Update Query";
        }

        @Override
        protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            final int bitMaskAttributeId = LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
            final int visibleMaskAttributeId = VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
            bitMaskQueries.updateBitMasks(graph, bitMaskAttributeId, visibleMaskAttributeId);
        }
    }
}
