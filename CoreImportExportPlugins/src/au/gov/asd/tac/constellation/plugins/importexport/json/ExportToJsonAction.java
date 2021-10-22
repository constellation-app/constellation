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
package au.gov.asd.tac.constellation.plugins.importexport.json;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.plugins.importexport.image.ExportToImagePlugin;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * Export to JSON.
 */
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.plugins.importexport.json.ExportToJsonAction")
@ActionRegistration(displayName = "#CTL_ExportToJsonAction", iconBase = "au/gov/asd/tac/constellation/plugins/importexport/json/exportToJSON.png", surviveFocusChange = true)
@ActionReference(path = "Menu/File/Export", position = 100)
@Messages("CTL_ExportToJsonAction=To JSON...")
public final class ExportToJsonAction implements ActionListener {

    private static final Preferences PREFERENCES = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    private static final boolean REMEMBER_OPEN_AND_SAVE_LOCATION = PREFERENCES.getBoolean(ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION, ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION_DEFAULT);
    private static final File DEFAULT_DIRECTORY = new File(System.getProperty("user.home"));
    private static File savedDirectory = DEFAULT_DIRECTORY;

    private static final String TITLE = "Export to JSON";

    private static final String EXT = ".json";
    private final GraphNode context;

    public ExportToJsonAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final FileChooserBuilder fileChooser = getExportToImageFileChooser();

        FileChooser.openSaveDialog(fileChooser).thenAccept(optionalFile -> optionalFile.ifPresent(selectedFile -> {
            savedDirectory = REMEMBER_OPEN_AND_SAVE_LOCATION ? selectedFile : DEFAULT_DIRECTORY;

            String fileName = selectedFile.getAbsolutePath();

            if (!fileName.toLowerCase().endsWith(EXT)) {
                fileName += EXT;
            }

            PluginExecution
                    .withPlugin(ImportExportPluginRegistry.EXPORT_JSON)
                    .withParameter(ExportToImagePlugin.FILE_NAME_PARAMETER_ID, fileName)
                    .executeLater(context.getGraph());
        }));
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getExportToImageFileChooser() {
        return new FileChooserBuilder(TITLE)
                .setTitle(TITLE)
                .setDefaultWorkingDirectory(savedDirectory)
                .setFileFilter(new FileNameExtensionFilter("JSON files (.json)", "json"))
                .setAcceptAllFileFilterUsed(false)
                .setFilesOnly(true);
    }
}
