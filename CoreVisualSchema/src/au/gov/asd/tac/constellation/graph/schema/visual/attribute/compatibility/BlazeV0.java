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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility;

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
 * <p>
 * Note that this should no longer be used and only remains to support legacy
 * graph files.
 *
 * @author algol
 */
@Deprecated
public class BlazeV0 implements Serializable, Comparable<BlazeV0> {

    /**
     * A regex used to parse a String to a Blaze.
     * <p>
     * Probably not the most efficient thing, but it will do for now.
     */
    private static final Pattern BLAZE_PATTERN = Pattern.compile("(\\d+);(.+);(.+);(.+)");

    private final int angle;
    private final ConstellationColor color;
    private final String iconLabel;
    private final boolean iconEnabled;

    public BlazeV0(final int angle, final ConstellationColor color, final String iconLabel, final boolean iconEnabled) {
        this.angle = angle;
        this.color = color;
        this.iconLabel = iconLabel;
        this.iconEnabled = iconEnabled;
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
     * Return the name of the icon in the blaze.
     *
     * @return The name of the icon in the blaze.
     */
    public String getIconLabel() {
        return iconLabel;
    }

    /**
     * Return whether the icon is enabled or not
     *
     * @return boolean
     */
    public boolean isIconEnabled() {
        return iconEnabled;
    }

    public static class IllegalBlazeFormatException extends IllegalArgumentException {

        public IllegalBlazeFormatException(final String message) {
            super(message);
        }
    }

    public static BlazeV0 valueOf(final String s) {
        if (s != null && s.length() > 0) {
            final Matcher m = BLAZE_PATTERN.matcher(s);
            if (m.matches()) {
                final int angle = Integer.valueOf(m.group(1));
                final ConstellationColor color = ConstellationColor.getColorValue(m.group(2));
                if (color == null) {
                    throw new IllegalBlazeFormatException("Undefined color for blaze.");
                }
                final String iconLabel = m.group(3);
                final boolean iconEnabled = Boolean.valueOf(m.group(4));

                return new BlazeV0(angle, color, iconLabel, iconEnabled);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("%d;%s;%s;%s", angle, color, iconLabel, iconEnabled);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.angle;
        hash = 79 * hash + (this.color != null ? this.color.hashCode() : 0);
        hash = 79 * hash + (this.iconLabel != null ? this.iconLabel.hashCode() : 0);
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
        final BlazeV0 other = (BlazeV0) obj;
        if (this.angle != other.angle) {
            return false;
        }
        if (this.color != other.color && (this.color == null || !this.color.equals(other.color))) {
            return false;
        }
        if ((this.iconLabel == null) ? (other.iconLabel != null) : !this.iconLabel.equals(other.iconLabel)) {
            return false;
        }
        return this.iconEnabled == other.iconEnabled;
    }

    @Override
    public int compareTo(final BlazeV0 o) {
        return color.compareTo(o.color);
    }
}
