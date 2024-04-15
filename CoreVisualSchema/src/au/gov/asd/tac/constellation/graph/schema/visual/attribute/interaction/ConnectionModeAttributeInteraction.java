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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.interaction;

import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ConnectionModeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class ConnectionModeAttributeInteraction extends AbstractAttributeInteraction<ConnectionMode> {

    @Override
    public String getDataType() {
        return ConnectionModeAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    @SuppressWarnings("unchecked") // value will be Enum of ConnectionMode
    public String getDisplayText(final Object value) {
        return value == null ? null : ((Enum<ConnectionMode>) value).name();
    }

    @Override
    protected Class<ConnectionMode> getValueType() {
        return ConnectionMode.class;
    }
}
