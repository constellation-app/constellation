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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticViewController;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AnalyticAggregator;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A collection of {@link AnalyticPlugin} which answer a particular question
 * asked of a graph.
 *
 * @param <R> The result type
 * @author cygnus_x-1
 */
public class AnalyticQuestion<R extends AnalyticResult<?>> {
    
    private static final Logger LOGGER = Logger.getLogger(AnalyticQuestion.class.getName());

    public static final String CUSTOM_QUESTION_NAME = "Custom";
    public static final String CUSTOM_QUESTION_DESCRIPTION = "An analytic built by the user.";

    private final AnalyticQuestionDescription<R> questionDescription;
    private final Map<AnalyticPlugin<R>, PluginParameters> pluginsWithParameters;
    private AnalyticAggregator<R> aggregator;
    private R result;
    private final List<Exception> exceptions;

    public AnalyticQuestion(final AnalyticQuestionDescription<R> questionDescription) {
        this.questionDescription = questionDescription;
        this.pluginsWithParameters = new HashMap<>();
        this.aggregator = null;
        this.result = null;
        this.exceptions = new ArrayList<>();
    }

    public String getName() {
        if (questionDescription != null) {
            return questionDescription.getName();
        }
        return CUSTOM_QUESTION_NAME;
    }

    public String getDescription() {
        if (questionDescription != null) {
            return questionDescription.getDescription();
        }
        return CUSTOM_QUESTION_DESCRIPTION;
    }

    public List<AnalyticPlugin<R>> getPlugins() {
        return new ArrayList<>(pluginsWithParameters.keySet());
    }

    public void addPlugin(final AnalyticPlugin<R> plugin, final PluginParameters parameters) {
        this.pluginsWithParameters.put(plugin, parameters);
    }

    public PluginParameters getParameters(final AnalyticPlugin<R> plugin) {
        return pluginsWithParameters.get(plugin);
    }

    public AnalyticAggregator<R> getAggregator() {
        return aggregator;
    }

    public void setAggregator(final AnalyticAggregator<R> aggregator) {
        this.aggregator = aggregator;
    }

    public R getResult() {
        return result;
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public void addException(final Exception exception) {
        this.exceptions.add(exception);
    }

    public void setResult(final R result) {
        this.result = result;
    }

    public AnalyticQuestion<R> answer(final Graph graph) {

        // run plugins
        final List<R> analyticResults = new ArrayList<>();
        final Map<Future<?>, AnalyticPlugin<R>> pluginFutures = new HashMap<>();
        pluginsWithParameters.forEach((plugin, parameters) -> pluginFutures.put(
                PluginExecution.withPlugin(plugin)
                        .withParameters(parameters)
                        .executeLater(graph),
                plugin));
        pluginFutures.forEach((future, plugin) -> {
            try {
                future.get();
                final R analyticResult = plugin.getResults();
                if (analyticResult != null) {
                    analyticResults.add(analyticResult);
                }
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Analytic answering was interrupted");
                pluginFutures.keySet().forEach(redundantFuture -> redundantFuture.cancel(true));
            } catch (final CancellationException | ExecutionException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                exceptions.add(ex);
            }
        });

        // aggregate and sort plugin results
        try {
            result = aggregator.aggregate(analyticResults);
            result.sort();
        } catch (final AnalyticException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            exceptions.add(ex);
        }

        AnalyticViewController.getDefault().setQuestion(this);
        return this;
    }
}
