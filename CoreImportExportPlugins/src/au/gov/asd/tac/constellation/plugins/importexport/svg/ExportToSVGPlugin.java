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
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
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
    private static final Logger LOGGER = Logger.getLogger(ExportToSVGPlugin.class.getName());
    public static final String FILE_NAME_PARAMETER_ID = PluginParameter.buildId(ExportToSVGPlugin.class, "filename");
    
    public ExportToSVGPlugin(){
        
    }
    
    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> fnamParam = StringParameterType.build(FILE_NAME_PARAMETER_ID);
        fnamParam.setName(FILE_NAME_PARAMETER_ID);
        fnamParam.setDescription("File to write to");

        parameters.addParameter(fnamParam);
        return parameters;
    }
    
    @Override
    protected void read(GraphReadMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException { 
        final String fnam = parameters.getStringValue(FILE_NAME_PARAMETER_ID);
        final File imageFile = new File(fnam);
        SVGResourceConstants resourceClass = new SVGResourceConstants();

        InputStream inputStream = resourceClass.getClass().getResourceAsStream(SVGResourceConstants.NODE);
        SVGObject node;
        try {
            node = SVGParser.parse(inputStream);
            try {
                exportToSVG(imageFile, node.toString());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
       
    }   
    
    /**
     * Exports a graph to an SVG format.
     * @param file
     * @param data
     * @throws IOException 
     */
    private void exportToSVG(File file, final String data) throws IOException{
        file.createNewFile();
        // Writes the content to the file
        try ( // creates a FileWriter Object
                FileWriter writer = new FileWriter(file)) {
            // Writes the content to the file
            writer.write(data);
            writer.flush();
        }
    }
}