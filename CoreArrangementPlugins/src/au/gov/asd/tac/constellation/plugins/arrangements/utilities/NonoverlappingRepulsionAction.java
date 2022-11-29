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
package au.gov.asd.tac.constellation.plugins.arrangements.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingWorker;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * action framework supporting the repulsion action
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.utilities.NonoverlappingRepulsionAction")
@ActionRegistration(iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/utilities/resources/repulsion.png", displayName = "#CTL_NonoverlappingRepulsionAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Arrangements", position = 0)
})
@Messages("CTL_NonoverlappingRepulsionAction=Nonoverlapping Repulse")
public final class NonoverlappingRepulsionAction implements ActionListener {

    private final GraphNode context;

    public NonoverlappingRepulsionAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final Graph graph = context.getGraph();
        final Worker worker = new Worker(graph);
        worker.execute();
    }

    private static class Worker extends SwingWorker<Object, Object> {

        private final Graph graph;

        /**
         * Construct a new Worker.
         *
         * @param graph A Graph.
         */
        private Worker(final Graph graph) {
            this.graph = graph;
        }

        @Override
        protected Object doInBackground() throws Exception {
            final NonoverlappingRepulsionArranger arrangement = new NonoverlappingRepulsionArranger();
            final WritableGraph wg = graph.getWritableGraph("Nonoverlapping repulsion", true);
            try {
                arrangement.arrange(wg);
            } finally {
                wg.commit();
            }

            return null;
        }

    }
}
