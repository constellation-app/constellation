/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview.utilities;

import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;

/**
 * The default implementation of a marker cache.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MarkerCache.class)
public class DefaultMarkerCache extends MarkerCache {

    @Override
    public Set<ConstellationAbstractMarker> getAllMarkers() {
        return keys();
    }

    @Override
    public void clearAllMarkers() {
        clear();
    }

    @Override
    public Set<ConstellationAbstractMarker> getSelectedMarkers() {
        return keys().stream().filter(marker -> marker.isSelected()).collect(Collectors.toSet());
    }

    @Override
    public void clearSelectedMarkers() {
        synchronized (MarkerCache.getDefault().lock) {
            getSelectedMarkers().forEach(marker -> remove(marker));
        }
    }

    @Override
    public Set<ConstellationAbstractMarker> getCustomMarkers() {
        return keys().stream().filter(marker -> marker.isCustom()).collect(Collectors.toSet());
    }

    @Override
    public void clearCustomMarkers() {
        synchronized (MarkerCache.getDefault().lock) {
            getCustomMarkers().forEach(marker -> remove(marker));
        }
    }
}
