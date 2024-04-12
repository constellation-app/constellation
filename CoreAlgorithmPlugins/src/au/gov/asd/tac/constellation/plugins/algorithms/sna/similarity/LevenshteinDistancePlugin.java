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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates levenshtein distance of an attribute for each pair of vertices.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("LevenshteinDistancePlugin=Levenshtein Distance")
@PluginInfo(tags = {PluginTags.ANALYTIC})
public class LevenshteinDistancePlugin extends SimpleEditPlugin {

    private static final SchemaAttribute LEVENSHTEIN_DISTANCE_ATTRIBUTE = SnaConcept.TransactionAttribute.LEVENSHTEIN_DISTANCE;

    public static final String ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(LevenshteinDistancePlugin.class, "attribute");
    public static final String MAXIMUM_DISTANCE_PARAMETER_ID = PluginParameter.buildId(LevenshteinDistancePlugin.class, "maximum_distance");
    public static final String CASE_INSENSITIVE_PARAMETER_ID = PluginParameter.buildId(LevenshteinDistancePlugin.class, "case_insensitive");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(LevenshteinDistancePlugin.class, "selected_only");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> attributeType = SingleChoiceParameterType.build(ATTRIBUTE_PARAMETER_ID);
        attributeType.setName("Compare Attribute");
        attributeType.setDescription("The node attribute to compare");
        attributeType.setRequired(true);
        parameters.addParameter(attributeType);

        final PluginParameter<IntegerParameterValue> maxDistance = IntegerParameterType.build(MAXIMUM_DISTANCE_PARAMETER_ID);
        maxDistance.setName("Maximum Distance");
        maxDistance.setDescription("Only draws links between nodes whose attributes are at most this far apart");
        maxDistance.setIntegerValue(1);
        IntegerParameterType.setMinimum(maxDistance, 0);
        parameters.addParameter(maxDistance);

        final PluginParameter<BooleanParameterValue> caseInsensitiveParameter = BooleanParameterType.build(CASE_INSENSITIVE_PARAMETER_ID);
        caseInsensitiveParameter.setName("Case Insensitive");
        caseInsensitiveParameter.setDescription("Ignore case when comparing attribute");
        parameters.addParameter(caseInsensitiveParameter);

        final PluginParameter<BooleanParameterValue> selectedOnlyParameter = BooleanParameterType.build(SELECTED_ONLY_PARAMETER_ID);
        selectedOnlyParameter.setName("Selected Only");
        selectedOnlyParameter.setDescription("Compare selected nodes only");
        parameters.addParameter(selectedOnlyParameter);

        return parameters;
    }

    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        final List<String> stringAttributes = new ArrayList<>();
        if (graph.getSchema() != null) {
            final Map<String, SchemaAttribute> attributes = graph.getSchema().getFactory().getRegisteredAttributes(GraphElementType.VERTEX);
            for (final String attr : attributes.keySet()) {
                final SchemaAttribute attribute = attributes.get(attr);
                final String attributeType = attribute.getAttributeType();
                if (attributeType.equals(StringAttributeDescription.ATTRIBUTE_NAME)) {
                    stringAttributes.add(attr);
                }
            }
        }

        stringAttributes.sort(String::compareTo);

        if (parameters != null && parameters.getParameters() != null) {
            @SuppressWarnings("unchecked") // ATTRIBUTE_PARAMETER_ID is created as a SingleChoiceParmeter in this class on line 71.
            final PluginParameter<SingleChoiceParameterValue> compareAttribute = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ATTRIBUTE_PARAMETER_ID);
            SingleChoiceParameterType.setOptions(compareAttribute, stringAttributes);
            if (stringAttributes.contains(VisualConcept.VertexAttribute.IDENTIFIER.getName())) {
                SingleChoiceParameterType.setChoice(compareAttribute, VisualConcept.VertexAttribute.IDENTIFIER.getName());
            }
        }
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final String compareAttribute = parameters.getStringValue(ATTRIBUTE_PARAMETER_ID);
        final int maxDistance = parameters.getIntegerValue(MAXIMUM_DISTANCE_PARAMETER_ID);
        final boolean caseInsensitive = parameters.getBooleanValue(CASE_INSENSITIVE_PARAMETER_ID);
        final boolean selectedOnly = parameters.getBooleanValue(SELECTED_ONLY_PARAMETER_ID);

        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int vertexCompareAttributeId = graph.getAttribute(GraphElementType.VERTEX, compareAttribute);

        if (vertexCompareAttributeId == Graph.NOT_FOUND) {
            NotifyDisplayer.display(String.format("The specified attribute %s does not exist on the graph.", compareAttribute), NotifyDescriptor.WARNING_MESSAGE);
            return;
        }

        // compare each pair of vertices
        final int vertexCount = graph.getVertexCount();
        for (int one = 0; one < vertexCount; one++) {
            final int vxOneId = graph.getVertex(one);
            if (selectedOnly && !graph.getBooleanValue(vertexSelectedAttributeId, vxOneId)) {
                continue;
            }
            for (int two = one + 1; two < vertexCount; two++) {
                final int vxTwoId = graph.getVertex(two);
                if (selectedOnly && !graph.getBooleanValue(vertexSelectedAttributeId, vxTwoId)) {
                    continue;
                }

                String stringOne = graph.getStringValue(vertexCompareAttributeId, vxOneId);
                String stringTwo = graph.getStringValue(vertexCompareAttributeId, vxTwoId);

                if (StringUtils.isBlank(stringOne)
                        || StringUtils.isBlank(stringTwo)
                        || Math.abs(stringOne.length() - stringTwo.length()) > maxDistance) {
                    continue;
                }

                if (caseInsensitive) {
                    stringOne = stringOne.toLowerCase();
                    stringTwo = stringTwo.toLowerCase();
                }

                final LevenshteinDistanceFunction ldf = new LevenshteinDistanceFunction(maxDistance);
                final double distance = ldf.getDistance(stringOne, stringTwo);
                if (distance > maxDistance) {
                    continue;
                }

                SimilarityUtilities.setGraphAndEnsureAttributes(graph, LEVENSHTEIN_DISTANCE_ATTRIBUTE);
                SimilarityUtilities.addScoreToGraph(vxOneId, vxTwoId, (float) distance);

            }
        }

        // complete with schema
        PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
    }
}
