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
package au.gov.asd.tac.constellation.utilities.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFileChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Help",
        id = "au.gov.asd.tac.constellation.utilities.support.SupportPackageAction"
)
@ActionRegistration(
        displayName = "#CTL_SupportPackageAction"
)
@ActionReference(path = "Menu/Help", position = 920)
@Messages({
    "CTL_SupportPackageAction=Support Package",
    "MSG_SaveAsTitle=Select Folder"
})
public final class SupportPackageAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        final File saveAsDirectory = getSaveAsDirectory();
        if (saveAsDirectory != null) {
            final Date now = new Date();
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            final String username = System.getenv("username");

            final File destination = new File(saveAsDirectory.getPath(), String.format("%s-%s-%s.zip", "SupportPackage", username, simpleDateFormat.format(now)));
            final SupportPackage supportPackage = new SupportPackage();
            final Thread supportPackageThread = new Thread(() -> {
                try {
                    supportPackage.createSupportPackage(new File(SupportPackage.getUserLogDirectory()), destination);
                    final NotifyDescriptor nd = new NotifyDescriptor.Message("Support package saved successfully to " + destination.getPath(), NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                } catch (IOException ex) {
                    final NotifyDescriptor nd = new NotifyDescriptor.Message("Failed to save support package. The error was " + ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            });
            supportPackageThread.setName("Support Package Thread");
            supportPackageThread.start();
        }
    }

    /**
     * Show file "Save As" dialog
     *
     * @return File selected by the user or null if no file was selected.
     */
    private File getSaveAsDirectory() {

        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(Bundle.MSG_SaveAsTitle());
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(WindowManager.getDefault().getMainWindow())) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }
}
