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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute;

import au.gov.asd.tac.constellation.graph.attribute.AbstractObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are
 * {@link ConstellationColor} objects.
 * <p>
 * Attribute values can be set either directly using
 * {@link #setObject setObject()} or using {@link #setString setString()} which
 * will utilise {@link ConstellationColor#getColorValue(String)}.
 * <p>
 * Attribute values can be retrieved either directly using
 * {@link #getObject getObject()} or using {@link #getString getString()} which
 * will utilise {@link String#valueOf String.valueOf()}.
 * <p>
 * Note that 'null' is considered a legitimate attribute value for attributes of
 * this type.
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeDescription.class)
public final class ColorAttributeDescription extends AbstractObjectAttributeDescription<ConstellationColor> {

    public static final String ATTRIBUTE_NAME = "color";
    public static final Class<ConstellationColor> NATIVE_CLASS = ConstellationColor.class;
    public static final ConstellationColor DEFAULT_VALUE = null;

    public ColorAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    private ConstellationColor fromInt(final Integer integer) {
        final float red = (integer >>> 24) / 255.0F;
        final float green = ((integer >>> 16) & 0xFF) / 255.0F;
        final float blue = ((integer >>> 8) & 0xFF) / 255.0F;
        final float alpha = (integer & 0xFF) / 255.0F;
        return ConstellationColor.getColorValue(red, green, blue, alpha);
    }

    @Override
    @SuppressWarnings("unchecked") //Casts are checked manually
    public ConstellationColor convertFromObject(final Object object) {
        try {
            return super.convertFromObject(object);
        } catch (final IllegalArgumentException ex) {
            if (object instanceof Integer) {
                return fromInt((int) object);
            } else {
                throw ex;
            }
        }
    }

    @Override
    protected ConstellationColor convertFromString(final String string) {
        return StringUtils.isBlank(string) ? getDefault() : ConstellationColor.getColorValue(string);
    }

    @Override
    public void setDefault(final Object value) {
        defaultValue = value instanceof String ? ConstellationColor.getColorValue((String) value) : (ConstellationColor) value;
    }

    @Override
    public int getInt(final int id) {
        final ConstellationColor color = (ConstellationColor) data[id];
        return color != null ? ((int) (color.getRed() * 255) << 24)
                | ((int) (color.getGreen() * 255) << 16)
                | ((int) (color.getBlue() * 255) << 8)
                | (int) (color.getAlpha() * 255) 
                : 0;
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = fromInt(value);
    }
}
