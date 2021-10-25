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
package au.gov.asd.tac.constellation.views.layers.shortcut;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.layers.LayersViewController;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateLayerSelectionPlugin;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Defines the keyboard shortcuts for use with the Layers view
 *
 * @author formalhaut69
 */
@ActionID(category = "Options", id = "au.gov.asd.tac.constellation.views.layersviewshortcuts")
@ActionRegistration(displayName = "#CTL_LayersViewShortcuts", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "CA-D"),
    @ActionReference(path = "Shortcuts", name = "CA-L"),
    @ActionReference(path = "Shortcuts", name = "CA-1"),
    @ActionReference(path = "Shortcuts", name = "CA-2"),
    @ActionReference(path = "Shortcuts", name = "CA-3"),
    @ActionReference(path = "Shortcuts", name = "CA-4"),
    @ActionReference(path = "Shortcuts", name = "CA-5"),
    @ActionReference(path = "Shortcuts", name = "CA-6"),
    @ActionReference(path = "Shortcuts", name = "CA-7"),
    @ActionReference(path = "Shortcuts", name = "CA-8"),
    @ActionReference(path = "Shortcuts", name = "CA-9")
})
@NbBundle.Messages("CTL_LayersViewShortcuts=Layers View: Shortcuts")
public class LayersViewShortcuts extends AbstractAction {
    
    private static final Logger LOGGER = Logger.getLogger(LayersViewShortcuts.class.getName());

    private void triggerLayerSelection(final Graph graph, final int layerId) {
        final BitMaskQueryCollection vxCollection = LayersViewController.getDefault().getVxQueryCollection();
        final BitMaskQueryCollection txCollection = LayersViewController.getDefault().getTxQueryCollection();

        if (vxCollection.getQuery(layerId) != null) {
            vxCollection.getQuery(layerId).setVisibility(!vxCollection.getQuery(layerId).getVisibility());
        }
        if (txCollection.getQuery(layerId) != null) {
            txCollection.getQuery(layerId).setVisibility(!txCollection.getQuery(layerId).getVisibility());
        }

        final int newBitmask = LayersUtilities.calculateCurrentLayerSelectionBitMask(vxCollection, txCollection);

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(newBitmask))
                .executeLater(graph);

        final Future<?> future = LayersViewController.getDefault().writeState();

        try {
            if (future != null) {
                future.get();
            }
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Layers State Writer was interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        if (!LayersViewController.getDefault().getParentVisibility()) {
            LayersViewController.getDefault().readStateFuture();
            LayersViewController.getDefault().updateQueries(graph);
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final String hotkey = e.getActionCommand();
        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
        switch (hotkey) {
            case "CA-L":
                PluginExecution.withPlugin(new NewLayerPlugin()).executeLater(currentGraph);
                break;
            case "CA-D":
                final Future<?> deselectFuture = PluginExecution.withPlugin(new DeselectAllLayersPlugin()).executeLater(currentGraph);
                try {
                    deselectFuture.get();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, "Deselecting All layers was interrupted", ex);
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
                LayersViewController.getDefault().updateQueriesFuture(currentGraph);
                break;
            case "CA-1":
                triggerLayerSelection(currentGraph, 1);
                break;
            case "CA-2":
                triggerLayerSelection(currentGraph, 2);
                break;
            case "CA-3":
                triggerLayerSelection(currentGraph, 3);
                break;
            case "CA-4":
                triggerLayerSelection(currentGraph, 4);
                break;
            case "CA-5":
                triggerLayerSelection(currentGraph, 5);
                break;
            case "CA-6":
                triggerLayerSelection(currentGraph, 6);
                break;
            case "CA-7":
                triggerLayerSelection(currentGraph, 7);
                break;
            case "CA-8":
                triggerLayerSelection(currentGraph, 8);
                break;
            case "CA-9":
                triggerLayerSelection(currentGraph, 9);
                break;
            default:
                break;
        }
    }
}
