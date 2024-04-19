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
package au.gov.asd.tac.constellation.views.mapview.utilities;

import au.gov.asd.tac.constellation.views.mapview.features.ConstellationAbstractFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationMultiFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationPointFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An object representing a unique geographic feature.
 *
 * @author cygnus_x-1
 */
public class FeatureKey {

    private final Class<? extends ConstellationAbstractFeature> featureClass;
    private final List<Location> featureLocations;

    public FeatureKey(final ConstellationAbstractFeature multiFeature) {
        switch (multiFeature.getType()) {
            case POINT -> {
                this.featureClass = ConstellationPointFeature.class;
                this.featureLocations = getLocations(multiFeature);
            }
            case LINE, POLYGON -> {
                this.featureClass = ConstellationShapeFeature.class;
                this.featureLocations = getLocations(multiFeature);
            }
            case MULTI, CLUSTER -> {
                this.featureClass = ConstellationMultiFeature.class;
                this.featureLocations = ((ConstellationMultiFeature) multiFeature).getFeatures().stream()
                        .map(feature -> getLocations(feature)).flatMap(List::stream).toList();
            }
            default -> {
                this.featureClass = null;
                this.featureLocations = null;
            }
        }
    }

    private List<Location> getLocations(final ConstellationAbstractFeature feature) {
        return switch (feature.getType()) {
            case POINT -> Arrays.asList(((ConstellationPointFeature) feature).getLocation());
            case LINE, POLYGON -> ((ConstellationShapeFeature) feature).getLocations();
            default -> new ArrayList<>();
        };
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.featureClass);
        hash = 41 * hash + Objects.hashCode(this.featureLocations);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FeatureKey other = (FeatureKey) obj;
        if (!Objects.equals(featureClass, other.featureClass)) {
            return false;
        }
        return Objects.equals(featureLocations, other.featureLocations);
    }

    @Override
    public String toString() {
        return String.format("FeatureKey:{class=%s, locations=%s}", featureClass, featureLocations);
    }
}
