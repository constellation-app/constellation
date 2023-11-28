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
import au.gov.asd.tac.constellation.graph.interaction.animation.Animation;
import au.gov.asd.tac.constellation.graph.interaction.animation.PanAnimation;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGObjectConstants;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGTemplateConstants;
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
import au.gov.asd.tac.constellation.utilities.visual.AxisConstants;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builder that generates the the output SVG file.
 * The builder abstracts the responsibility of building an SVG from the {@link ExportToSVGPlugin}.
 * Currently the builder requires a graph to be specified using .withGraph()
 * and for the build to be initialized using .build()
 * <pre>
 * Example Usage: {@code new SVGGraph.SVGGraphBuilder().withAccess(graph).build();}
 * </pre>
 * 
 * @author capricornunicorn123
 */
public class SVGGraphBuilder {
    
    //Variables specified when the Builder class is instantiated
    private static final Logger LOGGER = Logger.getLogger(SVGGraphBuilder.class.getName());
    private final Matrix44f modelViewProjectionMatrix = new Matrix44f();
    private final VisualManager visualManager;
    private final GraphVisualAccess access;
    
    
    //Variables with default values, customisable by the builder pattern 
    private boolean selectedNodesOnly = false;
    private boolean showConnections = true;
    private boolean showNodeLabels = true;
    private boolean showConnectionLabels = true;
    private ConstellationColor backgroundColor = VisualGraphDefaults.DEFAULT_BACKGROUND_COLOR;
        
    //Variables without default values, customisable by the builder pattern 
    private GraphReadMethods readableGraph = null;
    private PluginInteraction interaction= null;
    private AxisConstants exportPerspective = null;
    private String graphTitle = null;
    
    //Variables created during preBuild
    private Camera camera;
    private int[] viewPort;
       
    /**
    * Builder that generates the the output SVG file.
    * @param svg 
    */
    public SVGGraphBuilder() {
        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
        visualManager = GraphNode.getGraphNode(currentGraph).getVisualManager();
        access = new GraphVisualAccess(currentGraph);
   }

    /**
     * Specifies the {@link GraphReadMethods} representing the current active graph.
     * @param graph used to define the bounding box of the graph
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withReadableGraph(final GraphReadMethods graph) {
        this.readableGraph = graph;
        return this;
    }

    /**
     * Specifies the {@link PluginInteraction} instance to use for updating on plugin progress.
     * @param interaction 
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder  withInteraction(final PluginInteraction interaction) {
        this.interaction = interaction;
        return this;
    }

    /**
     * Specifies the title of the graph being exported.
     * @param title 
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withTitle(final String title) {
        this.graphTitle = title;
        return this;
    }

    /**
     * Specifies the {@link ConstellationColor} of the graph background being exported.
     * @param color the color of the graph background.
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withBackground(final ConstellationColor color) {
        this.backgroundColor = color;
        return this;
    }

    /**
     * Specifies if only selected Nodes and related connections are to be included in the export.
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
     * @param showNodeLabels
     * @return 
     */
    public SVGGraphBuilder includeNodeLabels(final Boolean showNodeLabels) {
        this.showNodeLabels = showNodeLabels;
        return this;
    }

    /**
     * Controls whether Node top labels are included or excluded from the SVG output file
     * @param showConnectionLabels
     * @return 
     */
    public SVGGraphBuilder includeConnectionLabels(final Boolean showConnectionLabels) {
        this.showConnectionLabels = showConnectionLabels;
        return this;
    }

    /**
     * Specifies the Perspective to set 
     * @param exportPerspective
     * @return 
     */
    public SVGGraphBuilder fromPerspective(final AxisConstants exportPerspective) {
        this.exportPerspective = exportPerspective;
        return this;
    }

    /**
     * Builds an SVGGraphObject representing the provided graph.
     * @return SVGData
     */
    public SVGData build() throws InterruptedException {
        
        final SVGObject svgGraph = SVGTemplateConstants.LAYOUT.getSVGObject();
        
        try{
            preBuild();
        } catch (IllegalArgumentException ex){
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            return null;
        }
        
        //Build the SVG image
        buildHeader(svgGraph);
        buildNodes(svgGraph);
        buildConnections(svgGraph);            
        buildLayout(svgGraph);
        
        // Clean up the builder
        postBuild();
        
        return svgGraph.toSVGData();
        
    }       

    /**
     * Sets up the Builder control attributes and reference utilities. 
     */
    private void preBuild() throws IllegalArgumentException {
        
        // Thorw errors if attributes without default values have not been set
        if (readableGraph == null ) {
            throw new IllegalArgumentException("SVGGraphBuilder requires GraphReadMethodsto build");
        } else if (interaction == null) {
            throw new IllegalArgumentException("SVGGraphBuilder requires a PluginInteraction to build");
        } else if (graphTitle == null) {
            throw new IllegalArgumentException("SVGGraphBuilder requires a Graph Title to build");
        }
        
        // Initialise the VisualGraphAccess
        access.beginUpdate();
        access.updateInternally();
        
        //Set the camera position and add repositioning animation 
        this.camera = access.getCamera();
        final Camera oldCamera = new Camera(this.camera);
        final BoundingBox box = new BoundingBox();
        if (exportPerspective != null) {
                BoundingBoxUtilities.recalculateFromGraph(box, readableGraph, selectedNodesOnly);
                CameraUtilities.refocus(camera, new Vector3f(exportPerspective.isNegative() ? -1 : 1, 0, 0), new Vector3f(0, 1, 0), box);
                Animation.startAnimation(new PanAnimation(String.format("Reset to @s View", exportPerspective), oldCamera, camera, true));
        }
        
        // Determine the height and width of the users InteractiveGraphView pane
        final int paneHeight = visualManager.getVisualComponent().getHeight();   
        final int paneWidth = visualManager.getVisualComponent().getWidth();
        
        //Set the viewPort dimensions for 3D to 2D projection
        viewPort = new int[] {Math.round(camera.lookAtEye.getX()),  Math.round(camera.lookAtEye.getY()), paneWidth,  paneHeight};

        // Define the view frustum
        final float aspect = paneWidth / (float) paneHeight;
        final float ymax = Camera.PERSPECTIVE_NEAR * (float) Math.tan(Camera.FIELD_OF_VIEW * Math.PI / 360.0);
        final float xmax = ymax * aspect;
        final float ymin = -ymax;
        final float xmin = -xmax;
        Frustum viewFrustum = new Frustum(Camera.FIELD_OF_VIEW, aspect, xmin, xmax, ymin, ymax, Camera.PERSPECTIVE_NEAR, Camera.PERSPECTIVE_FAR);
        
        // Get the projection matrix from the view frustum
        final Matrix44f projectionMatrix = viewFrustum.getProjectionMatrix();

        // Invert the y axis cardinality to align with SVG cardinality
        final Matrix44f scaleMatrix = new Matrix44f();
        scaleMatrix.makeScalingMatrix(new Vector3f(1.0F, -1.0F, 1.0F));
        projectionMatrix.multiply(projectionMatrix, scaleMatrix);

        // Generate the ModelViewProjectionMatrix for 3D to 2D projection 
        modelViewProjectionMatrix.multiply(projectionMatrix, Graphics3DUtilities.getModelViewMatrix(camera));   
    }
    
    /**
     * Tears down the Builder design pattern reference utilities. 
     */
    private void postBuild(){
        access.endUpdate();
    }

    /**
     * Generates SVG Nodes from the graph and assigns them as children to the Content element.
     * The template file Node.svg is used to build the node.
     * @param svgGraph The SVGObject holding all generated SVG data 
     */
    private void buildNodes(final SVGObject svgGraph) throws InterruptedException {
        
        // Initate plugin report information
        int progress = 0;
        final int totalSteps = access.getVertexCount();
        interaction.setExecutionStage(progress, totalSteps, "Building Graph", "Building Nodes", true);

        // Retrieve the svg element that holds the Nodes.
        final SVGObject svgNodes = SVGObjectConstants.CONTENT.findIn(svgGraph);

        // Itterate over all vertices in the graph
        for (int vertexIndex = 0 ; vertexIndex < access.getVertexCount() ; vertexIndex++) {
            // Retrieve values of relevent vertex attributes
            final Vector4f position = getVertexPosition(vertexIndex);
            final float radius = getVertexScaledRadius(vertexIndex);
            
            // Do not export this vertex if only selected nodes are being exported and the node is not selected.
            // Do not export the node if the node is not visable
            // Do not export the node if the node is not within the field of view
            if (!inView(position, radius) || ((selectedNodesOnly && !access.isVertexSelected(vertexIndex)) || access.getVertexVisibility(vertexIndex) == 0)) {
                continue;
            }
            
            final ConstellationColor color = access.getVertexColor(vertexIndex);
            final ConstellationIcon backgroundIcon = IconManager.getIcon(access.getBackgroundIcon(vertexIndex));
            final ConstellationIcon foregroundIcon = IconManager.getIcon(access.getForegroundIcon(vertexIndex));

            // Build the SVGobject representing the Node
            final SVGObject svgNode = SVGObject.loadFromTemplate(SVGTemplateConstants.NODE);
            svgNode.setPosition(position.getX() - radius, position.getY() - radius);
            svgNode.setID(String.format("node-%s",access.getVertexId(vertexIndex)));
            svgNode.setSortOrderValue(position.getW());
            svgNode.setParent(svgNodes);
            svgNode.setDimension(radius * 2, radius * 2);

            // Add labels to the Node if required
            if (showNodeLabels) {
                final SVGObject svgTopLabel = SVGObjectConstants.TOP_LABELS.findIn(svgNode);
                buildTopLabel(vertexIndex, svgTopLabel);
                
                final SVGObject svgBottomLabel = SVGObjectConstants.BOTTOM_LABELS.findIn(svgNode);
                buildBottomLabel(vertexIndex, svgBottomLabel);
            } else {
                SVGObjectConstants.TOP_LABELS.removeFrom(svgNode);
                SVGObjectConstants.BOTTOM_LABELS.removeFrom(svgNode);
            }

            // Retrieve the svg element containing all node images.
            final SVGObject svgImages = SVGObjectConstants.NODE_IMAGES.findIn(svgNode);

            // Add background image to the node
            final SVGObject svgNodeBackground = SVGObjectConstants.BACKGROUND_IMAGE.findIn(svgNode);
            final SVGData svgBackgroundImageimage = backgroundIcon.buildSVG(color.getJavaColor());
            svgBackgroundImageimage.setParent(svgNodeBackground.toSVGData());

            // Add foreground image to the node
            final SVGObject svgNodeForeground = SVGObjectConstants.FOREGROUND_IMAGE.findIn(svgNode);
            final SVGData svgForegroundImage = foregroundIcon.buildSVG();
            svgForegroundImage.setParent(svgNodeForeground.toSVGData());

            // Add decorators to the node       
            this.buildDecorator(SVGObjectConstants.NORTH_WEST_DECORATOR.findIn(svgNode), access.getNWDecorator(vertexIndex));
            this.buildDecorator(SVGObjectConstants.NORTH_EAST_DECORATOR.findIn(svgNode), access.getNEDecorator(vertexIndex));
            this.buildDecorator(SVGObjectConstants.SOUTH_WEST_DECORATOR.findIn(svgNode), access.getSWDecorator(vertexIndex));
            this.buildDecorator(SVGObjectConstants.SOUTH_EAST_DECORATOR.findIn(svgNode), access.getSEDecorator(vertexIndex));
            
            // Add dimmed property if dimmed
            // Note, this implementation is not a precice sollution, luminocity to alpha conversion would be better
            if (access.isVertexDimmed(vertexIndex)) {
                svgImages.applyGrayScaleFilter();
            }
            
            interaction.setProgress(progress++, totalSteps, true);
        } 
        interaction.setProgress(totalSteps, totalSteps, String.format("Created %s nodes", progress), true);
    }

    /**
     * Generates decorator images for Nodes.
     * @param svgDecorator
     * @param decoratorName
     */
    private void buildDecorator(final SVGObject svgDecorator, final String decoratorName) {
        
        //Do not build a decorator if the decorator is for a Pinned attribute value of false.
        if (decoratorName != null && !"false_pinned".equals(decoratorName) && IconManager.iconExists(decoratorName)) {
            final SVGData icon = IconManager.getIcon(decoratorName).buildSVG();
            icon.setParent(svgDecorator.toSVGData());
        } else {
            svgDecorator.getParent().removeChild(svgDecorator.getID());
        }
    }

    /**
     * Constructs bottom label SVG elements for a given vertex.
     * This method considers the bottom label requirements for nodes.
     * @param vertexIndex
     * @param svgBottomLabels
     */
    private void buildBottomLabel(final int vertexIndex, final SVGObject svgBottomLabels) {    
        
        // Track the distance bewteen the top of the svgBottomLabels element and the bottom of the most recently created svgLabel
        float offset = 0;
        for (int labelIndex = 0; labelIndex < access.getBottomLabelCount(); labelIndex++) {
            final String labelString = access.getVertexBottomLabelText(vertexIndex, labelIndex);
            
            //Only add the label if the label value exists.
            if (labelString != null) {
                final SVGObject svgLabel = SVGTemplateConstants.LABEL.getSVGObject();
                final float size = access.getBottomLabelSize(labelIndex) * 64;
                svgLabel.setFontSize(size);
                svgLabel.setYPosition(offset);
                svgLabel.setFillColor(access.getBottomLabelColor(labelIndex));
                svgLabel.setBaseline("hanging");
                svgLabel.setID(String.format("bottom-label-%s", labelIndex));
                svgLabel.setContent(labelString);
                svgLabel.setParent(svgBottomLabels);
                offset = offset + size;
            }
        }
    }

    /**
     * Constructs top label SVG elements for a given vertex.
     * This method considers the bottom label requirements for nodes.
     * @param vertexIndex
     * @param svgTopLabels
     */
    private void buildTopLabel(final int vertexIndex, final SVGObject svgTopLabel) {
        
        // Track the distance bewteen the bottom of the svgTopLabels element and the top of the most recently created svgLabel
        float offset = 0;
        for (int labelIndex = 0; labelIndex < access.getTopLabelCount(); labelIndex++) {
            final String labelString = access.getVertexTopLabelText(vertexIndex, labelIndex);
            
            //Only add the label if the label value exists.
            if (labelString != null) {
                final SVGObject svgLabel = SVGTemplateConstants.LABEL.getSVGObject();
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
     * Generates representations of transactions, links and edges depending on connectionMode.
     * Labels are supported for all connection types excluding looped connections.
     * Other graph attributes including maxTransactions are considered.
     * @param svgGraph The SVGObject holding all generated SVG data 
     */
    private void buildConnections(final SVGObject svgGraph) throws InterruptedException {

        // Initate plugin report information
        int progress = 0;
        final int totalSteps = access.getLinkCount();
        interaction.setExecutionStage(progress, totalSteps , "Building Graph", "Building Connections", false);

        // Do not export connections if the show connections parameter is disabled
        if (!showConnections) {
            interaction.setProgress(progress, progress, "Created 0 connections", true);
            return;
        }           

        // Retrieve the svg element that holds the Nodes.
        final SVGObject svgLinks = SVGObjectConstants.CONTENT.findIn(svgGraph);

        // Itterate over all links in the graph
        for (int linkIndex = 0; linkIndex < access.getLinkCount(); linkIndex++) {

            // Create a svgLink for all connections in the current link
            final SVGObject svgLink = SVGTemplateConstants.LINK.getSVGObject();
            svgLink.setID(String.format("link-%s", linkIndex));
            
            // Get source and destination vertex references
            final int highIndex =  access.getLinkHighVertex(linkIndex);
            final int lowIndex = access.getLinkLowVertex(linkIndex);
            
            // Determine the coordinates of the center of the vertices
            final Vector4f highCenterPosition = getVertexPosition(highIndex);
            final Vector4f lowCenterPosition = getVertexPosition(lowIndex);     
            
            final float  highRadius = getVertexScaledRadius(highIndex);
            final float  lowRadius = getVertexScaledRadius(lowIndex);

            // Do not export this link if only selected nodes are being exported 
            // Do not export the link if either of the associated nodes are not selected.
            // Do not export the link if the node is not within the field of view
            if ((!inView(highCenterPosition, highRadius) &&  !inView(lowCenterPosition, lowRadius))||(selectedNodesOnly && (!access.isVertexSelected(highIndex) || !access.isVertexSelected(lowIndex)))) {
                continue;
            }

            // Get the SVG angle of the connection between the two vertices
            final double highConnectionAngle = calculateConnectionAngle(highCenterPosition, lowCenterPosition);
            final double lowConnectionAngle = calculateConnectionAngle(lowCenterPosition, highCenterPosition);

            // Get the coordinates of the points where the connections intersect the node radius
            final Vector4f highCircumferencePosition = offSetPosition(highCenterPosition, highRadius, highConnectionAngle);
            final Vector4f lowCircumferencePosition = offSetPosition(lowCenterPosition, lowRadius, lowConnectionAngle);

            //Determine the scale factor at each of the vertices
            final float highScaleFactor = getDepthScaleFactor(getVertexWorldPosition(highIndex));
            final float lowScaleFactor = getDepthScaleFactor(getVertexWorldPosition(lowIndex));
            
            // Build all of the connections in the current link 
            final SVGObject svgConnections = SVGObjectConstants.CONNECTIONS.findIn(svgLink);
            final SVGObject svgLabels = SVGObjectConstants.LABELS.findIn(svgLink);
            for (int connectionIndex = 0; connectionIndex < access.getLinkConnectionCount(linkIndex); connectionIndex++) {

                // Get the reference to the current connection
                final int connection = access.getLinkConnection(linkIndex, connectionIndex);
                
                // Do not export the onnection if it is invisable 
                if (access.getConnectionVisibility(connection) == 0) {
                    continue;
                }
                
                if (highIndex == lowIndex) {
                    buildLoopedConnection(svgConnections, highIndex, connection);
                    SVGObjectConstants.LABELS.removeFrom(svgLink);

                } else {

                    // Determine the paralell distance of the current connection from the center line joing the source and destination node
                    final int paralellOffsetDistance = (connectionIndex / 2 + ((connectionIndex % 2 == 0) ? 0 : 1)) * 16;
                    
                    // Determine if this concton should be posiioned above or below the center line joing the source and destination node
                    final double paralellOffsetDirection = Math.pow(-1, connectionIndex);
                    
                    // Determine the angle that defines the offset direction from the center line joing the source and destination node
                    final double paralellOffsetAngle = Math.toRadians(90) * paralellOffsetDirection;

                    // Determine the unique end positions for the individual connection.
                    final Vector4f highPosition = offSetPosition(highCircumferencePosition, paralellOffsetDistance * highScaleFactor, highConnectionAngle - paralellOffsetAngle);
                    final Vector4f lowPosition = offSetPosition(lowCircumferencePosition, paralellOffsetDistance* lowScaleFactor, lowConnectionAngle + paralellOffsetAngle);
                    
                    // Create the connection
                    buildLinearConnection(svgConnections, highPosition, lowPosition, connection, highIndex, lowIndex);
                    
                    //Create the connection labels if required
                    if (showConnectionLabels) {
                        addConnectionLabels(svgLabels, highPosition, lowPosition, connectionIndex, access.getLinkConnectionCount(linkIndex), highIndex, lowIndex);
                    } else {
                        SVGObjectConstants.LABELS.removeFrom(svgLink);
                    }
                }
            } 
            
            //Set the sort order as an average of the distance of the source and destination vertex distance from the camera.
            svgLink.setSortOrderValue((highCenterPosition.getW() + lowCenterPosition.getW())/2F);
            svgLink.setParent(svgLinks);
            interaction.setProgress(progress++, totalSteps, true);
        }
        interaction.setProgress(totalSteps, totalSteps, String.format("Created %s links", progress), true);
    }              

    /**
     * Adds labels for connections in a link. 
     * Labels are not added for looped connections.
     * @param svgLabels
     * @param highPosition
     * @param lowPosition
     * @param connectionIndex
     * @param connectionCount 
     * @param highIndex
     * @param lowIndex
     */
    private void addConnectionLabels(final SVGObject svgLabels, final Vector4f highPosition, final Vector4f lowPosition, final int connectionIndex, final int connectionCount, final int highIndex, final int lowIndex) {
        
        // Determine how many segments along the connection length are needed.
        final int totalSegments;
        if (connectionCount > 7) {
            totalSegments = 8;
        } else{
            totalSegments = connectionCount + 1;
        }

        // Determine which segment this connection label will occupy
        final int labelSegment = (connectionIndex % 7) + 1;
        final float segmentRatio = (float) labelSegment / totalSegments;
        
        // Calculate the position of the label
        final float distance = getDistance(highPosition, lowPosition); 
        final float offsetDistance = distance * segmentRatio;
        final double angle = this.calculateConnectionAngle(lowPosition, highPosition);
        final Vector4f position = this.offSetPosition(lowPosition, offsetDistance, angle);
        
        // Determine the scale factor of the label
        final float highScaleFactor = getDepthScaleFactor(getVertexWorldPosition(highIndex));
        final float lowScaleFactor = getDepthScaleFactor(getVertexWorldPosition(lowIndex));
        final float scaleFactor = (highScaleFactor * segmentRatio) + (lowScaleFactor * (1 - segmentRatio));

        // Track the distance bewteen the bottom of the svgLabels element and the top of the most recently created svgLabel
        float offset = 0;
        for (int labelIndex = 0; labelIndex < access.getConnectionLabelCount(connectionIndex); labelIndex++) {
            final String labelString = access.getConnectionLabelText(connectionIndex, labelIndex);
            
            //Only add the label if the label value exists.
            if (labelString != null) {
                final SVGObject svgLabel = SVGTemplateConstants.LABEL.getSVGObject();
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
     * Builds a single SVG connection at a point.
     * Generates transactions, links and edges depending on connectionMode.
     * @param svgConnections
     * @param vertexIndex
     * @param connection 
     */
    private void buildLoopedConnection(final SVGObject svgConnections, final int vertexIndex, final int connection) {
        
        // Get the location of the north east corner of the Node 
        final Vector4f loopCenterPosition = new Vector4f();
        Vector4f.add(loopCenterPosition, getVertexPosition(vertexIndex), new Vector4f(getVertexScaledRadius(vertexIndex), -getVertexScaledRadius(vertexIndex), 0, 0));

        // Create the loopedConnection
        final SVGObject svgLoop = SVGTemplateConstants.CONNECTION_LOOP.getSVGObject();
        svgLoop.setID(access.getConnectionId(connection));
        svgLoop.setDimension(getDepthScaleFactor(getVertexWorldPosition(vertexIndex)) * 128, getDepthScaleFactor(getVertexWorldPosition(vertexIndex)) * 128);
        svgLoop.setPosition(loopCenterPosition.getX() - (svgLoop.getWidth()/2) , loopCenterPosition.getY() - (svgLoop.getHeight()/2));
        svgLoop.setParent(svgConnections);

        // Generate the SVG Loop Image
        final SVGData svgloopImage;
        final ConnectionDirection direction = access.getConnectionDirection(connection);
        switch (direction) { 
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
     * @param highIndex
     * @param lowIndex
     */
    private void buildLinearConnection(final SVGObject svgConnections, final Vector4f highPosition, final Vector4f lowPosition, final int connection, final int highIndex, final int lowIndex) {

        // Get references to SVG Objects being built within this method 
        final SVGObject svgConnection = SVGTemplateConstants.CONNECTION_LINEAR.getSVGObject();
        final SVGObject svgArrowShaft = SVGObjectConstants.ARROW_SHAFT.findIn(svgConnection);
        final SVGObject svgArrowHeadHigh;
        final SVGObject svgArrowHeadLow;

        // Get the scale factors of the hight and low vertices
        final float highScaleFactor = this.getDepthScaleFactor(this.getVertexWorldPosition(highIndex));
        final float lowScaleFactor = this.getDepthScaleFactor(this.getVertexWorldPosition(lowIndex));

        // Get the connection angles of the arow heads
        final Double highConnectionAngle = calculateConnectionAngle(highPosition, lowPosition);
        final Double lowConnectionAngle = calculateConnectionAngle(lowPosition, highPosition);

        // Get the coordinates of the potential shaft extremeties at 64px behind the arrow tip position.
        final Vector4f highPositionRecessed = offSetPosition(highPosition, 64 * highScaleFactor, highConnectionAngle);
        final Vector4f lowPositionRecessed = offSetPosition(lowPosition, 64 * lowScaleFactor, lowConnectionAngle);

        // Assign the positional values of shaft and arrow head/s based on the direction of the Transaction/Edge/Link
        final ConnectionDirection direction = access.getConnectionDirection(connection);            
        switch (direction) {
            //Bidirectional connectsions are Links with two link arrow heads
            case BIDIRECTED:
                buildLinearArrowShaft(svgArrowShaft, highPositionRecessed, lowPositionRecessed, highIndex, lowIndex);

                svgArrowHeadHigh = SVGTemplateConstants.ARROW_HEAD_LINK.getSVGObject();
                buildArrowHead(svgArrowHeadHigh, highPosition, lowConnectionAngle, highScaleFactor);
                svgArrowHeadHigh.setParent(svgConnection);

                svgArrowHeadLow = SVGTemplateConstants.ARROW_HEAD_LINK.getSVGObject();
                buildArrowHead(svgArrowHeadLow, lowPosition, highConnectionAngle, lowScaleFactor);
                svgArrowHeadLow.setParent(svgConnection);
                break;

            //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head    
            case LOW_TO_HIGH:
                buildLinearArrowShaft(svgArrowShaft, highPositionRecessed, lowPosition, highIndex, lowIndex);

                svgArrowHeadHigh = SVGTemplateConstants.ARROW_HEAD_TRANSACTION.getSVGObject();
                buildArrowHead(svgArrowHeadHigh, highPosition, lowConnectionAngle, highScaleFactor);
                svgArrowHeadHigh.setParent(svgConnection);
                break;

            //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head
            case HIGH_TO_LOW:
                buildLinearArrowShaft(svgArrowShaft, highPosition, lowPositionRecessed, highIndex, lowIndex); 

                svgArrowHeadLow = SVGTemplateConstants.ARROW_HEAD_TRANSACTION.getSVGObject();
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
     * @param position
     * @param connectionAngle 
     * @param scaleFactor
     */
    private void buildArrowHead(final SVGObject svgArrowHead, final Vector4f position, final double connectionAngle, final float scaleFactor) {
        
        //Set arrow head dimensions
        final float arrowHeadHeight = scaleFactor * 32;
        final float arrowHeadWidth = scaleFactor * 128;
        svgArrowHead.setDimension(arrowHeadWidth, arrowHeadHeight);
        svgArrowHead.setPosition(position.getX() , position.getY() - arrowHeadHeight/2);
        
        svgArrowHead.setID(String.format("arrow-head-%s-%s", position.getX(), position.getY() - arrowHeadHeight/2 ));

        //Rotate the arrow head polygon around the tip to align it with the angle of the connection
        final SVGObject svgArrowHeadPolygon = SVGObjectConstants.ARROW_HEAD.findIn(svgArrowHead);
        svgArrowHeadPolygon.setTransformation(String.format("rotate(%s %s %s)", Math.toDegrees(connectionAngle), 0, 16));
    }

    /**
     * Manipulates an arrow shaft container to adjust it's position.
     * @param svgArrowShaft
     * @param sourcePosition
     * @param destinationPosition 
     * @param sourceIndex
     * @param destinationIndex 
     */
    private void buildLinearArrowShaft(final SVGObject svgArrowShaft, final Vector4f sourcePosition, final Vector4f destinationPosition, final int sourceIndex, final int destinationIndex) {
        
        // Get the sclae factor of the source and destination vertices
        final float destinationScaleFactor = this.getDepthScaleFactor(getVertexWorldPosition(destinationIndex));
        final float sourceScaleFactor = this.getDepthScaleFactor(getVertexWorldPosition(sourceIndex));

        // Get the connection angles of the source and destination arrow heads
        final double sourceConnectionAngle = this.calculateConnectionAngle(sourcePosition, destinationPosition);
        final double detinationConnectionAngle = this.calculateConnectionAngle(destinationPosition, sourcePosition);

        // Calculate the four points of the arrow shaft.
        final Vector4f p1 = this.offSetPosition(sourcePosition, 4 * sourceScaleFactor, sourceConnectionAngle + 90);
        final Vector4f p2 = this.offSetPosition(sourcePosition, 4 * sourceScaleFactor, sourceConnectionAngle - 90);
        final Vector4f p3 = this.offSetPosition(destinationPosition, 4 * destinationScaleFactor, detinationConnectionAngle + 90);
        final Vector4f p4 = this.offSetPosition(destinationPosition, 4 * destinationScaleFactor, detinationConnectionAngle - 90);
        
        svgArrowShaft.setPoints(String.format("%s %s, %s %s, %s %s, %s %s", p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY(), p4.getX(), p4.getY() ));
    }

    /**
     * Builds the header area of the output SVG.
     * @param svgGraph The SVGObject holding all generated SVG data 
     */
    private void buildHeader(final SVGObject svgGraph) {
        
        final ZonedDateTime date = ZonedDateTime.now();
        final String dateInfo = String.format("Exported: %s %s, %s",
                        StringUtilities.camelCase(date.getMonth().toString()), 
                        date.getDayOfMonth(), 
                        date.getYear()
                );

        SVGObjectConstants.TITLE.findIn(svgGraph).setContent(graphTitle);
        SVGObjectConstants.SUBTITLE.findIn(svgGraph).setContent(dateInfo);
    }

    /**
     * Sets the layout of the exported SVG file.
     * @param svgGraph The SVGObject holding all generated SVG data 
     */
    private void buildLayout(final SVGObject svgGraph) {
        
        // Set the background color of the output file
        SVGObjectConstants.BACKGROUND.findIn(svgGraph).setFillColor(backgroundColor);
        
        // Get the dimensions of the users window
        final float contentWidth = viewPort[2];
        final float contentHeight = viewPort[3];           
       
        SVGObject content = SVGObjectConstants.CONTENT.findIn(svgGraph);

        content.setDimension(contentWidth, contentHeight);
        content.setDimensionScale("100%", "95%");

        svgGraph.setDimension(contentWidth, contentHeight);
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
     * x, y and z values as specified in vertex attributes.
     * @param vertexIndex
     * @return 
     */
    private Vector3f getVertexWorldPosition(final int vertexIndex) {

        return new Vector3f(access.getX(vertexIndex), access.getY(vertexIndex), access.getZ(vertexIndex));
    }

    /**
     * Translates a 3D world coordinate to a 2D world projection onto a predefined plane.
     * The returned position is normalized with respect to a predefined viewWindow 
     * with horizontal right and vertical down being positive directions.
     * @param worldPosition
     * @return 
     */
    private Vector4f getScreenPosition(final Vector3f worldPosition) {  
        final Vector4f screenPosition = new Vector4f();
        final Vector4f screenReflectionPoint = new Vector4f(viewPort[2]/2, viewPort[3]/2, 0, 0);
        Graphics3DUtilities.project(worldPosition, modelViewProjectionMatrix, viewPort, screenPosition);
        if (screenPosition.getW() < 0){
            Vector4f reflectedPosition = new Vector4f();
            Vector4f.reflect(reflectedPosition, screenPosition, screenReflectionPoint);
            return new Vector4f(reflectedPosition.getX(), reflectedPosition.getY(), screenPosition.getZ(), screenPosition.getW());
        }
        return screenPosition;
    }

    /**
     * Translates the node radius from graph units to SVG units.
     * @param vertexIndex
     * @return 
     */
    private float getVertexRadius(int vertexIndex) {
        return access.getRadius(vertexIndex)* 128;
    }
        
    /**
     * Determines the radius of the node in screen units.
     * The scale is determined by projecting a position at the edge of the node
     * to its correlating screen position. 
     * @param vertexIndex
     * @return 
     */
    private float getVertexScaledRadius(final int vertexIndex) {  

        //Get the radius value of the node
        final float radius = getVertexRadius(vertexIndex);

        //Get the scale foactor of the node determined by its distance from the camera.
        final float depthScaleFactor = getDepthScaleFactor(this.getVertexWorldPosition(vertexIndex));

        return radius * depthScaleFactor;         
    }

    /**
     * Determine the amount to scale an element based on its distance from the camera. 
     * 
     * @param worldPosition
     * @return 
     */
    private float getDepthScaleFactor(final Vector3f worldPosition) {

        // Get the screen position of the worldPosition
        final Vector4f screenPosition = getScreenPosition(worldPosition);

        // Move the point in the world equivelent of the screens upwards direction by one unit.
        final Vector3f up = camera.lookAtUp;
        worldPosition.add(up);            

        // Convert this translated position to screen coordinates.
        final Vector4f edgePosition = getScreenPosition(worldPosition);

        // Get the distance between the two points
        final float screenDistance = Math.abs(edgePosition.getY() - screenPosition.getY());

        // One unit in world dimensions in 128 units in svg dimensions so return this as a screen to world ratio.
        return screenDistance / 128;
    }

    /**
     * Calculates the angle at which a connection touches a node.
     * Return value is clockwise from a horizontal x axis with positive right direction.
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
        return access.isConnectionDimmed(connectionIndex) ? VisualGraphDefaults.DEFAULT_TRANSACTION_COLOR : access.getConnectionColor(connectionIndex);
    }

    /**
     * Determines the distance between two points.
     * @param a A position in 2d screen coordinates
     * @param b A position in 2d screen coordinates
     * @return 
     */
    private float getDistance(final Vector4f a, final Vector4f b) {
        final float xChange = Math.abs(a.getX()-b.getX());
        final float yChange = Math.abs(a.getY()-b.getY());
        return (float) Math.sqrt(Math.pow(xChange, 2) + Math.pow(yChange, 2));
    }

    /**
     * Determines if a vertex is within the boundaries of the output image.
     * @param position
     * @param radius
     * @return 
     */
    private boolean inView(Vector4f position, float radius) {
        if (position.getX() + radius < 0){
            return false;
        } 
        
        if (position.getX() - radius > this.viewPort[2]){
            return false;
        } 
        
        if (position.getY() + radius < 0){
            return false;
        } 
        
        if (position.getY() - radius > this.viewPort[3]){
            return false;
        } 
        
        return position.getW() >= 0;
    }
}

