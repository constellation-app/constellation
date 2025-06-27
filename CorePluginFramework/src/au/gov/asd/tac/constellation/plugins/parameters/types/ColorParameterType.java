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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import org.openide.util.lookup.ServiceProvider;

/**
 * The ColorParameterType defines {@link PluginParameter} objects that hold
 * {@link ConstellationColor} values.
 *
 * @author sirius
 */
@ServiceProvider(service = PluginParameterType.class)
public class ColorParameterType extends PluginParameterType<ColorParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "color";

    /**
     * Constructs a new instance of this type.
     * <p>
     * Note: This constructor should not be called directly; it is public for
     * the purposes of lookup (which may be removed for types in the future). To
     * buildId parameters from the type, the static method
     * {@link #build buildId()} should be used, or the singleton
     * {@link #INSTANCE INSTANCE}.
     */
    public ColorParameterType() {
        super(ID);
    }

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final ColorParameterType INSTANCE = new ColorParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of ColorParameterType.
     */
    public static PluginParameter<ColorParameterValue> build(final String id) {
        return new PluginParameter<>(new ColorParameterValue(), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link ColorParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link ColorParameterValue} describing the initial value of
     * the parameter being constructed.
     * @return A {@link PluginParameter} of ColorParameterType.
     */
    public static PluginParameter<ColorParameterValue> build(final String id, final ColorParameterValue pv) {
        return new PluginParameter<>(pv, INSTANCE, id);
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds {@link ConstellationColor} values.
     */
    public static class ColorParameterValue extends ParameterValue {

        private ConstellationColor c;

        /**
         * Constructs a new ColorParameterValue
         */
        public ColorParameterValue() {
            c = ConstellationColor.CLOUDS;
        }

        /**
         * Constructs a new ColorParameterValue holding the specified
         * {@link ConstellationColor}.
         *
         * @param c The {@link ConstellationColor} that this parameter value
         * should hold.
         */
        public ColorParameterValue(final ConstellationColor c) {
            this.c = c;
        }

        /**
         * Get the current value from this parameter value.
         *
         * @return The {@link ConstellationColor} that this parameter value is
         * holding.
         */
        public ConstellationColor get() {
            return c;
        }

        /**
         * Set the current value
         *
         * @param newc The {@link ConstellationColor} for this parameter value
         * to hold.
         * @return True if the new value was different to the current value,
         * false otherwise.
         */
        public boolean set(final ConstellationColor newc) {
            if (newc != c) {
                c = newc;
                return true;
            }

            return false;
        }

        @Override
        public String validateString(final String s) {
            // TODO: re-evaluate this logic. ConstellationColor.getColorValue doesn't throw an exception
            // either this should simply return null, or logic should be modified to check something else
            // e.g. should it return a message if the color is null?
            try {
                ConstellationColor.getColorValue(s);
            } catch (final IllegalArgumentException ex) {
                return ex.getMessage();
            }

            return null;
        }

        @Override
        public boolean setStringValue(final String s) {
            final ConstellationColor newc = ConstellationColor.getColorValue(s);
            if (newc != c) {
                c = newc;
                return true;
            }

            return false;
        }

        @Override
        public Object getObjectValue() {
            return c;
        }

        @Override
        public boolean setObjectValue(final Object o) {
            final ConstellationColor newc;
            if (o == null) {
                newc = ConstellationColor.CLOUDS;
            } else if (o instanceof ConstellationColor constellationColor) {
                newc = constellationColor;
            } else {
                throw new IllegalArgumentException(String.format("Unexpected class %s", o.getClass()));
            }

            return set(newc);
        }

        @Override
        protected ColorParameterValue createCopy() {
            return new ColorParameterValue(c);
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            return this.getClass() == o.getClass() && c.equals(((ColorParameterValue) o).c);
        }

        @Override
        public int hashCode() {
            return c.hashCode();
        }

        @Override
        public String toString() {
            return c.toString();
        }
    }
}
