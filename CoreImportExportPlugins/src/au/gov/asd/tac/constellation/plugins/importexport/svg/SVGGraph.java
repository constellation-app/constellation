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
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGAttributeConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGLayoutConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGFileNameConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.parser.SVGParser;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.time.ZonedDateTime;
import java.util.logging.Level;
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
        private GraphVisualAccess access;
        private GraphReadMethods graph;
        private Vector3f maxBound = null;
        private Vector3f minBound = null;
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
            
            final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
            access = new GraphVisualAccess(currentGraph);
            access.beginUpdate();
            access.updateInternally();

            defineBoundary();
            buildHeader(svgGraphLayout);
            buildFooter(svgGraphLayout);
            buildConnections(svgGraphLayout);
            buildNodes(svgGraphLayout);
            setLayoutDimensions(svgGraphLayout);
            access.endUpdate();
            return svgGraphLayout.toSVGObject();
        }       
        
        /**
         * Builds the header area of the output SVG.
         * @param svg 
         */
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
        
        /**
         * Builds the footer area of the output SVG.
         * @param svg 
         */
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

            for (int vertexPosition = 0 ; vertexPosition < access.getVertexCount() ; vertexPosition++) {
                //Get the values of the attributes relevent to the current node
                final Tuple<Double, Double> position = getVertexPosition(vertexPosition);
                final ConstellationColor color = access.getVertexColor(vertexPosition);
                final String bgi = access.getBackgroundIcon(vertexPosition);
                final String fgi = access.getForegroundIcon(vertexPosition);
                final ConstellationIcon backgroundIcon = IconManager.getIcon(bgi);
                final ConstellationIcon foregroundIcon = IconManager.getIcon(fgi);
                access.getBackgroundIcon(vertexPosition);
                
                //build the SVGobject representing the node
                final SVGObject node = buildSVGObjectFromTemplate(SVGFileNameConstant.NODE);
                node.setAttribute(SVGAttributeConstant.X.getKey(), position.getFirst() - 128);
                node.setAttribute(SVGAttributeConstant.Y.getKey(), position.getSecond() - 128);
                node.setAttribute(SVGAttributeConstant.ID.getKey(), access.getVertexId(vertexPosition));
                node.setParent(nodesContainer.toSVGObject());
                
                //Add labels to the Node
                final SVGContainer nodeContainer = new SVGContainer(node);
                final SVGContainer bottomLabelContainer = nodeContainer.getContainer(SVGLayoutConstant.BOTTOM_LABELS.id);
                final SVGContainer topLabelContainer = nodeContainer.getContainer(SVGLayoutConstant.TOP_LABELS.id);
                buildBottomLabel(vertexPosition, bottomLabelContainer);
                buildTopLabel(vertexPosition, topLabelContainer);
  
                //Add images to the node
                final SVGContainer backgroundContainer = nodeContainer.getContainer(SVGLayoutConstant.BACKGROUND_IMAGE.id);
                final SVGContainer foregroundContainer = nodeContainer.getContainer(SVGLayoutConstant.FOREGROUND_IMAGE.id);
                final byte[] backgroundData = backgroundIcon.getIconData().getData(0, color.getJavaColor());
                final byte[] foregroundData = foregroundIcon.getIconData().getData();
                this.buildSVGImageFromRasterImageData(backgroundContainer.toSVGObject(), backgroundData);
                this.buildSVGImageFromRasterImageData(foregroundContainer.toSVGObject(), foregroundData);
                
                //Add decorators to the node       
                
                this.buildDecorator(nodeContainer.getContainer(SVGLayoutConstant.NORTH_WEST_DECORATOR.id), access.getNWDecorator(vertexPosition));
                this.buildDecorator(nodeContainer.getContainer(SVGLayoutConstant.NORTH_EAST_DECORATOR.id), access.getNEDecorator(vertexPosition));
                this.buildDecorator(nodeContainer.getContainer(SVGLayoutConstant.SOUTH_WEST_DECORATOR.id), access.getSWDecorator(vertexPosition));
                this.buildDecorator(nodeContainer.getContainer(SVGLayoutConstant.SOUTH_EAST_DECORATOR.id), access.getSEDecorator(vertexPosition));
            }
        }
        
        /**
         * Generates decorator images for nodes.
         * @param vertexAttributeReference
         * @param vertex
         * @param decoratorContainer 
         */
        private void buildDecorator(final SVGContainer decoratorContainer, final String decoratorValue) {
            if (decoratorValue != null && !"false_pinned".equals(decoratorValue)){
                final byte[] decoratorIconData = IconManager.getIcon(decoratorValue).getIconData().getData();
                this.buildSVGImageFromRasterImageData(decoratorContainer.toSVGObject(), decoratorIconData);
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
        private void buildBottomLabel(final int vertex, final SVGContainer bottomLabelContainer){    
            float offset = 0;
            for (int labelPosition = 0; labelPosition < access.getBottomLabelCount(); labelPosition++) {
                final String labelString = access.getVertexBottomLabelText(vertex, labelPosition);
                if (labelString != null){
                    SVGObject text = buildSVGObjectFromTemplate(SVGFileNameConstant.BOTTOM_LABEL);
                    final float size = access.getBottomLabelSize(labelPosition) * 64;
                    final ConstellationColor color = access.getBottomLabelColor(labelPosition);
                    text.setAttribute(SVGAttributeConstant.FONT_SIZE.getKey(),  size);
                    text.setAttribute(SVGAttributeConstant.Y.getKey(), offset);
                    text.setAttribute(SVGAttributeConstant.FILL_COLOR.getKey(), color.getHtmlColor());
                    text.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("bottom-label-%s", labelPosition));
                    text.setContent(SVGParser.sanitisePlanText(labelString));
                    text.setParent(bottomLabelContainer.toSVGObject());
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
        private void buildTopLabel(final int vertex, final SVGContainer topLabelContainer){
            float offset = 0;
            for (int labelPosition = 0; labelPosition < access.getTopLabelCount(); labelPosition++) {
                final String labelString = access.getVertexTopLabelText(vertex, labelPosition);
                if (labelString != null){
                    final SVGObject text = buildSVGObjectFromTemplate(SVGFileNameConstant.TOP_LABEL);
                    final float size = access.getTopLabelSize(labelPosition) * 64;
                    final ConstellationColor color = access.getTopLabelColor(labelPosition);
                    text.setAttribute(SVGAttributeConstant.FONT_SIZE.getKey(),  size);
                    text.setAttribute(SVGAttributeConstant.Y.getKey(), offset);
                    text.setAttribute(SVGAttributeConstant.FILL_COLOR.getKey(), color.getHtmlColor());
                    text.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("top-label-%s", labelPosition));
                    text.setContent(SVGParser.sanitisePlanText(labelString));
                    text.setParent(topLabelContainer.toSVGObject());
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
        private void buildConnections(final SVGGraph svgGraph) {
            // Get the SVG element that will contain all connections
            final SVGContainer connectionsContainer = svgGraph.getContainer(SVGLayoutConstant.CONTENT.id).getContainer(SVGLayoutConstant.CONNECTIONS.id);
            
            //Itterate over all connections in the gaph
            for (int linkPosition = 0; linkPosition < access.getLinkCount(); linkPosition++) {
                
                //Get the source and destination node references
                final int high =  access.getLinkHighVertex(linkPosition);
                final int low = access.getLinkLowVertex(linkPosition);
                
                //Determine the SVG coordinates of the center of the nodes
                final Tuple<Double, Double> highCenterPosition = getVertexPosition(high);
                final Tuple<Double, Double> lowCenterposition = getVertexPosition(low);
                
                //Get the SVG angle of the connection between the two nodes
                final Double highConnectionAngle = calculateConnectionAngle(highCenterPosition, lowCenterposition);
                final Double lowConnectionAngle = calculateConnectionAngle(lowCenterposition, highCenterPosition);

                //Get the coordinates of the points where the connections intersect the node radius
                final Tuple<Double, Double> highCircumferencePosition = offSetPosition(highCenterPosition, 128, highConnectionAngle);
                final Tuple<Double, Double> lowCircumferencePosition = offSetPosition(lowCenterposition, 128, lowConnectionAngle);
                
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
                    final Tuple<Double, Double> highPosition = offSetPosition(highCircumferencePosition, paralellOffsetDistance, highConnectionAngle - paralellOffsetAngle);
                    final Tuple<Double, Double> lowPosition = offSetPosition(lowCircumferencePosition, paralellOffsetDistance, lowConnectionAngle + paralellOffsetAngle);
                    
                    //Create the Transaction/Edge/Link SVGObject
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
        private void buildConnection(final SVGContainer connectionsContainer, final Tuple<Double, Double> highPosition, final Tuple<Double, Double> lowPosition, final int connection){
            //Get references to SVG Objects being built within this method 
            final SVGObject connectionSVG = buildSVGObjectFromTemplate(SVGFileNameConstant.CONNECTION);
            final SVGObject arrowShaft = connectionSVG.getChild(SVGLayoutConstant.ARROW_SHAFT.id);
            final SVGObject highArrowHeadContainer;
            final SVGObject lowArrowHeadContainer;
            
            //Get the connection angles of the connection
            final Double highConnectionAngle = calculateConnectionAngle(highPosition, lowPosition);
            final Double lowConnectionAngle = calculateConnectionAngle(lowPosition, highPosition);
            
            //Get the coordinates of the potential shaft extremeties at 64px behind the arrow tip position.
            final Tuple<Double, Double> highPositionRecessed = offSetPosition(highPosition, 64, highConnectionAngle);
            final Tuple<Double, Double> lowPositionRecessed = offSetPosition(lowPosition, 64, lowConnectionAngle);

            //Assign the positional values of shaft and arrow head/s based on the direction of the Transaction/Edge/Link
            final ConnectionDirection direction = access.getConnectionDirection(connection);            
            switch (direction){
                
                //Bidirectional connectsions are Links with two link arrow heads
                case BIDIRECTED:
                    buildArrowShaft(arrowShaft, highPositionRecessed, lowPositionRecessed);
                    
                    highArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.LINK_ARROW_HEAD);
                    buildArrowHead(highArrowHeadContainer, highPosition, highConnectionAngle);
                    highArrowHeadContainer.setParent(connectionSVG);
                    
                    lowArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.LINK_ARROW_HEAD);
                    buildArrowHead(lowArrowHeadContainer, lowPosition, lowConnectionAngle);
                    lowArrowHeadContainer.setParent(connectionSVG);
                    break;
                
                //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head    
                case LOW_TO_HIGH:
                    buildArrowShaft(arrowShaft, highPositionRecessed, lowPosition);
                    
                    highArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.TRANSACTION_ARROW_HEAD);
                    buildArrowHead(highArrowHeadContainer, highPosition, highConnectionAngle);
                    highArrowHeadContainer.setParent(connectionSVG);
                    break;
                   
                //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head
                case HIGH_TO_LOW:
                    buildArrowShaft(arrowShaft, highPosition, lowPositionRecessed); 
                    
                    lowArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.TRANSACTION_ARROW_HEAD);
                    buildArrowHead(lowArrowHeadContainer, lowPosition, lowConnectionAngle);
                    lowArrowHeadContainer.setParent(connectionSVG);
                    break;
                
                //Undirected connections are Transactions, Edges and Links with no arrow heads.
                default:
                    buildArrowShaft(arrowShaft, highPosition, lowPosition);
                    break;
            }
            
            //Set the attributes of the connection and add it to the connections Conatainer  
            final String color = getConnectionColor(connection);
            connectionSVG.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("Connection_%s_%s", highPosition, lowPosition));
            connectionSVG.setAttribute(SVGAttributeConstant.FILL_COLOR.getKey(), color);
            connectionSVG.setAttribute(SVGAttributeConstant.STROKE_COLOR.getKey(), color);
            connectionSVG.setParent(connectionsContainer.toSVGObject());
        }
        
        /**
         * Manipulates an arrow head container to adjust it's position and rotation.
         * @param arrowHeadContainer
         * @param x
         * @param y
         * @param connectionAngle 
         */
        private void buildArrowHead(final SVGObject arrowHeadContainer, final Tuple<Double,Double> position, final Double connectionAngle) {
            
            //The size of the svgElement containing the arrow head polygon asset
            final int arrowHeadWidth = 128;
            final int arrowHeadheight = 32;
            
            //Set arrow head svg attributes
            arrowHeadContainer.setAttribute(SVGAttributeConstant.X.getKey(), position.getFirst() - arrowHeadWidth);
            arrowHeadContainer.setAttribute(SVGAttributeConstant.Y.getKey(), position.getSecond() - arrowHeadheight / 2);
            arrowHeadContainer.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("arrow-head-%s-%s", position.getFirst(), position.getSecond()));
            
            //Rotate the arrow head polygon around the tip to align it with the angle of the connection
            final SVGObject arrowHead = arrowHeadContainer.getChild("arrow-head");
            arrowHead.setAttribute(SVGAttributeConstant.TRANSFORM.getKey(), String.format("rotate(%s %s %s)", Math.toDegrees(connectionAngle), arrowHeadWidth, arrowHeadheight/2));
        }
        
        /**
         * Manipulates an arrow shaft container to adjust it's position.
         * @param arrowShaft
         * @param sourcePosition
         * @param destinationPosition 
         */
        private void buildArrowShaft(SVGObject arrowShaft, Tuple<Double,Double> sourcePosition, Tuple<Double,Double> destinationPosition) {
            arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_X.getKey(), sourcePosition.getFirst());
            arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_Y.getKey(), sourcePosition.getSecond());
            arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_X.getKey(), destinationPosition.getFirst());
            arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_Y.getKey(), destinationPosition.getSecond());
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
                LOGGER.log(Level.INFO, ex.getLocalizedMessage());
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
        private void defineBoundary() {
            Camera camera = access.getCamera();
            final BoundingBox box = camera.boundingBox;
            BoundingBoxUtilities.recalculateFromGraph(box, graph, false);
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
        private void setLayoutDimensions(final SVGGraph svg) {
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
            
            svg.setDimensions(fullWidth, fullHeight);

            svg.getContainer(SVGLayoutConstant.HEADER.id).setDimension(fullWidth, topMargin);
            svg.getContainer(SVGLayoutConstant.FOOTER.id).setDimension(fullWidth, bottomMargin);
            svg.getContainer(SVGLayoutConstant.FOOTER.id).setPosition(0F, footerYOffset);

            svg.getContainer(SVGLayoutConstant.CONTENT.id).setDimension(contentWidth, contentHeight);
            svg.getContainer(SVGLayoutConstant.CONTENT.id).setPosition(contentXOffset, contentYOffset);

            svg.getContainer(SVGLayoutConstant.BACKGROUND.id).setDimension(backgroundWidth, backgroundHeight);
            svg.getContainer(SVGLayoutConstant.BACKGROUND.id).setPosition(xMargin, topMargin);
            svg.getContainer(SVGLayoutConstant.BORDER.id).setDimension(fullWidth, fullHeight);
        }

        /**
         * Gets the position of the vertex.
         * Position is normalised with respect to the position of the top-left most vertex.
         * Position is with respect to the center of the vertex.
         * @param vertex
         * @return 
         */
        private Tuple<Double, Double> getVertexPosition(final int vertex) {           
            final Float constelationGraphX = access.getX(vertex);
            final Float constelationGraphY = access.getY(vertex);
            final Float constelationGraphZ = access.getZ(vertex);
            
            final int halfVertexSize = 128;
            
            final Float svgGraphX = (constelationGraphX * halfVertexSize) - minBound.getX() + halfVertexSize;
            final Float svgGraphY = (maxBound.getY() - minBound.getY()) - ((constelationGraphY * halfVertexSize) - minBound.getY()) + halfVertexSize;
            final Float svgGraphZ = (constelationGraphZ * halfVertexSize) - minBound.getZ() + halfVertexSize;
            
            return new Tuple<>(svgGraphX.doubleValue(), svgGraphY.doubleValue());
        }
        
        /**
         * Calculates the angle at which a connection touches a node.
         * Return value is clockwise from a horizontal x axis with positive values to the right.
         * @param sourcePosition
         * @param destinationPosition
         * @return 
         */
        private Double calculateConnectionAngle(final Tuple<Double, Double> sourcePosition, final Tuple<Double, Double> destinationPosition) {
            final Double xDirectionVector = sourcePosition.getFirst() - destinationPosition.getFirst();
            final Double yDirectionVector = sourcePosition.getSecond() - destinationPosition.getSecond();
            return Math.atan2(yDirectionVector, xDirectionVector);
        }

        /**
         * Calculates the coordinates of a position located a fixed distance and angle from an origin.
         * @param origin
         * @param distance
         * @param angle
         * @return 
         */
        private Tuple<Double, Double> offSetPosition(final Tuple<Double, Double> origin, final int distance, final Double angle) {
            final Double x = origin.getFirst() - (distance * Math.cos(angle));
            final Double y = origin.getSecond() - (distance * Math.sin(angle));
            return new Tuple<>(x,y);
        }

        /**
         * Determines the color of a connection.
         * Handles connection dimming and multiple Transaction color values for Edges and Links
         * @param connection
         * @return 
         */
        private String getConnectionColor(final int connection) {
            final ConstellationColor color;
            if (access.isConnectionDimmed(connection)){
                color = VisualGraphDefaults.DEFAULT_TRANSACTION_COLOR;
            } else {
                color = access.getConnectionColor(connection);
            }
            return color.getHtmlColor();
        }
    }
}
