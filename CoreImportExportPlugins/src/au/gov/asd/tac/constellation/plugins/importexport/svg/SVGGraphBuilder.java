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
import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Frustum;
import au.gov.asd.tac.constellation.utilities.graphics.Mathf;
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

/**
 * Builder that generates the the output SVG file. The builder abstracts the responsibility of building an SVG from the
 * {@link ExportToSVGPlugin}. Currently the builder requires a graph to be specified using .withGraph() and for the
 * build to be initialized using .build()
 * <pre>
 * Example Usage: {@code new SVGGraph.SVGGraphBuilder().withAccess(graph).build();}
 * </pre>
 *
 * @author capricornunicorn123
 */
public class SVGGraphBuilder {

    // Variables specified when the Builder class is instantiated
    private final Matrix44f modelViewProjectionMatrix = new Matrix44f();
    private final VisualManager visualManager;
    private final GraphVisualAccess access;

    // Variables with default values, customisable by the builder pattern 
    private boolean selectedElementsOnly = false;
    private boolean showNodes = true;
    private boolean showConnections = true;
    private boolean showNodeLabels = true;
    private boolean showConnectionLabels = true;
    private boolean showBlazes = true;
    private ConstellationColor backgroundColor = VisualGraphDefaults.DEFAULT_BACKGROUND_COLOR;

    // Variables without default values, customisable by the builder pattern 
    private GraphReadMethods readableGraph = null;
    private PluginInteraction interaction = null;
    private AxisConstants exportPerspective = null;
    private String graphTitle = null;

    // Variables created during preBuild
    private Camera camera = new Camera();
    private Frustum viewFrustum;
    private int[] viewPort;

    /**
     * Builder that generates the the output SVG file.
     *
     * @param svg
     */
    public SVGGraphBuilder() {
        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
        visualManager = GraphNode.getGraphNode(currentGraph).getVisualManager();
        access = new GraphVisualAccess(currentGraph);
    }

    /**
     * Specifies the {@link GraphReadMethods} representing the current active graph.
     *
     * @param graph used to define the bounding box of the graph
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withReadableGraph(final GraphReadMethods graph) {
        this.readableGraph = graph;
        return this;
    }

    /**
     * Specifies the {@link PluginInteraction} instance to use for updating on plugin progress.
     *
     * @param interaction
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withInteraction(final PluginInteraction interaction) {
        this.interaction = interaction;
        return this;
    }

    /**
     * Specifies the title of the graph being exported.
     *
     * @param title
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withTitle(final String title) {
        this.graphTitle = title;
        return this;
    }

    /**
     * Specifies the {@link ConstellationColor} of the graph background being exported.
     *
     * @param color the color of the graph background.
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withBackground(final ConstellationColor color) {
        this.backgroundColor = color;
        return this;
    }

    /**
     * Specifies if only selected Nodes and related connections are to be included in the export.
     *
     * @param selectedElementsOnly
     * @return
     */
    public SVGGraphBuilder withSelectedElementsOnly(final boolean selectedElementsOnly) {
        this.selectedElementsOnly = selectedElementsOnly;
        return this;
    }

    /**
     * Controls whether nodes are included or excluded from the SVG output file
     *
     * @param showNodes
     * @return
     */
    public SVGGraphBuilder includeNodes(final boolean showNodes) {
        this.showNodes = showNodes;
        return this;
    }

    /**
     * Controls whether connections are included or excluded from the SVG output file
     *
     * @param showConnections
     * @return
     */
    public SVGGraphBuilder includeConnections(final boolean showConnections) {
        this.showConnections = showConnections;
        return this;
    }

    /**
     * Controls whether Node top labels are included or excluded from the SVG output file
     *
     * @param showNodeLabels
     * @return
     */
    public SVGGraphBuilder includeNodeLabels(final boolean showNodeLabels) {
        this.showNodeLabels = showNodeLabels;
        return this;
    }

    /**
     * Controls whether Node top labels are included or excluded from the SVG output file
     *
     * @param showConnectionLabels
     * @return
     */
    public SVGGraphBuilder includeConnectionLabels(final boolean showConnectionLabels) {
        this.showConnectionLabels = showConnectionLabels;
        return this;
    }

    /**
     * Controls whether Blases are included or excluded from the SVG output file
     *
     * @param showBlazes
     * @return
     */
    public SVGGraphBuilder includeBlazes(final boolean showBlazes) {
        this.showBlazes = showBlazes;
        return this;
    }

    /**
     * Specifies the Perspective to set
     *
     * @param exportPerspective
     * @return
     */
    public SVGGraphBuilder fromPerspective(final AxisConstants exportPerspective) {
        this.exportPerspective = exportPerspective;
        return this;
    }

    /**
     * Builds an SVGGraphObject representing the provided graph.
     *
     * @return SVGData
     */
    public SVGData build() throws InterruptedException, IllegalArgumentException {

        final SVGObject svgGraph = SVGTemplateConstants.LAYOUT.getSVGObject();
        try {
            preBuild();
            // Build the SVG image
            buildHeader(svgGraph);
            buildNodes(svgGraph);
            buildConnections(svgGraph);
            buildLayout(svgGraph);
        } finally {
            // Clean up the builder
            postBuild();
        }
        return svgGraph.toSVGData();
    }

    /**
     * Sets up the Builder control attributes and reference utilities.
     */
    private void preBuild() throws IllegalArgumentException {

        // Initialise the VisualGraphAccess
        access.beginUpdate();
        access.updateInternally();

        // Thorw errors if attributes without default values have not been set
        if (readableGraph == null) {
            throw new IllegalArgumentException("SVGGraphBuilder requires GraphReadMethods to build");
        } else if (interaction == null) {
            throw new IllegalArgumentException("SVGGraphBuilder requires a PluginInteraction to build");
        } else if (graphTitle == null) {
            throw new IllegalArgumentException("SVGGraphBuilder requires a Graph Title to build");
        }

        // Set the camera position and add repositioning animation 
        this.camera = access.getCamera();
        final Camera oldCamera = new Camera(this.camera);
        final BoundingBox box = new BoundingBox();
        if (exportPerspective != null) {
            BoundingBoxUtilities.recalculateFromGraph(box, readableGraph, selectedElementsOnly);
            CameraUtilities.refocus(camera, exportPerspective, box);
            Animation.startAnimation(new PanAnimation(String.format("Reset to %s View", exportPerspective), oldCamera, camera, true));
        }

        // Determine the dimensions of the users InteractiveGraphView pane
        final int paneHeight = visualManager.getVisualComponent().getHeight();
        final int paneWidth = visualManager.getVisualComponent().getWidth();
        viewPort = new int[]{Math.round(camera.lookAtEye.getX()), Math.round(camera.lookAtEye.getY()), paneWidth, paneHeight};

        // Define the view frustum in local units
        final float fieldOfView = Camera.FIELD_OF_VIEW + 2.5F; //Increase the field of view to ensure that objects near the end are rendered corretly.
        final float aspect = paneWidth / (float) paneHeight;
        final float ymax = Camera.PERSPECTIVE_NEAR * (float) Math.tan(fieldOfView * Math.PI / 360.0);
        final float xmax = ymax * aspect;
        final float ymin = -ymax;
        final float xmin = -xmax;
        this.viewFrustum = new Frustum(fieldOfView, aspect, xmin, xmax, ymin, ymax, Camera.PERSPECTIVE_NEAR, Camera.PERSPECTIVE_FAR);

        // Generate the Model View Projection Matrix
        final Matrix44f projectionMatrix = new Matrix44f();
        projectionMatrix.set(viewFrustum.getProjectionMatrix());
        final Matrix44f scaleMatrix = new Matrix44f();
        scaleMatrix.makeScalingMatrix(new Vector3f(1.0F, -1.0F, 1.0F)); //invert the Y-axis for translation from OpenGL cardinality to SVG cardinality
        projectionMatrix.multiply(projectionMatrix, scaleMatrix);
        modelViewProjectionMatrix.multiply(projectionMatrix, Graphics3DUtilities.getModelViewMatrix(camera));

        // Translate the frustum from local units to world units
        final Frame frame = new Frame(camera.lookAtEye, camera.lookAtCentre, camera.lookAtUp);
        frame.setOrigin(camera.lookAtEye); //Reposition the viewing position from the origin to the camera eye location
        camera.setObjectFrame(frame); //Store the frame in the camera for easy access in other methods of this class.
        viewFrustum.transform(camera.getObjectFrame());
    }

    /**
     * Tears down the Builder design pattern reference utilities.
     */
    private void postBuild() {
        access.endUpdate();
    }

    /**
     * Generates SVG Nodes from the graph and assigns them as children to the Content element. The template file
     * Node.svg is used to build the node.
     *
     * @param svgGraph The SVGObject holding all generated SVG data
     */
    private void buildNodes(final SVGObject svgGraph) throws InterruptedException {

        // Initate plugin report information
        int progress = 0;
        final int totalSteps = access.getVertexCount();
        interaction.setExecutionStage(progress, totalSteps, "Building Graph", "Building Nodes", true);

        // Retrieve the svg element that holds the Nodes.
        final SVGObject svgNodes = SVGObjectConstants.CONTENT.findIn(svgGraph);

        if (!showNodes) {
            interaction.setProgress(progress, progress, "Created 0 nodes", true);
            return;
        }

        // Itterate over all vertices in the graph
        for (int vertexIndex = 0; vertexIndex < access.getVertexCount(); vertexIndex++) {

            // Do not export this vertex if only selected nodes are being exported and the node is not selected.
            // Do not export the node if the node is invisible
            // Do not export the node if the node is not within the field of view
            if (!inView(vertexIndex) || (selectedElementsOnly && !access.isVertexSelected(vertexIndex)) || access.getVertexVisibility(vertexIndex) == 0) {
                continue;
            }

            // Retrieve values of relevent vertex attributes
            final Vector4f position = getVertexPosition(vertexIndex);
            final float radius = getVertexScaledRadius(vertexIndex);
            final ConstellationColor color = access.getVertexColor(vertexIndex);
            final ConstellationIcon backgroundIcon = IconManager.getIcon(access.getBackgroundIcon(vertexIndex));
            final ConstellationIcon foregroundIcon = IconManager.getIcon(access.getForegroundIcon(vertexIndex));

            // Build the SVGobject representing the Node
            final SVGObject svgNode = SVGObject.loadFromTemplate(SVGTemplateConstants.NODE);
            svgNode.setPosition(position.getX() - radius, position.getY() - radius);
            svgNode.setID(String.format("node-%s", access.getVertexId(vertexIndex)));
            svgNode.setSortOrderValue(position.getW());
            svgNode.setParent(svgNodes);
            svgNode.setDimension(radius * 2, radius * 2);

            // Add labels to the Node if required
            if (showNodeLabels) {
                buildTopLabel(vertexIndex, svgNode);
                buildBottomLabel(vertexIndex, svgNode);
            } else {
                SVGObjectConstants.TOP_LABELS.removeFrom(svgNode);
                SVGObjectConstants.BOTTOM_LABELS.removeFrom(svgNode);
            }

            // Add background image to the node
            final SVGData svgBackgroundImageimage = backgroundIcon.buildSVG(color.getJavaColor());
            svgBackgroundImageimage.setParent(SVGObjectConstants.BACKGROUND_IMAGE.findIn(svgNode));

            // Add foreground image to the node
            final SVGData svgForegroundImage = foregroundIcon.buildSVG();
            svgForegroundImage.setParent(SVGObjectConstants.FOREGROUND_IMAGE.findIn(svgNode));

            // Add decorators to the node       
            this.buildDecorator(SVGObjectConstants.NORTH_WEST_DECORATOR.findIn(svgNode), access.getNWDecorator(vertexIndex));
            this.buildDecorator(SVGObjectConstants.NORTH_EAST_DECORATOR.findIn(svgNode), access.getNEDecorator(vertexIndex));
            this.buildDecorator(SVGObjectConstants.SOUTH_WEST_DECORATOR.findIn(svgNode), access.getSWDecorator(vertexIndex));
            this.buildDecorator(SVGObjectConstants.SOUTH_EAST_DECORATOR.findIn(svgNode), access.getSEDecorator(vertexIndex));

            // Add dimmed property if dimmed
            // Note, this implementation is not a precice sollution, luminocity to alpha conversion would be better
            if (access.isVertexDimmed(vertexIndex)) {
                SVGObjectConstants.NODE_IMAGES.findIn(svgNode).applyGrayScaleFilter();
                SVGObjectConstants.BACKGROUND_IMAGE.findIn(svgNode).setOpacity(0.5F);
            }

            // Add a blaze if present
            if (showBlazes && access.isBlazed(vertexIndex)) {
                this.buildBlaze(svgGraph, vertexIndex);
            }

            interaction.setProgress(progress++, totalSteps, true);
        }
        interaction.setProgress(totalSteps, totalSteps, String.format("Created %s nodes", progress), true);
    }

    /**
     * Generates decorator images for Nodes.
     *
     * @param svgDecorator
     * @param decoratorName
     */
    private void buildDecorator(final SVGObject svgDecorator, final String decoratorName) {

        // Do not build a decorator if the decorator is for a Pinned attribute value of false.
        if (decoratorName != null && !"false_pinned".equals(decoratorName) && IconManager.iconExists(decoratorName)) {
            final SVGData icon = IconManager.getIcon(decoratorName).buildSVG();
            icon.setParent(svgDecorator);
        } else {
            svgDecorator.getParent().removeChild(svgDecorator.getID());
        }
    }

    /**
     * Constructs bottom label SVG elements for a given vertex. This method considers the bottom label requirements for
     * nodes.
     *
     * @param vertexIndex
     * @param svgBottomLabels
     */
    private void buildBottomLabel(final int vertexIndex, final SVGObject svgNode) {

        final SVGObject svgBottomLabels = SVGObjectConstants.BOTTOM_LABELS.findIn(svgNode);

        // Track the distance bewteen the top of the svgBottomLabels element and the bottom of the most recently created svgLabel
        float offset = 0;
        for (int labelIndex = 0; labelIndex < access.getBottomLabelCount(); labelIndex++) {
            final String labelString = access.getVertexBottomLabelText(vertexIndex, labelIndex);

            // Only add the label if the label value exists.
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
     * Constructs top label SVG elements for a given vertex. This method considers the bottom label requirements for
     * nodes.
     *
     * @param vertexIndex
     * @param svgTopLabels
     */
    private void buildTopLabel(final int vertexIndex, final SVGObject svgNode) {

        final SVGObject svgTopLabels = SVGObjectConstants.TOP_LABELS.findIn(svgNode);

        // Track the distance bewteen the bottom of the svgTopLabels element and the top of the most recently created svgLabel
        float offset = 0;
        for (int labelIndex = 0; labelIndex < access.getTopLabelCount(); labelIndex++) {
            final String labelString = access.getVertexTopLabelText(vertexIndex, labelIndex);

            // Only add the label if the label value exists.
            if (labelString != null) {
                final SVGObject svgLabel = SVGTemplateConstants.LABEL.getSVGObject();
                final float size = access.getTopLabelSize(labelIndex) * 64;
                svgLabel.setFontSize(size);
                svgLabel.setYPosition(offset);
                svgLabel.setFillColor(access.getTopLabelColor(labelIndex));
                svgLabel.setBaseline("after-edge");
                svgLabel.setID(String.format("top-label-%s", labelIndex));
                svgLabel.setContent(labelString);
                svgLabel.setParent(svgTopLabels);
                offset = offset - size;
            }
        }
    }

    /**
     * Builds SVG representation of a Blaze. Blaze size, angle color and opacity are supported. Blazes are exported as
     * discrete SVG content so that they can overlay all exported elements.
     *
     * @param svgGraph
     * @param vertexIndex
     */
    private void buildBlaze(final SVGObject svgGraph, final int vertexIndex) {

        // Get relevant variables
        final int blazeAngle = access.getBlazeAngle(vertexIndex);
        final float blazeSize = access.getBlazeSize();
        final float blazeWidth = 512 * blazeSize;
        final float blazeHeight = 128 * blazeSize;
        final Vector4f edgePosition = this.offsetPosition(this.getVertexPosition(vertexIndex), this.getVertexScaledRadius(vertexIndex), Math.toRadians(blazeAngle + 90D));

        // Build the blaze
        final SVGObject svgBlaze = SVGTemplateConstants.BLAZE.getSVGObject();
        svgBlaze.setID(String.format("blaze-%s", vertexIndex));
        svgBlaze.setParent(SVGObjectConstants.CONTENT.findIn(svgGraph));
        svgBlaze.setSortOrderValue(0);
        svgBlaze.setFillColor(access.getBlazeColor(vertexIndex));
        svgBlaze.setOpacity(access.getBlazeOpacity());
        svgBlaze.setDimension(blazeWidth, blazeHeight);
        svgBlaze.setPosition(edgePosition.getX(), edgePosition.getY() - blazeHeight / 2);
        SVGObjectConstants.INDICATOR.findIn(svgBlaze).setTransformation(String.format("rotate(%s %s %s)", blazeAngle - 90, 0, 16));
    }

    /**
     * Builds SVG representations of Connections between Nodes. Generates representations of transactions, links and
     * edges depending on connectionMode. Labels are supported for all connection types excluding looped connections.
     * Other graph attributes including maxTransactions are considered.
     *
     * @param svgGraph The SVGObject holding all generated SVG data
     */
    private void buildConnections(final SVGObject svgGraph) throws InterruptedException {

        // Initate plugin report information
        int progress = 0;
        final int totalSteps = access.getLinkCount();
        interaction.setExecutionStage(progress, totalSteps, "Building Graph", "Building Connections", false);

        // Do not export any connections if the show connections parameter is disabled
        if (!showConnections) {
            interaction.setProgress(progress, progress, "Created 0 connections", true);
            return;
        }

        // Retrieve the svg element that holds all Links.
        final SVGObject svgLinks = SVGObjectConstants.CONTENT.findIn(svgGraph);

        // Itterate over all links in the graph
        for (int linkIndex = 0; linkIndex < access.getLinkCount(); linkIndex++) {

            // Create a SVGObject to represent the current link
            final SVGObject svgLink = SVGTemplateConstants.LINK.getSVGObject();
            svgLink.setID(String.format("link-%s", linkIndex));

            // Get source and destination vertex references for the current link
            final int highIndex = access.getLinkHighVertex(linkIndex);
            final int lowIndex = access.getLinkLowVertex(linkIndex);

            // Determine the world references for the center of the vertices
            final Vector3f highCenterPosition = this.getVertexWorldPosition(highIndex);
            final Vector3f lowCenterPosition = this.getVertexWorldPosition(lowIndex);

            // Build all of the connections in the current link 
            final SVGObject svgConnections = SVGObjectConstants.CONNECTIONS.findIn(svgLink);
            final SVGObject svgLabels = SVGObjectConstants.LABELS.findIn(svgLink);
            for (int connectionIndex = 0; connectionIndex < access.getLinkConnectionCount(linkIndex); connectionIndex++) {

                // Get the reference to the current connection
                final int connection = access.getLinkConnection(linkIndex, connectionIndex);

                // Do not export the conection if only selected element are being exported and the connection is not selected
                // Do not export the connection if it is invisable 
                if ((selectedElementsOnly && !access.isConnectionSelected(connection)) || access.getConnectionVisibility(connection) == 0) {
                    continue;
                }

                // Build Looped Connection
                if (highIndex == lowIndex) {

                    // Get the unit vectors for translation fom the node center to the nodes north west corner
                    final Vector3f upTranslation = camera.getObjectFrame().getUpVector();
                    final Vector3f rightTranslation = camera.getObjectFrame().getXAxis();

                    // Scale the translation to the radius length          
                    upTranslation.scale(access.getRadius(highIndex));
                    rightTranslation.scale(access.getRadius(highIndex));

                    // Apply the tranlsation
                    final Vector3f loopWorldCenterPosition = Vector3f.add(highCenterPosition, upTranslation, rightTranslation);
                    final Vector4f loopScreenCenterPosition = this.getScreenPosition(loopWorldCenterPosition);
                    final float loopSize = getDepthScaleFactor(loopWorldCenterPosition) * 128;

                    // Create the loopedConnection
                    final SVGObject svgLoop = SVGTemplateConstants.CONNECTION_LOOP.getSVGObject();
                    svgLoop.setID(access.getConnectionId(connection));
                    svgLoop.setDimension(loopSize, loopSize);
                    svgLoop.setPosition(loopScreenCenterPosition.getX() - (loopSize / 2), loopScreenCenterPosition.getY() - (loopSize / 2));
                    svgLoop.setParent(svgConnections);

                    // Generate the SVG Loop Image
                    final SVGData svgloopImage;
                    final ConnectionDirection direction = access.getConnectionDirection(connection);
                    svgloopImage = switch (direction) {
                        case LOW_TO_HIGH, HIGH_TO_LOW ->
                            DefaultIconProvider.LOOP_DIRECTED.buildSVG(access.getConnectionColor(connection).getJavaColor());
                        default ->
                            DefaultIconProvider.LOOP_UNDIRECTED.buildSVG(access.getConnectionColor(connection).getJavaColor());
                    };
                    svgloopImage.setParent(svgLoop);

                    //Loop labels have not been implementd
                    SVGObjectConstants.LABELS.removeFrom(svgLink);

                    // Build Linear Connection
                } else {
                    // Get references to SVG Objects being built
                    final SVGObject svgConnection = SVGTemplateConstants.CONNECTION_LINEAR.getSVGObject();
                    final SVGObject svgArrowShaft = SVGObjectConstants.ARROW_SHAFT.findIn(svgConnection);
                    final SVGObject svgArrowHeadHigh = SVGTemplateConstants.ARROW_HEAD.getSVGObject();
                    final SVGObject svgArrowHeadLow = SVGTemplateConstants.ARROW_HEAD.getSVGObject();

                    // Deterine the direction vectors of the link from the perspective of each vertex
                    final Vector3f lowDirectionVector = Vector3f.subtract(highCenterPosition, lowCenterPosition);
                    final Vector3f highDirectionVector = Vector3f.subtract(lowCenterPosition, highCenterPosition);

                    // Get the coordinates of the points where the connection intersects the node circumferences
                    final Vector3f highCircumferencePosition = offsetPosition(highCenterPosition, access.getRadius(highIndex), highDirectionVector);
                    final Vector3f lowCircumferencePosition = offsetPosition(lowCenterPosition, access.getRadius(lowIndex), lowDirectionVector);

                    // Get the direction vector of the line parallell to the viewing plane and perpendicular to the connection
                    final Vector3f vertexTagentDirection = new Vector3f();
                    vertexTagentDirection.crossProduct(highDirectionVector, camera.getObjectFrame().getForwardVector());

                    // Determine the perpendicular offset distance of the current connection from the center line joing the source and destination node
                    final float perpendicularOffsetDistance = ((float) connectionIndex / 2 + ((connectionIndex % 2 == 0) ? 0 : 1)) * 0.15F;

                    // Determine if this conection should be positioned above or below the center line joing the source and destination node
                    final float perpendicularOffsetDirection = ((Double) Math.pow(-1, connectionIndex)).floatValue();

                    // Determine the unique world corrdinates for end positions for the individual connection.
                    final Vector3f highEndPoint = offsetPosition(highCircumferencePosition, perpendicularOffsetDistance * perpendicularOffsetDirection, vertexTagentDirection);
                    final Vector3f lowEndPoint = offsetPosition(lowCircumferencePosition, perpendicularOffsetDistance * perpendicularOffsetDirection, vertexTagentDirection);

                    // Get the world corrdinates of the points where the conection passes through the frustum
                    final Vector3f lowFrustumEntryPoint = viewFrustum.getEntryPoint(lowEndPoint, highEndPoint);
                    final Vector3f highFrustumEntryPoint = viewFrustum.getEntryPoint(highEndPoint, lowEndPoint);

                    // The connection does not pass through the view frustum
                    if (lowFrustumEntryPoint == null || highFrustumEntryPoint == null) {
                        continue;
                    }

                    // Get the world coordinates of the point where the shaft will join the arrow head.
                    final Vector3f highArowHeadConnectionPoint = offsetPosition(highEndPoint, 0.65F, highDirectionVector);
                    final Vector3f lowArowHeadConnectionPoint = offsetPosition(lowEndPoint, 0.65F, lowDirectionVector);

                    // Assign the positional values of shaft and arrow head/s based on the direction of the Transaction/Edge/Link
                    final Vector3f highArrowShaftPosition = new Vector3f(highFrustumEntryPoint);
                    final Vector3f lowArrowShaftPosition = new Vector3f(lowFrustumEntryPoint);
                    final ConnectionDirection direction = access.getConnectionDirection(connection);
                    switch (direction) {
                        //Bidirectional connections are Links with two link arrow heads
                        case BIDIRECTED -> {
                            // Generate new arrow base for diamond arrow heads
                            final Vector3f highArrowHeadBasePoint = offsetPosition(highEndPoint, 1.0F, highDirectionVector);
                            final Vector3f lowArowHeadBasePoint = offsetPosition(lowEndPoint, 1.0F, lowDirectionVector);
                            // Only build the high arrow head if the high arrow head has not been cropped
                            if (highFrustumEntryPoint.areSame(highEndPoint)) {
                                buildArrowHead(svgArrowHeadHigh, highEndPoint, highArrowHeadBasePoint, highArowHeadConnectionPoint, vertexTagentDirection);
                                svgArrowHeadHigh.setParent(svgConnection);
                                highArrowShaftPosition.set(highArowHeadConnectionPoint);
                            }
                            // Only build the low arrow head if the low arrow head has not been cropped
                            if (lowFrustumEntryPoint.areSame(lowEndPoint)) {
                                buildArrowHead(svgArrowHeadLow, lowEndPoint, lowArowHeadBasePoint, lowArowHeadConnectionPoint, vertexTagentDirection);
                                svgArrowHeadLow.setParent(svgConnection);
                                lowArrowShaftPosition.set(lowArowHeadConnectionPoint);
                            }
                        }
                        // Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head    
                        case LOW_TO_HIGH -> {
                            // Only build the high arrow head if the high arrow head has not been cropped
                            if (highFrustumEntryPoint.areSame(highEndPoint)) {
                                buildArrowHead(svgArrowHeadHigh, highEndPoint, highArowHeadConnectionPoint, highArowHeadConnectionPoint, vertexTagentDirection);
                                svgArrowHeadHigh.setParent(svgConnection);
                                highArrowShaftPosition.set(highArowHeadConnectionPoint);
                            }
                            // The high arrow head is not in view 
                        }
                        //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head
                        case HIGH_TO_LOW -> {
                            // Only build the low arrow head if the high arrow head has not been cropped
                            if (lowFrustumEntryPoint.areSame(lowEndPoint)) {
                                buildArrowHead(svgArrowHeadLow, lowEndPoint, lowArowHeadConnectionPoint, lowArowHeadConnectionPoint, vertexTagentDirection);
                                svgArrowHeadLow.setParent(svgConnection);
                                lowArrowShaftPosition.set(lowArowHeadConnectionPoint);
                            }
                            // The low arrow head is not in view 
                        }
                        // Undirected connections are Transactions, Edges and Links with no arrow heads.
                        default -> {
                            //Do Nothing
                        }
                    }

                    buildLinearArrowShaft(svgArrowShaft, highArrowShaftPosition, lowArrowShaftPosition, vertexTagentDirection);

                    // Set the attributes of the connection and add it to the connections conatainer  
                    final ConstellationColor color = getConnectionColor(connection);
                    svgConnection.setID(String.format("Connection-%s", connection));
                    svgConnection.setFillColor(color);
                    svgConnection.setStrokeColor(color);
                    svgConnection.setStrokeStyle(access.getConnectionLineStyle(connection));
                    svgConnection.setParent(svgConnections);

                    // Create the connection labels if required
                    if (showConnectionLabels) {
                        addConnectionLabels(svgLabels, highEndPoint, lowEndPoint, connectionIndex, access.getLinkConnectionCount(linkIndex), highIndex, lowIndex);
                    } else {
                        SVGObjectConstants.LABELS.removeFrom(svgLink);
                    }
                }
            }

            // Set the sort order as an average of the distance of the source and destination vertex distance from the camera.
            svgLink.setSortOrderValue((this.getScreenPosition(highCenterPosition).getW() + this.getScreenPosition(lowCenterPosition).getW()) / 2F);
            svgLink.setParent(svgLinks);
            interaction.setProgress(progress++, totalSteps, true);
        }
        interaction.setProgress(totalSteps, totalSteps, String.format("Created %s links", progress), true);
    }

    /**
     * Adds labels for connections in a link. Labels are not added for looped connections.
     *
     * @param svgLabels
     * @param highPosition
     * @param lowPosition
     * @param connectionIndex
     * @param connectionCount
     * @param highIndex
     * @param lowIndex
     */
    private void addConnectionLabels(final SVGObject svgLabels, final Vector3f highPosition, final Vector3f lowPosition, final int connectionIndex, final int connectionCount, final int highIndex, final int lowIndex) {

        // Determine how many segments along the connection length are needed.
        final int totalSegments;
        if (connectionCount > 7) {
            totalSegments = 8;
        } else {
            totalSegments = connectionCount + 1;
        }

        // Determine which segment this connection label will occupy
        final int labelSegment = (connectionIndex % 7) + 1;
        final float segmentRatio = (float) labelSegment / totalSegments;

        // Calculate the position of the label
        final float offsetDistance = Mathf.distance(highPosition, lowPosition) * segmentRatio;
        final Vector3f angle = Vector3f.subtract(highPosition, lowPosition);
        final Vector3f worldPosition = this.offsetPosition(lowPosition, offsetDistance, angle);

        // Only procede if the label is in view
        if (viewFrustum.inView(worldPosition, 0)) {

            // Determine the scale factor of the label
            final float scaleFactor = getDepthScaleFactor(worldPosition);
            final Vector4f screenPosition = getScreenPosition(worldPosition);

            // Track the distance bewteen the bottom of the svgLabels element and the top of the most recently created svgLabel
            float offset = 0;
            for (int labelIndex = 0; labelIndex < access.getConnectionLabelCount(connectionIndex); labelIndex++) {
                final String labelString = access.getConnectionLabelText(connectionIndex, labelIndex);

                // Only add the label if the label value exists.
                if (labelString != null) {
                    final SVGObject svgLabel = SVGTemplateConstants.LABEL.getSVGObject();
                    final float size = access.getConnectionLabelSize(labelIndex) * 64 * scaleFactor;
                    svgLabel.setPosition(screenPosition.getX(), screenPosition.getY() + offset);
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
    }

    /**
     * Manipulates an arrow head container to adjust it's position and rotation.
     *
     * @param svgArrowHead
     * @param arrowPointPosition
     * @param arrowBasePosition
     * @param shaftEndPosition
     * @param perpendicularDirection
     */
    private void buildArrowHead(final SVGObject svgArrowHead, final Vector3f arrowPointPosition, final Vector3f arrowBasePosition, final Vector3f shaftEndPosition, final Vector3f perpendicularDirection) {

        // Calculate the four points of the arrow head.
        final Vector4f point = this.getScreenPosition(arrowPointPosition);
        final Vector4f base = this.getScreenPosition(arrowBasePosition);
        final Vector4f upperEdge = this.getScreenPosition(this.offsetPosition(shaftEndPosition, -0.15F, perpendicularDirection));
        final Vector4f lowerEdge = this.getScreenPosition(this.offsetPosition(shaftEndPosition, 0.15F, perpendicularDirection));

        svgArrowHead.setID(String.format("arrow-head-%s-%s", point.getX(), point.getY()));

        SVGObjectConstants.ARROW_HEAD.findIn(svgArrowHead).setPoints(point, upperEdge, base, lowerEdge);
    }

    /**
     * Manipulates an arrow shaft container to adjust it's position.
     *
     * @param svgArrowShaft
     * @param sourcePosition
     * @param destinationPosition
     * @param sourceIndex
     * @param destinationIndex
     */
    private void buildLinearArrowShaft(final SVGObject svgArrowShaft, final Vector3f sourcePosition, final Vector3f destinationPosition, final Vector3f perpendicularDirection) {

        // Calculate the four points of the arrow shaft.
        final Vector4f p1 = this.getScreenPosition(this.offsetPosition(sourcePosition, 0.03F, perpendicularDirection));
        final Vector4f p2 = this.getScreenPosition(this.offsetPosition(sourcePosition, -0.03F, perpendicularDirection));
        final Vector4f p3 = this.getScreenPosition(this.offsetPosition(destinationPosition, -0.03F, perpendicularDirection));
        final Vector4f p4 = this.getScreenPosition(this.offsetPosition(destinationPosition, 0.03F, perpendicularDirection));

        svgArrowShaft.setPoints(p1, p2, p3, p4);
    }

    /**
     * Builds the header area of the output SVG.
     *
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
     *
     * @param svgGraph The SVGObject holding all generated SVG data
     */
    private void buildLayout(final SVGObject svgGraph) {

        // Set the background color of the output file
        SVGObjectConstants.BACKGROUND.findIn(svgGraph).setFillColor(backgroundColor);

        // Get the dimensions of the users window
        final float viewPortWidth = viewPort[2];
        final float viewPortHeight = viewPort[3];

        // Trim the content window to reflect the users view window.
        // This is to counteract the increas to the field of view to ensure elements near the edge were drawn correctly
        final float widthTrimValue = viewPortWidth * 0.07F;
        final float heightTrimValue = viewPortHeight * 0.07F;
        final float contentWidth = viewPortWidth - widthTrimValue;
        final float contentHeight = viewPortHeight - heightTrimValue;

        SVGObjectConstants.CONTENT.findIn(svgGraph).setViewBox(widthTrimValue / 2, heightTrimValue / 2, contentWidth, contentHeight);

        svgGraph.setDimension(contentWidth, contentHeight + (contentHeight * .05F));
    }

    /**
     * Gets the normalized position of the vertex. Position is normalized with respect to a predefined viewWindow with
     * horizontal right and vertical down being positive directions. Position is with respect to the center of the
     * vertex.
     *
     * @param vertexIndex
     * @return
     */
    private Vector4f getVertexPosition(final int vertexIndex) {
        return getScreenPosition(getVertexWorldPosition(vertexIndex));
    }

    /**
     * Retrieves the 3D coordinates of a vertex. x, y and z values as specified in vertex attributes.
     *
     * @param vertexIndex
     * @return
     */
    private Vector3f getVertexWorldPosition(final int vertexIndex) {
        return new Vector3f(access.getX(vertexIndex), access.getY(vertexIndex), access.getZ(vertexIndex));
    }

    /**
     * Translates a 3D world coordinate to a 2D world projection onto a predefined plane. The returned position is
     * normalized with respect to a predefined viewWindow with horizontal right and vertical down being positive
     * directions.
     *
     * @param worldPosition
     * @return
     */
    private Vector4f getScreenPosition(final Vector3f worldPosition) {
        final Vector4f screenPosition = new Vector4f();
        Graphics3DUtilities.project(worldPosition, modelViewProjectionMatrix, viewPort, screenPosition);
        return screenPosition;
    }

    /**
     * Translates the node radius from graph units to SVG units.
     *
     * @param vertexIndex
     * @return
     */
    private float getVertexScreenRadius(final int vertexIndex) {
        return access.getRadius(vertexIndex) * 128;
    }

    /**
     * Determines the radius of the node in screen units. The scale is determined by projecting a position at the edge
     * of the node to its correlating screen position.
     *
     * @param vertexIndex
     * @return
     */
    private float getVertexScaledRadius(final int vertexIndex) {

        //Get the radius value of the node
        final float radius = getVertexScreenRadius(vertexIndex);

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
        final Vector3f up = new Vector3f(camera.lookAtUp);
        up.add(worldPosition);

        // Convert this translated position to screen coordinates.
        final Vector4f edgePosition = getScreenPosition(up);

        // Get the distance between the two points
        final float screenDistance = Math.abs(edgePosition.getY() - screenPosition.getY());

        // One unit in world dimensions in 128 units in svg dimensions so return this as a screen to world ratio.
        return screenDistance / 128;
    }

    /**
     * Calculates the coordinates of a position located a fixed distance and angle from an origin. Calculations are made
     * in 2D screen units.
     *
     * @param origin
     * @param distance
     * @param direction
     * @return
     */
    private Vector4f offsetPosition(final Vector4f origin, final float distance, final double direction) {
        final float x = (float) (origin.getX() - (distance * Math.cos(direction)));
        final float y = (float) (origin.getY() - (distance * Math.sin(direction)));
        return new Vector4f(x, y, origin.getZ(), origin.getW());
    }

    /**
     * Calculates the coordinates of a position located a fixed distance and angle from an origin. calculations are made
     * in 3D world units.
     *
     * @param origin
     * @param distance
     * @param direction
     * @return
     */
    private Vector3f offsetPosition(final Vector3f origin, final float distance, final Vector3f direction) {
        final Vector3f offsetVector = new Vector3f(direction);

        // Ensute the direction vector is normalised
        offsetVector.normalize();

        // Scale the direction by the distance.      
        offsetVector.scale(distance);

        return Vector3f.add(origin, offsetVector);
    }

    /**
     * Determines the color of a connection. Handles connection dimming and multiple Transaction color values for Edges
     * and Links
     *
     * @param connectionIndex
     * @return
     */
    private ConstellationColor getConnectionColor(final int connectionIndex) {
        return access.isConnectionDimmed(connectionIndex) ? VisualGraphDefaults.DEFAULT_TRANSACTION_COLOR : access.getConnectionColor(connectionIndex);
    }

    /**
     * Determines if a vertex is within the boundaries of the view frustum.
     *
     * @param vertexIndex
     * @return
     */
    private boolean inView(final int vertexIndex) {
        return viewFrustum.inView(getVertexWorldPosition(vertexIndex), access.getRadius(vertexIndex));
    }
}
