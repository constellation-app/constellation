/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.NBFilesystemTestAction")
@ActionRegistration(displayName = "#CTL_NBFilesystemTestAction")
@ActionReference(path = "Menu/Experimental/Developer", position = 0)
@Messages("CTL_NBFilesystemTestAction=Test NetBeans File System")
public final class NBFilesystemTestAction implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(NBFilesystemTestAction.class.getName());

    @Override
    public void actionPerformed(final ActionEvent e) {
        final FileObject root = FileUtil.getConfigRoot();
        for (final FileObject fo : root.getChildren()) {
            LOGGER.log(Level.INFO, "object: {0}", fo.getPath());
        }

        final FileObject toolbars = root.getFileObject("Toolbars");
        descend(toolbars, 0);

//        final FileObject misc = root.getFileObject("OptionsDialog/Advanced");
//        for(final FileObject fo : misc.getChildren())
//        {
//            System.out.printf("@@file object Advanced: %s\n", fo.getPath());
//        }
//        final String unwanted1 = "OptionsDialog/General.instance";
//        final String unwanted2 = "OptionsDialog/Advanced/org-netbeans-core-ui-options-filetypes-FileAssociationsOptionsPanelController.instance";
//        for(final String unwanted : new String[]{unwanted1, unwanted2})
//        {
//            final FileObject general = root.getFileObject(unwanted);
//            if(general!=null)
//            {
//                System.out.printf("@@file object delete %s\n", unwanted);
//                try
//                {
//                    general.delete();
//                }
//                catch(IOException ex)
//                {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//        }
    }

    private static void descend(final FileObject fo, final int level) {
        final String fmt = String.format("%%%ds", (level + 1) * 2);
        System.out.printf(fmt + " %s %s\n", " ", fo.getNameExt(), fo.getAttribute("position"));
        for (final FileObject child : fo.getChildren()) {
            descend(child, level + 1);
        }
    }
}
