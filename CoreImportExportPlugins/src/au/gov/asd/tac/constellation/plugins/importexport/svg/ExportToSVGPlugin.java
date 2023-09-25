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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author capricornunicorn123
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
@NbBundle.Messages("ExportToSVGPlugin=Export to SVG")
public class ExportToSVGPlugin extends SimpleReadPlugin {
    public static final String FILE_NAME_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "filename");
    public static final String GRAPH_TITLE_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "graphtitle");
    public static final String GRAPH_CONNECTION_MODE_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "connectionmode");

    
    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FileParameterValue> fnamParam = FileParameterType.build(FILE_NAME_PARAMETER_ID);
        fnamParam.setName(FILE_NAME_PARAMETER_ID);
        fnamParam.setDescription("File to write to");
        parameters.addParameter(fnamParam);
        
        final PluginParameter<StringParameterValue> graphTitleParam = StringParameterType.build(GRAPH_TITLE_PARAMETER_ID);
        graphTitleParam.setName(GRAPH_TITLE_PARAMETER_ID);
        graphTitleParam.setDescription("Title of the graph");
        parameters.addParameter(graphTitleParam);
        
        final PluginParameter<ObjectParameterValue> graphConnectionModeParam = ObjectParameterType.build(GRAPH_CONNECTION_MODE_PARAMETER_ID);
        graphConnectionModeParam.setName(GRAPH_CONNECTION_MODE_PARAMETER_ID);
        graphConnectionModeParam.setDescription("Connection mode of the graph");
        parameters.addParameter(graphConnectionModeParam);
        
        return parameters;
    }
    
    @Override
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException { 
        final String fnam = parameters.getStringValue(FILE_NAME_PARAMETER_ID);
        final String title = parameters.getStringValue(GRAPH_TITLE_PARAMETER_ID);
        final ConnectionMode connectionMode = (ConnectionMode) parameters.getObjectValue(GRAPH_CONNECTION_MODE_PARAMETER_ID);

        final File imageFile = new File(fnam);     
        final SVGObject svg = new SVGGraph.SVGGraphBuilder()
                .withTitle(title)
                .withGraph(graph)
                .withConnectionMode(connectionMode)
                .build();
        try {
            exportToSVG(imageFile, svg);
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }   
    
    /**
     * Exports a single SVG object to a specified file. 
     * 
     * @param file
     * @param data
     * @throws IOException 
     */
    private void exportToSVG(final File file, final SVGObject data) throws IOException {
        final boolean fileOverwritten = file.createNewFile();
        try (final FileWriter writer = new FileWriter(file)) {
            writer.write(data.toString());
            writer.flush();        
        }
    }
}