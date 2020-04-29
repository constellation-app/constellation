/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.attributeeditor;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.preferences.utilities.PreferenceUtilites;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.UndoRedo;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;

/**
 * This is the top component for CONSTELLATION's 'attribute editor' view. The
 * attribute editor is a simple user interface which allows users to view and
 * edit the values for any of the graph's attributes. Like many other
 * CONSTELLATION views, its display corresponds to the current selection on the
 * active graph. The attribute editor also facilitates the adding/removing and
 * editing of attributes (as opposed to their values).
 * <br>
 * There are four main components to the editor:
 * <ul>
 * <li> The user interface, contained here, in {@link AttributeEditorPanel}, and
 * {@link AttributeTitledPane}.
 * </li><li> The data model, described in {@link AttributeState} and
 * {@link AttributeData}, which is populated from the graph using
 * {@link AttributeReader}.
 * </li><li>
 * {@link au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor}
 * classes that describe how each type of attribute should be displayed and
 * edited.
 * </li><li>
 * {@link au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation}
 * classes that make changes to attributes and their values on the graph,
 * usually through plugins.
 * </li>
 * </ul>
 * Note that whilst the structure is to remain as above, the details of the last
 * two components are to be significantly changed in the future. This will
 * entail disentaglement of the GUI, the graph editing, and the representation
 * of attributes.
 *
 * @see AttributeEditorPanel
 * @see
 * au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.views.attributeeditor//AttributeEditor//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "AttributeEditorTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/attributeeditor/resources/attribute_editor.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "explorer",
        id = "au.gov.asd.tac.constellation.views.attributeeditor.AttributeEditorTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 200),
    @ActionReference(path = "Shortcuts", name = "CS-E")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_AttributeEditorAction",
        preferredID = "AttributeEditorTopComponent"
)
@Messages({
    "CTL_AttributeEditorAction=Attribute Editor",
    "CTL_AttributeEditorTopComponent=Attribute Editor",
    "HINT_AttributeEditorTopComponent=Attribute Editor"
})
public final class AttributeEditorTopComponent extends TopComponent implements GraphManagerListener, GraphChangeListener, UndoRedo.Provider, PreferenceChangeListener {

    private static final String ATTRIBUTE_EDITOR_GRAPH_CHANGED_THREAD_NAME = "Attribute Editor Graph Changed Updater";
    private static final String ATTRIBUTE_EDITOR_PREFERENCE_CHANGED_THREAD_NAME = "Attribute Editor Preference Changed Updater";
    private final AttributeEditorPanel attributePanel = new AttributeEditorPanel(this);
    private final Runnable refreshRunnable;
    private Graph activeGraph;
    private AttributeReader reader;
    private long latestGraphChangeID = 0;
    private final Preferences prefs = NbPreferences.forModule(AttributePreferenceKey.class);
    private LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    private Thread refreshThread;

    public AttributeEditorTopComponent() {
        initComponents();
        setName(Bundle.CTL_AttributeEditorTopComponent());
        setToolTipText(Bundle.HINT_AttributeEditorTopComponent());
        final BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);
        add(attributePanel);

        refreshRunnable = () -> {
            try {
                ArrayList<Object> devNull = new ArrayList<>();
                while (queue.drainTo(devNull) > 0) {
                    Thread.sleep(50);
                }

                if (reader != null) {
                    attributePanel.updateEditorPanel(reader.refreshAttributes());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        GraphManager.getDefault().addGraphManagerListener(AttributeEditorTopComponent.this);
        newActiveGraph(GraphManager.getDefault().getActiveGraph());
    }

    public Object[] getMoreData(AttributeData attribute) {
        Object[] result = new Object[0];
        if (reader != null) {
            result = reader.loadMoreDataFor(attribute);
        }
        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        GraphManager.getDefault().addGraphManagerListener(this);
        newActiveGraph(GraphManager.getDefault().getActiveGraph());

        PreferenceUtilites.addPreferenceChangeListener(prefs.absolutePath(), this);
        PreferenceUtilites.addPreferenceChangeListener(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE, this);
    }

    @Override
    public void componentClosed() {
        GraphManager.getDefault().removeGraphManagerListener(this);
        newActiveGraph(null);

        PreferenceUtilites.removePreferenceChangeListener(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE, this);
        PreferenceUtilites.removePreferenceChangeListener(prefs.absolutePath(), this);
    }

    void writeProperties(java.util.Properties p) {
        // Required for @ConvertAsProperties
    }

    void readProperties(java.util.Properties p) {
        // Required for @ConvertAsProperties
    }

    @Override
    public void graphOpened(Graph graph) {
        // This method is required for implementation of GraphManagerListener
    }

    @Override
    public void graphClosed(Graph graph) {
        attributePanel.resetPanel();
    }

    @Override
    public void newActiveGraph(Graph graph) {
        if (activeGraph != graph) {
            if (activeGraph != null) {
                activeGraph.removeGraphChangeListener(this);
            }
            activeGraph = graph;
            if (activeGraph != null) {
                activeGraph.addGraphChangeListener(this);
                reader = new AttributeReader(activeGraph);
                attributePanel.updateEditorPanel(reader.refreshAttributes());
            } else {
                reader = null;
            }
        }
    }

    @Override
    public void graphChanged(GraphChangeEvent event) {
        event = event.getLatest();
        if (event.getId() > latestGraphChangeID) {
            latestGraphChangeID = event.getId();
            if (activeGraph != null && reader != null) {
                queue.add(event);
                if (refreshThread == null || !refreshThread.isAlive()) {
                    refreshThread = new Thread(refreshRunnable);
                    refreshThread.setName(ATTRIBUTE_EDITOR_GRAPH_CHANGED_THREAD_NAME);
                    refreshThread.start();
                }
            }
        }
    }

    @Override
    public UndoRedo getUndoRedo() {
        GraphNode graphNode = GraphNode.getGraphNode(activeGraph);
        return (graphNode == null) ? null : graphNode.getUndoRedoManager();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent event) {
        if (event.getKey().equals(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE)) {
            attributePanel.setFontSize();
        } else {
            if (AttributePreferenceKey.ATTRIBUTE_COLOR_PREFS.contains(event.getKey())) {
                attributePanel.rebuildColourMenu();
            }
            queue.add(event);
            if (refreshThread == null || !refreshThread.isAlive()) {
                refreshThread = new Thread(() -> {
                    try {
                        ArrayList<Object> devNull = new ArrayList<>();
                        while (queue.drainTo(devNull) > 0) {
                            Thread.sleep(50);
                        }
                        if (reader != null) {
                            AttributeState state = reader.refreshAttributes(true);
                            attributePanel.updateEditorPanel(state);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                });
                refreshThread.setName(ATTRIBUTE_EDITOR_PREFERENCE_CHANGED_THREAD_NAME);
                refreshThread.start();
            }
        }
    }
}
