/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview.providers;

import de.fhpotsdam.unfolding.core.Coordinate;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PImage;

/**
 * Stamen map.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapProvider.class, position = Integer.MAX_VALUE - 1)
public class StamanMapProvider extends MapProvider {
    
    private static final String LAYER_TONER = "toner";
    private static final String LAYER_TONER_BACKGROUND = "toner-background";
    private static final String LAYER_TONER_LITE = "toner-lite";
    private static final String LAYER_WATERCOLOR = "watercolor";

    @Override
    public String getName() {
        return "Stamen Maps";
    }

    @Override
    public int zoomLevels() {
        return 16;
    }
    
    @Override
    public PImage getTile(Coordinate coordinate) {
        return null;
    }

    @Override
    public String[] getTileUrls(Coordinate coordinate) {
        final String url = String.format(
                "http://tile.stamen.com/%s/%s.png",
                LAYER_TONER, getZoomString(coordinate));
        return new String[]{url};
    }
}
