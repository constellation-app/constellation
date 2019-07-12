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
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * Time zone type.
 *
 * @author algol
 */
@ServiceProvider(service = AttributeDescription.class)
public final class TimeZoneAttributeDescription extends AbstractAttributeDescription {

    private static final int DESCRIPTION_VERSION = 1;
    private static final Logger LOGGER = Logger.getLogger(ZonedDateTimeAttributeDescription.class.getName());
    private static final ZoneId DEFAULT_VALUE = TimeZoneUtilities.UTC;
    private ZoneId[] data = new ZoneId[0];
    private ZoneId defaultValue = DEFAULT_VALUE;
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
    private static ZoneId parseObject(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof ZoneId) {
            return (ZoneId) value;
        } else if (value instanceof TimeZone) {
            return ((TimeZone) value).toZoneId();
        } else if (value instanceof String) {
            return ZoneId.of((String) value);
        } else {
            LOGGER.log(Level.WARNING, "Error converting Object '{0}' to time_zone", value.getClass());
        }
        return null;
    }

    @Override
    public void setObject(final int id, final Object value) {
        data[id] = parseObject(value);
    }

    @Override
    public void setString(final int id, final String value) {
        data[id] = parseString(value);
    }

    public static ZoneId parseString(final String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            final String offsetId = value.substring(0, 6);
            final String regionId = value.length() > 6 ? value.substring(8, value.length() - 1) : null;
            return regionId == null ? ZoneOffset.of(offsetId) : ZoneId.of(regionId);
        } catch (StringIndexOutOfBoundsException | NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "Can't parse timezone string '{0}': '{1}'", new Object[]{value, ex.getMessage()});
        }

        return null;
    }

    @Override
    public Object getObject(final int id) {
        return data[id];
    }

    @Override
    public String getString(final int id) {
        return data[id] == null ? null : TimeZoneUtilities.getTimeZoneAsString(data[id]);
    }

    @Override
    public AttributeDescription copy(GraphReadMethods graph) {
        final TimeZoneAttributeDescription attribute = new TimeZoneAttributeDescription();
        attribute.data = Arrays.copyOf(data, data.length);
        attribute.defaultValue = this.defaultValue;
        attribute.graph = graph;
        return attribute;
    }

    @Override
    public Class<?> getNativeClass() {
        return TimeZoneAttributeDescription.class;
    }

    @Override
    public void setDefault(final Object value) {
        final ZoneId parsedValue = parseObject(value);
        defaultValue = parsedValue != null ? parsedValue : DEFAULT_VALUE;
    }

    @Override
    public Object getDefault() {
        return defaultValue;
    }

    @Override
    public int hashCode(final int id) {
        return data[id] == null ? 0 : data[id].hashCode();
    }

    @Override
    public boolean equals(final int id1, final int id2) {
        return (data[id1] != null && data[id1].equals(data[id2])) || data[id1] == data[id2];
    }

    @Override
    public boolean canBeImported() {
        return false;
    }

    @Override
    public int ordering() {
        return 9;
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
        final ZoneId[] sd = (ZoneId[]) savedData;
        data = Arrays.copyOf(sd, sd.length);
    }

    @Override
    public int getVersion() {
        return DESCRIPTION_VERSION;
    }
}
