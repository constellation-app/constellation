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
 * A Short Object attribute that can be null.
 * 
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public class ShortObjectAttributeDescription extends AbstractObjectAttributeDescription<Short> {
    
    public static final String ATTRIBUTE_NAME = "short_or_null";
    public static final Class<Short> NATIVE_CLASS = Short.class;
    public static final Short DEFAULT_VALUE = null;

    public ShortObjectAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }
    
    @Override
    @SuppressWarnings("unchecked") //Casts are manually checked
    protected Short convertFromObject(final Object object) {
        try {
            return super.convertFromObject(object);
        } catch (final IllegalArgumentException ex) {
            if (object instanceof Number) {
                return ((Number) object).shortValue();
            } else if (object instanceof Boolean) {
                return ((Boolean) object) ? (short) 1 : (short) 0;
            } else {
                throw ex;
            }
        }
    }

    @Override
    protected Short convertFromString(final String string) {
        if (StringUtils.isBlank(string)) {
            return getDefault();
        } else {
            return Short.parseShort(string);
        }
    }
    
    @Override
    public byte getByte(final int id) {
        return data[id] != null ? ((Short) data[id]).byteValue() : (byte) 0;
    }

    @Override
    public void setByte(final int id, final byte value) {
        data[id] = (short) value;
    }
    
    
    @Override
    public short getShort(final int id) {
        return data[id] != null ? (Short) data[id] : (short) 0;
    }

    @Override
    public void setShort(final int id, final short value) {
        data[id] = value;
    }

    @Override
    public int getInt(final int id) {
        return data[id] != null ? ((Short) data[id]).intValue() : 0;
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = (short) value;
    }
    
    @Override
    public long getLong(final int id) {
        return data[id] != null ? ((Short) data[id]).longValue() : 0L;
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] =(short) value;
    }

    @Override
    public float getFloat(final int id) {
        return data[id] != null ? ((Short) data[id]).floatValue() : 0.0f;
    }

    @Override
    public void setFloat(final int id, final float value) {
        data[id] = (short) value;
    }

    @Override
    public double getDouble(final int id) {
        return data[id] != null ? ((Short) data[id]).doubleValue() : 0.0;
    }

    @Override
    public void setDouble(final int id, final double value) {
        data[id] = (short) value;
    }

    @Override
    public boolean getBoolean(final int id) {
        return data[id] != null && !data[id].equals((short) 0);
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        data[id] = value ? (short) 1 : (short) 0;
    }
    
    @Override
    public int hashCode(final int id) {
        return data[id] == null ? nullHash : ((Short) data[id]).intValue();
    }
}
