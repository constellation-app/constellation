/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
 @ServiceProvider(service=RestService.class)
public class GetServiceDescription extends RestService {
    private static final String NAME = "get_service_description";
    private static final String SERVICE_NAME_PARAMETER_ID = String.format("%s.%s", NAME, "service_name");
    private static final String METHOD_NAME_PARAMETER_ID = String.format("%s.%s", NAME, "method");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Return the description and parameters of the named service.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(SERVICE_NAME_PARAMETER_ID);
        nameParam.setName("Service name");
        nameParam.setDescription("The name of the service to be described.");
        parameters.addParameter(nameParam);

        final PluginParameter<StringParameterValue> methodParam = StringParameterType.build(METHOD_NAME_PARAMETER_ID);
        methodParam.setName("Method name");
        methodParam.setDescription("The HTTP method by which the service id called (GET, PUT, POST), default GET.");
        methodParam.setStringValue("GET");
        parameters.addParameter(methodParam);

        return parameters;
    }

    @Override
    public void service(final PluginParameters parameters, InputStream in, OutputStream out) throws IOException {
        final String serviceName = parameters.getStringValue(SERVICE_NAME_PARAMETER_ID);
        final String method = parameters.getStringValue(METHOD_NAME_PARAMETER_ID);

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();

        final Optional<? extends RestService> rs_o = Lookup.getDefault().lookupAll(RestService.class)
            .stream()
            .filter(rs -> rs.getName().equals(serviceName) && rs.getHttpMethod().equals(method))
            .findFirst();

        if(rs_o.isPresent()) {
            final RestService rs = rs_o.get();
            root.put("name", rs.getName());
            root.put("description", rs.getDescription());
            root.put("mimetype", rs.getMimeType());

            final ObjectNode params = root.putObject("parameters");
            rs.createParameters().getParameters().entrySet().forEach(entry -> {
                final PluginParameter<?> pp = entry.getValue();
                final ObjectNode param = params.putObject(entry.getKey());
                param.put("name", pp.getName());
                param.put("type", pp.getType().getId());
                param.put("description", pp.getDescription());
                if(pp.getObjectValue()!=null) {
                    param.put("value", pp.getObjectValue().toString());
                }
            });
        } else {
            throw new IllegalArgumentException(String.format("Service '%s' does not exist.", serviceName));
        }

        mapper.writeValue(out, root);
    }
}
