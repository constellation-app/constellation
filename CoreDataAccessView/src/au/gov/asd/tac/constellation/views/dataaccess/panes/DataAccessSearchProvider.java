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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.tasks.ShowDataAccessPluginTask;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 * Netbeans {@link SearchProvider} that backs the querying of data access plugins
 * in the quick search box.
 * Recognise and Process recent DAV plugin searches
 *
 * @author algol
 */
public class DataAccessSearchProvider implements SearchProvider {

    private static final Logger LOGGER = Logger.getLogger(DataAccessSearchProvider.class.getName());

//    public static final String CIRCLED_D = "\u24b9";
//    public static final String DIAMOND = "\u2666";
//    public static final String RH_HALF_DIAMOND = "\u23f5";
    public static final String LEFT_BRACKET = "\u276a";
    public static final String CIRCLED_D = "\u276a\uff24\u276b\u2005";
    //public static final String DIAMOND = "\u2b2b";
    //public static final String RH_HALF_DIAMOND = "\u208e"; // "\u23f5" \u3017
    

    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        LOGGER.log(Level.SEVERE, " >> EVALUATE ## D.A.V. search - Looking for {" + request + "} - {" + request.getText() + "}");
        final String text;
        if (request != null && StringUtils.isNotBlank(request.getText())) {
            text = request.getText().replace("\u227a","<").replace("\u227b",">").replace("\uff08","(").replace("\uff09",")");
        } else {
            return;
        }

        String prevPluginName = "";
        // Locally defined Recent searches will have a diamond character or half diamond character at the end of the search term
        if (text.startsWith(LEFT_BRACKET)) {
            final int termEnd = text.length();
            // A recent DAV plugin search will begin with a (D) character and end with a diamond character
            if (termEnd > 0 && text.startsWith(CIRCLED_D)) {
                final int termPos = text.indexOf(" ") + 1;
                // Set the recent/previous plugin name to the search term
                prevPluginName = text.substring(termPos, termEnd).trim();
                LOGGER.log(Level.SEVERE, " >> PREV D.A.V. search - Looking for (" + prevPluginName + ")");
            } else {
                LOGGER.log(Level.SEVERE, " >> PREV search NOT for D.A.V");
                // This is a recent search for a different category, so we can end the DAV plugin search here
                return;
            }
        }
        // Get all the available data access plugins
        final Map<String, Pair<Integer, List<DataAccessPlugin>>> plugins;
        try {
            plugins = DataAccessPaneState.getPlugins();
        } catch (ExecutionException ex) {
            throw new IllegalStateException("Failed to load data access plugins. "
                    + "Data Access View cannot be created.");
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException("Failed to load data access plugins. "
                    + "Data Access View cannot be created.");
        }

        final Map<String, List<DataAccessPlugin>> unorderedPlugins = new HashMap<>();

        plugins.entrySet().forEach(entry -> unorderedPlugins.put(entry.getKey(), entry.getValue().getValue()));

        final String comparisonText = "".equals(prevPluginName) ? text : prevPluginName;
        LOGGER.log(Level.SEVERE, " >> D.A.V. search  for (" + comparisonText + ")");
        // Find all matching plugin names
        final List<String> pluginNames = unorderedPlugins.values().stream()
                // Flatten everything to a single stream of plugins
                .flatMap(Collection::stream)
                // Filter out plugins whose name do NOT contain the filter text
                .filter(plugin -> StringUtils.containsIgnoreCase(plugin.getName(), comparisonText))
                .map(DataAccessPlugin::getName)
                .sorted((a, b) -> a.compareToIgnoreCase(b))
                .collect(Collectors.toList());

        for (final String name : pluginNames) {
            final String displayName = CIRCLED_D + "  " + name.replace("<","\u227a").replace(">","\u227b").replace("(" , "\uff08").replace(")", "\uff09");
            if (!"".equals(prevPluginName) && StringUtils.containsIgnoreCase(name, prevPluginName)) {
                // Found the recent DAV plugin search result. Set it and exit immediately
                response.addResult(new ShowDataAccessPluginTask(name), displayName);
                //SwingUtilities.invokeLater(new ShowDataAccessPluginTask(name, currentTime));
                LOGGER.log(Level.SEVERE, " >> FOUND D.A.V. search result : " + name);
                return;
            } else if (!response.addResult(new ShowDataAccessPluginTask(name), displayName)) {
                return;
            }
        }
        LOGGER.log(Level.SEVERE, " >> D.A.V. search  DONE");
    }
}
