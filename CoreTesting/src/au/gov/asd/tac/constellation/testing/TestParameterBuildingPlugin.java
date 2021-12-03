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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType.LocalDateParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;

/**
 * A plugin to the framework's GUI capability.
 *
 * @author algol
 */
@Messages("TestParameterBuildingPlugin=Test Parameter Building")
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.EXPERIMENTAL, PluginTags.DEVELOPER, PluginTags.CREATE})
public class TestParameterBuildingPlugin extends SimpleQueryPlugin {

    public static final String LOCALDATE_PARAMETER_ID = PluginParameter.buildId(TestParameterBuildingPlugin.class, "localdate");
    public static final String CHOICE_PARAMETER_ID = PluginParameter.buildId(TestParameterBuildingPlugin.class, "choice");
    public static final String INT_PARAMETER_ID = PluginParameter.buildId(TestParameterBuildingPlugin.class, "int");
    public static final String FLOAT_PARAMETER_ID = PluginParameter.buildId(TestParameterBuildingPlugin.class, "float");
    public static final String BOOLEAN_PARAMETER_ID = PluginParameter.buildId(TestParameterBuildingPlugin.class, "boolean");

    public static final String SOME_CHOICE_NAME = "Make a Choice";
    public static final String SOME_CHOICE_DESCRIPTION = "A parameter where you can make a choice";
    public static final String SOME_INT_NAME = "Some Integer";
    public static final String SOME_INT_DESCRIPTION = "Pick Some Integer";
    public static final String FLOAT_NAME = "Some Float";
    public static final String FLOAT_DESCRIPTION = "Enter some float between 0 and 1.";
    public static final String SOME_BOOLEAN_NAME = "Some Boolean";
    public static final String SOME_BOOLEAN_DESCRIPTION = "If true, it is so!";

    private static final Logger LOGGER = Logger.getLogger(TestParameterBuildingPlugin.class.getName());

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) { //    protected void query(final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException
        final Map<String, PluginParameter<?>> pmap = parameters.getParameters();
        pmap.entrySet().stream().forEach(entry
                -> LOGGER.log(Level.INFO, "{0}: {1}", new Object[]{entry.getKey(), entry.getValue().getStringValue()}));
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> fupChoiceParam = SingleChoiceParameterType.build(CHOICE_PARAMETER_ID);
        fupChoiceParam.setName(SOME_CHOICE_NAME);
        fupChoiceParam.setDescription(SOME_CHOICE_DESCRIPTION);
        SingleChoiceParameterType.setOptions(fupChoiceParam, Arrays.asList("Choice 1", "Choice 2", "Choice 3"));
        SingleChoiceParameterType.setChoice(fupChoiceParam, "Choice 1");
        params.addParameter(fupChoiceParam);

        final PluginParameter<LocalDateParameterValue> ldParam = LocalDateParameterType.build(LOCALDATE_PARAMETER_ID);
        ldParam.setName("Date");
        ldParam.setDescription("Pick a day");
        params.addParameter(ldParam);

        final PluginParameter<IntegerParameterValue> lenParam = IntegerParameterType.build(INT_PARAMETER_ID);
        lenParam.setName(SOME_INT_NAME);
        lenParam.setDescription(SOME_INT_DESCRIPTION);
        lenParam.setIntegerValue(5);
        params.addParameter(lenParam);

        final PluginParameter<FloatParameterValue> thresholdParam = FloatParameterType.build(FLOAT_PARAMETER_ID);
        thresholdParam.setName(FLOAT_NAME);
        thresholdParam.setDescription(FLOAT_DESCRIPTION);
        thresholdParam.setFloatValue(0F);
        FloatParameterType.setMinimum(thresholdParam, 0);
        FloatParameterType.setMaximum(thresholdParam, 1);
        FloatParameterType.setStep(thresholdParam, 0.1F);
        params.addParameter(thresholdParam);

        final PluginParameter<BooleanParameterValue> caseParam = BooleanParameterType.build(BOOLEAN_PARAMETER_ID);
        caseParam.setName(SOME_BOOLEAN_NAME);
        caseParam.setDescription(SOME_BOOLEAN_DESCRIPTION);
        params.addParameter(caseParam);

        for (int i = 0; i < 2; i++) {
            final PluginParameter<StringParameterValue> text = StringParameterType.build("text" + i);
            text.setName("Some text " + i);
            text.setDescription("Type some text into this thing");
            text.setStringValue("Value " + i);
            params.addParameter(text);
        }

        return params;
    }
}
