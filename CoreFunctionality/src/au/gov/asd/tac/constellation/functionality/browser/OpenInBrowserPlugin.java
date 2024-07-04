/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality.browser;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Open In Browser Plugin
 *
 * @author arcturus
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.UTILITY})
@NbBundle.Messages("OpenInBrowserPlugin=Open In Browser")
public class OpenInBrowserPlugin extends SimplePlugin {

    public static final String APPLICATION_PARAMETER_ID = PluginParameter.buildId(OpenInBrowserPlugin.class, "application");
    public static final String URL_PARAMETER_ID = PluginParameter.buildId(OpenInBrowserPlugin.class, "url");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> applicationParameter = StringParameterType.build(APPLICATION_PARAMETER_ID);
        applicationParameter.setName("Application");
        applicationParameter.setDescription("The name of the application being opened (optional)");
        parameters.addParameter(applicationParameter);

        final PluginParameter<StringParameterValue> urlParameter = StringParameterType.build(URL_PARAMETER_ID);
        urlParameter.setName("URL");
        urlParameter.setDescription("The url to open in the browser");
        parameters.addParameter(urlParameter);

        return parameters;
    }

    @Override
    protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        try {
            Desktop.getDesktop().browse(new URI(parameters.getStringValue(URL_PARAMETER_ID)));
        } catch (final IOException | URISyntaxException ex) {
            throw new PluginException(PluginNotificationLevel.FATAL, ex);
        }
    }

}
