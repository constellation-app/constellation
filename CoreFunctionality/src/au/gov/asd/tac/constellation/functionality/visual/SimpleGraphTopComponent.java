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
package au.gov.asd.tac.constellation.functionality.visual;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.GraphObjectUtilities;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.preferences.DeveloperPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * Top component which displays a Swing table representation of a graph.
 */
@TopComponent.Description(
        preferredID = "SimpleGraphTopComponent",
        iconBase = "au/gov/asd/tac/constellation/functionality/visual/resources/constellation.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(
        mode = "editor",
        openAtStartup = false
)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SimpleGraphAction",
        preferredID = "SimpleGraphTopComponent"
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.functionality.visual.SimpleGraphTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 0),
    @ActionReference(path = "Menu/Experimental/Views", position = 0)
})
@Messages({
    "CTL_SimpleGraphAction=Simple Graph",
    "CTL_SimpleGraphTopComponent=Simple Graph",
    "HINT_SimpleGraphTopComponent=Simple Graph"
})
public final class SimpleGraphTopComponent extends CloneableTopComponent implements GraphChangeListener, UndoRedo.Provider {

    private final InstanceContent content;
    private final Graph graph;
    private final GraphNode graphNode;

    public SimpleGraphTopComponent() {
        initComponents();
        setName(Bundle.CTL_SimpleGraphTopComponent());
        setToolTipText(Bundle.HINT_SimpleGraphTopComponent());

        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject("graph", true);
        graph = new DualGraph(null);
        graphNode = new GraphNode(graph, gdo, this, null);
        content = new InstanceContent();
        content.add(getActionMap());
        content.add(graphNode.getDataObject());
        content.add(graph);
        content.add(graphNode);
        associateLookup(new AbstractLookup(content));
        setActivatedNodes(new Node[]{
            graphNode
        });

        graph.addGraphChangeListener(this);
        MemoryManager.newObject(SimpleGraphTopComponent.class);
    }

    public SimpleGraphTopComponent(final GraphDataObject gdo, final Graph graph) {
        initComponents();
        setName(gdo.getName());
        setToolTipText(gdo.getToolTipText());

        this.graph = graph;
        graphText.setText(gdo.getPrimaryFile().getName());

        graphChanged(null);

        graphNode = new GraphNode(graph, gdo, this, null);
        content = new InstanceContent();
        content.add(getActionMap());
        content.add(graphNode.getDataObject());
        content.add(graph);
        content.add(graphNode);
        associateLookup(new AbstractLookup(content));
        setActivatedNodes(new Node[]{
            graphNode
        });

        graph.addGraphChangeListener(this);
    }

    /**
     * Return the GraphNode belonging to this TopComponent.
     *
     * @return The GraphNode belonging to this TopComponent.
     */
    public GraphNode getGraphNode() {
        return graphNode;
    }

    @Override
    public void graphChanged(final GraphChangeEvent evt) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            vxText.setText(Integer.toString(rg.getVertexCount()));
            txText.setText(Integer.toString(rg.getTransactionCount()));
            globalText.setText(Long.toString(rg.getGlobalModificationCounter()));
            attrText.setText(Long.toString(rg.getAttributeModificationCounter()));
            structureText.setText(Long.toString(rg.getStructureModificationCounter()));
        } finally {
            rg.release();
        }
    }

    @Override
    public void finalize() throws Throwable {
        try {
            MemoryManager.finalizeObject(SimpleGraphTopComponent.class);
        } finally {
            super.finalize();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txText = new javax.swing.JTextField();
        vxText = new javax.swing.JTextField();
        graphText = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        globalText = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        attrText = new javax.swing.JTextField();
        structureText = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.jLabel3.text")); // NOI18N

        txText.setEditable(false);
        txText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.txText.text")); // NOI18N

        vxText.setEditable(false);
        vxText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        vxText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.vxText.text")); // NOI18N

        graphText.setEditable(false);
        graphText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.graphText.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.jLabel5.text")); // NOI18N

        globalText.setEditable(false);
        globalText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        globalText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.globalText.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.jLabel7.text")); // NOI18N

        attrText.setEditable(false);
        attrText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        attrText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.attrText.text")); // NOI18N

        structureText.setEditable(false);
        structureText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        structureText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.structureText.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(graphText, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(vxText, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                            .addComponent(txText)
                            .addComponent(globalText)
                            .addComponent(attrText)
                            .addComponent(structureText))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(graphText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(vxText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(txText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))
                    .addComponent(globalText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(attrText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(structureText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(120, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField attrText;
    private javax.swing.JTextField globalText;
    private javax.swing.JTextField graphText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField structureText;
    private javax.swing.JTextField txText;
    private javax.swing.JTextField vxText;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        super.componentOpened();

        // Try to free up any unused memory
        final boolean forceGarbageCollectOnOpen = NbPreferences.forModule(ApplicationPreferenceKeys.class).getBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_OPEN, DeveloperPreferenceKeys.FORCE_GC_ON_OPEN_DEFAULT);
        if (forceGarbageCollectOnOpen) {
            System.gc();
        }
    }

    @Override
    public void componentClosed() {
        super.componentClosed();

        setActivatedNodes(new Node[]{});

        graph.removeGraphChangeListener(this);

        content.remove(graphNode.getDataObject());
        content.remove(graph);
        content.remove(graphNode);

        graphNode.destroy();

        // Try to free up any unused memory
        final boolean forceGarbageCollectOnClose = NbPreferences.forModule(ApplicationPreferenceKeys.class).getBoolean(DeveloperPreferenceKeys.FORCE_GC_ON_CLOSE, DeveloperPreferenceKeys.FORCE_GC_ON_CLOSE_DEFAULT);
        if (forceGarbageCollectOnClose) {
            System.gc();
        }
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }
}
