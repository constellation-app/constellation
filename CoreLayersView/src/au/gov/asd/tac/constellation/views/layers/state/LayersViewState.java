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
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.views.layers.utilities.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.utilities.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.utilities.Query;
import java.util.ArrayList;
import java.util.List;
import org.openide.NotifyDescriptor;

/**
 * Stores all Layer Queries currently active in the Layers View.
 *
 * @author aldebaran30701
 */
public class LayersViewState {

    private final BitMaskQueryCollection queries;
    private final List<SchemaAttribute> layerAttributes;

    public LayersViewState() {
        this((List<BitMaskQuery>) null, (List<SchemaAttribute>) null, new BitMaskQueryCollection(new ArrayList<BitMaskQuery>())); // TODO: Make this pass in list of queries
    }

    public LayersViewState(final LayersViewState state) {
        this(state.getLayers(), state.getLayerAttributes(), state.queries);
    }

    public LayersViewState(final List<BitMaskQuery> layers, final List<SchemaAttribute> layerAttributes, final BitMaskQueryCollection queries) {

        this.layerAttributes = new ArrayList<>();
        this.queries = queries;
        if (layerAttributes != null) {
            this.layerAttributes.addAll(layerAttributes);
        }
    }

    public int getLayerCount() {
        return queries.getQueriesCount();
    }

    public List<BitMaskQuery> getLayers() {
        return queries.getQueries();
    }

    public BitMaskQueryCollection getQueriesCollection() {
        return queries;
    }

    public List<SchemaAttribute> getLayerAttributes() {
        return layerAttributes;
    }

    public void addLayer(final BitMaskQuery layer) {
        queries.add(layer);
    }

    public void addLayer() {
        if (getLayerCount() < 32) {
            addLayer(false, "", "");
        } else {
            NotifyDisplayer.display("You cannot have more than 32 layers open", NotifyDescriptor.WARNING_MESSAGE);
        }
    }

    public void addLayer(final boolean visibility, final String queryString, final String description) {
        // TODO: Change signature to
        // public void addLayer(final GraphElementType elementType, final String queryString, final int bitIndex, final String description) {
        queries.add(new Query(GraphElementType.VERTEX, queryString), 1, description); // bitIndex, description);
    }

    public void setLayers(final List<BitMaskQuery> layers) {
        this.queries.setQueries(layers);
    }

    public void extractLayerAttributes(GraphWriteMethods graph) {
        final int graphCurrentBitMaskId = LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        final int currentBitmask = graph.getIntValue(graphCurrentBitMaskId, 0); // TODO: Get Long value once switched to long

        setLayerAttributes(queries.getListenedAttributes(currentBitmask));
    }

    public void setLayerAttributes(final List<SchemaAttribute> layerAttributes) {
        this.layerAttributes.clear();
        this.layerAttributes.addAll(layerAttributes);
    }
}
