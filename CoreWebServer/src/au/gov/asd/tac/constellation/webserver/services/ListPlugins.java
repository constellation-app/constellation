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
package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * List the available plugins.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class ListPlugins extends RestService {

    private static final String NAME = "list_plugins";
    private static final String ALIAS_PARAMETER_ID = "alias";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "List the available plugins.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"plugin"};
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterType.BooleanParameterValue> aliasParam = BooleanParameterType.build(ALIAS_PARAMETER_ID);
        aliasParam.setName("Show aliases");
        aliasParam.setDescription("Show the plugin aliases if true, the full name otherwise (default false).");
        aliasParam.setObjectValue(true);
        parameters.addParameter(aliasParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final boolean alias = parameters.getBooleanValue(ALIAS_PARAMETER_ID);

        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode root = mapper.createArrayNode();
        PluginRegistry.getPluginClassNames()
                .stream()
                .map(name -> alias ? PluginRegistry.getAlias(name) : name)
                .forEachOrdered(name -> root.add(name));

        mapper.writeValue(out, root);
    }
}
