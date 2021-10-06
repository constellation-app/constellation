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
package au.gov.asd.tac.constellation.views.find.quicksearch;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.advanced.FindResult;
import au.gov.asd.tac.constellation.views.find.advanced.QuickFindPlugin;
import au.gov.asd.tac.constellation.views.find.advanced.SelectFindResultsPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 * Class responsible for managing searching for content from the QuickSearch
 * box.
 * <p>
 * Note: This is a NetBeans platform specific feature.
 *
 * @author betelgeuse
 */
public class NodeQuickSearchProvider implements SearchProvider {
    
    private static final Logger LOGGER = Logger.getLogger(NodeQuickSearchProvider.class.getName());

    private final GraphRetriever graphRetriever = new GraphRetriever();

    /**
     * Determines whether any content from the current Graph matches the search
     * term.
     *
     * @param request The content being entered into the QuickSearch box.
     * @param response The content to be returned to the QuickSearch box.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        final Graph graph = graphRetriever.getGraph();

        if (graph != null) {
            final QuickFindPlugin plugin = new QuickFindPlugin(GraphElementType.VERTEX, request.getText());
            final Future<?> future = PluginExecution.withPlugin(plugin).interactively(true).executeLater(graph);

            // Wait for results:
            try {
                future.get();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Quick Find was interrupted", ex);
                Thread.currentThread().interrupt();
            } catch (final ExecutionException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

            final List<FindResult> results = plugin.getResults();
            for (FindResult item : results) {
                if (item != null) {
                    // We have a valid result, so report:
                    response.addResult(new SelectContent(graph, item), item.toString());
                }
            }
        }
    }

    /**
     * Private class that defines the actions to be performed when the item has
     * been selected in the QuickSearch box.
     */
    private final class SelectContent implements Runnable {

        private final FindResult result;
        private final Graph graph;

        /**
         * Constructs a SelectContent.
         *
         * @param searchTerm The particular FindResult that needs to be assigned
         * this action.
         */
        public SelectContent(final Graph graph, final FindResult result) {
            this.graph = graph;
            this.result = result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            final ArrayList<FindResult> results = new ArrayList<>();
            results.add(result);

            final SelectFindResultsPlugin plugin = new SelectFindResultsPlugin(results, false);
            PluginExecution.withPlugin(plugin).interactively(true).executeLater(graph);
        }
    }
}
