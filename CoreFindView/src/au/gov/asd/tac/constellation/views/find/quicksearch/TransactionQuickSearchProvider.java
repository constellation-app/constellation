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
package au.gov.asd.tac.constellation.views.find.quicksearch;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.utilities.FindResult;
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
 * Class responsible for managing searching for Transaction content from the QuickSearch
 * box.
 * Recognise and Process recent Node searches
 * <p>
 * Note: This is a Netbeans platform specific feature.
 *
 * @author betelgeuse
 */
public class TransactionQuickSearchProvider implements SearchProvider {

    private static final Logger LOGGER = Logger.getLogger(TransactionQuickSearchProvider.class.getName());
    
    private final GraphRetriever graphRetriever = new GraphRetriever();

    /**
     * Determines whether any content from the current Graph matches the search
     * term.
     *
     * @param request The content being entered into the QuickSearch box.
     * @param response The content to be returned to the QuickSearch box.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        final Graph graph = graphRetriever.getGraph();
        
        if (graph != null) {
            final String searchRequest = request.getText().replace("\uff1c","<").replace("\uff1e",">").replace("\uff08","(").replace("\uff09",")");
            String prevSearch = "";
            int prevId = -1;
            // Locally defined Recent searches will start with a specific unicode left bracket in the search term
            if (searchRequest.startsWith(FindResult.LEFT_BRACKET)) {
                final int codePos = searchRequest.indexOf(FindResult.LH_SUB_BRACKET) + FindResult.LH_SUB_BRACKET.length();
                if (codePos > FindResult.LH_SUB_BRACKET.length() && searchRequest.startsWith(FindResult.CIRCLED_T)) {
                    final int termPos = searchRequest.indexOf(" ") + 1;
                    prevSearch = searchRequest.substring(termPos, codePos - FindResult.LH_SUB_BRACKET.length()).trim();
                    final String prevIdText = FindResult.buildIDFromSubscript(searchRequest.substring(codePos, searchRequest.length() - FindResult.RH_SUB_BRACKET.length()));
                    prevId = Integer.parseInt(prevIdText);
                    final int attrPos = prevSearch.lastIndexOf(FindResult.SEPARATOR);
                    if (attrPos > -1) {
                        prevSearch = prevSearch.substring(0, attrPos);
                    }
                } else {
                    // This is a recent search for a different category, so we can end the Transaction search here
                    return;
                }
            }
            final String convertedSearch = !"".equals(prevSearch) ? prevSearch : searchRequest;
            final QuickFindPlugin plugin = new QuickFindPlugin(GraphElementType.TRANSACTION, convertedSearch);
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
            final List<FindResult> matchList = new ArrayList<>();
            for (final FindResult item : results) {
                if (item != null) {
                    // We have a valid result, so report:
                    final String subscriptId = FindResult.buildSubscriptFromID(Integer.toString(item.getID()));
                    final String displayText = FindResult.CIRCLED_T + "  " + item.toString().replace("<","\uff1c").replace(">","\uff1e").replace("\uff08","(").replace("\uff09",")") + "   " + FindResult.LH_SUB_BRACKET + subscriptId + FindResult.RH_SUB_BRACKET;
                    if ("".equals(prevSearch)) {
                        response.addResult(new SelectContent(graph, item), displayText);
                    } else if (item.toString().contains(prevSearch) && item.getID() == prevId) {
                        // Found the recent Transaction search result. Set it and exit immediately
                        response.addResult(new SelectContent(graph, item), displayText);
                        return;
                    } else if (item.toString().contains(prevSearch)) {
                        // can potentially match the search term on a different graph, store the matches
                        matchList.add(item);
                    }
                }
            }
            if (!matchList.isEmpty()) {
                // should only return 1 result when using the recent search function
                final FindResult result = matchList.get(0);
                final String subscriptId = FindResult.buildSubscriptFromID(Integer.toString(result.getID()));
                String displayText = FindResult.CIRCLED_T + "  " + result.toString().replace("<","\uff1c").replace(">","\uff1e").replace("\uff08","(").replace("\uff09",")") + "   " + FindResult.LH_SUB_BRACKET + subscriptId + FindResult.RH_SUB_BRACKET;
                response.addResult(new SelectContent(graph, result), displayText);                
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
            final List<FindResult> results = new ArrayList<>();
            results.add(result);

            final SelectFindResultsPlugin plugin = new SelectFindResultsPlugin(results, false);
            PluginExecution.withPlugin(plugin).interactively(true).executeLater(graph);
        }
    }
}