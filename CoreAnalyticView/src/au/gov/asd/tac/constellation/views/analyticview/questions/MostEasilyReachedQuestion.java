/*
 * Copyright 2010-2023 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.KatzCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AnalyticAggregator;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AppendScoreAggregator;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.analytics.KatzCentralityAnalytic;
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
public class MostEasilyReachedQuestion implements AnalyticQuestionDescription<ScoreResult> {

    @Override
    public String getName() {
        return "Most Easily Reached?";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public List<Class<? extends AnalyticPlugin<ScoreResult>>> getPluginClasses() {
        return Arrays.asList(KatzCentralityAnalytic.class);
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
        parameters.setFloatValue(KatzCentralityPlugin.ALPHA_PARAMETER_ID, 0.1F);
        parameters.setFloatValue(KatzCentralityPlugin.BETA_PARAMETER_ID, 1.0F);
        parameters.setIntegerValue(KatzCentralityPlugin.ITERATIONS_PARAMETER_ID, 100);
        parameters.setFloatValue(KatzCentralityPlugin.EPSILON_PARAMETER_ID, 1E-8F);
        parameters.setBooleanValue(KatzCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, true);
        parameters.setBooleanValue(KatzCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false);
    }
}
