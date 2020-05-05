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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute;

import au.gov.asd.tac.constellation.graph.attribute.AbstractObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are
 * {@link SchemaTransactionType} objects.
 * <p>
 * Attribute values can be set either directly using
 * {@link #setObject setObject()} or using {@link #setString setString()} which
 * will try to resolve the type by name using the schema. See {@link #stringify}
 * for more information.
 * <p>
 * Attribute values can be retrieved either directly using
 * {@link #getObject getObject()} or using {@link #getString getString()} which
 * will utilise
 * {@link SchemaTransactionType#getName SchemaTransactionType.getName()}.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public class TransactionTypeAttributeDescription extends AbstractObjectAttributeDescription<SchemaTransactionType> {

    public static final String ATTRIBUTE_NAME = "transaction_type";
    public static final Class<SchemaTransactionType> NATIVE_CLASS = SchemaTransactionType.class;
    public static final SchemaTransactionType DEFAULT_VALUE = null;

    public TransactionTypeAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    protected SchemaTransactionType convertFromString(final String string) {
        if (StringUtils.isBlank(string)) {
            return getDefault();
        } else {
            return SchemaTransactionTypeUtilities.getTypeOrBuildNew(string);
        }
    }

    @Override
    public void setDefault(final Object value) {
        super.setDefault(value);

        // ensure that the default is a registered type.
        if (defaultValue instanceof SchemaTransactionType && defaultValue.isIncomplete()) {
            defaultValue = DEFAULT_VALUE;
        }
    }
}
