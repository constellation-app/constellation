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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Plane;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.PlaneState;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class PlaneStateIOProvider extends AbstractGraphIOProvider {

    private static final String PLANE_LIST = "plane_list";

    @Override
    public String getName() {
        return PlaneState.ATTRIBUTE_NAME;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final PlaneState ps = new PlaneState();
            final ArrayList<Plane> planes = new ArrayList<>();

            final JsonNode planeList = jnode.get(PLANE_LIST);
            for (Iterator<JsonNode> i = planeList.elements(); i.hasNext();) {
                final JsonNode element = i.next();

                
                final Plane p = Plane.readNode(element, graph, byteReader);
                planes.add(p);
            }
            ps.setPlanes(planes);

            graph.setObjectValue(attributeId, elementId, ps);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final PlaneState state = (PlaneState) graph.getObjectValue(attr.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                jsonGenerator.writeArrayFieldStart(PLANE_LIST);
                for (final Plane p : state.getPlanes()) {
                    jsonGenerator.writeStartObject();
                    p.writeNode(jsonGenerator, byteWriter);
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
        }
    }
}
