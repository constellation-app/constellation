/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.utilities;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Class responsible for zeroing the Z-Field.
 *
 * @author betelgeuse
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.utilities.FlattenZFieldAction")
@ActionRegistration(displayName = "#CTL_FlattenZFieldAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/utilities/resources/zflat.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Arrange", position = 2000, separatorBefore = 1999)
})
@Messages("CTL_FlattenZFieldAction=Flatten Z-Field")
public final class FlattenZFieldAction extends AbstractAction {

    private final GraphNode context;

    public FlattenZFieldAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(ArrangementPluginRegistry.FLATTEN_Z).executeLater(context.getGraph());
    }
}
