/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Open a new graph with the last used schema (if a graph has been opened this
 * session), otherwise the default (highest priority) schema.
 *
 * @author sirius
 */
@ActionID(category = "Schema", id = "au.gov.asd.tac.constellation.graph.node.NewDefaultSchemaGraphAction")
@ActionRegistration(displayName = "#CTL_NewDefaultSchemaGraphAction", lazy = false)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 100),
    @ActionReference(path = "Toolbars/File", position = 0),
    @ActionReference(path = "Shortcuts", name = "C-N")
})
@NbBundle.Messages("CTL_NewDefaultSchemaGraphAction=New Default Schema Graph")
public class NewDefaultSchemaGraphAction extends AbstractAction implements PreferenceChangeListener {

    private static final String GRAPH_ACTION_THREAD_NAME = "Graph Action";
    private SchemaFactory schemaFactory = null;
    private String template;
    private final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);

    public NewDefaultSchemaGraphAction() {
        prefs.addPreferenceChangeListener(this);
        recreateAction();
    }

    public final void recreateAction() {
        template = prefs.get(ApplicationPreferenceKeys.DEFAULT_TEMPLATE, ApplicationPreferenceKeys.DEFAULT_TEMPLATE_DEFAULT);
        if (!Objects.equals(template, ApplicationPreferenceKeys.DEFAULT_TEMPLATE_DEFAULT)) {
            schemaFactory = null;
            putValue(Action.NAME, "New " + template + " Graph");
            final SchemaFactory factory = SchemaFactoryUtilities.getSchemaFactory(NewSchemaGraphAction.getTemplateNames().get(template));
            if (factory != null) {
                final Icon icon16 = SchemaFactoryUtilities.getSchemaFactory(NewSchemaGraphAction.getTemplateNames().get(template)).getIcon().buildIcon(16);
                putValue(Action.SMALL_ICON, icon16);
                final Icon icon24 = SchemaFactoryUtilities.getSchemaFactory(NewSchemaGraphAction.getTemplateNames().get(template)).getIcon().buildIcon(24);
                putValue(Action.LARGE_ICON_KEY, icon24);
                return;
            }
        }
        final SchemaFactory[] factories = SchemaFactoryUtilities.getSchemaFactories().values().toArray(new SchemaFactory[0]);
        schemaFactory = factories[0];

        putValue(Action.NAME, "New " + schemaFactory.getLabel());
        putValue(Action.LONG_DESCRIPTION, schemaFactory.getDescription());

        final Icon icon16 = schemaFactory.getIcon().buildIcon(16);
        putValue(Action.SMALL_ICON, icon16);

        final Icon icon24 = schemaFactory.getIcon().buildIcon(24);
        putValue(Action.LARGE_ICON_KEY, icon24);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (schemaFactory == null) {
            NewSchemaGraphAction.createTemplate(template);
        } else {
            new Thread() {
                @Override
                public void run() {
                    setName(GRAPH_ACTION_THREAD_NAME);
                    final Graph graph = new DualGraph(schemaFactory.createSchema());
                    final WritableGraph wg = graph.getWritableGraphNow("New " + schemaFactory.getLabel(), false);
                    try {
                        graph.getSchema().newGraph(wg);
                    } finally {
                        wg.commit();
                    }

                    // Give the new graph a default name the reflects the schema.
                    final String graphName = schemaFactory.getLabel().trim().toLowerCase();
                    GraphOpener.getDefault().openGraph(graph, graphName);
                }
            }.start();
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().equals(ApplicationPreferenceKeys.DEFAULT_TEMPLATE)) {
            SwingUtilities.invokeLater(this::recreateAction);
        }
    }
}
