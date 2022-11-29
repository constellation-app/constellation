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

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.TransactionGraphLabelsAttributeDescription;
import au.gov.asd.tac.constellation.graph.versioning.AttributeUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@SuppressWarnings("deprecation")
@ServiceProvider(service = UpdateProvider.class)
public class TransactionGraphLabelsV1AttributeUpdateProvider extends AttributeUpdateProvider {

    private static final AttributeDescription FROM_ATTRIBUTE;
    private static final AttributeDescription TO_ATTRIBUTE;

    static {
        try {
            FROM_ATTRIBUTE = TransactionGraphLabelsAttributeDescriptionV0.class.newInstance();
            TO_ATTRIBUTE = TransactionGraphLabelsAttributeDescription.class.newInstance();
        } catch (final IllegalAccessException | InstantiationException ex) {
            throw new IllegalArgumentException(String.format("Version provider %s unable to access required attribute descriptions %s or %s", TransactionGraphLabelsV1AttributeUpdateProvider.class.getName(), TransactionGraphLabelsAttributeDescriptionV0.class.getName(), TransactionGraphLabelsAttributeDescription.class.getName()));
        }
    }

    @Override
    public AttributeDescription getAttributeDescription() {
        return FROM_ATTRIBUTE;
    }

    @Override
    public AttributeDescription getUpdatedAttributeDescription() {
        return TO_ATTRIBUTE;
    }

    @Override
    public Object updateAttributeValue(final Object value) {
        if (value == null) {
            return null;
        }

        final ElementGraphLabelsV0 v0 = (ElementGraphLabelsV0) value;
        final List<ElementGraphLabelV0> labelsV0 = v0.getLabels();
        final List<GraphLabel> labels = new ArrayList<>();
        for (final ElementGraphLabelV0 labelV0 : labelsV0) {
            final GraphLabel label = new GraphLabel(labelV0.getAttributeName(), labelV0.getColor(), labelV0.getSize());
            labels.add(label);
        }

        return new GraphLabels(labels);
    }
}
