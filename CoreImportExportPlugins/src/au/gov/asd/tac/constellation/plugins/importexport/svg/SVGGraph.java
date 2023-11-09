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
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import au.gov.asd.tac.constellation.utilities.svg.SVGData;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGObjectConstant;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGTemplateConstant;
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
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
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
        private Camera camera = null;
        private Graph graph;
        private GraphReadMethods readableGraph;
        private PluginInteraction interaction;
        private GraphVisualAccess access;
        Matrix44f modelViewProjectionMatrix = new Matrix44f();
        int[] viewPort;
        private String graphTitle = null;
        private boolean selectedNodesOnly = false;
        private boolean showConnections = true;
        private boolean showTopLabels = true;
        private boolean showBottomLabels = true;
        private ConstellationColor backgroundColor = VisualGraphDefaults.DEFAULT_BACKGROUND_COLOR;
        private String exportPerspective = null;
        
        /**
         * Specifies the graph to build the SVG from.
         * @param graph The graph to be exported.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder withGraph(final Graph graph) {
            this.graph = graph;
            return this;
        }
        
        /**
         * Specifies the graph to build the SVG from.
         * @param graph The graph to be exported.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder withReadableGraph(final GraphReadMethods graph) {
            this.readableGraph = graph;
            return this;
        }
        
        /**
         * Specifies the GraphVisualAccess instance to use for retrieving graph information.
         * @param title The title of the graph.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder withAccess(final GraphVisualAccess access) {
            this.access = access;
            return this;
        }
        
        /**
         * Specifies the PluginInteraction instance to use for updating on plugin progress.
         * @param title The title of the graph.
         * @return SVGGraphBuilder
         */
        public SVGGraphBuilder  withInteraction(final PluginInteraction interaction) {
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
        public SVGGraphBuilder withNodes(final Boolean selectedNodesOnly) {
            this.selectedNodesOnly = selectedNodesOnly;
            return this;
        }
        
        /**
         * Controls whether connections are included or excluded from the SVG output file
         * @param showConnections
         * @return 
         */
        public SVGGraphBuilder includeConnections(final Boolean showConnections) {
            this.showConnections = showConnections;
            return this;
        }
        
        /**
         * Controls whether Node top labels are included or excluded from the SVG output file
         * @param showTopLabels
         * @return 
         */
        public SVGGraphBuilder includeTopLabels(final Boolean showTopLabels) {
            this.showTopLabels = showTopLabels;
            return this;
        }
        
        /**
         * Controls whether Node top labels are included or excluded from the SVG output file
         * @param showBottomLabels
         * @return 
         */
        public SVGGraphBuilder includeBottomLabels(final Boolean showBottomLabels) {
            this.showBottomLabels = showBottomLabels;
            return this;
        }
        
        /**
         * Specifies the Perspective to set 
         * @param exportPerspective
         * @return 
         */
        public SVGGraphBuilder fromPerspective(final String exportPerspective) {
            this.exportPerspective = exportPerspective;
            return this;
        }

        /**
         * Builds an SVGGraphObject representing the provided graph.
         * @return SVGData
         */
        public SVGData build() throws InterruptedException {
            final SVGObject svgGraph = SVGTemplateConstant.LAYOUT.getSVGObject();
            preBuild();
            buildHeader(svgGraph);
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
            this.camera = new Camera(access.getCamera());
            // Set the view port
            final BoundingBox box = camera.boundingBox;
            BoundingBoxUtilities.recalculateFromGraph(box, readableGraph, selectedNodesOnly);
            
            final GraphNode graphNode = GraphNode.getGraphNode(graph);
            final VisualManager visualManager = graphNode.getVisualManager();
            int viewPortHeight = visualManager.getVisualComponent().getHeight();   
            int viewPortWidth = visualManager.getVisualComponent().getWidth();
            
            switch (exportPerspective) {
                case "Y-Axis":
                    CameraUtilities.refocusOnYAxis(camera, box, false);
                    break;
                case "X-Axis":
                    CameraUtilities.refocusOnXAxis(camera, box, false);
                    break;
                case "Z-Axis":
                    CameraUtilities.refocusOnZAxis(camera, box, false);
                default:
                    break;
            }
            
            // Get Model view Matrix from the Camera.
            final Matrix44f mvMatrix = Graphics3DUtilities.getModelViewMatrix(camera);

            // Define the view frustum
            final float fov = 35;
            final float nearPerspective = 1;
            final float farPerspective = 500000;
            final float aspect = viewPortWidth / (float) viewPortHeight;
            final float aspectInverse = viewPortHeight / (float) viewPortWidth;
            
            final float ymax;
            final float ymin;
            final float xmax;
            final float xmin;
            
            if (aspect < 1){
                LOGGER.log(Level.SEVERE, "Im Here");
                xmax = nearPerspective * (float) Math.tan(fov * Math.PI / 360.0);
                ymax = xmax * aspectInverse;
            } else {
                ymax = nearPerspective * (float) Math.tan(fov * Math.PI / 360.0);
                xmax = ymax * aspect;
            }
            
            ymin = -ymax;
            xmin = -xmax;

            final Frustum viewFrustum = new Frustum(fov, aspect, xmin, xmax, ymin, ymax, nearPerspective, farPerspective);
            // Get the projection matrix from the view frustum
            final Matrix44f pMatrix = viewFrustum.getProjectionMatrix();
            
            // Switch the y sign for exporting to SVG
            final Matrix44f scaleMatrix = new Matrix44f();
            scaleMatrix.makeScalingMatrix(new Vector3f(1.0F, -1.0F, 1.0F));
            pMatrix.multiply(pMatrix, scaleMatrix);
            
            // Generate the ModelViewProjectionMatrix. 
            modelViewProjectionMatrix.multiply(pMatrix, mvMatrix);   
            
            viewPort = new int[] {Math.round(camera.lookAtEye.getX()),  Math.round(camera.lookAtEye.getY()), viewPortWidth,  viewPortHeight};
        }
        
        /**
         * Generates SVG Nodes from the graph and assigns them as children to the Nodes container.
         * The template file Node.svg is used to build the node.
         * @param svgGraph The SVGObject holding all generated SVG data 
         */
        private void buildNodes(final SVGObject svgGraph) throws InterruptedException {
            // Initate plugin report information
            int progress = 0;
            final int totalSteps = access.getVertexCount();
            interaction.setExecutionStage(progress, totalSteps, "Building Graph", "Building Nodes", true);
            
            // Retrieve the svg element that holds the nodes as a SVGObject
            final SVGObject svgNodes = SVGObjectConstant.CONTENT.findIn(svgGraph);
            
            // Itterate over all nodes in the graph
            for (int vertexIndex = 0 ; vertexIndex < access.getVertexCount() ; vertexIndex++) {
                              
                // Do not export this vertex if only selected nodes are being exported and the node is not selected.
                if (selectedNodesOnly && !access.isVertexSelected(vertexIndex) || access.getVertexVisibility(vertexIndex) == 0){
                    continue;
                }
                
                // Get the values of the attributes relevent to the current node
                final Vector4f position = getVertexPosition(vertexIndex);
                final float radius = getVertexScaledRadius(vertexIndex);
                final ConstellationColor color = access.getVertexColor(vertexIndex);
                final ConstellationIcon backgroundIcon = IconManager.getIcon(access.getBackgroundIcon(vertexIndex));
                final ConstellationIcon foregroundIcon = IconManager.getIcon(access.getForegroundIcon(vertexIndex));
                
                // Build the SVGobject representing the node
                final SVGObject svgNode = SVGObject.loadFromTemplate(SVGTemplateConstant.NODE);
                svgNode.setPosition(position.getX() - radius, position.getY() - radius);
                svgNode.setID(String.format("node-%s",access.getVertexId(vertexIndex)));
                svgNode.setSortOrderValue(position.getW());
                svgNode.setParent(svgNodes);
                svgNode.setDimension(radius * 2, radius * 2);
                
                // Add labels to the node
                if (showTopLabels){
                    final SVGObject svgTopLabel = SVGObjectConstant.TOP_LABELS.findIn(svgNode);
                    buildTopLabel(vertexIndex, svgTopLabel);
                }
                if (showBottomLabels){
                    final SVGObject svgBottomLabel = SVGObjectConstant.BOTTOM_LABELS.findIn(svgNode);
                    buildBottomLabel(vertexIndex, svgBottomLabel);
                }
                
                // Retrieve the svg element containing all node images.
                final SVGObject svgImages = SVGObjectConstant.NODE_IMAGES.findIn(svgNode);
                
                // Add dimmed property if dimmed
                // Node, this implementation is not a precice sollution, luminocity to alpha conversion would be better
                if (access.isVertexDimmed(vertexIndex)){
                    svgImages.applyGrayScaleFileter();
                }
                // Add background image to the node
                final SVGObject svgNodeBackground = SVGObjectConstant.BACKGROUND_IMAGE.findIn(svgNode);
                final SVGData svgBackgroundImageimage = backgroundIcon.buildSVG(color.getJavaColor());
                svgBackgroundImageimage.setParent(svgNodeBackground.toSVGData());
                
                // Add foreground image to the node
                final SVGObject svgNodeForeground = SVGObjectConstant.FOREGROUND_IMAGE.findIn(svgNode);
                final SVGData svgForegroundImage = foregroundIcon.buildSVG();
                svgForegroundImage.setParent(svgNodeForeground.toSVGData());
                
                // Add decorators to the node       
                this.buildDecorator(SVGObjectConstant.NORTH_WEST_DECORATOR.findIn(svgNode), access.getNWDecorator(vertexIndex));
                this.buildDecorator(SVGObjectConstant.NORTH_EAST_DECORATOR.findIn(svgNode), access.getNEDecorator(vertexIndex));
                this.buildDecorator(SVGObjectConstant.SOUTH_WEST_DECORATOR.findIn(svgNode), access.getSWDecorator(vertexIndex));
                this.buildDecorator(SVGObjectConstant.SOUTH_EAST_DECORATOR.findIn(svgNode), access.getSEDecorator(vertexIndex));
                
                interaction.setProgress(progress++, totalSteps, true);
            }
            interaction.setProgress(totalSteps, totalSteps, String.format("Created %s nodes", progress), true);
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
         * @param vertexIndex
         * @param svgBottomLabel
         */
        private void buildBottomLabel(final int vertexIndex, final SVGObject svgBottomLabel){    
            float offset = 0;
            for (int labelIndex = 0; labelIndex < access.getBottomLabelCount(); labelIndex++) {
                final String labelString = access.getVertexBottomLabelText(vertexIndex, labelIndex);
                if (labelString != null){
                    final SVGObject svgLabel = SVGTemplateConstant.LABEL.getSVGObject();
                    final float size = access.getBottomLabelSize(labelIndex) * 64;
                    svgLabel.setFontSize(size);
                    svgLabel.setYPosition(offset);
                    svgLabel.setFillColor(access.getBottomLabelColor(labelIndex));
                    svgLabel.setBaseline("hanging");
                    svgLabel.setID(String.format("bottom-label-%s", labelIndex));
                    svgLabel.setContent(labelString);
                    svgLabel.setParent(svgBottomLabel);
                    offset = offset + size;
                }
            }
        }
        
        /**
         * Constructs the top label SVG elements for a given vertex id in a given SVGContainer.
         * This method only considers the top label requirements for nodes.
         * This element of the output graph currently relies heavily on content overflow of parent elements
         * due to inabilities for parents to set their width and height based off of the width and height requirements of their children.
         * @param vertexIndex
         * @param svgTopLabel 
         */
        private void buildTopLabel(final int vertexIndex, final SVGObject svgTopLabel){
            float offset = 0;
            for (int labelIndex = 0; labelIndex < access.getTopLabelCount(); labelIndex++) {
                final String labelString = access.getVertexTopLabelText(vertexIndex, labelIndex);
                if (labelString != null){
                    final SVGObject svgLabel = SVGTemplateConstant.LABEL.getSVGObject();
                    final float size = access.getTopLabelSize(labelIndex) * 64;
                    svgLabel.setFontSize(size);
                    svgLabel.setYPosition(offset);
                    svgLabel.setFillColor(access.getTopLabelColor(labelIndex));
                    svgLabel.setBaseline("after-edge");
                    svgLabel.setID(String.format("top-label-%s", labelIndex));
                    svgLabel.setContent(labelString);
                    svgLabel.setParent(svgTopLabel);
                    offset = offset - size;
                }
            }
        }
        
        /**
         * Builds SVG representations of Connections between Nodes.
         * Connections are built in layers of svg elements of increasing specificity
         * following the structure of links(link(connections, labels)))
         * Generates representations of transactions, links and edges depending on connectionMode.
         * Labels are rendered for connections excluding looped connections.
         * Other graph attributes including maxTransactions are considered.
         * @param svgGraph The SVGObject holding all generated SVG data 
         */
        private void buildConnections(final SVGObject svgGraph) throws InterruptedException {
            
            // Initate plugin report information
            int progress = 0;
            final int totalSteps = access.getLinkCount();
            interaction.setExecutionStage(progress, totalSteps , "Building Graph", "Building Connections", false);
            
            // Do not export connections if the show connections parameter is disabled
            if (!showConnections){
                interaction.setProgress(progress, progress, "Created 0 connections", true);
                return;
            }           
            
            // Get the SVG element form the SVGGraph that will contain all connections
            final SVGObject svgLinks = SVGObjectConstant.CONTENT.findIn(svgGraph);
            
            // Itterate over all links in the graph
            for (int linkIndex = 0; linkIndex < access.getLinkCount(); linkIndex++) {
                
                // Create a container for all connections in the current link
                final SVGObject svgLink = SVGTemplateConstant.LINK.getSVGObject();
                svgLink.setID(String.format("link-%s", linkIndex));
                svgLink.setParent(svgLinks);
                
                // Get source and destination node references
                final int highIndex =  access.getLinkHighVertex(linkIndex);
                final int lowIndex = access.getLinkLowVertex(linkIndex);
                
                // Do not export this link if only selected nodes are being exported and either of the associated nodes are not selected.
                if (selectedNodesOnly && (!access.isVertexSelected(highIndex) || !access.isVertexSelected(lowIndex))){
                    continue;
                }
                
                // Determine the coordinates of the center of the nodes
                final Vector4f highCenterPosition = getVertexPosition(highIndex);
                final Vector4f lowCenterPosition = getVertexPosition(lowIndex);
                
                final float highScaleFactor = getDepthScaleFactor(getVertexWorldPosition(highIndex));
                final float lowScaleFactor = getDepthScaleFactor(getVertexWorldPosition(lowIndex));
                
                // Get the SVG angle of the connection between the two nodes
                final double highConnectionAngle = calculateConnectionAngle(highCenterPosition, lowCenterPosition);
                final double lowConnectionAngle = calculateConnectionAngle(lowCenterPosition, highCenterPosition);

                // Get the coordinates of the points where the connections intersect the node radius
                final Vector4f highCircumferencePosition = offSetPosition(highCenterPosition, getVertexScaledRadius(highIndex), highConnectionAngle);
                final Vector4f lowCircumferencePosition = offSetPosition(lowCenterPosition, getVertexScaledRadius(lowIndex), lowConnectionAngle);
                
                // Build all of the arrows in the current link 
                final SVGObject svgConnections = SVGObjectConstant.CONNECTIONS.findIn(svgLink);
                for (int connectionIndex = 0; connectionIndex < access.getLinkConnectionCount(linkIndex); connectionIndex++){

                    // Get the reference to the current connection
                    final int connection = access.getLinkConnection(linkIndex, connectionIndex);
                    
                    if (access.getConnectionVisibility(connection) == 0){
                        continue;
                    }
                    // Connection is a loop
                    if (highIndex == lowIndex) {
                        
                        buildLoopedConnection(svgConnections, highIndex, connection);
                    
                    // Connection is not a loops
                    } else {
                        
                        // Determine offset controlls for drawing multiple connections in paralell
                        final int paralellOffsetDistance = (connectionIndex / 2 + ((connectionIndex % 2 == 0) ? 0 : 1)) * 16;
                        final double paralellOffsetDirection = Math.pow(-1, connectionIndex);
                        final double paralellOffsetAngle = Math.toRadians(90) * paralellOffsetDirection;

                        //Determine the unique positions for the individual Transation/edge/link.
                        final Vector4f highPosition = offSetPosition(highCircumferencePosition, paralellOffsetDistance * highScaleFactor, highConnectionAngle - paralellOffsetAngle);
                        final Vector4f lowPosition = offSetPosition(lowCircumferencePosition, paralellOffsetDistance* lowScaleFactor, lowConnectionAngle + paralellOffsetAngle);
                        //Create the Transaction/Edge/Link SVGData
                        buildLinearConnection(svgConnections, highPosition, lowPosition, connection, highIndex, lowIndex);  
                    }
                } 
                
                //Build all of the labels in the current link 
                final SVGObject svgLabels = SVGObjectConstant.LABELS.findIn(svgLink);
                for (int connectionIndex = 0; connectionIndex < access.getLinkConnectionCount(linkIndex); connectionIndex++){
                    if (access.getConnectionVisibility(access.getLinkConnection(linkIndex, connectionIndex)) == 0){
                        continue;
                    }
                    //Determine offset controlls for drawing multiple Transactions/Edges in paralell
                    final int paralellOffsetDistance = (connectionIndex / 2 + ((connectionIndex % 2 == 0) ? 0 : 1)) * 16;
                    final double paralellOffsetDirection = Math.pow(-1, connectionIndex);
                    final double paralellOffsetAngle = Math.toRadians(90) * paralellOffsetDirection;

                    //Determine the unique positions for the individual Transation/edge/link.
                    final Vector4f highPosition = offSetPosition(highCircumferencePosition, paralellOffsetDistance * highScaleFactor, highConnectionAngle - paralellOffsetAngle);
                    final Vector4f lowPosition = offSetPosition(lowCircumferencePosition, paralellOffsetDistance * lowScaleFactor, lowConnectionAngle + paralellOffsetAngle);
                    
                    //Ignore looped transactions
                    if (highIndex != lowIndex) {
                        addConnectionLabels(svgLabels, highPosition, lowPosition, connectionIndex, access.getLinkConnectionCount(linkIndex), highIndex, lowIndex);
                    }
                }
                svgLink.setSortOrderValue((highCenterPosition.getW() + lowCenterPosition.getW())/2F);
                interaction.setProgress(progress++, totalSteps, true);
            }
            interaction.setProgress(totalSteps, totalSteps, String.format("Created %s links", progress), true);
        }              
              
        /**
         * Adds labels to connections in a link. 
         * Labels are not added for looped connections.
         * @param svgLabels
         * @param highPosition
         * @param lowPosition
         * @param connectionIndex
         * @param connectionCount 
         */
        private void addConnectionLabels(final SVGObject svgLabels, final Vector4f highPosition, final Vector4f lowPosition, final int connectionIndex, final int connectionCount, final int highIndex, final int lowIndex){
            final int totalSegments;
            if (connectionCount > 7){
                totalSegments = 8;
            } else{
                totalSegments = connectionCount + 1;
            }
            
            final int labelSegment = ((connectionIndex % 7)) + 1;
            //Labels go from low to high
            
            //Get the length of the connection
            float distance = getDistance(highPosition, lowPosition); 
            float segmentRatio = ((float) labelSegment) / totalSegments;
            float offsetDistance = distance * segmentRatio;
            
            //get ConnectionAngle
            double angle = this.calculateConnectionAngle(lowPosition, highPosition);
            Vector4f position = this.offSetPosition(lowPosition, offsetDistance, angle);
            final float highScaleFactor = getDepthScaleFactor(getVertexWorldPosition(highIndex));
            final float lowScaleFactor = getDepthScaleFactor(getVertexWorldPosition(lowIndex));
            final float scaleFactor = (highScaleFactor * segmentRatio) + (lowScaleFactor * (1 - segmentRatio));
            
            float offset = 0;
            for (int labelIndex = 0; labelIndex < access.getConnectionLabelCount(connectionIndex); labelIndex++) {
                final String labelString = access.getConnectionLabelText(connectionIndex, labelIndex);
                if (labelString != null){
                    SVGObject svgLabel = SVGTemplateConstant.LABEL.getSVGObject();
                    final float size = access.getConnectionLabelSize(labelIndex) * 64 * scaleFactor;
                    svgLabel.setPosition(position.getX(), position.getY() + offset);
                    svgLabel.setFontSize(size);
                    svgLabel.setFillColor(access.getConnectionLabelColor(labelIndex));
                    svgLabel.setBaseline("middle");
                    svgLabel.setID(String.format("label-%s-%s", connectionIndex, labelIndex));
                    svgLabel.setContent(labelString);
                    svgLabel.setParent(svgLabels);
                    offset = offset + size;
                }
            }
        }
        
        /**
         * Builds a single SVG Transaction/Edge/Link at one point.
         * Generates transactions, links and edges depending on connectionMode.
         * @param svgConnections
         * @param vertexIndex
         * @param connection 
         */
        private void buildLoopedConnection(final SVGObject svgConnections, final int vertexIndex, final int connection) {
            // Get the location of the north east corner of the node 
            final Vector4f loopCenterPosition = new Vector4f();
            Vector4f.add(loopCenterPosition, getVertexPosition(vertexIndex), new Vector4f(getVertexScaledRadius(vertexIndex), -getVertexScaledRadius(vertexIndex), 0, 0));
            
            // Create the loopedConnection
            SVGObject svgLoop = SVGTemplateConstant.CONNECTION_LOOP.getSVGObject();
            svgLoop.setID(access.getConnectionId(connection));
            svgLoop.setDimension(getDepthScaleFactor(getVertexWorldPosition(vertexIndex)) * 128, getDepthScaleFactor(getVertexWorldPosition(vertexIndex)) * 128);
            svgLoop.setPosition(loopCenterPosition.getX() - (svgLoop.getWidth()/2) , loopCenterPosition.getY() - (svgLoop.getHeight()/2));
            svgLoop.setParent(svgConnections);
            
            // Generate the SVG Loop Image
            final SVGData svgloopImage;
            final ConnectionDirection direction = access.getConnectionDirection(connection);
            switch (direction){
                //Directed connections are Transactions, Edges and links with one transaction arrow head    
                case LOW_TO_HIGH:
                    //This case uses the logic of the following case. 
                case HIGH_TO_LOW:
                    svgloopImage = DefaultIconProvider.LOOP_DIRECTED.buildSVG(access.getConnectionColor(connection).getJavaColor());
                    break;
                default:
                    svgloopImage = DefaultIconProvider.LOOP_UNDIRECTED.buildSVG(access.getConnectionColor(connection).getJavaColor());
                    break;
            }
            svgloopImage.setParent(svgLoop.toSVGData());
        }
        
        /**
         * Builds a single SVG Transaction/Edge/Link between two points.
         * Generates transactions, links and edges depending on connectionMode.
         * Transaction/Edge/Link direction is considered to define end points and arrow heads.
         * @param svgConnections
         * @param highPosition
         * @param lowPosition
         * @param connection 
         */
        private void buildLinearConnection(final SVGObject svgConnections, final Vector4f highPosition, final Vector4f lowPosition, final int connection, final int highIndex, final int lowIndex){

            //Get references to SVG Objects being built within this method 
            final SVGObject svgConnection = SVGTemplateConstant.CONNECTION_LINEAR.getSVGObject();
            final SVGObject svgArrowShaft = SVGObjectConstant.ARROW_SHAFT.findIn(svgConnection);

            final SVGObject svgArrowHeadHigh;
            final SVGObject svgArrowHeadLow;

            final float highScaleFactor = this.getDepthScaleFactor(this.getVertexWorldPosition(highIndex));
            final float lowScaleFactor = this.getDepthScaleFactor(this.getVertexWorldPosition(lowIndex));
            
            //Get the connection angles of the connection
            final Double highConnectionAngle = calculateConnectionAngle(highPosition, lowPosition);
            final Double lowConnectionAngle = calculateConnectionAngle(lowPosition, highPosition);
            
            //Get the coordinates of the potential shaft extremeties at 64px behind the arrow tip position.
            final Vector4f highPositionRecessed = offSetPosition(highPosition, 64 * highScaleFactor, highConnectionAngle);
            final Vector4f lowPositionRecessed = offSetPosition(lowPosition, 64 * lowScaleFactor, lowConnectionAngle);

            //Assign the positional values of shaft and arrow head/s based on the direction of the Transaction/Edge/Link
            final ConnectionDirection direction = access.getConnectionDirection(connection);            
                
            switch (direction){
                //Bidirectional connectsions are Links with two link arrow heads
                case BIDIRECTED:
                    buildLinearArrowShaft(svgArrowShaft, highPositionRecessed, lowPositionRecessed, highIndex, lowIndex);

                    svgArrowHeadHigh = SVGTemplateConstant.ARROW_HEAD_LINK.getSVGObject();
                    buildArrowHead(svgArrowHeadHigh, highPosition, lowConnectionAngle, highScaleFactor);
                    svgArrowHeadHigh.setParent(svgConnection);

                    svgArrowHeadLow = SVGTemplateConstant.ARROW_HEAD_LINK.getSVGObject();
                    buildArrowHead(svgArrowHeadLow, lowPosition, highConnectionAngle, lowScaleFactor);
                    svgArrowHeadLow.setParent(svgConnection);
                    break;

                //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head    
                case LOW_TO_HIGH:
                    buildLinearArrowShaft(svgArrowShaft, highPositionRecessed, lowPosition, highIndex, lowIndex);

                    svgArrowHeadHigh = SVGTemplateConstant.ARROW_HEAD_TRANSACTION.getSVGObject();
                    buildArrowHead(svgArrowHeadHigh, highPosition, lowConnectionAngle, highScaleFactor);
                    svgArrowHeadHigh.setParent(svgConnection);
                    break;

                //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head
                case HIGH_TO_LOW:
                    buildLinearArrowShaft(svgArrowShaft, highPosition, lowPositionRecessed, highIndex, lowIndex); 

                    svgArrowHeadLow = SVGTemplateConstant.ARROW_HEAD_TRANSACTION.getSVGObject();
                    buildArrowHead(svgArrowHeadLow, lowPosition, highConnectionAngle, lowScaleFactor);
                    svgArrowHeadLow.setParent(svgConnection);
                    break;

                //Undirected connections are Transactions, Edges and Links with no arrow heads.
                default:
                    buildLinearArrowShaft(svgArrowShaft, highPosition, lowPosition, highIndex, lowIndex);
                    break;
            }
            
            //Set the attributes of the connection and add it to the connections conatainer  
            final ConstellationColor color = getConnectionColor(connection);
            svgConnection.setID(String.format("Connection-%s", connection));
            svgConnection.setFillColor(color);
            svgConnection.setStrokeColor(color);
            svgConnection.setStrokeStyle(access.getConnectionLineStyle(connection));
            svgConnection.setParent(svgConnections);
        }
        
        /**
         * Manipulates an arrow head container to adjust it's position and rotation.
         * @param svgArrowHead
         * @param x
         * @param y
         * @param connectionAngle 
         */
        private void buildArrowHead(final SVGObject svgArrowHead, final Vector4f position, final double connectionAngle, final float scaleFactor) {
            float arrowHeadHeight = scaleFactor * 32;
            float arrowHeadWidth = scaleFactor * 128;
            
            svgArrowHead.setDimension(arrowHeadWidth, arrowHeadHeight);
           
            //Set arrow head svg attributes
            svgArrowHead.setPosition(position.getX() , position.getY() - arrowHeadHeight/2);
            svgArrowHead.setID(String.format("arrow-head-%s-%s", position.getX(), position.getY() - arrowHeadHeight/2 ));
            
            //Rotate the arrow head polygon around the tip to align it with the angle of the connection
            final SVGObject svgArrowHeadPolygon = SVGObjectConstant.ARROW_HEAD.findIn(svgArrowHead);
            svgArrowHeadPolygon.setTransformation(String.format("rotate(%s %s %s)", Math.toDegrees(connectionAngle), 0, 16));
        }
        
        /**
         * Manipulates an arrow shaft container to adjust it's position.
         * @param svgArrowShaft
         * @param sourcePosition
         * @param destinationPosition 
         */
        private void buildLinearArrowShaft(final SVGObject svgArrowShaft, final Vector4f sourcePosition, final Vector4f destinationPosition, final int sourceIndex, final int destinationIndex) {
            float destinationScaleFactor = this.getDepthScaleFactor(getVertexWorldPosition(destinationIndex));
            float sourceScaleFactor = this.getDepthScaleFactor(getVertexWorldPosition(sourceIndex));
            
            double sourceConnectionAngle = this.calculateConnectionAngle(sourcePosition, destinationPosition);
            double detinationConnectionAngle = this.calculateConnectionAngle(destinationPosition, sourcePosition);
            
            Vector4f p1 = this.offSetPosition(sourcePosition, 4 * sourceScaleFactor, sourceConnectionAngle + 90);
            Vector4f p2 = this.offSetPosition(sourcePosition, 4 * sourceScaleFactor, sourceConnectionAngle - 90);
            
            Vector4f p3 = this.offSetPosition(destinationPosition, 4 * destinationScaleFactor, detinationConnectionAngle + 90);
            Vector4f p4 = this.offSetPosition(destinationPosition, 4 * destinationScaleFactor, detinationConnectionAngle - 90);
            svgArrowShaft.setPoints(String.format("%s %s, %s %s, %s %s, %s %s", p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY(), p4.getX(), p4.getY() ));
        }

        /**
         * Builds the header area of the output SVG.
         * @param svgGraph The SVGObject holding all generated SVG data 
         */
        private void buildHeader(final SVGObject svgGraph){
            final ZonedDateTime date = ZonedDateTime.now();
            final String dateInfo = String.format("Exported: %s %s, %s",
                            StringUtilities.camelCase(date.getMonth().toString()), 
                            date.getDayOfMonth(), 
                            date.getYear()
                    );
            
            SVGObjectConstant.TITLE.findIn(svgGraph).setContent(graphTitle);
            SVGObjectConstant.SUBTITLE.findIn(svgGraph).setContent(dateInfo);
        }
        
        private void buildBackground(final SVGObject svgGraph){
            SVGObjectConstant.BACKGROUND.findIn(svgGraph).setFillColor(backgroundColor);
        }
        
        /**
         * Sets the dimensions for container objects within the Layout.svg template file.
         * This method is a temporary solution for setting layout dimensions semi-manually.
         * Ideally layout elements will be built from a top down approach.
         * In doing so each layout object will be able to set its own dimensions 
         * based on it's own content.
         * @param svgGraph The SVGObject holding all generated SVG data 
         */
        private void setLayoutDimensions(final SVGObject svgGraph) {
            final float contentWidth = viewPort[2];
            final float contentHeight = viewPort[3];
            
            final float fullWidth = contentWidth;// + (xPadding * 2);            
            final float fullHeight =  contentHeight;// + (bottomMargin) +(yPadding * 2);            

            SVGObject content = SVGObjectConstant.CONTENT.findIn(svgGraph);
                    
            content.setDimension(contentWidth, contentHeight);
            content.setDimensionScale("100%", "95%");
            
            svgGraph.setDimension(fullWidth, fullHeight);
            svgGraph.setDimensionScale("100%", "100%");
        }

        /**
         * Gets the normalized position of the vertex.
         * Position is normalized with respect to a predefined viewWindow 
         * with horizontal right and vertical down being positive directions.
         * Position is with respect to the center of the vertex.
         * @param vertexIndex
         * @return 
         */
        private Vector4f getVertexPosition(final int vertexIndex) {         
            return getScreenPosition(getVertexWorldPosition(vertexIndex));
        }

        /**
         * Retrieves the 3D coordinates of a vertex.
         * @param vertexIndex
         * @return 
         */
        private Vector3f getVertexWorldPosition(final int vertexIndex){

            return new Vector3f(access.getX(vertexIndex), access.getY(vertexIndex), access.getZ(vertexIndex));
        }
        
        /**
         * Translates a 3D world coordinate to a 2D world projection onto a predefined plane.
         * The returned position is normalized with respect to a predefined viewWindow 
         * with horizontal right and vertical down being positive directions.
         * @param worldPosition
         * @return 
         */
        private Vector4f getScreenPosition(final Vector3f worldPosition){
            final Vector4f screenPosition = new Vector4f();
            Graphics3DUtilities.project(worldPosition, modelViewProjectionMatrix, viewPort, screenPosition);
            //move the screen positn by a fixed amount to make it correctly align
            //dont know why this is needed
            //Vector4f.add(screenPosition, screenPosition, new Vector4f(512F, 128F, 0F, 0F));
            return screenPosition;
        }
        
        /**
         * Determines the radius of the node.
         * The scale is determined by projecting a position at the edge of the node
         * to its correlating screen position. 
         * A less than ideal solution that will not support alternate export perspectives. 
         * @param vertexIndex
         * @return 
         */
        private float getVertexScaledRadius(final int vertexIndex) {  
            
            //Get the radius value of the node
            final float radius = access.getRadius(vertexIndex);
            
            //Get the scale foactor of the node determined by its depth from the camera.
            final float depthScaleFactor = getDepthScaleFactor(this.getVertexWorldPosition(vertexIndex));
            
            return radius * depthScaleFactor * 128;         
        }
        
        /**
         * Determine the amount to scale an element based on its distance from the camera. 
         * 
         * @param vertexIndex
         * @return 
         */
        private float getDepthScaleFactor(final Vector3f worldPosition){
            
            // Get the screen position of point
            final Vector4f screenPosition = getScreenPosition(worldPosition);
            
            // Move the point in the screens upwards direction by one unit.
            Vector3f up = camera.lookAtUp;
            worldPosition.add(up);            
            
            // Convert this position to screen coordinates.
            // The is the edge of the node that is durectly above the center of the node.
            final Vector4f edgePosition = getScreenPosition(worldPosition);
            
            //get the distance between the two points
            final float screenDistance = Math.abs(edgePosition.getY() - screenPosition.getY());
                    
            // one unit in world dimensions in 128 units in svg dimensions so return this as a screen to world ratio.
            return screenDistance / 128;
        }
        
        /**
         * Calculates the angle at which a connection touches a node.
         * Return value is clockwise from a horizontal x axis with positive values to the right.
         * @param sourcePosition A position in 2d screen coordinates
         * @param destinationPosition A position in 2d screen coordinates
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
         * @param connectionIndex
         * @return 
         */
        private ConstellationColor getConnectionColor(final int connectionIndex) {
            final ConstellationColor color;
            if (access.isConnectionDimmed(connectionIndex)){
                color = VisualGraphDefaults.DEFAULT_TRANSACTION_COLOR;
            } else {
                color = access.getConnectionColor(connectionIndex);
            }
            return color;
        }
        
        /**
         * Determines the distance between two points.
         * @param a A position in 2d screen coordinates
         * @param b A position in 2d screen coordinates
         * @return 
         */
        private float getDistance(final Vector4f a, final Vector4f b) {
            float xChange = Math.abs(a.getX()-b.getX());
            float yChange = Math.abs(a.getY()-b.getY());
            return (float) Math.sqrt(Math.pow(xChange, 2) + Math.pow(yChange, 2));
        }
    }
}
