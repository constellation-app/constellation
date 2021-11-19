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
import de.fhpotsdam.unfolding.geo.AbstractProjection;
import de.fhpotsdam.unfolding.geo.MercatorProjection;
import de.fhpotsdam.unfolding.geo.Transformation;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import processing.core.PApplet;

/**
 * A tiled base map for use in the Map View.
 *
 * @author cygnus_x-1
 */
public abstract class MapProvider extends AbstractMapProvider {

    protected MapProvider(AbstractProjection projection) {
        super(projection);
    }

    protected MapProvider() {
        this(new MercatorProjection(26,
                new Transformation(
                        1.068070779e7, 0.0, 3.355443185e7,
                        0.0, -1.068070890e7, 3.355443057e7
                )
        ));
    }

    public String getZoomString(final Coordinate coordinate) {
        return (int) coordinate.zoom
                + "/" + (int) coordinate.column
                + "/" + (int) coordinate.row;
    }

    public boolean isDebug() {
        return false;
    }

    @Override
    public int tileWidth() {
        return 256;
    }

    @Override
    public int tileHeight() {
        return 256;
    }

    @Override
    public Coordinate sourceCoordinate(final Coordinate coordinate) {
        final float columnSize = PApplet.pow(2, coordinate.zoom);
        float wrappedColumn = coordinate.column % columnSize;
        while (wrappedColumn < 0) {
            wrappedColumn += columnSize;
        }

        final float rowSize = PApplet.pow(2, coordinate.zoom);
        float wrappedRow = coordinate.row % rowSize;
        while (wrappedRow < 0) {
            wrappedRow += rowSize;
        }

        return new Coordinate(wrappedRow, wrappedColumn, coordinate.zoom);
    }

    @Override
    public String toString() {
        return getName();
    }

    public abstract String getName();

    public abstract int zoomLevels();
}
