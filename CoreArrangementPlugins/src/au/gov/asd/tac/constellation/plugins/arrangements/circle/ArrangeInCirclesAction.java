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
package au.gov.asd.tac.constellation.plugins.arrangements.circle;

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
import org.openide.util.NbBundle.Messages;

/**
 * Arrange in a circle with pendants in a circle around their single neighbour.
 *
 * @author algol
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.circle.ArrangeInCirclesAction")
@ActionRegistration(displayName = "#CTL_ArrangeInCirclesAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Arrangements", position = 0)
})
@Messages("CTL_ArrangeInCirclesAction=Circles")
public final class ArrangeInCirclesAction extends AbstractAction {

    private final GraphNode context;

    public ArrangeInCirclesAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecutor.startWith(ArrangementPluginRegistry.CIRCLES)
                .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                .executeWriteLater(context.getGraph(), Bundle.CTL_ArrangeInCirclesAction());
    }
}
