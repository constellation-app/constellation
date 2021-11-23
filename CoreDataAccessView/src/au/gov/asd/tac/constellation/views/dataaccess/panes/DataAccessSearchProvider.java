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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 * Netbeans {@link SearchProvider} that backs the querying of data access plugins
 * in the quick search box.
 *
 * @author algol
 */
public class DataAccessSearchProvider implements SearchProvider {

    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        final String text;
        if (request != null && StringUtils.isNotBlank(request.getText())) {
            text = request.getText().toLowerCase();
        } else {
            return;
        }

        // Get all the available data access plugins
        final Map<String, List<DataAccessPlugin>> plugins;
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

        // Find all matching plugin names
        final List<String> pluginNames = plugins.values().stream()
                // Flatten everything to a single stream of plugins
                .flatMap(Collection::stream)
                // Filter out plugins whose name do NOT contain the filter text
                .filter(plugin -> plugin.getName().toLowerCase().contains(text))
                .map(DataAccessPlugin::getName)
                .sorted((a, b) -> a.compareToIgnoreCase(b))
                .collect(Collectors.toList());

        for (final String name : pluginNames) {
            if (!response.addResult(new ShowDataAccessPluginTask(name), name)) {
                return;
            }
        }
    }
}
