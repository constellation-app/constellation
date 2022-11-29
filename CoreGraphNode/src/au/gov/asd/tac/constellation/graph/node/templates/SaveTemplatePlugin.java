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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonWriter;
import au.gov.asd.tac.constellation.graph.node.create.NewSchemaGraphAction;
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
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Save Template Plugin
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.UTILITY})
@NbBundle.Messages("SaveTemplatePlugin=Save Template")
public class SaveTemplatePlugin extends SimplePlugin {

    private static final String TEMPLATE_DIR = "Graph Templates";

    public static final String TEMPLATE_NAME_PARAMETER_ID = PluginParameter.buildId(SaveTemplatePlugin.class, "name");
    public static final String TEMPLATE_NAME_PARAMETER_ID_NAME = "Template Name";
    public static final String TEMPLATE_NAME_PARAMETER_ID_DESCRIPTION = "The Name of the Template File";
    public static final String TEMPLATE_NAME_PARAMETER_ID_DEFAULT_VALUE = "My Template";

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();
        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(TEMPLATE_NAME_PARAMETER_ID);
        nameParam.setName(TEMPLATE_NAME_PARAMETER_ID_NAME);
        nameParam.setDescription(TEMPLATE_NAME_PARAMETER_ID_DESCRIPTION);
        nameParam.setStringValue(TEMPLATE_NAME_PARAMETER_ID_DEFAULT_VALUE);
        params.addParameter(nameParam);
        return params;
    }

    @Override
    public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph graph = graphs.getGraph();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            saveTemplate(rg, parameters.getStringValue(TEMPLATE_NAME_PARAMETER_ID));
            parameters.getParameters().get(TEMPLATE_NAME_PARAMETER_ID).storeRecentValue();
        } finally {
            rg.release();
        }
    }

    private void saveTemplate(final GraphReadMethods graph, final String templateName) throws PluginException {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);

        final File templateDir = new File(userDir, TEMPLATE_DIR);
        final File oldTemplate = new File(templateDir, NewSchemaGraphAction.getTemplateNames().get(templateName) + "/" + templateName);
        if (oldTemplate.exists()) {
            final boolean oldTemplateIsDeleted = oldTemplate.delete();
            if (!oldTemplateIsDeleted) {
                //TODO: Handle case where file not successfully deleted
            }
        }

        if (!templateDir.exists()) {
            templateDir.mkdir();
        }
        if (!templateDir.isDirectory()) {
            final String msg = String.format("Can't create template directory '%s'.", templateDir);
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        final File schemaDir = new File(templateDir, graph.getSchema().getFactory().getName());
        if (!schemaDir.exists()) {
            schemaDir.mkdir();
        }
        if (!schemaDir.isDirectory()) {
            final String msg = String.format("Can't create template directory '%s'.", schemaDir);
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        final File saveFile = new File(schemaDir, templateName);

        try {
            new GraphJsonWriter().writeTemplateToZip(graph, saveFile.getPath(), new HandleIoProgress("Saving Template..."));
        } catch (IOException ex) {
            throw new PluginException(this, PluginNotificationLevel.ERROR, "Failed to save template", ex);
        }
    }

}
