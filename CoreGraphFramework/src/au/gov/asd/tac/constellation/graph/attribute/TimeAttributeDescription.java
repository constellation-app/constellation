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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.variables.IntVariable;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalConstants;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
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

    public static final String ATTRIBUTE_NAME = "time";
    public static final int ATTRIBUTE_VERSION = 1;
    public static final Class<Integer> NATIVE_CLASS = Integer.class;
    public static final NativeAttributeType NATIVE_TYPE = NativeAttributeType.INT;
    public static final int DEFAULT_VALUE = 0;

    private int[] data = new int[0];
    private int defaultValue = DEFAULT_VALUE;

    private int convertFromObject(final Object object) throws IllegalArgumentException {
        switch (object) {
            case LocalTime localTime -> {
                return localTime.get(ChronoField.MILLI_OF_DAY);
            }       
            case Number number -> {
                return number.intValue();
            }       
            case String string -> {
                return convertFromString(string);
            }  
            case null -> {
                return (int) getDefault();
            }
            default -> throw new IllegalArgumentException(String.format(
                    "Error converting Object '%s' to time", object.getClass()));
        }
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
     * @param string An (almost) ISO datetime to be parsed.
     *
     * @return A Calendar representing the input datetime.
     */
    private int convertFromString(final String string) throws IllegalArgumentException {
        if (StringUtils.isBlank(string) || String.valueOf(DEFAULT_VALUE).equals(string)) {
            return (int) getDefault();
        } else {
            try {
                final int hour = Integer.parseInt(string.substring(0, 2), 10);
                final int mintue = Integer.parseInt(string.substring(3, 5), 10);
                final int second = Integer.parseInt(string.substring(6, 8), 10);
                final int millisecond = Integer.parseInt(string.substring(9, 12));
                return (hour * 3600000) + (mintue * 60000) + (second * 1000) + millisecond;
            } catch (final StringIndexOutOfBoundsException | NumberFormatException ex) {
                throw new IllegalArgumentException(String.format(
                        "Error converting String '%s' to time (expected hh:mm:ss[.SSS])", string), ex);
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
        return defaultValue == DEFAULT_VALUE ? 0
                : LocalTime.ofNanoOfDay(((long) defaultValue)
                        * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
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
    public int getInt(final int id) {
        return data[id];
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = value;
    }

    @Override
    public long getLong(final int id) {
        return (long) data[id];
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = (int) value;
    }

    @Override
    public String getString(final int id) {
        return data[id] == DEFAULT_VALUE ? null
                : LocalTime.ofNanoOfDay(((long) data[id])
                        * TemporalConstants.NANOSECONDS_IN_MILLISECOND)
                        .format(TemporalFormatting.TIME_FORMATTER);
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
        return data[id] == DEFAULT_VALUE ? null
                : LocalTime.ofNanoOfDay(((long) data[id])
                        * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = convertFromObject(value);
    }

    @Override
    public Object convertToNativeValue(final Object object) {
        return object == null ? DEFAULT_VALUE : ((LocalTime) object)
                .get(ChronoField.MILLI_OF_DAY);
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
    public AttributeDescription copy(GraphReadMethods graph) {
        final TimeAttributeDescription attribute = new TimeAttributeDescription();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
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
    public Object saveData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public void restoreData(final Object savedData) {
        final int[] sd = (int[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
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
