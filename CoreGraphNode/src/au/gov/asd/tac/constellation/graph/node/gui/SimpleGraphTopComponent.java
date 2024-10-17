/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.node.gui;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.GraphObjectUtilities;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * Top component which displays a Swing table representation of a graph.
 */
@TopComponent.Description(
        preferredID = "SimpleGraphTopComponent",
        iconBase = "au/gov/asd/tac/constellation/graph/node/gui/resources/constellation.png",
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
        id = "au.gov.asd.tac.constellation.graph.node.gui.SimpleGraphTopComponent"
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

    // For cleaning up object for garbage collection. Replaced finalize
    private static final Cleaner cleaner = Cleaner.create();
    private final Cleanable cleanable = cleaner.register(this, cleanupAction);
    private static final Runnable cleanupAction = () -> MemoryManager.finalizeObject(SimpleGraphTopComponent.class);

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
        schemaText.setText(graph.getSchema().getFactory().getName());

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

        graph.addGraphChangeListener(SimpleGraphTopComponent.this);
    }

    /**
     * Return the GraphNode belonging to this TopComponent.
     *
     * @return The GraphNode belonging to this TopComponent.
     */
    public GraphNode getGraphNode() {
        return graphNode;
    }

    public Cleanable getCleanable() {
        return cleanable;
    }

    @Override
    public void graphChanged(final GraphChangeEvent event) {
        final ReadableGraph readableGraph = graph.getReadableGraph();
        try {
            vertexCountText.setText(Integer.toString(readableGraph.getVertexCount()));
            transactionCountText.setText(Integer.toString(readableGraph.getTransactionCount()));
            edgeCountText.setText(Integer.toString(readableGraph.getEdgeCount()));
            linkCountText.setText(Integer.toString(readableGraph.getLinkCount()));
            globalModificationCounterText.setText(Long.toString(readableGraph.getGlobalModificationCounter()));
            attributeModificationCounterText.setText(Long.toString(readableGraph.getAttributeModificationCounter()));
            structureModificationCounterText.setText(Long.toString(readableGraph.getStructureModificationCounter()));
        } finally {
            readableGraph.release();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        graphLabel = new javax.swing.JLabel();
        vertexCountLabel = new javax.swing.JLabel();
        transactionCountLabel = new javax.swing.JLabel();
        transactionCountText = new javax.swing.JTextField();
        vertexCountText = new javax.swing.JTextField();
        graphText = new javax.swing.JTextField();
        modificationCountersLabel = new javax.swing.JLabel();
        globalModificationCounterLabel = new javax.swing.JLabel();
        globalModificationCounterText = new javax.swing.JTextField();
        attributeModificationCounterLabel = new javax.swing.JLabel();
        structureModificationCounterLabel = new javax.swing.JLabel();
        attributeModificationCounterText = new javax.swing.JTextField();
        structureModificationCounterText = new javax.swing.JTextField();
        schemaText = new javax.swing.JTextField();
        schemaLabel = new javax.swing.JLabel();
        edgeCountLabel = new javax.swing.JLabel();
        linkCountLabel = new javax.swing.JLabel();
        edgeCountText = new javax.swing.JTextField();
        linkCountText = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(graphLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.graphLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(vertexCountLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.vertexCountLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(transactionCountLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.transactionCountLabel.text")); // NOI18N

        transactionCountText.setEditable(false);
        transactionCountText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        transactionCountText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.transactionCountText.text")); // NOI18N
        transactionCountText.setEnabled(false);

        vertexCountText.setEditable(false);
        vertexCountText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        vertexCountText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.vertexCountText.text")); // NOI18N
        vertexCountText.setEnabled(false);

        graphText.setEditable(false);
        graphText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.graphText.text")); // NOI18N
        graphText.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(modificationCountersLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.modificationCountersLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(globalModificationCounterLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.globalModificationCounterLabel.text")); // NOI18N

        globalModificationCounterText.setEditable(false);
        globalModificationCounterText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        globalModificationCounterText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.globalModificationCounterText.text")); // NOI18N
        globalModificationCounterText.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(attributeModificationCounterLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.attributeModificationCounterLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(structureModificationCounterLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.structureModificationCounterLabel.text")); // NOI18N

        attributeModificationCounterText.setEditable(false);
        attributeModificationCounterText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        attributeModificationCounterText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.attributeModificationCounterText.text")); // NOI18N
        attributeModificationCounterText.setEnabled(false);

        structureModificationCounterText.setEditable(false);
        structureModificationCounterText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        structureModificationCounterText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.structureModificationCounterText.text")); // NOI18N
        structureModificationCounterText.setEnabled(false);

        schemaText.setEditable(false);
        schemaText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        schemaText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.schemaText.text")); // NOI18N
        schemaText.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(schemaLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.schemaLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(edgeCountLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.edgeCountLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(linkCountLabel, org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.linkCountLabel.text")); // NOI18N

        edgeCountText.setEditable(false);
        edgeCountText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        edgeCountText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.edgeCountText.text")); // NOI18N
        edgeCountText.setEnabled(false);

        linkCountText.setEditable(false);
        linkCountText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        linkCountText.setText(org.openide.util.NbBundle.getMessage(SimpleGraphTopComponent.class, "SimpleGraphTopComponent.linkCountText.text")); // NOI18N
        linkCountText.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(globalModificationCounterLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(attributeModificationCounterLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(structureModificationCounterLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(globalModificationCounterText, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attributeModificationCounterText, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(structureModificationCounterText, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(modificationCountersLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(graphLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(linkCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(edgeCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(vertexCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(schemaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(transactionCountLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(vertexCountText, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(schemaText, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(linkCountText, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(edgeCountText, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(graphText, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(transactionCountText, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(graphLabel)
                    .addComponent(graphText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(schemaLabel)
                    .addComponent(schemaText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vertexCountLabel)
                    .addComponent(vertexCountText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(transactionCountLabel)
                    .addComponent(transactionCountText, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edgeCountLabel)
                    .addComponent(edgeCountText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(linkCountLabel)
                    .addComponent(linkCountText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addComponent(modificationCountersLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(globalModificationCounterLabel)
                    .addComponent(globalModificationCounterText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attributeModificationCounterLabel)
                    .addComponent(attributeModificationCounterText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(structureModificationCounterLabel)
                    .addComponent(structureModificationCounterText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel attributeModificationCounterLabel;
    private javax.swing.JTextField attributeModificationCounterText;
    private javax.swing.JLabel edgeCountLabel;
    private javax.swing.JTextField edgeCountText;
    private javax.swing.JLabel globalModificationCounterLabel;
    private javax.swing.JTextField globalModificationCounterText;
    private javax.swing.JLabel graphLabel;
    private javax.swing.JTextField graphText;
    private javax.swing.JLabel linkCountLabel;
    private javax.swing.JTextField linkCountText;
    private javax.swing.JLabel modificationCountersLabel;
    private javax.swing.JLabel schemaLabel;
    private javax.swing.JTextField schemaText;
    private javax.swing.JLabel structureModificationCounterLabel;
    private javax.swing.JTextField structureModificationCounterText;
    private javax.swing.JLabel transactionCountLabel;
    private javax.swing.JTextField transactionCountText;
    private javax.swing.JLabel vertexCountLabel;
    private javax.swing.JTextField vertexCountText;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentClosed() {
        super.componentClosed();

        setActivatedNodes(new Node[]{});

        graph.removeGraphChangeListener(this);

        content.remove(graphNode.getDataObject());
        content.remove(graph);
        content.remove(graphNode);

        graphNode.destroy();
    }
}
