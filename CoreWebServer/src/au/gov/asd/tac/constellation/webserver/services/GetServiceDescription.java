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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import static au.gov.asd.tac.constellation.webserver.restapi.RestService.HTTP_UNPROCESSABLE_ENTITY;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceRegistry;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * Return the description and parameters of the named service.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class GetServiceDescription extends RestService {

    private static final String NAME = "get_service_description";
    private static final String SERVICE_NAME_PARAMETER_ID = "service_name";
    private static final String METHOD_NAME_PARAMETER_ID = "http_method";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Return the description and parameters of the named service.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"service"};
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(SERVICE_NAME_PARAMETER_ID);
        nameParam.setName("Service name");
        nameParam.setDescription("The name of the service to be described.");
        nameParam.setRequired(true);
        parameters.addParameter(nameParam);

        final PluginParameter<StringParameterValue> methodParam = StringParameterType.build(METHOD_NAME_PARAMETER_ID);
        methodParam.setName("HTTP method name");
        methodParam.setDescription("The HTTP method by which the service id called (GET, PUT, POST), default GET.");
        methodParam.setStringValue(HttpMethod.GET.name());
        methodParam.setRequired(true);
        parameters.addParameter(methodParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String serviceName = parameters.getStringValue(SERVICE_NAME_PARAMETER_ID);
        final HttpMethod httpMethod = HttpMethod.getValue(parameters.getStringValue(METHOD_NAME_PARAMETER_ID));

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = mapper.createObjectNode();

            final RestService rs = RestServiceRegistry.get(serviceName, httpMethod);
            root.put("name", rs.getName());
            root.put("http_method", httpMethod.name());
            root.put("description", rs.getDescription());
            root.put("mimetype", rs.getMimeType());
            final ArrayNode tags = root.putArray("tags");
            for (final String tag : rs.getTags()) {
                tags.add(tag);
            }

            final ObjectNode params = root.putObject("parameters");
            rs.createParameters().getParameters().entrySet().forEach(entry -> {
                final PluginParameter<?> pp = entry.getValue();
                final ObjectNode param = params.putObject(entry.getKey());
                param.put("name", pp.getName());
                param.put("type", pp.getType().getId());
                param.put("description", pp.getDescription());
                if (pp.getObjectValue() != null) {
                    param.put("value", pp.getObjectValue().toString());
                }
            });

            mapper.writeValue(out, root);
        } catch (final IllegalArgumentException ex) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, ex.getMessage());
        }
    }
}
