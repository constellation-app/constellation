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
package au.gov.asd.tac.constellation.graph.construction;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;

/**
 * Builder class to add the location attributes x, y, z and node radius to a
 * graph
 *
 * @author twilight_sparkle
 */
public class LocationAttributeBuilder extends GraphBuilder {

    private static final float X_DEFAULT = 0.0F;
    private static final float Y_DEFAULT = 0.0F;
    private static final float Z_DEFAULT = 0.0F;
    private static final float NRADIUS_DEFAULT = 0.0F;

    public static LocationAttributeBuilder addLocationAttributes(final GraphWriteMethods graph) {
        return addLocationAttributes(graph, X_DEFAULT, Y_DEFAULT, Z_DEFAULT, NRADIUS_DEFAULT);
    }

    public static LocationAttributeBuilder addLocationAttributes(final float xDefault, final float yDefault, final float zDefault, final float nRadiusDefault) {
        return addLocationAttributes(new StoreGraph(), xDefault, yDefault, zDefault, nRadiusDefault);
    }

    public static LocationAttributeBuilder addLocationAttributes(final GraphWriteMethods graph, final float xDefault, final float yDefault, final float zDefault, final float nRadiusDefault) {
        final int xAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", xDefault, null);
        final int yAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", yDefault, null);
        final int zAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", zDefault, null);
        final int nRadiusAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "nradius", "nradius", nRadiusDefault, null);
        return new LocationAttributeBuilder(graph, xAttr, yAttr, zAttr, nRadiusAttr, xDefault, yDefault, zDefault, nRadiusDefault);
    }

    public final int xAttr;
    public final int yAttr;
    public final int zAttr;
    public final int nRadiusAttr;
    public final float xDefault;
    public final float yDefault;
    public final float zDefault;
    public final float nRadiusDefault;

    private LocationAttributeBuilder(final GraphWriteMethods graph, final int xAttr, final int yAttr, final int zAttr, final int nRadiusAttr, final float xDefault, final float yDefault, final float zDefault, final float nRadiusDefault) {
        super(graph);
        this.xAttr = xAttr;
        this.yAttr = yAttr;
        this.zAttr = zAttr;
        this.nRadiusAttr = nRadiusAttr;
        this.xDefault = xDefault;
        this.yDefault = yDefault;
        this.zDefault = zDefault;
        this.nRadiusDefault = nRadiusDefault;
    }
}
