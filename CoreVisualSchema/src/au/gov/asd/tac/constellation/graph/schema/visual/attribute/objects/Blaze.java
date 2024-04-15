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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A representation of a blaze.
 * <p>
 * A blaze is something that highlights a node, like a "you are here" sign. Our
 * implementation is a colored shape that encompasses an icon and points to its
 * vertex. The shape can be shown at any integral angle around the node.
 * <p>
 * Blazes consist of an angle, a color, and an icon.
 *
 * @author algol
 */
public final class Blaze implements Serializable, Comparable<Blaze> {

    /**
     * A regex used to parse a String to a Blaze.
     * <p>
     * Probably not the most efficient thing, but it will do for now.
     */
    private static final Pattern BLAZE_PATTERN = Pattern.compile("(\\d+);(.+)");

    private final int angle;
    private final ConstellationColor color;
    private final String representation;

    /**
     * Blaze constructor - create a new Blaze using the supplied blaze angle
     * and color.
     * @param angle Angle that the Blaze will be pointing
     * @param color Color of the Blaze
     */
    public Blaze(final int angle, final ConstellationColor color) {
        this.angle = angle;
        this.color = color;
        this.representation = String.format("%d;%s", angle, color);
    }

    /**
     * Return the angle at which the blaze is pointing.
     *
     * @return The angle at which the blaze is pointing.
     */
    public int getAngle() {
        return angle;
    }

    /**
     * Return the color of the blaze.
     *
     * @return The color of the blaze.
     */
    public ConstellationColor getColor() {
        return color;
    }

    /**
     * Exception thrown if Blaze has invalid format.
     */
    public static class IllegalBlazeFormatException extends IllegalArgumentException {

        /**
         * Default exception constructor, create exception with given message.
         * @param message MEssage to assign to exception.
         */
        public IllegalBlazeFormatException(final String message) {
            super(message);
        }
    }

    /**
     * Attempt to create a Blaze from data parsed out of supplied string.
     * @param s String to parse to extract Blaze object.
     * The string can take several formats:
     *   "{angle};{color}"
     *   "{angle};{RGBValueCode}"
     *   "{angle};{RGBValueArray}"
     *   "{angle};[{RGBValueArray}]"
     *   "{angle};{HTMLColorCode}"
     * Where:
     *   "{angle}" is an integer angle
     *   "{color}" is a named color from ConstellationColor static colors
     *   "{RGBValueCode}" is a string of the format "RGBrrrgggbbb" for which
     *     "RGB" is fixed and rrr, ggg, bbb are 3 digit values for color values,
     *     ie: "RGB255000000" equates to red
     *   "{RGBValueArray}" is a comma separated list of 3 color components in
     *     the range 0.0 to 1.0 for red, green, blue components.
     *   "{HTMLColorCode}" is a HTML representation of color of the form
     *     "#FF0000" for red.
     * @return Blaze generated from supplied string, if parsing is possible.
     * null is returned if string is null or 0 length.
     * @throws IllegalBlazeFormatException if Blaze cannot be constructed from
     * supplied string.
     */
    public static Blaze valueOf(final String s) {
        if (s != null) {
            final Matcher m = BLAZE_PATTERN.matcher(s);
            if (m.matches()) {
                final int angle = Integer.parseInt(m.group(1));
                final ConstellationColor color = ConstellationColor.getColorValue(m.group(2));
                if (color == null) {
                    throw new IllegalBlazeFormatException("Undefined color for blaze.");
                }
                return new Blaze(angle, color);
            }
        }
        return null;
    }

    /**
     * Return string representation of the Blaze.
     * @return string representation of the Blaze.
     */
    @Override
    public String toString() {
        return representation;
    }

    /**
     * Generate a hash code for the Blaze.
     * @return Generated hash code.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.angle;
        hash = 79 * hash + (this.color != null ? this.color.hashCode() : 0);
        return hash;
    }

    /**
     * Blaze equality operator.
     * @param obj Object to compare against.
     * @return true if object matches this object.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Blaze other = (Blaze) obj;
        if (this.angle != other.angle) {
            return false;
        }
        return this.color == other.color || (this.color != null && this.color.equals(other.color));
    }

    /** Blaze compareTo operator.
     * Assigns ordering to comparison. This comparison only considers Blaze
     * color.
     * @param o Object to compare to.
     * @return Integer representing comparison. 
     */
    @Override
    public int compareTo(final Blaze o) {
        return color.compareTo(o.color);
    }
}
