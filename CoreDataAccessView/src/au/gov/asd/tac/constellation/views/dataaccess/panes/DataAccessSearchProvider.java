/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.NotificationDisplayer;

/**
 * Data Access Search Provider
 *
 * @author algol
 */
public class DataAccessSearchProvider implements SearchProvider {

    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        final String text = request.getText().toLowerCase();
        final Map<String, List<DataAccessPlugin>> plugins = DataAccessPane.lookupPlugins();

        final List<String> pluginNames = new ArrayList<>();
        plugins.values().stream().forEach((dapl) -> {
            for (final DataAccessPlugin dap : dapl) {
                if (dap.getName().toLowerCase().contains(text)) {
                    pluginNames.add(dap.getName());
                }
            }
        });

        Collections.sort(pluginNames, (a, b) -> a.compareToIgnoreCase(b));

        for (final String name : pluginNames) {
            if (!response.addResult(new PluginDisplayer(name), name)) {
                return;
            }
        }
    }

    private static class PluginDisplayer implements Runnable {

        private final String pluginName;

        private PluginDisplayer(final String pluginName) {
            this.pluginName = pluginName;
        }

        @Override
        public void run() {
            final String message;
            final DataAccessPane dapane = DataAccessUtilities.getDataAccessPane();
            if (dapane != null) {
                final Tab tab = dapane.getCurrentTab();
                if (tab != null) {
                    final QueryPhasePane queryPhasePane = DataAccessPane.getQueryPhasePane(tab);
                    Platform.runLater(() -> {
                        queryPhasePane.expandPlugin(pluginName);
                    });

                    return;
                } else {
                    message = "Please create a step in the Data Access view.";
                }
            } else {
                message = "Please open the Data Access view and create a step.";
            }

            NotificationDisplayer.getDefault().notify("Data Access view",
                    UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                    message,
                    null
            );
        }
    }
}
