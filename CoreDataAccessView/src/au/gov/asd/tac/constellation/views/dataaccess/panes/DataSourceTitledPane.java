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

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPaneListener;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ActionParameterType;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.openide.util.HelpCtx;

/**
 * A pane that contains a plugin.
 *
 * @author cygnus_x-1
 * @author ruby_crucis
 */
public class DataSourceTitledPane extends TitledPane implements PluginParametersPaneListener {

    private static final Logger LOGGER = Logger.getLogger(DataSourceTitledPane.class.getName());

    /**
     * A thread pool to create parameters in.
     */
    public static final ExecutorService PARAM_CREATOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final Label DUMMY_LABEL = new Label("Waiting...");
    private static final String DAV_CREATOR_THREAD_NAME = "DAV Pane Creator";

    private volatile PluginParameters dataSourceParameters;
    private final DataAccessPlugin plugin;
    private final CheckBox enabled;
    private final Label label;
    private final PluginParametersPaneListener top;
    private final Set<String> globalParamLabels;

    public static final String SELECTED_STYLE = "titled-pane-selected";
    public static final String MATCHED_STYLE = "titled-pane-matched";

    // If an exception is thrown getting the parameters and propagated from here,
    // the Data Access view will not be built: this is the JavaFX thread.
    private volatile boolean parametersCreated;
    private volatile String paramFailureMsg;

    // Have the parameters been loaded yet?
    // This is always accessed on the FX thread, so no need for synchronisation.
    private boolean isLoaded;

    private static final Image HELP_ICON = UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor());

    public DataSourceTitledPane(final DataAccessPlugin plugin, final ImageView dataSourceIcon, final PluginParametersPaneListener top, final Set<String> globalParamLabels) {
        this.plugin = plugin;
        this.top = top;
        this.globalParamLabels = globalParamLabels;

        isLoaded = false;
        enabled = new CheckBox();
        label = new Label(plugin.getName(), dataSourceIcon);

        setGraphic(createTitleBar());
        enabled.setDisable(true);

        final boolean isExpanded = DataAccessPreferences.isExpanded(plugin.getName(), false);

        createParameters(isExpanded, null);

        setPadding(Insets.EMPTY);
        setTooltip(new Tooltip(plugin.getDescription()));
    }

    /**
     * Create the plugin's parameters in a background thread to avoid stalling
     * the Data Access view.
     * <p>
     * Some plugins may take some time to create their parameters. Even worse,
     * once the parameters have been created, the equivalent JavaFX scenes can
     * take even longer. (At one point it was taking six seconds just to call
     * "container.setScene(new Scene(dataAccessViewPane))".) This synchronous
     * parameter create and display slows down the entire Data Access view
     * construction.
     * <p>
     * Therefore, we let each plugin create its parameters asynchronously in a
     * background thread. We remember the expanded state of each plugin pane,
     * and only buildId the PluginParametersPane when it needs to be displayed,
     * either immediately (if the pane was already expanded) or on user request.
     * This way, the cost of building the panes is only paid when it is seen.
     * For a Data Access view with only a few plugin panes expanded, this makes
     * the startup time much quicker.
     *
     * @param isExpanded Should the TitledPane be expanded immediately?
     * @param perPluginParamMap Parameter values that will override the
     * defaults.
     */
    private void createParameters(final boolean isExpanded, final Map<String, String> perPluginParamMap) {
        setExpanded(false);

        // Put a progress indicator on the title text.
        final ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(16, 16);
        ((HBox) getGraphic()).getChildren().add(pi);

        // Create the plugin parameters in a background thread.
        PARAM_CREATOR.execute(() -> {
            // In case two overlapping loads are happening at the same time...
            synchronized (this) {
                Thread.currentThread().setName(DAV_CREATOR_THREAD_NAME);
                boolean paramsCreated;
                String f;

                // warn when there is no error to the console, otherwise we forget to add help.
                final boolean requiresHelp = !DataAccessPluginCoreType.EXPERIMENTAL.equals(plugin.getType()) && !DataAccessPluginCoreType.DEVELOPER.equals(plugin.getType());
                if (plugin.getHelpCtx() == null && requiresHelp) {
                    LOGGER.log(Level.WARNING, "No help available for {0} ({1})", new Object[]{plugin.getName(), plugin.getClass().getName()});
                }

                try {
                    dataSourceParameters = DefaultPluginParameters.getDefaultParameters(plugin);
                    if (dataSourceParameters != null) {
                        dataSourceParameters = dataSourceParameters.copy();
                    }

                    if (perPluginParamMap != null) {
                        dataSourceParameters.startParameterLoading();
                        perPluginParamMap.entrySet().stream().forEach(entry -> {
                            final String key = entry.getKey();
                            if (dataSourceParameters.hasParameter(key) && !globalParamLabels.contains(key)) {
                                final PluginParameter pp = dataSourceParameters.getParameters().get(key);
                                // Don't set action type parameters.
                                // Since their only reason for existence is to perform an action,
                                // they don't have values, and setting them would kick off the action.
                                if (!pp.getId().equals(ActionParameterType.ID)) {
                                    pp.setStringValue(entry.getValue());
                                }
                            }
                        });
                        dataSourceParameters.endParameterLoading();
                    }

                    paramsCreated = true;
                    f = null;
                } catch (final Throwable t) {
                    System.out.printf("Parameter creation for plugin %s failed:\n", plugin.getName());
                    t.printStackTrace(System.out);

                    paramsCreated = false;
                    f = String.format("%s: %s", t.getClass().getName(), t.getMessage());
                }

                parametersCreated = paramsCreated;
                paramFailureMsg = f;

                Platform.runLater(() -> {
                    if (perPluginParamMap != null) {
                        validityChanged(true);
                    }

                    displayParameters(isExpanded);
                });
            }
        });
    }

    /**
     * Display the
     *
     * @param isExpanded
     */
    private void displayParameters(final boolean isExpanded) {
        assert Platform.isFxApplicationThread();

        // Couldn't be bothered keeping a reference to the progress indicator.
        // Find it and remove it. (But only one: if there are overlapping loads happening,
        // we don't want to remove someone else's indicator.)
        Node n = null;
        for (final Node node : ((HBox) getGraphic()).getChildren()) {
            if (node instanceof ProgressIndicator) {
                n = node;
                break;
            }
        }
        if (n != null) {
            ((HBox) getGraphic()).getChildren().remove(n);
        }

        if (parametersCreated) {
            enabled.setDisable(false);
            setCollapsible(true);
            if (hasNonGlobalParameters(dataSourceParameters)) {
                if (isExpanded) {
                    isLoaded = true;
                    final PluginParametersPane parametersPane = PluginParametersPane.buildPane(dataSourceParameters, this, globalParamLabels);
                    setContent(parametersPane);
                    setExpanded(true);
                } else {
                    // We need some content, any content, otherwise the first time the user attempts to expand the pane,
                    // the TitledPane will say to itself "I have no content, therefore I won't bother to expand".
                    //                    setContent(new Label("Waiting..."));
                    setContent(DUMMY_LABEL);
                }

                expandedProperty().addListener((final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) -> {
                    DataAccessPreferences.setExpanded(plugin.getName(), newValue);
                    if (newValue) {
                        if (!isLoaded) {
                            isLoaded = true;
                            final PluginParametersPane parametersPane = PluginParametersPane.buildPane(dataSourceParameters, this, globalParamLabels);
                            setContent(parametersPane);
                        }
                    }
                });
            } else {
                setCollapsible(false);
            }

            enabled.setOnAction(event -> {
                top.hierarchicalUpdate();
                if (enabled.isSelected()) {
                    while (getStyleClass().contains(MATCHED_STYLE)) {
                        getStyleClass().remove(MATCHED_STYLE);
                    }
                    getStyleClass().add(SELECTED_STYLE);
                } else {
                    while (getStyleClass().contains(SELECTED_STYLE)) {
                        getStyleClass().remove(SELECTED_STYLE);
                    }
                }
            });
        } else {
            // Something went wrong, so disable this plugin's GUI.
            enabled.setDisable(true);
            enabled.setSelected(false);
            label.setDisable(true);
            final Label text = new Label("Unexpected error while creating parameters:\n" + paramFailureMsg);
            text.setPrefWidth(400);
            text.setWrapText(true);
            setContent(text);
            setAlignment(Pos.TOP_LEFT);
        }
    }

    private Pane createTitleBar() {
        final HBox box = new HBox(enabled, label);
        final HelpCtx helpCtx = plugin.getHelpCtx();
        if (helpCtx != null) {
            final ImageView helpView = new ImageView(HELP_ICON);
            final Button helpButton = new Button("", helpView);
            helpButton.paddingProperty().set(DataAccessPane.HELP_INSETS);
            helpButton.setTooltip(new Tooltip(String.format("Display help for %s", plugin.getName())));
            helpButton.setOnAction(event -> {
                plugin.getHelpCtx().display();
            });

            // Get rid of the ugly button look so the icon stands alone.
            helpButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");

            // Align the help buttons on the right hand side if the width allows it.
            box.getChildren().add(helpButton);
        }

        return box;
    }

    private boolean hasNonGlobalParameters(final PluginParameters pp) {
        if (pp == null || pp.getParameters() == null) {
            return false;
        }

        final Set<String> paramNames = new HashSet<>(pp.getParameters().keySet());
        paramNames.removeAll(globalParamLabels);

        return !paramNames.isEmpty();
    }

    public boolean isQueryEnabled() {
        return parametersCreated && enabled.isSelected();
    }

    public PluginParameters getParameters() {
        return parametersCreated ? dataSourceParameters : null;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void validityChanged(boolean isEnabled) {
        isEnabled = parametersCreated && isEnabled;
        enabled.setSelected(isEnabled);
        if (enabled.isSelected()) {
            while (getStyleClass().contains(MATCHED_STYLE)) {
                getStyleClass().remove(MATCHED_STYLE);
            }
            getStyleClass().add(SELECTED_STYLE);
        } else {
            while (getStyleClass().contains(SELECTED_STYLE)) {
                getStyleClass().remove(SELECTED_STYLE);
            }
        }
        top.hierarchicalUpdate();
    }

    /**
     * Recreate and enable the pane with the provided parameter values.
     *
     * @param perPluginParamMap the parameter values.
     */
    public void setParameterValues(final Map<String, String> perPluginParamMap) {
        createParameters(true, perPluginParamMap);
    }

    @Override
    public void hierarchicalUpdate() {
        top.hierarchicalUpdate();
    }
}
