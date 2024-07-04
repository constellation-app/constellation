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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionsPlugin.LEAD_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionsPlugin.MERGER_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionsPlugin.MERGE_TYPE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionsPlugin.SELECTED_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionsPlugin.THRESHOLD_PARAMETER_ID;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Merge Transactions based on the DateTime attribute
 *
 * @author antares
 */
@ServiceProvider(service = MergeTransactionType.class)
public class MergeTransactionsByDateTime implements MergeTransactionType {

    private static final String MERGE_DATETIME_NAME = "DateTime";
    private static final String MERGE_DATETIME_THRESHOLD_DESCRIPTION = "Time window (in seconds)";

    @Override
    public String getName() {
        return MERGE_DATETIME_NAME;
    }

    @Override
    public void updateParameters(Map<String, PluginParameter<?>> parameters) {
        parameters.get(SELECTED_PARAMETER_ID).setEnabled(true);
        parameters.get(LEAD_PARAMETER_ID).setEnabled(true);
        parameters.get(MERGER_PARAMETER_ID).setEnabled(true);
        parameters.get(THRESHOLD_PARAMETER_ID).setEnabled(true);
        parameters.get(THRESHOLD_PARAMETER_ID).setDescription(MERGE_DATETIME_THRESHOLD_DESCRIPTION);
        parameters.get(MERGE_TYPE_PARAMETER_ID).setEnabled(true);
    }

    @Override
    public Map<Integer, Set<Integer>> getTransactionsToMerge(GraphWriteMethods graph, Comparator<Long> leadTransactionChooser, int threshold, boolean selectedOnly) throws MergeException {
        final Map<SchemaTransactionType, List<Integer>> typeMap = new HashMap<>();
        final Map<Integer, Set<Integer>> transactionsToMerge = new HashMap<>();

        final int identifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);
        final int typeAttribute = AnalyticConcept.TransactionAttribute.TYPE.get(graph);
        final int dateTimeAttribute = TemporalConcept.TransactionAttribute.DATETIME.get(graph);
        final int selectedAttribute = selectedOnly ? VisualConcept.TransactionAttribute.SELECTED.get(graph) : Graph.NOT_FOUND;

        if (typeAttribute == Graph.NOT_FOUND || dateTimeAttribute == Graph.NOT_FOUND || identifierAttribute == Graph.NOT_FOUND) {
            return transactionsToMerge;
        }

        final int edgeCount = graph.getEdgeCount();
        for (int edgePosition = 0; edgePosition < edgeCount; edgePosition++) {
            final int edgeId = graph.getEdge(edgePosition);
            final int transactionCount = graph.getEdgeTransactionCount(edgeId);
            if (transactionCount <= 1) {
                continue;
            }
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                final int transactionId = graph.getEdgeTransaction(edgeId, transactionPosition);
                if (!(selectedAttribute == Graph.NOT_FOUND || graph.getBooleanValue(selectedAttribute, transactionId))) {
                    continue;
                }
                SchemaTransactionType type = (SchemaTransactionType) graph.getObjectValue(typeAttribute, transactionId);
                if (type != null) {
                    type = (SchemaTransactionType) type.getTopLevelType();
                }
                List<Integer> typeTransactions = typeMap.get(type);
                if (typeTransactions == null) {
                    typeTransactions = new ArrayList<>();
                }

                typeTransactions.add(transactionId);
                typeMap.put(type, typeTransactions);
            }
            //If transactions of a particular type match and fall within threshold then add to transactions to merge
            for (SchemaTransactionType type : typeMap.keySet()) {
                final List<Integer> transactionsList = typeMap.get(type);
                final Integer[] transactions = transactionsList.toArray(new Integer[transactionsList.size()]);

                sortTransactions(transactions, graph, typeAttribute, dateTimeAttribute, leadTransactionChooser);

                Set<Integer> mergeGroup = new HashSet<>();
                Integer currentLead = null;

                for (int arrayPosition = 0; arrayPosition < transactions.length - 1; arrayPosition++) {
                    final int transactionA = transactions[arrayPosition];
                    final int transactionB = transactions[arrayPosition + 1];

                    final long transactionADateTime = graph.getLongValue(dateTimeAttribute, transactionA);
                    final long transactionBDateTime = graph.getLongValue(dateTimeAttribute, transactionB);
                    final long timeDifference = transactionADateTime - transactionBDateTime > 0 ? transactionADateTime - transactionBDateTime : transactionBDateTime - transactionADateTime;

                    if (timeDifference <= threshold * 1000) {
                        mergeGroup.add(transactionA); // will only be successfully added for the first element added to the empty set
                        mergeGroup.add(transactionB);

                        // We know the latest edition to the merge group will be the lead transaction
                        currentLead = transactionB;

                        // this check is so it adds the last mergeGroup to the map of transactions to merge (it wouldn't add it otherwise)
                        if (arrayPosition == transactions.length - 2) {
                            transactionsToMerge.put(currentLead, mergeGroup);
                        }
                    } else if (currentLead != null) {
                        transactionsToMerge.put(currentLead, mergeGroup);

                        // reset the values
                        currentLead = null;
                        mergeGroup = new HashSet<>();
                    } else {
                        // Do nothing
                    }
                }
            }

            typeMap.clear();
        }

        return transactionsToMerge;
    }
}
