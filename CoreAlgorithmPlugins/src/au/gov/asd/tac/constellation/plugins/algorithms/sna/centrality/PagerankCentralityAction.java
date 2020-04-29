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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality;

import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Pagerank centrality action.
 *
 * @author cygnus_x-1
 */
@ActionID(category = "Centrality", id = "au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.PagerankCentralityAction")
@ActionRegistration(displayName = "#CTL_PagerankCentralityAction")
@NbBundle.Messages("CTL_PagerankCentralityAction=Pagerank Centrality")
public class PagerankCentralityAction implements ActionListener {

    private final GraphNode context;

    public PagerankCentralityAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        PluginExecution.withPlugin(AlgorithmPluginRegistry.PAGERANK_CENTRALITY)
                .withParameter(PagerankCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL_PARAMETER_ID, true)
                .withParameter(PagerankCentralityPlugin.DAMPING_FACTOR_PARAMETER_ID, 0.85f)
                .withParameter(PagerankCentralityPlugin.ITERATIONS_PARAMETER_ID, 100)
                .withParameter(PagerankCentralityPlugin.EPSILON_PARAMETER_ID, 1E-8f)
                .withParameter(PagerankCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false)
                .executeLater(context.getGraph());
    }
}
