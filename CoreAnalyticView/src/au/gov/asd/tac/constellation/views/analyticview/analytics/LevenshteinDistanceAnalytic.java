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
package au.gov.asd.tac.constellation.views.analyticview.analytics;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.utilities.AttributeUtilities;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.LevenshteinDistancePlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * The Levenshtein Distance Similarity analytic for the Analytic View.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = AnalyticPlugin.class),
    @ServiceProvider(service = Plugin.class)
})
@PluginInfo(tags = {PluginTags.ANALYTIC})
@AnalyticInfo(analyticCategory = "Similarity")
@NbBundle.Messages("LevenshteinDistanceAnalytic=Levenshtein Distance Analytic")
public class LevenshteinDistanceAnalytic extends ScoreAnalyticPlugin {

    private static final String ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(LevenshteinDistancePlugin.class, "attribute");

    @Override
    public String getDocumentationUrl() {
        return getHelpPath() + "analytic-levenshtein-distance.md";
    }

    @Override
    public Set<SchemaAttribute> getAnalyticAttributes(final PluginParameters parameters) {
        final Set<SchemaAttribute> analyticAttributes = new HashSet<>();
        analyticAttributes.add(SnaConcept.TransactionAttribute.LEVENSHTEIN_DISTANCE);
        return Collections.unmodifiableSet(analyticAttributes);
    }

    @Override
    public Class<? extends Plugin> getAnalyticPlugin() {
        return LevenshteinDistancePlugin.class;
    }

    @Override
    public Set<SchemaAttribute> getPrerequisiteAttributes() {
        final Set<SchemaAttribute> analyticAttributes = new HashSet<>();
        analyticAttributes.add(VisualConcept.VertexAttribute.IDENTIFIER);
        return analyticAttributes;
    }

    @Override
    public void onPrerequisiteAttributeChange(final Graph graph, final PluginParameters parameters) {

        final List<String> stringAttributes = AttributeUtilities.getAttributeNames(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME);
        updateParameters(parameters);

        stringAttributes.sort(String::compareTo);

        @SuppressWarnings("unchecked") //ATTRIBUTE_PARAMETER always of type SingleChoiceParameter
        final PluginParameter<SingleChoiceParameterValue> attributeParam = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(ATTRIBUTE_PARAMETER_ID);

        SingleChoiceParameterType.setOptions(attributeParam, stringAttributes);

        if (stringAttributes.contains(VisualConcept.VertexAttribute.IDENTIFIER.getName())) {
            SingleChoiceParameterType.setChoice(attributeParam, VisualConcept.VertexAttribute.IDENTIFIER.getName());
        }
    }
}
