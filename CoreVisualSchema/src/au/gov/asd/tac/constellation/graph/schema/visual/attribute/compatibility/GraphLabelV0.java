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
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * A description of a label: the attribute from which the label's value is
 * taken; color; size.
 * <p>
 * Note that this should no longer be used and only remains to support legacy
 * graph files.
 *
 * @author algol
 */
@Deprecated
public final class GraphLabelV0 implements Serializable {

    private final String label;
    private final ConstellationColor color;
    private final float radius;

    /**
     * Construct a Label instance with a default radius.
     *
     * @param attributeName The name of the attribute from which the value is
     * read.
     * @param color The color of the label.
     */
    public GraphLabelV0(final String attributeName, final ConstellationColor color) {
        this(attributeName, color, 1);
    }

    /**
     * Construct a Label instance.
     *
     * @param label The name of the attribute from which the value is read.
     * @param color The color of the label.
     * @param radius The size of the label's characters.
     */
    public GraphLabelV0(final String label, final ConstellationColor color, final float radius) {
        this.label = label;
        this.color = color;
        this.radius = radius;
    }

    /**
     * Construct a Label instance from a String.
     * <p>
     * The String is a serialised form of a Label. If radius is not present, it
     * is assumed to be 1.
     *
     * @param s A string of the form "attrName;ColorValue" or
     * "attrName;ColorValue;radius".
     */
    public GraphLabelV0(final String s) {
        final String[] v = s.split(SeparatorConstants.SEMICOLON);
        if (v.length != 2 && v.length != 3) {
            throw new IllegalArgumentException("Can't parse '" + s + "'");
        }

        label = v[0];
        color = ConstellationColor.getColorValue(v[1]);
        radius = v.length == 3 ? Float.parseFloat(v[2]) : 1;
    }

    /**
     * Return the name of the attribute from which the label's value is read.
     *
     * @return The name of the attribute from which the label's value is read.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Return the color of the label.
     *
     * @return The color of the label.
     */
    public ConstellationColor getColor() {
        return color;
    }

    /**
     * Return the size of the label's characters.
     *
     * @return The size of the label's characters.
     */
    public float getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        char[] metaChar = {' ', ';', '[', ']'};
        return String.format("[GraphLabel:%s;%s;%f]", StringUtilities.escapeString(label, metaChar), color, radius);
    }

    public static GraphLabelV0 fromString(final String graphLabelString) {
        String thisLabel = null;
        ConstellationColor thisColor = null;
        Float thisRadius = null;
        if (StringUtils.isNotBlank(graphLabelString)) {
            final Set<Character> splitChar = new HashSet<>();
            splitChar.add(';');
            final List<String> graphLabelComponents = StringUtilities.splitLabelsWithEscapeCharacters(graphLabelString.substring(graphLabelString.indexOf(':') + 1, graphLabelString.lastIndexOf(']')), splitChar);
            final char[] metaChar = {' ', ';', '[', ']'};
            thisLabel = StringUtilities.unescapeString(graphLabelComponents.get(0), metaChar);
            final String[] rgba = graphLabelComponents.get(1).split(",");
            thisColor = ConstellationColor.getColorValue(Float.parseFloat(rgba[0]), Float.parseFloat(rgba[1]), Float.parseFloat(rgba[2]), Float.parseFloat(rgba[3]));
            thisRadius = Float.valueOf(graphLabelComponents.get(2));
        }

        return new GraphLabelV0(thisLabel, thisColor, thisRadius);
    }
}
