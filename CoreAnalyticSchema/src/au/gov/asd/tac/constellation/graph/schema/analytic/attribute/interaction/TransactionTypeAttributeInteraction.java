/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.TransactionTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class TransactionTypeAttributeInteraction extends AbstractAttributeInteraction<SchemaTransactionType> {

    @Override
    public String getDataType() {
        return TransactionTypeAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    @SuppressWarnings("unchecked") // cast is manually checked.
    public String getDisplayText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof SchemaTransactionType) {
            return ((SchemaTransactionType) value).getName();
        }
        return value.toString();
    }

    @Override
    protected Class<SchemaTransactionType> getValueType() {
        return SchemaTransactionType.class;
    }
}
