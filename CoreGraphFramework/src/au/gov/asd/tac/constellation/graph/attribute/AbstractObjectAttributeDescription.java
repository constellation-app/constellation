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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.Arrays;

/**
 *
 * @param <T> the type of object stored by this description.
 * @author cygnus_x-1
 */
public abstract class AbstractObjectAttributeDescription<T extends Object> extends AbstractAttributeDescription {

    protected final String name;
    protected final Class<T> nativeClass;
    protected T defaultValue;
    protected Object[] data = new Object[0];

    public AbstractObjectAttributeDescription(final String name, final Class<T> nativeClass, final T defaultValue) {
        this.name = name;
        this.nativeClass = nativeClass;
        this.defaultValue = defaultValue;
    }

    protected T convertFromObject(final Object object) {
        if (object == null) {
            return null;
        } else if (nativeClass.isAssignableFrom(object.getClass())) {
            return (T) object;
        } else if (object instanceof String) {
            return convertFromString((String) object);
        } else {
            throw new IllegalArgumentException(String.format(
                    "Error converting Object '%s' to %s", object.getClass(), nativeClass));
        }
    }

    protected T convertFromString(final String string) {
        throw new IllegalArgumentException(String.format("Error converting String to %s", nativeClass));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<T> getNativeClass() {
        return nativeClass;
    }

    @Override
    public T getDefault() {
        return defaultValue;
    }

    @Override
    public void setDefault(final Object value) {
        defaultValue = convertFromObject(value);
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
    public String getString(final int id) {
        return data[id] != null ? String.valueOf((T) data[id]) : null;
    }

    @Override
    public void setString(final int id, final String value) throws IllegalArgumentException {
        data[id] = convertFromString(value);
    }

    @Override
    public T getObject(final int id) {
        return (T) data[id];
    }

    @Override
    public void setObject(final int id, final Object value) throws IllegalArgumentException {
        data[id] = convertFromObject(value);
    }

    @Override
    public boolean isClear(final int id) {
        return equals(data[id], defaultValue);
    }

    @Override
    public void clear(final int id) {
        data[id] = defaultValue;
    }

    @Override
    public AttributeDescription copy(final GraphReadMethods graph) {
        final AbstractObjectAttributeDescription<T> attribute;
        try {
            attribute = this.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Unable to create instance of " + this.getClass().getCanonicalName(), ex);
        }
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.graph = graph;

        return attribute;
    }

    @Override
    public int hashCode(final int id) {
        return data[id] == null ? 0 : data[id].hashCode();
    }

    @Override
    public boolean equals(final int id1, final int id2) {
        return data[id1] == null ? data[id2] == null : data[id1].equals(data[id2]);
    }

    @Override
    public Object saveData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public void restoreData(final Object savedData) {
        final Object[] arrayData = (Object[]) savedData;
        data = Arrays.copyOf(arrayData, arrayData.length);
    }
}
