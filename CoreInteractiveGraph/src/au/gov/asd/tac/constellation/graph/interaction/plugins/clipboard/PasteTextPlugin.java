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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.util.BitSet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Paste text into a graph.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("PasteTextPlugin=Paste Text")
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
public class PasteTextPlugin extends SimplePlugin {

    public static final String TEXT_PARAMETER_ID = PluginParameter.buildId(PasteTextPlugin.class, "text");
    public static final String OUT_VX_PASTED_PARAMETER_ID = PluginParameter.buildId(PasteTextPlugin.class, "vertex_pasted");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> textParam = StringParameterType.build(TEXT_PARAMETER_ID);
        textParam.setName("Text");
        textParam.setDescription("The text to paste onto the current graph");
        textParam.setStringValue("");
        parameters.addParameter(textParam);

        final PluginParameter<ObjectParameterValue> outVxPastedParam = ObjectParameterType.build(OUT_VX_PASTED_PARAMETER_ID);
        outVxPastedParam.setName("Vertex Ids");
        outVxPastedParam.setDescription("A set of the vertex Ids pasted (output parameter)");
        parameters.addParameter(outVxPastedParam);

        return parameters;
    }

    @Override
    protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final BitSet vxAdded = new BitSet();

        parameters.getParameters().get(OUT_VX_PASTED_PARAMETER_ID).setObjectValue(vxAdded);
    }
}
