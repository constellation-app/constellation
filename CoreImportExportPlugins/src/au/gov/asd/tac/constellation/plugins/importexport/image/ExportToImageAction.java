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
package au.gov.asd.tac.constellation.plugins.importexport.image;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.plugins.importexport.json.ExportToJsonPlugin;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle.Messages;

/**
 * Export current graph as an image.
 */
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.plugins.importexport.image.ExportToImage")
@ActionRegistration(displayName = "#CTL_ExportToImage", iconBase = "au/gov/asd/tac/constellation/plugins/importexport/image/exportToImage.png", surviveFocusChange = true)
@ActionReference(path = "Menu/File/Export", position = 0)
@Messages("CTL_ExportToImage=To Screenshot Image...")
public final class ExportToImageAction implements ActionListener {

    private static File savedDirectory = FileChooser.DEFAULT_DIRECTORY;

    private static final String TITLE = "Export to Image";

    private static final String EXT = ".png";
    private final GraphNode context;

    public ExportToImageAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final FileChooserBuilder fileChooser = getExportToImageFileChooser();

        FileChooser.openSaveDialog(fileChooser).thenAccept(optionalFile -> optionalFile.ifPresent(selectedFile -> {
            savedDirectory = FileChooser.REMEMBER_OPEN_AND_SAVE_LOCATION ? selectedFile : FileChooser.DEFAULT_DIRECTORY;

            String fileName = selectedFile.getAbsolutePath();

            selectedFile.renameTo(new File("potato.png"));

            if (!fileName.toLowerCase().endsWith(EXT)) {
                fileName += EXT;
            }

            PluginExecution
                    .withPlugin(ImportExportPluginRegistry.EXPORT_IMAGE)
                    .withParameter(ExportToJsonPlugin.FILE_NAME_PARAMETER_ID, fileName)
                    .executeLater(context.getGraph());
        }));
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getExportToImageFileChooser() {
        return FileChooser.getBaseFileChooserBuilder(TITLE, savedDirectory, FileChooser.PNG_FILE_FILTER)
                .setAcceptAllFileFilterUsed(false)
                .setFilesOnly(true);
    }
}
