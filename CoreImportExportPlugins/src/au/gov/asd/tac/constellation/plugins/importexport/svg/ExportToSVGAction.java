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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileChooserBuilder.SelectionApprover;
import org.openide.util.NbBundle;

/**
 * Action responsible for triggering <code>ExportToSVGPlugin</code> that exports Graphs to SVG files.
 * 
 * @author capricornunicorn123
 */
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.plugins.importexport.image.ExportToSVG")
@ActionRegistration(displayName = "#CTL_ExportToSVG",
        iconBase = "au/gov/asd/tac/constellation/plugins/importexport/image/exportToImage.png", // 
        surviveFocusChange = true)
@ActionReference(path = "Menu/File/Export", position = 50)
@NbBundle.Messages("CTL_ExportToSVG=To SVG")
public final class ExportToSVGAction implements ActionListener {

    private static final String TITLE = "Export to SVG";
    
    private final GraphNode context; 
    
    public ExportToSVGAction(final GraphNode context) {
        this.context = context;
    }
   
    @Override
    public void actionPerformed(final ActionEvent e) {
        final ReadableGraph graph = context.getGraph().getReadableGraph();
        
        //The graph has data on it so it can be exported
        if (graph.getVertexCount() > 0){        
            FileChooser.openSaveDialog(getExportToSVGFileChooser()).thenAccept(optionalFile -> optionalFile.ifPresent(file -> {
                String fnam = file.getAbsolutePath();

                if (!fnam.toLowerCase().endsWith(FileExtensionConstants.SVG)) {
                    fnam += FileExtensionConstants.SVG;
                }
                int colorAttributeID = VisualConcept.GraphAttribute.BACKGROUND_COLOR.get(graph);
                ConstellationColor color = graph.getObjectValue(colorAttributeID, 0);
                
                PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_SVG)
                        .withParameter(ExportToSVGPlugin.FILE_NAME_PARAMETER_ID, fnam)
                        .withParameter(ExportToSVGPlugin.GRAPH_TITLE_PARAMETER_ID, "Milestone 5")
                        .withParameter(ExportToSVGPlugin.SELECTED_NODES_PARAMETER_ID, false)
                        .withParameter(ExportToSVGPlugin.SHOW_CONNECTIONS_PARAMETER_ID, true)
                        .withParameter(ExportToSVGPlugin.SHOW_TOP_LABELS_PARAMETER_ID, true)
                        .withParameter(ExportToSVGPlugin.SHOW_BOTTOM_LABELS_PARAMETER_ID, true)
                        .withParameter(ExportToSVGPlugin.BACKGROUND_COLOR_PARAMETER_ID, color)
                        .interactively(true)
                        .executeLater(context.getGraph());
            }));
        
        //The graph has no data on it so prevent the user from exporting
        } else {
            final String message = "Unable to export empty graph.";
            final Object[] options = new Object[]{NotifyDescriptor.OK_OPTION};
            final NotifyDescriptor d = new NotifyDescriptor(message, "Unable To Perform Action", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE, options, NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(d);
        }
        graph.release();
    }
    
    /**
     * Creates a new SVG file chooser.
     *
     * @return the created file chooser.
     */
    public FileChooserBuilder getExportToSVGFileChooser() {
        return new FileChooserBuilder(TITLE)
                .setTitle(TITLE)
                .setAcceptAllFileFilterUsed(false)
                .setFilesOnly(true)
                .setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(final File file) {
                        final String name = file.getName();
                        return (file.isFile() && StringUtils.endsWithIgnoreCase(name, FileExtensionConstants.SVG)) || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "SVG Files (" + FileExtensionConstants.SVG + ")";
                    }
                })
                .setSelectionApprover(new SelectionApprover() {
                    @Override
                    public boolean approve(final File[] files) {

                        for (final File file : files){
                            if (file.isFile()){
                                
                                final String message = String.format("%s already exists.%nDo you want to replace it?", file.getName());
                                final Object[] options = new Object[]{
                                    NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION
                                };
                                final NotifyDescriptor d = new NotifyDescriptor(message, "Confirm Save", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, options, "Save");
                                final Object o = DialogDisplayer.getDefault().notify(d);

                                if (NotifyDescriptor.NO_OPTION.equals(o)) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                });
    }
    
}
