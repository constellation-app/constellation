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
package au.gov.asd.tac.constellation.views.mapview.markers;

import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.MapPosition;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * A marker with polygon geometry.
 *
 * @author cygnus_x-1
 */
public class ConstellationPolygonMarker extends ConstellationAbstractMarker {

    public ConstellationPolygonMarker() {
        super();
    }

    public ConstellationPolygonMarker(final Location location) {
        super(location);
    }

    public ConstellationPolygonMarker(final List<Location> locations) {
        super(locations);
    }

    @Override
    public boolean draw(final PGraphics graphics, final List<MapPosition> positions, final UnfoldingMap map) {
        if (positions.isEmpty() || isHidden()) {
            return false;
        }

        graphics.pushStyle();
        graphics.strokeWeight(size == MarkerUtilities.DEFAULT_SIZE ? strokeWeight : size);
        graphics.stroke(strokeColor);
        graphics.fill(getFillColor());
        graphics.beginShape();
        positions.forEach(position -> graphics.vertex(position.x, position.y));
        graphics.endShape(PConstants.CLOSE);
        graphics.popStyle();

        return true;
    }

    @Override
    public boolean isInside(final UnfoldingMap map, final float checkX, final float checkY) {
        final List<ScreenPosition> positions = locations.stream()
                .map(location -> map.getScreenPosition(location))
                .collect(Collectors.toList());
        return isInside(checkX, checkY, positions);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ConstellationPolygonMarker other = (ConstellationPolygonMarker) obj;
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        if (!Objects.equals(this.getLocations(), other.getLocations())) {
            return false;
        }
        return Objects.equals(this.getProperties(), other.getProperties());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.getId());
        hash = 41 * hash + Objects.hashCode(this.getLocations());
        hash = 41 * hash + Objects.hashCode(this.getProperties());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("PolygonMarker @ %s", getLocation());
    }
}
