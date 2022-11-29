/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.value.readables.StringReadable;
import au.gov.asd.tac.constellation.graph.value.variables.StringVariable;
import java.util.Arrays;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are Strings.
 * <p>
 * All methods of setting these attribute values other than
 * {@link #setString setString()} will utilise
 * {@link String#valueOf String.valueOf()}.
 * <p>
 * When retrieving these attribute values as numeric types, 0 will be yielded
 * for null or empty strings, otherwise the string will be parsed as the numeric
 * type being requested. The {@link #getBoolean getBoolean()} method will yield
 * false for null or empty strings, otherwise
 * {@link Boolean#parseBoolean Boolean.parseBoolean()}.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public final class StringAttributeDescription extends AbstractAttributeDescription {

    public static final String ATTRIBUTE_NAME = "string";
    public static final Class<String> NATIVE_CLASS = String.class;
    public static final NativeAttributeType NATIVE_TYPE = NativeAttributeType.OBJECT;
    private static final String DEFAULT_VALUE = null;

    private String[] data = new String[0];
    private String defaultValue = DEFAULT_VALUE;

    @SuppressWarnings("unchecked") // Casts are manually checked
    private String convertFromObject(final Object object) throws IllegalArgumentException {
        if (object == null) {
            return (String) getDefault();
        } else if (object instanceof Number) {
            return ((Number) object).toString();
        } else if (object instanceof Boolean) {
            return ((Boolean) object).toString();
        } else if (object instanceof Character) {
            return ((Character) object).toString();
        } else if (object instanceof String) {
            return (String) object;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Error converting Object '%s' to String", object.getClass()));
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
        return data[id] != null && !data[id].isEmpty() ? Byte.parseByte(data[id]) : (byte) 0;
    }

    @Override
    public void setByte(final int id, final byte value) {
        data[id] = String.valueOf(value);
    }

    @Override
    public short getShort(final int id) {
        return data[id] != null && !data[id].isEmpty() ? Short.parseShort(data[id]) : (short) 0;
    }

    @Override
    public void setShort(final int id, final short value) {
        data[id] = String.valueOf(value);
    }

    @Override
    public int getInt(final int id) {
        return data[id] != null && !data[id].isEmpty() ? Integer.parseInt(data[id]) : 0;
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = String.valueOf(value);
    }

    @Override
    public long getLong(final int id) {
        return data[id] != null && !data[id].isEmpty() ? Long.parseLong(data[id]) : 0L;
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = String.valueOf(value);
    }

    @Override
    public float getFloat(final int id) {
        return data[id] != null && !data[id].isEmpty() ? Float.parseFloat(data[id]) : 0.0F;
    }

    @Override
    public void setFloat(final int id, final float value) {
        data[id] = String.valueOf(value);
    }

    @Override
    public double getDouble(final int id) {
        return data[id] != null && !data[id].isEmpty() ? Double.parseDouble(data[id]) : 0.0;
    }

    @Override
    public void setDouble(final int id, final double value) {
        data[id] = String.valueOf(value);
    }

    @Override
    public boolean getBoolean(final int id) {
        return data[id] != null && !data[id].isEmpty() && Boolean.parseBoolean(data[id]);
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        data[id] = String.valueOf(value);
    }

    @Override
    public char getChar(final int id) {
        return data[id] != null && !data[id].isEmpty() ? data[id].charAt(0) : (char) 0;
    }

    @Override
    public void setChar(final int id, final char value) {
        data[id] = String.valueOf(value);
    }

    @Override
    public String getString(final int id) {
        return data[id];
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = value;
    }

    @Override
    public Object getObject(final int id) {
        return data[id];
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = value != null ? String.valueOf(value) : null;
    }

    @Override
    public boolean isClear(final int id) {
        return equals(data[id], defaultValue);
    }

    @Override
    public void clear(final int id) {
        data[id] = defaultValue;
    }

    @Override
    public AttributeDescription copy(final GraphReadMethods graph) {
        final StringAttributeDescription attribute = new StringAttributeDescription();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
    }

    @Override
    public int hashCode(final int id) {
        return data[id] == null ? 0 : data[id].hashCode();
    }

    @Override
    public boolean equals(final int id1, final int id2) {
        return data[id1] == null ? data[id2] == null : data[id1].equals(data[id2]);
    }

    @Override
    public void save(final int id, final ParameterWriteAccess access) {
        access.setObject(data[id]);
    }

    @Override
    public void restore(final int id, final ParameterReadAccess access) {
        data[id] = (String) access.getUndoObject();
    }

    @Override
    public Object saveData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public void restoreData(final Object savedData) {
        final String[] sd = (String[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
    }

    @Override
    public Object createReadObject(final IntReadable indexReadable) {
        return (StringReadable) () -> data[indexReadable.readInt()];
    }

    @Override
    public Object createWriteObject(final GraphWriteMethods graph, final int attribute, final IntReadable indexReadable) {
        return new StringVariable() {
            @Override
            public String readString() {
                return data[indexReadable.readInt()];
            }

            @Override
            public void writeString(final String value) {
                graph.setStringValue(attribute, indexReadable.readInt(), value);
            }
        };
    }
}
