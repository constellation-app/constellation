/*
 * Copyright 2010-2024 Australian Signals Directorate
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
    }

    private static void descend(final FileObject fo, final int level) {
        final String fmt = String.format("%%%ds", (level + 1) * 2);
        final String log = String.format(fmt + " %s %s%n", " ", fo.getNameExt(), fo.getAttribute("position"));
        LOGGER.log(Level.INFO, log);
        for (final FileObject child : fo.getChildren()) {
            descend(child, level + 1);
        }
    }
}
