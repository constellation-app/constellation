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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Experimental",
        id = "au.gov.asd.tac.constellation.testing.ExportGlyphTexturesAction"
)
@ActionRegistration(
        displayName = "#CTL_ExportGlyphTexturesAction"
)
@ActionReference(path = "Menu/Experimental/Developer", position = 0)
@Messages("CTL_ExportGlyphTexturesAction=Export Glyph Textures")
public final class ExportGlyphTexturesAction implements ActionListener {

    private static final String TITLE = "Export Glyph Textures";

    @Override
    public void actionPerformed(final ActionEvent e) {
        FileChooser.openSaveDialog(getExportGlyphTexturesFileChooser())
                .thenAccept(optionalFile -> optionalFile.ifPresent(file -> SharedDrawable.exportGlyphTextures(file)));
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getExportGlyphTexturesFileChooser() {
        return FileChooser.createFileChooserBuilder(TITLE, FileExtensionConstants.PNG, "Image Files (" + FileExtensionConstants.PNG + ")", true);
    }
}
