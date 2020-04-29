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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity;

import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Levenshtein Distance action.
 *
 * @author canis_majoris
 */
@ActionID(category = "Similarity", id = "au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity.LevenshteinDistanceAction")
@ActionRegistration(displayName = "#CTL_LevenshteinDistanceAction")
@NbBundle.Messages("CTL_LevenshteinDistanceAction=Levenshtein Distance")
public class LevenshteinDistanceAction implements ActionListener {

    private final GraphNode context;

    public LevenshteinDistanceAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        PluginExecution.withPlugin(AlgorithmPluginRegistry.LEVENSHTEIN_DISTANCE)
                .withParameter(LevenshteinDistancePlugin.ATTRIBUTE_PARAMETER_ID, VisualConcept.VertexAttribute.IDENTIFIER.getName())
                .withParameter(LevenshteinDistancePlugin.MAXIMUM_DISTANCE_PARAMETER_ID, 1)
                .withParameter(LevenshteinDistancePlugin.CASE_INSENSITIVE_PARAMETER_ID, false)
                .withParameter(LevenshteinDistancePlugin.SELECTED_ONLY_PARAMETER_ID, false)
                .executeLater(context.getGraph());
    }
}
