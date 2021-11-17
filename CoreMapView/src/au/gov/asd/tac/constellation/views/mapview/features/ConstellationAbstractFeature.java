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
package au.gov.asd.tac.constellation.views.mapview.features;

import de.fhpotsdam.unfolding.data.Feature;
import java.util.HashMap;
import java.util.Map;

/**
 * A feature object for use in the Map View.
 *
 * @author cygnus_x-1
 */
public class ConstellationAbstractFeature {

    public enum ConstellationFeatureType {
        POINT, LINE, POLYGON, MULTI, CLUSTER
    }

    private String id;
    private final ConstellationFeatureType type;
    private HashMap<String, Object> properties = new HashMap<>();

    public ConstellationAbstractFeature(Feature feature) {
        this.id = feature.getId();
        switch(feature.getType()) {
            case POINT:
                this.type = ConstellationFeatureType.POINT;
                break;
            case LINES:
                this.type = ConstellationFeatureType.LINE;
                break;
            case POLYGON:
                this.type = ConstellationFeatureType.POLYGON;
                break;
            default:
                this.type = ConstellationFeatureType.MULTI;
        }
        this.properties = feature.getProperties();
    }

    public ConstellationAbstractFeature(ConstellationFeatureType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public ConstellationFeatureType getType() {
        return type;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public Object getProperty(final String key) {
        return properties.get(key);
    }

    public String getStringProperty(final String key) {
        final Object value = properties.get(key);
        if (value instanceof String) {
            return (String) value;
        } else {
            return null;
        }
    }

    public Object addProperty(final String key, final Object value) {
        return properties.put(key, value);
    }

    public void setProperties(final HashMap<String, Object> properties) {
        this.properties = properties;
    }
}
