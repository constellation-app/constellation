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
package au.gov.asd.tac.constellation.views.find.advanced;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A GraphIOProvider for FindState instances.
 *
 * @author betelgeuse
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public final class FindStateIOProvider extends AbstractGraphIOProvider {
    
    private static final Logger LOGGER = Logger.getLogger(FindStateIOProvider.class.getName());

    private static final String ARGS = "args";
    private static final String ATTR = "attr";
    private static final String ELMT = "elmt";
    private static final String GTYP = "gtyp";
    private static final String MODE = "mode";
    private static final String OPER = "oper";
    private static final String RULE = "rule";
    private static final String TYPE = "type";
    private static final String HOLD = "hold";
    private static final String IS_EMPTY = "is_empty";

    @Override
    public String getName() {
        return FindState.ATTRIBUTE_NAME;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexmap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, ImmutableObjectCache cache) {
        if (!jnode.isNull()) {
            final FindState state = new FindState();

            // Retrieve the GraphElementType:
            state.setGraphElementType(GraphElementType.getValue(jnode.get(GTYP).asText()));

            state.setAny(jnode.get(MODE).asBoolean());

            final Iterator<JsonNode> itr = jnode.get(RULE).iterator();

            while (itr.hasNext()) {
                final JsonNode node = itr.next();

                final FindRule fr = getRule(graph, node);
                if (fr != null) {
                    state.getRules().add(fr);
                }
            }
            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attr, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attr.getId(), elementId)) {
            final FindState state = (FindState) graph.getObjectValue(attr.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attr.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attr.getName());
                jsonGenerator.writeStringField(GTYP, state.getGraphElementType().toString());
                jsonGenerator.writeBooleanField(MODE, state.isAny());
                jsonGenerator.writeBooleanField(HOLD, state.isHeld());
                final Iterator<FindRule> itr = state.getRules().listIterator();
                jsonGenerator.writeArrayFieldStart(RULE);
                while (itr.hasNext()) {
                    addRule(jsonGenerator, itr.next());
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
        }
    }

    /**
     * Helper method to serialise an individual FindRule to a JSON node.
     *
     * @param node The parent node of the rule being serialised.
     * @param rule The rule to be serialised.
     */
    private static void addRule(final JsonGenerator jg, final FindRule rule) {
        try {
            jg.writeStartObject();

            jg.writeStringField(ATTR, rule.getAttribute().getName());
            jg.writeStringField(ELMT, rule.getAttribute().getElementType().toString());
            jg.writeStringField(TYPE, rule.getType().name());
            jg.writeStringField(OPER, rule.getOperator().name());
            jg.writeBooleanField(HOLD, rule.isHeld());

            addArguments(jg, rule.getArgs());

            jg.writeEndObject();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Helper method that deserialises an individual rule from a JSON node.
     *
     * @param graph The graph that is being loaded.
     * @param node The node that contains an individual FindRule.
     * @return A single FindRule.
     */
    private static FindRule getRule(final GraphReadMethods graph, final JsonNode node) {
        final FindRule rule = new FindRule();
        final int attrID = graph.getAttribute(GraphElementType.getValue(node.get(ELMT).asText()),
                node.get(ATTR).asText());
        if (attrID != Graph.NOT_FOUND) {
            final Attribute attr = new GraphAttribute(graph, attrID);

            rule.setAttribute(attr);
            rule.setOperator(FindTypeOperators.Operator.getTypeEnum(node.get(OPER).textValue()));
            rule.setType(FindTypeOperators.Type.getTypeEnum(node.get(TYPE).textValue()));
            rule.setHeld(node.get(HOLD).asBoolean());

            rule.setArgs(getArguments(node.get(ARGS)));

            return rule;
        }

        return null;
    }

    /**
     * Helper method that serialises a series of arguments for a FindRule into a
     * node.
     *
     * @param node The parent node for the series of arguments.
     * @param arguments Map of arguments to be serialised to a JSON node.
     */
    private static void addArguments(final JsonGenerator jg, final Map<String, Object> arguments) {
        try {
            jg.writeFieldName(ARGS);
            jg.writeStartObject();

            jg.writeBooleanField(IS_EMPTY, arguments.isEmpty());

            final Iterator<String> itr = arguments.keySet().iterator();
            while (itr.hasNext()) {
                final String key = itr.next();
                final Object value = arguments.get(key);

                if (value instanceof Boolean) {
                    jg.writeBooleanField(key, (Boolean) value);
                } else if (value instanceof Float) {
                    jg.writeNumberField(key, ((Number) value).floatValue());
                } else if (value instanceof Integer) {
                    jg.writeNumberField(key, ((Number) value).intValue());
                } else if (value instanceof String) {
                    jg.writeStringField(key, (String) value);
                } else if (value != null) {
                    jg.writeStringField(key, value.toString());
                } else {
                    jg.writeStringField(key, "");
                }
            }
            jg.writeEndObject();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Helper method to get the series of arguments for a given FindRule.
     *
     * @param node The node that contains the arguments.
     * @return Map of arguments relevant to this node.
     */
    private static Map<String, Object> getArguments(final JsonNode node) {
        final Map<String, Object> args = new HashMap<>();

        if (!node.get(IS_EMPTY).asBoolean()) {
            final Iterator<String> itr = node.fieldNames();

            while (itr.hasNext()) {
                final String key = itr.next();
                if (!key.equals(IS_EMPTY)) {
                    // The only special type we need to check for is boolean:
                    if (node.get(key).isBoolean()) {
                        args.put(key, node.get(key).asBoolean());
                    } else {
                        args.put(key, node.get(key).asText());
                    }
                }
            }
        }

        return args;
    }
}
