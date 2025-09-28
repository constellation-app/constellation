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
import au.gov.asd.tac.constellation.graph.value.variables.IntVariable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values represent times. The times
 * are stored as primitive int values, whilst the canonical string
 * representation is 'HH:mm:ss.SSS' (with the milliseconds present only if
 * non-zero).
 * <p>
 * Attribute values can be set from canonical strings using
 * {@link #setString setString()}, or from longs and ints using the relevant
 * methods.
 * <p>
 * Attribute values can be retrieved as canonical strings using
 * {@link #getString getString()}, or as {@link Representation} objects (java
 * {@link Number} objects whose toString method yields canonical strings) using
 * {@link #getObject getObject()}.
 * <p>
 * Note that this attribute description should no longer be used and only
 * remains to support legacy graph files.
 *
 * @author sirius
 */
@Deprecated
@ServiceProvider(service = AttributeDescription.class)
public final class TimeAttributeDescriptionV0 extends AbstractAttributeDescription {

    private static final Logger LOGGER = Logger.getLogger(TimeAttributeDescriptionV0.class.getName());
    /**
     * An array of primitives doesn't have a null equivalent, and the default
     * value of 0 for int is a valid time, so we use an otherwise invalid value
     * to indicate that a value has not been set. See setCapacity().
     */
    public static final int NULL_VALUE = Integer.MIN_VALUE;
    private static final int DEFAULT_VALUE = NULL_VALUE;
    private int[] data = new int[0];
    private int defaultValue = DEFAULT_VALUE;
    public static final Pattern RE_TIME1 = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}?(\\.\\d{3})?$");
    public static final Pattern RE_TIME2 = Pattern.compile("^\\d{2}:\\d{2}?(:\\d{2}\\.\\d{3})?$");
    public static final String ATTRIBUTE_NAME = "time";

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
    public void setInt(final int id, final int value) {
        data[id] = value;
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = (int) value;
    }

    private static int setObject(final Object value) {
        switch (value) {
            case Number number -> {
                return number.intValue();
            }       
            case String string -> {
                return setString(string);
            }
            case null -> {
                return NULL_VALUE;
            }
            default -> {
                final String msg = String.format("Error converting '%s' to TimeAttributeDescription", value.getClass());
                throw new IllegalArgumentException(msg);
            }   
        }
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = setObject(value);
    }

    @Override
    public String acceptsString(final String value) {
        return setString(value) == NULL_VALUE ? "Not a valid time value (Expected HH:mm:ss.SSS)" : null;
    }

    private static int setString(final String value) {
        return parseTime(value);
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = setString(value);
    }

    @Override
    public int getInt(final int id) {
        return data[id];
    }

    @Override
    public long getLong(final int id) {
        return data[id];
    }

    @Override
    public Object getObject(final int id) {
        if (data[id] == DEFAULT_VALUE) {
            return null;
        } else {
            return new Representation(data[id]);
        }
    }

    @Override
    public String getString(final int id) {
        int time = data[id];
        return getAsString(time);
    }

    @Override
    public AttributeDescription copy(final GraphReadMethods graph) {
        final TimeAttributeDescriptionV0 attribute = new TimeAttributeDescriptionV0();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
    }

    /**
     * Parse a string in (something close to) ISO format to a Calendar.
     *
     * The following formats are accepted: 'hh:mm:ss', 'hh:mm:ss.SSS'
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
     * @param time An (almost) ISO datetime to be parsed.
     *
     * @return A Calendar representing the input datetime.
     */
    public static int parseTime(final String time) {
        if (StringUtils.isBlank(time)) {
            return NULL_VALUE;
        }

        if (RE_TIME1.matcher(time).matches() || RE_TIME2.matcher(time).matches()) {
            try {
                final int h = Integer.parseInt(time.substring(0, 2), 10);
                final int min = Integer.parseInt(time.substring(3, 5), 10);
                final int sec = time.length() > 6 ? Integer.parseInt(time.substring(6, 8), 10) : 0;
                final int ms = time.length() > 8 ? Integer.parseInt(time.substring(9, 12)) : 0;

                return h * 3600000 + min * 60000 + sec * 1000 + ms;
            } catch (final StringIndexOutOfBoundsException | NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Can''t parse time string ''{0}'': {1}", new Object[]{time, ex.getMessage()});
            }
        } else {
            final String msg = String.format("Can't parse time string '%s'", time);
            LOGGER.warning(msg);
        }
        return NULL_VALUE;
    }

    /**
     * Return the time in its canonical format.
     *
     * @param time The time in internal int representation.
     *
     * @return The time in its canonical format.
     */
    public static String getAsString(int time) {
        if (time == NULL_VALUE) {
            return null;
        }

        int hours = time / 3600000;
        time -= hours * 3600000;
        int mins = time / 60000;
        time -= mins * 60000;
        int secs = time / 1000;
        int ms = time - secs * 1000;

        return ms != 0 ? String.format("%02d:%02d:%02d.%03d", hours, mins, secs, ms) : String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    /**
     * Return the time in its integer format.
     *
     * @param time The time in its calendar representation.
     * @return The time in its integer format.
     */
    public static int getAsInteger(final Calendar time) {
        return time.get(Calendar.HOUR_OF_DAY) * 3600000
                + time.get(Calendar.MINUTE) * 60000
                + time.get(Calendar.SECOND) * 1000 + time.get(Calendar.MILLISECOND);
    }

    @Override
    public Class<?> getNativeClass() {
        return TimeAttributeDescriptionV0.class;
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
        return data[id];
    }

    @Override
    public boolean equals(final int id1, final int id2) {
        return data[id1] == data[id2];
    }

    /**
     * A representation of Number (as a TimeAttributeDescription) that returns a
     * String the way we want it.
     */
    public static class Representation extends Number implements Comparable<Representation> {

        private final int time;

        private Representation(final int time) {
            this.time = time;
        }

        @Override
        public String toString() {
            return time != NULL_VALUE ? TimeAttributeDescriptionV0.getAsString(time) : "";
        }

        @Override
        public int intValue() {
            return time;
        }

        @Override
        public long longValue() {
            return time;
        }

        @Override
        public float floatValue() {
            return time;
        }

        @Override
        public double doubleValue() {
            return time;
        }

        @Override
        public int compareTo(final Representation other) {
            return equals(other) ? 0 : Integer.compare(time, other.time);
        }

        @Override
        public int hashCode() {
            return time;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() == obj.getClass()) {
                final Representation r = (Representation) obj;
                return time == r.time;
            }
            return false;
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
        final int[] sd = (int[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
    }

    @Override
    public NativeAttributeType getNativeType() {
        return NativeAttributeType.INT;
    }

    @Override
    public Object createReadObject(final IntReadable indexReadable) {
        return (IntReadable) () -> data[indexReadable.readInt()];
    }

    @Override
    public Object createWriteObject(final GraphWriteMethods graph, final int attribute, final IntReadable indexReadable) {
        return new IntVariable() {
            @Override
            public int readInt() {
                return data[indexReadable.readInt()];
            }

            @Override
            public void writeInt(final int value) {
                graph.setIntValue(attribute, indexReadable.readInt(), value);
            }
        };
    }
}
