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
package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * Get the named type as a JSON document.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class GetTypeDescription extends RestService {

    private static final String NAME = "get_type_description";
    private static final String TYPE_PARAMETER_ID = "type_name";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Get the description of the named type as a JSON document.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"type"};
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(TYPE_PARAMETER_ID);
        nameParam.setName("The name of the type.");
        nameParam.setDescription("The name of the Node or Transaction type.");
        nameParam.setRequired(true);
        parameters.addParameter(nameParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String typeName = parameters.getStringValue(TYPE_PARAMETER_ID);

        if (!SchemaVertexTypeUtilities.getDefaultType().equals(SchemaVertexTypeUtilities.getType(typeName))) {
            final SchemaVertexType vertexType = SchemaVertexTypeUtilities.getType(typeName);

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
        } else if (!SchemaTransactionTypeUtilities.getDefaultType().equals(SchemaTransactionTypeUtilities.getType(typeName))) {
            final SchemaTransactionType transactionType = SchemaTransactionTypeUtilities.getType(typeName);

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
            throw new IllegalArgumentException(String.format("The type '%s' is unknown.", typeName));
        }
    }
}
