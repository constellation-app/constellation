/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.LayerName;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.attribute.AbstractObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import org.openide.util.lookup.ServiceProvider;

/**
 * A representation of a data type that is represented by a String and an
 * integer.
 *
 * ie. In the case of Layer by Time, binning by Month, sees a layer created for
 * January whereby it has a corresponding numerical value of 1
 *
 * @author procyon
 */
@ServiceProvider(service = AttributeDescription.class)
public class LayerNameAttributeDescription extends AbstractObjectAttributeDescription<LayerName> {

    public static final String ATTRIBUTE_NAME = "layer_name";
    public static final Class<LayerName> NATIVE_CLASS = LayerName.class;
    public static final LayerName DEFAULT_VALUE = new LayerName(Graph.NOT_FOUND, "DEFAULT");

    public LayerNameAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    @SuppressWarnings("unchecked") // Casts are manually checked
    protected LayerName convertFromObject(final Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof LayerName) {
            return (LayerName) object;
        } else if (object instanceof String) {
            if (object.equals("DEFAULT")) {
                // handle default value from file import
                return DEFAULT_VALUE;
            } else {
                return convertFromString((String) object);
            }
        } else {
            throw new IllegalArgumentException(String.format("Error converting %s to LayerName.", object.getClass()));
        }
    }

    @Override
    protected LayerName convertFromString(final String string) {
        LayerName layerName = null;

        if (string != null) {
            final int ix = string.indexOf(',');
            if (ix > 0) {
                try {
                    final int layer = Integer.parseInt(string.substring(0, ix));
                    final String name = string.substring(ix + 1);
                    layerName = new LayerName(layer, name);
                } catch (final NumberFormatException ex) {
                }
            }
        }

        return layerName;
    }

    @Override
    public int getInt(final int id) {
        return ((LayerName) data[id]).getLayer();
    }

    @Override
    public String getString(final int id) {
        final LayerName layerName = (LayerName) data[id];
        return layerName == null ? null : String.format("%d,%s", layerName.getLayer(), layerName.getName());
    }
}
