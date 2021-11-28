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
package au.gov.asd.tac.constellation.graph.schema.visual;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A description of a attributeName: the attribute from which the
 * attributeName's value is taken; color; size.
 *
 * @author algol
 */
public final class GraphLabel implements Serializable, Comparable<GraphLabel> {

    private final String attributeName;
    private final ConstellationColor color;
    private final float size;

    /**
     * ColorValue uses "," as a delimiter, so we'll use ";".
     */
    private static final String DELIMITER = SeparatorConstants.SEMICOLON;

    /**
     * Construct a Label instance with a default size.
     *
     * @param attributeName The name of the attribute from which the value is
     * read.
     * @param color The color of the attributeName.
     */
    public GraphLabel(final String attributeName, final ConstellationColor color) {
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
    public GraphLabel(final String attributeName, final ConstellationColor color, final float size) {
        this.attributeName = attributeName;
        this.color = color;
        this.size = size;
    }

    /**
     * Copy constructor
     *
     * @param label The GraphLabel to copy.
     */
    public GraphLabel(final GraphLabel label) {
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
        final StringJoiner buf = new StringJoiner(DELIMITER);
        buf.add(attributeName);
        buf.add(StringUtilities.escape(color.toString(), DELIMITER));
        buf.add(String.valueOf(size));

        return buf.toString();
    }

    public static GraphLabel valueOf(final String graphLabelString) {
        if (graphLabelString == null) {
            return null;
        }
        final List<String> labelProperties;
        try {
            labelProperties = StringUtilities.splitEscaped(graphLabelString, DELIMITER);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("String does not represent a graph label: " + graphLabelString);
        }
        if (labelProperties.size() == 3) {
            final String attributeName = labelProperties.get(0);
            final ConstellationColor color = ConstellationColor.getColorValue(labelProperties.get(1));
            if (color == null) {
                throw new IllegalArgumentException("Undefined colour for label.");
            }
            final float size;
            try {
                size = Float.valueOf(labelProperties.get(2));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid size for label");
            }
            return new GraphLabel(attributeName, color, size);
        }
        throw new IllegalArgumentException("String for graph label has wrong number of fields: " + graphLabelString);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.attributeName);
        hash = 17 * hash + Objects.hashCode(this.color);
        hash = 17 * hash + Float.floatToIntBits(this.size);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GraphLabel other = (GraphLabel) obj;
        if (Float.floatToIntBits(this.size) != Float.floatToIntBits(other.size)) {
            return false;
        }
        if (!Objects.equals(this.attributeName, other.attributeName)) {
            return false;
        }
        return Objects.equals(this.color, other.color);
    }

    @Override
    public int compareTo(final GraphLabel o) {
        int compare = attributeName.compareTo(o.attributeName);
        if (compare != 0) {
            return compare;
        }
        compare = color.compareTo(o.color);
        if (compare != 0) {
            return compare;
        }

        return Float.compare(size, o.size);
    }
}
