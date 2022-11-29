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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * The IntegerParameterType defines {@link PluginParameter} objects that hold
 * integer values.
 *
 * @author sirius
 */
@ServiceProvider(service = PluginParameterType.class)
public class IntegerParameterType extends PluginParameterType<IntegerParameterValue> {
    
    private static final Logger LOGGER = Logger.getLogger(IntegerParameterType.class.getName());

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "integer";

    /**
     * The property of this type referring to whether the width of a GUI input
     * for this parameter should be scaled according to the valid range of
     * values.
     */
    public static final String SHRINK_VAL = "shrink";

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final IntegerParameterType INSTANCE = new IntegerParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of IntegterParameterType.
     */
    public static PluginParameter<IntegerParameterValue> build(final String id) {
        return new PluginParameter<>(new IntegerParameterValue(), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link IntegerParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link IntegerParameterValue} describing the initial value of
     * the parameter being constructed.
     * @return A {@link PluginParameter} of IntegerParameterType.
     */
    public static PluginParameter<IntegerParameterValue> build(final String id, final IntegerParameterValue pv) {
        return new PluginParameter<>(pv, INSTANCE, id);
    }

    /**
     * Constructs a new instance of this type.
     * <p>
     * Note: This constructor should not be called directly; it is public for
     * the purposes of lookup (which may be removed for types in the future). To
     * buildId parameters from the type, the static method
     * {@link #build buildId()} should be used, or the singleton
     * {@link #INSTANCE INSTANCE}.
     */
    public IntegerParameterType() {
        super(ID);
    }

    /**
     * Sets the whether the width of a GUI input for the given parameter should
     * be scaled according to the valid range of values.
     *
     * @param parameter A {@link PluginParameter}.
     * @param shrinkInputWidth Whether the width of a GUI input should be
     * scaled.
     */
    public static void setShrinkInputWidth(final PluginParameter<IntegerParameterValue> parameter, final boolean shrinkInputWidth) {
        parameter.setProperty(SHRINK_VAL, shrinkInputWidth);
    }

    /**
     * Set the minimum allowed value for the given parameter. When values
     * strictly less than this are set for the given parameter, the parameter
     * will be considered invalid.
     *
     * @param parameter A {@link PluginParameter}.
     * @param min The minimum value to set.
     */
    public static void setMinimum(final PluginParameter<IntegerParameterValue> parameter, final int min) {
        parameter.getParameterValue().setMinimumValue(min);
    }

    /**
     * Set the maximum allowed value for the given parameter. When values
     * strictly greater than this are set for the given parameter, the parameter
     * will be considered invalid.
     *
     * @param parameter A {@link PluginParameter}.
     * @param max The maximum value to set.
     */
    public static void setMaximum(final PluginParameter<IntegerParameterValue> parameter, final int max) {
        parameter.getParameterValue().setMaximumValue(max);
    }

    /**
     * Set the 'typical' step between values for the given parameter. This does
     * not affect the validity of the parameter, but rather acts as advice for a
     * GUI input that provides discrete changes to the parameter.
     *
     * @param parameter A {@link PluginParameter}.
     * @param step The step between values for the parameter.
     */
    public static void setStep(final PluginParameter<IntegerParameterValue> parameter, final int step) {
        parameter.getParameterValue().setStepValue(step);
    }

    @Override
    public String validateString(final PluginParameter<IntegerParameterValue> param, final String stringValue) {
        final IntegerParameterValue v = param.getParameterValue();
        return v.validateString(stringValue);
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds integer values.
     */
    public static class IntegerParameterValue extends ParameterValue implements NumberParameterValue {

        private static final NumberStringConverter CONVERTER = new NumberStringConverter();

        private int i;
        private Integer min;
        private Integer max;
        private Integer step;

        /**
         * Constructs a new IntegerParameterValue
         */
        public IntegerParameterValue() {
            i = 0;
        }

        /**
         * Constructs a new IntegerParameterValue holding the specified integer.
         *
         * @param i The integer that this parameter value should hold.
         */
        public IntegerParameterValue(final int i) {
            this.i = i;
        }

        /**
         * Get the current value from this parameter value.
         *
         * @return The integer that this parameter value is holding.
         */
        public int get() {
            return i;
        }

        /**
         * Set the current value
         *
         * @param newi The integer for this parameter value to hold.
         * @return True if the new value was different to the current value,
         * false otherwise.
         */
        public boolean set(final int newi) {
            if (newi != i) {
                if (min != null && newi < min) {
                    LOGGER.log(Level.WARNING, "{0} is lower than the minimum ({1}) allowed. Changing to "
                            + "the minimum value", new Object[]{newi, min});
                    return false;
                }

                if (max != null && newi > max) {
                    LOGGER.log(Level.WARNING, "{0} is higher than the maximum ({1}) allowed. Changing to "
                            + "the maximum value", new Object[]{newi, max});
                    return false;
                }

                i = newi;
                return true;
            }

            return false;
        }

        /**
         * Set the minimum valid value for this parameter value
         *
         * @param min The minimum value to set.
         */
        public void setMinimumValue(final Integer min) {
            this.min = min;
        }

        /**
         * Set the maximum valid value for this parameter value
         *
         * @param max The maximum value to set.
         */
        public void setMaximumValue(final Integer max) {
            this.max = max;
        }

        /**
         * Set the step between consecutive values for this parameter value
         *
         * @param step The step value to set.
         */
        public void setStepValue(final Integer step) {
            this.step = step;
        }

        @Override
        public String validateString(final String s) {
            if (StringUtils.isBlank(s)) {
                return "Value required";
            }

            try {
                final Number n = CONVERTER.fromString(s);
                if (n != null) {
                    final int val = n.intValue();
                    if (min != null && val < min) {
                        return "Value too small";
                    }
                    if (max != null && val > max) {
                        return "Value too large";
                    }
                }

                return null;
            } catch (final RuntimeException ex) {
                return "Not a valid integer";
            }
        }

        @Override
        public boolean setStringValue(final String s) {
            final Number n = CONVERTER.fromString(s);
            if (n != null) {
                final int newi = n.intValue();
                
                return set(newi);
            }

            return false;
        }

        @Override
        public Object getObjectValue() {
            return i;
        }

        @Override
        public boolean setObjectValue(final Object o) {
            final int newi;
            if (o == null) {
                newi = 0;
            } else if (o instanceof Integer) {
                newi = (Integer) o;
            } else {
                throw new IllegalArgumentException(String.format("Unexpected class %s", o.getClass()));
            }

            return set(newi);
        }

        @Override
        protected IntegerParameterValue createCopy() {
            final IntegerParameterValue pv = new IntegerParameterValue(i);
            pv.min = min;
            pv.max = max;
            pv.step = step;

            return pv;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            return this.getClass() == o.getClass() && i == ((IntegerParameterValue) o).i;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(i);
        }

        @Override
        public String toString() {
            return Integer.toString(i);
        }

        @Override
        public Number getNumberValue() {
            return i;
        }

        @Override
        public boolean setNumberValue(final Number n) {
            return set(n.intValue());
        }

        @Override
        public Number getMinimumValue() {
            return min;
        }

        @Override
        public Number getMaximumValue() {
            return max;
        }

        @Override
        public Number getStepValue() {
            return step;
        }
    }
}
