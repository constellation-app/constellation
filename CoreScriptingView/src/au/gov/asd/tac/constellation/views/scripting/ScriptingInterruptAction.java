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
package au.gov.asd.tac.constellation.views.scripting;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;

/**
 * Interrupt a script.
 *
 * @author algol
 * @author cygnus_x-1
 */
public final class ScriptingInterruptAction extends AbstractAction {

    @StaticResource
    private static final String INTERUPT_ICON = "au/gov/asd/tac/constellation/views/scripting/interrupt.png";
    private static final ImageIcon INTERRUPTED_ICON = ImageUtilities.loadImageIcon(INTERUPT_ICON, false);

    private Thread thread;

    public ScriptingInterruptAction() {
        putValue(NAME, "Interrupt Script");
        putValue(SMALL_ICON, INTERRUPTED_ICON);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void setThread(final Thread thread) {
        this.thread = thread;
    }
}
