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

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Deliberately throw a PluginException.
 * <p>
 * This exists to exercise the error handling capability.
 *
 * @author algol
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.PluginExceptionAction")
@ActionRegistration(displayName = "#CTL_PluginExceptionAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Developer", position = 0)
})
@Messages("CTL_PluginExceptionAction=Throw a PluginException")
public final class PluginExceptionAction implements ActionListener {

    private final GraphNode context;

    public PluginExceptionAction(GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(PluginRegistry.get(CoreTestingPluginRegistry.PLUGIN_EXCEPTION)).executeLater(context.getGraph());
    }
}
