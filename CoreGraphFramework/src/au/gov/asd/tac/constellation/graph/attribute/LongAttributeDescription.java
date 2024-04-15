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
import au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess;
import au.gov.asd.tac.constellation.graph.locking.ParameterWriteAccess;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.LongReadable;
import au.gov.asd.tac.constellation.graph.value.variables.LongVariable;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are primitive longs.
 * <p>
 *
 * When setting these attribute values from numeric types, the values are
 * implicitly or explicitly cast as necessary. The
 * {@link #setString setString()} method will utilise {@link Long#parseLong}.
 * The {@link #setBoolean setBoolean()} method will yield 1 for true and 0 for
 * false.
 * <p>
 * When retrieving these attribute values as numeric types the values are
 * implicitly or explicitly cast as necessary. The
 * {@link #getString getString()} method will utilise {@link String#valueOf}.
 * The {@link #getBoolean getBoolean()} method will return false for 0, and true
 * for any other value.
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeDescription.class)
public final class LongAttributeDescription extends AbstractAttributeDescription {

    public static final String ATTRIBUTE_NAME = "long";
    public static final Class<Long> NATIVE_CLASS = long.class;
    public static final NativeAttributeType NATIVE_TYPE = NativeAttributeType.LONG;
    public static final long DEFAULT_VALUE = 0L;

    private long[] data = new long[0];
    private long defaultValue = DEFAULT_VALUE;

    @SuppressWarnings("unchecked") // Casts are manually checked
    private long convertFromObject(final Object object) throws IllegalArgumentException {
        switch (object) {
            case Number number -> {
                return number.longValue();
            }       
            case Boolean bool -> {
                return bool ? 1L : 0L;
            }       
            case Character character -> {
                return (long) character;
            }       
            case String string -> {
                return convertFromString(string);
            }
            case null -> {
                return (long) getDefault();
            }
            default -> throw new IllegalArgumentException(String.format(
                    "Error converting Object '%s' to long", object.getClass()));
        }
    }

    private long convertFromString(final String string) throws IllegalArgumentException {
        if (StringUtils.isBlank(string)) {
            return (long) getDefault();
        } else {
            try {
                return Long.parseLong(string);
            } catch (final NumberFormatException ex) {
                throw new IllegalArgumentException(String.format(
                        "Error converting String '%s' to long", string), ex);
            }
        }
    }

    @Override
    public String getName() {
        return ATTRIBUTE_NAME;
    }

    @Override
    public Class<?> getNativeClass() {
        return long.class;
    }

    @Override
    public NativeAttributeType getNativeType() {
        return NATIVE_TYPE;
    }

    @Override
    public Object getDefault() {
        return defaultValue;
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
    public byte getByte(final int id) {
        return (byte) data[id];
    }

    @Override
    public void setByte(final int id, final byte value) {
        data[id] = (long) value;
    }

    @Override
    public short getShort(final int id) {
        return (short) data[id];
    }

    @Override
    public void setShort(final int id, final short value) {
        data[id] = (long) value;
    }

    @Override
    public int getInt(final int id) {
        return (int) data[id];
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = (long) value;
    }

    @Override
    public long getLong(final int id) {
        return data[id];
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = value;
    }

    @Override
    public float getFloat(final int id) {
        return (float) data[id];
    }

    @Override
    public void setFloat(final int id, final float value) {
        data[id] = (long) value;
    }

    @Override
    public double getDouble(final int id) {
        return (double) data[id];
    }

    @Override
    public void setDouble(final int id, final double value) {
        data[id] = (long) value;
    }

    @Override
    public boolean getBoolean(final int id) {
        return data[id] != 0L;
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        data[id] = value ? 1L : 0L;
    }

    @Override
    public char getChar(final int id) {
        return (char) data[id];
    }

    @Override
    public void setChar(final int id, final char value) {
        data[id] = (long) value;
    }

    @Override
    public String getString(final int id) {
        return String.valueOf(data[id]);
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
        return data[id];
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = convertFromObject(value);
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
        final LongAttributeDescription attribute = new LongAttributeDescription();
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
    public void save(final int id, final ParameterWriteAccess access) {
        access.setLong(data[id]);
    }

    @Override
    public void restore(final int id, final ParameterReadAccess access) {
        data[id] = access.getUndoLong();
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
