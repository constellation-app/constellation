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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 * collection of all attributes can be accessed from this class
 *
 * @author sirius
 */
public final class AttributeRegistry implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(AttributeRegistry.class.getName());
    private final SortedMap<String, Class<? extends AttributeDescription>> attributes = new TreeMap<>();
    private final SortedMap<String, Class<? extends AttributeDescription>> uAttributes = Collections.unmodifiableSortedMap(attributes);
    private static final AttributeRegistry DEFAULT_ATTRIBUTE_REGISTRY;

    static {
        DEFAULT_ATTRIBUTE_REGISTRY = new AttributeRegistry();

        // Use Lookup to find all of the AttributeDescriptions, wherever they are.
        Lookup.Result<AttributeDescription> attrDescrs = Lookup.getDefault().lookupResult(AttributeDescription.class);
        attrDescrs.allClasses().forEach(ad -> {
            DEFAULT_ATTRIBUTE_REGISTRY.registerAttributeIfNewer(ad);
            LOGGER.fine(String.format("Registered Attribute %s", ad.getName()));
        });
    }

    public static AttributeRegistry copyWithRegsitrations(final AttributeRegistry other,
            final Collection<Class<? extends AttributeDescription>> registrations) {
        final AttributeRegistry copy = new AttributeRegistry();
        other.attributes.values().forEach(ad -> copy.registerAttribute(ad));
        registrations.forEach(ad -> copy.registerAttribute(ad));
        return copy;
    }

    public static AttributeRegistry getDefault() {
        return DEFAULT_ATTRIBUTE_REGISTRY;
    }

    private AttributeRegistry() {
    }

    private void registerAttribute(final Class<? extends AttributeDescription> attributeDescription) {
        registerAttribute(attributeDescription, false);
    }

    private void registerAttributeIfNewer(final Class<? extends AttributeDescription> attributeDescription) {
        registerAttribute(attributeDescription, true);
    }

    private void registerAttribute(final Class<? extends AttributeDescription> attributeDescription, final boolean onlyRegisterIfNewer) {
        try {
            AttributeDescription attribute = attributeDescription.getDeclaredConstructor().newInstance();
            final String attrName = attribute.getName();
            if (!onlyRegisterIfNewer || !attributes.containsKey(attrName)
                    || attributes.get(attrName).getDeclaredConstructor().newInstance().getVersion() < attribute.getVersion()) {
                attributes.put(attrName, attributeDescription);
            }
        } catch (final IllegalAccessException | IllegalArgumentException
                | InstantiationException | NoSuchMethodException
                | SecurityException | InvocationTargetException ex) {
            throw new IllegalArgumentException("Error registering attribute description: "
                    + attributeDescription.getCanonicalName(), ex);
        }
    }

    public Class<?> getNativeType(final Class<? extends AttributeDescription> attributeDescription) {
        try {
            final AttributeDescription attribute = attributeDescription.getDeclaredConstructor().newInstance();
            return attribute.getNativeClass();
        } catch (final IllegalAccessException | IllegalArgumentException
                | InstantiationException | NoSuchMethodException
                | SecurityException | InvocationTargetException ex) {
            throw new IllegalArgumentException("Error getting type for attribute description: "
                    + attributeDescription.getCanonicalName(), ex);
        }
    }

    public SortedMap<String, Class<? extends AttributeDescription>> getAttributes() {
        return uAttributes;
    }
}
