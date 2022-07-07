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
package au.gov.asd.tac.constellation.utilities.support;

import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Help",
        id = "au.gov.asd.tac.constellation.utilities.support.SupportPackageAction"
)
@ActionRegistration(
        displayName = "#CTL_SupportPackageAction",
        iconBase = "au/gov/asd/tac/constellation/utilities/support/supportPackage.png"
)
@ActionReference(path = "Menu/Help", position = 920)
@Messages({
    "CTL_SupportPackageAction=Support Package",
    "MSG_SaveAsTitle=Select Folder"
})
public final class SupportPackageAction implements ActionListener {

    private static final String TITLE = "Select Folder";

    @Override
    public void actionPerformed(ActionEvent e) {
        FileChooser.openSaveDialog(getSupportPackageFileChooser()).thenAccept(optionalFolder -> optionalFolder.ifPresent(folder -> {
            final Date now = new Date();
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            final String username = StringUtilities.removeSpecialCharacters(System.getProperty("user.name"));

            final File destination = new File(folder.getPath(), String.format("%s-%s-%s.zip", "SupportPackage", username, simpleDateFormat.format(now)));
            final SupportPackage supportPackage = new SupportPackage();
            final Thread supportPackageThread = new Thread(() -> {
                try {
                    supportPackage.createSupportPackage(new File(SupportPackage.getUserLogDirectory()), destination);
                    NotifyDisplayer.display("Support package saved successfully to " + destination.getPath(), NotifyDescriptor.INFORMATION_MESSAGE);
                } catch (IOException ex) {
//                    NotifyDisplayer.display("Failed to save support package. The error was " + ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                }
            });
            supportPackageThread.setName("Support Package Thread");
            supportPackageThread.start();
        }));
    }

    /**
     * Creates a new file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getSupportPackageFileChooser() {
        return new FileChooserBuilder(TITLE)
                .setTitle(TITLE)
                .setAcceptAllFileFilterUsed(false)
                .setDirectoriesOnly(true);
    }
}
