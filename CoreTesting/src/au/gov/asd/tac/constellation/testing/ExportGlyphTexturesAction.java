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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

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

    private static final Frame window = WindowManager.getDefault().getMainWindow();
    private static final Preferences preferences = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    private static final boolean REMEMBER_OPEN_AND_SAVE_LOCATION = preferences.getBoolean(ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION, ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION_DEFAULT);

    private final File DEFAULT_DIRECTORY = new File(System.getProperty("user.home"));
    private File SAVED_DIRECTORY = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Glyph Textures");
        fileChooser.setCurrentDirectory(SAVED_DIRECTORY != null ? SAVED_DIRECTORY : DEFAULT_DIRECTORY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        if (fileChooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();

            if (file != null) {
                SAVED_DIRECTORY = REMEMBER_OPEN_AND_SAVE_LOCATION ? fileChooser.getCurrentDirectory() : DEFAULT_DIRECTORY;
                SharedDrawable.exportGlyphTextures(file);
            }
        }
    }
}
