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

import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGResourceConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.parser.SVGParser;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.io.IOException;
import java.io.InputStream;
import org.openide.util.Exceptions;

/**
 * A Wrapper class for the outer most SVGElement of the output file.
 * This element acts as the single outer element for the SVG file, providing 
 * height and width information as well as defining container SVG elements to house other
 * meaningful output data such as Nodes, Transactions, title information and legends.
 * This class also enables the use of the builder pattern. 
 * In future this object may be able to contain and display graph specific information 
 * such as a title, node counts, transaction counts, boarders and disclaimers. 
 * 
 * @author capricornunicorn123
 */
public class SVGGraph{
    
    private final SVGContainer svgContainerReference;
    
    private SVGGraph(final SVGContainer svg) {
        this.svgContainerReference = svg;
    }

    /**
     * Returns an SVGContainer that represents a primary layout component.
     * the intent of this method is tightly couplesd to the structure of the
     * Layout.svg file and has no current logical differences from the 
     * SVGContainer.getContainer() method.
     * @param classValue
     * @return 
     */
    private SVGContainer getContainer(final String classValue) {
        return svgContainerReference.getContainer(classValue);
    }
    
    /**
     * Sets the dimensions of the outermost SVG element.
     * @param width
     * @param height 
     */
    public void setDimensions(final Float width, final Float height){
        svgContainerReference.setDimension(width, height);
    }

    /**
     * Removes the SVGGraph wrapper class from the SVGObject.
     * @return 
     */
    private SVGObject toSVGObject() {
        return svgContainerReference.toSVGObject();
    }
    
    /**
     * Builder that generates the content for the output svg file.
     * The builder abstracts the responsibility of building an SVG from the ExportToSVGPlugin.
     * Currently the builder requires a graph to be specified using .withGraph()
     * and for the build to be initialized using .build()
     * <pre>
     * Example Usage: {@code new SVGGraph.SVGGraphBuilder().withGraph(graph).build();}
     * </pre>
     */
    public static class SVGGraphBuilder {

        private GraphReadMethods graph;
        private Float xBoundMin = null;
        private Float xBoundMax = null;
        private Float yBoundMin = null;
        private Float yBoundMax = null;
      
        /**
         * Specifies the graph to build the SVG from.
         * @param graph The graph to be exported.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder withGraph(final GraphReadMethods graph) {
            this.graph = graph;
            return this;
        }
      
        /**
         * Builds an SVGGraphObject representing the provided graph.
         * @return SVGObject
         */
        public SVGObject build() {
            final SVGGraph svgGraphLayout = buildSVGGraphFromTemplate(SVGResourceConstant.LAYOUT);
            defineBoundary(graph);
            buildLinks(svgGraphLayout);
            buildNodes(svgGraphLayout);
            setLayoutDimensions(svgGraphLayout);
            return svgGraphLayout.toSVGObject();
        }       
        
        /**
         * Generates SVG Nodes from the graph and assigns them as children to the Nodes container.
         * The template file Node.svg is used to build the node.
         * @param svgGraph
         */
        private void buildNodes(final SVGGraph svgGraph) {
            final SVGContainer nodesContainer = svgGraph.getContainer("content").getContainer("nodes");
            
            final int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
            final int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);

            final int vertexCount = graph.getVertexCount();
            for (int vertexPosition = 0 ; vertexPosition < vertexCount ; vertexPosition++) {
                final int vertexID = graph.getVertex(vertexPosition);

                final Float xVal = (graph.getFloatValue(xAttributeID, vertexID) * 128) - xBoundMin;
                final Float yVal = (yBoundMax - yBoundMin) - ((graph.getFloatValue(yAttributeID, vertexID) * 128) - yBoundMin);

                final SVGObject node = buildSVGObjectFromTemplate(SVGResourceConstant.NODE);
                node.setAttribute("x", xVal.toString());
                node.setAttribute("y", yVal.toString());
                node.setParent(nodesContainer.toSVGObject());
            }
        }
        
        /**
         * Creates a SVGObject representing a Link.
         * The template file Link.svg is used to build the node.
         * 
         * @param svgGraph
         */
        private void buildLinks(final SVGGraph svgGraph) {
            final SVGContainer linksContainer = svgGraph.getContainer("content").getContainer("links");
            
            int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
            int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);

            int linkCount = graph.getLinkCount();
            for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
                final int linkID = graph.getLink(linkPosition);
                final int sourceVxId = graph.getLinkHighVertex(linkID);
                final int destinationVxId = graph.getLinkLowVertex(linkID); 
                
                final Float x1Val = (graph.getFloatValue(xAttributeID, sourceVxId) * 128) - xBoundMin + 128;
                final Float y1Val = (yBoundMax - yBoundMin) - ((graph.getFloatValue(yAttributeID, sourceVxId) * 128) - yBoundMin) + 128;
                final Float x2Val = (graph.getFloatValue(xAttributeID, destinationVxId) * 128) - xBoundMin + 128;
                final Float y2Val = (yBoundMax - yBoundMin) - ((graph.getFloatValue(yAttributeID, destinationVxId) * 128) - yBoundMin) + 128;

                final SVGObject link = buildSVGObjectFromTemplate(SVGResourceConstant.LINK);
                link.setAttribute("x1", x1Val.toString());
                link.setAttribute("y1", y1Val.toString());
                link.setAttribute("x2", x2Val.toString());
                link.setAttribute("y2", y2Val.toString());
                link.setParent(linksContainer.toSVGObject());
            }
        }
        
        /**
         * Creates a SVGObject from a template SVG file
         * The object will be returned with no parent.
         * 
         * @param templateResource the filename of the template file.
         * @return 
         */
        private SVGObject buildSVGObjectFromTemplate(final SVGResourceConstant templateResource) {
            final InputStream inputStream = templateResource.getClass().getResourceAsStream(templateResource.resourceName);
            SVGObject templateSVG = null;
            try {
                templateSVG = SVGParser.parse(inputStream);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return templateSVG;
        }
        
                /**
         * Creates a SVGGraph from a template SVG file.
         * The object will be returned with no parent.
         * Note an SVGGraph is a Wrapper around a SVGContainer.
         * Note an SVGContainer is a wrapper around an SVGObject. 
         * @param templateResource the filename of the template file.
         * @return 
         */
        private SVGGraph buildSVGGraphFromTemplate(final SVGResourceConstant templateResource){
            return new SVGGraph(new SVGContainer(buildSVGObjectFromTemplate(templateResource)));
        }
 
        /**
         * Retrieves the min and max values for x and y for a given graph. 
         * Represents the extremities of the graph area that contains render-able objects.
         * @param graph
         * @return 
         */
        private void defineBoundary(final GraphReadMethods graph) {
            final int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
            final int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);
            final int vertexCount = graph.getVertexCount();
            for (int vertexPosition = 0 ; vertexPosition < vertexCount ; vertexPosition++) {
                final int vertexID = graph.getVertex(vertexPosition);
                final Float xCoordinate = graph.getFloatValue(xAttributeID, vertexID) * 128; 
                final Float yCoordinate = graph.getFloatValue(yAttributeID, vertexID) * 128;
                if (vertexPosition == 0) {
                    xBoundMin = xCoordinate;
                    xBoundMax = xCoordinate;
                    yBoundMin = yCoordinate;
                    yBoundMax = yCoordinate;
                } else {
                    if (xBoundMin > xCoordinate){
                        xBoundMin = xCoordinate;
                    }
                    if (xBoundMax < xCoordinate){
                        xBoundMax = xCoordinate;
                    }
                    if (yBoundMin > yCoordinate){
                        yBoundMin = yCoordinate;
                    }
                    if (yBoundMax < yCoordinate){
                        yBoundMax = yCoordinate;
                    }
                }
            }
        }

        /**
         * Sets the dimensions for container objects within the Layout.svg template file.
         * This method is a temporary solution for setting layout dimensions semi-manually.
         * Ideally layout elements will be built from a top down approach.
         * In doing so each layout object will be able to set its own dimensions 
         * based on it's own content.
         * @param svg 
         */
        private void setLayoutDimensions(final SVGGraph svg) {
            final Float contentWidth = xBoundMax - xBoundMin + 256;
            final Float contentHeight = yBoundMax - yBoundMin + 256;
            final Float xMargin = 50.0F;
            final Float yMargin = 50.0F;
            final Float xPadding = 50.0F;
            final Float yPadding = 50.0F;
            final Float footerYOffset = yMargin + contentHeight + (yPadding * 2);            
            final Float fullWidth = (xMargin * 2) + contentWidth + (xPadding * 2);            
            final Float fullHeight = (yMargin * 2) + contentHeight + (yPadding * 2);            
            final Float backgroundWidth = contentWidth + (xPadding * 2);
            final Float backgroundHeight = contentHeight + (yPadding * 2);
            final Float contentYOffset = yMargin + yPadding;
            final Float contentXOffset = xMargin + xPadding;
            
            svg.setDimensions(fullWidth, fullHeight);

            svg.getContainer("header").setDimension(fullWidth, yMargin);

            svg.getContainer("footer").setDimension(fullWidth, yMargin);
            svg.getContainer("footer").setposition(0F, footerYOffset);

            svg.getContainer("content").setDimension(contentWidth, contentHeight);
            svg.getContainer("content").setposition(contentXOffset, contentYOffset);

            svg.getContainer("background").setDimension(backgroundWidth, backgroundHeight);
            svg.getContainer("background").setposition(xMargin, yMargin);
            
            svg.getContainer("border").setDimension(fullWidth, fullHeight);
        }
    }
}
