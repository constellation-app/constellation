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
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle.Messages;

/*
 * export current graph as an image
 */
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.plugins.importexport.image.ExportToImage")
@ActionRegistration(displayName = "#CTL_ExportToImage",
        iconBase = "au/gov/asd/tac/constellation/plugins/importexport/image/exportToImage.png",
        surviveFocusChange = true)
@ActionReference(path = "Menu/File/Export", position = 0)
@Messages("CTL_ExportToImage=To Screenshot Image...")
public final class ExportToImageAction implements ActionListener {

    private static final String TITLE = "Export To Image";
    private final GraphNode context;

    public ExportToImageAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        FileChooser.openSaveDialog(getExportToImageFileChooser()).thenAccept(optionalFile -> optionalFile.ifPresent(file -> {
            String fnam = file.getAbsolutePath();

            if (!fnam.toLowerCase().endsWith(FileExtensionConstants.PNG)) {
                fnam += FileExtensionConstants.PNG;
            }

            PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_IMAGE)
                    .withParameter(ExportToImagePlugin.FILE_NAME_PARAMETER_ID, fnam)
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
                .setAcceptAllFileFilterUsed(false)
                .setFilesOnly(true)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File file) {
                        final String name = file.getName();
                        return (file.isFile() && StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.PNG)) || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Image Files (" + FileExtensionConstants.PNG + ")";
                    }
                });
    }
}
