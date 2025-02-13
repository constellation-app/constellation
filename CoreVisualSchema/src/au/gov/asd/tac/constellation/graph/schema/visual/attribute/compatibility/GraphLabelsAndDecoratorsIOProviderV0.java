/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * Graph Labels and Decorators IO Provider.
 * <p>
 * Note that this should no longer be used and only remains to support legacy
 * graph files.
 *
 * @author twinkle2_little
 */
@Deprecated
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class GraphLabelsAndDecoratorsIOProviderV0 extends AbstractGraphIOProvider {
    
    private static final Logger LOGGER = Logger.getLogger(GraphLabelsAndDecoratorsIOProviderV0.class.getName());

    @Override
    public String getName() {
        return "labels";
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, final ImmutableObjectCache cache) {
        if (!jnode.isNull()) {
            final GraphLabelsAndDecoratorsV0 labels = new GraphLabelsAndDecoratorsV0();
            labels.setBottomLabels(getLabels(jnode, GraphLabelsAndDecoratorsV0.FIELD_BOTTOM));
            labels.setTopLabels(getLabels(jnode, GraphLabelsAndDecoratorsV0.FIELD_TOP));
            labels.setConnectionLabels(getLabels(jnode, GraphLabelsAndDecoratorsV0.FIELD_CONNECTIONS));

            for (final GraphLabelsAndDecoratorsV0.Decorator dec : GraphLabelsAndDecoratorsV0.Decorator.values()) {
                labels.setDecoratorLabel(dec, getDecorator(jnode, dec));
            }

            graph.setObjectValue(attributeId, elementId, labels);
        }
    }

    private List<GraphLabelV0> getLabels(final JsonNode jnode, final String position) {
        List<GraphLabelV0> labelList = new ArrayList<>();
        if (jnode.has(position)) {
            final ArrayNode labelsNode = (ArrayNode) jnode.get(position);
            final Iterator<JsonNode> it = labelsNode.iterator();
            while (it.hasNext()) {
                final JsonNode lnode = it.next();
                final String attr = lnode.get(GraphLabelsAndDecoratorsV0.FIELD_ATTR).textValue();
                final String color = lnode.get(GraphLabelsAndDecoratorsV0.FIELD_COLOR).textValue();
                final float radius = lnode.has(GraphLabelsAndDecoratorsV0.FIELD_RADIUS) ? (float) lnode.get(GraphLabelsAndDecoratorsV0.FIELD_RADIUS).asDouble() : 1F;

                final GraphLabelV0 label = new GraphLabelV0(attr, ConstellationColor.getColorValue(color), radius);
                labelList.add(label);
            }
        }
        return labelList;
    }

    private static String getDecorator(final JsonNode jnode, final GraphLabelsAndDecoratorsV0.Decorator dec) {
        final String d = dec.toString();
        return jnode.has(d) ? jnode.get(d).textValue() : null;
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            GraphLabelsAndDecoratorsV0 state = (GraphLabelsAndDecoratorsV0) graph.getObjectValue(attr.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                putLabels(jsonGenerator, GraphLabelsAndDecoratorsV0.FIELD_BOTTOM, Arrays.asList(state.getBottomLabels()));
                putLabels(jsonGenerator, GraphLabelsAndDecoratorsV0.FIELD_TOP, Arrays.asList(state.getTopLabels()));
                putLabels(jsonGenerator, GraphLabelsAndDecoratorsV0.FIELD_CONNECTIONS, Arrays.asList(state.getConnectionLabels()));
                for (final GraphLabelsAndDecoratorsV0.Decorator dec : GraphLabelsAndDecoratorsV0.Decorator.values()) {
                    putDecorator(state, jsonGenerator, dec);
                }
                jsonGenerator.writeEndObject();
            }
        }
    }

    private static void putLabels(final JsonGenerator jg, final String position, final List<GraphLabelV0> labelList) {
        try {

            jg.writeArrayFieldStart(position);
            for (GraphLabelV0 label : labelList) {
                jg.writeStartObject();
                jg.writeStringField(GraphLabelsAndDecoratorsV0.FIELD_ATTR, label.getLabel());
                jg.writeStringField(GraphLabelsAndDecoratorsV0.FIELD_COLOR, label.getColor().toString());
                jg.writeNumberField(GraphLabelsAndDecoratorsV0.FIELD_RADIUS, label.getRadius());
                jg.writeEndObject();
            }
            jg.writeEndArray();

        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    private void putDecorator(final GraphLabelsAndDecoratorsV0 state, final JsonGenerator jg, final GraphLabelsAndDecoratorsV0.Decorator dec) {
        final String attr = state.getDecoratorLabels()[dec.ordinal()];
        if (attr != null) {
            try {
                jg.writeStringField(dec.toString(), attr);
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
