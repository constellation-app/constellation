/*
 * Copyright 2010-2025 Australian Signals Directorate
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

/**
 * A feature describing a point.
 *
 * @author cygnus_x-1
 */
public class ConstellationPointFeature extends ConstellationAbstractFeature {

    private Location location;

    public ConstellationPointFeature() {
        super(ConstellationFeatureType.POINT);
    }

    public ConstellationPointFeature(final Location location) {
        super(ConstellationFeatureType.POINT);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }
}
