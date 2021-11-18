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
package au.gov.asd.tac.constellation.graph.node.create;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNodePluginRegistry;
import au.gov.asd.tac.constellation.graph.node.templates.LoadTemplatePlugin;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Open a new graph with the specified schema.
 *
 * @author sirius
 */
@ActionID(category = "Schema", id = "au.gov.asd.tac.constellation.graph.node.NewSchemaGraphAction")
@ActionRegistration(displayName = "#CTL_NewSchemaGraphAction", lazy = false)
@ActionReference(path = "Menu/File", position = 200)
@NbBundle.Messages("CTL_NewSchemaGraphAction=New Schema Graph")
public class NewSchemaGraphAction extends AbstractAction implements DynamicMenuContent {

    private static final String GRAPH_ACTION_THREAD_NAME = "New Schema Graph Action";
    private static final String TEMPLATE_DIR_NAME = "Graph Templates";
    private static File TEMPLATE_DIRECTORY = null;
    private static final List<JMenuItem> TEMPLATES_MENU = new ArrayList<>();
    private static Map<String, String> templates;
    private static JMenu menu;
    private static final Map<String, Icon> ICON_CACHE = new HashMap<>();

    public static File getTemplateDirectory() {
        return TEMPLATE_DIRECTORY;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Override required for ActionListener
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] jcs) {
        return getMenuPresenters();
    }

    @Override
    public JComponent[] getMenuPresenters() {
        menu = new JMenu("New Graph");
        for (final SchemaFactory schemaFactory : SchemaFactoryUtilities.getSchemaFactories().values()) {
            if (isValid(schemaFactory)) {
                if (!ICON_CACHE.containsKey(schemaFactory.getName())) {
                    ICON_CACHE.put(schemaFactory.getName(), schemaFactory.getIcon().buildIcon(16));
                }
                final JMenuItem item = new JMenuItem(schemaFactory.getLabel(), ICON_CACHE.get(schemaFactory.getName()));
                item.setToolTipText(schemaFactory.getDescription());
                item.setActionCommand(schemaFactory.getName());
                item.addActionListener((final ActionEvent e) -> new Thread() {
                    @Override
                    public void run() {
                        setName(GRAPH_ACTION_THREAD_NAME);
                        final Graph graph = new DualGraph(schemaFactory.createSchema());
                        final WritableGraph wg = graph.getWritableGraphNow("New " + schemaFactory.getName(), false);
                        try {
                            graph.getSchema().newGraph(wg);
                        } finally {
                            wg.commit();
                        }

                        final String graphName = schemaFactory.getLabel().replace(" ", "").toLowerCase();
                        GraphOpener.getDefault().openGraph(graph, graphName);
                    }
                }.start());

                menu.add(item);
            }
        }

        menu.addSeparator();
        recreateTemplateMenuItems();
        return new JComponent[]{menu};
    }

    private static boolean loadTemplateDirectory() {
        if (TEMPLATE_DIRECTORY == null) {
            final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
            final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
            TEMPLATE_DIRECTORY = new File(userDir, TEMPLATE_DIR_NAME);
        }
        return TEMPLATE_DIRECTORY.isDirectory();
    }

    public static Map<String, String> getTemplateNames() {
        templates = new HashMap<>();
        List<String> schemaSubdirs = loadTemplateDirectory() ? Arrays.asList(TEMPLATE_DIRECTORY.list()) : Collections.emptyList();
        schemaSubdirs.forEach(schema -> {
            final File subdir = new File(TEMPLATE_DIRECTORY, schema);
            if (subdir.isDirectory()) {
                for (String template : subdir.list()) {
                    templates.put(template, schema);
                }
            }
        });
        return templates;
    }

    public static void recreateTemplateMenuItems() {
        SwingUtilities.invokeLater(() -> {
            final Map<String, String> templates = getTemplateNames();
            TEMPLATES_MENU.forEach(item -> menu.remove(item));
            TEMPLATES_MENU.clear();
            templates.forEach((template, schema) -> {
                SchemaFactory factory = SchemaFactoryUtilities.getSchemaFactory(schema);
                if (factory != null) {
                    if (!ICON_CACHE.containsKey(factory.getName())) {
                        ICON_CACHE.put(factory.getName(), SchemaFactoryUtilities.getSchemaFactory(schema).getIcon().buildIcon(16));
                    }
                    JMenuItem item = new JMenuItem(template + " Graph", ICON_CACHE.get(factory.getName()));
                    item.addActionListener((final ActionEvent e) -> createTemplate(template));
                    TEMPLATES_MENU.add(item);
                }
            });
            TEMPLATES_MENU.forEach(item -> menu.add(item));
        });
    }

    static void createTemplate(final String templateName) {
        final Plugin plugin = PluginRegistry.get(GraphNodePluginRegistry.LOAD_TEMPLATE);
        PluginParameters params = plugin.createParameters();
        params.setObjectValue(LoadTemplatePlugin.TEMPLATE_FILE_PARAMETER_ID, new File(TEMPLATE_DIRECTORY, templates.get(templateName) + "/" + templateName));
        params.setStringValue(LoadTemplatePlugin.TEMPLATE_NAME_PARAMETER_ID, templateName);
        PluginExecution.withPlugin(plugin).withParameters(params).executeLater(null);
    }

    protected boolean isValid(final SchemaFactory schemaFactory) {
        return schemaFactory.isPrimarySchema();
    }
}
