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

import au.gov.asd.tac.constellation.utilities.svg.SVGData;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Exports visual data viewed on an active graph to an Scalar Vector Graphic file.
 * SVG generating functionality of this plugin has been abstracted to the {@link SVGGraphBuilder} class.
 * This plugin functionality relies heavily on {@link VisualGraphAccess} methods to interpret the graph consistently.
 * 
 * @author capricornunicorn123
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
@NbBundle.Messages("ExportToSVGPlugin=Export to SVG")
public class ExportToSVGPlugin extends SimpleReadPlugin {
    public static final String FILE_NAME_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "file_name");
    public static final String GRAPH_TITLE_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "graph_title");
    public static final String BACKGROUND_COLOR_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "background_color");
    public static final String SELECTED_NODES_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "selected_nodes");
    public static final String SHOW_CONNECTIONS_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "show_connections");
    public static final String CONNECTION_MODE_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "connection_mode");
    public static final String SHOW_NODE_LABELS_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "show_node_labels");
    public static final String SHOW_CONNECTION_LABELS_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "show_transaction_labels");
    public static final String EXPORT_PERSPECTIVE_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "export_perspective");
    private static final Logger LOGGER = Logger.getLogger(ExportToSVGPlugin.class.getName());
    
    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FileParameterValue> fnamParam = FileParameterType.build(FILE_NAME_PARAMETER_ID);
        fnamParam.setName("File Location");
        fnamParam.setDescription("File location and name for export");
        FileParameterType.setKind(fnamParam, FileParameterType.FileParameterKind.SAVE);
        FileParameterType.setFileFilters(fnamParam, new FileChooser.ExtensionFilter("SVG file", "*" + FileExtensionConstants.SVG));
        fnamParam.setRequired(true);
        parameters.addParameter(fnamParam);
        
        final PluginParameter<StringParameterValue> graphTitleParam = StringParameterType.build(GRAPH_TITLE_PARAMETER_ID);
        graphTitleParam.setName("Graph Title");
        graphTitleParam.setDescription("Title of the graph");
        graphTitleParam.setRequired(true);
        parameters.addParameter(graphTitleParam);
        
        final PluginParameter<ColorParameterValue> backgroundColorParam = ColorParameterType.build(BACKGROUND_COLOR_PARAMETER_ID);
        backgroundColorParam.setName("Background Color");
        backgroundColorParam.setDescription("Set the background color");
        parameters.addParameter(backgroundColorParam);
        
        final PluginParameter<BooleanParameterValue> selectedNodesParam = BooleanParameterType.build(SELECTED_NODES_PARAMETER_ID);
        selectedNodesParam.setName("Selected Nodes");
        selectedNodesParam.setDescription("Export selected nodes only");
        parameters.addParameter(selectedNodesParam);
        
        final PluginParameter<BooleanParameterValue> showConnectionsParam = BooleanParameterType.build(SHOW_CONNECTIONS_PARAMETER_ID);
        showConnectionsParam.setName("Show Connections");
        showConnectionsParam.setDescription("Export connections between nodes");
        parameters.addParameter(showConnectionsParam);
        
        final PluginParameter<BooleanParameterValue> showNodeLabelsParam = BooleanParameterType.build(SHOW_NODE_LABELS_PARAMETER_ID);
        showNodeLabelsParam.setName("Show Node Labels");
        showNodeLabelsParam.setDescription("Include node labels in the export");
        parameters.addParameter(showNodeLabelsParam);
        
        final PluginParameter<BooleanParameterValue> showConnectionLabelsParam = BooleanParameterType.build(SHOW_CONNECTION_LABELS_PARAMETER_ID);
        showConnectionLabelsParam.setName("Show Connection Labels");
        showConnectionLabelsParam.setDescription("Include connection labels in the export");
        parameters.addParameter(showConnectionLabelsParam);
        
        final PluginParameter<SingleChoiceParameterValue> exportPerspectiveParam = SingleChoiceParameterType.build(EXPORT_PERSPECTIVE_PARAMETER_ID);
        exportPerspectiveParam.setName("Export Perspective");
        exportPerspectiveParam.setDescription("The perspectove the exported graph will be viewed from");
        SingleChoiceParameterType.setOptions(exportPerspectiveParam, Arrays.asList("X-Axis", "Y-Axis", "Z-Axis", "Current Perspective"));
        parameters.addParameter(exportPerspectiveParam);
        
        return parameters;
    }
    
    @Override
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {        
        
        // Get Parameter Values
        final String fnam = parameters.getStringValue(FILE_NAME_PARAMETER_ID);
        final String title = parameters.getStringValue(GRAPH_TITLE_PARAMETER_ID);
        final ConstellationColor color = parameters.getColorValue(BACKGROUND_COLOR_PARAMETER_ID);
        final boolean selectedNodes = parameters.getBooleanValue(SELECTED_NODES_PARAMETER_ID);
        final boolean showConnections = parameters.getBooleanValue(SHOW_CONNECTIONS_PARAMETER_ID);
        final boolean showNodeLabels = parameters.getBooleanValue(SHOW_NODE_LABELS_PARAMETER_ID);
        final boolean showConnectionLabels = parameters.getBooleanValue(SHOW_CONNECTION_LABELS_PARAMETER_ID);
        final String exportPerspective = parameters.getStringValue(EXPORT_PERSPECTIVE_PARAMETER_ID);
        
        final File imageFile = new File(fnam);  
       
        // Build a SVG representation of the graph
        final SVGData svg = new SVGGraphBuilder()
                .withInteraction(interaction)
                .withTitle(title)
                .withReadableGraph(graph)
                .withBackground(color)
                .withNodes(selectedNodes)
                .includeConnections(showConnections)
                .includeNodeLabels(showNodeLabels)
                .includeConnectionLabels(showConnectionLabels)
                .fromPerspective(exportPerspective)
                .build();
        try {
            interaction.setExecutionStage(0, -1, "Exporting Graph", "Writing data to file", true);
            exportToSVG(imageFile, svg, interaction);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        interaction.setProgress(1, 0, "Finished", true);
    }   
    
    /**
     * Exports a single SVGData object to a specified file. 
     * @param file
     * @param data
     * @throws IOException 
     */
    private void exportToSVG(final File file, final SVGData data, final PluginInteraction interaction) throws IOException, InterruptedException {
        final boolean fileOverwritten = file.createNewFile();
        try (final FileWriter writer = new FileWriter(file)) {
            writer.write(data.toString());
            writer.flush();        
            if (fileOverwritten){
                interaction.setProgress(0, -1, String.format("File %s has been overwritten", file.getName()), false);
            } else {
                interaction.setProgress(0, -1, String.format("File %s has been created", file.getName()), false);
            }
        }
    }
}