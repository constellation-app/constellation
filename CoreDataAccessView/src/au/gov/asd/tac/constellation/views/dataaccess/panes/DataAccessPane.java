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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.create.NewDefaultSchemaGraphAction;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPaneListener;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.RecentParameterValues;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginType;
import static au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginType.getTypeWithPosition;
import au.gov.asd.tac.constellation.views.dataaccess.io.ParameterIOUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessPreferenceKeys;
import au.gov.asd.tac.constellation.views.dataaccess.templates.DataAccessPreQueryValidation;
import au.gov.asd.tac.constellation.views.qualitycontrol.widget.QualityControlAutoButton;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * JavaFX Data Access View.
 *
 * @author cygnus_x-1
 * @author ruby_crucis
 * @author arcturus
 * @author antares
 */
public class DataAccessPane extends AnchorPane implements PluginParametersPaneListener {

    // Insets with 0 top and bottom so the title doesn't change size vertically.
    static final Insets HELP_INSETS = new Insets(0, 8, 0, 8);

    private static final Logger LOGGER = Logger.getLogger(DataAccessPane.class.getName());

    private static final String DAV_RUN_TAB_THREAD_NAME = "DAV Run Tab Queue";
    private static final String DAV_PLUGIN_QUEUE_THREAD_NAME = "DAV Plugin Queue";

    public static final String TAB_TITLE = "Step";

    private static final String EXECUTE_GO = "Go";
    private static final String EXECUTE_STOP = "Stop";
    private static final String GO_STYLE = "-fx-background-color: rgb(64,180,64); -fx-padding: 2 5 2 5;";
    private static final String STOP_STYLE = "-fx-background-color: rgb(180,64,64); -fx-padding: 2 5 2 5;";
    private static final String CONTINUE_STYLE = "-fx-background-color: rgb(255,180,0); -fx-padding: 2 5 2 5;";

    private final Preferences dataAccessPrefs = NbPreferences.forModule(DataAccessPreferenceKeys.class);

    private DataAccessViewTopComponent topComponent;
    private TabPane dataAccessTabPane;
    private Map<String, GraphState> graphState = new HashMap<>();
    private GraphState currentGraphState = null;
    private String graphId;
    private final Button executeButton = new Button(EXECUTE_GO);

    // search plugins
    private TextField searchPluginTextField;

    // favourites
    private static final String ADD_FAVOURITE = "Add";
    private static final String REMOVE_FAVOURITE = "Remove";

    private static Map<String, List<DataAccessPlugin>> plugins = null;

    private List<DataAccessPreQueryValidation> preQueryValidation = null;

    public DataAccessPane(DataAccessViewTopComponent topComponent) {
        this.topComponent = topComponent;

        dataAccessTabPane = new TabPane();
        dataAccessTabPane.setSide(Side.TOP);
        dataAccessTabPane.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            storeParameterValues();
        });

        // Update the button when the user adds/removes tabs.
        dataAccessTabPane.getTabs().addListener((ListChangeListener.Change<? extends Tab> c) -> {
            update();
        });

        plugins = lookupPlugins();

        // a list of dav types and their position
        final Map<String, Integer> typesWithPosition = getTypeWithPosition();

        // Sort the DataAccessPlugin lists within each type including the category type (so that favourites category is sorted properly).
        for (final List<DataAccessPlugin> pluginList : plugins.values()) {
            Collections.sort(pluginList, (final DataAccessPlugin p1, final DataAccessPlugin p2) -> {
                if (typesWithPosition.get(p1.getType()).equals(typesWithPosition.get(p2.getType()))) {
                    return Integer.compare(p1.getPosition(), p2.getPosition());
                } else {
                    return Integer.compare(typesWithPosition.get(p1.getType()), typesWithPosition.get(p2.getType()));
                }
            });
        }

        newTab(new QueryPhasePane(plugins, this, null));

        // Update the button when the user changes the time range
        getQueryPhasePane(getCurrentTab()).getGlobalParametersPane().getParams().getParameters().get(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID)
                .addListener((oldValue, newValue) -> update());

        final Button helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor())));
        helpButton.paddingProperty().set(HELP_INSETS);
        helpButton.setTooltip(new Tooltip("Display help for Data Access"));
        helpButton.setOnAction(event -> {
            new HelpCtx(DataAccessViewTopComponent.class.getName()).display();
        });

        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");
        final Button addButton = new Button("", new ImageView(UserInterfaceIconProvider.ADD.buildImage(16)));
        addButton.setTooltip(new Tooltip("Add new run tab"));
        addButton.setOnAction((ActionEvent t) -> {
            PluginParameters previousGlobals = null;
            if (dataAccessTabPane.getTabs().size() > 0) {
                previousGlobals = ((QueryPhasePane) ((ScrollPane) dataAccessTabPane.getTabs().get(dataAccessTabPane.getTabs().size() - 1).getContent()).getContent()).getGlobalParametersPane().getParams();
            }
            final PluginParameters finalPrevGlobals = previousGlobals;
            newTab(new QueryPhasePane(plugins, DataAccessPane.this, finalPrevGlobals));
        });

        // right click anywhere and get the tab's context menu
        setOnContextMenuRequested((ContextMenuEvent event) -> {
            getCurrentTab().getContextMenu().show(DataAccessPane.this, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        // load all pre query validation checks
        if (preQueryValidation == null) {
            preQueryValidation = new ArrayList<>(Lookup.getDefault().lookupAll(DataAccessPreQueryValidation.class));
        }

        executeButton.setStyle(GO_STYLE);
        executeButton.setOnAction((ActionEvent event) -> {
            boolean pluginSelected = false;
            boolean selectedPluginsValid = true;
            
            // check for activated plugins and their validity.
            for (Tab tab : dataAccessTabPane.getTabs()) {
                if (tabHasEnabledPlugins(tab)) {
                    pluginSelected = true;
                    if (!validateTabEnabledPlugins(tab)) {
                        selectedPluginsValid = false;
                    }
                } 
            }
            // when no graph present, create new graph
            if(graphId == null){
                if (pluginSelected && selectedPluginsValid) {
                    NewDefaultSchemaGraphAction graphAction = new NewDefaultSchemaGraphAction();
                    graphAction.actionPerformed(null);
                    while(GraphManager.getDefault().getActiveGraph() == null){
                        // Wait and do nothing while graph is getting made
                    }
                    graphId = GraphManager.getDefault().getActiveGraph().getId();
                    if (!graphState.containsKey(graphId)) {
                        graphState.put(graphId, new GraphState());
                    }
                    currentGraphState = graphState.get(graphId);
                }
            }
            // run the selected queries
            final ObservableList<Tab> tabs = dataAccessTabPane.getTabs();
            if (tabs != null && currentGraphState != null && !tabs.isEmpty() && currentGraphState.goButtonIsGo) {
                setExecuteButtonToStop();
                graphState.get(GraphManager.getDefault().getActiveGraph().getId()).queriesRunning = true;

                final File outputDir = DataAccessPreferenceKeys.getDataAccessResultsDirEx();
                if (outputDir != null) {
                    if (outputDir.isDirectory()) {
                        final String msg = String.format("Data access results will be written to %s", outputDir.getAbsolutePath());
                        StatusDisplayer.getDefault().setStatusText(msg);
                    } else {
                        final String msg = String.format("Results directory %s does not exist", outputDir.getAbsolutePath());
                        NotificationDisplayer.getDefault().notify("Save raw results",
                                UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                                msg,
                                null
                        );
                    }
                }

                PluginExecution.withPlugin(new SimplePlugin("Data Access View: Save State") {
                    @Override
                    protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                        ParameterIOUtilities.saveDataAccessState(dataAccessTabPane, GraphManager.getDefault().getActiveGraph());
                    }
                }).executeLater(null);

                List<Future<?>> barrier = null;
                for (final Tab tab : tabs) {
                    LOGGER.log(Level.INFO, "Running tab: {0}", tab.getText());
                    final QueryPhasePane queryPane = (QueryPhasePane) ((ScrollPane) tab.getContent()).getContent();
                    barrier = runPlugins(queryPane, barrier);
                }

                final String storedGraphId = graphId; // Need to take a copy for when it changes while this thread is still running
                final Thread waiting = new Thread() {
                    @Override
                    public void run() {
                        try {
                            for (Map.Entry<Future<?>, String> running : graphState.get(storedGraphId).runningPlugins.entrySet()) {
                                try {
                                    running.getKey().get();
                                } catch (ExecutionException e) {
                                    final String exceptionDescription = getExceptionDescription(e);
                                    LOGGER.log(Level.INFO, "Handling plug-in exception: {0}", exceptionDescription);
                                    e.printStackTrace(System.out);
                                    NotificationDisplayer.getDefault().notify("Problem with " + running.getValue(),
                                            UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                                            exceptionDescription,
                                            null,
                                            NotificationDisplayer.Priority.HIGH
                                    );
                                } catch (CancellationException e) {
                                }
                            }
                        } catch (InterruptedException e) {
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                graphState.get(storedGraphId).queriesRunning = false;
                                if (storedGraphId.equals(graphId)) {
                                    update();
                                }
                            }
                        });
                    }

                    private String getExceptionDescription(Throwable e) {
                        if (e.getCause() != null) {
                            return getExceptionDescription(e.getCause());
                        }
                        if (e.getMessage() != null) {
                            return e.getMessage();
                        }
                        return e.getClass().getName();
                    }
                };
                waiting.setName(DAV_PLUGIN_QUEUE_THREAD_NAME);
                waiting.start();
                LOGGER.info("Plugins run.");
            } else { // Button is a stop button
                if(currentGraphState != null){
                    for (Future<?> running : currentGraphState.runningPlugins.keySet()) {
                        running.cancel(true);
                    }
                setExecuteButtonToGo();
                }
            }
            if(DataAccessPreferenceKeys.isDeselectPluginsOnExecuteEnabled()) {
                deselectAllPlugins();
            }
        });
        updateForPlugins(false);

        // Options menu.
        final Menu optionsMenu = new Menu("Options");
        final MenuItem loadMenuItem = new MenuItem("Load...");
        loadMenuItem.setOnAction(event -> {
            ParameterIOUtilities.loadParameters(this);
        });

        final MenuItem saveMenuItem = new MenuItem("Save");
        saveMenuItem.setOnAction(event -> {
            ParameterIOUtilities.saveParameters(dataAccessTabPane);
        });

        final CheckMenuItem saveResultsItem = new CheckMenuItem("Save Results");
        final File daDir = DataAccessPreferenceKeys.getDataAccessResultsDir();
        saveResultsItem.setSelected(daDir != null);
        saveResultsItem.selectedProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue) {
                final DirectoryChooser dc = new DirectoryChooser();
                dc.setTitle("Folder to save data access results to");
                final File prev = DataAccessPreferenceKeys.getPreviousDataAccessResultsDir();
                if (prev != null && prev.isDirectory()) {
                    dc.setInitialDirectory(prev);
                }

                final File dir = dc.showDialog(getScene().getWindow());
                if (dir != null) {
                    DataAccessPreferenceKeys.setDataAccessResultsDir(dir);
                } else {
                    saveResultsItem.setSelected(false);
                }
            } else {
                DataAccessPreferenceKeys.setDataAccessResultsDir(null);
            }
        });
        
        final CheckMenuItem deselectPluginsOnExecution = new CheckMenuItem("Deselect Plugins On Go");
        deselectPluginsOnExecution.setSelected(DataAccessPreferenceKeys.isDeselectPluginsOnExecuteEnabled());
        deselectPluginsOnExecution.setOnAction(event -> {
            DataAccessPreferenceKeys.setDeselectPluginsOnExecute(deselectPluginsOnExecution.isSelected());
        });
        
        searchPluginTextField = new TextField();
        searchPluginTextField.setPromptText("Type to search for a plugin");
        searchPluginTextField.textProperty().addListener((ov, oldValue, newValue) -> {
            final QueryPhasePane queryPhasePane = getQueryPhasePane(getCurrentTab());
            queryPhasePane.showMatchingPlugins(newValue);
        });

        final Button favouriteButton = new Button("", new ImageView(AnalyticIconProvider.STAR.buildImage(16, ConstellationColor.YELLOW.getJavaColor())));
        favouriteButton.setTooltip(new Tooltip("Manage your favourites"));
        favouriteButton.setOnAction((ActionEvent t) -> {
            manageFavourites();
        });

        optionsMenu.getItems().addAll(loadMenuItem, saveMenuItem, saveResultsItem, deselectPluginsOnExecution);
        final MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(optionsMenu);
        menuBar.setMinHeight(32);

        final VBox vbox = new VBox(menuBar, searchPluginTextField, dataAccessTabPane);
        VBox.setVgrow(dataAccessTabPane, Priority.ALWAYS);
        AnchorPane.setTopAnchor(vbox, 0.0);
        AnchorPane.setBottomAnchor(vbox, 0.0);
        AnchorPane.setLeftAnchor(vbox, 0.0);
        AnchorPane.setRightAnchor(vbox, 0.0);
        getChildren().add(vbox);

        final HBox options = new HBox();
        options.setSpacing(10.0);
        options.getChildren().addAll(helpButton, addButton, favouriteButton);

        final QualityControlAutoButton rab = Lookup.getDefault().lookup(QualityControlAutoButton.class);
        if (rab != null) {
            options.getChildren().add(rab);
        }

        // add some padding between the Go button and the previous button to avoid accidental clicking
        final Region region = new Region();
        region.setMinSize(20, 0);
        options.getChildren().add(region);
        options.getChildren().add(executeButton);
        AnchorPane.setTopAnchor(options, 5.0);
        AnchorPane.setRightAnchor(options, 5.0);
        getChildren().add(options);
    }

    /**
     * Add and remove plugins from the favourites section
     */
    private void manageFavourites() {
        final List<String> selectedPlugins = new ArrayList<>();

        // get a list of the selected plugins
        final QueryPhasePane queryPhasePane = getQueryPhasePane(getCurrentTab());
        queryPhasePane.getDataAccessPanes().stream().forEach(tp -> {
            if (tp.isQueryEnabled()) {
                selectedPlugins.add(tp.getPlugin().getName());
            }
        });

        if (selectedPlugins.isEmpty()) {
            final NotifyDescriptor nd = new NotifyDescriptor.Message("No plugins selected.", NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } else {
            final StringBuilder message = new StringBuilder(300);
            message.append("Add or remove plugins from your favourites category.\n\n");
            message.append("The following plugins were selected:\n");
            selectedPlugins.stream().forEach(plugin -> {
                message.append(plugin).append("\n");
            });
            message.append("\nNote that you need to restart before changes take effect.");

            final NotifyDescriptor nde = new NotifyDescriptor(message.toString(), "Manage Favourites", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[]{ADD_FAVOURITE, REMOVE_FAVOURITE, NotifyDescriptor.CANCEL_OPTION}, NotifyDescriptor.OK_OPTION);
            final Object option = DialogDisplayer.getDefault().notify(nde);

            if (option != NotifyDescriptor.CANCEL_OPTION) {
                selectedPlugins.stream().forEach(name -> {
                    DataAccessPreferences.setFavourite(name, option == ADD_FAVOURITE);
                });
            }
        }
    }

    static Map<String, List<DataAccessPlugin>> lookupPlugins() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        if (plugins == null) {
            final Thread thread = new Thread("Data Access View: Load Plugins") {
                @Override
                public void run() {
                    plugins = new LinkedHashMap<>();

                    // Use a pre-filled LinkedHashMap to keep the types in the correct order.
                    final List<String> typeList = DataAccessPluginType.getTypes();
                    typeList.stream().forEach((type) -> {
                        plugins.put(type, new ArrayList<>());
                    });

                    // create the favourites category
                    if (plugins.get(DataAccessPluginCoreType.FAVOURITES) == null) {
                        plugins.put(DataAccessPluginCoreType.FAVOURITES, new ArrayList<>());
                    }

                    // Now fetch the DataAccessPlugin instances.
                    final Map<String, DataAccessPlugin> pluginOverrides = new HashMap<>();
                    Lookup.getDefault().lookupAll(DataAccessPlugin.class).stream().forEach(plugin -> {
                        if (!plugin.isEnabled()) {
                            // If plugin is disabled, ignore the plugin.
                            LOGGER.log(Level.INFO, "Disabled data access plugin {0} ({1})", new Object[]{plugin.getName(), plugin.getType()});
                        } else {
                            final String type = plugin.getType();
                            if (plugins.containsKey(type)) {
                                // If plugin type is valid, add the plugin to the Data Access View.
                                plugins.get(type).add(plugin);
                                LOGGER.log(Level.INFO, "Discovered data access plugin {0} ({1})", new Object[]{plugin.getName(), plugin.getType()});

                                // If plugin overrides another, record which plugin should be removed for later processing.
                                if (plugin.getOverriddenPlugins() != null) {
                                    for (final String overriddenPluginName : plugin.getOverriddenPlugins()) {
                                        pluginOverrides.put(overriddenPluginName, plugin);
                                    }
                                }
                            } else {
                                // If a plugin type is invalid (that is, not registered as a DataAccessPluginType), ignore the plugin.
                                LOGGER.log(Level.SEVERE, "Unexpected data access plugin type '{0}' for plugin {1}", new Object[]{type, plugin.getName()});
                            }

                            // favourites
                            if (DataAccessPreferences.isfavourite(plugin.getName(), false)) {
                                plugins.get(DataAccessPluginCoreType.FAVOURITES).add(plugin);
                                LOGGER.log(Level.INFO, "Discovered data access plugin {0} ({1})", new Object[]{plugin.getName(), DataAccessPluginCoreType.FAVOURITES});
                            }
                        }
                    });

                    // Remove any overridden plugins.
                    pluginOverrides.forEach((pluginName, overridingPlugin) -> {
                        String removeType = null;
                        DataAccessPlugin removePlugin = null;
                        for (String pluginType : plugins.keySet()) {
                            List<DataAccessPlugin> pluginList = plugins.get(pluginType);
                            for (DataAccessPlugin plugin : pluginList) {
                                if (plugin.getClass().getName().equals(pluginName)) {
                                    removeType = pluginType;
                                    removePlugin = plugin;
                                    break;
                                }
                            }
                        }

                        if (removeType != null && removePlugin != null) {
                            plugins.get(removeType).remove(removePlugin);
                            LOGGER.log(Level.INFO, "Removed data access plugin {0} ({1}) as it is overriden by data access plugin {2} ({3})",
                                    new Object[]{removePlugin.getName(), removeType, overridingPlugin.getName(), overridingPlugin.getType()});
                        }
                    });
                    countDownLatch.countDown();
                }
            };
            thread.start();
        } else {
            countDownLatch.countDown();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
            LOGGER.severe(ex.getLocalizedMessage());
        }

        return plugins;
    }

    public QueryPhasePane newTab() {
        final QueryPhasePane pane = new QueryPhasePane(plugins, this, null);
        newTab(pane);

        return pane;
    }

    /**
     * Create a new tab, which will renumber other tabs when it is closed
     *
     * @param queryPane
     */
    private void newTab(QueryPhasePane queryPane) {
        final Tab newTab = new Tab(TAB_TITLE + " " + (dataAccessTabPane.getTabs().size() + 1));
        final EventHandler<Event> origOnClose = newTab.getOnClosed();
        newTab.setOnClosed((Event t) -> {
            int queryNum = 1;
            for (Tab tab : dataAccessTabPane.getTabs()) {
                tab.setText(TAB_TITLE + " " + queryNum);
                queryNum++;
            }
            if (origOnClose != null) {
                origOnClose.handle(t);
            }
        });

        final MenuItem deactivateAllPlugins = new MenuItem("Deactivate all plugins");
        deactivateAllPlugins.setOnAction((ActionEvent event) -> {
            QueryPhasePane queryPhasePane = getQueryPhasePane(newTab);
            for (DataSourceTitledPane dataSourceTitledPane : queryPhasePane.getDataAccessPanes()) {
                if (dataSourceTitledPane.isQueryEnabled()) {
                    dataSourceTitledPane.validityChanged(false);
                }
            }
        });

        final MenuItem findPlugin = new MenuItem("Find plugin...");
        findPlugin.setOnAction(event -> {
            // Run it later to allow the menu to close.
            Platform.runLater(() -> {
                final PluginFinder pfinder = new PluginFinder();
                pfinder.find(this, getQueryPhasePane(newTab));
            });
        });

        final MenuItem openAllSections = new MenuItem("Open all sections");
        openAllSections.setOnAction(event -> {
            final QueryPhasePane queryPhasePane = getQueryPhasePane(newTab);
            queryPhasePane.setHeadingsExpanded(true, false);
        });

        final MenuItem closeAllSections = new MenuItem("Close all sections");
        closeAllSections.setOnAction(event -> {
            final QueryPhasePane queryPhasePane = getQueryPhasePane(newTab);
            queryPhasePane.setHeadingsExpanded(false, false);
        });

        final MenuItem run = new MenuItem("Run this tab only");
        run.setOnAction((ActionEvent event) -> {
            int index = dataAccessTabPane.getTabs().indexOf(newTab);
            DataAccessPane.this.runTabs(index, index);
        });

        final MenuItem runFromHere = new MenuItem("Run from this tab");
        runFromHere.setOnAction((ActionEvent event) -> {
            ObservableList<Tab> allTabs = dataAccessTabPane.getTabs();
            int index = allTabs.indexOf(newTab);
            DataAccessPane.this.runTabs(index, allTabs.size() - 1);
        });

        final MenuItem runToHere = new MenuItem("Run to this tab");
        runToHere.setOnAction((ActionEvent event) -> {
            int index = dataAccessTabPane.getTabs().indexOf(newTab);
            DataAccessPane.this.runTabs(0, index);
        });

        queryPane.addGraphDependentMenuItems(run, runFromHere, runToHere);
        queryPane.addPluginDependentMenuItems(deactivateAllPlugins);

        /**
         * The position order of the menu options has been considered carefully
         * based on feedback. For instance the "Deactivate all plugins" exists
         * as the first entry because it is the most common use case and also
         * makes it less likely for one of the run* options to be clicked
         * accidently.
         */
        final ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(
                deactivateAllPlugins,
                new SeparatorMenuItem(),
                findPlugin,
                openAllSections,
                closeAllSections,
                new SeparatorMenuItem(),
                run,
                runFromHere,
                runToHere
        );

        final ScrollPane queryPhaseScroll = new ScrollPane();
        queryPhaseScroll.setFitToWidth(true);
        queryPhaseScroll.setContent(queryPane);
        queryPhaseScroll.setStyle("-fx-background-color: black;");

        newTab.setContextMenu(menu);
        newTab.setContent(queryPhaseScroll);
        newTab.setTooltip(new Tooltip("Right click for more options"));
        newTab.setClosable(true);
        updateTabMenu(newTab, shouldEnableTabMenu(newTab)); // Must be called after setting the scroll pane
        dataAccessTabPane.getTabs().add(newTab);
    }

    public Tab getCurrentTab() {
        return dataAccessTabPane.getSelectionModel().getSelectedItem();
    }

    public void removeTabs() {
        dataAccessTabPane.getTabs().clear();
    }

    /**
     * Run a range of tabs, numbered inclusively from 0.
     *
     * @param firstTab
     * @param lastTab
     */
    private void runTabs(final int firstTab, final int lastTab) {
        setExecuteButtonToStop();
        graphState.get(GraphManager.getDefault().getActiveGraph().getId()).queriesRunning = true;

        List<Future<?>> barrier = null;
        for (int i = firstTab; i <= lastTab; i++) {
            final Tab tab = dataAccessTabPane.getTabs().get(i);
            LOGGER.log(Level.INFO, "Running tab: {0}", tab.getText());
            final QueryPhasePane queryPane = (QueryPhasePane) ((ScrollPane) tab.getContent()).getContent();
            barrier = runPlugins(queryPane, barrier);
        }

        final String storedGraphId = graphId; // Need to take a copy for when it changes while this thread is still running
        final Thread waiting = new Thread() {
            @Override
            public void run() {
                try {
                    for (Map.Entry<Future<?>, String> running : graphState.get(storedGraphId).runningPlugins.entrySet()) {
                        try {
                            running.getKey().get();
                        } catch (ExecutionException e) {
                            LOGGER.log(Level.INFO, "Handling plug-in exception: {0}", e.getCause().getMessage());
                            e.printStackTrace(System.out);
                            NotificationDisplayer.getDefault().notify("Problem with " + running.getValue(),
                                    UserInterfaceIconProvider.ERROR.buildIcon(16, ConstellationColor.CHERRY.getJavaColor()),
                                    e.getCause().getMessage(),
                                    null,
                                    NotificationDisplayer.Priority.HIGH
                            );
                        } catch (CancellationException e) {
                        }
                    }
                } catch (InterruptedException e) {
                }
                Platform.runLater(() -> {
                    graphState.get(storedGraphId).queriesRunning = false;
                    if (storedGraphId.equals(graphId)) {
                        update();
                    }
                });
            }
        };
        waiting.setName(DAV_RUN_TAB_THREAD_NAME);
        waiting.start();
    }

    /**
     * Update executeButton, tab contextual menus, etc. to enable running
     * plug-ins if there is a graph open and plug-ins are selected for running
     *
     */
    protected void update() {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            update((String) null);
        } else {
            update(graph.getId());
        }
    }

    /**
     * Update executeButton, tab contextual menus, etc., given a readable graph
     *
     * @param graph the DataAccessPane will be updated to reflect the state of
     * this graph.
     */
    protected void update(final Graph graph) {
        if (getCurrentTab() != null) {
            final List<DataSourceTitledPane> panes = getQueryPhasePane(getCurrentTab()).getDataAccessPanes();
            for (DataSourceTitledPane pane : panes) {
                pane.getPlugin().updateParameters(graph, pane.getParameters());
            }

            if (graph == null) {
                update((String) null);
            } else {
                update(graph.getId());
            }
        }

    }

    /**
     * Update executeButton, tab contextual menus, etc. to enable running
     * plug-ins if there is a graph open and plug-ins are selected for running
     *
     * @param id
     */
    private void update(final String id) {
        graphId = id;
        if (id == null) {
            setExecuteButtonToGo();
            updateForPlugins(false);
            currentGraphState = null;
        } else {
            if (!graphState.containsKey(graphId)) {
                graphState.put(id, new GraphState());
            }
            currentGraphState = graphState.get(graphId);
            if (currentGraphState.queriesRunning) {
                setExecuteButtonToStop();
                updateForPlugins(true);
            } else {
                setExecuteButtonToGo();
                updateForPlugins(true);
            }
        }

        updateTabMenus();
    }

    /**
     * Called when a field is enabling it's parent plug-in, to enable
     * executeButton, etc., if there is an open graph.
     */
    @Override
    public void hierarchicalUpdate() {
        update();
    }

    /**
     * Enable or disable executeButton (not the tab contextual menus) based on
     * whether any plug-ins are selected. This should *not* be called if
     * plug-ins are running as in that case executeButton (actually the stop
     * button) must remain enabled.
     */
    private void updateForPlugins(boolean graphPresent) {
        boolean pluginSelected = false;
        boolean validTimeRange = true;
        boolean selectedPluginsValid = true;

        for (Tab tab : dataAccessTabPane.getTabs()) {
            if (tabHasEnabledPlugins(tab)) {
                pluginSelected = true;
                if (!validateTabEnabledPlugins(tab)) {
                    selectedPluginsValid = false;
                }
                getQueryPhasePane(tab).enablePluginDependentMenuItems(true);
            } else {
                getQueryPhasePane(tab).enablePluginDependentMenuItems(false);
            }
            final GlobalParametersPane gpp = getQueryPhasePane(tab).getGlobalParametersPane();
            final PluginParameters params = gpp.getParams();
            final DateTimeRange range = params.getDateTimeRangeValue(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID);
            if (range.getZonedStartEnd()[0].isAfter(range.getZonedStartEnd()[1])) {
                validTimeRange = false;
            }
        }

        final boolean queryIsRunning = currentGraphState != null && currentGraphState.queriesRunning;

        // The button cannot be disabled if a query is running.
        // Otherwise, disable if there is no selected plugin, an invalid time range, or the selected plugins contain invalid parameter values.
        final boolean disable = !queryIsRunning && (!pluginSelected || !validTimeRange || !selectedPluginsValid);
        executeButton.setDisable(disable);
    }

    /**
     * check if a tab has any plug-ins selected for running
     *
     * @param tab
     * @return
     */
    private boolean tabHasEnabledPlugins(Tab tab) {
        for (DataSourceTitledPane pane : getQueryPhasePane(tab).getDataAccessPanes()) {
            if (pane.isQueryEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * check whether the selected plugins contain any parameters with invalid
     * values
     *
     * @param tab
     * @return
     */
    private boolean validateTabEnabledPlugins(Tab tab) {
        for (DataSourceTitledPane pane : getQueryPhasePane(tab).getDataAccessPanes()) {
            if (pane.isQueryEnabled()) {
                final PluginParameters params = pane.getParameters();
                if (params != null) {
                    final Map<String, PluginParameter<?>> paramsMap = params.getParameters();
                    for (Map.Entry<String, PluginParameter<?>> entry : paramsMap.entrySet()) {
                        final PluginParameter value = entry.getValue();
                        if (value.getError() != null) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static QueryPhasePane getQueryPhasePane(Tab tab) {
        return (QueryPhasePane) ((ScrollPane) tab.getContent()).getContent();
    }

    /**
     * Enable or disable the items in the contextual menu for a tab.
     *
     * @param tab
     * @param enabled
     */
    private void updateTabMenu(Tab tab, boolean enabled) {
        final QueryPhasePane queryPhasePane = getQueryPhasePane(tab);
        queryPhasePane.enableGraphDependentMenuItems(enabled);
    }

    /**
     * Enable or disable the items in the contextual menu for all tabs.
     */
    private void updateTabMenus() {
        for (final Tab tab : dataAccessTabPane.getTabs()) {
            updateTabMenu(tab, shouldEnableTabMenu(tab));
        }
    }

    /**
     * Check if a tab's contextual menu should be enabled or not.
     *
     * @param tab
     * @return
     */
    private boolean shouldEnableTabMenu(Tab tab) {
        if (currentGraphState != null) {
            return !executeButton.isDisable() && currentGraphState.goButtonIsGo && tabHasEnabledPlugins(tab);
        } else {
            return false;
        }
    }

    /**
     * Set executeButton to function as "go".
     */
    private void setExecuteButtonToGo() {
        if (currentGraphState != null) {
            currentGraphState.goButtonIsGo = true;
        }
        Platform.runLater(() -> {
            executeButton.setText(EXECUTE_GO);
            executeButton.setStyle(GO_STYLE);
        });
    }

    /**
     * Set executeButton to function as "stop".
     */
    private void setExecuteButtonToStop() {
        if (currentGraphState != null) {
            currentGraphState.goButtonIsGo = false;
        }
        Platform.runLater(() -> {
            executeButton.setText(EXECUTE_STOP);
            executeButton.setStyle(STOP_STYLE);
        });
    }

    /**
     * Set executeButton to function as "continue".
     */
    private void setExecuteButtonToContinue() {
        if (currentGraphState != null) {
            currentGraphState.goButtonIsGo = true;
        }
        Platform.runLater(() -> {
            executeButton.setText("Continue");
            executeButton.setStyle(CONTINUE_STYLE);
        });
    }

    /**
     * Run the selected plug-ins in pane given query pane, optionally waiting
     * first on a list of futures. This method does not block.
     *
     * @param pluginPane
     * @param async if not null, the plug-ins will wait till all futures are
     * complete before any run.
     * @return
     */
    private List<Future<?>> runPlugins(final QueryPhasePane pluginPane, final List<Future<?>> async) {
        storeParameterValues();

        final Map<String, PluginParameter<?>> globalParams = pluginPane.getGlobalParametersPane().getParams().getParameters();

        // pre query validation checking
        for (final DataAccessPreQueryValidation check : preQueryValidation) {
            if (!check.execute(pluginPane)) {
                return Collections.emptyList();
            }
        }

        int pluginsToRun = 0;
        for (final DataSourceTitledPane pane : pluginPane.getDataAccessPanes()) {
            if (pane.isQueryEnabled()) {
                pluginsToRun++;
            }
        }
        LOGGER.log(Level.INFO, "\tRunning {0} plugins", pluginsToRun);
        final PluginSynchronizer synchroniser = new PluginSynchronizer(pluginsToRun);
        final List<Future<?>> newAsync = new ArrayList<>(pluginsToRun);
        currentGraphState.runningPlugins.clear();
        for (final DataSourceTitledPane pane : pluginPane.getDataAccessPanes()) {
            if (pane.isQueryEnabled()) {
                final Plugin plugin = PluginRegistry.get(pane.getPlugin().getClass().getName());
                PluginParameters parameters = pane.getParameters();
                if (parameters != null) {
                    parameters = parameters.copy();
                    for (final Map.Entry<String, PluginParameter<?>> param : parameters.getParameters().entrySet()) {
                        final String id = param.getKey();
                        // Why were global parameters only being copied back if the plugin parameter's value was null?
                        //                        Object value = param.getValue().getObjectValue();
                        if (/*value == null &&*/globalParams.containsKey(id)) {
                            param.getValue().setObjectValue(globalParams.get(id).getObjectValue());
                        }
                    }
                }
                LOGGER.log(Level.INFO, "\t\tRunning {0}", plugin.getName());

                final Future<?> pluginResult = PluginExecution.withPlugin(plugin).withParameters(parameters)
                        .waitingFor(async).synchronizingOn(synchroniser)
                        .executeLater(GraphManager.getDefault().getActiveGraph());
                newAsync.add(pluginResult);
                currentGraphState.runningPlugins.put(pluginResult, plugin.getName());
            }
        }
        return newAsync;
    }

    /**
     * Store current parameter values for all tabs and plug-ins in the recent
     * values repository.
     */
    private void storeParameterValues() {
        for (final Tab tab : dataAccessTabPane.getTabs()) {
            final QueryPhasePane pluginPane = (QueryPhasePane) ((ScrollPane) tab.getContent()).getContent();
            for (final Map.Entry<String, PluginParameter<?>> param : pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet()) {
                final String id = param.getKey();
                final String value = param.getValue().getStringValue();
                if (value != null && value.length() > 0) {
                    RecentParameterValues.storeRecentValue(id, value);
                }
            }
            for (final DataSourceTitledPane pane : pluginPane.getDataAccessPanes()) {
                final PluginParameters parameters = pane.getParameters();
                if (parameters != null) {
                    for (final Map.Entry<String, PluginParameter<?>> param : parameters.getParameters().entrySet()) {
                        final String id = param.getKey();
                        final Object obj = param.getValue().getObjectValue();
                        if (obj != null && obj.toString() != null && !obj.toString().isEmpty()) {
                            String value = param.getValue().getStringValue();
                            RecentParameterValues.storeRecentValue(id, value);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void validityChanged(boolean enabled) {
    }

    private void deselectAllPlugins() {
        dataAccessTabPane.getTabs().stream().filter((tab) -> (tabHasEnabledPlugins(tab))).forEachOrdered((tab) -> {
            getQueryPhasePane(tab).getDataAccessPanes().forEach((dataAccessPane) -> {
                dataAccessPane.validityChanged(false);
            });
        });
    }

    /**
     * Store the "running" state of the plug-ins per graph
     */
    private static class GraphState {

        public boolean queriesRunning = false;
        public boolean goButtonIsGo = true;
        public Map<Future<?>, String> runningPlugins = new HashMap<>();
    }

}
