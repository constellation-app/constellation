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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Copy to clipboard plugin.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("CopyToClipboardPlugin=Copy To Clipboard")
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
public final class CopyToClipboardPlugin extends SimpleReadPlugin {

    @Override
    public void read(final GraphReadMethods rg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final String text = GraphCopyUtilities.copyGraphTextToSystemClipboard(rg);
        ConstellationLoggerHelper.copyPropertyBuilder(this, text.length(), ConstellationLoggerHelper.SUCCESS);

        GraphCopyUtilities.copySelectedGraphElementsToClipboard(rg);
    }
}
