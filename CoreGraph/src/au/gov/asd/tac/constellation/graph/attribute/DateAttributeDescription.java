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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A description of a date attribute.
 * <p>
 * Dates are considered to be fixed points on the Gregorian calendar,
 * independent of time-zone. They are represented internally as long primitives.
 * These long primitives give the number of days since the epoch to the desired
 * date.
 *
 * The object representation of these attribute values are java
 * {@link LocalDate} objects.
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeDescription.class)
public final class DateAttributeDescription extends AbstractAttributeDescription {

    private static final Logger LOGGER = Logger.getLogger(DateAttributeDescription.class.getName());
    private static final int DESCRIPTION_VERSION = 1;
    public static final String ATTRIBUTE_NAME = "date";
    public static final long NULL_VALUE = Long.MIN_VALUE;
    public static final DateTimeFormatter FORMATTER = TemporalFormatting.DATE_FORMATTER;

    private long[] data = new long[0];
    private long defaultValue = NULL_VALUE;

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
    private static long parseObject(final Object value) {
        if (value == null) {
            return NULL_VALUE;
        } else if (value instanceof LocalDate) {
            return ((LocalDate) value).toEpochDay();
        } else if (value instanceof Date) {
            return ((Date) value).getTime() / TemporalConstants.MILLISECONDS_IN_DAY;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof Calendar) {
            return ((Calendar) value).getTimeInMillis() / TemporalConstants.MILLISECONDS_IN_DAY;
        } else if (value instanceof String) {
            return parseString((String) value);
        } else {
            throw new IllegalArgumentException("Error converting Object to long");
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
    public long getLong(final int id) {
        return data[id];
    }

    @Override
    public Object getObject(final int id) {
        if (data[id] == NULL_VALUE) {
            return null;
        } else {
            return LocalDate.ofEpochDay(data[id]);
        }
    }

    @Override
    public String getString(final int id) {
        if (data[id] == NULL_VALUE) {
            return null;
        }
        return getAsString(LocalDate.ofEpochDay(data[id]));
    }

    @Override
    public AttributeDescription copy(GraphReadMethods graph) {
        final DateAttributeDescription attribute = new DateAttributeDescription();
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
     * @param value An (almost) ISO date to be parsed.
     *
     * @return A Calendar representing the input datetime.
     */
    public static long parseString(final String value) {
        if (value == null || value.isEmpty()) {
            return NULL_VALUE;
        }

        try {
            final int y = Integer.parseInt(value.substring(0, 4), 10);
            final int m = Integer.parseInt(value.substring(5, 7), 10);
            final int d = Integer.parseInt(value.substring(8, 10), 10);
            return LocalDate.of(y, m, d).toEpochDay();
        } catch (StringIndexOutOfBoundsException | NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "Can't parse date string '{0}': '{1}'", new Object[]{value, ex.getMessage()});
        }

        return NULL_VALUE;
    }

    /**
     * Return the date in its canonical format.
     *
     * @param value The date in internal long representation.
     *
     * @return The date in its canonical format.
     */
    public static String getAsString(final LocalDate value) {
        return value.format(FORMATTER);
    }

    @Override
    public Class<?> getNativeClass() {
        return DateAttributeDescription.class;
    }

    @Override
    public void setDefault(final Object value) {
        final long parsedValue = parseObject(value);
        defaultValue = parsedValue != NULL_VALUE ? parsedValue : NULL_VALUE;
    }

    @Override
    public Object getDefault() {
        return defaultValue == NULL_VALUE ? null : LocalDate.ofEpochDay(defaultValue);
    }

    @Override
    public int hashCode(final int id) {
        return (int) data[id];
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
        return 7;
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
    public Object convertToNativeValue(Object objectValue) {
        return objectValue == null ? NULL_VALUE : ((LocalDate) objectValue).toEpochDay();
    }

    @Override
    public String acceptsString(String value) {
        return parseString(value) == NULL_VALUE ? "Not a valid date" : null;
    }

    @Override
    public int getVersion() {
        return DESCRIPTION_VERSION;
    }
}
