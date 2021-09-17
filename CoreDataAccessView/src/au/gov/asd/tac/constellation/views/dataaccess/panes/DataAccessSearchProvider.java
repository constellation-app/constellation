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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 * Data Access Search Provider
 *
 * @author algol
 */
public class DataAccessSearchProvider implements SearchProvider {
    private static final Logger LOGGER = Logger.getLogger(DataAccessSearchProvider.class.getName());

    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        final String text;
        if (request != null && request.getText() != null) {
            text = request.getText().toLowerCase();
        } else {
            return;
        }

        final Map<String, List<DataAccessPlugin>> plugins;
        try {
            plugins = DataAccessPaneState.getPlugins();
        } catch (ExecutionException | InterruptedException ex) {
            // Technically this shouldn't happen as in order for this block to be
            // entered the plugins would have need to be successfully loaded
            LOGGER.log(Level.SEVERE, "Failed to load data access plugins", ex);
            throw new RuntimeException("Failed to load data access plugins. "
                    + "Data Access View failed to load.");
        }
        
        final List<String> pluginNames = new ArrayList<>();
        plugins.values().stream().forEach(dapl -> {
            for (final DataAccessPlugin dap : dapl) {
                if (dap.getName().toLowerCase().contains(text)) {
                    pluginNames.add(dap.getName());
                }
            }
        });

        Collections.sort(pluginNames, (a, b) -> a.compareToIgnoreCase(b));

        for (final String name : pluginNames) {
            if (!response.addResult(new ShowDataAccessPluginTask(name), name)) {
                return;
            }
        }
    }
}
