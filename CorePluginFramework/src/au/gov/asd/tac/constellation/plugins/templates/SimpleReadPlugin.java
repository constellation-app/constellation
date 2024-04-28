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
package au.gov.asd.tac.constellation.plugins.templates;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.plugins.AbstractPlugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginExecutionStageConstants;
import java.util.logging.Logger;

/**
 * A plugin template for plugins that only require read access to the graph.
 * <p>
 * This template will:
 * <ol>
 * <li>Set the graph to busy.</li>
 * <li>Start the progress bar.</li>
 * <li>Get and release a read lock on the graph.</li>
 * <li>Call the read method where developers should implement their plugin
 * logic.</li>
 * </ol>
 *
 * @author sirius
 */
public abstract class SimpleReadPlugin extends AbstractPlugin {

    protected SimpleReadPlugin() {
    }

    protected SimpleReadPlugin(String pluginName) {
        super(pluginName);
    }

    @Override
    public final void run(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph graph = graphs.getGraph();
        final int totalSteps = -1;
        // Make the graph appear busy
        interaction.setBusy(graph.getId(), true);
        try {
            // Make the progress bar appear nondeterminent
            interaction.setExecutionStage(0, totalSteps, PluginExecutionStageConstants.WAITING, "Waiting...", true);

            try {
                ReadableGraph readableGraph = graph.getReadableGraph();

                try {
                    interaction.setExecutionStage(1, totalSteps, PluginExecutionStageConstants.RUNNING, "Working...", true);
                    read(readableGraph, interaction, parameters);
                } finally {
                    readableGraph.release();
                }
            } finally {
                interaction.setExecutionStage(2, 1, PluginExecutionStageConstants.COMPLETE, "Finished", true);
            }
        } finally {
            interaction.setBusy(graph.getId(), false);
        }
    }

    @Override
    public void run(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        // Make the graph appear busy
        interaction.setBusy(graph.getId(), true);
        try {
            // Make the progress bar appear nondeterminent
            interaction.setExecutionStage(0, -1, PluginExecutionStageConstants.RUNNING, "Working...", true);

            try {
                read(graph, interaction, parameters);
            } finally {
                interaction.setExecutionStage(1, 0, PluginExecutionStageConstants.COMPLETE, "Finished", true);
            }
        } finally {
            interaction.setBusy(graph.getId(), false);
        }
    }

    @Override
    public final void run(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        run((GraphReadMethods) graph, interaction, parameters);
    }

    /**
     * Developers should implement this method to implement the logic of their
     * plugin.
     *
     * @param graph a GraphReadMethods object representing a current read lock
     * on the graph.
     * @param interaction A PluginInteraction object allowing interaction with
     * the Constellation UI.
     * @param parameters the parameters used to configure the plugin execution.
     * @throws InterruptedException if the plugin execution is canceled.
     * @throws PluginException if an anticipated error occurs during plugin
     * execution.
     */
    protected abstract void read(GraphReadMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException;
}
