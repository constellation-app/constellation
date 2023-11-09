/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.svg;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ImageConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Action responsible for triggering {@link ExportToSVGPlugin} that exports Graphs to SVG files.
 * 
 * @author capricornunicorn123
 */
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.plugins.importexport.image.ExportToSVG")
@ActionRegistration(displayName = "#CTL_ExportToSVG",
        iconBase = "au/gov/asd/tac/constellation/plugins/importexport/svg/exportToSVG.png",
        surviveFocusChange = true)
@ActionReference(path = "Menu/File/Export", position = 50)
@NbBundle.Messages("CTL_ExportToSVG=To SVG")
public final class ExportToSVGAction implements ActionListener {

    private final GraphNode context; 
    
    public ExportToSVGAction(final GraphNode context) {
        this.context = context;
    }
   
    @Override
    public void actionPerformed(final ActionEvent e) {
        
        //The Action must be bale to interpret the active graph 
        final ReadableGraph graph = context.getGraph().getReadableGraph();
        
        //The graph has data on it so it can be exported
        if (graph.getVertexCount() > 0) {        
                int colorAttributeID = VisualConcept.GraphAttribute.BACKGROUND_COLOR.get(graph);
                ConstellationColor color = graph.getObjectValue(colorAttributeID, 0);

                PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_SVG)
                        .withParameter(ExportToSVGPlugin.GRAPH_TITLE_PARAMETER_ID, GraphNode.getGraphNode(graph.getId()).getDisplayName())
                        .withParameter(ExportToSVGPlugin.SELECTED_NODES_PARAMETER_ID, false)
                        .withParameter(ExportToSVGPlugin.SHOW_CONNECTIONS_PARAMETER_ID, true)
                        .withParameter(ExportToSVGPlugin.SHOW_TOP_LABELS_PARAMETER_ID, true)
                        .withParameter(ExportToSVGPlugin.SHOW_BOTTOM_LABELS_PARAMETER_ID, true)
                        .withParameter(ExportToSVGPlugin.BACKGROUND_COLOR_PARAMETER_ID, color)
                        .withParameter(ExportToSVGPlugin.EXPORT_PERSPECTIVE_PARAMETER_ID, "Current Perspective")
                        .interactively(true)
                        .executeLater(context.getGraph());
        
        //The graph has no data on it so prevent the user from exporting
        } else {
            final String message = "Unable to export empty graph.";
            final Object[] options = new Object[]{NotifyDescriptor.OK_OPTION};
            final NotifyDescriptor d = new NotifyDescriptor(message, "Unable To Perform Action", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE, options, NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(d);
        }
        graph.release();
    }    
}
