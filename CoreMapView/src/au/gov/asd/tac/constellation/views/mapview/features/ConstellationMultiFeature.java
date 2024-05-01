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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A feature made up of other features.
 *
 * @author cygnus_x-1
 */
public class ConstellationMultiFeature extends ConstellationAbstractFeature {

    private final List<ConstellationAbstractFeature> features;

    public ConstellationMultiFeature(final ConstellationFeatureType type) {
        super(type);
        this.features = new ArrayList<>();
    }

    public ConstellationMultiFeature(final ConstellationFeatureType type, final List<ConstellationAbstractFeature> features) {
        super(type);
        this.features = features;
    }

    public List<ConstellationAbstractFeature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    public void addFeatures(final List<ConstellationAbstractFeature> features) {
        this.features.addAll(features);
    }

    public void addFeature(final ConstellationAbstractFeature feature) {
        this.features.add(feature);
    }
}
