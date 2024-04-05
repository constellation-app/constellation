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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.SubgraphUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.FactResult;
import au.gov.asd.tac.constellation.views.analyticview.results.FactResult.ElementFact;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A basic analytic plugin which reads facts from the graph based on the
 * execution of a single pre-existing plugin. This plugin can optionally be told
 * to compute only on a subset of the graph made up of a specified set of
 * transaction types.
 *
 * @author cygnus_x-1
 */
public abstract class FactAnalyticPlugin extends AnalyticPlugin<FactResult> {
    
    private static final Logger LOGGER = Logger.getLogger(FactAnalyticPlugin.class.getName());

    public static final String TRANSACTION_TYPES_PARAMETER_ID = PluginParameter.buildId(ScoreAnalyticPlugin.class, "transaction_types");

    protected FactResult result;

    public abstract Class<? extends Plugin> getAnalyticPlugin();

    public boolean ignoreDefaultValues() {
        return true;
    }

    protected final StoreGraph getSubgraph(final GraphWriteMethods graph, final SchemaFactory schemaFactory, final Set<SchemaTransactionType> subgraphTransactionTypes) {
        return SubgraphUtilities.getSubgraph(graph, schemaFactory.createSchema(), subgraphTransactionTypes, false);
    }

    protected final void copySubgraphToGraph(final GraphWriteMethods graph, final StoreGraph subgraph) {
        final RecordStore subgraphStore = GraphRecordStoreUtilities.getAll(subgraph, false, false);
        GraphRecordStoreUtilities.addRecordStoreToGraph(graph, subgraphStore, false, false, null);
    }

    protected final void computeResultsFromGraph(final GraphReadMethods graph, final PluginParameters parameters) {
        result = new FactResult();
        result.setIgnoreNullResults(ignoreDefaultValues());

        getAnalyticAttributes(parameters).forEach(schemaAttribute -> {
            int graphElementCount = 0;
            int identifierAttributeId = Graph.NOT_FOUND;

            final int factAttributeId = schemaAttribute.get(graph);
            if (factAttributeId == Graph.NOT_FOUND) {
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
            }

            for (int graphElementPosition = 0; graphElementPosition < graphElementCount; graphElementPosition++) {
                final int graphElementId;
                if (graphElementType == GraphElementType.VERTEX) {
                    graphElementId = graph.getVertex(graphElementPosition);
                } else {
                    graphElementId = graphElementType == GraphElementType.TRANSACTION ? graph.getTransaction(graphElementPosition) : Graph.NOT_FOUND;
                }
                final String identifier = graph.getStringValue(identifierAttributeId, graphElementId);
                final boolean fact = graph.getBooleanValue(factAttributeId, graphElementId);
                final boolean defaultFact = (boolean) graph.getAttributeDefaultValue(factAttributeId);
                final boolean isNull = ignoreDefaultValues() && fact == defaultFact;
                result.add(new ElementFact(graphElementType, graphElementId, identifier, isNull, schemaAttribute.getName(), fact));
            }
        });
    }

    @Override
    public Set<SchemaAttribute> getPrerequisiteAttributes() {
        final Set<SchemaAttribute> analyticAttributes = new HashSet<>();
        analyticAttributes.add(AnalyticConcept.TransactionAttribute.TYPE);
        return analyticAttributes;
    }

    @Override
    public void onPrerequisiteAttributeChange(final Graph graph, final PluginParameters parameters) {
        final Set<TransactionTypeParameterValue> transactionTypes = new HashSet<>();

        if (graph != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final int typeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.get(readableGraph);
                final int transactionCount = readableGraph.getTransactionCount();
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = readableGraph.getTransaction(transactionPosition);
                    final SchemaTransactionType type = readableGraph.getObjectValue(typeAttributeId, transactionId);
                    transactionTypes.add(new TransactionTypeParameterValue(type));
                }
            } finally {
                readableGraph.release();
            }
        }

        @SuppressWarnings("unchecked") //TRANSACTION_TYPES_PARAMETER always of type MultiChoiceParameter
        final PluginParameter<MultiChoiceParameterValue> transactionTypesParam = (PluginParameter<MultiChoiceParameterValue>) parameters.getParameters().get(TRANSACTION_TYPES_PARAMETER_ID);
        MultiChoiceParameterType.setOptionsData(transactionTypesParam, new ArrayList<>(transactionTypes));
        MultiChoiceParameterType.setChoicesData(transactionTypesParam, new ArrayList<>(transactionTypes));
    }

    @Override
    public final PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<MultiChoiceParameterValue> transactionTypeParameter = MultiChoiceParameterType.build(TRANSACTION_TYPES_PARAMETER_ID, TransactionTypeParameterValue.class);
        transactionTypeParameter.setName("Transaction Types");
        transactionTypeParameter.setDescription("Calculate analytic only on the subgraph of transactions of these types");
        MultiChoiceParameterType.setOptionsData(transactionTypeParameter, new ArrayList<>());
        MultiChoiceParameterType.setChoicesData(transactionTypeParameter, new ArrayList<>());
        parameters.addParameter(transactionTypeParameter);

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

            @SuppressWarnings("unchecked") //TRANSACTION_TYPES_PARAMETER always of type MultiChoiceParameter
            final PluginParameter<MultiChoiceParameterValue> transactionTypesParameter = (PluginParameter<MultiChoiceParameterValue>) parameters.getParameters().get(TRANSACTION_TYPES_PARAMETER_ID);
            final List<ParameterValue> allTransactionTypes = MultiChoiceParameterType.getOptionsData(transactionTypesParameter);
            final List<? extends ParameterValue> chosenTransactionTypes = MultiChoiceParameterType.getChoicesData(transactionTypesParameter);
            if (chosenTransactionTypes.equals(allTransactionTypes)) {
                // run analytic plugin on the entire graph and compute results
                PluginExecution.withPlugin(getAnalyticPlugin().getDeclaredConstructor().newInstance())
                        .withParameters(parameters)
                        .executeNow(graph);
                computeResultsFromGraph(graph, parameters);
            } else {
                // create subgraph
                final Set<SchemaTransactionType> transactionTypes = new HashSet<>();
                chosenTransactionTypes.forEach(parameterValue
                        -> transactionTypes.add((SchemaTransactionType) ((TransactionTypeParameterValue) parameterValue).getObjectValue()));
                assert transactionTypes.size() > 0 : "You must select at least one transaction type";
                final StoreGraph subgraph = getSubgraph(graph, SchemaFactoryUtilities.getDefaultSchemaFactory(), transactionTypes);

                // run analytic plugin on the subgraph and compute results
                PluginExecution.withPlugin(getAnalyticPlugin().getDeclaredConstructor().newInstance())
                        .withParameters(parameters)
                        .executeNow(subgraph);
                copySubgraphToGraph(graph, subgraph);
                computeResultsFromGraph(graph, parameters);
            }
        } catch (final IllegalAccessException | IllegalArgumentException
                | InstantiationException | NoSuchMethodException
                | SecurityException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public final FactResult getResults() {
        return result;
    }

    @Override
    public final Class<? extends AnalyticResult<?>> getResultType() {
        return FactResult.class;
    }

    public static final class TransactionTypeParameterValue extends ParameterValue {

        private SchemaTransactionType transactionType;

        public TransactionTypeParameterValue() {
            this.transactionType = null;
        }

        public TransactionTypeParameterValue(SchemaTransactionType transactionType) {
            this.transactionType = transactionType;
        }

        @Override
        public final String validateString(final String s) {
            SchemaTransactionType type = SchemaTransactionTypeUtilities.getType(s);
            return type.equals(SchemaTransactionTypeUtilities.getDefaultType())
                    ? "Invalid transaction type provided." : null;
        }

        @Override
        public final boolean setStringValue(final String s) {
            SchemaTransactionType type = SchemaTransactionTypeUtilities.getType(s);
            final boolean changed = Objects.equals(type, transactionType);
            transactionType = type;
            return changed;
        }

        @Override
        public final Object getObjectValue() {
            return transactionType;
        }

        @Override
        public final boolean setObjectValue(final Object o) {
            if (o instanceof SchemaTransactionType && !transactionType.getClass().equals(o.getClass())) {
                transactionType = (SchemaTransactionType) o;
                return true;
            }

            return false;
        }

        @Override
        protected final ParameterValue createCopy() {
            return new TransactionTypeParameterValue(transactionType);
        }

        @Override
        public final String toString() {
            return transactionType == null ? "No Value" : transactionType.getName();
        }

        @Override
        public final int hashCode() {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.transactionType);
            return hash;
        }

        @Override
        public final boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TransactionTypeParameterValue other = (TransactionTypeParameterValue) obj;
            return Objects.equals(this.transactionType, other.transactionType);
        }
    }
}
