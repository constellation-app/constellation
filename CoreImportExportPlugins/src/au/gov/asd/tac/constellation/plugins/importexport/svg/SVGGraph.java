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
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGAttributeConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGLayoutConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGFileNameConstant;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import java.util.Base64;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

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
    
    /**
     * Wrapper class for a SVGContainer. 
     * Represents the outer most element of a SVG file.
     * @param svg 
     */
    private SVGGraph() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Builder that generates the content for the output svg file.
     * The builder abstracts the responsibility of building an SVG from the ExportToSVGPlugin.
     * Currently the builder requires a graph to be specified using .withGraph()
     * and for the build to be initialized using .build()
     * <pre>
     * Example Usage: {@code new SVGGraph.SVGGraphBuilder().withAccess(graph).build();}
     * </pre>
     */
    public static class SVGGraphBuilder {
        private GraphReadMethods graph;
        private GraphVisualAccess access;
        private Vector3f maxBound = null;
        private Vector3f minBound = null;
        private String graphTitle = null;
        private boolean selectedNodesOnly = false;
        private boolean showConnections = true;
        private boolean showTopLabels = true;
        private boolean showBottomLabels = true;
        private ConstellationColor backgroundColor = VisualGraphDefaults.DEFAULT_BACKGROUND_COLOR;
      
        /**
         * Specifies the graph to build the SVG from.
         * @param graph The graph to be exported.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder withGraph(final GraphReadMethods graph) {
            this.graph = graph;
            return this;
        }
        
        public SVGGraphBuilder withAccess(final GraphVisualAccess access) {
            this.access = access;
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
         * Specifies the color of the graph background being exported.
         * @param color the color of the graph background.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder withBackground(final ConstellationColor color) {
            this.backgroundColor = color;
            return this;
        }
           
        /**
         * Specifies if only selected Nodes and related connections are to be included in the output svg.
         * @param selectedNodesOnly
         * @return 
         */
        public SVGGraphBuilder withNodes(Boolean selectedNodesOnly) {
            this.selectedNodesOnly = selectedNodesOnly;
            return this;
        }
        
        /**
         * Controls whether connections are included or excluded from the SVG output file
         * @param showConnections
         * @return 
         */
        public SVGGraphBuilder includeConnections(Boolean showConnections) {
            this.showConnections = showConnections;
            return this;
        }
        
        /**
         * Controls whether Node top labels are included or excluded from the SVG output file
         * @param showTopLabels
         * @return 
         */
        public SVGGraphBuilder includeTopLabels(Boolean showTopLabels) {
            this.showTopLabels = showTopLabels;
            return this;
        }
        
        /**
         * Controls whether Node top labels are included or excluded from the SVG output file
         * @param showBottomLabels
         * @return 
         */
        public SVGGraphBuilder includeBottomLabels(Boolean showBottomLabels) {
            this.showBottomLabels = showBottomLabels;
            return this;
        }

        /**
         * Builds an SVGGraphObject representing the provided graph.
         * @return SVGData
         */
        public SVGData build() {
            final SVGObject svgGraph = SVGObject.loadFromTemplate(SVGFileNameConstant.LAYOUT);
            defineBoundary();
            buildHeader(svgGraph);
            buildFooter(svgGraph);
            buildBackground(svgGraph);
            buildConnections(svgGraph);
            buildNodes(svgGraph);
            setLayoutDimensions(svgGraph);
            return svgGraph.toSVGData();
        }       
        
        /**
         * Builds the header area of the output SVG.
         * @param svg 
         */
        private void buildHeader(final SVGObject svg){
            final SVGObject titleContainer = svg.getChild(SVGLayoutConstant.HEADER).getChild(SVGLayoutConstant.TITLE);
            titleContainer.toSVGData().setContent(graphTitle);
            final SVGObject subtitleContainer = svg.getChild(SVGLayoutConstant.HEADER).getChild(SVGLayoutConstant.SUBTITLE);
            final ZonedDateTime date = ZonedDateTime.now();
            subtitleContainer.toSVGData().setContent(
                    String.format("Exported: %s %s, %s",
                            StringUtilities.camelCase(date.getMonth().toString()), 
                            date.getDayOfMonth(), 
                            date.getYear()
                    )
            );
        }
        
        /**
         * Builds the footer area of the output SVG.
         * @param svg 
         */
        private void buildFooter(final SVGObject svg){
            final SVGObject titleContainer = svg.getChild(SVGLayoutConstant.FOOTER).getChild(SVGLayoutConstant.FOOTNOTE);
            titleContainer.toSVGData().setContent("The Constellation community. All rights reserved.");
        }
        
        private void buildBackground(final SVGObject svg){
            svg.getChild(SVGLayoutConstant.BACKGROUND).setFillColor(backgroundColor);
        }
        
        /**
         * Generates SVG Nodes from the graph and assigns them as children to the Nodes container.
         * The template file Node.svg is used to build the node.
         * @param svgGraph
         */
        private void buildNodes(final SVGObject svgGraph) {
            //Retrieve the svg element that holds the nodes as a SVGObject
            final SVGObject nodesContainer = svgGraph.getChild(SVGLayoutConstant.CONTENT).getChild(SVGLayoutConstant.NODES); 
            for (int vertexPosition = 0 ; vertexPosition < access.getVertexCount() ; vertexPosition++) {
                              
                //Do not export this vertex if only selected nodes are being exported and the node is not selected.
                if (selectedNodesOnly && !access.isVertexSelected(vertexPosition) || access.getVertexVisibility(vertexPosition) == 0){
                    continue;
                }
                
                //Get the values of the attributes relevent to the current node
                final Vector3f position = getVertexPosition(vertexPosition);
                final ConstellationColor color = access.getVertexColor(vertexPosition);
                final String bgi = access.getBackgroundIcon(vertexPosition);
                final String fgi = access.getForegroundIcon(vertexPosition);
                final ConstellationIcon backgroundIcon = IconManager.getIcon(bgi);
                final ConstellationIcon foregroundIcon = IconManager.getIcon(fgi);
                access.getBackgroundIcon(vertexPosition);
                
                //build the SVGobject representing the node
                final SVGObject node = SVGObject.loadFromTemplate(SVGFileNameConstant.NODE);
                node.setPosition(position.getX() - 128, position.getY() - 128);
                node.setID(access.getVertexId(vertexPosition));
                node.setParent(nodesContainer);
                
                //Add labels to the node
                if (showTopLabels){
                    final SVGObject topLabelContainer = node.getChild(SVGLayoutConstant.TOP_LABELS);
                    buildTopLabel(vertexPosition, topLabelContainer);
                }
                if (showBottomLabels){
                    final SVGObject bottomLabelContainer = node.getChild(SVGLayoutConstant.BOTTOM_LABELS);
                    buildBottomLabel(vertexPosition, bottomLabelContainer);
                }
                
                //Add images to the node
                final SVGObject nodeImages = node.getChild(SVGLayoutConstant.NODE_IMAGES);
                
                //Add dimmed property if dimmed
                //This implementation is not an precice sollution, lumocity to alpha conversion would be better
                if (access.isVertexDimmed(vertexPosition)){
                    nodeImages.setAttribute(SVGAttributeConstant.FILTER, "grayscale(1)");
                }
                
                final SVGObject backgroundContainer = nodeImages.getChild(SVGLayoutConstant.BACKGROUND_IMAGE);
                final SVGObject foregroundContainer = nodeImages.getChild(SVGLayoutConstant.FOREGROUND_IMAGE);
                final byte[] backgroundData = backgroundIcon.getIconData().getData(0, color.getJavaColor());
                final byte[] foregroundData = foregroundIcon.getIconData().getData();
                this.buildSVGImageFromRasterImageData(backgroundContainer, backgroundData);
                this.buildSVGImageFromRasterImageData(foregroundContainer, foregroundData);

                //Add decorators to the node       
                this.buildDecorator(nodeImages.getChild(SVGLayoutConstant.NORTH_WEST_DECORATOR), access.getNWDecorator(vertexPosition));
                this.buildDecorator(nodeImages.getChild(SVGLayoutConstant.NORTH_EAST_DECORATOR), access.getNEDecorator(vertexPosition));
                this.buildDecorator(nodeImages.getChild(SVGLayoutConstant.SOUTH_WEST_DECORATOR), access.getSWDecorator(vertexPosition));
                this.buildDecorator(nodeImages.getChild(SVGLayoutConstant.SOUTH_EAST_DECORATOR), access.getSEDecorator(vertexPosition));
            }
        }
        
        /**
         * Generates decorator images for nodes.
         * @param vertexAttributeReference
         * @param vertex
         * @param decoratorContainer 
         */
        private void buildDecorator(final SVGObject decorator, final String decoratorValue) {
            if (decoratorValue != null && !"false_pinned".equals(decoratorValue)){
                final byte[] decoratorIconData = IconManager.getIcon(decoratorValue).getIconData().getData();
                this.buildSVGImageFromRasterImageData(decorator, decoratorIconData);
            }
        }
        
        /**
         * Constructs bottom label SVG elements for a given vertex id in a given SVGContainer.
         * This method only considers the bottom label requirements for nodes.
         * This element of the output graph currently relies heavily on content overflow of parent elements
         * due to inabilities for parents to set their width and height based off of the width and height requirements of their children.
         * @param vertex
         * @param bottomLabelContainer 
         */
        private void buildBottomLabel(final int vertex, final SVGObject bottomLabelContainer){    
            float offset = 0;
            for (int labelPosition = 0; labelPosition < access.getBottomLabelCount(); labelPosition++) {
                final String labelString = access.getVertexBottomLabelText(vertex, labelPosition);
                if (labelString != null){
                    SVGObject text = SVGObject.loadFromTemplate(SVGFileNameConstant.LABEL);
                    final float size = access.getBottomLabelSize(labelPosition) * 64;
                    text.setAttribute(SVGAttributeConstant.FONT_SIZE,  size);
                    text.setAttribute(SVGAttributeConstant.Y, offset);
                    text.setFillColor(access.getBottomLabelColor(labelPosition));
                    text.setAttribute(SVGAttributeConstant.BASELINE, "hanging");
                    text.setAttribute(SVGAttributeConstant.ID, String.format("bottom-label-%s", labelPosition));
                    text.setContent(labelString);
                    text.setParent(bottomLabelContainer);
                    offset = offset + size;
                }
            }
        }
        
        /**
         * Constructs the top label SVG elements for a given vertex id in a given SVGContainer.
         * This method only considers the top label requirements for nodes.
         * This element of the output graph currently relies heavily on content overflow of parent elements
         * due to inabilities for parents to set their width and height based off of the width and height requirements of their children.
         * @param vertex
         * @param bottomLabelContainer 
         */
        private void buildTopLabel(final int vertex, final SVGObject topLabelContainer){
            float offset = 0;
            for (int labelPosition = 0; labelPosition < access.getTopLabelCount(); labelPosition++) {
                final String labelString = access.getVertexTopLabelText(vertex, labelPosition);
                if (labelString != null){
                    final SVGObject text = SVGObject.loadFromTemplate(SVGFileNameConstant.LABEL);
                    final float size = access.getTopLabelSize(labelPosition) * 64;
                    text.setAttribute(SVGAttributeConstant.FONT_SIZE,  size);
                    text.setAttribute(SVGAttributeConstant.Y, offset);
                    text.setFillColor(access.getTopLabelColor(labelPosition));
                    text.setAttribute(SVGAttributeConstant.BASELINE, "after-edge");
                    text.setAttribute(SVGAttributeConstant.ID, String.format("top-label-%s", labelPosition));
                    text.setContent(labelString);
                    text.setParent(topLabelContainer);
                    offset = offset - size;
                }
            }
        }
        
        /**
         * Builds SVG connections between Nodes.
         * Generates transactions, links and edges depending on connectionMode.
         * Other graph state factors including maxTransactions and drawFlags are considered.
         * @param svgGraph 
         */
        private void buildConnections(final SVGObject svgGraph) {
            //Donot export connections if showConnections is disabled
            if (!showConnections){
                return;
            }
            
            // Get the SVG element that will contain all connections
            final SVGObject connectionsContainer = svgGraph.getChild(SVGLayoutConstant.CONTENT).getChild(SVGLayoutConstant.CONNECTIONS);
            
            //Itterate over all connections in the gaph
            for (int linkPosition = 0; linkPosition < access.getLinkCount(); linkPosition++) {
                
                //Get the source and destination node references
                final int high =  access.getLinkHighVertex(linkPosition);
                final int low = access.getLinkLowVertex(linkPosition);
                
                //Do not export this link if only selected nodes are being exported and either of the associated nodes are not selected.
                if (selectedNodesOnly && (!access.isVertexSelected(high) || !access.isVertexSelected(low))){
                    continue;
                }
                
                //Determine the SVG coordinates of the center of the nodes
                final Vector3f highCenterPosition = getVertexPosition(high);
                final Vector3f lowCenterposition = getVertexPosition(low);
                
                //Get the SVG angle of the connection between the two nodes
                final double highConnectionAngle = calculateConnectionAngle(highCenterPosition, lowCenterposition);
                final double lowConnectionAngle = calculateConnectionAngle(lowCenterposition, highCenterPosition);

                //Get the coordinates of the points where the connections intersect the node radius
                final Vector3f highCircumferencePosition = offSetPosition(highCenterPosition, 128, highConnectionAngle);
                final Vector3f lowCircumferencePosition = offSetPosition(lowCenterposition, 128, lowConnectionAngle);
                
                //Itterate over all of the Transactions/Edges/Links between the two nodes.
                //Note: the linkConnectionCount factors in the connection mode and the max transaction threshold.
                for (int connectionPosition = 0; connectionPosition < access.getLinkConnectionCount(linkPosition); connectionPosition++){
                    
                    //Get the reference tothe current transaction/Edge/Link
                    final int connection = access.getLinkConnection(linkPosition, connectionPosition);

                    //Determine offset controlls for drawing multiple Transactions/Edges in paralell
                    final int paralellOffsetDistance = (connectionPosition / 2 + ((connectionPosition % 2 == 0) ? 0 : 1)) * 16;
                    final double paralellOffsetDirection = Math.pow(-1, connectionPosition);
                    final double paralellOffsetAngle = Math.toRadians(90) * paralellOffsetDirection;

                    //Determine the unique positions for the individual Transation/edge/link.
                    final Vector3f highPosition = offSetPosition(highCircumferencePosition, paralellOffsetDistance, highConnectionAngle - paralellOffsetAngle);
                    final Vector3f lowPosition = offSetPosition(lowCircumferencePosition, paralellOffsetDistance, lowConnectionAngle + paralellOffsetAngle);
                    
                    //Create the Transaction/Edge/Link SVGData
                    buildConnection(connectionsContainer, highPosition, lowPosition, connection);  
                } 
            }
        }
        
        /**
         * Builds a single SVG Transaction/Edge/Link between two points.
         * Generates transactions, links and edges depending on connectionMode.
         * Transaction/Edge/Link direction is considered to define end points and arrow heads.
         * @param connectionsContainer
         * @param highPosition
         * @param lowPosition
         * @param connection 
         */
        private void buildConnection(final SVGObject connectionsContainer, final Vector3f highPosition, final Vector3f lowPosition, final int connection){
            //Get references to SVG Objects being built within this method 
            final SVGObject connectionSVG = SVGObject.loadFromTemplate(SVGFileNameConstant.CONNECTION);
            final SVGObject arrowShaft = connectionSVG.getChild(SVGLayoutConstant.ARROW_SHAFT);
            final SVGObject highArrowHeadContainer;
            final SVGObject lowArrowHeadContainer;
            
            //Get the connection angles of the connection
            final Double highConnectionAngle = calculateConnectionAngle(highPosition, lowPosition);
            final Double lowConnectionAngle = calculateConnectionAngle(lowPosition, highPosition);
            
            //Get the coordinates of the potential shaft extremeties at 64px behind the arrow tip position.
            final Vector3f highPositionRecessed = offSetPosition(highPosition, 64, highConnectionAngle);
            final Vector3f lowPositionRecessed = offSetPosition(lowPosition, 64, lowConnectionAngle);

            //Assign the positional values of shaft and arrow head/s based on the direction of the Transaction/Edge/Link
            final ConnectionDirection direction = access.getConnectionDirection(connection);            
            switch (direction){
                
                //Bidirectional connectsions are Links with two link arrow heads
                case BIDIRECTED:
                    buildArrowShaft(arrowShaft, highPositionRecessed, lowPositionRecessed);
                    
                    highArrowHeadContainer = SVGObject.loadFromTemplate(SVGFileNameConstant.LINK_ARROW_HEAD);
                    buildArrowHead(highArrowHeadContainer, highPosition, highConnectionAngle);
                    highArrowHeadContainer.setParent(connectionSVG);
                    
                    lowArrowHeadContainer = SVGObject.loadFromTemplate(SVGFileNameConstant.LINK_ARROW_HEAD);
                    buildArrowHead(lowArrowHeadContainer, lowPosition, lowConnectionAngle);
                    lowArrowHeadContainer.setParent(connectionSVG);
                    break;
                
                //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head    
                case LOW_TO_HIGH:
                    buildArrowShaft(arrowShaft, highPositionRecessed, lowPosition);
                    
                    highArrowHeadContainer = SVGObject.loadFromTemplate(SVGFileNameConstant.TRANSACTION_ARROW_HEAD);
                    buildArrowHead(highArrowHeadContainer, highPosition, highConnectionAngle);
                    highArrowHeadContainer.setParent(connectionSVG);
                    break;
                   
                //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head
                case HIGH_TO_LOW:
                    buildArrowShaft(arrowShaft, highPosition, lowPositionRecessed); 
                    
                    lowArrowHeadContainer = SVGObject.loadFromTemplate(SVGFileNameConstant.TRANSACTION_ARROW_HEAD);
                    buildArrowHead(lowArrowHeadContainer, lowPosition, lowConnectionAngle);
                    lowArrowHeadContainer.setParent(connectionSVG);
                    break;
                
                //Undirected connections are Transactions, Edges and Links with no arrow heads.
                default:
                    buildArrowShaft(arrowShaft, highPosition, lowPosition);
                    break;
            }
            
            //Set the attributes of the connection and add it to the connections Conatainer  
            final ConstellationColor color = getConnectionColor(connection);
            connectionSVG.setID(String.format("Connection_%s_%s", highPosition, lowPosition));
            connectionSVG.setFillColor(color);
            connectionSVG.setStrokeColor(color);
            connectionSVG.setStrokeStyle(access.getConnectionLineStyle(connection));
            connectionSVG.setParent(connectionsContainer);
        }
        
        /**
         * Manipulates an arrow head container to adjust it's position and rotation.
         * @param arrowHeadContainer
         * @param x
         * @param y
         * @param connectionAngle 
         */
        private void buildArrowHead(final SVGObject arrowHeadContainer, final Vector3f position, final double connectionAngle) {
            
            //The size of the svgElement containing the arrow head polygon asset
            final int arrowHeadWidth = 128;
            final int arrowHeadheight = 32;
            
            //Set arrow head svg attributes
            arrowHeadContainer.setPosition(position.getX() - arrowHeadWidth, position.getY() - arrowHeadheight / 2);
            arrowHeadContainer.setID(String.format("arrow-head-%s-%s", position.getX(), position.getY()));
            
            //Rotate the arrow head polygon around the tip to align it with the angle of the connection
            final SVGObject arrowHead = arrowHeadContainer.getChild(SVGLayoutConstant.ARROW_HEAD);
            arrowHead.setTransformation(String.format("rotate(%s %s %s)", Math.toDegrees(connectionAngle), arrowHeadWidth, arrowHeadheight/2));
        }
        
        /**
         * Manipulates an arrow shaft container to adjust it's position.
         * @param arrowShaft
         * @param sourcePosition
         * @param destinationPosition 
         */
        private void buildArrowShaft(SVGObject arrowShaft, Vector3f sourcePosition, Vector3f destinationPosition) {
            arrowShaft.setSourcePosition(sourcePosition);
            arrowShaft.setDestinationPosition(destinationPosition);
        }
        
        /**
         * Builds an image element within an SVG element.
         * @param parent
         * @param data 
         */
        private void buildSVGImageFromRasterImageData(final SVGObject parent, final byte[] data) {
            final String encodedString = Base64.getEncoder().encodeToString(data);
            final SVGData image = SVGData.loadFromTemplate(SVGFileNameConstant.IMAGE);
            image.setAttribute(SVGAttributeConstant.EXTERNAL_RESOURCE_REFERENCE, String.format("data:image/png;base64,%s", encodedString));
            image.setParent(parent.toSVGData());
        }
 
        /**
         * Retrieves the min and max values for x and y for a given graph. 
         * Represents the extremities of the graph area that contains render-able objects.
         * @param graph
         * @return 
         */
        private void defineBoundary() {
            Camera camera = access.getCamera();
            final BoundingBox box = camera.boundingBox;
            CameraUtilities.rotate(camera, 0, 0, 0);
            BoundingBoxUtilities.recalculateFromGraph(box, graph, selectedNodesOnly);
            maxBound = box.getMax();
            minBound = box.getMin();
            maxBound.scale(128);
            minBound.scale(128);
        }

        /**
         * Sets the dimensions for container objects within the Layout.svg template file.
         * This method is a temporary solution for setting layout dimensions semi-manually.
         * Ideally layout elements will be built from a top down approach.
         * In doing so each layout object will be able to set its own dimensions 
         * based on it's own content.
         * @param svg 
         */
        private void setLayoutDimensions(final SVGObject svg) {
            final float contentWidth = maxBound.getX() - minBound.getX() + 256;
            final float contentHeight = maxBound.getY() - minBound.getY() + 256;
            final float xMargin = 50.0F;
            final float topMargin = 288.0F;
            final float bottomMargin = 128.0F;
            final float xPadding = 250.0F;
            final float yPadding = 250.0F;
            final float footerYOffset = topMargin + contentHeight + (yPadding * 2);            
            final float fullWidth = (xMargin * 2) + contentWidth + (xPadding * 2);            
            final float fullHeight = (topMargin + bottomMargin) + contentHeight + (yPadding * 2);            
            final float backgroundWidth = contentWidth + (xPadding * 2);
            final float backgroundHeight = contentHeight + (yPadding * 2);
            final float contentYOffset = topMargin + yPadding;
            final float contentXOffset = xMargin + xPadding;
            
            svg.setDimension(fullWidth, fullHeight);

            svg.getChild(SVGLayoutConstant.HEADER).setDimension(fullWidth, topMargin);
            svg.getChild(SVGLayoutConstant.FOOTER).setDimension(fullWidth, bottomMargin);
            svg.getChild(SVGLayoutConstant.FOOTER).setPosition(0F, footerYOffset);

            svg.getChild(SVGLayoutConstant.CONTENT).setDimension(contentWidth, contentHeight);
            svg.getChild(SVGLayoutConstant.CONTENT).setPosition(contentXOffset, contentYOffset);

            svg.getChild(SVGLayoutConstant.BACKGROUND).setDimension(backgroundWidth, backgroundHeight);
            svg.getChild(SVGLayoutConstant.BACKGROUND).setPosition(xMargin, topMargin);
            svg.getChild(SVGLayoutConstant.BORDER).setDimension(fullWidth, fullHeight);
        }

        /**
         * Gets the position of the vertex.
         * Position is normalised with respect to the position of the top-left most vertex.
         * Position is with respect to the center of the vertex.
         * @param vertex
         * @return 
         */
        private Vector3f getVertexPosition(final int vertex) {           
            final Float constelationGraphX = access.getX(vertex);
            final Float constelationGraphY = access.getY(vertex);
            final Float constelationGraphZ = access.getZ(vertex);
            
            final int halfVertexSize = 128;
            
            final Float svgGraphX = (constelationGraphX * halfVertexSize) - minBound.getX() + halfVertexSize;
            final Float svgGraphY = (maxBound.getY() - minBound.getY()) - ((constelationGraphY * halfVertexSize) - minBound.getY()) + halfVertexSize;
            final Float svgGraphZ = (constelationGraphZ * halfVertexSize) - minBound.getZ() + halfVertexSize;
            
            return new Vector3f(svgGraphX, svgGraphY, svgGraphZ);
        }
        
        /**
         * Calculates the angle at which a connection touches a node.
         * Return value is clockwise from a horizontal x axis with positive values to the right.
         * @param sourcePosition
         * @param destinationPosition
         * @return 
         */
        private double calculateConnectionAngle(final Vector3f sourcePosition, final Vector3f destinationPosition) {
            final float xDirectionVector = sourcePosition.getX() - destinationPosition.getX();
            final float yDirectionVector = sourcePosition.getY() - destinationPosition.getY();
            return Math.atan2(yDirectionVector, xDirectionVector);
        }

        /**
         * Calculates the coordinates of a position located a fixed distance and angle from an origin.
         * @param origin
         * @param distance
         * @param angle
         * @return 
         */
        private Vector3f offSetPosition(final Vector3f origin, final int distance, final double angle) {
            final float x = (float) (origin.getX() - (distance * Math.cos(angle)));
            final float y = (float) (origin.getY() - (distance * Math.sin(angle)));
            final float z = origin.getZ();
            return new Vector3f(x,y,z);
        }

        /**
         * Determines the color of a connection.
         * Handles connection dimming and multiple Transaction color values for Edges and Links
         * @param connection
         * @return 
         */
        private ConstellationColor getConnectionColor(final int connection) {
            final ConstellationColor color;
            if (access.isConnectionDimmed(connection)){
                color = VisualGraphDefaults.DEFAULT_TRANSACTION_COLOR;
            } else {
                color = access.getConnectionColor(connection);
            }
            return color;
        }
    }
}
