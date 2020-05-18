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

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * This plugin uses the Control Backspace function so that it doesn't cause
 * problems if the shortcut is used in a text editor
 *
 * @author canis_majoris
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.NullAction")
@ActionRegistration(displayName = "#CTL_NullAction", surviveFocusChange = true)
@NbBundle.Messages("CTL_NullAction=Null Action")
@ActionReference(path = "Shortcuts", name = "C-Back_space")
public class NullAction extends SimplePluginAction {

    public NullAction(GraphNode context) {
        super(context, CoreTestingPluginRegistry.NULL_PLUGIN, false);
    }

}
