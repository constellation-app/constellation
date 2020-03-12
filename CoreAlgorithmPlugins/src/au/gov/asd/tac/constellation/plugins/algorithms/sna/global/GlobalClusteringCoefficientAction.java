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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.global;

import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Global clustering coefficient action.
 *
 * @author canis_majoris
 */
@ActionID(category = "Global", id = "au.gov.asd.tac.constellation.plugins.algorithms.sna.global.GlobalClusteringCoefficientAction")
@ActionRegistration(displayName = "#CTL_GlobalClusteringCoefficientAction")
@NbBundle.Messages("CTL_GlobalClusteringCoefficientAction=Global Clustering Coefficient")
public class GlobalClusteringCoefficientAction implements ActionListener {

    private final GraphNode context;

    public GlobalClusteringCoefficientAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        PluginExecution.withPlugin(AlgorithmPluginRegistry.GLOBAL_CLUSTERING_COEFFICIENT).executeLater(context.getGraph());
    }
}
