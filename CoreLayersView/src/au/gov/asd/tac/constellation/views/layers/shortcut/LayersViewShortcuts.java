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

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.layers.LayersViewController;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.utilities.LayersUtilities;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateLayerSelectionPlugin;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
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

    private void triggerLayerSelection(final int layerId) {
        BitMaskQueryCollection vxCollection = LayersViewController.getDefault().getVxQueryCollection();
        BitMaskQueryCollection txCollection = LayersViewController.getDefault().getTxQueryCollection();

        if (vxCollection.getQuery(layerId) != null) {
            vxCollection.getQuery(layerId).setVisibility(!vxCollection.getQuery(layerId).getVisibility());
        }
        if (txCollection.getQuery(layerId) != null) {
            txCollection.getQuery(layerId).setVisibility(!txCollection.getQuery(layerId).getVisibility());
        }

        final int newBitmask = LayersUtilities.calculateCurrentLayerSelectionBitMask(vxCollection, txCollection);

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(newBitmask))
                .executeLater(GraphManager.getDefault().getActiveGraph());

        final Future<?> future = LayersViewController.getDefault().writeState();

        try {
            if(future != null){
                future.get();
            }
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (!LayersViewController.getDefault().getParentVisibility()) {
            LayersViewController.getDefault().readStateFuture();
            LayersViewController.getDefault().updateQueries(GraphManager.getDefault().getActiveGraph());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String hotkey = e.getActionCommand();
        switch (hotkey) {
            case "CA-L":
                PluginExecution.withPlugin(new NewLayerPlugin()).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-D":
                final Future<?> deselectFuture = PluginExecution.withPlugin(new DeselectAllLayersPlugin()).executeLater(GraphManager.getDefault().getActiveGraph());
                try {
                    deselectFuture.get();
                } catch (final InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
                LayersViewController.getDefault().updateQueriesFuture(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-1":
                triggerLayerSelection(1);
                break;
            case "CA-2":
                triggerLayerSelection(2);
                break;
            case "CA-3":
                triggerLayerSelection(3);
                break;
            case "CA-4":
                triggerLayerSelection(4);
                break;
            case "CA-5":
                triggerLayerSelection(5);
                break;
            case "CA-6":
                triggerLayerSelection(6);
                break;
            case "CA-7":
                triggerLayerSelection(7);
                break;
            case "CA-8":
                triggerLayerSelection(8);
                break;
            case "CA-9":
                triggerLayerSelection(9);
                break;
            default:
                break;
        }
    }
}
