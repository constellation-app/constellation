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
import java.io.IOException;
import java.io.InputStream;
import org.openide.util.Exceptions;

/**
 * The SVGElement that encapsulates the SVG representations of graph elements.
 * Currently this object does not contain any meaningful information other than
 * acting as the single outer element for the SVG file, providing height and width information
 * as well as enabling the use of the builder pattern. 
 * In future this object may be able to contain and display graph specific information 
 * such as a title, node counts, transaction counts, boarders and disclaimers. 
 * 
 * @author capricornunicorn123
 */
public class SVGGraph extends SVGObject{
    
    private SVGGraph(String type, SVGObject parent) {
        super(type, parent);
    }
    
    /**
     * The builder responsible for abstracting the responsibility of building an SVG from the ExportToSVGPlugin
     * Currently the builder requires a graph to be specified using .withGraph()
     * and for the build to be initialized using .build()
     * <pre>
     * Example Usage: {@code new SVGGraph.SVGGraphBuilder().withGraph(graph).build();}
     * </pre>
     * 
     */
    public static class SVGGraphBuilder {

        private GraphReadMethods graph;
        private Float[][] bounds;
      
        /**
         * Specifies the graph to build the SVG from.
         * @param graph The graph to be exported.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder withGraph(GraphReadMethods graph){
            this.graph = graph;
            return this;
        }
        
        /**
         * Builds an SVGGraphObject representing the provided graph.
         * @return SVGObject
         */
        public SVGObject build(){
            defineBoundary(graph);
            SVGGraph svg = new SVGGraph("svg", null);
            svg.setAttribute("width", String.format("%s", bounds[0][1] - bounds[0][0] + 256));
            svg.setAttribute("height", String.format("%s", bounds[1][1] - bounds[1][0] + 256));
            buildLinks(svg);
            buildNodes(svg);
            return svg;
        }
        
        /**
         * Creates a SVGObject representing a Node.
         * The template file Node.svg is used to build the node.
         * 
         * @param parent The parent elements that the nodes should be nested within.
         */
        private void buildNodes(SVGObject parent){

            final int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
            final int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);

            final int vertexCount = graph.getVertexCount();
            for (int vertexPosition = 0 ; vertexPosition < vertexCount ; vertexPosition++) {

                final int vertexID = graph.getVertex(vertexPosition);

                final Float xVal = (graph.getFloatValue(xAttributeID, vertexID) * 128) - bounds[0][0];
                final Float yVal = (bounds[1][1] - bounds[1][0]) - ((graph.getFloatValue(yAttributeID, vertexID) * 128) - bounds[1][0]);

                final Float[] coordinates = {xVal, yVal};

                final SVGObject node = buildFromTemplate(SVGResourceConstants.NODE);
                node.setAttribute("x", coordinates[0].toString());
                node.setAttribute("y", coordinates[1].toString());

                node.setParent(parent);
            }

        }
        
        /**
         * Creates a SVGObject representing a Link.
         * The template file Link.svg is used to build the node.
         * 
         * @param parent The parent elements that the links should be nested within.
         */
        private void buildLinks(SVGObject parent){

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

                SVGObject link = buildFromTemplate(SVGResourceConstants.LINK);
                link.setAttribute("x1", coords[0][0].toString());
                link.setAttribute("y1", coords[0][1].toString());
                link.setAttribute("x2", coords[1][0].toString());
                link.setAttribute("y2", coords[1][1].toString());
                link.setParent(parent);
            }
        }
        
        /**
         * Creates a SVGObject from a template SVG file
         * The object will be returned with no parent.
         * 
         * @param templateResource the filename of the template file.
         * @return 
         */
        private SVGObject buildFromTemplate(String templateResource){
            SVGResourceConstants resourceClass = new SVGResourceConstants();
            InputStream inputStream = resourceClass.getClass().getResourceAsStream(templateResource);
            SVGObject templateSVG = null;
            try {
                templateSVG = SVGParser.parse(inputStream);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return templateSVG;
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
        private void defineBoundary(GraphReadMethods graph) {
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
            this.bounds = bounds;
        }
    }

}
