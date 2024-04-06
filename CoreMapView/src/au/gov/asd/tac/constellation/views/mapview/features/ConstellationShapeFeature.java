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
package au.gov.asd.tac.constellation.views.mapview.features;

import de.fhpotsdam.unfolding.geo.Location;
import java.util.ArrayList;
import java.util.List;

/**
 * A feature describing a shape.
 *
 * @author cygnus_x-1
 */
public class ConstellationShapeFeature extends ConstellationAbstractFeature {

    private final List<Location> locations;
    private final List<List<Location>> interiorRingLocationArray;

    public ConstellationShapeFeature(final ConstellationFeatureType type) {
        super(type);
        this.locations = new ArrayList<>();
        this.interiorRingLocationArray = new ArrayList<>();
    }

    public ConstellationShapeFeature(final ConstellationFeatureType type, final List<Location> locations) {
        super(type);
        this.locations = locations;
        this.interiorRingLocationArray = new ArrayList<>();
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void addLocations(final List<Location> locations) {
        this.locations.addAll(locations);
    }

    public void addLocation(final Location location) {
        this.locations.add(location);
    }

    public void addInteriorRing(final List<Location> interiorRingLocations) {
        this.interiorRingLocationArray.add(interiorRingLocations);
    }

    public List<List<Location>> getInteriorRings() {
        return interiorRingLocationArray;
    }
}
