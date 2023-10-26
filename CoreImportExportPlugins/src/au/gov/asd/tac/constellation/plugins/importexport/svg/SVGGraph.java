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

import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import au.gov.asd.tac.constellation.utilities.svg.SVGData;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.utilities.svg.SVGAttributeConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGLayoutConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGFileNameConstant;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Frustum;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
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
        private PluginInteraction interaction;
        private GraphVisualAccess access;
        Matrix44f modelViewProjectionMatrix;
        int[] viewPort;
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
        
        public SVGGraphBuilder  withInteraction(PluginInteraction interaction) {
            this.interaction = interaction;
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
        public SVGData build() throws InterruptedException {
            final SVGObject svgGraph = SVGObject.loadFromTemplate(SVGFileNameConstant.LAYOUT);
            preBuild();
            buildHeader(svgGraph);
            buildFooter(svgGraph);
            buildBackground(svgGraph);
            buildNodes(svgGraph);
            buildConnections(svgGraph);
            
            setLayoutDimensions(svgGraph);
            return svgGraph.toSVGData();
        }       
        
        /**
         * Sets up the Builder control attributes. 
         */
        private void preBuild(){
            
            Camera camera = new Camera(access.getCamera());

            //Set the view port
            final BoundingBox box = camera.boundingBox;
            BoundingBoxUtilities.recalculateFromGraph(box, graph, selectedNodesOnly);
            CameraUtilities.refocusOnZAxis(camera, box, false);
            Vector3f maxBound = box.getMax();
            Vector3f minBound = box.getMin();

            maxBound.scale(256);
            minBound.scale(256);
            
            float viewPortHeight = maxBound.getY() - minBound.getY();
            viewPortHeight = viewPortHeight < 1 ? 1 : viewPortHeight;
            
            float viewPortWidth = maxBound.getX() - minBound.getX();
            viewPortWidth = viewPortWidth < 1 ? 1 : viewPortWidth;
            
            //Get Model view Matrix from the Camera.
            final Matrix44f mvMatrix = Graphics3DUtilities.getModelViewMatrix(camera);
            
            // Define the view frustum
            final float fov = 35;
            final float nearPerspective = 1;
            final float farPerspective = 500000;
            final float aspect = viewPortWidth / viewPortHeight;
            
            final float ymax;
            final float ymin;
            final float xmax;
            final float xmin;
            
            if (aspect < 1){
                ymax = nearPerspective * (float) Math.tan(fov * Math.PI / 360.0);
                xmax = ymax * viewPortWidth / viewPortHeight;
            } else {
                xmax = nearPerspective * (float) Math.tan(fov * Math.PI / 360.0);
                ymax = xmax * viewPortHeight / viewPortWidth;
            }
            
            ymin = -ymax;
            xmin = -xmax;

            Frustum viewFrustum = new Frustum(fov, aspect, xmin, xmax, ymin, ymax, nearPerspective, farPerspective);

            //Get the projection matrix from the view frustum
            Matrix44f pMatrix = viewFrustum.getProjectionMatrix();
            
            //Switch the y sign for exporting to SVG
            final Matrix44f scaleMatrix = new Matrix44f();
            scaleMatrix.makeScalingMatrix(new Vector3f(1.0F, -1.0F, 1.0F));
            pMatrix.multiply(pMatrix, scaleMatrix);
            
            //Generate the ModelVieqwprojectionMatrix. 
            final Matrix44f mvpMatrix = new Matrix44f();
            mvpMatrix.multiply(pMatrix, mvMatrix);   
            
            viewPort = new int[] {Math.round(camera.lookAtEye.getX()),  Math.round(camera.lookAtEye.getY()), Math.round(viewPortWidth),  Math.round(viewPortHeight)};
            modelViewProjectionMatrix = mvpMatrix; 
        }
        
        /**
         * Builds the header area of the output SVG.
         * @param svg 
         */
        private void buildHeader(final SVGObject svg){
            final SVGObject titleContainer = svg.getChild(SVGLayoutConstant.HEADER.getValue()).getChild(SVGLayoutConstant.TITLE.getValue());
            titleContainer.toSVGData().setContent(graphTitle);
            final SVGObject subtitleContainer = svg.getChild(SVGLayoutConstant.HEADER.getValue()).getChild(SVGLayoutConstant.SUBTITLE.getValue());
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
            final SVGObject titleContainer = svg.getChild(SVGLayoutConstant.FOOTER.getValue()).getChild(SVGLayoutConstant.FOOTNOTE.getValue());
            titleContainer.toSVGData().setContent("The Constellation community. All rights reserved.");
        }
        
        private void buildBackground(final SVGObject svg){
            svg.getChild(SVGLayoutConstant.BACKGROUND.getValue()).setFillColor(backgroundColor);
        }
        
        /**
         * Generates SVG Nodes from the graph and assigns them as children to the Nodes container.
         * The template file Node.svg is used to build the node.
         * @param svgGraph
         */
        private void buildNodes(final SVGObject svgGraph) throws InterruptedException {
            int progress = 0;
            interaction.setExecutionStage(progress, access.getVertexCount(), "Building Graph", "Building Nodes", true);
            //Retrieve the svg element that holds the nodes as a SVGObject
            final SVGObject nodesContainer = svgGraph.getChild(SVGLayoutConstant.CONTENT.getValue()).getChild(SVGLayoutConstant.NODES.getValue()); 
            for (int vertexPosition = 0 ; vertexPosition < access.getVertexCount() ; vertexPosition++) {
                              
                //Do not export this vertex if only selected nodes are being exported and the node is not selected.
                if (selectedNodesOnly && !access.isVertexSelected(vertexPosition) || access.getVertexVisibility(vertexPosition) == 0){
                    continue;
                }
                
                //Get the values of the attributes relevent to the current node
                final Vector4f position = getVertexPosition(vertexPosition);
                final float radius = getVertexScaledRadius(vertexPosition);
                final ConstellationColor color = access.getVertexColor(vertexPosition);
                final String bgi = access.getBackgroundIcon(vertexPosition);
                final String fgi = access.getForegroundIcon(vertexPosition);
                final ConstellationIcon backgroundIcon = IconManager.getIcon(bgi);
                final ConstellationIcon foregroundIcon = IconManager.getIcon(fgi);
                access.getBackgroundIcon(vertexPosition);
                
                //Build the SVGobject representing the node
                final SVGObject node = SVGObject.loadFromTemplate(SVGFileNameConstant.NODE);
                node.setPosition(position.getX() - radius, position.getY() - radius);
                node.setID(access.getVertexId(vertexPosition));
                node.setParent(nodesContainer);
                node.setDimension(radius * 2, radius * 2);
                
                //Add labels to the node
                if (showTopLabels){
                    final SVGObject topLabelContainer = node.getChild(SVGLayoutConstant.TOP_LABELS.getValue());
                    buildTopLabel(vertexPosition, topLabelContainer);
                }
                if (showBottomLabels){
                    final SVGObject bottomLabelContainer = node.getChild(SVGLayoutConstant.BOTTOM_LABELS.getValue());
                    buildBottomLabel(vertexPosition, bottomLabelContainer);
                }
                
                //Add images to the node
                final SVGObject nodeImages = node.getChild(SVGLayoutConstant.NODE_IMAGES.getValue());
                
                //Add dimmed property if dimmed
                //This implementation is not an precice sollution, lumocity to alpha conversion would be better
                if (access.isVertexDimmed(vertexPosition)){
                    nodeImages.setAttribute(SVGAttributeConstant.FILTER, "grayscale(1)");
                }
                
                final SVGObject backgroundContainer = nodeImages.getChild(SVGLayoutConstant.BACKGROUND_IMAGE.getValue());
                SVGData bgimage = backgroundIcon.buildSVG(color.getJavaColor());
                bgimage.setParent(backgroundContainer.toSVGData());
                
                final SVGObject foregroundContainer = nodeImages.getChild(SVGLayoutConstant.FOREGROUND_IMAGE.getValue());
                SVGData fgimage = foregroundIcon.buildSVG();
                fgimage.setParent(foregroundContainer.toSVGData());
                
                //Add decorators to the node       
                this.buildDecorator(nodeImages.getChild(SVGLayoutConstant.NORTH_WEST_DECORATOR.getValue()), access.getNWDecorator(vertexPosition));
                this.buildDecorator(nodeImages.getChild(SVGLayoutConstant.NORTH_EAST_DECORATOR.getValue()), access.getNEDecorator(vertexPosition));
                this.buildDecorator(nodeImages.getChild(SVGLayoutConstant.SOUTH_WEST_DECORATOR.getValue()), access.getSWDecorator(vertexPosition));
                this.buildDecorator(nodeImages.getChild(SVGLayoutConstant.SOUTH_EAST_DECORATOR.getValue()), access.getSEDecorator(vertexPosition));
                
                interaction.setProgress(progress++, access.getVertexCount(), true);
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
                if (IconManager.iconExists(decoratorValue)){
                    final SVGData icon = IconManager.getIcon(decoratorValue).buildSVG();
                    icon.setParent(decorator.toSVGData());
                }
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
        private void buildConnections(final SVGObject svgGraph) throws InterruptedException {
            //Donot export connections if showConnections is disabled
            if (!showConnections){
                interaction.setProgress(access.getLinkCount(), access.getLinkCount(), "Created 0 links", true);
                return;
            }
            
            int progress = 0;
            interaction.setExecutionStage(progress, access.getVertexCount(), "Building Graph", "Building Connections", false);
            
            // Get the SVG element that will contain all connections
            final SVGObject connectionsContainer = svgGraph.getChild(SVGLayoutConstant.CONTENT.getValue()).getChild(SVGLayoutConstant.CONNECTIONS.getValue());
            
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
                final Vector4f highCenterPosition = getVertexPosition(high);
                final Vector4f lowCenterPosition = getVertexPosition(low);
                
                //Get the SVG angle of the connection between the two nodes
                final double highConnectionAngle = calculateConnectionAngle(highCenterPosition, lowCenterPosition);
                final double lowConnectionAngle = calculateConnectionAngle(lowCenterPosition, highCenterPosition);

                //Get the coordinates of the points where the connections intersect the node radius
                final Vector4f highCircumferencePosition = offSetPosition(highCenterPosition, getVertexScaledRadius(high), highConnectionAngle);
                final Vector4f lowCircumferencePosition = offSetPosition(lowCenterPosition, getVertexScaledRadius(low), lowConnectionAngle);
                
                //Itterate over all of the Transactions/Edges/Links between the two nodes.
                //Note: the linkConnectionCount factors in the connection mode and the max transaction threshold.
                for (int connectionPosition = 0; connectionPosition < access.getLinkConnectionCount(linkPosition); connectionPosition++){
                    
                    //Get the reference tothe current transaction/Edge/Link
                    final int connection = access.getLinkConnection(linkPosition, connectionPosition);
                    
                    if (high == low) {
                        final Vector4f loopConnectionCentrePosition = new Vector4f();
                        Vector4f.add(loopConnectionCentrePosition, highCenterPosition, new Vector4f(getVertexScaledRadius(high), -getVertexScaledRadius(high), 0, 0));
                        buildLoopedConnection(connectionsContainer, loopConnectionCentrePosition, connection);
                    } else {
                    //Determine offset controlls for drawing multiple Transactions/Edges in paralell
                    final int paralellOffsetDistance = (connectionPosition / 2 + ((connectionPosition % 2 == 0) ? 0 : 1)) * 16;
                    final double paralellOffsetDirection = Math.pow(-1, connectionPosition);
                    final double paralellOffsetAngle = Math.toRadians(90) * paralellOffsetDirection;

                    //Determine the unique positions for the individual Transation/edge/link.
                    final Vector4f highPosition = offSetPosition(highCircumferencePosition, paralellOffsetDistance, highConnectionAngle - paralellOffsetAngle);
                    final Vector4f lowPosition = offSetPosition(lowCircumferencePosition, paralellOffsetDistance, lowConnectionAngle + paralellOffsetAngle);
                    LOGGER.log(Level.SEVERE, String.format("Low: %s, high: %s", lowPosition.toString(), highPosition.toString()));
                    //Create the Transaction/Edge/Link SVGData
                    buildLinearConnection(connectionsContainer, highPosition, lowPosition, connection);  
                    }
                } 
                interaction.setProgress(progress++, access.getLinkCount(), true);
            }
        }
        
        private void buildLoopedConnection(final SVGObject connectionsContainer, final Vector4f position, final int connectionReference) {
            final SVGObject connection = SVGObject.loadFromTemplate(SVGFileNameConstant.CONNECTION_LOOP);
            final SVGObject arrowShaft = connection.getChild(SVGLayoutConstant.ARROW_SHAFT.getValue());

            
            //Get the coordinates of the potential shaft extremeties at 64px behind the arrow tip position.
            final ConstellationColor color = getConnectionColor(connectionReference);
            //Assign the positional values of shaft and arrow head/s based on the direction of the Transaction/Edge/Link
            final ConnectionDirection direction = access.getConnectionDirection(connectionReference);            
                
            switch (direction){

                //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head    
                case LOW_TO_HIGH:
                case HIGH_TO_LOW:
                    final SVGObject arrowHead = SVGObject.loadFromTemplate(SVGFileNameConstant.ARROW_HEAD_TRANSACTION_LOOP);
                    Vector4f arrowHeadPosition = new Vector4f(position.getX(), position.getY() + 100, position.getZ(), position.getW());
                    
                    arrowHead.setFillColor(color);
                    buildArrowHead(arrowHead, arrowHeadPosition, 0);
                    arrowHead.setParent(connection);
                    break;

                //Undirected connections are Transactions, Edges and Links with no arrow heads.
                default:
                    //connection.setDimension(50,50);
                    arrowShaft.setAttribute(SVGAttributeConstant.RADIUS, "120");
                    arrowShaft.setStrokeArray(188.5f, 188.5f, 377f);
                    break;
            }
            
            arrowShaft.setPosition(position.getX(), position.getY());
            //Set the attributes of the connection and add it to the connections Conatainer  
            
            connection.setID(String.format("Connection_%s_%s", position, connectionReference));
            connection.setStrokeColor(color);
            
            connection.setStrokeStyle(access.getConnectionLineStyle(connectionReference));
            connection.setParent(connectionsContainer);
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
        private void buildLinearConnection(final SVGObject connectionsContainer, final Vector4f highPosition, final Vector4f lowPosition, final int connection){
            //Get references to SVG Objects being built within this method 

            final SVGObject connectionSVG = SVGObject.loadFromTemplate(SVGFileNameConstant.CONNECTION_LINEAR);
            final SVGObject arrowShaft = connectionSVG.getChild(SVGLayoutConstant.ARROW_SHAFT.getValue());

            final SVGObject highArrowHeadContainer;
            final SVGObject lowArrowHeadContainer;
            
            //Get the connection angles of the connection
            final Double highConnectionAngle = calculateConnectionAngle(highPosition, lowPosition);
            final Double lowConnectionAngle = calculateConnectionAngle(lowPosition, highPosition);
            
            //Get the coordinates of the potential shaft extremeties at 64px behind the arrow tip position.
            final Vector4f highPositionRecessed = offSetPosition(highPosition, 64, highConnectionAngle);
            final Vector4f lowPositionRecessed = offSetPosition(lowPosition, 64, lowConnectionAngle);

            //Assign the positional values of shaft and arrow head/s based on the direction of the Transaction/Edge/Link
            final ConnectionDirection direction = access.getConnectionDirection(connection);            
                
            switch (direction){
                //Bidirectional connectsions are Links with two link arrow heads
                case BIDIRECTED:
                    buildArrowShaft(arrowShaft, highPositionRecessed, lowPositionRecessed);

                    highArrowHeadContainer = SVGObject.loadFromTemplate(SVGFileNameConstant.ARROW_HEAD_LINK);
                    buildArrowHead(highArrowHeadContainer, highPosition, lowConnectionAngle);
                    highArrowHeadContainer.setParent(connectionSVG);

                    lowArrowHeadContainer = SVGObject.loadFromTemplate(SVGFileNameConstant.ARROW_HEAD_LINK);
                    buildArrowHead(lowArrowHeadContainer, lowPosition, highConnectionAngle);
                    lowArrowHeadContainer.setParent(connectionSVG);
                    break;

                //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head    
                case LOW_TO_HIGH:
                    buildArrowShaft(arrowShaft, highPositionRecessed, lowPosition);

                    highArrowHeadContainer = SVGObject.loadFromTemplate(SVGFileNameConstant.ARROW_HEAD_TRANSACTION);
                    buildArrowHead(highArrowHeadContainer, highPosition, lowConnectionAngle);
                    highArrowHeadContainer.setParent(connectionSVG);
                    break;

                //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head
                case HIGH_TO_LOW:
                    buildArrowShaft(arrowShaft, highPosition, lowPositionRecessed); 

                    lowArrowHeadContainer = SVGObject.loadFromTemplate(SVGFileNameConstant.ARROW_HEAD_TRANSACTION);
                    buildArrowHead(lowArrowHeadContainer, lowPosition, highConnectionAngle);
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
        private void buildArrowHead(final SVGObject arrowHeadContainer, final Vector4f position, final double connectionAngle) {

            final float arrowHeadHeight = arrowHeadContainer.getHeight();
            LOGGER.log(Level.SEVERE, String.format("ARROW HEAD HEIGHT: %s", arrowHeadHeight));
            
            //Set arrow head svg attributes
            arrowHeadContainer.setPosition(position.getX() , position.getY() - arrowHeadHeight/2);
            //arrowHeadContainer.setDimension(arrowHeadWidth, arrowHeadheight);
            arrowHeadContainer.setID(String.format("arrow-head-%s-%s", position.getX(), position.getY() - arrowHeadHeight/2 ));
            
            //Rotate the arrow head polygon around the tip to align it with the angle of the connection
            final SVGObject arrowHead = arrowHeadContainer.getChild(SVGLayoutConstant.ARROW_HEAD.getValue());
            arrowHead.setTransformation(String.format("rotate(%s %s %s)", Math.toDegrees(connectionAngle), 0, arrowHeadHeight/2));
        }
        
        /**
         * Manipulates an arrow shaft container to adjust it's position.
         * @param arrowShaft
         * @param sourcePosition
         * @param destinationPosition 
         */
        private void buildArrowShaft(SVGObject arrowShaft, Vector4f sourcePosition, Vector4f destinationPosition) {
            arrowShaft.setSourcePosition(sourcePosition);
            arrowShaft.setDestinationPosition(destinationPosition);
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
            final float contentWidth = viewPort[2] + 256.0F;
            final float contentHeight = viewPort[3] + 256.0F;
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
            
            svg.getChild(SVGLayoutConstant.FOOTER.getValue()).setPosition(0F, footerYOffset);
            svg.getChild(SVGLayoutConstant.HEADER.getValue()).setMinimumDimension(fullWidth, topMargin);
            svg.getChild(SVGLayoutConstant.FOOTER.getValue()).setMinimumDimension(fullWidth, bottomMargin);  
            
            svg.getChild(SVGLayoutConstant.CONTENT.getValue()).setPosition(contentXOffset, contentYOffset);
          
            svg.getChild(SVGLayoutConstant.BACKGROUND.getValue()).setPosition(xMargin, topMargin);
            svg.getChild(SVGLayoutConstant.BACKGROUND.getValue()).setMinimumDimension(backgroundWidth, backgroundHeight);
            
            svg.getChild(SVGLayoutConstant.BORDER.getValue()).setMinimumDimension(fullWidth, fullHeight);
            svg.setDimensionScale("100%", "100%");
        }

        /**
         * Gets the normalised position of the vertex.
         * Position is normalised with respect to a width and hight space of -1 to +1.
         * Position is with respect to the center of the vertex.
         * @param vertex
         * @return 
         */

        private Vector4f getVertexPosition(final int vertex) {           
            final Float constelationGraphX = access.getX(vertex);
            final Float constelationGraphY = access.getY(vertex);
            final Float constelationGraphZ = access.getZ(vertex);
            
            Vector3f worldPosition = new Vector3f(constelationGraphX, constelationGraphY, constelationGraphZ);
            return getVertexPosition(worldPosition);
        }
        
        private Vector4f getVertexPosition(final Vector3f worldPosition){
            Vector4f screenPosition = new Vector4f();
            
            Graphics3DUtilities.project(worldPosition, modelViewProjectionMatrix, viewPort, screenPosition);
            Vector4f centerOffSet = new Vector4f(128, 128, 0, 0);
            Vector4f.add(screenPosition, screenPosition, centerOffSet);
            
            LOGGER.log(Level.SEVERE, String.format("Vertex %s", screenPosition));
            return screenPosition;
        }
        
        /**
         * Determines the radius of the node.
         * The scale is determined by projecting a position at the edge of the node
         * to its correlating screen position. 
         * A less than ideal solution that will not support alternate export perspectives. 
         * @param position
         * @param vertexPosition
         * @return 
         */
        private float getVertexScaledRadius(final int vertex) {  
            Vector4f screenPosition = getVertexPosition(vertex);
            
            final Float x = access.getX(vertex) + 1;
            final Float y = access.getY(vertex) + 1;
            final Float z = access.getZ(vertex);
            
            Vector4f edgePosition = getVertexPosition(new Vector3f(x,y,z));
            
            int radiusID = VisualConcept.VertexAttribute.NODE_RADIUS.get(graph);
            float radius = graph.getFloatValue(radiusID, access.getVertexId(vertex));
            float depthScaleFactor = Math.abs(edgePosition.getX() - screenPosition.getX());
            return radius * depthScaleFactor;            
        }
        
        /**
         * Calculates the angle at which a connection touches a node.
         * Return value is clockwise from a horizontal x axis with positive values to the right.
         * @param sourcePosition
         * @param destinationPosition
         * @return 
         */
        private double calculateConnectionAngle(final Vector4f sourcePosition, final Vector4f destinationPosition) {
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
        private Vector4f offSetPosition(final Vector4f origin, final float distance, final double angle) {
            final float x = (float) (origin.getX() - (distance * Math.cos(angle)));
            final float y = (float) (origin.getY() - (distance * Math.sin(angle)));
            return new Vector4f(x, y, origin.getZ(), origin.getW());
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
