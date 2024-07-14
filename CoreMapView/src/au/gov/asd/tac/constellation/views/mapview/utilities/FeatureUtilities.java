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
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationAbstractFeature.ConstellationFeatureType;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationMultiFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationPointFeature;
import au.gov.asd.tac.constellation.views.mapview.features.ConstellationShapeFeature;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.MultiFeature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import java.util.List;

/**
 * Utility methods for features in the Map View.
 *
 * @author cygnus_x-1
 */
public class FeatureUtilities {

    private FeatureUtilities() {
        throw new IllegalStateException("Utility Class");
    }

    public static ConstellationAbstractFeature convert(final Feature feature) {
        final ConstellationAbstractFeature constellationFeature;
        switch (feature.getType()) {
            case POINT -> constellationFeature = new ConstellationPointFeature(((PointFeature) feature).getLocation());
            case LINES -> constellationFeature = new ConstellationShapeFeature(ConstellationFeatureType.LINE, ((ShapeFeature) feature).getLocations());
            case POLYGON -> constellationFeature = new ConstellationShapeFeature(ConstellationFeatureType.POLYGON, ((ShapeFeature) feature).getLocations());
            case MULTI -> {
                final List<ConstellationAbstractFeature> constellationFeatures = ((MultiFeature) feature).getFeatures().stream()
                        .map(FeatureUtilities::convert).toList();
                constellationFeature = new ConstellationMultiFeature(ConstellationFeatureType.MULTI, constellationFeatures);
            }
            default -> {
                return null;
            }
        }
        return constellationFeature;
    }
}
