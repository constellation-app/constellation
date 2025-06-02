/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.visual.plugins.hop;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Hop Out Plugin
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("HopOutPlugin=Hop Out")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class HopOutPlugin extends SimpleEditPlugin {

    public static final String HOPS_PARAMETER_ID = PluginParameter.buildId(HopOutPlugin.class, "hops");
    public static final String OUTGOING_PARAMETER_ID = PluginParameter.buildId(HopOutPlugin.class, "outgoing");
    public static final String INCOMING_PARAMETER_ID = PluginParameter.buildId(HopOutPlugin.class, "incoming");
    public static final String UNDIRECTED_PARAMETER_ID = PluginParameter.buildId(HopOutPlugin.class, "undirected");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> hopsParam = IntegerParameterType.build(HOPS_PARAMETER_ID);
        hopsParam.setName("Hops");
        hopsParam.setDescription("The number of hops. 0 is hop out half, 1 is hop out 1 and 2 to hop out full.");
        hopsParam.setIntegerValue(HopUtilities.HOP_OUT_HALF);
        parameters.addParameter(hopsParam);

        final PluginParameter<BooleanParameterValue> outgoingParam = BooleanParameterType.build(OUTGOING_PARAMETER_ID);
        outgoingParam.setName("Outgoing");
        outgoingParam.setDescription("True if outgoing transactions should be included, default is True");
        outgoingParam.setBooleanValue(true);
        parameters.addParameter(outgoingParam);

        final PluginParameter<BooleanParameterValue> incomingParam = BooleanParameterType.build(INCOMING_PARAMETER_ID);
        incomingParam.setName("Incoming");
        incomingParam.setDescription("True is incoming transactions should be included, default is True");
        incomingParam.setBooleanValue(true);
        parameters.addParameter(incomingParam);

        final PluginParameter<BooleanParameterValue> undirectedParam = BooleanParameterType.build(UNDIRECTED_PARAMETER_ID);
        undirectedParam.setName("Undirected");
        undirectedParam.setDescription("True is undirected transactions should be included, default is True");
        undirectedParam.setBooleanValue(true);
        parameters.addParameter(undirectedParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int hops = parameters.getParameters().get(HOPS_PARAMETER_ID).getIntegerValue();
        final boolean outgoing = parameters.getParameters().get(OUTGOING_PARAMETER_ID).getBooleanValue();
        final boolean incoming = parameters.getParameters().get(INCOMING_PARAMETER_ID).getBooleanValue();
        final boolean undirected = parameters.getParameters().get(UNDIRECTED_PARAMETER_ID).getBooleanValue();

        switch (hops) {
            case HopUtilities.HOP_OUT_HALF -> HopUtilities.hopOutHalf(graph, outgoing, incoming, undirected);
            case HopUtilities.HOP_OUT_ONE -> HopUtilities.hopOutOne(graph, outgoing, incoming, undirected);
            case HopUtilities.HOP_OUT_FULL -> HopUtilities.hopOutFull(graph, outgoing, incoming, undirected);
            default -> {
                // Do nothing
            }
        }
    }
}
