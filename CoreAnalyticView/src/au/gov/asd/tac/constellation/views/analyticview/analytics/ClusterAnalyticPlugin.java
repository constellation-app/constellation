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
import static au.gov.asd.tac.constellation.graph.GraphElementType.TRANSACTION;
import static au.gov.asd.tac.constellation.graph.GraphElementType.VERTEX;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ClusterResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ClusterResult.ClusterData;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A basic analytic plugin which reads cluster data from the graph based on the
 * execution of a single pre-existing plugin.
 *
 * @author cygnus_x-1
 */
public abstract class ClusterAnalyticPlugin extends AnalyticPlugin<ClusterResult> {
    
    private static final Logger LOGGER = Logger.getLogger(ClusterAnalyticPlugin.class.getName());

    protected ClusterResult result;

    protected abstract Class<? extends Plugin> getAnalyticPlugin();

    protected boolean ignoreDefaultValues() {
        return true;
    }

    protected final void computeResultsFromGraph(final GraphReadMethods graph, final PluginParameters parameters) {
        result = new ClusterResult();
        result.setIgnoreNullResults(ignoreDefaultValues());

        getAnalyticAttributes(parameters).forEach(schemaAttribute -> {
            int graphElementCount = 0;
            int identifierAttributeId = Graph.NOT_FOUND;

            final int clusterNumberAtributeId = schemaAttribute.get(graph);
            if (clusterNumberAtributeId == Graph.NOT_FOUND) {
                throw new RuntimeException("Expected attribute not found on graph: " + schemaAttribute.getName());
            }

            final GraphElementType graphElementType = schemaAttribute.getElementType();
            switch (graphElementType) {
                case VERTEX -> {
                    graphElementCount = graph.getVertexCount();
                    identifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
                }
                case TRANSACTION -> {
                    graphElementCount = graph.getTransactionCount();
                    identifierAttributeId = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);
                }
                default -> {
                    // Do nothing 
                }
            }

            for (int graphElementPosition = 0; graphElementPosition < graphElementCount; graphElementPosition++) {
                final int graphElementId;
                if (graphElementType == GraphElementType.VERTEX) {
                    graphElementId = graph.getVertex(graphElementPosition);
                } else {
                    graphElementId = graphElementType == GraphElementType.TRANSACTION ? graph.getTransaction(graphElementPosition) : Graph.NOT_FOUND;
                }
                final String identifier = graph.getStringValue(identifierAttributeId, graphElementId);
                final int clusterNumber = graph.getIntValue(clusterNumberAtributeId, graphElementId);
                final int defaultNumber = (int) graph.getAttributeDefaultValue(clusterNumberAtributeId);
                final boolean isNull = ignoreDefaultValues() && clusterNumber == defaultNumber;
                result.add(new ClusterData(graphElementType, graphElementId, identifier, isNull, clusterNumber));
            }
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
    public final ClusterResult getResults() {
        return result;
    }

    @Override
    public final Class<? extends AnalyticResult<?>> getResultType() {
        return ClusterResult.class;
    }
}
