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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import java.security.SecureRandom;
import java.util.Map;

/**
 * Synchronizer Plugin
 *
 * @author sirius
 */
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.DEVELOPER})
public class SynchronizerPlugin extends SimpleQueryPlugin {

    private static int NEXT_ID = 1;

    public static final String COPY_PARAMETER_ID = PluginParameter.buildId(SynchronizerPlugin.class, "copy");
    public static final String NAME_PARAMETER_ID = PluginParameter.buildId(SynchronizerPlugin.class, "name");
    private final int readTime;
    private final int queryTime;
    private final int writeTime;
    private final String name;

    private final SecureRandom random = new SecureRandom();

    public SynchronizerPlugin() {
        readTime = random.nextInt(5) + 5;
        queryTime = random.nextInt(10) + 10;
        writeTime = random.nextInt(5) + 5;

        name = "Synchronizer Plugin " + NEXT_ID++;
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();
        parameters.addParameter(StringParameterType.build(NAME_PARAMETER_ID));
        parameters.addParameter(StringParameterType.build(COPY_PARAMETER_ID));
        parameters.addController(NAME_PARAMETER_ID, (final PluginParameter<?> master, final Map<String, PluginParameter<?>> params, final ParameterChange change) -> {
            if (change == ParameterChange.VALUE) {
                @SuppressWarnings("unchecked") //COPY_PARAMETER will be of type StringParameter
                final PluginParameter<StringParameterValue> slave = (PluginParameter<StringParameterValue>) params.get(COPY_PARAMETER_ID);
                slave.setStringValue("COPY: " + master.getStringValue());
            }
        });
        return parameters;
    }

    @Override
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        for (int i = 0; i < readTime; i++) {
            Thread.sleep(1000);
            interaction.setProgress(i, readTime + queryTime + writeTime, parameters.getParameters().get(COPY_PARAMETER_ID).getStringValue() + ": Reading...", true);
        }
    }

    @Override
    protected void query(final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        for (int i = 0; i < queryTime; i++) {
            Thread.sleep(1000);
            interaction.setProgress(readTime + i, readTime + queryTime + writeTime, parameters.getParameters().get(COPY_PARAMETER_ID).getStringValue() + ": Querying...", true);
        }
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        for (int i = 0; i < writeTime; i++) {
            Thread.sleep(1000);
            interaction.setProgress(readTime + queryTime + i, readTime + queryTime + writeTime, parameters.getParameters().get(COPY_PARAMETER_ID).getStringValue() + ": Editing...", true);
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
