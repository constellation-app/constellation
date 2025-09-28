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
package au.gov.asd.tac.constellation.graph.attribute.compatibility;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.attribute.AbstractAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.LongReadable;
import au.gov.asd.tac.constellation.graph.value.variables.LongVariable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values represent dates (without
 * times) in UTC. The dates are stored as primitive long values, whilst the
 * canonical string representation is 'yyyy-MM-dd'.
 * <p>
 * Attribute values can be set from canonical strings using
 * {@link #setString setString()}, or from {@link Date} and {@link Calendar}
 * objects using {@link #setObject setObject()}.
 * <p>
 * Attribute values can be retrieved as canonical strings using
 * {@link #getString getString()}, or as {@link Representation} objects (java
 * {@link Date} objects whose toString method yields canonical strings) using
 * {@link #getObject getObject()}.
 * <p>
 * Note that this attribute description should no longer be used and only
 * remains to support legacy graph files.
 *
 * @author sirius
 */
@Deprecated
@ServiceProvider(service = AttributeDescription.class)
public final class DateAttributeDescriptionV0 extends AbstractAttributeDescription {

    private static final Logger LOGGER = Logger.getLogger(DateAttributeDescriptionV0.class.getName());
    /**
     * An array of primitives doesn't have a null equivalent, and the default
     * value of 0 for long is a valid time, so we use an otherwise invalid value
     * to indicate that a value has not been set. See setCapacity().
     */
    public static final long NULL_VALUE = Long.MIN_VALUE;
    public static final long DEFAULT_VALUE = NULL_VALUE;
    private long[] data = new long[0];
    private long defaultValue = DEFAULT_VALUE;
    public static final String ATTRIBUTE_NAME = "date";
    /**
     * A regular expression that matches legal date Strings.
     */
    public static final Pattern RE_DATE = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$");
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override
    public String getName() {
        return ATTRIBUTE_NAME;
    }

    @Override
    public int getCapacity() {
        return data.length;
    }

    @Override
    public void setCapacity(final int capacity) {
        final int len = data.length;
        data = Arrays.copyOf(data, capacity);
        if (capacity > len) {
            Arrays.fill(data, len, capacity, defaultValue);
        }
    }

    @Override
    public void clear(final int id) {
        data[id] = defaultValue;
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = value;
    }

    /**
     * Extract a long from an Object.
     *
     * @param value An Object.
     *
     * @return A long.
     */
    private static long setObject(final Object value) {
        switch (value) {
            case Date date -> {
                return date.getTime();
            }      
            case Number number -> {
                return number.longValue();
            }       
            case Calendar calendar -> {
                return calendar.getTimeInMillis();
            }       
            case String string -> {
                return setString(string);
            }
            case null -> {
                return DEFAULT_VALUE;
            }
            default -> throw new IllegalArgumentException("Error converting Object to long");
        }
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = setObject(value);
    }

    /**
     * Extract a long from a String.
     *
     * @param value A String.
     *
     * @return A long.
     */
    private static long setString(final String value) {
        return parseDate(value);
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = setString(value);
    }

    @Override
    public long getLong(final int id) {
        return data[id];
    }

    @Override
    public Object getObject(final int id) {
        return data[id] == DEFAULT_VALUE ? null : new Representation(data[id]);
    }

    @Override
    public String getString(final int id) {
        return getAsString(data[id]);
    }

    @Override
    public AttributeDescription copy(GraphReadMethods graph) {
        final DateAttributeDescriptionV0 attribute = new DateAttributeDescriptionV0();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
    }

    /**
     * Parse a string in (something close to) ISO format to a Calendar.
     *
     * The following format is accepted: 'yyyy-mm-dd'
     *
     * This is basically doing a SimpleDateFormat.parse(), but a lot faster. All
     * fields are numeric.
     *
     * Parsing isn't strict: the date 2011-99-01 will be accepted. This reflects
     * the way that SimpleDateFormat.parse() works. The parsing is lenient in
     * other ways (for instance, punctuation isn't checked), but since the
     * context is parsing of dates from CONSTELLATION files, this isn't expected
     * to be a problem. However, this should not be taken as an excuse to write
     * syntactically incorrect datetime strings elsewhere.
     *
     * @param date An (almost) ISO date to be parsed.
     *
     * @return A Calendar representing the input datetime.
     */
    public static long parseDate(final String date) {
        if (StringUtils.isBlank(date)) {
            return NULL_VALUE;
        }

        try {
            final int y = Integer.parseInt(date.substring(0, 4), 10);
            final int m = Integer.parseInt(date.substring(5, 7), 10);
            final int d = Integer.parseInt(date.substring(8, 10), 10);

            final int h = 0;
            final int min = 0;
            final int sec = 0;
            final int ms = 0;

            final Calendar cal = new GregorianCalendar(UTC);
            cal.set(Calendar.YEAR, y);
            cal.set(Calendar.MONTH, m - 1);
            cal.set(Calendar.DAY_OF_MONTH, d);
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, min);
            cal.set(Calendar.SECOND, sec);
            cal.set(Calendar.MILLISECOND, ms);

            return cal.getTimeInMillis();
        } catch (final StringIndexOutOfBoundsException | NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "Can''t parse date string ''{0}'': {1}", new Object[]{date, ex.getMessage()});
        }

        return NULL_VALUE;
    }

    /**
     * Return the date in its canonical format.
     *
     * @param time The date in internal long representation.
     *
     * @return The date in its canonical format.
     */
    public static String getAsString(final long time) {
        if (time == NULL_VALUE) {
            return null;
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        return String.format("%4d-%02d-%02d",
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public Class<?> getNativeClass() {
        return DateAttributeDescriptionV0.class;
    }

    @Override
    public void setDefault(final Object value) {
        defaultValue = setObject(value);
    }

    @Override
    public Object getDefault() {
        return defaultValue == DEFAULT_VALUE ? null : getAsString(defaultValue);
    }

    @Override
    public int hashCode(final int id) {
        return (int) data[id];
    }

    @Override
    public boolean equals(final int id1, final int id2) {
        return data[id1] == data[id2];
    }

    /**
     * A representation of Date that returns a String the way we want it.
     */
    public static class Representation extends Date {

        private Representation(final long dt) {
            super(dt);
        }

        @Override
        public String toString() {
            final long dt = getTime();
            return dt != NULL_VALUE ? DateAttributeDescriptionV0.getAsString(dt) : "";
        }

        @Override
        public int compareTo(final Date other) {
            return other instanceof Representation && equals(other) ? 0 : super.compareTo(other);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() == obj.getClass()) {
                return toString().equals(obj.toString());
            } else {
                return super.equals(obj);
            }
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

    }

    @Override
    public boolean isClear(final int id) {
        return data[id] == defaultValue;
    }

    @Override
    public Object saveData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public void restoreData(final Object savedData) {
        final long[] sd = (long[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
    }

    @Override
    public NativeAttributeType getNativeType() {
        return NativeAttributeType.LONG;
    }

    @Override
    public String acceptsString(final String value) {
        return parseDate(value) == NULL_VALUE ? "Not a valid date (Expected yyyy-mm-dd)" : null;
    }

    @Override
    public Object createReadObject(final IntReadable indexReadable) {
        return (LongReadable) () -> data[indexReadable.readInt()];
    }

    @Override
    public Object createWriteObject(final GraphWriteMethods graph, final int attribute, final IntReadable indexReadable) {
        return new LongVariable() {
            @Override
            public long readLong() {
                return data[indexReadable.readInt()];
            }

            @Override
            public void writeLong(final long value) {
                graph.setLongValue(attribute, indexReadable.readInt(), value);
            }
        };
    }
}
