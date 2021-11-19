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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Paste a graph {@link RecordStore} into the current graph.
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = Plugin.class)
@Messages({
    "PasteGraphPlugin=Paste Graph",
    "# {0} - Graph to paste",
    "MSG_Param=Must provide {0} argument",
    "MSG_VxKeys=Node keys don't match",
    "MSG_TxKeys=Transaction keys don't match",
    "MSG_VxAttrs=Node attributes don't match",
    "MSG_TxAttrs=Transaction attributes don't match"
})
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
public final class PasteGraphPlugin extends SimpleEditPlugin {

    // The graph being pasted from the paste buffer.
    public static final String RECORDSTORE_PARAMETER_ID = PluginParameter.buildId(PasteGraphPlugin.class, "recordstore");
    public static final String OUT_VX_PASTED_PARAMETER_ID = PluginParameter.buildId(PasteGraphPlugin.class, "vertex_pasted");
    public static final String OUT_TX_PASTED_PARAMETER_ID = PluginParameter.buildId(PasteGraphPlugin.class, "transaction_pasted");


    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<ObjectParameterValue> pasterParam = ObjectParameterType.build(RECORDSTORE_PARAMETER_ID);
        pasterParam.setName("RecordStore");
        pasterParam.setDescription("The RecordStore object to be pasted onto the current graph");
        parameters.addParameter(pasterParam);

        final PluginParameter<ObjectParameterValue> outVxPastedParam = ObjectParameterType.build(OUT_VX_PASTED_PARAMETER_ID);
        outVxPastedParam.setName("Vertex Ids pasted");
        outVxPastedParam.setDescription("A set of the vertex ids pasted (output parameter)");
        parameters.addParameter(outVxPastedParam);

        final PluginParameter<ObjectParameterValue> outTxPastedParam = ObjectParameterType.build(OUT_TX_PASTED_PARAMETER_ID);
        outTxPastedParam.setName("Transaction Ids pasted");
        outTxPastedParam.setDescription("A set of the transaction ids pasted (output parameter)");
        parameters.addParameter(outTxPastedParam);
        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final Object paramPaster = parameters.getParameters().get(RECORDSTORE_PARAMETER_ID).getObjectValue();
        if (!(paramPaster instanceof RecordStore)) {
            throw new IllegalArgumentException(Bundle.MSG_Param(RECORDSTORE_PARAMETER_ID));
        }
        final RecordStore paster = ((RecordStore) paramPaster);

        synchronized (paster) {
            GraphRecordStoreUtilities.addRecordStoreToGraph(wg, paster, false, false, null);
        }

        ConstellationLoggerHelper.createPropertyBuilder(
                this,
                paster.getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                ConstellationLoggerHelper.SUCCESS
        );
    }
}
