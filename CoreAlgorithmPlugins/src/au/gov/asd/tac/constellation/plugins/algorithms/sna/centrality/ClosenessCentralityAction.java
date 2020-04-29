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
 * Closeness centrality action.
 *
 * @author cygnus_x-1
 */
@ActionID(category = "Centrality", id = "au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.ClosenessCentralityAction")
@ActionRegistration(displayName = "#CTL_ClosenessCentralityAction")
@NbBundle.Messages("CTL_ClosenessCentralityAction=Closeness Centrality")
public class ClosenessCentralityAction implements ActionListener {

    private final GraphNode context;

    public ClosenessCentralityAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        PluginExecution.withPlugin(AlgorithmPluginRegistry.CLOSENESS_CENTRALITY)
                .withParameter(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID, false)
                .withParameter(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID, true)
                .withParameter(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID, true)
                .withParameter(ClosenessCentralityPlugin.TREAT_UNDIRECTED_BIDIRECTIONAL, true)
                .withParameter(ClosenessCentralityPlugin.NORMALISE_POSSIBLE_PARAMETER_ID, true)
                .withParameter(ClosenessCentralityPlugin.NORMALISE_AVAILABLE_PARAMETER_ID, false)
                .withParameter(ClosenessCentralityPlugin.NORMALISE_CONNECTED_COMPONENTS_PARAMETER_ID, false)
                .withParameter(ClosenessCentralityPlugin.SELECTED_ONLY_PARAMETER_ID, false)
                .executeLater(context.getGraph());
    }
}
