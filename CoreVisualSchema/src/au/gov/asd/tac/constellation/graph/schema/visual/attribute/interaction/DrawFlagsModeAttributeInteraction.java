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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.interaction;

import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DrawFlagsAttributeDescription;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class DrawFlagsModeAttributeInteraction extends AbstractAttributeInteraction<DrawFlags> {

    @Override
    public String getDataType() {
        return DrawFlagsAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public String getDisplayText(final Object value) {
        if (value == null) {
            return null;
        }
        final DrawFlags drawFlags = (DrawFlags) value;
        return String.format("nodes: %s; connections: %s; node_labels: %s; connection_labels: %s; blazes: %s",
                drawFlags.drawNodes(), drawFlags.drawConnections(), drawFlags.drawNodeLabels(), drawFlags.drawConnectionLabels(), drawFlags.drawBlazes());
    }

    @Override
    protected Class<DrawFlags> getValueType() {
        return DrawFlags.class;
    }
}
