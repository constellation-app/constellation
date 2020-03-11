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

import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.RawAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AttributeValueTranslator;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import java.util.Arrays;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractAttributeInteraction.class)
public class RawAttributeInteraction extends AbstractAttributeInteraction<RawData> {

    @Override
    public String getDataType() {
        return RawAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public String getDisplayText(Object attrVal) {
        if (attrVal == null) {
            return null;
        }
        return attrVal.toString();
    }

    @Override
    public List<String> getPreferredEditTypes() {
        return Arrays.asList(StringAttributeDescription.ATTRIBUTE_NAME);
    }

    @Override
    public ValueValidator<RawData> fromEditValidator(String dataType) {
        return ValueValidator.getAlwaysFailValidator("Raw values are uneditable.");
    }

    @Override
    public AttributeValueTranslator toEditTranslator(String dataType) {
        if (dataType.equals(StringAttributeDescription.ATTRIBUTE_NAME)) {
            return v -> {
                return v == null ? v : getDisplayText(v);
            };
        }
        return super.toEditTranslator(dataType);
    }

    @Override
    protected Class<RawData> getValueType() {
        return RawData.class;
    }
}
