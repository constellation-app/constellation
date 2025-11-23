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
package au.gov.asd.tac.constellation.plugins.arrangements.datetime;

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Quasar985
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.datetime.ArrangeByDateTimeAction")
@ActionRegistration(displayName = "#CTL_ArrangeByDateTimeAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/datetime/resources/arrangeByDateTime.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Arrange", position = 500), //@ActionReference(path = "Shortcuts", name = "C-H")
})
@NbBundle.Messages("CTL_ArrangeByDateTimeAction=DateTime")
public class ArrangeByDateTimeAction extends AbstractAction {

   private final GraphNode context;

    public ArrangeByDateTimeAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecutor.startWith(ArrangementPluginRegistry.DATETIME)
                .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                .executeWriteLater(context.getGraph(), Bundle.CTL_ArrangeByDateTimeAction());
    }
}
