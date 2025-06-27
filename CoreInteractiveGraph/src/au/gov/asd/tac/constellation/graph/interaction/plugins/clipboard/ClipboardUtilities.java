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

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * A clipboard utility class
 *
 * @author arcturus
 */
public class ClipboardUtilities {
    
    private ClipboardUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static void copyToClipboard(final String text) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);

        PluginExecution.withPlugin(new CopyToClipboard(text)).interactively(true).executeLater(null);
    }

    /**
     * Plugin to copy to the clipboard.
     */
    @PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
    private static class CopyToClipboard extends SimplePlugin {

        private final String text;

        public CopyToClipboard(final String text) {
            this.text = text;
        }

        @Override
        public String getName() {
            return "Clipboard Utilities: Copy To Clipboard";
        }

        @Override
        protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            ConstellationLoggerHelper.copyPropertyBuilder(this, text.length(), ConstellationLoggerHelper.SUCCESS);
        }
    }
}
