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
package au.gov.asd.tac.constellation.webserver.impl;

import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.webserver.api.EndpointException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author algol
 */
public class TypeImpl {

    /**
     * Describe the specified type.
     *
     * @param type The type to describe.
     * @param out An OutputStream to write the response to.
     *
     * @throws IOException
     */
    public static void get_describe(final String type, final OutputStream out) throws IOException {
        if (!SchemaVertexTypeUtilities.getDefaultType().equals(SchemaVertexTypeUtilities.getType(type))) {
            final SchemaVertexType vertexType = SchemaVertexTypeUtilities.getType(type);

            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = mapper.createObjectNode();
            root.put("name", vertexType.getName());
            if (vertexType.getDescription() != null) {
                root.put("description", vertexType.getDescription());
            }
            if (vertexType.getColor() != null) {
                root.put("color", vertexType.getColor().toString());
            }
            if (vertexType.getForegroundIcon() != null) {
                root.put("foregroundIcon", vertexType.getForegroundIcon().toString());
            }
            if (vertexType.getBackgroundIcon() != null) {
                root.put("backgroundIcon", vertexType.getBackgroundIcon().toString());
            }
            if (vertexType.getDetectionRegex() != null) {
                root.put("detectionRegex", vertexType.getDetectionRegex().pattern());
            }
            if (vertexType.getValidationRegex() != null) {
                root.put("validationRegex", vertexType.getValidationRegex().pattern());
            }
            root.put("properties", vertexType.getProperties().toString());
            root.put("super_type", vertexType.getSuperType().getName());
            root.put("top_level_type", vertexType.getTopLevelType().getName());
            root.put("hierarchy", vertexType.getHierachy());

            mapper.writeValue(out, root);
        } else if (!SchemaTransactionTypeUtilities.getDefaultType().equals(SchemaTransactionTypeUtilities.getType(type))) {
            final SchemaTransactionType transactionType = SchemaTransactionTypeUtilities.getType(type);

            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = mapper.createObjectNode();
            root.put("name", transactionType.getName());
            if (transactionType.getDescription() != null) {
                root.put("description", transactionType.getDescription());
            }
            if (transactionType.getColor() != null) {
                root.put("color", transactionType.getColor().toString());
            }
            if (transactionType.getStyle() != null) {
                root.put("style", transactionType.getStyle().toString());
            }
            if (transactionType.isDirected() != null) {
                root.put("directed", transactionType.isDirected().toString());
            }
            root.put("properties", transactionType.getProperties().toString());
            root.put("super_type", transactionType.getSuperType().getName());
            root.put("top_level_type", transactionType.getTopLevelType().getName());
            root.put("hierarchy", transactionType.getHierachy());

            mapper.writeValue(out, root);
        } else {
            throw new EndpointException("The given type was not recognised.");
        }
    }
}
