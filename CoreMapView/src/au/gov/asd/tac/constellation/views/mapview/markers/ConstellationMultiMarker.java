/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.MapPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import processing.core.PGraphics;

/**
 * A marker made up of other markers of various geometries.
 *
 * @author cygnus_x-1
 */
public class ConstellationMultiMarker extends ConstellationAbstractMarker {

    protected final List<ConstellationAbstractMarker> markers = new ArrayList<>();

    public ConstellationMultiMarker() {
        super();
    }

    public void setMarker(final ConstellationAbstractMarker marker) {
        locations.clear();
        markers.add(marker);
    }

    public void addMarker(final ConstellationAbstractMarker marker) {
        markers.add(marker);
    }

    public void removeMarker(final ConstellationAbstractMarker marker) {
        markers.remove(marker);
    }

    public List<ConstellationAbstractMarker> getMarkers() {
        return markers;
    }

    public void setMarkers(final List<ConstellationAbstractMarker> markers) {
        this.markers.clear();
        this.markers.addAll(markers);
    }

    public void addMarkers(final List<ConstellationAbstractMarker> markers) {
        this.markers.addAll(markers);
    }

    public void removeMarkers(final List<ConstellationAbstractMarker> markers) {
        this.markers.removeAll(markers);
    }

    @Override
    public Location getLocation() {
        final Location center = new Location(0, 0);
        markers.forEach(marker -> {
            center.add(marker.getLocation());
        });
        center.div((float) markers.size());
        return center;
    }

    @Override
    public void setLocation(final float lat, final float lng) {
        throw new UnsupportedOperationException("You cannot set a location on a MultiMarker.");
    }

    @Override
    public void setLocation(final Location location) {
        throw new UnsupportedOperationException("You cannot set a location on a MultiMarker.");
    }

    @Override
    public void addLocation(final float latitude, final float longitude) {
        throw new UnsupportedOperationException("You cannot add a location on a MultiMarker.");
    }

    @Override
    public void addLocation(final Location location) {
        throw new UnsupportedOperationException("You cannot add a location on a MultiMarker.");
    }

    @Override
    public void removeLocation(final float latitude, final float longitude) {
        throw new UnsupportedOperationException("You cannot remove a location on a MultiMarker.");
    }

    @Override
    public void removeLocation(final Location location) {
        throw new UnsupportedOperationException("You cannot remove a location on a MultiMarker.");
    }

    @Override
    public List<Location> getLocations() {
        final List<Location> locations = new ArrayList<>();
        markers.forEach(marker -> {
            locations.addAll(marker.getLocations());
        });
        return locations;
    }

    @Override
    public void setLocations(final List<Location> locations) {
        throw new UnsupportedOperationException("You cannot set locations on a MultiMarker.");
    }

    @Override
    public void addLocations(final List<Location> locations) {
        throw new UnsupportedOperationException("You cannot add locations on a MultiMarker.");
    }

    @Override
    public void removeLocations(final List<Location> locations) {
        throw new UnsupportedOperationException("You cannot remove locations on a MultiMarker.");
    }

    @Override
    public double getDistanceTo(final Location location) {
        double minDistance = Double.MAX_VALUE;
        for (final Marker marker : markers) {
            final double dist = marker.getDistanceTo(location);
            minDistance = dist < minDistance ? dist : minDistance;
        }
        return minDistance;
    }

    @Override
    public void draw(final UnfoldingMap map) {
        markers.forEach(marker -> {
            marker.draw(map);
        });
    }

    @Override
    protected boolean draw(final PGraphics graphics, final List<MapPosition> positions, final UnfoldingMap map) {
        return false;
    }

    @Override
    public boolean isInside(final UnfoldingMap map, final float checkX, final float checkY) {
        boolean inside = false;
        for (final Marker marker : markers) {
            inside |= marker.isInside(map, checkX, checkY);
        }
        return inside;
    }

    @Override
    public void setHighlighted(final boolean highlighted) {
        markers.forEach(marker -> {
            marker.setHighlighted(highlighted);
        });
        this.highlighted = highlighted;
    }

    @Override
    public void setSelected(final boolean selected) {
        markers.forEach(marker -> {
            marker.setSelected(selected);
        });
        this.selected = selected;
    }

    @Override
    public void setHidden(final boolean hidden) {
        markers.forEach(marker -> {
            marker.setHidden(hidden);
        });
        this.hidden = hidden;
    }

    @Override
    public void setColor(final int color) {
        markers.forEach(marker -> {
            marker.setColor(color);
        });
    }

    @Override
    public void setStrokeColor(final int color) {
        markers.forEach(marker -> {
            marker.setStrokeColor(color);
        });
    }

    @Override
    public void setStrokeWeight(final int weight) {
        markers.forEach(marker -> {
            marker.setStrokeWeight(weight);
        });
    }

    @Override
    public void setHighlightColor(final int color) {
        markers.forEach(marker -> {
            marker.setHighlightColor(color);
        });
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ConstellationMultiMarker other = (ConstellationMultiMarker) obj;
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        if (!Objects.equals(this.getLocation(), other.getLocation())) {
            return false;
        }
        if (!Objects.equals(this.getProperties(), other.getProperties())) {
            return false;
        }
        return Objects.equals(this.getMarkers(), other.getMarkers());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.getId());
        hash = 79 * hash + Objects.hashCode(this.getLocation());
        hash = 79 * hash + Objects.hashCode(this.getProperties());
        hash = 79 * hash + Objects.hashCode(this.getMarkers());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("MultiMarker @ %s", getLocation());
    }
}
