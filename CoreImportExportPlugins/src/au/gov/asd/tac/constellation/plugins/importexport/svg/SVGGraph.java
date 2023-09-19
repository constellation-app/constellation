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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGFileNameConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.parser.SVGParser;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGAttributeConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGLayoutConstant;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

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
public class SVGGraph {

    private static final Logger LOGGER = Logger.getLogger(SVGGraph.class.getName());
    
    private final SVGContainer svgContainerReference;
    
    /**
     * Wrapper class for a SVGContainer. 
     * Represents the outer most element of a SVG file.
     * @param svg 
     */
    private SVGGraph(final SVGContainer svg) {
        this.svgContainerReference = svg;
    }

    /**
     * Returns an SVGContainer that represents a primary layout component.
     * the intent of this method is tightly coupled to the structure of the
     * Layout.svg file and has no current logical differences from the 
     * SVGContainer.getContainer() method.
     * @param idValue
     * @return 
     */
    private SVGContainer getContainer(final String idValue) {
        return svgContainerReference.getContainer(idValue);
    }
    
    /**
     * Sets the dimensions of the outermost SVG element.
     * @param width
     * @param height 
     */
    public void setDimensions(final Float width, final Float height) {
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
        private String graphTitle = null;
      
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
         * Specifies the title of the graph being exported.
         * @param title The title of the graph.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder withTitle(final String title) {
            this.graphTitle = title;
            return this;
        }
      
        /**
         * Builds an SVGGraphObject representing the provided graph.
         * @return SVGObject
         */
        public SVGObject build() {
            final SVGGraph svgGraphLayout = buildSVGGraphFromTemplate(SVGFileNameConstant.LAYOUT);
            defineBoundary(graph);
            buildHeader(svgGraphLayout);
            buildFooter(svgGraphLayout);
            buildConnections(svgGraphLayout);
            buildNodes(svgGraphLayout);
            setLayoutDimensions(svgGraphLayout);
            return svgGraphLayout.toSVGObject();
        }       
        
        private void buildHeader(final SVGGraph svg){
            final SVGContainer titleContainer = svg.getContainer(SVGLayoutConstant.HEADER.id).getContainer(SVGLayoutConstant.TITLE.id);
            titleContainer.toSVGObject().setContent(graphTitle);
            final SVGContainer subtitleContainer = svg.getContainer(SVGLayoutConstant.HEADER.id).getContainer(SVGLayoutConstant.SUBTITLE.id);
            final ZonedDateTime date = ZonedDateTime.now();
            subtitleContainer.toSVGObject().setContent(
                    String.format("Exported: %s %s, %s",
                            StringUtilities.camelCase(date.getMonth().toString()), 
                            date.getDayOfMonth(), 
                            date.getYear()
                    )
            );
        }
        
        private void buildFooter(final SVGGraph svg){
            final SVGContainer titleContainer = svg.getContainer(SVGLayoutConstant.FOOTER.id).getContainer(SVGLayoutConstant.FOOTNOTE.id);
            titleContainer.toSVGObject().setContent("The Constellation community. All rights reserved.");
        }
        
        /**
         * Generates SVG Nodes from the graph and assigns them as children to the Nodes container.
         * The template file Node.svg is used to build the node.
         * @param svgGraph
         */
        private void buildNodes(final SVGGraph svgGraph) {
            //Retrieve the svg element that holds the nodes as a SVGContainer
            final SVGContainer nodesContainer = svgGraph.getContainer(SVGLayoutConstant.CONTENT.id).getContainer(SVGLayoutConstant.NODES.id);
            
            //Specify attribute ID's for the node vertex's'
            final int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
            final int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);
            final int fillColorAttributeID = VisualConcept.VertexAttribute.COLOR.get(graph); 
            final int backgroundIconID = VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph);
            final int foregroundIconID = VisualConcept.VertexAttribute.FOREGROUND_ICON.get(graph);
            final int decoratorIconsID = VisualConcept.GraphAttribute.DECORATORS.get(graph);
            final VertexDecorators vertexDecorators = graph.getObjectValue(decoratorIconsID, 0);
            final String northEastDecoratorAttributeName=  vertexDecorators.getNorthEastDecoratorAttribute();
            final String northWestDecoratorAttributeName = vertexDecorators.getNorthWestDecoratorAttribute();
            final String southEastDecoratorAttributeName = vertexDecorators.getSouthEastDecoratorAttribute();
            final String southWestDecoratorAttributeName = vertexDecorators.getSouthWestDecoratorAttribute();
                    
            final int vertexCount = graph.getVertexCount();
            for (int vertexPosition = 0 ; vertexPosition < vertexCount ; vertexPosition++) {
                final int vertexID = graph.getVertex(vertexPosition);

                //Get the values of the attributes relevent to the current node
                final Float xVal = (graph.getFloatValue(xAttributeID, vertexID) * 128) - xBoundMin;
                final Float yVal = (yBoundMax - yBoundMin) - ((graph.getFloatValue(yAttributeID, vertexID) * 128) - yBoundMin);
                final String fillColor = graph.getStringValue(fillColorAttributeID, vertexID);
                final String htmlColor = ConstellationColor.getColorValue(fillColor).getHtmlColor();
                final Color color = Color.decode(htmlColor);
                final ConstellationIcon backgroundIcon = graph.getObjectValue(backgroundIconID, vertexID);
                final ConstellationIcon foregroundIcon = graph.getObjectValue(foregroundIconID, vertexID);

                //build the SVGobject representing the node
                final SVGObject node = buildSVGObjectFromTemplate(SVGFileNameConstant.NODE);
                node.setAttribute(SVGAttributeConstant.X.getKey(), xVal.toString());
                node.setAttribute(SVGAttributeConstant.Y.getKey(), yVal.toString());
                node.setAttribute(SVGAttributeConstant.ID.getKey(), ((Integer) vertexID).toString());
                node.setParent(nodesContainer.toSVGObject());
                
                //Add labels to the Node
                final SVGContainer nodeContainer = new SVGContainer(node);
                final SVGContainer bottomLabelContainer = nodeContainer.getContainer(SVGLayoutConstant.BOTTOM_LABELS.id);
                final SVGContainer topLabelContainer = nodeContainer.getContainer(SVGLayoutConstant.TOP_LABELS.id);
                buildBottomLabel(vertexID, bottomLabelContainer);
                buildTopLabel(vertexID, topLabelContainer);
  
                //Add images to the node
                final SVGContainer backgroundContainer = nodeContainer.getContainer(SVGLayoutConstant.BACKGROUND_IMAGE.id);
                final SVGContainer foregroundContainer = nodeContainer.getContainer(SVGLayoutConstant.FOREGROUND_IMAGE.id);
                final byte[] backgroundData = backgroundIcon.getIconData().getData(0, color);
                final byte[] foregroundData = foregroundIcon.getIconData().getData();
                this.buildSVGImageFromRasterImageData(backgroundContainer.toSVGObject(), backgroundData);
                this.buildSVGImageFromRasterImageData(foregroundContainer.toSVGObject(), foregroundData);
                
                //Add decorators to the node                
                this.buildDecorator(northWestDecoratorAttributeName, vertexID, nodeContainer.getContainer(SVGLayoutConstant.NORTH_WEST_DECORATOR.id));
                this.buildDecorator(northEastDecoratorAttributeName, vertexID, nodeContainer.getContainer(SVGLayoutConstant.NORTH_EAST_DECORATOR.id));
                this.buildDecorator(southWestDecoratorAttributeName, vertexID, nodeContainer.getContainer(SVGLayoutConstant.SOUTH_WEST_DECORATOR.id));
                this.buildDecorator(southEastDecoratorAttributeName, vertexID, nodeContainer.getContainer(SVGLayoutConstant.SOUTH_EAST_DECORATOR.id));
            }
        }
        
        private void buildConnections(SVGGraph svgGraph) {
            final SVGContainer linksContainer = svgGraph.getContainer(SVGLayoutConstant.CONTENT.id).getContainer(SVGLayoutConstant.LINKS.id);
             final int linkCount = graph.getLinkCount();
             for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
                 buildLink(linksContainer, graph.getLink(linkPosition));
             }
        }
     
        /**
         * Creates a SVGObject representing a Link.
         * In this context, a link is a connection between nodes 
         * that contains two or more edges.
         * The template file Link.svg is used to build the node.
         * @param svgGraph
         */
        private void buildLink(final SVGContainer linksContainer, int linkID) {
            final int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
            final int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);
            final int sourceVxId = graph.getLinkHighVertex(linkID);
            final int destinationVxId = graph.getLinkLowVertex(linkID); 

            final Float sourceX = (graph.getFloatValue(xAttributeID, sourceVxId) * 128) - xBoundMin + 128;
            final Float sourceY = (yBoundMax - yBoundMin) - ((graph.getFloatValue(yAttributeID, sourceVxId) * 128) - yBoundMin) + 128;
            final Float destinationX = (graph.getFloatValue(xAttributeID, destinationVxId) * 128) - xBoundMin + 128;
            final Float destinationY = (yBoundMax - yBoundMin) - ((graph.getFloatValue(yAttributeID, destinationVxId) * 128) - yBoundMin) + 128;

            final Double sourceConnectionAngle = calculateConnectionAngle(sourceX, sourceY, destinationX, destinationY);
            final Double destinationConnectionAngle = calculateConnectionAngle(destinationX, destinationY, sourceX, sourceY);

            final Double adjustedSourceX = sourceX - (192 * Math.cos(sourceConnectionAngle));
            final Double adjustedSourceY = sourceY - (192 * Math.sin(sourceConnectionAngle));
            final Double adjustedDestinationX = destinationX - (192 * Math.cos(destinationConnectionAngle));
            final Double adjustedDestinationY = destinationY - (192 * Math.sin(destinationConnectionAngle));

            final SVGObject link = buildSVGObjectFromTemplate(SVGFileNameConstant.LINK);
            final SVGObject connection = link.getChild("connection");
            connection.setAttribute(SVGAttributeConstant.SOURCE_X.getKey(), String.format("%s", adjustedSourceX));
            connection.setAttribute(SVGAttributeConstant.SOURCE_Y.getKey(), String.format("%s", adjustedSourceY));
            connection.setAttribute(SVGAttributeConstant.DESTINATION_X.getKey(), String.format("%s", adjustedDestinationX));
            connection.setAttribute(SVGAttributeConstant.DESTINATION_Y.getKey(), String.format("%s", adjustedDestinationY));

            link.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("%s", linkID));
            link.setParent(linksContainer.toSVGObject());

            final SVGObject sourceLinkArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.LINK_ARROW_HEAD);
            buildArrowHead(sourceLinkArrowHeadContainer, sourceX, sourceY, sourceConnectionAngle);
            sourceLinkArrowHeadContainer.setParent(link);

            final SVGObject destinationLinkArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.LINK_ARROW_HEAD);
            buildArrowHead(destinationLinkArrowHeadContainer, destinationX, destinationY, destinationConnectionAngle);
            destinationLinkArrowHeadContainer.setParent(link);
        } 
        
        /**
         * Manipulates an arrow head container to adjust it's position and rotation.
         * @param arrowHeadContainer
         * @param x
         * @param y
         * @param connectionAngle 
         */
        private void buildArrowHead(SVGObject arrowHeadContainer, Float x, Float y, Double connectionAngle) {
            //The size of the svg element containing the arrow head polygon asset
            final int arrowHeadWidth = 128;
            final int arrowHeadheight = 32;
            
            //Redefine the tip of the arrow head as 128px from the center point of the relevent node.
            final Double offsetX = x - (128 * Math.cos(connectionAngle));
            final Double offsetY = y - (128 * Math.sin(connectionAngle));
            
            //Set arrow head svg attributes
            arrowHeadContainer.setAttribute(SVGAttributeConstant.X.getKey(), String.format("%s", offsetX - arrowHeadWidth));
            arrowHeadContainer.setAttribute(SVGAttributeConstant.Y.getKey(), String.format("%s", offsetY - arrowHeadheight /2 ));
            arrowHeadContainer.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("arrow-head-%s-%s", offsetX.intValue(), offsetY.intValue()));
            
            //Rotate the arrow head polygon around the tip to align it with the angle of the connection
            final SVGObject arrowHead = arrowHeadContainer.getChild("arrow-head");
            arrowHead.setAttribute(SVGAttributeConstant.TRANSFORM.getKey(), String.format("rotate(%s %s %s)", Math.toDegrees(connectionAngle), arrowHeadWidth, arrowHeadheight/2));
        }
        
        /**
         * Constructs bottom label SVG elements for a given vertex id in a given SVGContainer.
         * This method only considers the bottom label requirements for nodes.
         * This element of the output graph currently relies heavily on content overflow of parent elements
         * due to inabilities for parents to set their width and height based off of the width and height requirements of their children.
         * @param vertexID
         * @param bottomLabelContainer 
         */
        private void buildBottomLabel(final int vertexID, final SVGContainer bottomLabelContainer){
            final int bottomLabelsAttributeID = VisualConcept.GraphAttribute.BOTTOM_LABELS.get(graph);
            final GraphLabels bottomLabelsContainer = graph.getObjectValue(bottomLabelsAttributeID, 0);
            final List<GraphLabel> labels = bottomLabelsContainer.getLabels();
            
            Integer offset = 0;
            for (int i = 0; i < bottomLabelsContainer.getNumberOfLabels(); i++) {
                final GraphLabel label = labels.get(i);
                final String labelAttributeNameReference = label.getAttributeName();
                int labelAttribnuteID = graph.getAttribute(GraphElementType.VERTEX, labelAttributeNameReference);

                final String labelString = graph.getStringValue(labelAttribnuteID, vertexID);

                if (labelString != null){
                    SVGObject text = buildSVGObjectFromTemplate(SVGFileNameConstant.BOTTOM_LABEL);
                    //Note: size scale of 1 is 128 px
                    final Float size = label.getSize() * 64;
                    final Integer intSize = size.intValue();
                    text.setAttribute(SVGAttributeConstant.FONT_SIZE.getKey(),  String.format("%s", intSize));
                    text.setAttribute(SVGAttributeConstant.Y.getKey(), String.format("%spx", offset));
                    text.setAttribute(SVGAttributeConstant.FILL_COLOR.getKey(),label.getColor().getHtmlColor());
                    text.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("bottom-label-%s", i));
                    text.setContent(SVGParser.sanitisePlanText(labelString));
                    text.setParent(bottomLabelContainer.toSVGObject());
                    offset = offset + intSize;
                }
            }
        }
        
        /**
         * Constructs the top label SVG elements for a given vertex id in a given SVGContainer.
         * This method only considers the top label requirements for nodes.
         * This element of the output graph currently relies heavily on content overflow of parent elements
         * due to inabilities for parents to set their width and height based off of the width and height requirements of their children.
         * @param vertexID
         * @param bottomLabelContainer 
         */
        private void buildTopLabel(final int vertexID, final SVGContainer topLabelContainer){
            final int topLabelsAttributeID = VisualConcept.GraphAttribute.TOP_LABELS.get(graph);
            final GraphLabels topLabelsContainer = graph.getObjectValue(topLabelsAttributeID, 0);
            final List<GraphLabel> labels = topLabelsContainer.getLabels();
            
            Integer offset = 0;
            for (int i = 0; i < topLabelsContainer.getNumberOfLabels(); i++) {
                final GraphLabel label = labels.get(i);
                final String labelAttributeName = label.getAttributeName();
                final int labelAttribnuteID = graph.getAttribute(GraphElementType.VERTEX, labelAttributeName);
                final String labelString = graph.getStringValue(labelAttribnuteID, vertexID);
                if (labelString != null){
                    final SVGObject text = buildSVGObjectFromTemplate(SVGFileNameConstant.TOP_LABEL);
                    final Float size = label.getSize() * 64;
                    final Integer intSize = size.intValue();
                    text.setAttribute(SVGAttributeConstant.FONT_SIZE.getKey(), String.format("%s", intSize));
                    text.setAttribute(SVGAttributeConstant.Y.getKey(), String.format("%spx", offset.toString()));
                    text.setAttribute(SVGAttributeConstant.FILL_COLOR.getKey(),label.getColor().getHtmlColor());
                    text.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("top-label-%s", i));
                    text.setContent(SVGParser.sanitisePlanText(labelString));
                    text.setParent(topLabelContainer.toSVGObject());
                    offset = offset - intSize;
                }
            }
        }
        
        /**
         * Builds an image element within an SVG element.
         * @param parent
         * @param data 
         */
        private void buildSVGImageFromRasterImageData(final SVGObject parent, final byte[] data) {
            final String encodedString = Base64.getEncoder().encodeToString(data);
            final SVGObject image = buildSVGObjectFromTemplate(SVGFileNameConstant.IMAGE);
            image.setAttribute("xlink:href", String.format("data:image/png;base64,%s", encodedString));
            image.setParent(parent);
        }
        
        /**
         * Creates a SVGObject from a template SVG file
         * The object will be returned with no parent.
         * 
         * @param templateResource the filename of the template file.
         * @return 
         */
        private SVGObject buildSVGObjectFromTemplate(final SVGFileNameConstant templateResource) {
            final InputStream inputStream = templateResource.getClass().getResourceAsStream(templateResource.resourceName);
            SVGObject templateSVG = null;
            try {
                templateSVG = SVGParser.parse(inputStream);
            } catch (final IOException ex) {
                LOGGER.log(Level.INFO, Arrays.toString(ex.getStackTrace()));
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
        private SVGGraph buildSVGGraphFromTemplate(final SVGFileNameConstant templateResource) {
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
                    if (xBoundMin > xCoordinate) {
                        xBoundMin = xCoordinate;
                    }
                    if (xBoundMax < xCoordinate) {
                        xBoundMax = xCoordinate;
                    }
                    if (yBoundMin > yCoordinate) {
                        yBoundMin = yCoordinate;
                    }
                    if (yBoundMax < yCoordinate) {
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
            final Float topMargin = 288.0F;
            final Float bottomMargin = 128.0F;
            final Float xPadding = 250.0F;
            final Float yPadding = 250.0F;
            final Float footerYOffset = topMargin + contentHeight + (yPadding * 2);            
            final Float fullWidth = (xMargin * 2) + contentWidth + (xPadding * 2);            
            final Float fullHeight = (topMargin + bottomMargin) + contentHeight + (yPadding * 2);            
            final Float backgroundWidth = contentWidth + (xPadding * 2);
            final Float backgroundHeight = contentHeight + (yPadding * 2);
            final Float contentYOffset = topMargin + yPadding;
            final Float contentXOffset = xMargin + xPadding;
            
            svg.setDimensions(fullWidth, fullHeight);

            svg.getContainer(SVGLayoutConstant.HEADER.id).setDimension(fullWidth, topMargin);
            svg.getContainer(SVGLayoutConstant.FOOTER.id).setDimension(fullWidth, bottomMargin);
            svg.getContainer(SVGLayoutConstant.FOOTER.id).setposition(0F, footerYOffset);

            svg.getContainer(SVGLayoutConstant.CONTENT.id).setDimension(contentWidth, contentHeight);
            svg.getContainer(SVGLayoutConstant.CONTENT.id).setposition(contentXOffset, contentYOffset);

            svg.getContainer(SVGLayoutConstant.BACKGROUND.id).setDimension(backgroundWidth, backgroundHeight);
            svg.getContainer(SVGLayoutConstant.BACKGROUND.id).setposition(xMargin, topMargin);
            svg.getContainer(SVGLayoutConstant.BORDER.id).setDimension(fullWidth, fullHeight);
        }

        /**
         * Generates decorator images for nodes.
         * @param vertexAttributeReference
         * @param vertexID
         * @param decoratorContainer 
         */
        private void buildDecorator(final String vertexAttributeReference, final int vertexID, final SVGContainer decoratorContainer) {
            if (vertexAttributeReference != null){
                final int attributeID = graph.getAttribute(GraphElementType.VERTEX, vertexAttributeReference);
                if (attributeID != Graph.NOT_FOUND){
                    final String attributeValue;
                    if ("pinned".equals(vertexAttributeReference)){
                        if ("true".equals(graph.getStringValue(attributeID, vertexID))){
                            attributeValue = "Pin";
                        } else {
                            return;
                        }
                    } else {
                        attributeValue = graph.getStringValue(attributeID, vertexID);
                    }
                    if (attributeValue != null){
                        final byte[] decoratorIconData = IconManager.getIcon(attributeValue).getIconData().getData();
                        this.buildSVGImageFromRasterImageData(decoratorContainer.toSVGObject(), decoratorIconData);
                    }
                }
            }
        }
        
        /**
         * Calculates the angle at which a connection touches a node.
         * Return value is clockwise from a horizontal x axis with positive values to the right.
         * @param sourceX
         * @param sourceY
         * @param destinationX
         * @param destinationY
         * @return 
         */
        private Double calculateConnectionAngle(Float sourceX, Float sourceY, Float destinationX, Float destinationY) {
            final Float xDirectionVector = sourceX - destinationX;
            final Float yDirectionVector = sourceY - destinationY;
            return Math.atan2(yDirectionVector, xDirectionVector);
        }
    }
}
