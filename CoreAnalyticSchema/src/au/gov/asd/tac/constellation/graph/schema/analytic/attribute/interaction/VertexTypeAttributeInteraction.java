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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.interaction;

import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.VertexTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class VertexTypeAttributeInteraction extends AbstractAttributeInteraction<SchemaVertexType> {

    @Override
    public String getDataType() {
        return VertexTypeAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    @SuppressWarnings("unchecked") // cast is manually checked.
    public String getDisplayText(final Object value) {
        if (value instanceof SchemaVertexType type) {
            return type.getName();
        }
        return value == null ? null : value.toString();
    }

    @Override
    protected Class<SchemaVertexType> getValueType() {
        return SchemaVertexType.class;
    }
}
