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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
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
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Exports data stored on an active graph and a .svg file.
 * A large mount of the SVG generating functionality of this plugin has been abstracted from this class.
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
    public static final String SHOW_TOP_LABELS_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "show_top_labels");
    public static final String SHOW_BOTTOM_LABELS_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "show_bottom_labels");
    private static final Logger LOGGER = Logger.getLogger(ExportToSVGPlugin.class.getName());
    
    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FileParameterValue> fnamParam = FileParameterType.build(FILE_NAME_PARAMETER_ID);
        fnamParam.setName("File Name");
        fnamParam.setDescription("File to write to");
        FileParameterType.setKind(fnamParam, FileParameterType.FileParameterKind.SAVE);
        FileParameterType.setFileFilters(fnamParam, new FileChooser.ExtensionFilter("SVG file", "*" + FileExtensionConstants.SVG));
        parameters.addParameter(fnamParam);
        
        final PluginParameter<StringParameterValue> graphTitleParam = StringParameterType.build(GRAPH_TITLE_PARAMETER_ID);
        graphTitleParam.setName("Graph Title");
        graphTitleParam.setDescription("Title of the graph");
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
        
        final PluginParameter<BooleanParameterValue> showTopNodesParam = BooleanParameterType.build(SHOW_TOP_LABELS_PARAMETER_ID);
        showTopNodesParam.setName("Show Top Labels");
        showTopNodesParam.setDescription("Export the top labels of nodes");
        parameters.addParameter(showTopNodesParam);
        
        final PluginParameter<BooleanParameterValue> showBottomNodesParam = BooleanParameterType.build(SHOW_BOTTOM_LABELS_PARAMETER_ID);
        showBottomNodesParam.setName("Show Bottom Labels");
        showBottomNodesParam.setDescription("Export the bottom labels of nodes");
        parameters.addParameter(showBottomNodesParam);
        
        return parameters;
    }
    
    @Override
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {        
        
        final String fnam = parameters.getStringValue(FILE_NAME_PARAMETER_ID);
        final String title = parameters.getStringValue(GRAPH_TITLE_PARAMETER_ID);
        final ConstellationColor color = parameters.getColorValue(BACKGROUND_COLOR_PARAMETER_ID);
        final Boolean selectedNodes = parameters.getBooleanValue(SELECTED_NODES_PARAMETER_ID);
        final Boolean showConnections = parameters.getBooleanValue(SHOW_CONNECTIONS_PARAMETER_ID);
        final Boolean showTopLabels = parameters.getBooleanValue(SHOW_TOP_LABELS_PARAMETER_ID);
        final Boolean showBottomLabels = parameters.getBooleanValue(SHOW_BOTTOM_LABELS_PARAMETER_ID);
        
        final File imageFile = new File(fnam);  
        
        //This plugin functionality relies heavily on VisualgraphAccess methods to interpret the graph consistenly.
        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
        GraphVisualAccess access = new GraphVisualAccess(currentGraph);
        access.beginUpdate();
        access.updateInternally();
        
        //Generate the SVG output.
        final SVGData svg = new SVGGraph.SVGGraphBuilder()
                .withInteraction(interaction)
                .withTitle(title)
                .withAccess(access)
                .withGraph(graph)
                .withBackground(color)
                .withNodes(selectedNodes)
                .includeConnections(showConnections)
                .includeTopLabels(showTopLabels)
                .includeBottomLabels(showBottomLabels)
                .build();
        try {
            interaction.setExecutionStage(0, -1, "Exporting Graph", "Writing data to file", true);
            exportToSVG(imageFile, svg);
        } catch (final IOException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage());
        }
        access.endUpdate();
        interaction.setProgress(1, 0, "Finished", true);
    }   
    
    /**
     * Exports a single SVGData object to a specified file. 
     * @param file
     * @param data
     * @throws IOException 
     */
    private void exportToSVG(final File file, final SVGData data) throws IOException {
        final boolean fileOverwritten = file.createNewFile();
        try (final FileWriter writer = new FileWriter(file)) {
            writer.write(data.toString());
            writer.flush();        
        }
    }
}