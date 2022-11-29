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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AnalyticAggregator;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import java.util.List;

/**
 * A collection of AnalyticPlugin which answer a particular question asked of a
 * graph.
 *
 * @param <R> the type of the results
 * @author cygnus_x-1
 */
public interface AnalyticQuestionDescription<R extends AnalyticResult<?>> {

    public String getName();

    public String getDescription();

    public List<Class<? extends AnalyticPlugin<R>>> getPluginClasses();

    public Class<? extends AnalyticAggregator<R>> getAggregatorType();

    public Class<? extends AnalyticResult<?>> getResultType();

    /**
     * Gives you the opportunity to initialise parameters for the given plugin.
     * If this question uses multiple plugins this method will be called once
     * per plugin. Check which plugin has been provided and update parameters
     * appropriately.
     *
     * @param plugin the plugin to be updated
     * @param parameters the current parameters for the plugin
     */
    public void initialiseParameters(final AnalyticPlugin<R> plugin, final PluginParameters parameters);
}
