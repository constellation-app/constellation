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
package au.gov.asd.tac.constellation.views.layers.state;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openide.NotifyDescriptor;

/**
 * Stores all Layer Queries currently active in the Layers View.
 *
 * @author aldebaran30701
 */
public class LayersViewState {

    private final BitMaskQueryCollection vxQueries;
    private final BitMaskQueryCollection txQueries;
    private final List<SchemaAttribute> layerAttributes;

    public LayersViewState() {
        this((List<SchemaAttribute>) null, new BitMaskQueryCollection(GraphElementType.VERTEX),
                new BitMaskQueryCollection(GraphElementType.TRANSACTION));
    }

    public LayersViewState(final LayersViewState state) {
        this(state.getLayerAttributes(), state.vxQueries, state.txQueries);
    }

    public LayersViewState(final List<SchemaAttribute> layerAttributes,
            final BitMaskQueryCollection vxQueries, final BitMaskQueryCollection txQueries) {

        this.layerAttributes = new ArrayList<>();
        this.vxQueries = vxQueries;
        this.txQueries = txQueries;
        if (layerAttributes != null) {
            this.layerAttributes.addAll(layerAttributes);
        }
    }

    /**
     * returns the highest no of layers
     */
    public int getLayerCount() {
        return Math.max(vxQueries.getHighestQueryIndex(), txQueries.getHighestQueryIndex());
    }

    public BitMaskQueryCollection getVxQueriesCollection() {
        return vxQueries;
    }

    public BitMaskQueryCollection getTxQueriesCollection() {
        return txQueries;
    }

    public List<SchemaAttribute> getLayerAttributes() {
        return layerAttributes;
    }

    public void addLayer() {
        final int count = getLayerCount();
        if (count < BitMaskQueryCollection.MAX_QUERY_AMT) {
            vxQueries.add(new BitMaskQuery(new Query(GraphElementType.VERTEX, null), count + 1, StringUtils.EMPTY));
            txQueries.add(new BitMaskQuery(new Query(GraphElementType.TRANSACTION, null), count + 1, StringUtils.EMPTY));
        } else {
            NotifyDisplayer.display("You cannot have more than " + BitMaskQueryCollection.MAX_QUERY_AMT + " layers open", NotifyDescriptor.WARNING_MESSAGE);
        }
    }

    public void setVxLayers(final BitMaskQuery[] vxLayers) {
        this.vxQueries.setQueries(vxLayers);
    }

    public void setTxLayers(final BitMaskQuery[] txLayers) {
        this.txQueries.setQueries(txLayers);
    }

    public void extractLayerAttributes(GraphWriteMethods graph) {
        final int graphCurrentBitMaskId = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        final long currentBitmask = graph.getLongValue(graphCurrentBitMaskId, 0);

        // get attributes from vertex and transactions
        final List<SchemaAttribute> newLayerAttributes = vxQueries.getListenedAttributes(graph, currentBitmask);
        newLayerAttributes.addAll(txQueries.getListenedAttributes(graph, currentBitmask));

        this.layerAttributes.clear();
        this.layerAttributes.addAll(newLayerAttributes);
    }
}
