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
package au.gov.asd.tac.constellation.views.schemaview.providers;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.openide.util.lookup.ServiceProvider;

/**
 * Plugins Node Provider
 *
 * @author algol
 */
@ServiceProvider(service = SchemaViewNodeProvider.class, position = 300)
public class PluginsNodeProvider implements SchemaViewNodeProvider {

    private static final Logger LOGGER = Logger.getLogger(PluginsNodeProvider.class.getName());

    final VBox pane;

    public PluginsNodeProvider() {
        pane = new VBox();
    }

    @Override
    public void setContent(final Tab tab) {
        final Map<String, String> pluginNames = new TreeMap<>();
        final Map<String, ObservableList<PluginParameter<?>>> pluginParameters = new HashMap<>();
        final Map<String, String> dataAccessTypes = new HashMap<>();

        // Get plugins in order of description.
        PluginRegistry.getPluginClassNames().stream().forEach(pluginClassName -> {
            boolean isEnabled = true;
            final Plugin plugin = PluginRegistry.get(pluginClassName);
            final String pluginName = plugin.getName();
            if (plugin instanceof DataAccessPlugin) {
                final DataAccessPlugin dataAccessPlugin = (DataAccessPlugin) plugin;
                final String dataAccessType = dataAccessPlugin.getType();
                isEnabled = dataAccessPlugin.isEnabled();
                if (isEnabled && !dataAccessType.equals(DataAccessPluginCoreType.EXPERIMENTAL)
                        && !dataAccessType.equals(DataAccessPluginCoreType.DEVELOPER)) {
                    dataAccessTypes.put(pluginName != null ? pluginName : pluginClassName, dataAccessType);
                }
            }

            if (isEnabled) {
                if (pluginName == null) {
                    LOGGER.log(Level.WARNING, "null name for plugin %s{0}", pluginClassName);
                } else if (pluginNames.containsKey(pluginName)) {
                    LOGGER.log(Level.WARNING, "duplicate name {0} for plugins {1}, {2}", new Object[]{pluginName, pluginClassName, pluginNames.get(pluginName)});
                } else {
                    // Do nothing
                }

                pluginNames.put(pluginName != null ? pluginName : pluginClassName, pluginClassName);

                try {
                    final PluginParameters parameters = plugin.createParameters();
                    if (parameters != null) {
                        final ObservableList<PluginParameter<?>> parameterList = FXCollections.observableArrayList();
                        parameters.getParameters().entrySet().stream().forEach(entry -> {
                            final PluginParameter<?> parameter = entry.getValue();
                            parameterList.add(parameter);
                        });

                        Collections.sort(parameterList, (a, b) -> a.getId().compareToIgnoreCase(b.getId()));

                        pluginParameters.put(pluginName != null ? pluginName : pluginClassName, parameterList);
                    }
                } catch (final Exception ex) {
                    LOGGER.log(Level.SEVERE, "plugin " + pluginClassName + " created an exception", ex);
                }
            }
        });

        final Accordion pluginList = new Accordion();
        pluginNames.entrySet().stream().forEach(entry -> {
            final String pluginName = entry.getKey();
            final String pluginClassName = entry.getValue();

            final GridPane grid = new GridPane();
            grid.setPadding(new Insets(0, 0, 0, 5));
            grid.setHgap(5);
            grid.setVgap(10);

            grid.add(boldLabel("Name"), 0, 0);
            grid.add(new Label(pluginClassName), 1, 0);

            grid.add(boldLabel("Alias"), 0, 1);
            grid.add(new Label(PluginRegistry.getAlias(pluginClassName)), 1, 1);

            if (PluginRegistry.get(pluginClassName).getDescription() != null) {
                grid.add(boldLabel("Description"), 0, 2);
                final Label description = new Label(PluginRegistry.get(pluginClassName).getDescription());
                description.setPrefHeight(Region.USE_PREF_SIZE);
                description.setWrapText(true);
                grid.add(description, 1, 2);
            }

            final VBox pluginContent = new VBox();
            if (pluginParameters.containsKey(pluginName)) {
                grid.add(boldLabel("Parameters"), 0, 3);

                final TableColumn<PluginParameter<?>, String> colName = new TableColumn<>("Name");
                colName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getId()));

                final TableColumn<PluginParameter<?>, String> colType = new TableColumn<>("Type");
                colType.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getType().getId()));

                final TableColumn<PluginParameter<?>, String> colLabel = new TableColumn<>("Label");
                colLabel.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getName()));

                final TableColumn<PluginParameter<?>, String> colDescr = new TableColumn<>("Description");
                colDescr.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getDescription()));

                final TableColumn<PluginParameter<?>, String> colDefault = new TableColumn<>("Default Value");
                colDefault.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getStringValue()));

                final TableView<PluginParameter<?>> parameterTable = new TableView<>(pluginParameters.get(pluginName));
                parameterTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                parameterTable.getColumns().addAll(colName, colType, colLabel, colDescr, colDefault);
                parameterTable.setFixedCellSize(25);
                parameterTable.prefHeightProperty().bind(parameterTable.fixedCellSizeProperty().multiply(Bindings.size(parameterTable.getItems()).add(1.01)));
                parameterTable.minHeightProperty().bind(parameterTable.prefHeightProperty());
                parameterTable.maxHeightProperty().bind(parameterTable.prefHeightProperty());
                pluginContent.getChildren().add(grid);
                pluginContent.getChildren().add(parameterTable);
            } else {
                pluginContent.getChildren().add(grid);
            }

            final TitledPane pluginPane = new TitledPane(pluginName, pluginContent);
            if (dataAccessTypes.containsKey(pluginName)) {
                final ImageView iv = new ImageView(UserInterfaceIconProvider.VISIBLE.buildImage(16, ConstellationColor.CLOUDS.getJavaColor()));
                final Label l = new Label(String.format("(%s)", dataAccessTypes.get(pluginName)));
                final HBox box = new HBox(iv, l);
                pluginPane.setGraphic(box);
                pluginPane.setGraphicTextGap(20);
                pluginPane.setContentDisplay(ContentDisplay.RIGHT);
            }
            pluginList.getPanes().add(pluginPane);
        });

        final Button exportPluginsButton = new Button();
        exportPluginsButton.setTooltip(new Tooltip("Export Plugin Details to CSV"));
        exportPluginsButton.setGraphic(new ImageView(UserInterfaceIconProvider.DOWNLOAD.buildImage(16, ConstellationColor.CLOUDS.getJavaColor())));
        exportPluginsButton.setOnAction(action -> exportPluginsToCsv(tab.getContent().getScene().getWindow()));

        Platform.runLater(() -> {
            pane.setAlignment(Pos.TOP_RIGHT);

            final ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(pluginList);
            scrollPane.setFitToWidth(true);

            pane.getChildren().add(exportPluginsButton);
            pane.getChildren().add(scrollPane);

            tab.setContent(pane);
        });
    }

    @Override
    public void discardNode() {
        pane.getChildren().clear();
    }

    @Override
    public String getText() {
        return "Plugins";
    }

    /**
     * A Label containing bold text.
     *
     * @param text
     * @return
     */
    static Label boldLabel(final String text) {
        final Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setMaxWidth(Region.USE_PREF_SIZE);
        return label;
    }

    private static void exportPluginsToCsv(final Window window) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a location to save the CSV file");

        final File dir = directoryChooser.showDialog(window);
        if (dir != null) {
            final StringBuilder sb = new StringBuilder();

            // heading
            sb.append("Plugin class path").append(SeparatorConstants.COMMA)
                    .append("Plugin Alias").append(SeparatorConstants.COMMA)
                    .append("Parameter Name").append(SeparatorConstants.COMMA)
                    .append("Parameter Type").append(SeparatorConstants.COMMA)
                    .append("Parameter Label").append(SeparatorConstants.COMMA)
                    .append("Parameter Description").append(SeparatorConstants.COMMA)
                    .append("Parameter Default Value").append(SeparatorConstants.NEWLINE);

            // details
            PluginRegistry.getPluginClassNames().stream().forEach((final String pname) -> {
                final Plugin plugin = PluginRegistry.get(pname);
                final String name = plugin.getName();
                final Map<String, List<PluginParameter<?>>> params = new HashMap<>();

                try {
                    final PluginParameters pp = plugin.createParameters();
                    if (pp != null) {
                        final List<PluginParameter<?>> paramList = new ArrayList<>();
                        pp.getParameters().entrySet().stream().forEach(entry -> {
                            final PluginParameter<?> param = entry.getValue();
                            paramList.add(param);
                        });

                        Collections.sort(paramList, (a, b) -> a.getId().compareToIgnoreCase(b.getId()));

                        paramList.stream().forEach(p -> sb.append(plugin.getClass().getName()).append(SeparatorConstants.COMMA)                                    .append(PluginRegistry.getAlias(pname)).append(SeparatorConstants.COMMA)
                                    .append(p.getId()).append(SeparatorConstants.COMMA)
                                    .append(p.getType().getId()).append(SeparatorConstants.COMMA)
                                    .append(p.getName()).append(SeparatorConstants.COMMA)
                                    .append("\"").append(p.getDescription()).append("\"").append(SeparatorConstants.COMMA)
                                    .append("\"").append(p.getStringValue()).append("\"")
                                .append(SeparatorConstants.NEWLINE));
                    } else {
                        sb.append(plugin.getClass().getName()).append(SeparatorConstants.COMMA)
                                .append(PluginRegistry.getAlias(pname)).append(SeparatorConstants.COMMA)
                                .append(SeparatorConstants.NEWLINE);
                    }
                } catch (final Exception ex) {
                    LOGGER.log(Level.SEVERE, "plugin " + pname + " created an exception", ex);
                }

                final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                final File file = new File(dir, String.format("Plugin Details - %s.csv", dateFormatter.format(new Date())));
                try (final FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    fileOutputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8.name()));
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Error during export of plugin details to csv", ex);
                }
            });
        }
    }
}
