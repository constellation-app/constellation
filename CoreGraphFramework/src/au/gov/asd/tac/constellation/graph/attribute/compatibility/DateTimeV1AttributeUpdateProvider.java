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
package au.gov.asd.tac.constellation.graph.attribute.compatibility;

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.versioning.AttributeUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import java.time.Instant;
import java.time.ZonedDateTime;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@SuppressWarnings("deprecation")
@ServiceProvider(service = UpdateProvider.class)
public class DateTimeV1AttributeUpdateProvider extends AttributeUpdateProvider {

    private static final AttributeDescription FROM_ATTRIBUTE;
    private static final AttributeDescription TO_ATTRIBUTE;

    static {
        try {
            FROM_ATTRIBUTE = DateTimeAttributeDescriptionV0.class.newInstance();
            TO_ATTRIBUTE = ZonedDateTimeAttributeDescription.class.newInstance();
        } catch (final IllegalAccessException | InstantiationException ex) {
            throw new IllegalArgumentException(String.format("Version provider %s unable to access required attribute descriptions %s or %s", DateTimeV1AttributeUpdateProvider.class.getName(), DateTimeAttributeDescriptionV0.class.getName(), DateTimeAttributeDescriptionV0.class.getName()));
        }
    }

    @Override
    public AttributeDescription getAttributeDescription() {
        return FROM_ATTRIBUTE;
    }

    @Override
    public AttributeDescription getUpdatedAttributeDescription() {
        return TO_ATTRIBUTE;
    }

    @Override
    public Object updateAttributeValue(final Object value) {
        DateTimeAttributeDescriptionV0.Representation v = ((DateTimeAttributeDescriptionV0.Representation) value);
        return v == null ? null : ZonedDateTime.ofInstant(Instant.ofEpochMilli(v.getTime()), v.getTimeZone().toZoneId());
    }
}
