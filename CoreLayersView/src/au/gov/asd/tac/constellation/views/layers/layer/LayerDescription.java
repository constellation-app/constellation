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
package au.gov.asd.tac.constellation.views.layers.layer;

/**
 * A description about a layer for the layers view.
 *
 * @author aldebaran30701
 */
public class LayerDescription {

    public static final String DEFAULT_QUERY_STRING = "Default";
    public static final String DEFAULT_QUERY_DESCRIPTION = "Show All";

    private final int layerIndex;
    private boolean currentVisibility;
    private String query;
    private String description;

    public LayerDescription(final int layerIndex, final boolean currentVisibility, final String query, final String description) {
        this.layerIndex = layerIndex;
        this.currentVisibility = currentVisibility;
        this.query = query;
        this.description = description;
    }

    public LayerDescription(final LayerDescription copy) {
        this.layerIndex = copy.layerIndex;
        this.currentVisibility = copy.currentVisibility;
        this.query = copy.query;
        this.description = copy.description;
    }

    public int getLayerIndex() {
        return layerIndex;
    }

    public boolean getCurrentLayerVisibility() {
        return currentVisibility;
    }

    public void setCurrentLayerVisibility(final boolean value) {
        currentVisibility = value;
    }

    public String getLayerQuery() {
        return query == null ? DEFAULT_QUERY_STRING : query;
    }

    public String getLayerDescription() {
        return query == null ? DEFAULT_QUERY_DESCRIPTION : description;
    }

    public void setDescriptionText(final String newDescription) {
        description = newDescription;
    }

    public void setQueryText(final String newQuery) {
        query = newQuery;
    }
}
