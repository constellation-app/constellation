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
import processing.core.PApplet;
import processing.core.PImage;

/**
 * A tiled base map being served by an ArcGIS Server. This class will ensure
 * tiles are exported in the Web Mercator projection with the correct alignment.
 *
 * @author cygnus_x-1
 */
public abstract class ArcgisMapProvider extends MapProvider {

    @Override
    public int tileWidth() {
        return 256;
    }

    @Override
    public int tileHeight() {
        return 256;
    }

    @Override
    public PImage getTile(final Coordinate coordinate) {
        return null;
    }

    @Override
    public String[] getTileUrls(final Coordinate coordinate) {
        final String url = String.format(
                "%s/export?f=image&format=png&size=256,256&imagesr=3857&bbox=%s",
                getMapServer(), getBoundingBox(coordinate));
        return new String[]{url};
    }

    protected String getBoundingBox(final Coordinate coordinate) {
        final float n = PApplet.pow(2, coordinate.zoom);
        final double minLon = (coordinate.column / n) * 360 - 180;
        final double minLat = Math.atan(Math.sinh(Math.PI * (1 - 2 * (coordinate.row / n)))) * (180 / Math.PI);
        final double maxLon = ((coordinate.column + 1) / n) * 360 - 180;
        final double maxLat = Math.atan(Math.sinh(Math.PI * (1 - 2 * ((coordinate.row + 1) / n)))) * (180 / Math.PI);
        return String.format("%f,%f,%f,%f", minLon, minLat, maxLon, maxLat);
    }

    public abstract String getMapServer();
}
