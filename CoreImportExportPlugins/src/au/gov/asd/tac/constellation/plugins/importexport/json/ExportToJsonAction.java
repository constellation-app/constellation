/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "File", id = "au.gov.asd.tac.constellation.plugins.importexport.json.ExportToJsonAction")
@ActionRegistration(displayName = "#CTL_ExportToJsonAction", surviveFocusChange = true)
@ActionReference(path = "Menu/File/Export", position = 100)
@Messages("CTL_ExportToJsonAction=To JSON...")
public final class ExportToJsonAction implements ActionListener {

    private static final String EXT = ".json";

    final GraphNode context;

    public ExportToJsonAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final FileChooserBuilder fChooser = new FileChooserBuilder("ExportJson")
                .setTitle("Export to JSON")
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File pathName) {
                        final String name = pathName.getName().toLowerCase();
                        if (pathName.isFile() && name.toLowerCase().endsWith(EXT)) {
                            return true;
                        }

                        return pathName.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "JSON file";
                    }
                });

        final File file = fChooser.showSaveDialog();
        if (file != null) {
            String fnam = file.getAbsolutePath();
            if (!fnam.toLowerCase().endsWith(EXT)) {
                fnam += EXT;
            }

            PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_JSON)
                    .withParameter(ExportToJsonPlugin.FILE_NAME_PARAMETER_ID, fnam)
                    .executeLater(context.getGraph());
        }
    }
}
