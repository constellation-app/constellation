/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview;

import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import de.fhpotsdam.unfolding.geo.Location;

public final class MapViewState {
    private static final int DEFAULT_ZOOM = 4;
    private static final Location DEFAULT_LOCATION = new Location(3.0, 126.0);
    private final MapProvider defaultProvider;
    private MapProvider provider;
    private Location center;
    private int zoomLevel = -1;
    private boolean valid = false;

    public MapViewState(MapProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public void update(MapProvider provider, Location center, int zoomLevel) {
        this.provider = provider;
        this.center = center;
        this.zoomLevel = zoomLevel;
        this.valid = true;
    }

    public MapProvider getProvider() {
        return provider;
    }

    public Location getCenter() {
        return center;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public Location getCenterOrDefault() {
        return center != null ? center : DEFAULT_LOCATION;
    }

    public int getZoomOrDefault() {
        return zoomLevel >= 0 ? zoomLevel : DEFAULT_ZOOM;
    }

    public MapProvider getProviderOrDefault() {
        return provider != null ? provider : defaultProvider;
    }

    public boolean isValid() {
        return valid;
    }
}
