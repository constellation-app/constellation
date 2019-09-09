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
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Bing Maps map.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapProvider.class, position = Integer.MAX_VALUE - 5)
public class BingMapProvider extends MapProvider {
    
    private static final String LAYER_ROADS = "r";
    private static final String LAYER_ARIAL = "a";
    private static final String LAYER_HYBRID = "h";

    @Override
    public String getName() {
        return "Bing Maps";
    }

    @Override
    public int zoomLevels() {
        return 20;
    }

    @Override
    public String getZoomString(final Coordinate coordinate) {
        final String y = PApplet.binary((int) coordinate.row, (int) coordinate.zoom);
        final String x = PApplet.binary((int) coordinate.column, (int) coordinate.zoom);
        final StringBuilder quadkey = new StringBuilder();
        for (int i = 0; i < coordinate.zoom; i++) {
            quadkey.append(PApplet.unbinary(String.format("%s%s", y.charAt(i), x.charAt(i))));
        }
        return quadkey.toString();
    }

    @Override
    public PImage getTile(Coordinate coordinate) {
        return null;
    }

    @Override
    public String[] getTileUrls(Coordinate coordinate) {
        final String url = String.format(
                "http://%s%d.ortho.tiles.virtualearth.net/tiles/r%s.png?g=90&shading=hill",
                LAYER_ROADS, (int) random(0, 4), getZoomString(coordinate));
        return new String[]{url};
    }
}
