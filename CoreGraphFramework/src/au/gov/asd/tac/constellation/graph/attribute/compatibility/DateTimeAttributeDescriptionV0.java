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
package au.gov.asd.tac.constellation.graph.attribute.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.attribute.AbstractAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.LongReadable;
import au.gov.asd.tac.constellation.graph.value.variables.LongVariable;
import java.text.SimpleDateFormat;
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
 * This describes a type of attribute whose values represent date-times. The
 * date-times are stored as primitive long values, whilst the canonical string
 * representation is 'yyyy-MM-dd HH:mm:ss'.
 * <p>
 * Attribute values can be set from strings in various formats (see
 * {@link #parseDateTime parseDateTime}) using {@link #setString setString()},
 * or from {@link Date} and {@link Calendar} objects using
 * {@link #setObject setObject()}.
 * <p>
 * Attribute values can be retrieved as canonical strings using
 * {@link #getString getString()}, or as {@link Representation} objects (java
 * {@link Date} objects whose toString method yields canonical strings) using
 * {@link #getObject getObject()}.
 * <p>
 * Note that prior to object or string retrieval, the values are updated to
 * reflect the graph's current time-zone.
 * <p>
 * Note that this attribute description should no longer be used and only
 * remains to support legacy graph files.
 *
 * @author sirius
 */
@Deprecated
@ServiceProvider(service = AttributeDescription.class)
public final class DateTimeAttributeDescriptionV0 extends AbstractAttributeDescription {

    /**
     * UTC time zone.
     */
    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final Logger LOGGER = Logger.getLogger(DateTimeAttributeDescriptionV0.class.getName());
    /**
     * An array of primitives doesn't have a null equivalent, and the default
     * value of 0 for long is a valid datetime, so we use an otherwise invalid
     * value to indicate that a value has not been set. See setCapacity().
     */
    public static final long NULL_VALUE = Long.MIN_VALUE;
    public static final long MIN_VALUE = 0;
    private static final long DEFAULT_VALUE = NULL_VALUE;
    private long[] data = new long[0];
    private long defaultValue = NULL_VALUE;
    private int tzAttr = Graph.NOT_FOUND;
    private long attrModificationCounter = -1;
    private TimeZone tz = null;
    public static final String ATTRIBUTE_NAME = "datetime";
    /**
     * A regular expression that matches legal datetime Strings. yyyy-mm-dd
     * hh:mm:ss((.sss)+zzzz
     */
    public static final Pattern RE_DATETIME1 = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}[T ]?\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?([+-]\\d{4})?$");
    public static final Pattern RE_DATETIME2 = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}[T ]?\\d{2}:\\d{2}(:\\d{2}\\.\\d{3})?([+-]\\d{4})?$");
    public static final Pattern RE_DATETIME3 = Pattern.compile("^\\d{8}:\\d{2}:\\d{2}:\\d{2}:\\d{3}$");
    public static final Pattern RE_DATETIME4 = Pattern.compile("^\\d{14}$");

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

    private static long setObject(final Object value, final TimeZone tz) {
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
                return setString(string, tz);
            }
            case null -> {
                return DEFAULT_VALUE;
            }
            default -> throw new IllegalArgumentException("Error converting Object to long: " + value.getClass().getName());
        }
    }

    @Override
    public void setObject(final int id, final Object value) {
        updateTz();
        data[id] = setObject(value, tz);
    }

    private static long setString(final String value, final TimeZone tz) {
        return parseDateTime(value, tz);
    }

    @Override
    public void setString(final int id, final String value) {
        updateTz();
        data[id] = setString(value, tz);
    }

    @Override
    public long getLong(final int id) {
        updateTz();
        return data[id];
    }

    @Override
    public Object getObject(final int id) {
        if (data[id] == DEFAULT_VALUE) {
            return null;
        } else {
            updateTz();
            return new Representation(data[id], tz);
        }
    }

    @Override
    public String getString(final int id) {
        updateTz();

        return getAsString(data[id], tz);
    }

    private void updateTz() {
        if (tzAttr == Graph.NOT_FOUND) {
            tzAttr = graph.getAttribute(GraphElementType.GRAPH, "time_zone");
            if (tzAttr != Graph.NOT_FOUND) {
                tz = (TimeZone) graph.getObjectValue(tzAttr, 0);
                attrModificationCounter = graph.getValueModificationCounter(tzAttr);
            } else {
                tz = UTC;
            }
        } else if (attrModificationCounter != graph.getValueModificationCounter(tzAttr)) {
            tz = (TimeZone) graph.getObjectValue(tzAttr, 0);
            attrModificationCounter = graph.getValueModificationCounter(tzAttr);
        }
    }

    @Override
    public AttributeDescription copy(GraphReadMethods graph) {
        final DateTimeAttributeDescriptionV0 attribute = new DateTimeAttributeDescriptionV0();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
    }

    /**
     * Parse a string in (something close to) ISO format to a Calendar.
     *
     * The following formats are accepted: 'yyyy-mm-dd hh:mm:ss+zzzz',
     * 'yyyy-mm-dd hh:mm:ss-zzzz', 'yyyy-mm-dd hh:mm:ss.SSS+zzzz', 'yyyy-mm-dd
     * hh:mm:ss.SSS-zzzz',
     *
     * (Note that this isn't strictly ISO format, which uses a T instead of a
     * space.)
     *
     * This is basically doing a SimpleDateFormat.parse(), but a lot faster. All
     * fields are numeric. The numeric timezone is mandatory.
     *
     * Parsing isn't strict: the date 2011-99-01 will be accepted. This reflects
     * the way that SimpleDateFormat.parse() works. The parsing is lenient in
     * other ways (for instance, punctuation isn't checked), but since the
     * context is parsing of dates from CONSTELLATION files, this isn't expected
     * to be a problem. However, this should not be taken as an excuse to write
     * syntactically incorrect datetime strings elsewhere.
     *
     * @param dt An (almost) ISO datetime to be parsed.
     * @param tz A TimeZone to use to parse the datetime string. If the string
     * does not have an explicit time zone, this time zone will be used unless
     * it is null, in which case UTC will be used.
     *
     * @return A Calendar representing the input datetime.
     */
    public static long parseDateTime(final String dt, final TimeZone tz) {
        if (StringUtils.isBlank(dt)) {
            return NULL_VALUE;
        }

        if (RE_DATETIME1.matcher(dt).matches() || RE_DATETIME2.matcher(dt).matches() || RE_DATETIME3.matcher(dt).matches()) {
            final int ye;
            final int mo;
            final int da;
            final int ho;
            final int mi;
            TimeZone tzp = (tz == null) ? UTC : tz;
            int p = 16;
            int se = 0;
            int ms = 0;
            if (RE_DATETIME1.matcher(dt).matches() || RE_DATETIME2.matcher(dt).matches()) {
                ye = Integer.parseInt(dt.substring(0, 4), 10);
                mo = Integer.parseInt(dt.substring(5, 7), 10);
                da = Integer.parseInt(dt.substring(8, 10), 10);
                ho = Integer.parseInt(dt.substring(11, 13), 10);
                mi = Integer.parseInt(dt.substring(14, 16), 10);

                // Parse the optional seconds.
                if (dt.length() > p && dt.charAt(p) == ':') {
                    se = Integer.parseInt(dt.substring(p + 1, p + 3), 10);
                }

                // Parse the optional milliseconds and/or timezone offset.
                p = 19;
                if (dt.length() > p && dt.charAt(p) == '.') {
                    ms = Integer.parseInt(dt.substring(p + 1, p + 4), 10);
                    p += 4;
                }

                if (dt.length() > p) {
                    tzp = TimeZone.getTimeZone("GMT" + dt.substring(p));
                }
            } else {
                ye = Integer.parseInt(dt.substring(0, 4), 10);
                mo = Integer.parseInt(dt.substring(4, 6), 10);
                da = Integer.parseInt(dt.substring(6, 8), 10);
                ho = Integer.parseInt(dt.substring(9, 11), 10);
                mi = Integer.parseInt(dt.substring(12, 14), 10);
                se = Integer.parseInt(dt.substring(15, 17), 10);
                ms = Integer.parseInt(dt.substring(18, 20), 10);
            }

            final Calendar cal = new GregorianCalendar(tzp);
            cal.set(Calendar.YEAR, ye);
            cal.set(Calendar.MONTH, mo - 1);
            cal.set(Calendar.DAY_OF_MONTH, da);
            cal.set(Calendar.HOUR_OF_DAY, ho);
            cal.set(Calendar.MINUTE, mi);
            cal.set(Calendar.SECOND, se);
            cal.set(Calendar.MILLISECOND, ms);

            return cal.getTimeInMillis();

        } else if (RE_DATETIME4.matcher(dt).matches()) {
            final int ye = Integer.parseInt(dt.substring(0, 4), 10);
            final int mo = Integer.parseInt(dt.substring(4, 6), 10);
            final int da = Integer.parseInt(dt.substring(6, 8), 10);
            final int ho = Integer.parseInt(dt.substring(8, 10), 10);
            final int mi = Integer.parseInt(dt.substring(10, 12), 10);
            final int se = Integer.parseInt(dt.substring(12, 14), 10);
            final int ms = 0;
            
            final TimeZone tzp = (tz == null) ? UTC : tz;

            final Calendar cal = new GregorianCalendar(tzp);
            cal.set(Calendar.YEAR, ye);
            cal.set(Calendar.MONTH, mo - 1);
            cal.set(Calendar.DAY_OF_MONTH, da);
            cal.set(Calendar.HOUR_OF_DAY, ho);
            cal.set(Calendar.MINUTE, mi);
            cal.set(Calendar.SECOND, se);
            cal.set(Calendar.MILLISECOND, ms);

            return cal.getTimeInMillis();
        } else {
            LOGGER.log(Level.WARNING, "Can''t parse datetime string ''{0}'': {1}", new Object[]{dt, "incorrect format"});
        }

        return NULL_VALUE;
    }

    @Override
    public String acceptsString(final String value) {
        return parseDateTime(value, tz) == NULL_VALUE ? "Not a valid datetime" : null;
    }

    /**
     * Return the datetime in ISO format.
     *
     * @param time The datetime in internal long representation.
     * @param tz the timezone of the datetime.
     *
     * @return The datetime in ISO format.
     */
    public static String getAsString(final long time, final TimeZone tz) {
        if (time == NULL_VALUE) {
            return null;
        }

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(tz);
        String s = sdf.format(time);
        final int ms = (int) (time % 1000);
        if (ms > 0) {
            s += String.format(".%03d", ms);
        }

        return s;
    }

    @Override
    public Class<?> getNativeClass() {
        return DateTimeAttributeDescriptionV0.class;
    }

    @Override
    public void setDefault(final Object value) {
        defaultValue = setObject(value, null);
    }

    @Override
    public Object getDefault() {
        return defaultValue == DEFAULT_VALUE ? null : getAsString(defaultValue, UTC);
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
     * A representation of Datetime that returns a String the way we want it.
     */
    public static class Representation extends Date {

        private final TimeZone tz;

        private Representation(final long dt, final TimeZone tz) {
            super(dt);
            this.tz = tz;
        }

        @Override
        public String toString() {
            final long dt = getTime();
            return dt != NULL_VALUE ? DateTimeAttributeDescriptionV0.getAsString(dt, tz) : null;
        }

        public TimeZone getTimeZone() {
            return tz;
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
