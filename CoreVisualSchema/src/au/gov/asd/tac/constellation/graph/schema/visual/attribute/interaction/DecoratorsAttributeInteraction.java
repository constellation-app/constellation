/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DecoratorsAttributeDescription;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class DecoratorsAttributeInteraction extends AbstractAttributeInteraction<VertexDecorators> {

    @Override
    public String getDataType() {
        return DecoratorsAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public String getDisplayText(final Object value) {
        if (value == null) {
            return null;
        }

        final VertexDecorators decoratorsVal = ((VertexDecorators) value);

        final StringBuilder decoratorsString = new StringBuilder();
        if (decoratorsVal.getNorthWestDecoratorAttribute() != null) {
            decoratorsString.append(String.format("NW: %s, ", decoratorsVal.getNorthWestDecoratorAttribute()));
        }
        if (decoratorsVal.getNorthEastDecoratorAttribute() != null) {
            decoratorsString.append(String.format("NE: %s, ", decoratorsVal.getNorthEastDecoratorAttribute()));
        }
        if (decoratorsVal.getSouthEastDecoratorAttribute() != null) {
            decoratorsString.append(String.format("SE: %s, ", decoratorsVal.getSouthEastDecoratorAttribute()));
        }
        if (decoratorsVal.getSouthWestDecoratorAttribute() != null) {
            decoratorsString.append(String.format("SW: %s, ", decoratorsVal.getSouthWestDecoratorAttribute()));
        }

        return decoratorsString.length() > 0 ? decoratorsString.substring(0, decoratorsString.length() - 2) : decoratorsString.toString();
    }

    @Override
    protected Class<VertexDecorators> getValueType() {
        return VertexDecorators.class;
    }
}
