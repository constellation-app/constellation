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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility;

import au.gov.asd.tac.constellation.graph.attribute.AbstractObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import org.openide.util.lookup.ServiceProvider;

/**
 * Transaction Graph Label Attribute Description.
 * <p>
 * Note that this attribute description should no longer be used and only
 * remains to support legacy graph files.
 *
 * @author twilight_sparkle
 */
@Deprecated
@ServiceProvider(service = AttributeDescription.class)
public final class TransactionGraphLabelsAttributeDescriptionV0 extends AbstractObjectAttributeDescription<ElementGraphLabelsV0> {

    public static final String ATTRIBUTE_NAME = "graph_labels_transactions";
    public static final Class<ElementGraphLabelsV0> NATIVE_CLASS = ElementGraphLabelsV0.class;
    private static final ElementGraphLabelsV0 DEFAULT_VALUE = ElementGraphLabelsV0.NO_LABELS;

    public TransactionGraphLabelsAttributeDescriptionV0() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    /**
     * Extract a GraphLabels from an Object.
     *
     * @param object An Object.
     *
     * @return A GraphLabels.
     */
    @Override
    @SuppressWarnings("unchecked") // Casts are checked manually
    protected ElementGraphLabelsV0 convertFromObject(final Object object) {
        if (object == null) {
            return DEFAULT_VALUE;
        } else if (object instanceof ElementGraphLabelsV0) {
            return (ElementGraphLabelsV0) object;
        } else if (object instanceof String) {
            return convertFromString((String) object);
        } else {
            final String msg = String.format("Error converting Object '%s' to GraphLabels", object.getClass());
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    protected ElementGraphLabelsV0 convertFromString(final String string) {
        return ElementGraphLabelsV0.valueOf(string);
    }
}
