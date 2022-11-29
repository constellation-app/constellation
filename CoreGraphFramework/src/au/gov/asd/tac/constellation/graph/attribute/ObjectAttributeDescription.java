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

import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are arbitrary objects. This
 * acts as the base class for the descriptions of many complex attribute types.
 * <p>
 * Attribute values may be set in any matter or retrieved in any manner, where
 * in the later case they are explicitly cast (without checking) to the required
 * type. Note that in many cases this is not desired behaviour and so
 * descriptions which extend this class will need to explicitly override these
 * methods to throw an {@link IllegalArgumentException}. This may be changed in
 * the future.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public class ObjectAttributeDescription extends AbstractObjectAttributeDescription<Object> {

    public static final String ATTRIBUTE_NAME = "object";
    public static final Class<Object> NATIVE_CLASS = Object.class;
    public static final Object DEFAULT_VALUE = null;

    public ObjectAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    public ObjectAttributeDescription(final String name) {
        super(name, Object.class, DEFAULT_VALUE);
    }
}
