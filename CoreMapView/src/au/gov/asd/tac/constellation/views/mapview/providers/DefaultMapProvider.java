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

import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import de.fhpotsdam.unfolding.core.Coordinate;
import de.fhpotsdam.unfolding.tiles.MBTilesLoaderUtils;
import java.io.File;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * The Default Map.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapProvider.class, position = Integer.MAX_VALUE)
public class DefaultMapProvider extends MapProvider {

    @Override
    public String getName() {
        return "Default Map";
    }

    @Override
    public int zoomLevels() {
        return 6;
    }

    @Override
    public String getZoomString(final Coordinate coordinate) {
        final float gridSize = PApplet.pow(2, coordinate.zoom);
        final float negativeRow = gridSize - coordinate.row - 1;
        return (int) coordinate.zoom + "/" + (int) coordinate.column + "/" + (int) negativeRow;
    }

    @Override
    public PImage getTile(final Coordinate coordinate) {
        final File defaultMap = ConstellationInstalledFileLocator.locate(
                "modules/ext/defaultMap.mbtiles",
                "au.gov.asd.tac.constellation.views.mapview",
                DefaultMapProvider.class.getProtectionDomain());
        final String connection = String.format("jdbc:sqlite:%s", defaultMap.getAbsolutePath());
        final int zoom = (int) coordinate.zoom;
        final float gridSize = PApplet.pow(2, coordinate.zoom);
        final float negativeRow = gridSize - coordinate.row - 1;
        final int row = (int) negativeRow;
        final int column = (int) coordinate.column;
        return MBTilesLoaderUtils.getMBTile(column, row, zoom, connection);
    }

    @Override
    public String[] getTileUrls(final Coordinate coordinate) {
        return new String[0];  
    }
}
