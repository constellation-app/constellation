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

@ActionID(category = "File", id = "au.gov.asd.tac.constellation.plugins.importexport.json.ExportToJsonAction")
@ActionRegistration(displayName = "#CTL_ExportToJsonAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/importexport/json/exportToJSON.png",
        surviveFocusChange = true)
@ActionReference(path = "Menu/File/Export", position = 100)
@Messages("CTL_ExportToJsonAction=To JSON...")
public final class ExportToJsonAction implements ActionListener {

    private static final String TITLE = "Export To JSON";
    private final GraphNode context;

    public ExportToJsonAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        FileChooser.openSaveDialog(getExportToJSONFileChooser()).thenAccept(optionalFile -> optionalFile.ifPresent(file -> {
            String fnam = file.getAbsolutePath();

            if (!StringUtils.endsWithIgnoreCase(fnam, FileExtensionConstants.JSON)) {
                fnam += FileExtensionConstants.JSON;
            }

            PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_JSON)
                    .withParameter(ExportToJsonPlugin.FILE_NAME_PARAMETER_ID, fnam)
                    .executeLater(context.getGraph());
        }));
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getExportToJSONFileChooser() {
        return new FileChooserBuilder(TITLE)
                .setTitle(TITLE)
                .setAcceptAllFileFilterUsed(false)
                .setFilesOnly(true)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File file) {
                        final String name = file.getName();
                        return (file.isFile() && StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.JSON)) || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "JSON Files (" + FileExtensionConstants.JSON + ")";
                    }
                });
    }
}
