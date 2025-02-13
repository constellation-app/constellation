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
package au.gov.asd.tac.constellation.graph.interaction.plugins.draw;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
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
 * Create a new transaction.
 *
 * @author procyon
 */
@ServiceProvider(service = Plugin.class)
@Messages("CreateTransactionPlugin=Create Transaction")
@PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
public final class CreateTransactionPlugin extends SimpleEditPlugin {

    public static final String SOURCE_PARAMETER_ID = PluginParameter.buildId(CreateTransactionPlugin.class, "source");
    public static final String DESTINATION_PARAMETER_ID = PluginParameter.buildId(CreateTransactionPlugin.class, "destination");
    public static final String DIRECTED_PARAMETER_ID = PluginParameter.buildId(CreateTransactionPlugin.class, "directed");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> sourceParam = IntegerParameterType.build(SOURCE_PARAMETER_ID);
        sourceParam.setName("Source");
        sourceParam.setDescription("The source vertex id");
        sourceParam.setIntegerValue(0);
        parameters.addParameter(sourceParam);

        final PluginParameter<IntegerParameterValue> destinationParam = IntegerParameterType.build(DESTINATION_PARAMETER_ID);
        destinationParam.setName("Destination");
        destinationParam.setDescription("The destination vertex id");
        destinationParam.setIntegerValue(0);
        parameters.addParameter(destinationParam);

        final PluginParameter<BooleanParameterValue> directedParam = BooleanParameterType.build(DIRECTED_PARAMETER_ID);
        directedParam.setName("Directed");
        directedParam.setDescription("If True then make it a directed transaction, default is True");
        directedParam.setBooleanValue(true);
        parameters.addParameter(directedParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int source = parameters.getParameters().get(SOURCE_PARAMETER_ID).getIntegerValue();
        final int destination = parameters.getParameters().get(DESTINATION_PARAMETER_ID).getIntegerValue();
        final boolean directed = parameters.getParameters().get(DIRECTED_PARAMETER_ID).getBooleanValue();

        final int txLayerAttrId = LayersConcept.TransactionAttribute.LAYER_MASK.get(graph);
        final int graphLayerAttrId = LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.get(graph);

        final int txId = graph.addTransaction(source, destination, directed);

        // add layer mask attributes
        if (graphLayerAttrId != Graph.NOT_FOUND && txLayerAttrId != Graph.NOT_FOUND) {
            int layer = graph.getIntValue(graphLayerAttrId, 0);
            layer = layer == 1 ? 1 : layer | (1 << 0);
            graph.setIntValue(txLayerAttrId, txId, layer);
        }
        graph.getSchema().newTransaction(graph, txId);
    }
}
