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
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.UserCancelException;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Open a file
 *
 * @author canis_majoris
 */
@ServiceProvider(service = Plugin.class)
@Messages("OpenFilePlugin=Open File")
@PluginInfo(pluginType = PluginType.IMPORT, tags = {"LOW LEVEL"})
public class OpenFilePlugin extends SimpleReadPlugin {

    private boolean running;
    private static File currentDirectory = null;

    private static void setCurrentDirectory(final File currentDir) {
        currentDirectory = currentDir;
    }

    /**
     * Creates and initializes a file chooser.
     *
     * @return the initialized file chooser
     */
    protected JFileChooser prepareFileChooser() {
        final JFileChooser chooser = new FileChooser();
        chooser.setCurrentDirectory(getCurrentDirectory());
        return chooser;
    }

    /**
     * Displays the specified file chooser and returns a list of selected files.
     *
     * @param chooser file chooser to display
     * @return array of selected files,
     * @exception org.openide.util.UserCancelException if the user cancelled the
     * operation
     */
    public static File[] chooseFilesToOpen(final JFileChooser chooser)
            throws UserCancelException {
        File[] files;
        do {
            final int selectedOption = chooser.showOpenDialog(
                    WindowManager.getDefault().getMainWindow());

            if (selectedOption != JFileChooser.APPROVE_OPTION) {
                throw new UserCancelException();
            }
            files = chooser.getSelectedFiles();
        } while (files.length == 0);
        return files;
    }

    private static File getCurrentDirectory() {
        if (Boolean.getBoolean("netbeans.openfile.197063")) {
            // Prefer to open from parent of active editor, if any.
            final TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated != null && WindowManager.getDefault().isOpenedEditorTopComponent(activated)) {
                final DataObject d = activated.getLookup().lookup(DataObject.class);
                if (d != null) {
                    final File f = FileUtil.toFile(d.getPrimaryFile());
                    if (f != null) {
                        return f.getParentFile();
                    }
                }
            }
        }
        // Fall back to default location ($HOME or similar).
        if (currentDirectory == null || !currentDirectory.exists()) {
            currentDirectory = new File(System.getProperty("user.home"));
        }
        return currentDirectory;
    }

    @Override
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        SwingUtilities.invokeLater(() -> {
            if (running) {
                return;
            }
            try {
                running = true;
                final JFileChooser chooser = prepareFileChooser();
                final File[] files;
                try {
                    files = chooseFilesToOpen(chooser);
                    OpenFilePlugin.setCurrentDirectory(chooser.getCurrentDirectory());
                    currentDirectory = chooser.getCurrentDirectory();
                } catch (final UserCancelException ex) {
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    OpenFile.openFile(files[i], -1);
                }
            } finally {
                running = false;
            }
        });
    }
}
