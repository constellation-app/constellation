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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphIndex;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess;
import au.gov.asd.tac.constellation.graph.locking.ParameterWriteAccess;
import java.util.Arrays;
import java.util.Random;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are boolean primitives.
 * <p>
 * When setting these attribute values from numeric types, 0 is considered
 * false, whilst all other values considered true. The
 * {@link #setString setString()} method will utilise
 * {@link Boolean#parseBoolean}.
 * <p>
 * When retrieving these attribute values as numeric types, false is represented
 * as 0, whilst true is represented as 1. The {@link #getString getString()}
 * method will utilise {@link String#valueOf}.
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeDescription.class)
public final class BooleanAttributeDescription extends AbstractAttributeDescription {

    private static final Random RANDOM = new Random();
    private static final boolean DEFAULT_VALUE = false;
    private boolean[] data = new boolean[0];
    private boolean defaultValue = DEFAULT_VALUE;
    private final int trueHash = RANDOM.nextInt();
    private final int falseHash = RANDOM.nextInt();
    public static final String ATTRIBUTE_NAME = "boolean";

    @Override
    public String getName() {
        return ATTRIBUTE_NAME;
    }

    @Override
    public Class<?> getNativeClass() {
        return boolean.class;
    }

    @Override
    public Object getDefault() {
        return defaultValue;
    }

    @Override
    public void setDefault(final Object value) {
        if (value instanceof Number) {
            defaultValue = ((Number) value).longValue() != 0;
        } else if (value instanceof Boolean) {
            defaultValue = (Boolean) value;
        } else if (value instanceof String) {
            defaultValue = Boolean.parseBoolean((String) value);
        } else {
            defaultValue = false;
        }
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

    private static boolean setObject(final Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue() != 0;
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Character) {
            return ((char) value) != 0;
        } else {
            return DEFAULT_VALUE;
        }
    }

    private static boolean setString(String value) {
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new IllegalArgumentException("Not a valid boolean value");
        }
    }

    @Override
    public byte getByte(final int id) {
        return data[id] ? (byte) 1 : (byte) 0;
    }

    @Override
    public void setByte(final int id, final byte value) {
        data[id] = value != 0;
    }

    @Override
    public short getShort(final int id) {
        return data[id] ? (short) 1 : (short) 0;
    }

    @Override
    public void setShort(final int id, final short value) {
        data[id] = value != 0;
    }

    @Override
    public int getInt(final int id) {
        return data[id] ? 1 : 0;
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = value != 0;
    }

    @Override
    public long getLong(final int id) {
        return data[id] ? 1L : 0L;
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = value != 0L;
    }

    @Override
    public float getFloat(final int id) {
        return data[id] ? 1.0f : 0.0f;
    }

    @Override
    public void setFloat(final int id, final float value) {
        data[id] = value != 0.0f;
    }

    @Override
    public double getDouble(final int id) {
        return data[id] ? 1.0 : 0.0;
    }

    @Override
    public void setDouble(final int id, final double value) {
        data[id] = value != 0.0;
    }

    @Override
    public boolean getBoolean(final int id) {
        return data[id];
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        data[id] = value;
    }

    @Override
    public char getChar(final int id) {
        return data[id] ? (char) 1 : (char) 0;
    }

    @Override
    public void setChar(final int id, final char value) {
        data[id] = value != 0;
    }

    @Override
    public Object getObject(final int id) {
        return data[id];
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = setObject(value);
    }

    @Override
    public String getString(final int id) {
        return String.valueOf(data[id]);
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = Boolean.parseBoolean(value);
    }

    @Override
    public String acceptsString(String value) {
        try {
            setString(value);
            return null;
        } catch (Exception ex) {
            return "Not a valid boolean value";
        }
    }

    @Override
    public boolean isClear(final int id) {
        return data[id] == defaultValue;
    }

    @Override
    public void clear(final int id) {
        data[id] = DEFAULT_VALUE;
    }

    @Override
    public AttributeDescription copy(GraphReadMethods graph) {
        final BooleanAttributeDescription attribute = new BooleanAttributeDescription();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
    }

    @Override
    public NativeAttributeType getNativeType() {
        return NativeAttributeType.BOOLEAN;
    }

    @Override
    public boolean canBeImported() {
        return true;
    }

    @Override
    public int hashCode(final int id) {
        return data[id] ? trueHash : falseHash;
    }

    @Override
    public boolean equals(final int id1, final int id2) {
        return data[id1] == data[id2];
    }

    @Override
    public int ordering() {
        return 2;
    }

    @Override
    public void save(final int id, final ParameterWriteAccess access) {
        access.setInt(data[id] ? 1 : 0);
    }

    @Override
    public void restore(final int id, final ParameterReadAccess access) {
        data[id] = access.getUndoInt() != 0;
    }

    @Override
    public Object saveData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public void restoreData(final Object savedData) {
        final boolean[] sd = (boolean[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
    }

    @Override
    public boolean supportsIndexType(GraphIndexType indexType) {
        return indexType != GraphIndexType.ORDERED;
    }

    @Override
    public GraphIndex createIndex(GraphIndexType indexType) {
        return indexType == GraphIndexType.ORDERED ? NULL_GRAPH_INDEX : new Index();
    }

    private class Index implements GraphIndex {

        private int[] id2position = new int[data.length];
        private int[] position2id = new int[data.length];
        int nextTrue = 0;
        int nextFalse = data.length;

        @Override
        public void addElement(int element) {
            if (data[element]) {
                id2position[element] = nextTrue;
                position2id[nextTrue++] = element;
            } else {
                id2position[element] = --nextFalse;
                position2id[nextFalse] = element;
            }
        }

        @Override
        public void removeElement(int element) {
            int position = id2position[element];
            if (position < nextTrue) {
                int lastTrue = position2id[--nextTrue];
                position2id[position] = lastTrue;
                id2position[lastTrue] = position;
            } else if (position >= nextFalse) {
                int firstFalse = position2id[nextFalse++];
                position2id[position] = firstFalse;
                id2position[firstFalse] = position;
            }
        }

        @Override
        public void updateElement(int element) {
            removeElement(element);
            addElement(element);
        }

        @Override
        public GraphIndexResult getElementsWithAttributeValue(Object value) {
            if ((Boolean) value) {
                return new IndexResult(nextTrue, 0);
            } else {
                return new IndexResult(data.length - nextFalse, nextFalse);
            }
        }

        @Override
        public GraphIndexResult getElementsWithAttributeValueRange(Object start, Object end) {
            return null;
        }

        @Override
        public void expandCapacity(int newCapacity) {
            int[] i2p = new int[newCapacity];
            int[] p2i = new int[newCapacity];
            int nt = 0;
            int nf = newCapacity;

            for (int i = 0; i < nextTrue; i++) {
                int element = position2id[i];
                i2p[element] = nt;
                p2i[nt++] = element;
            }

            for (int i = nextFalse; i < position2id.length; i++) {
                int element = position2id[i];
                i2p[element] = --nf;
                p2i[nf] = element;
            }

            id2position = i2p;
            position2id = p2i;
            nextTrue = nt;
            nextFalse = nf;
        }

        private class IndexResult implements GraphIndexResult {

            private int count;
            private int position;

            public IndexResult(int count, int position) {
                this.count = count;
                this.position = position;
            }

            @Override
            public int getCount() {
                return count;
            }

            @Override
            public int getNextElement() {
                if (count-- == 0) {
                    return Graph.NOT_FOUND;
                }
                return position2id[position++];
            }

        }
    }

}
