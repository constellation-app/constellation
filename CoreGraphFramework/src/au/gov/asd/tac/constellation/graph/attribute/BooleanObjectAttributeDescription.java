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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphIndex;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * A Boolean Object attribute that can be null.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public final class BooleanObjectAttributeDescription extends AbstractObjectAttributeDescription<Boolean> {

    private final int trueHash = random.nextInt();
    private final int falseHash = random.nextInt();

    public static final String ATTRIBUTE_NAME = "boolean_or_null";
    public static final Class<Boolean> NATIVE_CLASS = Boolean.class;
    public static final Boolean DEFAULT_VALUE = null;

    public BooleanObjectAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    @SuppressWarnings("unchecked") // Casts are manually checked
    protected Boolean convertFromObject(final Object object) {
        try {
            return super.convertFromObject(object);
        } catch (final IllegalArgumentException ex) {
            if (object instanceof Number) {
                return ((Number) object).intValue() != 0;
            } else if (object instanceof Character) {
                return ((char) object) != 0;
            } else {
                throw ex;
            }
        }
    }

    @Override
    protected Boolean convertFromString(final String string) {
        if (StringUtils.isBlank(string)) {
            return getDefault();
        } else {
            return Boolean.parseBoolean(string);
        }
    }

    @Override
    public byte getByte(final int id) {
        return (data[id] != null && (Boolean) data[id]) ? (byte) 1 : (byte) 0;
    }

    @Override
    public void setByte(final int id, final byte value) {
        data[id] = value != (byte) 0;
    }

    @Override
    public short getShort(final int id) {
        return (data[id] != null && (Boolean) data[id]) ? (short) 1 : (short) 0;
    }

    @Override
    public void setShort(final int id, final short value) {
        data[id] = value != (short) 0;
    }

    @Override
    public int getInt(final int id) {
        return (data[id] != null && (Boolean) data[id]) ? 1 : 0;
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = value != 0;
    }

    @Override
    public long getLong(final int id) {
        return (data[id] != null && (Boolean) data[id]) ? 1L : 0L;
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = value != 0L;
    }

    @Override
    public float getFloat(final int id) {
        return (data[id] != null && (Boolean) data[id]) ? 1.0F : 0.0F;
    }

    @Override
    public void setFloat(final int id, final float value) {
        data[id] = value != 0.0F;
    }

    @Override
    public double getDouble(final int id) {
        return (data[id] != null && (Boolean) data[id]) ? 1.0 : 0.0;
    }

    @Override
    public void setDouble(final int id, final double value) {
        data[id] = value != 0.0;
    }

    @Override
    public boolean getBoolean(final int id) {
        return data[id] != null && (Boolean) data[id];
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        data[id] = value;
    }

    @Override
    public char getChar(final int id) {
        return (data[id] != null && (Boolean) data[id]) ? (char) 1 : (char) 0;
    }

    @Override
    public void setChar(final int id, final char value) {
        data[id] = value != (char) 0;
    }

    @Override
    public int hashCode(final int id) {
        return data[id] == null ? nullHash
                : (Boolean) data[id] ? trueHash : falseHash;
    }

    @Override
    public boolean supportsIndexType(final GraphIndexType indexType) {
        return indexType != GraphIndexType.ORDERED;
    }

    @Override
    public GraphIndex createIndex(final GraphIndexType indexType) {
        return indexType == GraphIndexType.ORDERED ? NULL_GRAPH_INDEX : new Index();
    }

    private class Index implements GraphIndex {

        private int[] id2position = new int[data.length];
        private int[] position2id = new int[data.length];
        int nextTrue = 0;
        int nextFalse = data.length;

        @Override
        public void addElement(final int element) {
            if (data[element] != null && (Boolean) data[element]) {
                id2position[element] = nextTrue;
                position2id[nextTrue++] = element;
            } else {
                id2position[element] = --nextFalse;
                position2id[nextFalse] = element;
            }
        }

        @Override
        public void removeElement(final int element) {
            int position = id2position[element];
            if (position < nextTrue) {
                int lastTrue = position2id[--nextTrue];
                position2id[position] = lastTrue;
                id2position[lastTrue] = position;
            } else {
                int firstFalse = position2id[nextFalse++];
                position2id[position] = firstFalse;
                id2position[firstFalse] = position;
            }
        }

        @Override
        public void updateElement(final int element) {
            removeElement(element);
            addElement(element);
        }

        @Override
        public GraphIndexResult getElementsWithAttributeValue(final Object value) {
            if ((Boolean) value) {
                return new IndexResult(nextTrue, 0);
            } else {
                return new IndexResult(data.length - nextFalse, nextFalse);
            }
        }

        @Override
        public GraphIndexResult getElementsWithAttributeValueRange(final Object start, final Object end) {
            return null;
        }

        @Override
        public void expandCapacity(final int newCapacity) {

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

            public IndexResult(final int count, final int position) {
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
