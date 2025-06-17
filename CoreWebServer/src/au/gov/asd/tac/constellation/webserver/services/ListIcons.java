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
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * List the available icons.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class ListIcons extends RestService {

    private static final String NAME = "list_icons";
    private static final String EDITABLE_PARAMETER_ID = "editable";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "List the available icons.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"icon"};
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> editableParam = BooleanParameterType.build(EDITABLE_PARAMETER_ID);
        editableParam.setName("Editable");
        editableParam.setDescription("If false (the default), return the built-in icons, else return the editable icons.");
        editableParam.setObjectValue(false);
        parameters.addParameter(editableParam);

        return parameters;
    }

    @Override
    public void callService(PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final boolean editable = parameters.getBooleanValue(EDITABLE_PARAMETER_ID);
        final List<String> names = new ArrayList<>(IconManager.getIconNames(editable));
        names.sort(String::compareToIgnoreCase);

        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode root = mapper.createArrayNode();
        names.forEach(root::add);

        mapper.writeValue(out, root);
    }
}
