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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGAttributeConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGLayoutConstant;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
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
        private GraphVisualAccess access;
        private GraphReadMethods graph;
        private Vector3f maxBound = null;
        private Vector3f minBound = null;
        private String graphTitle = null;
        private ConnectionMode connectionMode = VisualGraphDefaults.DEFAULT_CONNECTION_MODE;
      
        /**
         * Specifies the graph to build the SVG from.
         * @param graph The graph to be exported.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder withGraph(final GraphReadMethods graph) {
            this.graph = graph;
            return this;
        }
        public SVGGraphBuilder withConnectionMode(final ConnectionMode connectionMode){
            this.connectionMode = connectionMode;
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
                final Tuple<Double, Double> position = getVertexPosition(vertexID);
                final String fillColor = graph.getStringValue(fillColorAttributeID, vertexID);
                final String htmlColor = ConstellationColor.getColorValue(fillColor).getHtmlColor();
                final Color color = Color.decode(htmlColor);
                final ConstellationIcon backgroundIcon = graph.getObjectValue(backgroundIconID, vertexID);
                final ConstellationIcon foregroundIcon = graph.getObjectValue(foregroundIconID, vertexID);

                //build the SVGobject representing the node
                final SVGObject node = buildSVGObjectFromTemplate(SVGFileNameConstant.NODE);
                node.setAttribute(SVGAttributeConstant.X.getKey(), position.getFirst() - 128);
                node.setAttribute(SVGAttributeConstant.Y.getKey(), position.getSecond() - 128);
                node.setAttribute(SVGAttributeConstant.ID.getKey(), vertexID);
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
        
        /**
         * Builds SVG connections between Nodes.
         * Generates transactions, links and edges depending on connection mode.
         * Other graph state factors including maxTransactions and drawFlags are considered.
         * @param svgGraph 
         */
        private void buildConnections(SVGGraph svgGraph) {
            // Get the SVG element that will contain all connections
            final SVGContainer connectionsContainer = svgGraph.getContainer(SVGLayoutConstant.CONTENT.id).getContainer(SVGLayoutConstant.CONNECTIONS.id);

            //Itterate over all links in the gaph
            final int linkCount = graph.getLinkCount();
            for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
                final int linkID = graph.getLink(linkPosition);
                
                // Build the relevent connection depending on the connection mode and applicalbel edge cases.
                switch (connectionMode){
                    case TRANSACTION:
                        final int maxTransactionsAttributeID = VisualConcept.GraphAttribute.MAX_TRANSACTIONS.get(graph);
                        final int maxTransactions = graph.getIntValue(maxTransactionsAttributeID, 0);
                        if (graph.getLinkTransactionCount(linkID) > maxTransactions){
                            // The connection mode is in TRANSACTION mode but there are more transactions than the maximum transactions
                            // so render as a an edge connection.
                            buildEdges(connectionsContainer, linkID);
                        } else {
                            // The connection mode is in TRANSACTION mode and there are less transactions than the maximum transactions.
                            buildTransactions(connectionsContainer, linkID);
                        }
                        break;
                        
                    case EDGE:
                        buildEdges(connectionsContainer, linkID);
                        break;
                        
                    case LINK:
                        //Determine the number of directed edges in the current link
                        int directedEdgeCount = 0;
                        final int linkEdgeCount = graph.getLinkEdgeCount(linkID);
                        for (int position = 0; position < linkEdgeCount; position++){
                            int edgeID = graph.getLinkEdge(linkID, position);
                            //Count the edge if its is not undirected
                            if (graph.getEdgeDirection(edgeID) != Graph.FLAT){
                                directedEdgeCount++;
                            }
                        }
                        
                        if (directedEdgeCount == 2){
                           // Two directed edges were found so render as a double link 
                           buildDoubleLink(connectionsContainer, linkID);
                        } else {
                            // less than two directed edges were found so render as a single link 
                            buildSingleLink(connectionsContainer, linkID);
                        }      
                        break;
                }
            }
        }

        /**
         * Creates a SVGObject representing a Link.
         * In this context, a link is a connection between nodes 
         * that contains two directed edges.
         * The template file Connection.svg is used to build the connection.
         * The template file LinkArrowHead.svg will be used to build the arrow heads.
         * @param connectionsContainer
         * @param linkID
         */
        private void buildDoubleLink(final SVGContainer connectionsContainer, int linkID) {
            final int sourceVxId = graph.getLinkHighVertex(linkID);
            final int destinationVxId = graph.getLinkLowVertex(linkID); 

            //Get the coordinates of the source and destination node.
            final Tuple<Double, Double> sourcePosition = getVertexPosition(sourceVxId);
            final Tuple<Double, Double> destinationPosition = getVertexPosition(destinationVxId);

            //get the connection angles of the transactions
            final Double sourceConnectionAngle = calculateConnectionAngle(sourcePosition, destinationPosition);
            final Double destinationConnectionAngle = calculateConnectionAngle(destinationPosition, sourcePosition);

            //get the coordinates of the points where the transactions intersect the radius around the node that equal to half it's size.
            final Tuple<Double, Double> sourceCircumferencePosition = offSetPosition(sourcePosition, 128, sourceConnectionAngle);
            final Tuple<Double, Double> destinationCircumferencePosition = offSetPosition(destinationPosition, 128, destinationConnectionAngle);
            
            //Set the arrow head position to the circumference point
            final Tuple<Double, Double> sourceArrowHeadPosition = sourceCircumferencePosition;
            final Tuple<Double, Double> destinationArrowHeadPosition = destinationCircumferencePosition;
            
            //Set the shaft estremeties to 64px behind the arrow position.
            final Tuple<Double, Double> shaftSourcePosition = offSetPosition(sourceArrowHeadPosition, 64, sourceConnectionAngle);
            final Tuple<Double, Double> shaftDestinationPosition = offSetPosition(destinationArrowHeadPosition, 64, destinationConnectionAngle);

            //Construct the connection
            final SVGObject connection = buildSVGObjectFromTemplate(SVGFileNameConstant.CONNECTION);
            final SVGObject arrowShaft = connection.getChild("arrow-shaft");
            arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_X.getKey(), shaftSourcePosition.getFirst());
            arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_Y.getKey(), shaftSourcePosition.getSecond());
            arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_X.getKey(), shaftDestinationPosition.getFirst());
            arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_Y.getKey(), shaftDestinationPosition.getSecond());

            connection.setAttribute(SVGAttributeConstant.ID.getKey(), linkID);
            connection.setParent(connectionsContainer.toSVGObject());

            final SVGObject sourceLinkArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.LINK_ARROW_HEAD);
            buildArrowHead(sourceLinkArrowHeadContainer, sourceArrowHeadPosition, sourceConnectionAngle);
            sourceLinkArrowHeadContainer.setParent(connection);

            final SVGObject destinationLinkArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.LINK_ARROW_HEAD);
            buildArrowHead(destinationLinkArrowHeadContainer, destinationArrowHeadPosition, destinationConnectionAngle);
            destinationLinkArrowHeadContainer.setParent(connection);
        } 
        
        /**
         * Creates a SVGObjects representing an Link.
         * In this context, a link is a connection between nodes 
         * that does not contain two directed edges.
         * The template file Connection.svg is used to build the edge.
         * The template file TransactionArrowHead.svg will be used to build the arrow head.
         * @param connectionsContainer
         * @param linkID
         */
        private void buildSingleLink(final SVGContainer ConnectionsContainer, int linkID) {
            final int sourceVxId = graph.getLinkHighVertex(linkID);
            final int destinationVxId = graph.getLinkLowVertex(linkID); 
            final int colorAttributeID = VisualConcept.TransactionAttribute.COLOR.get(graph);

            //Get the coordinates of the source and destination node.
            final Tuple<Double, Double> sourcePosition = getVertexPosition(sourceVxId);
            final Tuple<Double, Double> destinationPosition = getVertexPosition(destinationVxId);

            //get the connection angles of the transactions
            final Double sourceConnectionAngle = calculateConnectionAngle(sourcePosition, destinationPosition);
            final Double destinationConnectionAngle = calculateConnectionAngle(destinationPosition, sourcePosition);

            //get the coordinates of the points where the transactions intersect the radius around the node that equal to half it's size.
            final Tuple<Double, Double> sourceCircumferencePosition = offSetPosition(sourcePosition, 128, sourceConnectionAngle);
            final Tuple<Double, Double> destinationCircumferencePosition = offSetPosition(destinationPosition, 128, destinationConnectionAngle);
            
            //Extract needed information from edges.
            int edgeDirection = Graph.NOT_FOUND;
            final String htmlColor;
            final int edgeCount = graph.getLinkEdgeCount(linkID);
            
            //Only one edge exists to the color and direction can be determined from that edge
            if (edgeCount == 1){
                final int edgeID = graph.getLinkEdge(linkID, 0);
                edgeDirection = graph.getEdgeDirection(edgeID); 
                if (graph.getEdgeTransactionCount(edgeID) != 1){
                    htmlColor = ConstellationColor.WHITE.getHtmlColor();
                } else {
                    final int transactionID = graph.getEdgeTransaction(edgeID, 0);
                    final String fillColor = graph.getStringValue(colorAttributeID, transactionID);
                    htmlColor = ConstellationColor.getColorValue(fillColor).getHtmlColor();
                }
            
            //More than one edge exists to color and direction must factor both
            } else {
                for (int edgePosition = 0 ; edgePosition < edgeCount; edgePosition++) {
                    final int edgeID = graph.getLinkEdge(linkID, edgePosition); 
                    if (graph.getEdgeDirection(edgeID)!= Graph.FLAT){
                        edgeDirection = graph.getEdgeDirection(edgeID);
                    }
                }
                htmlColor = ConstellationColor.WHITE.getHtmlColor();
            }

            //Determine arrow components connection angles and positions
            final Tuple<Double, Double> arrowHeadPosition;
            final Double arrowHeadConnectionAngle;
            Tuple<Double, Double> shaftSourcePosition = sourceCircumferencePosition;
            Tuple<Double, Double> shaftDestinationPosition = destinationCircumferencePosition;
            
            switch (edgeDirection) {
                case Graph.UPHILL:
                    //Source node has the arrow head
                    arrowHeadPosition = shaftSourcePosition;
                    arrowHeadConnectionAngle = sourceConnectionAngle;
                    shaftSourcePosition = offSetPosition(arrowHeadPosition, 64, sourceConnectionAngle);
                    break;
                case Graph.DOWNHILL:
                    //Destination node has the arrow head
                    arrowHeadPosition = shaftDestinationPosition;
                    arrowHeadConnectionAngle = destinationConnectionAngle;
                    shaftDestinationPosition = offSetPosition(arrowHeadPosition, 64, destinationConnectionAngle);
                    break;
                default:
                    //Undirected graphs have no arrow head
                    //Values set to 0 to avoid errors, however arrow heads are not rendered to the screen in this case.
                    arrowHeadPosition = new Tuple(0D, 0D);
                    arrowHeadConnectionAngle = 0D;
                    break;
            }

            //Construct the connection
            final SVGObject connection = buildSVGObjectFromTemplate(SVGFileNameConstant.CONNECTION);
            final SVGObject arrowShaft = connection.getChild("arrow-shaft");
            arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_X.getKey(), shaftSourcePosition.getFirst());
            arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_Y.getKey(), shaftSourcePosition.getSecond());
            arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_X.getKey(), shaftDestinationPosition.getFirst());
            arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_Y.getKey(), shaftDestinationPosition.getSecond());
            arrowShaft.setAttribute(SVGAttributeConstant.STROKE_COLOR.getKey(), htmlColor);
            connection.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("link-%s", linkID));
            connection.setParent(ConnectionsContainer.toSVGObject());
            if (edgeDirection != Graph.FLAT){
                final SVGObject edgeArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.TRANSACTION_ARROW_HEAD);
                buildArrowHead(edgeArrowHeadContainer, arrowHeadPosition, arrowHeadConnectionAngle);
                edgeArrowHeadContainer.setParent(connection);
                edgeArrowHeadContainer.setAttribute(SVGAttributeConstant.FILL_COLOR.getKey(), htmlColor);
            }
        }
        
        /**
         * Creates 1 to 3 SVGObjects representing an Edge.
         * In this context, an edge is a group of similarly directed transactions between nodes.
         * The template file Connection.svg is used to build the edge.
         * The template file TransactionArrowHead.svg will be used to build the arrow head.
         * @param connectionsContainer
         * @param linkID 
         */
        private void buildEdges(final SVGContainer connectionsContainer, int linkID) {
            final int sourceVxId = graph.getLinkHighVertex(linkID);
            final int destinationVxId = graph.getLinkLowVertex(linkID); 
            final int colorAttributeID = VisualConcept.TransactionAttribute.COLOR.get(graph);

            //Get the coordinates of the source and destination node.
            final Tuple<Double, Double> sourcePosition = getVertexPosition(sourceVxId);
            final Tuple<Double, Double> destinationPosition = getVertexPosition(destinationVxId);

            //get the connection angles of the transactions
            final Double sourceConnectionAngle = calculateConnectionAngle(sourcePosition, destinationPosition);
            final Double destinationConnectionAngle = calculateConnectionAngle(destinationPosition, sourcePosition);

            //get the coordinates of the points where the transactions intersect the radius around the node that equal to half it's size.
            final Tuple<Double, Double> sourceCircumferencePosition = offSetPosition(sourcePosition, 128, sourceConnectionAngle);
            final Tuple<Double, Double> destinationCircumferencePosition = offSetPosition(destinationPosition, 128, destinationConnectionAngle);
            
            //Loop through all eges in the link
            int processedEdgeCount = 0;
            final int edgeCount = graph.getLinkEdgeCount(linkID);
            for (int edgePosition = 0 ; edgePosition < edgeCount; edgePosition++) {
                final int edgeID = graph.getLinkEdge(linkID, edgePosition); 
                int edgeDirection = graph.getEdgeDirection(edgeID);
                
                final String htmlColor;
                //if the edge has only one transaction set the color to that of the transaction
                if (graph.getEdgeTransactionCount(edgeID) == 1) {
                    final int transactionID = graph.getEdgeTransaction(edgeID, 0);
                    final String fillColor = graph.getStringValue(colorAttributeID, transactionID);
                    htmlColor = ConstellationColor.getColorValue(fillColor).getHtmlColor();
                //if the edge has more than 1 transaction set the color to white
                } else {
                    htmlColor = ConstellationColor.WHITE.getHtmlColor();
                }
                
                //Determine offset controlls
                int paralellOffsetDistance = (processedEdgeCount / 2 + ((processedEdgeCount % 2 == 0) ? 0 : 1)) * 16;
                double paralellOffsetDirection = Math.pow(-1, processedEdgeCount);
                double paralellOffsetAngle = Math.toRadians(90) * paralellOffsetDirection;

                //Determine arrow components connection angles and positions
                final Tuple<Double, Double> arrowHeadPosition;
                final Double arrowHeadConnectionAngle;
                Tuple<Double, Double> shaftSourcePosition = offSetPosition(sourceCircumferencePosition, paralellOffsetDistance, sourceConnectionAngle - paralellOffsetAngle);
                Tuple<Double, Double> shaftDestinationPosition = offSetPosition(destinationCircumferencePosition, paralellOffsetDistance, destinationConnectionAngle + paralellOffsetAngle);

                switch (edgeDirection) {
                case Graph.UPHILL:
                    //Source node has the arrow head
                    arrowHeadPosition = shaftSourcePosition;
                    arrowHeadConnectionAngle = sourceConnectionAngle;
                    shaftSourcePosition = offSetPosition(arrowHeadPosition, 64, sourceConnectionAngle);
                    break;
                case Graph.DOWNHILL:
                    //Destination node has the arrow head
                    arrowHeadPosition = shaftDestinationPosition;
                    arrowHeadConnectionAngle = destinationConnectionAngle;
                    shaftDestinationPosition = offSetPosition(arrowHeadPosition, 64, destinationConnectionAngle);
                    break;
                default:
                    //Undirected graphs have no arrow head
                    //Values set to 0 to avoid errors, however arrow heads are not rendered to the screen in this case.
                    arrowHeadPosition = new Tuple(0D, 0D);
                    arrowHeadConnectionAngle = 0D;
                    break;
                }

                //Construct the connection
                final SVGObject connection = buildSVGObjectFromTemplate(SVGFileNameConstant.CONNECTION);
                final SVGObject arrowShaft = connection.getChild("arrow-shaft");
                arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_X.getKey(), shaftSourcePosition.getFirst());
                arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_Y.getKey(), shaftSourcePosition.getSecond());
                arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_X.getKey(), shaftDestinationPosition.getFirst());
                arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_Y.getKey(), shaftDestinationPosition.getSecond());
                arrowShaft.setAttribute(SVGAttributeConstant.STROKE_COLOR.getKey(), htmlColor);
                connection.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("link-%s-edge-%s", linkID, edgePosition));
                connection.setParent(connectionsContainer.toSVGObject());
                if (edgeDirection != Graph.FLAT){
                    final SVGObject edgeArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.TRANSACTION_ARROW_HEAD);
                    buildArrowHead(edgeArrowHeadContainer, arrowHeadPosition, arrowHeadConnectionAngle);
                    edgeArrowHeadContainer.setParent(connection);
                    edgeArrowHeadContainer.setAttribute(SVGAttributeConstant.FILL_COLOR.getKey(), htmlColor);
                }

                processedEdgeCount++;
            }
        }
        
/**
         * Creates a SVGObjects representing an Link.
         * In this context, a link is a connection between nodes 
         * that does not contain two directed edges.
         * The template file Connection.svg is used to build the edge.
         * The template file TransactionArrowHead.svg will be used to build the arrow head.
         * @param connectionsContainer
         * @param linkID
         */
        private void buildTransactions(final SVGContainer connectionsContainer, int linkID) {
            final int sourceVxId = graph.getLinkHighVertex(linkID);
            final int destinationVxId = graph.getLinkLowVertex(linkID); 
            final int colorAttributeID = VisualConcept.TransactionAttribute.COLOR.get(graph);

            //Get the coordinates of the source and destination node.
            final Tuple<Double, Double> sourcePosition = getVertexPosition(sourceVxId);
            final Tuple<Double, Double> destinationPosition = getVertexPosition(destinationVxId);

            //get the connection angles of the transactions
            final Double sourceConnectionAngle = calculateConnectionAngle(sourcePosition, destinationPosition);
            final Double destinationConnectionAngle = calculateConnectionAngle(destinationPosition, sourcePosition);

            //get the coordinates of the points where the transactions intersect the radius around the node that equal to half it's size.
            final Tuple<Double, Double> sourceCircumferencePosition = offSetPosition(sourcePosition, 128, sourceConnectionAngle);
            final Tuple<Double, Double> destinationCircumferencePosition = offSetPosition(destinationPosition, 128, destinationConnectionAngle);
            
            //Loop through all eges in the link
            int processedTransactionCount = 0;
            final int transactionCount = graph.getLinkTransactionCount(linkID);
            for (int transactionPosition = 0 ; transactionPosition < transactionCount; transactionPosition++) {
                final int transactionID = graph.getLinkTransaction(linkID, transactionPosition); 
                int transactionDirection = graph.getTransactionDirection(transactionID);
                
                final String htmlColor;
                final String fillColor = graph.getStringValue(colorAttributeID, transactionID);
                htmlColor = ConstellationColor.getColorValue(fillColor).getHtmlColor();
                
                //Determine offset controlls
                int paralellOffsetDistance = (processedTransactionCount / 2 + ((processedTransactionCount % 2 == 0) ? 0 : 1)) * 16;
                double paralellOffsetDirection = Math.pow(-1, processedTransactionCount);
                double paralellOffsetAngle = Math.toRadians(90) * paralellOffsetDirection;
                
                //Determine arrow components connection angles and positions
                final Tuple<Double, Double> arrowHeadPosition;
                final Double arrowHeadConnectionAngle;
                Tuple<Double, Double> shaftSourcePosition = offSetPosition(sourceCircumferencePosition, paralellOffsetDistance, sourceConnectionAngle - paralellOffsetAngle);
                Tuple<Double, Double> shaftDestinationPosition = offSetPosition(destinationCircumferencePosition, paralellOffsetDistance, destinationConnectionAngle + paralellOffsetAngle);
                switch (transactionDirection) {
                    case Graph.UPHILL:
                        //Source node has the arrow head
                        arrowHeadPosition = shaftSourcePosition;
                        arrowHeadConnectionAngle = sourceConnectionAngle;
                        shaftSourcePosition = offSetPosition(arrowHeadPosition, 64, sourceConnectionAngle);
                        break;
                    case Graph.DOWNHILL:
                        //Destination node has the arrow head
                        arrowHeadPosition = shaftDestinationPosition;
                        arrowHeadConnectionAngle = destinationConnectionAngle;
                        shaftDestinationPosition = offSetPosition(arrowHeadPosition, 64, destinationConnectionAngle);
                        break;
                    default:
                        //Undirected graphs have no arrow head
                        //Values set to 0 to avoid errors, however arrow heads are not rendered to the screen in this case.
                        arrowHeadPosition = new Tuple(0D, 0D);
                        arrowHeadConnectionAngle = 0D;
                        break;
                }

                //Construct the connection
                final SVGObject connection = buildSVGObjectFromTemplate(SVGFileNameConstant.CONNECTION);
                final SVGObject arrowShaft = connection.getChild("arrow-shaft");
                arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_X.getKey(), shaftSourcePosition.getFirst());
                arrowShaft.setAttribute(SVGAttributeConstant.SOURCE_Y.getKey(), shaftSourcePosition.getSecond());
                arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_X.getKey(), shaftDestinationPosition.getFirst());
                arrowShaft.setAttribute(SVGAttributeConstant.DESTINATION_Y.getKey(), shaftDestinationPosition.getSecond());
                arrowShaft.setAttribute(SVGAttributeConstant.STROKE_COLOR.getKey(), htmlColor);
                connection.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("link-%s-transaction-%s", linkID, transactionPosition));
                connection.setParent(connectionsContainer.toSVGObject());
                if (transactionDirection != Graph.FLAT){
                    final SVGObject transactionArrowHeadContainer = buildSVGObjectFromTemplate(SVGFileNameConstant.TRANSACTION_ARROW_HEAD);
                    buildArrowHead(transactionArrowHeadContainer, arrowHeadPosition, arrowHeadConnectionAngle);
                    transactionArrowHeadContainer.setParent(connection);
                    transactionArrowHeadContainer.setAttribute(SVGAttributeConstant.FILL_COLOR.getKey(), htmlColor);
                }

                processedTransactionCount++;
            }
        }
        
        /**
         * Manipulates an arrow head container to adjust it's position and rotation.
         * @param arrowHeadContainer
         * @param x
         * @param y
         * @param connectionAngle 
         */
        private void buildArrowHead(SVGObject arrowHeadContainer, Tuple<Double,Double> position, Double connectionAngle) {
            //The size of the svg element containing the arrow head polygon asset
            final int arrowHeadWidth = 128;
            final int arrowHeadheight = 32;
            
            final Double x = position.getFirst();
            final Double y = position.getSecond();
            //Set arrow head svg attributes
            arrowHeadContainer.setAttribute(SVGAttributeConstant.X.getKey(), x - arrowHeadWidth);
            arrowHeadContainer.setAttribute(SVGAttributeConstant.Y.getKey(), y - arrowHeadheight /2 );
            arrowHeadContainer.setAttribute(SVGAttributeConstant.ID.getKey(), String.format("arrow-head-%s-%s", x.intValue(), y.intValue()));
            
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
                    text.setAttribute(SVGAttributeConstant.FONT_SIZE.getKey(),  intSize);
                    text.setAttribute(SVGAttributeConstant.Y.getKey(), offset);
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
                    text.setAttribute(SVGAttributeConstant.FONT_SIZE.getKey(), intSize);
                    text.setAttribute(SVGAttributeConstant.Y.getKey(), offset);
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
            final BoundingBox box = new BoundingBox();
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
            final Float contentWidth = maxBound.getX() - minBound.getX() + 256;
            final Float contentHeight = maxBound.getY() - minBound.getY() + 256;
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
         * Gets the position of the vertex.
         * Position is normalised with respect to the position of the top-left most vertex.
         * Position is with respect to the center of the vertex.
         * @param vertexID
         * @return 
         */
        private Tuple<Double, Double> getVertexPosition(final int vertexID) {
            final int xAttributeID = VisualConcept.VertexAttribute.X.get(graph);
            final int yAttributeID = VisualConcept.VertexAttribute.Y.get(graph);            
            
            final int halfVertexSize = 128;
            
            final Double constelationGraphX = graph.getDoubleValue(xAttributeID, vertexID);
            final Double constelationGraphY = graph.getDoubleValue(yAttributeID, vertexID);
            
            final Double svgGraphX = (constelationGraphX * halfVertexSize) - minBound.getX() + halfVertexSize;
            final Double svgGraphY = (maxBound.getY() - minBound.getY()) - ((constelationGraphY * halfVertexSize) - minBound.getY()) + halfVertexSize;
            
            return new Tuple<>(svgGraphX, svgGraphY);
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

        private Tuple<Double, Double> offSetPosition(final Tuple<Double, Double> origin, final int distance, final Double angle) {
            final Double x = origin.getFirst() - (distance * Math.cos(angle));
            final Double y = origin.getSecond() - (distance * Math.sin(angle));
            return new Tuple<>(x,y);
        }
    }
}
