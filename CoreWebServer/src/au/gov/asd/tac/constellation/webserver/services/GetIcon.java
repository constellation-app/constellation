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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import static au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities.IMAGE_PNG;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 * Get the named icon as a PNG file.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class GetIcon extends RestService {

    private static final String NAME = "get_icon";
    private static final String ICON_PARAMETER_ID = "icon_name";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Get the named icon as a PNG file.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"icon", "PNG"};
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(ICON_PARAMETER_ID);
        nameParam.setName("Icon Name");
        nameParam.setDescription("The name of the icon to return as a PNG file.");
        nameParam.setRequired(true);
        parameters.addParameter(nameParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String iconName = parameters.getStringValue(ICON_PARAMETER_ID);
        if (!IconManager.iconExists(iconName)) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "No icon with name " + iconName);
        }
        final ConstellationIcon icon = IconManager.getIcon(iconName);
        out.write(icon.buildByteArray());
    }

    @Override
    public String getMimeType() {
        return IMAGE_PNG;
    }
}
