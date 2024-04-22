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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.AbstractAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.ObjectReadable;
import au.gov.asd.tac.constellation.graph.value.variables.ObjectVariable;
import java.util.Arrays;
import org.openide.util.lookup.ServiceProvider;

/**
 * Blaze Attribute Description.
 * <p>
 * Note that this attribute description should no longer be used and only
 * remains to support legacy graph files.
 *
 * @author algol
 */
@Deprecated
@ServiceProvider(service = AttributeDescription.class)
public final class BlazeAttributeDescriptionV0 extends AbstractAttributeDescription {

    public static final String ATTRIBUTE_NAME = "blaze";
    private static final BlazeV0 DEFAULT_VALUE = null;

    private BlazeV0[] data = new BlazeV0[0];
    private BlazeV0 defaultValue = DEFAULT_VALUE;

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
    public void setByte(final int id, final byte value) {
        throw new IllegalArgumentException("Error converting byte to Blaze");
    }

    @Override
    public void setShort(final int id, final short value) {
        throw new IllegalArgumentException("Error converting short to Blaze");
    }

    @Override
    public void setInt(final int id, final int value) {
        throw new IllegalArgumentException("Error converting int to Blaze");
    }

    @Override
    public void setLong(final int id, final long value) {
        throw new IllegalArgumentException("Error converting long to Blaze");
    }

    @Override
    public void setFloat(final int id, final float value) {
        throw new IllegalArgumentException("Error converting float to Blaze");
    }

    @Override
    public void setDouble(final int id, final double value) {
        throw new IllegalArgumentException("Error converting double to Blaze");
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        throw new IllegalArgumentException("Error converting boolean to Blaze");
    }

    @Override
    public void setChar(final int id, final char value) {
        throw new IllegalArgumentException("Error converting char to Blaze");
    }

    /**
     * Extract a Blaze from an Object.
     *
     * @param value An Object.
     *
     * @return An Blaze.
     */
    private static BlazeV0 setObject(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof BlazeV0 blaze) {
            return blaze;
        } else if (value instanceof String string) {
            return BlazeV0.valueOf(string);
        } else {
            final String msg = String.format("Error converting Object '%s' to Blaze", value.getClass());
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = setObject(value);
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = BlazeV0.valueOf(value);
    }

    @Override
    public byte getByte(final int id) {
        throw new IllegalArgumentException("Error converting Blaze to byte");
    }

    @Override
    public short getShort(final int id) {
        throw new IllegalArgumentException("Error converting Blaze to short");
    }

    @Override
    public int getInt(final int id) {
        throw new IllegalArgumentException("Error converting Blaze to int");
    }

    @Override
    public long getLong(final int id) {
        throw new IllegalArgumentException("Error converting Blaze to long");
    }

    @Override
    public float getFloat(final int id) {
        throw new IllegalArgumentException("Error converting Blaze to float");
    }

    @Override
    public double getDouble(final int id) {
        throw new IllegalArgumentException("Error converting Blaze to double");
    }

    @Override
    public boolean getBoolean(final int id) {
        throw new IllegalArgumentException("Error converting Blaze to boolean");
    }

    @Override
    public char getChar(final int id) {
        throw new IllegalArgumentException("Error converting Blaze to char");
    }

    @Override
    public Object getObject(final int id) {
        return data[id];
    }

    @Override
    public String getString(final int id) {
        return data[id] != null ? data[id].toString() : null;
    }

    @Override
    public AttributeDescription copy(GraphReadMethods graph) {
        final BlazeAttributeDescriptionV0 attribute = new BlazeAttributeDescriptionV0();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
    }

    @Override
    public Class<?> getNativeClass() {
        return BlazeV0.class;
    }

    @Override
    public void setDefault(final Object value) {
        defaultValue = setObject(value != null ? value : DEFAULT_VALUE);
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
        return data[id1] == data[id2];
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
        final BlazeV0[] sd = (BlazeV0[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
    }

    @Override
    public Object createReadObject(final IntReadable indexReadable) {
        return (ObjectReadable) () -> data[indexReadable.readInt()];
    }

    @Override
    public Object createWriteObject(final GraphWriteMethods graph, final int attribute, final IntReadable indexReadable) {
        return new ObjectVariable() {
            @Override
            public Object readObject() {
                return data[indexReadable.readInt()];
            }

            @Override
            public void writeObject(final Object value) {
                graph.setObjectValue(attribute, indexReadable.readInt(), value);
            }
        };
    }
}
