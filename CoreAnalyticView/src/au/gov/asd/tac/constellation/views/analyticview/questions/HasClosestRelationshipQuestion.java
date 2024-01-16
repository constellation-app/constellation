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

import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.MultiplexityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.RatioOfReciprocityPlugin;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.WeightPlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AnalyticAggregator;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.MeanScoreAggregator;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.analytics.MultiplexityAnalytic;
import au.gov.asd.tac.constellation.views.analyticview.analytics.RatioOfReciprocityAnalytic;
import au.gov.asd.tac.constellation.views.analyticview.analytics.WeightAnalytic;
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
public class HasClosestRelationshipQuestion implements AnalyticQuestionDescription<ScoreResult> {

    @Override
    public String getName() {
        return "Has Closest Relationship?";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public List<Class<? extends AnalyticPlugin<ScoreResult>>> getPluginClasses() {
        return Arrays.asList(RatioOfReciprocityAnalytic.class, MultiplexityAnalytic.class, WeightAnalytic.class);
    }

    @Override
    public Class<? extends AnalyticAggregator<ScoreResult>> getAggregatorType() {
        return MeanScoreAggregator.class;
    }

    @Override
    public Class<? extends AnalyticResult<?>> getResultType() {
        return ScoreResult.class;
    }

    @Override
    public void initialiseParameters(final AnalyticPlugin<ScoreResult> plugin, final PluginParameters parameters) {
        if (plugin instanceof RatioOfReciprocityAnalytic) {
            parameters.setBooleanValue(RatioOfReciprocityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true);
            parameters.setBooleanValue(RatioOfReciprocityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        } else if (plugin instanceof MultiplexityAnalytic) {
            parameters.setBooleanValue(MultiplexityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
            parameters.setBooleanValue(MultiplexityPlugin.GROUP_BY_TOP_LEVEL_TYPE, false);
        } else if (plugin instanceof WeightAnalytic) {
            parameters.setBooleanValue(WeightPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, true);
        }
    }
}
