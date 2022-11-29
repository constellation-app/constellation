/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * A tiled base map being served by an ArcGIS Server. This class will ensure
 * tiles are exported in the Web Mercator projection with the correct alignment
 * by exporting tiles as images.
 *
 * @author cygnus_x-1
 */
public abstract class EsriMapProvider extends MapProvider {

    private static final Logger LOGGER = Logger.getLogger(EsriMapProvider.class.getName());

    public enum MapServerType {
        /**
         * The {@link MapServerType#TILE} type supports ESRI tile-based mapping
         * services, this is the default.
         */
        TILE,
        /**
         * The {@link MapServerType#EXPORT} type supports ESRI mapping services
         * which allow image exports.
         */
        EXPORT
    }

    @Override
    public String getZoomString(final Coordinate coordinate) {
        return (int) coordinate.zoom
                + "/" + (int) coordinate.row
                + "/" + (int) coordinate.column;
    }

    @Override
    public PImage getTile(final Coordinate coordinate) {
        return null;
    }

    @Override
    public String[] getTileUrls(final Coordinate coordinate) {
        final String url;
        switch (getMapServerType()) {
            case TILE:
                url = String.format(
                        "%s/tile/%s",
                        getMapServer(), getZoomString(coordinate));
                break;
            case EXPORT:
                url = String.format(
                        "%s/export?f=image&format=png&size=256,256&imagesr=3857&bbox=%s",
                        getMapServer(), getBoundingBox(coordinate));
                break;
            default:
                url = "";
                break;
        }

        if (StringUtils.isBlank(url)) {
            LOGGER.log(Level.WARNING, "Tile URL, {0}, is invalid", url);
        }

        return new String[]{url};
    }

    /**
     * Calculate a bounding box to emulate tile boundaries when using the
     * {@link MapServerType#EXPORT} option.
     *
     * @param coordinate the requested tile coordinate.
     * @return a {@link String} representing a bounding box.
     */
    protected String getBoundingBox(final Coordinate coordinate) {
        final float n = PApplet.pow(2, coordinate.zoom);
        final double minLon = (coordinate.column / n) * 360 - 180;
        final double minLat = Math.atan(Math.sinh(Math.PI * (1 - 2 * (coordinate.row / n)))) * (180 / Math.PI);
        final double maxLon = ((coordinate.column + 1) / n) * 360 - 180;
        final double maxLat = Math.atan(Math.sinh(Math.PI * (1 - 2 * ((coordinate.row + 1) / n)))) * (180 / Math.PI);
        return String.format("%f,%f,%f,%f", minLon, minLat, maxLon, maxLat);
    }

    /**
     * Get the type of map server this provider is working with.
     *
     * @return a {@link MapServerType} specifying the map server type.
     */
    protected MapServerType getMapServerType() {
        return MapServerType.TILE;
    }

    /**
     * Get the map server address.
     *
     * @return a {@link String} representing the URL of the map server.
     */
    protected abstract String getMapServer();
}
