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
package au.gov.asd.tac.constellation.testing.debug;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Help", id = "au.gov.asd.tac.constellation.testing.debug.MemoryAction")
@ActionRegistration(displayName = "#CTL_MemoryAction")
@ActionReference(path = "Menu/Help", position = 1451)
@Messages("CTL_MemoryAction=Memory")
public final class MemoryAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        final long freemem = Runtime.getRuntime().freeMemory();
        final long totalmem = Runtime.getRuntime().totalMemory();
        final long maxmem = Runtime.getRuntime().maxMemory();

        final StringBuilder b = new StringBuilder();
        b.append(String.format("Free memory: %,d%n", freemem));
        b.append(String.format("Total memory: %,d%n", totalmem));
        b.append(String.format("Maximum memory: %,d%n", maxmem));

//        // test
//        for (MemoryPoolMXBean memoryPoolMXBeans : ManagementFactory.getMemoryPoolMXBeans()) {
//            b.append(memoryPoolMXBeans.getName());
//            b.append(memoryPoolMXBeans.getUsage());
//            b.append("\n");
//        }
        final NotifyDescriptor nd = new NotifyDescriptor.Message(b.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }
}
