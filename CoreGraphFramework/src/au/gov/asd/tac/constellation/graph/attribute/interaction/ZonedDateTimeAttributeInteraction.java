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
package au.gov.asd.tac.constellation.graph.attribute.interaction;

import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import java.time.ZonedDateTime;
import org.openide.util.lookup.ServiceProvider;

/**
 * AttributeInteraction for attributes described by
 * {@link au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription}
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class ZonedDateTimeAttributeInteraction extends AbstractAttributeInteraction<ZonedDateTime> {

    @Override
    public String getDataType() {
        return ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public String getDisplayText(final Object value) {
        if (value == null) {
            return null;
        }

        String representation = ((ZonedDateTime) value).format(TemporalFormatting.ZONED_DATE_TIME_FORMATTER);

        // if the milliseconds component is 0, trim it from the string representation
        if (((ZonedDateTime) value).getNano() / 1000000 == 0) {
            representation = representation.substring(0, TemporalFormatting.DATE_HMS_FORMAT_LENGTH) + representation.substring(TemporalFormatting.DATE_TIME_FORMAT_LENGTH);
        }

        return representation;
    }

    @Override
    protected Class<ZonedDateTime> getValueType() {
        return ZonedDateTime.class;
    }
}
