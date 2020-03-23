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

    public static class IllegalBlazeFormatException extends IllegalArgumentException {

        public IllegalBlazeFormatException(final String message) {
            super(message);
        }
    }

    public static Blaze valueOf(final String s) {
        if (s != null && s.length() > 0) {
            final Matcher m = BLAZE_PATTERN.matcher(s);
            if (m.matches()) {
                final int angle = Integer.valueOf(m.group(1));
                final ConstellationColor color = ConstellationColor.getColorValue(m.group(2));
                if (color == null) {
                    throw new IllegalBlazeFormatException("Undefined colour for blaze.");
                }

                return new Blaze(angle, color);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return representation;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.angle;
        hash = 79 * hash + (this.color != null ? this.color.hashCode() : 0);
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
        final Blaze other = (Blaze) obj;
        if (this.angle != other.angle) {
            return false;
        }
        if (this.color != other.color && (this.color == null || !this.color.equals(other.color))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final Blaze o) {
        return color.compareTo(o.color);
    }
}
