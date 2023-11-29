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
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportUtilities;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodeType.MergeException;
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
 * Merge Nodes Plugin
 *
 * @author cygnus_x-1
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@Messages("MergeNodesPlugin=Merge Nodes")
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public class MergeNodesPlugin extends SimpleQueryPlugin implements DataAccessPlugin {

    public static final String MERGE_TYPE_PARAMETER_ID = PluginParameter.buildId(MergeNodesPlugin.class, "merge_type");
    public static final String THRESHOLD_PARAMETER_ID = PluginParameter.buildId(MergeNodesPlugin.class, "threshold");
    public static final String MERGER_PARAMETER_ID = PluginParameter.buildId(MergeNodesPlugin.class, "merger");
    public static final String LEAD_PARAMETER_ID = PluginParameter.buildId(MergeNodesPlugin.class, "lead");
    public static final String SELECTED_PARAMETER_ID = PluginParameter.buildId(MergeNodesPlugin.class, "selected");
    public static final String ASK_PARAMETER_ID = PluginParameter.buildId(MergeNodesPlugin.class, "ask");

    private static final Map<String, Comparator<String>> VERTEX_CHOOSER = new LinkedHashMap<>();
    private static final Map<String, MergeNodeType> MERGE_TYPES = new LinkedHashMap<>();
    private static final Map<String, GraphElementMerger> MERGERS = new LinkedHashMap<>();

    @Override
    public String getType() {
        return DataAccessPluginCoreType.CLEAN;
    }

    @Override
    public int getPosition() {
        return 100;
    }

    @Override
    public String getDescription() {
        return "Merge nodes in your graph together";
    }

    protected static final Comparator<String> LONGEST_VERTEX_CHOOSER = (o1, o2) -> {
        if (o1.length() > o2.length()) {
            return -1;
        } else if (o1.length() < o2.length()) {
            return 1;
        } else {
            return o1.compareTo(o2);
        }
    };

    protected static final Comparator<String> SHORTEST_VERTEX_CHOOSER = (o1, o2) -> {
        if (o1.length() > o2.length()) {
            return 1;
        } else if (o1.length() < o2.length()) {
            return -1;
        } else {
            return o1.compareTo(o2);
        }
    };

    static {
        MERGERS.put("Retain lead vertex attributes if present", new PrioritySurvivingGraphElementMerger());
        MERGERS.put("Retain lead vertex attributes always", new IgnoreMergedGraphElementMerger());
        MERGERS.put("Copy merged vertex attributes if present", new PriorityMergedGraphElementMerger());
        MERGERS.put("Copy merged vertex attributes always", new IgnoreSurvivingGraphElementMerger());

        VERTEX_CHOOSER.put("Longest Value", LONGEST_VERTEX_CHOOSER);
        VERTEX_CHOOSER.put("Shortest Value", SHORTEST_VERTEX_CHOOSER);
        VERTEX_CHOOSER.put("Ask Me", null);

        final Collection<? extends MergeNodeType> mergeNodeTypes = Lookup.getDefault().lookupAll(MergeNodeType.class);
        for (final MergeNodeType mergeNodeType : mergeNodeTypes) {
            MERGE_TYPES.put(mergeNodeType.getName(), mergeNodeType);
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> mergeType = SingleChoiceParameterType.build(MERGE_TYPE_PARAMETER_ID);
        mergeType.setName("Merge By");
        mergeType.setDescription("Nodes will be merged based on this");
        mergeType.setRequired(true);
        final List<String> mergeTypes = new ArrayList<>(MERGE_TYPES.keySet());
        SingleChoiceParameterType.setOptions(mergeType, mergeTypes);
        params.addParameter(mergeType);

        final PluginParameter<IntegerParameterValue> threshold = IntegerParameterType.build(THRESHOLD_PARAMETER_ID);
        threshold.setName("Threshold");
        threshold.setDescription("The maximum nodes to merge");
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

        final PluginParameter<SingleChoiceParameterValue> leadNode = SingleChoiceParameterType.build(LEAD_PARAMETER_ID);
        leadNode.setName("Lead Node");
        leadNode.setDescription("The rule deciding how to choose the lead node");
        final List<String> leadVertexChooserNames = new ArrayList<>(VERTEX_CHOOSER.keySet());
        SingleChoiceParameterType.setOptions(leadNode, leadVertexChooserNames);
        SingleChoiceParameterType.setChoice(leadNode, leadVertexChooserNames.get(0));
        leadNode.setEnabled(false);
        params.addParameter(leadNode);

        final PluginParameter<BooleanParameterValue> selectedOnly = BooleanParameterType.build(SELECTED_PARAMETER_ID);
        selectedOnly.setName("Selected Only");
        selectedOnly.setDescription("Merge Only Selected Nodes");
        selectedOnly.setBooleanValue(true);
        selectedOnly.setEnabled(false);
        params.addParameter(selectedOnly);

        params.addController(MERGE_TYPE_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final String selectedMergeType = parameters.get(MERGE_TYPE_PARAMETER_ID).getStringValue();
                if (MERGE_TYPES.containsKey(selectedMergeType)) {
                    final MergeNodeType mergeNodeTypeSelected = MERGE_TYPES.get(selectedMergeType);
                    mergeNodeTypeSelected.updateParameters(parameters);
                }
            }
        });
        // value is set after the controller definition so that the controller gets triggered
        SingleChoiceParameterType.setChoice(mergeType, mergeTypes.get(0));

        return params;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                
        // Retrieve PluginParameter values 
        final String mergeNodeTypeName = parameters.getParameters().get(MERGE_TYPE_PARAMETER_ID).getStringValue();
        final int threshold = parameters.getParameters().get(THRESHOLD_PARAMETER_ID).getIntegerValue();
        final boolean selectedOnly = parameters.getParameters().get(SELECTED_PARAMETER_ID).getBooleanValue();
        final String mergerName = parameters.getParameters().get(MERGER_PARAMETER_ID).getStringValue();
        final String leadNodeChooserName = parameters.getParameters().get(LEAD_PARAMETER_ID).getStringValue();
        
        // Throw errors if a merge type was not selected or does not exist
        if (mergeNodeTypeName == null) {
            throw new PluginException(PluginNotificationLevel.ERROR, "Select a Merge By option.");
        }
        if (!MERGE_TYPES.containsKey(mergeNodeTypeName)) {
            throw new PluginException(PluginNotificationLevel.FATAL, String.format("Merge node type %s not found.", mergeNodeTypeName));
        }
        
        //Local process-tracking varables (Process is indeteminate until quantity of merged nodes is known)
        int currentProcessStep = 0;
        int totalProcessSteps = -1; 
        interaction.setProgress(currentProcessStep, totalProcessSteps, "Merging nodes...", true);

        //Determine which nodes need to be merged
        final Comparator<String> leadNodeChooser = VERTEX_CHOOSER.get(leadNodeChooserName);
        final MergeNodeType mergeNodeType = MERGE_TYPES.get(mergeNodeTypeName);
        final Map<Integer, Set<Integer>> nodesToMerge;
        try {
            nodesToMerge = mergeNodeType.getNodesToMerge(graph, leadNodeChooser, threshold, selectedOnly);
        } catch (final MergeException ex) {
            throw new PluginException(PluginNotificationLevel.ERROR, ex);
        }
        
        totalProcessSteps = nodesToMerge.size();
        
        // Perform the merge
        int mergedNodesCount = 0;
        final GraphElementMerger merger = MERGERS.get(mergerName);
        for (final Map.Entry<Integer, Set<Integer>> entry : nodesToMerge.entrySet()) {
            mergedNodesCount += mergeVertices(graph, entry.getValue(), entry.getKey(), merger);
            interaction.setProgress(++currentProcessStep, totalProcessSteps, true);
        }

        // Set process to complete
        totalProcessSteps = 0;
        interaction.setProgress(currentProcessStep, 
                totalProcessSteps, 
                String.format("Merged %s.", 
                        PluginReportUtilities.getNodeCountString(mergedNodesCount)
                ), 
                true
        );
        PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
    }

    private int mergeVertices(final GraphWriteMethods graph, final Set<Integer> verticesToMerge, final int leadVertex, final GraphElementMerger merger) {
        int mergedCount = 0;

        for (final int vertex : verticesToMerge) {
            if (vertex != leadVertex && merger.mergeElement(graph, GraphElementType.VERTEX, leadVertex, vertex)) {
                mergedCount++;
            }
        }

        return mergedCount;
    }
}
