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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.clipboard.ConstellationClipboardOwner;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Paste the contents of the clipboard into a graph.
 * <p>
 * This plugin will determine what is on the clipboard (graph or text), and call
 * the appropriate graph/text plugin to deal with it.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("PasteFromClipboardPlugin=Paste from Clipboard")
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
public class PasteFromClipboardPlugin extends SimpleEditPlugin {
    
    private static final Logger LOGGER = Logger.getLogger(PasteFromClipboardPlugin.class.getName());

    @Override
    public PluginParameters createParameters() {
        return new PluginParameters();
    }

    @Override
    protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        try {
            final Clipboard cb = ConstellationClipboardOwner.getConstellationClipboard();
            if (cb.isDataFlavorAvailable(RecordStoreTransferable.RECORDSTORE_FLAVOR)) {
                final RecordStoreTransferable rt = (RecordStoreTransferable) cb.getContents(null);
                final RecordStore cbRecordStore = (RecordStore) rt.getTransferData(RecordStoreTransferable.RECORDSTORE_FLAVOR);
                if (cbRecordStore != null) {
                    // There is a graph on the local clipboard.
                    PluginExecution.withPlugin(InteractiveGraphPluginRegistry.PASTE_GRAPH)
                            .withParameter(PasteGraphPlugin.RECORDSTORE_PARAMETER_ID, cbRecordStore)
                            .executeNow(wg);
                }
            }
        } catch (final UnsupportedFlavorException | IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

    }
}
