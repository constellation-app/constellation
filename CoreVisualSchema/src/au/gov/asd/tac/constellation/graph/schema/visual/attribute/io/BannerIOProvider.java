/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.utilities.ImmutableObjectCache;
import static au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription.ATTRIBUTE_NAME;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Banner;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class BannerIOProvider extends AbstractGraphIOProvider {

    @Override
    public String getName() {
        return ATTRIBUTE_NAME;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final boolean active_ = jnode.get("active").asBoolean();
            final int level_ = jnode.get("level").asInt(0);
            final String text_ = jnode.get("text").asText();
            final ConstellationColor fgColor_ = ConstellationColor.getColorValue(jnode.get("fg").asText());
            final ConstellationColor bgColor_ = ConstellationColor.getColorValue(jnode.get("bg").asText());
            final String template_ = jnode.get("template").asText();

            final Banner attrVal = new Banner(active_, level_, text_, fgColor_, bgColor_, template_);
            graph.setObjectValue(attributeId, 0, attrVal);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final com.fasterxml.jackson.core.JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final Banner state = (Banner) graph.getObjectValue(attr.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                jsonGenerator.writeBooleanField("active", state.isActive());
                jsonGenerator.writeNumberField("level", state.getLevel());
                jsonGenerator.writeStringField("template", state.getTemplate());
                jsonGenerator.writeStringField("text", state.getText());
                jsonGenerator.writeStringField("fg", state.getFgColor().toString());
                jsonGenerator.writeStringField("bg", state.getBgColor().toString());
                jsonGenerator.writeEndObject();
            }
        }
    }
}
