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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.utilities.temporal.TemporalUtilities;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * Time zone type.
 *
 * @author algol
 */
@ServiceProvider(service = AttributeDescription.class)
public final class TimeZoneAttributeDescription extends AbstractObjectAttributeDescription<ZoneId> {

    public static final String ATTRIBUTE_NAME = "time_zone";
    public static final int ATTRIBUTE_VERSION = 1;
    public static final Class<ZoneId> NATIVE_CLASS = ZoneId.class;
    public static final ZoneId DEFAULT_VALUE = TemporalUtilities.UTC;

    public TimeZoneAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    public int getVersion() {
        return ATTRIBUTE_VERSION;
    }

    @Override
    protected ZoneId convertFromObject(final Object object) {
        try {
            return super.convertFromObject(object);
        } catch (final IllegalArgumentException ex) {
            if (object instanceof TimeZone timeZone) {
                return timeZone.toZoneId();
            } else {
                throw ex;
            }
        }
    }

    @Override
    protected ZoneId convertFromString(final String string) {
        if (StringUtils.isBlank(string)) {
            return getDefault();
        } else {
            try {
                final String timeString = string.length() < 6 ? "+ 00:00 UTC" : string;
                final String offsetId = timeString.substring(0, 6);
                final String regionId = timeString.length() > 6 ? timeString.substring(8, timeString.length() - 1) : null;
                return regionId == null ? ZoneOffset.of(offsetId) : ZoneId.of(regionId);
            } catch (final StringIndexOutOfBoundsException | NumberFormatException ex) {
                throw new IllegalArgumentException(String.format(
                        "Error converting String '%s' to time zone", string), ex);
            }
        }
    }

    @Override
    public String getString(final int id) {
        return data[id] == null ? null : TemporalUtilities.getTimeZoneAsString((ZoneId) data[id]);
    }

    @Override
    public int hashCode(final int id) {
        return data[id] == null ? 0 : data[id].hashCode();
    }
}
