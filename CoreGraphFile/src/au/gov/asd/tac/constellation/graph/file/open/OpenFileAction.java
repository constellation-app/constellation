/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package au.gov.asd.tac.constellation.graph.file.open;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;
import org.openide.util.UserCancelException;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action which allows user open file from disk. It is installed in Menu | File
 * | Open file... .
 *
 * @author Jesse Glick
 * @author Marian Petras
 */
@ActionRegistration(
        displayName = "#LBL_openFile",
        iconBase = "au/gov/asd/tac/constellation/graph/file/open/resources/openFile.png",
        iconInMenu = false)
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.graph.file.open.OpenFileAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 300),
    @ActionReference(path = "Shortcuts", name = "C-O")})
public class OpenFileAction implements ActionListener {

    private boolean running;

    private static final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    private static String lastFileOpenAndSaveLocation = prefs.get(ApplicationPreferenceKeys.FILE_OPEN_AND_SAVE_LOCATION, "");
    private static boolean rememberOpenAndSaveLocation = prefs.getBoolean(ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION, ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION_DEFAULT);

    private HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass().getName());
    }

    /**
     * Creates and initializes a file chooser.
     *
     * @return the initialized file chooser
     */
    protected JFileChooser prepareFileChooser() {
        final JFileChooser chooser = new FileChooser();
        chooser.setCurrentDirectory(getCurrentDirectory());
        HelpCtx.setHelpIDString(chooser, getHelpCtx().getHelpID());

        return chooser;
    }

    /**
     * When the preference "Remember Open/Save Location" is set, save the
     * Current Directory.
     *
     */
    private static void saveCurrentDirectory(final File currentDir) {
        lastFileOpenAndSaveLocation = prefs.get(ApplicationPreferenceKeys.FILE_OPEN_AND_SAVE_LOCATION, "");
        rememberOpenAndSaveLocation = prefs.getBoolean(ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION, ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION_DEFAULT);

        if (!lastFileOpenAndSaveLocation.equals(currentDir.getAbsolutePath()) && rememberOpenAndSaveLocation) {
            prefs.put(ApplicationPreferenceKeys.FILE_OPEN_AND_SAVE_LOCATION, currentDir.getAbsolutePath());
        }
    }

    /**
     * Displays the specified file chooser and returns a list of selected files.
     *
     * @param chooser file chooser to display
     * @return array of selected files,
     * @exception org.openide.util.UserCancelException if the user cancelled the
     * operation
     */
    public static File[] chooseFilesToOpen(final JFileChooser chooser) throws UserCancelException {
        File[] files;
        do {
            int selectedOption = chooser.showOpenDialog(
                    WindowManager.getDefault().getMainWindow());

            if (selectedOption != JFileChooser.APPROVE_OPTION) {
                throw new UserCancelException();
            }
            files = chooser.getSelectedFiles();
        } while (files.length == 0);
        return files;
    }

    /**
     * {@inheritDoc} Displays a file chooser dialog and opens the selected
     * files.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (running) {
            return;
        }
        try {
            running = true;
            final JFileChooser chooser = prepareFileChooser();
            File[] files;
            try {
                files = chooseFilesToOpen(chooser);
                saveCurrentDirectory(chooser.getCurrentDirectory());
            } catch (final UserCancelException ex) {
                return;
            }
            for (final File file : files) {
                OpenFile.openFile(file, -1);
            }
        } finally {
            running = false;
        }
    }

    /**
     * When the preference "Remember Open/Save Location" is set, get the
     * Directory saved in the preference, otherwise use the user's home
     * directory
     */
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

        lastFileOpenAndSaveLocation = prefs.get(ApplicationPreferenceKeys.FILE_OPEN_AND_SAVE_LOCATION, "");
        rememberOpenAndSaveLocation = prefs.getBoolean(ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION, ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION_DEFAULT);

        // Check prefferences for last saved directory
        return new File((lastFileOpenAndSaveLocation.isEmpty() || !rememberOpenAndSaveLocation) ? System.getProperty("user.home") : lastFileOpenAndSaveLocation);
    }
}
