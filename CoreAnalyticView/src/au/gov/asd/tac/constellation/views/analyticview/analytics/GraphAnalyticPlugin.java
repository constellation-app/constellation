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
package au.gov.asd.tac.constellation.views.analyticview.analytics;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.GraphResult;
import au.gov.asd.tac.constellation.views.analyticview.results.GraphResult.GraphScore;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A basic analytic plugin reads whole-of-graph scores from the graph based on
 * the execution of a single pre-existing plugin.
 *
 * @author cygnus_x-1
 */
public abstract class GraphAnalyticPlugin extends AnalyticPlugin<GraphResult> {
    
    private static final Logger LOGGER = Logger.getLogger(GraphAnalyticPlugin.class.getName());

    protected GraphResult result;

    protected abstract Class<? extends Plugin> getAnalyticPlugin();

    protected final void computeResultsFromGraph(final GraphReadMethods graph, final PluginParameters parameters) {
        result = new GraphResult();

        getAnalyticAttributes(parameters).forEach(schemaAttribute -> {
            final int scoreAttributeId = schemaAttribute.get(graph);
            if (scoreAttributeId == Graph.NOT_FOUND) {
                throw new RuntimeException("Expected attribute not found on graph: " + schemaAttribute.getName());
            }

            final String identifier = graph.getId();
            final float score = graph.getFloatValue(scoreAttributeId, 0);
            result.add(new GraphScore(identifier, false, schemaAttribute.getName(), score));
        });
    }

    @Override
    public final PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameters analyticPluginParameters = PluginRegistry.get(getAnalyticPlugin().getName()).createParameters();
        if (analyticPluginParameters != null) {
            analyticPluginParameters.getParameters().values().forEach(parameter -> parameters.addParameter(parameter));
        }

        updateParameters(parameters);

        return parameters;
    }

    @Override
    protected final void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        try {
            // prepare the graph
            prepareGraph(graph, parameters);

            // ensure the required analytic attributes exists on the graph
            getAnalyticAttributes(parameters).forEach(schemaAttribute -> schemaAttribute.ensure(graph));

            // run analytic plugin on the entire graph and compute results
            PluginExecution.withPlugin(getAnalyticPlugin().getDeclaredConstructor().newInstance())
                    .withParameters(parameters)
                    .executeNow(graph);
            computeResultsFromGraph(graph, parameters);
        } catch (final IllegalAccessException | IllegalArgumentException
                | InstantiationException | NoSuchMethodException
                | SecurityException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public final GraphResult getResults() {
        return result;
    }

    @Override
    public final Class<? extends AnalyticResult<?>> getResultType() {
        return GraphResult.class;
    }
}
