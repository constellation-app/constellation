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
package au.gov.asd.tac.constellation.plugins.algorithms.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Spanning Tree Action
 *
 * @author algol
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.plugins.algorithms.tree.SpanningTreeAction")
@ActionRegistration(displayName = "#CTL_SpanningTreeAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Tools", position = 0)
})
@NbBundle.Messages("CTL_SpanningTreeAction=Spanning Tree")
public class SpanningTreeAction extends AbstractAction {

    private final GraphNode context;

    public SpanningTreeAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

        // Note: only works on one component.
        final Graph graph = context.getGraph();
        PluginExecution.withPlugin(AlgorithmPluginRegistry.SPANNING_TREE).executeLater(graph);
    }
}
