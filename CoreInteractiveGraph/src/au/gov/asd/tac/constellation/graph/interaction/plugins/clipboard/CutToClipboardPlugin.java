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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.BitSet;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Cut to clipboard plugin.
 *
 * @author sirius
 */
@ServiceProvider(service = Plugin.class)
@Messages("CutToClipboardPlugin=Cut to Clipboard")
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
public final class CutToClipboardPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        // Do a copy to the clipboard.
        final String text = GraphCopyUtilities.copyGraphTextToSystemClipboard(wg);
        ConstellationLoggerHelper.copyPropertyBuilder(this, text.length(), ConstellationLoggerHelper.SUCCESS);

        final BitSet[] selectedElements = GraphCopyUtilities.copySelectedGraphElementsToClipboard(wg);
        final BitSet vxCopied = selectedElements[0];
        final BitSet txCopied = selectedElements[1];

        if (vxCopied != null && txCopied != null) {
            // Delete the elements that were copied.
            for (int id = vxCopied.nextSetBit(0); id >= 0; id = vxCopied.nextSetBit(id + 1)) {
                wg.removeVertex(id);
            }

            for (int id = txCopied.nextSetBit(0); id >= 0; id = txCopied.nextSetBit(id + 1)) {
                if (wg.transactionExists(id)) {
                    wg.removeTransaction(id);
                }
            }

            final String msg = Bundle.MSG_Cut(vxCopied.cardinality(), txCopied.cardinality());
            final StatusDisplayer statusDisplayer = StatusDisplayer.getDefault();
            if (statusDisplayer != null) {
                statusDisplayer.setStatusText(msg);
            }
        } else {
            throw new PluginException(PluginNotificationLevel.ERROR, "Failed to copy selection to the clipboard");
        }
    }
}
