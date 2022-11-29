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
package au.gov.asd.tac.constellation.graph.node.templates;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonReader;
import au.gov.asd.tac.constellation.graph.file.io.GraphParseException;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import java.io.File;
import java.io.IOException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Load Template Plugin
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.UTILITY})
@NbBundle.Messages("LoadTemplatePlugin=Load Template")
public class LoadTemplatePlugin extends SimplePlugin {

    public static final String TEMPLATE_FILE_PARAMETER_ID = PluginParameter.buildId(LoadTemplatePlugin.class, "file");
    public static final String TEMPLATE_NAME_PARAMETER_ID = PluginParameter.buildId(LoadTemplatePlugin.class, "name");

    @Override
    public PluginParameters createParameters() {

        final PluginParameters params = new PluginParameters();
        final PluginParameter<ObjectParameterValue> fileParam = ObjectParameterType.build(TEMPLATE_FILE_PARAMETER_ID);
        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(TEMPLATE_NAME_PARAMETER_ID);
        params.addParameter(fileParam);
        params.addParameter(nameParam);
        return params;
    }

    @Override
    public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        loadTemplate((File) parameters.getObjectValue(TEMPLATE_FILE_PARAMETER_ID), parameters.getStringValue(TEMPLATE_NAME_PARAMETER_ID));
    }

    private void loadTemplate(final File loadFile, final String templateName) throws PluginException {
        try {
            final Graph graph = new GraphJsonReader().readGraphZip(loadFile, new HandleIoProgress("Loading Template..."));
            GraphOpener.getDefault().openGraph(graph, templateName);
        } catch (GraphParseException | IOException ex) {
            throw new PluginException(this, PluginNotificationLevel.ERROR, "Failed to open template", ex);
        }
    }

}
