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

import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.MapPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * A marker representing a cluster of geographically close markers.
 *
 * @author cygnus_x-1
 */
public class ConstellationClusterMarker extends ConstellationAbstractMarker {

    private static final float MIN_RADIUS = 30F;

    private MapPosition clusterCenter = new MapPosition();
    private float clusterRadius = 0;
    private int clusterSize = 0;

    protected final List<ConstellationAbstractMarker> markers = new ArrayList<>();

    public ConstellationClusterMarker() {
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
        markers.forEach(marker -> center.add(marker.getLocation()));
        center.div(markers.size());
        return center;
    }

    @Override
    public void setLocation(final float lat, final float lng) {
        throw new UnsupportedOperationException("You cannot set a location on a Cluster Marker.");
    }

    @Override
    public void setLocation(final Location location) {
        throw new UnsupportedOperationException("You cannot set a location on a Cluster Marker.");
    }

    @Override
    public void addLocation(final float latitude, final float longitude) {
        throw new UnsupportedOperationException("You cannot add a location on a Cluster Marker.");
    }

    @Override
    public void addLocation(final Location location) {
        throw new UnsupportedOperationException("You cannot add a location on a Cluster Marker.");
    }

    @Override
    public void removeLocation(final float latitude, final float longitude) {
        throw new UnsupportedOperationException("You cannot remove a location on a Cluster Marker.");
    }

    @Override
    public void removeLocation(final Location location) {
        throw new UnsupportedOperationException("You cannot remove a location on a Cluster Marker.");
    }

    public List<Location> getCentroidLocations() {
        final List<Location> markerCentroids = new ArrayList<>();
        markers.forEach(marker -> markerCentroids.add(marker.getLocation()));
        return markerCentroids;
    }

    @Override
    public List<Location> getLocations() {
        final List<Location> markerLocations = new ArrayList<>();
        markers.forEach(marker -> markerLocations.addAll(marker.getLocations()));
        return markerLocations;
    }

    @Override
    public void setLocations(final List<Location> locations) {
        throw new UnsupportedOperationException("You cannot set locations on a Cluster Marker.");
    }

    @Override
    public void addLocations(final List<Location> locations) {
        throw new UnsupportedOperationException("You cannot add locations on a Cluster Marker.");
    }

    @Override
    public void removeLocations(final List<Location> locations) {
        throw new UnsupportedOperationException("You cannot remove locations on a Cluster Marker.");
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
        clusterSize = getCentroidLocations().size();

        final PGraphics graphics = map.mapDisplay.getOuterPG();
        final List<MapPosition> positions = getLocations().stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .toList();
        draw(graphics, positions, map);
    }

    @Override
    protected boolean draw(final PGraphics graphics, final List<MapPosition> positions, final UnfoldingMap map) {
        if (positions.isEmpty() || isHidden()) {
            return false;
        }

        clusterCenter = new MapPosition();
        positions.forEach(position -> 
            clusterCenter.add(position));
        clusterCenter.div(positions.size());

        double diameter = 0;
        if (positions.size() > 1) {
            final MapPosition minPosition = new MapPosition(
                    new float[]{Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE});
            final MapPosition maxPosition = new MapPosition(
                    new float[]{Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE});
            positions.forEach(position -> {
                minPosition.x = Math.min(position.x, minPosition.x);
                minPosition.y = Math.min(position.y, minPosition.y);
                maxPosition.x = Math.max(position.x, maxPosition.x);
                maxPosition.y = Math.max(position.y, maxPosition.y);
            });
            diameter = Math.sqrt(Math.pow((maxPosition.x - minPosition.x), 2)
                    + Math.pow((maxPosition.y - minPosition.y), 2));
        }
        clusterRadius = Math.max((float) diameter / 2, MIN_RADIUS);

        graphics.strokeWeight(size == MarkerUtilities.DEFAULT_SIZE ? strokeWeight : size);
        graphics.stroke(strokeColor);
        graphics.fill(getFillColor());
        graphics.ellipseMode(PConstants.RADIUS);
        graphics.ellipse(clusterCenter.x, clusterCenter.y, clusterRadius, clusterRadius);

        final String clusterLabel = String.valueOf(clusterSize);
        graphics.fill(FONT_COLOR);
        graphics.textSize(FONT_SIZE);
        graphics.text(clusterLabel,
                clusterCenter.x - (CHAR_WIDTH * clusterLabel.length() * 0.6F),
                clusterCenter.y + (FONT_SIZE * 0.35F));

        return true;
    }

    @Override
    public boolean isInside(final UnfoldingMap map, final float checkX, final float checkY) {
        return Math.pow((checkX - clusterCenter.x), 2)
                + Math.pow((checkY - clusterCenter.y), 2) <= Math.pow(clusterRadius, 2);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ConstellationClusterMarker other = (ConstellationClusterMarker) obj;
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
        return String.format("ClusterMarker @ %s", getLocation());
    }
}
