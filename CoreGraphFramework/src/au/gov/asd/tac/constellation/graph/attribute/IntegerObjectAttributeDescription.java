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

import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * An Integer Object attribute that can be null.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public class IntegerObjectAttributeDescription extends AbstractObjectAttributeDescription<Integer> {

    public static final String ATTRIBUTE_NAME = "integer_or_null";
    public static final Class<Integer> NATIVE_CLASS = Integer.class;
    public static final Integer DEFAULT_VALUE = null;

    public IntegerObjectAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    @SuppressWarnings("unchecked") // Casts are manually checked
    protected Integer convertFromObject(final Object object) {
        try {
            return super.convertFromObject(object);
        } catch (final IllegalArgumentException ex) {
            if (object instanceof Number) {
                return ((Number) object).intValue();
            } else if (object instanceof Boolean) {
                return ((Boolean) object) ? 1 : 0;
            } else if (object instanceof Character) {
                return (int) object;
            } else {
                throw ex;
            }
        }
    }

    @Override
    protected Integer convertFromString(final String string) {
        if (StringUtils.isBlank(string)) {
            return getDefault();
        } else {
            return Integer.parseInt(string);
        }
    }

    @Override
    public byte getByte(final int id) {
        return data[id] != null ? ((Integer) data[id]).byteValue() : (byte) 0;
    }

    @Override
    public void setByte(final int id, final byte value) {
        data[id] = (int) value;
    }

    @Override
    public short getShort(final int id) {
        return data[id] != null ? ((Integer) data[id]).shortValue() : (short) 0;
    }

    @Override
    public void setShort(final int id, final short value) {
        data[id] = (int) value;
    }

    @Override
    public int getInt(final int id) {
        return data[id] != null ? (Integer) data[id] : 0;
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = value;
    }

    @Override
    public long getLong(final int id) {
        return data[id] != null ? ((Integer) data[id]).longValue() : 0L;
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = (int) value;
    }

    @Override
    public float getFloat(final int id) {
        return data[id] != null ? ((Integer) data[id]).floatValue() : 0.0f;
    }

    @Override
    public void setFloat(final int id, final float value) {
        data[id] = (int) value;
    }

    @Override
    public double getDouble(final int id) {
        return data[id] != null ? ((Integer) data[id]).doubleValue() : 0.0;
    }

    @Override
    public void setDouble(final int id, final double value) {
        data[id] = (int) value;
    }

    @Override
    public boolean getBoolean(final int id) {
        return data[id] != null && !data[id].equals(0);
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        data[id] = value ? 1 : 0;
    }

    @Override
    public char getChar(final int id) {
        return data[id] != null ? (char) (int) data[id] : (char) 0;
    }

    @Override
    public void setChar(final int id, final char value) {
        data[id] = (int) value;
    }

    @Override
    public int hashCode(final int id) {
        return data[id] == null ? nullHash : (Integer) data[id];
    }
}
