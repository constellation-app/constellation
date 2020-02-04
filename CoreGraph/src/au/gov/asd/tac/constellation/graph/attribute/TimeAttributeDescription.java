/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalConstants;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A description of a time attribute.
 * <p>
 * Times are considered to be fixed points on a twenty-four hour clock,
 * independent of time-zone. They are represented internally as integer
 * primitives. These integer primitives give the number of milliseconds since
 * the start of the day.
 *
 * The object representation of these attribute values are java
 * {@link LocalTime} objects.
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeDescription.class)
public final class TimeAttributeDescription extends AbstractAttributeDescription {

    private static final Logger LOGGER = Logger.getLogger(TimeAttributeDescription.class.getName());
    private static final int DESCRIPTION_VERSION = 1;
    public static final String ATTRIBUTE_NAME = "time";
    public static final int NULL_VALUE = Integer.MIN_VALUE;

    private int[] data = new int[0];
    private int defaultValue = NULL_VALUE;

    private static final DateTimeFormatter FORMATTER = TemporalFormatting.TIME_FORMATTER;

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

    private static int parseObject(final Object value) {
        if (value == null) {
            return NULL_VALUE;
        } else if (value instanceof LocalTime) {
            return ((LocalTime) value).get(ChronoField.MILLI_OF_DAY);
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return parseString((String) value);
        } else {
            final String msg = String.format("Error converting '%s' to TimeAttributeDescription", value.getClass());
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = parseObject(value);
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = parseString(value);
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
        if (data[id] == NULL_VALUE) {
            return null;
        } else {
            return LocalTime.ofNanoOfDay(((long) data[id]) * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
        }
    }

    @Override
    public String getString(final int id) {
        if (data[id] == NULL_VALUE) {
            return null;
        } else {
            return getAsString(LocalTime.ofNanoOfDay(((long) data[id]) * TemporalConstants.NANOSECONDS_IN_MILLISECOND));
        }
    }

    @Override
    public AttributeDescription copy(GraphReadMethods graph) {
        final TimeAttributeDescription attribute = new TimeAttributeDescription();
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
    public static int parseString(final String time) {
        if (time == null || time.isEmpty()) {
            return NULL_VALUE;
        }

        try {
            final int hour = Integer.parseInt(time.substring(0, 2), 10);
            final int min = Integer.parseInt(time.substring(3, 5), 10);
            final int sec = Integer.parseInt(time.substring(6, 8), 10);
            final int ms = Integer.parseInt(time.substring(9, 12));

            return (hour * 3600000) + (min * 60000) + (sec * 1000) + ms;
        } catch (StringIndexOutOfBoundsException | NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "Can't parse time string '{0}': '{1}'", new Object[]{time, ex.getMessage()});
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
    public static String getAsString(LocalTime time) {
        return time.format(FORMATTER);
    }

    @Override
    public Class<?> getNativeClass() {
        return TimeAttributeDescription.class;
    }

    @Override
    public void setDefault(final Object value) {
        final int parsedValue = parseObject(value);
        defaultValue = parsedValue != NULL_VALUE ? parsedValue : NULL_VALUE;
    }

    @Override
    public Object getDefault() {
        return defaultValue == NULL_VALUE ? null : LocalTime.ofNanoOfDay(((long) defaultValue) * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
    }

    @Override
    public int hashCode(final int id) {
        return data[id];
    }

    @Override
    public boolean equals(final int id1, final int id2) {
        return data[id1] == data[id2];
    }

    @Override
    public boolean canBeImported() {
        return true;
    }

    @Override
    public int ordering() {
        return 8;
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
    public Object convertToNativeValue(Object objectValue) {
        return objectValue == null ? NULL_VALUE : ((LocalTime) objectValue).get(ChronoField.MILLI_OF_DAY);
    }

    @Override
    public int getVersion() {
        return DESCRIPTION_VERSION;
    }
}
