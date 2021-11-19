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
package au.gov.asd.tac.constellation.graph.file.open;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.io.File;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Open a file.
 *
 * @author canis_majoris
 * @author sol695510
 */
@ServiceProvider(service = Plugin.class)
@Messages("OpenFilePlugin=Open File")
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.LOW_LEVEL})
public class OpenFilePlugin extends SimpleReadPlugin {

    private static File savedDirectory = FileChooser.DEFAULT_DIRECTORY;

    private static final String TITLE = "Open";

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass().getName());
    }

    @Override
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final FileChooserBuilder fileChooser = getOpenFileChooser();

        FileChooser.openMultiDialog(fileChooser).thenAccept(optionalFiles -> optionalFiles.ifPresent(selectedFiles -> {
            savedDirectory = FileChooser.REMEMBER_OPEN_AND_SAVE_LOCATION ? selectedFiles.get(0) : FileChooser.DEFAULT_DIRECTORY;
            selectedFiles.forEach(file -> OpenFile.openFile(file, -1));
        }));
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getOpenFileChooser() {
        return FileChooser.getBaseFileChooserBuilder(TITLE, savedDirectory, FileChooser.CONSTELLATION_FILE_FILTER)
                .setAcceptAllFileFilterUsed(false)
                .setFilesOnly(true);
    }
}
