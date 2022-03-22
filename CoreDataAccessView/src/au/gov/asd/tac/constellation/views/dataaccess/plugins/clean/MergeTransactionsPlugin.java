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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.GraphElementMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.mergers.IgnoreMergedGraphElementMerger;
import au.gov.asd.tac.constellation.graph.mergers.IgnoreSurvivingGraphElementMerger;
import au.gov.asd.tac.constellation.graph.mergers.PriorityMergedGraphElementMerger;
import au.gov.asd.tac.constellation.graph.mergers.PrioritySurvivingGraphElementMerger;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionType.MergeException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Merge Transactions Plugin
 *
 * @author cygnus_x-1
 * @author arcturus
 * @author antares
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@Messages("MergeTransactionsPlugin=Merge Transactions")
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public class MergeTransactionsPlugin extends SimpleQueryPlugin implements DataAccessPlugin {

    private static final Map<String, Comparator<Long>> LEAD_TRANSACTION_CHOOSERS = new LinkedHashMap<>();
    private static final Map<String, MergeTransactionType> MERGE_TYPES = new LinkedHashMap<>();
    private static final Map<String, GraphElementMerger> MERGERS = new LinkedHashMap<>();

    // plugin parameter ID's
    public static final String SELECTED_PARAMETER_ID = PluginParameter.buildId(MergeTransactionsPlugin.class, "selected");
    public static final String LEAD_PARAMETER_ID = PluginParameter.buildId(MergeTransactionsPlugin.class, "lead");
    public static final String MERGER_PARAMETER_ID = PluginParameter.buildId(MergeNodesPlugin.class, "merger");
    public static final String THRESHOLD_PARAMETER_ID = PluginParameter.buildId(MergeTransactionsPlugin.class, "threshold");
    public static final String MERGE_TYPE_PARAMETER_ID = PluginParameter.buildId(MergeTransactionsPlugin.class, "merge_type");

    // lead transaction choices
    public static final String LATEST_TIME = "Latest Time";
    public static final String EARLIEST_TIME = "Earliest Time";

    @Override
    public String getType() {
        return DataAccessPluginCoreType.CLEAN;
    }

    @Override
    public int getPosition() {
        return 300;
    }

    @Override
    public String getDescription() {
        return "Merge transactions in your graph together";
    }

    /**
     * Keeping this package protected so that it can be used in the unit tests
     */
    static final Comparator<Long> EARLIEST_TRANSACTION_CHOOSER = ((o1, o2) -> {
        if (o1 > o2) {
            return -1;
        } else if (o1 < o2) {
            return 1;
        } else {
            return o1.compareTo(o2);
        }
    });

    /**
     * Keeping this package protected so that it can be used in the unit tests
     */
    static final Comparator<Long> LATEST_TRANSACTION_CHOOSER = ((o1, o2) -> {
        if (o1 > o2) {
            return 1;
        } else if (o1 < o2) {
            return -1;
        } else {
            return o1.compareTo(o2);
        }
    });

    static {
        LEAD_TRANSACTION_CHOOSERS.put(LATEST_TIME, LATEST_TRANSACTION_CHOOSER);
        LEAD_TRANSACTION_CHOOSERS.put(EARLIEST_TIME, EARLIEST_TRANSACTION_CHOOSER);

        MERGERS.put("Retain lead transaction attributes if present", new PrioritySurvivingGraphElementMerger());
        MERGERS.put("Retain lead transaction attributes always", new IgnoreMergedGraphElementMerger());
        MERGERS.put("Copy merged transaction attributes if present", new PriorityMergedGraphElementMerger());
        MERGERS.put("Copy merged transaction attributes always", new IgnoreSurvivingGraphElementMerger());

        final Collection<? extends MergeTransactionType> mergeTransactionTypes = Lookup.getDefault().lookupAll(MergeTransactionType.class);
        for (final MergeTransactionType mergeTransactionType : mergeTransactionTypes) {
            MERGE_TYPES.put(mergeTransactionType.getName(), mergeTransactionType);
        }
    }

    @Override
    public PluginParameters createParameters() {

        PluginParameters params = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> mergeType = SingleChoiceParameterType.build(MERGE_TYPE_PARAMETER_ID);
        mergeType.setName("Merge By");
        mergeType.setDescription("Transactions will be merged based on this");
        mergeType.setRequired(true);
        final List<String> mergeTypes = new ArrayList<>(MERGE_TYPES.keySet());
        SingleChoiceParameterType.setOptions(mergeType, mergeTypes);
        SingleChoiceParameterType.setChoice(mergeType, mergeTypes.get(0));
        params.addParameter(mergeType);

        final PluginParameter<IntegerParameterValue> threshold = IntegerParameterType.build(THRESHOLD_PARAMETER_ID);
        threshold.setName("Threshold");
        threshold.setIntegerValue(0);
        IntegerParameterType.setMinimum(threshold, 0);
        threshold.setEnabled(false);
        params.addParameter(threshold);

        final PluginParameter<SingleChoiceParameterValue> mergingRule = SingleChoiceParameterType.build(MERGER_PARAMETER_ID);
        mergingRule.setName("Merging Rule");
        mergingRule.setDescription("The rule deciding how attributes are merged");
        final List<String> mergerNames = new ArrayList<>(MERGERS.keySet());
        SingleChoiceParameterType.setOptions(mergingRule, mergerNames);
        SingleChoiceParameterType.setChoice(mergingRule, mergerNames.get(0));
        mergingRule.setEnabled(false);
        params.addParameter(mergingRule);

        final PluginParameter<SingleChoiceParameterValue> leadParam = SingleChoiceParameterType.build(LEAD_PARAMETER_ID);
        leadParam.setName("Lead Transaction");
        leadParam.setDescription("The rule deciding how to choose the lead transaction");
        final List<String> leadTransactionChooserNames = new ArrayList<>(LEAD_TRANSACTION_CHOOSERS.keySet());
        SingleChoiceParameterType.setOptions(leadParam, leadTransactionChooserNames);
        SingleChoiceParameterType.setChoice(leadParam, leadTransactionChooserNames.get(0));
        leadParam.setEnabled(false);
        params.addParameter(leadParam);

        final PluginParameter<BooleanParameterValue> selectedParam = BooleanParameterType.build(SELECTED_PARAMETER_ID);
        selectedParam.setName("Selected Only");
        selectedParam.setDescription("Merge Only Selected Transactions");
        selectedParam.setEnabled(false);
        params.addParameter(selectedParam);

        params.addController(MERGE_TYPE_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final String selectedMergeType = parameters.get(MERGE_TYPE_PARAMETER_ID).getStringValue();
                if (MERGE_TYPES.containsKey(selectedMergeType)) {
                    final MergeTransactionType mergeTransactionTypeSelected = MERGE_TYPES.get(selectedMergeType);
                    mergeTransactionTypeSelected.updateParameters(parameters);
                }
            }
        });

        return params;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        int mergedCount = 0;

        interaction.setProgress(0, 0, "Merging transactions...", true);

        final String mergeTransactionTypeName = parameters.getParameters().get(MERGE_TYPE_PARAMETER_ID).getStringValue();

        if (mergeTransactionTypeName == null) {
            throw new PluginException(PluginNotificationLevel.ERROR, "Select a Merge By option.");
        }

        if (!MERGE_TYPES.containsKey(mergeTransactionTypeName)) {
            throw new PluginException(PluginNotificationLevel.FATAL, String.format("Merge node type %s not found.", mergeTransactionTypeName));
        }

        final int threshold = parameters.getParameters().get(THRESHOLD_PARAMETER_ID).getIntegerValue();
        final GraphElementMerger merger = MERGERS.get(parameters.getParameters().get(MERGER_PARAMETER_ID).getStringValue());
        final String leadTransactionChooserName = parameters.getParameters().get(LEAD_PARAMETER_ID).getStringValue();
        final Comparator<Long> leadTransactionChooser = LEAD_TRANSACTION_CHOOSERS.get(leadTransactionChooserName);
        final boolean selectedOnly = parameters.getParameters().get(SELECTED_PARAMETER_ID).getBooleanValue();

        final MergeTransactionType mergeTransactionType = MERGE_TYPES.get(mergeTransactionTypeName);
        final Map<Integer, Set<Integer>> transactionsToMerge;
        try {
            transactionsToMerge = mergeTransactionType.getTransactionsToMerge(graph, leadTransactionChooser, threshold, selectedOnly);
        } catch (final MergeException ex) {
            throw new PluginException(PluginNotificationLevel.ERROR, ex);
        }

        for (final Map.Entry<Integer, Set<Integer>> entry : transactionsToMerge.entrySet()) {
            mergedCount += mergeTransactions(graph, entry.getValue(), entry.getKey(), merger);
        }

        interaction.setProgress(1, 0, "Merged " + mergedCount + " transactions.", true);

        PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
    }

    protected int mergeTransactions(final GraphWriteMethods graph, final Set<Integer> transactionsToMerge, final int leadTransaction, final GraphElementMerger merger) {
        int mergedCount = 0;

        for (final int transaction : transactionsToMerge) {
            if (transaction != leadTransaction && merger.mergeElement(graph, GraphElementType.TRANSACTION, leadTransaction, transaction)) {
                mergedCount++;
            }
        }

        return mergedCount;
    }
}
