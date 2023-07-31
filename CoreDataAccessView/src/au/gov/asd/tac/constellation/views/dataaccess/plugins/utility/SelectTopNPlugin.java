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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.utility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Select the top n based on the transaction count
 *
 * @author arcturus
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@Messages("SelectTopNPlugin=Select Top N")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class SelectTopNPlugin extends SimpleQueryPlugin implements DataAccessPlugin {

    private static final Logger LOGGER = Logger.getLogger(SelectTopNPlugin.class.getName());

    public static final String MODE_PARAMETER_ID = PluginParameter.buildId(SelectTopNPlugin.class, "mode");
    public static final String TYPE_CATEGORY_PARAMETER_ID = PluginParameter.buildId(SelectTopNPlugin.class, "type_category");
    public static final String TYPE_PARAMETER_ID = PluginParameter.buildId(SelectTopNPlugin.class, "type");
    public static final String LIMIT_PARAMETER_ID = PluginParameter.buildId(SelectTopNPlugin.class, "limit");

    public static final String NODE = "Node";
    public static final String TRANSACTION = "Transaction";

    private static final String MISSING_PROPERTY_FORMAT = "%s property is missing";

    @Override
    public String getType() {
        return DataAccessPluginCoreType.UTILITY;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public String getDescription() {
        return "Select the top N on your graph using types";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final List<String> modes = new ArrayList<>();
        modes.add(NODE);
        modes.add(TRANSACTION);

        final PluginParameter<SingleChoiceParameterValue> modeParameter = SingleChoiceParameterType.build(MODE_PARAMETER_ID);
        modeParameter.setName("Mode");
        modeParameter.setDescription("Select either the Node or Transaction mode");
        modeParameter.setRequired(true);
        SingleChoiceParameterType.setOptions(modeParameter, modes);
        params.addParameter(modeParameter);

        final PluginParameter<SingleChoiceParameterValue> typeCategoryParameter = SingleChoiceParameterType.build(TYPE_CATEGORY_PARAMETER_ID);
        typeCategoryParameter.setName("Type Category");
        typeCategoryParameter.setDescription("The high level type category");
        typeCategoryParameter.setRequired(true);
        params.addParameter(typeCategoryParameter);

        final PluginParameter<MultiChoiceParameterValue> typeParameter = MultiChoiceParameterType.build(TYPE_PARAMETER_ID);
        typeParameter.setName("Specific Types");
        typeParameter.setDescription("The specific types to include when calculating the top N");
        typeParameter.setRequired(true);
        params.addParameter(typeParameter);

        final PluginParameter<IntegerParameterValue> limitParameter = IntegerParameterType.build(LIMIT_PARAMETER_ID);
        limitParameter.setName("Limit");
        limitParameter.setDescription("The limit, default being 10");
        limitParameter.setIntegerValue(10);
        params.addParameter(limitParameter);

        params.addController(MODE_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final String mode = parameters.get(MODE_PARAMETER_ID).getStringValue();
                if (mode != null) {
                    final List<String> types = new ArrayList<>();
                    switch (mode) {
                        case NODE:
                            for (final SchemaVertexType type : SchemaVertexTypeUtilities.getTypes()) {
                                if (type.isTopLevelType()) {
                                    types.add(type.getName());
                                }
                            }
                            break;
                        case TRANSACTION:
                            for (final SchemaTransactionType type : SchemaTransactionTypeUtilities.getTypes()) {
                                if (type.isTopLevelType()) {
                                    types.add(type.getName());
                                }
                            }
                            break;
                        default:
                            LOGGER.severe("Invalid mode provided. Mode values accepted are " + NODE + " or " + TRANSACTION);
                    }

                    @SuppressWarnings("unchecked") //TYPE_CATEGORY_PARAMETER will always be of type SingleChoiceParameter
                    final PluginParameter<SingleChoiceParameterValue> typeCategoryParamter = (PluginParameter<SingleChoiceParameterValue>) parameters.get(TYPE_CATEGORY_PARAMETER_ID);
                    types.sort(String::compareTo);
                    SingleChoiceParameterType.setOptions(typeCategoryParamter, types);
                    if (!types.isEmpty()) {
                        SingleChoiceParameterType.setChoice(typeCategoryParamter, types.get(0));
                    }
                }
            }
        });

        params.addController(TYPE_CATEGORY_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final String mode = parameters.get(MODE_PARAMETER_ID).getStringValue();
                final String typeCategory = parameters.get(TYPE_CATEGORY_PARAMETER_ID).getStringValue();
                if (mode != null && typeCategory != null) {

                    final List<String> types = new ArrayList<>();
                    switch (mode) {
                        case NODE:
                            final SchemaVertexType typeCategoryVertexType = SchemaVertexTypeUtilities.getType(typeCategory);
                            for (final SchemaVertexType type : SchemaVertexTypeUtilities.getTypes()) {
                                if (type.getSuperType().equals(typeCategoryVertexType)) {
                                    types.add(type.getName());
                                }
                            }
                            break;
                        case TRANSACTION:
                            final SchemaTransactionType typeCategoryTransactionType = SchemaTransactionTypeUtilities.getType(typeCategory);
                            for (final SchemaTransactionType type : SchemaTransactionTypeUtilities.getTypes()) {
                                if (type.getSuperType().equals(typeCategoryTransactionType)) {
                                    types.add(type.getName());
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    // update the sub level types
                    @SuppressWarnings("unchecked") //TYPE_PARAMETER will always be of type MultiChoiceParameter
                    final PluginParameter<MultiChoiceParameterValue> typeParamter = (PluginParameter<MultiChoiceParameterValue>) parameters.get(TYPE_PARAMETER_ID);
                    types.sort(String::compareTo);
                    MultiChoiceParameterType.setOptions(typeParamter, types);
                    MultiChoiceParameterType.setChoices(typeParamter, types);
                }
            }
        });

        return params;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final String mode = parameters.getParameters().get(MODE_PARAMETER_ID).getStringValue();
        final String typeCategory = parameters.getParameters().get(TYPE_CATEGORY_PARAMETER_ID).getStringValue();
        final List<String> subTypes = parameters.getParameters().get(TYPE_PARAMETER_ID).getMultiChoiceValue().getChoices();
        final int limit = parameters.getParameters().get(LIMIT_PARAMETER_ID).getIntegerValue();

        if (mode == null || (!mode.equals(NODE) && !mode.equals(TRANSACTION))) {
            throw new PluginException(PluginNotificationLevel.ERROR, "Invalid mode value provided");
        }

        if (typeCategory == null) {
            throw new PluginException(PluginNotificationLevel.ERROR, "Select a type category");
        }

        if (subTypes.isEmpty()) {
            throw new PluginException(PluginNotificationLevel.ERROR, "Select some types to perform the calculation");
        }

        final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.get(graph);
        if (vertexLabelAttribute == Graph.NOT_FOUND) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(MISSING_PROPERTY_FORMAT, VisualConcept.VertexAttribute.LABEL.getName()));
        }

        final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (vertexSelectedAttribute == Graph.NOT_FOUND) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(MISSING_PROPERTY_FORMAT, VisualConcept.VertexAttribute.SELECTED.getName()));
        }

        final int vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.get(graph);
        if (vertexTypeAttribute == Graph.NOT_FOUND) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(MISSING_PROPERTY_FORMAT, AnalyticConcept.VertexAttribute.TYPE.getName()));
        }

        final int transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.get(graph);
        if (transactionTypeAttribute == Graph.NOT_FOUND) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(MISSING_PROPERTY_FORMAT, AnalyticConcept.TransactionAttribute.TYPE.getName()));
        }

        // make a set of the highlighted nodes
        final Set<Integer> selectedNodes = new HashSet<>();
        final int vertexCount = graph.getVertexCount();
        for (int position = 0; position < vertexCount; position++) {
            final int vxId = graph.getVertex(position);
            if (graph.getBooleanValue(vertexSelectedAttribute, vxId)) {
                selectedNodes.add(vxId);
            }
        }

        if (selectedNodes.isEmpty()) {
            throw new PluginException(PluginNotificationLevel.ERROR, "Select at least 1 node");
        }

        // calculate the occurrences of destination vertices
        int txId;
        int sourceVxId;
        int destinationVxId;
        int targetVxId;
        int step = 0;
        SchemaVertexType destinationVertexType;
        SchemaTransactionType transactionType;
        final Map<Integer, Integer> occurrences = new HashMap<>();
        int randomNumber = 0;
        for (final Integer vxId : selectedNodes) {
            final String label = graph.getStringValue(vertexLabelAttribute, vxId);
            interaction.setProgress(++step, selectedNodes.size(), String.format("Calculating top %s for %s", limit, label), true);

            final int transactionCount = graph.getVertexTransactionCount(vxId);
            for (int position = 0; position < transactionCount; position++) {
                txId = graph.getVertexTransaction(vxId, position);
                sourceVxId = graph.getTransactionSourceVertex(txId);
                destinationVxId = graph.getTransactionDestinationVertex(txId);
                targetVxId = vxId == sourceVxId ? destinationVxId : sourceVxId;

                switch (mode) {
                    case NODE:
                        destinationVertexType = graph.getObjectValue(vertexTypeAttribute, targetVxId);
                        if (destinationVertexType != null && subTypes.contains(destinationVertexType.getName())) {
                            if (!occurrences.containsKey(targetVxId)) {
                                occurrences.put(targetVxId, 0);
                            }

                            occurrences.put(targetVxId, occurrences.get(targetVxId) + 1);
                        }
                        break;
                    case TRANSACTION:
                        transactionType = graph.getObjectValue(transactionTypeAttribute, txId);
                        if (transactionType != null && subTypes.contains(transactionType.getName())) {
                            if (!occurrences.containsKey(targetVxId)) {
                                occurrences.put(targetVxId, 0);
                            }

                            occurrences.put(targetVxId, occurrences.get(targetVxId) + 1);
                        }
                        break;
                    default:
                        break;
                }
            }

            // make a map sorted by the count in descending order
            final LinkedHashMap<Integer, Integer> sortedMap = occurrences.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            sortedMap.keySet().stream().limit(limit).forEach(id -> graph.setBooleanValue(vertexSelectedAttribute, id, true));
            
            //Note: This plugin does not factor in currently selected nodes to its Top N selection.
            interaction.setProgress(1, 0, "Selected " + (sortedMap.size() < limit ? sortedMap.size() : limit) + " nodes.", true);
        }
    }
}
