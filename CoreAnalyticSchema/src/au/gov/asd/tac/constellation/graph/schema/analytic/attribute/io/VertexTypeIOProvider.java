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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.VertexTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * IOProvider for attributes described by
 * {@link au.gov.asd.tac.constellation.graph.schema.analytic.attribute.VertexTypeAttributeDescription}
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class VertexTypeIOProvider extends AbstractGraphIOProvider {

    private static final String NAME_FIELD = "Name";
    private static final String DESCRIPTION_FIELD = "Description";
    private static final String COLOR_FIELD = "Color";
    private static final String FOREGROUND_ICON_FIELD = "Foreground Icon";
    private static final String BACKGROUND_ICON_FIELD = "Background Icon";
    private static final String DETECTION_REGEX_FIELD = "Detection Regex";
    private static final String VALIDATION_REGEX_FIELD = "Validation Regex";
    private static final String SUPERTYPE_FIELD = "Super Type";
    private static final String OVERRIDDEN_TYPE_FIELD = "Overridden Type";
    private static final String PROPERTIES_FIELD = "Properties";
    private static final String INCOMPLETE_FIELD = "Incomplete";
    
    private static final String NAME = "name";
    private static final String RED = "red";
    private static final String GREEN = "green";
    private static final String BLUE = "blue";
    private static final String ALPHA = "alpha";

    @Override
    public String getName() {
        return VertexTypeAttributeDescription.ATTRIBUTE_NAME;
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode, final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull() && jnode.isObject()) {
            graph.setObjectValue(attributeId, elementId, cache.deduplicate(readTypeObject(jnode)));
        } else {
            //  legacy
            graph.setStringValue(attributeId, elementId, cache.deduplicate(jnode.isNull() ? null : jnode.textValue()));
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator, final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final SchemaVertexType attributeValue = graph.getObjectValue(attribute.getId(), elementId);
            if (attributeValue == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attribute.getName());
                writeTypeObject(attributeValue, jsonGenerator);
                jsonGenerator.writeEndObject();
            }
        }
    }

    private static SchemaVertexType readTypeObject(final JsonNode type) throws IOException {
        final JsonNode name = type.get(NAME_FIELD);
        final JsonNode description = type.get(DESCRIPTION_FIELD);
        final JsonNode color = type.get(COLOR_FIELD);
        final JsonNode foregroundIcon = type.get(FOREGROUND_ICON_FIELD);
        final JsonNode backgroundIcon = type.get(BACKGROUND_ICON_FIELD);
        final JsonNode detectRegex = type.get(DETECTION_REGEX_FIELD);
        final JsonNode validationRegex = type.get(VALIDATION_REGEX_FIELD);
        final JsonNode superType = type.get(SUPERTYPE_FIELD);
        final JsonNode overriddenType = type.get(OVERRIDDEN_TYPE_FIELD);
        final JsonNode properties = type.get(PROPERTIES_FIELD);
        final JsonNode incomplete = type.get(INCOMPLETE_FIELD);

        final Map<String, String> props = new HashMap<>();
        if (!properties.isNull() && properties.isObject()) {
            final Iterator<String> keys = properties.fieldNames();
            while (keys.hasNext()) {
                final String key = keys.next();
                final JsonNode value = properties.get(key);
                props.put(key, value.isNull() ? null : value.textValue());
            }
        }

        // TODO: need to serialise the pattern case sensitivity
        final SchemaVertexType schemaVertexType = new SchemaVertexType.Builder(name.textValue())
                .setDescription(description == null ? null : description.textValue())
                .setColor(color == null ? null : readColorObject(color))
                .setForegroundIcon(foregroundIcon == null ? null : IconManager.getIcon(foregroundIcon.textValue()))
                .setBackgroundIcon(backgroundIcon == null ? null : IconManager.getIcon(backgroundIcon.textValue()))
                .setDetectionRegex(detectRegex == null ? null : Pattern.compile(detectRegex.textValue(), Pattern.CASE_INSENSITIVE))
                .setValidationRegex(validationRegex == null ? null : Pattern.compile(validationRegex.textValue(), Pattern.CASE_INSENSITIVE))
                .setSuperType(superType == null ? null : readTypeObject(superType))
                .setOverridenType(overriddenType == null ? null : readTypeObject(overriddenType))
                .setProperties(props)
                .setIncomplete(incomplete == null ? null : incomplete.booleanValue())
                .build();

        final SchemaVertexType singletonType = SchemaVertexTypeUtilities.getType(schemaVertexType.getName());
        return schemaVertexType.equals(singletonType) ? singletonType : schemaVertexType;
    }

    private static void writeTypeObject(final SchemaVertexType type, final JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField(NAME_FIELD, type.getName());
        if (type.getDescription() != null) {
            jsonGenerator.writeStringField(DESCRIPTION_FIELD, type.getDescription());
        }
        if (type.getColor() != null) {
            jsonGenerator.writeObjectFieldStart(COLOR_FIELD);
            writeColorObject(type.getColor(), jsonGenerator);
            jsonGenerator.writeEndObject();
        }
        if (type.getForegroundIcon() != null) {
            jsonGenerator.writeStringField(FOREGROUND_ICON_FIELD, type.getForegroundIcon().getExtendedName());
        }
        if (type.getBackgroundIcon() != null) {
            jsonGenerator.writeStringField(BACKGROUND_ICON_FIELD, type.getBackgroundIcon().getExtendedName());
        }
        if (type.getDetectionRegex() != null) {
            jsonGenerator.writeStringField(DETECTION_REGEX_FIELD, type.getDetectionRegex().pattern());
        }
        if (type.getValidationRegex() != null) {
            jsonGenerator.writeStringField(VALIDATION_REGEX_FIELD, type.getValidationRegex().pattern());
        }
        if (type.getSuperType() != null && type != type.getSuperType()) {
            jsonGenerator.writeObjectFieldStart(SUPERTYPE_FIELD);
            writeTypeObject((SchemaVertexType) type.getSuperType(), jsonGenerator);
            jsonGenerator.writeEndObject();
        }
        if (type.getOverridenType() != null && type != type.getOverridenType()) {
            jsonGenerator.writeObjectFieldStart(OVERRIDDEN_TYPE_FIELD);
            writeTypeObject((SchemaVertexType) type.getOverridenType(), jsonGenerator);
            jsonGenerator.writeEndObject();
        }
        if (type.getProperties() != null) {
            jsonGenerator.writeObjectFieldStart(PROPERTIES_FIELD);
            for (final Map.Entry<String, String> entry : type.getProperties().entrySet()) {
                jsonGenerator.writeStringField(entry.getKey(), entry.getValue() != null ? entry.getValue() : null);
            }
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeBooleanField(INCOMPLETE_FIELD, type.isIncomplete());
    }

    /**
     * Read a json object representing a color and return a ColorValue object
     *
     * @param color The json node representing the color
     * @return A ColorValue object representing the color
     */
    private static ConstellationColor readColorObject(final JsonNode color) {
        if (color.has(NAME)) {
            final String name = color.get(NAME).textValue();
            return ConstellationColor.getColorValue(name);
        } else {
            final float red = color.get(RED).floatValue();
            final float green = color.get(GREEN).floatValue();
            final float blue = color.get(BLUE).floatValue();
            final float alpha = color.get(ALPHA).floatValue();
            return ConstellationColor.getColorValue(red, green, blue, alpha);
        }
    }

    /**
     * Write a json object representing the color
     *
     * @param color The color value
     * @param jsonGenerator The JsonGenerator used to write to the JSON
     * document.
     * @throws IOException
     */
    private static void writeColorObject(final ConstellationColor color, final JsonGenerator jsonGenerator) throws IOException {
        if (color.getName() != null) {
            jsonGenerator.writeStringField(NAME, color.getName());
        } else {
            jsonGenerator.writeNumberField(RED, color.getRed());
            jsonGenerator.writeNumberField(GREEN, color.getGreen());
            jsonGenerator.writeNumberField(BLUE, color.getBlue());
            jsonGenerator.writeNumberField(ALPHA, color.getAlpha());
        }
    }
}
