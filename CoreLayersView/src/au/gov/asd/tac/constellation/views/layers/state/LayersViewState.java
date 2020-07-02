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

import au.gov.asd.tac.constellation.views.layers.layer.LayerDescription;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores all Layer Queries currently active in the Layers View.
 *
 * @author aldebaran30701
 */
public class LayersViewState {

    private final List<LayerDescription> layers;

    public LayersViewState() {
        this((List<LayerDescription>) null);
    }

    public LayersViewState(final LayersViewState state) {
        this(state.getLayers());
    }

    public LayersViewState(final List<LayerDescription> layers) {
        this.layers = new ArrayList<>();
        if (layers != null) {
            this.layers.addAll(layers);
        }
    }

    public int getLayerCount() {
        return layers.size();
    }

    public List<LayerDescription> getLayers() {
        return layers;
    }

    public void addLayer(final LayerDescription layer) {
        layers.add(layer);
    }

    public void setLayers(final List<LayerDescription> layers) {
        this.layers.clear();
        this.layers.addAll(layers);
    }
}
