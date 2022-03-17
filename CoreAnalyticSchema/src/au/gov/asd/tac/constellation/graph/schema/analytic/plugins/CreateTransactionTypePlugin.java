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
package au.gov.asd.tac.constellation.graph.schema.analytic.plugins;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.util.Map;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Create a new transaction type on the fly.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
@Messages("CreateTransactionTypePlugin=Create Transaction Type")
public class CreateTransactionTypePlugin extends SimpleEditPlugin {

    public static final String NAME_PARAMETER_ID = PluginParameter.buildId(CreateTransactionTypePlugin.class, "name");
    public static final String DESCRIPTION_PARAMETER_ID = PluginParameter.buildId(CreateTransactionTypePlugin.class, "description");
    public static final String COLOR_PARAMETER_ID = PluginParameter.buildId(CreateTransactionTypePlugin.class, "color");
    public static final String LINE_STYLE_PARAMETER_ID = PluginParameter.buildId(CreateTransactionTypePlugin.class, "line_style");
    public static final String DIRECTED_PARAMETER_ID = PluginParameter.buildId(CreateTransactionTypePlugin.class, "directed");
    public static final String SUPER_TYPE_PARAMETER_ID = PluginParameter.buildId(CreateTransactionTypePlugin.class, "super_type");
    public static final String OVERRIDDEN_TYPE_PARAMETER_ID = PluginParameter.buildId(CreateTransactionTypePlugin.class, "overridden_type");
    public static final String INCOMPLETE_PARAMETER_ID = PluginParameter.buildId(CreateTransactionTypePlugin.class, "incomplete");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(NAME_PARAMETER_ID);
        nameParam.setName("Name");
        nameParam.setDescription("The name of the new transaction type");
        nameParam.setStringValue("My new type");
        params.addParameter(nameParam);

        final PluginParameter<StringParameterValue> descriptionParam = StringParameterType.build(DESCRIPTION_PARAMETER_ID);
        descriptionParam.setName("Description");
        descriptionParam.setDescription("The description of the new transaction type");
        descriptionParam.setStringValue("Description of my new type");
        params.addParameter(descriptionParam);

        final PluginParameter<ColorParameterType.ColorParameterValue> colorParam = ColorParameterType.build(COLOR_PARAMETER_ID);
        colorParam.setName("Color");
        colorParam.setDescription("The color of the new transaction type");
        colorParam.setStringValue(ConstellationColor.RED.getName());
        params.addParameter(colorParam);

        final PluginParameter<StringParameterValue> lineStyleParam = StringParameterType.build(LINE_STYLE_PARAMETER_ID);
        lineStyleParam.setName("Line Style");
        lineStyleParam.setDescription("The line style of the new transaction type");
        lineStyleParam.setStringValue(LineStyle.SOLID.toString());
        params.addParameter(lineStyleParam);

        final PluginParameter<BooleanParameterType.BooleanParameterValue> directedParam = BooleanParameterType.build(DIRECTED_PARAMETER_ID);
        directedParam.setName("Directed");
        directedParam.setDescription("Is the transaction directed?");
        directedParam.setBooleanValue(true);
        params.addParameter(directedParam);

        final PluginParameter<StringParameterValue> superTypeParam = StringParameterType.build(SUPER_TYPE_PARAMETER_ID);
        superTypeParam.setName("Super Type");
        superTypeParam.setDescription("The name of the super type of the new transaction type");
        params.addParameter(superTypeParam);

        final PluginParameter<StringParameterValue> overriddenTypeParam = StringParameterType.build(OVERRIDDEN_TYPE_PARAMETER_ID);
        overriddenTypeParam.setName("Overridden Type");
        overriddenTypeParam.setDescription("The name of the overridden type of the new transaction type");
        params.addParameter(overriddenTypeParam);

        final PluginParameter<BooleanParameterType.BooleanParameterValue> incompleteParam = BooleanParameterType.build(INCOMPLETE_PARAMETER_ID);
        incompleteParam.setName("Incomplete");
        incompleteParam.setDescription("Is the new transaction type incomplete?");
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

        final String lsName = parameters.getStringValue(LINE_STYLE_PARAMETER_ID);
        final LineStyle lineStyle = LineStyle.valueOf(lsName);

        final boolean directed = parameters.getBooleanValue(DIRECTED_PARAMETER_ID);

        final String stype = parameters.getStringValue(SUPER_TYPE_PARAMETER_ID);
        final SchemaTransactionType superType = stype != null ? SchemaTransactionTypeUtilities.getType(stype) : null;

        final String otype = parameters.getStringValue(OVERRIDDEN_TYPE_PARAMETER_ID);
        final SchemaTransactionType overridenType = otype != null ? SchemaTransactionTypeUtilities.getType(otype) : null;

        final boolean incomplete = parameters.getBooleanValue(INCOMPLETE_PARAMETER_ID);

        final Map<String, String> properties = null;

        final SchemaTransactionType stt = new SchemaTransactionType(
                name, description,
                color, lineStyle, directed,
                superType, overridenType,
                properties, incomplete);

        SchemaTransactionTypeUtilities.addCustomType(stt, true);
    }
}
