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
package au.gov.asd.tac.constellation.graph.attribute.compatibility;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.AbstractAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.ObjectReadable;
import au.gov.asd.tac.constellation.graph.value.variables.ObjectVariable;
import java.util.Arrays;
import java.util.TimeZone;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are {@link TimeZone} objects.
 * <p>
 * Attribute values can be set either directly using
 * {@link #setObject setObject()} or using {@link #setString setString()} which
 * will utilise
 * {@link TimeZone#getTimeZone(String) TimeZone.getTimeZone(String)}.
 * <p>
 * Attribute values can be retrieved either directly using
 * {@link #getObject getObject()} or using {@link #getString getString()} which
 * will utilise {@link TimeZone#getID TimeZone.getID()}.
 * <p>
 * As this was originally intended for use ONLY as a graph-wide timezone, a
 * single instantiation has been enforced in setCapacity(). This should be
 * removed in future versions.
 * <p>
 * Note that this attribute description should no longer be used and only
 * remains to support legacy graph files.
 *
 * @author algol
 */
@Deprecated
@ServiceProvider(service = AttributeDescription.class)
public final class TimeZoneAttributeDescriptionV0 extends AbstractAttributeDescription {

    private static final TimeZone DEFAULT_VALUE = TimeZone.getTimeZone("UTC");
    private TimeZone[] data = new TimeZone[0];
    private TimeZone defaultValue = DEFAULT_VALUE;
    public static final String ATTRIBUTE_NAME = "time_zone";

    @Override
    public String getName() {
        return ATTRIBUTE_NAME;
    }

    @Override
    public int getCapacity() {
        return data.length;
    }

    @Override
    public void setCapacity(final int capacity) {
        if (capacity != 1) {
            throw new IllegalArgumentException("Only one time zone is necessary.");
        }

        final int len = data.length;
        data = Arrays.copyOf(data, capacity);
        if (capacity > len) {
            Arrays.fill(data, len, capacity, defaultValue);
        }
    }

    @Override
    public void clear(final int id) {
        data[id] = defaultValue;
    }

    /**
     * Extract a TimeZone from an Object.
     *
     * @param value An Object.
     *
     * @return A TimeZone.
     */
    private static TimeZone setObject(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof TimeZone) {
            return (TimeZone) value;
        } else if (value instanceof String) {
            return TimeZone.getTimeZone((String) value);
        } else {
            final String msg = String.format("Error converting Object '%s' to time_zone", value.getClass());
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = setObject(value);
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = TimeZone.getTimeZone(value);
    }

    @Override
    public Object getObject(final int id) {
        return data[id];
    }

    @Override
    public String getString(final int id) {
        return data[id] != null ? data[id].getID() : null;
    }

    @Override
    public AttributeDescription copy(GraphReadMethods graph) {
        final TimeZoneAttributeDescriptionV0 attribute = new TimeZoneAttributeDescriptionV0();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
    }

    @Override
    public Class<?> getNativeClass() {
        return TimeZoneAttributeDescriptionV0.class;
    }

    @Override
    public void setDefault(final Object value) {
        if (value == null) {
            defaultValue = DEFAULT_VALUE;
        }
        if (value instanceof String) {
            defaultValue = TimeZone.getTimeZone((String) value);
        } else if (value instanceof TimeZone) {
            defaultValue = (TimeZone) value;
        } else {
            defaultValue = DEFAULT_VALUE;
        }
    }

    @Override
    public Object getDefault() {
        return defaultValue.getID();
    }

    @Override
    public int hashCode(final int id) {
        return data[id] == null ? 0 : data[id].hashCode();
    }

    @Override
    public boolean equals(final int id1, final int id2) {
        return data[id1].equals(data[id2]);
    }

    @Override
    public boolean isClear(final int id) {
        return equals(data[id], defaultValue);
    }

    @Override
    public Object saveData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public void restoreData(final Object savedData) {
        final TimeZone[] sd = (TimeZone[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
    }

    @Override
    public Object createReadObject(IntReadable indexReadable) {
        return (ObjectReadable) () -> data[indexReadable.readInt()];
    }

    @Override
    public Object createWriteObject(GraphWriteMethods graph, int attribute, IntReadable indexReadable) {
        return new ObjectVariable() {
            @Override
            public Object readObject() {
                return data[indexReadable.readInt()];
            }

            @Override
            public void writeObject(Object value) {
                graph.setObjectValue(attribute, indexReadable.readInt(), value);
            }
        };
    }
}
