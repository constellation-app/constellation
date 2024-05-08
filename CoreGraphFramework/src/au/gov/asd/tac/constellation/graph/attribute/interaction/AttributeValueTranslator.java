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
package au.gov.asd.tac.constellation.graph.attribute.interaction;

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/**
 * A Function interface for translating an attribute value of one type to a
 * value of another type. No type handling is done here - the contexts which
 * create and use AttributeValueTranslators should do type checking.
 * <p>
 * Note that {@link #translate translate(val)} should always return {@code null}
 * when {@code val == null}.
 *
 * @see AbstractAttributeInteraction#toEditTranslator
 * @see AbstractAttributeInteraction#fromEditTranslator
 * @author twilight_sparkle
 */
@FunctionalInterface
public interface AttributeValueTranslator {

    static final Logger LOGGER = Logger.getLogger(AttributeValueTranslator.class.getName());

    public static AttributeValueTranslator IDENTITY = val -> val;

    public static AttributeValueTranslator getNativeTranslator(final String attrType) {
        try {
            final AttributeDescription description = AttributeRegistry.getDefault().getAttributes().get(attrType)
                    .getDeclaredConstructor().newInstance();
            return description::convertToNativeValue;
        } catch (final IllegalAccessException | IllegalArgumentException | InstantiationException 
                | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            LOGGER.warning("Could not create attribute description corresponding to the given type");
            return IDENTITY;
        }
    }

    public Object translate(final Object val);
}
