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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Wrapper around PinVertexPositionsPlugin which is responsible for setting
 * selected vertexes PINNED attributes to true to ensure the vertexes are not
 * moved when an arrangement plugin is run.
 *
 * @author serpens24
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.grid.PinVertexPositionsAction")
@ActionRegistration(displayName = "#CTL_PinVertexPositionsAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/resources/pin.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Arrange", position = 9998),
    @ActionReference(path = "Toolbars/Arrange", position = 9998)})
@NbBundle.Messages("CTL_PinVertexPositionsAction=Pin Vertex Positions")
public final class PinVertexPositionsAction extends AbstractAction {

    private final GraphNode context;

    /**
     * Construct a new instance.
     *
     * @param context GraphNode context.
     */
    public PinVertexPositionsAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecutor.startWith(ArrangementPluginRegistry.PIN)
                .executeWriteLater(context.getGraph());
    }
}
