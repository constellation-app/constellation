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
package au.gov.asd.tac.constellation.graph.node.templates;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.io.GraphJsonReader;
import au.gov.asd.tac.constellation.graph.io.GraphParseException;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginGraphs;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.PluginNotificationLevel;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameter;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.pluginframework.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.pluginframework.templates.SimplePlugin;
import au.gov.asd.tac.constellation.visual.IoProgressHandle;
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
            final Graph graph = new GraphJsonReader().readGraphZip(loadFile, new IoProgressHandle("Loading Template..."));
            GraphOpener.getDefault().openGraph(graph, templateName);
        } catch (GraphParseException | IOException ex) {
            throw new PluginException(this, PluginNotificationLevel.ERROR, "Failed to open template", ex);
        }
    }

}
