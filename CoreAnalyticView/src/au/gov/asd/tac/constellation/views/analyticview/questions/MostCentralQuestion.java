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
package au.gov.asd.tac.constellation.views.analyticview.questions;

import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.ClosenessCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AnalyticAggregator;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AppendScoreAggregator;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.analytics.ClosenessCentralityAnalytic;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import java.util.Arrays;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AnalyticQuestionDescription.class)
public class MostCentralQuestion implements AnalyticQuestionDescription<ScoreResult> {

    @Override
    public String getName() {
        return "Most Central?";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public List<Class<? extends AnalyticPlugin<ScoreResult>>> getPluginClasses() {
        return Arrays.asList(ClosenessCentralityAnalytic.class);
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
        parameters.setBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, true);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false);
        parameters.setBooleanValue(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false);
    }
}
