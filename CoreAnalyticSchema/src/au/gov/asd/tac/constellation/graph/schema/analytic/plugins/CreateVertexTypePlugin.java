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
package au.gov.asd.tac.constellation.graph.schema.analytic.plugins;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.CharacterIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import java.util.Map;
import java.util.regex.Pattern;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Create a new vertex type on the fly.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
@Messages("CreateVertexTypePlugin=Create Vertex Type")
public class CreateVertexTypePlugin extends SimpleEditPlugin {

    public static final String NAME_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "name");
    public static final String DESCRIPTION_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "description");
    public static final String COLOR_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "color");
    public static final String FG_ICON_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "fg_icon");
    public static final String BG_ICON_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "bg_icon");
    public static final String DETECTION_REGEX_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "detection_regex");
    public static final String VALIDATION_REGEX_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "validation_regex");
    public static final String SUPER_TYPE_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "super_type");
    public static final String OVERRIDDEN_TYPE_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "overridden_type");
    public static final String INCOMPLETE_PARAMETER_ID = PluginParameter.buildId(CreateVertexTypePlugin.class, "incomplete");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(NAME_PARAMETER_ID);
        nameParam.setName("Name");
        nameParam.setDescription("The name of the new vertex type");
        nameParam.setRequired(true);
        params.addParameter(nameParam);

        final PluginParameter<StringParameterValue> descriptionParam = StringParameterType.build(DESCRIPTION_PARAMETER_ID);
        descriptionParam.setName("Description");
        descriptionParam.setDescription("The description of the new vertex type");
        descriptionParam.setRequired(true);
        params.addParameter(descriptionParam);

        final PluginParameter<ColorParameterValue> colorParam = ColorParameterType.build(COLOR_PARAMETER_ID);
        colorParam.setName("Color");
        colorParam.setDescription("The color of the new vertex type");
        colorParam.setStringValue(ConstellationColor.GREY.getName());
        params.addParameter(colorParam);

        final PluginParameter<StringParameterValue> fgIconParam = StringParameterType.build(FG_ICON_PARAMETER_ID);
        fgIconParam.setName("Foreground Icon");
        fgIconParam.setDescription("The name of the foreground icon of the new vertex type");
        fgIconParam.setStringValue(CharacterIconProvider.CHAR_003F.getExtendedName());
        params.addParameter(fgIconParam);

        final PluginParameter<StringParameterValue> bgIconParam = StringParameterType.build(BG_ICON_PARAMETER_ID);
        bgIconParam.setName("Background Icon");
        bgIconParam.setDescription("The name of the background icon of the new vertex type");
        bgIconParam.setStringValue(DefaultIconProvider.FLAT_SQUARE.getExtendedName());
        params.addParameter(bgIconParam);

        final PluginParameter<StringParameterValue> detectionRegexParam = StringParameterType.build(DETECTION_REGEX_PARAMETER_ID);
        detectionRegexParam.setName("Detection Regular Expression");
        detectionRegexParam.setDescription("The detection regular expression (case of the new vertex type");
        params.addParameter(detectionRegexParam);

        final PluginParameter<StringParameterValue> validationRegexParam = StringParameterType.build(VALIDATION_REGEX_PARAMETER_ID);
        validationRegexParam.setName("Validation Regular Expression");
        validationRegexParam.setDescription("The detection regular expression of the new vertex type");
        params.addParameter(validationRegexParam);

        final PluginParameter<StringParameterValue> superTypeParam = StringParameterType.build(SUPER_TYPE_PARAMETER_ID);
        superTypeParam.setName("Super Type");
        superTypeParam.setDescription("The name of the super type of the new vertex type");
        params.addParameter(superTypeParam);

        final PluginParameter<StringParameterValue> overriddenTypeParam = StringParameterType.build(OVERRIDDEN_TYPE_PARAMETER_ID);
        overriddenTypeParam.setName("Overridden Type");
        overriddenTypeParam.setDescription("The name of the overridden type of the new vertex type");
        params.addParameter(overriddenTypeParam);

        final PluginParameter<BooleanParameterValue> incompleteParam = BooleanParameterType.build(INCOMPLETE_PARAMETER_ID);
        incompleteParam.setName("Incomplete");
        incompleteParam.setDescription("Is the new vertex type incomplete?");
        params.addParameter(incompleteParam);

        return params;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final String name = parameters.getStringValue(NAME_PARAMETER_ID);
        if (name == null) {
            throw new IllegalArgumentException("A name must be supplied");
        }

        final String description = parameters.getStringValue(DESCRIPTION_PARAMETER_ID);
        if (description == null) {
            throw new IllegalArgumentException("A description must be supplied");
        }

        final ConstellationColor color = parameters.getColorValue(COLOR_PARAMETER_ID);

        final String fgIconName = parameters.getStringValue(FG_ICON_PARAMETER_ID);
        final ConstellationIcon foregroundIcon = IconManager.getIcon(fgIconName);

        final String bgIconName = parameters.getStringValue(BG_ICON_PARAMETER_ID);
        final ConstellationIcon backgroundIcon = IconManager.getIcon(bgIconName);

        final String dregex = parameters.getStringValue(DETECTION_REGEX_PARAMETER_ID);
        final Pattern detectionRegex = dregex != null ? Pattern.compile(dregex, Pattern.CASE_INSENSITIVE) : null;

        final String vregex = parameters.getStringValue(VALIDATION_REGEX_PARAMETER_ID);
        final Pattern validationRegex = vregex != null ? Pattern.compile(vregex, Pattern.CASE_INSENSITIVE) : null;

        final String stype = parameters.getStringValue(SUPER_TYPE_PARAMETER_ID);
        final SchemaVertexType superType = stype != null ? SchemaVertexTypeUtilities.getType(stype) : null;

        final String otype = parameters.getStringValue(OVERRIDDEN_TYPE_PARAMETER_ID);
        final SchemaVertexType overridenType = otype != null ? SchemaVertexTypeUtilities.getType(otype) : null;

        final boolean incomplete = parameters.getBooleanValue(INCOMPLETE_PARAMETER_ID);

        final Map<String, String> properties = null;

        final SchemaVertexType svt = new SchemaVertexType(
                name, description,
                color, foregroundIcon,
                backgroundIcon, detectionRegex,
                validationRegex, superType,
                overridenType, properties,
                incomplete);

        SchemaVertexTypeUtilities.addCustomType(svt, true);
    }
}
