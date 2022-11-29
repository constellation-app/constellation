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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * This plugin sleeps for a number of seconds; that's it.
 * <p>
 * Used for testing the RunPlugins service, for example.
 *
 * @author algol
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@Messages("SleepReadPlugin=Sleep Read")
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.EXPERIMENTAL, PluginTags.DEVELOPER})
public class SleepReadPlugin extends SimpleReadPlugin {

    public static final String SECONDS_PARAMETER_ID = PluginParameter.buildId(SleepReadPlugin.class, "seconds");

    @Override
    public String getDescription() {
        return "Sleep while reading a graph for a number of seconds.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<IntegerParameterValue> secondsParam = IntegerParameterType.build(SECONDS_PARAMETER_ID);
        secondsParam.setName("Seconds to sleep");
        secondsParam.setDescription("The number of seconds to sleep on the graph");
        secondsParam.setIntegerValue(10);
        IntegerParameterType.setMinimum(secondsParam, 0);
        params.addParameter(secondsParam);

        return params;
    }

    @Override
    protected void read(GraphReadMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final int nSecs = parameters.getIntegerValue(SECONDS_PARAMETER_ID);
        interaction.setProgress(0, 0, String.format("Sleeping (read) for %d second%s...", nSecs, nSecs == 1 ? "" : "s"), true);

        Thread.sleep(nSecs * 1000L);

        interaction.setProgress(1, 0, String.format("Slept for %d second%s.", nSecs, nSecs == 1 ? "" : "s"), true);
    }
}
