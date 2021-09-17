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
package au.gov.asd.tac.constellation.views.tableview2.components;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileChooserBuilder;

/**
 * Creates file chooser that can be used to shown to a user allowing them to
 * select a file when the select a table export option.
 *
 * @author formalhaunt
 */
public class ExportFileChooser {

    private final FileChooserBuilder fileChooser;

    private final String expectedFileExtension;

    /**
     * Creates a new export file chooser.
     *
     * @param fileChooserTitle the title that will be on the export file chooser
     * dialog
     * @param expectedFileExtension the file extension the file chooser will
     * save
     * @param fileChooserDescription the description that will be on the export
     * file chooser dialog
     */
    public ExportFileChooser(final String fileChooserTitle,
            final String expectedFileExtension,
            final String fileChooserDescription) {
        this.expectedFileExtension = expectedFileExtension;

        fileChooser = new FileChooserBuilder(fileChooserTitle)
                .setTitle(fileChooserTitle)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File file) {
                        final String name = file.getName();
                        // if it is an actual file and it ends with the expected extension
                        return file.isFile() && name.toLowerCase()
                                .endsWith(expectedFileExtension.toLowerCase());
                    }

                    @Override
                    public String getDescription() {
                        return fileChooserDescription;
                    }
                });
    }

    /**
     * Opens the export file chooser and returns the selected file. Attempts to
     * correct any simple mistakes in the selected file like missing extensions.
     *
     * @return the selected file
     */
    public File openExportFileChooser() {
        final File selectedFile = getFileChooser().showSaveDialog();

        if (selectedFile != null && selectedFile.getAbsolutePath() != null) {
            // If somehow a file was selected that does not end in the required
            // extension, add it
            final String cleanedFilePath = selectedFile.getAbsolutePath().toLowerCase()
                    .endsWith(expectedFileExtension)
                    ? selectedFile.getAbsolutePath()
                    : selectedFile.getAbsolutePath() + expectedFileExtension;

            return new File(cleanedFilePath);
        }
        return selectedFile;
    }

    /**
     * Gets the export file chooser.
     *
     * @return the export file chooser
     */
    public FileChooserBuilder getFileChooser() {
        return fileChooser;
    }
}
