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
package au.gov.asd.tac.constellation.views.dataaccess.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessConcept;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.prefs.Preferences;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Save and load data access view plugin parameters.
 * <p>
 * Parameters are saved using their names as keys. Therefore, having two plugins
 * with the same parameter names is a bad idea. Parameter names should be
 * qualified with their simple class name: using the fully qualified name
 * (including the package) would cause saved parameters to be useless if classes
 * are refactored into different packages. Note that refactoring the class name
 * will also break a saved file, but we'll take our chances.
 *
 * @author algol
 */
public class ParameterIOUtilities {

    private static final String DATA_ACCESS_DIR = "DataAccessView";
    public static final String GLOBAL_OBJECT = "global";
    public static final String PLUGINS_OBJECT = "plugins";
    public static final String IS_ENABLED = "__is_enabled__";

    /**
     * Load the data access graph state and update the data access view.
     * <p>
     * Currently only looking at string parameter types and only shows the first
     * step if it exists.
     *
     * @param dap The data access pane
     * @param graph The active graph to load the state from
     */
    public static void loadDataAccessState(final DataAccessPane dap, final Graph graph) {
        if (graph != null && dap.getCurrentTab() != null) {
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                final int dataAccessStateAttribute = DataAccessConcept.MetaAttribute.DATAACCESS_STATE.get(rg);
                if (dataAccessStateAttribute != Graph.NOT_FOUND) {
                    final DataAccessState dataAccessState = rg.getObjectValue(dataAccessStateAttribute, 0);
                    if (dataAccessState != null && dataAccessState.getState().size() > 0) {
                        for (Map<String, String> tabState : dataAccessState.getState()) {
                            final Tab step = dap.getCurrentTab().getTabPane().getTabs().get(0);
                            final QueryPhasePane pluginPane = (QueryPhasePane) ((ScrollPane) step.getContent()).getContent();
                            pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet().stream().forEach((param) -> {
                                final PluginParameter<?> pp = param.getValue();
                                final String paramvalue = tabState.get(param.getKey());
                                if (paramvalue != null) {
                                    pp.setStringValue(paramvalue); // appologies for the nestedness
                                }
                            });
                            break;
                            // TODO: support multiple tabs, but don't introduce memory leaks
                        }
                    }
                }
            } finally {
                rg.release();
            }
        }
    }

    /**
     * Save the data access state to the graph.
     * <p>
     * Currently only global parameters are saved.
     *
     * @param tabs The TabPane
     * @param graph The active graph to save the state to
     */
    public static void saveDataAccessState(final TabPane tabs, final Graph graph) {
        if (graph != null) {
            // buildId the data access state object
            final DataAccessState dataAccessState = new DataAccessState();
            for (final Tab step : tabs.getTabs()) {
                dataAccessState.newTab();
                final QueryPhasePane pluginPane = (QueryPhasePane) ((ScrollPane) step.getContent()).getContent();
                for (final Map.Entry<String, PluginParameter<?>> param : pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet()) {
                    final String id = param.getKey();
                    final PluginParameter<?> pp = param.getValue();
                    final String value = pp.getStringValue();
                    if (value != null) {
                        dataAccessState.add(id, value);
                    }
                }
            }

            // save the state onto the graph
            WritableGraph wg = null;
            try {
                wg = graph.getWritableGraph("Update Data Access State", true);
                final int dataAccessStateAttribute = DataAccessConcept.MetaAttribute.DATAACCESS_STATE.ensure(wg);
                wg.setObjectValue(dataAccessStateAttribute, 0, dataAccessState);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (wg != null) {
                    wg.commit();
                }
            }
        }
    }

    public static void saveParameters(final TabPane tabs) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File dataAccessDir = new File(userDir, DATA_ACCESS_DIR);
        if (!dataAccessDir.exists()) {
            dataAccessDir.mkdir();
        }

        if (!dataAccessDir.isDirectory()) {
            final String msg = String.format("Can't create data access directory '%s'.", dataAccessDir);
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        // A JSON document to store everything in;
        // an array of objects where each array element is a tab, and the objects are the parameters in each tab.
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode rootNode = mapper.createArrayNode();

        String queryName = null;
        for (final Tab step : tabs.getTabs()) {
            // Remember the global parameters: if plugins have these, they don't need to be saved.
            final Set<String> globalParams = new HashSet<>();

            final ObjectNode tabNode = rootNode.addObject();
            final ObjectNode global = tabNode.putObject(GLOBAL_OBJECT);
            final QueryPhasePane pluginPane = (QueryPhasePane) ((ScrollPane) step.getContent()).getContent();
            for (final Map.Entry<String, PluginParameter<?>> param : pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet()) {
                final String id = param.getKey();
                final String value = param.getValue().getStringValue();
                if (value != null) {
                    global.put(id, value);
                } else {
                    global.putNull(id);
                }

                globalParams.add(id);

                // Remember the first non-null, non-blank query name.
                if (queryName == null && id.equals(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID) && value != null && !value.trim().isEmpty()) {
                    queryName = value;
                }
            }

            final ObjectNode plugins = tabNode.putObject(PLUGINS_OBJECT);
            for (final DataSourceTitledPane pane : pluginPane.getDataAccessPanes()) {
                // Is this plugin enabled?
                // Only save data if it is.
                if (pane.isQueryEnabled()) {
                    final String isEnabledId = String.format("%s.%s", pane.getPlugin().getClass().getSimpleName(), IS_ENABLED);
                    plugins.put(isEnabledId, pane.isQueryEnabled());

                    final PluginParameters parameters = pane.getParameters();
                    if (parameters != null) {
                        parameters.getParameters().entrySet().stream().forEach((param) -> {
                            if (!PasswordParameterType.ID.equals(param.getValue().getType().getId())) {
                                final String id = param.getKey();
                                final String value = param.getValue().getStringValue();
                                if (!globalParams.contains(id)) {
                                    if (value != null) {
                                        plugins.put(id, value);
                                    } else {
                                        plugins.putNull(id);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }

        if (queryName != null) {
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
            final File f = new File(dataAccessDir, encode(queryName + ".json"));
            boolean go = true;
            if (f.exists()) {
                final String msg = String.format("'%s' already exists. Do you want to overwrite it?", queryName);
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Data access parameter file exists");
                alert.setContentText(msg);
                final Optional<ButtonType> option = alert.showAndWait();
                go = option.isPresent() && option.get() == ButtonType.OK;
            }

            if (go) {
                try {
                    mapper.writeValue(f, rootNode);
                    StatusDisplayer.getDefault().setStatusText(String.format("Query saved to %s.", f.getPath()));
                } catch (IOException ex) {
                    final String msg = String.format("Can't save data access view configuration: %s", ex.getMessage());
                    final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        } else {
            final NotifyDescriptor nd = new NotifyDescriptor.Message("There must be a valid query name.", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    public static void loadParameters(final DataAccessPane dap) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File dataAccessDir = new File(userDir, DATA_ACCESS_DIR);
        final String[] names;
        if (dataAccessDir.isDirectory()) {
            names = dataAccessDir.list((File dir, String name) -> {
                return name.toLowerCase().endsWith(".json");
            });
        } else {
            names = new String[0];
        }

        // Chop off ".json".
        for (int i = 0; i < names.length; i++) {
            names[i] = decode(names[i].substring(0, names[i].length() - 5));
        }

        final String queryName = QueryListDialog.getQueryName(dap, names);
        if (queryName != null) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode root = mapper.readTree(new File(dataAccessDir, encode(queryName) + ".json"));
                if (root.isArray()) {
                    // Remove all the existing tabs and start some new ones.
                    dap.removeTabs();
                    for (final JsonNode step : root) {
                        final QueryPhasePane pluginPane = dap.newTab();

                        // Remember the global parameters: if plugins have these, they don't need to be loaded.
                        final Set<String> globalParams = new HashSet<>();

                        // Load the per-step global parameters.
                        final JsonNode global = step.get(GLOBAL_OBJECT);
                        pluginPane.getGlobalParametersPane().getParams().getParameters().entrySet().stream().forEach((param) -> {
                            final String id = param.getKey();
                            if (global.has(id)) {
                                final JsonNode value = global.get(id);
                                final PluginParameter pp = param.getValue();
                                pp.setStringValue(value.isNull() ? null : value.textValue());

                                globalParams.add(id);
                            }
                        });

                        // Load the per-step plugin parameters.
                        final JsonNode plugins = step.get(PLUGINS_OBJECT);
                        final Map<String, Map<String, String>> ppmap = toPerPluginParamMap(plugins);
                        pluginPane.getDataAccessPanes().stream().forEach((pane) -> {
                            // Only load and enable from the JSON if the JSON contains data for this plugin
                            // and it's enabled; otherwise, disable the plugin.
                            // They're disabled by default anyway, but let's be obvious.)
                            final String isEnabledId = String.format("%s.%s", pane.getPlugin().getClass().getSimpleName(), IS_ENABLED);
                            if (plugins.has(isEnabledId)) {
                                // Is this plugin enabled in the saved JSON?
                                final boolean isEnabled = plugins.get(isEnabledId).booleanValue();
//                                pane.validityChanged(isEnabled);
                                if (isEnabled) {
                                    pane.setParameterValues(ppmap.get(pane.getPlugin().getClass().getSimpleName()));
                                    // TODO: review this section, remove it if its working, else fix it
////                                    pane.setExpanded(true);
//                                    final PluginParameters parameters = pane.getParameters();
//                                    if(parameters != null)
//                                    {
//                                        parameters.getParameters().entrySet().stream().forEach((param) ->
//                                        {
//                                            final String id = param.getKey();
//                                            if(!globalParams.contains(id) && plugins.has(id))
//                                            {
//                                                final JsonNode newValue = plugins.get(id);
//                                                final PluginParameter pp = param.getValue();
//                                                // Don't set action type parameters.
//                                                // Since their only reason for existence is to perform an action,
//                                                // they don't have values, and setting them would kick off the action.
//                                                //                                                if(!pp.getId().equals(ActionParameterType.ID))
//                                                {
//                                                    // There appears to be a listener on each parameter so that if it is updated,
//                                                    // it will be selected. We want to avoid this: only set a parameter value if
//                                                    // the new value is different (including allowing for null).
//                                                    //                                                    final String oldValue = pp.getStringValue();
//                                                    if(oldValue==null)
//                                                    {
//                                                        if(!newValue.isNull())
//                                                        {
//                                                            pp.setStringValue(newValue.textValue());
//                                                        }
//                                                    }
//                                                    else
//                                                    {
//                                                        if(newValue.isNull())
//                                                        {
//                                                            pp.setStringValue(null);
//                                                        }
//                                                        else
//                                                        {
//                                                            final String s = newValue.textValue();
//                                                            if(!s.equals(oldValue))
//                                                            {
//                                                                pp.setStringValue(s);
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        });
//                                    }

//                                pane.validityChanged(isEnabled);
                                }
//                                pane.validityChanged(isEnabled);
                            } else {
                                // This plugin isn't mentioned in the JSON, so disable it.
                                pane.validityChanged(false);
                            }
                        });
                    }

                    StatusDisplayer.getDefault().setStatusText(String.format("Query '%s' loaded.", encode(queryName)));
                }
            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Convert the contents of a JSON "plugins" node to a per-plugin key:value
     * map.
     * <p>
     * The JSON object looks like:
     * <pre>
     *      plugina.param1: value,
     *      plugina.param2: value,
     *      pluginb.param1: value,
     *      pluginc.param1: value
     * </pre> Build a Map mapping plugin names to Maps of param key:values.
     *
     * @param pluginsNode
     * @return
     */
    private static Map<String, Map<String, String>> toPerPluginParamMap(final JsonNode pluginsNode) {
        final Map<String, Map<String, String>> pluginMap = new HashMap<>();
        for (final Iterator<Map.Entry<String, JsonNode>> i = pluginsNode.fields(); i.hasNext();) {
            final Map.Entry<String, JsonNode> entry = i.next();
            final String name = entry.getKey();
            final String value = entry.getValue().textValue();

            final int ix = name.indexOf('.');
            if (ix != -1) {
                final String plugin = name.substring(0, ix);
                if (!pluginMap.containsKey(plugin)) {
                    pluginMap.put(plugin, new HashMap<>());
                }

                pluginMap.get(plugin).put(name, value);
            }
        }

        return pluginMap;
    }

    /**
     * Encode a String so it can be used as a filename.
     *
     * @param s The String to be encoded.
     *
     * @return The encoded String.
     */
    public static String encode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (final char c : s.toCharArray()) {
            if (isValidFileCharacter(c)) {
                b.append(c);
            } else {
                b.append(String.format("_%04x", (int) c));
            }
        }

        return b.toString();
    }

    /**
     * Decode a String that has been encoded by {@link encode(String)}.
     *
     * @param s The String to be decoded.
     *
     * @return The decoded String.
     */
    static String decode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c != '_') {
                b.append(c);
            } else {
                final String hex = s.substring(i + 1, Math.min(i + 5, s.length()));
                if (hex.length() == 4) {
                    try {
                        final int value = Integer.parseInt(hex, 16);
                        b.append((char) value);
                        i += 4;
                    } catch (final NumberFormatException ex) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }

        return b.toString();
    }

    static boolean isValidFileCharacter(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == ' ' || c == '-' || c == '.';
    }
}
