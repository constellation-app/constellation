/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.notes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;

/**
 *
 * @author sol695510
 */
public class NotesViewController {

    private final NotesViewTopComponent parent;

    public NotesViewController(final NotesViewTopComponent parent) {
        this.parent = parent;
    }

        /**
     * Add attributes required by the Layers View for it to function
     */
    public void addAttributes() {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null) {
            PluginExecution.withPlugin(new SimpleEditPlugin("Notes View: Add Required Attributes") {
                @Override
                public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
//                    LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
//                    LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
//                    LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
//                    LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);
//                    LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);
                }
            }).executeLater(activeGraph);
        }
    }
    // Here inside this file will be any control that does not relate to
    // visual changes such as reloading menus (will only be responsible for
    // changes like saving values to the graph state.)
}
