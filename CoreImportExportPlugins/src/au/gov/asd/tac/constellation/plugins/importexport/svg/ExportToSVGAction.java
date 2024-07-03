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
package au.gov.asd.tac.constellation.plugins.importexport.svg;

import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        
        //The Action must be able to interpret the active graph to prefill parameters
        final ReadableGraph graph = context.getGraph().getReadableGraph();
        
        //Get the attribute IDs
        final int graphFlagAttributeID = VisualConcept.GraphAttribute.DRAW_FLAGS.get(graph);
        final int graphBackgroundAttributeID = VisualConcept.GraphAttribute.BACKGROUND_COLOR.get(graph);

        //Retrive AtttrbuteValues
        final DrawFlags flags = graph.getObjectValue(graphFlagAttributeID, 0);
        final ConstellationColor color = graph.getObjectValue(graphBackgroundAttributeID, 0);
        final String graphName = GraphNode.getGraphNode(graph.getId()).getDisplayName();
        
        //The graph has no visual data on it so prevent the user from exporting
        if (graph.getVertexCount() < 1) {
            final String message = "Unable to export empty graph.";
            final Object[] options = new Object[]{NotifyDescriptor.OK_OPTION};
            final NotifyDescriptor d = new NotifyDescriptor(message, "Unable To Perform Action", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE, options, NotifyDescriptor.OK_OPTION);
            NotifyDisplayer.display(d);
        
        //The graph has visual data so export it
        } else {
            
            //This plugin is not performant so warn users when exporting large graphs. 
            final String disclaimer;
            if (graph.getVertexCount() + graph.getTransactionCount() > 5000){
                disclaimer = "This export plugin is processor and memory intensive and is likely cause Constellation and your machine to respond slowly. Please consider saving your graph before running this plugin.";     
            } else {
                disclaimer = null;
            }
            
            PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_SVG)
                    .withParameter(ExportToSVGPlugin.GRAPH_TITLE_PARAMETER_ID, graphName)
                    .withParameter(ExportToSVGPlugin.SELECTED_ELEMENTS_PARAMETER_ID, false)
                    .withParameter(ExportToSVGPlugin.EXPORT_CORES_PARAMETER_ID, (int) Math.ceil(Runtime.getRuntime().availableProcessors() / 2D))
                    .withParameter(ExportToSVGPlugin.SHOW_NODES_PARAMETER_ID, flags.drawNodes())
                    .withParameter(ExportToSVGPlugin.SHOW_CONNECTIONS_PARAMETER_ID, flags.drawConnections())
                    .withParameter(ExportToSVGPlugin.SHOW_NODE_LABELS_PARAMETER_ID, flags.drawNodeLabels())
                    .withParameter(ExportToSVGPlugin.SHOW_CONNECTION_LABELS_PARAMETER_ID, flags.drawConnectionLabels())
                    .withParameter(ExportToSVGPlugin.SHOW_BLAZES_PARAMETER_ID, flags.drawBlazes())
                    .withParameter(ExportToSVGPlugin.BACKGROUND_COLOR_PARAMETER_ID, color)
                    .interactively(true, disclaimer)
                    .executeLater(context.getGraph());
        }
        
        //Release the local lock on the graph
        graph.release();
    }    
}
