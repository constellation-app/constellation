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

import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginRegistry;
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
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
 @ServiceProvider(service=RestService.class)
public class GetPluginParameters extends RestService {
    private static final String NAME = "get_plugin_parameters";
    private static final String PLUGIN_NAME_PARAMETER_ID = String.format("%s.%s", NAME, "plugin_name");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Get the plugin parameters for the named plugin.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(PLUGIN_NAME_PARAMETER_ID);
        nameParam.setName("Plugin name");
        nameParam.setDescription("Show the plugin parameters.");
        parameters.addParameter(nameParam);

        return parameters;
    }

    @Override
    public void service(final PluginParameters parameters, InputStream in, OutputStream out) throws IOException {
        final String pluginName = parameters.getStringValue(PLUGIN_NAME_PARAMETER_ID);

        final Plugin plugin = PluginRegistry.get(pluginName);
        final PluginParameters pluginParams = plugin.createParameters();

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();
        pluginParams.getParameters().entrySet().forEach(entry -> {
            final ObjectNode param = root.putObject(entry.getKey());
            final PluginParameter<?> pp = entry.getValue();
            param.put("name", pp.getName());
            param.put("type", pp.getType().getId());
            param.put("description", pp.getDescription());
        });

        mapper.writeValue(out, root);
    }
}
