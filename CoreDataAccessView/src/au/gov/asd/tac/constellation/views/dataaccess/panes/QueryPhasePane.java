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

import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPaneListener;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * A panel for selecting plug-ins to run during a query phase.
 *
 * @author cygnus_x-1
 * @author ruby_crucis
 */
public class QueryPhasePane extends VBox {

    private final GlobalParametersPane globalParametersPane;
    private final VBox dataSourceList = new VBox();
    private final List<DataSourceTitledPane> dataSources = new ArrayList<>();

    private final Set<MenuItem> graphDependentMenuItems = new HashSet<>();
    private final Set<MenuItem> pluginDependentMenuItems = new HashSet<>();

    public QueryPhasePane(final Map<String, List<DataAccessPlugin>> plugins, final PluginParametersPaneListener top, final PluginParameters presetGlobalParms) {
        globalParametersPane = new GlobalParametersPane(presetGlobalParms);

        for (final Map.Entry<String, List<DataAccessPlugin>> pluginsOfType : plugins.entrySet()) {
            final List<DataAccessPlugin> pluginList = pluginsOfType.getValue();
            if (!pluginList.isEmpty()) {
                final String type = pluginsOfType.getKey();
                final HeadingPane heading = new HeadingPane(type, pluginList, top, globalParametersPane.getParamLabels());
                dataSourceList.getChildren().add(heading);
                dataSources.addAll(heading.getDataSources());
            }
        }

        setFillWidth(true);
        getChildren().addAll(globalParametersPane, dataSourceList);
    }

    public void enableGraphDependentMenuItems(boolean enabled) {
        for (MenuItem menuItem : graphDependentMenuItems) {
            menuItem.setDisable(!enabled);
        }
    }

    public void addGraphDependentMenuItems(MenuItem... menuItems) {
        graphDependentMenuItems.addAll(Arrays.asList(menuItems));
    }

    public void enablePluginDependentMenuItems(boolean enabled) {
        for (MenuItem menuItem : pluginDependentMenuItems) {
            menuItem.setDisable(!enabled);
        }
    }

    public void addPluginDependentMenuItems(MenuItem... menuItems) {
        pluginDependentMenuItems.addAll(Arrays.asList(menuItems));
    }

    /**
     * Get the actual data access panes. These can then be queried to see which
     * ones are enabled and to get the parameters.
     *
     * @return a list of the data access panes.
     */
    public List<DataSourceTitledPane> getDataAccessPanes() {
        return dataSources;
    }

    public GlobalParametersPane getGlobalParametersPane() {
        return globalParametersPane;
    }

    /**
     * Expand headings
     *
     * @param value Expand heading section
     * @param childrenAlso Expand child section
     */
    public void setHeadingsExpanded(final boolean value, final boolean childrenAlso) {
        dataSourceList.getChildren().stream().forEach(child -> {
            final HeadingPane headingPage = (HeadingPane) child;
            headingPage.setExpanded(value);
            if (childrenAlso) {
                headingPage.getDataSources().stream().forEach(tp -> {
                    tp.setExpanded(value);
                });
            }
        });
    }

    /**
     * Expand the named plugin (and the section it's in) and scroll it to a
     * visible position.
     * <p>
     * All other plugins and sections will be unexpanded.
     *
     * @param pluginName the name of the plugin to expand.
     */
    public void expandPlugin(final String pluginName) {
        setHeadingsExpanded(false, true);
        for (final Node node : dataSourceList.getChildrenUnmodifiable()) {
            final HeadingPane headingPane = (HeadingPane) node;
            for (final DataSourceTitledPane tp : headingPane.getDataSources()) {
                if (pluginName.equals(tp.getPlugin().getName())) {
                    headingPane.setExpanded(true);
                    tp.setExpanded(true);
                    final ScrollPane sp = (ScrollPane) getParent().getParent().getParent();
                    Platform.runLater(() -> {
                        sp.setHvalue(0);
                        final double v = Math.min(headingPane.getLayoutY() + tp.getLayoutY() + tp.getHeight() / 2, getHeight());
                        sp.setVvalue(v);
                    });
                    break;
                }
            }
        }
    }

    /**
     * Expand the section matching the plugin text
     *
     * @param text The text to search for across plugin names
     */
    public void showMatchingPlugins(final String text) {
        if (text != null) {
            for (final Node node : dataSourceList.getChildrenUnmodifiable()) {
                final HeadingPane headingPane = (HeadingPane) node;
                boolean shouldExpand = false;
                for (final DataSourceTitledPane tp : headingPane.getDataSources()) {
                    tp.getStyleClass().remove(DataSourceTitledPane.MATCHED_STYLE);
                    if (!text.isEmpty() && tp.getPlugin().getName().toLowerCase().contains(text.toLowerCase())) {
                        tp.getStyleClass().add(DataSourceTitledPane.MATCHED_STYLE);
                        shouldExpand = true;
                    }
                }
                if (shouldExpand != headingPane.isExpanded()) {
                    headingPane.setExpanded(shouldExpand);
                }
            }
        }
    }
}
