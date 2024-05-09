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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * The DateTiemRangeParameterType defines {@link PluginParameter} objects that
 * hold {@link DateTimeRange} values.
 *
 * @author ruby_crucis
 */
@ServiceProvider(service = PluginParameterType.class)
public class DateTimeRangeParameterType extends PluginParameterType<DateTimeRangeParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "datetimerange";

    // Absolute start/end dateFormatter.
    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss z";
    public static final String RANGE_SEPARATOR = ";";

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final DateTimeRangeParameterType INSTANCE = new DateTimeRangeParameterType();
    protected final SimpleDateFormat dateFormatter = new SimpleDateFormat(FORMAT);

    /**
     * Constructs a new instance of this type.
     * <p>
     * Note: This constructor should not be called directly; it is public for
     * the purposes of lookup (which may be removed for types in the future). To
     * buildId parameters from the type, the static method
     * {@link #build buildId()} should be used, or the singleton
     * {@link #INSTANCE INSTANCE}.
     */
    public DateTimeRangeParameterType() {
        super(ID);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of BooleanParameterType.
     */
    public static PluginParameter<DateTimeRangeParameterValue> build(final String id) {
        return new PluginParameter<>(new DateTimeRangeParameterValue(), INSTANCE, id);
    }

    @Override
    public String validateString(final PluginParameter<DateTimeRangeParameterValue> param, final String stringValue) {
        final DateTimeRangeParameterValue v = param.getParameterValue();
        return v.validateString(stringValue);
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds {@link DateTimeRange} values.
     */
    public static class DateTimeRangeParameterValue extends ParameterValue {

        private DateTimeRange dtr;

        /**
         * Constructs a new DateTimeRangeParameterValue
         */
        public DateTimeRangeParameterValue() {
            dtr = null;
        }

        /**
         * Constructs a new DateTimeRangeParameterValue holding the specified
         * {@link DateTimeRange}.
         *
         * @param dtr The {@link DateTimeRange} that this parameter value should
         * hold.
         */
        public DateTimeRangeParameterValue(final DateTimeRange dtr) {
            this.dtr = dtr != null ? new DateTimeRange(dtr) : null;
        }

        /**
         * Get the current value from this parameter value.
         * <p>
         * This will default to {@link Period#ofDays Period.ofDays(1)} if not
         * previously set, meaning that the return value will change over time.
         *
         * @return The {@link DateTimeRange} that this parameter value is
         * holding.
         */
        public DateTimeRange get() {
            return dtr != null ? dtr : new DateTimeRange(Period.ofDays(1), TimeZoneUtilities.UTC);
        }

        /**
         * Set the current value
         *
         * @param newdtr The {@link DateTimeRange} for this parameter value to
         * hold.
         * @return True if the new value was different to the current value,
         * false otherwise.
         */
        public boolean set(final DateTimeRange newdtr) {
            if (!Objects.equals(dtr, newdtr)) {
                dtr = newdtr;
                return true;
            }

            return false;
        }

        @Override
        public String validateString(final String s) {
            try {
                DateTimeRange.parse(s);
                return null;
            } catch (final DateTimeParseException ex) {
                return String.format("Format is '%s%s%s' or 'PnMnD Z'", FORMAT, RANGE_SEPARATOR, FORMAT);
            }
        }

        @Override
        public boolean setStringValue(final String s) {
            final DateTimeRange newdtr = StringUtils.isBlank(s) ? null : DateTimeRange.parse(s);

            return set(newdtr);
        }

        @Override
        public Object getObjectValue() {
            return get();
        }

        @Override
        public boolean setObjectValue(final Object o) {
            final DateTimeRange newdtr;
            if (o == null) {
                newdtr = null;
            } else if (o instanceof DateTimeRange dateTimeRange) {
                newdtr = dateTimeRange;
            } else {
                throw new IllegalArgumentException(String.format("Unexpected class %s", o.getClass()));
            }

            return set(newdtr);
        }

        @Override
        protected DateTimeRangeParameterValue createCopy() {
            return new DateTimeRangeParameterValue(dtr);
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            return this.getClass() == o.getClass() && Objects.equals(dtr, ((DateTimeRangeParameterValue) o).dtr);
        }

        @Override
        public int hashCode() {
            return dtr.hashCode();
        }

        @Override
        public String toString() {
            return dtr != null ? dtr.toString() : "";
        }
    }
}
