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
 * Google Maps map.
 * 
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapProvider.class, position = Integer.MAX_VALUE - 4)
public class GoogleMapProvider extends MapProvider {
    
    private static final String LAYER_ROADS_ONLY = "h";
    private static final String LAYER_STREETMAP = "m";
    private static final String LAYER_STREETMAP_TERRAIN = "p";
    private static final String LAYER_STREETMAP_ALT = "r";
    private static final String LAYER_SATELLITE_ONLY = "s";
    private static final String LAYER_TERRAIN_ONLY = "t";
    private static final String LAYER_HYBRID = "y";
    
    @Override
    public String getName() {
        return "Google Maps";
    }

    @Override
    public int zoomLevels() {
        return 20;
    }
    
    @Override
    public String getZoomString(final Coordinate coordinate) {
        return "x=" + (int) coordinate.column
                + "&y=" + (int) coordinate.row
                + "&z=" + (int) coordinate.zoom;
    }

    @Override
    public PImage getTile(Coordinate coordinate) {
        return null;
    }

    @Override
    public String[] getTileUrls(Coordinate coordinate) {
        // TODO: supply an api key or this will be blocked
        final String url = String.format(
                "http://mt.google.com/vt/lyrs=%s&%s", 
                LAYER_STREETMAP, getZoomString(coordinate));
        return new String[]{url};
    }
}
