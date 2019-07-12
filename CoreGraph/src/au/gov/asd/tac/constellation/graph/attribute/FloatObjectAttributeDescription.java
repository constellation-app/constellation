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

import au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess;
import java.util.Arrays;
import org.openide.util.lookup.ServiceProvider;

/**
 * A Float Object attribute that can be null.
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeDescription.class)
public final class FloatObjectAttributeDescription extends AbstractObjectAttributeDescription<Float> {

    public static final String ATTRIBUTE_NAME = "float_or_null";
    public static final Class<Float> NATIVE_CLASS = Float.class;
    public static final Float DEFAULT_VALUE = null;
    private final int nullHash = 0x7f800001;

    public FloatObjectAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    @SuppressWarnings("unchecked") // Casts are manually checked
    protected Float convertFromObject(final Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).floatValue();
        } else if (object instanceof String) {
            return convertFromString((String) object);
        } else if (object instanceof Boolean) {
            return ((Boolean) object) ? 1.0f : 0.0f;
        } else if (object instanceof Character) {
            return (float) object;
        } else {
            throw new IllegalArgumentException(String.format("Error converting Object '%s' to Float", object.getClass()));
        }
    }

    @Override
    protected Float convertFromString(final String string) {
        if (string == null || string.isEmpty()) {
            return getDefault();
        } else {
            try {
                return Float.parseFloat(string);
            } catch (final NumberFormatException ex) {
                return getDefault();
            }
        }
    }

    @Override
    public byte getByte(final int id) {
        return data[id] == null ? 0 : ((Float) data[id]).byteValue();
    }

    @Override
    public void setByte(final int id, final byte value) {
        data[id] = (float) value;
    }

    @Override
    public short getShort(final int id) {
        return data[id] == null ? 0 : ((Float) data[id]).shortValue();
    }

    @Override
    public void setShort(final int id, final short value) {
        data[id] = (float) value;
    }

    @Override
    public int getInt(final int id) {
        return data[id] == null ? 0 : ((Float) data[id]).intValue();
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = (float) value;
    }

    @Override
    public long getLong(final int id) {
        return data[id] == null ? 0 : ((Float) data[id]).longValue();
    }

    @Override
    public void setLong(final int id, final long value) {
        data[id] = (float) value;
    }

    @Override
    public float getFloat(final int id) {
        return data[id] == null ? 0 : ((Float) data[id]);
    }

    @Override
    public void setFloat(final int id, final float value) {
        data[id] = value;
    }

    @Override
    public double getDouble(final int id) {
        return data[id] == null ? 0 : ((Float) data[id]).doubleValue();
    }

    @Override
    public void setDouble(final int id, final double value) {
        data[id] = (float) value;
    }

    @Override
    public boolean getBoolean(final int id) {
        return data[id] != null && !data[id].equals(0.0f);
    }

    @Override
    public void setBoolean(final int id, final boolean value) {
        data[id] = value ? 1.0f : 0.0f;
    }

    @Override
    public char getChar(final int id) {
        return data[id] == null ? 0 : (char) (float) data[id];
    }

    @Override
    public void setChar(final int id, final char value) {
        data[id] = (float) value;
    }

    @Override
    public String acceptsString(String value) {
        try {
            Float.parseFloat(value);
            return null;
        } catch (NumberFormatException ex) {
            return "Not a valid float value";
        }
    }

    @Override
    public int hashCode(final int id) {
        return data[id] == null ? nullHash : Float.floatToIntBits((Float) data[id]);
    }

    @Override
    public int ordering() {
        return 5;
    }

    @Override
    public void restore(final int id, final ParameterReadAccess access) {
        data[id] = (Float) access.getUndoObject();
    }

    @Override
    public Object saveData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public void restoreData(final Object savedData) {
        final Float[] sd = (Float[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
    }
}
