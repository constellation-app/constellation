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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * A marker object for use in the Map View.
 *
 * @author cygnus_x-1
 */
public abstract class ConstellationAbstractMarker implements Marker {

    // constants
    protected static final int FONT_COLOR = 0;
    protected static final int FONT_SIZE = 11;
    protected static final int CHAR_WIDTH = Math.round(FONT_SIZE * 0.5F);

    protected String id;
    protected List<Location> locations;
    protected HashMap<String, Object> properties;
    protected boolean custom;
    protected boolean highlighted;
    protected boolean selected;
    protected boolean dimmed;
    protected boolean hidden;
    protected int size;
    protected int color;
    protected int customColor;
    protected int highlightColor;
    protected int selectColor;
    protected int strokeColor;
    protected int strokeWeight;

    protected ConstellationAbstractMarker() {
        this(new Location(0, 0));
    }

    protected ConstellationAbstractMarker(final Location location) {
        this(new ArrayList<>(Arrays.asList(location)));
    }

    protected ConstellationAbstractMarker(final List<Location> locations) {
        this.id = null;
        this.locations = locations;
        this.properties = new HashMap<>();
        this.custom = false;
        this.highlighted = false;
        this.selected = false;
        this.dimmed = false;
        this.hidden = false;
        this.size = MarkerUtilities.DEFAULT_SIZE;
        this.color = MarkerUtilities.DEFAULT_COLOR;
        this.customColor = MarkerUtilities.DEFAULT_CUSTOM_COLOR;
        this.highlightColor = MarkerUtilities.DEFAULT_HIGHLIGHT_COLOR;
        this.selectColor = MarkerUtilities.DEFAULT_SELECT_COLOR;
        this.strokeColor = MarkerUtilities.DEFAULT_STROKE_COLOR;
        this.strokeWeight = MarkerUtilities.DEFAULT_STROKE_WEIGHT;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Location getLocation() {
        final Location center = new Location(0, 0);
        locations.forEach(center::add);
        center.div(locations.size());
        return center;
    }

    @Override
    public void setLocation(final float latitude, final float longitude) {
        locations.clear();
        locations.add(new Location(latitude, longitude));
    }

    @Override
    public void setLocation(final Location location) {
        locations.clear();
        locations.add(location);
    }

    public void addLocation(final float latitude, final float longitude) {
        locations.add(new Location(latitude, longitude));
    }

    public void addLocation(final Location location) {
        locations.add(location);
    }

    public void removeLocation(final float latitude, final float longitude) {
        assert locations.size() > 1 : "A marker must have one or more locations.";
        locations.remove(new Location(latitude, longitude));
    }

    public void removeLocation(final Location location) {
        assert locations.size() > 1 : "A marker must have one or more locations.";
        locations.remove(location);
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(final List<Location> locations) {
        this.locations.clear();
        this.locations.addAll(locations);
    }

    public void addLocations(final List<Location> locations) {
        this.locations.addAll(locations);
    }

    public void removeLocations(final List<Location> locations) {
        this.locations.removeAll(locations);
    }

    @Override
    public double getDistanceTo(final Location location) {
        double minDistance = Double.MAX_VALUE;
        for (final Location markerLocation : locations) {
            final double distance = markerLocation.getDistance(location);
            minDistance = distance < minDistance ? distance : minDistance;
        }
        return minDistance;
    }

    public double getRadius() {
        double maxDistance = 0;
        if (locations.size() > 1) {
            for (int i = 0; i < locations.size(); i++) {
                final Location currentLocation = locations.get(i);
                for (int j = 0; j < locations.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    final Location nextLocation = locations.get(j);
                    final double distance = currentLocation.getDistance(nextLocation);
                    maxDistance = distance > maxDistance ? distance : maxDistance;
                }
            }
        }
        return maxDistance;
    }

    @Override
    public Object getProperty(final String key) {
        return properties.get(key);
    }

    @Override
    public String getStringProperty(final String key) {
        final Object value = properties.get(key);
        if (value instanceof String string) {
            return string;
        } else {
            return null;
        }
    }

    @Override
    public Integer getIntegerProperty(final String key) {
        final Object value = properties.get(key);
        if (value instanceof Integer integer) {
            return integer;
        } else {
            return null;
        }
    }

    @Override
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    @Override
    public Object setProperty(final String key, final Object value) {
        final Object previousValue = properties.get(key);
        properties.put(key, value);
        return previousValue;
    }

    @Override
    public void setProperties(final HashMap<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void draw(final UnfoldingMap map) {
        final PGraphics graphics = map.mapDisplay.getOuterPG();
        final List<MapPosition> positions = locations.stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .collect(Collectors.toList());

        final boolean validMarker = draw(graphics, positions, map);

        // draw label
        if (validMarker && getId() != null) {
            final MapPosition center = new MapPosition();
            positions.forEach(center::add);
            center.div(positions.size());
            graphics.fill(FONT_COLOR);
            graphics.textSize(FONT_SIZE);
            graphics.text(id, center.x - (CHAR_WIDTH * id.length() * 0.6F), center.y + (FONT_SIZE * 0.35F));
        }
    }

    protected int getFillColor() {
        if (isHighlighted()) {
            return isDimmed() ? MarkerUtilities.greyscale(highlightColor) : highlightColor;
        } else if (isSelected()) {
            return isDimmed() ? MarkerUtilities.greyscale(selectColor) : selectColor;
        } else if (isCustom()) {
            return isDimmed() ? MarkerUtilities.greyscale(customColor) : customColor;
        } else {
            return isDimmed() ? MarkerUtilities.greyscale(color) : color;
        }
    }

    protected abstract boolean draw(final PGraphics graphics, final List<MapPosition> positions, final UnfoldingMap map);

    protected boolean isInside(final float checkX, final float checkY, final List<? extends PVector> vectors) {
        boolean inside = false;
        for (int i = 0, j = vectors.size() - 1; i < vectors.size(); j = i++) {
            final PVector pi = vectors.get(i);
            final PVector pj = vectors.get(j);
            if ((((pi.y <= checkY) && (checkY < pj.y)) || ((pj.y <= checkY) && (checkY < pi.y)))
                    && (checkX < (pj.x - pi.x) * (checkY - pi.y) / (pj.y - pi.y) + pi.x)) {
                inside = !inside;
            }
        }
        return inside;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(final boolean custom) {
        this.custom = custom;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(final boolean highlighted) {
        this.highlighted = highlighted;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public boolean isDimmed() {
        return dimmed;
    }

    public void setDimmed(final boolean dimmed) {
        this.dimmed = dimmed;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void setColor(final int color) {
        this.color = color;
    }

    public int getCustomColor() {
        return customColor;
    }

    public void setCustomColor(final int color) {
        this.customColor = color;
    }

    public int getHighlightColor() {
        return highlightColor;
    }

    @Override
    public void setHighlightColor(final int color) {
        this.highlightColor = color;
    }

    @Override
    public void setHighlightStrokeColor(final int color) {
        throw new UnsupportedOperationException("This operation is not "
                + "supported by ConstellationAbstractMarker");
    }

    public int getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(final int color) {
        this.selectColor = color;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    @Override
    public void setStrokeColor(final int color) {
        this.strokeColor = color;
    }

    public int getStrokeWeight() {
        return strokeWeight;
    }

    @Override
    public void setStrokeWeight(final int weight) {
        this.strokeWeight = weight;
    }
}
