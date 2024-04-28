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
import au.gov.asd.tac.constellation.graph.value.variables.IntVariable;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are primitive integers.
 * <p>
 *
 * When setting these attribute values from numeric types, the values are
 * implicitly or explicitly cast as necessary. The
 * {@link #setString setString()} method will utilise {@link Integer#parseInt}.
 * The {@link #setBoolean setBoolean()} method will yield 1 for true and 0 for
 * false.
 * <p>
 * When retrieving these attribute values as numeric types the values are
 * implicitly or explicitly cast as necessary. The
 * {@link #getString getString()} method will utilise {@link String#valueOf}.
 * The {@link #getBoolean getBoolean()} method will return false for 0, and true
 * for any other value.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public final class IntegerAttributeDescription extends AbstractAttributeDescription {

    public static final String ATTRIBUTE_NAME = "integer";
    public static final Class<Integer> NATIVE_CLASS = int.class;
    public static final NativeAttributeType NATIVE_TYPE = NativeAttributeType.INT;
    public static final int DEFAULT_VALUE = 0;

    private int[] data = new int[0];
    private int defaultValue = DEFAULT_VALUE;

    @SuppressWarnings("unchecked") // Casts are manually checked
    private int convertFromObject(final Object object) throws IllegalArgumentException {
        switch (object) {
            case Number number -> {
                return number.intValue();
            }       
            case Boolean bool -> {
                return bool ? 1 : 0;
            }       
            case Character character -> {
                return character;
            }       
            case String string -> {
                return convertFromString(string);
            }
            case null -> {
                return (int) getDefault();
            }
            default -> throw new IllegalArgumentException(String.format(
                    "Error converting Object '%s' to integer", object.getClass()));
        }
    }

    private int convertFromString(final String string) throws IllegalArgumentException {
        if (StringUtils.isBlank(string)) {
            return (int) getDefault();
        } else {
            try {
                return Integer.parseInt(string);
            } catch (final NumberFormatException ex) {
                throw new IllegalArgumentException(String.format(
                        "Error converting String '%s' to integer", string), ex);
            }
        }
    }

    @Override
    public String getName() {
        return ATTRIBUTE_NAME;
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
        data[id] = value;
    }

    @Override
    public short getShort(final int id) {
        return (short) data[id];
    }

    @Override
    public void setShort(final int id, final short value) {
        data[id] = value;
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
        return data[id];
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = (int) value;
    }

    @Override
    public float getFloat(final int id) {
        return data[id];
    }

    @Override
    public void setFloat(final int id, final float value) {
        data[id] = (int) value;
    }

    @Override
    public double getDouble(final int id) {
        return data[id];
    }

    @Override
    public void setDouble(final int id, final double value) {
        data[id] = (int) value;
    }

    @Override
    public boolean getBoolean(final int id) {
        return data[id] != 0;
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        data[id] = value ? 1 : 0;
    }

    @Override
    public char getChar(final int id) {
        return (char) data[id];
    }

    @Override
    public void setChar(final int id, final char value) {
        data[id] = value;
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
        final IntegerAttributeDescription attribute = new IntegerAttributeDescription();
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
    public void save(final int id, final ParameterWriteAccess access) {
        access.setInt(data[id]);
    }

    @Override
    public void restore(final int id, final ParameterReadAccess access) {
        data[id] = access.getUndoInt();
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
