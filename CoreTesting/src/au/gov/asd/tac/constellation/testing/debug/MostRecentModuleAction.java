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
package au.gov.asd.tac.constellation.testing.debug;

import au.gov.asd.tac.constellation.functionality.startup.MostRecentModules;
import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.modules.ModuleInfo;
import org.openide.util.NbBundle.Messages;

/**
 * Display the most recent module to the user.
 * <p>
 * This tells the user how recently CONSTELLATION has been updated.
 *
 * @author algol
 */
@ActionID(category = "Help", id = "au.gov.asd.tac.constellation.testing.debug.MostRecentModuleAction")
@ActionRegistration(displayName = "#CTL_MostRecentModuleAction",
        iconBase = "au/gov/asd/tac/constellation/testing/debug/versions.png")
@ActionReference(path = "Menu/Help", position = 1375)
@Messages("CTL_MostRecentModuleAction=Module Versions")
public final class MostRecentModuleAction implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
        final List<ModuleInfo> moduleList = MostRecentModules.getModules();

        final StringBuilder sb = new StringBuilder();
        moduleList.stream().forEach(mi -> sb.append(String.format("%-40s %20s\n", mi.getDisplayName(), mi.getSpecificationVersion())));

        final InfoTextPanel itp = new InfoTextPanel(sb.toString());
        final NotifyDescriptor.Message msg = new NotifyDescriptor.Message(itp);
        msg.setTitle(Bundle.CTL_MostRecentModuleAction());
        DialogDisplayer.getDefault().notify(msg);
    }
}
