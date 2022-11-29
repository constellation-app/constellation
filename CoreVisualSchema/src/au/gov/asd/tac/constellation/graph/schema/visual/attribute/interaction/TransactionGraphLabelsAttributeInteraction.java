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
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.TransactionGraphLabelsAttributeDescription;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class TransactionGraphLabelsAttributeInteraction extends AbstractAttributeInteraction<GraphLabels> {

    @Override
    public String getDataType() {
        return TransactionGraphLabelsAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public String getDisplayText(final Object value) {
        if (value == null) {
            return null;
        }

        final GraphLabels labelsValue = ((GraphLabels) value);
        final StringBuilder labelsString = new StringBuilder();
        labelsValue.getLabels().forEach(label -> {
            labelsString.append(label.getAttributeName());
            labelsString.append(": (");
            labelsString.append(label.getColor());
            labelsString.append("), ");
        });
        return labelsString.length() > 0 ? labelsString.substring(0, labelsString.length() - 2) : labelsString.toString();
    }

    @Override
    protected Class<GraphLabels> getValueType() {
        return GraphLabels.class;
    }
}
