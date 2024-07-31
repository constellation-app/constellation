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
package au.gov.asd.tac.constellation.graph.node.templates;

import au.gov.asd.tac.constellation.graph.node.create.NewSchemaGraphAction;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ActionParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Manage Templates Plugin
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.UTILITY})
@NbBundle.Messages("ManageTemplatesPlugin=Manage Templates")
public class ManageTemplatesPlugin extends SimplePlugin {

    public static final String TEMPLATE_NAME_PARAMETER_ID = PluginParameter.buildId(ManageTemplatesPlugin.class, "template");
    public static final String TEMPLATE_NAME_PARAMETER_ID_NAME = "Template";
    public static final String TEMPLATE_NAME_PARAMETER_ID_DESCRIPTION = "The name of the template";
    public static final String DELETE_TEMPLATE_PARAMETER_ID = PluginParameter.buildId(ManageTemplatesPlugin.class, "delete");
    public static final String DELETE_TEMPLATE_PARAMETER_ID_NAME = "Delete";
    public static final String DEFAULT_TEMPLATE_PARAMETER_ID = PluginParameter.buildId(ManageTemplatesPlugin.class, "default");
    public static final String DEFAULT_TEMPLATE_PARAMETER_ID_NAME = "Set Default";
    public static final String CURRENT_DEFAULT_PARAMETER_ID = PluginParameter.buildId(ManageTemplatesPlugin.class, "current_default");
    public static final String CURRENT_DEFAULT_PARAMETER_ID_NAME = "Current Default";
    public static final String CLEAR_DEFAULT_PARAMETER_ID = PluginParameter.buildId(ManageTemplatesPlugin.class, "clear_default");
    public static final String CLEAR_DEFAULT_PARAMETER_ID_NAME = "Clear Default";
    public static final String ACTIONS_GROUP_NAME = "actions";

    private static final String NO_DEFAULT = "<None>";

    private final List<String> deletedTemplates = new ArrayList<>();

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);

        final PluginParameter<SingleChoiceParameterValue> templateParam = SingleChoiceParameterType.build(TEMPLATE_NAME_PARAMETER_ID);
        templateParam.setName(TEMPLATE_NAME_PARAMETER_ID_NAME);
        templateParam.setDescription(TEMPLATE_NAME_PARAMETER_ID_DESCRIPTION);
        final List<String> templateNames = new ArrayList<>(NewSchemaGraphAction.getTemplateNames().keySet());
        SingleChoiceParameterType.setOptions(templateParam, templateNames);
        templateParam.setStringValue(templateNames.isEmpty() ? "" : templateNames.get(0));
        params.addParameter(templateParam);

        final PluginParameter<ParameterValue> deleteParam = ActionParameterType.build(DELETE_TEMPLATE_PARAMETER_ID);
        params.addGroup(ACTIONS_GROUP_NAME, new PluginParametersPane.HorizontalParameterGroupLayout(false));
        deleteParam.setName(null);
        deleteParam.setDescription(null);
        deleteParam.setStringValue(DELETE_TEMPLATE_PARAMETER_ID_NAME);
        params.addParameter(deleteParam, ACTIONS_GROUP_NAME);
        params.addController(DELETE_TEMPLATE_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.NO_CHANGE) { // button pressed
                final String deleteTemplateName = parameters.get(TEMPLATE_NAME_PARAMETER_ID).getStringValue();
                if (StringUtils.isNotBlank(deleteTemplateName)) {
                    templateNames.remove(deleteTemplateName);
                    deletedTemplates.add(deleteTemplateName);
                    SingleChoiceParameterType.setOptions(templateParam, templateNames);
                    templateParam.setStringValue("");
                    if (deleteTemplateName.equals(parameters.get(CURRENT_DEFAULT_PARAMETER_ID).getStringValue())) {
                        parameters.get(CURRENT_DEFAULT_PARAMETER_ID).setStringValue(NO_DEFAULT);
                    }
                }
            }
        });

        final PluginParameter<ParameterValue> defaultParam = ActionParameterType.build(DEFAULT_TEMPLATE_PARAMETER_ID);
        defaultParam.setName(null);
        defaultParam.setDescription(null);
        defaultParam.setStringValue(DEFAULT_TEMPLATE_PARAMETER_ID_NAME);
        params.addParameter(defaultParam, ACTIONS_GROUP_NAME);
        params.addController(DEFAULT_TEMPLATE_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.NO_CHANGE) { // button pressed
                final String chosenTemplate = parameters.get(TEMPLATE_NAME_PARAMETER_ID).getStringValue();
                if (StringUtils.isNotBlank(chosenTemplate)) {
                    parameters.get(CURRENT_DEFAULT_PARAMETER_ID).setStringValue(chosenTemplate);
                }
            }
        });

        final PluginParameter<ParameterValue> clearParam = ActionParameterType.build(CLEAR_DEFAULT_PARAMETER_ID);
        clearParam.setName(null);
        clearParam.setDescription(null);
        clearParam.setStringValue(CLEAR_DEFAULT_PARAMETER_ID_NAME);
        params.addParameter(clearParam, ACTIONS_GROUP_NAME);
        params.addController(CLEAR_DEFAULT_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.NO_CHANGE) { // button pressed
                parameters.get(CURRENT_DEFAULT_PARAMETER_ID).setStringValue(NO_DEFAULT);
            }
        });

        final PluginParameter<StringParameterValue> currentDefaultParam = StringParameterType.build(CURRENT_DEFAULT_PARAMETER_ID);
        currentDefaultParam.setName(CURRENT_DEFAULT_PARAMETER_ID_NAME);
        StringParameterType.setIsLabel(currentDefaultParam, true);
        final String currentDefault = prefs.get(ApplicationPreferenceKeys.DEFAULT_TEMPLATE, ApplicationPreferenceKeys.DEFAULT_TEMPLATE_DEFAULT);
        currentDefaultParam.setStringValue(Objects.equals(ApplicationPreferenceKeys.DEFAULT_TEMPLATE_DEFAULT, currentDefault) ? NO_DEFAULT : currentDefault);
        params.addParameter(currentDefaultParam);

        return params;
    }

    @Override
    public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);

        final Map<String, String> templates = NewSchemaGraphAction.getTemplateNames();
        deletedTemplates.forEach(template -> {
            final File newFile = new File(NewSchemaGraphAction.getTemplateDirectory(), templates.get(template) + NewSchemaGraphAction.FORWARD_SLASH + template);
            try {
                Files.delete(Path.of(newFile.getPath()));
            } catch (final IOException ex) {
                //TODO: Handle case where file not successfully deleted
            }
        });

        final String defaultTemplate = parameters.getStringValue(CURRENT_DEFAULT_PARAMETER_ID);
        if (NO_DEFAULT.equals(defaultTemplate)) {
            prefs.remove(ApplicationPreferenceKeys.DEFAULT_TEMPLATE);
        } else {
            prefs.put(ApplicationPreferenceKeys.DEFAULT_TEMPLATE, defaultTemplate);
        }
    }

}
