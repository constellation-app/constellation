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
package au.gov.asd.tac.constellation.views.scatterplot.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * IO provider for the ScatterPlotState object.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class ScatterPlotStateIoProvider extends AbstractGraphIOProvider {

    private static final String X_ATTRIBUTE = "xAttribute";
    private static final String Y_ATTRIBUTE = "yAttribute";

    @Override
    public String getName() {
        return ScatterPlotConcept.MetaAttribute.SCATTER_PLOT_STATE.getName();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, 
            final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, 
            final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final ScatterPlotState state = new ScatterPlotState();
            final GraphElementType elementType = GraphElementType.valueOf(jnode.get("elementType").asText());
            final Attribute xAttribute = "null".equalsIgnoreCase(jnode.get(X_ATTRIBUTE).asText()) ? null
                    : new GraphAttribute(graph, graph.getAttribute(elementType, jnode.get(X_ATTRIBUTE).asText()));
            final Attribute yAttribute = "null".equalsIgnoreCase(jnode.get(Y_ATTRIBUTE).asText()) ? null
                    : new GraphAttribute(graph, graph.getAttribute(elementType, jnode.get(Y_ATTRIBUTE).asText()));
            state.setElementType(elementType);
            state.setXAttribute(xAttribute);
            state.setYAttribute(yAttribute);
            state.setSelectedOnly(jnode.get("selectedOnly").asBoolean());

            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator, 
            final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final ScatterPlotState state = (ScatterPlotState) graph.getObjectValue(attribute.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attribute.getName());
                jsonGenerator.writeStringField("elementType", state.getElementType().name());
                jsonGenerator.writeStringField(X_ATTRIBUTE, state.getXAttribute() == null ? null : state.getXAttribute().getName());
                jsonGenerator.writeStringField(Y_ATTRIBUTE, state.getYAttribute() == null ? null : state.getYAttribute().getName());
                jsonGenerator.writeBooleanField("selectedOnly", state.isSelectedOnly());
                jsonGenerator.writeEndObject();
            }
        }
    }
}
