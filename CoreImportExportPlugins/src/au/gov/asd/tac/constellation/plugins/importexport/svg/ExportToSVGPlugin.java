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
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
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
import java.util.ArrayList;
import java.util.List;
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
    protected void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException { 
        final String fnam = parameters.getStringValue(FILE_NAME_PARAMETER_ID);
        final File imageFile = new File(fnam);     
        
        final Float[][] bounds = getBounds(graph);
        
        final SVGObject svg = new SVGObject("svg", null);     
        svg.setAttribute("width", String.format("%s", bounds[0][1] - bounds[0][0] + 256));
        svg.setAttribute("height", String.format("%s", bounds[1][1] - bounds[1][0] + 256));
        
        final List<SVGObject> links = extractLinkElements(graph);
        final List<SVGObject> nodes = extractNodeElements(graph);
        
        links.forEach(link -> link.setParent(svg));
        nodes.forEach(node -> node.setParent(svg));
        
        try {
            exportToSVG(imageFile, svg);
        } catch (IOException ex) {
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
    private void exportToSVG(final File file, final SVGObject data) throws IOException{
        final boolean fileOverwritten = file.createNewFile();
            data.setAttribute("xmlns", "http://www.w3.org/2000/svg"); // Makes SVG Object Valid
            try (final FileWriter writer = new FileWriter(file)){
                writer.write(data.toString());
                writer.flush();        
        }
    }
    
    /**
     * Creates a list of SVGObjects representing nodes from a graph.
     * These nodes contain positioning data relative to the external boundaries 
     * created by render-able objects in the graph.
     * 
     * @param graph
     * @return 
     */
    private ArrayList<SVGObject> extractNodeElements(final GraphReadMethods graph) {
        ArrayList<SVGObject> nodes = new ArrayList<>();
        Float[][] bounds = getBounds(graph);
        
        final int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);

        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0 ; vertexPosition < vertexCount ; vertexPosition++) {
            
            final int vertexID = graph.getVertex(vertexPosition);
            
            final Float xVal = (graph.getFloatValue(xAttributeID, vertexID) * 128) - bounds[0][0];
            final Float yVal = (bounds[1][1] - bounds[1][0]) - ((graph.getFloatValue(yAttributeID, vertexID) * 128) - bounds[1][0]);
            
            final Float[] coordinates = {xVal, yVal};
            
            final SVGObject node = generateNode(null);
            node.setAttribute("x", coordinates[0].toString());
            node.setAttribute("y", coordinates[1].toString());
            
            nodes.add(node);
        }

        return nodes;
    }
        
    /**
     * Creates a list of SVGObjects representing links from a graph.
     * These links contain positioning data relative to the external boundaries 
     * created by transactions in the graph.
     * 
     * @param graph
     * @return 
     */
    private List<SVGObject> extractLinkElements(GraphReadMethods graph) {
        ArrayList<SVGObject> links = new ArrayList<>();
        Float[][] bounds = getBounds(graph);

        int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
        int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);

        int linkCount = graph.getLinkCount();
        for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {

            final int linkID = graph.getLink(linkPosition);
            
            final int sourceVxId = graph.getLinkHighVertex(linkID);
            final int destinationVxId = graph.getLinkLowVertex(linkID);
            
            int[] points = {sourceVxId, destinationVxId};
            Float[][] coords = {{0.0f,0.0f},{0.0f,0.0f}};
            for (int i = 0 ; i < 2; i++){
                Float xVal = (graph.getFloatValue(xAttributeID, points[i]) * 128) - bounds[0][0] + 128;
                Float yVal = (bounds[1][1] - bounds[1][0]) - ((graph.getFloatValue(yAttributeID, points[i]) * 128) - bounds[1][0]) + 128;
                coords[i][0] = xVal;
                coords[i][1] = yVal;
            }

            SVGObject link = generateLink(null);
            link.setAttribute("x1", coords[0][0].toString());
            link.setAttribute("y1", coords[0][1].toString());
            link.setAttribute("x2", coords[1][0].toString());
            link.setAttribute("y2", coords[1][1].toString());
            
            links.add(link);
        }

        return links;
    }
     
    /**
     * Creates a SVGObject representing a Node from a template SVG file
     * 
     * @param parent
     * @return 
     */
    private SVGObject generateNode(final SVGObject parent) { 
        final SVGResourceConstants resourceClass = new SVGResourceConstants();
        final InputStream inputStream = resourceClass.getClass().getResourceAsStream(SVGResourceConstants.NODE);
        SVGObject node = null;
        try {
            node = SVGParser.parse(inputStream);
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        node.setParent(parent);
        return node;
    }

    /**
     * Retrieves the minimum and maximum values for x and y coordinates on a graph. 
     * Represents the extremities of the graph area that contains render-able objects.
     * Given the fact that this method is located within the plugin and caution is being made 
     * about assigning non parameter related local variables to this plugin 
     * encapsulation in a separate class may be useful
     * 
     * @param graph
     * @return 
     */
    private Float[][] getBounds(final GraphReadMethods graph) {
        final int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);
        final Float[][] bounds = {{null,null},{null,null}};
        int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0 ; vertexPosition < vertexCount ; vertexPosition++) {
            
            final int vertexID = graph.getVertex(vertexPosition);
            final Float[] coordinates = {graph.getFloatValue(xAttributeID, vertexID) * 128 , graph.getFloatValue(yAttributeID, vertexID) * 128};
            if (bounds[0][0] == null){
                bounds[0][0] = coordinates[0];
                bounds[0][1] = coordinates[0];
                bounds[1][0] = coordinates[1];
                bounds[1][1] = coordinates[1];
            } else {
                for (int i = 0 ; i < coordinates.length ; i++){
                    if (coordinates[i] < bounds[i][0]) {
                        bounds[i][0] = coordinates[i];
                    }

                    if (coordinates[i] > bounds[i][1]) {
                        bounds[i][1] = coordinates[i];
                    }
                } 
            }
        }
        return bounds;
    }
    
    private SVGObject generateLink(SVGObject parent) {
        SVGResourceConstants resourceClass = new SVGResourceConstants();
        InputStream inputStream = resourceClass.getClass().getResourceAsStream(SVGResourceConstants.LINK);
        SVGObject link = null;
        try {
            link = SVGParser.parse(inputStream);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        link.setParent(parent);
        return link;
    }
}