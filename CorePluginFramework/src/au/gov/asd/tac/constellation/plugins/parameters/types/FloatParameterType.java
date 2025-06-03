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
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * The FloatParameterType defines {@link PluginParameter} objects that hold
 * float values.
 *
 * @author sirius
 */
@ServiceProvider(service = PluginParameterType.class)
public class FloatParameterType extends PluginParameterType<FloatParameterValue> {
    
    private static final Logger LOGGER = Logger.getLogger(FloatParameterType.class.getName());

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "float";

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
    public static final FloatParameterType INSTANCE = new FloatParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of FloatParameterType.
     */
    public static PluginParameter<FloatParameterValue> build(final String id) {
        return new PluginParameter<>(new FloatParameterValue(), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link FloatParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link FloatParameterValue} describing the initial value of
     * the parameter being constructed.
     * @return A {@link PluginParameter} of FloatParameterType.
     */
    public static PluginParameter<FloatParameterValue> build(final String id, final FloatParameterValue pv) {
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
    public FloatParameterType() {
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
    public static void setShrinkInputWidth(final PluginParameter<FloatParameterValue> parameter, final boolean shrinkInputWidth) {
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
    public static void setMinimum(final PluginParameter<FloatParameterValue> parameter, final float min) {
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
    public static void setMaximum(final PluginParameter<FloatParameterValue> parameter, final float max) {
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
    public static void setStep(final PluginParameter<FloatParameterValue> parameter, final float step) {
        parameter.getParameterValue().setStepValue(step);
    }

    @Override
    public String validateString(final PluginParameter<FloatParameterValue> param, final String stringValue) {
        final FloatParameterValue v = param.getParameterValue();
        return v.validateString(stringValue);
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds float values.
     */
    public static class FloatParameterValue extends ParameterValue implements NumberParameterValue {

        private static final NumberStringConverter CONVERTER = new NumberStringConverter();

        private float f;
        private Float min;
        private Float max;
        private Float step;

        /**
         * Constructs a new FloatParameterValue
         */
        public FloatParameterValue() {
            f = 0;
        }

        /**
         * Constructs a new FloatParameterValue holding the specified float.
         *
         * @param f The float that this parameter value should hold.
         */
        public FloatParameterValue(final float f) {
            this.f = f;
        }

        /**
         * Get the current value from this parameter value.
         *
         * @return The float that this parameter value is holding.
         */
        public float get() {
            return f;
        }

        /**
         * Set the current value
         *
         * @param newf The float for this parameter value to hold.
         * @return True if the new value was different to the current value,
         * false otherwise.
         */
        public boolean set(final float newf) {
            if (newf != f) {
                if (min != null && newf < min) {
                    LOGGER.log(Level.WARNING, "{0} is lower than the minimum ({1}) allowed. Changing to "
                            + "the minimum value", new Object[]{newf, min});
                    return false;
                }

                if (max != null && newf > max) {
                    LOGGER.log(Level.WARNING, "{0} is higher than the maximum ({1}) allowed. Changing to "
                            + "the maximum value", new Object[]{newf, max});
                    return false;
                }

                f = newf;
                return true;
            }

            return false;
        }

        /**
         * Set the minimum valid value for this parameter value
         *
         * @param min The minimum value to set.
         */
        public void setMinimumValue(final Float min) {
            this.min = min;
        }

        /**
         * Set the maximum valid value for this parameter value
         *
         * @param max The maximum value to set.
         */
        public void setMaximumValue(final Float max) {
            this.max = max;
        }

        /**
         * Set the step between consecutive values for this parameter value
         *
         * @param step The step value to set.
         */
        public void setStepValue(final Float step) {
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
                    final float val = n.floatValue();
                    if (min != null && val < min) {
                        return "Value too small";
                    }
                    if (max != null && val > max) {
                        return "Value too large";
                    }
                }

                return null;
            } catch (final RuntimeException ex) {
                return "Not a valid float";
            }
        }

        @Override
        public boolean setStringValue(final String s) {
            final Number n = CONVERTER.fromString(s);
            if (n != null) {
                final float newf = n.floatValue();
                
                return set(newf);
            }

            return false;
        }

        @Override
        public Object getObjectValue() {
            return get();
        }

        @Override
        public boolean setObjectValue(final Object o) {
            final float newf;
            if (o == null) {
                newf = 0;
            } else if (o instanceof Float aFloat) {
                newf = aFloat;
            } else {
                throw new IllegalArgumentException(String.format("Unexpected object value %s", o.getClass()));
            }

            return set(newf);
        }

        @Override
        protected FloatParameterValue createCopy() {
            final FloatParameterValue pv = new FloatParameterValue(f);
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
            return this.getClass() == o.getClass() && f == ((FloatParameterValue) o).f;
        }

        @Override
        public int hashCode() {
            return Float.hashCode(f);
        }

        @Override
        public String toString() {
            return Float.toString(f);
        }

        @Override
        public Number getNumberValue() {
            return get();
        }

        @Override
        public boolean setNumberValue(final Number n) {
            return set(n.floatValue());
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
