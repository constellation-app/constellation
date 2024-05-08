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

import au.gov.asd.tac.constellation.graph.attribute.TimeAttributeDescription;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import java.time.LocalTime;
import org.openide.util.lookup.ServiceProvider;

/**
 * AttributeInteraction for attributes described by
 * {@link au.gov.asd.tac.constellation.graph.attribute.TimeAttributeDescription}
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class TimeAttributeInteraction extends AbstractAttributeInteraction<LocalTime> {

    private static final int MILLISECONDS_START_INDEX = 8;
    private static final int MILLISECONDS_END_INDEX = 12;

    @Override
    public String getDataType() {
        return TimeAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public String getDisplayText(final Object value) {
        if (value == null) {
            return null;
        }

        String representation = ((LocalTime) value).format(TemporalFormatting.TIME_FORMATTER);

        // If the milliseconds component is 0, trim it from the string representation
        if (((LocalTime) value).getNano() / 1000000 == 0) {
            representation = representation.substring(0, MILLISECONDS_START_INDEX) + representation.substring(MILLISECONDS_END_INDEX);
        }

        return representation;
    }

    @Override
    protected Class<LocalTime> getValueType() {
        return LocalTime.class;
    }
}
