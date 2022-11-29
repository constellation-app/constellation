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
package au.gov.asd.tac.constellation.graph.attribute.compatibility;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.AbstractAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.StringReadable;
import au.gov.asd.tac.constellation.graph.value.variables.StringVariable;
import java.util.Arrays;

/**
 * This describes a type of attribute whose values are lists of the names of
 * other attributes.
 * <p>
 * The string representation of an attribute value of this type consists is a
 * comma-separated list of attribute names.
 * <p>
 * Note that this attribute description should no longer be used and only
 * remains to support legacy graph files.
 *
 * @author algol
 */
@Deprecated
//@ServiceProvider(service = AttributeDescription.class)
public final class AttrListAttributeDescriptionV0 extends AbstractAttributeDescription {

    private static final String DEFAULT_VALUE = null;
    public static final String ATTR_NAME = "attr_list";
    private String[] data = new String[0];
    private String defaultValue = DEFAULT_VALUE;

    @Override
    public String getName() {
        return ATTR_NAME;
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
    public void setByte(final int id, final byte value) {
        throw new IllegalArgumentException(String.format("Error converting byte to %s", getName()));
    }

    @Override
    public void setShort(final int id, final short value) {
        throw new IllegalArgumentException(String.format("Error converting short to %s", getName()));
    }

    @Override
    public void setInt(final int id, final int value) {
        throw new IllegalArgumentException(String.format("Error converting int to %s", getName()));
    }

    @Override
    public void setLong(final int id, final long value) {
        throw new IllegalArgumentException(String.format("Error converting long to %s", getName()));
    }

    @Override
    public void setFloat(final int id, final float value) {
        throw new IllegalArgumentException(String.format("Error converting float to %s", getName()));
    }

    @Override
    public void setDouble(final int id, final double value) {
        throw new IllegalArgumentException(String.format("Error converting double to %s", getName()));
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        throw new IllegalArgumentException(String.format("Error converting boolean to %s", getName()));
    }

    @Override
    public void setChar(final int id, final char value) {
        throw new IllegalArgumentException(String.format("Error converting char to %s", getName()));
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = (String) value;
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = value;
    }

    @Override
    public byte getByte(final int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to byte", getName()));
    }

    @Override
    public short getShort(final int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to short", getName()));
    }

    @Override
    public int getInt(final int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to int", getName()));
    }

    @Override
    public long getLong(final int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to long", getName()));
    }

    @Override
    public float getFloat(final int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to float", getName()));
    }

    @Override
    public double getDouble(final int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to double", getName()));
    }

    @Override
    public boolean getBoolean(final int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to boolean", getName()));
    }

    @Override
    public char getChar(final int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to char", getName()));
    }

    @Override
    public Object getObject(final int id) {
        return data[id];
    }

    @Override
    public String getString(final int id) {
        return data[id];
    }

    @Override
    public AttributeDescription copy(GraphReadMethods graph) {
        final AttrListAttributeDescriptionV0 attribute = new AttrListAttributeDescriptionV0();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
    }

    @Override
    public Class<?> getNativeClass() {
        return AttrListAttributeDescriptionV0.class;
    }

    @Override
    public void setDefault(final Object value) {
        defaultValue = (String) value;
    }

    @Override
    public Object getDefault() {
        return defaultValue;
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
    public boolean isClear(final int id) {
        return equals(data[id], defaultValue);
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
    public Object createReadObject(IntReadable indexReadable) {
        return (StringReadable) () -> data[indexReadable.readInt()];
    }

    @Override
    public Object createWriteObject(GraphWriteMethods graph, int attribute, IntReadable indexReadable) {
        return new StringVariable() {
            @Override
            public String readString() {
                return data[indexReadable.readInt()];
            }

            @Override
            public void writeString(String value) {
                graph.setObjectValue(attribute, indexReadable.readInt(), value);
            }
        };
    }
}
