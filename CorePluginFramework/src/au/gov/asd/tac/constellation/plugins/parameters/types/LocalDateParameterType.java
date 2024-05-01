/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType.LocalDateParameterValue;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * The LocalDateParameterType defines {@link PluginParameter} objects that hold
 * {@link LocalDate} values.
 *
 * @author algol
 */
@ServiceProvider(service = PluginParameterType.class)
public class LocalDateParameterType extends PluginParameterType<LocalDateParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "localdate";

    /**
     * Constructs a new instance of this type.
     * <p>
     * Note: This constructor should not be called directly; it is public for
     * the purposes of lookup (which may be removed for types in the future). To
     * buildId parameters from the type, the static method
     * {@link #build buildId()} should be used, or the singleton
     * {@link #INSTANCE INSTANCE}.
     */
    public LocalDateParameterType() {
        super(ID);
    }

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final LocalDateParameterType INSTANCE = new LocalDateParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of LocalDateParameterType.
     */
    public static PluginParameter<LocalDateParameterValue> build(final String id) {
        return new PluginParameter<>(new LocalDateParameterValue(), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link LocalDateParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link LocalDateParameterValue} describing the initial value
     * of the parameter being constructed.
     * @return A {@link PluginParameter} of LocalDateParameterType.
     */
    public static PluginParameter<LocalDateParameterValue> build(final String id, final LocalDateParameterValue pv) {
        return new PluginParameter<>(pv, INSTANCE, id);
    }

    /**
     * Convert a LocalDate to a Date in UTC.
     *
     * @param localDate The {@link LocalDate to convert}.
     * @return The {@link Date} representation of the localDate in UTC.
     */
    public static Date toDate(final LocalDate localDate) {
        return new Date(localDate.atStartOfDay(ZoneId.of("UTC")).toEpochSecond() * 1000);
    }

    /**
     * Convert a LocalDate to a Calendar in UTC.
     *
     * @param localDate The {@link LocalDate to convert}.
     * @return The {@link Calendar} representation of the localDate in UTC.
     */
    public static Calendar toCalendar(final LocalDate localDate) {
        final Date date = new Date(localDate.atStartOfDay(ZoneId.of("UTC")).toEpochSecond() * 1000);
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);

        return cal;
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds {@link LocalDate} values.
     */
    public static class LocalDateParameterValue extends ParameterValue {

        private LocalDate ld;

        /**
         * Constructs a new LocalDateParameterValue
         */
        public LocalDateParameterValue() {
            ld = null;
        }

        /**
         * Constructs a new LocalDateParameterValue holding the specified
         * {@link LocalDate}.
         *
         * @param ld The {@link LocalDate} that this parameter value should
         * hold.
         */
        public LocalDateParameterValue(final LocalDate ld) {
            this.ld = ld;
        }

        /**
         * Get the current value from this parameter value.
         * <p>
         * This will default to {@link LocalDate#now LocalDate.now()} if not
         * previously set, meaning that the return value will change over time.
         *
         * @return The {@link LocalDate} that this parameter value is holding.
         */
        public LocalDate get() {
            return ld != null ? ld : LocalDate.now();
        }

        /**
         * Set the current value
         *
         * @param newld The {@link LocalDate} for this parameter value to hold.
         * @return True if the new value was different to the current value,
         * false otherwise.
         */
        public boolean set(final LocalDate newld) {
            if (!Objects.equals(ld, newld)) {
                ld = newld;
                return true;
            }

            return false;
        }

        @Override
        public String validateString(final String s) {
            String err = null;
            try {
                LocalDate.parse(s);
            } catch (final DateTimeParseException ex) {
                err = ex.getMessage();
            }

            return err;
        }

        @Override
        public boolean setStringValue(final String s) {
            final LocalDate newld = StringUtils.isBlank(s) ? null : LocalDate.parse(s);

            return set(newld);
        }

        @Override
        public Object getObjectValue() {
            return get();
        }

        @Override
        public boolean setObjectValue(final Object o) {
            final LocalDate newld;
            if (o == null) {
                newld = null;
            } else if (o instanceof LocalDate localDate) {
                newld = localDate;
            } else {
                throw new IllegalArgumentException(String.format("Unexpected class %s", o.getClass()));
            }

            return set(newld);
        }

        @Override
        protected LocalDateParameterValue createCopy() {
            return new LocalDateParameterValue(ld);
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            return this.getClass() == o.getClass() && Objects.equals(ld, ((LocalDateParameterValue) o).ld);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(ld);
        }

        @Override
        public String toString() {
            return ld != null ? ld.toString() : "";
        }
    }
}
