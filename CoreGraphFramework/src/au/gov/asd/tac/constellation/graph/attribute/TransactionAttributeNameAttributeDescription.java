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
package au.gov.asd.tac.constellation.graph.attribute;

import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are the names of other
 * attributes.
 * <p>
 * This description behaves exactly as StringAttributeDescription, because at
 * the storage level the two types of attributes are identical.
 * <p>
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeDescription.class)
public final class TransactionAttributeNameAttributeDescription extends AbstractObjectAttributeDescription<String> {

    public static final String ATTRIBUTE_NAME = "transaction_attribute_name";
    public static final Class<String> NATIVE_CLASS = String.class;
    private static final String DEFAULT_VALUE = null;

    public TransactionAttributeNameAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    protected String convertFromString(final String string) {
        return string;
    }
}
