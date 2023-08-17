/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.node.plugins.ThreadConstraints;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.graph.utilities.io.CopyGraphUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.dataaccess.GlobalParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A plugin template which separates the seed set into batches, and runs each
 * batch through a defined workflow (ie. a series of plugins) concurrently.
 *
 * @author twilight_sparkle
 */
public abstract class WorkflowQueryPlugin extends SimplePlugin {
    
    private static final String WORKFLOW_GROUP = "workflow_group";
    public static final String BATCH_SIZE_PARAMETER_ID = PluginParameter.buildId(WorkflowQueryPlugin.class, "batch_size");
    private static final String BATCH_SIZE_PARAM_NAME = "Batch Size";
    private static final String BATCH_SIZE_PARAM_DESCRIPTION = "The number of nodes to process in each batch";
    private static final int BATCH_SIZE_PARAM_DEFAULT = 100;
    public static final String MAX_CONCURRENT_PLUGINS_PARAMETER_ID = PluginParameter.buildId(WorkflowQueryPlugin.class, "max_concurrent_plugins");
    private static final String MAX_CONCURRENT_THREADS_PARAM_NAME = "Max Concurrent Plugins";
    private static final String MAX_CONCURRENT_THREADS_PARAM_DESCRIPTION = "The maximum number of plugins to run concurrently, with each plugin handling one batch.";
    private static final int MAX_CONCURRENT_THREADS_PARAM_DEFAULT = 25;
    private static final String THREAD_POOL_NAME = "Workflow Query Plugin";
    private static final Logger LOGGER = Logger.getLogger(WorkflowQueryPlugin.class.getName());
    
    private List<GraphRecordStore> queryBatches;
    
    @Override
    protected void execute(final PluginGraphs pluginGraphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph graph = pluginGraphs.getGraph();
        final ReadableGraph readableGraph = graph.getReadableGraph();

        // buildId batches
        try {
            queryBatches = GraphRecordStoreUtilities.SOURCE.equals(getRecordStoreType())
                    ? GraphRecordStoreUtilities.getSelectedVerticesBatches(readableGraph, parameters.getIntegerValue(BATCH_SIZE_PARAMETER_ID))
                    : GraphRecordStoreUtilities.getSelectedTransactionBatches(readableGraph, parameters.getIntegerValue(BATCH_SIZE_PARAMETER_ID));
        } finally {
            readableGraph.release();
        }
        pluginGraphs.waitAtGate(1);

        // create a service for executing jobs, limiting concurrent executions to the max concurrent plugins parameter.
        final int maxConcurrentPlugins = parameters.getIntegerValue(MAX_CONCURRENT_PLUGINS_PARAMETER_ID);

        // Note that we are not using the global thread pool here so that we can further limit the number of concurrent plugins we can run at once
        // via the max concurrent plugins parameter
        final ExecutorService workflowExecutor = Executors.newFixedThreadPool(maxConcurrentPlugins);

        // schedule a job for each batch, where the job is to execute the defined workflow
        final List<Future<?>> workerPlugins = new ArrayList<>();
        final List<PluginException> exceptions = new ArrayList<>();
        if (queryBatches.isEmpty()) {
            queryBatches.add(new GraphRecordStore());
        }
        final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();
        // run plugin once for every batch record store
        queryBatches.forEach(batch -> {
            final StoreGraph batchGraph = new StoreGraph(graph.getSchema() != null ? graph.getSchema().getFactory().createSchema() : null);
            batchGraph.getSchema().newGraph(batchGraph);
            CopyGraphUtilities.copyGraphTypeElements(readableGraph, batchGraph);
            
            final Map<String, Integer> vertexMap = new HashMap<>();
            final Map<String, Integer> transactionMap = new HashMap<>();
            
            batch.reset();
            while (batch.next()) {
                if (GraphRecordStoreUtilities.SOURCE.equals(getRecordStoreType())) {
                    final String id = batch.get(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID);
                    vertexMap.put(id, Integer.valueOf(id));
                } else {
                    final String sid = batch.get(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID);
                    final String did = batch.get(GraphRecordStoreUtilities.DESTINATION + GraphRecordStoreUtilities.ID);
                    final String tid = batch.get(GraphRecordStoreUtilities.TRANSACTION + GraphRecordStoreUtilities.ID);
                    
                    vertexMap.put(sid, Integer.valueOf(sid));
                    vertexMap.put(did, Integer.valueOf(did));
                    transactionMap.put(tid, Integer.valueOf(tid));
                }
            }
            
            GraphRecordStoreUtilities.addRecordStoreToGraph(batchGraph, batch, true, true, null, vertexMap, transactionMap);
            final WorkerQueryPlugin worker = new WorkerQueryPlugin(getWorkflow(), batchGraph, exceptions, getErrorHandlingPlugin(), addPartialResults());
            workerPlugins.add(workflowExecutor.submit(() -> {
                final ThreadConstraints workerConstraints = ThreadConstraints.getConstraints();
                workerConstraints.setCurrentReport(callingConstraints.getCurrentReport());

                Thread.currentThread().setName(THREAD_POOL_NAME);
                try {
                    PluginExecution.withPlugin(worker).withParameters(parameters).executeNow(graph);
                } catch (InterruptedException | PluginException ex) {
                    throw new RuntimeException(ex);
                }
            }));
        });
        
        final int[] workerFailCount = {0};
        for (Future<?> worker : workerPlugins) {
            try {
                worker.get();
            } catch (InterruptedException ex) {
                workerPlugins.forEach(workerToInterrupt -> workerToInterrupt.cancel(true));
                throw ex;
            } catch (ExecutionException ex) {
                workerFailCount[0]++;
            }
        }
        workflowExecutor.shutdown();

        // if there were any errors, collect them and display them to the user
        if (!exceptions.isEmpty()) {
            final StringBuilder entireException = new StringBuilder();
            entireException.append(workerFailCount[0]).append(" workers failed.").append(SeparatorConstants.NEWLINE);
            exceptions.forEach(ex -> entireException.append(ex.getMessage()).append(SeparatorConstants.NEWLINE));
            throw new PluginException(PluginNotificationLevel.ERROR, entireException.toString());
        }
    }
    
    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        // instantiate a copy of each plugin in the workflow to create their parameters and add them to this plugin.
        // this plugin then adds the set parameters values back to the individual workflow plugins at query time.
        final Set<String> globalParameterIds = GlobalParameters.getParameters(null).getParameters().keySet();
        getWorkflow().forEach(pluginName -> {
            final PluginParameters pluginParameters = PluginRegistry.get(pluginName).createParameters();
            if (pluginParameters != null && !pluginParameters.getParameters().isEmpty() && !parameters.hasGroup(pluginName)) {
                parameters.addGroup(pluginName, new PluginParametersPane.TitledSeparatedParameterLayout(String.format("%s Parameters", PluginRegistry.get(pluginName).getName()), 12, false));
                pluginParameters.getParameters().entrySet().forEach(parameter -> {
                    if (!parameters.hasParameter(parameter.getKey())) {
                        if (globalParameterIds.contains(parameter.getKey())) {
                            parameters.addParameter(parameter.getValue());
                        } else {
                            parameters.addParameter(parameter.getValue(), pluginName);
                        }
                    }
                });
                pluginParameters.getControllers().entrySet().forEach(controller -> {
                    if (!parameters.getControllers().containsKey(controller)) {
                        parameters.addController(controller.getKey(), controller.getValue());
                    }
                });
            }
        });

        // add parameters specific to a batched workflow plugin
        parameters.addGroup(WORKFLOW_GROUP, new PluginParametersPane.TitledSeparatedParameterLayout("Workflow Parameters", 12, false));
        
        final PluginParameter<IntegerParameterValue> batchSizeParameter = IntegerParameterType.build(BATCH_SIZE_PARAMETER_ID);
        batchSizeParameter.setName(BATCH_SIZE_PARAM_NAME);
        batchSizeParameter.setDescription(BATCH_SIZE_PARAM_DESCRIPTION);
        batchSizeParameter.setIntegerValue(getDefaultBatchSize());
        parameters.addParameter(batchSizeParameter, WORKFLOW_GROUP);
        
        final PluginParameter<IntegerParameterValue> maxConcurrentPluginsParam = IntegerParameterType.build(MAX_CONCURRENT_PLUGINS_PARAMETER_ID);
        maxConcurrentPluginsParam.setName(MAX_CONCURRENT_THREADS_PARAM_NAME);
        maxConcurrentPluginsParam.setDescription(MAX_CONCURRENT_THREADS_PARAM_DESCRIPTION);
        maxConcurrentPluginsParam.setIntegerValue(getDefaultConcurrentThreads());
        parameters.addParameter(maxConcurrentPluginsParam, WORKFLOW_GROUP);
        
        return parameters;
    }
    
    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {
        getWorkflow().forEach(pluginName -> PluginRegistry.get(pluginName).updateParameters(graph, parameters));
    }
    
    protected int getDefaultBatchSize() {
        return BATCH_SIZE_PARAM_DEFAULT;
    }
    
    protected int getDefaultConcurrentThreads() {
        return MAX_CONCURRENT_THREADS_PARAM_DEFAULT;
    }
    
    public abstract List<String> getWorkflow();
    
    public abstract String getErrorHandlingPlugin();
    
    public boolean addPartialResults() {
        return false;
    }
    
    /**
     * Returns the type of 'query' RecordStore that is generated from the graph
     * in this plugin's read stage. The options are RecordStoreUtilities.SOURCE, 
     * which creates a RecordStore representing the graph's nodes only, 
     * RecordStoreUtilities.TRANSACTION, which creates a RecordStore
     * representing the graph's transactions only, and RecordStoreUtilities.ALL, 
     * which creates a RecordStore representing the whole graph.
     * 
     * Implementation should be sure to override this if their query needs to 
     * work on something other than the graph's nodes only (the default)
     * 
     * @return A String representing the type of 'query' RecordStore to be 
     * generated from the graph
     */
    public String getRecordStoreType() {
        return GraphRecordStoreUtilities.SOURCE;
    }
    
    private static class WorkerQueryPlugin extends SimplePlugin {
        
        final List<Plugin> plugins = new ArrayList<>();
        final StoreGraph batchGraph;
        final StoreGraph originalGraph;
        final List<PluginException> wholeOfWorkflowExceptions;
        final Plugin errorHandlingPlugin;
        final boolean addPartialResults;
        static int pluginNum = 0;
        final int pluginNumber;
        
        private WorkerQueryPlugin(final List<String> pluginNames, final StoreGraph batchGraph, final List<PluginException> wholeOfWorkflowExceptions, final String errorHandlingPlugin, final boolean addPartialResults) {
            pluginNames.forEach(pluginName -> plugins.add(PluginRegistry.get(pluginName)));
            this.errorHandlingPlugin = errorHandlingPlugin == null ? null : PluginRegistry.get(errorHandlingPlugin);
            this.batchGraph = batchGraph;
            this.originalGraph = new StoreGraph(batchGraph);
            this.pluginNumber = ++pluginNum;
            this.wholeOfWorkflowExceptions = wholeOfWorkflowExceptions;
            this.addPartialResults = addPartialResults;
        }
        
        @Override
        public String getName() {
            return "Worker Query Plugin";
        }
        
        @Override
        protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            boolean error = false;
            for (final Plugin plugin : plugins) {
                // create the parameters for the plugin, including any global parameters it requires
                PluginParameters pluginSpecificParameters = plugin.createParameters();
                if (pluginSpecificParameters != null) {
                    parameters.getParameters().values().forEach(param -> {
                        if (pluginSpecificParameters.hasParameter(param.getId())) {
                            pluginSpecificParameters.setObjectValue(param.getId(), param.getObjectValue());
                        }
                    });
                }

                // run the plugin
                try {
                    LOGGER.log(Level.INFO, "Running {0}", plugin.getName());
                    PluginExecution.withPlugin(plugin)
                            .withParameters(pluginSpecificParameters)
                            .executeNow(batchGraph);
                } catch (PluginException ex) {
                    wholeOfWorkflowExceptions.add(ex);
                    error = true;
                    break;
                }
            }

            // handle any errors which occurred
            if (error && errorHandlingPlugin != null) {
                PluginExecution.withPlugin(errorHandlingPlugin).executeNow(addPartialResults ? batchGraph : originalGraph);
            }
            
            PluginExecution.withPlugin(new AddToGraphPlugin(error && !addPartialResults ? originalGraph : batchGraph)).executeLater(graphs.getGraph());
        }
    }
    
    private static class AddToGraphPlugin extends SimpleEditPlugin {
        
        private final StoreGraph copyGraph;
        
        public AddToGraphPlugin(final StoreGraph batchGraph) {
            this.copyGraph = batchGraph;
        }
        
        @Override
        public String getName() {
            return "Add to Graph Plugin";
        }
        
        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            CopyGraphUtilities.copyGraphToGraph(copyGraph, graph, true);
            PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
        }
    }
}
