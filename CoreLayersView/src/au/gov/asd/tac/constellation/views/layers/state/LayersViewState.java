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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.views.layers.layer.LayerDescription;
import au.gov.asd.tac.constellation.views.layers.layer.LayerEvaluator;
import java.util.ArrayList;
import java.util.List;
import org.openide.NotifyDescriptor;

/**
 * Stores all Layer Queries currently active in the Layers View.
 *
 * @author aldebaran30701
 */
public class LayersViewState {

    private final List<LayerDescription> layers;
    private final List<SchemaAttribute> layerAttributes;

    public LayersViewState() {
        this((List<LayerDescription>) null, (List<SchemaAttribute>) null);
    }

    public LayersViewState(final LayersViewState state) {
        this(state.getLayers(), state.getLayerAttributes());
    }

    public LayersViewState(final List<LayerDescription> layers, final List<SchemaAttribute> layerAttributes) {
        this.layers = new ArrayList<>();
        this.layerAttributes = new ArrayList<>();

        if (layers != null) {
            this.layers.addAll(layers);
        }
        if (layerAttributes != null) {
            this.layerAttributes.addAll(layerAttributes);
        }
    }

    public int getLayerCount() {
        return layers.size();
    }

    public List<LayerDescription> getLayers() {
        return layers;
    }

    public List<SchemaAttribute> getLayerAttributes() {
        return layerAttributes;
    }

    public void addLayer(final LayerDescription layer) {
        layers.add(layer);
    }

    public void addLayer() {
        if (layers.size() < 32) {
            addLayer(false, "", "");
        } else {
            NotifyDisplayer.display("You cannot have more than 32 layers open", NotifyDescriptor.WARNING_MESSAGE);
        }
    }

    public void addLayer(final boolean visibility, final String name, final String description) {
        layers.add(new LayerDescription(layers.size() + 1, visibility, name, description));
    }

    public void setLayers(final List<LayerDescription> layers) {
        this.layers.clear();
        this.layers.addAll(layers);
    }

    public void extractLayerAttributes(GraphWriteMethods graph) {
        final List<SchemaAttribute> attributes = new ArrayList<>();
        for (final LayerDescription layer : layers) {
            attributes.addAll(LayerEvaluator.getQueryAttributes(graph, layer.getLayerQuery()));
        }
        setLayerAttributes(attributes);
    }

    public void setLayerAttributes(final List<SchemaAttribute> layerAttributes) {
        this.layerAttributes.clear();
        this.layerAttributes.addAll(layerAttributes);
    }
}
