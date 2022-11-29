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
package au.gov.asd.tac.constellation.views.analyticview.questions;

import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.CosineSimilarityPlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AnalyticAggregator;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AppendScoreAggregator;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.analytics.CosineSimilarityAnalytic;
import au.gov.asd.tac.constellation.views.analyticview.analytics.ScoreAnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.analytics.ScoreAnalyticPlugin.TransactionTypeParameterValue;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AnalyticQuestionDescription.class)
public class MostLikelyCorrelatedQuestion implements AnalyticQuestionDescription<ScoreResult> {

    @Override
    public String getName() {
        return "Most Likely Correlated?";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public List<Class<? extends AnalyticPlugin<ScoreResult>>> getPluginClasses() {
        return Arrays.asList(CosineSimilarityAnalytic.class);
    }

    @Override
    public Class<? extends AnalyticAggregator<ScoreResult>> getAggregatorType() {
        return AppendScoreAggregator.class;
    }

    @Override
    public Class<? extends AnalyticResult<?>> getResultType() {
        return ScoreResult.class;
    }

    @Override
    public void initialiseParameters(final AnalyticPlugin<ScoreResult> plugin, final PluginParameters parameters) {
        @SuppressWarnings("unchecked") //TRANSACTION_TYPES_PARAMETER always of type MultiChoiceParameter
        final PluginParameter<MultiChoiceParameterValue> transactionTypeParameter = (PluginParameter<MultiChoiceParameterValue>) parameters.getParameters().get(ScoreAnalyticPlugin.TRANSACTION_TYPES_PARAMETER_ID);
        final List<TransactionTypeParameterValue> newChecked = new ArrayList<>();
        final List<ParameterValue> checked = transactionTypeParameter.getMultiChoiceValue().getChoicesData();
        checked.forEach(parameterValue -> {
            final TransactionTypeParameterValue transactionTypeParameterValue = (TransactionTypeParameterValue) parameterValue;
            final SchemaTransactionType transactionType = (SchemaTransactionType) transactionTypeParameterValue.getObjectValue();
            if (transactionType == null || !transactionType.isSubTypeOf(AnalyticConcept.TransactionType.SIMILARITY)) {
                newChecked.add(transactionTypeParameterValue);
            }
        });
        MultiChoiceParameterType.setChoicesData(transactionTypeParameter, newChecked);

        parameters.setBooleanValue(CosineSimilarityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(CosineSimilarityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(CosineSimilarityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true);
    }
}
