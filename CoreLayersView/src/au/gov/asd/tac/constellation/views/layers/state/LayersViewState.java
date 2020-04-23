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

    private List<LayerDescription> layers = new ArrayList<>();

    public LayersViewState() {
    }

    public LayersViewState(final List<LayerDescription> layers) {
        this.layers = layers;
    }

    public LayersViewState(final LayersViewState state) {
        layers = List.copyOf(state.getAllLayers());
    }

    public List<LayerDescription> getAllLayers() {
        return layers;
    }

    public int getLayerCount() {
        return layers.size();
    }

    public void addLayer(final LayerDescription layer) {
        layers.add(layer);
    }

    public void setLayers(final List<LayerDescription> layersList) {
        layers = layersList;
    }
}
