/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.utilities.VertexDominanceCalculator;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Split nodes based on
 *
 * @author canis_majoris
 * @author antares
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@NbBundle.Messages("SplitNodesPlugin=Split Nodes Based on Identifier")
public class SplitNodesPlugin extends RecordStoreQueryPlugin implements DataAccessPlugin {

    private static final String SOURCE_ID = GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID;
    private static final String SOURCE_IDENTIFIER = GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER;

    public static final String SPLIT_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "split");
    public static final String TRANSACTION_TYPE_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "transaction_type");
    public static final String ALL_OCCURRENCES_PARAMETER_ID = PluginParameter.buildId(SplitNodesPlugin.class, "all_occurances");

    @Override
    public String getType() {
        return DataAccessPluginCoreType.CLEAN;
    }

    @Override
    public int getPosition() {
        return 10000;
    }

    @Override
    public String getDescription() {
        return "Split nodes from character(s) in identifier.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> split = StringParameterType.build(SPLIT_PARAMETER_ID);
        split.setName("Split Character(s)");
        split.setDescription("A new term will be extracted from the first instance of this character(s) in the Identifier");
        split.setStringValue(null);
        params.addParameter(split);

        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> transactionType = SingleChoiceParameterType.build(TRANSACTION_TYPE_PARAMETER_ID);
        transactionType.setName("Transaction Type");
        transactionType.setDescription("Set the type of transaction between nodes");
        params.addParameter(transactionType);

        final PluginParameter<BooleanParameterValue> allOccurrences = BooleanParameterType.build(ALL_OCCURRENCES_PARAMETER_ID);
        allOccurrences.setName("Split on All Occurrences");
        allOccurrences.setDescription("Choose to split on all instances of the character(s) rather than just the first instance");
        allOccurrences.setBooleanValue(false);
        params.addParameter(allOccurrences);

        return params;

    }

    @Override
    public void updateParameters(Graph graph, PluginParameters parameters) {
        if (parameters != null && parameters.getParameters() != null) {
            final PluginParameter transactionType = parameters.getParameters().get(TRANSACTION_TYPE_PARAMETER_ID);
            final List<String> types = new ArrayList<>();
            if (graph != null && graph.getSchema() != null) {
                for (final SchemaTransactionType type : SchemaTransactionTypeUtilities.getTypes(graph.getSchema().getFactory().getRegisteredConcepts())) {
                    types.add(type.getName());
                }
                if (!types.isEmpty()) {
                    types.sort(String::compareTo);
                }
            }
            transactionType.suppressEvent(true, new ArrayList());
            SingleChoiceParameterType.setOptions(transactionType, types);

            if (types.contains("Correlation")) {
                SingleChoiceParameterType.setChoice(transactionType, "Correlation");
            }
            transactionType.suppressEvent(false, new ArrayList());
        }
    }

    private void editResultStore(final RecordStore result, final String left, final String right, final RecordStore query, final String linkType) {
        final HashMap<SchemaVertexType, String> types = new HashMap<>();
        final List<SchemaVertexType> leftVertexTypesMatches = new ArrayList<>(SchemaVertexTypeUtilities.matchVertexTypes(left));
        leftVertexTypesMatches.sort(VertexDominanceCalculator.getDefault().getComparator());
        final List<SchemaVertexType> rightVertexTypesMatches = new ArrayList<>(SchemaVertexTypeUtilities.matchVertexTypes(right));
        rightVertexTypesMatches.sort(VertexDominanceCalculator.getDefault().getComparator());

        if (leftVertexTypesMatches.size() > 0) {
            types.put(SchemaVertexTypeUtilities.matchVertexTypes(left).get(0), left);
        }
        if (rightVertexTypesMatches.size() > 0) {
            types.put(SchemaVertexTypeUtilities.matchVertexTypes(right).get(0), right);
        }
        final List<SchemaVertexType> ordered_types = new ArrayList<>(types.keySet());

        result.add();
        result.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, query.get(SOURCE_ID));
        result.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, left);
        if (ordered_types.size() > 0 && leftVertexTypesMatches.size() > 0) {
            result.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, leftVertexTypesMatches.get(0));
        }
        result.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, right);
        
        //Loops through all of the Node attributes and copies them to the new node
        query.reset();
        while(query.next()){
            for (final String key : query.keys()){
                if(key.endsWith(".[id]") || SOURCE_IDENTIFIER.equals(key)) {
                     //Skips the id and Identifier to make the new node unique
                } else if ((GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X).equals(key) 
                        || (GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y).equals(key) 
                        || (GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z).equals(key)){ 
                    //The coordinates are also skipped so that the second node is not created in the exact same location
                    //as the first node
                } else {
                     result.set(GraphRecordStoreUtilities.DESTINATION + key.replace(GraphRecordStoreUtilities.SOURCE, ""), query.get(key));
                }
            }
        }
        
        if (ordered_types.size() > 1 && rightVertexTypesMatches.size() > 0) {
            result.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, rightVertexTypesMatches.get(0));
        }
        result.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.TYPE, linkType);
    }

    @Override
    protected RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final RecordStore result = new GraphRecordStore();

        final Map<String, PluginParameter<?>> splitParameters = parameters.getParameters();
        final String character = splitParameters.get(SPLIT_PARAMETER_ID).getStringValue();
        final ParameterValue transactionTypeChoice = splitParameters.get(TRANSACTION_TYPE_PARAMETER_ID).getSingleChoice();
        final String linkType = transactionTypeChoice != null ? transactionTypeChoice.toString() : "Correlation";
        final boolean allOccurrences = splitParameters.get(ALL_OCCURRENCES_PARAMETER_ID).getBooleanValue();

        query.reset();
        while (query.next()) {
            final String identifier = query.get(SOURCE_IDENTIFIER);
            if (identifier != null && identifier.contains(character) && identifier.indexOf(character) < identifier.length() - character.length()) {
                if (allOccurrences) {
                    final List<String> substrings = new ArrayList<>(Arrays.asList(identifier.split(character)));
                    final String left = substrings.get(0);
                    for (int i = 1; i < substrings.size(); i++) {
                        final String right = substrings.get(i);
                        editResultStore(result, left, right, query, linkType);
                    }
                } else {
                    final int i = identifier.indexOf(character);
                    final String left = identifier.substring(0, i);
                    final String right = identifier.substring(i + 1);
                    editResultStore(result, left, right, query, linkType);
                }
            }
        }

        return result;
    }
}
