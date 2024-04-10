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
package au.gov.asd.tac.constellation.graph.utilities.perspectives;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.utilities.perspectives.PerspectiveModel.Perspective;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class PerspectiveIOProvider extends AbstractGraphIOProvider {
    
    private static final Logger LOGGER = Logger.getLogger(PerspectiveIOProvider.class.getName());

    private static final String LIST = "list";
    private static final String LABEL = "label";
    private static final String RELATIVE_TO = "relative_to";
    private static final String EYE = "eye";
    private static final String CENTRE = "centre";
    private static final String UP = "up";
    private static final String ROTATE = "rotate";

    @Override
    public String getName() {
        return PerspectiveAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final PerspectiveModel model = new PerspectiveModel();

            final JsonNode root = jnode.get(LIST);
            if (root.isArray()) {
                final ArrayNode rootArray = (ArrayNode) root;
                for (final Iterator<JsonNode> i = rootArray.elements(); i.hasNext();) {
                    final JsonNode element = i.next();

                    final String label = element.get(LABEL).textValue();
                    final int oldRelativeTo = element.get(RELATIVE_TO).intValue();
                    final int relativeTo = vertexMap.getOrDefault(oldRelativeTo, Graph.NOT_FOUND);
                    final Vector3f eye = readVector(element, EYE);
                    final Vector3f centre = readVector(element, CENTRE);
                    final Vector3f up = readVector(element, UP);
                    final Vector3f rotate = readVector(element, ROTATE);

                    // Don't load dud values.
                    if (label != null && eye != null && centre != null && up != null && rotate != null) {
                        final Perspective p = new Perspective(label, relativeTo, centre, eye, up, rotate);
                        model.addElement(p);
                    }
                }
            }

            graph.setObjectValue(attributeId, elementId, model);
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final PerspectiveModel model = (PerspectiveModel) graph.getObjectValue(attribute.getId(), elementId);
            if (model == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attribute.getName());

                jsonGenerator.writeArrayFieldStart(LIST);
                for (final Perspective p : model.perspectives) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(LABEL, p.label);
                    jsonGenerator.writeNumberField(RELATIVE_TO, p.relativeTo);
                    writeVector(jsonGenerator, EYE, p.eye);
                    writeVector(jsonGenerator, CENTRE, p.centre);
                    writeVector(jsonGenerator, UP, p.up);
                    writeVector(jsonGenerator, ROTATE, p.rotate);
                    jsonGenerator.writeEndObject();
                }

                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
        }
    }

    /**
     * Read a Vector3f from a JsonNode.
     *
     * @param node The JsonNode to get the Vector3f from.
     * @param label The label of the Vector3f node.
     *
     * @return
     */
    private static Vector3f readVector(final JsonNode node, final String label) {
        if (node.hasNonNull(label)) {
            final ArrayNode array = (ArrayNode) node.withArray(label);

            return new Vector3f((float) array.get(0).asDouble(), (float) array.get(1).asDouble(), (float) array.get(2).asDouble());
        }

        return null;
    }

    /**
     * Write a Vector3f to an ObjectNode.
     *
     * @param node The ObjectNode to write the Vector3f to.
     * @param label The label of the Vector3f node.
     * @param v The Vector3f to be written.
     */
    private static void writeVector(final JsonGenerator jg, final String label, final Vector3f v) {
        if (v != null) {
            try {
                jg.writeArrayFieldStart(label);
                jg.writeNumber(v.getX());
                jg.writeNumber(v.getY());
                jg.writeNumber(v.getZ());
                jg.writeEndArray();
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        } else {
            try {
                jg.writeNullField(label);
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }
}
