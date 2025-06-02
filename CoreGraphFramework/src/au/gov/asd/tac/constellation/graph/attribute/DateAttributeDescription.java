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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.LongReadable;
import au.gov.asd.tac.constellation.graph.value.variables.LongVariable;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalConstants;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
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

    public static final String ATTRIBUTE_NAME = "date";
    public static final int ATTRIBUTE_VERSION = 1;
    public static final Class<Long> NATIVE_CLASS = Long.class;
    public static final NativeAttributeType NATIVE_TYPE = NativeAttributeType.LONG;
    public static final long DEFAULT_VALUE = 0L;

    private long[] data = new long[0];
    private long defaultValue = DEFAULT_VALUE;

    private long convertFromObject(final Object object) throws IllegalArgumentException {
        switch (object) {
            case LocalDate localDate -> {
                return localDate.toEpochDay();
            }       
            case Date date -> {
                return date.getTime() / TemporalConstants.MILLISECONDS_IN_DAY;
            }       
            case Calendar calendar -> {
                return calendar.getTimeInMillis() / TemporalConstants.MILLISECONDS_IN_DAY;
            }       
            case Number number -> {
                return number.longValue();
            }       
            case String string -> {
                return convertFromString(string);
            }
            case null -> {
                return (long) getDefault();
            }
            default -> throw new IllegalArgumentException(String.format(
                    "Error converting Object '%s' to date", object.getClass()));
        }
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
     * @param string An (almost) ISO date to be parsed.
     *
     * @return A Calendar representing the input datetime.
     */
    public long convertFromString(final String string) throws IllegalArgumentException {
        if (StringUtils.isBlank(string) || String.valueOf(DEFAULT_VALUE).equals(string)) {
            return (long) getDefault();
        } else {
            try {
                final int year = Integer.parseInt(string.substring(0, 4), 10);
                final int month = Integer.parseInt(string.substring(5, 7), 10);
                final int day = Integer.parseInt(string.substring(8, 10), 10);
                return LocalDate.of(year, month, day).toEpochDay();
            } catch (final StringIndexOutOfBoundsException | NumberFormatException ex) {
                throw new IllegalArgumentException(String.format(
                        "Error converting String '%s' to date (expected yyyy-mm-dd)", string), ex);
            }
        }
    }

    @Override
    public String getName() {
        return ATTRIBUTE_NAME;
    }

    @Override
    public int getVersion() {
        return ATTRIBUTE_VERSION;
    }

    @Override
    public Class<?> getNativeClass() {
        return NATIVE_CLASS;
    }

    @Override
    public NativeAttributeType getNativeType() {
        return NATIVE_TYPE;
    }

    @Override
    public Object getDefault() {
        return defaultValue == DEFAULT_VALUE ? 0L
                : LocalDate.ofEpochDay(defaultValue);
    }

    @Override
    public void setDefault(final Object value) {
        defaultValue = convertFromObject(value);
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
    public long getLong(final int id) {
        return data[id] == defaultValue ? 0L : data[id];
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = value;
    }

    @Override
    public String getString(final int id) {
        return data[id] == defaultValue ? null
                : LocalDate.ofEpochDay(data[id]).format(TemporalFormatting.DATE_FORMATTER);
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = convertFromString(value);
    }

    @Override
    public String acceptsString(final String value) {
        try {
            convertFromString(value);
            return null;
        } catch (final IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public Object getObject(final int id) {
        return data[id] == DEFAULT_VALUE ? null : LocalDate.ofEpochDay(data[id]);
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = convertFromObject(value);
    }

    @Override
    public Object convertToNativeValue(final Object object) {
        return object == null ? DEFAULT_VALUE : ((LocalDate) object).toEpochDay();
    }

    @Override
    public boolean isClear(final int id) {
        return data[id] == defaultValue;
    }

    @Override
    public void clear(final int id) {
        data[id] = defaultValue;
    }

    @Override
    public AttributeDescription copy(final GraphReadMethods graph) {
        final DateAttributeDescription attribute = new DateAttributeDescription();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
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
    public Object saveData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public void restoreData(final Object savedData) {
        final long[] sd = (long[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
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
