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
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import org.openide.util.lookup.ServiceProvider;

/**
 * The BooleanParameterType defines {@link PluginParameter} objects that hold
 * boolean values.
 *
 * @author sirius
 */
@ServiceProvider(service = PluginParameterType.class)
public class BooleanParameterType extends PluginParameterType<BooleanParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "boolean";

    /**
     * Constructs a new instance of this type.
     * <p>
     * Note: This constructor should not be called directly; it is public for
     * the purposes of lookup (which may be removed for types in the future). To
     * buildId parameters from the type, the static method
     * {@link #build buildId()} should be used, or the singleton
     * {@link #INSTANCE INSTANCE}.
     */
    public BooleanParameterType() {
        super(ID);
    }

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final BooleanParameterType INSTANCE = new BooleanParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of BooleanParameterType.
     */
    public static PluginParameter<BooleanParameterValue> build(final String id) {
        return new PluginParameter<>(new BooleanParameterValue(), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link BooleanParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link BooleanParameterValue} describing the initial value of
     * the parameter being constructed.
     * @return A {@link PluginParameter} of BooleanParameterType.
     */
    public static PluginParameter<BooleanParameterValue> build(final String id, final BooleanParameterValue pv) {
        return new PluginParameter<>(pv, INSTANCE, id);
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds boolean values.
     */
    public static class BooleanParameterValue extends ParameterValue {

        private boolean b;

        /**
         * Constructs a new BooleanParameterValue
         */
        public BooleanParameterValue() {
            b = false;
        }

        /**
         * Constructs a new BooleanParameterValue holding the specified boolean.
         *
         * @param b The boolean that this parameter value should hold.
         */
        public BooleanParameterValue(final boolean b) {
            this.b = b;
        }

        /**
         * Get the current value from this parameter value.
         *
         * @return The boolean that this parameter value is holding.
         */
        public boolean getValue() {
            return b;
        }

        /**
         * Set the current value
         *
         * @param newb The boolean for this parameter value to hold.
         * @return True if the new value was different to the current value,
         * false otherwise.
         */
        public boolean set(final boolean newb) {
            if (newb != b) {
                b = newb;
                return true;
            }

            return false;
        }

        @Override
        public String validateString(final String s) {
            return null;
        }

        @Override
        public boolean setStringValue(final String s) {
            final boolean newb = Boolean.parseBoolean(s);
            if (newb != b) {
                b = newb;
                return true;
            }

            return false;
        }

        @Override
        public Object getObjectValue() {
            return getValue();
        }

        @Override
        public boolean setObjectValue(final Object o) {
            final boolean newb;
            if (o == null) {
                newb = false;
            } else if (o instanceof Boolean aBoolean) {
                newb = aBoolean;
            } else {
                throw new IllegalArgumentException(String.format("Unexpected class %s", o.getClass()));
            }

            return set(newb);
        }

        @Override
        protected BooleanParameterValue createCopy() {
            return new BooleanParameterValue(b);
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            return this.getClass() == o.getClass() && b == ((BooleanParameterValue) o).b;
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(b);
        }

        @Override
        public String toString() {
            return Boolean.toString(b);
        }
    }
}
