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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * A description of a attributeName: the attribute from which the
 * attributeName's value is taken; color; size.
 * <p>
 * Note that this should no longer be used and only remains to support legacy
 * graph files.
 *
 * @author algol
 */
@Deprecated
public final class ElementGraphLabelV0 implements Serializable {

    private final String attributeName;
    private final ConstellationColor color;
    private final float size;

    private static final char LABEL_PROPERTY_DELIMITER = ',';

    /**
     * Construct a Label instance with a default size.
     *
     * @param attributeName The name of the attribute from which the value is
     * read.
     * @param color The color of the attributeName.
     */
    public ElementGraphLabelV0(final String attributeName, final ConstellationColor color) {
        this(attributeName, color, 1);
    }

    /**
     * Construct a Label instance.
     *
     * @param attributeName The name of the attribute from which the value is
     * read.
     * @param color The color of the attributeName.
     * @param size The size of the attributeName's characters.
     */
    public ElementGraphLabelV0(final String attributeName, final ConstellationColor color, final float size) {
        this.attributeName = attributeName;
        this.color = color;
        this.size = size;
    }

    /**
     * Copy constructor
     *
     * @param label The GraphLabel to copy.
     */
    public ElementGraphLabelV0(final ElementGraphLabelV0 label) {
        attributeName = label.attributeName;
        color = label.color;
        size = label.size;
    }

    /**
     * Return the name of the attribute from which the attributeName's value is
     * read.
     *
     * @return The name of the attribute from which the attributeName's value is
     * read.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Return the color of the attributeName.
     *
     * @return The color of the attributeName.
     */
    public ConstellationColor getColor() {
        return color;
    }

    /**
     * Return the size of the attributeName's characters.
     *
     * @return The size of the attributeName's characters.
     */
    public float getSize() {
        return size;
    }

    @Override
    public String toString() {
        return StringUtilities.quoteAndDelimitString(Arrays.asList(attributeName, color.toString(), String.valueOf(size)), LABEL_PROPERTY_DELIMITER);
    }

    public static ElementGraphLabelV0 valueOf(final String graphLabelString) {
        if (graphLabelString == null) {
            return null;
        }
        final List<String> labelProperties;
        try {
            labelProperties = StringUtilities.unquoteAndSplitString(graphLabelString, LABEL_PROPERTY_DELIMITER);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("String does not represent a graph label: " + graphLabelString);
        }
        if (labelProperties.size() == 3) {
            final String attributeName = labelProperties.get(0);
            final ConstellationColor color = ConstellationColor.getColorValue(labelProperties.get(1));
            if (color == null) {
                throw new IllegalArgumentException("Undefined color for label.");
            }
            final float size;
            try {
                size = Float.valueOf(labelProperties.get(2));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid size for label");
            }
            return new ElementGraphLabelV0(attributeName, color, size);
        }
        throw new IllegalArgumentException("String for graph label has wrong number of fields: " + graphLabelString);
    }
}
